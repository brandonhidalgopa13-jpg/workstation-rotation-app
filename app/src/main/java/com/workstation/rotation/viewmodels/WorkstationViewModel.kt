package com.workstation.rotation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import com.workstation.rotation.data.dao.WorkstationDao
import com.workstation.rotation.data.entities.Workstation

class WorkstationViewModel(
    private val workstationDao: WorkstationDao
) : ViewModel() {
    
    val allWorkstations = workstationDao.getAllWorkstations().asLiveData()
    val activeWorkstations = workstationDao.getAllActiveWorkstations().asLiveData()
    
    suspend fun insertWorkstation(workstation: Workstation): Long {
        return workstationDao.insertWorkstation(workstation)
    }
    
    suspend fun updateWorkstation(workstation: Workstation) {
        workstationDao.updateWorkstation(workstation)
    }
    
    suspend fun deleteWorkstation(workstation: Workstation) {
        workstationDao.deleteWorkstation(workstation)
    }
    
    suspend fun updateWorkstationStatus(id: Long, isActive: Boolean) {
        workstationDao.updateWorkstationStatus(id, isActive)
    }
    
    suspend fun getWorkstationById(id: Long): Workstation? {
        return workstationDao.getWorkstationById(id)
    }
    
    suspend fun getPriorityWorkstations(): List<Workstation> {
        return workstationDao.getPriorityWorkstations()
    }
    
    suspend fun getTrainingWorkstations(): List<Workstation> {
        return workstationDao.getTrainingWorkstations()
    }
    
    suspend fun getActiveWorkstationCount(): Int {
        return workstationDao.getActiveWorkstationCount()
    }
    
    suspend fun getTotalRequiredWorkers(): Int {
        return workstationDao.getTotalRequiredWorkers() ?: 0
    }
    
    suspend fun getTotalMaxWorkers(): Int {
        return workstationDao.getTotalMaxWorkers() ?: 0
    }
    
    suspend fun updateUtilizationRate(id: Long, rate: Double) {
        workstationDao.updateUtilizationRate(id, rate)
    }
    
    suspend fun updateMaintenanceDates(id: Long, lastMaintenance: Long?, nextMaintenance: Long?) {
        lastMaintenance?.let { workstationDao.updateLastMaintenanceDate(id, it) }
        nextMaintenance?.let { workstationDao.updateNextMaintenanceDate(id, it) }
    }
    
    suspend fun getAvailableWorkstationsForRotation(): List<Workstation> {
        return workstationDao.getAvailableWorkstationsForRotation()
    }
    
    suspend fun isWorkstationUsedForTraining(workstationId: Long): Boolean {
        // Esta función debería verificar si hay trabajadores en entrenamiento asignados a esta estación
        // Por ahora retornamos false, pero se puede implementar la lógica completa más tarde
        return false
    }
}

class WorkstationViewModelFactory(
    private val workstationDao: WorkstationDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WorkstationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WorkstationViewModel(workstationDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}