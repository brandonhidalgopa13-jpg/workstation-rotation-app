package com.workstation.rotation.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import com.workstation.rotation.data.entities.Worker
import com.workstation.rotation.data.entities.Workstation
import com.workstation.rotation.models.RotationTable
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * Generador de reportes y estadísticas avanzadas
 */
class ReportGenerator(private val context: Context) {
    
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    
    /**
     * Genera un reporte completo de rotación
     */
    fun generateRotationReport(
        workers: List<Worker>,
        workstations: List<Workstation>,
        rotationTable: RotationTable?
    ): RotationReport {
        return RotationReport(
            timestamp = Date(),
            totalWorkers = workers.size,
            activeWorkers = workers.count { it.isActive },
            totalWorkstations = workstations.size,
            activeWorkstations = workstations.count { it.isActive },
            trainers = workers.count { it.isTrainer },
            trainees = workers.count { it.isTrainee },
            leaders = workers.count { it.isLeader },
            priorityWorkstations = workstations.count { it.isPriority },
            rotationEfficiency = calculateRotationEfficiency(workers, workstations, rotationTable),
            workstationUtilization = calculateWorkstationUtilization(workstations, rotationTable),
            workerDistribution = calculateWorkerDistribution(workers, rotationTable),
            leadershipDistribution = calculateLeadershipDistribution(workers, rotationTable)
        )
    }
    
    /**
     * Genera estadísticas de rendimiento
     */
    fun generatePerformanceStats(
        workers: List<Worker>,
        workstations: List<Workstation>
    ): PerformanceStats {
        val avgAvailability = workers.map { it.availabilityPercentage }.average()
        val capacityUtilization = calculateCapacityUtilization(workers, workstations)
        
        return PerformanceStats(
            averageAvailability = avgAvailability,
            capacityUtilization = capacityUtilization,
            trainingCoverage = calculateTrainingCoverage(workers),
            leadershipCoverage = calculateLeadershipCoverage(workers, workstations),
            restrictionImpact = calculateRestrictionImpact(workers),
            recommendations = generateRecommendations(workers, workstations)
        )
    }
    
    /**
     * Exporta reporte a texto
     */
    fun exportReportToText(report: RotationReport): String {
        return buildString {
            appendLine("📊 REPORTE DE ROTACIÓN REWS")
            appendLine("=" .repeat(50))
            appendLine("📅 Fecha: ${dateFormat.format(report.timestamp)}")
            appendLine()
            
            appendLine("👥 TRABAJADORES")
            appendLine("-".repeat(30))
            appendLine("Total: ${report.totalWorkers}")
            appendLine("Activos: ${report.activeWorkers}")
            appendLine("Entrenadores: ${report.trainers}")
            appendLine("Entrenados: ${report.trainees}")
            appendLine("Líderes: ${report.leaders}")
            appendLine()
            
            appendLine("🏭 ESTACIONES")
            appendLine("-".repeat(30))
            appendLine("Total: ${report.totalWorkstations}")
            appendLine("Activas: ${report.activeWorkstations}")
            appendLine("Prioritarias: ${report.priorityWorkstations}")
            appendLine()
            
            appendLine("📈 MÉTRICAS")
            appendLine("-".repeat(30))
            appendLine("Eficiencia de Rotación: ${String.format("%.1f%%", report.rotationEfficiency)}")
            appendLine("Utilización de Estaciones: ${String.format("%.1f%%", report.workstationUtilization)}")
            appendLine()
            
            appendLine("👑 DISTRIBUCIÓN DE LIDERAZGO")
            appendLine("-".repeat(30))
            report.leadershipDistribution.forEach { (type, count) ->
                appendLine("$type: $count líderes")
            }
            appendLine()
            
            appendLine("📊 DISTRIBUCIÓN POR ESTACIÓN")
            appendLine("-".repeat(30))
            report.workerDistribution.forEach { (stationName, count) ->
                appendLine("$stationName: $count trabajadores")
            }
        }
    }
    
