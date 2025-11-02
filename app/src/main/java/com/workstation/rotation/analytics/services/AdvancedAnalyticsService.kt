package com.workstation.rotation.analytics.services

import com.workstation.rotation.analytics.models.*
import com.workstation.rotation.data.entities.Worker
import com.workstation.rotation.data.entities.Workstation
import com.workstation.rotation.data.entities.RotationHistory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.math.*
import kotlin.random.Random

/**
 * Servicio de Analytics Avanzados
 * Implementa análisis predictivo, detección de patrones y métricas ML básicas
 */
class AdvancedAnalyticsService {

    // Análisis de Patrones de Rotación
    suspend fun analyzeRotationPatterns(
        rotationHistory: List<RotationHistory>,
        workers: List<Worker>,
        workstations: List<Workstation>
    ): List<RotationPattern> = withContext(Dispatchers.Default) {
        val patterns = mutableListOf<RotationPattern>()
        
        // Detectar secuencias óptimas
        patterns.addAll(detectOptimalSequences(rotationHistory))
        
        // Identificar cuellos de botella
        patterns.addAll(detectBottlenecks(rotationHistory, workstations))
        
        // Encontrar patrones de alta eficiencia
        patterns.addAll(detectHighEfficiencyPatterns(rotationHistory, workers))
        
        // Detectar desajustes de habilidades
        patterns.addAll(detectSkillMismatches(rotationHistory, workers, workstations))
        
        patterns
    }

    private fun detectOptimalSequences(history: List<RotationHistory>): List<RotationPattern> {
        val patterns = mutableListOf<RotationPattern>()
        
        // Agrupar por trabajador y analizar secuencias
        val workerSequences = history.groupBy { it.workerId }
        
        workerSequences.forEach { (workerId, rotations) ->
            val sortedRotations = rotations.sortedBy { it.startTime }
            
            // Buscar secuencias con alta eficiencia
            for (i in 0 until sortedRotations.size - 2) {
                val sequence = sortedRotations.subList(i, i + 3)
                val avgEfficiency = sequence.map { calculateEfficiency(it) }.average()
                
                if (avgEfficiency > 0.8) {
                    sequence.forEach { rotation ->
                        patterns.add(
                            RotationPattern(
                                patternType = PatternType.OPTIMAL_SEQUENCE,
                                workstationId = rotation.workstationId,
                                workerId = workerId,
                                frequency = calculateFrequency(workerId, rotation.workstationId, history),
                                efficiency = avgEfficiency,
                                confidence = 0.85,
                                metadata = mapOf(
                                    "sequenceLength" to 3,
                                    "avgDuration" to sequence.map { it.duration }.average()
                                )
                            )
                        )
                    }
                }
            }
        }
        
        return patterns
    }

    private fun detectBottlenecks(
        history: List<RotationHistory>,
        workstations: List<Workstation>
    ): List<RotationPattern> {
        val patterns = mutableListOf<RotationPattern>()
        
        workstations.forEach { workstation ->
            val stationRotations = history.filter { it.workstationId == workstation.id }
            val avgDuration = stationRotations.map { it.duration }.average()
            val maxDuration = stationRotations.maxOfOrNull { it.duration } ?: 0L
            
            // Si la duración promedio es significativamente mayor que otras estaciones
            if (avgDuration > 0 && maxDuration > avgDuration * 1.5) {
                patterns.add(
                    RotationPattern(
                        patternType = PatternType.BOTTLENECK,
                        workstationId = workstation.id,
                        workerId = 0, // Aplica a todos los trabajadores
                        frequency = stationRotations.size.toDouble() / history.size,
                        efficiency = 1.0 - (avgDuration / maxDuration),
                        confidence = 0.75,
                        metadata = mapOf(
                            "avgDuration" to avgDuration,
                            "maxDuration" to maxDuration,
                            "rotationCount" to stationRotations.size
                        )
                    )
                )
            }
        }
        
        return patterns
    }

