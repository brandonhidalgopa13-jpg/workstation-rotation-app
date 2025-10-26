package com.workstation.rotation.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "worker_workstations",
    primaryKeys = ["workerId", "workstationId"],
    foreignKeys = [
        ForeignKey(
            entity = Worker::class,
            parentColumns = ["id"],
            childColumns = ["workerId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Workstation::class,
            parentColumns = ["id"],
            childColumns = ["workstationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("workerId"), Index("workstationId")]
)
data class WorkerWorkstation(
    val workerId: Long,
    val workstationId: Long
)