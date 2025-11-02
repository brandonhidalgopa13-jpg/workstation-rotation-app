package com.workstation.rotation.analytics.models

import java.util.*

/**
 * Modelos de datos para el sistema de Analytics Avanzados
 * Incluye análisis predictivo, patrones y métricas ML
 */

// Análisis de Patrones de Rotación
data class RotationPattern(
    val id: String = UUID.randomUUID().toString(),
    val patternType: PatternType,
    val workstationId: Long,
    val workerId: Long,
    val frequency: Double, // Frecuencia de ocurrencia (0.0 - 1.0)
    val efficiency: Double, // Eficiencia del patrón (0.0 - 1.0)
    val confidence: Double, // Confianza del análisis (0.0 - 1.0)
    val detectedAt: Date = Date(),
    val metadata: Map<String, Any> = emptyMap()
)

enum class PatternType {
    OPTIMAL_SEQUENCE,      // Secuencia óptima de rotaciones
    BOTTLENECK,           // Cuello de botella detectado
    HIGH_EFFICIENCY,      // Patrón de alta eficiencia
    SKILL_MISMATCH,       // Desajuste de habilidades
    FATIGUE_INDICATOR,    // Indicador de fatiga
    PREFERENCE_PATTERN    // Patrón de preferencias
}

// Predicciones de Rotación
data class RotationPrediction(
    val id: String = UUID.randomUUID().toString(),
    val targetDate: Date,
    val workstationId: Long,
    val predictedWorkerId: Long,
    val confidence: Double, // Confianza de la predicción (0.0 - 1.0)
    val expectedDuration: Long, // Duración esperada en minutos
    val riskFactors: List<RiskFactor>,
    val recommendations: List<String>,
    val createdAt: Date = Date()
)

data class RiskFactor(
    val type: RiskType,
    val severity: RiskSeverity,
    val description: String,
    val impact: Double // Impacto en la eficiencia (0.0 - 1.0)
)

enum class RiskType {
    SKILL_GAP,
    FATIGUE_RISK,
    EQUIPMENT_ISSUE,
    WORKLOAD_IMBALANCE,
    TRAINING_NEEDED
}

enum class RiskSeverity {
    LOW, MEDIUM, HIGH, CRITICAL
}

// Análisis de Carga de Trabajo
data class WorkloadAnalysis(
    val workstationId: Long,
    val analysisDate: Date,
    val currentLoad: Double, // Carga actual (0.0 - 1.0)
    val optimalLoad: Double, // Carga óptima calculada
    val utilizationRate: Double, // Tasa de utilización
    val bottleneckScore: Double, // Puntuación de cuello de botella
    val recommendations: List<WorkloadRecommendation>
)

data class WorkloadRecommendation(
    val type: RecommendationType,
    val priority: Priority,
    val description: String,
    val expectedImprovement: Double // Mejora esperada en %
)

enum class RecommendationType {
    INCREASE_WORKERS,
    REDISTRIBUTE_TASKS,
    SKILL_TRAINING,
    EQUIPMENT_UPGRADE,
    PROCESS_OPTIMIZATION
}

enum class Priority {
    LOW, MEDIUM, HIGH, URGENT
}

// Métricas de Rendimiento Individual
data class WorkerPerformanceMetrics(
    val workerId: Long,
    val analysisDate: Date,
    val overallScore: Double, // Puntuación general (0.0 - 10.0)
    val efficiencyScore: Double,
    val adaptabilityScore: Double,
    val consistencyScore: Double,
    val skillUtilization: Double,
    val improvementAreas: List<ImprovementArea>,
    val strengths: List<String>,
    val trendDirection: TrendDirection
)

data class ImprovementArea(
    val skill: String,
    val currentLevel: Double, // Nivel actual (0.0 - 10.0)
    val targetLevel: Double,
    val trainingRecommendations: List<String>
)

enum class TrendDirection {
    IMPROVING, STABLE, DECLINING
}

// Análisis de Satisfacción Laboral
data class SatisfactionAnalysis(
    val workerId: Long,
    val analysisDate: Date,
    val satisfactionScore: Double, // Puntuación de satisfacción (0.0 - 10.0)
    val engagementLevel: EngagementLevel,
    val stressIndicators: List<StressIndicator>,
    val motivationFactors: List<String>,
    val burnoutRisk: Double // Riesgo de burnout (0.0 - 1.0)
)

enum class EngagementLevel {
    VERY_LOW, LOW, MODERATE, HIGH, VERY_HIGH
}

data class StressIndicator(
    val type: StressType,
    val level: Double, // Nivel de estrés (0.0 - 1.0)
    val triggers: List<String>
)

enum class StressType {
    WORKLOAD_STRESS,
    SKILL_STRESS,
    SOCIAL_STRESS,
    ENVIRONMENTAL_STRESS
}

// Análisis de Cuellos de Botella
data class BottleneckAnalysis(
    val id: String = UUID.randomUUID().toString(),
    val workstationId: Long,
    val detectedAt: Date,
    val severity: BottleneckSeverity,
    val impactScore: Double, // Impacto en la productividad (0.0 - 1.0)
    val rootCauses: List<String>,
    val affectedWorkers: List<Long>,
    val suggestedSolutions: List<BottleneckSolution>
)

enum class BottleneckSeverity {
    MINOR, MODERATE, MAJOR, CRITICAL
}

data class BottleneckSolution(
    val description: String,
    val implementationCost: CostLevel,
    val expectedImprovement: Double, // Mejora esperada en %
    val timeToImplement: Long // Tiempo en días
)

enum class CostLevel {
    LOW, MEDIUM, HIGH, VERY_HIGH
}

// Reportes Avanzados
data class AdvancedReport(
    val id: String = UUID.randomUUID().toString(),
    val reportType: ReportType,
    val title: String,
    val generatedAt: Date,
    val dateRange: DateRange,
    val summary: ReportSummary,
    val sections: List<ReportSection>,
    val recommendations: List<String>,
    val attachments: List<ReportAttachment> = emptyList()
)

enum class ReportType {
    PERFORMANCE_ANALYSIS,
    EFFICIENCY_REPORT,
    PREDICTIVE_INSIGHTS,
    BOTTLENECK_ANALYSIS,
    SATISFACTION_SURVEY,
    COMPREHENSIVE_OVERVIEW
}

data class DateRange(
    val startDate: Date,
    val endDate: Date
)

data class ReportSummary(
    val keyMetrics: Map<String, Double>,
    val highlights: List<String>,
    val concerns: List<String>,
    val overallScore: Double
)

data class ReportSection(
    val title: String,
    val content: String,
    val charts: List<ChartData>,
    val tables: List<TableData>
)

data class ChartData(
    val type: ChartType,
    val title: String,
    val data: Map<String, Double>,
    val metadata: Map<String, Any> = emptyMap()
)

enum class ChartType {
    LINE_CHART,
    BAR_CHART,
    PIE_CHART,
    SCATTER_PLOT,
    HEATMAP
}

data class TableData(
    val headers: List<String>,
    val rows: List<List<String>>
)

data class ReportAttachment(
    val name: String,
    val type: AttachmentType,
    val data: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as ReportAttachment
        if (name != other.name) return false
        if (type != other.type) return false
        if (!data.contentEquals(other.data)) return false
        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + data.contentHashCode()
        return result
    }
}

enum class AttachmentType {
    PDF, EXCEL, CSV, IMAGE
}