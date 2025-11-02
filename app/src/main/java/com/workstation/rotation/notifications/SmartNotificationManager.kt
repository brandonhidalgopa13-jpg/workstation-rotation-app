package com.workstation.rotation.notifications

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
import com.workstation.rotation.models.BenchmarkResult
import com.workstation.rotation.models.RotationItem

/**
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 🔔 SISTEMA DE NOTIFICACIONES INTELIGENTES
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 
 * Gestiona notificaciones contextuales y inteligentes para:
 * 1. Resultados de rotación
 * 2. Comparaciones de benchmark
 * 3. Alertas de rendimiento
 * 4. Recordatorios de optimización
 * 5. Estadísticas de uso
 * 
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 */
class SmartNotificationManager(private val context: Context) {
    
    companion object {
        private const val CHANNEL_ROTATION_RESULTS = "rotation_results"
        private const val CHANNEL_BENCHMARK_ALERTS = "benchmark_alerts"
        private const val CHANNEL_PERFORMANCE_TIPS = "performance_tips"
        private const val CHANNEL_SYSTEM_STATUS = "system_status"
        
        private const val NOTIFICATION_ROTATION_COMPLETE = 1001
        private const val NOTIFICATION_BENCHMARK_COMPLETE = 1002
        private const val NOTIFICATION_PERFORMANCE_TIP = 1003
        private const val NOTIFICATION_SYSTEM_ALERT = 1004
    }
    
    private val notificationManager = NotificationManagerCompat.from(context)
    
    init {
        createNotificationChannels()
    }
    
    /**
     * Crea los canales de notificación necesarios.
     */
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channels = listOf(
                NotificationChannel(
                    CHANNEL_ROTATION_RESULTS,
                    "Resultados de Rotación",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Notificaciones sobre rotaciones completadas"
                    enableVibration(true)
                },
                
                NotificationChannel(
                    CHANNEL_BENCHMARK_ALERTS,
                    "Alertas de Benchmark",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Resultados de comparaciones de rendimiento"
                    enableVibration(true)
                    enableLights(true)
                },
                
                NotificationChannel(
                    CHANNEL_PERFORMANCE_TIPS,
                    "Tips de Rendimiento",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Consejos para optimizar el sistema"
                },
                
                NotificationChannel(
                    CHANNEL_SYSTEM_STATUS,
                    "Estado del Sistema",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Alertas sobre el estado del sistema"
                    enableVibration(true)
                }
            )
            