    private fun detectHighEfficiencyPatterns(
        history: List<RotationHistory>,
        workers: List<Worker>
    ): List<RotationPattern> {
        val patterns = mutableListOf<RotationPattern>()
        
        workers.forEach { worker ->
            val workerRotations = history.filter { it.workerId == worker.id }
            val efficiencies = workerRotations.map { calculateEfficiency(it) }
            
            if (efficiencies.isNotEmpty()) {
                val avgEfficiency = efficiencies.average()
                val consistency = 1.0 - efficiencies.map { abs(it - avgEfficiency) }.average()
                
                if (avgEfficiency > 0.85 && consistency > 0.8) {
                    workerRotations.forEach { rotation ->
                        patterns.add(
                            RotationPattern(
                                patternType = PatternType.HIGH_EFFICIENCY,
                                workstationId = rotation.workstationId,
                                workerId = worker.id,
                                frequency = 1.0,
                                efficiency = avgEfficiency,
                                confidence = consistency,
                                metadata = mapOf(
                                    "consistency" to consistency,
                                    "rotationCount" to workerRotations.size
                                )
                            )
                        )
                    }
                }
            }
        }
        
        return patterns
    }

    private fun detectSkillMismatches(
        history: List<RotationHistory>,
        workers: List<Worker>,
        workstations: List<Workstation>
    ): List<RotationPattern> {
        val patterns = mutableListOf<RotationPattern>()
        
        // Simular detección de desajustes basado en eficiencia baja
        history.forEach { rotation ->
            val efficiency = calculateEfficiency(rotation)
            
            if (efficiency < 0.6) {
                patterns.add(
                    RotationPattern(
                        patternType = PatternType.SKILL_MISMATCH,
                        workstationId = rotation.workstationId,
                        workerId = rotation.workerId,
                        frequency = 0.3,
                        efficiency = efficiency,
                        confidence = 0.7,
                        metadata = mapOf(
                            "lowEfficiencyIndicator" to true,
                            "suggestedTraining" to "Capacitación específica requerida"
                        )
                    )
                )
            }
        }
        
        return patterns
    }

    // Predicciones de Rotación
    suspend fun generateRotationPredictions(
        workers: List<Worker>,
        workstations: List<Workstation>,
        history: List<RotationHistory>,
        daysAhead: Int = 7
    ): List<RotationPrediction> = withContext(Dispatchers.Default) {
        val predictions = mutableListOf<RotationPrediction>()
        val calendar = Calendar.getInstance()
        
        repeat(daysAhead) { day ->
            calendar.add(Calendar.DAY_OF_MONTH, 1)
            val targetDate = calendar.time
            
            workstations.forEach { workstation ->
                val prediction = predictOptimalWorker(workstation, workers, history, targetDate)
                predictions.add(prediction)
            }
        }
        
        predictions
    }

    private fun predictOptimalWorker(
        workstation: Workstation,
        workers: List<Worker>,
        history: List<RotationHistory>,
        targetDate: Date
    ): RotationPrediction {
        // Algoritmo de predicción basado en historial y patrones
        val stationHistory = history.filter { it.workstationId == workstation.id }
        
        val workerScores = workers.map { worker ->
            val workerHistory = stationHistory.filter { it.workerId == worker.id }
            val efficiency = if (workerHistory.isNotEmpty()) {
                workerHistory.map { calculateEfficiency(it) }.average()
            } else {
                0.5 // Puntuación neutral para trabajadores sin historial
            }
            
            val frequency = workerHistory.size.toDouble() / stationHistory.size.coerceAtLeast(1)
            val recency = calculateRecency(worker.id, workstation.id, history)
            
            // Puntuación combinada
            val score = (efficiency * 0.5) + (frequency * 0.3) + (recency * 0.2)
            
            worker to score
        }
        
        val bestWorker = workerScores.maxByOrNull { it.second }?.first
            ?: workers.random()
        
        val confidence = workerScores.maxOfOrNull { it.second } ?: 0.5
        val riskFactors = generateRiskFactors(bestWorker, workstation, history)
        
        return RotationPrediction(
            targetDate = targetDate,
            workstationId = workstation.id,
            predictedWorkerId = bestWorker.id,
            confidence = confidence,
            expectedDuration = calculateExpectedDuration(bestWorker.id, workstation.id, history),
            riskFactors = riskFactors,
            recommendations = generateRecommendations(bestWorker, workstation, riskFactors)
        )
    }

