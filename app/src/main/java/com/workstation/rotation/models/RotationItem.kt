package com.workstation.rotation.models

data class RotationItem(
    val workerName: String,
    val currentWorkstation: String,
    val nextWorkstation: String,
    val rotationOrder: Int,
    val isPriorityAssignment: Boolean = false
)