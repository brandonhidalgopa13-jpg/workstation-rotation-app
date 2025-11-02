package com.workstation.rotation.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 🏭 ENTIDAD ESTACIÓN DE TRABAJO - NÚCLEO DEL SISTEMA DE ROTACIÓN
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 
 * 📋 FUNCIONES DE ESTA ENTIDAD:
 * 
 * 🆔 IDENTIFICACIÓN Y DATOS BÁSICOS:
 * @property id - Identificador único de la estación en la base de datos
 * @property name - Nombre descriptivo de la estación para identificación visual
 * @property description - Descripción detallada de las actividades de la estación
 * @property isActive - Estado activo/inactivo para incluir/excluir de rotaciones
 * 
 * 👥 CONFIGURACIÓN DE CAPACIDAD:
 * @property requiredWorkers - Número de trabajadores necesarios para operar la estación
 * @property maxWorkers - Número máximo de trabajadores que pueden trabajar simultáneamente
 * 
 * ⭐ SISTEMA DE PRIORIDADES:
 * @property isPriority - Indica si es una estación prioritaria que debe llenarse primero
 *   • Estaciones prioritarias se llenan antes que las regulares
 *   • Garantiza operación continua de procesos críticos
 * 
 * 🎓 CONFIGURACIÓN DE ENTRENAMIENTO:
 * @property isTrainingStation - Indica si esta estación se usa para entrenar nuevos trabajadores
 *   • Estaciones de entrenamiento reciben parejas entrenador-entrenado
 *   • Tienen configuraciones especiales para aprendizaje
 * 
 * 📊 MÉTRICAS Y SEGUIMIENTO:
 * @property utilizationRate - Porcentaje de utilización promedio de la estación
 * @property lastMaintenanceDate - Fecha del último mantenimiento (timestamp)
 * @property nextMaintenanceDate - Fecha programada del próximo mantenimiento
 * 
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 🔄 COMPORTAMIENTO EN ROTACIONES:
 * 
 * • PRIORIDAD MÁXIMA: Estaciones prioritarias (isPriority = true)
 * • ALTA PRIORIDAD: Estaciones de entrenamiento con parejas asignadas
 * • PRIORIDAD NORMAL: Estaciones regulares
 * • CONSIDERACIONES: Capacidad, trabajadores compatibles, mantenimiento
 * 
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 */
@Entity(tableName = "workstations")
data class Workstation(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val requiredWorkers: Int = 1,
    val maxWorkers: Int = 1,
    val isPriority: Boolean = false,
    val isTrainingStation: Boolean = false,
    val isActive: Boolean = true,
    val utilizationRate: Double = 0.0,
    val lastMaintenanceDate: Long? = null,
    val nextMaintenanceDate: Long? = null,
    // Campos para configuración avanzada
    val color: String = "#2196F3", // Color para identificación visual
    val icon: String = "🏭", // Icono para la estación
    val location: String = "", // Ubicación física de la estación
    val notes: String = "" // Notas adicionales sobre la estación
) {
    
    /**
     * Obtiene el nombre de visualización de la estación incluyendo indicadores de estado.
     */
    fun getDisplayName(): String {
        val indicators = mutableListOf<String>()
        
        if (isPriority) indicators.add("⭐")
        if (isTrainingStation) indicators.add("🎓")
        if (!isActive) indicators.add("❌")
        
        val indicatorString = if (indicators.isNotEmpty()) " ${indicators.joinToString("")}" else ""
        return "$name$indicatorString"
    }
    
    /**
     * Verifica si la estación necesita mantenimiento.
     */
    fun needsMaintenance(): Boolean {
        val currentTime = System.currentTimeMillis()
        return nextMaintenanceDate?.let { it <= currentTime } ?: false
    }
    
    /**
     * Obtiene el estado de la estación como texto descriptivo.
     */
    fun getStatusDescription(): String {
        return when {
            !isActive -> "Inactiva"
            needsMaintenance() -> "Mantenimiento Requerido"
            isPriority -> "Prioritaria"
            isTrainingStation -> "Entrenamiento"
            else -> "Activa"
        }
    }
    
    /**
     * Verifica si la estación puede aceptar más trabajadores.
     */
    fun canAcceptMoreWorkers(currentWorkerCount: Int): Boolean {
        return isActive && currentWorkerCount < maxWorkers
    }
    
    /**
     * Verifica si la estación está completamente ocupada.
     */
    fun isFullyOccupied(currentWorkerCount: Int): Boolean {
        return currentWorkerCount >= requiredWorkers
    }
    
    /**
     * Calcula el porcentaje de ocupación actual.
     */
    fun getOccupancyPercentage(currentWorkerCount: Int): Double {
        return if (requiredWorkers > 0) {
            (currentWorkerCount.toDouble() / requiredWorkers.toDouble()) * 100.0
        } else {
            0.0
        }
    }
    
    /**
     * Obtiene la prioridad de llenado de la estación.
     * Números más altos indican mayor prioridad.
     */
    fun getFillPriority(): Int {
        return when {
            !isActive -> 0 // Estaciones inactivas no se llenan
            isPriority -> 100 // Máxima prioridad para estaciones prioritarias
            isTrainingStation -> 80 // Alta prioridad para entrenamiento
            else -> 50 // Prioridad normal
        }
    }
    
    /**
     * Verifica si la estación está disponible para rotaciones.
     */
    fun isAvailableForRotation(): Boolean {
        return isActive && !needsMaintenance()
    }
    
    /**
     * Obtiene información de capacidad como texto.
     */
    fun getCapacityInfo(): String {
        return if (requiredWorkers == maxWorkers) {
            "$requiredWorkers trabajadores"
        } else {
            "$requiredWorkers-$maxWorkers trabajadores"
        }
    }
    
    /**
     * Verifica si esta estación es compatible con el entrenamiento.
     */
    fun isCompatibleWithTraining(): Boolean {
        return isTrainingStation && isActive && maxWorkers >= 2
    }
}