            val systemNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            channels.forEach { systemNotificationManager.createNotificationChannel(it) }
        }
    }
    
    /**
     * Notifica cuando se completa una rotación exitosamente.
     */
    fun notifyRotationComplete(
        rotationItems: List<RotationItem>,
        executionTimeMs: Long,
        algorithmType: String
    ) {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val workersCount = rotationItems.size
        val stationsCount = rotationItems.map { it.workstationId }.distinct().size
        val leadersCount = rotationItems.count { it.isLeader }
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ROTATION_RESULTS)
            .setSmallIcon(R.drawable.ic_rotation)
            .setContentTitle("✅ Rotación Completada")
            .setContentText("$workersCount trabajadores asignados en ${executionTimeMs}ms")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("""
                    🚀 Algoritmo: $algorithmType
                    👥 Trabajadores: $workersCount
                    🏭 Estaciones: $stationsCount
                    👑 Líderes: $leadersCount
                    ⏱️ Tiempo: ${executionTimeMs}ms
                """.trimIndent()))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        
        notificationManager.notify(NOTIFICATION_ROTATION_COMPLETE, notification)
    }
    
    /**
     * Notifica resultados de benchmark con comparación.
     */
    fun notifyBenchmarkResults(
        originalResult: BenchmarkResult,
        sqlResult: BenchmarkResult
    ) {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val improvement = ((originalResult.executionTimeMs - sqlResult.executionTimeMs).toDouble() / originalResult.executionTimeMs * 100)
        val winner = if (improvement > 0) "SQL" else "Original"
        val improvementText = if (improvement > 0) "${improvement.toInt()}% más rápido" else "${(-improvement).toInt()}% más lento"
        
        val notification = NotificationCompat.Builder(context, CHANNEL_BENCHMARK_ALERTS)
            .setSmallIcon(R.drawable.ic_certification)
            .setContentTitle("🏁 Benchmark Completado")
            .setContentText("Algoritmo $winner es $improvementText")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("""
                    🔄 Original: ${originalResult.getFormattedExecutionTime()}
                    🚀 SQL: ${sqlResult.getFormattedExecutionTime()}
                    🏆 Ganador: Algoritmo $winner
                    📈 Mejora: $improvementText
                """.trimIndent()))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        
        notificationManager.notify(NOTIFICATION_BENCHMARK_COMPLETE, notification)
    }
    
    /**
     * Envía tips de rendimiento basados en el uso.
     */
    fun sendPerformanceTip(tipType: PerformanceTipType) {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val (title, message, details) = when (tipType) {
            PerformanceTipType.USE_SQL_FOR_LARGE_DATASETS -> Triple(
                "💡 Tip de Rendimiento",
                "Usa algoritmo SQL para datasets grandes",
                "Para más de 50 trabajadores, el algoritmo SQL es significativamente más rápido"
            )
            
            PerformanceTipType.OPTIMIZE_WORKSTATION_ASSIGNMENTS -> Triple(
                "🔧 Optimización Sugerida",
                "Revisa asignaciones de estaciones",
                "Algunos trabajadores no tienen estaciones asignadas, lo que puede afectar el rendimiento"
            )
            
            PerformanceTipType.REDUCE_TRAINING_PAIRS -> Triple(
                "⚡ Mejora de Velocidad",
                "Considera reducir parejas de entrenamiento",
                "Muchas parejas de entrenamiento pueden ralentizar la generación de rotaciones"
            )
            
            PerformanceTipType.UPDATE_WORKER_AVAILABILITY -> Triple(
                "📊 Actualización Recomendada",
                "Actualiza disponibilidad de trabajadores",
                "Trabajadores con baja disponibilidad pueden causar asignaciones subóptimas"
            )
        }
        
        val notification = NotificationCompat.Builder(context, CHANNEL_PERFORMANCE_TIPS)
            .setSmallIcon(R.drawable.ic_guide)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(details))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
        
        notificationManager.notify(NOTIFICATION_PERFORMANCE_TIP, notification)
    }
    
    /**
     * Alerta sobre problemas del sistema.
     */
    fun alertSystemIssue(issueType: SystemIssueType, details: String) {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val (title, icon) = when (issueType) {
            SystemIssueType.NO_WORKERS_AVAILABLE -> "❌ Sin Trabajadores" to R.drawable.ic_rotation
            SystemIssueType.NO_STATIONS_CONFIGURED -> "⚠️ Sin Estaciones" to R.drawable.ic_rotation
            SystemIssueType.INSUFFICIENT_CAPACITY -> "📊 Capacidad Insuficiente" to R.drawable.ic_rotation
            SystemIssueType.ROTATION_FAILED -> "🚫 Rotación Fallida" to R.drawable.ic_rotation
        }
        
        val notification = NotificationCompat.Builder(context, CHANNEL_SYSTEM_STATUS)
            .setSmallIcon(icon)
            .setContentTitle(title)
            .setContentText(details)
            .setStyle(NotificationCompat.BigTextStyle().bigText(details))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        
        notificationManager.notify(NOTIFICATION_SYSTEM_ALERT, notification)
    }
    
    /**
     * Notificación de progreso para operaciones largas.
     */
    fun showProgressNotification(
        title: String,
        message: String,
        progress: Int,
        maxProgress: Int = 100
    ): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, CHANNEL_SYSTEM_STATUS)
            .setSmallIcon(R.drawable.ic_rotation)
            .setContentTitle(title)
            .setContentText(message)
            .setProgress(maxProgress, progress, false)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
    }
    
    /**
     * Cancela todas las notificaciones.
     */
    fun cancelAllNotifications() {
        notificationManager.cancelAll()
    }
    
    /**
     * Cancela una notificación específica.
     */
    fun cancelNotification(notificationId: Int) {
        notificationManager.cancel(notificationId)
    }
}

/**
 * Tipos de tips de rendimiento.
 */
enum class PerformanceTipType {
    USE_SQL_FOR_LARGE_DATASETS,
    OPTIMIZE_WORKSTATION_ASSIGNMENTS,
    REDUCE_TRAINING_PAIRS,
    UPDATE_WORKER_AVAILABILITY
}

/**
 * Tipos de problemas del sistema.
 */
enum class SystemIssueType {
    NO_WORKERS_AVAILABLE,
    NO_STATIONS_CONFIGURED,
    INSUFFICIENT_CAPACITY,
    ROTATION_FAILED
}