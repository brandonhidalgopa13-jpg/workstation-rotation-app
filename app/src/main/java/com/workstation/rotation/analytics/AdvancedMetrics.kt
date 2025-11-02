package com.workstation.rotation.analytics

import com.workstation.rotation.data.entities.Worker
import com.workstation.rotation.data.entities.Workstation
import com.workstation.rotation.models.BenchmarkResult
import com.workstation.rotation.models.RotationItem
import kotlin.math.sqrt

/**
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 📊 SISTEMA DE MÉTRICAS AVANZADAS
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 
 * Calcula métricas sofisticadas para evaluar la calidad de las rotaciones:
 * 1. Índice de Balanceamiento
 * 2. Eficiencia de Asignación
 * 3. Satisfacción de Restricciones
 * 4. Optimización de Capacidades
 * 5. Análisis de Rendimiento Comparativo
 * 
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 */
object AdvancedMetrics {
    
    /**
     * Calcula el índice de balanceamiento de la rotación.
     * Rango: 0.0 (desbalanceado) a 1.0 (perfectamente balanceado)
     */
    fun calculateBalanceIndex(
        rotationItems: List<RotationItem>,
        workstations: List<Workstation>
    ): Double {
        if (rotationItems.isEmpty() || workstations.isEmpty()) return 0.0
        
        val assignmentsByStation = rotationItems.groupBy { it.workstationId }
        val balanceScores = mutableListOf<Double>()
        
        for (station in workstations) {
            val assigned = assignmentsByStation[station.id]?.size ?: 0
            val required = station.requiredWorkers
            
            val score = if (required > 0) {
                minOf(assigned.toDouble() / required, 1.0)
            } else {
                if (assigned == 0) 1.0 else 0.0
            }
            
            balanceScores.add(score)
        }
        
        return balanceScores.average()
    }
    
    /**
     * Calcula la eficiencia de asignación.
     * Considera la utilización óptima de trabajadores y estaciones.
     */
    fun calculateAssignmentEfficiency(
        rotationItems: List<RotationItem>,
        workers: List<Worker>,
        workstations: List<Workstation>
    ): AssignmentEfficiency {
        val totalWorkers = workers.size
        val assignedWorkers = rotationItems.size
        val utilizationRate = if (totalWorkers > 0) assignedWorkers.toDouble() / totalWorkers else 0.0
        
        val totalCapacity = workstations.sumOf { it.requiredWorkers }
        val filledCapacity = rotationItems.size
        val capacityUtilization = if (totalCapacity > 0) filledCapacity.toDouble() / totalCapacity else 0.0
        
        val leadersCovered = rotationItems.count { it.isLeader }
        val totalLeaders = workers.count { it.isLeader }
        val leadershipCoverage = if (totalLeaders > 0) leadersCovered.toDouble() / totalLeaders else 1.0
        
        val trainingPairsCovered = rotationItems.count { it.isTrainee }
        val totalTrainees = workers.count { it.isTrainee }
        val trainingCoverage = if (totalTrainees > 0) trainingPairsCovered.toDouble() / totalTrainees else 1.0
        
        return AssignmentEfficiency(
            workerUtilization = utilizationRate,
            capacityUtilization = capacityUtilization,
            leadershipCoverage = leadershipCoverage,
            trainingCoverage = trainingCoverage,
            overallScore = (utilizationRate + capacityUtilization + leadershipCoverage + trainingCoverage) / 4.0
        )
    }
    
