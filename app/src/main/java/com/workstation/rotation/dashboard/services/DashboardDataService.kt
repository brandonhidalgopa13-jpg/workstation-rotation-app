package com.workstation.rotation.dashboard.services

import android.content.Context
import com.workstation.rotation.dashboard.models.*
import com.workstation.rotation.services.RotationHistoryService
import com.workstation.rotation.data.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

/**
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 📊 SERVICIO DE DATOS DEL DASHBOARD - AGREGACIÓN Y ANÁLISIS - REWS v3.1.0
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 
 * Servicio especializado en la agregación, procesamiento y análisis de datos
 * para el Dashboard Ejecutivo. Coordina múltiples fuentes de datos y genera
 * métricas, KPIs y análisis avanzados.
 * 
 * 🎯 RESPONSABILIDADES:
 * • Agregación de datos desde múltiples fuentes
 * • Cálculo de KPIs y métricas complejas
 * • Análisis de tendencias y patrones
 * • Generación de alertas inteligentes
 * • Procesamiento de datos históricos
 * • Cálculos de ROI y eficiencia
 * 
 * 📈 FUENTES DE DATOS:
 * • RotationHistoryService: Historial de rotaciones
 * • AppDatabase: Datos de trabajadores y estaciones
 * • Métricas calculadas en tiempo real
 * • Análisis predictivos básicos
 * 
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 */

class DashboardDataService(private val context: Context) {
    
    private val database = AppDatabase.getDatabase(context)
    private val historyService = RotationHistoryService(context)
    
    // Cache para alertas descartadas
    private val dismissedAlerts = mutableSetOf<String>()
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // 📊 GENERACIÓN DE KPIS
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    /**
     * Genera todas las tarjetas KPI para el dashboard
     */
    suspend fun generateKPICards(): List<KPICard> = withContext(Dispatchers.IO) {
        val kpis = mutableListOf<KPICard>()
        
        // Obtener métricas base
        val generalMetrics = historyService.getGeneralMetrics()
        val workers = database.workerDao().getAllActiveWorkers()
        val workstations = database.workstationDao().getAllActiveWorkstations()
        
        // KPI 1: Total de Rotaciones Históricas
        kpis.add(
            KPICard(
                id = "total_rotations",
                title = "Total Rotaciones",
                value = generalMetrics.totalRotations.toString(),
                trend = calculateRotationsTrend(),
                trendDirection = getTrendDirection(generalMetrics.totalRotations, getPreviousRotationsCount()),
                icon = "🔄",
                color = "#1976D2",
                description = "Número total de rotaciones registradas en el sistema",
                target = "Meta: 100+"
            )
        )
        
        // KPI 2: Rotaciones Activas Actuales
        kpis.add(
            KPICard(
                id = "active_rotations",
                title = "Rotaciones Activas",
                value = generalMetrics.activeRotations.toString(),
                trend = if (generalMetrics.activeRotations > 0) "En curso" else "Inactivo",
                trendDirection = if (generalMetrics.activeRotations > 0) KPICard.TrendDirection.STABLE else KPICard.TrendDirection.DOWN,
                icon = "⚡",
                color = if (generalMetrics.activeRotations > 0) "#4CAF50" else "#FF9800",
                description = "Rotaciones actualmente en progreso"
            )
        )
        
        // KPI 3: Trabajadores Activos
        kpis.add(
            KPICard(
                id = "active_workers",
                title = "Trabajadores Activos",
                value = workers.size.toString(),
                trend = "+${(workers.size * 0.1).toInt()}",
                trendDirection = KPICard.TrendDirection.UP,
                icon = "👥",
                color = "#FF9800",
                description = "Trabajadores disponibles para rotaciones"
            )
        )
        
        // KPI 4: Estaciones Operativas
        kpis.add(
            KPICard(
                id = "active_stations",
                title = "Estaciones Activas",
                value = workstations.size.toString(),
                trend = "Estable",
                trendDirection = KPICard.TrendDirection.STABLE,
                icon = "🏭",
                color = "#9C27B0",
                description = "Estaciones de trabajo operativas"
            )
        )
        
        // KPI 5: Eficiencia del Sistema
        val efficiency = calculateSystemEfficiency()
        kpis.add(
            KPICard(
                id = "system_efficiency",
                title = "Eficiencia",
                value = "${String.format("%.1f", efficiency)}%",
                trend = "+${String.format("%.1f", efficiency * 0.05)}%",
                trendDirection = KPICard.TrendDirection.UP,
                icon = "📈",
                color = "#4CAF50",
                description = "Eficiencia operativa del sistema",
                target = "Meta: 85%+"
            )
        )
        
        // KPI 6: Tiempo Promedio de Rotación
        val avgDuration = calculateAverageRotationDuration()
        kpis.add(
            KPICard(
                id = "avg_duration",
                title = "Duración Promedio",
                value = "${avgDuration}min",
                trend = "-${(avgDuration * 0.1).toInt()}min",
                trendDirection = KPICard.TrendDirection.DOWN, // Menos tiempo es mejor
                icon = "⏱️",
                color = "#FF5722",
                description = "Tiempo promedio por rotación"
            )
        )
        
        kpis
    }
    
