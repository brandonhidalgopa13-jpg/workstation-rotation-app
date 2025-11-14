package com.workstation.rotation.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.workstation.rotation.domain.models.WorkerModel
import com.workstation.rotation.domain.repository.RotationRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class WorkerViewModel(
    private val repository: RotationRepository
) : ViewModel() {
    
    private val _workers = MutableStateFlow<List<WorkerModel>>(emptyList())
    val workers: StateFlow<List<WorkerModel>> = _workers.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    init {
        loadWorkers()
    }
    
    private fun loadWorkers() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.getAllWorkers().collect { workerList ->
                    _workers.value = workerList
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun addWorker(name: String, code: String, photoPath: String? = null) {
        viewModelScope.launch {
            try {
                val worker = WorkerModel(
                    name = name,
                    code = code,
                    photoPath = photoPath
                )
                repository.insertWorker(worker)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
    
    fun updateWorker(worker: WorkerModel) {
        viewModelScope.launch {
            try {
                repository.updateWorker(worker)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
    
    fun deleteWorker(id: Long) {
        viewModelScope.launch {
            try {
                repository.deleteWorker(id)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
    
    fun toggleWorkerActive(worker: WorkerModel) {
        updateWorker(worker.copy(isActive = !worker.isActive))
    }
    
    fun clearError() {
        _error.value = null
    }
}
