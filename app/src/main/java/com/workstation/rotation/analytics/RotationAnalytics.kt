package com.workstation.rotation.analytics

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.ConcurrentHashMap
import kotlin.system.measureTimeMillis

/**
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 📊 SISTEMA DE ANALYTICS Y MÉTRICAS EN TIEMPO REAL
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 
 * Este sistema recopila y analiza métricas del sistema de rotación en tiempo real:
 * - Rendimiento de algoritmos
 * - Patrones de uso
 * - Eficiencia de asignaciones
 * - Estadísticas de liderazgo y entrenamiento
 * - Métricas de satisfacción del usuario
 * 
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 */
class RotationAnalytics {
    
    // Métricas de rendimiento
    private val _performanceMetrics = MutableStateFlow(PerformanceMetrics())
    val performanceMetrics: StateFlow<PerformanceMetrics> = _performanceMetrics.asStateFlow()
    
    // Métricas de uso
    private val _usageMetrics = MutableStateFlow(UsageMetrics())
    val usageMetrics: StateFlow<UsageMetrics> = _usageMetrics.asStateFlow()
    
    // Métricas de calidad
    private val _qualityMetrics = MutableStateFlow(QualityMetrics())
    val qualityMetrics: StateFlow<QualityMetrics> = _qualityMetrics.asStateFlow()
    
    // Cache de métricas históricas
    private val metricsHistory = ConcurrentHashMap<String, MutableList<MetricSnapshot>>()
    
