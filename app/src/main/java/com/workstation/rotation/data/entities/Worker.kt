package com.workstation.rotation.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workers")
data class Worker(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val email: String = "",
    val availabilityPercentage: Int = 100, // 0-100% disponibilidad
    val restrictionNotes: String = "", // Notas sobre restricciones
    val isActive: Boolean = true
)