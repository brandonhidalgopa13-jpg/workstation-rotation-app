package com.workstation.rotation.data.dao

import androidx.room.*
import androidx.lifecycle.LiveData
import kotlinx.coroutines.flow.Flow
import com.workstation.rotation.data.entities.WorkerWorkstationCapability

/**
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 🗄️ DAO CAPACIDADES TRABAJADOR-ESTACIÓN - NUEVA ARQUITECTURA v4.0
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 
 * 🎯 FUNCIONALIDADES:
 * • CRUD completo para capacidades trabajador-estación
 * • Consultas por trabajador y estación
 * • Filtros por nivel de competencia y certificación
 * • Operaciones de evaluación y actualización
 * • Métricas de capacidades y competencias
 * 
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 */

@Dao
interface WorkerWorkstationCapabilityDao {
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // 🔧 OPERACIONES BÁSICAS CRUD
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    @Insert
    suspend fun insert(capability: WorkerWorkstationCapability): Long
    
    @Insert
    suspend fun insertAll(capabilities: List<WorkerWorkstationCapability>): List<Long>
    
    @Update
    suspend fun update(capability: WorkerWorkstationCapability)
    
    @Delete
    suspend fun delete(capability: WorkerWorkstationCapability)
    
    @Query("DELETE FROM worker_workstation_capabilities WHERE id = :id")
    suspend fun deleteById(id: Long)
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // 📋 CONSULTAS BÁSICAS
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    @Query("SELECT * FROM worker_workstation_capabilities WHERE id = :id")
    suspend fun getById(id: Long): WorkerWorkstationCapability?
    
    @Query("SELECT * FROM worker_workstation_capabilities ORDER BY updated_at DESC")
    fun getAllFlow(): Flow<List<WorkerWorkstationCapability>>
    
    @Query("SELECT * FROM worker_workstation_capabilities WHERE is_active = 1 ORDER BY updated_at DESC")
    fun getActiveCapabilitiesFlow(): Flow<List<WorkerWorkstationCapability>>
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // 👤 CONSULTAS POR TRABAJADOR
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    @Query("""
        SELECT * FROM worker_workstation_capabilities 
        WHERE worker_id = :workerId AND is_active = 1
        ORDER BY competency_level DESC, workstation_id
    """)
    fun getByWorkerFlow(workerId: Long): Flow<List<WorkerWorkstationCapability>>
    
    @Query("""
        SELECT * FROM worker_workstation_capabilities 
        WHERE worker_id = :workerId AND is_active = 1
        ORDER BY competency_level DESC, workstation_id
    """)
    suspend fun getByWorker(workerId: Long): List<WorkerWorkstationCapability>
    
    @Query("""
        SELECT wwc.*, ws.name as workstation_name, ws.requiredWorkers as required_workers
        FROM worker_workstation_capabilities wwc
        INNER JOIN workstations ws ON wwc.workstation_id = ws.id
        WHERE wwc.worker_id = :workerId AND wwc.is_active = 1 AND ws.isActive = 1
        ORDER BY wwc.competency_level DESC, ws.name
    """)
    fun getWorkerCapabilitiesWithDetailsFlow(workerId: Long): Flow<List<WorkerCapabilityWithDetails>>
    
    @Query("""
        SELECT COUNT(*) FROM worker_workstation_capabilities 
        WHERE worker_id = :workerId AND is_active = 1
    """)
    suspend fun getWorkerCapabilityCount(workerId: Long): Int
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // 🏭 CONSULTAS POR ESTACIÓN
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    @Query("""
        SELECT * FROM worker_workstation_capabilities 
        WHERE workstation_id = :workstationId AND is_active = 1
        ORDER BY competency_level DESC, worker_id
    """)
    fun getByWorkstationFlow(workstationId: Long): Flow<List<WorkerWorkstationCapability>>
    
    @Query("""
        SELECT * FROM worker_workstation_capabilities 
        WHERE workstation_id = :workstationId AND is_active = 1
        ORDER BY competency_level DESC, worker_id
    """)
    suspend fun getByWorkstation(workstationId: Long): List<WorkerWorkstationCapability>
    
