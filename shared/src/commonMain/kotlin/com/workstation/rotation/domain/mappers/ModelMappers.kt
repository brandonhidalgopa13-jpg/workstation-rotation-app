package com.workstation.rotation.domain.mappers

import com.workstation.rotation.database.RotationAssignment
import com.workstation.rotation.database.RotationSession
import com.workstation.rotation.database.Worker
import com.workstation.rotation.database.WorkerWorkstationCapability
import com.workstation.rotation.database.Workstation
import com.workstation.rotation.domain.models.CapabilityModel
import com.workstation.rotation.domain.models.RotationAssignmentModel
import com.workstation.rotation.domain.models.RotationSessionModel
import com.workstation.rotation.domain.models.WorkerModel
import com.workstation.rotation.domain.models.WorkstationModel

/**
 * Extensiones para convertir entidades de SQLDelight a modelos de dominio
 */

// Worker mappers
fun Worker.toModel() = WorkerModel(
    id = id,
    name = name,
    employeeId = employeeId,
    isActive = isActive,
    photoPath = photoPath,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun WorkerModel.toInsertParams() = Triple(
    name,
    employeeId,
    photoPath
)

// Workstation mappers
fun Workstation.toModel() = WorkstationModel(
    id = id,
    name = name,
    code = code,
    description = description,
    isActive = isActive,
    requiredWorkers = requiredWorkers.toInt(),
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun WorkstationModel.toInsertParams() = Triple(
    name,
    code,
    description
)

// Capability mappers
fun WorkerWorkstationCapability.toModel() = CapabilityModel(
    id = id,
    workerId = workerId,
    workstationId = workstationId,
    proficiencyLevel = proficiencyLevel.toInt(),
    certificationDate = certificationDate
)

fun CapabilityModel.toInsertParams() = Triple(
    workerId,
    workstationId,
    proficiencyLevel.toLong()
)

// RotationSession mappers
fun RotationSession.toModel() = RotationSessionModel(
    id = id,
    name = name,
    startDate = startDate,
    endDate = endDate,
    isActive = isActive,
    createdAt = createdAt
)

fun RotationSessionModel.toInsertParams() = Triple(
    name,
    startDate,
    endDate
)

// RotationAssignment mappers
fun RotationAssignment.toModel() = RotationAssignmentModel(
    id = id,
    sessionId = sessionId,
    workerId = workerId,
    workstationId = workstationId,
    position = position.toInt(),
    assignedAt = assignedAt
)

fun RotationAssignmentModel.toInsertParams() = Pair(
    sessionId,
    workerId
)