    companion object {
        @Volatile
        private var INSTANCE: RotationAnalytics? = null
        
        fun getInstance(): RotationAnalytics {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: RotationAnalytics().also { INSTANCE = it }
            }
        }
    }
    
    /**
     * Registra el tiempo de ejecución de una operación.
     */
    suspend fun <T> measureOperation(
        operationName: String,
        operation: suspend () -> T
    ): T {
        val startTime = System.currentTimeMillis()
        val result: T
        val executionTime = measureTimeMillis {
            result = operation()
        }
        
        recordPerformanceMetric(operationName, executionTime)
        return result
    }
    
    /**
     * Registra métricas de rendimiento.
     */
    fun recordPerformanceMetric(operationName: String, executionTimeMs: Long) {
        val current = _performanceMetrics.value
        val updated = when (operationName) {
            "sql_rotation_generation" -> current.copy(
                sqlRotationGenerationTime = executionTimeMs,
                sqlRotationCount = current.sqlRotationCount + 1,
                avgSqlRotationTime = calculateAverage(current.avgSqlRotationTime, executionTimeMs, current.sqlRotationCount)
            )
            "original_rotation_generation" -> current.copy(
                originalRotationGenerationTime = executionTimeMs,
                originalRotationCount = current.originalRotationCount + 1,
                avgOriginalRotationTime = calculateAverage(current.avgOriginalRotationTime, executionTimeMs, current.originalRotationCount)
            )
            "database_query" -> current.copy(
                lastDatabaseQueryTime = executionTimeMs,
                totalDatabaseQueries = current.totalDatabaseQueries + 1,
                avgDatabaseQueryTime = calculateAverage(current.avgDatabaseQueryTime, executionTimeMs, current.totalDatabaseQueries)
            )
            else -> current
        }
        
        _performanceMetrics.value = updated
        recordHistoricalMetric(operationName, executionTimeMs)
    }
    
    /**
     * Registra métricas de uso de la aplicación.
     */
    fun recordUsageMetric(action: String, details: Map<String, Any> = emptyMap()) {
        val current = _usageMetrics.value
        val updated = when (action) {
            "rotation_generated" -> {
                val rotationType = details["type"] as? String ?: "unknown"
                current.copy(
                    totalRotationsGenerated = current.totalRotationsGenerated + 1,
                    sqlRotationsGenerated = if (rotationType == "sql") current.sqlRotationsGenerated + 1 else current.sqlRotationsGenerated,
                    originalRotationsGenerated = if (rotationType == "original") current.originalRotationsGenerated + 1 else current.originalRotationsGenerated,
                    lastRotationTimestamp = System.currentTimeMillis()
                )
            }
            "rotation_half_toggled" -> current.copy(
                rotationHalfToggles = current.rotationHalfToggles + 1
            )
            "rotation_cleared" -> current.copy(
                rotationsCleared = current.rotationsCleared + 1
            )
            "app_opened" -> current.copy(
                appOpenCount = current.appOpenCount + 1,
                lastAppOpenTimestamp = System.currentTimeMillis()
            )
            else -> current
        }
        
        _usageMetrics.value = updated
    }
    
    /**
     * Registra métricas de calidad de las rotaciones.
     */
    fun recordQualityMetric(
        workersAssigned: Int,
        stationsCompleted: Int,
        totalStations: Int,
        leadersCorrectlyAssigned: Int,
        totalLeaders: Int,
        trainingPairsKeptTogether: Int,
        totalTrainingPairs: Int
    ) {
        val completionRate = if (totalStations > 0) (stationsCompleted.toFloat() / totalStations) * 100 else 0f
        val leaderAccuracy = if (totalLeaders > 0) (leadersCorrectlyAssigned.toFloat() / totalLeaders) * 100 else 100f
        val trainingAccuracy = if (totalTrainingPairs > 0) (trainingPairsKeptTogether.toFloat() / totalTrainingPairs) * 100 else 100f
        
        val current = _qualityMetrics.value
        val updated = current.copy(
            lastWorkersAssigned = workersAssigned,
            lastStationCompletionRate = completionRate,
            lastLeaderAccuracy = leaderAccuracy,
            lastTrainingAccuracy = trainingAccuracy,
            avgStationCompletionRate = calculateAverage(current.avgStationCompletionRate, completionRate, current.totalQualityMeasurements),
            avgLeaderAccuracy = calculateAverage(current.avgLeaderAccuracy, leaderAccuracy, current.totalQualityMeasurements),
            avgTrainingAccuracy = calculateAverage(current.avgTrainingAccuracy, trainingAccuracy, current.totalQualityMeasurements),
            totalQualityMeasurements = current.totalQualityMeasurements + 1
        )
        
        _qualityMetrics.value = updated
    }
    
    /**
     * Obtiene un reporte completo de analytics.
     */
    fun getAnalyticsReport(): AnalyticsReport {
        val performance = _performanceMetrics.value
        val usage = _usageMetrics.value
        val quality = _qualityMetrics.value
        
        return AnalyticsReport(
            performanceMetrics = performance,
            usageMetrics = usage,
            qualityMetrics = quality,
            generatedAt = System.currentTimeMillis(),
            recommendations = generateRecommendations(performance, usage, quality)
        )
    }
    
    /**
     * Genera recomendaciones basadas en las métricas.
     */
    private fun generateRecommendations(
        performance: PerformanceMetrics,
        usage: UsageMetrics,
        quality: QualityMetrics
    ): List<String> {
        val recommendations = mutableListOf<String>()
        
        // Recomendaciones de rendimiento
        if (performance.avgSqlRotationTime > 0 && performance.avgOriginalRotationTime > 0) {
            val improvement = ((performance.avgOriginalRotationTime - performance.avgSqlRotationTime).toFloat() / performance.avgOriginalRotationTime) * 100
            if (improvement > 30) {
                recommendations.add("🚀 El sistema SQL es ${improvement.toInt()}% más rápido. Se recomienda usar exclusivamente la rotación SQL.")
            }
        }
        
        // Recomendaciones de calidad
        if (quality.avgLeaderAccuracy < 95) {
            recommendations.add("👑 La precisión de asignación de líderes es ${quality.avgLeaderAccuracy.toInt()}%. Revisar configuración de liderazgo.")
        }
        
        if (quality.avgTrainingAccuracy < 100) {
            recommendations.add("🎯 Algunas parejas de entrenamiento se están separando. Verificar configuración de entrenamientos.")
        }
        
        if (quality.avgStationCompletionRate < 80) {
            recommendations.add("🏭 Solo ${quality.avgStationCompletionRate.toInt()}% de estaciones se completan. Considerar ajustar capacidades o agregar más trabajadores.")
        }
        
        // Recomendaciones de uso
        if (usage.sqlRotationsGenerated > usage.originalRotationsGenerated * 2) {
            recommendations.add("📊 Los usuarios prefieren la rotación SQL. Considerar hacer SQL el método predeterminado.")
        }
        
        if (usage.rotationHalfToggles > usage.totalRotationsGenerated) {
            recommendations.add("🔄 Los usuarios alternan frecuentemente entre partes de rotación. Considerar mostrar ambas simultáneamente.")
        }
        
        return recommendations
    }
    
    /**
     * Calcula el promedio incremental.
     */
    private fun calculateAverage(currentAvg: Float, newValue: Long, count: Int): Float {
        return if (count <= 1) newValue.toFloat()
        else ((currentAvg * (count - 1)) + newValue) / count
    }
    
    private fun calculateAverage(currentAvg: Float, newValue: Float, count: Int): Float {
        return if (count <= 1) newValue
        else ((currentAvg * (count - 1)) + newValue) / count
    }
    
    /**
     * Registra métricas históricas para análisis de tendencias.
     */
    private fun recordHistoricalMetric(operationName: String, value: Long) {
        val history = metricsHistory.getOrPut(operationName) { mutableListOf() }
        history.add(MetricSnapshot(System.currentTimeMillis(), value))
        
        // Mantener solo los últimos 100 registros
        if (history.size > 100) {
            history.removeAt(0)
        }
    }
    
    /**
     * Obtiene tendencias históricas de una métrica.
     */
    fun getMetricTrend(operationName: String): List<MetricSnapshot> {
        return metricsHistory[operationName]?.toList() ?: emptyList()
    }
    
    /**
     * Limpia todas las métricas (útil para testing).
     */
    fun clearAllMetrics() {
        _performanceMetrics.value = PerformanceMetrics()
        _usageMetrics.value = UsageMetrics()
        _qualityMetrics.value = QualityMetrics()
        metricsHistory.clear()
    }
}

