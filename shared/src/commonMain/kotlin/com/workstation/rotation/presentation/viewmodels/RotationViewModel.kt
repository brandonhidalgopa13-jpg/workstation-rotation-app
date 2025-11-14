package com.workstation.rotation.presentation.viewmodels

import com.workstation.rotation.domain.models.RotationAssignmentModel
import com.workstation.rotation.domain.models.RotationSessionModel
import com.workstation.rotation.domain.repository.RotationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RotationViewModel(
    private val repository: RotationRepository,
    private val scope: CoroutineScope
) {
    // Sessions
    private val _sessions = MutableStateFlow<List<RotationSessionModel>>(emptyList())
    val sessions: StateFlow<List<RotationSessionModel>> = _sessions.asStateFlow()
    
    private val _activeSession = MutableStateFlow<RotationSessionModel?>(null)
    val activeSession: StateFlow<RotationSessionModel?> = _activeSession.asStateFlow()
    
    // Assignments
    private val _assignments = MutableStateFlow<List<RotationAssignmentModel>>(emptyList())
    val assignments: StateFlow<List<RotationAssignmentModel>> = _assignments.asStateFlow()
    
    // UI State
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    init {
        loadSessions()
        loadActiveSession()
    }
    
    private fun loadSessions() {
        scope.launch {
            try {
                _isLoading.value = true
                repository.getAllSessions().collect { sessionList ->
                    _sessions.value = sessionList
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Error loading sessions"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    private fun loadActiveSession() {
        scope.launch {
            try {
                repository.getActiveSession().collect { session ->
                    _activeSession.value = session
                    session?.let { loadAssignments(it.id) }
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Error loading active session"
            }
        }
    }
    
    fun loadAssignments(sessionId: Long) {
        scope.launch {
            try {
                _isLoading.value = true
                repository.getAssignmentsBySession(sessionId).collect { assignmentList ->
                    _assignments.value = assignmentList
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Error loading assignments"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun createSession(session: RotationSessionModel) {
        scope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                repository.insertSession(session)
            } catch (e: Exception) {
                _error.value = e.message ?: "Error creating session"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun updateSession(session: RotationSessionModel) {
        scope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                repository.updateSession(session)
            } catch (e: Exception) {
                _error.value = e.message ?: "Error updating session"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun deleteSession(sessionId: Long) {
        scope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                repository.deleteSession(sessionId)
            } catch (e: Exception) {
                _error.value = e.message ?: "Error deleting session"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun addAssignment(assignment: RotationAssignmentModel) {
        scope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                repository.insertAssignment(assignment)
            } catch (e: Exception) {
                _error.value = e.message ?: "Error adding assignment"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun deleteAssignment(assignmentId: Long) {
        scope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                repository.deleteAssignment(assignmentId)
            } catch (e: Exception) {
                _error.value = e.message ?: "Error deleting assignment"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun clearAssignments(sessionId: Long) {
        scope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                repository.deleteAssignmentsBySession(sessionId)
            } catch (e: Exception) {
                _error.value = e.message ?: "Error clearing assignments"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun clearError() {
        _error.value = null
    }
}
