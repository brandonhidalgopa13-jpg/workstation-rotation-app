package com.workstation.rotation

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.workstation.rotation.notifications.IntelligentNotificationSystem

/**
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 🚀 APLICACIÓN PRINCIPAL - INICIALIZACIÓN DEL SISTEMA
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 
 * Clase Application que se ejecuta al iniciar la app y configura los sistemas principales:
 * • Sistema de notificaciones inteligentes
 * • Configuraciones globales
 * • Inicialización de servicios en background
 * 
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 */

class RotationApplication : Application() {
    
    private lateinit var notificationSystem: IntelligentNotificationSystem
    
    override fun onCreate() {
        super.onCreate()
        
        // Forzar modo claro (desactivar modo oscuro)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        
        // Inicializar sistema de notificaciones inteligentes
        initializeNotificationSystem()
    }
    
    /**
     * Inicializa el sistema de notificaciones inteligentes
     */
    private fun initializeNotificationSystem() {
        try {
            notificationSystem = IntelligentNotificationSystem(this)
            
            // El sistema se inicializa automáticamente y programa las verificaciones periódicas
            // No es necesario hacer nada más aquí
            
        } catch (e: Exception) {
            // Log error silently - no queremos que la app crashee por notificaciones
            e.printStackTrace()
        }
    }
    
    /**
     * Obtiene la instancia del sistema de notificaciones
     */
    fun getNotificationSystem(): IntelligentNotificationSystem? {
        return if (::notificationSystem.isInitialized) notificationSystem else null
    }
}