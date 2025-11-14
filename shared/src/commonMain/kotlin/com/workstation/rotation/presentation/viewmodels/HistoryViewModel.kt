package com.workstation.rotation.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.workstation.rotation.domain.models.RotationSessionModel
import com.workstation.rotation.domain.service.RotationService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val rotationService: RotationService
) : ViewModel() {
    
    private val _sessions = MutableStateFlow<List<RotationSessionModel>>(emptyList())
    val sessions: StateFlow<List<RotationSessionModel>> = _sessions.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    init {
        loadHistory()
    }
    
    fun loadHistory() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val history = rotationService.getRotationHistory()
                _sessions.value = history
            } catch (e: Exception) {
                _error.value = e.message ?: "Error al cargar historial"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun clearError() {
        _error.value = null
    }
}
