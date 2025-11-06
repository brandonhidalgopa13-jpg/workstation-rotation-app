package com.workstation.rotation.models

/**
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 📊 MODELOS PARA GRID DE ROTACIÓN - NUEVA ARQUITECTURA v4.0
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 
 * 🎯 PROPÓSITO:
 * Modelos específicos para la nueva UI de rotación con scroll horizontal y vertical.
 * Soporta visualización de "Rotación Actual" y "Siguiente Rotación" lado a lado.
 * 
 * 📋 CARACTERÍSTICAS:
 * • Grid bidimensional con estaciones y trabajadores
 * • Información de capacidad por estación
 * • Estados visuales para diferentes tipos de asignación
 * • Soporte para drag & drop de trabajadores
 * 
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 */

/**
 * Representa una celda individual en el grid de rotación
 */
data class RotationGridCell(
    val workerId: Long? = null,
    val workerName: String? = null,
    val workstationId: Long,
    val workstationName: String,
    val rotationType: String, // "CURRENT" o "NEXT"
    val isAssigned: Boolean = false,
    val competencyLevel: Int? = null,
    val canBeLeader: Boolean = false,
    val canTrain: Boolean = false,
    val isOptimalAssignment: Boolean = false,
    val conflictReason: String? = null
) {
    
    /**
     * Obtiene el color de fondo según el estado de la celda
     */
    fun getBackgroundColor(): String {
        return when {
            conflictReason != null -> "#FFEBEE"        // Rojo claro - conflicto
            !isAssigned -> "#F5F5F5"                   // Gris claro - vacío
            isOptimalAssignment -> "#E8F5E8"           // Verde claro - óptimo
            competencyLevel != null && competencyLevel >= 4 -> "#E3F2FD" // Azul claro - experto
            canBeLeader -> "#FFF3E0"                   // Naranja claro - líder
            else -> "#FFFFFF"                          // Blanco - normal
        }
    }
    
    /**
     * Obtiene el color del texto según el estado
     */
    fun getTextColor(): String {
        return when {
            conflictReason != null -> "#D32F2F"        // Rojo - conflicto
            !isAssigned -> "#9E9E9E"                   // Gris - vacío
            isOptimalAssignment -> "#2E7D32"           // Verde - óptimo
            competencyLevel != null && competencyLevel >= 4 -> "#1976D2" // Azul - experto
            canBeLeader -> "#F57C00"                   // Naranja - líder
            else -> "#212121"                          // Negro - normal
        }
    }
    
    /**
     * Obtiene el icono representativo
     */
    fun getIcon(): String? {
        return when {
            conflictReason != null -> "⚠️"
            !isAssigned -> null
            canBeLeader -> "👑"
            canTrain -> "🎓"
            competencyLevel != null && competencyLevel >= 4 -> "⭐"
            else -> null
        }
    }
}

/**
 * Representa una fila de estación en el grid
 */
data class RotationGridRow(
    val workstationId: Long,
    val workstationName: String,
    val requiredWorkers: Int,
    val currentAssignments: List<RotationGridCell>,
    val nextAssignments: List<RotationGridCell>
) {
    
    /**
     * Obtiene el número de trabajadores asignados actualmente
     */
    fun getCurrentAssignedCount(): Int = currentAssignments.count { it.isAssigned }
    
    /**
     * Obtiene el número de trabajadores asignados para la siguiente rotación
     */
    fun getNextAssignedCount(): Int = nextAssignments.count { it.isAssigned }
    
    /**
     * Verifica si la estación está completamente staffed en rotación actual
     */
    fun isCurrentFullyStaffed(): Boolean = getCurrentAssignedCount() >= requiredWorkers
    
    /**
     * Verifica si la estación está completamente staffed en siguiente rotación
     */
    fun isNextFullyStaffed(): Boolean = getNextAssignedCount() >= requiredWorkers
    
    /**
     * Obtiene el porcentaje de ocupación actual
     */
    fun getCurrentUtilizationPercentage(): Float {
        return if (requiredWorkers > 0) {
            (getCurrentAssignedCount().toFloat() / requiredWorkers.toFloat() * 100f).coerceAtMost(100f)
        } else 0f
    }
    
    /**
     * Obtiene el porcentaje de ocupación siguiente
     */
    fun getNextUtilizationPercentage(): Float {
        return if (requiredWorkers > 0) {
            (getNextAssignedCount().toFloat() / requiredWorkers.toFloat() * 100f).coerceAtMost(100f)
        } else 0f
    }
    
    /**
     * Obtiene el color de la barra de capacidad actual
     */
    fun getCurrentCapacityColor(): String {
        val percentage = getCurrentUtilizationPercentage()
        return when {
            percentage >= 100f -> "#4CAF50"  // Verde - completo
            percentage >= 75f -> "#FF9800"   // Naranja - casi completo
            percentage >= 50f -> "#FFC107"   // Amarillo - medio
            percentage > 0f -> "#F44336"     // Rojo - insuficiente
            else -> "#E0E0E0"                // Gris - vacío
        }
    }
    
    /**
     * Obtiene el color de la barra de capacidad siguiente
     */
    fun getNextCapacityColor(): String {
        val percentage = getNextUtilizationPercentage()
        return when {
            percentage >= 100f -> "#4CAF50"  // Verde - completo
            percentage >= 75f -> "#FF9800"   // Naranja - casi completo
            percentage >= 50f -> "#FFC107"   // Amarillo - medio
            percentage > 0f -> "#F44336"     // Rojo - insuficiente
            else -> "#E0E0E0"                // Gris - vacío
        }
    }
}

/**
 * Representa el grid completo de rotación
 */
