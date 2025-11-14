package com.workstation.rotation.domain.models

/**
 * Modelo de dominio para Asignación de Rotación
 * Representa la asignación de un trabajador a una estación en una sesión específica
 */
data class RotationAssignmentModel(
    val id: Long = 0,
    val sessionId: Long,
    val workerId: Long,
    val workstationId: Long,
    val position: Int,
    val assignedAt: Long = System.currentTimeMillis()
) {
    companion object {
        fun create(
            sessionId: Long,
            workerId: Long,
            workstationId: Long,
            position: Int
        ) = RotationAssignmentModel(
            sessionId = sessionId,
            workerId = workerId,
            workstationId = workstationId,
            position = position
        )
    }
    
    fun isValid(): Boolean {
        return sessionId > 0 && 
               workerId > 0 && 
               workstationId > 0 && 
               position >= 0
    }
}