    // Análisis de Carga de Trabajo
    suspend fun analyzeWorkload(
        workstations: List<Workstation>,
        history: List<RotationHistory>
    ): List<WorkloadAnalysis> = withContext(Dispatchers.Default) {
        workstations.map { workstation ->
            val stationHistory = history.filter { it.workstationId == workstation.id }
            
            val currentLoad = calculateCurrentLoad(stationHistory)
            val optimalLoad = calculateOptimalLoad(workstation, stationHistory)
            val utilizationRate = if (optimalLoad > 0) currentLoad / optimalLoad else 0.0
            val bottleneckScore = calculateBottleneckScore(stationHistory)
            
            WorkloadAnalysis(
                workstationId = workstation.id,
                analysisDate = Date(),
                currentLoad = currentLoad,
                optimalLoad = optimalLoad,
                utilizationRate = utilizationRate,
                bottleneckScore = bottleneckScore,
                recommendations = generateWorkloadRecommendations(
                    currentLoad, optimalLoad, utilizationRate, bottleneckScore
                )
            )
        }
    }

    // Métricas de Rendimiento Individual
    suspend fun analyzeWorkerPerformance(
        workers: List<Worker>,
        history: List<RotationHistory>
    ): List<WorkerPerformanceMetrics> = withContext(Dispatchers.Default) {
        workers.map { worker ->
            val workerHistory = history.filter { it.workerId == worker.id }
            
            if (workerHistory.isEmpty()) {
                // Trabajador sin historial
                WorkerPerformanceMetrics(
                    workerId = worker.id,
                    analysisDate = Date(),
                    overallScore = 5.0,
                    efficiencyScore = 5.0,
                    adaptabilityScore = 5.0,
                    consistencyScore = 5.0,
                    skillUtilization = 0.5,
                    improvementAreas = listOf(
                        ImprovementArea(
                            skill = "Experiencia General",
                            currentLevel = 3.0,
                            targetLevel = 7.0,
                            trainingRecommendations = listOf("Capacitación inicial", "Mentoring")
                        )
                    ),
                    strengths = listOf("Potencial de crecimiento"),
                    trendDirection = TrendDirection.STABLE
                )
            } else {
                val efficiencies = workerHistory.map { calculateEfficiency(it) }
                val efficiencyScore = efficiencies.average() * 10
                val consistencyScore = (1.0 - efficiencies.map { abs(it - efficiencies.average()) }.average()) * 10
                val adaptabilityScore = calculateAdaptabilityScore(workerHistory)
                val skillUtilization = calculateSkillUtilization(worker, workerHistory)
                
                val overallScore = (efficiencyScore + consistencyScore + adaptabilityScore + skillUtilization) / 4
                
                WorkerPerformanceMetrics(
                    workerId = worker.id,
                    analysisDate = Date(),
                    overallScore = overallScore,
                    efficiencyScore = efficiencyScore,
                    adaptabilityScore = adaptabilityScore,
                    consistencyScore = consistencyScore,
                    skillUtilization = skillUtilization,
                    improvementAreas = generateImprovementAreas(worker, workerHistory),
                    strengths = generateStrengths(efficiencyScore, consistencyScore, adaptabilityScore),
                    trendDirection = calculateTrendDirection(workerHistory)
                )
            }
        }
    }

    // Funciones auxiliares de cálculo
    private fun calculateEfficiency(rotation: RotationHistory): Double {
        // Simular cálculo de eficiencia basado en duración y otros factores
        val baseDuration = 480L // 8 horas en minutos
        val efficiency = when {
            rotation.duration <= baseDuration * 0.8 -> 0.9 + Random.nextDouble(0.1)
            rotation.duration <= baseDuration -> 0.7 + Random.nextDouble(0.2)
            rotation.duration <= baseDuration * 1.2 -> 0.5 + Random.nextDouble(0.2)
            else -> 0.3 + Random.nextDouble(0.2)
        }
        return efficiency.coerceIn(0.0, 1.0)
    }

    private fun calculateFrequency(workerId: Long, workstationId: Long, history: List<RotationHistory>): Double {
        val workerStationRotations = history.count { it.workerId == workerId && it.workstationId == workstationId }
        val totalWorkerRotations = history.count { it.workerId == workerId }
        return if (totalWorkerRotations > 0) {
            workerStationRotations.toDouble() / totalWorkerRotations
        } else 0.0
    }

