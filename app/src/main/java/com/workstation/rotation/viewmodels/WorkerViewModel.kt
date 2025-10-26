package com.workstation.rotation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import com.workstation.rotation.adapters.WorkerWithWorkstationCount
import com.workstation.rotation.data.dao.WorkerDao
import com.workstation.rotation.data.dao.WorkstationDao
import com.workstation.rotation.data.entities.Worker
import com.workstation.rotation.data.entities.WorkerWorkstation
import kotlinx.coroutines.flow.combine

class WorkerViewModel(
    private val workerDao: WorkerDao,
    private val workstationDao: WorkstationDao
) : ViewModel() {
    
    val allWorkers = liveData {
        workerDao.getAllWorkers().combine(workerDao.getAllWorkers()) { workers, _ ->
            workers.map { worker ->
                val count = workerDao.getWorkerWorkstationIds(worker.id).size
                WorkerWithWorkstationCount(worker, count)
            }
        }.collect { emit(it) }
    }
    
    val activeWorkstations = workstationDao.getAllActiveWorkstations().asLiveData()
    val workersWithWorkstations = workerDao.getWorkersWithWorkstations().asLiveData()
    
    suspend fun insertWorkerWithWorkstations(worker: Worker, workstationIds: List<Long>) {
        val workerId = workerDao.insertWorker(worker)
        workstationIds.forEach { workstationId ->
            workerDao.insertWorkerWorkstation(WorkerWorkstation(workerId, workstationId))
        }
    }
    
    suspend fun updateWorkerWithWorkstations(worker: Worker, workstationIds: List<Long>) {
        workerDao.updateWorker(worker)
        workerDao.deleteAllWorkerWorkstations(worker.id)
        workstationIds.forEach { workstationId ->
            workerDao.insertWorkerWorkstation(WorkerWorkstation(worker.id, workstationId))
        }
    }
    
    suspend fun updateWorkerStatus(id: Long, isActive: Boolean) {
        workerDao.updateWorkerStatus(id, isActive)
    }
    
    suspend fun getWorkerWorkstationIds(workerId: Long): List<Long> {
        return workerDao.getWorkerWorkstationIds(workerId)
    }
    
    suspend fun getTrainers(): List<Worker> {
        return workerDao.getAllWorkers().first().filter { it.isTrainer && it.isActive }
    }
    
    suspend fun getWorkerById(workerId: Long): Worker? {
        return workerDao.getWorkerById(workerId)
    }
}

class WorkerViewModelFactory(
    private val workerDao: WorkerDao,
    private val workstationDao: WorkstationDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WorkerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WorkerViewModel(workerDao, workstationDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}