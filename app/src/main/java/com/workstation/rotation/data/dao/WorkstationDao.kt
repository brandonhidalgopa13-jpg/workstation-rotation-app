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
    
    // Métodos síncronos para respaldo
    @Query("SELECT * FROM workstations ORDER BY name")
    suspend fun getAllWorkstationsSync(): List<Workstation>
    
    @Query("DELETE FROM workstations")
    suspend fun deleteAllWorkstations()
    
    // Métodos para sincronización en la nube
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateWorkstation(workstation: Workstation): Long
    
    @Query("SELECT COUNT(*) > 0 FROM workers WHERE trainingWorkstationId = :workstationId AND isTrainee = 1")
    suspend fun isWorkstationUsedForTraining(workstationId: Long): Boolean
}