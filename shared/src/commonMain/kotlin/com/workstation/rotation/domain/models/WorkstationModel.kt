package com.workstation.rotation.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class WorkstationModel(
    val id: Long = 0,
    val name: String,
    val code: String,
    val isActive: Boolean = true,
    val requiredCapabilities: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
