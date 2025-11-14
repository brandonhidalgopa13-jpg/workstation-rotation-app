package com.workstation.rotation.domain.models

/**
 * Modelo de dominio para Sesión de Rotación
 * Representa una sesión de rotación de trabajadores
 */
data class RotationSessionModel(
    val id: Long = 0,
    val name: String,
    val startDate: Long,
    val endDate: Long? = null,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        fun create(name: String) = RotationSessionModel(
            name = name,
            startDate = System.currentTimeMillis()
        )
    }
    
    fun isValid(): Boolean {
        return name.isNotBlank() && startDate > 0
    }
    
    fun isOngoing(): Boolean {
        val now = System.currentTimeMillis()
        return isActive && startDate <= now && (endDate == null || endDate > now)
    }
    
    fun getDurationMillis(): Long? {
        return endDate?.let { it - startDate }
    }
}
