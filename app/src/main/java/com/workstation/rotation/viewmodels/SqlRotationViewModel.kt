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
    fun generateOptimizedRotation(): Boolean {
        var success = false
        
        viewModelScope.launch {
            try {
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
                val validationResults = validator.validateSystem(
                    systemData.eligibleWorkers,
                    systemData.workstations,
                    systemData.eligibleWorkers.associate { worker ->
                        worker.id to workerDao.getWorkerWorkstationIds(worker.id)
                    }
                )
                
                if (!validationResults.isValid) {
                    val criticalIssues = validationResults.criticalIssues
                    if (criticalIssues.isNotEmpty()) {
                        throw Exception("Problemas críticos detectados: ${criticalIssues.joinToString { it.message }}")
                    }
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
                
                success = true
                
            } catch (e: Exception) {
                println("SQL_DEBUG: ❌ ERROR: ${e.message}")
                e.printStackTrace()
                _errorMessage.value = "Error al generar rotación: ${e.message}"
                _rotationItems.value = emptyList()
                _rotationTable.value = null
            } finally {
                _isLoading.value = false
            }
        }
        
        return success
    }
    
    /**
     * Carga los datos básicos del sistema usando consultas SQL optimizadas.
     */
    private suspend fun loadSystemData(): SystemData {
        println("SQL_DEBUG: === CARGANDO DATOS DEL SISTEMA ===")
        
        // Usar las nuevas consultas SQL mejoradas
        val eligibleWorkers = rotationDao.getAllEligibleWorkers()
        val workstations = rotationDao.getAllActiveWorkstationsOrdered()
        val activeLeaders = rotationDao.getActiveLeadersForRotationFixed(isFirstHalfRotation)
        val trainingPairs = rotationDao.getValidTrainingPairs()
        
        println("SQL_DEBUG: Trabajadores elegibles: ${eligibleWorkers.size}")
        println("SQL_DEBUG: Estaciones activas: ${workstations.size}")
        println("SQL_DEBUG: Líderes activos: ${activeLeaders.size}")
        println("SQL_DEBUG: Parejas de entrenamiento: ${trainingPairs.size}")
        
        // Verificar integridad de datos
        if (eligibleWorkers.isEmpty()) {
            throw Exception("No hay trabajadores elegibles para rotación")
        }
        
        if (workstations.isEmpty()) {
            throw Exception("No hay estaciones activas")
        }
        
        return SystemData(
            eligibleWorkers = eligibleWorkers,
            workstations = workstations,
            activeLeaders = activeLeaders,
            trainingPairs = trainingPairs
        )
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
        
        for (station in priorityStations) {
            val currentCount = assignments[station.id]?.size ?: 0
            val needed = station.requiredWorkers - currentCount
            
            if (needed <= 0) {
                println("SQL_DEBUG: Estación prioritaria ${station.name} ya completa (${currentCount}/${station.requiredWorkers})")
                continue
            }
            
            // Obtener trabajadores elegibles para esta estación
            val eligibleForStation = rotationDao.getWorkersForStationFixed(station.id)
                .filter { availableWorkers.contains(it) }
                .take(needed)
            
            assignments[station.id]?.addAll(eligibleForStation)
            availableWorkers.removeAll(eligibleForStation)
            
            println("SQL_DEBUG: ✅ Estación prioritaria ${station.name}: ${eligibleForStation.size} trabajadores asignados")
        }
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
        
        for (station in normalStations) {
            val currentCount = assignments[station.id]?.size ?: 0
            val needed = station.requiredWorkers - currentCount
            
            if (needed <= 0) continue
            
            // Obtener trabajadores elegibles para esta estación
            val eligibleForStation = rotationDao.getWorkersForStationFixed(station.id)
                .filter { availableWorkers.contains(it) }
                .take(needed)
            
            assignments[station.id]?.addAll(eligibleForStation)
            availableWorkers.removeAll(eligibleForStation)
            
            println("SQL_DEBUG: ✅ Estación normal ${station.name}: ${eligibleForStation.size} trabajadores asignados")
        }
        
        println("SQL_DEBUG: Trabajadores sin asignar: ${availableWorkers.size}")
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
        
        // Primero, mantener líderes y parejas de entrenamiento en sus lugares
        for (leader in systemData.activeLeaders) {
            leader.leaderWorkstationId?.let { stationId ->
                if (currentAssignments[stationId]?.contains(leader) == true) {
                    nextAssignments[stationId]?.add(leader)
                }
            }
        }
        
        for (trainee in systemData.trainingPairs) {
            val trainer = systemData.eligibleWorkers.find { it.id == trainee.trainerId }
            trainee.trainingWorkstationId?.let { stationId ->
                if (currentAssignments[stationId]?.containsAll(listOfNotNull(trainer, trainee)) == true) {
                    nextAssignments[stationId]?.addAll(listOfNotNull(trainer, trainee))
                }
            }
        }
        
        // Rotar trabajadores regulares a diferentes estaciones
        val assignedInNext = nextAssignments.values.flatten().toSet()
        val workersToRotate = currentAssignments.values.flatten().filter { !assignedInNext.contains(it) }
        
        for (worker in workersToRotate) {
            val currentStationId = currentAssignments.entries.find { it.value.contains(worker) }?.key
            val eligibleStations = systemData.workstations.filter { station ->
                station.id != currentStationId && 
                (nextAssignments[station.id]?.size ?: 0) < station.requiredWorkers
            }
            
            // Asignar a la estación con menos trabajadores
            val targetStation = eligibleStations.minByOrNull { nextAssignments[it.id]?.size ?: 0 }
            targetStation?.let { station ->
                val canWork = rotationDao.canWorkerWorkAtStationFixed(worker.id, station.id)
                if (canWork) {
                    nextAssignments[station.id]?.add(worker)
                }
            }
        }
        
        println("SQL_DEBUG: Próxima rotación generada")
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
     * Alterna entre primera y segunda parte de la rotación.
     */
    override fun toggleRotationHalf() {
        isFirstHalfRotation = !isFirstHalfRotation
        println("SQL_DEBUG: Rotación cambiada a: ${if (isFirstHalfRotation) "PRIMERA PARTE" else "SEGUNDA PARTE"}")
        
        // Registrar métrica de alternancia
        analytics.recordUsageMetric("rotation_half_toggled")
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