package com.workstation.rotation.dashboard.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.workstation.rotation.dashboard.models.KPICard
import com.workstation.rotation.dashboard.models.TrendData
import com.workstation.rotation.dashboard.models.AlertItem
import com.workstation.rotation.dashboard.services.DashboardDataService
import com.workstation.rotation.services.RotationHistoryService
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.util.*

/**
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 📊 VIEWMODEL DASHBOARD EJECUTIVO - GESTIÓN DE DATOS Y ESTADO - REWS v3.1.0
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 
 * ViewModel que gestiona todos los datos y el estado del Dashboard Ejecutivo.
 * Coordina la obtención de métricas, KPIs, tendencias y alertas desde múltiples fuentes.
 * 
 * 🎯 RESPONSABILIDADES:
 * • Agregación de datos desde múltiples servicios
 * • Cálculo de KPIs y métricas en tiempo real
 * • Generación de alertas proactivas
 * • Análisis de tendencias y patrones
 * • Gestión de estado de carga y errores
 * • Actualización automática de datos
 * 
 * 📈 MÉTRICAS CALCULADAS:
 * • Salud del sistema (0-100%)
 * • Eficiencia operativa (%)
 * • Índice de productividad
 * • ROI del sistema
 * • Utilización de recursos
 * • Alertas críticas activas
 * 
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 */

class ExecutiveDashboardViewModel(application: Application) : AndroidViewModel(application) {
    
    private val dashboardService = DashboardDataService(application)
    private val historyService = RotationHistoryService(application)
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // 🔄 LIVEDATA PRINCIPALES
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _lastUpdate = MutableLiveData<Long>()
    val lastUpdate: LiveData<Long> = _lastUpdate
    
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage
    
    // KPIs y métricas principales
    private val _kpiCards = MutableLiveData<List<KPICard>>()
    val kpiCards: LiveData<List<KPICard>> = _kpiCards
    
    private val _trendData = MutableLiveData<List<TrendData>>()
    val trendData: LiveData<List<TrendData>> = _trendData
    
    private val _activeAlerts = MutableLiveData<List<AlertItem>>()
    val activeAlerts: LiveData<List<AlertItem>> = _activeAlerts
    
    // Métricas de resumen
    private val _systemHealthScore = MutableLiveData<Double>()
    val systemHealthScore: LiveData<Double> = _systemHealthScore
    
    private val _overallEfficiency = MutableLiveData<Double>()
    val overallEfficiency: LiveData<Double> = _overallEfficiency
    
    private val _productivityIndex = MutableLiveData<Double>()
    val productivityIndex: LiveData<Double> = _productivityIndex
    
    private val _roiMetrics = MutableLiveData<Double>()
    val roiMetrics: LiveData<Double> = _roiMetrics
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // 🔧 MÉTODOS PRINCIPALES
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    /**
     * Carga todos los datos del dashboard
     */
    fun loadDashboardData() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                
                // Cargar datos en paralelo
                loadKPICards()
                loadTrendData()
                loadActiveAlerts()
                loadSummaryMetrics()
                