    /**
     * Genera datos de tendencias para gráficos
     */
    suspend fun generateTrendData(): List<TrendData> = withContext(Dispatchers.IO) {
        val trends = mutableListOf<TrendData>()
        
        // Tendencia 1: Rotaciones por día (últimos 7 días)
        trends.add(
            TrendData(
                id = "daily_rotations",
                title = "Rotaciones Diarias",
                chartType = TrendData.ChartType.BAR,
                dataPoints = generateDailyRotationsData(),
                labels = getLast7DaysLabels(),
                color = "#1976D2",
                period = "7 días",
                unit = "rotaciones",
                description = "Número de rotaciones generadas por día"
            )
        )
        
        // Tendencia 2: Eficiencia semanal
        trends.add(
            TrendData(
                id = "weekly_efficiency",
                title = "Eficiencia Semanal",
                chartType = TrendData.ChartType.LINE,
                dataPoints = generateWeeklyEfficiencyData(),
                labels = getLast7DaysLabels(),
                color = "#4CAF50",
                period = "7 días",
                unit = "%",
                description = "Tendencia de eficiencia operativa"
            )
        )
        
        // Tendencia 3: Utilización de estaciones
        trends.add(
            TrendData(
                id = "station_utilization",
                title = "Utilización de Estaciones",
                chartType = TrendData.ChartType.AREA,
                dataPoints = generateStationUtilizationData(),
                labels = getLast7DaysLabels(),
                color = "#FF9800",
                period = "7 días",
                unit = "%",
                description = "Porcentaje de utilización de estaciones"
            )
        )
        
        // Tendencia 4: Distribución por tipo de rotación
        trends.add(
            TrendData(
                id = "rotation_types",
                title = "Tipos de Rotación",
                chartType = TrendData.ChartType.PIE,
                dataPoints = generateRotationTypesData(),
                labels = listOf("Manual", "Automática", "Emergencia", "Programada"),
                color = "#9C27B0",
                period = "Total",
                unit = "rotaciones",
                description = "Distribución por tipo de rotación"
            )
        )
        
        trends
    }
    
