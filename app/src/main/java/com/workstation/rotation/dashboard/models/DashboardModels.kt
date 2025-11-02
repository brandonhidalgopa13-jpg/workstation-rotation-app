package com.workstation.rotation.dashboard.models

/**
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 📊 MODELOS DE DATOS PARA DASHBOARD EJECUTIVO - REWS v3.1.0
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 
 * Modelos de datos que representan los diferentes elementos del Dashboard Ejecutivo:
 * KPIs, tendencias, alertas, métricas y configuraciones de visualización.
 * 
 * 🎯 MODELOS INCLUIDOS:
 * • KPICard: Tarjetas de indicadores clave de rendimiento
 * • TrendData: Datos de tendencias y gráficos
 * • AlertItem: Alertas y notificaciones del sistema
 * • MetricSummary: Resúmenes de métricas agregadas
 * • ChartConfiguration: Configuración de gráficos
 * 
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 */

/**
 * Tarjeta de KPI (Key Performance Indicator)
 */
data class KPICard(
    val id: String,
    val title: String,
    val value: String,
    val trend: String,
    val trendDirection: TrendDirection,
    val icon: String,
    val color: String,
    val description: String? = null,
    val target: String? = null,
    val lastUpdated: Long = System.currentTimeMillis()
) {
    enum class TrendDirection {
        UP,     // Tendencia positiva ↗️
        DOWN,   // Tendencia negativa ↘️
        STABLE  // Tendencia estable ➡️
    }
    
    /**
     * Obtiene el emoji de tendencia según la dirección
     */
    fun getTrendEmoji(): String {
        return when (trendDirection) {
            TrendDirection.UP -> "↗️"
            TrendDirection.DOWN -> "↘️"
            TrendDirection.STABLE -> "➡️"
        }
    }
    
    /**
     * Obtiene el color de tendencia según la dirección
     */
    fun getTrendColor(): String {
        return when (trendDirection) {
            TrendDirection.UP -> "#4CAF50"      // Verde
            TrendDirection.DOWN -> "#F44336"    // Rojo
            TrendDirection.STABLE -> "#FF9800"  // Naranja
        }
    }
}

/**
 * Datos de tendencias para gráficos
 */
data class TrendData(
    val id: String,
    val title: String,
    val chartType: ChartType,
    val dataPoints: List<Double>,
    val labels: List<String> = emptyList(),
    val color: String,
    val period: String,
    val unit: String = "",
    val description: String? = null
) {
    enum class ChartType {
        LINE,       // Gráfico de líneas
        BAR,        // Gráfico de barras
        AREA,       // Gráfico de área
        PIE,        // Gráfico circular
        DONUT       // Gráfico de dona
    }
    
    /**
     * Calcula el cambio porcentual entre el primer y último punto
     */
    fun getPercentageChange(): Double {
        if (dataPoints.size < 2) return 0.0
        val first = dataPoints.first()
        val last = dataPoints.last()
        return if (first != 0.0) ((last - first) / first) * 100 else 0.0
    }
    
    /**
     * Obtiene el valor máximo de los datos
     */
    fun getMaxValue(): Double = dataPoints.maxOrNull() ?: 0.0
    
    /**
     * Obtiene el valor mínimo de los datos
     */
    fun getMinValue(): Double = dataPoints.minOrNull() ?: 0.0
    
    /**
     * Obtiene el valor promedio de los datos
     */
    fun getAverageValue(): Double = if (dataPoints.isNotEmpty()) dataPoints.average() else 0.0
}

/**
 * Item de alerta del sistema
 */
data class AlertItem(
    val id: String,
    val title: String,
    val description: String,
    val severity: Severity,
    val timestamp: Long,
    val actionRequired: Boolean = false,
    val category: Category = Category.SYSTEM,
    val source: String? = null,
    val isDismissed: Boolean = false
) {
    enum class Severity {
        LOW,        // Información general
        MEDIUM,     // Advertencia
        HIGH,       // Crítico
        CRITICAL    // Emergencia
    }
    
    enum class Category {
        SYSTEM,         // Sistema general
        PERFORMANCE,    // Rendimiento
        CAPACITY,       // Capacidad
        TRAINING,       // Entrenamiento
        ROTATION,       // Rotaciones
        MAINTENANCE     // Mantenimiento
    }
    
    /**
     * Obtiene el color según la severidad
     */
    fun getSeverityColor(): String {
        return when (severity) {
            Severity.LOW -> "#4CAF50"       // Verde
            Severity.MEDIUM -> "#FF9800"    // Naranja
            Severity.HIGH -> "#F44336"      // Rojo
            Severity.CRITICAL -> "#9C27B0"  // Púrpura
        }
    }
    
    /**
     * Obtiene el icono según la categoría
     */
    fun getCategoryIcon(): String {
        return when (category) {
            Category.SYSTEM -> "⚙️"
            Category.PERFORMANCE -> "📈"
            Category.CAPACITY -> "📊"
            Category.TRAINING -> "🎓"
            Category.ROTATION -> "🔄"
            Category.MAINTENANCE -> "🔧"
        }
    }
    
    /**
     * Obtiene el texto de severidad
     */
    fun getSeverityText(): String {
        return when (severity) {
            Severity.LOW -> "Información"
            Severity.MEDIUM -> "Advertencia"
            Severity.HIGH -> "Crítico"
            Severity.CRITICAL -> "Emergencia"
        }
    }
    
    /**
     * Verifica si la alerta es reciente (menos de 1 hora)
     */
    fun isRecent(): Boolean {
        val oneHourAgo = System.currentTimeMillis() - (60 * 60 * 1000)
        return timestamp > oneHourAgo
    }
}

