package com.workstation.rotation.models

import com.workstation.rotation.data.entities.Worker
import com.workstation.rotation.data.entities.Workstation

/**
 * Data class representing a workstation column in the rotation table view.
 * 
 * @property workstation The workstation information
 * @property currentWorkers List of workers currently assigned to this workstation
 * @property nextWorkers List of workers assigned to this workstation in next rotation
 */
data class WorkstationColumn(
    val workstation: Workstation,
    val currentWorkers: List<Worker> = emptyList(),
    val nextWorkers: List<Worker> = emptyList()
) {
    /**
     * Gets the current occupancy percentage for this workstation.
     */
    fun getCurrentOccupancyPercentage(): Double {
        return if (workstation.requiredWorkers > 0) {
            (currentWorkers.size.toDouble() / workstation.requiredWorkers) * 100
        } else {
            0.0
        }
    }
    
    /**
     * Gets the next occupancy percentage for this workstation.
     */
    fun getNextOccupancyPercentage(): Double {
        return if (workstation.requiredWorkers > 0) {
            (nextWorkers.size.toDouble() / workstation.requiredWorkers) * 100
        } else {
            0.0
        }
    }
    
    /**
     * Checks if the workstation is at full capacity in current phase.
     */
    fun isCurrentlyAtCapacity(): Boolean {
        return currentWorkers.size >= workstation.requiredWorkers
    }
    
    /**
     * Checks if the workstation will be at full capacity in next phase.
     */
    fun willBeAtCapacity(): Boolean {
        return nextWorkers.size >= workstation.requiredWorkers
    }
    
    /**
     * Gets the current capacity status as a formatted string.
     */
    fun getCurrentCapacityStatus(): String {
        return "${currentWorkers.size}/${workstation.requiredWorkers}"
    }
    
    /**
     * Gets the next capacity status as a formatted string.
     */
    fun getNextCapacityStatus(): String {
        return "${nextWorkers.size}/${workstation.requiredWorkers}"
    }
    
    /**
     * Gets training pairs in current workers.
     */
    fun getCurrentTrainingPairs(): List<Pair<Worker, Worker>> {
        val pairs = mutableListOf<Pair<Worker, Worker>>()
        val trainees = currentWorkers.filter { it.isTrainee && it.trainerId != null }
        
        for (trainee in trainees) {
            val trainer = currentWorkers.find { it.id == trainee.trainerId }
            if (trainer != null) {
                pairs.add(Pair(trainer, trainee))
            }
        }
        
        return pairs
    }
    
    /**
     * Gets training pairs in next workers.
     */
    fun getNextTrainingPairs(): List<Pair<Worker, Worker>> {
        val pairs = mutableListOf<Pair<Worker, Worker>>()
        val trainees = nextWorkers.filter { it.isTrainee && it.trainerId != null }
        
        for (trainee in trainees) {
            val trainer = nextWorkers.find { it.id == trainee.trainerId }
            if (trainer != null) {
                pairs.add(Pair(trainer, trainee))
            }
        }
        
        return pairs
    }
}