    /**
     * Analiza la satisfacción de restricciones.
     */
    fun analyzeConstraintSatisfaction(
        rotationItems: List<RotationItem>,
        workers: List<Worker>
    ): ConstraintSatisfaction {
        val violations = mutableListOf<String>()
        var satisfiedConstraints = 0
        var totalConstraints = 0
        
        // Verificar líderes en estaciones correctas
        val leadersInRotation = rotationItems.filter { it.isLeader }
        val leadersInSystem = workers.filter { it.isLeader }
        
        for (leader in leadersInSystem) {
            totalConstraints++
            val leaderAssignment = leadersInRotation.find { it.workerId == leader.id }
            
            if (leaderAssignment != null && leader.leaderWorkstationId == leaderAssignment.workstationId) {
                satisfiedConstraints++
            } else {
                violations.add("Líder ${leader.getDisplayName()} no está en su estación asignada")
            }
        }
        
        // Verificar parejas de entrenamiento
        val traineesInRotation = rotationItems.filter { it.isTrainee }
        val traineesInSystem = workers.filter { it.isTrainee }
        
        for (trainee in traineesInSystem) {
            totalConstraints++
            val traineeAssignment = traineesInRotation.find { it.workerId == trainee.id }
            
            if (traineeAssignment != null && trainee.trainingWorkstationId == traineeAssignment.workstationId) {
                satisfiedConstraints++
                
                // Verificar que el entrenador esté en la misma estación
                if (trainee.trainerId != null) {
                    val trainerAssignment = rotationItems.find { 
                        it.workerId == trainee.trainerId && it.workstationId == traineeAssignment.workstationId 
                    }
                    
                    if (trainerAssignment == null) {
                        violations.add("Pareja de entrenamiento separada: ${trainee.getDisplayName()}")
                    }
                }
            } else {
                violations.add("Entrenado ${trainee.getDisplayName()} no está en su estación de entrenamiento")
            }
        }
        
        val satisfactionRate = if (totalConstraints > 0) satisfiedConstraints.toDouble() / totalConstraints else 1.0
        
        return ConstraintSatisfaction(
            satisfactionRate = satisfactionRate,
            violationsCount = violations.size,
            violations = violations,
            totalConstraints = totalConstraints,
            satisfiedConstraints = satisfiedConstraints
        )
    }
    
    /**
     * Calcula métricas de calidad de la rotación.
     */
    fun calculateRotationQuality(
        rotationItems: List<RotationItem>,
        workers: List<Worker>,
        workstations: List<Workstation>
    ): RotationQuality {
        val balanceIndex = calculateBalanceIndex(rotationItems, workstations)
        val efficiency = calculateAssignmentEfficiency(rotationItems, workers, workstations)
        val constraints = analyzeConstraintSatisfaction(rotationItems, workers)
        
        // Calcular distribución de disponibilidad
        val availabilityScores = rotationItems.mapNotNull { rotation ->
            workers.find { it.id == rotation.workerId }?.availabilityPercentage
        }
        
        val avgAvailability = if (availabilityScores.isNotEmpty()) {
            availabilityScores.average() / 100.0
        } else 0.0
        
        // Calcular varianza de asignaciones por estación
        val assignmentsByStation = rotationItems.groupBy { it.workstationId }
        val assignmentCounts = workstations.map { station ->
            assignmentsByStation[station.id]?.size ?: 0
        }
        
        val avgAssignments = assignmentCounts.average()
        val variance = assignmentCounts.map { (it - avgAssignments) * (it - avgAssignments) }.average()
        val distributionUniformity = 1.0 / (1.0 + sqrt(variance))
        
        // Score general de calidad
        val qualityScore = (
            balanceIndex * 0.25 +
            efficiency.overallScore * 0.25 +
            constraints.satisfactionRate * 0.30 +
            avgAvailability * 0.10 +
            distributionUniformity * 0.10
        )
        
        return RotationQuality(
            overallScore = qualityScore,
            balanceIndex = balanceIndex,
            efficiency = efficiency,
            constraints = constraints,
            averageAvailability = avgAvailability,
            distributionUniformity = distributionUniformity,
            recommendations = generateRecommendations(balanceIndex, efficiency, constraints)
        )
    }
    
