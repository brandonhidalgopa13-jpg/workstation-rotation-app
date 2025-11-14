package com.workstation.rotation.domain.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.workstation.rotation.database.AppDatabase
import com.workstation.rotation.domain.mappers.toModel
import com.workstation.rotation.domain.models.WorkerModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class WorkerRepository(private val database: AppDatabase) {
    
    fun getAllWorkers(): Flow<List<WorkerModel>> {
        return database.appDatabaseQueries.getAllWorkers()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { workers -> workers.map { it.toModel() } }
    }
    
    fun getActiveWorkers(): Flow<List<WorkerModel>> {
        return database.appDatabaseQueries.getActiveWorkers()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { workers -> workers.map { it.toModel() } }
    }
    
    suspend fun getWorkerById(id: Long): WorkerModel? = withContext(Dispatchers.Default) {
        database.appDatabaseQueries.getWorkerById(id).executeAsOneOrNull()?.toModel()
    }
    
    suspend fun insertWorker(worker: WorkerModel) = withContext(Dispatchers.Default) {
        database.appDatabaseQueries.insertWorker(
            name = worker.name,
            employeeId = worker.employeeId,
            isActive = worker.isActive,
            photoPath = worker.photoPath,
            createdAt = worker.createdAt,
            updatedAt = worker.updatedAt
        )
    }
    
    suspend fun updateWorker(worker: WorkerModel) = withContext(Dispatchers.Default) {
        database.appDatabaseQueries.updateWorker(
            name = worker.name,
            employeeId = worker.employeeId,
            isActive = worker.isActive,
            photoPath = worker.photoPath,
            updatedAt = System.currentTimeMillis(),
            id = worker.id
        )
    }
    
    suspend fun deleteWorker(workerId: Long) = withContext(Dispatchers.Default) {
        database.appDatabaseQueries.deleteWorker(workerId)
    }
}
