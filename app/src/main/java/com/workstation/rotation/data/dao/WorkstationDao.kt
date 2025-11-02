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
    
    @Query("SELECT * FROM workstations WHERE isPriority = 1 AND isActive = 1 ORDER BY name")
    suspend fun getPriorityWorkstations(): List<Workstation>
    
    @Query("SELECT * FROM workstations WHERE isTrainingStation = 1 AND isActive = 1 ORDER BY name")
    suspend fun getTrainingWorkstations(): List<Workstation>
    
    @Query("SELECT COUNT(*) FROM workstations WHERE isActive = 1")
    suspend fun getActiveWorkstationCount(): Int
    
    @Query("SELECT SUM(requiredWorkers) FROM workstations WHERE isActive = 1")
    suspend fun getTotalRequiredWorkers(): Int?
    
    @Query("SELECT SUM(maxWorkers) FROM workstations WHERE isActive = 1")
    suspend fun getTotalMaxWorkers(): Int?
    
    // Métodos síncronos para respaldo
    @Query("SELECT * FROM workstations ORDER BY name")
    suspend fun getAllWorkstationsSync(): List<Workstation>
    
    @Query("DELETE FROM workstations")
    suspend fun deleteAllWorkstations()
    
    // Métodos para sincronización en la nube
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateWorkstation(workstation: Workstation): Long
    
    @Query("UPDATE workstations SET utilizationRate = :rate WHERE id = :id")
    suspend fun updateUtilizationRate(id: Long, rate: Double)
    
    @Query("UPDATE workstations SET lastMaintenanceDate = :date WHERE id = :id")
    suspend fun updateLastMaintenanceDate(id: Long, date: Long)
    
    @Query("UPDATE workstations SET nextMaintenanceDate = :date WHERE id = :id")
    suspend fun updateNextMaintenanceDate(id: Long, date: Long)
    
    @Query("""
        SELECT * FROM workstations 
        WHERE isActive = 1 
        AND (nextMaintenanceDate IS NULL OR nextMaintenanceDate > :currentTime)
        ORDER BY 
            CASE WHEN isPriority = 1 THEN 0 ELSE 1 END,
            name
    """)
    suspend fun getAvailableWorkstationsForRotation(currentTime: Long = System.currentTimeMillis()): List<Workstation>
}