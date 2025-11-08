package com.workstation.rotation.security

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.workstation.rotation.MainActivity
import com.workstation.rotation.databinding.ActivityLoginBinding
import kotlinx.coroutines.launch

/**
 * Actividad de login seguro con autenticación biométrica
 */
class LoginActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityLoginBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var biometricAuthManager: BiometricAuthManager
    private lateinit var deviceSecurityChecker: DeviceSecurityChecker
    
    private var loginAttempts = 0
    private val maxLoginAttempts = 3
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Inicializar componentes de seguridad
        initializeSecurityComponents()
        
        // Verificar seguridad del dispositivo
        performDeviceSecurityCheck()
        
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupUI()
        checkExistingSession()
    }
    
    private fun initializeSecurityComponents() {
        sessionManager = SessionManager.getInstance(this)
        biometricAuthManager = BiometricAuthManager(this)
        deviceSecurityChecker = DeviceSecurityChecker(this)
        
        // Inicializar logger de seguridad
        SecurityLogger.initialize(this)
    }
    
    private fun performDeviceSecurityCheck() {
        lifecycleScope.launch {
            val securityStatus = deviceSecurityChecker.performSecurityCheck()
            
            when (securityStatus.securityLevel) {
                SecurityLevel.CRITICAL -> {
                    showSecurityWarning(
                        "Dispositivo Comprometido",
                        "Este dispositivo presenta riesgos críticos de seguridad. La aplicación funcionará con limitaciones."
                    )
                }
                SecurityLevel.HIGH -> {
                    showSecurityWarning(
                        "Advertencia de Seguridad",
                        "Se detectaron configuraciones que pueden comprometer la seguridad."
                    )
                }
                else -> {
                    // Dispositivo seguro, continuar normalmente
                }
            }
        }
    }
    
    private fun setupUI() {
        // Configurar botón de login tradicional
        binding.btnLogin.setOnClickListener {
            performTraditionalLogin()
        }
        
        // Configurar botón de login biométrico
        binding.btnBiometricLogin.setOnClickListener {
            performBiometricLogin()
        }
        
        // Verificar disponibilidad biométrica
        val biometricAvailability = biometricAuthManager.isBiometricAvailable()
        binding.btnBiometricLogin.visibility = if (biometricAvailability == BiometricAvailability.AVAILABLE) {
            View.VISIBLE
        } else {
            View.GONE
        }
        
        // Mostrar información de capacidades biométricas
        if (biometricAvailability != BiometricAvailability.AVAILABLE) {
            binding.tvBiometricStatus.text = biometricAvailability.message
            binding.tvBiometricStatus.visibility = View.VISIBLE
        }
    }
    
    private fun checkExistingSession() {
        // Restaurar sesiones desde preferencias
        sessionManager.restoreSessionsFromPrefs()
        
        // Verificar si hay una sesión activa válida
        val savedToken = getSharedPreferences("app_prefs", MODE_PRIVATE)
            .getString("current_session_token", null)
        
        if (savedToken != null) {
            when (val validation = sessionManager.validateSession(savedToken)) {
                is SessionValidation.Valid -> {
                    // Sesión válida, ir directamente a MainActivity
                    navigateToMainActivity(validation.session)
                    return
                }
                is SessionValidation.Expired -> {
                    showMessage("Sesión expirada. Por favor, inicia sesión nuevamente.")
                }
                is SessionValidation.Invalid -> {
                    showMessage("Sesión inválida. Por favor, inicia sesión.")
                }
            }
        }
    }
    
    private fun performTraditionalLogin() {
        val username = binding.etUsername.text.toString().trim()
        val password = binding.etPassword.text.toString()
        
        if (username.isEmpty() || password.isEmpty()) {
            showMessage("Por favor, completa todos los campos")
            return
        }
        
        // Mostrar loading
        setLoadingState(true)
        
        lifecycleScope.launch {
            try {
                // Validar credenciales (implementar según tu lógica de negocio)
                val loginResult = validateCredentials(username, password)
                
                if (loginResult.isSuccess) {
                    val userRole = loginResult.userRole ?: UserRole.VIEWER
                    val deviceId = getDeviceIdSecure()
                    
                    // Crear sesión
                    when (val sessionResult = sessionManager.startSession(username, userRole, deviceId)) {
                        is SessionResult.Success -> {
                            // Guardar token de sesión
                            saveSessionToken(sessionResult.session.token)
                            
                            SecurityLogger.logInfo(
                                "Login tradicional exitoso",
                                "Usuario: $username, Role: $userRole",
                                username
                            )
                            
                            navigateToMainActivity(sessionResult.session)
                        }
                        is SessionResult.Error -> {
                            showMessage("Error creando sesión: ${sessionResult.message}")
                            SecurityLogger.logError(
                                "Error creando sesión después de login exitoso",
                                Exception(sessionResult.message),
                                username
                            )
                        }
                    }
                } else {
                    handleFailedLogin(username, loginResult.errorMessage ?: "Error desconocido")
                }
                
            } catch (e: Exception) {
                SecurityLogger.logError("Error en proceso de login", e, username)
                showMessage("Error interno del sistema")
            } finally {
                setLoadingState(false)
            }
        }
    }
    
    private fun performBiometricLogin() {
        // Verificar si hay credenciales guardadas para login biométrico
        val savedUsername = getSharedPreferences("app_prefs", MODE_PRIVATE)
            .getString("biometric_username", null)
        
        if (savedUsername == null) {
            showMessage("No hay credenciales configuradas para login biométrico")
            return
        }
        
        biometricAuthManager.authenticateUser(
            title = "Login Biométrico",
            subtitle = "Verifica tu identidad para acceder",
            description = "Usuario: $savedUsername",
            callback = object : BiometricAuthManager.AuthCallback {
                override fun onAuthenticationSucceeded() {
                    lifecycleScope.launch {
                        // Obtener rol del usuario guardado
                        val userRole = getUserRole(savedUsername ?: "viewer")
                        val deviceId = getDeviceIdSecure()
                        
                        when (val sessionResult = sessionManager.startSession(savedUsername ?: "viewer", userRole, deviceId)) {
                            is SessionResult.Success -> {
                                saveSessionToken(sessionResult.session.token)
                                
                                SecurityLogger.logInfo(
                                    "Login biométrico exitoso",
                                    "Usuario: $savedUsername",
                                    savedUsername
                                )
                                
                                navigateToMainActivity(sessionResult.session)
                            }
                            is SessionResult.Error -> {
                                showMessage("Error creando sesión: ${sessionResult.message}")
                            }
                        }
                    }
                }
                
                override fun onAuthenticationFailed(error: String) {
                    showMessage("Autenticación biométrica fallida: $error")
                    SecurityLogger.logWarning(
                        "Login biométrico fallido",
                        error,
                        savedUsername
                    )
                }
                
                override fun onAuthenticationError(errorCode: Int, errorMessage: String) {
                    showMessage("Error biométrico: $errorMessage")
                    SecurityLogger.logError(
                        "Error en login biométrico",
                        Exception("Code: $errorCode, Message: $errorMessage"),
                        savedUsername ?: "unknown"
                    )
                }
            }
        )
    }
    
    private fun handleFailedLogin(username: String, errorMessage: String) {
        loginAttempts++
        
        SecurityLogger.logWarning(
            "Intento de login fallido",
            "Usuario: $username, Intento: $loginAttempts, Error: $errorMessage",
            username
        )
        
        if (loginAttempts >= maxLoginAttempts) {
            // Bloquear temporalmente
            SecurityLogger.logCritical(
                "Múltiples intentos de login fallidos",
                "Usuario: $username, Intentos: $loginAttempts",
                username
            )
            
            showMessage("Demasiados intentos fallidos. Intenta nuevamente en 15 minutos.")
            
            // Deshabilitar botones por 15 minutos
            disableLoginButtons()
            
            lifecycleScope.launch {
                kotlinx.coroutines.delay(15 * 60 * 1000) // 15 minutos
                enableLoginButtons()
                loginAttempts = 0
            }
        } else {
            val remainingAttempts = maxLoginAttempts - loginAttempts
            showMessage("Credenciales incorrectas. Intentos restantes: $remainingAttempts")
        }
    }
    
    private fun navigateToMainActivity(session: SessionInfo) {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("session_id", session.sessionId)
            putExtra("user_id", session.userId)
            putExtra("user_role", session.userRole.name)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
    
    private fun showSecurityWarning(title: String, message: String) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Entendido") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }
    
    private fun setLoadingState(isLoading: Boolean) {
        binding.btnLogin.isEnabled = !isLoading
        binding.btnBiometricLogin.isEnabled = !isLoading
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
    
    private fun disableLoginButtons() {
        binding.btnLogin.isEnabled = false
        binding.btnBiometricLogin.isEnabled = false
    }
    
    private fun enableLoginButtons() {
        binding.btnLogin.isEnabled = true
        binding.btnBiometricLogin.isEnabled = true
    }
    
    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
    
    private fun saveSessionToken(token: String) {
        getSharedPreferences("app_prefs", MODE_PRIVATE)
            .edit()
            .putString("current_session_token", token)
            .apply()
    }
    
    private fun getDeviceIdSecure(): String {
        return android.provider.Settings.Secure.getString(
            contentResolver,
            android.provider.Settings.Secure.ANDROID_ID
        ) ?: "unknown_device"
    }
    
    // Métodos placeholder - implementar según lógica de negocio
    private suspend fun validateCredentials(username: String, password: String): LoginResult {
        // Implementar validación real de credenciales
        // Por ahora, credenciales de prueba
        return when {
            username == "admin" && password == "admin123" -> 
                LoginResult(true, UserRole.SUPER_ADMIN, null)
            username == "supervisor" && password == "super123" -> 
                LoginResult(true, UserRole.AREA_SUPERVISOR, null)
            username == "viewer" && password == "view123" -> 
                LoginResult(true, UserRole.VIEWER, null)
            else -> 
                LoginResult(false, null, "Credenciales incorrectas")
        }
    }
    
    private fun getUserRole(username: String): UserRole {
        // Implementar obtención de rol desde base de datos
        return when (username) {
            "admin" -> UserRole.SUPER_ADMIN
            "supervisor" -> UserRole.AREA_SUPERVISOR
            else -> UserRole.VIEWER
        }
    }
}

/**
 * Resultado de validación de login
 */
data class LoginResult(
    val isSuccess: Boolean,
    val userRole: UserRole?,
    val errorMessage: String?
)