package com.workstation.rotation.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workstations")
data class Workstation(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val requiredWorkers: Int = 1,
    val isPriority: Boolean = false,
    val isActive: Boolean = true
)