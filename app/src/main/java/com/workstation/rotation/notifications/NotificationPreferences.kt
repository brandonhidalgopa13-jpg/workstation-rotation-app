package com.workstation.rotation.notifications

import android.content.Context
import android.content.SharedPreferences

/**
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * ⚙️ CONFIGURACIONES DE NOTIFICACIONES INTELIGENTES - REWS v3.1.0
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 
 * Gestiona las preferencias del usuario para el sistema de notificaciones inteligentes.
 * Permite personalizar qué notificaciones recibir y cuándo recibirlas.
 * 
 * 🎯 CONFIGURACIONES DISPONIBLES:
 * • Habilitar/deshabilitar tipos específicos de notificaciones
 * • Configurar horarios de trabajo para notificaciones contextuales
 * • Ajustar umbrales de alertas según necesidades del negocio
 * • Personalizar frecuencia de recordatorios y reportes
 * 
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 */

class NotificationPreferences(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "notification_preferences", 
        Context.MODE_PRIVATE
    )
    
    companion object {
        // Claves de preferencias
        private const val KEY_ROTATION_REMINDERS_ENABLED = "rotation_reminders_enabled"
        private const val KEY_CAPACITY_ALERTS_ENABLED = "capacity_alerts_enabled"
        private const val KEY_TRAINING_UPDATES_ENABLED = "training_updates_enabled"
        private const val KEY_WEEKLY_REPORTS_ENABLED = "weekly_reports_enabled"
        private const val KEY_PROACTIVE_ALERTS_ENABLED = "proactive_alerts_enabled"
        
        // Configuraciones de timing
        private const val KEY_ROTATION_REMINDER_HOURS = "rotation_reminder_hours"
        private const val KEY_CAPACITY_CHECK_MINUTES = "capacity_check_minutes"
        private const val KEY_WEEKLY_REPORT_DAY = "weekly_report_day"
        private const val KEY_WEEKLY_REPORT_HOUR = "weekly_report_hour"
        
        // Umbrales de alertas
        private const val KEY_CRITICAL_CAPACITY_THRESHOLD = "critical_capacity_threshold"
        private const val KEY_LOW_PERFORMANCE_THRESHOLD = "low_performance_threshold"
        private const val KEY_LONG_ROTATION_THRESHOLD_HOURS = "long_rotation_threshold_hours"
        
        // Horarios de trabajo
        private const val KEY_WORK_START_HOUR = "work_start_hour"
        private const val KEY_WORK_END_HOUR = "work_end_hour"
        private const val KEY_QUIET_HOURS_ENABLED = "quiet_hours_enabled"
        
        // Valores por defecto
        private const val DEFAULT_ROTATION_REMINDER_HOURS = 4L
        private const val DEFAULT_CAPACITY_CHECK_MINUTES = 15L
        private const val DEFAULT_WEEKLY_REPORT_DAY = 2 // Lunes
        private const val DEFAULT_WEEKLY_REPORT_HOUR = 8
        private const val DEFAULT_CRITICAL_CAPACITY_THRESHOLD = 0.8f
        private const val DEFAULT_LOW_PERFORMANCE_THRESHOLD = 6.0f
        private const val DEFAULT_LONG_ROTATION_THRESHOLD_HOURS = 6L
        private const val DEFAULT_WORK_START_HOUR = 7
        private const val DEFAULT_WORK_END_HOUR = 18
    }
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // 🔔 CONFIGURACIONES DE TIPOS DE NOTIFICACIÓN
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    var rotationRemindersEnabled: Boolean
        get() = prefs.getBoolean(KEY_ROTATION_REMINDERS_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_ROTATION_REMINDERS_ENABLED, value).apply()
    
    var capacityAlertsEnabled: Boolean
        get() = prefs.getBoolean(KEY_CAPACITY_ALERTS_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_CAPACITY_ALERTS_ENABLED, value).apply()
    
    var trainingUpdatesEnabled: Boolean
        get() = prefs.getBoolean(KEY_TRAINING_UPDATES_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_TRAINING_UPDATES_ENABLED, value).apply()
    
    var weeklyReportsEnabled: Boolean
        get() = prefs.getBoolean(KEY_WEEKLY_REPORTS_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_WEEKLY_REPORTS_ENABLED, value).apply()
    
    var proactiveAlertsEnabled: Boolean
        get() = prefs.getBoolean(KEY_PROACTIVE_ALERTS_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_PROACTIVE_ALERTS_ENABLED, value).apply()
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // ⏰ CONFIGURACIONES DE TIMING
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    var rotationReminderHours: Long
        get() = prefs.getLong(KEY_ROTATION_REMINDER_HOURS, DEFAULT_ROTATION_REMINDER_HOURS)
        set(value) = prefs.edit().putLong(KEY_ROTATION_REMINDER_HOURS, value).apply()
    
    var capacityCheckMinutes: Long
        get() = prefs.getLong(KEY_CAPACITY_CHECK_MINUTES, DEFAULT_CAPACITY_CHECK_MINUTES)
        set(value) = prefs.edit().putLong(KEY_CAPACITY_CHECK_MINUTES, value).apply()
    
    var weeklyReportDay: Int
        get() = prefs.getInt(KEY_WEEKLY_REPORT_DAY, DEFAULT_WEEKLY_REPORT_DAY)
        set(value) = prefs.edit().putInt(KEY_WEEKLY_REPORT_DAY, value).apply()
    
    var weeklyReportHour: Int
        get() = prefs.getInt(KEY_WEEKLY_REPORT_HOUR, DEFAULT_WEEKLY_REPORT_HOUR)
        set(value) = prefs.edit().putInt(KEY_WEEKLY_REPORT_HOUR, value).apply()
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // 🚨 UMBRALES DE ALERTAS
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    var criticalCapacityThreshold: Float
        get() = prefs.getFloat(KEY_CRITICAL_CAPACITY_THRESHOLD, DEFAULT_CRITICAL_CAPACITY_THRESHOLD)
        set(value) = prefs.edit().putFloat(KEY_CRITICAL_CAPACITY_THRESHOLD, value).apply()
    
    var lowPerformanceThreshold: Float
        get() = prefs.getFloat(KEY_LOW_PERFORMANCE_THRESHOLD, DEFAULT_LOW_PERFORMANCE_THRESHOLD)
        set(value) = prefs.edit().putFloat(KEY_LOW_PERFORMANCE_THRESHOLD, value).apply()
    
    var longRotationThresholdHours: Long
        get() = prefs.getLong(KEY_LONG_ROTATION_THRESHOLD_HOURS, DEFAULT_LONG_ROTATION_THRESHOLD_HOURS)
        set(value) = prefs.edit().putLong(KEY_LONG_ROTATION_THRESHOLD_HOURS, value).apply()
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // 🕐 HORARIOS DE TRABAJO
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    var workStartHour: Int
        get() = prefs.getInt(KEY_WORK_START_HOUR, DEFAULT_WORK_START_HOUR)
        set(value) = prefs.edit().putInt(KEY_WORK_START_HOUR, value).apply()
    
    var workEndHour: Int
        get() = prefs.getInt(KEY_WORK_END_HOUR, DEFAULT_WORK_END_HOUR)
        set(value) = prefs.edit().putInt(KEY_WORK_END_HOUR, value).apply()
    
    var quietHoursEnabled: Boolean
        get() = prefs.getBoolean(KEY_QUIET_HOURS_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_QUIET_HOURS_ENABLED, value).apply()
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // 🔧 MÉTODOS UTILITARIOS
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    /**
     * Verifica si estamos en horario de trabajo
     */
    fun isWorkingHours(): Boolean {
        if (!quietHoursEnabled) return true
        
        val currentHour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        return currentHour in workStartHour..workEndHour
    }
    
    /**
     * Verifica si un tipo de notificación está habilitado
     */
    fun isNotificationTypeEnabled(type: NotificationType): Boolean {
        return when (type) {
            NotificationType.ROTATION_REMINDER -> rotationRemindersEnabled
            NotificationType.CAPACITY_ALERT -> capacityAlertsEnabled
            NotificationType.TRAINING_UPDATE -> trainingUpdatesEnabled
            NotificationType.WEEKLY_REPORT -> weeklyReportsEnabled
            NotificationType.PROACTIVE_ALERT -> proactiveAlertsEnabled
        }
    }
    
    /**
     * Verifica si se debe mostrar una notificación considerando horarios
     */
    fun shouldShowNotification(type: NotificationType): Boolean {
        if (!isNotificationTypeEnabled(type)) return false
        
        // Los reportes semanales y alertas críticas siempre se muestran
        if (type == NotificationType.WEEKLY_REPORT || type == NotificationType.CAPACITY_ALERT) {
            return true
        }
        
        // Otras notificaciones respetan horarios de trabajo
        return isWorkingHours()
    }
    
    /**
     * Obtiene configuración completa como mapa
     */
    fun getAllSettings(): Map<String, Any> {
        return mapOf(
            "rotation_reminders_enabled" to rotationRemindersEnabled,
            "capacity_alerts_enabled" to capacityAlertsEnabled,
            "training_updates_enabled" to trainingUpdatesEnabled,
            "weekly_reports_enabled" to weeklyReportsEnabled,
            "proactive_alerts_enabled" to proactiveAlertsEnabled,
            "rotation_reminder_hours" to rotationReminderHours,
            "capacity_check_minutes" to capacityCheckMinutes,
            "weekly_report_day" to weeklyReportDay,
            "weekly_report_hour" to weeklyReportHour,
            "critical_capacity_threshold" to criticalCapacityThreshold,
            "low_performance_threshold" to lowPerformanceThreshold,
            "long_rotation_threshold_hours" to longRotationThresholdHours,
            "work_start_hour" to workStartHour,
            "work_end_hour" to workEndHour,
            "quiet_hours_enabled" to quietHoursEnabled
        )
    }
    
    /**
     * Restaura configuración por defecto
     */
    fun resetToDefaults() {
        prefs.edit().clear().apply()
    }
    
    /**
     * Exporta configuración como JSON string
     */
    fun exportSettings(): String {
        val settings = getAllSettings()
        return settings.entries.joinToString(",\n") { (key, value) ->
            "\"$key\": ${if (value is String) "\"$value\"" else value}"
        }.let { "{\n$it\n}" }
    }
    
    enum class NotificationType {
        ROTATION_REMINDER,
        CAPACITY_ALERT,
        TRAINING_UPDATE,
        WEEKLY_REPORT,
        PROACTIVE_ALERT
    }
}