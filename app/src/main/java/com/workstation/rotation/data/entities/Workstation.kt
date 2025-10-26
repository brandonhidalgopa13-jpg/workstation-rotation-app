package com.workstation.rotation.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workstations")
data class Workstation(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val isActive: Boolean = true
)