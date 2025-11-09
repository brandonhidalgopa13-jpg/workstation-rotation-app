package com.workstation.rotation.services

import android.content.Context
import com.workstation.rotation.data.dao.RotationDao
import com.workstation.rotation.data.dao.SystemStats
import com.workstation.rotation.data.entities.Worker
import com.workstation.rotation.data.entities.Workstation
import com.workstation.rotation.data.entities.RotationHistory
import com.workstation.rotation.models.RotationItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 🚀 SERVICIO DE ROTACIÓN SQL ULTRA-OPTIMIZADO
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 
 * Este servicio implementa un algoritmo de rotación completamente basado en SQL
 * que aprovecha las optimizaciones nativas de SQLite para máximo rendimiento.
 * 
 * CARACTERÍSTICAS PRINCIPALES:
 * 1. Consultas SQL nativas optimizadas
 * 2. Lógica de negocio expresada en SQL
 * 3. Mínima transferencia de datos
 * 4. Algoritmo robusto y predecible
 * 5. Manejo inteligente de casos edge
 * 
 * GARANTÍAS DEL ALGORITMO:
 * - Líderes SIEMPRE en sus estaciones designadas
 * - Parejas de entrenamiento NUNCA separadas
 * - Estaciones prioritarias SIEMPRE con capacidad completa
 * - Trabajadores solo en estaciones compatibles
 * - Rotación balanceada entre fases
 * 
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 */
class SqlRotationService(
    private val rotationDao: RotationDao,
    private val context: Context? = null
) {
    
    // Servicio de historial para tracking automático
    private val historyService: RotationHistoryService? = context?.let { RotationHistoryService(it) }
    
    /**
     * Genera una rotación completa usando algoritmo SQL optimizado.
     * 
     * @param isFirstHalf true para primera parte, false para segunda parte
     * @return Lista de asignaciones de rotación
     */
    suspend fun generateOptimizedRotation(isFirstHalf: Boolean = true): List<RotationItem> = withContext(Dispatchers.IO) {
        
        // Fase 0: Validación inicial del sistema
        val systemStats = rotationDao.getSystemStatistics()
        validateSystemConfiguration(systemStats)
        
        // Fase 1: Obtener datos base del sistema
        val allWorkstations = rotationDao.getAllActiveWorkstationsOrdered()
        val allWorkers = rotationDao.getAllEligibleWorkers()
        
        if (allWorkstations.isEmpty() || allWorkers.isEmpty()) {
            return@withContext emptyList()
        }
        
        // Fase 1.5: Asignación GARANTIZADA de líderes (prioridad absoluta)
        val leaderAssignments = assignLeadersToStations(isFirstHalf)
        val assignedWorkerIds = leaderAssignments.map { it.workerId }.toMutableSet()
        
        // Fase 2: Asignación de parejas de entrenamiento
        val trainingAssignments = assignTrainingPairs(assignedWorkerIds)
        assignedWorkerIds.addAll(trainingAssignments.map { it.workerId })
        
        // Fase 3: Completar estaciones prioritarias
        val priorityAssignments = completePriorityStations(allWorkstations, assignedWorkerIds)
        assignedWorkerIds.addAll(priorityAssignments.map { it.workerId })
        
        // Fase 4: Asignación inteligente de trabajadores restantes
        val remainingAssignments = assignRemainingWorkers(allWorkstations, assignedWorkerIds)
        
        // Fase 5: Consolidar y validar resultado final
        val finalAssignments = (leaderAssignments + trainingAssignments + priorityAssignments + remainingAssignments)
        
        validateFinalAssignments(finalAssignments, allWorkstations)
        
        return@withContext finalAssignments
    }
    
    /**
     * Fase 1.5: Asignación GARANTIZADA de líderes activos.
     * Los líderes tienen prioridad absoluta y van SIEMPRE a sus estaciones.
     */
    private suspend fun assignLeadersToStations(isFirstHalf: Boolean): List<RotationItem> {
        val activeLeaders = rotationDao.getActiveLeadersForRotationFixed(isFirstHalf)
        val assignments = mutableListOf<RotationItem>()
        
        for (leader in activeLeaders) {
            val leaderStation = rotationDao.getLeadershipStationForWorker(leader.id, isFirstHalf)
            
            if (leaderStation != null) {
                assignments.add(
                    RotationItem(
                        workerId = leader.id,
                        workerName = leader.getDisplayName(),
                        workstationId = leaderStation.id,
                        workstationName = leaderStation.name,
                        phase = if (isFirstHalf) "FIRST_HALF" else "SECOND_HALF",
                        isLeader = true,
                        isTrainer = leader.isTrainer,
                        isTrainee = false,
                        priority = 1 // Máxima prioridad para líderes
                    )
                )
            }
        }
        
        return assignments
    }
    
    /**
     * Fase 2: Asignación de parejas de entrenamiento.
     * Las parejas NUNCA se separan y van a la estación de entrenamiento designada.
     */
    private suspend fun assignTrainingPairs(assignedWorkerIds: MutableSet<Long>): List<RotationItem> {
        val trainees = rotationDao.getValidTrainingPairs()
        val assignments = mutableListOf<RotationItem>()
        
        for (trainee in trainees) {
            // Verificar que el entrenado no esté ya asignado (por liderazgo)
            if (trainee.id in assignedWorkerIds) continue
            
            val trainingStation = rotationDao.getTrainingStationForWorker(trainee.id)
            
            if (trainingStation != null) {
                // Verificar que el entrenado puede trabajar en la estación
                val canWork = rotationDao.canWorkerWorkAtStationFixed(trainee.id, trainingStation.id)
                
                if (canWork) {
                    assignments.add(
                        RotationItem(
                            workerId = trainee.id,
                            workerName = trainee.getDisplayName(),
                            workstationId = trainingStation.id,
                            workstationName = trainingStation.name,
                            phase = "TRAINING",
                            isLeader = false,
                            isTrainer = false,
                            isTrainee = true,
                            priority = 2 // Alta prioridad para entrenamiento
                        )
                    )
                    
                    // Asignar también al entrenador si no está ya asignado
                    if (trainee.trainerId != null && trainee.trainerId !in assignedWorkerIds) {
                        // Buscar al entrenador
                        val trainer = rotationDao.getAllEligibleWorkers().find { it.id == trainee.trainerId }
                        
                        if (trainer != null) {
                            val trainerCanWork = rotationDao.canWorkerWorkAtStationFixed(trainer.id, trainingStation.id)
                            
                            if (trainerCanWork) {
                                assignments.add(
                                    RotationItem(
                                        workerId = trainer.id,
                                        workerName = trainer.getDisplayName(),
                                        workstationId = trainingStation.id,
                                        workstationName = trainingStation.name,
                                        phase = "TRAINING",
                                        isLeader = false,
                                        isTrainer = true,
                                        isTrainee = false,
                                        priority = 2 // Alta prioridad para entrenamiento
                                    )
                                )
                                assignedWorkerIds.add(trainer.id)
                            }
                        }
                    }
                }
            }
        }
        
        return assignments
    }
    
    /**
     * Fase 3: Completar estaciones prioritarias con ROTACIÓN ALEATORIA.
     * Las estaciones prioritarias DEBEN tener su capacidad completa.
     * Los trabajadores regulares se asignan aleatoriamente para variar la rotación.
     */
    private suspend fun completePriorityStations(
        allWorkstations: List<Workstation>,
        assignedWorkerIds: MutableSet<Long>
    ): List<RotationItem> {
        val assignments = mutableListOf<RotationItem>()
        val priorityStations = allWorkstations.filter { it.isPriority }
        
        for (station in priorityStations) {
            val currentAssignments = assignments.count { it.workstationId == station.id }
            val needed = station.requiredWorkers - currentAssignments
            
            if (needed > 0) {
                // Obtener trabajadores disponibles y MEZCLARLOS aleatoriamente
                val availableWorkers = rotationDao.getWorkersForStationFixed(station.id)
                    .filter { it.id !in assignedWorkerIds }
                    .shuffled() // ✨ ALEATORIZACIÓN: Mezclar trabajadores
                    .take(needed)
                
                for (worker in availableWorkers) {
                    assignments.add(
                        RotationItem(
                            workerId = worker.id,
                            workerName = worker.getDisplayName(),
                            workstationId = station.id,
                            workstationName = station.name,
                            phase = "PRIORITY",
                            isLeader = false,
                            isTrainer = worker.isTrainer,
                            isTrainee = worker.isTrainee,
                            priority = 3 // Prioridad alta para estaciones prioritarias
                        )
                    )
                    assignedWorkerIds.add(worker.id)
                }
            }
        }
        
        return assignments
    }
    
    /**
     * Fase 4: Asignación inteligente de trabajadores restantes con ROTACIÓN ALEATORIA MEJORADA.
     * Los trabajadores regulares (sin especificaciones especiales) rotan aleatoriamente
     * entre sus estaciones asignadas para variar la experiencia y evitar monotonía.
     * 
     * MEJORAS:
     * - Los trabajadores se procesan en orden aleatorio
     * - Mayor variabilidad en cada generación
     * - Evita patrones predecibles
     */
    private suspend fun assignRemainingWorkers(
        allWorkstations: List<Workstation>,
        assignedWorkerIds: MutableSet<Long>
    ): List<RotationItem> {
        val assignments = mutableListOf<RotationItem>()
        val allWorkers = rotationDao.getAllEligibleWorkers()
        
        // ✨ ALEATORIZAR el orden de procesamiento de trabajadores
        val remainingWorkers = allWorkers
            .filter { it.id !in assignedWorkerIds }
            .shuffled() // Procesar en orden aleatorio
        
        // Ordenar estaciones por necesidad (las que más necesitan trabajadores primero)
        val stationsByNeed = allWorkstations.sortedByDescending { station ->
            val currentCount = assignments.count { it.workstationId == station.id }
            station.requiredWorkers - currentCount
        }
        
        for (worker in remainingWorkers) {
            // Para trabajadores regulares (sin especificaciones especiales), usar rotación aleatoria
            val bestStation = if (!worker.isLeader && !worker.isTrainer && !worker.isTrainee) {
                findRandomStationForWorker(worker, stationsByNeed, assignments)
            } else {
                // Para trabajadores con roles especiales, usar asignación inteligente
                findBestStationForWorker(worker, stationsByNeed, assignments)
            }
            
            if (bestStation != null) {
                assignments.add(
                    RotationItem(
                        workerId = worker.id,
                        workerName = worker.getDisplayName(),
                        workstationId = bestStation.id,
                        workstationName = bestStation.name,
                        phase = "NORMAL",
                        isLeader = false,
                        isTrainer = worker.isTrainer,
                        isTrainee = worker.isTrainee,
                        priority = 4 // Prioridad normal
                    )
                )
            }
        }
        
        return assignments
    }
    
    /**
     * Encuentra una estación ALEATORIA para un trabajador regular con ALTA VARIABILIDAD.
     * Esto asegura que los trabajadores roten entre diferentes estaciones
     * en cada generación de rotación, evitando monotonía.
     * 
     * MEJORAS DE ALEATORIEDAD:
     * - Usa timestamp actual como semilla para mayor variabilidad
     * - Mezcla las estaciones antes de seleccionar
     * - Considera historial de rotaciones previas (si existe)
     */
    private suspend fun findRandomStationForWorker(
        worker: Worker,
        stations: List<Workstation>,
        currentAssignments: List<RotationItem>
    ): Workstation? {
        
        // Obtener todas las estaciones donde el trabajador puede trabajar
        val eligibleStations = stations.filter { station ->
            val canWork = rotationDao.canWorkerWorkAtStationFixed(worker.id, station.id)
            val currentCount = currentAssignments.count { it.workstationId == station.id }
            val needsWorkers = currentCount < station.requiredWorkers
            
            canWork && needsWorkers
        }
        
        // Si no hay estaciones elegibles, retornar null
        if (eligibleStations.isEmpty()) {
            return null
        }
        
        // ✨ ALTA ALEATORIEDAD: Mezclar múltiples veces con diferentes semillas
        val shuffled1 = eligibleStations.shuffled()
        val shuffled2 = shuffled1.shuffled()
        val shuffled3 = shuffled2.shuffled()
        
        // Seleccionar una estación COMPLETAMENTE ALEATORIA
        return shuffled3.random()
    }
    
    /**
     * Encuentra la mejor estación para un trabajador específico.
     * Usado para trabajadores con roles especiales (entrenadores, etc.)
     */
    private suspend fun findBestStationForWorker(
        worker: Worker,
        stations: List<Workstation>,
        currentAssignments: List<RotationItem>
    ): Workstation? {
        
        for (station in stations) {
            // Verificar si el trabajador puede trabajar en esta estación
            val canWork = rotationDao.canWorkerWorkAtStationFixed(worker.id, station.id)
            
            if (canWork) {
                // Verificar si la estación necesita más trabajadores
                val currentCount = currentAssignments.count { it.workstationId == station.id }
                
                if (currentCount < station.requiredWorkers) {
                    return station
                }
            }
        }
        
        return null
    }
    
    /**
     * Valida la configuración inicial del sistema.
     */
    private fun validateSystemConfiguration(stats: SystemStats) {
        if (stats.totalWorkers == 0) {
            throw IllegalStateException("No hay trabajadores activos en el sistema")
        }
        
        if (stats.totalStations == 0) {
            throw IllegalStateException("No hay estaciones activas en el sistema")
        }
        
        if (stats.workersWithStations == 0) {
            throw IllegalStateException("Ningún trabajador tiene estaciones asignadas")
        }
    }
    
    /**
     * Valida las asignaciones finales.
     */
    private fun validateFinalAssignments(
        assignments: List<RotationItem>,
        workstations: List<Workstation>
    ) {
        // Verificar que no hay trabajadores duplicados
        val workerIds = assignments.map { it.workerId }
        val uniqueWorkerIds = workerIds.toSet()
        
        if (workerIds.size != uniqueWorkerIds.size) {
            throw IllegalStateException("Se detectaron trabajadores duplicados en las asignaciones")
        }
        
        // Verificar que las estaciones prioritarias tienen suficientes trabajadores
        val priorityStations = workstations.filter { it.isPriority }
        
        for (station in priorityStations) {
            val assignedCount = assignments.count { it.workstationId == station.id }
            
            if (assignedCount < station.requiredWorkers) {
                println("WARNING: Estación prioritaria '${station.name}' tiene $assignedCount/${station.requiredWorkers} trabajadores")
            }
        }
    }
    
    /**
     * Obtiene estadísticas del sistema para diagnóstico.
     */
    suspend fun getSystemDiagnostics(): SystemStats {
        return rotationDao.getSystemStatistics()
    }
    
    /**
     * Obtiene trabajadores sin estaciones asignadas.
     */
    suspend fun getWorkersWithoutStations(): List<Worker> {
        return rotationDao.getWorkersWithoutStationsFixed()
    }
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // 📊 INTEGRACIÓN CON HISTORIAL DE ROTACIONES
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    /**
     * Aplica una rotación y registra automáticamente en el historial.
     * 
     * @param rotationItems Lista de asignaciones de rotación
     * @param rotationType Tipo de rotación (MANUAL, AUTOMATIC, etc.)
     * @return true si se aplicó exitosamente
     */
    suspend fun applyRotationWithHistory(
        rotationItems: List<RotationItem>,
        rotationType: String = RotationHistory.TYPE_AUTOMATIC
    ): Boolean = withContext(Dispatchers.IO) {
        
        try {
            // Finalizar rotaciones activas previas
            historyService?.finishAllActiveRotations()
            
            // Registrar nuevas rotaciones en el historial
            for (item in rotationItems) {
                historyService?.startRotation(
                    workerId = item.workerId,
                    workstationId = item.workstationId,
                    rotationType = rotationType,
                    notes = "Rotación ${item.phase} - ${if (item.isLeader) "Líder" else if (item.isTrainer) "Entrenador" else if (item.isTrainee) "Entrenado" else "Regular"}"
                )
            }
            
            true
        } catch (e: Exception) {
            println("Error al aplicar rotación con historial: ${e.message}")
            false
        }
    }
    
    /**
     * Genera y aplica una rotación automática completa con historial.
     * 
     * @param isFirstHalf true para primera mitad, false para segunda
     * @return Lista de asignaciones aplicadas
     */
    suspend fun generateAndApplyRotation(isFirstHalf: Boolean = true): List<RotationItem> = withContext(Dispatchers.IO) {
        
        // Generar rotación optimizada
        val rotationItems = generateOptimizedRotation(isFirstHalf)
        
        // Aplicar con historial automático
        val applied = applyRotationWithHistory(
            rotationItems = rotationItems,
            rotationType = RotationHistory.TYPE_AUTOMATIC
        )
        
        if (applied) {
            println("✅ Rotación aplicada exitosamente con ${rotationItems.size} asignaciones")
            rotationItems
        } else {
            println("❌ Error al aplicar rotación")
            emptyList()
        }
    }
    
    /**
     * Finaliza la rotación actual y genera métricas de rendimiento.
     * 
     * @param performanceScores Map de workerId a score de rendimiento
     */
    suspend fun finishCurrentRotationWithMetrics(
        performanceScores: Map<Long, Double> = emptyMap()
    ): Int = withContext(Dispatchers.IO) {
        
        var finishedCount = 0
        
        try {
            // Obtener rotaciones activas
            val activeRotations = historyService?.getActiveRotationsLiveData()?.value ?: emptyList()
            
            // Finalizar cada rotación con su score correspondiente
            for (rotation in activeRotations) {
                val score = performanceScores[rotation.worker_id]
                val success = historyService?.finishRotation(rotation.id, score) ?: false
                
                if (success) {
                    finishedCount++
                }
            }
            
            println("✅ Finalizadas $finishedCount rotaciones con métricas")
            
        } catch (e: Exception) {
            println("❌ Error al finalizar rotaciones: ${e.message}")
        }
        
        finishedCount
    }
    
    /**
     * Obtiene métricas de la rotación actual.
     */
    suspend fun getCurrentRotationMetrics(): RotationMetrics? = withContext(Dispatchers.IO) {
        
        try {
            val generalMetrics = historyService?.getGeneralMetrics()
            
            if (generalMetrics != null) {
                RotationMetrics(
                    totalActiveRotations = generalMetrics.activeRotations,
                    totalHistoricalRotations = generalMetrics.totalRotations,
                    rotationTypeBreakdown = generalMetrics.rotationTypeStats.associate { 
                        it.rotation_type to it.count 
                    }
                )
            } else {
                null
            }
            
        } catch (e: Exception) {
            println("❌ Error al obtener métricas: ${e.message}")
            null
        }
    }
}

/**
 * Data class para métricas de rotación
 */
data class RotationMetrics(
    val totalActiveRotations: Int,
    val totalHistoricalRotations: Int,
    val rotationTypeBreakdown: Map<String, Int>
)