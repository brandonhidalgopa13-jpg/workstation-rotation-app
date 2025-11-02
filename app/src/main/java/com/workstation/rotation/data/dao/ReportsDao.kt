package com.workstation.rotation.data.dao

import androidx.room.*
import com.workstation.rotation.data.entities.Worker
import com.workstation.rotation.data.entities.Workstation

/**
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 📊 DAO ESPECIALIZADO PARA REPORTES Y MÉTRICAS CON SQL OPTIMIZADO
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 
 * Este DAO utiliza consultas SQL nativas para generar reportes detallados de:
 * - Métricas individuales por trabajador
 * - Porcentaje de permanencia por estación
 * - Estadísticas de rotación y rendimiento
 * - Análisis de distribución y utilización
 * 
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 */

@Dao
interface ReportsDao {
    
    /**
     * Obtiene métricas detalladas de un trabajador específico.
     */
    @Query("""
        SELECT 
            w.id,
            w.name,
            w.email,
            w.availabilityPercentage,
            w.isLeader,
            w.isTrainer,
            w.isTrainee,
            w.leadershipType,
            w.restrictionNotes,
            w.rotationsInCurrentStation,
            w.lastRotationTimestamp,
            COUNT(DISTINCT ww.workstationId) as totalStationsAssigned,
            GROUP_CONCAT(DISTINCT ws.name) as stationNames
        FROM workers w
        LEFT JOIN worker_workstations ww ON w.id = ww.workerId
        LEFT JOIN workstations ws ON ww.workstationId = ws.id AND ws.isActive = 1
        WHERE w.id = :workerId AND w.isActive = 1
        GROUP BY w.id
    """)
    suspend fun getWorkerMetrics(workerId: Long): WorkerMetricsResult?
    
    /**
     * Calcula el porcentaje de permanencia por estación para un trabajador.
     * Basado en las rotaciones históricas y asignaciones actuales.
     */
    @Query("""
        SELECT 
            ws.id as stationId,
            ws.name as stationName,
            ws.requiredWorkers,
            ws.isPriority,
            CASE 
                WHEN EXISTS (
                    SELECT 1 FROM worker_workstations ww 
                    WHERE ww.workerId = :workerId AND ww.workstationId = ws.id
                ) THEN 1 
                ELSE 0 
            END as canWork,
            CASE 
                WHEN w.currentWorkstationId = ws.id THEN w.rotationsInCurrentStation 
                ELSE 0 
            END as rotationsInStation,
            CASE 
                WHEN w.leaderWorkstationId = ws.id AND w.isLeader = 1 THEN 1 
                ELSE 0 
            END as isLeadershipStation,
            CASE 
                WHEN w.trainingWorkstationId = ws.id AND w.isTrainee = 1 THEN 1 
                ELSE 0 
            END as isTrainingStation
        FROM workstations ws
        CROSS JOIN workers w
        WHERE w.id = :workerId AND w.isActive = 1 AND ws.isActive = 1
        ORDER BY ws.isPriority DESC, ws.name
    """)
    suspend fun getWorkerStationMetrics(workerId: Long): List<WorkerStationMetric>
    
    /**
     * Obtiene estadísticas generales de todos los trabajadores activos.
     */
    @Query("""
        SELECT 
            w.id,
            w.name,
            w.email,
            w.availabilityPercentage,
            w.isLeader,
            w.isTrainer,
            w.isTrainee,
            w.rotationsInCurrentStation,
            COUNT(DISTINCT ww.workstationId) as totalStationsAssigned,
            CASE 
                WHEN w.isLeader = 1 THEN 'LÍDER'
                WHEN w.isTrainer = 1 THEN 'ENTRENADOR'
                WHEN w.isTrainee = 1 THEN 'ENTRENADO'
                ELSE 'REGULAR'
            END as workerType,
            CASE 
                WHEN w.restrictionNotes != '' THEN 1 
                ELSE 0 
            END as hasRestrictions
        FROM workers w
        LEFT JOIN worker_workstations ww ON w.id = ww.workerId
        WHERE w.isActive = 1
        GROUP BY w.id
        ORDER BY w.name
    """)
    suspend fun getAllWorkersMetrics(): List<WorkerSummaryMetric>
    
