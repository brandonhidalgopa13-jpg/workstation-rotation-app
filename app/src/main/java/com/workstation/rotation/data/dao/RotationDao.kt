package com.workstation.rotation.data.dao

import androidx.room.*
import com.workstation.rotation.data.entities.Worker
import com.workstation.rotation.data.entities.Workstation

/**
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 🔄 DAO ESPECIALIZADO PARA GENERACIÓN DE ROTACIONES CON SQL
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 
 * Este DAO utiliza consultas SQL optimizadas para generar rotaciones de manera eficiente,
 * considerando todas las reglas de negocio: liderazgo, entrenamiento, restricciones, etc.
 * 
 * VENTAJAS DEL ENFOQUE SQL:
 * - Mayor rendimiento que algoritmos en memoria
 * - Consultas optimizadas por el motor de base de datos
 * - Menos propenso a errores de lógica compleja
 * - Más fácil de debuggear y mantener
 * 
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 */

@Dao
interface RotationDao {
    
    /**
     * Obtiene todos los trabajadores elegibles para rotación con sus estaciones asignadas.
     * Incluye información de liderazgo, entrenamiento y restricciones.
     */
    @Query("""
        SELECT DISTINCT w.*
        FROM workers w
        INNER JOIN worker_workstations ww ON w.id = ww.workerId
        WHERE w.isActive = 1
        ORDER BY 
            CASE 
                WHEN w.isLeader = 1 AND w.leadershipType = 'BOTH' THEN 1
                WHEN w.isLeader = 1 THEN 2
                WHEN w.isTrainer = 1 THEN 3
                WHEN w.isTrainee = 1 THEN 4
                ELSE 5
            END,
            w.availabilityPercentage DESC,
            w.name
    """)
    suspend fun getEligibleWorkersForRotation(): List<Worker>
    
    /**
     * Obtiene líderes activos para una rotación específica (primera o segunda parte).
     * @param isFirstHalf true para primera parte, false para segunda parte
     */
    @Query("""
        SELECT w.*
        FROM workers w
        WHERE w.isActive = 1 
        AND w.isLeader = 1
        AND w.leaderWorkstationId IS NOT NULL
        AND (
            w.leadershipType = 'BOTH' OR
            (w.leadershipType = 'FIRST_HALF' AND :isFirstHalf = 1) OR
            (w.leadershipType = 'SECOND_HALF' AND :isFirstHalf = 0)
        )
        ORDER BY 
            CASE w.leadershipType 
                WHEN 'BOTH' THEN 1 
                ELSE 2 
            END,
            w.name
    """)
    suspend fun getActiveLeadersForRotation(isFirstHalf: Boolean): List<Worker>
    
    /**
     * Obtiene parejas de entrenamiento activas (entrenador + entrenado).
     */
    @Query("""
        SELECT trainee.*
        FROM workers trainee
        INNER JOIN workers trainer ON trainee.trainerId = trainer.id
        WHERE trainee.isActive = 1 
        AND trainee.isTrainee = 1 
        AND trainee.trainerId IS NOT NULL
        AND trainer.isActive = 1
        AND trainer.isTrainer = 1
        ORDER BY 
            CASE 
                WHEN EXISTS (
                    SELECT 1 FROM workstations ws 
                    WHERE ws.id = trainee.trainingWorkstationId 
                    AND ws.isPriority = 1
                ) THEN 1 
                ELSE 2 
            END,
            trainee.name
    """)
    suspend fun getTrainingPairs(): List<Worker>
    
    /**
     * Obtiene trabajadores que pueden trabajar en una estación específica,
     * excluyendo aquellos con restricciones prohibitivas.
     */
    @Query("""
        SELECT w.*
        FROM workers w
        INNER JOIN worker_workstations ww ON w.id = ww.workerId
        WHERE w.isActive = 1
        AND ww.workstationId = :workstationId
        AND NOT EXISTS (
            SELECT 1 FROM worker_restrictions wr 
            WHERE wr.workerId = w.id 
            AND wr.workstationId = :workstationId 
            AND wr.restrictionType = 'PROHIBITED'
        )
        ORDER BY 
            CASE 
                WHEN w.isLeader = 1 AND w.leaderWorkstationId = :workstationId THEN 1
                WHEN w.isTrainer = 1 THEN 2
                ELSE 3
            END,
            w.availabilityPercentage DESC,
            RANDOM()
    """)
    suspend fun getEligibleWorkersForStation(workstationId: Long): List<Worker>
    
