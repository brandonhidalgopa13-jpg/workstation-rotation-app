package com.workstation.rotation.monitoring.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.workstation.rotation.monitoring.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 🎯 VIEWMODEL DASHBOARD TIEMPO REAL - WorkStation Rotation v4.0.2+
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 
 * ViewModel que gestiona el estado del dashboard en tiempo real.
 * Coordina la recolección de métricas y la presentación de datos.
 * 
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 */

class RealTimeDashboardViewModel(application: Application) : AndroidViewModel(application) {
    
    private val monitor = RealTimeMonitor.getInstance(application)
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // 📊 ESTADOS OBSERVABLES
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    val systemMetrics: StateFlow<SystemMetrics> = monitor.systemMetrics
    val rotationMetrics: StateFlow<RotationMetrics> = monitor.rotationMetrics
    val performanceMetrics: StateFlow<PerformanceMetrics> = monitor.performanceMetrics
    
    private val _alerts = MutableStateFlow<List<Alert>>(emptyList())
    val alerts: StateFlow<List<Alert>> = _alerts.asStateFlow()
    
    private val _isConnected = MutableStateFlow(true)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()
    
    private val _metricsCards = MutableStateFlow<List<MetricCard>>(emptyList())
    val metricsCards: StateFlow<List<MetricCard>> = _metricsCards.asStateFlow()
    
    private val _isMonitoring = MutableStateFlow(false)
    val isMonitoring: StateFlow<Boolean> = _isMonitoring.asStateFlow()
    
    // Lista de alertas activas
    private val activeAlerts = mutableListOf<Alert>()
    
    init {
        setupAlertCollection()
        generateMetricsCards()
    }
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // 🚀 CONTROL DE MONITOREO
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    fun startMonitoring() {
        if (!_isMonitoring.value) {
            monitor.startMonitoring()
            _isMonitoring.value = true
            _isConnected.value = true
        }
    }
    
    fun stopMonitoring() {
        monitor.stopMonitoring()
        _isMonitoring.value = false
    }
    
    fun pauseMonitoring() {
        _isMonitoring.value = false
        // No detener completamente el monitor, solo pausar la UI
    }
    
    fun forceRefresh() {
        viewModelScope.launch {
            // Forzar actualización inmediata de métricas
            generateMetricsCards()
        }
    }
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // 🚨 GESTIÓN DE ALERTAS
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    private fun setupAlertCollection() {
        viewModelScope.launch {
            monitor.alerts.collect { alert ->
                // Agregar nueva alerta a la lista
                activeAlerts.add(alert)
                
                // Mantener solo las últimas 50 alertas
                if (activeAlerts.size > 50) {
                    activeAlerts.removeAt(0)
                }
                
                // Actualizar estado
                _alerts.value = activeAlerts.toList()
            }
        }
    }
    
    fun dismissAlert(alert: Alert) {
        activeAlerts.remove(alert)
        _alerts.value = activeAlerts.toList()
    }
    
    fun clearAllAlerts() {
        activeAlerts.clear()
        _alerts.value = emptyList()
    }
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // 📊 GENERACIÓN DE CARDS DE MÉTRICAS
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    private fun generateMetricsCards() {
        viewModelScope.launch {
            val cards = mutableListOf<MetricCard>()
            
            // Card de Sistema
            cards.add(
                MetricCard(
                    id = "system",
                    title = "Sistema",
                    icon = "system",
                    metrics = listOf(
                        MetricItem("Memoria", "${systemMetrics.value.memoryUsed} MB", getMemoryStatus()),
                        MetricItem("CPU", "${String.format("%.1f", systemMetrics.value.cpuUsage)}%", getCpuStatus()),
                        MetricItem("Threads", systemMetrics.value.activeThreads.toString(), MetricStatus.NORMAL)
                    )
                )
            )
            
            // Card de Rotaciones
            cards.add(
                MetricCard(
                    id = "rotations",
                    title = "Rotaciones",
                    icon = "rotation",
                    metrics = listOf(
                        MetricItem("Total", rotationMetrics.value.totalRotations.toString(), MetricStatus.NORMAL),
                        MetricItem("Por Hora", String.format("%.1f", rotationMetrics.value.rotationsPerHour), MetricStatus.NORMAL),
                        MetricItem("Utilización", "${String.format("%.1f", rotationMetrics.value.utilizationRate)}%", getUtilizationStatus())
                    )
                )
            )
            
            // Card de Rendimiento
            cards.add(
                MetricCard(
                    id = "performance",
                    title = "Rendimiento",
                    icon = "performance",
                    metrics = listOf(
                        MetricItem("Respuesta", "${String.format("%.0f", performanceMetrics.value.averageResponseTime)}ms", getResponseTimeStatus()),
                        MetricItem("Errores", "${String.format("%.2f", performanceMetrics.value.errorRate)}%", getErrorRateStatus()),
                        MetricItem("Throughput", String.format("%.1f", performanceMetrics.value.throughput), MetricStatus.NORMAL)
                    )
                )
            )
            
            // Card de Base de Datos
            cards.add(
                MetricCard(
                    id = "database",
                    title = "Base de Datos",
                    icon = "database",
                    metrics = listOf(
                        MetricItem("Consultas", performanceMetrics.value.databaseQueries.toString(), MetricStatus.NORMAL),
                        MetricItem("Cache Hit", "${String.format("%.1f", performanceMetrics.value.cacheHitRate)}%", getCacheHitStatus()),
                        MetricItem("Acciones", performanceMetrics.value.userActions.toString(), MetricStatus.NORMAL)
                    )
                )
            )
            
            _metricsCards.value = cards
        }
    }
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // 📈 CÁLCULO DE ESTADOS
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    private fun getMemoryStatus(): MetricStatus {
        val usage = systemMetrics.value.memoryUsed.toFloat() / systemMetrics.value.memoryTotal.toFloat()
        return when {
            usage > 0.9f -> MetricStatus.CRITICAL
            usage > 0.75f -> MetricStatus.WARNING
            else -> MetricStatus.NORMAL
        }
    }
    
