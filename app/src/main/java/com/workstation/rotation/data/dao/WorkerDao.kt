package com.workstation.rotation.data.dao

import androidx.room.*
import com.workstation.rotation.data.entities.Worker
import com.workstation.rotation.data.entities.WorkerWorkstation
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkerDao {
    
    @Query("SELECT * FROM workers WHERE isActive = 1 ORDER BY name")
    fun getAllActiveWorkers(): Flow<List<Worker>>
    
    @Query("SELECT * FROM workers ORDER BY name")
    fun getAllWorkers(): Flow<List<Worker>>
    
    @Query("SELECT * FROM workers WHERE id = :id")
    suspend fun getWorkerById(id: Long): Worker?
    
    @Insert
    suspend fun insertWorker(worker: Worker): Long
    
    @Update
    suspend fun updateWorker(worker: Worker)
    
    @Delete
    suspend fun deleteWorker(worker: Worker)
    
    @Query("UPDATE workers SET isActive = :isActive WHERE id = :id")
    suspend fun updateWorkerStatus(id: Long, isActive: Boolean)
    
    // Worker-Workstation relationships
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkerWorkstation(workerWorkstation: WorkerWorkstation)
    
    @Delete
    suspend fun deleteWorkerWorkstation(workerWorkstation: WorkerWorkstation)
    
    @Query("DELETE FROM worker_workstations WHERE workerId = :workerId")
    suspend fun deleteAllWorkerWorkstations(workerId: Long)
    
    @Query("""
        SELECT workstationId FROM worker_workstations 
        WHERE workerId = :workerId
    """)
    suspend fun getWorkerWorkstationIds(workerId: Long): List<Long>
    
    @Query("""
        SELECT DISTINCT w.* FROM workers w
        INNER JOIN worker_workstations ww ON w.id = ww.workerId
        WHERE w.isActive = 1
        ORDER BY w.name
    """)
    fun getWorkersWithWorkstations(): Flow<List<Worker>>
    
    // Rotation tracking methods
    @Query("""
        UPDATE workers 
        SET currentWorkstationId = :workstationId, 
            rotationsInCurrentStation = CASE 
                WHEN currentWorkstationId = :workstationId THEN rotationsInCurrentStation + 1 
                ELSE 1 
            END,
            lastRotationTimestamp = :timestamp
        WHERE id = :workerId
    """)
    suspend fun updateWorkerRotation(workerId: Long, workstationId: Long, timestamp: Long)
    
    @Query("""
        SELECT * FROM workers 
        WHERE isActive = 1 
        AND isTrainer = 0 
        AND isTrainee = 0 
        AND rotationsInCurrentStation >= :minRotations
        ORDER BY rotationsInCurrentStation DESC, name
    """)
    suspend fun getWorkersNeedingRotation(minRotations: Int = 2): List<Worker>
    
    @Query("UPDATE workers SET rotationsInCurrentStation = 0 WHERE id = :workerId")
    suspend fun resetWorkerRotationCount(workerId: Long)
}