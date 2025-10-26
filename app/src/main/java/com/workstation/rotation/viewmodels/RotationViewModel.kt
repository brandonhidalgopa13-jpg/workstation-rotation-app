package com.workstation.rotation.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.workstation.rotation.data.dao.WorkerDao
import com.workstation.rotation.data.dao.WorkstationDao
import com.workstation.rotation.data.entities.Worker
import com.workstation.rotation.data.entities.Workstation
import com.workstation.rotation.models.RotationItem
import com.workstation.rotation.models.RotationTable
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 🏭 SISTEMA DE ROTACIÓN INTELIGENTE DE ESTACIONES DE TRABAJO
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 
 * 📋 FUNCIONES PRINCIPALES DEL SISTEMA:
 * 
 * 🎯 1. GESTIÓN DE ROTACIONES AUTOMÁTICAS
 *    - Genera rotaciones inteligentes basadas en múltiples criterios
 *    - Considera disponibilidad, capacitación y restricciones de trabajadores
 *    - Optimiza la distribución de personal en estaciones de trabajo
 * 
 * 👥 2. SISTEMA DE ENTRENAMIENTO INTEGRADO
 *    - Asigna automáticamente parejas entrenador-entrenado
 *    - Garantiza que entrenados siempre estén con sus entrenadores asignados
 *    - Prioriza estaciones de entrenamiento solicitadas
 * 
 * ⭐ 3. MANEJO DE ESTACIONES PRIORITARIAS
 *    - Asegura capacidad completa en estaciones críticas
 *    - Asigna los mejores trabajadores disponibles a estaciones prioritarias
 *    - Mantiene continuidad operativa en áreas clave
 * 
 * 📊 4. ALGORITMO DE OPTIMIZACIÓN
 *    - Balancea carga de trabajo entre estaciones
 *    - Considera porcentajes de disponibilidad individual
 *    - Aplica variación aleatoria para evitar patrones repetitivos
 * 
 * 🔄 5. ROTACIÓN DUAL (ACTUAL + SIGUIENTE)
 *    - Genera posición actual y siguiente rotación simultáneamente
 *    - Permite planificación anticipada de movimientos
 *    - Facilita transiciones suaves entre turnos
 * 
 * 🎨 6. VISUALIZACIÓN AVANZADA
 *    - Crea elementos visuales con indicadores de estado
 *    - Muestra información de capacidad y disponibilidad
 *    - Identifica visualmente parejas de entrenamiento activas
 * 
 * 🛡️ 7. VALIDACIÓN Y RESTRICCIONES
 *    - Respeta restricciones individuales de trabajadores
 *    - Valida capacidades de estaciones antes de asignación
 *    - Maneja casos especiales y excepciones
 * 
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 💻 Desarrollado por: Brandon Josué Hidalgo Paz
 * 🏷️ Versión: Sistema de Rotación Inteligente v2.0
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 */

