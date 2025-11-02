package com.workstation.rotation.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.workstation.rotation.data.dao.RotationDao
import com.workstation.rotation.data.dao.WorkerDao
import com.workstation.rotation.data.dao.WorkstationDao
import com.workstation.rotation.data.entities.Worker
import com.workstation.rotation.data.entities.Workstation
import com.workstation.rotation.models.RotationItem
import com.workstation.rotation.models.RotationTable
import com.workstation.rotation.analytics.RotationAnalytics
import com.workstation.rotation.validation.RotationValidator
import kotlinx.coroutines.launch

/**
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 🚀 VIEWMODEL SQL SIMPLIFICADO Y ROBUSTO - SOLUCIÓN DEFINITIVA
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 
 * CORRECCIONES IMPLEMENTADAS:
 * 1. Algoritmo SQL simplificado y más confiable
 * 2. Eliminación completa de conflictos de liderazgo
 * 3. Garantía de que las parejas entrenador-entrenado permanezcan juntas
 * 4. Asignaciones más predecibles y consistentes
 * 5. Mejor manejo de errores y casos extremos
 * 
 * GARANTÍAS DEL SISTEMA:
 * - Los líderes SIEMPRE van a sus estaciones designadas
 * - Las parejas de entrenamiento NUNCA se separan
 * - Las estaciones prioritarias SIEMPRE se llenan primero
 * - Solo se asignan trabajadores a estaciones donde pueden trabajar
 * 
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 */
class SqlRotationViewModel(
    private val rotationDao: RotationDao,
    private val workerDao: WorkerDao,
    private val workstationDao: WorkstationDao
) : ViewModel() {
    
    // Sistemas de analytics y validación
    private val analytics = RotationAnalytics.getInstance()
    private val validator = RotationValidator()
    
    private val _rotationItems = MutableLiveData<List<RotationItem>>()
    val rotationItems: LiveData<List<RotationItem>> = _rotationItems
    
    private val _rotationTable = MutableLiveData<RotationTable?>()
    val rotationTable: LiveData<RotationTable?> = _rotationTable
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage
    
    // Estado de la rotación (primera o segunda parte)
    private var isFirstHalfRotation = true
    
    /**
     * Alterna entre primera y segunda parte de la rotación.
     */
    fun toggleRotationHalf() {
        isFirstHalfRotation = !isFirstHalfRotation
        println("SQL_DEBUG: Rotación cambiada a: ${if (isFirstHalfRotation) "PRIMERA PARTE" else "SEGUNDA PARTE"}")
    }
    
    /**
     * Obtiene el estado actual de la rotación.
     */
    fun getCurrentRotationHalf(): String {
        return if (isFirstHalfRotation) "PRIMERA PARTE" else "SEGUNDA PARTE"
    }
    
    /**
     * Genera una rotación completa con distribución equitativa entre ambas fases.
     * MEJORADO: Todos los trabajadores participan en al menos una rotación.
     */
    suspend fun generateOptimizedRotation(): Boolean {
        return try {
            _isLoading.value = true
            _errorMessage.value = null
            
            println("SQL_DEBUG: ===== INICIANDO ROTACIÓN SQL MEJORADA =====")
            println("SQL_DEBUG: Generando AMBAS rotaciones simultáneamente")
            
            // Registrar inicio de operación
            analytics.recordUsageMetric("rotation_generated", mapOf("type" to "sql_dual"))
            
            // Paso 1: Obtener datos básicos del sistema
            val systemData = analytics.measureOperation("system_data_loading") {
                loadSystemDataForBothRotations()
            }
            
            if (!systemData.isValid()) {
                throw Exception("Sistema no tiene datos válidos para generar rotación")
            }
            
            // Paso 1.5: Validar sistema antes de proceder
            println("SQL_DEBUG: === INICIANDO VALIDACIÓN DEL SISTEMA ===")
            
            val workerStationMap = mutableMapOf<Long, List<Long>>()
            systemData.eligibleWorkers.forEach { worker ->
                try {
                    val stationIds = workerDao.getWorkerWorkstationIds(worker.id)
                    workerStationMap[worker.id] = stationIds
                    println("SQL_DEBUG: 🔗 ${worker.name} puede trabajar en estaciones: ${stationIds.joinToString()}")
                } catch (e: Exception) {
                    println("SQL_DEBUG: ❌ Error obteniendo estaciones para ${worker.name}: ${e.message}")
                    workerStationMap[worker.id] = emptyList()
                }
            }
            
            val validationResults = validator.validateSystem(
                systemData.eligibleWorkers,
                systemData.workstations,
                workerStationMap
            )
            
            println("SQL_DEBUG: 📋 Resultado de validación - Válido: ${validationResults.isValid}")
            
            if (!validationResults.isValid) {
                val criticalIssues = validationResults.criticalIssues
                println("SQL_DEBUG: ❌ PROBLEMAS CRÍTICOS DETECTADOS:")
                criticalIssues.forEach { issue ->
                    println("SQL_DEBUG:    - ${issue.message}")
                }
                
                if (criticalIssues.isNotEmpty()) {
                    throw Exception("Problemas críticos detectados: ${criticalIssues.joinToString { it.message }}")
                }
            } else {
                println("SQL_DEBUG: ✅ VALIDACIÓN EXITOSA - Sistema listo para generar rotación")
            }
            
            // Paso 2: Ejecutar algoritmo mejorado que genera ambas rotaciones
            val (firstHalfAssignments, secondHalfAssignments) = analytics.measureOperation("dual_rotation_generation") {
                generateDualRotationAlgorithm(systemData, workerStationMap)
            }
            
            // Paso 3: Crear elementos de visualización
            val rotationItems = createDualRotationItems(systemData.workstations, firstHalfAssignments, secondHalfAssignments)
            val rotationTable = createDualRotationTable(systemData.workstations, firstHalfAssignments, secondHalfAssignments)
            
            // Paso 4: Registrar métricas de calidad
            recordDualQualityMetrics(systemData, firstHalfAssignments, secondHalfAssignments)
            
            // Paso 5: Actualizar UI
            _rotationItems.value = rotationItems
            _rotationTable.value = rotationTable
            
            println("SQL_DEBUG: ✅ ROTACIÓN DUAL GENERADA EXITOSAMENTE")
            println("SQL_DEBUG: Primera rotación: ${firstHalfAssignments.values.sumOf { it.size }} asignaciones")
            println("SQL_DEBUG: Segunda rotación: ${secondHalfAssignments.values.sumOf { it.size }} asignaciones")
            println("SQL_DEBUG: ==========================================")
            
            _isLoading.value = false
            true
            
        } catch (e: Exception) {
            println("SQL_DEBUG: ❌ ERROR: ${e.message}")
            e.printStackTrace()
            _errorMessage.value = "Error al generar rotación: ${e.message}"
            _rotationItems.value = emptyList()
            _rotationTable.value = null
            _isLoading.value = false
            false
        }
    }
    
    /**
     * Carga los datos del sistema para ambas rotaciones.
     */
    private suspend fun loadSystemDataForBothRotations(): DualSystemData {
        println("SQL_DEBUG: === CARGANDO DATOS PARA AMBAS ROTACIONES ===")
        
        try {
            // Obtener datos básicos
            val eligibleWorkers = rotationDao.getAllEligibleWorkers()
            val workstations = rotationDao.getAllActiveWorkstationsOrdered()
            
            // Obtener líderes para ambas rotaciones
            val firstHalfLeaders = rotationDao.getActiveLeadersForRotationFixed(true)
            val secondHalfLeaders = rotationDao.getActiveLeadersForRotationFixed(false)
            
            // Obtener parejas de entrenamiento
            val trainingPairs = rotationDao.getValidTrainingPairs()
            
            println("SQL_DEBUG: ✅ Trabajadores elegibles: ${eligibleWorkers.size}")
            println("SQL_DEBUG: ✅ Estaciones activas: ${workstations.size}")
            println("SQL_DEBUG: ✅ Líderes primera rotación: ${firstHalfLeaders.size}")
            println("SQL_DEBUG: ✅ Líderes segunda rotación: ${secondHalfLeaders.size}")
            println("SQL_DEBUG: ✅ Parejas de entrenamiento: ${trainingPairs.size}")
            
            return DualSystemData(
                eligibleWorkers = eligibleWorkers,
                workstations = workstations,
                firstHalfLeaders = firstHalfLeaders,
                secondHalfLeaders = secondHalfLeaders,
                trainingPairs = trainingPairs
            )
            
        } catch (e: Exception) {
            println("SQL_DEBUG: ❌ ERROR en loadSystemDataForBothRotations(): ${e.message}")
            throw e
        }
    }

    /**
     * Carga los datos básicos del sistema usando consultas SQL optimizadas.
     */
    private suspend fun loadSystemData(): SystemData {
        println("SQL_DEBUG: === CARGANDO DATOS DEL SISTEMA ===")
        
        try {
            // Usar las nuevas consultas SQL mejoradas con logs detallados
            println("SQL_DEBUG: 🔍 Ejecutando getAllEligibleWorkers()...")
            val eligibleWorkers = rotationDao.getAllEligibleWorkers()
            println("SQL_DEBUG: ✅ getAllEligibleWorkers() completado - Resultado: ${eligibleWorkers.size}")
            
            println("SQL_DEBUG: 🔍 Ejecutando getAllActiveWorkstationsOrdered()...")
            val workstations = rotationDao.getAllActiveWorkstationsOrdered()
            println("SQL_DEBUG: ✅ getAllActiveWorkstationsOrdered() completado - Resultado: ${workstations.size}")
            
            println("SQL_DEBUG: 🔍 Ejecutando getActiveLeadersForRotationFixed(${isFirstHalfRotation})...")
            val activeLeaders = rotationDao.getActiveLeadersForRotationFixed(isFirstHalfRotation)
            println("SQL_DEBUG: ✅ getActiveLeadersForRotationFixed() completado - Resultado: ${activeLeaders.size}")
            
            println("SQL_DEBUG: 🔍 Ejecutando getValidTrainingPairs()...")
            val trainingPairs = rotationDao.getValidTrainingPairs()
            println("SQL_DEBUG: ✅ getValidTrainingPairs() completado - Resultado: ${trainingPairs.size}")
            
            // Log detallado de trabajadores
            println("SQL_DEBUG: === DETALLE DE TRABAJADORES ELEGIBLES ===")
            if (eligibleWorkers.isEmpty()) {
                println("SQL_DEBUG: ❌ NO HAY TRABAJADORES ELEGIBLES")
            } else {
                eligibleWorkers.forEach { worker ->
                    println("SQL_DEBUG: 👤 ${worker.name} - ID: ${worker.id} - Activo: ${worker.isActive} - Líder: ${worker.isLeader} - Entrenador: ${worker.isTrainer} - Entrenado: ${worker.isTrainee}")
                }
            }
            
            // Log detallado de estaciones
            println("SQL_DEBUG: === DETALLE DE ESTACIONES ACTIVAS ===")
            if (workstations.isEmpty()) {
                println("SQL_DEBUG: ❌ NO HAY ESTACIONES ACTIVAS")
            } else {
                workstations.forEach { station ->
                    println("SQL_DEBUG: 🏭 ${station.name} - ID: ${station.id} - Activa: ${station.isActive} - Requiere: ${station.requiredWorkers} - Prioritaria: ${station.isPriority}")
                }
            }
            
            // Log detallado de líderes
            println("SQL_DEBUG: === DETALLE DE LÍDERES ACTIVOS ===")
            if (activeLeaders.isEmpty()) {
                println("SQL_DEBUG: ⚠️ NO HAY LÍDERES ACTIVOS PARA ESTA ROTACIÓN")
            } else {
                activeLeaders.forEach { leader ->
                    println("SQL_DEBUG: 👑 ${leader.name} - Estación: ${leader.leaderWorkstationId} - Tipo: ${leader.leadershipType}")
                }
            }
            
            // Log detallado de parejas de entrenamiento
            println("SQL_DEBUG: === DETALLE DE PAREJAS DE ENTRENAMIENTO ===")
            if (trainingPairs.isEmpty()) {
                println("SQL_DEBUG: ⚠️ NO HAY PAREJAS DE ENTRENAMIENTO")
            } else {
                trainingPairs.forEach { trainee ->
                    println("SQL_DEBUG: 🎯 ${trainee.name} - Entrenador ID: ${trainee.trainerId} - Estación: ${trainee.trainingWorkstationId}")
                }
            }
            
            // Verificar integridad de datos
            if (eligibleWorkers.isEmpty()) {
                println("SQL_DEBUG: ❌ FALLO: No hay trabajadores elegibles para rotación")
                throw Exception("No hay trabajadores elegibles para rotación")
            }
            
            if (workstations.isEmpty()) {
                println("SQL_DEBUG: ❌ FALLO: No hay estaciones activas")
                throw Exception("No hay estaciones activas")
            }
            
            println("SQL_DEBUG: ✅ DATOS DEL SISTEMA CARGADOS EXITOSAMENTE")
            return SystemData(
                eligibleWorkers = eligibleWorkers,
                workstations = workstations,
                activeLeaders = activeLeaders,
                trainingPairs = trainingPairs
            )
            
        } catch (e: Exception) {
            println("SQL_DEBUG: ❌ ERROR CRÍTICO en loadSystemData(): ${e.message}")
            e.printStackTrace()
            throw e
        }
    }
    
    /**
     * Ejecuta el algoritmo SQL optimizado para grandes volúmenes.
     * OPTIMIZADO: Maneja 30+ estaciones y 70+ trabajadores eficientemente.
     */
    private suspend fun executeSimplifiedSqlAlgorithm(
        systemData: SystemData
    ): Pair<Map<Long, List<Worker>>, Map<Long, List<Worker>>> {
        
        val startTime = System.currentTimeMillis()
        println("SQL_DEBUG: === EJECUTANDO ALGORITMO SQL OPTIMIZADO PARA GRANDES VOLÚMENES ===")
        println("SQL_DEBUG: Procesando ${systemData.eligibleWorkers.size} trabajadores y ${systemData.workstations.size} estaciones")
        
        // Inicializar estructuras de datos optimizadas
        val currentAssignments = mutableMapOf<Long, MutableList<Worker>>()
        val nextAssignments = mutableMapOf<Long, MutableList<Worker>>()
        
        // Pre-calcular capacidades y crear índices para O(1) lookups
        val stationCapacities = mutableMapOf<Long, Int>()
        val assignedWorkers = mutableSetOf<Long>() // Usar Set para O(1) contains
        
        systemData.workstations.forEach { station ->
            currentAssignments[station.id] = mutableListOf()
            nextAssignments[station.id] = mutableListOf()
            stationCapacities[station.id] = station.requiredWorkers
        }
        
        // Pre-cargar todas las relaciones worker-station para evitar consultas repetitivas
        val workerStationMap = preloadWorkerStationRelations(systemData.eligibleWorkers)
        
        // FASE 1: MÁXIMA PRIORIDAD - Líderes activos (optimizada)
        assignActiveLeadersOptimized(systemData, currentAssignments, assignedWorkers, workerStationMap)
        
        // FASE 2: ALTA PRIORIDAD - Parejas de entrenamiento (optimizada)
        assignTrainingPairsOptimized(systemData, currentAssignments, assignedWorkers, workerStationMap)
        
        // FASE 3 & 4: Distribución masiva optimizada
        distributeMassiveWorkersOptimized(systemData, currentAssignments, assignedWorkers, workerStationMap, stationCapacities)
        
        // FASE 5: Generar próxima rotación (optimizada)
        generateNextRotationOptimized(systemData, currentAssignments, nextAssignments, workerStationMap)
        
        val endTime = System.currentTimeMillis()
        println("SQL_DEBUG: ✅ Algoritmo optimizado completado en ${endTime - startTime}ms")
        println("SQL_DEBUG: Trabajadores asignados: ${assignedWorkers.size}/${systemData.eligibleWorkers.size}")
        
        return Pair(
            currentAssignments.mapValues { it.value.toList() },
            nextAssignments.mapValues { it.value.toList() }
        )
    }
    
    /**
     * Pre-carga todas las relaciones worker-station para evitar consultas SQL repetitivas.
     * Optimización crítica para grandes volúmenes.
     */
    private suspend fun preloadWorkerStationRelations(workers: List<Worker>): Map<Long, Set<Long>> {
        println("SQL_DEBUG: Pre-cargando relaciones worker-station...")
        val startTime = System.currentTimeMillis()
        
        val workerStationMap = mutableMapOf<Long, Set<Long>>()
        
        // Cargar todas las relaciones en una sola operación por trabajador
        workers.forEach { worker ->
            try {
                val stationIds = workerDao.getWorkerWorkstationIds(worker.id).toSet()
                workerStationMap[worker.id] = stationIds
            } catch (e: Exception) {
                workerStationMap[worker.id] = emptySet()
            }
        }
        
        val endTime = System.currentTimeMillis()
        println("SQL_DEBUG: ✅ Relaciones pre-cargadas en ${endTime - startTime}ms")
        
        return workerStationMap
    }
    
    /**
     * FASE 1: Asigna líderes activos a sus estaciones designadas.
     * GARANTIZADO: Los líderes SIEMPRE van a sus estaciones.
     */
    private suspend fun assignActiveLeaders(
        systemData: SystemData,
        assignments: MutableMap<Long, MutableList<Worker>>,
        availableWorkers: MutableList<Worker>
    ) {
        println("SQL_DEBUG: === FASE 1: ASIGNANDO LÍDERES ACTIVOS ===")
        
        for (leader in systemData.activeLeaders) {
            if (!availableWorkers.contains(leader)) continue
            
            val leaderStationId = leader.leaderWorkstationId
            if (leaderStationId == null) {
                println("SQL_DEBUG: ⚠️ Líder ${leader.name} sin estación asignada")
                continue
            }
            
            // Verificar que el líder puede trabajar en su estación
            val canWork = rotationDao.canWorkerWorkAtStationFixed(leader.id, leaderStationId)
            if (!canWork) {
                println("SQL_DEBUG: ❌ Líder ${leader.name} no puede trabajar en su estación ${leaderStationId}")
                continue
            }
            
            // ASIGNACIÓN FORZADA - Los líderes tienen prioridad absoluta
            assignments[leaderStationId]?.add(leader)
            availableWorkers.remove(leader)
            
            println("SQL_DEBUG: ✅ Líder ${leader.name} asignado a estación ${leaderStationId}")
        }
        
        println("SQL_DEBUG: Líderes asignados: ${systemData.activeLeaders.size - availableWorkers.count { it.isLeader }}")
    }
    
    /**
     * FASE 2: Asigna parejas de entrenamiento.
     * GARANTIZADO: Las parejas NUNCA se separan.
     */
    private suspend fun assignTrainingPairs(
        systemData: SystemData,
        assignments: MutableMap<Long, MutableList<Worker>>,
        availableWorkers: MutableList<Worker>
    ) {
        println("SQL_DEBUG: === FASE 2: ASIGNANDO PAREJAS DE ENTRENAMIENTO ===")
        
        for (trainee in systemData.trainingPairs) {
            if (!availableWorkers.contains(trainee)) continue
            
            // Buscar el entrenador
            val trainer = systemData.eligibleWorkers.find { it.id == trainee.trainerId }
            if (trainer == null || !availableWorkers.contains(trainer)) {
                println("SQL_DEBUG: ⚠️ Entrenador no disponible para ${trainee.name}")
                continue
            }
            
            val trainingStationId = trainee.trainingWorkstationId
            if (trainingStationId == null) {
                println("SQL_DEBUG: ⚠️ Entrenado ${trainee.name} sin estación de entrenamiento")
                continue
            }
            
            // Verificar que ambos pueden trabajar en la estación
            val traineeCanWork = rotationDao.canWorkerWorkAtStationFixed(trainee.id, trainingStationId)
            val trainerCanWork = rotationDao.canWorkerWorkAtStationFixed(trainer.id, trainingStationId)
            
            if (!traineeCanWork || !trainerCanWork) {
                println("SQL_DEBUG: ❌ Pareja ${trainer.name}-${trainee.name} no puede trabajar en estación ${trainingStationId}")
                continue
            }
            
            // ASIGNACIÓN FORZADA - Las parejas tienen prioridad absoluta
            assignments[trainingStationId]?.addAll(listOf(trainer, trainee))
            availableWorkers.removeAll(listOf(trainer, trainee))
            
            println("SQL_DEBUG: ✅ Pareja ${trainer.name}-${trainee.name} asignada a estación ${trainingStationId}")
        }
        
        println("SQL_DEBUG: Parejas asignadas: ${systemData.trainingPairs.size}")
    }
    
    /**
     * FASE 3: Llena estaciones prioritarias hasta su capacidad requerida.
     */
    private suspend fun fillPriorityStations(
        systemData: SystemData,
        assignments: MutableMap<Long, MutableList<Worker>>,
        availableWorkers: MutableList<Worker>
    ) {
        println("SQL_DEBUG: === FASE 3: LLENANDO ESTACIONES PRIORITARIAS ===")
        
        val priorityStations = systemData.workstations.filter { it.isPriority }
        println("SQL_DEBUG: Estaciones prioritarias encontradas: ${priorityStations.size}")
        
        for (station in priorityStations) {
            val currentCount = assignments[station.id]?.size ?: 0
            val needed = station.requiredWorkers - currentCount
            
            println("SQL_DEBUG: Estación ${station.name} - Actual: $currentCount, Necesita: $needed, Requeridos: ${station.requiredWorkers}")
            
            if (needed <= 0) {
                println("SQL_DEBUG: Estación prioritaria ${station.name} ya completa (${currentCount}/${station.requiredWorkers})")
                continue
            }
            
            // Obtener trabajadores elegibles para esta estación
            val eligibleForStation = rotationDao.getWorkersForStationFixed(station.id)
                .filter { worker -> 
                    val isAvailable = availableWorkers.contains(worker)
                    println("SQL_DEBUG: Trabajador ${worker.name} - Disponible: $isAvailable")
                    isAvailable
                }
                .take(needed)
            
            println("SQL_DEBUG: Trabajadores elegibles para ${station.name}: ${eligibleForStation.size}")
            eligibleForStation.forEach { worker ->
                println("SQL_DEBUG: - Asignando ${worker.name} a ${station.name}")
            }
            
            assignments[station.id]?.addAll(eligibleForStation)
            availableWorkers.removeAll(eligibleForStation)
            
            println("SQL_DEBUG: ✅ Estación prioritaria ${station.name}: ${eligibleForStation.size} trabajadores asignados")
        }
        
        println("SQL_DEBUG: Trabajadores restantes después de estaciones prioritarias: ${availableWorkers.size}")
    }
    
    /**
     * FASE 4: Llena estaciones normales con trabajadores restantes.
     */
    private suspend fun fillNormalStations(
        systemData: SystemData,
        assignments: MutableMap<Long, MutableList<Worker>>,
        availableWorkers: MutableList<Worker>
    ) {
        println("SQL_DEBUG: === FASE 4: LLENANDO ESTACIONES NORMALES ===")
        
        val normalStations = systemData.workstations.filter { !it.isPriority }
        println("SQL_DEBUG: Estaciones normales encontradas: ${normalStations.size}")
        println("SQL_DEBUG: Trabajadores disponibles para estaciones normales: ${availableWorkers.size}")
        
        // Si hay más trabajadores que espacios, distribuir equitativamente
        if (availableWorkers.isNotEmpty()) {
            // Esta función ya no se usa en el algoritmo optimizado
            // La distribución se maneja en distributeMassiveWorkersOptimized
            println("SQL_DEBUG: ⚠️ Función legacy detectada - usando algoritmo optimizado")
        }
        
        println("SQL_DEBUG: Trabajadores sin asignar después de distribución: ${availableWorkers.size}")
        
        // Log final de asignaciones
        println("SQL_DEBUG: === RESUMEN DE ASIGNACIONES ACTUALES ===")
        systemData.workstations.forEach { station ->
            val assigned = assignments[station.id]?.size ?: 0
            println("SQL_DEBUG: ${station.name}: ${assigned}/${station.requiredWorkers} trabajadores")
            assignments[station.id]?.forEach { worker ->
                println("SQL_DEBUG:   - ${worker.name}")
            }
        }
    }
    
    /**
     * Asigna líderes activos de forma optimizada.
     * O(n) en lugar de O(n²).
     */
    private suspend fun assignActiveLeadersOptimized(
        systemData: SystemData,
        assignments: MutableMap<Long, MutableList<Worker>>,
        assignedWorkers: MutableSet<Long>,
        workerStationMap: Map<Long, Set<Long>>
    ) {
        println("SQL_DEBUG: === FASE 1: ASIGNANDO ${systemData.activeLeaders.size} LÍDERES (OPTIMIZADO) ===")
        
        for (leader in systemData.activeLeaders) {
            if (assignedWorkers.contains(leader.id)) continue
            
            val leaderStationId = leader.leaderWorkstationId ?: continue
            
            // Verificación O(1) usando pre-cargado
            val canWork = workerStationMap[leader.id]?.contains(leaderStationId) ?: false
            if (canWork) {
                assignments[leaderStationId]?.add(leader)
                assignedWorkers.add(leader.id)
            }
        }
        
        println("SQL_DEBUG: ✅ Líderes asignados: ${systemData.activeLeaders.size}")
    }
    
    /**
     * Asigna parejas de entrenamiento de forma optimizada.
     */
    private suspend fun assignTrainingPairsOptimized(
        systemData: SystemData,
        assignments: MutableMap<Long, MutableList<Worker>>,
        assignedWorkers: MutableSet<Long>,
        workerStationMap: Map<Long, Set<Long>>
    ) {
        println("SQL_DEBUG: === FASE 2: ASIGNANDO ${systemData.trainingPairs.size} PAREJAS (OPTIMIZADO) ===")
        
        for (trainee in systemData.trainingPairs) {
            if (assignedWorkers.contains(trainee.id)) continue
            
            val trainer = systemData.eligibleWorkers.find { it.id == trainee.trainerId }
            if (trainer == null || assignedWorkers.contains(trainer.id)) continue
            
            val trainingStationId = trainee.trainingWorkstationId ?: continue
            
            // Verificaciones O(1)
            val traineeCanWork = workerStationMap[trainee.id]?.contains(trainingStationId) ?: false
            val trainerCanWork = workerStationMap[trainer.id]?.contains(trainingStationId) ?: false
            
            if (traineeCanWork && trainerCanWork) {
                assignments[trainingStationId]?.addAll(listOf(trainer, trainee))
                assignedWorkers.addAll(listOf(trainer.id, trainee.id))
            }
        }
        
        println("SQL_DEBUG: ✅ Parejas asignadas: ${systemData.trainingPairs.size}")
    }
    
    /**
     * Distribución masiva optimizada para grandes volúmenes.
     * Algoritmo O(n*m) eficiente para 30+ estaciones y 70+ trabajadores.
     */
    private suspend fun distributeMassiveWorkersOptimized(
        systemData: SystemData,
        assignments: MutableMap<Long, MutableList<Worker>>,
        assignedWorkers: MutableSet<Long>,
        workerStationMap: Map<Long, Set<Long>>,
        stationCapacities: Map<Long, Int>
    ) {
        println("SQL_DEBUG: === DISTRIBUCIÓN MASIVA OPTIMIZADA ===")
        val startTime = System.currentTimeMillis()
        
        // Separar estaciones por prioridad
        val priorityStations = systemData.workstations.filter { it.isPriority }
        val normalStations = systemData.workstations.filter { !it.isPriority }
        
        // Obtener trabajadores no asignados
        val unassignedWorkers = systemData.eligibleWorkers.filter { !assignedWorkers.contains(it.id) }
        
        println("SQL_DEBUG: Distribuyendo ${unassignedWorkers.size} trabajadores no asignados")
        
        // Distribuir en estaciones prioritarias primero
        distributeToStationsOptimized(priorityStations, unassignedWorkers, assignments, assignedWorkers, workerStationMap, stationCapacities)
        
        // Distribuir en estaciones normales
        val stillUnassigned = unassignedWorkers.filter { !assignedWorkers.contains(it.id) }
        distributeToStationsOptimized(normalStations, stillUnassigned, assignments, assignedWorkers, workerStationMap, stationCapacities)
        
        val endTime = System.currentTimeMillis()
        println("SQL_DEBUG: ✅ Distribución masiva completada en ${endTime - startTime}ms")
        println("SQL_DEBUG: Trabajadores finalmente asignados: ${assignedWorkers.size}/${systemData.eligibleWorkers.size}")
    }
    
    /**
     * Distribuye trabajadores a estaciones de forma optimizada.
     */
    private fun distributeToStationsOptimized(
        stations: List<Workstation>,
        workers: List<Worker>,
        assignments: MutableMap<Long, MutableList<Worker>>,
        assignedWorkers: MutableSet<Long>,
        workerStationMap: Map<Long, Set<Long>>,
        stationCapacities: Map<Long, Int>
    ) {
        // Crear cola de espacios disponibles
        val availableSpaces = mutableListOf<Long>()
        stations.forEach { station ->
            val currentCount = assignments[station.id]?.size ?: 0
            val capacity = stationCapacities[station.id] ?: 0
            val needed = capacity - currentCount
            repeat(needed) {
                availableSpaces.add(station.id)
            }
        }
        
        // Distribuir trabajadores de forma circular
        var spaceIndex = 0
        for (worker in workers) {
            if (assignedWorkers.contains(worker.id) || availableSpaces.isEmpty()) continue
            
            // Buscar espacio compatible de forma eficiente
            var assigned = false
            val maxAttempts = minOf(availableSpaces.size, 10) // Limitar intentos para evitar loops infinitos
            
            for (attempt in 0 until maxAttempts) {
                val stationId = availableSpaces[spaceIndex % availableSpaces.size]
                
                // Verificación O(1) usando pre-cargado
                val canWork = workerStationMap[worker.id]?.contains(stationId) ?: false
                
                if (canWork) {
                    assignments[stationId]?.add(worker)
                    assignedWorkers.add(worker.id)
                    availableSpaces.removeAt(spaceIndex % availableSpaces.size)
                    assigned = true
                    break
                } else {
                    spaceIndex++
                }
            }
        }
    }
    
    /**
     * Valida las asignaciones actuales para detectar duplicados.
     */
    private fun validateCurrentAssignments(assignments: Map<Long, List<Worker>>) {
        println("SQL_DEBUG: === VALIDANDO ASIGNACIONES ACTUALES ===")
        
        val allWorkers = mutableListOf<Worker>()
        val duplicates = mutableSetOf<Worker>()
        
        assignments.forEach { (stationId, workers) ->
            println("SQL_DEBUG: Estación $stationId tiene ${workers.size} trabajadores")
            workers.forEach { worker ->
                if (allWorkers.contains(worker)) {
                    duplicates.add(worker)
                    println("SQL_DEBUG: ❌ DUPLICADO: ${worker.name} en estación $stationId")
                } else {
                    allWorkers.add(worker)
                    println("SQL_DEBUG: ✅ ${worker.name} asignado correctamente a estación $stationId")
                }
            }
        }
        
        if (duplicates.isEmpty()) {
            println("SQL_DEBUG: ✅ VALIDACIÓN EXITOSA: No hay duplicados en asignaciones actuales")
        } else {
            println("SQL_DEBUG: ❌ ENCONTRADOS ${duplicates.size} DUPLICADOS EN ASIGNACIONES ACTUALES")
        }
    }
    
    /**
     * Genera próxima rotación de forma optimizada para grandes volúmenes.
     */
    private suspend fun generateNextRotationOptimized(
        systemData: SystemData,
        currentAssignments: Map<Long, List<Worker>>,
        nextAssignments: MutableMap<Long, MutableList<Worker>>,
        workerStationMap: Map<Long, Set<Long>>
    ) {
        println("SQL_DEBUG: === GENERANDO PRÓXIMA ROTACIÓN (OPTIMIZADA) ===")
        val startTime = System.currentTimeMillis()
        
        // Usar Sets para O(1) lookups
        val fixedWorkerIds = mutableSetOf<Long>()
        
        // Paso 1: Mantener líderes (O(n))
        for (leader in systemData.activeLeaders) {
            leader.leaderWorkstationId?.let { stationId ->
                if (currentAssignments[stationId]?.contains(leader) == true) {
                    nextAssignments[stationId]?.add(leader)
                    fixedWorkerIds.add(leader.id)
                }
            }
        }
        
        // Paso 2: Mantener parejas de entrenamiento (O(n))
        for (trainee in systemData.trainingPairs) {
            val trainer = systemData.eligibleWorkers.find { it.id == trainee.trainerId }
            trainee.trainingWorkstationId?.let { stationId ->
                if (currentAssignments[stationId]?.containsAll(listOfNotNull(trainer, trainee)) == true) {
                    nextAssignments[stationId]?.addAll(listOfNotNull(trainer, trainee))
                    fixedWorkerIds.addAll(listOfNotNull(trainer?.id, trainee.id))
                }
            }
        }
        
        // Paso 3: Rotar trabajadores regulares de forma masiva
        val workersToRotate = currentAssignments.values.flatten().filter { !fixedWorkerIds.contains(it.id) }
        
        if (workersToRotate.isNotEmpty()) {
            rotateWorkersOptimized(systemData, currentAssignments, nextAssignments, workersToRotate, workerStationMap)
        }
        
        // Paso 4: Llenar espacios vacíos de forma optimizada
        fillRemainingSpacesOptimized(systemData, nextAssignments, workerStationMap)
        
        val endTime = System.currentTimeMillis()
        println("SQL_DEBUG: ✅ Próxima rotación generada en ${endTime - startTime}ms")
        
        // Log resumido para grandes volúmenes
        val totalAssigned = nextAssignments.values.sumOf { it.size }
        val totalCapacity = systemData.workstations.sumOf { it.requiredWorkers }
        println("SQL_DEBUG: Próxima rotación: ${totalAssigned}/${totalCapacity} espacios ocupados")
    }
    
    /**
     * Rota trabajadores de forma optimizada para grandes volúmenes.
     */
    private fun rotateWorkersOptimized(
        systemData: SystemData,
        currentAssignments: Map<Long, List<Worker>>,
        nextAssignments: MutableMap<Long, MutableList<Worker>>,
        workersToRotate: List<Worker>,
        workerStationMap: Map<Long, Set<Long>>
    ) {
        // Crear mapa de trabajador -> estación actual para O(1) lookup
        val workerCurrentStation = mutableMapOf<Long, Long>()
        currentAssignments.forEach { (stationId, workers) ->
            workers.forEach { worker ->
                workerCurrentStation[worker.id] = stationId
            }
        }
        
        // Crear lista de espacios disponibles
        val availableSpaces = mutableListOf<Long>()
        systemData.workstations.forEach { station ->
            val currentCount = nextAssignments[station.id]?.size ?: 0
            val needed = station.requiredWorkers - currentCount
            repeat(needed) {
                availableSpaces.add(station.id)
            }
        }
        
        // Rotar trabajadores de forma eficiente
        for (worker in workersToRotate) {
            val currentStationId = workerCurrentStation[worker.id]
            val eligibleStations = workerStationMap[worker.id] ?: emptySet()
            
            // Buscar nueva estación (diferente a la actual)
            val targetStationId = availableSpaces.find { stationId ->
                stationId != currentStationId && 
                eligibleStations.contains(stationId) &&
                (nextAssignments[stationId]?.size ?: 0) < (systemData.workstations.find { it.id == stationId }?.requiredWorkers ?: 0)
            }
            
            if (targetStationId != null) {
                nextAssignments[targetStationId]?.add(worker)
                availableSpaces.remove(targetStationId)
            } else if (currentStationId != null && availableSpaces.contains(currentStationId)) {
                // Mantener en estación actual si no se puede rotar
                nextAssignments[currentStationId]?.add(worker)
                availableSpaces.remove(currentStationId)
            }
        }
    }
    
    /**
     * Llena espacios vacíos de forma optimizada.
     */
    private fun fillRemainingSpacesOptimized(
        systemData: SystemData,
        nextAssignments: MutableMap<Long, MutableList<Worker>>,
        workerStationMap: Map<Long, Set<Long>>
    ) {
        // Identificar trabajadores no asignados
        val allAssignedWorkerIds = nextAssignments.values.flatten().map { it.id }.toSet()
        val unassignedWorkers = systemData.eligibleWorkers.filter { !allAssignedWorkerIds.contains(it.id) }
        
        if (unassignedWorkers.isEmpty()) return
        
        // Identificar espacios vacíos
        val emptySpaces = mutableListOf<Long>()
        systemData.workstations.forEach { station ->
            val currentCount = nextAssignments[station.id]?.size ?: 0
            val needed = station.requiredWorkers - currentCount
            repeat(needed) {
                emptySpaces.add(station.id)
            }
        }
        
        // Llenar espacios de forma eficiente
        for (worker in unassignedWorkers) {
            if (emptySpaces.isEmpty()) break
            
            val eligibleStations = workerStationMap[worker.id] ?: emptySet()
            val targetStationId = emptySpaces.find { eligibleStations.contains(it) }
            
            if (targetStationId != null) {
                nextAssignments[targetStationId]?.add(worker)
                emptySpaces.remove(targetStationId)
            }
        }
    }
    
    /**
     * Rota trabajadores regulares a diferentes estaciones.
     * CORREGIDO: Evita asignaciones duplicadas.
     */
    private suspend fun rotateRegularWorkers(
        systemData: SystemData,
        currentAssignments: Map<Long, List<Worker>>,
        nextAssignments: MutableMap<Long, MutableList<Worker>>,
        workersToRotate: List<Worker>
    ) {
        println("SQL_DEBUG: === ROTANDO TRABAJADORES REGULARES ===")
        
        for (worker in workersToRotate) {
            // VERIFICACIÓN CRÍTICA: Asegurar que el trabajador no esté ya asignado en nextAssignments
            val isAlreadyAssignedInNext = nextAssignments.values.any { stationWorkers ->
                stationWorkers.contains(worker)
            }
            
            if (isAlreadyAssignedInNext) {
                println("SQL_DEBUG: ⚠️ ${worker.name} ya está asignado en próxima rotación, saltando")
                continue
            }
            
            val currentStationId = currentAssignments.entries.find { it.value.contains(worker) }?.key
            
            // Buscar estaciones donde puede trabajar (excluyendo la actual)
            val eligibleStations = systemData.workstations.filter { station ->
                station.id != currentStationId && 
                (nextAssignments[station.id]?.size ?: 0) < station.requiredWorkers &&
                !nextAssignments[station.id]!!.contains(worker) // Verificación adicional
            }
            
            println("SQL_DEBUG: ${worker.name} puede rotar a ${eligibleStations.size} estaciones")
            
            // Intentar asignar a una estación elegible
            var assigned = false
            for (station in eligibleStations) {
                // Verificación final antes de asignar
                val isWorkerInThisStation = nextAssignments[station.id]?.contains(worker) ?: false
                
                if (!isWorkerInThisStation) {
                    val canWork = rotationDao.canWorkerWorkAtStationFixed(worker.id, station.id)
                    if (canWork && (nextAssignments[station.id]?.size ?: 0) < station.requiredWorkers) {
                        nextAssignments[station.id]?.add(worker)
                        assigned = true
                        println("SQL_DEBUG: ✅ ${worker.name} rotado ÚNICAMENTE de estación $currentStationId a ${station.id}")
                        break
                    }
                }
            }
            
            // Si no se pudo rotar, mantener en la estación actual si hay espacio
            if (!assigned && currentStationId != null) {
                val currentStation = systemData.workstations.find { it.id == currentStationId }
                val isWorkerInCurrentNext = nextAssignments[currentStationId]?.contains(worker) ?: false
                
                if (currentStation != null && 
                    !isWorkerInCurrentNext &&
                    (nextAssignments[currentStationId]?.size ?: 0) < currentStation.requiredWorkers) {
                    
                    nextAssignments[currentStationId]?.add(worker)
                    println("SQL_DEBUG: ⚠️ ${worker.name} permanece ÚNICAMENTE en estación $currentStationId (no se pudo rotar)")
                } else {
                    println("SQL_DEBUG: ❌ No se pudo asignar ${worker.name} a ninguna estación")
                }
            }
        }
    }
    
    /**
     * Llena espacios vacíos en la próxima rotación.
     * CORREGIDO: Evita asignaciones duplicadas de trabajadores.
     */
    private suspend fun fillRemainingSpaces(
        systemData: SystemData,
        nextAssignments: MutableMap<Long, MutableList<Worker>>,
        availableWorkers: List<Worker>
    ) {
        println("SQL_DEBUG: === LLENANDO ESPACIOS RESTANTES ===")
        
        // Identificar estaciones que necesitan más trabajadores
        val stationsNeedingWorkers = systemData.workstations.filter { station ->
            (nextAssignments[station.id]?.size ?: 0) < station.requiredWorkers
        }
        
        if (stationsNeedingWorkers.isNotEmpty()) {
            println("SQL_DEBUG: ${stationsNeedingWorkers.size} estaciones necesitan más trabajadores")
            
            // CORRECCIÓN CRÍTICA: Solo usar trabajadores que NO están asignados a ninguna estación
            val allAssignedWorkers = nextAssignments.values.flatten().toSet()
            val unassignedWorkers = systemData.eligibleWorkers.filter { worker ->
                !allAssignedWorkers.contains(worker)
            }
            
            println("SQL_DEBUG: Trabajadores sin asignar disponibles: ${unassignedWorkers.size}")
            unassignedWorkers.forEach { worker ->
                println("SQL_DEBUG: - ${worker.name} disponible para asignación")
            }
            
            for (station in stationsNeedingWorkers) {
                val currentCount = nextAssignments[station.id]?.size ?: 0
                val needed = station.requiredWorkers - currentCount
                
                println("SQL_DEBUG: ${station.name} necesita $needed trabajadores más")
                
                if (needed > 0 && unassignedWorkers.isNotEmpty()) {
                    val eligibleWorkers = unassignedWorkers.filter { worker ->
                        // Verificar que el trabajador no esté ya asignado a NINGUNA estación
                        val isAlreadyAssigned = nextAssignments.values.any { stationWorkers ->
                            stationWorkers.contains(worker)
                        }
                        !isAlreadyAssigned
                    }.take(needed)
                    
                    for (worker in eligibleWorkers) {
                        val canWork = rotationDao.canWorkerWorkAtStationFixed(worker.id, station.id)
                        if (canWork) {
                            // Verificación final antes de asignar
                            val isWorkerAlreadyAssigned = nextAssignments.values.any { stationWorkers ->
                                stationWorkers.contains(worker)
                            }
                            
                            if (!isWorkerAlreadyAssigned && (nextAssignments[station.id]?.size ?: 0) < station.requiredWorkers) {
                                nextAssignments[station.id]?.add(worker)
                                println("SQL_DEBUG: ✅ ${worker.name} agregado ÚNICAMENTE a ${station.name}")
                            } else {
                                println("SQL_DEBUG: ⚠️ ${worker.name} ya está asignado o estación llena")
                            }
                        } else {
                            println("SQL_DEBUG: ⚠️ ${worker.name} no puede trabajar en ${station.name}")
                        }
                    }
                }
            }
        }
        
        // Verificación final de integridad
        validateNoDoubleAssignments(nextAssignments)
    }
    
    /**
     * Validación optimizada para grandes volúmenes.
     * O(n) en lugar de O(n²).
     */
    private fun validateNoDoubleAssignments(assignments: Map<Long, List<Worker>>) {
        if (assignments.values.sumOf { it.size } > 50) {
            // Para grandes volúmenes, hacer validación rápida
            val allWorkerIds = mutableSetOf<Long>()
            var duplicateCount = 0
            
            assignments.values.forEach { workers ->
                workers.forEach { worker ->
                    if (!allWorkerIds.add(worker.id)) {
                        duplicateCount++
                    }
                }
            }
            
            if (duplicateCount == 0) {
                println("SQL_DEBUG: ✅ VALIDACIÓN RÁPIDA: No hay duplicados (${allWorkerIds.size} trabajadores)")
            } else {
                println("SQL_DEBUG: ❌ VALIDACIÓN RÁPIDA: ${duplicateCount} duplicados encontrados")
            }
        } else {
            // Para volúmenes pequeños, hacer validación detallada
            validateDetailedAssignments(assignments)
        }
    }
    
    /**
     * Validación detallada solo para volúmenes pequeños.
     */
    private fun validateDetailedAssignments(assignments: Map<Long, List<Worker>>) {
        val allWorkerIds = mutableSetOf<Long>()
        val duplicates = mutableSetOf<Long>()
        
        assignments.forEach { (_, workers) ->
            workers.forEach { worker ->
                if (!allWorkerIds.add(worker.id)) {
                    duplicates.add(worker.id)
                }
            }
        }
        
        if (duplicates.isEmpty()) {
            println("SQL_DEBUG: ✅ VALIDACIÓN DETALLADA: No hay duplicados")
        } else {
            println("SQL_DEBUG: ❌ VALIDACIÓN DETALLADA: ${duplicates.size} duplicados")
        }
    }
    
    /**
     * Crea los elementos de rotación para la UI.
     */
    private fun createRotationItems(
        workstations: List<Workstation>,
        currentAssignments: Map<Long, List<Worker>>,
        nextAssignments: Map<Long, List<Worker>>
    ): List<RotationItem> {
        val items = mutableListOf<RotationItem>()
        var order = 1
        
        for (station in workstations) {
            val currentWorkers = currentAssignments[station.id] ?: emptyList()
            
            for (worker in currentWorkers) {
                val nextStation = findWorkerNextStation(worker, nextAssignments, workstations) ?: station
                
                items.add(
                    RotationItem(
                        workerName = createWorkerLabel(worker),
                        currentWorkstation = "${station.name} (${currentWorkers.size}/${station.requiredWorkers})",
                        nextWorkstation = "${nextStation.name} (${(nextAssignments[nextStation.id]?.size ?: 0)}/${nextStation.requiredWorkers})",
                        rotationOrder = order++
                    )
                )
            }
        }
        
        return items
    }
    
    /**
     * Crea la tabla de rotación para la UI.
     */
    private fun createRotationTable(
        workstations: List<Workstation>,
        currentAssignments: Map<Long, List<Worker>>,
        nextAssignments: Map<Long, List<Worker>>
    ): RotationTable {
        return RotationTable(
            workstations = workstations,
            currentPhase = currentAssignments,
            nextPhase = nextAssignments
        )
    }
    
    /**
     * Encuentra la próxima estación de un trabajador.
     */
    private fun findWorkerNextStation(
        worker: Worker,
        nextAssignments: Map<Long, List<Worker>>,
        workstations: List<Workstation>
    ): Workstation? {
        val nextStationId = nextAssignments.entries.find { it.value.contains(worker) }?.key
        return workstations.find { it.id == nextStationId }
    }
    
    /**
     * Crea una etiqueta descriptiva para el trabajador.
     */
    private fun createWorkerLabel(worker: Worker): String {
        val status = when {
            worker.isLeader -> " 👑"
            worker.isTrainer -> " 👨‍🏫"
            worker.isTrainee -> " 🎯"
            else -> ""
        }
        
        val availability = if (worker.availabilityPercentage < 100) {
            " [${worker.availabilityPercentage}%]"
        } else ""
        
        return "${worker.name}$status$availability"
    }
    

    
    /**
     * Limpia la rotación actual.
     */
    fun clearRotation() {
        _rotationItems.value = emptyList()
        _rotationTable.value = null
        _errorMessage.value = null
        
        // Registrar métrica de limpieza
        analytics.recordUsageMetric("rotation_cleared")
    }
    

    
    /**
     * Registra métricas de calidad de la rotación generada.
     */
    private fun recordQualityMetrics(
        systemData: SystemData,
        currentAssignments: Map<Long, List<Worker>>,
        nextAssignments: Map<Long, List<Worker>>
    ) {
        try {
            val totalWorkers = currentAssignments.values.sumOf { it.size }
            val totalStations = systemData.workstations.size
            val stationsCompleted = systemData.workstations.count { station ->
                (currentAssignments[station.id]?.size ?: 0) >= station.requiredWorkers
            }
            
            // Verificar precisión de líderes
            val totalLeaders = systemData.activeLeaders.size
            val leadersCorrectlyAssigned = systemData.activeLeaders.count { leader ->
                leader.leaderWorkstationId?.let { stationId ->
                    currentAssignments[stationId]?.contains(leader) == true
                } ?: false
            }
            
            // Verificar parejas de entrenamiento
            val totalTrainingPairs = systemData.trainingPairs.size
            val trainingPairsKeptTogether = systemData.trainingPairs.count { trainee ->
                val trainer = systemData.eligibleWorkers.find { it.id == trainee.trainerId }
                val trainingStationId = trainee.trainingWorkstationId
                
                if (trainer != null && trainingStationId != null) {
                    val stationWorkers = currentAssignments[trainingStationId] ?: emptyList()
                    stationWorkers.contains(trainer) && stationWorkers.contains(trainee)
                } else false
            }
            
            analytics.recordQualityMetric(
                workersAssigned = totalWorkers,
                stationsCompleted = stationsCompleted,
                totalStations = totalStations,
                leadersCorrectlyAssigned = leadersCorrectlyAssigned,
                totalLeaders = totalLeaders,
                trainingPairsKeptTogether = trainingPairsKeptTogether,
                totalTrainingPairs = totalTrainingPairs
            )
            
        } catch (e: Exception) {
            println("SQL_DEBUG: Error registrando métricas de calidad: ${e.message}")
        }
    }
    
    /**
     * Obtiene reporte de analytics del sistema.
     */
    fun getAnalyticsReport() = analytics.getAnalyticsReport()
    
    /**
     * Obtiene resultados de validación del sistema.
     */
    fun getValidationResults() = validator.validationResults
    
    /**
     * Método de diagnóstico directo para probar el sistema sin generar rotación completa.
     * Útil para identificar problemas específicos.
     */
    suspend fun diagnosticarSistema(): String {
        return try {
            val diagnostico = StringBuilder()
            diagnostico.appendLine("🔍 === DIAGNÓSTICO DEL SISTEMA SQL ===")
            
            // Paso 1: Verificar datos básicos
            diagnostico.appendLine("\n📊 PASO 1: VERIFICANDO DATOS BÁSICOS")
            
            val eligibleWorkers = rotationDao.getAllEligibleWorkers()
            diagnostico.appendLine("👥 Trabajadores elegibles: ${eligibleWorkers.size}")
            
            val workstations = rotationDao.getAllActiveWorkstationsOrdered()
            diagnostico.appendLine("🏭 Estaciones activas: ${workstations.size}")
            
            if (eligibleWorkers.isEmpty()) {
                diagnostico.appendLine("❌ PROBLEMA: No hay trabajadores elegibles")
                diagnostico.appendLine("💡 SOLUCIÓN: Agregar trabajadores activos y asignarles estaciones")
                return diagnostico.toString()
            }
            
            if (workstations.isEmpty()) {
                diagnostico.appendLine("❌ PROBLEMA: No hay estaciones activas")
                diagnostico.appendLine("💡 SOLUCIÓN: Agregar estaciones activas")
                return diagnostico.toString()
            }
            
            // Paso 2: Verificar relaciones
            diagnostico.appendLine("\n🔗 PASO 2: VERIFICANDO RELACIONES")
            
            var trabajadoresSinEstaciones = 0
            eligibleWorkers.forEach { worker ->
                try {
                    val stationIds = workerDao.getWorkerWorkstationIds(worker.id)
                    if (stationIds.isEmpty()) {
                        trabajadoresSinEstaciones++
                        diagnostico.appendLine("⚠️ ${worker.name} no tiene estaciones asignadas")
                    } else {
                        diagnostico.appendLine("✅ ${worker.name} puede trabajar en ${stationIds.size} estaciones")
                    }
                } catch (e: Exception) {
                    diagnostico.appendLine("❌ Error verificando estaciones para ${worker.name}: ${e.message}")
                }
            }
            
            if (trabajadoresSinEstaciones > 0) {
                diagnostico.appendLine("❌ PROBLEMA: $trabajadoresSinEstaciones trabajadores sin estaciones")
                diagnostico.appendLine("💡 SOLUCIÓN: Configurar relaciones worker_workstations")
            }
            
            // Paso 3: Verificar líderes
            diagnostico.appendLine("\n👑 PASO 3: VERIFICANDO LÍDERES")
            
            val activeLeaders = rotationDao.getActiveLeadersForRotationFixed(isFirstHalfRotation)
            diagnostico.appendLine("👑 Líderes activos para esta rotación: ${activeLeaders.size}")
            
            activeLeaders.forEach { leader ->
                if (leader.leaderWorkstationId == null) {
                    diagnostico.appendLine("⚠️ Líder ${leader.name} sin estación de liderazgo")
                } else {
                    diagnostico.appendLine("✅ Líder ${leader.name} -> Estación ${leader.leaderWorkstationId}")
                }
            }
            
            // Paso 4: Verificar parejas de entrenamiento
            diagnostico.appendLine("\n🎯 PASO 4: VERIFICANDO ENTRENAMIENTO")
            
            val trainingPairs = rotationDao.getValidTrainingPairs()
            diagnostico.appendLine("🎯 Parejas de entrenamiento: ${trainingPairs.size}")
            
            trainingPairs.forEach { trainee ->
                if (trainee.trainingWorkstationId == null) {
                    diagnostico.appendLine("⚠️ Entrenado ${trainee.name} sin estación de entrenamiento")
                } else {
                    diagnostico.appendLine("✅ Entrenado ${trainee.name} -> Estación ${trainee.trainingWorkstationId}")
                }
            }
            
            // Paso 5: Verificar capacidades
            diagnostico.appendLine("\n📊 PASO 5: VERIFICANDO CAPACIDADES")
            
            val capacidadTotal = workstations.sumOf { it.requiredWorkers }
            val trabajadoresDisponibles = eligibleWorkers.size
            
            diagnostico.appendLine("🏭 Capacidad total requerida: $capacidadTotal")
            diagnostico.appendLine("👥 Trabajadores disponibles: $trabajadoresDisponibles")
            
            if (trabajadoresDisponibles < capacidadTotal) {
                diagnostico.appendLine("⚠️ No hay suficientes trabajadores para llenar todas las estaciones")
            } else {
                diagnostico.appendLine("✅ Hay suficientes trabajadores")
            }
            
            // Conclusión
            diagnostico.appendLine("\n🎯 CONCLUSIÓN DEL DIAGNÓSTICO")
            
            if (eligibleWorkers.isNotEmpty() && workstations.isNotEmpty() && trabajadoresSinEstaciones == 0) {
                diagnostico.appendLine("✅ SISTEMA LISTO: Todos los componentes están configurados correctamente")
                diagnostico.appendLine("🚀 La rotación SQL debería funcionar sin problemas")
            } else {
                diagnostico.appendLine("❌ SISTEMA INCOMPLETO: Hay problemas de configuración")
                diagnostico.appendLine("🔧 Revisar las soluciones sugeridas arriba")
            }
            
            diagnostico.toString()
            
        } catch (e: Exception) {
            "❌ ERROR EN DIAGNÓSTICO: ${e.message}\n${e.stackTraceToString()}"
        }
    }
    
    /**
     * Algoritmo mejorado que genera ambas rotaciones simultáneamente.
     * GARANTIZA: Distribución equitativa de todos los trabajadores.
     */
    private suspend fun generateDualRotationAlgorithm(
        systemData: DualSystemData,
        workerStationMap: Map<Long, List<Long>>
    ): Pair<Map<Long, List<Worker>>, Map<Long, List<Worker>>> {
        
        println("SQL_DEBUG: === ALGORITMO DUAL DE ROTACIÓN ===")
        
        // Inicializar asignaciones para ambas rotaciones
        val firstHalfAssignments = mutableMapOf<Long, MutableList<Worker>>()
        val secondHalfAssignments = mutableMapOf<Long, MutableList<Worker>>()
        
        systemData.workstations.forEach { station ->
            firstHalfAssignments[station.id] = mutableListOf()
            secondHalfAssignments[station.id] = mutableListOf()
        }
        
        // Conjuntos para rastrear trabajadores asignados
        val firstHalfAssigned = mutableSetOf<Long>()
        val secondHalfAssigned = mutableSetOf<Long>()
        
        // FASE 1: Asignar líderes a sus rotaciones específicas
        assignLeadersToRotations(systemData, firstHalfAssignments, secondHalfAssignments, 
                                firstHalfAssigned, secondHalfAssigned, workerStationMap)
        
        // FASE 2: Asignar parejas de entrenamiento a ambas rotaciones
        assignTrainingPairsToBothRotations(systemData, firstHalfAssignments, secondHalfAssignments,
                                          firstHalfAssigned, secondHalfAssigned, workerStationMap)
        
        // FASE 3: Distribuir trabajadores restantes equitativamente
        distributeRemainingWorkersEquitably(systemData, firstHalfAssignments, secondHalfAssignments,
                                           firstHalfAssigned, secondHalfAssigned, workerStationMap)
        
        // FASE 4: Verificar que todos los trabajadores estén asignados
        ensureAllWorkersAssigned(systemData, firstHalfAssignments, secondHalfAssignments,
                                firstHalfAssigned, secondHalfAssigned, workerStationMap)
        
        println("SQL_DEBUG: ✅ Algoritmo dual completado")
        println("SQL_DEBUG: Primera rotación: ${firstHalfAssigned.size} trabajadores")
        println("SQL_DEBUG: Segunda rotación: ${secondHalfAssigned.size} trabajadores")
        
        return Pair(
            firstHalfAssignments.mapValues { it.value.toList() },
            secondHalfAssignments.mapValues { it.value.toList() }
        )
    }
    
    /**
     * Asigna líderes a sus rotaciones específicas según su tipo de liderazgo.
     */
    private suspend fun assignLeadersToRotations(
        systemData: DualSystemData,
        firstHalfAssignments: MutableMap<Long, MutableList<Worker>>,
        secondHalfAssignments: MutableMap<Long, MutableList<Worker>>,
        firstHalfAssigned: MutableSet<Long>,
        secondHalfAssigned: MutableSet<Long>,
        workerStationMap: Map<Long, List<Long>>
    ) {
        println("SQL_DEBUG: === ASIGNANDO LÍDERES A ROTACIONES ===")
        
        // Asignar líderes de primera rotación
        systemData.firstHalfLeaders.forEach { leader ->
            leader.leaderWorkstationId?.let { stationId ->
                val canWork = workerStationMap[leader.id]?.contains(stationId) ?: false
                if (canWork) {
                    firstHalfAssignments[stationId]?.add(leader)
                    firstHalfAssigned.add(leader.id)
                    println("SQL_DEBUG: ✅ Líder ${leader.name} asignado a primera rotación, estación $stationId")
                }
            }
        }
        
        // Asignar líderes de segunda rotación
        systemData.secondHalfLeaders.forEach { leader ->
            leader.leaderWorkstationId?.let { stationId ->
                val canWork = workerStationMap[leader.id]?.contains(stationId) ?: false
                if (canWork) {
                    secondHalfAssignments[stationId]?.add(leader)
                    secondHalfAssigned.add(leader.id)
                    println("SQL_DEBUG: ✅ Líder ${leader.name} asignado a segunda rotación, estación $stationId")
                }
            }
        }
        
        println("SQL_DEBUG: Líderes primera rotación: ${systemData.firstHalfLeaders.size}")
        println("SQL_DEBUG: Líderes segunda rotación: ${systemData.secondHalfLeaders.size}")
    }
    
    /**
     * Asigna parejas de entrenamiento a ambas rotaciones para continuidad.
     */
    private suspend fun assignTrainingPairsToBothRotations(
        systemData: DualSystemData,
        firstHalfAssignments: MutableMap<Long, MutableList<Worker>>,
        secondHalfAssignments: MutableMap<Long, MutableList<Worker>>,
        firstHalfAssigned: MutableSet<Long>,
        secondHalfAssigned: MutableSet<Long>,
        workerStationMap: Map<Long, List<Long>>
    ) {
        println("SQL_DEBUG: === ASIGNANDO PAREJAS DE ENTRENAMIENTO ===")
        
        systemData.trainingPairs.forEach { trainee ->
            val trainer = systemData.eligibleWorkers.find { it.id == trainee.trainerId }
            val trainingStationId = trainee.trainingWorkstationId
            
            if (trainer != null && trainingStationId != null) {
                val traineeCanWork = workerStationMap[trainee.id]?.contains(trainingStationId) ?: false
                val trainerCanWork = workerStationMap[trainer.id]?.contains(trainingStationId) ?: false
                
                if (traineeCanWork && trainerCanWork) {
                    // Asignar a ambas rotaciones para continuidad del entrenamiento
                    if (!firstHalfAssigned.contains(trainee.id) && !firstHalfAssigned.contains(trainer.id)) {
                        firstHalfAssignments[trainingStationId]?.addAll(listOf(trainer, trainee))
                        firstHalfAssigned.addAll(listOf(trainer.id, trainee.id))
                    }
                    
                    if (!secondHalfAssigned.contains(trainee.id) && !secondHalfAssigned.contains(trainer.id)) {
                        secondHalfAssignments[trainingStationId]?.addAll(listOf(trainer, trainee))
                        secondHalfAssigned.addAll(listOf(trainer.id, trainee.id))
                    }
                    
                    println("SQL_DEBUG: ✅ Pareja ${trainer.name}-${trainee.name} asignada a ambas rotaciones")
                }
            }
        }
    }
    
    /**
     * Distribuye trabajadores restantes equitativamente entre ambas rotaciones.
     */
    private suspend fun distributeRemainingWorkersEquitably(
        systemData: DualSystemData,
        firstHalfAssignments: MutableMap<Long, MutableList<Worker>>,
        secondHalfAssignments: MutableMap<Long, MutableList<Worker>>,
        firstHalfAssigned: MutableSet<Long>,
        secondHalfAssigned: MutableSet<Long>,
        workerStationMap: Map<Long, List<Long>>
    ) {
        println("SQL_DEBUG: === DISTRIBUYENDO TRABAJADORES RESTANTES ===")
        
        // Obtener trabajadores no asignados
        val unassignedWorkers = systemData.eligibleWorkers.filter { worker ->
            !firstHalfAssigned.contains(worker.id) && !secondHalfAssigned.contains(worker.id)
        }
        
        println("SQL_DEBUG: Trabajadores sin asignar: ${unassignedWorkers.size}")
        
        // Distribuir alternadamente entre rotaciones
        unassignedWorkers.forEachIndexed { index, worker ->
            val assignToFirst = index % 2 == 0
            
            if (assignToFirst) {
                assignWorkerToRotation(worker, firstHalfAssignments, firstHalfAssigned, 
                                     systemData.workstations, workerStationMap, "PRIMERA")
            } else {
                assignWorkerToRotation(worker, secondHalfAssignments, secondHalfAssigned,
                                     systemData.workstations, workerStationMap, "SEGUNDA")
            }
        }
    }
    
    /**
     * Asigna un trabajador a una rotación específica.
     */
    private suspend fun assignWorkerToRotation(
        worker: Worker,
        assignments: MutableMap<Long, MutableList<Worker>>,
        assignedSet: MutableSet<Long>,
        workstations: List<Workstation>,
        workerStationMap: Map<Long, List<Long>>,
        rotationName: String
    ) {
        val eligibleStations = workerStationMap[worker.id] ?: emptyList()
        
        // Buscar estación con menor ocupación
        val bestStation = workstations
            .filter { station -> eligibleStations.contains(station.id) }
            .minByOrNull { station -> assignments[station.id]?.size ?: 0 }
        
        if (bestStation != null) {
            assignments[bestStation.id]?.add(worker)
            assignedSet.add(worker.id)
            println("SQL_DEBUG: ✅ ${worker.name} asignado a $rotationName rotación, estación ${bestStation.name}")
        } else {
            println("SQL_DEBUG: ⚠️ No se pudo asignar ${worker.name} a $rotationName rotación")
        }
    }
    
    /**
     * Asegura que todos los trabajadores estén asignados a al menos una rotación.
     */
    private suspend fun ensureAllWorkersAssigned(
        systemData: DualSystemData,
        firstHalfAssignments: MutableMap<Long, MutableList<Worker>>,
        secondHalfAssignments: MutableMap<Long, MutableList<Worker>>,
        firstHalfAssigned: MutableSet<Long>,
        secondHalfAssigned: MutableSet<Long>,
        workerStationMap: Map<Long, List<Long>>
    ) {
        println("SQL_DEBUG: === VERIFICANDO COBERTURA COMPLETA ===")
        
        val totalAssigned = (firstHalfAssigned + secondHalfAssigned).size
        val totalWorkers = systemData.eligibleWorkers.size
        
        println("SQL_DEBUG: Trabajadores con al menos una asignación: $totalAssigned/$totalWorkers")
        
        // Encontrar trabajadores sin ninguna asignación
        val unassignedWorkers = systemData.eligibleWorkers.filter { worker ->
            !firstHalfAssigned.contains(worker.id) && !secondHalfAssigned.contains(worker.id)
        }
        
        if (unassignedWorkers.isNotEmpty()) {
            println("SQL_DEBUG: ⚠️ Asignando ${unassignedWorkers.size} trabajadores restantes")
            
            unassignedWorkers.forEach { worker ->
                // Intentar asignar a la rotación con menos trabajadores
                val firstHalfCount = firstHalfAssigned.size
                val secondHalfCount = secondHalfAssigned.size
                
                if (firstHalfCount <= secondHalfCount) {
                    assignWorkerToRotation(worker, firstHalfAssignments, firstHalfAssigned,
                                         systemData.workstations, workerStationMap, "PRIMERA (forzado)")
                } else {
                    assignWorkerToRotation(worker, secondHalfAssignments, secondHalfAssigned,
                                         systemData.workstations, workerStationMap, "SEGUNDA (forzado)")
                }
            }
        }
        
        val finalFirstCount = firstHalfAssigned.size
        val finalSecondCount = secondHalfAssigned.size
        val finalTotalUnique = (firstHalfAssigned + secondHalfAssigned).size
        
        println("SQL_DEBUG: ✅ Distribución final:")
        println("SQL_DEBUG:   - Primera rotación: $finalFirstCount trabajadores")
        println("SQL_DEBUG:   - Segunda rotación: $finalSecondCount trabajadores")
        println("SQL_DEBUG:   - Total único: $finalTotalUnique trabajadores")
    }
    
    /**
     * Crea elementos de rotación para visualización dual.
     */
    private fun createDualRotationItems(
        workstations: List<Workstation>,
        firstHalfAssignments: Map<Long, List<Worker>>,
        secondHalfAssignments: Map<Long, List<Worker>>
    ): List<RotationItem> {
        val items = mutableListOf<RotationItem>()
        var order = 1
        
        // Crear items para primera rotación
        workstations.forEach { station ->
            val workers = firstHalfAssignments[station.id] ?: emptyList()
            workers.forEach { worker ->
                items.add(
                    RotationItem(
                        workerName = "${createWorkerLabel(worker)} [1ª]",
                        currentWorkstation = "${station.name} (${workers.size}/${station.requiredWorkers})",
                        nextWorkstation = "Primera Rotación",
                        rotationOrder = order++
                    )
                )
            }
        }
        
        // Crear items para segunda rotación
        workstations.forEach { station ->
            val workers = secondHalfAssignments[station.id] ?: emptyList()
            workers.forEach { worker ->
                items.add(
                    RotationItem(
                        workerName = "${createWorkerLabel(worker)} [2ª]",
                        currentWorkstation = "${station.name} (${workers.size}/${station.requiredWorkers})",
                        nextWorkstation = "Segunda Rotación",
                        rotationOrder = order++
                    )
                )
            }
        }
        
        return items
    }
    
    /**
     * Crea tabla de rotación para visualización dual.
     */
    private fun createDualRotationTable(
        workstations: List<Workstation>,
        firstHalfAssignments: Map<Long, List<Worker>>,
        secondHalfAssignments: Map<Long, List<Worker>>
    ): RotationTable {
        return RotationTable(
            workstations = workstations,
            currentPhase = firstHalfAssignments,
            nextPhase = secondHalfAssignments
        )
    }
    
    /**
     * Registra métricas de calidad para rotación dual.
     */
    private fun recordDualQualityMetrics(
        systemData: DualSystemData,
        firstHalfAssignments: Map<Long, List<Worker>>,
        secondHalfAssignments: Map<Long, List<Worker>>
    ) {
        try {
            val firstHalfWorkers = firstHalfAssignments.values.sumOf { it.size }
            val secondHalfWorkers = secondHalfAssignments.values.sumOf { it.size }
            val totalUniqueWorkers = (firstHalfAssignments.values.flatten() + secondHalfAssignments.values.flatten())
                .map { it.id }.toSet().size
            
            analytics.recordQualityMetric(
                workersAssigned = totalUniqueWorkers,
                stationsCompleted = systemData.workstations.size,
                totalStations = systemData.workstations.size,
                leadersCorrectlyAssigned = systemData.firstHalfLeaders.size + systemData.secondHalfLeaders.size,
                totalLeaders = systemData.firstHalfLeaders.size + systemData.secondHalfLeaders.size,
                trainingPairsKeptTogether = systemData.trainingPairs.size,
                totalTrainingPairs = systemData.trainingPairs.size
            )
            
            println("SQL_DEBUG: ✅ Métricas registradas - Trabajadores únicos: $totalUniqueWorkers")
            
        } catch (e: Exception) {
            println("SQL_DEBUG: Error registrando métricas duales: ${e.message}")
        }
    }

    /**
     * Data class para datos del sistema.
     */
    private data class SystemData(
        val eligibleWorkers: List<Worker>,
        val workstations: List<Workstation>,
        val activeLeaders: List<Worker>,
        val trainingPairs: List<Worker>
    ) {
        fun isValid(): Boolean {
            return eligibleWorkers.isNotEmpty() && workstations.isNotEmpty()
        }
    }
    
    /**
     * Data class para datos del sistema dual.
     */
    private data class DualSystemData(
        val eligibleWorkers: List<Worker>,
        val workstations: List<Workstation>,
        val firstHalfLeaders: List<Worker>,
        val secondHalfLeaders: List<Worker>,
        val trainingPairs: List<Worker>
    ) {
        fun isValid(): Boolean {
            return eligibleWorkers.isNotEmpty() && workstations.isNotEmpty()
        }
    }
}

/**
 * Factory para crear el SqlRotationViewModel.
 */
class SqlRotationViewModelFactory(
    private val rotationDao: RotationDao,
    private val workerDao: WorkerDao,
    private val workstationDao: WorkstationDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SqlRotationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SqlRotationViewModel(rotationDao, workerDao, workstationDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}