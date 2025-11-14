package com.workstation.rotation.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.workstation.rotation.domain.models.WorkstationModel
import com.workstation.rotation.domain.repository.RotationRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class WorkstationViewModel(
    private val repository: RotationRepository
) : ViewModel() {
    
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
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.getAllWorkstations().collect { workstationList ->
                    _workstations.value = workstationList
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun addWorkstation(name: String, code: String, requiredCapabilities: List<String> = emptyList()) {
        viewModelScope.launch {
            try {
                val workstation = WorkstationModel(
                    name = name,
                    code = code,
                    requiredCapabilities = requiredCapabilities
                )
                repository.insertWorkstation(workstation)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
    
    fun updateWorkstation(workstation: WorkstationModel) {
        viewModelScope.launch {
            try {
                repository.updateWorkstation(workstation)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
    
    fun deleteWorkstation(id: Long) {
        viewModelScope.launch {
            try {
                repository.deleteWorkstation(id)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
    
    fun toggleWorkstationActive(workstation: WorkstationModel) {
        updateWorkstation(workstation.copy(isActive = !workstation.isActive))
    }
    
    fun clearError() {
        _error.value = null
    }
}
