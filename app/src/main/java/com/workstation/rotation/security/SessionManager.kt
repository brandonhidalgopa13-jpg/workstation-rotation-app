package com.workstation.rotation.security

import android.content.Context

/**
 * Gestor de sesiones simplificado
 */
class SessionManager private constructor(private val context: Context) {
    
    companion object {
        @Volatile
        private var INSTANCE: SessionManager? = null
        
        fun getInstance(context: Context): SessionManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SessionManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    fun startSession(userId: String, userRole: UserRole, deviceId: String): SessionResult {
        val session = SessionInfo(
            sessionId = "session_${System.currentTimeMillis()}",
            userId = userId,
            userRole = userRole,
            deviceId = deviceId,
            startTime = System.currentTimeMillis(),
            lastActivity = System.currentTimeMillis(),
            expirationTime = System.currentTimeMillis() + (30 * 60 * 1000),
            token = "token_${System.currentTimeMillis()}",
            isActive = true
        )
        return SessionResult.Success(session)
    }
    
    fun validateSession(token: String): SessionValidation {
        return SessionValidation.Valid(
            SessionInfo(
                sessionId = "session_1",
                userId = "user_1",
                userRole = UserRole.VIEWER,
                deviceId = "device_1",
                startTime = System.currentTimeMillis(),
                lastActivity = System.currentTimeMillis(),
                expirationTime = System.currentTimeMillis() + (30 * 60 * 1000),
                token = token,
                isActive = true
            )
        )
    }
    
    fun endSession(sessionId: String): Boolean = true
    
    fun restoreSessionsFromPrefs() {
        // Implementación simplificada
    }
}

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
)

enum class UserRole {
    SUPER_ADMIN, HR_ADMIN, PRODUCTION_MANAGER, AREA_SUPERVISOR, VIEWER
}

sealed class SessionResult {
    data class Success(val session: SessionInfo) : SessionResult()
    data class Error(val message: String) : SessionResult()
}

sealed class SessionValidation {
    data class Valid(val session: SessionInfo) : SessionValidation()
    data class Invalid(val reason: String) : SessionValidation()
    data class Expired(val reason: String) : SessionValidation()
}