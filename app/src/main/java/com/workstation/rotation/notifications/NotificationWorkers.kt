package com.workstation.rotation.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.workstation.rotation.data.database.AppDatabase
import com.workstation.rotation.services.RotationHistoryService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

/**
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 🔔 WORKERS PARA NOTIFICACIONES EN BACKGROUND - REWS v3.1.0
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 
 * Workers especializados para ejecutar verificaciones y generar notificaciones
 * inteligentes en background usando WorkManager de Android.
 * 
 * 🎯 WORKERS IMPLEMENTADOS:
 * • RotationReminderWorker: Recordatorios inteligentes de rotación
 * • CapacityMonitorWorker: Monitoreo continuo de capacidad
 * • WeeklyReportWorker: Generación de reportes semanales
 * • ProactiveAnalysisWorker: Análisis predictivo y alertas proactivas
 * 
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 */

/**
 * Worker para recordatorios inteligentes de rotación
 */
class RotationReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val notificationSystem = IntelligentNotificationSystem(applicationContext)
            val historyService = RotationHistoryService(applicationContext)
            
            // Obtener rotaciones activas
            val activeRotations = historyService.getActiveRotationsLiveData().value ?: emptyList()
            
            if (activeRotations.isNotEmpty()) {
                // Calcular tiempo promedio de rotaciones activas
                val currentTime = System.currentTimeMillis()
                val averageActiveTime = activeRotations.map { rotation ->
                    (currentTime - rotation.rotation_date) / (1000 * 60 * 60) // Horas
                }.average()
                
                // Determinar momento óptimo basado en patrones históricos
                val optimalTime = calculateOptimalRotationTime()
                
                // Solo notificar si es necesario
                if (averageActiveTime >= IntelligentNotificationSystem.ROTATION_REMINDER_HOURS) {
                    notificationSystem.showRotationReminder(
                        hoursActive = averageActiveTime.toLong(),
                        activeRotations = activeRotations.size,
                        predictedOptimalTime = optimalTime
                    )
                }
            }
            
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
    
    private suspend fun calculateOptimalRotationTime(): String {
        // Análisis simple basado en hora del día
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        
        return when {
            hour in 6..10 -> "Mañana (óptimo para inicio de turno)"
            hour in 11..14 -> "Mediodía (cambio de turno recomendado)"
            hour in 15..18 -> "Tarde (rotación de productividad)"
            else -> "Fuera de horario laboral"
        }
    }
}

/**
 * Worker para monitoreo continuo de capacidad
 */
class CapacityMonitorWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val notificationSystem = IntelligentNotificationSystem(applicationContext)
            val database = AppDatabase.getDatabase(applicationContext)
            val historyService = RotationHistoryService(applicationContext)
            
            // Obtener todas las estaciones activas
            val workstations = database.workstationDao().getAllActiveWorkstations()
            val activeRotations = historyService.getActiveRotationsLiveData().value ?: emptyList()
            
            // Analizar capacidad de cada estación
            for (workstation in workstations) {
                val currentWorkers = activeRotations.count { it.workstation_id == workstation.id }
                val utilization = if (workstation.requiredWorkers > 0) {
                    currentWorkers.toDouble() / workstation.requiredWorkers.toDouble()
                } else {
                    0.0
                }
                
                // Alertar si la capacidad es crítica
                if (utilization >= IntelligentNotificationSystem.CRITICAL_CAPACITY_THRESHOLD) {
                    notificationSystem.showCapacityAlert(
                        stationName = workstation.name,
                        currentCapacity = currentWorkers,
                        requiredCapacity = workstation.requiredWorkers,
                        utilizationPercent = utilization
                    )
                }
            }
            
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}

/**
 * Worker para generación de reportes semanales
 */
class WeeklyReportWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val notificationSystem = IntelligentNotificationSystem(applicationContext)
            val historyService = RotationHistoryService(applicationContext)
            
            // Calcular fechas de la semana pasada
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.WEEK_OF_YEAR, -1)
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            val startOfWeek = calendar.time
            
            calendar.add(Calendar.DAY_OF_WEEK, 6)
            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            val endOfWeek = calendar.time
            
            // Generar reporte de productividad
            val report = historyService.generateProductivityReport(startOfWeek, endOfWeek)
            
            // Calcular métricas adicionales
            val weeklyHistory = historyService.getHistoryByDateRange(startOfWeek, endOfWeek)
            val completedTrainings = weeklyHistory.count { 
                it.rotation_type == "TRAINING" && it.completed 
            }
            
            // Encontrar mejor rendimiento
            val topPerformer = weeklyHistory
                .filter { it.performance_score != null }
                .maxByOrNull { it.performance_score!! }
            
            // Determinar tendencia (simplificado)
            val efficiencyTrend = if (report.averagePerformanceScore >= 7.5) "up" else "stable"
            
            notificationSystem.showWeeklyReport(
                totalRotations = report.totalRotations,
                averageDuration = report.averageDurationMinutes,
                topPerformer = topPerformer?.let { "Trabajador #${it.worker_id}" } ?: "N/A",
                topPerformanceScore = topPerformer?.performance_score ?: 0.0,
                totalTrainingsCompleted = completedTrainings,
                efficiencyTrend = efficiencyTrend
            )
            
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}

/**
 * Worker para análisis predictivo y alertas proactivas
 */
class ProactiveAnalysisWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val notificationSystem = IntelligentNotificationSystem(applicationContext)
            val historyService = RotationHistoryService(applicationContext)
            val database = AppDatabase.getDatabase(applicationContext)
            
            // Análisis 1: Verificar rotaciones activas
            val activeRotations = historyService.getActiveRotationsLiveData().value ?: emptyList()
            val totalWorkers = database.workerDao().getAllActiveWorkers().size
            
            if (activeRotations.isEmpty() && totalWorkers > 0) {
                notificationSystem.showProactiveAlert(
                    "🔄 Sistema Inactivo",
                    "No hay rotaciones activas en el sistema",
                    "Considera generar una rotación para optimizar la productividad del equipo.",
                    IntelligentNotificationSystem.ProactiveAlertType.NO_ACTIVE_ROTATIONS
                )
            }
            
            // Análisis 2: Detectar rotaciones muy largas
            val longRotations = activeRotations.filter { rotation ->
                val hoursActive = (System.currentTimeMillis() - rotation.rotation_date) / (1000 * 60 * 60)
                hoursActive >= IntelligentNotificationSystem.LONG_ROTATION_THRESHOLD_HOURS
            }
            
            if (longRotations.isNotEmpty()) {
                notificationSystem.showProactiveAlert(
                    "⏰ Rotaciones Prolongadas",
                    "${longRotations.size} rotaciones activas por más de ${IntelligentNotificationSystem.LONG_ROTATION_THRESHOLD_HOURS}h",
                    "Rotaciones muy largas pueden causar fatiga. Considera generar una nueva rotación.",
                    IntelligentNotificationSystem.ProactiveAlertType.NO_ACTIVE_ROTATIONS
                )
            }
            
            // Análisis 3: Verificar rendimiento bajo
            val recentHistory = historyService.getHistoryByDateRange(
                Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000), // Últimas 24h
                Date()
            )
            
            val lowPerformanceRotations = recentHistory.filter { rotation ->
                rotation.performance_score != null && 
                rotation.performance_score < IntelligentNotificationSystem.LOW_PERFORMANCE_THRESHOLD
            }
            
            if (lowPerformanceRotations.size >= 3) {
                notificationSystem.showProactiveAlert(
                    "📉 Rendimiento Bajo Detectado",
                    "${lowPerformanceRotations.size} rotaciones con score < ${IntelligentNotificationSystem.LOW_PERFORMANCE_THRESHOLD}",
                    "Revisa las asignaciones y considera entrenamiento adicional para los trabajadores afectados.",
                    IntelligentNotificationSystem.ProactiveAlertType.LOW_PERFORMANCE_DETECTED
                )
            }
            
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}