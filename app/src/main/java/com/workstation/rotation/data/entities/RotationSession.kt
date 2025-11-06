package com.workstation.rotation.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

/**
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 📅 ENTIDAD SESIÓN DE ROTACIÓN - NUEVA ARQUITECTURA v4.0
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 
 * 🎯 PROPÓSITO:
 * Representa una sesión completa de rotación que contiene tanto la rotación actual
 * como la siguiente rotación. Actúa como contenedor para las asignaciones.
 * 
 * 📋 CARACTERÍSTICAS:
 * • Agrupa rotación actual y siguiente rotación
 * • Control de estado de la sesión completa
 * • Métricas y estadísticas de la sesión
 * • Historial de cambios y transiciones
 * 
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 */

@Entity(tableName = "rotation_sessions")
data class RotationSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    /**
     * Nombre descriptivo de la sesión
     */
    val name: String,
    
    /**
     * Descripción opcional de la sesión
     */
    val description: String? = null,
    
    /**
     * Estado de la sesión:
     * - "DRAFT": Borrador, en preparación
     * - "ACTIVE": Sesión activa
     * - "COMPLETED": Sesión completada
     * - "CANCELLED": Sesión cancelada
     */
    val status: String = STATUS_DRAFT,
    
    /**
     * Timestamp de creación de la sesión
     */
    val created_at: Long = System.currentTimeMillis(),
    
    /**
     * Timestamp de cuando se activó la sesión
     */
    val activated_at: Long? = null,
    
    /**
     * Timestamp de cuando se completó la sesión
     */
    val completed_at: Long? = null,
    
    /**
     * ID del usuario que creó la sesión
     */
    val created_by: String? = null,
    
    /**
     * Número total de trabajadores en la sesión
     */
    val total_workers: Int = 0,
    
    /**
     * Número total de estaciones involucradas
     */
    val total_workstations: Int = 0,
    
    /**
     * Duración estimada en minutos
     */
    val estimated_duration_minutes: Int? = null,
    
    /**
     * Duración real en minutos (calculada al completar)
     */
    val actual_duration_minutes: Int? = null,
    
    /**
     * Notas adicionales sobre la sesión
     */
    val notes: String? = null,
    
    /**
     * Configuración JSON de la sesión (opcional)
     */
    val configuration: String? = null
) {
    
    companion object {
        const val STATUS_DRAFT = "DRAFT"
        const val STATUS_ACTIVE = "ACTIVE"
        const val STATUS_COMPLETED = "COMPLETED"
        const val STATUS_CANCELLED = "CANCELLED"
        
        /**
         * Genera un nombre automático para la sesión
         */
        fun generateSessionName(): String {
            val date = Date()
            return "Rotación ${android.text.format.DateFormat.format("dd/MM/yyyy HH:mm", date)}"
        }
    }
    
    /**
     * Verifica si la sesión está activa
     */
    fun isActive(): Boolean = status == STATUS_ACTIVE
    
    /**
     * Verifica si la sesión está completada
     */
    fun isCompleted(): Boolean = status == STATUS_COMPLETED
    
    /**
     * Verifica si la sesión es un borrador
     */
    fun isDraft(): Boolean = status == STATUS_DRAFT
    
    /**
     * Obtiene la duración real o estimada
     */
    fun getDuration(): Int? = actual_duration_minutes ?: estimated_duration_minutes
    
    /**
     * Obtiene el progreso de la sesión (0.0 - 1.0)
     */
    fun getProgress(): Float {
        return when (status) {
            STATUS_DRAFT -> 0.0f
            STATUS_ACTIVE -> 0.5f
            STATUS_COMPLETED -> 1.0f
            STATUS_CANCELLED -> 0.0f
            else -> 0.0f
        }
    }
    
    /**
     * Obtiene el color asociado al estado
     */
    fun getStatusColor(): String {
        return when (status) {
            STATUS_DRAFT -> "#FFC107"      // Amarillo
            STATUS_ACTIVE -> "#4CAF50"     // Verde
            STATUS_COMPLETED -> "#2196F3"  // Azul
            STATUS_CANCELLED -> "#F44336"  // Rojo
            else -> "#9E9E9E"              // Gris
        }
    }
    
    /**
     * Obtiene el texto legible del estado
     */
    fun getStatusText(): String {
        return when (status) {
            STATUS_DRAFT -> "Borrador"
            STATUS_ACTIVE -> "Activa"
            STATUS_COMPLETED -> "Completada"
            STATUS_CANCELLED -> "Cancelada"
            else -> "Desconocido"
        }
    }
}