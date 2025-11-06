package com.workstation.rotation.security

import android.content.Context
import android.util.Log

/**
 * Sistema de logging de seguridad simplificado
 */
object SecurityLogger {
    
    private const val TAG = "SecurityLogger"
    
    enum class SecurityLevel {
        INFO, WARNING, ERROR, CRITICAL
    }
    
    fun initialize(context: Context) {
        // Inicialización básica
    }
    
    fun logInfo(event: String, details: String = "", userId: String? = null) {
        Log.i(TAG, "INFO: $event - $details")
    }
    
    fun logWarning(event: String, details: String = "", userId: String? = null) {
        Log.w(TAG, "WARNING: $event - $details")
    }
    
    fun logError(event: String, throwable: Throwable? = null, userId: String? = null) {
        Log.e(TAG, "ERROR: $event", throwable)
    }
    
    fun logCritical(event: String, details: String = "", userId: String? = null) {
        Log.e(TAG, "CRITICAL: $event - $details")
    }
}