    /**
     * Calcula estadísticas de utilización por estación.
     */
    @Query("""
        SELECT 
            ws.id as stationId,
            ws.name as stationName,
            ws.requiredWorkers,
            ws.isPriority,
            COUNT(DISTINCT ww.workerId) as assignedWorkers,
            COUNT(DISTINCT CASE WHEN w.isLeader = 1 AND w.leaderWorkstationId = ws.id THEN w.id END) as leaders,
            COUNT(DISTINCT CASE WHEN w.isTrainer = 1 THEN w.id END) as trainers,
            COUNT(DISTINCT CASE WHEN w.isTrainee = 1 AND w.trainingWorkstationId = ws.id THEN w.id END) as trainees,
            ROUND(
                (COUNT(DISTINCT ww.workerId) * 100.0) / NULLIF(ws.requiredWorkers, 0), 2
            ) as utilizationPercentage
        FROM workstations ws
        LEFT JOIN worker_workstations ww ON ws.id = ww.workstationId
        LEFT JOIN workers w ON ww.workerId = w.id AND w.isActive = 1
        WHERE ws.isActive = 1
        GROUP BY ws.id
        ORDER BY ws.isPriority DESC, ws.name
    """)
    suspend fun getStationUtilizationMetrics(): List<StationUtilizationMetric>
    
    /**
     * Obtiene métricas de distribución de roles.
     */
    @Query("""
        SELECT 
            'TOTAL' as category,
            COUNT(*) as count,
            ROUND(COUNT(*) * 100.0 / (SELECT COUNT(*) FROM workers WHERE isActive = 1), 2) as percentage
        FROM workers 
        WHERE isActive = 1
        
        UNION ALL
        
        SELECT 
            'LÍDERES' as category,
            COUNT(*) as count,
            ROUND(COUNT(*) * 100.0 / (SELECT COUNT(*) FROM workers WHERE isActive = 1), 2) as percentage
        FROM workers 
        WHERE isActive = 1 AND isLeader = 1
        
        UNION ALL
        
        SELECT 
            'ENTRENADORES' as category,
            COUNT(*) as count,
            ROUND(COUNT(*) * 100.0 / (SELECT COUNT(*) FROM workers WHERE isActive = 1), 2) as percentage
        FROM workers 
        WHERE isActive = 1 AND isTrainer = 1
        
        UNION ALL
        
        SELECT 
            'ENTRENADOS' as category,
            COUNT(*) as count,
            ROUND(COUNT(*) * 100.0 / (SELECT COUNT(*) FROM workers WHERE isActive = 1), 2) as percentage
        FROM workers 
        WHERE isActive = 1 AND isTrainee = 1
        
        UNION ALL
        
        SELECT 
            'CON RESTRICCIONES' as category,
            COUNT(*) as count,
            ROUND(COUNT(*) * 100.0 / (SELECT COUNT(*) FROM workers WHERE isActive = 1), 2) as percentage
        FROM workers 
        WHERE isActive = 1 AND restrictionNotes != ''
        
        UNION ALL
        
        SELECT 
            'DISPONIBILIDAD < 100%' as category,
            COUNT(*) as count,
            ROUND(COUNT(*) * 100.0 / (SELECT COUNT(*) FROM workers WHERE isActive = 1), 2) as percentage
        FROM workers 
        WHERE isActive = 1 AND availabilityPercentage < 100
    """)
    suspend fun getRoleDistributionMetrics(): List<RoleDistributionMetric>
    
