package com.workstation.rotation.services

import android.content.Context
import androidx.lifecycle.LiveData
import com.workstation.rotation.data.database.AppDatabase
import com.workstation.rotation.data.entities.RotationHistory
import com.workstation.rotation.data.dao.RotationHistoryDao
import com.workstation.rotation.data.dao.WorkerWorkstationStats
import com.workstation.rotation.data.dao.RotationTypeStats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

/**
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 🔄 SERVICIO DE HISTORIAL DE ROTACIONES - GESTIÓN COMPLETA DE TRACKING
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 
 * 🎯 RESPONSABILIDADES:
 * • Registro automático de rotaciones
 * • Finalización y cálculo de métricas
 * • Consultas optimizadas para reportes
 * • Limpieza y mantenimiento de datos
 * • Análisis de patrones y tendencias
 * 
 * 📊 FUNCIONALIDADES CLAVE:
 * • Tracking en tiempo real de rotaciones activas
 * • Cálculo automático de duraciones y rendimiento
 * • Generación de métricas para dashboards
 * • Exportación de datos para análisis externos
 * 
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 */

class RotationHistoryService(context: Context) {
    
    private val database = AppDatabase.getDatabase(context)
    private val rotationHistoryDao: RotationHistoryDao = database.rotationHistoryDao()
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // 🔧 OPERACIONES DE ROTACIÓN
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    /**
     * Inicia una nueva rotación y la registra en el historial
     */
    suspend fun startRotation(
        workerId: Long,
        workstationId: Long,
        rotationType: String = RotationHistory.TYPE_MANUAL,
        notes: String? = null
    ): Long = withContext(Dispatchers.IO) {
        
        // Finalizar cualquier rotación activa del trabajador
        finishActiveRotationForWorker(workerId)
        
        // Crear nuevo registro de rotación
        val rotationHistory = RotationHistory(
            worker_id = workerId,
            workstation_id = workstationId,
            rotation_date = System.currentTimeMillis(),
            rotation_type = rotationType,
            notes = notes,
            completed = false
        )
        
        rotationHistoryDao.insert(rotationHistory)
    }
    
    /**
     * Finaliza una rotación específica
     */
    suspend fun finishRotation(
        rotationId: Long,
        performanceScore: Double? = null,
        notes: String? = null
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val rotation = rotationHistoryDao.getById(rotationId)
            if (rotation != null && rotation.end_date == null) {
                val endTime = System.currentTimeMillis()
                val duration = RotationHistory.calculateDuration(rotation.rotation_date, endTime)
                
                rotationHistoryDao.completeRotation(rotationId, endTime, duration)
                
                // Actualizar score si se proporciona
                performanceScore?.let {
                    rotationHistoryDao.updatePerformanceScore(rotationId, it)
                }
                
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Finaliza la rotación activa de un trabajador específico
     */
    suspend fun finishActiveRotationForWorker(
        workerId: Long,
        performanceScore: Double? = null
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val activeRotation = rotationHistoryDao.getActiveRotationForWorker(workerId)
            activeRotation?.let { rotation ->
                finishRotation(rotation.id, performanceScore)
            } ?: false
        } catch (e: Exception) {
            false
        }
    }
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // 📋 CONSULTAS DE DATOS
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    /**
     * Obtiene todo el historial con LiveData
     */
    fun getAllHistoryLiveData(): LiveData<List<RotationHistory>> {
        return rotationHistoryDao.getAllLiveData()
    }
    
    /**
     * Obtiene historial por trabajador
     */
    fun getHistoryByWorkerLiveData(workerId: Long): LiveData<List<RotationHistory>> {
        return rotationHistoryDao.getByWorkerLiveData(workerId)
    }
    
    /**
     * Obtiene historial por estación
     */
    fun getHistoryByWorkstationLiveData(workstationId: Long): LiveData<List<RotationHistory>> {
        return rotationHistoryDao.getByWorkstationLiveData(workstationId)
    }
    
    /**
     * Obtiene rotaciones activas
     */
    fun getActiveRotationsLiveData(): LiveData<List<RotationHistory>> {
        return rotationHistoryDao.getActiveRotationsLiveData()
    }
    
    /**
     * Obtiene historial por rango de fechas
     */
    suspend fun getHistoryByDateRange(startDate: Date, endDate: Date): List<RotationHistory> = 
        withContext(Dispatchers.IO) {
            rotationHistoryDao.getByDateRange(startDate.time, endDate.time)
        }
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // 📊 MÉTRICAS Y ESTADÍSTICAS
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    /**
     * Obtiene métricas generales del sistema
     */
    suspend fun getGeneralMetrics(): GeneralMetrics = withContext(Dispatchers.IO) {
        val totalRotations = rotationHistoryDao.getTotalRotations()
        val activeRotations = rotationHistoryDao.getActiveRotations().size
        val rotationTypeStats = rotationHistoryDao.getRotationTypeStats()
        
        GeneralMetrics(
            totalRotations = totalRotations,
            activeRotations = activeRotations,
            rotationTypeStats = rotationTypeStats
        )
    }
    
    /**
     * Obtiene métricas de un trabajador específico
     */
    suspend fun getWorkerMetrics(workerId: Long): WorkerMetrics = withContext(Dispatchers.IO) {
        val totalRotations = rotationHistoryDao.getTotalRotationsByWorker(workerId)
        val averageDuration = rotationHistoryDao.getAverageDurationByWorker(workerId)
        val averagePerformance = rotationHistoryDao.getAveragePerformanceByWorker(workerId)
        val recentHistory = rotationHistoryDao.getByWorker(workerId).take(10)
        
        WorkerMetrics(
            workerId = workerId,
            totalRotations = totalRotations,
            averageDuration = averageDuration,
            averagePerformance = averagePerformance,
            recentHistory = recentHistory
        )
    }
    
    /**
     * Obtiene estadísticas de trabajador-estación para un período
     */
    suspend fun getWorkerWorkstationStats(
        startDate: Date,
        endDate: Date
    ): List<WorkerWorkstationStats> = withContext(Dispatchers.IO) {
        rotationHistoryDao.getWorkerWorkstationStats(startDate.time, endDate.time)
    }
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // 🧹 MANTENIMIENTO Y LIMPIEZA
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    /**
     * Limpia registros antiguos (más de X días)
     */
    suspend fun cleanOldRecords(daysToKeep: Int = 90): Int = withContext(Dispatchers.IO) {
        val cutoffDate = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -daysToKeep)
        }.timeInMillis
        
