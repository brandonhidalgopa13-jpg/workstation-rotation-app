package com.workstation.rotation.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 🚫 ENTIDAD RESTRICCIONES DE TRABAJADOR - SISTEMA DE RESTRICCIONES ESPECÍFICAS
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 
 * 📋 FUNCIONES DE ESTA ENTIDAD:
 * 
 * 🎯 RESTRICCIONES ESPECÍFICAS POR ESTACIÓN:
 * @property workerId - ID del trabajador que tiene la restricción
 * @property workstationId - ID de la estación específica restringida
 * @property restrictionType - Tipo de restricción (PROHIBITED, LIMITED, etc.)
 * @property notes - Notas específicas sobre la restricción
 * @property isActive - Si la restricción está activa o no
 * 
 * 🔄 COMPORTAMIENTO EN ROTACIONES:
 * • PROHIBITED: El trabajador NO puede ser asignado a esta estación
 * • LIMITED: El trabajador puede ser asignado pero con limitaciones
 * • TEMPORARY: Restricción temporal (con fecha de expiración)
 * 
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 */
@Entity(
    tableName = "worker_restrictions",
    primaryKeys = ["workerId", "workstationId"],
    foreignKeys = [
        ForeignKey(
            entity = Worker::class,
            parentColumns = ["id"],
            childColumns = ["workerId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Workstation::class,
            parentColumns = ["id"],
            childColumns = ["workstationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("workerId"), Index("workstationId")]
)
data class WorkerRestriction(
    val workerId: Long,
    val workstationId: Long,
    val restrictionType: RestrictionType = RestrictionType.PROHIBITED,
    val notes: String = "",
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val expiresAt: Long? = null // Para restricciones temporales
)

enum class RestrictionType {
    PROHIBITED,  // No puede trabajar en esta estación
    LIMITED,     // Puede trabajar pero con limitaciones
    TEMPORARY    // Restricción temporal
}