/**
 * Data classes para métricas
 */
data class PerformanceMetrics(
    val sqlRotationGenerationTime: Long = 0,
    val originalRotationGenerationTime: Long = 0,
    val lastDatabaseQueryTime: Long = 0,
    val avgSqlRotationTime: Float = 0f,
    val avgOriginalRotationTime: Float = 0f,
    val avgDatabaseQueryTime: Float = 0f,
    val sqlRotationCount: Int = 0,
    val originalRotationCount: Int = 0,
    val totalDatabaseQueries: Int = 0
)

data class UsageMetrics(
    val totalRotationsGenerated: Int = 0,
    val sqlRotationsGenerated: Int = 0,
    val originalRotationsGenerated: Int = 0,
    val rotationHalfToggles: Int = 0,
    val rotationsCleared: Int = 0,
    val appOpenCount: Int = 0,
    val lastRotationTimestamp: Long = 0,
    val lastAppOpenTimestamp: Long = 0
)

data class QualityMetrics(
    val lastWorkersAssigned: Int = 0,
    val lastStationCompletionRate: Float = 0f,
    val lastLeaderAccuracy: Float = 100f,
    val lastTrainingAccuracy: Float = 100f,
    val avgStationCompletionRate: Float = 0f,
    val avgLeaderAccuracy: Float = 100f,
    val avgTrainingAccuracy: Float = 100f,
    val totalQualityMeasurements: Int = 0
)

data class MetricSnapshot(
    val timestamp: Long,
    val value: Long
)

data class AnalyticsReport(
    val performanceMetrics: PerformanceMetrics,
    val usageMetrics: UsageMetrics,
    val qualityMetrics: QualityMetrics,
    val generatedAt: Long,
    val recommendations: List<String>
)