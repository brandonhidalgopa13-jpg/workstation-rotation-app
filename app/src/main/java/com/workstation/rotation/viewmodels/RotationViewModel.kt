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
            
            // Generate rotation with priority logic
            val rotationItems = mutableListOf<RotationItem>()
            
            // First, handle priority workstations
            val priorityWorkstations = allWorkstations.filter { it.isPriority }
            val assignedToPriority = mutableSetOf<Long>() // Track workers assigned to priority stations
            
            // Assign exact number of workers to priority stations
            for (priorityStation in priorityWorkstations) {
                val availableWorkers = eligibleWorkers.filter { worker ->
                    !assignedToPriority.contains(worker.id) &&
                    workerDao.getWorkerWorkstationIds(worker.id).contains(priorityStation.id)
                }
                
                val workersToAssign = minOf(priorityStation.requiredWorkers, availableWorkers.size)
                
                for (i in 0 until workersToAssign) {
                    val worker = availableWorkers[i]
                    assignedToPriority.add(worker.id)
                    
                    val workerWorkstationIds = workerDao.getWorkerWorkstationIds(worker.id)
                    val workerWorkstations = allWorkstations.filter { it.id in workerWorkstationIds }
                    val nextStation = workerWorkstations.find { it.id != priorityStation.id } ?: priorityStation
                    
                    val currentInfo = "${priorityStation.name} (${priorityStation.requiredWorkers} req.) ⭐"
                    val nextInfo = "${nextStation.name} (${nextStation.requiredWorkers} req.)"
                    
                    rotationItems.add(
                        RotationItem(
                            workerName = "${worker.name} [PRIORITARIO]",
                            currentWorkstation = currentInfo,
                            nextWorkstation = nextInfo,
                            rotationOrder = rotationItems.size + 1
                        )
                    )
                }
            }
            
            // Then handle remaining workers normally
            val remainingWorkers = eligibleWorkers.filter { !assignedToPriority.contains(it.id) }
            
            for ((index, worker) in remainingWorkers.withIndex()) {
                val workerWorkstationIds = workerDao.getWorkerWorkstationIds(worker.id)
                val workerWorkstations = allWorkstations.filter { it.id in workerWorkstationIds }
                
                if (workerWorkstations.isNotEmpty()) {
                    val currentIndex = index % workerWorkstations.size
                    val nextIndex = (currentIndex + 1) % workerWorkstations.size
                    
                    val currentWorkstation = workerWorkstations[currentIndex]
                    val nextWorkstation = workerWorkstations[nextIndex]
                    
                    val currentInfo = "${currentWorkstation.name} (${currentWorkstation.requiredWorkers} req.)"
                    val nextInfo = "${nextWorkstation.name} (${nextWorkstation.requiredWorkers} req.)"
                    
                    rotationItems.add(
                        RotationItem(
                            workerName = worker.name,
                            currentWorkstation = currentInfo,
                            nextWorkstation = nextInfo,
                            rotationOrder = rotationItems.size + 1
                        )
                    )
                }
            }
            
            _rotationItems.value = rotationItems
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