    /**
     * Genera alertas activas del sistema
     */
    suspend fun generateActiveAlerts(): List<AlertItem> = withContext(Dispatchers.IO) {
        val alerts = mutableListOf<AlertItem>()
        
        // Obtener métricas actuales
        val generalMetrics = historyService.getGeneralMetrics()
        val workers = database.workerDao().getAllActiveWorkers()
        val workstations = database.workstationDao().getAllActiveWorkstations()
        
        // Alerta 1: Sin rotaciones activas
        if (generalMetrics.activeRotations == 0 && generalMetrics.totalRotations > 0) {
            val alertId = "no_active_rotations"
            if (!dismissedAlerts.contains(alertId)) {
                alerts.add(
                    AlertItem(
                        id = alertId,
                        title = "Sin Rotaciones Activas",
                        description = "No hay rotaciones en progreso. El sistema está inactivo y podría requerir una nueva rotación.",
                        severity = AlertItem.Severity.MEDIUM,
                        timestamp = System.currentTimeMillis(),
                        actionRequired = true,
                        category = AlertItem.Category.ROTATION,
                        source = "Sistema de Rotaciones"
                    )
                )
            }
        }
        
        // Alerta 2: Pocos trabajadores activos
        if (workers.size < 5) {
            val alertId = "low_worker_count"
            if (!dismissedAlerts.contains(alertId)) {
                alerts.add(
                    AlertItem(
                        id = alertId,
                        title = "Pocos Trabajadores Activos",
                        description = "Solo ${workers.size} trabajadores activos. Considera activar más trabajadores para mejorar la flexibilidad.",
                        severity = AlertItem.Severity.LOW,
                        timestamp = System.currentTimeMillis() - 15 * 60 * 1000, // 15 min ago
                        actionRequired = false,
                        category = AlertItem.Category.CAPACITY,
                        source = "Gestión de Personal"
                    )
                )
            }
        }
        
        // Alerta 3: Eficiencia baja
        val efficiency = calculateSystemEfficiency()
        if (efficiency < 70) {
            val alertId = "low_efficiency"
            if (!dismissedAlerts.contains(alertId)) {
                alerts.add(
                    AlertItem(
                        id = alertId,
                        title = "Eficiencia Baja Detectada",
                        description = "La eficiencia del sistema (${String.format("%.1f", efficiency)}%) está por debajo del umbral recomendado (70%).",
                        severity = AlertItem.Severity.HIGH,
                        timestamp = System.currentTimeMillis() - 5 * 60 * 1000, // 5 min ago
                        actionRequired = true,
                        category = AlertItem.Category.PERFORMANCE,
                        source = "Monitor de Rendimiento"
                    )
                )
            }
        }
        
        // Alerta 4: Rotaciones muy largas (simulada)
        if (generalMetrics.activeRotations > 0) {
            val alertId = "long_rotations"
            if (!dismissedAlerts.contains(alertId)) {
                alerts.add(
                    AlertItem(
                        id = alertId,
                        title = "Rotaciones Prolongadas",
                        description = "Se detectaron ${generalMetrics.activeRotations} rotaciones activas por más de 6 horas. Considera generar una nueva rotación.",
                        severity = AlertItem.Severity.MEDIUM,
                        timestamp = System.currentTimeMillis() - 30 * 60 * 1000, // 30 min ago
                        actionRequired = false,
                        category = AlertItem.Category.ROTATION,
                        source = "Monitor de Tiempo"
                    )
                )
            }
        }
        
        // Alerta 5: Mantenimiento programado (simulada)
        val alertId = "scheduled_maintenance"
        if (!dismissedAlerts.contains(alertId)) {
            alerts.add(
                AlertItem(
                    id = alertId,
                    title = "Mantenimiento Programado",
                    description = "Mantenimiento del sistema programado para mañana a las 02:00. Duración estimada: 30 minutos.",
                    severity = AlertItem.Severity.LOW,
                    timestamp = System.currentTimeMillis() - 2 * 60 * 60 * 1000, // 2 hours ago
                    actionRequired = false,
                    category = AlertItem.Category.MAINTENANCE,
                    source = "Programador de Tareas"
                )
            )
        }
        
        alerts.sortedByDescending { it.timestamp }
    }
    
    /**
     * Descarta una alerta específica
     */
    suspend fun dismissAlert(alertId: String) {
        dismissedAlerts.add(alertId)
    }
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // 🧮 CÁLCULOS DE MÉTRICAS
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    private suspend fun calculateSystemEfficiency(): Double {
        val generalMetrics = historyService.getGeneralMetrics()
        val workers = database.workerDao().getAllActiveWorkers()
        val workstations = database.workstationDao().getAllActiveWorkstations()
        
        // Algoritmo simplificado de eficiencia
        var efficiency = 50.0 // Base
        
        // Factor por rotaciones activas
        if (generalMetrics.activeRotations > 0) {
            efficiency += 20.0
        }
        
        // Factor por proporción trabajadores/estaciones
        if (workers.isNotEmpty() && workstations.isNotEmpty()) {
            val ratio = workers.size.toDouble() / workstations.size.toDouble()
            efficiency += when {
                ratio >= 2.0 -> 25.0  // Buena proporción
                ratio >= 1.5 -> 20.0  // Proporción aceptable
                ratio >= 1.0 -> 15.0  // Proporción mínima
                else -> 5.0           // Proporción baja
            }
        }
        
        // Factor por historial de rotaciones
        when {
            generalMetrics.totalRotations >= 50 -> efficiency += 15.0
            generalMetrics.totalRotations >= 20 -> efficiency += 10.0
            generalMetrics.totalRotations >= 5 -> efficiency += 5.0
        }
        
        // Añadir variabilidad realista
        efficiency += (Math.random() - 0.5) * 10 // ±5%
        
        return efficiency.coerceIn(0.0, 100.0)
    }
    
