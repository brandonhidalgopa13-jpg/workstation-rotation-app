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
        android.util.Log.d("WorkerViewModel", "=== ACTUALIZANDO TRABAJADOR CON ESTACIONES ===")
        android.util.Log.d("WorkerViewModel", "Trabajador: ${worker.name} (ID: ${worker.id})")
        android.util.Log.d("WorkerViewModel", "Estaciones a asignar: $workstationIds")
        
        // Obtener estaciones actuales antes de la actualización
        val currentStations = workerDao.getWorkerWorkstationIds(worker.id)
        android.util.Log.d("WorkerViewModel", "Estaciones actuales: $currentStations")
        
        // Actualizar trabajador
        workerDao.updateWorker(worker)
        android.util.Log.d("WorkerViewModel", "Trabajador actualizado en BD")
        
        // Eliminar todas las asignaciones actuales
        workerDao.deleteAllWorkerWorkstations(worker.id)
        android.util.Log.d("WorkerViewModel", "Asignaciones anteriores eliminadas")
        
        // Insertar nuevas asignaciones
        workstationIds.forEach { workstationId ->
            workerDao.insertWorkerWorkstation(WorkerWorkstation(worker.id, workstationId))
            android.util.Log.d("WorkerViewModel", "Asignación insertada: Worker ${worker.id} -> Workstation $workstationId")
        }
        
        // Verificar que las asignaciones se guardaron correctamente
        val finalStations = workerDao.getWorkerWorkstationIds(worker.id)
        android.util.Log.d("WorkerViewModel", "Estaciones finales: $finalStations")
        
        if (finalStations.size != workstationIds.size) {
            android.util.Log.e("WorkerViewModel", "ERROR: No se guardaron todas las asignaciones!")
            android.util.Log.e("WorkerViewModel", "Esperadas: ${workstationIds.size}, Guardadas: ${finalStations.size}")
        } else {
            android.util.Log.d("WorkerViewModel", "✅ Todas las asignaciones guardadas correctamente")
        }
        
        android.util.Log.d("WorkerViewModel", "===============================================")
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
     * IMPORTANTE: Este método solo actualiza el estado del trabajador.
     * Las estaciones deben actualizarse por separado usando updateWorkerWithWorkstations.
     */
    suspend fun certifyWorker(workerId: Long) {
        android.util.Log.d("WorkerViewModel", "=== CERTIFICANDO TRABAJADOR $workerId ===")
        val worker = workerDao.getWorkerById(workerId)
        worker?.let {
            android.util.Log.d("WorkerViewModel", "Trabajador antes: ${it.name}")
            android.util.Log.d("WorkerViewModel", "- isTrainee: ${it.isTrainee}")
            android.util.Log.d("WorkerViewModel", "- trainerId: ${it.trainerId}")
            android.util.Log.d("WorkerViewModel", "- trainingWorkstationId: ${it.trainingWorkstationId}")
            android.util.Log.d("WorkerViewModel", "- isCertified: ${it.isCertified}")
            
            val certifiedWorker = it.copy(
                isTrainee = false,
                isCertified = true,
                trainerId = null,
                trainingWorkstationId = null,
                certificationDate = System.currentTimeMillis()
            )
            workerDao.updateWorker(certifiedWorker)
            
            android.util.Log.d("WorkerViewModel", "Trabajador después: ${certifiedWorker.name}")
            android.util.Log.d("WorkerViewModel", "- isTrainee: ${certifiedWorker.isTrainee}")
            android.util.Log.d("WorkerViewModel", "- isCertified: ${certifiedWorker.isCertified}")
            android.util.Log.d("WorkerViewModel", "- certificationDate: ${certifiedWorker.certificationDate}")
        } ?: run {
            android.util.Log.e("WorkerViewModel", "ERROR: No se encontró trabajador con ID $workerId")
        }
        android.util.Log.d("WorkerViewModel", "=======================================")
    }
    
    /**
     * Certifica a un trabajador de forma completa, incluyendo la adición automática
     * de la estación de entrenamiento a sus estaciones asignadas.
     */
    suspend fun certifyWorkerComplete(workerId: Long): Boolean {
        android.util.Log.d("WorkerViewModel", "=== CERTIFICACIÓN COMPLETA TRABAJADOR $workerId ===")
        
        return try {
            val worker = workerDao.getWorkerById(workerId)
            if (worker == null) {
                android.util.Log.e("WorkerViewModel", "ERROR: Trabajador no encontrado")
                return false
            }
            
            if (!worker.isTrainee) {
                android.util.Log.w("WorkerViewModel", "WARNING: Trabajador no está en entrenamiento")
                return false
            }
            
            // Obtener estaciones actuales
            val currentWorkstationIds = getWorkerWorkstationIds(workerId).toMutableList()
            android.util.Log.d("WorkerViewModel", "Estaciones actuales: $currentWorkstationIds")
            
            // Agregar estación de entrenamiento si no está ya incluida
            worker.trainingWorkstationId?.let { trainingStationId ->
                if (!currentWorkstationIds.contains(trainingStationId)) {
                    currentWorkstationIds.add(trainingStationId)
                    android.util.Log.d("WorkerViewModel", "Agregando estación de entrenamiento: $trainingStationId")
                } else {
                    android.util.Log.d("WorkerViewModel", "Estación de entrenamiento ya está asignada: $trainingStationId")
                }
            }
            
            // Certificar el trabajador
            certifyWorker(workerId)
            
            // Actualizar estaciones asignadas
            val certifiedWorker = worker.copy(
                isTrainee = false,
                isCertified = true,
                trainerId = null,
                trainingWorkstationId = null,
                certificationDate = System.currentTimeMillis()
            )
            
            updateWorkerWithWorkstations(certifiedWorker, currentWorkstationIds)
            
            android.util.Log.d("WorkerViewModel", "Estaciones finales: $currentWorkstationIds")
            android.util.Log.d("WorkerViewModel", "Certificación completa exitosa")
            android.util.Log.d("WorkerViewModel", "===============================================")
            
            true
        } catch (e: Exception) {
            android.util.Log.e("WorkerViewModel", "ERROR en certificación completa: ${e.message}", e)
            false
        }
    }
    
    /**
     * Notifica que las asignaciones de trabajadores han cambiado y se debe limpiar cualquier caché.
     */
    fun notifyWorkerAssignmentsChanged() {
        android.util.Log.d("WorkerViewModel", "Notificando cambios en asignaciones de trabajadores")
        // Este método puede ser usado por otros ViewModels para saber cuándo limpiar sus cachés
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
        val workstations = workstationDao.getAllActiveWorkstationsSync()
        android.util.Log.d("WorkerViewModel", "getActiveWorkstationsSync: encontradas ${workstations.size} estaciones")
        workstations.forEachIndexed { index, station ->
            android.util.Log.d("WorkerViewModel", "Estación $index: ${station.name} (ID: ${station.id}, Activa: ${station.isActive}, Requeridos: ${station.requiredWorkers})")
        }
        android.util.Log.d("WorkerViewModel", "==========================================")
        return workstations
    }
    
    /**
     * Método de testing para verificar el estado completo de un trabajador después de certificación.
     */
    suspend fun debugWorkerCertificationState(workerId: Long): String {
        android.util.Log.d("WorkerViewModel", "=== DEBUG ESTADO CERTIFICACIÓN ===")
        
        val worker = workerDao.getWorkerById(workerId)
        if (worker == null) {
            return "ERROR: Trabajador no encontrado"
        }
        
        val workstationIds = getWorkerWorkstationIds(workerId)
        val workstations = getActiveWorkstationsSync()
        val assignedWorkstations = workstations.filter { workstationIds.contains(it.id) }
        
        val report = StringBuilder()
        report.append("ESTADO DEL TRABAJADOR DESPUÉS DE CERTIFICACIÓN:\n")
        report.append("Nombre: ${worker.name}\n")
        report.append("ID: ${worker.id}\n")
        report.append("Es entrenado: ${worker.isTrainee}\n")
        report.append("Está certificado: ${worker.isCertified}\n")
        report.append("ID del entrenador: ${worker.trainerId}\n")
        report.append("ID estación de entrenamiento: ${worker.trainingWorkstationId}\n")
        report.append("Fecha de certificación: ${worker.certificationDate}\n")
        report.append("Estaciones asignadas (${workstationIds.size}): $workstationIds\n")
        report.append("Nombres de estaciones:\n")
        assignedWorkstations.forEach { station ->
            report.append("  - ${station.name} (ID: ${station.id})\n")
        }
        
        android.util.Log.d("WorkerViewModel", report.toString())
        android.util.Log.d("WorkerViewModel", "===============================")
        
        return report.toString()
    }
    
    /**
     * Obtiene todos los líderes activos.
     */
    suspend fun getLeaders(): List<Worker> {
        android.util.Log.d("WorkerViewModel", "=== OBTENIENDO LÍDERES ===")
        val leaders = workerDao.getLeaders()
        android.util.Log.d("WorkerViewModel", "Líderes encontrados: ${leaders.size}")
        leaders.forEach { leader ->
            android.util.Log.d("WorkerViewModel", "- ${leader.name} (Estación: ${leader.leaderWorkstationId}, Tipo: ${leader.leadershipType})")
        }
        android.util.Log.d("WorkerViewModel", "==========================")
        return leaders
    }
    
    /**
     * Obtiene los líderes para una estación específica.
     */
    suspend fun getLeadersForWorkstation(workstationId: Long): List<Worker> {
        return workerDao.getLeadersForWorkstation(workstationId)
    }
    
    /**
     * Actualiza el liderazgo de un trabajador.
     */
    suspend fun updateWorkerLeadership(workerId: Long, isLeader: Boolean, workstationId: Long?, leadershipType: String) {
        android.util.Log.d("WorkerViewModel", "=== ACTUALIZANDO LIDERAZGO ===")
        android.util.Log.d("WorkerViewModel", "Trabajador: $workerId, Líder: $isLeader, Estación: $workstationId, Tipo: $leadershipType")
        workerDao.updateWorkerLeadership(workerId, isLeader, workstationId, leadershipType)
        android.util.Log.d("WorkerViewModel", "==============================")
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