package com.workstation.rotation.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 🔄 ENTIDAD ASIGNACIÓN DE ROTACIÓN - NUEVA ARQUITECTURA v4.0
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 
 * 🎯 PROPÓSITO:
 * Representa una asignación específica de un trabajador a una estación en una rotación.
 * Cada trabajador puede estar en máximo 1 estación por rotación.
 * 
 * 📋 CARACTERÍSTICAS:
 * • Un trabajador = Una estación por rotación
 * • Soporte para "Rotación Actual" y "Siguiente Rotación"
 * • Tracking de tiempo y estado de la asignación
 * • Validación de capacidad de estaciones
 * 
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 */

@Entity(
    tableName = "rotation_assignments",
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
        ),
        ForeignKey(
            entity = RotationSession::class,
            parentColumns = ["id"],
            childColumns = ["rotation_session_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["worker_id"]),
        Index(value = ["workstation_id"]),
        Index(value = ["rotation_session_id"]),
        Index(value = ["rotation_type"]),
        Index(value = ["is_active"])
    ]
)
data class RotationAssignment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    /**
     * ID del trabajador asignado
     */
    val worker_id: Long,
    
    /**
     * ID de la estación asignada
     */
    val workstation_id: Long,
    
    /**
     * ID de la sesión de rotación
     */
    val rotation_session_id: Long,
    
    /**
     * Tipo de rotación:
     * - "CURRENT": Rotación actual
     * - "NEXT": Siguiente rotación
     */
    val rotation_type: String,
    
    /**
     * Indica si la asignación está activa
     */
    val is_active: Boolean = true,
    
    /**
     * Timestamp de cuando se creó la asignación
     */
    val assigned_at: Long = System.currentTimeMillis(),
    
    /**
     * Timestamp de cuando inició la rotación (null si no ha iniciado)
     */
    val started_at: Long? = null,
    
    /**
     * Timestamp de cuando terminó la rotación (null si está activa)
     */
    val completed_at: Long? = null,
    
    /**
     * Notas adicionales sobre la asignación
     */
    val notes: String? = null,
    
    /**
     * Prioridad de la asignación (1 = alta, 5 = baja)
     */
    val priority: Int = 3
) {
    
    companion object {
        const val TYPE_CURRENT = "CURRENT"
        const val TYPE_NEXT = "NEXT"
        
        /**
         * Calcula la duración en minutos de la asignación
         */
        fun calculateDuration(startTime: Long, endTime: Long): Int {
            return ((endTime - startTime) / (1000 * 60)).toInt()
        }
    }
    
    /**
     * Verifica si la asignación está en progreso
     */
    fun isInProgress(): Boolean = started_at != null && completed_at == null
    
    /**
     * Verifica si la asignación está completada
     */
    fun isCompleted(): Boolean = completed_at != null
    
    /**
     * Obtiene la duración en minutos si está completada
     */
    fun getDurationMinutes(): Int? {
        return if (started_at != null && completed_at != null) {
            calculateDuration(started_at, completed_at)
        } else null
    }
    
    /**
     * Obtiene el estado legible de la asignación
     */
    fun getStatusText(): String {
        return when {
            !is_active -> "Inactiva"
            isCompleted() -> "Completada"
            isInProgress() -> "En Progreso"
            else -> "Pendiente"
        }
    }
}