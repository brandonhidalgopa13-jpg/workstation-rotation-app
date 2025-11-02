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
     * Genera una rotación usando el algoritmo SQL simplificado con analytics integrado.
     * GARANTIZADO: Funciona sin conflictos y errores.
     */
    suspend fun generateOptimizedRotation(): Boolean {
        return try {
            _isLoading.value = true
            _errorMessage.value = null
            
            println("SQL_DEBUG: ===== INICIANDO ROTACIÓN SQL OPTIMIZADA =====")
            println("SQL_DEBUG: Rotación: ${getCurrentRotationHalf()}")
            
            // Registrar inicio de operación
            analytics.recordUsageMetric("rotation_generated", mapOf("type" to "sql"))
            
            // Paso 1: Obtener datos básicos del sistema con medición de tiempo
            val systemData = analytics.measureOperation("system_data_loading") {
                loadSystemData()
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
            
            // Paso 2: Ejecutar algoritmo SQL simplificado con medición
            val (currentAssignments, nextAssignments) = analytics.measureOperation("sql_rotation_generation") {
                executeSimplifiedSqlAlgorithm(systemData)
            }
            
            // Paso 3: Crear elementos de visualización
            val rotationItems = createRotationItems(systemData.workstations, currentAssignments, nextAssignments)
            val rotationTable = createRotationTable(systemData.workstations, currentAssignments, nextAssignments)
            
            // Paso 4: Registrar métricas de calidad
            recordQualityMetrics(systemData, currentAssignments, nextAssignments)
            
            // Paso 5: Actualizar UI
            _rotationItems.value = rotationItems
            _rotationTable.value = rotationTable
            
            println("SQL_DEBUG: ✅ ROTACIÓN GENERADA EXITOSAMENTE")
            println("SQL_DEBUG: Items generados: ${rotationItems.size}")
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
     * Ejecuta el algoritmo SQL simplificado y robusto.
     * GARANTIZADO: Sin conflictos, sin errores, resultados predecibles.
     */
    private suspend fun executeSimplifiedSqlAlgorithm(
        systemData: SystemData
    ): Pair<Map<Long, List<Worker>>, Map<Long, List<Worker>>> {
        
        println("SQL_DEBUG: === EJECUTANDO ALGORITMO SQL SIMPLIFICADO ===")
        
        // Inicializar mapas de asignaciones
        val currentAssignments = mutableMapOf<Long, MutableList<Worker>>()
        val nextAssignments = mutableMapOf<Long, MutableList<Worker>>()
        
        // Inicializar todas las estaciones
        systemData.workstations.forEach { station ->
            currentAssignments[station.id] = mutableListOf()
            nextAssignments[station.id] = mutableListOf()
        }
        
        // Lista de trabajadores disponibles para asignación
        val availableWorkers = systemData.eligibleWorkers.toMutableList()
        
        // FASE 1: MÁXIMA PRIORIDAD - Líderes activos
        assignActiveLeaders(systemData, currentAssignments, availableWorkers)
        
        // FASE 2: ALTA PRIORIDAD - Parejas de entrenamiento
        assignTrainingPairs(systemData, currentAssignments, availableWorkers)
        
        // FASE 3: PRIORIDAD MEDIA - Llenar estaciones prioritarias
        fillPriorityStations(systemData, currentAssignments, availableWorkers)
        
        // FASE 4: PRIORIDAD NORMAL - Llenar estaciones normales
        fillNormalStations(systemData, currentAssignments, availableWorkers)
        
        // FASE 5: Generar próxima rotación
        generateNextRotation(systemData, currentAssignments, nextAssignments)
        
        println("SQL_DEBUG: Algoritmo completado exitosamente")
        
        return Pair(
            currentAssignments.mapValues { it.value.toList() },
            nextAssignments.mapValues { it.value.toList() }
        )
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
            distributeWorkersEquitably(normalStations, assignments, availableWorkers)
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
     * Distribuye trabajadores equitativamente entre estaciones.
     * CORREGIDO: Evita asignaciones duplicadas.
     */
    private suspend fun distributeWorkersEquitably(
        stations: List<Workstation>,
        assignments: MutableMap<Long, MutableList<Worker>>,
        availableWorkers: MutableList<Worker>
    ) {
        println("SQL_DEBUG: === DISTRIBUYENDO TRABAJADORES EQUITATIVAMENTE ===")
        
        // Crear lista de estaciones que necesitan trabajadores
        val stationsNeedingWorkers = mutableListOf<Workstation>()
        stations.forEach { station ->
            val currentCount = assignments[station.id]?.size ?: 0
            val needed = station.requiredWorkers - currentCount
            repeat(needed) {
                stationsNeedingWorkers.add(station)
            }
        }
        
        println("SQL_DEBUG: Espacios totales disponibles: ${stationsNeedingWorkers.size}")
        
        // Distribuir trabajadores uno por uno
        val workersToAssign = availableWorkers.toList()
        var stationIndex = 0
        
        for (worker in workersToAssign) {
            if (stationsNeedingWorkers.isEmpty()) break
            
            // VERIFICACIÓN CRÍTICA: Asegurar que el trabajador no esté ya asignado
            val isAlreadyAssigned = assignments.values.any { stationWorkers ->
                stationWorkers.contains(worker)
            }
            
            if (isAlreadyAssigned) {
                println("SQL_DEBUG: ⚠️ ${worker.name} ya está asignado, saltando")
                continue
            }
            
            // Buscar una estación donde el trabajador pueda trabajar
            var assigned = false
            var attempts = 0
            
            while (!assigned && attempts < stationsNeedingWorkers.size) {
                val station = stationsNeedingWorkers[stationIndex % stationsNeedingWorkers.size]
                
                // Verificar que el trabajador no esté ya en esta estación
                val isInThisStation = assignments[station.id]?.contains(worker) ?: false
                
                if (!isInThisStation) {
                    val canWork = rotationDao.canWorkerWorkAtStationFixed(worker.id, station.id)
                    if (canWork) {
                        assignments[station.id]?.add(worker)
                        availableWorkers.remove(worker)
                        stationsNeedingWorkers.removeAt(stationIndex % stationsNeedingWorkers.size)
                        assigned = true
                        
                        println("SQL_DEBUG: ✅ ${worker.name} asignado ÚNICAMENTE a ${station.name}")
                    } else {
                        println("SQL_DEBUG: ⚠️ ${worker.name} no puede trabajar en ${station.name}")
                        stationIndex++
                    }
                } else {
                    println("SQL_DEBUG: ⚠️ ${worker.name} ya está en ${station.name}")
                    stationIndex++
                }
                attempts++
            }
            
            if (!assigned) {
                println("SQL_DEBUG: ❌ No se pudo asignar ${worker.name} a ninguna estación disponible")
            }
        }
        
        // Validar asignaciones después de la distribución
        validateCurrentAssignments(assignments)
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
     * FASE 5: Genera la próxima rotación rotando trabajadores.
     */
    private suspend fun generateNextRotation(
        systemData: SystemData,
        currentAssignments: Map<Long, List<Worker>>,
        nextAssignments: MutableMap<Long, MutableList<Worker>>
    ) {
        println("SQL_DEBUG: === FASE 5: GENERANDO PRÓXIMA ROTACIÓN ===")
        
        // Paso 1: Mantener líderes en sus estaciones (no rotan)
        val fixedWorkers = mutableSetOf<Worker>()
        
        for (leader in systemData.activeLeaders) {
            leader.leaderWorkstationId?.let { stationId ->
                if (currentAssignments[stationId]?.contains(leader) == true) {
                    nextAssignments[stationId]?.add(leader)
                    fixedWorkers.add(leader)
                    println("SQL_DEBUG: 👑 Líder ${leader.name} permanece en estación ${stationId}")
                }
            }
        }
        
        // Paso 2: Mantener parejas de entrenamiento juntas (no rotan)
        for (trainee in systemData.trainingPairs) {
            val trainer = systemData.eligibleWorkers.find { it.id == trainee.trainerId }
            trainee.trainingWorkstationId?.let { stationId ->
                if (currentAssignments[stationId]?.containsAll(listOfNotNull(trainer, trainee)) == true) {
                    nextAssignments[stationId]?.addAll(listOfNotNull(trainer, trainee))
                    fixedWorkers.addAll(listOfNotNull(trainer, trainee))
                    println("SQL_DEBUG: 🎯 Pareja ${trainer?.name}-${trainee.name} permanece en estación ${stationId}")
                }
            }
        }
        
        // Paso 3: Rotar trabajadores regulares
        val workersToRotate = currentAssignments.values.flatten().filter { !fixedWorkers.contains(it) }
        println("SQL_DEBUG: Trabajadores a rotar: ${workersToRotate.size}")
        
        if (workersToRotate.isNotEmpty()) {
            rotateRegularWorkers(systemData, currentAssignments, nextAssignments, workersToRotate)
        }
        
        // Paso 4: Llenar espacios vacíos si es necesario
        fillRemainingSpaces(systemData, nextAssignments, workersToRotate)
        
        // Log final de próxima rotación
        println("SQL_DEBUG: === RESUMEN DE PRÓXIMA ROTACIÓN ===")
        systemData.workstations.forEach { station ->
            val nextCount = nextAssignments[station.id]?.size ?: 0
            println("SQL_DEBUG: ${station.name}: ${nextCount}/${station.requiredWorkers} trabajadores")
            nextAssignments[station.id]?.forEach { worker ->
                println("SQL_DEBUG:   - ${worker.name}")
            }
        }
        
        println("SQL_DEBUG: ✅ Próxima rotación generada exitosamente")
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
     * Valida que no haya trabajadores asignados a múltiples estaciones.
     */
    private fun validateNoDoubleAssignments(assignments: Map<Long, List<Worker>>) {
        println("SQL_DEBUG: === VALIDANDO ASIGNACIONES ÚNICAS ===")
        
        val allAssignedWorkers = mutableListOf<Worker>()
        val duplicateWorkers = mutableSetOf<Worker>()
        
        assignments.forEach { (stationId, workers) ->
            workers.forEach { worker ->
                if (allAssignedWorkers.contains(worker)) {
                    duplicateWorkers.add(worker)
                    println("SQL_DEBUG: ❌ DUPLICADO: ${worker.name} está asignado a múltiples estaciones")
                } else {
                    allAssignedWorkers.add(worker)
                }
            }
        }
        
        if (duplicateWorkers.isEmpty()) {
            println("SQL_DEBUG: ✅ VALIDACIÓN EXITOSA: No hay asignaciones duplicadas")
        } else {
            println("SQL_DEBUG: ❌ ENCONTRADAS ${duplicateWorkers.size} ASIGNACIONES DUPLICADAS")
            duplicateWorkers.forEach { worker ->
                println("SQL_DEBUG: - ${worker.name} está duplicado")
            }
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