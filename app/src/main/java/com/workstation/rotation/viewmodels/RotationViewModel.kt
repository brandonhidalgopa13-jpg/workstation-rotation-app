package com.workstation.rotation.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.workstation.rotation.data.dao.WorkerDao
import com.workstation.rotation.data.dao.WorkstationDao
import com.workstation.rotation.data.dao.WorkerRestrictionDao
import com.workstation.rotation.data.entities.Worker
import com.workstation.rotation.data.entities.Workstation
import com.workstation.rotation.data.entities.RestrictionType
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
    private val workstationDao: WorkstationDao,
    private val workerRestrictionDao: WorkerRestrictionDao
) : ViewModel() {
    
    // Cache for worker-workstation relationships to avoid multiple DB calls
    private var workerWorkstationCache: Map<Long, List<Long>> = emptyMap()
    
    /**
     * Gets worker workstation IDs from cache or database.
     */
    private suspend fun getWorkerWorkstationIds(workerId: Long): List<Long> {
        val cachedIds = workerWorkstationCache[workerId]
        return if (cachedIds != null) {
            cachedIds
        } else {
            // Si no está en caché, obtener de la base de datos
            val ids = workerDao.getWorkerWorkstationIds(workerId)
            // Debug: Log para verificar las asignaciones
            println("DEBUG: Worker $workerId has workstation IDs: $ids")
            ids
        }
    }
    
    private val _rotationItems = MutableLiveData<List<RotationItem>>()
    val rotationItems: LiveData<List<RotationItem>> = _rotationItems
    
    private val _rotationTable = MutableLiveData<RotationTable?>()
    val rotationTable: LiveData<RotationTable?> = _rotationTable
    
    private var eligibleWorkersCount = 0
    
    /**
     * Generates an intelligent rotation considering priorities, availability, training relationships, and rotation cycles.
     * 
     * PRIORITY HIERARCHY (from highest to lowest):
     * 1. MAXIMUM PRIORITY: Trainer-trainee pairs in PRIORITY workstations
     * 2. HIGH PRIORITY: Trainer-trainee pairs in normal workstations  
     * 3. MEDIUM-HIGH PRIORITY: Trained workers needing forced rotation (been in same station too long)
     * 4. MEDIUM PRIORITY: Individual workers in priority workstations
     * 5. NORMAL PRIORITY: Individual workers in normal workstations
     * 
     * Key Features:
     * - ABSOLUTE PRIORITY for trainer-trainee pairs: When a trainee has an assigned trainer, 
     *   both are ALWAYS placed together at the trainee's requested training workstation
     * - SPECIAL PRIORITY for training in priority workstations: These get assigned FIRST
     * - FORCED ROTATION for trained workers: Workers who are not trainers/trainees and have been
     *   in the same station for multiple rotations are forced to change stations
     * - Training relationships override ALL other constraints (capacity, availability, workstation restrictions)
     * - Maintains priority workstation capacity requirements for non-training assignments
     * - Considers worker availability percentages and restrictions for individual assignments
     * - Ensures proper rotation variety while guaranteeing training continuity
     * - Tracks rotation history to prevent stagnation and promote skill development
     * 
     * @return Boolean indicating if rotation was successfully generated
     */
    suspend fun generateRotation(): Boolean {
        return try {
            println("DEBUG: ===== INICIANDO GENERACIÓN DE ROTACIÓN =====")
            
            // Verificar integridad de datos antes de proceder
            verifyDataIntegrity()
            
            val rotationData = prepareRotationData()
            if (!rotationData.isValid()) {
                println("DEBUG: ERROR - Rotation data is not valid")
                _rotationItems.value = emptyList()
                _rotationTable.value = null
                return false
            }
            
            val (rotationItems, rotationTable) = executeRotationAlgorithm(rotationData)
            _rotationItems.value = rotationItems.sortedBy { it.rotationOrder }
            _rotationTable.value = rotationTable
            
            println("DEBUG: Rotation generation completed - ${rotationItems.size} items generated")
            println("DEBUG: ============================================")
            
            rotationItems.isNotEmpty()
            
        } catch (e: Exception) {
            println("DEBUG: ERROR in generateRotation: ${e.message}")
            e.printStackTrace()
            _rotationItems.value = emptyList()
            _rotationTable.value = null
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
        
        // Pre-load all worker-workstation relationships to avoid multiple DB calls
        val workerWorkstationMap = mutableMapOf<Long, List<Long>>()
        
        println("DEBUG: Preparing rotation data for ${allWorkers.size} active workers")
        
        for (worker in allWorkers) {
            val workstationIds = workerDao.getWorkerWorkstationIds(worker.id)
            workerWorkstationMap[worker.id] = workstationIds
            
            // Debug: Log worker assignments
            println("DEBUG: Worker ${worker.name} (ID: ${worker.id}) assigned to ${workstationIds.size} workstations: $workstationIds")
            
            if (workstationIds.isNotEmpty()) {
                eligibleWorkers.add(worker)
            } else {
                println("DEBUG: Worker ${worker.name} has NO workstation assignments - excluded from rotation")
            }
        }
        
        // Cache the relationships for use in other functions
        workerWorkstationCache = workerWorkstationMap
        
        eligibleWorkersCount = eligibleWorkers.size
        val allWorkstations = workstationDao.getAllActiveWorkstations().first()
        
        println("DEBUG: Found ${eligibleWorkers.size} eligible workers and ${allWorkstations.size} active workstations")
        
        return RotationData(eligibleWorkers, allWorkstations)
    }
    
    /**
     * Data class to hold rotation preparation data.
     */
    private data class RotationData(
        val eligibleWorkers: List<Worker>,
        val allWorkstations: List<Workstation>
    ) {
        fun isValid(): Boolean {
            val hasWorkers = eligibleWorkers.isNotEmpty()
            val hasWorkstations = allWorkstations.isNotEmpty()
            
            println("DEBUG: RotationData validation - Workers: $hasWorkers (${eligibleWorkers.size}), Workstations: $hasWorkstations (${allWorkstations.size})")
            
            return hasWorkers && hasWorkstations
        }
    }
    
    /**
     * Verifica la integridad de los datos de trabajadores y estaciones.
     */
    private suspend fun verifyDataIntegrity() {
        println("DEBUG: ===== VERIFICACIÓN DE INTEGRIDAD DE DATOS =====")
        
        // Verificar trabajadores activos
        val allWorkers = workerDao.getAllWorkers().first()
        val activeWorkers = allWorkers.filter { it.isActive }
        println("DEBUG: Total workers: ${allWorkers.size}, Active workers: ${activeWorkers.size}")
        
        // Verificar estaciones activas
        val allWorkstations = workstationDao.getAllActiveWorkstations().first()
        println("DEBUG: Active workstations: ${allWorkstations.size}")
        allWorkstations.forEach { station ->
            println("DEBUG: Workstation: ${station.name} (ID: ${station.id}, Required: ${station.requiredWorkers}, Priority: ${station.isPriority})")
        }
        
        // Verificar asignaciones trabajador-estación
        var workersWithAssignments = 0
        var totalAssignments = 0
        
        for (worker in activeWorkers) {
            val assignments = workerDao.getWorkerWorkstationIds(worker.id)
            if (assignments.isNotEmpty()) {
                workersWithAssignments++
                totalAssignments += assignments.size
                println("DEBUG: Worker ${worker.name} assigned to ${assignments.size} stations: $assignments")
            } else {
                println("DEBUG: WARNING - Worker ${worker.name} has NO station assignments!")
            }
        }
        
        println("DEBUG: Workers with assignments: $workersWithAssignments/${activeWorkers.size}")
        println("DEBUG: Total assignments: $totalAssignments")
        println("DEBUG: ================================================")
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
     * Assigns trainer-trainee pairs to the NEXT rotation with ABSOLUTE PRIORITY.
     * 
     * CRITICAL BUSINESS RULE: Trainer-trainee pairs MUST stay together in BOTH rotations
     * until the trainee is certified. This ensures continuous training and mentorship.
     * 
     * This function ensures that:
     * - Trainer and trainee are ALWAYS assigned to the SAME station in next rotation
     * - They are assigned to their designated training workstation
     * - Training continuity is maintained across rotation cycles
     * - No other constraints can separate a training pair
     */
    private fun assignTrainerTraineePairsToNextRotation(
        eligibleWorkers: List<Worker>,
        nextAssignments: MutableMap<Long, MutableList<Worker>>,
        allWorkstations: List<Workstation>,
        remainingWorkers: MutableList<Worker>
    ) {
        val traineesWithTrainers = remainingWorkers.filter { worker ->
            worker.isTrainee && worker.trainerId != null && worker.trainingWorkstationId != null && !worker.isCertified
        }
        
        // Sort trainees by priority: priority workstations first, then normal workstations
        val sortedTrainees = traineesWithTrainers.sortedByDescending { trainee ->
            val trainingStation = allWorkstations.find { it.id == trainee.trainingWorkstationId }
            trainingStation?.isPriority ?: false
        }
        
        for (trainee in sortedTrainees) {
            val trainer = eligibleWorkers.find { it.id == trainee.trainerId }
            
            if (trainer != null && 
                remainingWorkers.contains(trainer) && 
                remainingWorkers.contains(trainee)) {
                
                val trainingStation = allWorkstations.find { it.id == trainee.trainingWorkstationId }
                
                trainingStation?.let { station ->
                    // FORCE assignment to next rotation regardless of ALL constraints
                    // Training pairs have ABSOLUTE PRIORITY over capacity limits
                    nextAssignments[station.id]?.addAll(listOf(trainee, trainer))
                    remainingWorkers.removeAll(listOf(trainee, trainer))
                    
                    // CRITICAL: Training pairs override capacity limits
                    // If this causes the station to exceed capacity, that's acceptable
                    // because training continuity is more important than operational efficiency
                }
            }
        }
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
        println("DEBUG: Assigning ${priorityWorkstations.size} priority workstations")
        
        for (station in priorityWorkstations) {
            val currentlyAssigned = assignments[station.id]?.size ?: 0
            val remainingCapacity = station.requiredWorkers - currentlyAssigned
            
            println("DEBUG: Priority station ${station.name}: ${currentlyAssigned}/${station.requiredWorkers} assigned, remaining capacity: $remainingCapacity")
            
            if (remainingCapacity > 0) {
                val availableWorkers = eligibleWorkers.filter { worker ->
                    val workerStations = getWorkerWorkstationIds(worker.id)
                    val canWorkHere = workerStations.contains(station.id)
                    val alreadyAssigned = assignments.values.any { it.contains(worker) }
                    
                    println("DEBUG: Worker ${worker.name} - can work at ${station.name}: $canWorkHere, already assigned: $alreadyAssigned")
                    
                    canWorkHere && !alreadyAssigned
                }
                
                println("DEBUG: Found ${availableWorkers.size} available workers for ${station.name}")
                
                // Separate leaders for this station from other workers
                val leadersForThisStation = availableWorkers.filter { worker ->
                    worker.isLeader && worker.leaderWorkstationId == station.id
                }
                val otherWorkers = availableWorkers.filter { worker ->
                    !(worker.isLeader && worker.leaderWorkstationId == station.id)
                }
                
                // Sort workers with leaders getting highest priority
                val sortedWorkers = (leadersForThisStation + otherWorkers.sortedWith(
                    compareByDescending<Worker> { it.isTrainer }
                        .thenByDescending { it.availabilityPercentage }
                ))
                
                val workersToAssign = minOf(remainingCapacity, sortedWorkers.size)
                val assignedWorkers = sortedWorkers.take(workersToAssign)
                
                assignments[station.id]?.addAll(assignedWorkers)
                
                println("DEBUG: Assigned ${assignedWorkers.size} workers to ${station.name}: ${assignedWorkers.map { it.name }}")
                if (leadersForThisStation.isNotEmpty()) {
                    println("DEBUG: Leaders assigned to ${station.name}: ${leadersForThisStation.map { it.name }}")
                }
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
        
        println("DEBUG: Assigning ${normalWorkstations.size} normal workstations to ${unassignedWorkers.size} unassigned workers")
        
        // First assign trainer-trainee pairs
        assignTrainerTraineePairs(eligibleWorkers, assignments, normalWorkstations, unassignedWorkers)
        
        // Then handle forced rotation for trained workers
        assignTrainedWorkersWithRotation(normalWorkstations, unassignedWorkers, assignments)
        
        // Finally assign remaining workers with leadership priority
        val sortedWorkers = unassignedWorkers.sortedWith(
            compareByDescending<Worker> { it.isLeader }
                .thenByDescending { it.isTrainer }
                .thenByDescending { worker ->
                    worker.availabilityPercentage + Random.nextInt(0, 30)
                }
        )
        
        println("DEBUG: Attempting to assign ${sortedWorkers.size} remaining workers to normal stations")
        
        for (worker in sortedWorkers) {
            val availabilityRoll = Random.nextInt(1, 101)
            val isAvailable = availabilityRoll <= worker.availabilityPercentage
            
            println("DEBUG: Worker ${worker.name} availability check: $availabilityRoll <= ${worker.availabilityPercentage} = $isAvailable")
            
            if (isAvailable) {
                assignWorkerToOptimalStation(worker, normalWorkstations, assignments)
            } else {
                println("DEBUG: Worker ${worker.name} not available for this rotation (availability check failed)")
            }
        }
    }

    /**
     * Handles forced rotation for trained workers who have been in the same station too long.
     * Trained workers (not trainers, not trainees) must rotate after being in the same station
     * for half the rotation cycle to ensure skill development and prevent stagnation.
     */
    private suspend fun assignTrainedWorkersWithRotation(
        availableStations: List<Workstation>,
        unassignedWorkers: MutableList<Worker>,
        assignments: MutableMap<Long, MutableList<Worker>>
    ) {
        // Get trained workers who need to rotate (sorted by priority)
        val trainedWorkersNeedingRotation = unassignedWorkers
            .filter { it.isTrainedWorker() }
            .sortedByDescending { it.getRotationPriority() }
        
        for (worker in trainedWorkersNeedingRotation) {
            val workerStationIds = workerDao.getWorkerWorkstationIds(worker.id)
            val compatibleStations = availableStations.filter { station ->
                station.id in workerStationIds &&
                (assignments[station.id]?.size ?: 0) < station.requiredWorkers
            }
            
            val targetStation = if (worker.needsToRotate()) {
                // Force rotation: avoid current station if possible
                val alternativeStations = compatibleStations.filter { 
                    it.id != worker.currentWorkstationId 
                }
                
                if (alternativeStations.isNotEmpty()) {
                    // Assign to different station with least workers
                    alternativeStations.minByOrNull { assignments[it.id]?.size ?: 0 }
                } else {
                    // If no alternatives, assign to any compatible station
                    compatibleStations.minByOrNull { assignments[it.id]?.size ?: 0 }
                }
            } else {
                // Normal assignment for trained workers
                compatibleStations.minByOrNull { assignments[it.id]?.size ?: 0 }
            }
            
            targetStation?.let { station ->
                assignments[station.id]?.add(worker)
                unassignedWorkers.remove(worker)
                
                // Update rotation tracking
                updateWorkerRotationTracking(worker, station.id)
            }
        }
    }

    /**
     * Updates rotation tracking for a worker assigned to a station.
     */
    private suspend fun updateWorkerRotationTracking(worker: Worker, stationId: Long) {
        val currentTime = System.currentTimeMillis()
        workerDao.updateWorkerRotation(worker.id, stationId, currentTime)
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
        
        println("DEBUG: Assigning worker ${worker.name} to optimal station")
        println("DEBUG: Worker ${worker.name} can work at stations: $workerStationIds")
        
        val compatibleStations = availableStations.filter { station ->
            val canWorkHere = station.id in workerStationIds
            val hasCapacity = (assignments[station.id]?.size ?: 0) < station.requiredWorkers
            val currentAssigned = assignments[station.id]?.size ?: 0
            
            println("DEBUG: Station ${station.name} - can work: $canWorkHere, capacity: $currentAssigned/${station.requiredWorkers}, has space: $hasCapacity")
            
            canWorkHere && hasCapacity
        }
        
        println("DEBUG: Found ${compatibleStations.size} compatible stations for ${worker.name}")
        
        val targetStation = when {
            worker.isLeader && worker.leaderWorkstationId != null -> {
                println("DEBUG: Worker ${worker.name} is leader, looking for leadership station ID: ${worker.leaderWorkstationId}")
                val leadershipStation = compatibleStations.find { it.id == worker.leaderWorkstationId }
                if (leadershipStation != null) {
                    println("DEBUG: Found leadership station ${leadershipStation.name} for leader ${worker.name}")
                    leadershipStation
                } else {
                    println("DEBUG: Leadership station not available, using station with least workers")
                    compatibleStations.minByOrNull { assignments[it.id]?.size ?: 0 }
                }
            }
            worker.isTrainee && worker.trainingWorkstationId != null -> {
                println("DEBUG: Worker ${worker.name} is trainee, looking for training station ID: ${worker.trainingWorkstationId}")
                val trainingStation = compatibleStations.find { it.id == worker.trainingWorkstationId }
                if (trainingStation != null) {
                    println("DEBUG: Found training station ${trainingStation.name} for trainee ${worker.name}")
                    trainingStation
                } else {
                    println("DEBUG: Training station not available, using station with least workers")
                    compatibleStations.minByOrNull { assignments[it.id]?.size ?: 0 }
                }
            }
            else -> {
                val selected = compatibleStations.minByOrNull { assignments[it.id]?.size ?: 0 }
                println("DEBUG: Selected station with least workers: ${selected?.name}")
                selected
            }
        }
        
        targetStation?.let { station ->
            assignments[station.id]?.add(worker)
            println("DEBUG: Successfully assigned ${worker.name} to ${station.name}")
            // Update rotation tracking for all workers
            updateWorkerRotationTracking(worker, station.id)
        } ?: run {
            println("DEBUG: ERROR - Could not find suitable station for worker ${worker.name}")
        }
    }

    /**
     * Assigns workers with forced rotation - ensures workers change stations between current and next rotation.
     * This is the core improvement that guarantees true rotation of personnel.
     * 
     * ROTATION RULES:
     * 1. Trainer-trainee pairs are EXEMPT from forced rotation (they stay together)
     * 2. All other workers MUST change stations between current and next rotation
     * 3. Priority stations maintain their capacity requirements
     * 4. Workers can only be assigned to stations they're qualified for
     * 5. If no alternative station is available, worker stays in current (exception case)
     */
    private suspend fun assignWorkersWithForcedRotation(
        eligibleWorkers: List<Worker>,
        currentAssignments: Map<Long, List<Worker>>,
        nextAssignments: MutableMap<Long, MutableList<Worker>>,
        allWorkstations: List<Workstation>,
        priorityWorkstations: List<Workstation>
    ) {
        // Get all workers who are already assigned in current rotation
        val workersInCurrentRotation = currentAssignments.values.flatten()
        
        // Separate workers by type
        val traineeWorkers = workersInCurrentRotation.filter { it.isTrainee && it.trainerId != null }
        val trainerWorkers = workersInCurrentRotation.filter { worker ->
            worker.isTrainer && traineeWorkers.any { trainee -> trainee.trainerId == worker.id }
        }
        val regularWorkers = workersInCurrentRotation.filter { worker ->
            !traineeWorkers.contains(worker) && !trainerWorkers.contains(worker)
        }
        
        // Phase 1: Handle priority stations first (maintain capacity)
        assignPriorityStationsWithRotation(
            priorityWorkstations,
            regularWorkers,
            currentAssignments,
            nextAssignments,
            allWorkstations
        )
        
        // Phase 2: Handle remaining regular workers with forced rotation
        assignRemainingWorkersWithRotation(
            regularWorkers,
            currentAssignments,
            nextAssignments,
            allWorkstations
        )
        
        // Phase 3: Fill any remaining capacity with unassigned workers
        fillRemainingCapacity(
            eligibleWorkers,
            nextAssignments,
            allWorkstations
        )
    }
    
    /**
     * Assigns workers to priority stations ensuring capacity while forcing rotation.
     */
    private suspend fun assignPriorityStationsWithRotation(
        priorityWorkstations: List<Workstation>,
        regularWorkers: List<Worker>,
        currentAssignments: Map<Long, List<Worker>>,
        nextAssignments: MutableMap<Long, MutableList<Worker>>,
        allWorkstations: List<Workstation>
    ) {
        for (station in priorityWorkstations) {
            val currentCapacity = nextAssignments[station.id]?.size ?: 0
            val remainingCapacity = station.requiredWorkers - currentCapacity
            
            if (remainingCapacity > 0) {
                // Get workers who can work at this station but are NOT currently assigned to it
                val eligibleWorkers = getEligibleWorkersForStation(regularWorkers, station.id)
                val availableWorkers = eligibleWorkers.filter { worker ->
                    // Worker is NOT currently assigned to this station
                    !isWorkerCurrentlyAssignedToStation(worker, station.id, currentAssignments) &&
                    // Worker is not already assigned to next rotation
                    !nextAssignments.values.any { it.contains(worker) }
                }
                
                // Sort by availability and trainer status
                val sortedWorkers = availableWorkers.sortedWith(
                    compareByDescending<Worker> { it.isTrainer }
                        .thenByDescending { it.availabilityPercentage }
                )
                
                // Assign workers up to remaining capacity
                val workersToAssign = sortedWorkers.take(remainingCapacity)
                nextAssignments[station.id]?.addAll(workersToAssign)
            }
        }
    }
    
    /**
     * Determines if a worker can rotate based on availability and general restrictions.
     * Workers who cannot rotate will stay in their current station.
     */
    private fun canWorkerRotate(worker: Worker): Boolean {
        // Only workers with very low availability (below 30%) should stay in place
        if (worker.availabilityPercentage < 30) return false
        
        // Most workers can rotate freely - specific station restrictions are handled separately
        return true
    }
    
    /**
     * Verifica si un trabajador puede trabajar en una estación específica.
     * Considera las restricciones específicas por estación.
     */
    private suspend fun canWorkerWorkAtStation(workerId: Long, workstationId: Long): Boolean {
        // Verificar si el trabajador tiene restricciones prohibitivas para esta estación
        val hasProhibitedRestriction = workerRestrictionDao.hasRestriction(
            workerId, 
            workstationId, 
            RestrictionType.PROHIBITED
        )
        
        if (hasProhibitedRestriction) {
            return false
        }
        
        // Verificar si el trabajador está asignado a esta estación
        val workerWorkstations = getWorkerWorkstationIds(workerId)
        return workerWorkstations.contains(workstationId)
    }
    
    /**
     * Obtiene los trabajadores elegibles para una estación específica.
     * Filtra por restricciones específicas y asignaciones de estación.
     */
    private suspend fun getEligibleWorkersForStation(
        workers: List<Worker>, 
        workstationId: Long
    ): List<Worker> {
        return workers.filter { worker ->
            // Verificar de forma síncrona si puede trabajar en esta estación
            runCatching {
                // Usar una versión simplificada para evitar suspend en filter
                val workerWorkstations = workerWorkstationCache[worker.id] ?: emptyList()
                workerWorkstations.contains(workstationId)
            }.getOrDefault(false)
        }
    }

    /**
     * Assigns remaining workers ensuring they rotate to different stations.
     * Workers who cannot rotate stay in their current station.
     */
    private suspend fun assignRemainingWorkersWithRotation(
        regularWorkers: List<Worker>,
        currentAssignments: Map<Long, List<Worker>>,
        nextAssignments: MutableMap<Long, MutableList<Worker>>,
        allWorkstations: List<Workstation>
    ) {
        // Get workers not yet assigned to next rotation
        val unassignedWorkers = regularWorkers.filter { worker ->
            !nextAssignments.values.any { it.contains(worker) }
        }
        
        for (worker in unassignedWorkers) {
            // Find current station of this worker
            val currentStationId = findWorkerCurrentStation(worker, currentAssignments)
            
            // Check if worker can rotate (availability >= 50%, no restrictions, not trainer/trainee)
            if (!canWorkerRotate(worker)) {
                // Worker cannot rotate - keep in current station for safety and continuity
                var workerAssigned = false
                
                currentStationId?.let { stationId ->
                    val currentStation = allWorkstations.find { it.id == stationId }
                    currentStation?.let { station ->
                        // Check if current station has capacity
                        if ((nextAssignments[station.id]?.size ?: 0) < station.requiredWorkers) {
                            nextAssignments[station.id]?.add(worker)
                            updateWorkerRotationTracking(worker, station.id)
                            workerAssigned = true
                        }
                    }
                }
                
                // If current station is full, try to find any available station
                if (!workerAssigned) {
                    val qualifiedStationIds = workerDao.getWorkerWorkstationIds(worker.id)
                    val availableStation = allWorkstations.find { station ->
                        station.id in qualifiedStationIds &&
                        (nextAssignments[station.id]?.size ?: 0) < station.requiredWorkers
                    }
                    
                    availableStation?.let { station ->
                        nextAssignments[station.id]?.add(worker)
                        updateWorkerRotationTracking(worker, station.id)
                    }
                }
            } else {
                // Worker can rotate - proceed with normal rotation logic
                val qualifiedStationIds = workerDao.getWorkerWorkstationIds(worker.id)
                val qualifiedStations = allWorkstations.filter { it.id in qualifiedStationIds }
                
                // Find alternative stations (different from current)
                val alternativeStations = qualifiedStations.filter { station ->
                    station.id != currentStationId &&
                    (nextAssignments[station.id]?.size ?: 0) < station.requiredWorkers
                }
                
                // Assign to best alternative station
                val targetStation = if (alternativeStations.isNotEmpty()) {
                    // Prefer stations with fewer assigned workers
                    alternativeStations.minByOrNull { nextAssignments[it.id]?.size ?: 0 }
                } else {
                    // Fallback: if no alternatives, find any available station (including current)
                    qualifiedStations.find { station ->
                        (nextAssignments[station.id]?.size ?: 0) < station.requiredWorkers
                    }
                }
                
                targetStation?.let { station ->
                    nextAssignments[station.id]?.add(worker)
                    
                    // Update rotation tracking
                    updateWorkerRotationTracking(worker, station.id)
                }
            }
        }
    }
    
    /**
     * Fills remaining capacity with any unassigned eligible workers.
     */
    private suspend fun fillRemainingCapacity(
        eligibleWorkers: List<Worker>,
        nextAssignments: MutableMap<Long, MutableList<Worker>>,
        allWorkstations: List<Workstation>
    ) {
        // Get workers not assigned to any rotation yet
        val completelyUnassigned = eligibleWorkers.filter { worker ->
            !nextAssignments.values.any { it.contains(worker) }
        }
        
        for (worker in completelyUnassigned) {
            // Find stations with available capacity
            val qualifiedStationIds = workerDao.getWorkerWorkstationIds(worker.id)
            val availableStations = allWorkstations.filter { station ->
                station.id in qualifiedStationIds &&
                (nextAssignments[station.id]?.size ?: 0) < station.requiredWorkers
            }
            
            // Assign to station with least workers
            val targetStation = availableStations.minByOrNull { 
                nextAssignments[it.id]?.size ?: 0 
            }
            
            targetStation?.let { station ->
                nextAssignments[station.id]?.add(worker)
                updateWorkerRotationTracking(worker, station.id)
            }
        }
    }
    
    /**
     * Checks if a worker is currently assigned to a specific station.
     */
    private fun isWorkerCurrentlyAssignedToStation(
        worker: Worker,
        stationId: Long,
        currentAssignments: Map<Long, List<Worker>>
    ): Boolean {
        return currentAssignments[stationId]?.contains(worker) ?: false
    }
    
    /**
     * Finds the current station ID for a worker.
     */
    private fun findWorkerCurrentStation(
        worker: Worker,
        currentAssignments: Map<Long, List<Worker>>
    ): Long? {
        return currentAssignments.entries
            .find { it.value.contains(worker) }
            ?.key
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
        
        // Phase 3: Generate next rotation - SIMPLIFIED APPROACH
        // Always generate a next rotation by rotating workers to different stations
        generateNextRotationSimple(eligibleWorkers, currentAssignments, nextAssignments, allWorkstations)
        
        // Phase 4: Create rotation items and table
        val rotationItems = createRotationItems(allWorkstations, currentAssignments, nextAssignments)
        val rotationTable = createRotationTable(allWorkstations, currentAssignments, nextAssignments)
        
        return Pair(rotationItems, rotationTable)
    }

    /**
     * Generates next rotation with simple rotation logic - ALWAYS generates a next rotation.
     * CRITICAL: Ensures trainer-trainee pairs stay together in BOTH rotations.
     */
    private suspend fun generateNextRotationSimple(
        eligibleWorkers: List<Worker>,
        currentAssignments: Map<Long, List<Worker>>,
        nextAssignments: MutableMap<Long, MutableList<Worker>>,
        allWorkstations: List<Workstation>
    ) {
        // PHASE 0: ABSOLUTE PRIORITY - Assign trainer-trainee pairs FIRST in next rotation
        // This ensures they stay together in BOTH current AND next rotations
        val remainingWorkers = eligibleWorkers.toMutableList()
        assignTrainerTraineePairsToNextRotation(eligibleWorkers, nextAssignments, allWorkstations, remainingWorkers)
        
        // Get all workers currently assigned (excluding those already assigned as pairs)
        val allCurrentWorkers = currentAssignments.values.flatten()
        val workersToRotate = allCurrentWorkers.filter { remainingWorkers.contains(it) }
        
        // For each remaining worker, try to assign them to a different station
        for (worker in workersToRotate) {
            val currentStationId = findWorkerCurrentStation(worker, currentAssignments)
            val qualifiedStationIds = getWorkerWorkstationIds(worker.id)
            val qualifiedStations = allWorkstations.filter { it.id in qualifiedStationIds }
            
            // Try to find a different station first (for rotation)
            val alternativeStations = qualifiedStations.filter { station ->
                station.id != currentStationId &&
                (nextAssignments[station.id]?.size ?: 0) < station.requiredWorkers
            }
            
            val targetStation = if (alternativeStations.isNotEmpty()) {
                // Prefer stations with fewer workers assigned
                alternativeStations.minByOrNull { nextAssignments[it.id]?.size ?: 0 }
            } else {
                // If no alternatives, find any available station
                qualifiedStations.find { station ->
                    (nextAssignments[station.id]?.size ?: 0) < station.requiredWorkers
                }
            }
            
            targetStation?.let { station ->
                nextAssignments[station.id]?.add(worker)
            }
        }
        
        // Fill remaining capacity with any unassigned workers
        val assignedInNext = nextAssignments.values.flatten().toSet()
        val unassignedWorkers = eligibleWorkers.filter { !assignedInNext.contains(it) }
        
        for (worker in unassignedWorkers) {
            val qualifiedStationIds = getWorkerWorkstationIds(worker.id)
            val availableStation = allWorkstations.find { station ->
                station.id in qualifiedStationIds &&
                (nextAssignments[station.id]?.size ?: 0) < station.requiredWorkers
            }
            
            availableStation?.let { station ->
                nextAssignments[station.id]?.add(worker)
            }
        }
    }

    /**
     * Generates a simple next rotation when the complex algorithm fails to assign workers.
     * This ensures that there's always a next rotation generated.
     */
    private suspend fun generateSimpleNextRotation(
        eligibleWorkers: List<Worker>,
        currentAssignments: Map<Long, List<Worker>>,
        nextAssignments: MutableMap<Long, MutableList<Worker>>,
        allWorkstations: List<Workstation>
    ) {
        // Get all workers currently assigned
        val allCurrentWorkers = currentAssignments.values.flatten()
        
        // For each worker, try to assign them to a different station
        for (worker in allCurrentWorkers) {
            val currentStationId = findWorkerCurrentStation(worker, currentAssignments)
            val qualifiedStationIds = workerDao.getWorkerWorkstationIds(worker.id)
            val qualifiedStations = allWorkstations.filter { it.id in qualifiedStationIds }
            
            // Try to find a different station first
            val alternativeStations = qualifiedStations.filter { station ->
                station.id != currentStationId &&
                (nextAssignments[station.id]?.size ?: 0) < station.requiredWorkers
            }
            
            val targetStation = if (alternativeStations.isNotEmpty()) {
                // Assign to different station with least workers
                alternativeStations.minByOrNull { nextAssignments[it.id]?.size ?: 0 }
            } else {
                // If no alternatives, assign to any available station
                qualifiedStations.find { station ->
                    (nextAssignments[station.id]?.size ?: 0) < station.requiredWorkers
                }
            }
            
            targetStation?.let { station ->
                nextAssignments[station.id]?.add(worker)
            }
        }
        
        // Fill remaining capacity with unassigned workers
        val unassignedWorkers = eligibleWorkers.filter { worker ->
            !nextAssignments.values.any { it.contains(worker) }
        }
        
        for (worker in unassignedWorkers) {
            val qualifiedStationIds = workerDao.getWorkerWorkstationIds(worker.id)
            val availableStation = allWorkstations.find { station ->
                station.id in qualifiedStationIds &&
                (nextAssignments[station.id]?.size ?: 0) < station.requiredWorkers
            }
            
            availableStation?.let { station ->
                nextAssignments[station.id]?.add(worker)
            }
        }
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
     * Creates a formatted worker label with status indicators including rotation status.
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
        val leadershipStatus = getLeadershipStatus(worker, currentStation, nextStation)
        val rotationStatus = getRotationStatus(currentStation, nextStation, worker)
        
        val baseName = if (isPriorityWorker) {
            "${worker.name} [PRIORITARIO]"
        } else {
            worker.name
        }
        
        return "$baseName$availabilityStatus$restrictionStatus$trainingStatus$leadershipStatus$rotationStatus"
    }
    
    /**
     * Gets leadership status indicator for workers who are leaders.
     */
    private fun getLeadershipStatus(
        worker: Worker,
        currentStation: Workstation,
        nextStation: Workstation
    ): String {
        if (!worker.isLeader || worker.leaderWorkstationId == null) return ""
        
        val isLeaderInCurrent = worker.leaderWorkstationId == currentStation.id
        val isLeaderInNext = worker.leaderWorkstationId == nextStation.id
        
        return when {
            isLeaderInCurrent && isLeaderInNext -> " 👑 [LÍDER]"
            isLeaderInCurrent -> " 👑 [LÍDER ACTUAL]"
            isLeaderInNext -> " 👑 [LÍDER SIGUIENTE]"
            else -> ""
        }
    }
    
    /**
     * Gets rotation status indicator showing if worker is rotating or staying.
     */
    private fun getRotationStatus(
        currentStation: Workstation,
        nextStation: Workstation,
        worker: Worker
    ): String {
        return when {
            // Trainer-trainee pairs are exempt from rotation indicators
            worker.isTrainee && worker.trainerId != null -> ""
            worker.isTrainer -> ""
            // Leaders get special rotation status
            worker.isLeader -> ""
            // Show rotation status for regular workers
            currentStation.id != nextStation.id -> " 🔄 [ROTANDO]"
            else -> " 📍 [PERMANECE]"
        }
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
        // Debug: Verificar asignaciones finales
        println("DEBUG: ===== ASIGNACIONES FINALES =====")
        
        var totalCurrentWorkers = 0
        var totalNextWorkers = 0
        
        allWorkstations.forEach { station ->
            val currentWorkers = currentAssignments[station.id] ?: emptyList()
            val nextWorkers = nextAssignments[station.id] ?: emptyList()
            
            totalCurrentWorkers += currentWorkers.size
            totalNextWorkers += nextWorkers.size
            
            println("DEBUG: ${station.name}:")
            println("DEBUG:   Current: ${currentWorkers.size}/${station.requiredWorkers} - ${currentWorkers.map { it.name }}")
            println("DEBUG:   Next: ${nextWorkers.size}/${station.requiredWorkers} - ${nextWorkers.map { it.name }}")
        }
        
        println("DEBUG: Total workers assigned - Current: $totalCurrentWorkers, Next: $totalNextWorkers")
        println("DEBUG: ================================")
        
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
    
    /**
     * Gets rotation statistics for the current rotation.
     */
    fun getRotationStatistics(): RotationStatistics {
        val rotationTable = _rotationTable.value
        val rotationItems = _rotationItems.value ?: emptyList()
        
        if (rotationTable == null) {
            return RotationStatistics()
        }
        
        var totalWorkers = 0
        var workersRotating = 0
        var workersStaying = 0
        var trainerTraineePairs = 0
        
        // Count workers in current phase
        rotationTable.currentPhase.values.forEach { workers ->
            totalWorkers += workers.size
        }
        
        // Analyze rotation patterns
        rotationTable.currentPhase.forEach { (currentStationId, currentWorkers) ->
            currentWorkers.forEach { worker ->
                // Find where this worker goes in next phase
                val nextStationId = rotationTable.nextPhase.entries
                    .find { it.value.contains(worker) }?.key
                
                when {
                    worker.isTrainee && worker.trainerId != null -> {
                        // Count trainer-trainee pairs
                        val trainer = rotationTable.currentPhase.values.flatten()
                            .find { it.id == worker.trainerId }
                        if (trainer != null) {
                            trainerTraineePairs++
                        }
                    }
                    nextStationId != null && nextStationId != currentStationId -> {
                        workersRotating++
                    }
                    nextStationId == currentStationId -> {
                        workersStaying++
                    }
                }
            }
        }
        
        // Avoid double counting trainer-trainee pairs
        trainerTraineePairs = trainerTraineePairs / 2
        
        return RotationStatistics(
            totalWorkers = totalWorkers,
            workersRotating = workersRotating,
            workersStaying = workersStaying,
            trainerTraineePairs = trainerTraineePairs,
            rotationPercentage = if (totalWorkers > 0) (workersRotating * 100) / totalWorkers else 0
        )
    }
    
    /**
     * Data class for rotation statistics.
     */
    data class RotationStatistics(
        val totalWorkers: Int = 0,
        val workersRotating: Int = 0,
        val workersStaying: Int = 0,
        val trainerTraineePairs: Int = 0,
        val rotationPercentage: Int = 0
    ) {
        fun getSummaryText(): String {
            return "Rotación: $workersRotating/$totalWorkers trabajadores ($rotationPercentage%) • " +
                   "Permanecen: $workersStaying • Entrenamientos: $trainerTraineePairs parejas"
        }
    }
}

class RotationViewModelFactory(
    private val workerDao: WorkerDao,
    private val workstationDao: WorkstationDao,
    private val workerRestrictionDao: WorkerRestrictionDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RotationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RotationViewModel(workerDao, workstationDao, workerRestrictionDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}