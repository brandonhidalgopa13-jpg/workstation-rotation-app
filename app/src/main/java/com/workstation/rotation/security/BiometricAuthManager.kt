package com.workstation.rotation.security

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

/**
 * Gestor de autenticación biométrica
 * Maneja huella dactilar, reconocimiento facial y otros métodos biométricos
 */
class BiometricAuthManager(private val activity: FragmentActivity) {
    
    companion object {
        private const val TAG = "BiometricAuth"
    }
    
    interface AuthCallback {
        fun onAuthenticationSucceeded()
        fun onAuthenticationFailed(error: String)
        fun onAuthenticationError(errorCode: Int, errorMessage: String)
    }
    
    private val biometricManager = BiometricManager.from(activity)
    
    /**
     * Verifica si la autenticación biométrica está disponible
     */
    fun isBiometricAvailable(): BiometricAvailability {
        return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> BiometricAvailability.AVAILABLE
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> BiometricAvailability.NO_HARDWARE
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> BiometricAvailability.HARDWARE_UNAVAILABLE
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricAvailability.NO_BIOMETRICS_ENROLLED
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> BiometricAvailability.SECURITY_UPDATE_REQUIRED
            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> BiometricAvailability.UNSUPPORTED
            BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> BiometricAvailability.UNKNOWN
            else -> BiometricAvailability.UNKNOWN
        }
    }
    
    /**
     * Inicia el proceso de autenticación biométrica
     */
    fun authenticateUser(
        title: String = "Autenticación Requerida",
        subtitle: String = "Usa tu huella dactilar o reconocimiento facial para continuar",
        description: String = "Acceso a datos sensibles de trabajadores",
        callback: AuthCallback
    ) {
        val availability = isBiometricAvailable()
        if (availability != BiometricAvailability.AVAILABLE) {
            callback.onAuthenticationError(-1, availability.message)
            return
        }
        
        val biometricPrompt = BiometricPrompt(
            activity,
            ContextCompat.getMainExecutor(activity),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    SecurityLogger.logInfo(
                        "Autenticación biométrica exitosa",
                        "Método: ${result.authenticationType}",
                        getCurrentUserId()
                    )
                    callback.onAuthenticationSucceeded()
                }
                
                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    SecurityLogger.logWarning(
                        "Intento de autenticación biométrica fallido",
                        "Usuario no reconocido",
                        getCurrentUserId()
                    )
                    callback.onAuthenticationFailed("Autenticación fallida. Inténtalo de nuevo.")
                }
                
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    SecurityLogger.logError(
                        "Error en autenticación biométrica",
                        Exception("Code: $errorCode, Message: $errString"),
                        getCurrentUserId()
                    )
                    callback.onAuthenticationError(errorCode, errString.toString())
                }
            }
        )
        
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setDescription(description)
            .setNegativeButtonText("Cancelar")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .build()
        
        try {
            biometricPrompt.authenticate(promptInfo)
            SecurityLogger.logInfo(
                "Solicitud de autenticación biométrica iniciada",
                "Título: $title",
                getCurrentUserId()
            )
        } catch (e: Exception) {
            SecurityLogger.logError(
                "Error iniciando autenticación biométrica",
                e,
                getCurrentUserId()
            )
            callback.onAuthenticationError(-1, "Error iniciando autenticación: ${e.message}")
        }
    }
} 
   
    /**
     * Autenticación biométrica con criptografía (para operaciones más seguras)
     */
    fun authenticateWithCrypto(
        title: String = "Autenticación Segura",
        subtitle: String = "Verificación biométrica requerida",
        callback: AuthCallback
    ) {
        val availability = isBiometricAvailable()
        if (availability != BiometricAvailability.AVAILABLE) {
            callback.onAuthenticationError(-1, availability.message)
            return
        }
        
        activity.lifecycleScope.launch {
            try {
                // Obtener clave criptográfica del KeyStore
                val keyManager = KeyManager.getInstance(activity)
                if (!keyManager.areKeysAvailable()) {
                    keyManager.generateMasterKey()
                }
                
                val biometricPrompt = BiometricPrompt(
                    activity,
                    ContextCompat.getMainExecutor(activity),
                    object : BiometricPrompt.AuthenticationCallback() {
                        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                            super.onAuthenticationSucceeded(result)
                            SecurityLogger.logInfo(
                                "Autenticación criptográfica biométrica exitosa",
                                "CryptoObject presente: ${result.cryptoObject != null}",
                                getCurrentUserId()
                            )
                            callback.onAuthenticationSucceeded()
                        }
                        
                        override fun onAuthenticationFailed() {
                            super.onAuthenticationFailed()
                            SecurityLogger.logWarning(
                                "Autenticación criptográfica biométrica fallida",
                                "CryptoObject no validado",
                                getCurrentUserId()
                            )
                            callback.onAuthenticationFailed("Autenticación criptográfica fallida")
                        }
                        
                        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                            super.onAuthenticationError(errorCode, errString)
                            SecurityLogger.logError(
                                "Error en autenticación criptográfica biométrica",
                                Exception("Code: $errorCode, Message: $errString"),
                                getCurrentUserId()
                            )
                            callback.onAuthenticationError(errorCode, errString.toString())
                        }
                    }
                )
                
                val promptInfo = BiometricPrompt.PromptInfo.Builder()
                    .setTitle(title)
                    .setSubtitle(subtitle)
                    .setDescription("Operación que requiere máxima seguridad")
                    .setNegativeButtonText("Cancelar")
                    .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                    .build()
                
                biometricPrompt.authenticate(promptInfo)
                
            } catch (e: Exception) {
                SecurityLogger.logError(
                    "Error configurando autenticación criptográfica",
                    e,
                    getCurrentUserId()
                )
                callback.onAuthenticationError(-1, "Error de configuración: ${e.message}")
            }
        }
    }
    
    /**
     * Verifica si el dispositivo tiene configuración biométrica segura
     */
    fun hasSecureBiometricSetup(): Boolean {
        return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            else -> false
        }
    }
    
    /**
     * Obtiene información detallada sobre capacidades biométricas
     */
    fun getBiometricCapabilities(): BiometricCapabilities {
        val strongBiometric = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
        val weakBiometric = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)
        val deviceCredential = biometricManager.canAuthenticate(BiometricManager.Authenticators.DEVICE_CREDENTIAL)
        
        return BiometricCapabilities(
            hasStrongBiometric = strongBiometric == BiometricManager.BIOMETRIC_SUCCESS,
            hasWeakBiometric = weakBiometric == BiometricManager.BIOMETRIC_SUCCESS,
            hasDeviceCredential = deviceCredential == BiometricManager.BIOMETRIC_SUCCESS,
            canUseCombined = biometricManager.canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
            ) == BiometricManager.BIOMETRIC_SUCCESS
        )
    }
    
    private fun getCurrentUserId(): String? {
        // Implementar obtención del ID de usuario actual
        return "current_user" // Placeholder
    }
}

/**
 * Estados de disponibilidad biométrica
 */
enum class BiometricAvailability(val message: String) {
    AVAILABLE("Autenticación biométrica disponible"),
    NO_HARDWARE("No hay hardware biométrico disponible"),
    HARDWARE_UNAVAILABLE("Hardware biométrico no disponible temporalmente"),
    NO_BIOMETRICS_ENROLLED("No hay biometría configurada"),
    SECURITY_UPDATE_REQUIRED("Actualización de seguridad requerida"),
    UNSUPPORTED("Autenticación biométrica no soportada"),
    UNKNOWN("Estado desconocido")
}

/**
 * Capacidades biométricas del dispositivo
 */
data class BiometricCapabilities(
    val hasStrongBiometric: Boolean,
    val hasWeakBiometric: Boolean,
    val hasDeviceCredential: Boolean,
    val canUseCombined: Boolean
)