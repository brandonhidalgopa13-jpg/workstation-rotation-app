package com.workstation.rotation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import com.workstation.rotation.data.dao.WorkstationDao
import com.workstation.rotation.data.entities.Workstation

class WorkstationViewModel(private val workstationDao: WorkstationDao) : ViewModel() {
    
    val allWorkstations = workstationDao.getAllWorkstations().asLiveData()
    val activeWorkstations = workstationDao.getAllActiveWorkstations().asLiveData()
    
    suspend fun insertWorkstation(workstation: Workstation) {
        workstationDao.insertWorkstation(workstation)
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
}

class WorkstationViewModelFactory(private val workstationDao: WorkstationDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WorkstationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WorkstationViewModel(workstationDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}