    private fun calculateRecency(workerId: Long, workstationId: Long, history: List<RotationHistory>): Double {
        val lastRotation = history
            .filter { it.workerId == workerId && it.workstationId == workstationId }
            .maxByOrNull { it.startTime }
        
        return if (lastRotation != null) {
            val daysSince = (Date().time - lastRotation.startTime.time) / (1000 * 60 * 60 * 24)
            (1.0 / (1.0 + daysSince * 0.1)).coerceIn(0.0, 1.0)
        } else 0.0
    }

    private fun calculateExpectedDuration(workerId: Long, workstationId: Long, history: List<RotationHistory>): Long {
        val relevantHistory = history.filter { it.workerId == workerId && it.workstationId == workstationId }
        return if (relevantHistory.isNotEmpty()) {
            relevantHistory.map { it.duration }.average().toLong()
        } else {
            480L // 8 horas por defecto
        }
    }

    private fun generateRiskFactors(
        worker: Worker,
        workstation: Workstation,
        history: List<RotationHistory>
    ): List<RiskFactor> {
        val risks = mutableListOf<RiskFactor>()
        
        // Simular factores de riesgo basados en datos
        val workerHistory = history.filter { it.workerId == worker.id }
        
        if (workerHistory.isEmpty()) {
            risks.add(
                RiskFactor(
                    type = RiskType.SKILL_GAP,
                    severity = RiskSeverity.MEDIUM,
                    description = "Trabajador sin experiencia previa en esta estación",
                    impact = 0.3
                )
            )
        }
        
        // Agregar más factores de riesgo según lógica de negocio
        if (Random.nextDouble() < 0.3) {
            risks.add(
                RiskFactor(
                    type = RiskType.WORKLOAD_IMBALANCE,
                    severity = RiskSeverity.LOW,
                    description = "Posible desbalance de carga de trabajo",
                    impact = 0.15
                )
            )
        }
        
        return risks
    }

    private fun generateRecommendations(
        worker: Worker,
        workstation: Workstation,
        riskFactors: List<RiskFactor>
    ): List<String> {
        val recommendations = mutableListOf<String>()
        
        riskFactors.forEach { risk ->
            when (risk.type) {
                RiskType.SKILL_GAP -> recommendations.add("Proporcionar capacitación específica antes de la asignación")
                RiskType.FATIGUE_RISK -> recommendations.add("Considerar descansos adicionales")
                RiskType.WORKLOAD_IMBALANCE -> recommendations.add("Redistribuir tareas para equilibrar la carga")
                else -> recommendations.add("Monitorear de cerca el rendimiento")
            }
        }
        
        if (recommendations.isEmpty()) {
            recommendations.add("Asignación óptima - proceder según lo planificado")
        }
        
        return recommendations
    }

    private fun calculateCurrentLoad(stationHistory: List<RotationHistory>): Double {
        if (stationHistory.isEmpty()) return 0.0
        
        val totalDuration = stationHistory.sumOf { it.duration }
        val avgDuration = totalDuration.toDouble() / stationHistory.size
        
        // Normalizar a escala 0-1
        return (avgDuration / (8 * 60)).coerceIn(0.0, 1.0) // 8 horas = carga completa
    }

    private fun calculateOptimalLoad(workstation: Workstation, history: List<RotationHistory>): Double {
        // Calcular carga óptima basada en capacidad y historial
        return 0.8 // 80% de capacidad como óptimo
    }

    private fun calculateBottleneckScore(stationHistory: List<RotationHistory>): Double {
        if (stationHistory.isEmpty()) return 0.0
        
        val durations = stationHistory.map { it.duration }
        val avgDuration = durations.average()
        val maxDuration = durations.maxOrNull() ?: 0L
        
        return if (maxDuration > 0) {
            (avgDuration / maxDuration).coerceIn(0.0, 1.0)
        } else 0.0
    }

