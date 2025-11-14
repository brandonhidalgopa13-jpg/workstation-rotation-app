package com.workstation.rotation.presentation.viewmodels

import com.workstation.rotation.domain.models.WorkstationModel
import com.workstation.rotation.domain.repository.WorkstationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WorkstationViewModel(
    private val repository: WorkstationRepository,
    private val scope: CoroutineScope
) {
    private val _workstations = MutableStateFlow<List<WorkstationModel>>(emptyList())
    val workstations: StateFlow<List<WorkstationModel>> = _workstations.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    init {
        loadWorkstations()
    }
    
    private fun loadWorkstations() {
        scope.launch {
            try {
                _isLoading.value = true
                repository.getAllWorkstations().collect { workstationList ->
                    _workstations.value = workstationList
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Error loading workstations"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun addWorkstation(workstation: WorkstationModel) {
        scope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                repository.insertWorkstation(workstation)
            } catch (e: Exception) {
                _error.value = e.message ?: "Error adding workstation"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun updateWorkstation(workstation: WorkstationModel) {
        scope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                repository.updateWorkstation(workstation)
            } catch (e: Exception) {
                _error.value = e.message ?: "Error updating workstation"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun deleteWorkstation(workstationId: Long) {
        scope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                repository.deleteWorkstation(workstationId)
            } catch (e: Exception) {
                _error.value = e.message ?: "Error deleting workstation"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun clearError() {
        _error.value = null
    }
}