    /**
     * Calcula métricas de eficiencia del sistema.
     */
    @Query("""
        SELECT 
            (SELECT COUNT(*) FROM workers WHERE isActive = 1) as totalActiveWorkers,
            (SELECT COUNT(*) FROM workstations WHERE isActive = 1) as totalActiveStations,
            (SELECT SUM(requiredWorkers) FROM workstations WHERE isActive = 1) as totalRequiredCapacity,
            (SELECT COUNT(DISTINCT ww.workerId) 
             FROM worker_workstations ww 
             INNER JOIN workers w ON ww.workerId = w.id 
             WHERE w.isActive = 1) as workersWithStations,
            (SELECT ROUND(AVG(availabilityPercentage), 2) 
             FROM workers WHERE isActive = 1) as averageAvailability,
            (SELECT COUNT(*) FROM workstations WHERE isActive = 1 AND isPriority = 1) as priorityStations
    """)
    suspend fun getSystemEfficiencyMetrics(): SystemEfficiencyMetric
    
    /**
     * Obtiene el historial de rotaciones simulado basado en datos actuales.
     * (Para futuras implementaciones con tabla de historial)
     */
    @Query("""
        SELECT 
            w.id as workerId,
            w.name as workerName,
            w.currentWorkstationId as stationId,
            ws.name as stationName,
            w.rotationsInCurrentStation as rotationCount,
            w.lastRotationTimestamp,
            CASE 
                WHEN w.isLeader = 1 AND w.leaderWorkstationId = w.currentWorkstationId THEN 'LIDERAZGO'
                WHEN w.isTrainee = 1 AND w.trainingWorkstationId = w.currentWorkstationId THEN 'ENTRENAMIENTO'
                ELSE 'ROTACIÓN NORMAL'
            END as rotationType
        FROM workers w
        LEFT JOIN workstations ws ON w.currentWorkstationId = ws.id
        WHERE w.isActive = 1 AND w.currentWorkstationId IS NOT NULL
        ORDER BY w.lastRotationTimestamp DESC, w.name
    """)
    suspend fun getRotationHistory(): List<RotationHistoryMetric>
}

/**
 * Data classes para los resultados de las consultas SQL.
 */
data class WorkerMetricsResult(
    val id: Long,
    val name: String,
    val email: String,
    val availabilityPercentage: Int,
    val isLeader: Boolean,
    val isTrainer: Boolean,
    val isTrainee: Boolean,
    val leadershipType: String?,
    val restrictionNotes: String,
    val rotationsInCurrentStation: Int,
    val lastRotationTimestamp: Long?,
    val totalStationsAssigned: Int,
    val stationNames: String?
)

data class WorkerStationMetric(
    val stationId: Long,
    val stationName: String,
    val requiredWorkers: Int,
    val isPriority: Boolean,
    val canWork: Int, // 0 o 1 (boolean en SQL)
    val rotationsInStation: Int,
    val isLeadershipStation: Int, // 0 o 1
    val isTrainingStation: Int // 0 o 1
)

data class WorkerSummaryMetric(
    val id: Long,
    val name: String,
    val email: String,
    val availabilityPercentage: Int,
    val isLeader: Boolean,
    val isTrainer: Boolean,
    val isTrainee: Boolean,
    val rotationsInCurrentStation: Int,
    val totalStationsAssigned: Int,
    val workerType: String,
    val hasRestrictions: Int
)

data class StationUtilizationMetric(
    val stationId: Long,
    val stationName: String,
    val requiredWorkers: Int,
    val isPriority: Boolean,
    val assignedWorkers: Int,
    val leaders: Int,
    val trainers: Int,
    val trainees: Int,
    val utilizationPercentage: Double
)

data class RoleDistributionMetric(
    val category: String,
    val count: Int,
    val percentage: Double
)

data class SystemEfficiencyMetric(
    val totalActiveWorkers: Int,
    val totalActiveStations: Int,
    val totalRequiredCapacity: Int,
    val workersWithStations: Int,
    val averageAvailability: Double,
    val priorityStations: Int
)

data class RotationHistoryMetric(
    val workerId: Long,
    val workerName: String,
    val stationId: Long?,
    val stationName: String?,
    val rotationCount: Int,
    val lastRotationTimestamp: Long?,
    val rotationType: String
)