    @Query("""
        SELECT wwc.*, w.name as worker_name, w.employeeId as employee_id
        FROM worker_workstation_capabilities wwc
        INNER JOIN workers w ON wwc.worker_id = w.id
        WHERE wwc.workstation_id = :workstationId AND wwc.is_active = 1 AND w.isActive = 1
        ORDER BY wwc.competency_level DESC, w.name
    """)
    fun getWorkstationCapabilitiesWithDetailsFlow(workstationId: Long): Flow<List<WorkstationCapabilityWithDetails>>
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // 🔍 CONSULTAS ESPECÍFICAS
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    @Query("""
        SELECT * FROM worker_workstation_capabilities 
        WHERE worker_id = :workerId AND workstation_id = :workstationId
    """)
    suspend fun getByWorkerAndWorkstation(workerId: Long, workstationId: Long): WorkerWorkstationCapability?
    
    @Query("""
        SELECT * FROM worker_workstation_capabilities 
        WHERE competency_level >= :minLevel AND is_active = 1
        ORDER BY competency_level DESC, updated_at DESC
    """)
    suspend fun getByMinimumCompetencyLevel(minLevel: Int): List<WorkerWorkstationCapability>
    
    @Query("""
        SELECT * FROM worker_workstation_capabilities 
        WHERE is_certified = 1 AND is_active = 1
        ORDER BY certified_at DESC
    """)
    suspend fun getCertifiedCapabilities(): List<WorkerWorkstationCapability>
    
    @Query("""
        SELECT * FROM worker_workstation_capabilities 
        WHERE can_be_leader = 1 AND is_active = 1
        ORDER BY competency_level DESC, experience_hours DESC
    """)
    suspend fun getLeaderCapabilities(): List<WorkerWorkstationCapability>
    
    @Query("""
        SELECT * FROM worker_workstation_capabilities 
        WHERE can_train = 1 AND is_active = 1
        ORDER BY competency_level DESC, experience_hours DESC
    """)
    suspend fun getTrainerCapabilities(): List<WorkerWorkstationCapability>
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // 🎯 CONSULTAS PARA ASIGNACIÓN INTELIGENTE
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    @Query("""
        SELECT wwc.*, w.name as worker_name, w.isActive as worker_is_active
        FROM worker_workstation_capabilities wwc
        INNER JOIN workers w ON wwc.worker_id = w.id
        WHERE wwc.workstation_id = :workstationId 
        AND wwc.is_active = 1 
        AND w.isActive = 1
        AND wwc.competency_level >= :minCompetencyLevel
        ORDER BY wwc.competency_level DESC, wwc.experience_hours DESC, wwc.last_evaluation_score DESC
    """)
    suspend fun getBestWorkersForWorkstation(
        workstationId: Long, 
        minCompetencyLevel: Int = 2
    ): List<WorkerCapabilityForAssignment>
    
    @Query("""
        SELECT wwc.*, ws.name as workstation_name, ws.isActive as workstation_is_active
        FROM worker_workstation_capabilities wwc
        INNER JOIN workstations ws ON wwc.workstation_id = ws.id
        WHERE wwc.worker_id = :workerId 
        AND wwc.is_active = 1 
        AND ws.isActive = 1
        AND wwc.competency_level >= :minCompetencyLevel
        ORDER BY wwc.competency_level DESC, wwc.experience_hours DESC, wwc.last_evaluation_score DESC
    """)
    suspend fun getBestWorkstationsForWorker(
        workerId: Long, 
        minCompetencyLevel: Int = 2
    ): List<WorkstationCapabilityForAssignment>
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // 📊 MÉTRICAS Y ESTADÍSTICAS
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    @Query("""
        SELECT 
            competency_level,
            COUNT(*) as count
        FROM worker_workstation_capabilities 
        WHERE is_active = 1
        GROUP BY competency_level
        ORDER BY competency_level
    """)
    suspend fun getCompetencyLevelStats(): List<CompetencyLevelStats>
    
    @Query("""
        SELECT AVG(competency_level) FROM worker_workstation_capabilities 
        WHERE worker_id = :workerId AND is_active = 1
    """)
    suspend fun getWorkerAverageCompetency(workerId: Long): Double?
    
