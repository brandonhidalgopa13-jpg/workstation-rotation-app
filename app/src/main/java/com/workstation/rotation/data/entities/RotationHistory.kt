package com.workstation.rotation.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 📊 ENTIDAD HISTORIAL DE ROTACIONES - TRACKING COMPLETO DE MOVIMIENTOS
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 
 * 🎯 PROPÓSITO:
 * Registra cada rotación realizada en el sistema para análisis, métricas y auditoría.
 * Permite generar reportes basados en datos reales vs simulaciones.
 * 
 * 📋 CAMPOS PRINCIPALES:
 * • ID único para cada registro de rotación
 * • Referencias a trabajador y estación involucrados
 * • Timestamps precisos de inicio y fin
 * • Tipo de rotación (manual, automática, emergencia)
 * • Métricas de rendimiento y duración real
 * 
 * 🔗 RELACIONES:
 * • FK a Worker: Quién realizó la rotación
 * • FK a Workstation: En qué estación trabajó
 * • Índices optimizados para consultas por fecha y trabajador
 * 
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 */

@Entity(
    tableName = "rotation_history",
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
        Index(value = ["rotation_date"]),
        Index(value = ["rotation_type"])
    ]
)
data class RotationHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    /**
     * ID del trabajador que realizó la rotación
     */
    val worker_id: Long,
    
    /**
     * ID de la estación donde trabajó
     */
    val workstation_id: Long,
    
    /**
     * Timestamp de cuando inició la rotación (System.currentTimeMillis())
     */
    val rotation_date: Long,
    
    /**
     * Timestamp de cuando terminó la rotación (null si aún está activa)
     */
    val end_date: Long? = null,
    
    /**
     * Tipo de rotación realizada:
     * - "MANUAL": Asignación manual por supervisor
     * - "AUTOMATIC": Rotación automática del sistema
     * - "EMERGENCY": Rotación de emergencia por ausencia
     * - "SCHEDULED": Rotación programada
     */
    val rotation_type: String,
    
    /**
     * Duración real en minutos (calculada automáticamente)
     */
    val duration_minutes: Int? = null,
    
    /**
     * Score de rendimiento (0.0 - 10.0)
     * Puede ser calculado automáticamente o ingresado manualmente
     */
    val performance_score: Double? = null,
    
    /**
     * Notas adicionales sobre la rotación
     */
    val notes: String? = null,
    
    /**
     * Indica si la rotación fue completada exitosamente
     */
    val completed: Boolean = false,
    
    /**
     * Timestamp de creación del registro
     */
    val created_at: Long = System.currentTimeMillis()
) {
    
    /**
     * Tipos de rotación disponibles
     */
    companion object {
        const val TYPE_MANUAL = "MANUAL"
        const val TYPE_AUTOMATIC = "AUTOMATIC"
        const val TYPE_EMERGENCY = "EMERGENCY"
        const val TYPE_SCHEDULED = "SCHEDULED"
        
        /**
         * Calcula la duración en minutos entre dos timestamps
         */
        fun calculateDuration(startTime: Long, endTime: Long): Int {
            return ((endTime - startTime) / (1000 * 60)).toInt()
        }
    }
    
    /**
     * Verifica si la rotación está actualmente activa
     */
    fun isActive(): Boolean = end_date == null
    
    /**
     * Obtiene la duración calculada si la rotación ha terminado
     */
    fun getCalculatedDuration(): Int? {
        return end_date?.let { calculateDuration(rotation_date, it) }
    }
    
    /**
     * Obtiene un resumen legible de la rotación
     */
    fun getSummary(): String {
        val status = if (isActive()) "Activa" else "Completada"
        val duration = getCalculatedDuration()?.let { "${it}min" } ?: "En curso"
        return "Rotación $rotation_type - $status ($duration)"
    }
}