    private fun generateWorkloadRecommendations(
        currentLoad: Double,
        optimalLoad: Double,
        utilizationRate: Double,
        bottleneckScore: Double
    ): List<WorkloadRecommendation> {
        val recommendations = mutableListOf<WorkloadRecommendation>()
        
        when {
            utilizationRate > 1.2 -> {
                recommendations.add(
                    WorkloadRecommendation(
                        type = RecommendationType.INCREASE_WORKERS,
                        priority = Priority.HIGH,
                        description = "Sobrecarga detectada - considerar agregar más trabajadores",
                        expectedImprovement = 25.0
                    )
                )
            }
            utilizationRate < 0.6 -> {
                recommendations.add(
                    WorkloadRecommendation(
                        type = RecommendationType.REDISTRIBUTE_TASKS,
                        priority = Priority.MEDIUM,
                        description = "Subutilización - redistribuir tareas para optimizar recursos",
                        expectedImprovement = 15.0
                    )
                )
            }
            bottleneckScore < 0.7 -> {
                recommendations.add(
                    WorkloadRecommendation(
                        type = RecommendationType.PROCESS_OPTIMIZATION,
                        priority = Priority.MEDIUM,
                        description = "Cuello de botella identificado - optimizar procesos",
                        expectedImprovement = 20.0
                    )
                )
            }
        }
        
        return recommendations
    }

    private fun calculateAdaptabilityScore(workerHistory: List<RotationHistory>): Double {
        val uniqueStations = workerHistory.map { it.workstationId }.distinct().size
        val totalRotations = workerHistory.size
        
        return if (totalRotations > 0) {
            (uniqueStations.toDouble() / totalRotations * 10).coerceAtMost(10.0)
        } else 5.0
    }

    private fun calculateSkillUtilization(worker: Worker, workerHistory: List<RotationHistory>): Double {
        // Simular utilización de habilidades basada en variedad de estaciones
        val uniqueStations = workerHistory.map { it.workstationId }.distinct().size
        return (uniqueStations * 2.0).coerceAtMost(10.0)
    }

    private fun generateImprovementAreas(worker: Worker, workerHistory: List<RotationHistory>): List<ImprovementArea> {
        val areas = mutableListOf<ImprovementArea>()
        
        val efficiencies = workerHistory.map { calculateEfficiency(it) }
        val avgEfficiency = efficiencies.average()
        
        if (avgEfficiency < 0.7) {
            areas.add(
                ImprovementArea(
                    skill = "Eficiencia Operativa",
                    currentLevel = avgEfficiency * 10,
                    targetLevel = 8.0,
                    trainingRecommendations = listOf(
                        "Capacitación en mejores prácticas",
                        "Mentoring con trabajadores experimentados"
                    )
                )
            )
        }
        
        val uniqueStations = workerHistory.map { it.workstationId }.distinct().size
        if (uniqueStations < 3) {
            areas.add(
                ImprovementArea(
                    skill = "Versatilidad",
                    currentLevel = uniqueStations * 2.0,
                    targetLevel = 8.0,
                    trainingRecommendations = listOf(
                        "Capacitación cruzada en múltiples estaciones",
                        "Rotación programada para ganar experiencia"
                    )
                )
            )
        }
        
        return areas
    }

    private fun generateStrengths(
        efficiencyScore: Double,
        consistencyScore: Double,
        adaptabilityScore: Double
    ): List<String> {
        val strengths = mutableListOf<String>()
        
        if (efficiencyScore > 8.0) strengths.add("Alta eficiencia operativa")
        if (consistencyScore > 8.0) strengths.add("Rendimiento consistente")
        if (adaptabilityScore > 7.0) strengths.add("Buena adaptabilidad")
        
        if (strengths.isEmpty()) {
            strengths.add("Potencial de mejora identificado")
        }
        
        return strengths
    }

    private fun calculateTrendDirection(workerHistory: List<RotationHistory>): TrendDirection {
        if (workerHistory.size < 3) return TrendDirection.STABLE
        
        val recentHistory = workerHistory.sortedBy { it.startTime }.takeLast(5)
        val recentEfficiencies = recentHistory.map { calculateEfficiency(it) }
        
        val trend = recentEfficiencies.zipWithNext { a, b -> b - a }.average()
        
        return when {
            trend > 0.05 -> TrendDirection.IMPROVING
            trend < -0.05 -> TrendDirection.DECLINING
            else -> TrendDirection.STABLE
        }
    }
}