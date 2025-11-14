package com.workstation.rotation.presentation.viewmodels

import com.workstation.rotation.domain.models.WorkerModel
import com.workstation.rotation.domain.repository.WorkerRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WorkerViewModel(
    private val repository: WorkerRepository,
    private val scope: CoroutineScope
) {
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
        scope.launch {
            try {
                _isLoading.value = true
                repository.getAllWorkers().collect { workerList ->
                    _workers.value = workerList
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Error loading workers"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun addWorker(worker: WorkerModel) {
        scope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                repository.insertWorker(worker)
            } catch (e: Exception) {
                _error.value = e.message ?: "Error adding worker"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun updateWorker(worker: WorkerModel) {
        scope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                repository.updateWorker(worker)
            } catch (e: Exception) {
                _error.value = e.message ?: "Error updating worker"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun deleteWorker(workerId: Long) {
        scope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                repository.deleteWorker(workerId)
            } catch (e: Exception) {
                _error.value = e.message ?: "Error deleting worker"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun clearError() {
        _error.value = null
    }
}