    /**
     * Genera imagen del reporte
     */
    fun generateReportImage(report: RotationReport): Bitmap {
        val width = 800
        val height = 1200
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        // Fondo blanco
        canvas.drawColor(Color.WHITE)
        
        val paint = Paint().apply {
            isAntiAlias = true
            textSize = 24f
            color = Color.BLACK
        }
        
        val titlePaint = Paint().apply {
            isAntiAlias = true
            textSize = 32f
            color = Color.parseColor("#6200EE")
            typeface = Typeface.DEFAULT_BOLD
        }
        
        var y = 60f
        
        // Título
        canvas.drawText("📊 REPORTE REWS", 50f, y, titlePaint)
        y += 60f
        
        // Fecha
        canvas.drawText("📅 ${dateFormat.format(report.timestamp)}", 50f, y, paint)
        y += 80f
        
        // Métricas principales
        canvas.drawText("👥 Trabajadores: ${report.activeWorkers}/${report.totalWorkers}", 50f, y, paint)
        y += 40f
        canvas.drawText("🏭 Estaciones: ${report.activeWorkstations}/${report.totalWorkstations}", 50f, y, paint)
        y += 40f
        canvas.drawText("👑 Líderes: ${report.leaders}", 50f, y, paint)
        y += 40f
        canvas.drawText("🎓 Entrenadores: ${report.trainers}", 50f, y, paint)
        y += 40f
        canvas.drawText("🎯 Entrenados: ${report.trainees}", 50f, y, paint)
        y += 80f
        
        // Eficiencia
        canvas.drawText("📈 Eficiencia: ${String.format("%.1f%%", report.rotationEfficiency)}", 50f, y, paint)
        y += 40f
        canvas.drawText("📊 Utilización: ${String.format("%.1f%%", report.workstationUtilization)}", 50f, y, paint)
        
        return bitmap
    }
    
