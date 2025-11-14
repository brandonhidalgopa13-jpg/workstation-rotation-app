package com.workstation.rotation.domain.models

/**
 * Modelo de dominio para Capacidad
 * Representa la relación entre un trabajador y una estación de trabajo
 * con su nivel de competencia
 */
data class CapabilityModel(
    val id: Long = 0,
    val workerId: Long,
    val workstationId: Long,
    val proficiencyLevel: Int = 1,
    val certificationDate: Long? = null
) {
    companion object {
        const val MIN_PROFICIENCY = 1
        const val MAX_PROFICIENCY = 5
        
        fun create(workerId: Long, workstationId: Long) = CapabilityModel(
            workerId = workerId,
            workstationId = workstationId,
            proficiencyLevel = MIN_PROFICIENCY
        )
    }
    
    fun isValid(): Boolean {
        return workerId > 0 && 
               workstationId > 0 && 
               proficiencyLevel in MIN_PROFICIENCY..MAX_PROFICIENCY
    }
    
    fun getProficiencyLabel(): String {
        return when (proficiencyLevel) {
            1 -> "Principiante"
            2 -> "Básico"
            3 -> "Intermedio"
            4 -> "Avanzado"
            5 -> "Experto"
            else -> "Desconocido"
        }
    }
}
