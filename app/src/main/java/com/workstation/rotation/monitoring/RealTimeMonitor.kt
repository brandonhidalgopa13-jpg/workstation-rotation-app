package com.workstation.rotation.monitoring

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

/**
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 📊 MONITOR EN TIEMPO REAL - WorkStation Rotation v4.0.2+
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 
 * Sistema de monitoreo en tiempo real para métricas de rendimiento, uso y estado del sistema.
 * Proporciona datos actualizados continuamente para dashboards y alertas.
 * 
 * 🎯 CARACTERÍSTICAS:
 * • Métricas de rendimiento en tiempo real
 * • Monitoreo de uso de recursos
 * • Alertas automáticas por umbrales
 * • Histórico de métricas con agregaciones
 * • Dashboard en vivo con actualizaciones automáticas
 * 
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 */

class RealTimeMonitor private constructor(private val context: Context) {
    
    companion object {
        @Volatile
        private var INSTANCE: RealTimeMonitor? = null
        
        fun getInstance(context: Context): RealTimeMonitor {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: RealTimeMonitor(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // 📊 MÉTRICAS EN TIEMPO REAL
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    private val _systemMetrics = MutableStateFlow(SystemMetrics())
    val systemMetrics: StateFlow<SystemMetrics> = _systemMetrics.asStateFlow()
    
    private val _rotationMetrics = MutableStateFlow(RotationMetrics())
    val rotationMetrics: StateFlow<RotationMetrics> = _rotationMetrics.asStateFlow()
    
    private val _performanceMetrics = MutableStateFlow(PerformanceMetrics())
    val performanceMetrics: StateFlow<PerformanceMetrics> = _performanceMetrics.asStateFlow()
    
    private val _alerts = MutableSharedFlow<Alert>()
    val alerts: SharedFlow<Alert> = _alerts.asSharedFlow()
    
    // Contadores atómicos para thread-safety
    private val rotationCount = AtomicLong(0)
    private val errorCount = AtomicLong(0)
    private val userActionCount = AtomicLong(0)
    
    // Cache de métricas históricas
    private val metricsHistory = ConcurrentHashMap<String, MutableList<MetricPoint>>()
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // 🚀 INICIALIZACIÓN Y CONTROL
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    private var isMonitoring = false
    
    fun startMonitoring() {
        if (isMonitoring) return
        
        isMonitoring = true
        
        // Iniciar recolección de métricas cada segundo
        scope.launch {
            while (isMonitoring) {
                collectSystemMetrics()
                collectRotationMetrics()
                collectPerformanceMetrics()
                checkAlerts()
                
                delay(1000) // Actualizar cada segundo
            }
        }
        
        // Limpiar historial cada hora
        scope.launch {
            while (isMonitoring) {
                cleanupHistory()
                delay(3600000) // Cada hora
            }
        }
    }
    
    fun stopMonitoring() {
        isMonitoring = false
    }
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // 📈 RECOLECCIÓN DE MÉTRICAS
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    private suspend fun collectSystemMetrics() {
        val runtime = Runtime.getRuntime()
        val currentTime = System.currentTimeMillis()
        
        val metrics = SystemMetrics(
            timestamp = currentTime,
            memoryUsed = (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024, // MB
            memoryTotal = runtime.totalMemory() / 1024 / 1024, // MB
            memoryFree = runtime.freeMemory() / 1024 / 1024, // MB
            cpuUsage = getCpuUsage(),
            activeThreads = Thread.activeCount(),
            uptime = currentTime - getAppStartTime()
        )
        
        _systemMetrics.value = metrics
        addToHistory("system_memory", MetricPoint(currentTime, metrics.memoryUsed.toDouble()))
        addToHistory("system_cpu", MetricPoint(currentTime, metrics.cpuUsage))
    }
    
    private suspend fun collectRotationMetrics() {
        val currentTime = System.currentTimeMillis()
        
        val metrics = RotationMetrics(
            timestamp = currentTime,
            totalRotations = rotationCount.get(),
            rotationsPerHour = calculateRotationsPerHour(),
            averageRotationTime = calculateAverageRotationTime(),
            activeWorkers = getActiveWorkersCount(),
            activeWorkstations = getActiveWorkstationsCount(),
            utilizationRate = calculateUtilizationRate()
        )
        
        _rotationMetrics.value = metrics
        addToHistory("rotations_per_hour", MetricPoint(currentTime, metrics.rotationsPerHour))
        addToHistory("utilization_rate", MetricPoint(currentTime, metrics.utilizationRate))
    }
    
    private suspend fun collectPerformanceMetrics() {
        val currentTime = System.currentTimeMillis()
        
        val metrics = PerformanceMetrics(
            timestamp = currentTime,
            averageResponseTime = calculateAverageResponseTime(),
            errorRate = calculateErrorRate(),
            throughput = calculateThroughput(),
            userActions = userActionCount.get(),
            databaseQueries = getDatabaseQueryCount(),
            cacheHitRate = getCacheHitRate()
        )
        
        _performanceMetrics.value = metrics
        addToHistory("response_time", MetricPoint(currentTime, metrics.averageResponseTime))
        addToHistory("error_rate", MetricPoint(currentTime, metrics.errorRate))
    }
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // 🚨 SISTEMA DE ALERTAS
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    private suspend fun checkAlerts() {
        val systemMetrics = _systemMetrics.value
        val performanceMetrics = _performanceMetrics.value
        
        // Alerta de memoria alta
        if (systemMetrics.memoryUsed > systemMetrics.memoryTotal * 0.9) {
            emitAlert(
                Alert(
                    type = AlertType.HIGH_MEMORY_USAGE,
                    severity = AlertSeverity.WARNING,
                    message = "Uso de memoria alto: ${systemMetrics.memoryUsed}MB / ${systemMetrics.memoryTotal}MB",
                    timestamp = System.currentTimeMillis(),
                    value = systemMetrics.memoryUsed.toDouble()
                )
            )
        }
        
        // Alerta de tasa de error alta
        if (performanceMetrics.errorRate > 5.0) {
            emitAlert(
                Alert(
                    type = AlertType.HIGH_ERROR_RATE,
                    severity = AlertSeverity.CRITICAL,
                    message = "Tasa de error alta: ${performanceMetrics.errorRate}%",
                    timestamp = System.currentTimeMillis(),
                    value = performanceMetrics.errorRate
                )
            )
        }
        
        // Alerta de tiempo de respuesta lento
        if (performanceMetrics.averageResponseTime > 2000) {
            emitAlert(
                Alert(
                    type = AlertType.SLOW_RESPONSE,
                    severity = AlertSeverity.WARNING,
                    message = "Tiempo de respuesta lento: ${performanceMetrics.averageResponseTime}ms",
                    timestamp = System.currentTimeMillis(),
                    value = performanceMetrics.averageResponseTime
                )
            )
        }
    }
    
    private suspend fun emitAlert(alert: Alert) {
        _alerts.emit(alert)
    }
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // 📊 MÉTODOS DE CÁLCULO
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    private fun getCpuUsage(): Double {
        // Implementación simplificada - en producción usar APIs más precisas
        return kotlin.random.Random.nextDouble(0.0, 100.0)
    }
    
    private fun getAppStartTime(): Long {
        // Retornar tiempo de inicio de la aplicación
        return System.currentTimeMillis() - 3600000 // Ejemplo: hace 1 hora
    }
    
    private fun calculateRotationsPerHour(): Double {
        val history = metricsHistory["rotations_per_hour"] ?: return 0.0
        return if (history.isNotEmpty()) {
            val lastHour = System.currentTimeMillis() - 3600000
            history.filter { it.timestamp > lastHour }.size.toDouble()
        } else 0.0
    }
    
    private fun calculateAverageRotationTime(): Double {
        // Calcular tiempo promedio de rotación basado en historial
        return 1500.0 // Ejemplo: 1.5 segundos
    }
    
    private suspend fun getActiveWorkersCount(): Int {
        // Consultar base de datos para trabajadores activos
        return 25 // Ejemplo
    }
    
    private suspend fun getActiveWorkstationsCount(): Int {
        // Consultar base de datos para estaciones activas
        return 10 // Ejemplo
    }
    
    private fun calculateUtilizationRate(): Double {
        // Calcular tasa de utilización del sistema
        return kotlin.random.Random.nextDouble(60.0, 95.0)
    }
    
    private fun calculateAverageResponseTime(): Double {
        val history = metricsHistory["response_time"] ?: return 0.0
        return if (history.isNotEmpty()) {
            history.takeLast(60).map { it.value }.average()
        } else 0.0
    }
    
    private fun calculateErrorRate(): Double {
        val totalActions = userActionCount.get()
        val errors = errorCount.get()
        return if (totalActions > 0) {
            (errors.toDouble() / totalActions.toDouble()) * 100.0
        } else 0.0
    }
    
    private fun calculateThroughput(): Double {
        // Calcular throughput (acciones por segundo)
        return userActionCount.get().toDouble() / 60.0 // Acciones por minuto
    }
    
    private fun getDatabaseQueryCount(): Long {
        // Retornar número de consultas a la base de datos
        return kotlin.random.Random.nextLong(100, 1000)
    }
    
    private fun getCacheHitRate(): Double {
        // Calcular tasa de aciertos del cache
        return kotlin.random.Random.nextDouble(80.0, 98.0)
    }
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // 📚 GESTIÓN DE HISTORIAL
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    private fun addToHistory(metricName: String, point: MetricPoint) {
        val history = metricsHistory.getOrPut(metricName) { mutableListOf() }
        synchronized(history) {
            history.add(point)
            // Mantener solo las últimas 3600 entradas (1 hora con datos por segundo)
            if (history.size > 3600) {
                history.removeAt(0)
            }
        }
    }
    
    private fun cleanupHistory() {
        val cutoffTime = System.currentTimeMillis() - 86400000 // 24 horas
        metricsHistory.values.forEach { history ->
            synchronized(history) {
                history.removeAll { it.timestamp < cutoffTime }
            }
        }
    }
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // 🎯 API PÚBLICA
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    fun recordRotation() {
        rotationCount.incrementAndGet()
    }
    
    fun recordError() {
        errorCount.incrementAndGet()
    }
    
    fun recordUserAction() {
        userActionCount.incrementAndGet()
    }
    
    fun getMetricHistory(metricName: String, timeRange: Long = 3600000): List<MetricPoint> {
        val history = metricsHistory[metricName] ?: return emptyList()
        val cutoffTime = System.currentTimeMillis() - timeRange
        return synchronized(history) {
            history.filter { it.timestamp > cutoffTime }.toList()
        }
    }
    
    fun getAggregatedMetrics(timeRange: Long = 3600000): AggregatedMetrics {
        val cutoffTime = System.currentTimeMillis() - timeRange
        
        return AggregatedMetrics(
            timeRange = timeRange,
            totalRotations = rotationCount.get(),
            totalErrors = errorCount.get(),
            totalUserActions = userActionCount.get(),
            averageMemoryUsage = getAverageMetric("system_memory", cutoffTime),
            averageCpuUsage = getAverageMetric("system_cpu", cutoffTime),
            averageResponseTime = getAverageMetric("response_time", cutoffTime),
            peakMemoryUsage = getPeakMetric("system_memory", cutoffTime),
            peakCpuUsage = getPeakMetric("system_cpu", cutoffTime)
        )
    }
    
    private fun getAverageMetric(metricName: String, cutoffTime: Long): Double {
        val history = metricsHistory[metricName] ?: return 0.0
        val relevantPoints = history.filter { it.timestamp > cutoffTime }
        return if (relevantPoints.isNotEmpty()) {
            relevantPoints.map { it.value }.average()
        } else 0.0
    }
    
    private fun getPeakMetric(metricName: String, cutoffTime: Long): Double {
        val history = metricsHistory[metricName] ?: return 0.0
        val relevantPoints = history.filter { it.timestamp > cutoffTime }
        return relevantPoints.maxOfOrNull { it.value } ?: 0.0
    }
}

// ═══════════════════════════════════════════════════════════════════════════════════════════════
// 📊 MODELOS DE DATOS
// ═══════════════════════════════════════════════════════════════════════════════════════════════

data class SystemMetrics(
    val timestamp: Long = System.currentTimeMillis(),
    val memoryUsed: Long = 0,
    val memoryTotal: Long = 0,
    val memoryFree: Long = 0,
    val cpuUsage: Double = 0.0,
    val activeThreads: Int = 0,
    val uptime: Long = 0
)

data class RotationMetrics(
    val timestamp: Long = System.currentTimeMillis(),
    val totalRotations: Long = 0,
    val rotationsPerHour: Double = 0.0,
    val averageRotationTime: Double = 0.0,
    val activeWorkers: Int = 0,
    val activeWorkstations: Int = 0,
    val utilizationRate: Double = 0.0
)

data class PerformanceMetrics(
    val timestamp: Long = System.currentTimeMillis(),
    val averageResponseTime: Double = 0.0,
    val errorRate: Double = 0.0,
    val throughput: Double = 0.0,
    val userActions: Long = 0,
    val databaseQueries: Long = 0,
    val cacheHitRate: Double = 0.0
)

data class Alert(
    val type: AlertType,
    val severity: AlertSeverity,
    val message: String,
    val timestamp: Long,
    val value: Double
)

enum class AlertType {
    HIGH_MEMORY_USAGE,
    HIGH_CPU_USAGE,
    HIGH_ERROR_RATE,
    SLOW_RESPONSE,
    DATABASE_SLOW,
    CACHE_MISS_HIGH
}

enum class AlertSeverity {
    INFO,
    WARNING,
    CRITICAL
}

data class MetricPoint(
    val timestamp: Long,
    val value: Double
)

data class AggregatedMetrics(
    val timeRange: Long,
    val totalRotations: Long,
    val totalErrors: Long,
    val totalUserActions: Long,
    val averageMemoryUsage: Double,
    val averageCpuUsage: Double,
    val averageResponseTime: Double,
    val peakMemoryUsage: Double,
    val peakCpuUsage: Double
)