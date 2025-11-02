package com.workstation.rotation.services

import com.workstation.rotation.data.dao.ReportsDao
import com.workstation.rotation.data.dao.WorkerDao
import com.workstation.rotation.data.dao.WorkstationDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

/**
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 📊 SERVICIO DE REPORTES Y MÉTRICAS CON SQL OPTIMIZADO
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 
 * Este servicio genera reportes detallados de métricas por trabajador utilizando
 * consultas SQL nativas para máximo rendimiento y precisión.
 * 
 * CARACTERÍSTICAS PRINCIPALES:
 * 1. Métricas individuales por trabajador
 * 2. Porcentaje de permanencia por estación
 * 3. Análisis de distribución de roles
 * 4. Estadísticas de utilización del sistema
 * 5. Reportes exportables en múltiples formatos
 * 
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 */
class ReportsService(
    private val reportsDao: ReportsDao,
    private val workerDao: WorkerDao,
    private val workstationDao: WorkstationDao
) {
    
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    
    /**
     * Genera un reporte completo de métricas para un trabajador específico.
     */
    suspend fun generateWorkerReport(workerId: Long): WorkerReport = withContext(Dispatchers.IO) {
        
        // Obtener métricas básicas del trabajador
        val workerMetrics = reportsDao.getWorkerMetrics(workerId)
            ?: throw Exception("Trabajador no encontrado o inactivo")
        
        // Obtener métricas por estación
        val stationMetrics = reportsDao.getWorkerStationMetrics(workerId)
        
        // Calcular porcentajes de permanencia
        val stationPercentages = calculateStationPercentages(stationMetrics)
        
        // Generar análisis y recomendaciones
        val analysis = generateWorkerAnalysis(workerMetrics, stationMetrics)
        
        return@withContext WorkerReport(
            workerId = workerId,
            workerName = workerMetrics.name,
            workerEmail = workerMetrics.email,
            generatedAt = Date(),
            basicMetrics = WorkerBasicMetrics(
                availabilityPercentage = workerMetrics.availabilityPercentage,
                totalStationsAssigned = workerMetrics.totalStationsAssigned,
                currentRotations = workerMetrics.rotationsInCurrentStation,
                workerType = determineWorkerType(workerMetrics),
                hasRestrictions = workerMetrics.restrictionNotes.isNotEmpty(),
                restrictionNotes = workerMetrics.restrictionNotes
            ),
            stationMetrics = stationPercentages,
            roleAnalysis = RoleAnalysis(
                isLeader = workerMetrics.isLeader,
                leadershipType = workerMetrics.leadershipType,
                leadershipStations = stationMetrics.filter { it.isLeadershipStation == 1 }.map { it.stationName },
                isTrainer = workerMetrics.isTrainer,
                isTrainee = workerMetrics.isTrainee,
                trainingStations = stationMetrics.filter { it.isTrainingStation == 1 }.map { it.stationName }
            ),
            performanceAnalysis = analysis,
            recommendations = generateRecommendations(workerMetrics, stationMetrics, analysis)
        )
    }
    
    /**
     * Genera un reporte de todos los trabajadores con métricas resumidas.
     */
    suspend fun generateAllWorkersReport(): AllWorkersReport = withContext(Dispatchers.IO) {
        
        val workersMetrics = reportsDao.getAllWorkersMetrics()
        val roleDistribution = reportsDao.getRoleDistributionMetrics()
        val stationUtilization = reportsDao.getStationUtilizationMetrics()
        val systemEfficiency = reportsDao.getSystemEfficiencyMetrics()
        
        val workerSummaries = workersMetrics.map { worker ->
            WorkerSummary(
                id = worker.id,
                name = worker.name,
                email = worker.email,
                workerType = worker.workerType,
                availabilityPercentage = worker.availabilityPercentage,
                totalStationsAssigned = worker.totalStationsAssigned,
                currentRotations = worker.rotationsInCurrentStation,
                hasRestrictions = worker.hasRestrictions == 1,
                utilizationScore = calculateWorkerUtilizationScore(worker)
            )
        }
        
        return@withContext AllWorkersReport(
            generatedAt = Date(),
            totalWorkers = workersMetrics.size,
            workerSummaries = workerSummaries,
            roleDistribution = roleDistribution.associate { it.category to it.percentage },
            stationUtilization = stationUtilization.map { 
                StationUtilizationSummary(
                    stationName = it.stationName,
                    utilizationPercentage = it.utilizationPercentage,
                    assignedWorkers = it.assignedWorkers,
                    requiredWorkers = it.requiredWorkers,
                    isPriority = it.isPriority
                )
            },
            systemMetrics = SystemMetrics(
                totalActiveWorkers = systemEfficiency.totalActiveWorkers,
                totalActiveStations = systemEfficiency.totalActiveStations,
                totalRequiredCapacity = systemEfficiency.totalRequiredCapacity,
                workersWithStations = systemEfficiency.workersWithStations,
                averageAvailability = systemEfficiency.averageAvailability,
                systemUtilization = calculateSystemUtilization(systemEfficiency),
                coveragePercentage = calculateCoveragePercentage(systemEfficiency)
            )
        )
    }
    
    /**
     * Calcula los porcentajes de permanencia por estación.
     */
    private fun calculateStationPercentages(stationMetrics: List<com.workstation.rotation.data.dao.WorkerStationMetric>): List<StationPercentage> {
        val totalRotations = stationMetrics.sumOf { it.rotationsInStation }
        
        return stationMetrics.map { metric ->
            val percentage = if (totalRotations > 0) {
                (metric.rotationsInStation.toDouble() / totalRotations) * 100
            } else {
                // Si no hay historial, calcular basado en capacidad de trabajar
                if (metric.canWork == 1) {
                    // Distribución equitativa entre estaciones donde puede trabajar
                    val eligibleStations = stationMetrics.count { it.canWork == 1 }
                    if (eligibleStations > 0) 100.0 / eligibleStations else 0.0
                } else 0.0
            }
            
            StationPercentage(
                stationId = metric.stationId,
                stationName = metric.stationName,
                permanencePercentage = percentage,
                rotationCount = metric.rotationsInStation,
                canWork = metric.canWork == 1,
                isPriority = metric.isPriority,
                isLeadershipStation = metric.isLeadershipStation == 1,
                isTrainingStation = metric.isTrainingStation == 1,
                requiredWorkers = metric.requiredWorkers
            )
        }.sortedByDescending { it.permanencePercentage }
    }
    
    /**
     * Genera análisis de rendimiento del trabajador.
     */
    private fun generateWorkerAnalysis(
        workerMetrics: com.workstation.rotation.data.dao.WorkerMetricsResult,
        stationMetrics: List<com.workstation.rotation.data.dao.WorkerStationMetric>
    ): PerformanceAnalysis {
        
        val eligibleStations = stationMetrics.count { it.canWork == 1 }
        val priorityStations = stationMetrics.count { it.canWork == 1 && it.isPriority }
        val specialRoles = listOfNotNull(
            if (workerMetrics.isLeader) "Líder" else null,
            if (workerMetrics.isTrainer) "Entrenador" else null,
            if (workerMetrics.isTrainee) "Entrenado" else null
        )
        
        val versatilityScore = calculateVersatilityScore(eligibleStations, stationMetrics.size)
        val availabilityScore = workerMetrics.availabilityPercentage.toDouble()
        val specialRoleScore = if (specialRoles.isNotEmpty()) 20.0 else 0.0
        val restrictionPenalty = if (workerMetrics.restrictionNotes.isNotEmpty()) -10.0 else 0.0
        
        val overallScore = (versatilityScore + availabilityScore + specialRoleScore + restrictionPenalty) / 4
        
        return PerformanceAnalysis(
            versatilityScore = versatilityScore,
            availabilityScore = availabilityScore,
            overallScore = overallScore.coerceIn(0.0, 100.0),
            eligibleStationsCount = eligibleStations,
            priorityStationsAccess = priorityStations,
            specialRoles = specialRoles,
            performanceLevel = when {
                overallScore >= 90 -> "EXCELENTE"
                overallScore >= 75 -> "BUENO"
                overallScore >= 60 -> "REGULAR"
                else -> "NECESITA MEJORA"
            }
        )
    }
    
    /**
     * Calcula el puntaje de versatilidad basado en estaciones asignadas.
     */
    private fun calculateVersatilityScore(eligibleStations: Int, totalStations: Int): Double {
        return if (totalStations > 0) {
            (eligibleStations.toDouble() / totalStations) * 100
        } else 0.0
    }
    
    /**
     * Determina el tipo de trabajador basado en sus roles.
     */
    private fun determineWorkerType(workerMetrics: com.workstation.rotation.data.dao.WorkerMetricsResult): String {
        return when {
            workerMetrics.isLeader -> "LÍDER ${workerMetrics.leadershipType ?: ""}"
            workerMetrics.isTrainer -> "ENTRENADOR"
            workerMetrics.isTrainee -> "ENTRENADO"
            else -> "REGULAR"
        }
    }
    
    /**
     * Genera recomendaciones personalizadas para el trabajador.
     */
    private fun generateRecommendations(
        workerMetrics: com.workstation.rotation.data.dao.WorkerMetricsResult,
        stationMetrics: List<com.workstation.rotation.data.dao.WorkerStationMetric>,
        analysis: PerformanceAnalysis
    ): List<String> {
        val recommendations = mutableListOf<String>()
        
        // Recomendaciones basadas en disponibilidad
        if (workerMetrics.availabilityPercentage < 80) {
            recommendations.add("⚠️ Considerar revisar la disponibilidad del trabajador (${workerMetrics.availabilityPercentage}%)")
        }
        
        // Recomendaciones basadas en versatilidad
        if (analysis.eligibleStationsCount < stationMetrics.size * 0.5) {
            recommendations.add("📈 Considerar capacitar al trabajador en más estaciones para mayor versatilidad")
        }
        
        // Recomendaciones para líderes
        if (workerMetrics.isLeader && analysis.priorityStationsAccess == 0) {
            recommendations.add("👑 Como líder, considerar asignar acceso a estaciones prioritarias")
        }
        
        // Recomendaciones para entrenados
        if (workerMetrics.isTrainee && workerMetrics.rotationsInCurrentStation > 10) {
            recommendations.add("🎓 El trabajador podría estar listo para certificación (${workerMetrics.rotationsInCurrentStation} rotaciones)")
        }
        
        // Recomendaciones basadas en restricciones
        if (workerMetrics.restrictionNotes.isNotEmpty()) {
            recommendations.add("♿ Revisar si las restricciones actuales siguen siendo necesarias")
        }
        
        // Recomendaciones de rendimiento
        when (analysis.performanceLevel) {
            "NECESITA MEJORA" -> {
                recommendations.add("🔧 Considerar plan de mejora: capacitación adicional o revisión de asignaciones")
            }
            "EXCELENTE" -> {
                recommendations.add("⭐ Trabajador de alto rendimiento - considerar para roles de liderazgo o entrenamiento")
            }
        }
        
        return recommendations
    }
    
    /**
     * Calcula el puntaje de utilización de un trabajador.
     */
    private fun calculateWorkerUtilizationScore(worker: com.workstation.rotation.data.dao.WorkerSummaryMetric): Double {
        val baseScore = worker.availabilityPercentage.toDouble()
        val versatilityBonus = (worker.totalStationsAssigned * 5).coerceAtMost(20)
        val roleBonus = when {
            worker.isLeader -> 15
            worker.isTrainer -> 10
            worker.isTrainee -> 5
            else -> 0
        }
        val restrictionPenalty = if (worker.hasRestrictions == 1) -10 else 0
        
        return (baseScore + versatilityBonus + roleBonus + restrictionPenalty).coerceIn(0.0, 100.0)
    }
    
    /**
     * Calcula la utilización general del sistema.
     */
    private fun calculateSystemUtilization(systemMetrics: com.workstation.rotation.data.dao.SystemEfficiencyMetric): Double {
        return if (systemMetrics.totalRequiredCapacity > 0) {
            (systemMetrics.workersWithStations.toDouble() / systemMetrics.totalRequiredCapacity) * 100
        } else 0.0
    }
    
    /**
     * Calcula el porcentaje de cobertura del sistema.
     */
    private fun calculateCoveragePercentage(systemMetrics: com.workstation.rotation.data.dao.SystemEfficiencyMetric): Double {
        return if (systemMetrics.totalActiveWorkers > 0) {
            (systemMetrics.workersWithStations.toDouble() / systemMetrics.totalActiveWorkers) * 100
        } else 0.0
    }
    
    /**
     * Exporta un reporte de trabajador a texto formateado.
     */
    fun exportWorkerReportToText(report: WorkerReport): String {
        return buildString {
            appendLine("📊 REPORTE INDIVIDUAL DE TRABAJADOR")
            appendLine("═".repeat(50))
            appendLine("👤 Trabajador: ${report.workerName}")
            appendLine("📧 Email: ${report.workerEmail}")
            appendLine("📅 Generado: ${dateFormat.format(report.generatedAt)}")
            appendLine()
            
            appendLine("📈 MÉTRICAS BÁSICAS")
            appendLine("-".repeat(30))
            appendLine("Tipo: ${report.basicMetrics.workerType}")
            appendLine("Disponibilidad: ${report.basicMetrics.availabilityPercentage}%")
            appendLine("Estaciones asignadas: ${report.basicMetrics.totalStationsAssigned}")
            appendLine("Rotaciones actuales: ${report.basicMetrics.currentRotations}")
            if (report.basicMetrics.hasRestrictions) {
                appendLine("⚠️ Restricciones: ${report.basicMetrics.restrictionNotes}")
            }
            appendLine()
            
            appendLine("🏭 PERMANENCIA POR ESTACIÓN")
            appendLine("-".repeat(30))
            report.stationMetrics.forEach { station ->
                val status = when {
                    station.isLeadershipStation -> " 👑"
                    station.isTrainingStation -> " 🎓"
                    station.isPriority -> " ⭐"
                    else -> ""
                }
                appendLine("${station.stationName}$status: ${String.format("%.1f%%", station.permanencePercentage)} (${station.rotationCount} rotaciones)")
            }
            appendLine()
            
            appendLine("🎯 ANÁLISIS DE RENDIMIENTO")
            appendLine("-".repeat(30))
            appendLine("Nivel: ${report.performanceAnalysis.performanceLevel}")
            appendLine("Puntaje general: ${String.format("%.1f", report.performanceAnalysis.overallScore)}/100")
            appendLine("Versatilidad: ${String.format("%.1f", report.performanceAnalysis.versatilityScore)}/100")
            appendLine("Disponibilidad: ${String.format("%.1f", report.performanceAnalysis.availabilityScore)}/100")
            if (report.performanceAnalysis.specialRoles.isNotEmpty()) {
                appendLine("Roles especiales: ${report.performanceAnalysis.specialRoles.joinToString(", ")}")
            }
            appendLine()
            
            if (report.recommendations.isNotEmpty()) {
                appendLine("💡 RECOMENDACIONES")
                appendLine("-".repeat(30))
                report.recommendations.forEach { recommendation ->
                    appendLine("• $recommendation")
                }
            }
        }
    }
}