    /**
     * Guarda imagen en almacenamiento
     */
    fun saveReportImage(bitmap: Bitmap): File? {
        return try {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val filename = "reporte_rews_$timestamp.png"
            val file = File(context.getExternalFilesDir(null), filename)
            
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    private fun calculateRotationEfficiency(
        workers: List<Worker>,
        workstations: List<Workstation>,
        rotationTable: RotationTable?
    ): Double {
        if (rotationTable == null) return 0.0
        
        val totalCapacity = workstations.sumOf { it.requiredWorkers }
        val assignedWorkers = rotationTable.currentPhase.values.sumOf { it.size }
        
        return if (totalCapacity > 0) {
            (assignedWorkers.toDouble() / totalCapacity) * 100
        } else 0.0
    }
    
    private fun calculateWorkstationUtilization(
        workstations: List<Workstation>,
        rotationTable: RotationTable?
    ): Double {
        if (rotationTable == null || workstations.isEmpty()) return 0.0
        
        val utilizationSum = workstations.sumOf { workstation ->
            val assignedWorkers = rotationTable.currentPhase[workstation.id]?.size ?: 0
            val requiredWorkers = workstation.requiredWorkers
            
            if (requiredWorkers > 0) {
                minOf(assignedWorkers.toDouble() / requiredWorkers, 1.0) * 100
            } else 0.0
        }
        
        return utilizationSum / workstations.size
    }
    
    private fun calculateWorkerDistribution(
        workers: List<Worker>,
        rotationTable: RotationTable?
    ): Map<String, Int> {
        if (rotationTable == null) return emptyMap()
        
        return rotationTable.currentPhase.mapKeys { (workstationId, _) ->
            "Estación $workstationId"
        }.mapValues { (_, workerList) ->
            workerList.size
        }
    }
    
    private fun calculateLeadershipDistribution(
        workers: List<Worker>,
        rotationTable: RotationTable?
    ): Map<String, Int> {
        val leaders = workers.filter { it.isLeader }
        
        return mapOf(
            "Ambas Partes" to leaders.count { it.leadershipType == "BOTH_PARTS" },
            "Primera Parte" to leaders.count { it.leadershipType == "FIRST_PART" },
            "Segunda Parte" to leaders.count { it.leadershipType == "SECOND_PART" }
        )
    }
    
    private fun calculateCapacityUtilization(
        workers: List<Worker>,
        workstations: List<Workstation>
    ): Double {
        val totalCapacity = workstations.sumOf { it.requiredWorkers }
        val availableWorkers = workers.count { it.isActive }
        
        return if (totalCapacity > 0) {
            minOf(availableWorkers.toDouble() / totalCapacity, 1.0) * 100
        } else 0.0
    }
    
    private fun calculateTrainingCoverage(workers: List<Worker>): Double {
        val trainees = workers.count { it.isTrainee }
        val trainers = workers.count { it.isTrainer }
        
        return if (trainers > 0) {
            minOf(trainees.toDouble() / trainers, 1.0) * 100
        } else 0.0
    }
    
    private fun calculateLeadershipCoverage(
        workers: List<Worker>,
        workstations: List<Workstation>
    ): Double {
        val leaders = workers.count { it.isLeader }
        val activeStations = workstations.count { it.isActive }
        
        return if (activeStations > 0) {
            minOf(leaders.toDouble() / activeStations, 1.0) * 100
        } else 0.0
    }
    
    private fun calculateRestrictionImpact(workers: List<Worker>): Double {
        val restrictedWorkers = workers.count { it.restrictionNotes.isNotBlank() }
        val totalWorkers = workers.size
        
        return if (totalWorkers > 0) {
            (restrictedWorkers.toDouble() / totalWorkers) * 100
        } else 0.0
    }
    
    private fun generateRecommendations(
        workers: List<Worker>,
        workstations: List<Workstation>
    ): List<String> {
        val recommendations = mutableListOf<String>()
        
        val avgAvailability = workers.map { it.availabilityPercentage }.average()
        if (avgAvailability < 80) {
            recommendations.add("⚠️ Disponibilidad promedio baja (${String.format("%.1f%%", avgAvailability)}). Considerar revisar horarios.")
        }
        
        val trainers = workers.count { it.isTrainer }
        val trainees = workers.count { it.isTrainee }
        if (trainees > trainers * 2) {
            recommendations.add("👨‍🏫 Considerar agregar más entrenadores. Ratio actual: $trainees entrenados por $trainers entrenadores.")
        }
        
        val leaders = workers.count { it.isLeader }
        val activeStations = workstations.count { it.isActive }
        if (leaders < activeStations * 0.5) {
            recommendations.add("👑 Considerar designar más líderes. Solo $leaders líderes para $activeStations estaciones activas.")
        }
        
        val restrictedWorkers = workers.count { it.restrictionNotes.isNotBlank() }
        if (restrictedWorkers > workers.size * 0.3) {
            recommendations.add("🚫 Alto número de trabajadores con restricciones ($restrictedWorkers). Revisar asignaciones.")
        }
        
        return recommendations
    }
    
    data class RotationReport(
        val timestamp: Date,
        val totalWorkers: Int,
        val activeWorkers: Int,
        val totalWorkstations: Int,
        val activeWorkstations: Int,
        val trainers: Int,
        val trainees: Int,
        val leaders: Int,
        val priorityWorkstations: Int,
        val rotationEfficiency: Double,
        val workstationUtilization: Double,
        val workerDistribution: Map<String, Int>,
        val leadershipDistribution: Map<String, Int>
    )
    
    data class PerformanceStats(
        val averageAvailability: Double,
        val capacityUtilization: Double,
        val trainingCoverage: Double,
        val leadershipCoverage: Double,
        val restrictionImpact: Double,
        val recommendations: List<String>
    )
}