/**
 * Resumen de métricas agregadas
 */
data class MetricSummary(
    val id: String,
    val title: String,
    val currentValue: Double,
    val previousValue: Double,
    val target: Double? = null,
    val unit: String = "",
    val format: Format = Format.NUMBER,
    val period: String = "Actual"
) {
    enum class Format {
        NUMBER,         // Número simple
        PERCENTAGE,     // Porcentaje
        CURRENCY,       // Moneda
        TIME,           // Tiempo (minutos, horas)
        DECIMAL         // Decimal con precisión
    }
    
    /**
     * Calcula el cambio porcentual respecto al valor anterior
     */
    fun getChangePercentage(): Double {
        return if (previousValue != 0.0) {
            ((currentValue - previousValue) / previousValue) * 100
        } else 0.0
    }
    
    /**
     * Verifica si el cambio es positivo
     */
    fun isPositiveChange(): Boolean = currentValue > previousValue
    
    /**
     * Verifica si se alcanzó el objetivo
     */
    fun isTargetMet(): Boolean = target?.let { currentValue >= it } ?: false
    
    /**
     * Formatea el valor según el tipo especificado
     */
    fun getFormattedValue(): String {
        return when (format) {
            Format.NUMBER -> currentValue.toInt().toString()
            Format.PERCENTAGE -> "${String.format("%.1f", currentValue)}%"
            Format.CURRENCY -> "$${String.format("%.2f", currentValue)}"
            Format.TIME -> "${currentValue.toInt()}min"
            Format.DECIMAL -> String.format("%.2f", currentValue)
        }
    }
}

/**
 * Configuración de gráficos
 */
data class ChartConfiguration(
    val id: String,
    val title: String,
    val type: TrendData.ChartType,
    val showGrid: Boolean = true,
    val showLegend: Boolean = true,
    val showLabels: Boolean = true,
    val animationEnabled: Boolean = true,
    val colors: List<String> = listOf("#1976D2", "#FF9800", "#4CAF50"),
    val height: Int = 200,
    val refreshInterval: Long = 30000L // 30 segundos
) {
    /**
     * Obtiene la configuración de colores para el gráfico
     */
    fun getColorPalette(): List<String> {
        return if (colors.isNotEmpty()) colors else listOf("#1976D2")
    }
}

/**
 * Período de tiempo para análisis
 */
data class TimePeriod(
    val id: String,
    val name: String,
    val startTime: Long,
    val endTime: Long,
    val granularity: Granularity = Granularity.DAILY
) {
    enum class Granularity {
        HOURLY,     // Por horas
        DAILY,      // Por días
        WEEKLY,     // Por semanas
        MONTHLY     // Por meses
    }
    
    /**
     * Obtiene la duración del período en milisegundos
     */
    fun getDurationMs(): Long = endTime - startTime
    
    /**
     * Obtiene la duración del período en días
     */
    fun getDurationDays(): Int = (getDurationMs() / (24 * 60 * 60 * 1000)).toInt()
    
    companion object {
        /**
         * Crea un período para los últimos N días
         */
        fun lastDays(days: Int): TimePeriod {
            val endTime = System.currentTimeMillis()
            val startTime = endTime - (days * 24 * 60 * 60 * 1000L)
            return TimePeriod(
                id = "last_${days}_days",
                name = "Últimos $days días",
                startTime = startTime,
                endTime = endTime,
                granularity = if (days <= 7) Granularity.DAILY else Granularity.WEEKLY
            )
        }
        
        /**
         * Crea un período para la semana actual
         */
        fun currentWeek(): TimePeriod {
            val calendar = java.util.Calendar.getInstance()
            calendar.set(java.util.Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
            calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
            calendar.set(java.util.Calendar.MINUTE, 0)
            calendar.set(java.util.Calendar.SECOND, 0)
            val startTime = calendar.timeInMillis
            
            calendar.add(java.util.Calendar.WEEK_OF_YEAR, 1)
            val endTime = calendar.timeInMillis
            
            return TimePeriod(
                id = "current_week",
                name = "Semana Actual",
                startTime = startTime,
                endTime = endTime,
                granularity = Granularity.DAILY
            )
        }
        
        /**
         * Crea un período para el mes actual
         */
        fun currentMonth(): TimePeriod {
            val calendar = java.util.Calendar.getInstance()
            calendar.set(java.util.Calendar.DAY_OF_MONTH, 1)
            calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
            calendar.set(java.util.Calendar.MINUTE, 0)
            calendar.set(java.util.Calendar.SECOND, 0)
            val startTime = calendar.timeInMillis
            
            calendar.add(java.util.Calendar.MONTH, 1)
            val endTime = calendar.timeInMillis
            
            return TimePeriod(
                id = "current_month",
                name = "Mes Actual",
                startTime = startTime,
                endTime = endTime,
                granularity = Granularity.WEEKLY
            )
        }
    }
}

/**
 * Configuración del dashboard
 */
data class DashboardConfig(
    val refreshInterval: Long = 30000L, // 30 segundos
    val autoRefresh: Boolean = true,
    val showAnimations: Boolean = true,
    val compactMode: Boolean = false,
    val theme: Theme = Theme.LIGHT
) {
    enum class Theme {
        LIGHT,
        DARK,
        AUTO
    }
}