package com.workstation.rotation.domain.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.workstation.rotation.database.AppDatabase
import com.workstation.rotation.domain.mappers.toModel
import com.workstation.rotation.domain.models.WorkstationModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class WorkstationRepository(private val database: AppDatabase) {
    
    fun getAllWorkstations(): Flow<List<WorkstationModel>> {
        return database.appDatabaseQueries.getAllWorkstations()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { workstations -> workstations.map { it.toModel() } }
    }
    
    fun getActiveWorkstations(): Flow<List<WorkstationModel>> {
        return database.appDatabaseQueries.getActiveWorkstations()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { workstations -> workstations.map { it.toModel() } }
    }
    
    suspend fun getWorkstationById(id: Long): WorkstationModel? = withContext(Dispatchers.Default) {
        database.appDatabaseQueries.getWorkstationById(id).executeAsOneOrNull()?.toModel()
    }
    
    suspend fun insertWorkstation(workstation: WorkstationModel) = withContext(Dispatchers.Default) {
        database.appDatabaseQueries.insertWorkstation(
            name = workstation.name,
            code = workstation.code,
            description = workstation.description,
            isActive = workstation.isActive,
            requiredWorkers = workstation.requiredWorkers.toLong(),
            createdAt = workstation.createdAt,
            updatedAt = workstation.updatedAt
        )
    }
    
    suspend fun updateWorkstation(workstation: WorkstationModel) = withContext(Dispatchers.Default) {
        database.appDatabaseQueries.updateWorkstation(
            name = workstation.name,
            code = workstation.code,
            description = workstation.description,
            isActive = workstation.isActive,
            requiredWorkers = workstation.requiredWorkers.toLong(),
            updatedAt = System.currentTimeMillis(),
            id = workstation.id
        )
    }
    
    suspend fun deleteWorkstation(workstationId: Long) = withContext(Dispatchers.Default) {
        database.appDatabaseQueries.deleteWorkstation(workstationId)
    }
}