        rotationHistoryDao.deleteOldRecords(cutoffDate)
    }
    
    /**
     * Finaliza todas las rotaciones activas (útil para cierre de turno)
     */
    suspend fun finishAllActiveRotations(): Int = withContext(Dispatchers.IO) {
        val activeRotations = rotationHistoryDao.getActiveRotations()
        var finishedCount = 0
        
        activeRotations.forEach { rotation ->
            if (finishRotation(rotation.id)) {
                finishedCount++
            }
        }
        
        finishedCount
    }
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // 📈 ANÁLISIS Y REPORTES
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    /**
     * Genera reporte de productividad por período
     */
    suspend fun generateProductivityReport(
        startDate: Date,
        endDate: Date
    ): ProductivityReport = withContext(Dispatchers.IO) {
        val historyInPeriod = rotationHistoryDao.getByDateRange(startDate.time, endDate.time)
        val completedRotations = historyInPeriod.filter { it.completed }
        
        val totalDuration = completedRotations.mapNotNull { it.duration_minutes }.sum()
        val averageDuration = if (completedRotations.isNotEmpty()) {
            totalDuration.toDouble() / completedRotations.size
        } else 0.0
        
        val averagePerformance = completedRotations
            .mapNotNull { it.performance_score }
            .takeIf { it.isNotEmpty() }
            ?.average() ?: 0.0
        
        ProductivityReport(
            period = "${startDate.time} - ${endDate.time}",
            totalRotations = historyInPeriod.size,
            completedRotations = completedRotations.size,
            totalDurationMinutes = totalDuration,
            averageDurationMinutes = averageDuration,
            averagePerformanceScore = averagePerformance
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════════════════════════
// 📊 DATA CLASSES PARA MÉTRICAS
// ═══════════════════════════════════════════════════════════════════════════════════════════════

data class GeneralMetrics(
    val totalRotations: Int,
    val activeRotations: Int,
    val rotationTypeStats: List<RotationTypeStats>
)

data class WorkerMetrics(
    val workerId: Long,
    val totalRotations: Int,
    val averageDuration: Double?,
    val averagePerformance: Double?,
    val recentHistory: List<RotationHistory>
)

data class ProductivityReport(
    val period: String,
    val totalRotations: Int,
    val completedRotations: Int,
    val totalDurationMinutes: Int,
    val averageDurationMinutes: Double,
    val averagePerformanceScore: Double
)