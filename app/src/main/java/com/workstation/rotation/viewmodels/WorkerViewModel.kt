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
import kotlinx.coroutines.flow.first

class WorkerViewModel(
    private val workerDao: WorkerDao,
    private val workstationDao: WorkstationDao
) : ViewModel() {
    
    val allWorkers = liveData {
        workerDao.getAllWorkers().collect { workers ->
            val workersWithCount = workers.map { worker ->
                val count = workerDao.getWorkerWorkstationIds(worker.id).size
                WorkerWithWorkstationCount(worker, count)
            }
            emit(workersWithCount)
        }
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
    
    suspend fun getWorkstationById(workstationId: Long) = workstationDao.getWorkstationById(workstationId)
    
    /**
     * Certifica a un trabajador (remueve el estado de entrenamiento).
     * El trabajador pasa de "en entrenamiento" a "trabajador normal".
     */
    suspend fun certifyWorker(workerId: Long) {
        val worker = workerDao.getWorkerById(workerId)
        worker?.let {
            val certifiedWorker = it.copy(
                isTrainee = false,
                trainerId = null,
                trainingWorkstationId = null
            )
            workerDao.updateWorker(certifiedWorker)
        }
    }
    
    /**
     * Obtiene todos los trabajadores que están en entrenamiento.
     */
    suspend fun getWorkersInTraining(): List<Worker> {
        return workerDao.getAllWorkers().first().filter { it.isTrainee && it.isActive }
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