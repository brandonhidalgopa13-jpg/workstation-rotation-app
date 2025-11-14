package com.workstation.rotation.domain.models

/**
 * Modelo de dominio para Trabajador
 * Representa un trabajador en el sistema de rotación
 */
data class WorkerModel(
    val id: Long = 0,
    val name: String,
    val employeeId: String,
    val isActive: Boolean = true,
    val photoPath: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    companion object {
        fun empty() = WorkerModel(
            name = "",
            employeeId = ""
        )
    }
    
    fun isValid(): Boolean {
        return name.isNotBlank() && employeeId.isNotBlank()
    }
}
