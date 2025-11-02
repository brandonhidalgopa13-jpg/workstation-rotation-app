package com.workstation.rotation.data.dao

import androidx.room.*
import androidx.lifecycle.LiveData
import com.workstation.rotation.data.entities.RotationHistory

/**
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 🗄️ DAO HISTORIAL DE ROTACIONES - OPERACIONES DE BASE DE DATOS
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 
 * 🎯 FUNCIONALIDADES:
 * • CRUD completo para registros de historial
 * • Consultas optimizadas para reportes y análisis
 * • Filtros por trabajador, estación, fecha y tipo
 * • Métricas agregadas para dashboards
 * • Operaciones de limpieza y mantenimiento
 * 
 * 📊 CONSULTAS ESPECIALIZADAS:
 * • Historial por trabajador con paginación
 * • Rotaciones activas en tiempo real
 * • Métricas de rendimiento por período
 * • Análisis de patrones de rotación
 * 
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 */

@Dao
interface RotationHistoryDao {
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // 🔧 OPERACIONES BÁSICAS CRUD
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    @Insert
    suspend fun insert(rotationHistory: RotationHistory): Long
    
    @Insert
    suspend fun insertAll(rotationHistories: List<RotationHistory>)
    
    @Update
    suspend fun update(rotationHistory: RotationHistory)
    
    @Delete
    suspend fun delete(rotationHistory: RotationHistory)
    
    @Query("DELETE FROM rotation_history WHERE id = :id")
    suspend fun deleteById(id: Long)
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // 📋 CONSULTAS DE LECTURA BÁSICAS
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    @Query("SELECT * FROM rotation_history WHERE id = :id")
    suspend fun getById(id: Long): RotationHistory?
    
    @Query("SELECT * FROM rotation_history ORDER BY rotation_date DESC")
    fun getAllLiveData(): LiveData<List<RotationHistory>>
    
    @Query("SELECT * FROM rotation_history ORDER BY rotation_date DESC")
    suspend fun getAll(): List<RotationHistory>
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // 🔍 CONSULTAS POR TRABAJADOR
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    @Query("""
        SELECT rh.*, w.name as worker_name, ws.name as workstation_name 
        FROM rotation_history rh
        INNER JOIN workers w ON rh.worker_id = w.id
        INNER JOIN workstations ws ON rh.workstation_id = ws.id
        WHERE rh.worker_id = :workerId 
        ORDER BY rh.rotation_date DESC
        LIMIT :limit OFFSET :offset
    """)
    suspend fun getByWorkerPaginated(workerId: Long, limit: Int, offset: Int): List<RotationHistoryWithNames>
    
    @Query("SELECT * FROM rotation_history WHERE worker_id = :workerId ORDER BY rotation_date DESC")
    fun getByWorkerLiveData(workerId: Long): LiveData<List<RotationHistory>>
    
    @Query("SELECT * FROM rotation_history WHERE worker_id = :workerId ORDER BY rotation_date DESC")
    suspend fun getByWorker(workerId: Long): List<RotationHistory>
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // 🏭 CONSULTAS POR ESTACIÓN
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    @Query("SELECT * FROM rotation_history WHERE workstation_id = :workstationId ORDER BY rotation_date DESC")
    fun getByWorkstationLiveData(workstationId: Long): LiveData<List<RotationHistory>>
    
    @Query("SELECT * FROM rotation_history WHERE workstation_id = :workstationId ORDER BY rotation_date DESC")
    suspend fun getByWorkstation(workstationId: Long): List<RotationHistory>
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // 📅 CONSULTAS POR FECHA Y PERÍODO
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    @Query("""
        SELECT * FROM rotation_history 
        WHERE rotation_date >= :startDate AND rotation_date <= :endDate 
        ORDER BY rotation_date DESC
    """)
    suspend fun getByDateRange(startDate: Long, endDate: Long): List<RotationHistory>
    
    @Query("""
        SELECT * FROM rotation_history 
        WHERE rotation_date >= :startDate AND rotation_date <= :endDate 
        ORDER BY rotation_date DESC
    """)
    fun getByDateRangeLiveData(startDate: Long, endDate: Long): LiveData<List<RotationHistory>>
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // 🔄 ROTACIONES ACTIVAS
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    @Query("SELECT * FROM rotation_history WHERE end_date IS NULL ORDER BY rotation_date DESC")
    fun getActiveRotationsLiveData(): LiveData<List<RotationHistory>>
    
