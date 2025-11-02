package com.workstation.rotation.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.workstation.rotation.R

class SmartNotificationManager(private val context: Context) {
    
    companion object {
        private const val CHANNEL_RESULTS = "rotation_results"
        private const val CHANNEL_BENCHMARK = "benchmark_results"
        private const val CHANNEL_TIPS = "rotation_tips"
        private const val CHANNEL_ALERTS = "rotation_alerts"
        
        private const val NOTIFICATION_RESULT = 1001
        private const val NOTIFICATION_BENCHMARK = 1002
        private const val NOTIFICATION_TIP = 1003
        private const val NOTIFICATION_ALERT = 1004
    }
    
    init {
        createNotificationChannels()
    }
    
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channels = listOf(
                NotificationChannel(
                    CHANNEL_RESULTS,
                    "Resultados de Rotación",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Notificaciones sobre resultados de rotaciones generadas"
                },
                
                NotificationChannel(
                    CHANNEL_BENCHMARK,
                    "Resultados de Benchmark",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Notificaciones sobre resultados de pruebas de rendimiento"
                },
                
                NotificationChannel(
                    CHANNEL_TIPS,
                    "Tips y Sugerencias",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Consejos para optimizar el uso de la aplicación"
                },
                
                NotificationChannel(
                    CHANNEL_ALERTS,
                    "Alertas del Sistema",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Alertas importantes sobre el funcionamiento del sistema"
                }
            )
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            channels.forEach { notificationManager.createNotificationChannel(it) }
        }
    }
    
    fun showRotationResult(workersCount: Int, stationsCount: Int, executionTime: Long) {
        val notification = NotificationCompat.Builder(context, CHANNEL_RESULTS)
            .setSmallIcon(R.drawable.ic_rotation)
            .setContentTitle("Rotación Generada Exitosamente")
            .setContentText("$workersCount trabajadores asignados a $stationsCount estaciones en ${executionTime}ms")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        
        NotificationManagerCompat.from(context).notify(NOTIFICATION_RESULT, notification)
    }
    
    fun showBenchmarkResult(originalTime: Long, sqlTime: Long, improvement: Double) {
        val improvementText = if (improvement > 0) {
            "SQL ${improvement.toInt()}% más rápido"
        } else {
            "Original ${(-improvement).toInt()}% más rápido"
        }
        
        val notification = NotificationCompat.Builder(context, CHANNEL_BENCHMARK)
            .setSmallIcon(R.drawable.ic_certification)
            .setContentTitle("Benchmark Completado")
            .setContentText("Original: ${originalTime}ms, SQL: ${sqlTime}ms - $improvementText")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        
        NotificationManagerCompat.from(context).notify(NOTIFICATION_BENCHMARK, notification)
    }
    
    fun showOptimizationTip(tip: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_TIPS)
            .setSmallIcon(R.drawable.ic_guide)
            .setContentTitle("Tip de Optimización")
            .setContentText(tip)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .build()
        
        NotificationManagerCompat.from(context).notify(NOTIFICATION_TIP, notification)
    }
    
    fun showAlert(title: String, message: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ALERTS)
            .setSmallIcon(R.drawable.ic_rotation)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ALERT, notification)
    }
    
    fun showLongRunningBenchmark() {
        showOptimizationTip("Para datasets grandes (100+ trabajadores), el algoritmo SQL es significativamente más rápido")
    }
    
    fun showMemoryOptimization() {
        showOptimizationTip("El algoritmo SQL usa hasta 50% menos memoria para operaciones complejas")
    }
    
    fun showConsistencyTip() {
        showOptimizationTip("El algoritmo SQL garantiza resultados más consistentes y reproducibles")
    }
}