package com.workstation.rotation.data.dao

import androidx.room.*
import androidx.lifecycle.LiveData
import kotlinx.coroutines.flow.Flow
import com.workstation.rotation.data.entities.RotationAssignment

/**
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 🗄️ DAO ASIGNACIONES DE ROTACIÓN - NUEVA ARQUITECTURA v4.0
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 
 * 🎯 FUNCIONALIDADES:
 * • CRUD completo para asignaciones de rotación
 * • Consultas por sesión, trabajador y estación
 * • Filtros por tipo de rotación (CURRENT/NEXT)
 * • Operaciones de transición entre rotaciones
 * • Métricas y estadísticas de asignaciones
 * 
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 */

@Dao
interface RotationAssignmentDao {
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // 🔧 OPERACIONES BÁSICAS CRUD
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    @Insert
    suspend fun insert(assignment: RotationAssignment): Long
    
    @Insert
    suspend fun insertAll(assignments: List<RotationAssignment>): List<Long>
    
    @Update
    suspend fun update(assignment: RotationAssignment)
    
    @Delete
    suspend fun delete(assignment: RotationAssignment)
    
    @Query("DELETE FROM rotation_assignments WHERE id = :id")
    suspend fun deleteById(id: Long)
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // 📋 CONSULTAS BÁSICAS
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    @Query("SELECT * FROM rotation_assignments WHERE id = :id")
    suspend fun getById(id: Long): RotationAssignment?
    
    @Query("SELECT * FROM rotation_assignments ORDER BY assigned_at DESC")
    fun getAllFlow(): Flow<List<RotationAssignment>>
    
    @Query("SELECT * FROM rotation_assignments WHERE is_active = 1 ORDER BY assigned_at DESC")
    fun getActiveAssignmentsFlow(): Flow<List<RotationAssignment>>
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // 🔍 CONSULTAS POR SESIÓN
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    @Query("""
        SELECT * FROM rotation_assignments 
        WHERE rotation_session_id = :sessionId 
        ORDER BY rotation_type, workstation_id, assigned_at
    """)
    fun getBySessionFlow(sessionId: Long): Flow<List<RotationAssignment>>
    
    @Query("""
        SELECT * FROM rotation_assignments 
        WHERE rotation_session_id = :sessionId 
        ORDER BY rotation_type, workstation_id, assigned_at
    """)
    suspend fun getBySession(sessionId: Long): List<RotationAssignment>
    
    @Query("""
        SELECT * FROM rotation_assignments 
        WHERE rotation_session_id = :sessionId AND rotation_type = :rotationType
        ORDER BY workstation_id, assigned_at
    """)
    fun getBySessionAndTypeFlow(sessionId: Long, rotationType: String): Flow<List<RotationAssignment>>
    
    @Query("""
        SELECT * FROM rotation_assignments 
        WHERE rotation_session_id = :sessionId AND rotation_type = :rotationType
        ORDER BY workstation_id, assigned_at
    """)
    suspend fun getBySessionAndType(sessionId: Long, rotationType: String): List<RotationAssignment>
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // 👤 CONSULTAS POR TRABAJADOR
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    @Query("""
        SELECT * FROM rotation_assignments 
        WHERE worker_id = :workerId AND is_active = 1
        ORDER BY assigned_at DESC
    """)
    fun getActiveByWorkerFlow(workerId: Long): Flow<List<RotationAssignment>>
    
    @Query("""
        SELECT * FROM rotation_assignments 
        WHERE worker_id = :workerId AND rotation_session_id = :sessionId
        ORDER BY rotation_type, assigned_at
    """)
    suspend fun getByWorkerAndSession(workerId: Long, sessionId: Long): List<RotationAssignment>
    
    @Query("""
        SELECT * FROM rotation_assignments 
        WHERE worker_id = :workerId AND rotation_type = :rotationType AND is_active = 1
        ORDER BY assigned_at DESC
        LIMIT 1
    """)
    suspend fun getCurrentAssignmentByWorker(workerId: Long, rotationType: String): RotationAssignment?
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // 🏭 CONSULTAS POR ESTACIÓN
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    @Query("""
        SELECT * FROM rotation_assignments 
        WHERE workstation_id = :workstationId AND is_active = 1
        ORDER BY assigned_at DESC
    """)
    fun getActiveByWorkstationFlow(workstationId: Long): Flow<List<RotationAssignment>>
    
    @Query("""
        SELECT * FROM rotation_assignments 
        WHERE workstation_id = :workstationId AND rotation_session_id = :sessionId AND rotation_type = :rotationType
        ORDER BY assigned_at
    """)
    suspend fun getByWorkstationSessionAndType(
        workstationId: Long, 
        sessionId: Long, 
        rotationType: String
    ): List<RotationAssignment>
    
