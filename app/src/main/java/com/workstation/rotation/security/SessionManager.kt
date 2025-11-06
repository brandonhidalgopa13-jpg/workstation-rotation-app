package com.workstation.rotation.security

import android.content.Context
import android.content.SharedPreferences
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import kotlinx.coroutines.*
import java.security.Key
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Gestor de sesiones seguras con JWT y timeouts automáticos
 */
class SessionManager private constructor(private val context: Context) {
    
    companion object {
        private const val TAG = "SessionManager"
        private const val SESSION_TIMEOUT_MINUTES = 30L
        private const val REFRESH_TOKEN_DAYS = 7L
        private const val PREFS_NAME = "secure_session_prefs"
        
        @Volatile
        private var INSTANCE: SessionManager? = null
        
        fun getInstance(context: Context): SessionManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SessionManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    private val keyManager = KeyManager.getInstance(context)
    private val activeSessions = ConcurrentHashMap<String, SessionInfo>()
    private val sessionTimeoutJobs = ConcurrentHashMap<String, Job>()
    private val sessionScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    
    private val jwtKey: Key by lazy {
        Keys.secretKeyFor(SignatureAlgorithm.HS256)
    }
    
    /**
     * Inicia una nueva sesión de usuario
     */
    fun startSession(userId: String, userRole: UserRole, deviceId: String): SessionResult {
        return try {
            // Verificar si ya existe una sesión activa
            val existingSession = getActiveSession(userId)
            if (existingSession != null) {
                SecurityLogger.logWarning(
                    "Intento de crear sesión duplicada",
                    "Usuario: $userId ya tiene sesión activa",
                    userId
                )
                return SessionResult.Error("Ya existe una sesión activa para este usuario")
            }
            
            val sessionId = generateSessionId()
            val currentTime = System.currentTimeMillis()
            val expirationTime = currentTime + (SESSION_TIMEOUT_MINUTES * 60 * 1000)
            
            // Crear JWT token
            val token = createJwtToken(sessionId, userId, userRole, expirationTime)
            
            // Crear información de sesión
            val sessionInfo = SessionInfo(
                sessionId = sessionId,
                userId = userId,
                userRole = userRole,
                deviceId = deviceId,
                startTime = currentTime,
                lastActivity = currentTime,
                expirationTime = expirationTime,
                token = token,
                isActive = true
            )
            
            // Almacenar sesión
            activeSessions[sessionId] = sessionInfo
            saveSessionToPrefs(sessionInfo)
            
            // Configurar timeout automático
            setupSessionTimeout(sessionId)
            
            SecurityLogger.logInfo(
                "Sesión iniciada exitosamente",
                "SessionId: $sessionId, Role: $userRole",
                userId
            )
            
            SessionResult.Success(sessionInfo)
            
        } catch (e: Exception) {
            SecurityLogger.logError("Error iniciando sesión", e, userId)
            SessionResult.Error("Error interno del sistema")
        }
    }
    
    /**
     * Valida y renueva una sesión existente
     */
    fun validateSession(token: String): SessionValidation {
        return try {
            // Parsear JWT token
            val claims = parseJwtToken(token)
            val sessionId = claims.subject
            val userId = claims.get("userId", String::class.java)
            
            // Buscar sesión activa
            val session = activeSessions[sessionId]
                ?: return SessionValidation.Invalid("Sesión no encontrada")
            
            // Verificar si la sesión está activa
            if (!session.isActive) {
                return SessionValidation.Invalid("Sesión inactiva")
            }
            
            // Verificar expiración
            val currentTime = System.currentTimeMillis()
            if (currentTime > session.expirationTime) {
                endSession(sessionId)
                return SessionValidation.Expired("Sesión expirada")
            }
            
            // Actualizar última actividad
            val updatedSession = session.copy(lastActivity = currentTime)
            activeSessions[sessionId] = updatedSession
            saveSessionToPrefs(updatedSession)
            
            // Resetear timeout
            setupSessionTimeout(sessionId)
            
            SessionValidation.Valid(updatedSession)
            
        } catch (e: Exception) {
            SecurityLogger.logError("Error validando sesión", e)
            SessionValidation.Invalid("Token inválido")
        }
    }
}  
  
