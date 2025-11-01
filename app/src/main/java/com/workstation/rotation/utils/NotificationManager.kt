package com.workstation.rotation.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.workstation.rotation.MainActivity
import com.workstation.rotation.R

/**
 * Gestor de notificaciones para el sistema REWS
 */
class NotificationManager(private val context: Context) {
    
    companion object {
        private const val CHANNEL_ID_ROTATION = "rotation_channel"
        private const val CHANNEL_ID_TRAINING = "training_channel"
        private const val CHANNEL_ID_ALERTS = "alerts_channel"
        
        private const val NOTIFICATION_ID_ROTATION = 1001
        private const val NOTIFICATION_ID_TRAINING = 1002
        private const val NOTIFICATION_ID_ALERT = 1003
    }
    
    init {
        createNotificationChannels()
    }
    
    /**
     * Crea los canales de notificación necesarios
     */
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            // Canal para rotaciones
            val rotationChannel = NotificationChannel(
                CHANNEL_ID_ROTATION,
                "Rotaciones",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notificaciones sobre cambios de rotación"
                enableVibration(true)
                setShowBadge(true)
            }
            
            // Canal para entrenamiento
            val trainingChannel = NotificationChannel(
                CHANNEL_ID_TRAINING,
                "Entrenamiento",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones sobre actividades de entrenamiento"
                enableVibration(true)
                setShowBadge(true)
            }
            
            // Canal para alertas
            val alertsChannel = NotificationChannel(
                CHANNEL_ID_ALERTS,
                "Alertas del Sistema",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alertas importantes del sistema"
                enableVibration(true)
                setShowBadge(true)
            }
            
            notificationManager.createNotificationChannels(listOf(
                rotationChannel,
                trainingChannel,
                alertsChannel
            ))
        }
    }
    
    /**
     * Notifica sobre una nueva rotación generada
     */
    fun notifyRotationGenerated(
        totalWorkers: Int,
        totalStations: Int,
        efficiency: Double
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_ROTATION)
            .setSmallIcon(R.drawable.ic_rotation)
            .setContentTitle("🔄 Nueva Rotación Generada")
            .setContentText("$totalWorkers trabajadores asignados a $totalStations estaciones")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("✅ Rotación completada exitosamente\n" +
                        "👥 Trabajadores: $totalWorkers\n" +
                        "🏭 Estaciones: $totalStations\n" +
                        "📈 Eficiencia: ${String.format("%.1f%%", efficiency)}"))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .addAction(
                R.drawable.ic_rotation,
                "Ver Rotación",
                pendingIntent
            )
            .build()
        
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_ROTATION, notification)
    }
    
    /**
     * Notifica sobre completación de entrenamiento
     */
    fun notifyTrainingCompleted(
        traineeName: String,
        trainerName: String,
        workstationName: String
    ) {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_TRAINING)
            .setSmallIcon(R.drawable.ic_certification)
            .setContentTitle("🎓 Entrenamiento Completado")
            .setContentText("$traineeName ha completado el entrenamiento")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("🎉 ¡Felicitaciones!\n\n" +
                        "👤 Entrenado: $traineeName\n" +
                        "👨‍🏫 Entrenador: $trainerName\n" +
                        "🏭 Estación: $workstationName\n\n" +
                        "El trabajador ahora está certificado y puede trabajar independientemente."))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_TRAINING, notification)
    }
    
    /**
     * Notifica sobre designación de nuevo líder
     */
    fun notifyLeaderAssigned(
        leaderName: String,
        workstationName: String,
        leadershipType: String
    ) {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val typeText = when (leadershipType) {
            "BOTH_PARTS" -> "ambas partes"
            "FIRST_PART" -> "primera parte"
            "SECOND_PART" -> "segunda parte"
            else -> "tipo desconocido"
        }
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_ALERTS)
            .setSmallIcon(R.drawable.ic_certification)
            .setContentTitle("👑 Nuevo Líder Designado")
            .setContentText("$leaderName es ahora líder de $workstationName")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("👑 Liderazgo Asignado\n\n" +
                        "👤 Líder: $leaderName\n" +
                        "🏭 Estación: $workstationName\n" +
                        "📋 Tipo: $typeText\n\n" +
                        "El líder tendrá prioridad en la asignación de esta estación."))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_ALERT, notification)
    }
    
    /**
     * Notifica sobre problemas en la rotación
     */
    fun notifyRotationIssue(
        issueType: RotationIssueType,
        details: String
    ) {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val (title, icon) = when (issueType) {
            RotationIssueType.INSUFFICIENT_WORKERS -> "⚠️ Trabajadores Insuficientes" to R.drawable.ic_certification
            RotationIssueType.CAPACITY_EXCEEDED -> "📊 Capacidad Excedida" to R.drawable.ic_rotation
            RotationIssueType.TRAINING_CONFLICT -> "🎓 Conflicto de Entrenamiento" to R.drawable.ic_certification
            RotationIssueType.RESTRICTION_VIOLATION -> "🚫 Violación de Restricción" to R.drawable.ic_certification
            RotationIssueType.LEADER_CONFLICT -> "👑 Conflicto de Liderazgo" to R.drawable.ic_certification
        }
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_ALERTS)
            .setSmallIcon(icon)
            .setContentTitle(title)
            .setContentText("Se detectó un problema en la rotación")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("⚠️ Problema Detectado\n\n$details\n\n" +
                        "Revisa la configuración y genera una nueva rotación."))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_ALERT, notification)
    }
    
    /**
     * Notifica sobre estadísticas de rendimiento
     */
    fun notifyPerformanceStats(
        efficiency: Double,
        utilization: Double,
        recommendations: Int
    ) {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val performanceLevel = when {
            efficiency >= 90 -> "Excelente"
            efficiency >= 75 -> "Bueno"
            efficiency >= 60 -> "Regular"
            else -> "Necesita Mejora"
        }
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_ALERTS)
            .setSmallIcon(R.drawable.ic_rotation)
            .setContentTitle("📊 Reporte de Rendimiento")
            .setContentText("Rendimiento: $performanceLevel (${String.format("%.1f%%", efficiency)})")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("📈 Estadísticas de Rendimiento\n\n" +
                        "🎯 Eficiencia: ${String.format("%.1f%%", efficiency)}\n" +
                        "📊 Utilización: ${String.format("%.1f%%", utilization)}\n" +
                        "💡 Recomendaciones: $recommendations\n\n" +
                        "Nivel: $performanceLevel"))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_ALERT, notification)
    }
    
    /**
     * Programa notificación de recordatorio de rotación
     */
    fun scheduleRotationReminder(hours: Int) {
        // Implementar con WorkManager para notificaciones programadas
        // Por ahora, solo mostramos una notificación inmediata
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_ROTATION)
            .setSmallIcon(R.drawable.ic_rotation)
            .setContentTitle("⏰ Recordatorio de Rotación")
            .setContentText("Es hora de generar una nueva rotación")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("🔄 Recordatorio Programado\n\n" +
                        "Han pasado $hours horas desde la última rotación.\n" +
                        "Considera generar una nueva rotación para optimizar la distribución."))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_ROTATION, notification)
    }
    
    /**
     * Cancela todas las notificaciones
     */
    fun cancelAllNotifications() {
        NotificationManagerCompat.from(context).cancelAll()
    }
    
    /**
     * Tipos de problemas en la rotación
     */
    enum class RotationIssueType {
        INSUFFICIENT_WORKERS,
        CAPACITY_EXCEEDED,
        TRAINING_CONFLICT,
        RESTRICTION_VIOLATION,
        LEADER_CONFLICT
    }
}