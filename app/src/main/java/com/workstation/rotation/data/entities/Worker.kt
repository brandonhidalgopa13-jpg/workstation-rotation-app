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
    val isActive: Boolean = true
) {
    /**
     * Returns a display name for the worker including training status.
     */
    fun getDisplayName(): String {
        val status = when {
            isTrainer -> " 👨‍🏫"
            isTrainee -> " 🎯"
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
}