data class RotationGrid(
    val sessionId: Long,
    val sessionName: String,
    val rows: List<RotationGridRow>,
    val availableWorkers: List<AvailableWorker>,
    val lastUpdated: Long = System.currentTimeMillis()
) {
    
    /**
     * Obtiene el total de trabajadores asignados en rotación actual
     */
    fun getTotalCurrentAssigned(): Int = rows.sumOf { it.getCurrentAssignedCount() }
    
    /**
     * Obtiene el total de trabajadores asignados en siguiente rotación
     */
    fun getTotalNextAssigned(): Int = rows.sumOf { it.getNextAssignedCount() }
    
    /**
     * Obtiene el total de trabajadores requeridos
     */
    fun getTotalRequired(): Int = rows.sumOf { it.requiredWorkers }
    
    /**
     * Obtiene el número de estaciones completamente staffed en rotación actual
     */
    fun getCurrentFullyStaffedStations(): Int = rows.count { it.isCurrentFullyStaffed() }
    
    /**
     * Obtiene el número de estaciones completamente staffed en siguiente rotación
     */
    fun getNextFullyStaffedStations(): Int = rows.count { it.isNextFullyStaffed() }
    
    /**
     * Obtiene el porcentaje de completitud general actual
     */
    fun getCurrentCompletionPercentage(): Float {
        return if (getTotalRequired() > 0) {
            (getTotalCurrentAssigned().toFloat() / getTotalRequired().toFloat() * 100f).coerceAtMost(100f)
        } else 0f
    }
    
    /**
     * Obtiene el porcentaje de completitud general siguiente
     */
    fun getNextCompletionPercentage(): Float {
        return if (getTotalRequired() > 0) {
            (getTotalNextAssigned().toFloat() / getTotalRequired().toFloat() * 100f).coerceAtMost(100f)
        } else 0f
    }
    
    /**
     * Verifica si hay conflictos en las asignaciones
     */
    fun hasConflicts(): Boolean {
        return rows.any { row ->
            row.currentAssignments.any { it.conflictReason != null } ||
            row.nextAssignments.any { it.conflictReason != null }
        }
    }
    
    /**
     * Obtiene la lista de conflictos encontrados
     */
    fun getConflicts(): List<String> {
        val conflicts = mutableListOf<String>()
        rows.forEach { row ->
            row.currentAssignments.forEach { cell ->
                cell.conflictReason?.let { conflicts.add("${row.workstationName} (Actual): $it") }
            }
            row.nextAssignments.forEach { cell ->
                cell.conflictReason?.let { conflicts.add("${row.workstationName} (Siguiente): $it") }
            }
        }
        return conflicts
    }
}

/**
 * Representa un trabajador disponible para asignación
 */
data class AvailableWorker(
    val workerId: Long,
    val workerName: String,
    val employeeId: String?,
    val isActive: Boolean,
    val availableWorkstations: List<WorkstationCapability>,
    val currentAssignment: Long? = null, // ID de estación actual
    val nextAssignment: Long? = null,    // ID de estación siguiente
    val isAssignedInCurrent: Boolean = false,
    val isAssignedInNext: Boolean = false
) {
    
    /**
     * Verifica si el trabajador puede ser asignado a una estación específica
     */
    fun canBeAssignedTo(workstationId: Long): Boolean {
        return availableWorkstations.any { 
            it.workstationId == workstationId && it.canBeAssigned 
        }
    }
    
    /**
     * Obtiene la capacidad para una estación específica
     */
    fun getCapabilityFor(workstationId: Long): WorkstationCapability? {
        return availableWorkstations.find { it.workstationId == workstationId }
    }
    
    /**
     * Verifica si está disponible para asignación
     */
    fun isAvailableForAssignment(): Boolean {
        return isActive && !isAssignedInCurrent && !isAssignedInNext
    }
    
    /**
     * Obtiene el color del trabajador según su estado
     */
    fun getStatusColor(): String {
        return when {
            !isActive -> "#F44336"                    // Rojo - inactivo
            isAssignedInCurrent && isAssignedInNext -> "#4CAF50"  // Verde - completamente asignado
            isAssignedInCurrent || isAssignedInNext -> "#FF9800"  // Naranja - parcialmente asignado
            else -> "#2196F3"                         // Azul - disponible
        }
    }
}

/**
 * Representa la capacidad de un trabajador en una estación específica
 */
data class WorkstationCapability(
    val workstationId: Long,
    val workstationName: String,
    val competencyLevel: Int,
    val canBeLeader: Boolean,
    val canTrain: Boolean,
    val isCertified: Boolean,
    val canBeAssigned: Boolean
) {
    
    /**
     * Calcula el puntaje de idoneidad para esta estación
     */
    fun getSuitabilityScore(): Double {
        var score = competencyLevel / 5.0
        if (isCertified) score += 0.2
        if (canBeLeader) score += 0.1
        if (canTrain) score += 0.1
        return score.coerceIn(0.0, 1.0)
    }
    
    /**
     * Obtiene el texto del nivel de competencia
     */
    fun getCompetencyText(): String {
        return when (competencyLevel) {
            1 -> "Principiante"
            2 -> "Básico"
            3 -> "Intermedio"
            4 -> "Avanzado"
            5 -> "Experto"
            else -> "Desconocido"
        }
    }
}

/**
 * Representa una operación de drag & drop en el grid
 */
data class RotationDragOperation(
    val workerId: Long,
    val workerName: String,
    val sourceWorkstationId: Long?,
    val targetWorkstationId: Long,
    val rotationType: String,
    val isValid: Boolean,
    val validationMessage: String?
) {
    
    /**
     * Verifica si es una operación de movimiento (no nueva asignación)
     */
    fun isMove(): Boolean = sourceWorkstationId != null
    
    /**
     * Verifica si es una nueva asignación
     */
    fun isNewAssignment(): Boolean = sourceWorkstationId == null
}