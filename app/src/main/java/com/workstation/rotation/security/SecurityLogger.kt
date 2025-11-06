package com.workstation.rotation.security

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Sistema de logging de seguridad para auditoría y monitoreo
 */
object SecurityLogger {
    
    private const val TAG = "SecurityLogger"
    private const val MAX_LOG_ENTRIES = 10000
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
    
    enum class SecurityLevel {
        INFO, WARNING, ERROR, CRITICAL
    }
    
    data class SecurityEvent(
        val timestamp: Long,
        val level: SecurityLevel,
        val event: String,
        val details: String,
        val userId: String? = null,
        val deviceId: String? = null,
        val ipAddress: String? = null
    )
    
    private val securityEvents = mutableListOf<SecurityEvent>()
    private var context: Context? = null
    
    fun initialize(context: Context) {
        this.context = context.applicationContext
    }
    
    fun logInfo(event: String, details: String = "", userId: String? = null) {
        logEvent(SecurityLevel.INFO, event, details, userId)
    }
    
    fun logWarning(event: String, details: String = "", userId: String? = null) {
        logEvent(SecurityLevel.WARNING, event, details, userId)
    }
    
    fun logError(event: String, throwable: Throwable? = null, userId: String? = null) {
        val details = throwable?.let { "${it.javaClass.simpleName}: ${it.message}" } ?: ""
        logEvent(SecurityLevel.ERROR, event, details, userId)
    }
}  
  
    fun logCritical(event: String, details: String = "", userId: String? = null) {
        logEvent(SecurityLevel.CRITICAL, event, details, userId)
        // Para eventos críticos, también log a sistema Android
        Log.e(TAG, "CRITICAL SECURITY EVENT: $event - $details")
    }
    
    private fun logEvent(level: SecurityLevel, event: String, details: String, userId: String?) {
        val securityEvent = SecurityEvent(
            timestamp = System.currentTimeMillis(),
            level = level,
            event = event,
            details = details,
            userId = userId,
            deviceId = getDeviceId(),
            ipAddress = getLocalIpAddress()
        )
        
        // Añadir a lista en memoria
        synchronized(securityEvents) {
            securityEvents.add(securityEvent)
            
            // Mantener solo los últimos MAX_LOG_ENTRIES
            if (securityEvents.size > MAX_LOG_ENTRIES) {
                securityEvents.removeAt(0)
            }
        }
        
        // Log a Android Log para debugging
        val logMessage = "${dateFormat.format(Date(securityEvent.timestamp))} [$level] $event: $details"
        when (level) {
            SecurityLevel.INFO -> Log.i(TAG, logMessage)
            SecurityLevel.WARNING -> Log.w(TAG, logMessage)
            SecurityLevel.ERROR -> Log.e(TAG, logMessage)
            SecurityLevel.CRITICAL -> Log.e(TAG, "CRITICAL: $logMessage")
        }
        
        // Persistir eventos críticos inmediatamente
        if (level == SecurityLevel.CRITICAL || level == SecurityLevel.ERROR) {
            CoroutineScope(Dispatchers.IO).launch {
                persistSecurityEvent(securityEvent)
            }
        }
    }
    
    fun getSecurityEvents(level: SecurityLevel? = null, limit: Int = 100): List<SecurityEvent> {
        synchronized(securityEvents) {
            return securityEvents
                .let { if (level != null) it.filter { event -> event.level == level } else it }
                .takeLast(limit)
                .toList()
        }
    }
    
    fun getSecurityEventsInRange(startTime: Long, endTime: Long): List<SecurityEvent> {
        synchronized(securityEvents) {
            return securityEvents.filter { it.timestamp in startTime..endTime }
        }
    }
    
    private fun persistSecurityEvent(event: SecurityEvent) {
        // Implementar persistencia en base de datos encriptada
        // Por ahora, solo logging
        Log.d(TAG, "Persisting security event: ${event.event}")
    }
    
    private fun getDeviceId(): String? {
        return context?.let { 
            android.provider.Settings.Secure.getString(
                it.contentResolver,
                android.provider.Settings.Secure.ANDROID_ID
            )
        }
    }
    
    private fun getLocalIpAddress(): String? {
        return try {
            val interfaces = java.net.NetworkInterface.getNetworkInterfaces()
            for (networkInterface in interfaces) {
                val addresses = networkInterface.inetAddresses
                for (address in addresses) {
                    if (!address.isLoopbackAddress && address is java.net.Inet4Address) {
                        return address.hostAddress
                    }
                }
            }
            null
        } catch (e: Exception) {
            null
        }
    }
    
    fun clearLogs() {
        synchronized(securityEvents) {
            securityEvents.clear()
        }
        Log.i(TAG, "Security logs cleared")
    }
    
    fun exportSecurityReport(): String {
        synchronized(securityEvents) {
            val report = StringBuilder()
            report.appendLine("=== SECURITY AUDIT REPORT ===")
            report.appendLine("Generated: ${dateFormat.format(Date())}")
            report.appendLine("Total Events: ${securityEvents.size}")
            report.appendLine()
            
            val eventsByLevel = securityEvents.groupBy { it.level }
            eventsByLevel.forEach { (level, events) ->
                report.appendLine("$level: ${events.size} events")
            }
            report.appendLine()
            
            securityEvents.takeLast(50).forEach { event ->
                report.appendLine("${dateFormat.format(Date(event.timestamp))} [$${event.level}] ${event.event}")
                if (event.details.isNotEmpty()) {
                    report.appendLine("  Details: ${event.details}")
                }
                event.userId?.let { report.appendLine("  User: $it") }
                report.appendLine()
            }
            
            return report.toString()
        }
    }
}