package com.workstation.rotation.models

/**
 * Data class representing a single rotation item for display.
 * 
 * @property workerId The ID of the worker
 * @property workerName The formatted name of the worker with status indicators
 * @property workstationId The ID of the workstation
 * @property workstationName The name of the workstation
 * @property currentWorkstation The current workstation assignment with capacity info
 * @property nextWorkstation The next workstation assignment with capacity info
 * @property rotationOrder The order of this item in the rotation sequence
 * @property phase The phase of the rotation (FIRST_HALF, SECOND_HALF)
 * @property isLeader Whether this worker is a leader
 * @property isTrainer Whether this worker is a trainer
 * @property isTrainee Whether this worker is a trainee
 * @property priority The priority level of this assignment
 */
data class RotationItem(
    val workerId: Long = 0,
    val workerName: String,
    val workstationId: Long = 0,
    val workstationName: String = "",
    val currentWorkstation: String = "",
    val nextWorkstation: String = "",
    val rotationOrder: Int = 0,
    val phase: String = "",
    val isLeader: Boolean = false,
    val isTrainer: Boolean = false,
    val isTrainee: Boolean = false,
    val priority: Int = 0
) {
    /**
     * Checks if this is a training rotation (contains training indicators).
     */
    fun isTrainingRotation(): Boolean {
        return workerName.contains("🤝") || workerName.contains("[ENTRENANDO]") || workerName.contains("[EN ENTRENAMIENTO]")
    }
    
    /**
     * Checks if this involves a priority workstation.
     */
    fun involvesPriorityStation(): Boolean {
        return currentWorkstation.contains("⭐") || nextWorkstation.contains("⭐")
    }
    
    /**
     * Gets the worker's base name without status indicators.
     */
    fun getWorkerBaseName(): String {
        return workerName.split(" [")[0].split(" 👨‍🏫")[0].split(" 🎯")[0].split(" 🔒")[0]
    }
}