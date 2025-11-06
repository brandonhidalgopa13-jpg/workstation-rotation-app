package com.workstation.rotation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.workstation.rotation.data.entities.RotationSession
import com.workstation.rotation.models.RotationGrid
import com.workstation.rotation.models.RotationGridCell
import com.workstation.rotation.services.NewRotationService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 🧠 VIEWMODEL PARA NUEVA ROTACIÓN - v4.0
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 
 * 🎯 PROPÓSITO:
 * ViewModel que gestiona el estado y la lógica de la nueva interfaz de rotación.
 * Coordina entre la UI y el servicio de rotación.
 * 
 * 📋 CARACTERÍSTICAS:
 * • Gestión de estado reactivo con Flow
 * • Operaciones asíncronas con Coroutines
 * • Manejo de errores y loading states
 * • Comunicación bidireccional con UI
 * 
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 */

class NewRotationViewModel(
    private val rotationService: NewRotationService
) : ViewModel() {

    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // 🔄 ESTADO DE LA UI
    // ═══════════════════════════════════════════════════════════════════════════════════════════

    private val _uiState = MutableStateFlow(NewRotationUiState())
    val uiState: StateFlow<NewRotationUiState> = _uiState.asStateFlow()

    private val _activeSession = MutableStateFlow<RotationSession?>(null)
    val activeSession: StateFlow<RotationSession?> = _activeSession.asStateFlow()

    private val _rotationGrid = MutableStateFlow<RotationGrid?>(null)
    val rotationGrid: StateFlow<RotationGrid?> = _rotationGrid.asStateFlow()

    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // 🚀 INICIALIZACIÓN
    // ═══════════════════════════════════════════════════════════════════════════════════════════

    init {
        observeActiveSession()
    }

    /**
     * Carga los datos iniciales necesarios para la rotación
     */
    fun loadInitialData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, loadingMessage = "Cargando datos...")
            
            try {
                // Verificar si hay una sesión activa usando el flow
                rotationService.getActiveSessionFlow().collect { activeSession ->
                    if (activeSession == null) {
                        // Crear una sesión inicial si no existe
                        createNewSession("Sesión Inicial", "Sesión creada automáticamente")
                    } else {
                        _uiState.value = _uiState.value.copy(isLoading = false, loadingMessage = null)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    loadingMessage = null,
                    error = "Error al cargar datos: ${e.message}"
                )
            }
        }
    }

    private fun observeActiveSession() {
        viewModelScope.launch {
            rotationService.getActiveSessionFlow().collect { session ->
                _activeSession.value = session
                session?.let { observeRotationGrid(it.id) }
            }
        }
    }

    private fun observeRotationGrid(sessionId: Long) {
        viewModelScope.launch {
            rotationService.getRotationGridFlow(sessionId).collect { grid ->
                _rotationGrid.value = grid
                updateUiMetrics(grid)
            }
        }
    }

    private fun updateUiMetrics(grid: RotationGrid) {
        _uiState.value = _uiState.value.copy(
            totalCurrentAssigned = grid.getTotalCurrentAssigned(),
            totalNextAssigned = grid.getTotalNextAssigned(),
            totalRequired = grid.getTotalRequired(),
            currentCompletionPercentage = grid.getCurrentCompletionPercentage(),
            nextCompletionPercentage = grid.getNextCompletionPercentage(),
            hasConflicts = grid.hasConflicts(),
            conflicts = grid.getConflicts()
        )
    }

    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // 🎯 OPERACIONES DE SESIÓN
    // ═══════════════════════════════════════════════════════════════════════════════════════════

    fun createNewSession(name: String? = null, description: String? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, loadingMessage = "Creando sesión...")
            
            try {
                val sessionId = rotationService.createRotationSession(
                    name = name ?: RotationSession.generateSessionName(),
                    description = description
                )
                
                rotationService.activateSession(sessionId)
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    message = "Sesión creada exitosamente"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error al crear sesión: ${e.message}"
                )
            }
        }
    }

    fun activateSession(sessionId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, loadingMessage = "Activando sesión...")
            
            try {
                val success = rotationService.activateSession(sessionId)
                if (success) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        message = "Sesión activada exitosamente"
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "No se pudo activar la sesión"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error al activar sesión: ${e.message}"
                )
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // 🎯 OPERACIONES DE ASIGNACIÓN
    // ═══════════════════════════════════════════════════════════════════════════════════════════

    fun assignWorkerToWorkstation(
        workerId: Long,
        workstationId: Long,
        rotationType: String
    ) {
        val sessionId = _activeSession.value?.id ?: return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, loadingMessage = "Asignando trabajador...")
            
            try {
                val result = rotationService.assignWorkerToWorkstation(
                    sessionId = sessionId,
                    workerId = workerId,
                    workstationId = workstationId,
                    rotationType = rotationType
                )
                
                result.fold(
                    onSuccess = {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            message = "Trabajador asignado exitosamente"
                        )
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Error en asignación: ${error.message}"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error inesperado: ${e.message}"
                )
            }
        }
    }

    fun removeWorkerAssignment(workerId: Long, rotationType: String) {
        val sessionId = _activeSession.value?.id ?: return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, loadingMessage = "Removiendo asignación...")
            
            try {
                val success = rotationService.removeWorkerAssignment(
                    sessionId = sessionId,
                    workerId = workerId,
                    rotationType = rotationType
                )
                
                if (success) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        message = "Asignación removida exitosamente"
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "No se pudo remover la asignación"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error al remover asignación: ${e.message}"
                )
            }
        }
    }

    fun moveWorkerAssignment(
        workerId: Long,
        fromWorkstationId: Long,
        toWorkstationId: Long,
        rotationType: String
    ) {
        val sessionId = _activeSession.value?.id ?: return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, loadingMessage = "Moviendo trabajador...")
            
            try {
                val result = rotationService.moveWorkerAssignment(
                    sessionId = sessionId,
                    workerId = workerId,
                    fromWorkstationId = fromWorkstationId,
                    toWorkstationId = toWorkstationId,
                    rotationType = rotationType
                )
                
                result.fold(
                    onSuccess = {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            message = "Trabajador movido exitosamente"
                        )
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Error al mover trabajador: ${error.message}"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error inesperado: ${e.message}"
                )
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // 🤖 GENERACIÓN AUTOMÁTICA
    // ═══════════════════════════════════════════════════════════════════════════════════════════

    fun generateOptimizedRotation(rotationType: String, clearExisting: Boolean = true) {
        val sessionId = _activeSession.value?.id ?: return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true, 
                loadingMessage = "Generando rotación optimizada..."
            )
            
            try {
                val result = rotationService.generateOptimizedRotation(
                    sessionId = sessionId,
                    rotationType = rotationType,
                    clearExisting = clearExisting
                )
                
                result.fold(
                    onSuccess = { assignmentCount ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            message = "Rotación generada: $assignmentCount asignaciones creadas"
                        )
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Error al generar rotación: ${error.message}"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error inesperado: ${e.message}"
                )
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // 🔄 TRANSICIONES DE ROTACIÓN
    // ═══════════════════════════════════════════════════════════════════════════════════════════

    fun promoteNextToCurrent() {
        val sessionId = _activeSession.value?.id ?: return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true, 
                loadingMessage = "Promoviendo siguiente rotación..."
            )
            
            try {
                val success = rotationService.promoteNextToCurrent(sessionId)
                if (success) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        message = "Rotación promovida exitosamente"
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "No se pudo promover la rotación"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error al promover rotación: ${e.message}"
                )
            }
        }
    }

    fun copyCurrentToNext() {
        val sessionId = _activeSession.value?.id ?: return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true, 
                loadingMessage = "Copiando rotación actual..."
            )
            
            try {
                val success = rotationService.copyCurrentToNext(sessionId)
                if (success) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        message = "Rotación copiada exitosamente"
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "No se pudo copiar la rotación"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error al copiar rotación: ${e.message}"
                )
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // 🎯 MANEJO DE EVENTOS DE UI
    // ═══════════════════════════════════════════════════════════════════════════════════════════

    fun onCellClick(cell: RotationGridCell, rotationType: String) {
        // Lógica para manejar click en celda
        if (cell.isAssigned) {
            // Mostrar opciones para trabajador asignado
            _uiState.value = _uiState.value.copy(
                selectedCell = cell,
                showWorkerOptions = true
            )
        } else {
            // Mostrar lista de trabajadores disponibles para asignar
            _uiState.value = _uiState.value.copy(
                selectedCell = cell,
                showWorkerSelection = true
            )
        }
    }

    fun onCellLongClick(cell: RotationGridCell, rotationType: String): Boolean {
        if (cell.isAssigned && cell.workerId != null) {
            // Iniciar drag & drop o mostrar menú contextual
            _uiState.value = _uiState.value.copy(
                selectedCell = cell,
                showContextMenu = true
            )
            return true
        }
        return false
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            message = null,
            error = null
        )
    }

    fun clearSelections() {
        _uiState.value = _uiState.value.copy(
            selectedCell = null,
            showWorkerOptions = false,
            showWorkerSelection = false,
            showContextMenu = false
        )
    }

    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // 🏭 FACTORY
    // ═══════════════════════════════════════════════════════════════════════════════════════════

    class Factory(
        private val rotationService: NewRotationService
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(NewRotationViewModel::class.java)) {
                return NewRotationViewModel(rotationService) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

/**
 * Estado de la UI para la nueva rotación
 */
data class NewRotationUiState(
    val isLoading: Boolean = false,
    val loadingMessage: String? = null,
    val message: String? = null,
    val error: String? = null,
    
    // Métricas
    val totalCurrentAssigned: Int = 0,
    val totalNextAssigned: Int = 0,
    val totalRequired: Int = 0,
    val currentCompletionPercentage: Float = 0f,
    val nextCompletionPercentage: Float = 0f,
    
    // Conflictos
    val hasConflicts: Boolean = false,
    val conflicts: List<String> = emptyList(),
    
    // Selecciones y diálogos
    val selectedCell: RotationGridCell? = null,
    val showWorkerOptions: Boolean = false,
    val showWorkerSelection: Boolean = false,
    val showContextMenu: Boolean = false
)