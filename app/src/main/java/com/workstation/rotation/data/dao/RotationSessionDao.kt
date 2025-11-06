package com.workstation.rotation.data.dao

import androidx.room.*
import androidx.lifecycle.LiveData
import kotlinx.coroutines.flow.Flow
import com.workstation.rotation.data.entities.RotationSession

/**
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 🗄️ DAO SESIONES DE ROTACIÓN - NUEVA ARQUITECTURA v4.0
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 
 * 🎯 FUNCIONALIDADES:
 * • CRUD completo para sesiones de rotación
 * • Consultas por estado y fecha
 * • Operaciones de activación y finalización
 * • Métricas y estadísticas de sesiones
 * 
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 */

@Dao
interface RotationSessionDao {
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // 🔧 OPERACIONES BÁSICAS CRUD
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    @Insert
    suspend fun insert(session: RotationSession): Long
    
    @Update
    suspend fun update(session: RotationSession)
    
    @Delete
    suspend fun delete(session: RotationSession)
    
    @Query("DELETE FROM rotation_sessions WHERE id = :id")
    suspend fun deleteById(id: Long)
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // 📋 CONSULTAS BÁSICAS
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    @Query("SELECT * FROM rotation_sessions WHERE id = :id")
    suspend fun getById(id: Long): RotationSession?
    
    @Query("SELECT * FROM rotation_sessions ORDER BY created_at DESC")
    fun getAllFlow(): Flow<List<RotationSession>>
    
    @Query("SELECT * FROM rotation_sessions ORDER BY created_at DESC")
    suspend fun getAll(): List<RotationSession>
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // 🔍 CONSULTAS POR ESTADO
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    @Query("SELECT * FROM rotation_sessions WHERE status = :status ORDER BY created_at DESC")
    fun getByStatusFlow(status: String): Flow<List<RotationSession>>
    
    @Query("SELECT * FROM rotation_sessions WHERE status = :status ORDER BY created_at DESC")
    suspend fun getByStatus(status: String): List<RotationSession>
    
    @Query("SELECT * FROM rotation_sessions WHERE status = 'ACTIVE' ORDER BY activated_at DESC LIMIT 1")
    suspend fun getActiveSession(): RotationSession?
    
    @Query("SELECT * FROM rotation_sessions WHERE status = 'ACTIVE' ORDER BY activated_at DESC LIMIT 1")
    fun getActiveSessionFlow(): Flow<RotationSession?>
    
    @Query("SELECT * FROM rotation_sessions WHERE status IN ('DRAFT', 'ACTIVE') ORDER BY created_at DESC")
    fun getActiveDraftSessionsFlow(): Flow<List<RotationSession>>
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // 📅 CONSULTAS POR FECHA
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    @Query("""
        SELECT * FROM rotation_sessions 
        WHERE created_at >= :startDate AND created_at <= :endDate 
        ORDER BY created_at DESC
    """)
    suspend fun getByDateRange(startDate: Long, endDate: Long): List<RotationSession>
    
    @Query("""
        SELECT * FROM rotation_sessions 
        WHERE created_at >= :startDate AND created_at <= :endDate 
        ORDER BY created_at DESC
    """)
    fun getByDateRangeFlow(startDate: Long, endDate: Long): Flow<List<RotationSession>>
    
    @Query("""
        SELECT * FROM rotation_sessions 
        WHERE DATE(created_at/1000, 'unixepoch') = DATE('now') 
        ORDER BY created_at DESC
    """)
    fun getTodaySessionsFlow(): Flow<List<RotationSession>>
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // 🔄 OPERACIONES DE ESTADO
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    @Query("""
        UPDATE rotation_sessions 
        SET status = 'ACTIVE', activated_at = :activationTime 
        WHERE id = :id
    """)
    suspend fun activateSession(id: Long, activationTime: Long = System.currentTimeMillis())
    
    @Query("""
        UPDATE rotation_sessions 
        SET status = 'COMPLETED', completed_at = :completionTime, actual_duration_minutes = :duration
        WHERE id = :id
    """)
    suspend fun completeSession(id: Long, completionTime: Long = System.currentTimeMillis(), duration: Int? = null)
    
    @Query("""
        UPDATE rotation_sessions 
        SET status = 'CANCELLED', completed_at = :cancellationTime
        WHERE id = :id
    """)
    suspend fun cancelSession(id: Long, cancellationTime: Long = System.currentTimeMillis())
    
    @Query("UPDATE rotation_sessions SET status = 'COMPLETED' WHERE status = 'ACTIVE'")
    suspend fun completeAllActiveSessions()
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // 📊 MÉTRICAS Y ESTADÍSTICAS
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    @Query("SELECT COUNT(*) FROM rotation_sessions")
    suspend fun getTotalSessionCount(): Int
    
    @Query("SELECT COUNT(*) FROM rotation_sessions WHERE status = :status")
    suspend fun getSessionCountByStatus(status: String): Int
    
    @Query("""
        SELECT AVG(actual_duration_minutes) FROM rotation_sessions 
        WHERE actual_duration_minutes IS NOT NULL AND status = 'COMPLETED'
    """)
    suspend fun getAverageDuration(): Double?
    
    @Query("""
        SELECT 
            status,
            COUNT(*) as count
        FROM rotation_sessions 
        GROUP BY status
    """)
    suspend fun getSessionStatusStats(): List<SessionStatusStats>
    
    @Query("""
        SELECT * FROM rotation_sessions 
        WHERE status = 'COMPLETED' 
        ORDER BY actual_duration_minutes DESC 
        LIMIT :limit
    """)
    suspend fun getLongestSessions(limit: Int = 10): List<RotationSession>
    
    @Query("""
        SELECT * FROM rotation_sessions 
        WHERE status = 'COMPLETED' 
        ORDER BY total_workers DESC 
        LIMIT :limit
    """)
    suspend fun getLargestSessions(limit: Int = 10): List<RotationSession>
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // 🧹 OPERACIONES DE MANTENIMIENTO
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    @Query("""
        DELETE FROM rotation_sessions 
        WHERE status = 'DRAFT' AND created_at < :cutoffDate
    """)
    suspend fun deleteOldDraftSessions(cutoffDate: Long): Int
    
    @Query("""
        UPDATE rotation_sessions 
        SET total_workers = :workerCount, total_workstations = :workstationCount
        WHERE id = :id
    """)
    suspend fun updateSessionCounts(id: Long, workerCount: Int, workstationCount: Int)
    
    @Query("""
        SELECT * FROM rotation_sessions 
        WHERE name LIKE '%' || :searchTerm || '%' OR description LIKE '%' || :searchTerm || '%'
        ORDER BY created_at DESC
    """)
    suspend fun searchSessions(searchTerm: String): List<RotationSession>
}

// ═══════════════════════════════════════════════════════════════════════════════════════════════
// 📊 DATA CLASSES PARA ESTADÍSTICAS
// ═══════════════════════════════════════════════════════════════════════════════════════════════

data class SessionStatusStats(
    val status: String,
    val count: Int
)