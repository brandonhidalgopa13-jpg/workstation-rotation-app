package com.workstation.rotation.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing a worker in the rotation system.
 * 
 * @property id Unique identifier for the worker
 * @property name Full name of the worker
 * @property email Email address (optional)
 * @property availabilityPercentage Availability percentage (0-100%)
 * @property restrictionNotes Notes about any work restrictions
 * @property isTrainer Whether this worker can train others
 * @property isTrainee Whether this worker is currently in training
 * @property trainerId ID of the assigned trainer (if isTrainee is true)
 * @property trainingWorkstationId ID of the workstation where training occurs
 * @property isActive Whether the worker is currently active in the system
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