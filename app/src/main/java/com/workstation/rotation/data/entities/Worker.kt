package com.workstation.rotation.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 👤 ENTIDAD TRABAJADOR - NÚCLEO DEL SISTEMA DE ROTACIÓN
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 
 * 📋 FUNCIONES DE ESTA ENTIDAD:
 * 
 * 🆔 IDENTIFICACIÓN Y DATOS BÁSICOS:
 * @property id - Identificador único del trabajador en la base de datos
 * @property name - Nombre completo del trabajador para identificación visual
 * @property email - Correo electrónico (opcional) para contacto y notificaciones
 * @property isActive - Estado activo/inactivo para incluir/excluir de rotaciones
 * 
 * 📊 SISTEMA DE DISPONIBILIDAD:
 * @property availabilityPercentage - Porcentaje de disponibilidad (0-100%)
 *   • 80-100%: Alta disponibilidad (sin indicador visual)
 *   • 50-79%: Disponibilidad media (muestra porcentaje)
 *   • 0-49%: Baja disponibilidad (muestra porcentaje + ⚠️)
 * 
 * 🚫 SISTEMA DE RESTRICCIONES:
 * @property restrictionNotes - Notas sobre restricciones laborales
 *   • Limitaciones físicas, horarios especiales, etc.
 *   • Se muestra con icono 🔒 cuando hay restricciones
 * 
 * 🎓 SISTEMA DE ENTRENAMIENTO INTEGRADO:
 * @property isTrainer - Indica si puede entrenar a otros trabajadores
 *   • Entrenadores se priorizan en asignaciones
 *   • Se muestran con icono 👨‍🏫
 * 
 * @property isTrainee - Indica si está actualmente en entrenamiento
 *   • Entrenados se asignan a estaciones de entrenamiento específicas
 *   • Se muestran con icono 🎯
 * 
 * @property trainerId - ID del entrenador asignado (si isTrainee = true)
 *   • Crea relación entrenador-entrenado
 *   • Sistema garantiza que siempre estén juntos
 * 
 * @property trainingWorkstationId - ID de la estación donde ocurre el entrenamiento
 *   • Estación específica solicitada para el entrenamiento
 *   • Entrenador y entrenado se asignan SIEMPRE a esta estación
 * 
 * 🏆 SISTEMA DE CERTIFICACIÓN:
 * @property isCertified - Indica si el trabajador completó su entrenamiento y fue certificado
 *   • Trabajadores certificados pueden rotar libremente
 *   • Se muestran con icono 🏆
 * 
 * @property certificationDate - Fecha de certificación (timestamp)
 *   • Registro histórico de cuándo se completó el entrenamiento
 *   • Útil para reportes y seguimiento de progreso
 * 
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 🔄 COMPORTAMIENTO EN ROTACIONES:
 * 
 * • PRIORIDAD MÁXIMA: Parejas entrenador-entrenado (ignora todas las restricciones)
 * • ALTA PRIORIDAD: Entrenadores individuales
 * • PRIORIDAD NORMAL: Trabajadores regulares
 * • CONSIDERACIONES: Disponibilidad, restricciones, estaciones compatibles
 * 
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 */
@Entity(tableName = "workers")
data class Worker(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val email: String = "",
    val availabilityPercentage: Int = 100,
    val restrictionNotes: String = "",
    val isTrainer: Boolean = false,
    val isTrainee: Boolean = false,
    val trainerId: Long? = null,
    val trainingWorkstationId: Long? = null,
    val isActive: Boolean = true,
    val isCertified: Boolean = false,
    val certificationDate: Long? = null,
    // Campos para seguimiento de rotación
    val currentWorkstationId: Long? = null,
    val rotationsInCurrentStation: Int = 0,
    val lastRotationTimestamp: Long = 0L
) {
    /**
     * Returns a display name for the worker including training status.
     */
    fun getDisplayName(): String {
        val status = when {
            isTrainer -> " 👨‍🏫"
            isTrainee -> " 🎯"
            isCertified -> " 🏆"
            else -> ""
        }
        return "$name$status"
    }
    
    /**
     * Checks if the worker has any restrictions.
     */
    fun hasRestrictions(): Boolean = restrictionNotes.isNotEmpty()
    
    /**
     * Gets the availability status as a descriptive string.
     */
    fun getAvailabilityStatus(): String {
        return when {
            availabilityPercentage >= 80 -> "Alta"
            availabilityPercentage >= 50 -> "Media"
            else -> "Baja"
        }
    }
    
    /**
     * Checks if this worker is a trained worker (not trainer, not trainee).
     */
    fun isTrainedWorker(): Boolean {
        return !isTrainer && !isTrainee
    }
    
    /**
     * Checks if this worker can be certified (was in training but not certified yet).
     */
    fun canBeCertified(): Boolean {
        return !isTrainee && !isCertified && trainerId != null
    }
    
    /**
     * Checks if this worker needs to rotate based on time spent in current station.
     * Trained workers should rotate after being in the same station for half the rotation cycle.
     */
    fun needsToRotate(maxRotationsInSameStation: Int = 2): Boolean {
        return isTrainedWorker() && rotationsInCurrentStation >= maxRotationsInSameStation
    }
    
    /**
     * Gets the rotation priority for this worker.
     * Higher numbers indicate higher priority to rotate.
     */
    fun getRotationPriority(): Int {
        return when {
            // Trained workers who have been in same station too long get highest priority
            isTrainedWorker() && needsToRotate() -> 100 + rotationsInCurrentStation
            // Regular trained workers get medium priority
            isTrainedWorker() -> 50
            // Trainers and trainees get lower priority (they have other constraints)
            else -> 10
        }
    }
}