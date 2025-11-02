package com.workstation.rotation.analytics.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.workstation.rotation.analytics.models.*
import com.workstation.rotation.analytics.services.AdvancedAnalyticsService
import com.workstation.rotation.data.database.AppDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.*

/**
 * ViewModel para Analytics Avanzados
 * Gestiona el estado y las operaciones de análisis predictivo
 */
class AdvancedAnalyticsViewModel(
    private val database: AppDatabase,
    private val analyticsService: AdvancedAnalyticsService = AdvancedAnalyticsService()
) : ViewModel() {

    // Estados de UI
    private val _uiState = MutableStateFlow(AdvancedAnalyticsUiState())
    val uiState: StateFlow<AdvancedAnalyticsUiState> = _uiState.asStateFlow()

    // Datos de análisis
    private val _rotationPatterns = MutableStateFlow<List<RotationPattern>>(emptyList())
    val rotationPatterns: StateFlow<List<RotationPattern>> = _rotationPatterns.asStateFlow()

    private val _predictions = MutableStateFlow<List<RotationPrediction>>(emptyList())
    val predictions: StateFlow<List<RotationPrediction>> = _predictions.asStateFlow()

    private val _workloadAnalysis = MutableStateFlow<List<WorkloadAnalysis>>(emptyList())
    val workloadAnalysis: StateFlow<List<WorkloadAnalysis>> = _workloadAnalysis.asStateFlow()

    private val _performanceMetrics = MutableStateFlow<List<WorkerPerformanceMetrics>>(emptyList())
    val performanceMetrics: StateFlow<List<WorkerPerformanceMetrics>> = _performanceMetrics.asStateFlow()

    private val _bottleneckAnalysis = MutableStateFlow<List<BottleneckAnalysis>>(emptyList())
    val bottleneckAnalysis: StateFlow<List<BottleneckAnalysis>> = _bottleneckAnalysis.asStateFlow()

    private val _advancedReports = MutableStateFlow<List<AdvancedReport>>(emptyList())
    val advancedReports: StateFlow<List<AdvancedReport>> = _advancedReports.asStateFlow()

    init {
        loadAllAnalytics()
    }

    fun loadAllAnalytics() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                // Cargar datos base
                val workers = database.workerDao().getAllWorkers().first()
                val workstations = database.workstationDao().getAllWorkstations().first()
                val rotationHistory = database.rotationHistoryDao().getAll()

                // Ejecutar análisis en paralelo
                launch { analyzeRotationPatterns(rotationHistory, workers, workstations) }
                launch { generatePredictions(workers, workstations, rotationHistory) }
                launch { analyzeWorkload(workstations, rotationHistory) }
                launch { analyzePerformance(workers, rotationHistory) }
                launch { analyzeBottlenecks(workstations, rotationHistory) }
                launch { generateAdvancedReports(workers, workstations, rotationHistory) }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    lastUpdated = Date()
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error al cargar analytics: ${e.message}"
                )
            }
        }
    }

    private suspend fun analyzeRotationPatterns(
        rotationHistory: List<com.workstation.rotation.data.entities.RotationHistory>,
        workers: List<com.workstation.rotation.data.entities.Worker>,
        workstations: List<com.workstation.rotation.data.entities.Workstation>
    ) {
        try {
            val patterns = analyticsService.analyzeRotationPatterns(rotationHistory, workers, workstations)
            _rotationPatterns.value = patterns
        } catch (e: Exception) {
            // Log error but don't fail the entire operation
        }
    }

    private suspend fun generatePredictions(
        workers: List<com.workstation.rotation.data.entities.Worker>,
        workstations: List<com.workstation.rotation.data.entities.Workstation>,
        rotationHistory: List<com.workstation.rotation.data.entities.RotationHistory>
    ) {
        try {
            val predictions = analyticsService.generateRotationPredictions(
                workers, workstations, rotationHistory, 7
            )
            _predictions.value = predictions
        } catch (e: Exception) {
            // Log error but don't fail the entire operation
        }
    }

    private suspend fun analyzeWorkload(
        workstations: List<com.workstation.rotation.data.entities.Workstation>,
        rotationHistory: List<com.workstation.rotation.data.entities.RotationHistory>
    ) {
        try {
            val analysis = analyticsService.analyzeWorkload(workstations, rotationHistory)
            _workloadAnalysis.value = analysis
        } catch (e: Exception) {
            // Log error but don't fail the entire operation
        }
    }

    private suspend fun analyzePerformance(
        workers: List<com.workstation.rotation.data.entities.Worker>,
        rotationHistory: List<com.workstation.rotation.data.entities.RotationHistory>
    ) {
        try {
            val metrics = analyticsService.analyzeWorkerPerformance(workers, rotationHistory)
            _performanceMetrics.value = metrics
        } catch (e: Exception) {
            // Log error but don't fail the entire operation
        }
    }

    private suspend fun analyzeBottlenecks(
        workstations: List<com.workstation.rotation.data.entities.Workstation>,
        rotationHistory: List<com.workstation.rotation.data.entities.RotationHistory>
    ) {
        try {
            val bottlenecks = generateBottleneckAnalysis(workstations, rotationHistory)
            _bottleneckAnalysis.value = bottlenecks
        } catch (e: Exception) {
            // Log error but don't fail the entire operation
        }
    }

    private suspend fun generateAdvancedReports(
        workers: List<com.workstation.rotation.data.entities.Worker>,
        workstations: List<com.workstation.rotation.data.entities.Workstation>,
        rotationHistory: List<com.workstation.rotation.data.entities.RotationHistory>
    ) {
        try {
            val reports = createAdvancedReports(workers, workstations, rotationHistory)
            _advancedReports.value = reports
        } catch (e: Exception) {
            // Log error but don't fail the entire operation
        }
    }

    // Funciones específicas de análisis
    fun getPatternsByType(type: PatternType): List<RotationPattern> {
        return _rotationPatterns.value.filter { it.patternType == type }
    }

    fun getPredictionsForWorkstation(workstationId: Long): List<RotationPrediction> {
        return _predictions.value.filter { it.workstationId == workstationId }
    }

    fun getWorkerPerformance(workerId: Long): WorkerPerformanceMetrics? {
        return _performanceMetrics.value.find { it.workerId == workerId }
    }

    fun getHighRiskPredictions(): List<RotationPrediction> {
        return _predictions.value.filter { prediction ->
            prediction.riskFactors.any { it.severity in listOf(RiskSeverity.HIGH, RiskSeverity.CRITICAL) }
        }
    }

    fun getTopPerformers(limit: Int = 5): List<WorkerPerformanceMetrics> {
        return _performanceMetrics.value
            .sortedByDescending { it.overallScore }
            .take(limit)
    }

    fun getBottlenecksByWorkstation(): Map<Long, List<BottleneckAnalysis>> {
        return _bottleneckAnalysis.value.groupBy { it.workstationId }
    }

    fun refreshAnalytics() {
        loadAllAnalytics()
    }

    fun generateCustomReport(
        reportType: ReportType,
        dateRange: DateRange,
        includeWorkstations: List<Long> = emptyList(),
        includeWorkers: List<Long> = emptyList()
    ) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isGeneratingReport = true)
                
                // Simular generación de reporte personalizado
                val customReport = createCustomReport(
                    reportType, dateRange, includeWorkstations, includeWorkers
                )
                
                val updatedReports = _advancedReports.value + customReport
                _advancedReports.value = updatedReports
                
                _uiState.value = _uiState.value.copy(
                    isGeneratingReport = false,
                    lastReportGenerated = customReport
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isGeneratingReport = false,
                    error = "Error al generar reporte: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    // Funciones auxiliares privadas
    private fun generateBottleneckAnalysis(
        workstations: List<com.workstation.rotation.data.entities.Workstation>,
        rotationHistory: List<com.workstation.rotation.data.entities.RotationHistory>
    ): List<BottleneckAnalysis> {
        return workstations.mapNotNull { workstation ->
            val stationHistory = rotationHistory.filter { it.workstation_id == workstation.id }
            
            if (stationHistory.isNotEmpty()) {
                val durations = stationHistory.mapNotNull { it.duration_minutes?.toDouble() }
                val allDurations = rotationHistory.mapNotNull { it.duration_minutes?.toDouble() }
                val avgDuration = if (durations.isNotEmpty()) durations.average() else 0.0
                val overallAvg = if (allDurations.isNotEmpty()) allDurations.average() else 0.0
                
                if (avgDuration > overallAvg * 1.3) { // 30% más que el promedio
                    BottleneckAnalysis(
                        workstationId = workstation.id,
                        detectedAt = Date(),
                        severity = when {
                            avgDuration > overallAvg * 2.0 -> BottleneckSeverity.CRITICAL
                            avgDuration > overallAvg * 1.7 -> BottleneckSeverity.MAJOR
                            avgDuration > overallAvg * 1.5 -> BottleneckSeverity.MODERATE
                            else -> BottleneckSeverity.MINOR
                        },
                        impactScore = ((avgDuration - overallAvg) / overallAvg).coerceAtMost(1.0),
                        rootCauses = listOf(
                            "Duración promedio superior al estándar",
                            "Posible complejidad de tareas",
                            "Necesidad de capacitación específica"
                        ),
                        affectedWorkers = stationHistory.map { it.worker_id }.distinct(),
                        suggestedSolutions = listOf(
                            BottleneckSolution(
                                description = "Capacitación especializada para trabajadores",
                                implementationCost = CostLevel.MEDIUM,
                                expectedImprovement = 25.0,
                                timeToImplement = 14
                            ),
                            BottleneckSolution(
                                description = "Optimización de procesos en la estación",
                                implementationCost = CostLevel.LOW,
                                expectedImprovement = 15.0,
                                timeToImplement = 7
                            )
                        )
                    )
                } else null
            } else null
        }
    }

    private fun createAdvancedReports(
        workers: List<com.workstation.rotation.data.entities.Worker>,
        workstations: List<com.workstation.rotation.data.entities.Workstation>,
        rotationHistory: List<com.workstation.rotation.data.entities.RotationHistory>
    ): List<AdvancedReport> {
        val reports = mutableListOf<AdvancedReport>()
        
        // Reporte de Análisis de Rendimiento
        reports.add(createPerformanceReport(workers, rotationHistory))
        
        // Reporte de Eficiencia
        reports.add(createEfficiencyReport(workstations, rotationHistory))
        
        // Reporte de Insights Predictivos
        reports.add(createPredictiveInsightsReport())
        
        return reports
    }

    private fun createPerformanceReport(
        workers: List<com.workstation.rotation.data.entities.Worker>,
        rotationHistory: List<com.workstation.rotation.data.entities.RotationHistory>
    ): AdvancedReport {
        val topPerformers = getTopPerformers(3)
        val avgPerformance = _performanceMetrics.value.map { it.overallScore }.average()
        
        return AdvancedReport(
            reportType = ReportType.PERFORMANCE_ANALYSIS,
            title = "Análisis de Rendimiento de Trabajadores",
            generatedAt = Date(),
            dateRange = DateRange(
                startDate = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, -30) }.time,
                endDate = Date()
            ),
            summary = ReportSummary(
                keyMetrics = mapOf(
                    "Rendimiento Promedio" to avgPerformance,
                    "Trabajadores Analizados" to workers.size.toDouble(),
                    "Rotaciones Evaluadas" to rotationHistory.size.toDouble()
                ),
                highlights = listOf(
                    "Top performer: ${topPerformers.firstOrNull()?.workerId ?: "N/A"}",
                    "Rendimiento promedio: ${String.format("%.1f", avgPerformance)}/10"
                ),
                concerns = _performanceMetrics.value
                    .filter { it.overallScore < 6.0 }
                    .map { "Trabajador ${it.workerId} requiere atención" },
                overallScore = avgPerformance
            ),
            sections = listOf(
                ReportSection(
                    title = "Resumen Ejecutivo",
                    content = "Análisis detallado del rendimiento de ${workers.size} trabajadores basado en ${rotationHistory.size} rotaciones.",
                    charts = listOf(
                        ChartData(
                            type = ChartType.BAR_CHART,
                            title = "Distribución de Rendimiento",
                            data = _performanceMetrics.value.associate { 
                                "Trabajador ${it.workerId}" to it.overallScore 
                            }
                        )
                    ),
                    tables = listOf(
                        TableData(
                            headers = listOf("Trabajador", "Rendimiento", "Eficiencia", "Consistencia"),
                            rows = _performanceMetrics.value.map { metrics ->
                                listOf(
                                    "Trabajador ${metrics.workerId}",
                                    String.format("%.1f", metrics.overallScore),
                                    String.format("%.1f", metrics.efficiencyScore),
                                    String.format("%.1f", metrics.consistencyScore)
                                )
                            }
                        )
                    )
                )
            ),
            recommendations = listOf(
                "Implementar programa de capacitación para trabajadores con rendimiento < 6.0",
                "Reconocer y recompensar a los top performers",
                "Establecer mentoring entre trabajadores experimentados y nuevos"
            )
        )
    }

    private fun createEfficiencyReport(
        workstations: List<com.workstation.rotation.data.entities.Workstation>,
        rotationHistory: List<com.workstation.rotation.data.entities.RotationHistory>
    ): AdvancedReport {
        val workloadData = _workloadAnalysis.value
        val avgUtilization = workloadData.map { it.utilizationRate }.average()
        
        return AdvancedReport(
            reportType = ReportType.EFFICIENCY_REPORT,
            title = "Reporte de Eficiencia Operativa",
            generatedAt = Date(),
            dateRange = DateRange(
                startDate = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, -30) }.time,
                endDate = Date()
            ),
            summary = ReportSummary(
                keyMetrics = mapOf(
                    "Utilización Promedio" to avgUtilization * 100,
                    "Estaciones Analizadas" to workstations.size.toDouble(),
                    "Cuellos de Botella" to _bottleneckAnalysis.value.size.toDouble()
                ),
                highlights = listOf(
                    "Utilización promedio: ${String.format("%.1f", avgUtilization * 100)}%",
                    "${_bottleneckAnalysis.value.size} cuellos de botella identificados"
                ),
                concerns = workloadData
                    .filter { it.utilizationRate > 1.2 || it.utilizationRate < 0.6 }
                    .map { "Estación ${it.workstationId} requiere optimización" },
                overallScore = avgUtilization * 10
            ),
            sections = listOf(
                ReportSection(
                    title = "Análisis de Carga de Trabajo",
                    content = "Evaluación de la eficiencia y utilización de ${workstations.size} estaciones de trabajo.",
                    charts = listOf(
                        ChartData(
                            type = ChartType.BAR_CHART,
                            title = "Utilización por Estación",
                            data = workloadData.associate { 
                                "Estación ${it.workstationId}" to (it.utilizationRate * 100) 
                            }
                        )
                    ),
                    tables = listOf(
                        TableData(
                            headers = listOf("Estación", "Carga Actual", "Carga Óptima", "Utilización %"),
                            rows = workloadData.map { analysis ->
                                listOf(
                                    "Estación ${analysis.workstationId}",
                                    String.format("%.2f", analysis.currentLoad),
                                    String.format("%.2f", analysis.optimalLoad),
                                    String.format("%.1f%%", analysis.utilizationRate * 100)
                                )
                            }
                        )
                    )
                )
            ),
            recommendations = workloadData.flatMap { it.recommendations.map { rec -> rec.description } }
        )
    }

    private fun createPredictiveInsightsReport(): AdvancedReport {
        val highConfidencePredictions = _predictions.value.filter { it.confidence > 0.8 }
        val patterns = _rotationPatterns.value
        
        return AdvancedReport(
            reportType = ReportType.PREDICTIVE_INSIGHTS,
            title = "Insights Predictivos y Patrones",
            generatedAt = Date(),
            dateRange = DateRange(
                startDate = Date(),
                endDate = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, 7) }.time
            ),
            summary = ReportSummary(
                keyMetrics = mapOf(
                    "Predicciones Generadas" to _predictions.value.size.toDouble(),
                    "Confianza Promedio" to (_predictions.value.map { it.confidence }.average() * 100),
                    "Patrones Detectados" to patterns.size.toDouble()
                ),
                highlights = listOf(
                    "${highConfidencePredictions.size} predicciones de alta confianza",
                    "${patterns.count { it.patternType == PatternType.OPTIMAL_SEQUENCE }} secuencias óptimas identificadas"
                ),
                concerns = _predictions.value
                    .filter { it.riskFactors.any { risk -> risk.severity == RiskSeverity.HIGH } }
                    .map { "Predicción de alto riesgo para estación ${it.workstationId}" },
                overallScore = _predictions.value.map { it.confidence }.average() * 10
            ),
            sections = listOf(
                ReportSection(
                    title = "Predicciones de Rotación",
                    content = "Análisis predictivo para los próximos 7 días basado en patrones históricos.",
                    charts = listOf(
                        ChartData(
                            type = ChartType.LINE_CHART,
                            title = "Confianza de Predicciones por Día",
                            data = _predictions.value
                                .groupBy { it.targetDate }
                                .mapKeys { it.key.toString() }
                                .mapValues { it.value.map { pred -> pred.confidence }.average() * 100 }
                        )
                    ),
                    tables = listOf(
                        TableData(
                            headers = listOf("Fecha", "Estación", "Trabajador Predicho", "Confianza %"),
                            rows = _predictions.value.take(10).map { prediction ->
                                listOf(
                                    prediction.targetDate.toString().substring(0, 10),
                                    "Estación ${prediction.workstationId}",
                                    "Trabajador ${prediction.predictedWorkerId}",
                                    String.format("%.1f%%", prediction.confidence * 100)
                                )
                            }
                        )
                    )
                )
            ),
            recommendations = listOf(
                "Utilizar predicciones de alta confianza para planificación proactiva",
                "Investigar y mitigar factores de riesgo identificados",
                "Aprovechar patrones óptimos para mejorar eficiencia general"
            )
        )
    }

    private fun createCustomReport(
        reportType: ReportType,
        dateRange: DateRange,
        includeWorkstations: List<Long>,
        includeWorkers: List<Long>
    ): AdvancedReport {
        return AdvancedReport(
            reportType = reportType,
            title = "Reporte Personalizado - ${reportType.name}",
            generatedAt = Date(),
            dateRange = dateRange,
            summary = ReportSummary(
                keyMetrics = mapOf("Elementos Incluidos" to (includeWorkstations.size + includeWorkers.size).toDouble()),
                highlights = listOf("Reporte generado según criterios específicos"),
                concerns = emptyList(),
                overallScore = 8.0
            ),
            sections = listOf(
                ReportSection(
                    title = "Datos Personalizados",
                    content = "Reporte generado con filtros específicos aplicados.",
                    charts = emptyList(),
                    tables = emptyList()
                )
            ),
            recommendations = listOf("Revisar datos filtrados según criterios seleccionados")
        )
    }
}

/**
 * Estado de UI para Analytics Avanzados
 */
data class AdvancedAnalyticsUiState(
    val isLoading: Boolean = false,
    val isGeneratingReport: Boolean = false,
    val error: String? = null,
    val lastUpdated: Date? = null,
    val lastReportGenerated: AdvancedReport? = null,
    val selectedAnalysisType: AnalysisType = AnalysisType.OVERVIEW
)

enum class AnalysisType {
    OVERVIEW,
    PATTERNS,
    PREDICTIONS,
    PERFORMANCE,
    WORKLOAD,
    BOTTLENECKS,
    REPORTS
}