    @Query("""
        SELECT COUNT(*) FROM rotation_assignments 
        WHERE workstation_id = :workstationId AND rotation_session_id = :sessionId 
        AND rotation_type = :rotationType AND is_active = 1
    """)
    suspend fun getWorkstationAssignmentCount(
        workstationId: Long, 
        sessionId: Long, 
        rotationType: String
    ): Int
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // 📊 CONSULTAS CON JOINS PARA UI
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    @Query("""
        SELECT 
            ra.*,
            w.name as worker_name,
            w.employeeId as worker_employee_id,
            ws.name as workstation_name,
            ws.requiredWorkers as workstation_capacity
        FROM rotation_assignments ra
        INNER JOIN workers w ON ra.worker_id = w.id
        INNER JOIN workstations ws ON ra.workstation_id = ws.id
        WHERE ra.rotation_session_id = :sessionId AND ra.rotation_type = :rotationType
        ORDER BY ws.name, w.name
    """)
    fun getAssignmentsWithDetailsFlow(sessionId: Long, rotationType: String): Flow<List<RotationAssignmentWithDetails>>
    
    @Query("""
        SELECT 
            ws.id as workstation_id,
            ws.name as workstation_name,
            ws.requiredWorkers as required_workers,
            COUNT(ra.id) as assigned_workers
        FROM workstations ws
        LEFT JOIN rotation_assignments ra ON ws.id = ra.workstation_id 
            AND ra.rotation_session_id = :sessionId 
            AND ra.rotation_type = :rotationType 
            AND ra.is_active = 1
        WHERE ws.isActive = 1
        GROUP BY ws.id, ws.name, ws.requiredWorkers
        ORDER BY ws.name
    """)
    fun getWorkstationCapacityStatusFlow(sessionId: Long, rotationType: String): Flow<List<WorkstationCapacityStatus>>
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // 🔄 OPERACIONES DE TRANSICIÓN
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    @Query("""
        UPDATE rotation_assignments 
        SET rotation_type = 'CURRENT', started_at = :startTime
        WHERE rotation_session_id = :sessionId AND rotation_type = 'NEXT' AND is_active = 1
    """)
    suspend fun promoteNextToCurrent(sessionId: Long, startTime: Long = System.currentTimeMillis())
    
    @Query("""
        UPDATE rotation_assignments 
        SET is_active = 0, completed_at = :completionTime
        WHERE rotation_session_id = :sessionId AND rotation_type = 'CURRENT' AND is_active = 1
    """)
    suspend fun completeCurrentRotation(sessionId: Long, completionTime: Long = System.currentTimeMillis())
    
    @Query("""
        DELETE FROM rotation_assignments 
        WHERE rotation_session_id = :sessionId AND rotation_type = :rotationType
    """)
    suspend fun clearRotationType(sessionId: Long, rotationType: String)
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // 📈 MÉTRICAS Y VALIDACIONES
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    @Query("""
        SELECT COUNT(*) FROM rotation_assignments 
        WHERE rotation_session_id = :sessionId AND rotation_type = :rotationType AND is_active = 1
    """)
    suspend fun getAssignmentCount(sessionId: Long, rotationType: String): Int
    
    @Query("""
        SELECT COUNT(DISTINCT worker_id) FROM rotation_assignments 
        WHERE rotation_session_id = :sessionId AND rotation_type = :rotationType AND is_active = 1
    """)
    suspend fun getUniqueWorkerCount(sessionId: Long, rotationType: String): Int
    
    @Query("""
        SELECT COUNT(DISTINCT workstation_id) FROM rotation_assignments 
        WHERE rotation_session_id = :sessionId AND rotation_type = :rotationType AND is_active = 1
    """)
    suspend fun getUniqueWorkstationCount(sessionId: Long, rotationType: String): Int
    
    @Query("""
        SELECT worker_id FROM rotation_assignments 
        WHERE rotation_session_id = :sessionId AND rotation_type = :rotationType AND is_active = 1
        GROUP BY worker_id
        HAVING COUNT(*) > 1
    """)
    suspend fun findDuplicateWorkerAssignments(sessionId: Long, rotationType: String): List<Long>
}

// ═══════════════════════════════════════════════════════════════════════════════════════════════
// 📊 DATA CLASSES PARA CONSULTAS COMPLEJAS
// ═══════════════════════════════════════════════════════════════════════════════════════════════

data class RotationAssignmentWithDetails(
    val id: Long,
    val worker_id: Long,
    val workstation_id: Long,
    val rotation_session_id: Long,
    val rotation_type: String,
    val is_active: Boolean,
    val assigned_at: Long,
    val started_at: Long?,
    val completed_at: Long?,
    val notes: String?,
    val priority: Int,
    val worker_name: String,
    val worker_employee_id: String?,
    val workstation_name: String,
    val workstation_capacity: Int
)

data class WorkstationCapacityStatus(
    val workstation_id: Long,
    val workstation_name: String,
    val required_workers: Int,
    val assigned_workers: Int
) {
    fun isFullyStaffed(): Boolean = assigned_workers >= required_workers
    fun isOverStaffed(): Boolean = assigned_workers > required_workers
    fun getAvailableSlots(): Int = (required_workers - assigned_workers).coerceAtLeast(0)
    fun getUtilizationPercentage(): Float = if (required_workers > 0) {
        (assigned_workers.toFloat() / required_workers.toFloat() * 100f).coerceAtMost(100f)
    } else 0f
}