/**
 * Data classes para los reportes generados.
 */
data class WorkerReport(
    val workerId: Long,
    val workerName: String,
    val workerEmail: String,
    val generatedAt: Date,
    val basicMetrics: WorkerBasicMetrics,
    val stationMetrics: List<StationPercentage>,
    val roleAnalysis: RoleAnalysis,
    val performanceAnalysis: PerformanceAnalysis,
    val recommendations: List<String>
)

data class WorkerBasicMetrics(
    val availabilityPercentage: Int,
    val totalStationsAssigned: Int,
    val currentRotations: Int,
    val workerType: String,
    val hasRestrictions: Boolean,
    val restrictionNotes: String
)

data class StationPercentage(
    val stationId: Long,
    val stationName: String,
    val permanencePercentage: Double,
    val rotationCount: Int,
    val canWork: Boolean,
    val isPriority: Boolean,
    val isLeadershipStation: Boolean,
    val isTrainingStation: Boolean,
    val requiredWorkers: Int
)

data class RoleAnalysis(
    val isLeader: Boolean,
    val leadershipType: String?,
    val leadershipStations: List<String>,
    val isTrainer: Boolean,
    val isTrainee: Boolean,
    val trainingStations: List<String>
)

data class PerformanceAnalysis(
    val versatilityScore: Double,
    val availabilityScore: Double,
    val overallScore: Double,
    val eligibleStationsCount: Int,
    val priorityStationsAccess: Int,
    val specialRoles: List<String>,
    val performanceLevel: String
)

data class AllWorkersReport(
    val generatedAt: Date,
    val totalWorkers: Int,
    val workerSummaries: List<WorkerSummary>,
    val roleDistribution: Map<String, Double>,
    val stationUtilization: List<StationUtilizationSummary>,
    val systemMetrics: SystemMetrics
)

data class WorkerSummary(
    val id: Long,
    val name: String,
    val email: String,
    val workerType: String,
    val availabilityPercentage: Int,
    val totalStationsAssigned: Int,
    val currentRotations: Int,
    val hasRestrictions: Boolean,
    val utilizationScore: Double
)

data class StationUtilizationSummary(
    val stationName: String,
    val utilizationPercentage: Double,
    val assignedWorkers: Int,
    val requiredWorkers: Int,
    val isPriority: Boolean
)

data class SystemMetrics(
    val totalActiveWorkers: Int,
    val totalActiveStations: Int,
    val totalRequiredCapacity: Int,
    val workersWithStations: Int,
    val averageAvailability: Double,
    val systemUtilization: Double,
    val coveragePercentage: Double
)