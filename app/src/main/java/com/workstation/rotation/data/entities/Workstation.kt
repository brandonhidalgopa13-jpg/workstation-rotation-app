package com.workstation.rotation.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing a workstation in the rotation system.
 * 
 * @property id Unique identifier for the workstation
 * @property name Name of the workstation
 * @property requiredWorkers Number of workers required for this workstation
 * @property isPriority Whether this is a priority workstation (always maintains full capacity)
 * @property isActive Whether the workstation is currently active in the system
 */
@Entity(tableName = "workstations")
data class Workstation(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val requiredWorkers: Int = 1,
    val isPriority: Boolean = false,
    val isActive: Boolean = true
) {
    /**
     * Returns a display name for the workstation including priority status.
     */
    fun getDisplayName(): String {
        val priority = if (isPriority) " ⭐" else ""
        return "$name$priority"
    }
    
    /**
     * Gets the capacity info as a formatted string.
     */
    fun getCapacityInfo(currentWorkers: Int): String {
        return "($currentWorkers/$requiredWorkers)"
    }
    
    /**
     * Checks if the workstation is at full capacity.
     */
    fun isAtCapacity(currentWorkers: Int): Boolean {
        return currentWorkers >= requiredWorkers
    }
    
    /**
     * Gets the priority level as a descriptive string.
     */
    fun getPriorityLevel(): String {
        return if (isPriority) "Alta Prioridad" else "Normal"
    }
}