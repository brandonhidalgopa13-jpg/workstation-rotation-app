package com.workstation.rotation.data.dao

import androidx.room.*
import com.workstation.rotation.data.entities.Workstation
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkstationDao {
    
    @Query("SELECT * FROM workstations WHERE isActive = 1 ORDER BY name")
    fun getAllActiveWorkstations(): Flow<List<Workstation>>
    
    @Query("SELECT * FROM workstations ORDER BY name")
    fun getAllWorkstations(): Flow<List<Workstation>>
    
    @Query("SELECT * FROM workstations WHERE id = :id")
    suspend fun getWorkstationById(id: Long): Workstation?
    
    @Insert
    suspend fun insertWorkstation(workstation: Workstation): Long
    
    @Update
    suspend fun updateWorkstation(workstation: Workstation)
    
    @Delete
    suspend fun deleteWorkstation(workstation: Workstation)
    
    @Query("UPDATE workstations SET isActive = :isActive WHERE id = :id")
    suspend fun updateWorkstationStatus(id: Long, isActive: Boolean)
}