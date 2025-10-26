package com.workstation.rotation.models

/**
 * Data class representing a single rotation item for display.
 * 
 * @property workerName The formatted name of the worker with status indicators
 * @property currentWorkstation The current workstation assignment with capacity info
 * @property nextWorkstation The next workstation assignment with capacity info
 * @property rotationOrder The order of this item in the rotation sequence
 */
data class RotationItem(
    val workerName: String,
    val currentWorkstation: String,
    val nextWorkstation: String,
    val rotationOrder: Int
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