    /**
     * Obtiene estaciones prioritarias que requieren capacidad completa.
     */
    @Query("""
        SELECT ws.*
        FROM workstations ws
        WHERE ws.isActive = 1 
        AND ws.isPriority = 1
        ORDER BY ws.requiredWorkers DESC, ws.name
    """)
    suspend fun getPriorityWorkstations(): List<Workstation>
    
    /**
     * Obtiene estaciones normales (no prioritarias).
     */
    @Query("""
        SELECT ws.*
        FROM workstations ws
        WHERE ws.isActive = 1 
        AND ws.isPriority = 0
        ORDER BY ws.requiredWorkers DESC, ws.name
    """)
    suspend fun getNormalWorkstations(): List<Workstation>
    
    /**
     * Verifica si un trabajador puede trabajar en una estación específica.
     */
    @Query("""
        SELECT COUNT(*) > 0
        FROM worker_workstations ww
        WHERE ww.workerId = :workerId 
        AND ww.workstationId = :workstationId
        AND NOT EXISTS (
            SELECT 1 FROM worker_restrictions wr 
            WHERE wr.workerId = :workerId 
            AND wr.workstationId = :workstationId 
            AND wr.restrictionType = 'PROHIBITED'
        )
    """)
    suspend fun canWorkerWorkAtStation(workerId: Long, workstationId: Long): Boolean
    
    /**
     * Obtiene estadísticas de asignación para diagnóstico.
     */
    @Query("""
        SELECT 
            COUNT(DISTINCT w.id) as totalWorkers,
            COUNT(DISTINCT ww.workstationId) as assignedStations,
            COUNT(DISTINCT CASE WHEN w.isLeader = 1 THEN w.id END) as totalLeaders,
            COUNT(DISTINCT CASE WHEN w.isTrainer = 1 THEN w.id END) as totalTrainers,
            COUNT(DISTINCT CASE WHEN w.isTrainee = 1 THEN w.id END) as totalTrainees
        FROM workers w
        LEFT JOIN worker_workstations ww ON w.id = ww.workerId
        WHERE w.isActive = 1
    """)
    suspend fun getRotationStatistics(): RotationStats
    
    /**
     * Obtiene trabajadores sin estaciones asignadas (para diagnóstico).
     */
    @Query("""
        SELECT w.*
        FROM workers w
        WHERE w.isActive = 1
        AND NOT EXISTS (
            SELECT 1 FROM worker_workstations ww 
            WHERE ww.workerId = w.id
        )
        ORDER BY w.name
    """)
    suspend fun getWorkersWithoutStations(): List<Worker>
    
    /**
     * Obtiene la estación de liderazgo de un trabajador si es líder activo.
     */
    @Query("""
        SELECT ws.*
        FROM workstations ws
        INNER JOIN workers w ON ws.id = w.leaderWorkstationId
        WHERE w.id = :workerId
        AND w.isActive = 1
        AND w.isLeader = 1
        AND ws.isActive = 1
        AND (
            w.leadershipType = 'BOTH' OR
            (w.leadershipType = 'FIRST_HALF' AND :isFirstHalf = 1) OR
            (w.leadershipType = 'SECOND_HALF' AND :isFirstHalf = 0)
        )
    """)
    suspend fun getLeadershipStationForWorker(workerId: Long, isFirstHalf: Boolean): Workstation?
    
    /**
     * Obtiene la estación de entrenamiento para un trabajador en entrenamiento.
     */
    @Query("""
        SELECT ws.*
        FROM workstations ws
        INNER JOIN workers w ON ws.id = w.trainingWorkstationId
        WHERE w.id = :workerId
        AND w.isActive = 1
        AND w.isTrainee = 1
        AND ws.isActive = 1
    """)
    suspend fun getTrainingStationForWorker(workerId: Long): Workstation?
}

/**
 * Data class para estadísticas de rotación.
 */
data class RotationStats(
    val totalWorkers: Int,
    val assignedStations: Int,
    val totalLeaders: Int,
    val totalTrainers: Int,
    val totalTrainees: Int
)