package com.workstation.rotation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import com.workstation.rotation.adapters.WorkerWithWorkstationCount
import com.workstation.rotation.data.dao.WorkerDao
import com.workstation.rotation.data.dao.WorkstationDao
import com.workstation.rotation.data.dao.WorkerRestrictionDao
import com.workstation.rotation.data.entities.Worker
import com.workstation.rotation.data.entities.WorkerWorkstation
import com.workstation.rotation.data.entities.WorkerRestriction
import com.workstation.rotation.data.entities.RestrictionType
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first

class WorkerViewModel(
    private val workerDao: WorkerDao,
    private val workstationDao: WorkstationDao,
    private val workerRestrictionDao: WorkerRestrictionDao
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
    
    /**
     * Elimina un trabajador del sistema.
     * También elimina todas sus asignaciones de estaciones.
     */
    suspend fun deleteWorker(worker: Worker) {
        // Primero eliminar todas las asignaciones de estaciones
        workerDao.deleteAllWorkerWorkstations(worker.id)
        // Luego eliminar el trabajador
        workerDao.deleteWorker(worker)
    }
    
    /**
     * Verifica si un entrenador tiene trabajadores asignados.
     */
    suspend fun hasTrainees(trainerId: Long): Boolean {
        return workerDao.hasTrainees(trainerId)
    }
    
    /**
     * Obtiene las restricciones de un trabajador.
     */
    fun getWorkerRestrictions(workerId: Long) = workerRestrictionDao.getWorkerRestrictions(workerId)
    
    /**
     * Obtiene las restricciones de un trabajador de forma síncrona.
     */
    suspend fun getWorkerRestrictionsSync(workerId: Long) = workerRestrictionDao.getWorkerRestrictionsSync(workerId)
    
    /**
     * Guarda las restricciones de un trabajador.
     */
    suspend fun saveWorkerRestrictions(workerId: Long, restrictedWorkstations: List<Long>, restrictionType: RestrictionType, notes: String) {
        // Primero eliminar todas las restricciones existentes del trabajador
        workerRestrictionDao.deleteAllWorkerRestrictions(workerId)
        
        // Luego agregar las nuevas restricciones
        restrictedWorkstations.forEach { workstationId ->
            val restriction = WorkerRestriction(
                workerId = workerId,
                workstationId = workstationId,
                restrictionType = restrictionType,
                notes = notes
            )
            workerRestrictionDao.insertRestriction(restriction)
        }
    }
    
    /**
     * Obtiene el conteo de restricciones de un trabajador.
     */
    suspend fun getWorkerRestrictionCount(workerId: Long): Int {
        return workerRestrictionDao.getWorkerRestrictionCount(workerId)
    }
    
    /**
     * Obtiene las estaciones prohibidas para un trabajador.
     */
    suspend fun getProhibitedWorkstations(workerId: Long): List<Long> {
        return workerRestrictionDao.getProhibitedWorkstations(workerId)
    }
    
    /**
     * Obtiene las estaciones donde puede trabajar un entrenador específico.
     */
    suspend fun getTrainerWorkstations(trainerId: Long): List<Workstation> {
        val trainerWorkstationIds = getWorkerWorkstationIds(trainerId)
        return workstationDao.getAllActiveWorkstations().first().filter { workstation ->
            trainerWorkstationIds.contains(workstation.id)
        }
    }
}

class WorkerViewModelFactory(
    private val workerDao: WorkerDao,
    private val workstationDao: WorkstationDao,
    private val workerRestrictionDao: WorkerRestrictionDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WorkerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WorkerViewModel(workerDao, workstationDao, workerRestrictionDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}