    /**
     * Termina una sesión específica
     */
    fun endSession(sessionId: String): Boolean {
        return try {
            val session = activeSessions[sessionId]
            if (session != null) {
                // Marcar como inactiva
                val inactiveSession = session.copy(isActive = false)
                activeSessions[sessionId] = inactiveSession
                
                // Cancelar timeout job
                sessionTimeoutJobs[sessionId]?.cancel()
                sessionTimeoutJobs.remove(sessionId)
                
                // Remover de preferencias
                removeSessionFromPrefs(sessionId)
                
                SecurityLogger.logInfo(
                    "Sesión terminada",
                    "SessionId: $sessionId",
                    session.userId
                )
                
                true
            } else {
                false
            }
        } catch (e: Exception) {
            SecurityLogger.logError("Error terminando sesión", e)
            false
        }
    }
    
    /**
     * Termina todas las sesiones de un usuario
     */
    fun endAllUserSessions(userId: String): Int {
        var endedSessions = 0
        activeSessions.values.filter { it.userId == userId && it.isActive }.forEach { session ->
            if (endSession(session.sessionId)) {
                endedSessions++
            }
        }
        
        SecurityLogger.logInfo(
            "Sesiones de usuario terminadas",
            "Usuario: $userId, Sesiones: $endedSessions",
            userId
        )
        
        return endedSessions
    }
    
    /**
     * Obtiene la sesión activa de un usuario
     */
    fun getActiveSession(userId: String): SessionInfo? {
        return activeSessions.values.find { it.userId == userId && it.isActive }
    }
    
    /**
     * Obtiene todas las sesiones activas
     */
    fun getActiveSessions(): List<SessionInfo> {
        return activeSessions.values.filter { it.isActive }
    }
    
    /**
     * Limpia sesiones expiradas
     */
    fun cleanupExpiredSessions() {
        val currentTime = System.currentTimeMillis()
        val expiredSessions = activeSessions.values.filter { 
            it.isActive && currentTime > it.expirationTime 
        }
        
        expiredSessions.forEach { session ->
            endSession(session.sessionId)
        }
        
        if (expiredSessions.isNotEmpty()) {
            SecurityLogger.logInfo(
                "Sesiones expiradas limpiadas",
                "Cantidad: ${expiredSessions.size}"
            )
        }
    }
    
    /**
     * Genera un ID único para la sesión
     */
    private fun generateSessionId(): String {
        return UUID.randomUUID().toString() + "_" + System.currentTimeMillis()
    }
    
    /**
     * Crea un JWT token
     */
    private fun createJwtToken(sessionId: String, userId: String, userRole: UserRole, expirationTime: Long): String {
        return Jwts.builder()
            .setSubject(sessionId)
            .claim("userId", userId)
            .claim("userRole", userRole.name)
            .setIssuedAt(Date())
            .setExpiration(Date(expirationTime))
            .signWith(jwtKey)
            .compact()
    }
    
    /**
     * Parsea un JWT token
     */
    private fun parseJwtToken(token: String): Claims {
        return Jwts.parserBuilder()
            .setSigningKey(jwtKey)
            .build()
            .parseClaimsJws(token)
            .body
    }
    
    /**
     * Configura timeout automático para una sesión
     */
    private fun setupSessionTimeout(sessionId: String) {
        // Cancelar job anterior si existe
        sessionTimeoutJobs[sessionId]?.cancel()
        
        // Crear nuevo job de timeout
        val timeoutJob = sessionScope.launch {
            delay(SESSION_TIMEOUT_MINUTES * 60 * 1000)
            
            val session = activeSessions[sessionId]
            if (session?.isActive == true) {
                SecurityLogger.logInfo(
                    "Sesión expirada por timeout",
                    "SessionId: $sessionId",
                    session.userId
                )
                endSession(sessionId)
            }
        }
        
        sessionTimeoutJobs[sessionId] = timeoutJob
    }
    
    /**
     * Guarda sesión en SharedPreferences encriptadas
     */
    private fun saveSessionToPrefs(session: SessionInfo) {
        try {
            val prefs = keyManager.getEncryptedSharedPreferences()
            prefs.edit()
                .putString("session_${session.sessionId}", session.toJson())
                .apply()
        } catch (e: Exception) {
            SecurityLogger.logError("Error guardando sesión en preferencias", e)
        }
    }
    
    /**
     * Remueve sesión de SharedPreferences
     */
    private fun removeSessionFromPrefs(sessionId: String) {
        try {
            val prefs = keyManager.getEncryptedSharedPreferences()
            prefs.edit()
                .remove("session_$sessionId")
                .apply()
        } catch (e: Exception) {
            SecurityLogger.logError("Error removiendo sesión de preferencias", e)
        }
    }
    