    private fun getCpuStatus(): MetricStatus {
        return when {
            systemMetrics.value.cpuUsage > 80 -> MetricStatus.CRITICAL
            systemMetrics.value.cpuUsage > 60 -> MetricStatus.WARNING
            else -> MetricStatus.NORMAL
        }
    }
    
    private fun getUtilizationStatus(): MetricStatus {
        return when {
            rotationMetrics.value.utilizationRate > 95 -> MetricStatus.CRITICAL
            rotationMetrics.value.utilizationRate > 85 -> MetricStatus.WARNING
            rotationMetrics.value.utilizationRate < 60 -> MetricStatus.WARNING
            else -> MetricStatus.NORMAL
        }
    }
    
    private fun getResponseTimeStatus(): MetricStatus {
        return when {
            performanceMetrics.value.averageResponseTime > 2000 -> MetricStatus.CRITICAL
            performanceMetrics.value.averageResponseTime > 1000 -> MetricStatus.WARNING
            else -> MetricStatus.NORMAL
        }
    }
    
    private fun getErrorRateStatus(): MetricStatus {
        return when {
            performanceMetrics.value.errorRate > 5.0 -> MetricStatus.CRITICAL
            performanceMetrics.value.errorRate > 2.0 -> MetricStatus.WARNING
            else -> MetricStatus.NORMAL
        }
    }
    
    private fun getCacheHitStatus(): MetricStatus {
        return when {
            performanceMetrics.value.cacheHitRate < 70 -> MetricStatus.CRITICAL
            performanceMetrics.value.cacheHitRate < 85 -> MetricStatus.WARNING
            else -> MetricStatus.NORMAL
        }
    }
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // 📤 EXPORTACIÓN DE DATOS
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    fun exportMetrics() {
        viewModelScope.launch {
            try {
                val aggregatedMetrics = monitor.getAggregatedMetrics()
                val exportData = generateExportData(aggregatedMetrics)
                
                // TODO: Implementar exportación a archivo CSV o JSON
                // Por ahora, solo log
                println("Exportando métricas: $exportData")
                
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    private fun generateExportData(metrics: AggregatedMetrics): String {
        return """
            Métricas Agregadas - ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(java.util.Date())}
            
            Rango de Tiempo: ${metrics.timeRange / 1000 / 60} minutos
            
            Rotaciones:
            - Total: ${metrics.totalRotations}
            
            Errores:
            - Total: ${metrics.totalErrors}
            
            Acciones de Usuario:
            - Total: ${metrics.totalUserActions}
            
            Memoria:
            - Promedio: ${String.format("%.1f", metrics.averageMemoryUsage)} MB
            - Pico: ${String.format("%.1f", metrics.peakMemoryUsage)} MB
            
            CPU:
            - Promedio: ${String.format("%.1f", metrics.averageCpuUsage)}%
            - Pico: ${String.format("%.1f", metrics.peakCpuUsage)}%
            
            Tiempo de Respuesta:
            - Promedio: ${String.format("%.0f", metrics.averageResponseTime)}ms
        """.trimIndent()
    }
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // 📊 HISTORIAL DE MÉTRICAS
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    fun getMetricHistory(metricName: String, timeRange: Long = 3600000): List<MetricPoint> {
        return monitor.getMetricHistory(metricName, timeRange)
    }
    
    fun getAggregatedMetrics(timeRange: Long = 3600000): AggregatedMetrics {
        return monitor.getAggregatedMetrics(timeRange)
    }
}

// ═══════════════════════════════════════════════════════════════════════════════════════════════
// 📊 MODELOS PARA UI
// ═══════════════════════════════════════════════════════════════════════════════════════════════

data class MetricCard(
    val id: String,
    val title: String,
    val icon: String,
    val metrics: List<MetricItem>
)

data class MetricItem(
    val label: String,
    val value: String,
    val status: MetricStatus
)

enum class MetricStatus {
    NORMAL,
    WARNING,
    CRITICAL
}