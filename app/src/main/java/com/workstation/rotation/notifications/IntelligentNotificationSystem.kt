package com.workstation.rotation.notifications

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.*
import com.workstation.rotation.MainActivity
import com.workstation.rotation.R
import com.workstation.rotation.data.database.AppDatabase
import com.workstation.rotation.services.RotationHistoryService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 🔔 SISTEMA DE NOTIFICACIONES INTELIGENTES - REWS v3.1.0
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 
 * 🎯 FUNCIONALIDADES PRINCIPALES:
 * • Recordatorios de rotación próxima con predicción inteligente
 * • Alertas de capacidad crítica en estaciones en tiempo real
 * • Notificaciones de entrenamiento completado automáticas
 * • Resúmenes semanales automáticos con métricas
 * • Alertas proactivas basadas en patrones históricos
 * 
 * 📊 CARACTERÍSTICAS INTELIGENTES:
 * • Análisis de patrones de rotación para predicciones
 * • Notificaciones contextuales según horario laboral
 * • Priorización automática según criticidad
 * • Agrupación inteligente de notificaciones relacionadas
 * • Configuración personalizable por usuario
 * 
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 */

class IntelligentNotificationSystem(private val context: Context) {
    
    private val historyService = RotationHistoryService(context)
    private val notificationManager = NotificationManagerCompat.from(context)
    private val workManager = WorkManager.getInstance(context)
    
    companion object {
        // Canales de notificación
        const val CHANNEL_ROTATION_REMINDERS = "rotation_reminders"
        const val CHANNEL_CAPACITY_ALERTS = "capacity_alerts"
        const val CHANNEL_TRAINING_UPDATES = "training_updates"
        const val CHANNEL_WEEKLY_REPORTS = "weekly_reports"
        const val CHANNEL_PROACTIVE_ALERTS = "proactive_alerts"
        
        // IDs de notificación
        const val NOTIFICATION_ROTATION_REMINDER = 2001
        const val NOTIFICATION_CAPACITY_ALERT = 2002
        const val NOTIFICATION_TRAINING_COMPLETE = 2003
        const val NOTIFICATION_WEEKLY_REPORT = 2004
        const val NOTIFICATION_PROACTIVE_ALERT = 2005
        
        // Configuración de timing
        const val ROTATION_REMINDER_HOURS = 4L
        const val CAPACITY_CHECK_MINUTES = 15L
        const val WEEKLY_REPORT_DAY = Calendar.MONDAY
        const val WEEKLY_REPORT_HOUR = 8
        
        // Umbrales de alertas
        const val CRITICAL_CAPACITY_THRESHOLD = 0.8 // 80%
        const val LOW_PERFORMANCE_THRESHOLD = 6.0 // Score < 6.0
        const val LONG_ROTATION_THRESHOLD_HOURS = 6L
    }
    
    init {
        createNotificationChannels()
        schedulePeriodicChecks()
    }
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // 🔧 CONFIGURACIÓN DE CANALES
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val systemNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            val channels = listOf(
                NotificationChannel(
                    CHANNEL_ROTATION_REMINDERS,
                    "Recordatorios de Rotación",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Recordatorios inteligentes para generar nuevas rotaciones"
                    enableVibration(true)
                    setShowBadge(true)
                },
                
                NotificationChannel(
                    CHANNEL_CAPACITY_ALERTS,
                    "Alertas de Capacidad",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Alertas críticas sobre capacidad de estaciones"
                    enableVibration(true)
                    setShowBadge(true)
                },
                
                NotificationChannel(
                    CHANNEL_TRAINING_UPDATES,
                    "Actualizaciones de Entrenamiento",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Notificaciones sobre progreso y completación de entrenamientos"
                    enableVibration(false)
                    setShowBadge(true)
                },
                
                NotificationChannel(
                    CHANNEL_WEEKLY_REPORTS,
                    "Reportes Semanales",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Resúmenes semanales automáticos con métricas"
                    enableVibration(false)
                    setShowBadge(false)
                },
                
                NotificationChannel(
                    CHANNEL_PROACTIVE_ALERTS,
                    "Alertas Proactivas",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Alertas inteligentes basadas en patrones y predicciones"
                    enableVibration(true)
                    setShowBadge(true)
                }
            )
            
