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
import com.workstation.rotation.data.entities.Workstation
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
        android.util.Log.d("WorkerViewModel", "=== CERTIFICANDO TRABAJADOR $workerId ===")
        val worker = workerDao.getWorkerById(workerId)
        worker?.let {
            android.util.Log.d("WorkerViewModel", "Trabajador antes: ${it.name} - isTrainee: ${it.isTrainee}, trainerId: ${it.trainerId}")
            val certifiedWorker = it.copy(
                isTrainee = false,
                isCertified = true,
                trainerId = null,
                trainingWorkstationId = null,
                certificationDate = System.currentTimeMillis()
            )
            workerDao.updateWorker(certifiedWorker)
            android.util.Log.d("WorkerViewModel", "Trabajador después: ${certifiedWorker.name} - isTrainee: ${certifiedWorker.isTrainee}, isCertified: ${certifiedWorker.isCertified}")
        } ?: run {
            android.util.Log.e("WorkerViewModel", "ERROR: No se encontró trabajador con ID $workerId")
        }
        android.util.Log.d("WorkerViewModel", "=======================================")
    }
    
    /**
     * Obtiene todos los trabajadores que están en entrenamiento.
     */
    suspend fun getWorkersInTraining(): List<Worker> {
        android.util.Log.d("WorkerViewModel", "=== OBTENIENDO TRABAJADORES EN ENTRENAMIENTO ===")
        val workersInTraining = workerDao.getWorkersInTraining()
        android.util.Log.d("WorkerViewModel", "Trabajadores en entrenamiento encontrados: ${workersInTraining.size}")
        workersInTraining.forEach { worker ->
            android.util.Log.d("WorkerViewModel", "- ${worker.name} (ID: ${worker.id}, Entrenador: ${worker.trainerId}, Estación: ${worker.trainingWorkstationId})")
        }
        android.util.Log.d("WorkerViewModel", "================================================")
        return workersInTraining
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
        android.util.Log.d("WorkerViewModel", "=== OBTENIENDO ESTACIONES DEL ENTRENADOR $trainerId ===")
        
        val trainerWorkstationIds = getWorkerWorkstationIds(trainerId)
        android.util.Log.d("WorkerViewModel", "Entrenador $trainerId tiene asignadas las estaciones: $trainerWorkstationIds")
        
        if (trainerWorkstationIds.isEmpty()) {
            android.util.Log.w("WorkerViewModel", "WARNING: Entrenador $trainerId no tiene estaciones asignadas")
            return emptyList()
        }
        
        val allActiveWorkstations = workstationDao.getAllActiveWorkstations().first()
        android.util.Log.d("WorkerViewModel", "Total de estaciones activas: ${allActiveWorkstations.size}")
        
        val trainerWorkstations = allActiveWorkstations.filter { workstation ->
            trainerWorkstationIds.contains(workstation.id)
        }
        
        android.util.Log.d("WorkerViewModel", "Estaciones filtradas para entrenador $trainerId: ${trainerWorkstations.size}")
        trainerWorkstations.forEach { station ->
            android.util.Log.d("WorkerViewModel", "- ${station.name} (ID: ${station.id})")
        }
        android.util.Log.d("WorkerViewModel", "===============================================")
        
        return trainerWorkstations
    }
    
    /**
     * Obtiene las estaciones activas de forma síncrona.
     */
    suspend fun getActiveWorkstationsSync(): List<Workstation> {
        android.util.Log.d("WorkerViewModel", "=== OBTENIENDO ESTACIONES ACTIVAS ===")
        val workstations = workstationDao.getAllActiveWorkstations().first()
        android.util.Log.d("WorkerViewModel", "getActiveWorkstationsSync: encontradas ${workstations.size} estaciones")
        workstations.forEachIndexed { index, station ->
            android.util.Log.d("WorkerViewModel", "Estación $index: ${station.name} (ID: ${station.id}, Activa: ${station.isActive}, Requeridos: ${station.requiredWorkers})")
        }
        android.util.Log.d("WorkerViewModel", "==========================================")
        return workstations
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