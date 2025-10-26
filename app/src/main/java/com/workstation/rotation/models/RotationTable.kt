package com.workstation.rotation.models

import com.workstation.rotation.data.entities.Worker
import com.workstation.rotation.data.entities.Workstation

/**
 * Data class representing the complete rotation table for visualization.
 * 
 * @property workstations List of all workstations in the rotation
 * @property currentPhase Map of workstation ID to list of workers in current phase
 * @property nextPhase Map of workstation ID to list of workers in next phase
 */
data class RotationTable(
    val workstations: List<Workstation>,
    val currentPhase: Map<Long, List<Worker>>,
    val nextPhase: Map<Long, List<Worker>>
) {
    /**
     * Gets the total number of workers in the current phase.
     */
    fun getTotalCurrentWorkers(): Int {
        return currentPhase.values.sumOf { it.size }
    }
    
    /**
     * Gets the total number of workers in the next phase.
     */
    fun getTotalNextWorkers(): Int {
        return nextPhase.values.sumOf { it.size }
    }
    
    /**
     * Gets the total capacity of all workstations.
     */
    fun getTotalCapacity(): Int {
        return workstations.sumOf { it.requiredWorkers }
    }
    
    /**
     * Gets the current occupancy percentage.
     */
    fun getCurrentOccupancyPercentage(): Double {
        val totalCapacity = getTotalCapacity()
        return if (totalCapacity > 0) {
            (getTotalCurrentWorkers().toDouble() / totalCapacity) * 100
        } else {
            0.0
        }
    }
    
    /**
     * Gets workstations that are at full capacity in current phase.
     */
    fun getFullCapacityStations(): List<Workstation> {
        return workstations.filter { station ->
            val currentWorkers = currentPhase[station.id]?.size ?: 0
            currentWorkers >= station.requiredWorkers
        }
    }
    
    /**
     * Gets priority workstations.
     */
    fun getPriorityWorkstations(): List<Workstation> {
        return workstations.filter { it.isPriority }
    }
    
    /**
     * Checks if all priority workstations are at full capacity.
     */
    fun areAllPriorityStationsFull(): Boolean {
        return getPriorityWorkstations().all { station ->
            val currentWorkers = currentPhase[station.id]?.size ?: 0
            currentWorkers >= station.requiredWorkers
        }
    }
}