package com.workstation.rotation.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.workstation.rotation.data.entities.RotationHistory
import com.workstation.rotation.services.RotationHistoryService
import com.workstation.rotation.services.GeneralMetrics
import com.workstation.rotation.services.WorkerMetrics
import com.workstation.rotation.services.ProductivityReport
import kotlinx.coroutines.launch
import java.util.*

/**
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 🎯 VIEWMODEL HISTORIAL DE ROTACIONES - GESTIÓN DE ESTADO UI
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 
 * 🔄 FUNCIONALIDADES:
 * • Gestión de estado para pantallas de historial
 * • Operaciones asíncronas de rotación
 * • Métricas en tiempo real
 * • Filtros y búsquedas
 * • Manejo de errores y loading states
 * 
 * 📊 DATOS EXPUESTOS:
 * • LiveData de historial completo y filtrado
 * • Métricas generales y por trabajador
 * • Estados de carga y error
 * • Rotaciones activas en tiempo real
 * 
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 */

class RotationHistoryViewModel(application: Application) : AndroidViewModel(application) {
    
    private val rotationHistoryService = RotationHistoryService(application)
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // 🔄 LIVEDATA PRINCIPALES
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    val allHistory: LiveData<List<RotationHistory>> = rotationHistoryService.getAllHistoryLiveData()
    val activeRotations: LiveData<List<RotationHistory>> = rotationHistoryService.getActiveRotationsLiveData()
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // 🎯 ESTADOS DE UI
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage
    
    private val _generalMetrics = MutableLiveData<GeneralMetrics>()
    val generalMetrics: LiveData<GeneralMetrics> = _generalMetrics
    
    private val _workerMetrics = MutableLiveData<WorkerMetrics>()
    val workerMetrics: LiveData<WorkerMetrics> = _workerMetrics
    
    private val _productivityReport = MutableLiveData<ProductivityReport>()
    val productivityReport: LiveData<ProductivityReport> = _productivityReport
    
    private val _operationSuccess = MutableLiveData<String?>()
    val operationSuccess: LiveData<String?> = _operationSuccess
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // 🔧 FILTROS Y BÚSQUEDA
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    private val _filteredHistory = MutableLiveData<List<RotationHistory>>()
    val filteredHistory: LiveData<List<RotationHistory>> = _filteredHistory
    
    private val _selectedWorkerId = MutableLiveData<Long?>()
    val selectedWorkerId: LiveData<Long?> = _selectedWorkerId
    
    private val _selectedWorkstationId = MutableLiveData<Long?>()
    val selectedWorkstationId: LiveData<Long?> = _selectedWorkstationId
    
    private val _dateRangeStart = MutableLiveData<Date?>()
    val dateRangeStart: LiveData<Date?> = _dateRangeStart
    