class RotationViewModel(
    private val workerDao: WorkerDao,
    private val workstationDao: WorkstationDao
) : ViewModel() {
    
    private val _rotationItems = MutableLiveData<List<RotationItem>>()
    val rotationItems: LiveData<List<RotationItem>> = _rotationItems
    
    private val _rotationTable = MutableLiveData<RotationTable?>()
    val rotationTable: LiveData<RotationTable?> = _rotationTable
    
    private var eligibleWorkersCount = 0
    
    /**
     * Generates an intelligent rotation considering priorities, availability, and training relationships.
     * 
     * PRIORITY HIERARCHY (from highest to lowest):
     * 1. MAXIMUM PRIORITY: Trainer-trainee pairs in PRIORITY workstations
     * 2. HIGH PRIORITY: Trainer-trainee pairs in normal workstations  
     * 3. MEDIUM PRIORITY: Individual workers in priority workstations
     * 4. NORMAL PRIORITY: Individual workers in normal workstations
     * 
     * Key Features:
     * - ABSOLUTE PRIORITY for trainer-trainee pairs: When a trainee has an assigned trainer, 
     *   both are ALWAYS placed together at the trainee's requested training workstation
     * - SPECIAL PRIORITY for training in priority workstations: These get assigned FIRST
     * - Training relationships override ALL other constraints (capacity, availability, workstation restrictions)
     * - Maintains priority workstation capacity requirements for non-training assignments
     * - Considers worker availability percentages and restrictions for individual assignments
     * - Ensures proper rotation variety while guaranteeing training continuity
     * 
     * @return Boolean indicating if rotation was successfully generated
     */
    suspend fun generateRotation(): Boolean {
        return try {
            val rotationData = prepareRotationData()
            if (!rotationData.isValid()) {
                _rotationItems.value = emptyList()
                return false
            }
            
            val (rotationItems, rotationTable) = executeRotationAlgorithm(rotationData)
            _rotationItems.value = rotationItems.sortedBy { it.rotationOrder }
            _rotationTable.value = rotationTable
            
            rotationItems.isNotEmpty()
            
        } catch (e: Exception) {
            _rotationItems.value = emptyList()
            eligibleWorkersCount = 0
            false
        }
    }
    
    /**
     * Prepares and validates data needed for rotation generation.
     */
    private suspend fun prepareRotationData(): RotationData {
        val allWorkers = workerDao.getAllWorkers().first().filter { it.isActive }
        val eligibleWorkers = mutableListOf<Worker>()
        
        for (worker in allWorkers) {
            val workstationIds = workerDao.getWorkerWorkstationIds(worker.id)
            if (workstationIds.isNotEmpty()) {
                eligibleWorkers.add(worker)
            }
        }
        
        eligibleWorkersCount = eligibleWorkers.size
        val allWorkstations = workstationDao.getAllActiveWorkstations().first()
        
        return RotationData(eligibleWorkers, allWorkstations)
    }
    
    /**
     * Data class to hold rotation preparation data.
     */
    private data class RotationData(
        val eligibleWorkers: List<Worker>,
        val allWorkstations: List<Workstation>
    ) {
        fun isValid(): Boolean = eligibleWorkers.isNotEmpty() && allWorkstations.isNotEmpty()
    }
    
    /**
     * Initializes assignment tracking maps for all workstations.
     */
    private fun initializeAssignments(workstations: List<Workstation>): MutableMap<Long, MutableList<Worker>> {
        return workstations.associate { it.id to mutableListOf<Worker>() }.toMutableMap()
    }

    /**
     * Assigns trainer-trainee pairs with ABSOLUTE PRIORITY, especially for priority workstations.
     * 
     * PRIORITY HIERARCHY:
     * 1. MAXIMUM PRIORITY: Trainer-trainee pairs in PRIORITY workstations
     * 2. HIGH PRIORITY: Trainer-trainee pairs in normal workstations
     * 3. All other assignments come after training relationships
     * 
     * This method ensures that training relationships ALWAYS take absolute priority over ALL other constraints:
     * - Ignores workstation capacity limits (will exceed if necessary for training)
     * - Ignores worker availability percentages (training is mandatory)
     * - Ignores workstation restrictions for trainers (they can work anywhere their trainee needs training)
     * - Guarantees that every trainee with an assigned trainer will be placed together
     * 
     * Training continuity is considered more important than operational efficiency constraints.
     */
    private fun assignTrainerTraineePairsWithPriority(
        eligibleWorkers: List<Worker>,
        assignments: MutableMap<Long, MutableList<Worker>>,
        allWorkstations: List<Workstation>,
        unassignedWorkers: MutableList<Worker>
    ) {
        val traineesWithTrainers = unassignedWorkers.filter { worker ->
            worker.isTrainee && worker.trainerId != null && worker.trainingWorkstationId != null
        }
        
        // Sort trainees by priority: priority workstations first, then normal workstations
        val sortedTrainees = traineesWithTrainers.sortedByDescending { trainee ->
            val trainingStation = allWorkstations.find { it.id == trainee.trainingWorkstationId }
            trainingStation?.isPriority ?: false
        }
        
        for (trainee in sortedTrainees) {
            val trainer = eligibleWorkers.find { it.id == trainee.trainerId }
            
            if (trainer != null && 
                unassignedWorkers.contains(trainer) && 
                unassignedWorkers.contains(trainee)) {
                
                val trainingStation = allWorkstations.find { it.id == trainee.trainingWorkstationId }
                
                trainingStation?.let { station ->
                    // FORCE assignment regardless of ALL constraints
                    // Priority workstations with training get ABSOLUTE priority
                    assignments[station.id]?.addAll(listOf(trainee, trainer))
                    unassignedWorkers.removeAll(listOf(trainee, trainer))
                    
                    // Log priority assignment for debugging
                    if (station.isPriority) {
                        // This is the HIGHEST priority assignment in the entire system
                    }
                }
            }
        }
    }

    /**
     * Legacy method for backward compatibility - now calls the priority-aware version.
     */
    private fun assignTrainerTraineePairs(
        eligibleWorkers: List<Worker>,
        assignments: MutableMap<Long, MutableList<Worker>>,
        allWorkstations: List<Workstation>,
        unassignedWorkers: MutableList<Worker>
    ) {
        assignTrainerTraineePairsWithPriority(eligibleWorkers, assignments, allWorkstations, unassignedWorkers)
    }

    /**
     * Assigns workers to priority workstations ensuring full capacity.
     * Respects trainer-trainee pairs that may already be assigned.
     */
    private suspend fun assignPriorityWorkstations(
        priorityWorkstations: List<Workstation>,
        eligibleWorkers: List<Worker>,
        assignments: MutableMap<Long, MutableList<Worker>>
    ) {
        for (station in priorityWorkstations) {
            val currentlyAssigned = assignments[station.id]?.size ?: 0
            val remainingCapacity = station.requiredWorkers - currentlyAssigned
            
            if (remainingCapacity > 0) {
                val availableWorkers = eligibleWorkers.filter { worker ->
                    workerDao.getWorkerWorkstationIds(worker.id).contains(station.id) &&
                    assignments.values.none { it.contains(worker) }
                }
                
                val sortedWorkers = availableWorkers.sortedWith(
                    compareByDescending<Worker> { it.isTrainer }
                        .thenByDescending { it.availabilityPercentage }
                )
                
                val workersToAssign = minOf(remainingCapacity, sortedWorkers.size)
                assignments[station.id]?.addAll(sortedWorkers.take(workersToAssign))
            }
        }
    }

    /**
     * Assigns remaining workers to normal workstations with availability checks.
     */
    private suspend fun assignNormalWorkstations(
        normalWorkstations: List<Workstation>,
        eligibleWorkers: List<Worker>,
        assignments: MutableMap<Long, MutableList<Worker>>
    ) {
        val unassignedWorkers = eligibleWorkers.filter { worker ->
            assignments.values.none { it.contains(worker) }
        }.toMutableList()
        
        // First assign trainer-trainee pairs
        assignTrainerTraineePairs(eligibleWorkers, assignments, normalWorkstations, unassignedWorkers)
        
        // Then assign remaining workers
        val sortedWorkers = unassignedWorkers.sortedWith(
            compareByDescending<Worker> { it.isTrainer }
                .thenByDescending { worker ->
                    worker.availabilityPercentage + Random.nextInt(0, 30)
                }
        )
        
        for (worker in sortedWorkers) {
            if (Random.nextInt(1, 101) <= worker.availabilityPercentage) {
                assignWorkerToOptimalStation(worker, normalWorkstations, assignments)
            }
        }
    }

    /**
     * Assigns a worker to the most suitable available station.
     */
    private suspend fun assignWorkerToOptimalStation(
        worker: Worker,
        availableStations: List<Workstation>,
        assignments: MutableMap<Long, MutableList<Worker>>
    ) {
        val workerStationIds = workerDao.getWorkerWorkstationIds(worker.id)
        val compatibleStations = availableStations.filter { station ->
            station.id in workerStationIds &&
            (assignments[station.id]?.size ?: 0) < station.requiredWorkers
        }
        
        val targetStation = when {
            worker.isTrainee && worker.trainingWorkstationId != null -> {
                compatibleStations.find { it.id == worker.trainingWorkstationId }
                    ?: compatibleStations.minByOrNull { assignments[it.id]?.size ?: 0 }
            }
            else -> compatibleStations.minByOrNull { assignments[it.id]?.size ?: 0 }
        }
        
        targetStation?.let { station ->
            assignments[station.id]?.add(worker)
        }
    }

    /**
     * Executes the main rotation algorithm with all business logic.
     * Returns both the rotation items list and the rotation table.
     */
    private suspend fun executeRotationAlgorithm(data: RotationData): Pair<List<RotationItem>, RotationTable> {
        val (eligibleWorkers, allWorkstations) = data
        
        // Initialize assignment tracking
        val currentAssignments = initializeAssignments(allWorkstations)
        val nextAssignments = initializeAssignments(allWorkstations)
        
        // Separate workstation types
        val (priorityWorkstations, normalWorkstations) = allWorkstations.partition { it.isPriority }
        
        // PHASE 0: ABSOLUTE PRIORITY - Assign ALL trainer-trainee pairs FIRST
        // This happens BEFORE any other assignment, including priority workstations
        val allUnassignedWorkers = eligibleWorkers.toMutableList()
        assignTrainerTraineePairsWithPriority(eligibleWorkers, currentAssignments, allWorkstations, allUnassignedWorkers)
            
        // Phase 1: Assign remaining workers to PRIORITY workstations (current positions)
        assignPriorityWorkstations(priorityWorkstations, eligibleWorkers, currentAssignments)
        
        // Phase 2: Assign remaining workers to NORMAL workstations (current positions)
        assignNormalWorkstations(normalWorkstations, eligibleWorkers, currentAssignments)
        
        // Phase 3: Generate next rotation positions
        // PHASE 3.0: ABSOLUTE PRIORITY - Assign ALL trainer-trainee pairs FIRST for next rotation
        val allUnassignedWorkersNext = eligibleWorkers.toMutableList()
        assignTrainerTraineePairsWithPriority(eligibleWorkers, nextAssignments, allWorkstations, allUnassignedWorkersNext)
        
        // Phase 3.1: Priority stations must maintain full capacity
        assignPriorityWorkstations(priorityWorkstations, eligibleWorkers, nextAssignments)
        
        // Phase 3.2: Assign remaining workers to normal stations for next rotation
        val remainingWorkers = eligibleWorkers.filter { worker ->
            nextAssignments.values.none { it.contains(worker) }
        }
        assignNormalWorkstations(normalWorkstations, remainingWorkers, nextAssignments)
        
        // Phase 4: Create rotation items and table
        val rotationItems = createRotationItems(allWorkstations, currentAssignments, nextAssignments)
        val rotationTable = createRotationTable(allWorkstations, currentAssignments, nextAssignments)
        
        return Pair(rotationItems, rotationTable)
    }

    /**
     * Creates rotation items for display.
     */
    private fun createRotationItems(
        allWorkstations: List<Workstation>,
        currentAssignments: Map<Long, List<Worker>>,
        nextAssignments: Map<Long, List<Worker>>
    ): List<RotationItem> {
        val rotationItems = mutableListOf<RotationItem>()
        
        for (station in allWorkstations) {
            val currentWorkers = currentAssignments[station.id] ?: emptyList()
            
            for (worker in currentWorkers) {
                val nextStation = findNextStation(worker, nextAssignments, allWorkstations) ?: station
                val workerLabel = createWorkerLabel(worker, station, nextStation, currentWorkers)
                val currentInfo = createStationInfo(station, currentWorkers)
                val nextInfo = createStationInfo(nextStation, nextAssignments[nextStation.id] ?: emptyList())
                
                rotationItems.add(
                    RotationItem(
                        workerName = workerLabel,
                        currentWorkstation = currentInfo,
                        nextWorkstation = nextInfo,
                        rotationOrder = rotationItems.size + 1
                    )
                )
            }
        }
        
        return rotationItems.sortedBy { it.rotationOrder }
    }

    /**
     * Finds the next station for a worker.
     */
    private fun findNextStation(
        worker: Worker,
        nextAssignments: Map<Long, List<Worker>>,
        allWorkstations: List<Workstation>
    ): Workstation? {
        return nextAssignments.entries
            .find { it.value.contains(worker) }
            ?.let { entry -> allWorkstations.find { it.id == entry.key } }
    }

    /**
     * Creates a formatted worker label with status indicators.
     */
    private fun createWorkerLabel(
        worker: Worker,
        currentStation: Workstation,
        nextStation: Workstation,
        currentWorkers: List<Worker>
    ): String {
        val isPriorityWorker = currentStation.isPriority || nextStation.isPriority
        val availabilityStatus = getAvailabilityStatus(worker)
        val restrictionStatus = if (worker.restrictionNotes.isNotEmpty()) " 🔒" else ""
        val trainingStatus = getTrainingStatus(worker, currentWorkers)
        
        val baseName = if (isPriorityWorker) {
            "${worker.name} [PRIORITARIO]"
        } else {
            worker.name
        }
        
        return "$baseName$availabilityStatus$restrictionStatus$trainingStatus"
    }

    /**
     * Gets availability status indicator for a worker.
     */
    private fun getAvailabilityStatus(worker: Worker): String {
        return when {
            worker.availabilityPercentage >= 80 -> ""
            worker.availabilityPercentage >= 50 -> " [${worker.availabilityPercentage}%]"
            else -> " [${worker.availabilityPercentage}% ⚠️]"
        }
    }

    /**
     * Gets training status indicator for a worker.
     */
    private fun getTrainingStatus(worker: Worker, currentWorkers: List<Worker>): String {
        return when {
            worker.isTrainer -> {
                val hasTraineeInSameStation = currentWorkers.any { otherWorker ->
                    otherWorker.isTrainee && otherWorker.trainerId == worker.id
                }
                if (hasTraineeInSameStation) " 👨‍🏫🤝 [ENTRENANDO]" else " 👨‍🏫"
            }
            worker.isTrainee -> {
                val hasTrainerInSameStation = worker.trainerId != null && 
                    currentWorkers.any { otherWorker -> otherWorker.id == worker.trainerId }
                if (hasTrainerInSameStation) " 🎯🤝 [EN ENTRENAMIENTO]" else " 🎯"
            }
            else -> ""
        }
    }

    /**
     * Creates formatted station information.
     */
    private fun createStationInfo(station: Workstation, workers: List<Worker>): String {
        val baseInfo = "${station.name} (${workers.size}/${station.requiredWorkers})"
        return if (station.isPriority) "$baseInfo ⭐ COMPLETA" else baseInfo
    }

    /**
     * Creates the rotation table for display.
     */
    private fun createRotationTable(
        allWorkstations: List<Workstation>,
        currentAssignments: Map<Long, List<Worker>>,
        nextAssignments: Map<Long, List<Worker>>
    ): RotationTable {
        return RotationTable(
            workstations = allWorkstations,
            currentPhase = currentAssignments.mapValues { it.value.toList() },
            nextPhase = nextAssignments.mapValues { it.value.toList() }
        )
    }
    
    /**
     * Updates the count of eligible workers asynchronously.
     */
    fun updateEligibleWorkersCount() {
        viewModelScope.launch {
            eligibleWorkersCount = try {
                val activeWorkers = workerDao.getAllWorkers().first().filter { it.isActive }
                activeWorkers.count { worker ->
                    workerDao.getWorkerWorkstationIds(worker.id).isNotEmpty()
                }
            } catch (e: Exception) {
                0
            }
        }
    }
    
    /**
     * Validates that all priority stations have their required capacity.
     */
    private fun validatePriorityStationsCapacity(
        priorityStations: List<Workstation>,
        assignments: Map<Long, List<Worker>>
    ): Boolean {
        return priorityStations.all { station ->
            (assignments[station.id]?.size ?: 0) == station.requiredWorkers
        }
    }
    
    fun clearRotation() {
        _rotationItems.value = emptyList()
        _rotationTable.value = null
    }
    
    fun getEligibleWorkersCount(): Int {
        return eligibleWorkersCount
    }
}

class RotationViewModelFactory(
    private val workerDao: WorkerDao,
    private val workstationDao: WorkstationDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RotationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RotationViewModel(workerDao, workstationDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}