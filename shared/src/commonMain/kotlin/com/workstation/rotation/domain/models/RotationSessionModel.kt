package com.workstation.rotation.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class RotationSessionModel(
    val id: Long = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis(),
    val rotationIntervalMinutes: Int = 60,
    val isActive: Boolean = true
)

@Serializable
data class RotationAssignmentModel(
    val id: Long = 0,
    val sessionId: Long,
    val workerId: Long,
    val workstationId: Long,
    val rotationOrder: Int,
    val startTime: Long? = null,
    val endTime: Long? = null
)