    @Query("SELECT * FROM rotation_history WHERE end_date IS NULL ORDER BY rotation_date DESC")
    suspend fun getActiveRotations(): List<RotationHistory>
    
    @Query("SELECT * FROM rotation_history WHERE worker_id = :workerId AND end_date IS NULL")
    suspend fun getActiveRotationForWorker(workerId: Long): RotationHistory?
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // 📊 MÉTRICAS Y ESTADÍSTICAS
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    @Query("SELECT COUNT(*) FROM rotation_history")
    suspend fun getTotalRotations(): Int
    
    @Query("SELECT COUNT(*) FROM rotation_history WHERE worker_id = :workerId")
    suspend fun getTotalRotationsByWorker(workerId: Long): Int
    
    @Query("SELECT COUNT(*) FROM rotation_history WHERE workstation_id = :workstationId")
    suspend fun getTotalRotationsByWorkstation(workstationId: Long): Int
    
    @Query("""
        SELECT AVG(duration_minutes) FROM rotation_history 
        WHERE duration_minutes IS NOT NULL AND worker_id = :workerId
    """)
    suspend fun getAverageDurationByWorker(workerId: Long): Double?
    
    @Query("""
        SELECT AVG(performance_score) FROM rotation_history 
        WHERE performance_score IS NOT NULL AND worker_id = :workerId
    """)
    suspend fun getAveragePerformanceByWorker(workerId: Long): Double?
    
    @Query("""
        SELECT rotation_type, COUNT(*) as count 
        FROM rotation_history 
        GROUP BY rotation_type
    """)
    suspend fun getRotationTypeStats(): List<RotationTypeStats>
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // 🧹 OPERACIONES DE MANTENIMIENTO
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    @Query("DELETE FROM rotation_history WHERE rotation_date < :cutoffDate")
    suspend fun deleteOldRecords(cutoffDate: Long): Int
    
    @Query("""
        UPDATE rotation_history 
        SET end_date = :endDate, 
            duration_minutes = :duration,
            completed = 1
        WHERE id = :id
    """)
    suspend fun completeRotation(id: Long, endDate: Long, duration: Int)
    
    @Query("""
        UPDATE rotation_history 
        SET performance_score = :score 
        WHERE id = :id
    """)
    suspend fun updatePerformanceScore(id: Long, score: Double)
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // 📈 CONSULTAS AVANZADAS PARA REPORTES
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    @Query("""
        SELECT 
            w.name as worker_name,
            ws.name as workstation_name,
            COUNT(*) as total_rotations,
            AVG(rh.duration_minutes) as avg_duration,
            AVG(rh.performance_score) as avg_performance
        FROM rotation_history rh
        INNER JOIN workers w ON rh.worker_id = w.id
        INNER JOIN workstations ws ON rh.workstation_id = ws.id
        WHERE rh.rotation_date >= :startDate AND rh.rotation_date <= :endDate
        GROUP BY rh.worker_id, rh.workstation_id
        ORDER BY total_rotations DESC
    """)
    suspend fun getWorkerWorkstationStats(startDate: Long, endDate: Long): List<WorkerWorkstationStats>
}

// ═══════════════════════════════════════════════════════════════════════════════════════════════
// 📊 DATA CLASSES PARA CONSULTAS COMPLEJAS
// ═══════════════════════════════════════════════════════════════════════════════════════════════

data class RotationHistoryWithNames(
    val id: Long,
    val worker_id: Long,
    val workstation_id: Long,
    val rotation_date: Long,
    val end_date: Long?,
    val rotation_type: String,
    val duration_minutes: Int?,
    val performance_score: Double?,
    val notes: String?,
    val completed: Boolean,
    val created_at: Long,
    val worker_name: String,
    val workstation_name: String
)

data class RotationTypeStats(
    val rotation_type: String,
    val count: Int
)

data class WorkerWorkstationStats(
    val worker_name: String,
    val workstation_name: String,
    val total_rotations: Int,
    val avg_duration: Double?,
    val avg_performance: Double?
)