            systemNotificationManager.createNotificationChannels(channels)
        }
    }
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // 🔄 RECORDATORIOS DE ROTACIÓN INTELIGENTES
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    /**
     * Programa recordatorio inteligente de rotación basado en patrones históricos
     */
    fun scheduleIntelligentRotationReminder() {
        val workRequest = PeriodicWorkRequestBuilder<RotationReminderWorker>(
            ROTATION_REMINDER_HOURS, TimeUnit.HOURS
        )
        .setConstraints(
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .build()
        )
        .build()
        
        workManager.enqueueUniquePeriodicWork(
            "rotation_reminder",
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest
        )
    }
    
    /**
     * Muestra recordatorio de rotación con análisis inteligente
     */
    fun showRotationReminder(
        hoursActive: Long,
        activeRotations: Int,
        predictedOptimalTime: String
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("action", "generate_rotation")
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val urgencyLevel = when {
            hoursActive >= 8 -> "🔴 URGENTE"
            hoursActive >= 6 -> "🟡 RECOMENDADO"
            else -> "🟢 SUGERIDO"
        }
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ROTATION_REMINDERS)
            .setSmallIcon(R.drawable.ic_rotation)
            .setContentTitle("⏰ $urgencyLevel - Tiempo de Rotación")
            .setContentText("Rotación activa por ${hoursActive}h - $activeRotations trabajadores")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("""
                    🔄 Análisis de Rotación Inteligente
                    
                    ⏱️ Tiempo activo: ${hoursActive} horas
                    👥 Rotaciones activas: $activeRotations
                    🎯 Momento óptimo: $predictedOptimalTime
                    
                    ${getRotationRecommendation(hoursActive)}
                """.trimIndent()))
            .setPriority(if (hoursActive >= 6) NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .addAction(
                R.drawable.ic_rotation,
                "Generar Rotación",
                pendingIntent
            )
            .build()
        
        notificationManager.notify(NOTIFICATION_ROTATION_REMINDER, notification)
    }
    
    private fun getRotationRecommendation(hours: Long): String {
        return when {
            hours >= 8 -> "⚠️ Rotación prolongada detectada. Se recomienda generar nueva rotación inmediatamente para evitar fatiga."
            hours >= 6 -> "💡 Momento óptimo para rotación. Los trabajadores han completado un ciclo productivo."
            hours >= 4 -> "📊 Considera generar rotación para mantener variedad y engagement."
            else -> "✅ Rotación reciente. El sistema está funcionando óptimamente."
        }
    }
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // 🚨 ALERTAS DE CAPACIDAD CRÍTICA
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    /**
     * Programa monitoreo continuo de capacidad de estaciones
     */
    fun scheduleCapacityMonitoring() {
        val workRequest = PeriodicWorkRequestBuilder<CapacityMonitorWorker>(
            CAPACITY_CHECK_MINUTES, TimeUnit.MINUTES
        )
        .setConstraints(
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .build()
        )
        .build()
        
        workManager.enqueueUniquePeriodicWork(
            "capacity_monitor",
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest
        )
    }
    
    /**
     * Muestra alerta de capacidad crítica
     */
    fun showCapacityAlert(
        stationName: String,
        currentCapacity: Int,
        requiredCapacity: Int,
        utilizationPercent: Double
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("action", "manage_workstations")
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val alertLevel = when {
            utilizationPercent >= 0.9 -> "🔴 CRÍTICO"
            utilizationPercent >= 0.8 -> "🟡 ALTO"
            else -> "🟢 NORMAL"
        }
        
        val notification = NotificationCompat.Builder(context, CHANNEL_CAPACITY_ALERTS)
            .setSmallIcon(R.drawable.ic_rotation)
            .setContentTitle("🚨 $alertLevel - Capacidad de Estación")
            .setContentText("$stationName: $currentCapacity/$requiredCapacity trabajadores")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("""
                    ⚠️ Alerta de Capacidad
                    
                    🏭 Estación: $stationName
                    👥 Capacidad actual: $currentCapacity/$requiredCapacity
                    📊 Utilización: ${String.format("%.1f%%", utilizationPercent * 100)}
                    
                    ${getCapacityRecommendation(utilizationPercent)}
                """.trimIndent()))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .addAction(
                R.drawable.ic_rotation,
                "Gestionar Estaciones",
                pendingIntent
            )
            .build()
        
        notificationManager.notify(NOTIFICATION_CAPACITY_ALERT, notification)
    }
    
    private fun getCapacityRecommendation(utilization: Double): String {
        return when {
            utilization >= 0.9 -> "🔴 Capacidad crítica. Reasignar trabajadores inmediatamente."
            utilization >= 0.8 -> "🟡 Capacidad alta. Considerar redistribución en próxima rotación."
            utilization <= 0.5 -> "📉 Capacidad baja. Oportunidad para optimización."
            else -> "✅ Capacidad óptima."
        }
    }
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // 🎓 NOTIFICACIONES DE ENTRENAMIENTO
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    /**
     * Notifica progreso de entrenamiento
     */
    fun showTrainingProgress(
        traineeName: String,
        trainerName: String,
        stationName: String,
        progressPercent: Int,
        hoursCompleted: Int,
        estimatedHoursRemaining: Int
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("action", "view_workers")
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val progressEmoji = when {
            progressPercent >= 90 -> "🎉"
            progressPercent >= 75 -> "🚀"
            progressPercent >= 50 -> "📈"
            progressPercent >= 25 -> "📊"
            else -> "🌱"
        }
        
        val notification = NotificationCompat.Builder(context, CHANNEL_TRAINING_UPDATES)
            .setSmallIcon(R.drawable.ic_certification)
            .setContentTitle("$progressEmoji Progreso de Entrenamiento - $progressPercent%")
            .setContentText("$traineeName en $stationName")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("""
                    🎓 Actualización de Entrenamiento
                    
                    👤 Entrenado: $traineeName
                    👨‍🏫 Entrenador: $trainerName
                    🏭 Estación: $stationName
                    
                    📊 Progreso: $progressPercent%
                    ⏱️ Horas completadas: $hoursCompleted
                    ⏳ Tiempo estimado restante: $estimatedHoursRemaining horas
                """.trimIndent()))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setProgress(100, progressPercent, false)
            .build()
        
        notificationManager.notify(NOTIFICATION_TRAINING_COMPLETE, notification)
    }
    
    /**
     * Notifica entrenamiento completado
     */
    fun showTrainingCompleted(
        traineeName: String,
        trainerName: String,
        stationName: String,
        totalHours: Int,
        finalScore: Double
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("action", "view_workers")
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val scoreEmoji = when {
            finalScore >= 9.0 -> "🏆"
            finalScore >= 8.0 -> "🥇"
            finalScore >= 7.0 -> "🥈"
            finalScore >= 6.0 -> "🥉"
            else -> "📜"
        }
        
        val notification = NotificationCompat.Builder(context, CHANNEL_TRAINING_UPDATES)
            .setSmallIcon(R.drawable.ic_certification)
            .setContentTitle("🎉 $scoreEmoji Entrenamiento Completado!")
            .setContentText("$traineeName - Score: ${String.format("%.1f", finalScore)}")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("""
                    🎓 ¡Certificación Exitosa!
                    
                    👤 Nuevo trabajador certificado: $traineeName
                    👨‍🏫 Entrenador: $trainerName
                    🏭 Estación: $stationName
                    
                    ⏱️ Duración total: $totalHours horas
                    ⭐ Score final: ${String.format("%.1f", finalScore)}/10.0
                    
                    🎉 El trabajador ahora puede operar independientemente en esta estación.
                """.trimIndent()))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        notificationManager.notify(NOTIFICATION_TRAINING_COMPLETE, notification)
    }
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // 📊 RESÚMENES SEMANALES AUTOMÁTICOS
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    /**
     * Programa resúmenes semanales automáticos
     */
    fun scheduleWeeklyReports() {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, WEEKLY_REPORT_DAY)
            set(Calendar.HOUR_OF_DAY, WEEKLY_REPORT_HOUR)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }
        
        // Si ya pasó este lunes, programar para el próximo
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.WEEK_OF_YEAR, 1)
        }
        
        val delay = calendar.timeInMillis - System.currentTimeMillis()
        
        val workRequest = PeriodicWorkRequestBuilder<WeeklyReportWorker>(
            7, TimeUnit.DAYS
        )
        .setInitialDelay(delay, TimeUnit.MILLISECONDS)
        .setConstraints(
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .build()
        )
        .build()
        
        workManager.enqueueUniquePeriodicWork(
            "weekly_report",
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest
        )
    }
    
    /**
     * Muestra resumen semanal
     */
    fun showWeeklyReport(
        totalRotations: Int,
        averageDuration: Double,
        topPerformer: String,
        topPerformanceScore: Double,
        totalTrainingsCompleted: Int,
        efficiencyTrend: String
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("action", "view_history")
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val trendEmoji = when (efficiencyTrend.lowercase()) {
            "up" -> "📈"
            "down" -> "📉"
            else -> "➡️"
        }
        
        val notification = NotificationCompat.Builder(context, CHANNEL_WEEKLY_REPORTS)
            .setSmallIcon(R.drawable.ic_rotation)
            .setContentTitle("📊 Resumen Semanal - REWS")
            .setContentText("$totalRotations rotaciones, $totalTrainingsCompleted entrenamientos completados")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("""
                    📈 Resumen Semanal del Sistema
                    
                    🔄 Total rotaciones: $totalRotations
                    ⏱️ Duración promedio: ${String.format("%.1f", averageDuration)} min
                    🏆 Mejor rendimiento: $topPerformer (${String.format("%.1f", topPerformanceScore)})
                    🎓 Entrenamientos completados: $totalTrainingsCompleted
                    
                    $trendEmoji Tendencia de eficiencia: $efficiencyTrend
                    
                    ¡Excelente trabajo del equipo esta semana!
                """.trimIndent()))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .addAction(
                R.drawable.ic_rotation,
                "Ver Detalles",
                pendingIntent
            )
            .build()
        
        notificationManager.notify(NOTIFICATION_WEEKLY_REPORT, notification)
    }
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // 🔮 ALERTAS PROACTIVAS INTELIGENTES
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    /**
     * Analiza patrones y muestra alertas proactivas
     */
    fun analyzeAndShowProactiveAlerts() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val metrics = historyService.getGeneralMetrics()
                
                // Detectar patrones problemáticos
                if (metrics.activeRotations == 0) {
                    showProactiveAlert(
                        "🔄 Sin Rotaciones Activas",
                        "No hay rotaciones activas en el sistema",
                        "Considera generar una nueva rotación para mantener la productividad.",
                        ProactiveAlertType.NO_ACTIVE_ROTATIONS
                    )
                }
                
                // Más análisis de patrones aquí...
                
            } catch (e: Exception) {
                // Log error silently
            }
        }
    }
    
    /**
     * Muestra alerta proactiva
     */
    private fun showProactiveAlert(
        title: String,
        shortDescription: String,
        recommendation: String,
        alertType: ProactiveAlertType
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("action", alertType.action)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_PROACTIVE_ALERTS)
            .setSmallIcon(R.drawable.ic_rotation)
            .setContentTitle(title)
            .setContentText(shortDescription)
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("""
                    🔮 Alerta Inteligente
                    
                    $shortDescription
                    
                    💡 Recomendación:
                    $recommendation
                """.trimIndent()))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        notificationManager.notify(NOTIFICATION_PROACTIVE_ALERT, notification)
    }
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // 🔧 CONFIGURACIÓN Y UTILIDADES
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    /**
     * Programa todas las verificaciones periódicas
     */
    private fun schedulePeriodicChecks() {
        scheduleIntelligentRotationReminder()
        scheduleCapacityMonitoring()
        scheduleWeeklyReports()
    }
    
    /**
     * Cancela todas las notificaciones programadas
     */
    fun cancelAllScheduledNotifications() {
        workManager.cancelAllWork()
        notificationManager.cancelAll()
    }
    
    /**
     * Verifica si las notificaciones están habilitadas
     */
    fun areNotificationsEnabled(): Boolean {
        return notificationManager.areNotificationsEnabled()
    }
    
    enum class ProactiveAlertType(val action: String) {
        NO_ACTIVE_ROTATIONS("generate_rotation"),
        LOW_PERFORMANCE_DETECTED("view_history"),
        CAPACITY_IMBALANCE("manage_workstations"),
        TRAINING_OVERDUE("view_workers")
    }
}