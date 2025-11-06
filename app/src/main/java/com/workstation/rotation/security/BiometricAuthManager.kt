package com.workstation.rotation.security

import androidx.fragment.app.FragmentActivity

/**
 * Gestor de autenticación biométrica simplificado
 */
class BiometricAuthManager(private val activity: FragmentActivity) {
    
    interface AuthCallback {
        fun onAuthenticationSucceeded()
        fun onAuthenticationFailed(error: String)
        fun onAuthenticationError(errorCode: Int, errorMessage: String)
    }
    
    fun isBiometricAvailable(): BiometricAvailability {
        return BiometricAvailability.AVAILABLE
    }
    
    fun authenticateUser(
        title: String = "Autenticación Requerida",
        subtitle: String = "Usa tu huella dactilar",
        description: String = "Acceso seguro",
        callback: AuthCallback
    ) {
        // Simulación de autenticación exitosa para testing
        callback.onAuthenticationSucceeded()
    }
}

enum class BiometricAvailability(val message: String) {
    AVAILABLE("Autenticación biométrica disponible"),
    NO_HARDWARE("No hay hardware biométrico disponible")
}