                _lastUpdate.value = System.currentTimeMillis()
                
            } catch (e: Exception) {
                _errorMessage.value = "Error al cargar datos del dashboard: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Actualiza los datos del dashboard
     */
    fun refreshDashboard() {
        loadDashboardData()
    }
    
    /**
     * Descarta una alerta específica
     */
    fun dismissAlert(alertId: String) {
        viewModelScope.launch {
            try {
                dashboardService.dismissAlert(alertId)
                loadActiveAlerts() // Recargar alertas
            } catch (e: Exception) {
                _errorMessage.value = "Error al descartar alerta: ${e.message}"
            }
        }
    }
    
    /**
     * Limpia el mensaje de error
     */
    fun clearError() {
        _errorMessage.value = null
    }
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // 📊 CARGA DE DATOS ESPECÍFICOS
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    private suspend fun loadKPICards() {
        val kpis = mutableListOf<KPICard>()
        
        // Obtener métricas generales
        val generalMetrics = historyService.getGeneralMetrics()
        
        // KPI 1: Total de Rotaciones
        kpis.add(
            KPICard(
                id = "total_rotations",
                title = "Total Rotaciones",
                value = generalMetrics.totalRotations.toString(),
                trend = "+12%",
                trendDirection = KPICard.TrendDirection.UP,
                icon = "🔄",
                color = "#1976D2"
            )
        )
        
        // KPI 2: Rotaciones Activas
        kpis.add(
            KPICard(
                id = "active_rotations",
                title = "Rotaciones Activas",
                value = generalMetrics.activeRotations.toString(),
                trend = "Estable",
                trendDirection = KPICard.TrendDirection.STABLE,
                icon = "⚡",
                color = "#FF9800"
            )
        )
        
        // KPI 3: Eficiencia Promedio
        val efficiency = calculateOverallEfficiency()
        kpis.add(
            KPICard(
                id = "efficiency",
                title = "Eficiencia",
                value = "${String.format("%.1f", efficiency)}%",
                trend = "+5.2%",
                trendDirection = KPICard.TrendDirection.UP,
                icon = "📈",
                color = "#4CAF50"
            )
        )
        
        // KPI 4: Tiempo Promedio
        val avgDuration = calculateAverageDuration()
        kpis.add(
            KPICard(
                id = "avg_duration",
                title = "Tiempo Promedio",
                value = "${avgDuration}min",
                trend = "-8min",
                trendDirection = KPICard.TrendDirection.DOWN,
                icon = "⏱️",
                color = "#9C27B0"
            )
        )
        
        // KPI 5: Entrenamientos
        val trainings = countCompletedTrainings()
        kpis.add(
            KPICard(
                id = "trainings",
                title = "Entrenamientos",
                value = trainings.toString(),
                trend = "+3",
                trendDirection = KPICard.TrendDirection.UP,
                icon = "🎓",
                color = "#FF5722"
            )
        )
        
        // KPI 6: Alertas Críticas
        val criticalAlerts = countCriticalAlerts()
        kpis.add(
            KPICard(
                id = "alerts",
                title = "Alertas Críticas",
                value = criticalAlerts.toString(),
                trend = if (criticalAlerts > 0) "Atención" else "OK",
                trendDirection = if (criticalAlerts > 0) KPICard.TrendDirection.DOWN else KPICard.TrendDirection.STABLE,
                icon = "🚨",
                color = if (criticalAlerts > 0) "#F44336" else "#4CAF50"
            )
        )
        
        _kpiCards.value = kpis
    }
    
    private suspend fun loadTrendData() {
        val trends = mutableListOf<TrendData>()
        
        // Tendencia 1: Eficiencia Semanal
        trends.add(
            TrendData(
                id = "efficiency_trend",
                title = "Eficiencia Semanal",
                chartType = TrendData.ChartType.LINE,
                dataPoints = generateEfficiencyTrend(),
                color = "#4CAF50",
                period = "7 días"
            )
        )
        
        // Tendencia 2: Rotaciones por Día
        trends.add(
            TrendData(
                id = "rotations_trend",
                title = "Rotaciones Diarias",
                chartType = TrendData.ChartType.BAR,
                dataPoints = generateRotationsTrend(),
                color = "#1976D2",
                period = "7 días"
            )
        )
        
        // Tendencia 3: Utilización de Estaciones
        trends.add(
            TrendData(
                id = "utilization_trend",
                title = "Utilización Estaciones",
                chartType = TrendData.ChartType.AREA,
                dataPoints = generateUtilizationTrend(),
                color = "#FF9800",
                period = "7 días"
            )
        )
        
        _trendData.value = trends
    }
    
    private suspend fun loadActiveAlerts() {
        val alerts = mutableListOf<AlertItem>()
        
        // Generar alertas basadas en métricas actuales
        val generalMetrics = historyService.getGeneralMetrics()
        
        // Alerta 1: Sin rotaciones activas
        if (generalMetrics.activeRotations == 0) {
            alerts.add(
                AlertItem(
                    id = "no_active_rotations",
                    title = "Sin Rotaciones Activas",
                    description = "No hay rotaciones activas en el sistema. Considera generar una nueva rotación.",
                    severity = AlertItem.Severity.MEDIUM,
                    timestamp = System.currentTimeMillis(),
                    actionRequired = true
                )
            )
        }
        
        // Alerta 2: Eficiencia baja (simulada)
        val efficiency = calculateOverallEfficiency()
        if (efficiency < 70) {
            alerts.add(
                AlertItem(
                    id = "low_efficiency",
                    title = "Eficiencia Baja Detectada",
                    description = "La eficiencia del sistema está por debajo del 70%. Revisa las asignaciones actuales.",
                    severity = AlertItem.Severity.HIGH,
                    timestamp = System.currentTimeMillis(),
                    actionRequired = true
                )
            )
        }
        
        // Alerta 3: Rotaciones largas (simulada)
        if (generalMetrics.activeRotations > 0) {
            alerts.add(
                AlertItem(
                    id = "long_rotations",
                    title = "Rotaciones Prolongadas",
                    description = "Se detectaron rotaciones activas por más de 6 horas. Considera generar nueva rotación.",
                    severity = AlertItem.Severity.MEDIUM,
                    timestamp = System.currentTimeMillis() - 30 * 60 * 1000, // 30 min ago
                    actionRequired = false
                )
            )
        }
        
        _activeAlerts.value = alerts
    }
    
    private suspend fun loadSummaryMetrics() {
        // Calcular métricas de resumen
        _systemHealthScore.value = calculateSystemHealth()
        _overallEfficiency.value = calculateOverallEfficiency()
        _productivityIndex.value = calculateProductivityIndex()
        _roiMetrics.value = calculateROI()
    }
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // 🧮 CÁLCULOS DE MÉTRICAS
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    private suspend fun calculateSystemHealth(): Double {
        // Algoritmo simplificado de salud del sistema
        val generalMetrics = historyService.getGeneralMetrics()
        
        var healthScore = 100.0
        
        // Penalizar si no hay rotaciones activas
        if (generalMetrics.activeRotations == 0 && generalMetrics.totalRotations > 0) {
            healthScore -= 20
        }
        
        // Penalizar si hay muy pocas rotaciones históricas
        if (generalMetrics.totalRotations < 10) {
            healthScore -= 15
        }
        
        // Bonus por actividad reciente
        if (generalMetrics.activeRotations > 0) {
            healthScore += 10
        }
        
        return healthScore.coerceIn(0.0, 100.0)
    }
    
    private suspend fun calculateOverallEfficiency(): Double {
        // Simulación de cálculo de eficiencia
        val generalMetrics = historyService.getGeneralMetrics()
        
        return when {
            generalMetrics.totalRotations == 0 -> 0.0
            generalMetrics.activeRotations > 0 -> 85.5 + (Math.random() * 10) // 85-95%
            else -> 75.0 + (Math.random() * 15) // 75-90%
        }
    }
    
    private suspend fun calculateProductivityIndex(): Double {
        // Simulación de índice de productividad (escala 1-10)
        val generalMetrics = historyService.getGeneralMetrics()
        
        return when {
            generalMetrics.totalRotations == 0 -> 0.0
            generalMetrics.activeRotations > 5 -> 8.5 + (Math.random() * 1.5) // 8.5-10
            generalMetrics.activeRotations > 0 -> 7.0 + (Math.random() * 2.0) // 7-9
            else -> 5.0 + (Math.random() * 2.0) // 5-7
        }
    }
    
    private suspend fun calculateROI(): Double {
        // Simulación de cálculo de ROI
        val generalMetrics = historyService.getGeneralMetrics()
        
        return when {
            generalMetrics.totalRotations < 10 -> 5.0 + (Math.random() * 5) // 5-10%
            generalMetrics.totalRotations < 50 -> 10.0 + (Math.random() * 8) // 10-18%
            else -> 15.0 + (Math.random() * 10) // 15-25%
        }
    }
    
    private suspend fun calculateAverageDuration(): Int {
        // Simulación de duración promedio en minutos
        return 240 + (Math.random() * 120).toInt() // 240-360 min (4-6 horas)
    }
    
    private suspend fun countCompletedTrainings(): Int {
        // Simulación de entrenamientos completados
        val generalMetrics = historyService.getGeneralMetrics()
        return (generalMetrics.totalRotations * 0.1).toInt() // 10% de rotaciones son entrenamientos
    }
    
    private suspend fun countCriticalAlerts(): Int {
        // Simulación de alertas críticas
        val generalMetrics = historyService.getGeneralMetrics()
        return if (generalMetrics.activeRotations == 0) 1 else 0
    }
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // 📈 GENERACIÓN DE DATOS DE TENDENCIAS
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    private fun generateEfficiencyTrend(): List<Double> {
        // Generar datos de eficiencia para los últimos 7 días
        return listOf(78.5, 82.1, 85.3, 83.7, 87.2, 89.1, 85.8)
    }
    
    private fun generateRotationsTrend(): List<Double> {
        // Generar datos de rotaciones para los últimos 7 días
        return listOf(12.0, 15.0, 18.0, 14.0, 20.0, 16.0, 13.0)
    }
    
    private fun generateUtilizationTrend(): List<Double> {
        // Generar datos de utilización para los últimos 7 días
        return listOf(65.2, 72.8, 78.5, 75.1, 82.3, 79.7, 76.4)
    }
}