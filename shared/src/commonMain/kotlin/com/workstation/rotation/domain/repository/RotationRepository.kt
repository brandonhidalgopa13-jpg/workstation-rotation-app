package com.workstation.rotation.domain.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.workstation.rotation.database.AppDatabase
import com.workstation.rotation.domain.models.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class RotationRepository(private val database: AppDatabase) {
    
    // Workers
    fun getAllWorkers(): Flow<List<WorkerModel>> {
        return database.workerQueries.selectAll()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { workers ->
                workers.map { worker ->
                    WorkerModel(
                        id = worker.id,
                        name = worker.name,
                        code = worker.code,
                        isActive = worker.isActive,
                        photoPath = worker.photoPath,
                        createdAt = worker.createdAt,
                        updatedAt = worker.updatedAt
                    )
                }
            }
    }
    
    fun getActiveWorkers(): Flow<List<WorkerModel>> {
        return database.workerQueries.selectActive()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { workers ->
                workers.map { worker ->
                    WorkerModel(
                        id = worker.id,
                        name = worker.name,
                        code = worker.code,
                        isActive = worker.isActive,
                        photoPath = worker.photoPath,
                        createdAt = worker.createdAt,
                        updatedAt = worker.updatedAt
                    )
                }
            }
    }
    
    suspend fun insertWorker(worker: WorkerModel): Long {
        database.workerQueries.insert(
            name = worker.name,
            code = worker.code,
            isActive = worker.isActive,
            photoPath = worker.photoPath,
            createdAt = worker.createdAt,
            updatedAt = worker.updatedAt
        )
        return database.workerQueries.lastInsertRowId().executeAsOne()
    }
    
    suspend fun updateWorker(worker: WorkerModel) {
        database.workerQueries.update(
            name = worker.name,
            code = worker.code,
            isActive = worker.isActive,
            photoPath = worker.photoPath,
            updatedAt = System.currentTimeMillis(),
            id = worker.id
        )
    }
    
    suspend fun deleteWorker(id: Long) {
        database.workerQueries.delete(id)
    }
    
    // Workstations
    fun getAllWorkstations(): Flow<List<WorkstationModel>> {
        return database.workstationQueries.selectAll()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { workstations ->
                workstations.map { ws ->
                    WorkstationModel(
                        id = ws.id,
                        name = ws.name,
                        code = ws.code,
                        isActive = ws.isActive,
                        requiredCapabilities = Json.decodeFromString(ws.requiredCapabilities),
                        createdAt = ws.createdAt,
                        updatedAt = ws.updatedAt
                    )
                }
            }
    }
    
    fun getActiveWorkstations(): Flow<List<WorkstationModel>> {
        return database.workstationQueries.selectActive()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { workstations ->
                workstations.map { ws ->
                    WorkstationModel(
                        id = ws.id,
                        name = ws.name,
                        code = ws.code,
                        isActive = ws.isActive,
                        requiredCapabilities = Json.decodeFromString(ws.requiredCapabilities),
                        createdAt = ws.createdAt,
                        updatedAt = ws.updatedAt
                    )
                }
            }
    }
    
    suspend fun insertWorkstation(workstation: WorkstationModel): Long {
        database.workstationQueries.insert(
            name = workstation.name,
            code = workstation.code,
            isActive = workstation.isActive,
            requiredCapabilities = Json.encodeToString(workstation.requiredCapabilities),
            createdAt = workstation.createdAt,
            updatedAt = workstation.updatedAt
        )
        return database.workstationQueries.lastInsertRowId().executeAsOne()
    }
    
    suspend fun updateWorkstation(workstation: WorkstationModel) {
        database.workstationQueries.update(
            name = workstation.name,
            code = workstation.code,
            isActive = workstation.isActive,
            requiredCapabilities = Json.encodeToString(workstation.requiredCapabilities),
            updatedAt = System.currentTimeMillis(),
            id = workstation.id
        )
    }
    
    suspend fun deleteWorkstation(id: Long) {
        database.workstationQueries.delete(id)
    }
    
    // Rotation Sessions
    fun getAllSessions(): Flow<List<RotationSessionModel>> {
        return database.rotationSessionQueries.selectAll()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { sessions ->
                sessions.map { session ->
                    RotationSessionModel(
                        id = session.id,
                        name = session.name,
                        createdAt = session.createdAt,
                        rotationIntervalMinutes = session.rotationIntervalMinutes.toInt(),
                        isActive = session.isActive
                    )
                }
            }
    }
    
    suspend fun insertSession(session: RotationSessionModel): Long {
        database.rotationSessionQueries.insert(
            name = session.name,
            createdAt = session.createdAt,
            rotationIntervalMinutes = session.rotationIntervalMinutes.toLong(),
            isActive = session.isActive
        )
        return database.rotationSessionQueries.lastInsertRowId().executeAsOne()
    }
    
    suspend fun getAssignmentsBySession(sessionId: Long): List<RotationAssignmentModel> {
        return database.rotationAssignmentQueries.selectBySession(sessionId)
            .executeAsList()
            .map { assignment ->
                RotationAssignmentModel(
                    id = assignment.id,
                    sessionId = assignment.sessionId,
                    workerId = assignment.workerId,
                    workstationId = assignment.workstationId,
                    rotationOrder = assignment.rotationOrder.toInt(),
                    startTime = assignment.startTime,
                    endTime = assignment.endTime
                )
            }
    }
    
    suspend fun insertAssignment(assignment: RotationAssignmentModel) {
        database.rotationAssignmentQueries.insert(
            sessionId = assignment.sessionId,
            workerId = assignment.workerId,
            workstationId = assignment.workstationId,
            rotationOrder = assignment.rotationOrder.toLong(),
            startTime = assignment.startTime,
            endTime = assignment.endTime
        )
    }
}
