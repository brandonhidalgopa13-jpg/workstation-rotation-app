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
    
    suspend fun generateRotation(): Boolean {
        return try {
            // Get all active workers using first() to get single emission
            val allWorkers = workerDao.getAllWorkers().first().filter { it.isActive }
            
            // Get workers that have workstations assigned
            val eligibleWorkers = mutableListOf<Worker>()
            for (worker in allWorkers) {
                val workstationIds = workerDao.getWorkerWorkstationIds(worker.id)
                if (workstationIds.isNotEmpty()) {
                    eligibleWorkers.add(worker)
                }
            }
            
            eligibleWorkersCount = eligibleWorkers.size
            
            if (eligibleWorkers.isEmpty()) {
                _rotationItems.value = emptyList()
                return false
            }
            
            // Get all active workstations
            val allWorkstations = workstationDao.getAllActiveWorkstations().first()
            
            if (allWorkstations.isEmpty()) {
                _rotationItems.value = emptyList()
                return false
            }
            
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
                
                val workersToAssign = minOf(priorityStation.requiredWorkers, availableWorkers.size)
                
                for (i in 0 until workersToAssign) {
                    currentAssignments[priorityStation.id]?.add(availableWorkers[i])
                }
            }
            
            // Phase 2: Assign remaining workers to normal workstations (current positions)
            val unassignedWorkers = eligibleWorkers.filter { worker ->
                currentAssignments.values.none { it.contains(worker) }
            }
            
            for (worker in unassignedWorkers) {
                val workerWorkstationIds = workerDao.getWorkerWorkstationIds(worker.id)
                val availableStations = normalWorkstations.filter { station ->
                    station.id in workerWorkstationIds &&
                    (currentAssignments[station.id]?.size ?: 0) < station.requiredWorkers
                }
                
                if (availableStations.isNotEmpty()) {
                    // Assign to station with least workers
                    val targetStation = availableStations.minByOrNull { 
                        currentAssignments[it.id]?.size ?: 0 
                    }
                    targetStation?.let { station ->
                        currentAssignments[station.id]?.add(worker)
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
                
                // Assign EXACTLY the required number to maintain priority
                val workersToAssign = minOf(priorityStation.requiredWorkers, availableWorkers.size)
                
                // Clear any existing assignments to this priority station in next round
                nextAssignments[priorityStation.id]?.clear()
                
                // Assign the required workers
                for (i in 0 until workersToAssign) {
                    nextAssignments[priorityStation.id]?.add(availableWorkers[i])
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
                    val workerLabel = if (isPriorityWorker) "${worker.name} [PRIORITARIO]" else worker.name
                    
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
            
            _rotationItems.value = rotationItems.sortedBy { it.rotationOrder }
            rotationItems.isNotEmpty()
            
        } catch (e: Exception) {
            _rotationItems.value = emptyList()
            eligibleWorkersCount = 0
            false
        }
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