    @Query("""
        SELECT AVG(competency_level) FROM worker_workstation_capabilities 
        WHERE workstation_id = :workstationId AND is_active = 1
    """)
    suspend fun getWorkstationAverageCompetency(workstationId: Long): Double?
    
    @Query("""
        SELECT COUNT(*) FROM worker_workstation_capabilities 
        WHERE is_certified = 1 AND certification_expires_at > :currentTime AND is_active = 1
    """)
    suspend fun getValidCertificationCount(currentTime: Long = System.currentTimeMillis()): Int
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // 🔄 OPERACIONES DE ACTUALIZACIÓN
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    @Query("""
        UPDATE worker_workstation_capabilities 
        SET competency_level = :level, last_evaluated_at = :evaluationTime, 
            last_evaluation_score = :score, updated_at = :updateTime
        WHERE id = :id
    """)
    suspend fun updateCompetencyLevel(
        id: Long, 
        level: Int, 
        score: Double? = null,
        evaluationTime: Long = System.currentTimeMillis(),
        updateTime: Long = System.currentTimeMillis()
    )
    
    @Query("""
        UPDATE worker_workstation_capabilities 
        SET is_certified = :certified, certified_at = :certificationTime, 
            certification_expires_at = :expirationTime, updated_at = :updateTime
        WHERE id = :id
    """)
    suspend fun updateCertification(
        id: Long, 
        certified: Boolean, 
        certificationTime: Long? = null,
        expirationTime: Long? = null,
        updateTime: Long = System.currentTimeMillis()
    )
    
    @Query("""
        UPDATE worker_workstation_capabilities 
        SET experience_hours = experience_hours + :additionalHours, updated_at = :updateTime
        WHERE id = :id
    """)
    suspend fun addExperienceHours(
        id: Long, 
        additionalHours: Int,
        updateTime: Long = System.currentTimeMillis()
    )
}

// ═══════════════════════════════════════════════════════════════════════════════════════════════
// 📊 DATA CLASSES PARA CONSULTAS COMPLEJAS
// ═══════════════════════════════════════════════════════════════════════════════════════════════

data class WorkerCapabilityWithDetails(
    val id: Long,
    val worker_id: Long,
    val workstation_id: Long,
    val competency_level: Int,
    val is_active: Boolean,
    val is_certified: Boolean,
    val can_be_leader: Boolean,
    val can_train: Boolean,
    val certified_at: Long?,
    val certification_expires_at: Long?,
    val last_evaluated_at: Long?,
    val last_evaluation_score: Double?,
    val experience_hours: Int,
    val notes: String?,
    val workstation_name: String,
    val required_workers: Int
)

data class WorkstationCapabilityWithDetails(
    val id: Long,
    val worker_id: Long,
    val workstation_id: Long,
    val competency_level: Int,
    val is_active: Boolean,
    val is_certified: Boolean,
    val can_be_leader: Boolean,
    val can_train: Boolean,
    val certified_at: Long?,
    val certification_expires_at: Long?,
    val last_evaluated_at: Long?,
    val last_evaluation_score: Double?,
    val experience_hours: Int,
    val notes: String?,
    val worker_name: String,
    val employee_id: String?
)

data class WorkerCapabilityForAssignment(
    val id: Long,
    val worker_id: Long,
    val workstation_id: Long,
    val competency_level: Int,
    val is_active: Boolean,
    val is_certified: Boolean,
    val can_be_leader: Boolean,
    val can_train: Boolean,
    val experience_hours: Int,
    val last_evaluation_score: Double?,
    val worker_name: String,
    val worker_is_active: Boolean
)

data class WorkstationCapabilityForAssignment(
    val id: Long,
    val worker_id: Long,
    val workstation_id: Long,
    val competency_level: Int,
    val is_active: Boolean,
    val is_certified: Boolean,
    val can_be_leader: Boolean,
    val can_train: Boolean,
    val experience_hours: Int,
    val last_evaluation_score: Double?,
    val workstation_name: String,
    val workstation_is_active: Boolean
)

data class CompetencyLevelStats(
    val competency_level: Int,
    val count: Int
)