    /**
     * Compara dos resultados de benchmark de manera avanzada.
     */
    fun compareBenchmarkResults(
        result1: BenchmarkResult,
        result2: BenchmarkResult
    ): BenchmarkComparison {
        val timeImprovement = if (result1.executionTimeMs > 0) {
            ((result1.executionTimeMs - result2.executionTimeMs).toDouble() / result1.executionTimeMs) * 100
        } else 0.0
        
        val memoryImprovement = if (result1.memoryUsageMB > 0) {
            ((result1.memoryUsageMB - result2.memoryUsageMB) / result1.memoryUsageMB) * 100
        } else 0.0
        
        val assignmentsDiff = result2.assignmentsCount - result1.assignmentsCount
        val successRateDiff = result2.successRate - result1.successRate
        
        val winner = when {
            timeImprovement > 5 -> result2.algorithmName
            timeImprovement < -5 -> result1.algorithmName
            else -> "Empate"
        }
        
        return BenchmarkComparison(
            winner = winner,
            timeImprovementPercent = timeImprovement,
            memoryImprovementPercent = memoryImprovement,
            assignmentsDifference = assignmentsDiff,
            successRateDifference = successRateDiff,
            recommendation = generateBenchmarkRecommendation(timeImprovement, memoryImprovement, assignmentsDiff)
        )
    }
    
    /**
     * Genera recomendaciones basadas en las métricas.
     */
    private fun generateRecommendations(
        balanceIndex: Double,
        efficiency: AssignmentEfficiency,
        constraints: ConstraintSatisfaction
    ): List<String> {
        val recommendations = mutableListOf<String>()
        
        if (balanceIndex < 0.7) {
            recommendations.add("🔄 Considera redistribuir trabajadores para mejor balance")
        }
        
        if (efficiency.workerUtilization < 0.8) {
            recommendations.add("👥 Hay trabajadores sin asignar, revisa las restricciones")
        }
        
        if (efficiency.leadershipCoverage < 1.0) {
            recommendations.add("👑 Algunos líderes no están asignados a sus estaciones")
        }
        
        if (constraints.satisfactionRate < 0.9) {
            recommendations.add("⚠️ Hay violaciones de restricciones que requieren atención")
        }
        
        if (efficiency.capacityUtilization > 1.1) {
            recommendations.add("📊 Algunas estaciones están sobrecargadas")
        }
        
        if (recommendations.isEmpty()) {
            recommendations.add("✅ La rotación está bien optimizada")
        }
        
        return recommendations
    }
    
    /**
     * Genera recomendación para benchmark.
     */
    private fun generateBenchmarkRecommendation(
        timeImprovement: Double,
        memoryImprovement: Double,
        assignmentsDiff: Int
    ): String {
        return when {
            timeImprovement > 20 -> "🚀 Excelente mejora de rendimiento, usa este algoritmo"
            timeImprovement > 10 -> "⚡ Buena mejora de rendimiento"
            timeImprovement > 0 -> "📈 Ligera mejora de rendimiento"
            timeImprovement > -10 -> "⚖️ Rendimiento similar, elige según preferencia"
            else -> "🐌 Rendimiento inferior, considera el algoritmo alternativo"
        }
    }
}

/**
 * Data classes para métricas.
 */
data class AssignmentEfficiency(
    val workerUtilization: Double,
    val capacityUtilization: Double,
    val leadershipCoverage: Double,
    val trainingCoverage: Double,
    val overallScore: Double
)

data class ConstraintSatisfaction(
    val satisfactionRate: Double,
    val violationsCount: Int,
    val violations: List<String>,
    val totalConstraints: Int,
    val satisfiedConstraints: Int
)

data class RotationQuality(
    val overallScore: Double,
    val balanceIndex: Double,
    val efficiency: AssignmentEfficiency,
    val constraints: ConstraintSatisfaction,
    val averageAvailability: Double,
    val distributionUniformity: Double,
    val recommendations: List<String>
)

data class BenchmarkComparison(
    val winner: String,
    val timeImprovementPercent: Double,
    val memoryImprovementPercent: Double,
    val assignmentsDifference: Int,
    val successRateDifference: Double,
    val recommendation: String
)