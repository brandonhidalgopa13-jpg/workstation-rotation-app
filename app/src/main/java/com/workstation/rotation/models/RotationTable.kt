package com.workstation.rotation.models

import com.workstation.rotation.data.entities.Worker
import com.workstation.rotation.data.entities.Workstation

/**
 * Data class representing a tabular rotation view with workstations as columns
 * and workers distributed in current and next phases.
 */
data class RotationTable(
    val workstations: List<Workstation>,
    val currentPhase: Map<Long, List<Worker>>, // workstationId -> workers
    val nextPhase: Map<Long, List<Worker>>     // workstationId -> workers
)

/**
 * Data class for individual workstation column in the rotation table.
 */
data class WorkstationColumn(
    val workstation: Workstation,
    val currentWorkers: List<Worker>,
    val nextWorkers: List<Worker>
) {
    /**
     * Gets the capacity status for current phase.
     */
    fun getCurrentCapacityStatus(): String {
        return "${currentWorkers.size}/${workstation.requiredWorkers}"
    }
    
    /**
     * Gets the capacity status for next phase.
     */
    fun getNextCapacityStatus(): String {
        return "${nextWorkers.size}/${workstation.requiredWorkers}"
    }
    
    /**
     * Checks if current phase is at full capacity.
     */
    fun isCurrentAtCapacity(): Boolean {
        return currentWorkers.size >= workstation.requiredWorkers
    }
    
    /**
     * Checks if next phase is at full capacity.
     */
    fun isNextAtCapacity(): Boolean {
        return nextWorkers.size >= workstation.requiredWorkers
    }
}