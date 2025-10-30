package com.workstation.rotation.data.dao

import androidx.room.*
import com.workstation.rotation.data.entities.WorkerRestriction
import com.workstation.rotation.data.entities.RestrictionType
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkerRestrictionDao {
    
    @Query("SELECT * FROM worker_restrictions WHERE workerId = :workerId AND isActive = 1")
    fun getWorkerRestrictions(workerId: Long): Flow<List<WorkerRestriction>>
    
    @Query("SELECT * FROM worker_restrictions WHERE workerId = :workerId AND isActive = 1")
    suspend fun getWorkerRestrictionsSync(workerId: Long): List<WorkerRestriction>
    
    @Query("SELECT * FROM worker_restrictions WHERE workstationId = :workstationId AND isActive = 1")
    fun getWorkstationRestrictions(workstationId: Long): Flow<List<WorkerRestriction>>
    
    @Query("""
        SELECT * FROM worker_restrictions 
        WHERE workerId = :workerId AND workstationId = :workstationId AND isActive = 1
    """)
    suspend fun getWorkerWorkstationRestriction(workerId: Long, workstationId: Long): WorkerRestriction?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRestriction(restriction: WorkerRestriction)
    
    @Update
    suspend fun updateRestriction(restriction: WorkerRestriction)
    
    @Delete
    suspend fun deleteRestriction(restriction: WorkerRestriction)
    
    @Query("DELETE FROM worker_restrictions WHERE workerId = :workerId")
    suspend fun deleteAllWorkerRestrictions(workerId: Long)
    
    @Query("DELETE FROM worker_restrictions WHERE workstationId = :workstationId")
    suspend fun deleteAllWorkstationRestrictions(workstationId: Long)
    
    @Query("""
        UPDATE worker_restrictions 
        SET isActive = :isActive 
        WHERE workerId = :workerId AND workstationId = :workstationId
    """)
    suspend fun updateRestrictionStatus(workerId: Long, workstationId: Long, isActive: Boolean)
    
    @Query("""
        SELECT COUNT(*) > 0 FROM worker_restrictions 
        WHERE workerId = :workerId AND workstationId = :workstationId 
        AND restrictionType = :restrictionType AND isActive = 1
    """)
    suspend fun hasRestriction(workerId: Long, workstationId: Long, restrictionType: RestrictionType): Boolean
    
    @Query("""
        SELECT COUNT(*) FROM worker_restrictions 
        WHERE workerId = :workerId AND isActive = 1
    """)
    suspend fun getWorkerRestrictionCount(workerId: Long): Int
    
    // Para el algoritmo de rotación
    @Query("""
        SELECT workstationId FROM worker_restrictions 
        WHERE workerId = :workerId AND restrictionType = 'PROHIBITED' AND isActive = 1
    """)
    suspend fun getProhibitedWorkstations(workerId: Long): List<Long>
    
    @Query("""
        SELECT workstationId FROM worker_restrictions 
        WHERE workerId = :workerId AND restrictionType IN ('LIMITED', 'TEMPORARY') AND isActive = 1
    """)
    suspend fun getLimitedWorkstations(workerId: Long): List<Long>
    
    // Limpiar restricciones expiradas
    @Query("""
        UPDATE worker_restrictions 
        SET isActive = 0 
        WHERE expiresAt IS NOT NULL AND expiresAt < :currentTime AND isActive = 1
    """)
    suspend fun deactivateExpiredRestrictions(currentTime: Long = System.currentTimeMillis())
    
    // Para respaldo y sincronización
    @Query("SELECT * FROM worker_restrictions ORDER BY workerId, workstationId")
    suspend fun getAllRestrictionsSync(): List<WorkerRestriction>
    
    @Query("DELETE FROM worker_restrictions")
    suspend fun deleteAllRestrictions()
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateRestriction(restriction: WorkerRestriction)
}