package com.workstation.rotation.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.workstation.rotation.domain.models.*
import com.workstation.rotation.domain.repository.RotationRepository
import com.workstation.rotation.domain.service.RotationService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class RotationResult(
    val session: RotationSessionModel,
    val assignments: List<RotationAssignmentModel>,
    val workers: List<WorkerModel>,
    val workstations: List<WorkstationModel>
)

class RotationViewModel(
    private val repository: RotationRepository,
    private val rotationService: RotationService
) : ViewModel() {
    
    private val _workers = MutableStateFlow<List<WorkerModel>>(emptyList())
    val workers: StateFlow<List<WorkerModel>> = _workers.asStateFlow()
    
    private val _workstations = MutableStateFlow<List<WorkstationModel>>(emptyList())
    val workstations: StateFlow<List<WorkstationModel>> = _workstations.asStateFlow()
    
    private val _rotationResult = MutableStateFlow<RotationResult?>(null)
    val rotationResult: StateFlow<RotationResult?> = _rotationResult.asStateFlow()
    
    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    init {
        loadData()
    }
    
    private fun loadData() {
        viewModelScope.launch {
            try {
                repository.getActiveWorkers().collect { workerList ->
                    _workers.value = workerList
                }
            } catch (e: Exception) {
                _error.value = "Error al cargar trabajadores: ${e.message}"
            }
        }
        
        viewModelScope.launch {
            try {
                repository.getActiveWorkstations().collect { workstationList ->
                    _workstations.value = workstationList
                }
            } catch (e: Exception) {
                _error.value = "Error al cargar estaciones: ${e.message}"
            }
        }
    }
    
    fun generateRotation(sessionName: String, intervalMinutes: Int = 60) {
        viewModelScope.launch {
            _isGenerating.value = true
            _error.value = null
            
            try {
                val result = rotationService.generateRotation(sessionName, intervalMinutes)
                
                result.onSuccess { session ->
                    val assignments = repository.getAssignmentsBySession(session.id)
                    _rotationResult.value = RotationResult(
                        session = session,
                        assignments = assignments,
                        workers = _workers.value,
                        workstations = _workstations.value
                    )
                }.onFailure { exception ->
                    _error.value = exception.message ?: "Error desconocido"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Error al generar rotación"
            } finally {
                _isGenerating.value = false
            }
        }
    }
    
    fun clearResult() {
        _rotationResult.value = null
    }
    
    fun clearError() {
        _error.value = null
    }
}