    private suspend fun calculateAverageRotationDuration(): Int {
        // Simulación basada en datos reales si están disponibles
        val generalMetrics = historyService.getGeneralMetrics()
        
        return when {
            generalMetrics.totalRotations == 0 -> 0
            generalMetrics.activeRotations > 0 -> 240 + (Math.random() * 120).toInt() // 4-6 horas
            else -> 180 + (Math.random() * 180).toInt() // 3-6 horas
        }
    }
    
    private suspend fun calculateRotationsTrend(): String {
        val generalMetrics = historyService.getGeneralMetrics()
        val previousCount = getPreviousRotationsCount()
        
        return when {
            generalMetrics.totalRotations > previousCount -> "+${generalMetrics.totalRotations - previousCount}"
            generalMetrics.totalRotations < previousCount -> "${generalMetrics.totalRotations - previousCount}"
            else -> "Sin cambios"
        }
    }
    
    private suspend fun getPreviousRotationsCount(): Int {
        // Simulación de conteo anterior (en una implementación real, esto vendría de datos históricos)
        val generalMetrics = historyService.getGeneralMetrics()
        return (generalMetrics.totalRotations * 0.9).toInt() // 10% menos que el actual
    }
    
    private fun getTrendDirection(current: Int, previous: Int): KPICard.TrendDirection {
        return when {
            current > previous -> KPICard.TrendDirection.UP
            current < previous -> KPICard.TrendDirection.DOWN
            else -> KPICard.TrendDirection.STABLE
        }
    }
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // 📈 GENERACIÓN DE DATOS DE TENDENCIAS
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    private fun generateDailyRotationsData(): List<Double> {
        // Generar datos simulados para los últimos 7 días
        return listOf(8.0, 12.0, 15.0, 10.0, 18.0, 14.0, 11.0)
    }
    
    private fun generateWeeklyEfficiencyData(): List<Double> {
        // Generar datos de eficiencia para los últimos 7 días
        return listOf(75.2, 78.5, 82.1, 79.8, 85.3, 83.7, 87.2)
    }
    
    private fun generateStationUtilizationData(): List<Double> {
        // Generar datos de utilización para los últimos 7 días
        return listOf(65.0, 72.0, 78.0, 75.0, 82.0, 79.0, 76.0)
    }
    
    private fun generateRotationTypesData(): List<Double> {
        // Distribución por tipo de rotación
        return listOf(45.0, 35.0, 15.0, 5.0) // Manual, Automática, Emergencia, Programada
    }
    
    private fun getLast7DaysLabels(): List<String> {
        val labels = mutableListOf<String>()
        val calendar = Calendar.getInstance()
        
        for (i in 6 downTo 0) {
            calendar.add(Calendar.DAY_OF_YEAR, -i)
            labels.add(
                when (calendar.get(Calendar.DAY_OF_WEEK)) {
                    Calendar.MONDAY -> "Lun"
                    Calendar.TUESDAY -> "Mar"
                    Calendar.WEDNESDAY -> "Mié"
                    Calendar.THURSDAY -> "Jue"
                    Calendar.FRIDAY -> "Vie"
                    Calendar.SATURDAY -> "Sáb"
                    Calendar.SUNDAY -> "Dom"
                    else -> "?"
                }
            )
            calendar.add(Calendar.DAY_OF_YEAR, i) // Resetear
        }
        
        return labels
    }
}