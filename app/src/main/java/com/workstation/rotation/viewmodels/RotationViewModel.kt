package com.workstation.rotation.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.workstation.rotation.data.dao.WorkerDao
import com.workstation.rotation.data.dao.WorkstationDao
import com.workstation.rotation.data.entities.Worker
import com.workstation.rotation.data.entities.Workstation
import com.workstation.rotation.models.RotationItem

class RotationViewModel(
    private val workerDao: WorkerDao,
    private val workstationDao: WorkstationDao
) : ViewModel() {
    
    private val _rotationItems = MutableLiveData<List<RotationItem>>()
    val rotationItems: LiveData<List<RotationItem>> = _rotationItems
    
    private var eligibleWorkersCount = 0
    
    suspend fun generateRotation(): Boolean {
        // Get workers that have workstations assigned
        val eligibleWorkers = mutableListOf<Worker>()
        val allWorkers = workerDao.getAllWorkers()
        
        allWorkers.collect { workers ->
            for (worker in workers.filter { it.isActive }) {
                val workstationIds = workerDao.getWorkerWorkstationIds(worker.id)
                if (workstationIds.isNotEmpty()) {
                    eligibleWorkers.add(worker)
                }
            }
        }
        
        eligibleWorkersCount = eligibleWorkers.size
        
        if (eligibleWorkers.isEmpty()) {
            _rotationItems.value = emptyList()
            return false
        }
        
        // Get all active workstations
        val workstations = mutableListOf<Workstation>()
        workstationDao.getAllActiveWorkstations().collect { stations ->
            workstations.addAll(stations)
        }
        
        if (workstations.isEmpty()) {
            _rotationItems.value = emptyList()
            return false
        }
        
        // Generate rotation
        val rotationItems = mutableListOf<RotationItem>()
        
        for ((index, worker) in eligibleWorkers.withIndex()) {
            val workerWorkstationIds = workerDao.getWorkerWorkstationIds(worker.id)
            val workerWorkstations = workstations.filter { it.id in workerWorkstationIds }
            
            if (workerWorkstations.isNotEmpty()) {
                // Simple rotation: assign next workstation in the list
                val currentIndex = index % workerWorkstations.size
                val nextIndex = (currentIndex + 1) % workerWorkstations.size
                
                val currentWorkstation = workerWorkstations[currentIndex]
                val nextWorkstation = workerWorkstations[nextIndex]
                
                rotationItems.add(
                    RotationItem(
                        workerName = worker.name,
                        currentWorkstation = currentWorkstation.name,
                        nextWorkstation = nextWorkstation.name,
                        rotationOrder = index + 1
                    )
                )
            }
        }
        
        _rotationItems.value = rotationItems
        return true
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