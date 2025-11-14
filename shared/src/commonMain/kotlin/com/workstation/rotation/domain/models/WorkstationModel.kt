package com.workstation.rotation.domain.models

/**
 * Modelo de dominio para Estación de Trabajo
 * Representa una estación donde los trabajadores pueden ser asignados
 */
data class WorkstationModel(
    val id: Long = 0,
    val name: String,
    val code: String,
    val description: String? = null,
    val isActive: Boolean = true,
    val requiredWorkers: Int = 1,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    companion object {
        fun empty() = WorkstationModel(
            name = "",
            code = ""
        )
    }
    
    fun isValid(): Boolean {
        return name.isNotBlank() && 
               code.isNotBlank() && 
               requiredWorkers > 0
    }
}
