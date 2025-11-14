package com.workstation.rotation.domain.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOne
import com.workstation.rotation.database.AppDatabase
import com.workstation.rotation.domain.mappers.toModel
import com.workstation.rotation.domain.models.RotationAssignmentModel
import com.workstation.rotation.domain.models.RotationSessionModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class RotationRepository(private val database: AppDatabase) {
    
    // Session operations
    fun getAllSessions(): Flow<List<RotationSessionModel>> {
        return database.appDatabaseQueries.getAllSessions()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { sessions -> sessions.map { it.toModel() } }
    }
    
    fun getActiveSession(): Flow<RotationSessionModel?> {
        return database.appDatabaseQueries.getActiveSession()
            .asFlow()
            .mapToOne(Dispatchers.Default)
            .map { it.toModel() }
    }
    
    suspend fun getSessionById(id: Long): RotationSessionModel? = withContext(Dispatchers.Default) {
        database.appDatabaseQueries.getSessionById(id).executeAsOneOrNull()?.toModel()
    }
    
    suspend fun insertSession(session: RotationSessionModel) = withContext(Dispatchers.Default) {
        database.appDatabaseQueries.insertSession(
            name = session.name,
            startDate = session.startDate,
            endDate = session.endDate,
            isActive = session.isActive,
            createdAt = session.createdAt
        )
    }
    
    suspend fun updateSession(session: RotationSessionModel) = withContext(Dispatchers.Default) {
        database.appDatabaseQueries.updateSession(
            name = session.name,
            startDate = session.startDate,
            endDate = session.endDate,
            isActive = session.isActive,
            id = session.id
        )
    }
    
    suspend fun deleteSession(sessionId: Long) = withContext(Dispatchers.Default) {
        database.appDatabaseQueries.deleteSession(sessionId)
    }
    
    // Assignment operations
    fun getAssignmentsBySession(sessionId: Long): Flow<List<RotationAssignmentModel>> {
        return database.appDatabaseQueries.getAssignmentsBySession(sessionId)
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { assignments -> assignments.map { it.toModel() } }
    }
    
    suspend fun insertAssignment(assignment: RotationAssignmentModel) = withContext(Dispatchers.Default) {
        database.appDatabaseQueries.insertAssignment(
            sessionId = assignment.sessionId,
            workerId = assignment.workerId,
            workstationId = assignment.workstationId,
            position = assignment.position.toLong(),
            assignedAt = assignment.assignedAt
        )
    }
    
    suspend fun deleteAssignmentsBySession(sessionId: Long) = withContext(Dispatchers.Default) {
        database.appDatabaseQueries.deleteAssignmentsBySession(sessionId)
    }
    
    suspend fun deleteAssignment(assignmentId: Long) = withContext(Dispatchers.Default) {
        database.appDatabaseQueries.deleteAssignment(assignmentId)
    }
}
