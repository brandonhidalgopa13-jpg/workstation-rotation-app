package com.workstation.rotation.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 🔗 ENTIDAD CAPACIDADES TRABAJADOR-ESTACIÓN - NUEVA ARQUITECTURA v4.0
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 
 * 🎯 PROPÓSITO:
 * Define qué estaciones puede manejar cada trabajador y su nivel de competencia.
 * Un trabajador puede tener de 1 a 15 estaciones asignadas.
 * 
 * 📋 CARACTERÍSTICAS:
 * • Relación muchos a muchos entre trabajadores y estaciones
 * • Nivel de competencia por estación
 * • Estado de certificación y entrenamiento
 * • Restricciones temporales o permanentes
 * 
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 */

@Entity(
    tableName = "worker_workstation_capabilities",
    foreignKeys = [
        ForeignKey(
            entity = Worker::class,
            parentColumns = ["id"],
            childColumns = ["worker_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Workstation::class,
            parentColumns = ["id"],
            childColumns = ["workstation_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["worker_id"]),
        Index(value = ["workstation_id"]),
        Index(value = ["is_active"]),
        Index(value = ["competency_level"])
    ]
)
data class WorkerWorkstationCapability(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    /**
     * ID del trabajador
     */
    val worker_id: Long,
    
    /**
     * ID de la estación
     */
    val workstation_id: Long,
    
    /**
     * Nivel de competencia:
     * 1 = Principiante (requiere supervisión)
     * 2 = Básico (puede trabajar con apoyo ocasional)
     * 3 = Intermedio (trabaja independientemente)
     * 4 = Avanzado (puede entrenar a otros)
     * 5 = Experto (líder de estación)
     */
    val competency_level: Int,
    
    /**
     * Indica si la capacidad está activa
     */
    val is_active: Boolean = true,
    
    /**
     * Indica si está certificado para esta estación
     */
    val is_certified: Boolean = false,
    
    /**
     * Indica si puede ser líder en esta estación
     */
    val can_be_leader: Boolean = false,
    
    /**
     * Indica si puede entrenar en esta estación
     */
    val can_train: Boolean = false,
    
    /**
     * Fecha de certificación (timestamp)
     */
    val certified_at: Long? = null,
    
    /**
     * Fecha de expiración de certificación (timestamp)
     */
    val certification_expires_at: Long? = null,
    
    /**
     * Fecha de última evaluación (timestamp)
     */
    val last_evaluated_at: Long? = null,
    
    /**
     * Puntuación de la última evaluación (0.0 - 10.0)
     */
    val last_evaluation_score: Double? = null,
    
    /**
     * Número de horas de experiencia en esta estación
     */
    val experience_hours: Int = 0,
    
    /**
     * Notas sobre la capacidad del trabajador
     */
    val notes: String? = null,
    
    /**
     * Restricciones especiales (JSON)
     */
    val restrictions: String? = null,
    
    /**
     * Timestamp de creación del registro
     */
    val created_at: Long = System.currentTimeMillis(),
    
    /**
     * Timestamp de última actualización
     */
    val updated_at: Long = System.currentTimeMillis()
) {
    
    companion object {
        const val LEVEL_BEGINNER = 1
        const val LEVEL_BASIC = 2
        const val LEVEL_INTERMEDIATE = 3
        const val LEVEL_ADVANCED = 4
        const val LEVEL_EXPERT = 5
        
        /**
         * Obtiene el texto del nivel de competencia
         */
        fun getCompetencyLevelText(level: Int): String {
            return when (level) {
                LEVEL_BEGINNER -> "Principiante"
                LEVEL_BASIC -> "Básico"
                LEVEL_INTERMEDIATE -> "Intermedio"
                LEVEL_ADVANCED -> "Avanzado"
                LEVEL_EXPERT -> "Experto"
                else -> "Desconocido"
            }
        }
        
        /**
         * Obtiene el color del nivel de competencia
         */
        fun getCompetencyLevelColor(level: Int): String {
            return when (level) {
                LEVEL_BEGINNER -> "#F44336"    // Rojo
                LEVEL_BASIC -> "#FF9800"       // Naranja
                LEVEL_INTERMEDIATE -> "#FFC107" // Amarillo
                LEVEL_ADVANCED -> "#4CAF50"    // Verde
                LEVEL_EXPERT -> "#2196F3"      // Azul
                else -> "#9E9E9E"              // Gris
            }
        }
    }
    
    /**
     * Verifica si la certificación está vigente
     */
    fun isCertificationValid(): Boolean {
        return is_certified && (certification_expires_at == null || 
                certification_expires_at > System.currentTimeMillis())
    }
    
    /**
     * Verifica si necesita recertificación pronto (30 días)
     */
    fun needsRecertificationSoon(): Boolean {
        return certification_expires_at != null && 
                certification_expires_at - System.currentTimeMillis() < (30 * 24 * 60 * 60 * 1000L)
    }
    
    /**
     * Obtiene el texto del nivel de competencia
     */
    fun getCompetencyText(): String = getCompetencyLevelText(competency_level)
    
    /**
     * Obtiene el color del nivel de competencia
     */
    fun getCompetencyColor(): String = getCompetencyLevelColor(competency_level)
    
    /**
     * Calcula el puntaje de idoneidad para asignación (0.0 - 1.0)
     */
    fun calculateSuitabilityScore(): Double {
        var score = competency_level / 5.0 // Base score from competency
        
        // Bonus for certification
        if (isCertificationValid()) score += 0.2
        
        // Bonus for leadership capability
        if (can_be_leader) score += 0.1
        
        // Bonus for training capability
        if (can_train) score += 0.1
        
        // Bonus for recent evaluation
        if (last_evaluation_score != null && last_evaluation_score >= 8.0) {
            score += 0.1
        }
        
        // Penalty if not active
        if (!is_active) score *= 0.5
        
        return score.coerceIn(0.0, 1.0)
    }
    
    /**
     * Verifica si puede ser asignado a esta estación
     */
    fun canBeAssigned(): Boolean {
        return is_active && competency_level >= LEVEL_BASIC && 
                (is_certified || competency_level >= LEVEL_INTERMEDIATE)
    }
}