    private val _dateRangeEnd = MutableLiveData<Date?>()
    val dateRangeEnd: LiveData<Date?> = _dateRangeEnd
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // 🔄 OPERACIONES DE ROTACIÓN
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    /**
     * Inicia una nueva rotación
     */
    fun startRotation(
        workerId: Long,
        workstationId: Long,
        rotationType: String = RotationHistory.TYPE_MANUAL,
        notes: String? = null
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                
                val rotationId = rotationHistoryService.startRotation(
                    workerId, workstationId, rotationType, notes
                )
                
                _operationSuccess.value = "Rotación iniciada exitosamente (ID: $rotationId)"
                loadGeneralMetrics() // Actualizar métricas
                
            } catch (e: Exception) {
                _errorMessage.value = "Error al iniciar rotación: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Finaliza una rotación específica
     */
    fun finishRotation(
        rotationId: Long,
        performanceScore: Double? = null,
        notes: String? = null
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                
                val success = rotationHistoryService.finishRotation(rotationId, performanceScore, notes)
                
                if (success) {
                    _operationSuccess.value = "Rotación finalizada exitosamente"
                    loadGeneralMetrics() // Actualizar métricas
                } else {
                    _errorMessage.value = "No se pudo finalizar la rotación"
                }
                
            } catch (e: Exception) {
                _errorMessage.value = "Error al finalizar rotación: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Finaliza la rotación activa de un trabajador
     */
    fun finishActiveRotationForWorker(workerId: Long, performanceScore: Double? = null) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                
                val success = rotationHistoryService.finishActiveRotationForWorker(workerId, performanceScore)
                
                if (success) {
                    _operationSuccess.value = "Rotación del trabajador finalizada"
                    loadGeneralMetrics()
                } else {
                    _errorMessage.value = "El trabajador no tiene rotaciones activas"
                }
                
            } catch (e: Exception) {
                _errorMessage.value = "Error al finalizar rotación: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // 📊 CARGA DE MÉTRICAS
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    /**
     * Carga métricas generales del sistema
     */
    fun loadGeneralMetrics() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val metrics = rotationHistoryService.getGeneralMetrics()
                _generalMetrics.value = metrics
            } catch (e: Exception) {
                _errorMessage.value = "Error al cargar métricas: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Carga métricas de un trabajador específico
     */
    fun loadWorkerMetrics(workerId: Long) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val metrics = rotationHistoryService.getWorkerMetrics(workerId)
                _workerMetrics.value = metrics
            } catch (e: Exception) {
                _errorMessage.value = "Error al cargar métricas del trabajador: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Genera reporte de productividad
     */
    fun generateProductivityReport(startDate: Date, endDate: Date) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val report = rotationHistoryService.generateProductivityReport(startDate, endDate)
                _productivityReport.value = report
            } catch (e: Exception) {
                _errorMessage.value = "Error al generar reporte: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // 🔍 FILTROS Y BÚSQUEDA
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    /**
     * Obtiene historial por trabajador
     */
    fun getHistoryByWorker(workerId: Long): LiveData<List<RotationHistory>> {
        _selectedWorkerId.value = workerId
        return rotationHistoryService.getHistoryByWorkerLiveData(workerId)
    }
    
    /**
     * Obtiene historial por estación
     */
    fun getHistoryByWorkstation(workstationId: Long): LiveData<List<RotationHistory>> {
        _selectedWorkstationId.value = workstationId
        return rotationHistoryService.getHistoryByWorkstationLiveData(workstationId)
    }
    
    /**
     * Filtra historial por rango de fechas
     */
    fun filterByDateRange(startDate: Date, endDate: Date) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _dateRangeStart.value = startDate
                _dateRangeEnd.value = endDate
                
                val filteredData = rotationHistoryService.getHistoryByDateRange(startDate, endDate)
                _filteredHistory.value = filteredData
                
            } catch (e: Exception) {
                _errorMessage.value = "Error al filtrar por fechas: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Limpia todos los filtros
     */
    fun clearFilters() {
        _selectedWorkerId.value = null
        _selectedWorkstationId.value = null
        _dateRangeStart.value = null
        _dateRangeEnd.value = null
        _filteredHistory.value = emptyList()
    }
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // 🧹 OPERACIONES DE MANTENIMIENTO
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    /**
     * Limpia registros antiguos
     */
    fun cleanOldRecords(daysToKeep: Int = 90) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val deletedCount = rotationHistoryService.cleanOldRecords(daysToKeep)
                _operationSuccess.value = "Se eliminaron $deletedCount registros antiguos"
                loadGeneralMetrics()
            } catch (e: Exception) {
                _errorMessage.value = "Error al limpiar registros: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Finaliza todas las rotaciones activas
     */
    fun finishAllActiveRotations() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val finishedCount = rotationHistoryService.finishAllActiveRotations()
                _operationSuccess.value = "Se finalizaron $finishedCount rotaciones activas"
                loadGeneralMetrics()
            } catch (e: Exception) {
                _errorMessage.value = "Error al finalizar rotaciones: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // 🔄 UTILIDADES
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    /**
     * Limpia mensajes de error y éxito
     */
    fun clearMessages() {
        _errorMessage.value = null
        _operationSuccess.value = null
    }
    
    /**
     * Inicialización del ViewModel
     */
    init {
        loadGeneralMetrics()
    }
}