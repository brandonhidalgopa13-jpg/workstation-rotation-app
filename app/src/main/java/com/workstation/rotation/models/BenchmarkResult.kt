package com.workstation.rotation.models

/**
 * Modelo para almacenar resultados de benchmark de algoritmos de rotación.
 */
data class BenchmarkResult(
    val algorithmName: String,
    val executionTimeMs: Long,
    val memoryUsageMB: Double,
    val assignmentsCount: Int,
    val successRate: Double,
    val timestamp: Long
) {
    
    /**
     * Obtiene el tiempo de ejecución formateado.
     */
    fun getFormattedExecutionTime(): String {
        return when {
            executionTimeMs < 1000 -> "${executionTimeMs}ms"
            executionTimeMs < 60000 -> "${executionTimeMs / 1000.0}s"
            else -> "${executionTimeMs / 60000.0}min"
        }
    }
    
    /**
     * Obtiene el uso de memoria formateado.
     */
    fun getFormattedMemoryUsage(): String {
        return when {
            memoryUsageMB < 1.0 -> "${(memoryUsageMB * 1024).toInt()}KB"
            memoryUsageMB < 1024.0 -> "${memoryUsageMB.toInt()}MB"
            else -> "${(memoryUsageMB / 1024).toInt()}GB"
        }
    }
    
    /**
     * Obtiene la tasa de éxito formateada.
     */
    fun getFormattedSuccessRate(): String {
        return "${successRate.toInt()}%"
    }
    
    /**
     * Obtiene el timestamp formateado.
     */
    fun getFormattedTimestamp(): String {
        val date = java.util.Date(timestamp)
        val format = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
        return format.format(date)
    }
    
    /**
     * Determina si este resultado es mejor que otro en términos de rendimiento.
     */
    fun isBetterThan(other: BenchmarkResult): Boolean {
        // Priorizar tiempo de ejecución, luego uso de memoria, luego tasa de éxito
        return when {
            executionTimeMs != other.executionTimeMs -> executionTimeMs < other.executionTimeMs
            memoryUsageMB != other.memoryUsageMB -> memoryUsageMB < other.memoryUsageMB
            else -> successRate > other.successRate
        }
    }
    
    /**
     * Calcula la mejora porcentual respecto a otro resultado.
     */
    fun getImprovementPercentage(baseline: BenchmarkResult): Double {
        if (baseline.executionTimeMs == 0L) return 0.0
        return ((baseline.executionTimeMs - executionTimeMs).toDouble() / baseline.executionTimeMs) * 100
    }
}