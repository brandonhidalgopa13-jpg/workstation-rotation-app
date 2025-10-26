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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class RotationViewModel(
    private val workerDao: WorkerDao,
    private val workstationDao: WorkstationDao
) : ViewModel() {
    
    private val _rotationItems = MutableLiveData<List<RotationItem>>()
    val rotationItems: LiveData<List<RotationItem>> = _rotationItems
    
    private var eligibleWorkersCount = 0
    
    /**
     * Generates an intelligent rotation considering priorities, availability, and training relationships.
     * @return Boolean indicating if rotation was successfully generated
     */
    suspend fun generateRotation(): Boolean {
        return try {
            val rotationData = prepareRotationData()
            if (!rotationData.isValid()) {
                _rotationItems.value = emptyList()
                return false
            }
            
            val rotationItems = executeRotationAlgorithm(rotationData)
            _rotationItems.value = rotationItems.sortedBy { it.rotationOrder }
            
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
     * Executes the main rotation algorithm with all business logic.
     */
    private suspend fun executeRotationAlgorithm(data: RotationData): List<RotationItem> {
        val eligibleWorkers = data.eligibleWorkers
        val allWorkstations = data.allWorkstations
            
        // Generate intelligent rotation with capacity control
        val rotationItems = mutableListOf<RotationItem>()
        
        // Track current assignments per workstation
        val currentAssignments = mutableMapOf<Long, MutableList<Worker>>()
        val nextAssignments = mutableMapOf<Long, MutableList<Worker>>()
        
        // Initialize assignment tracking
        allWorkstations.forEach { station ->
            currentAssignments[station.id] = mutableListOf()
            nextAssignments[station.id] = mutableListOf()
        }
        
        // Separate priority and normal workstations
        val priorityWorkstations = allWorkstations.filter { it.isPriority }
        val normalWorkstations = allWorkstations.filter { !it.isPriority }
            
            // Phase 1: Assign workers to PRIORITY workstations first (current positions)
            for (priorityStation in priorityWorkstations) {
                val availableWorkers = eligibleWorkers.filter { worker ->
                    workerDao.getWorkerWorkstationIds(worker.id).contains(priorityStation.id) &&
                    currentAssignments.values.none { it.contains(worker) }
                }
                
                // Sort by availability and training status (trainers first, then by availability)
                val sortedWorkers = availableWorkers.sortedWith(compareByDescending<Worker> { it.isTrainer }
                    .thenByDescending { it.availabilityPercentage })
                
                val workersToAssign = minOf(priorityStation.requiredWorkers, sortedWorkers.size)
                
                for (i in 0 until workersToAssign) {
                    currentAssignments[priorityStation.id]?.add(sortedWorkers[i])
                }
            }
            
            // Phase 2: Assign remaining workers to normal workstations (current positions)
            val unassignedWorkers = eligibleWorkers.filter { worker ->
                currentAssignments.values.none { it.contains(worker) }
            }
            
            // Sort unassigned workers by training status and availability
            val sortedUnassignedWorkers = unassignedWorkers.sortedWith(
                compareByDescending<Worker> { it.isTrainer }
                    .thenByDescending { worker ->
                        // Create weighted score: availability + random factor for rotation variety
                        worker.availabilityPercentage + (kotlin.random.Random.nextInt(0, 30))
                    }
            )
            
            for (worker in sortedUnassignedWorkers) {
                // Apply availability probability check
                val shouldAssign = kotlin.random.Random.nextInt(1, 101) <= worker.availabilityPercentage
                
                if (shouldAssign) {
                    val workerWorkstationIds = workerDao.getWorkerWorkstationIds(worker.id)
                    val availableStations = normalWorkstations.filter { station ->
                        station.id in workerWorkstationIds &&
                        (currentAssignments[station.id]?.size ?: 0) < station.requiredWorkers
                    }
                    
                    if (availableStations.isNotEmpty()) {
                        // For trainees, prioritize their training workstation
                        val targetStation = if (worker.isTrainee && worker.trainingWorkstationId != null) {
                            availableStations.find { it.id == worker.trainingWorkstationId }
                                ?: availableStations.minByOrNull { currentAssignments[it.id]?.size ?: 0 }
                        } else {
                            // Assign to station with least workers
                            availableStations.minByOrNull { 
                                currentAssignments[it.id]?.size ?: 0 
                            }
                        }
                        
                        targetStation?.let { station ->
                            currentAssignments[station.id]?.add(worker)
                        }
                    }
                }
            }
            
            // Phase 3: Generate next rotation positions
            // PRIORITY STATIONS: Must maintain FULL capacity in BOTH current and next positions
            
            // First, ensure ALL priority stations have their required workers in NEXT position
            for (priorityStation in priorityWorkstations) {
                val availableWorkers = eligibleWorkers.filter { worker ->
                    workerDao.getWorkerWorkstationIds(worker.id).contains(priorityStation.id)
                }
                
                // Sort by availability for priority assignments (best available workers first)
                val sortedAvailableWorkers = availableWorkers.sortedByDescending { it.availabilityPercentage }
                
                // Assign EXACTLY the required number to maintain priority
                val workersToAssign = minOf(priorityStation.requiredWorkers, sortedAvailableWorkers.size)
                
                // Clear any existing assignments to this priority station in next round
                nextAssignments[priorityStation.id]?.clear()
                
                // Assign the required workers (best availability first)
                for (i in 0 until workersToAssign) {
                    nextAssignments[priorityStation.id]?.add(sortedAvailableWorkers[i])
                }
            }
            
            // Now handle workers not assigned to priority stations in next round
            val workersInNextPriority = priorityWorkstations.flatMap { station ->
                nextAssignments[station.id] ?: emptyList()
            }.toSet()
            
            val remainingWorkersForNext = eligibleWorkers.filter { worker ->
                !workersInNextPriority.contains(worker)
            }
            
            // Assign remaining workers to normal stations for next round
            for (worker in remainingWorkersForNext) {
                val workerWorkstationIds = workerDao.getWorkerWorkstationIds(worker.id)
                val availableNormalStations = normalWorkstations.filter { station ->
                    station.id in workerWorkstationIds &&
                    (nextAssignments[station.id]?.size ?: 0) < station.requiredWorkers
                }
                
                if (availableNormalStations.isNotEmpty()) {
                    // Assign to station with least workers
                    val targetStation = availableNormalStations.minByOrNull { 
                        nextAssignments[it.id]?.size ?: 0 
                    }
                    targetStation?.let { station ->
                        nextAssignments[station.id]?.add(worker)
                    }
                }
            }
            
            // Phase 4: Create rotation items
            for (station in allWorkstations) {
                val currentWorkers = currentAssignments[station.id] ?: emptyList()
                
                for (worker in currentWorkers) {
                    // Find where this worker goes next
                    val nextStation = nextAssignments.entries.find { entry ->
                        entry.value.contains(worker)
                    }?.let { entry ->
                        allWorkstations.find { it.id == entry.key }
                    } ?: station
                    
                    val isPriorityWorker = station.isPriority || nextStation.isPriority
                    val availabilityStatus = when {
                        worker.availabilityPercentage >= 80 -> ""
                        worker.availabilityPercentage >= 50 -> " [${worker.availabilityPercentage}%]"
                        else -> " [${worker.availabilityPercentage}% ⚠️]"
                    }
                    
                    val restrictionStatus = if (worker.restrictionNotes.isNotEmpty()) " 🔒" else ""
                    val trainingStatus = when {
                        worker.isTrainer -> " 👨‍🏫"
                        worker.isTrainee -> " 🎯"
                        else -> ""
                    }
                    
                    val workerLabel = if (isPriorityWorker) {
                        "${worker.name} [PRIORITARIO]$availabilityStatus$restrictionStatus$trainingStatus"
                    } else {
                        "${worker.name}$availabilityStatus$restrictionStatus$trainingStatus"
                    }
                    
                    val currentInfo = "${station.name} (${currentWorkers.size}/${station.requiredWorkers})" + 
                                    if (station.isPriority) " ⭐ COMPLETA" else ""
                    
                    val nextWorkerCount = nextAssignments[nextStation.id]?.size ?: 0
                    val nextInfo = "${nextStation.name} (${nextWorkerCount}/${nextStation.requiredWorkers})" +
                                  if (nextStation.isPriority) " ⭐ COMPLETA" else ""
                    
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
            
            return rotationItems
    }
    
    fun updateEligibleWorkersCount() {
        viewModelScope.launch {
            try {
                val allWorkers = workerDao.getAllWorkers().first().filter { it.isActive }
                var count = 0
                for (worker in allWorkers) {
                    val workstationIds = workerDao.getWorkerWorkstationIds(worker.id)
                    if (workstationIds.isNotEmpty()) {
                        count++
                    }
                }
                eligibleWorkersCount = count
            } catch (e: Exception) {
                eligibleWorkersCount = 0
            }
        }
    }
    
    private fun validatePriorityStationsCapacity(
        priorityStations: List<Workstation>,
        assignments: Map<Long, List<Worker>>
    ): Boolean {
        return priorityStations.all { station ->
            val assignedCount = assignments[station.id]?.size ?: 0
            assignedCount == station.requiredWorkers
        }
    }
    
    fun clearRotation() {
        _rotationItems.value = emptyList()
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