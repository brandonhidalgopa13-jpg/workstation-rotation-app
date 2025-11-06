package com.workstation.rotation.security

import android.content.Context
import android.content.SharedPreferences

/**
 * Gestor de claves simplificado
 */
class KeyManager private constructor(private val context: Context) {
    
    companion object {
        @Volatile
        private var INSTANCE: KeyManager? = null
        
        fun getInstance(context: Context): KeyManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: KeyManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    fun generateMasterKey(): Boolean {
        return true // Implementación simplificada
    }
    
    fun generateDatabaseKey(): String? {
        return "default_key" // Implementación simplificada
    }
    
    fun getDatabaseKey(): String? {
        return "default_key"
    }
    
    fun areKeysAvailable(): Boolean {
        return true
    }
    
    fun getEncryptedSharedPreferences(): SharedPreferences {
        return context.getSharedPreferences("secure_prefs", Context.MODE_PRIVATE)
    }
}