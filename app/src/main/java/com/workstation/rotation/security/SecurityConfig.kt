package com.workstation.rotation.security

import android.content.Context
import android.content.SharedPreferences

/**
 * Configuración del sistema de seguridad
 * Permite activar/desactivar características de seguridad
 */
object SecurityConfig {
    
    private const val PREFS_NAME = "security_config"
    private const val KEY_SECURITY_ENABLED = "security_enabled"
    private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
    private const val KEY_DEVICE_CHECK_ENABLED = "device_check_enabled"
    private const val KEY_SESSION_TIMEOUT_MINUTES = "session_timeout_minutes"
    
    // Valores por defecto
    private const val DEFAULT_SECURITY_ENABLED = false // Desactivado por defecto
    private const val DEFAULT_BIOMETRIC_ENABLED = true
    private const val DEFAULT_DEVICE_CHECK_ENABLED = true
    private const val DEFAULT_SESSION_TIMEOUT = 30
    
    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    /**
     * Verifica si el sistema de seguridad está activado
     */
    fun isSecurityEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_SECURITY_ENABLED, DEFAULT_SECURITY_ENABLED)
    }
    
    /**
     * Activa o desactiva el sistema de seguridad
     */
    fun setSecurityEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_SECURITY_ENABLED, enabled).apply()
    }
    
    /**
     * Verifica si la autenticación biométrica está habilitada
     */
    fun isBiometricEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_BIOMETRIC_ENABLED, DEFAULT_BIOMETRIC_ENABLED)
    }
    
    /**
     * Activa o desactiva la autenticación biométrica
     */
    fun setBiometricEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply()
    }
    
    /**
     * Verifica si la verificación de dispositivo está habilitada
     */
    fun isDeviceCheckEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_DEVICE_CHECK_ENABLED, DEFAULT_DEVICE_CHECK_ENABLED)
    }
    
    /**
     * Activa o desactiva la verificación de dispositivo
     */
    fun setDeviceCheckEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_DEVICE_CHECK_ENABLED, enabled).apply()
    }
    
    /**
     * Obtiene el timeout de sesión en minutos
     */
    fun getSessionTimeoutMinutes(context: Context): Int {
        return getPrefs(context).getInt(KEY_SESSION_TIMEOUT_MINUTES, DEFAULT_SESSION_TIMEOUT)
    }
    
    /**
     * Establece el timeout de sesión en minutos
     */
    fun setSessionTimeoutMinutes(context: Context, minutes: Int) {
        getPrefs(context).edit().putInt(KEY_SESSION_TIMEOUT_MINUTES, minutes).apply()
    }
    
    /**
     * Resetea toda la configuración de seguridad a valores por defecto
     */
    fun resetToDefaults(context: Context) {
        getPrefs(context).edit().clear().apply()
    }
    
    /**
     * Obtiene un resumen de la configuración actual
     */
    fun getConfigSummary(context: Context): String {
        return buildString {
            appendLine("=== CONFIGURACIÓN DE SEGURIDAD ===")
            appendLine("Sistema de Seguridad: ${if (isSecurityEnabled(context)) "ACTIVADO" else "DESACTIVADO"}")
            appendLine("Autenticación Biométrica: ${if (isBiometricEnabled(context)) "Habilitada" else "Deshabilitada"}")
            appendLine("Verificación de Dispositivo: ${if (isDeviceCheckEnabled(context)) "Habilitada" else "Deshabilitada"}")
            appendLine("Timeout de Sesión: ${getSessionTimeoutMinutes(context)} minutos")
        }
    }
}