    /**
     * Restaura sesiones desde SharedPreferences al iniciar la app
     */
    fun restoreSessionsFromPrefs() {
        try {
            val prefs = keyManager.getEncryptedSharedPreferences()
            val allPrefs = prefs.all
            
            allPrefs.entries.filter { it.key.startsWith("session_") }.forEach { entry ->
                try {
                    val sessionJson = entry.value as? String
                    if (sessionJson != null) {
                        val session = SessionInfo.fromJson(sessionJson)
                        if (session.isActive && System.currentTimeMillis() < session.expirationTime) {
                            activeSessions[session.sessionId] = session
                            setupSessionTimeout(session.sessionId)
                        } else {
                            // Remover sesión expirada
                            prefs.edit().remove(entry.key).apply()
                        }
                    }
                } catch (e: Exception) {
                    SecurityLogger.logError("Error restaurando sesión individual", e)
                    // Remover entrada corrupta
                    prefs.edit().remove(entry.key).apply()
                }
            }
            
            SecurityLogger.logInfo(
                "Sesiones restauradas desde preferencias",
                "Cantidad: ${activeSessions.size}"
            )
            
        } catch (e: Exception) {
            SecurityLogger.logError("Error restaurando sesiones desde preferencias", e)
        }
    }
}

/**
 * Información de sesión de usuario
 */
data class SessionInfo(
    val sessionId: String,
    val userId: String,
    val userRole: UserRole,
    val deviceId: String,
    val startTime: Long,
    val lastActivity: Long,
    val expirationTime: Long,
    val token: String,
    val isActive: Boolean
) {
    fun toJson(): String {
        // Implementar serialización JSON
        return """
            {
                "sessionId": "$sessionId",
                "userId": "$userId",
                "userRole": "${userRole.name}",
                "deviceId": "$deviceId",
                "startTime": $startTime,
                "lastActivity": $lastActivity,
                "expirationTime": $expirationTime,
                "token": "$token",
                "isActive": $isActive
            }
        """.trimIndent()
    }
    
    companion object {
        fun fromJson(json: String): SessionInfo {
            // Implementar deserialización JSON simple
            // Por simplicidad, usar regex para extraer valores
            val sessionId = Regex("\"sessionId\":\\s*\"([^\"]+)\"").find(json)?.groupValues?.get(1) ?: ""
            val userId = Regex("\"userId\":\\s*\"([^\"]+)\"").find(json)?.groupValues?.get(1) ?: ""
            val userRoleStr = Regex("\"userRole\":\\s*\"([^\"]+)\"").find(json)?.groupValues?.get(1) ?: "VIEWER"
            val deviceId = Regex("\"deviceId\":\\s*\"([^\"]+)\"").find(json)?.groupValues?.get(1) ?: ""
            val startTime = Regex("\"startTime\":\\s*(\\d+)").find(json)?.groupValues?.get(1)?.toLongOrNull() ?: 0L
            val lastActivity = Regex("\"lastActivity\":\\s*(\\d+)").find(json)?.groupValues?.get(1)?.toLongOrNull() ?: 0L
            val expirationTime = Regex("\"expirationTime\":\\s*(\\d+)").find(json)?.groupValues?.get(1)?.toLongOrNull() ?: 0L
            val token = Regex("\"token\":\\s*\"([^\"]+)\"").find(json)?.groupValues?.get(1) ?: ""
            val isActive = Regex("\"isActive\":\\s*(true|false)").find(json)?.groupValues?.get(1)?.toBoolean() ?: false
            
            return SessionInfo(
                sessionId = sessionId,
                userId = userId,
                userRole = UserRole.valueOf(userRoleStr),
                deviceId = deviceId,
                startTime = startTime,
                lastActivity = lastActivity,
                expirationTime = expirationTime,
                token = token,
                isActive = isActive
            )
        }
    }
}

/**
 * Roles de usuario en el sistema
 */
enum class UserRole {
    SUPER_ADMIN,        // Acceso completo al sistema
    HR_ADMIN,           // Gestión de trabajadores y rotaciones
    PRODUCTION_MANAGER, // Gestión de producción y asignaciones
    AREA_SUPERVISOR,    // Supervisión de área específica
    VIEWER              // Solo lectura
}

/**
 * Resultado de operaciones de sesión
 */
sealed class SessionResult {
    data class Success(val session: SessionInfo) : SessionResult()
    data class Error(val message: String) : SessionResult()
}

/**
 * Resultado de validación de sesión
 */
sealed class SessionValidation {
    data class Valid(val session: SessionInfo) : SessionValidation()
    data class Invalid(val reason: String) : SessionValidation()
    data class Expired(val reason: String) : SessionValidation()
}