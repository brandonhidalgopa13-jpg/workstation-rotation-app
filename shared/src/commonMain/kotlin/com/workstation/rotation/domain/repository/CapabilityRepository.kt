package com.workstation.rotation.domain.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.workstation.rotation.database.AppDatabase
import com.workstation.rotation.domain.mappers.toModel
import com.workstation.rotation.domain.models.CapabilityModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class CapabilityRepository(private val database: AppDatabase) {
    
    fun getCapabilitiesByWorker(workerId: Long): Flow<List<CapabilityModel>> {
        return database.appDatabaseQueries.getCapabilitiesByWorker(workerId)
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { capabilities -> capabilities.map { it.toModel() } }
    }
    
    fun getCapabilitiesByWorkstation(workstationId: Long): Flow<List<CapabilityModel>> {
        return database.appDatabaseQueries.getCapabilitiesByWorkstation(workstationId)
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { capabilities -> capabilities.map { it.toModel() } }
    }
    
    suspend fun insertCapability(capability: CapabilityModel) = withContext(Dispatchers.Default) {
        database.appDatabaseQueries.insertCapability(
            workerId = capability.workerId,
            workstationId = capability.workstationId,
            proficiencyLevel = capability.proficiencyLevel.toLong(),
            certificationDate = capability.certificationDate
        )
    }
    
    suspend fun deleteCapability(capabilityId: Long) = withContext(Dispatchers.Default) {
        database.appDatabaseQueries.deleteCapability(capabilityId)
    }
    
    suspend fun deleteCapabilitiesByWorker(workerId: Long) = withContext(Dispatchers.Default) {
        database.appDatabaseQueries.deleteCapabilitiesByWorker(workerId)
    }
}
