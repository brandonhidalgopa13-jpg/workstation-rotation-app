package com.workstation.rotation.services

import android.content.Context
import com.workstation.rotation.data.database.AppDatabase
import com.workstation.rotation.data.entities.*
import com.workstation.rotation.models.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first

/**
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 🔄 SERVICIO DE ROTACIÓN NUEVA ARQUITECTURA v4.0
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 
 * 🎯 PROPÓSITO:
 * Servicio principal para manejar la nueva lógica de rotación con arquitectura simplificada.
 * Gestiona sesiones de rotación, asignaciones y capacidades de trabajadores.
 * 
 * 📋 CARACTERÍSTICAS PRINCIPALES:
 * • Gestión de sesiones de rotación (CURRENT/NEXT)
 * • Asignación inteligente basada en capacidades
 * • Validación de restricciones y conflictos
 * • Generación automática de rotaciones optimizadas
 * • Transición fluida entre rotaciones
 * 
 * 🔧 REGLAS DE NEGOCIO:
 * • Un trabajador = Una estación por rotación
 * • Trabajadores pueden tener 1-15 estaciones asignadas
 * • Validación de competencias mínimas
 * • Priorización de líderes y entrenadores
 * • Balanceo de carga entre estaciones
 * 
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 */

class NewRotationService(private val context: Context) {
    
    private val database = AppDatabase.getDatabase(context)
    private val sessionDao = database.rotationSessionDao()
    private val assignmentDao = database.rotationAssignmentDao()
    private val capabilityDao = database.workerWorkstationCapabilityDao()
    private val workerDao = database.workerDao()
    private val workstationDao = database.workstationDao()
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // 📊 GESTIÓN DE SESIONES
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    /**
     * Crea una nueva sesión de rotación
     */
    suspend fun createRotationSession(
        name: String = RotationSession.generateSessionName(),
        description: String? = null
    ): Long = withContext(Dispatchers.IO) {
        
        // Completar sesión activa anterior si existe
        sessionDao.getActiveSession()?.let { activeSession ->
            sessionDao.completeSession(activeSession.id)
        }
        
        // Crear nueva sesión
        val session = RotationSession(
            name = name,
            description = description,
            status = RotationSession.STATUS_DRAFT
        )
        
        sessionDao.insert(session)
    }
    
    /**
     * Activa una sesión de rotación
     */
    suspend fun activateSession(sessionId: Long): Boolean = withContext(Dispatchers.IO) {
        try {
            // Completar otras sesiones activas
            sessionDao.completeAllActiveSessions()
            
            // Activar la sesión especificada
            sessionDao.activateSession(sessionId)
            
            // Actualizar contadores
            updateSessionCounts(sessionId)
            
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Obtiene la sesión activa actual
     */
    fun getActiveSessionFlow(): Flow<RotationSession?> = sessionDao.getActiveSessionFlow()
    
    /**
     * Obtiene todas las sesiones
     */
    fun getAllSessionsFlow(): Flow<List<RotationSession>> = sessionDao.getAllFlow()
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // 🎯 GENERACIÓN DE GRID DE ROTACIÓN
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    /**
     * Genera el grid completo de rotación para una sesión
     */
    fun getRotationGridFlow(sessionId: Long): Flow<RotationGrid> {
        return combine(
            workstationDao.getAllActiveWorkstations(),
            assignmentDao.getBySessionFlow(sessionId),
            workerDao.getAllActiveWorkers(),
            capabilityDao.getActiveCapabilitiesFlow()
        ) { workstations, assignments, workers, capabilities ->
            
            buildRotationGrid(sessionId, workstations, assignments, workers, capabilities)
        }
    }
    
    /**
     * Construye el grid de rotación con todos los datos
     */
    private suspend fun buildRotationGrid(
        sessionId: Long,
        workstations: List<Workstation>,
        assignments: List<RotationAssignment>,
        workers: List<Worker>,
        capabilities: List<WorkerWorkstationCapability>
    ): RotationGrid {
        
        // 🔍 LOGS DE DIAGNÓSTICO
        android.util.Log.d("NewRotationService", "═══════════════════════════════════════════════════════")
        android.util.Log.d("NewRotationService", "🔍 CONSTRUYENDO GRID DE ROTACIÓN")
        android.util.Log.d("NewRotationService", "═══════════════════════════════════════════════════════")
        android.util.Log.d("NewRotationService", "📊 Datos recibidos:")
        android.util.Log.d("NewRotationService", "  • Estaciones: ${workstations.size}")
        android.util.Log.d("NewRotationService", "  • Asignaciones: ${assignments.size}")
        android.util.Log.d("NewRotationService", "  • Trabajadores: ${workers.size}")
        android.util.Log.d("NewRotationService", "  • Capacidades: ${capabilities.size}")
        
        workstations.forEach { ws ->
            android.util.Log.d("NewRotationService", "  📍 Estación: ${ws.name} (ID: ${ws.id}, Req: ${ws.requiredWorkers})")
        }
        
        workers.forEach { w ->
            val workerCaps = capabilities.filter { it.worker_id == w.id && it.is_active }
            android.util.Log.d("NewRotationService", "  👤 Trabajador: ${w.name} (ID: ${w.id}, Caps activas: ${workerCaps.size})")
        }
        
        val session = sessionDao.getById(sessionId)
        
        // Agrupar asignaciones por estación y tipo
        val assignmentsByStation = assignments.groupBy { it.workstation_id }
        
        // Crear filas del grid
        val rows = workstations.map { workstation ->
            val stationAssignments = assignmentsByStation[workstation.id] ?: emptyList()
            
            val currentAssignments = stationAssignments
                .filter { it.rotation_type == RotationAssignment.TYPE_CURRENT && it.is_active }
                .map { assignment ->
                    val worker = workers.find { it.id == assignment.worker_id }
                    val capability = capabilities.find { 
                        it.worker_id == assignment.worker_id && it.workstation_id == workstation.id 
                    }
                    
                    RotationGridCell(
                        workerId = assignment.worker_id,
                        workerName = worker?.name,
                        workstationId = workstation.id,
                        workstationName = workstation.name,
                        rotationType = RotationAssignment.TYPE_CURRENT,
                        isAssigned = true,
                        competencyLevel = capability?.competency_level,
                        canBeLeader = capability?.can_be_leader ?: false,
                        canTrain = capability?.can_train ?: false,
                        isOptimalAssignment = capability?.calculateSuitabilityScore() ?: 0.0 > 0.8
                    )
                }
            
            val nextAssignments = stationAssignments
                .filter { it.rotation_type == RotationAssignment.TYPE_NEXT && it.is_active }
                .map { assignment ->
                    val worker = workers.find { it.id == assignment.worker_id }
                    val capability = capabilities.find { 
                        it.worker_id == assignment.worker_id && it.workstation_id == workstation.id 
                    }
                    
                    RotationGridCell(
                        workerId = assignment.worker_id,
                        workerName = worker?.name,
                        workstationId = workstation.id,
                        workstationName = workstation.name,
                        rotationType = RotationAssignment.TYPE_NEXT,
                        isAssigned = true,
                        competencyLevel = capability?.competency_level,
                        canBeLeader = capability?.can_be_leader ?: false,
                        canTrain = capability?.can_train ?: false,
                        isOptimalAssignment = capability?.calculateSuitabilityScore() ?: 0.0 > 0.8
                    )
                }
            
            // Completar con celdas vacías hasta la capacidad requerida
            val currentCells = currentAssignments + (0 until (workstation.requiredWorkers - currentAssignments.size)).map {
                RotationGridCell(
                    workstationId = workstation.id,
                    workstationName = workstation.name,
                    rotationType = RotationAssignment.TYPE_CURRENT,
                    isAssigned = false
                )
            }
            
            val nextCells = nextAssignments + (0 until (workstation.requiredWorkers - nextAssignments.size)).map {
                RotationGridCell(
                    workstationId = workstation.id,
                    workstationName = workstation.name,
                    rotationType = RotationAssignment.TYPE_NEXT,
                    isAssigned = false
                )
            }
            
            RotationGridRow(
                workstationId = workstation.id,
                workstationName = workstation.name,
                requiredWorkers = workstation.requiredWorkers,
                currentAssignments = currentCells,
                nextAssignments = nextCells
            )
        }
        
        // Crear lista de trabajadores disponibles
        // FILTRO CRÍTICO: Solo incluir trabajadores con capacidades activas
        val assignedWorkerIds = assignments.filter { it.is_active }.map { it.worker_id }.toSet()
        val availableWorkers = workers.mapNotNull { worker ->
            val workerCapabilities = capabilities.filter { it.worker_id == worker.id && it.is_active }
            
            // ⚠️ VALIDACIÓN: Excluir trabajadores sin capacidades activas
            if (workerCapabilities.isEmpty()) {
                android.util.Log.w("NewRotationService", "⚠️ Trabajador '${worker.name}' (ID: ${worker.id}) excluido - sin capacidades activas")
                return@mapNotNull null
            }
            
            val workstationCapabilities = workerCapabilities.map { capability ->
                val workstation = workstations.find { it.id == capability.workstation_id }
                WorkstationCapability(
                    workstationId = capability.workstation_id,
                    workstationName = workstation?.name ?: "Desconocida",
                    competencyLevel = capability.competency_level,
                    canBeLeader = capability.can_be_leader,
                    canTrain = capability.can_train,
                    isCertified = capability.isCertificationValid(),
                    canBeAssigned = capability.canBeAssigned()
                )
            }
            
            val currentAssignment = assignments.find { 
                it.worker_id == worker.id && it.rotation_type == RotationAssignment.TYPE_CURRENT && it.is_active 
            }
            val nextAssignment = assignments.find { 
                it.worker_id == worker.id && it.rotation_type == RotationAssignment.TYPE_NEXT && it.is_active 
            }
            
            AvailableWorker(
                workerId = worker.id,
                workerName = worker.name,
                employeeId = worker.employeeId,
                isActive = worker.isActive,
                availableWorkstations = workstationCapabilities,
                currentAssignment = currentAssignment?.workstation_id,
                nextAssignment = nextAssignment?.workstation_id,
                isAssignedInCurrent = currentAssignment != null,
                isAssignedInNext = nextAssignment != null
            )
        }
        
        // 🔍 LOGS DE RESULTADO
        android.util.Log.d("NewRotationService", "═══════════════════════════════════════════════════════")
        android.util.Log.d("NewRotationService", "✅ GRID CONSTRUIDO:")
        android.util.Log.d("NewRotationService", "  • Filas (estaciones): ${rows.size}")
        android.util.Log.d("NewRotationService", "  • Trabajadores disponibles: ${availableWorkers.size}")
        rows.forEach { row ->
            android.util.Log.d("NewRotationService", "  📍 ${row.workstationName}: ${row.currentAssignments.count { it.isAssigned }}/${row.requiredWorkers} actual, ${row.nextAssignments.count { it.isAssigned }}/${row.requiredWorkers} siguiente")
        }
        android.util.Log.d("NewRotationService", "═══════════════════════════════════════════════════════")
        
        return RotationGrid(
            sessionId = sessionId,
            sessionName = session?.name ?: "Sesión Desconocida",
            rows = rows,
            availableWorkers = availableWorkers
        )
    }
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // 🎯 ASIGNACIÓN DE TRABAJADORES
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    /**
     * Asigna un trabajador a una estación
     */
    suspend fun assignWorkerToWorkstation(
        sessionId: Long,
        workerId: Long,
        workstationId: Long,
        rotationType: String
    ): Result<Long> = withContext(Dispatchers.IO) {
        
        try {
            // Validar que el trabajador puede ser asignado a esta estación
            val capability = capabilityDao.getByWorkerAndWorkstation(workerId, workstationId)
            if (capability == null || !capability.canBeAssigned()) {
                return@withContext Result.failure(Exception("El trabajador no tiene capacidad para esta estación"))
            }
            
            // Verificar que no esté ya asignado en este tipo de rotación
            val existingAssignment = assignmentDao.getCurrentAssignmentByWorker(workerId, rotationType)
            if (existingAssignment != null) {
                return@withContext Result.failure(Exception("El trabajador ya está asignado en esta rotación"))
            }
            
            // Verificar capacidad de la estación
            val currentCount = assignmentDao.getWorkstationAssignmentCount(workstationId, sessionId, rotationType)
            val workstation = workstationDao.getWorkstationById(workstationId)
            if (workstation != null && currentCount >= workstation.requiredWorkers) {
                return@withContext Result.failure(Exception("La estación ya está completa"))
            }
            
            // Crear la asignación
            val assignment = RotationAssignment(
                worker_id = workerId,
                workstation_id = workstationId,
                rotation_session_id = sessionId,
                rotation_type = rotationType,
                priority = if (capability.can_be_leader) 1 else 3
            )
            
            val assignmentId = assignmentDao.insert(assignment)
            updateSessionCounts(sessionId)
            
            Result.success(assignmentId)
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Remueve una asignación de trabajador
     */
    suspend fun removeWorkerAssignment(
        sessionId: Long,
        workerId: Long,
        rotationType: String
    ): Boolean = withContext(Dispatchers.IO) {
        
        try {
            val assignment = assignmentDao.getCurrentAssignmentByWorker(workerId, rotationType)
            if (assignment != null) {
                assignmentDao.delete(assignment)
                updateSessionCounts(sessionId)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Mueve un trabajador de una estación a otra
     */
    suspend fun moveWorkerAssignment(
        sessionId: Long,
        workerId: Long,
        fromWorkstationId: Long,
        toWorkstationId: Long,
        rotationType: String
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        
        try {
            // Validar capacidad en la estación destino
            val capability = capabilityDao.getByWorkerAndWorkstation(workerId, toWorkstationId)
            if (capability == null || !capability.canBeAssigned()) {
                return@withContext Result.failure(Exception("El trabajador no tiene capacidad para la estación destino"))
            }
            
            // Verificar capacidad de la estación destino
            val currentCount = assignmentDao.getWorkstationAssignmentCount(toWorkstationId, sessionId, rotationType)
            val workstation = workstationDao.getWorkstationById(toWorkstationId)
            if (workstation != null && currentCount >= workstation.requiredWorkers) {
                return@withContext Result.failure(Exception("La estación destino ya está completa"))
            }
            
            // Buscar y actualizar la asignación existente
            val existingAssignment = assignmentDao.getCurrentAssignmentByWorker(workerId, rotationType)
            if (existingAssignment != null && existingAssignment.workstation_id == fromWorkstationId) {
                val updatedAssignment = existingAssignment.copy(
                    workstation_id = toWorkstationId,
                    priority = if (capability.can_be_leader) 1 else 3
                )
                assignmentDao.update(updatedAssignment)
                updateSessionCounts(sessionId)
                Result.success(true)
            } else {
                Result.failure(Exception("No se encontró la asignación original"))
            }
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // 🤖 GENERACIÓN AUTOMÁTICA DE ROTACIONES
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    /**
     * Genera automáticamente una rotación optimizada
     */
    suspend fun generateOptimizedRotation(
        sessionId: Long,
        rotationType: String,
        clearExisting: Boolean = true
    ): Result<Int> = withContext(Dispatchers.IO) {
        
        try {
            // Limpiar asignaciones existentes si se solicita
            if (clearExisting) {
                assignmentDao.clearRotationType(sessionId, rotationType)
            }
            
            // Obtener datos necesarios
            val workstations = workstationDao.getAllActiveWorkstations().first()
            val workers = workerDao.getAllActiveWorkers().first()
            val capabilities = capabilityDao.getActiveCapabilitiesFlow().first()
            
            // Algoritmo de asignación optimizada
            val assignments = mutableListOf<RotationAssignment>()
            val assignedWorkers = mutableSetOf<Long>()
            
            // Filtrar solo trabajadores que tienen al menos una estación asignada
            val workersWithStations = capabilities
                .filter { it.canBeAssigned() }
                .map { it.worker_id }
                .distinct()
            
            android.util.Log.d("NewRotationService", "═══════════════════════════════════════")
            android.util.Log.d("NewRotationService", "🔄 GENERANDO ROTACIÓN OPTIMIZADA")
            android.util.Log.d("NewRotationService", "═══════════════════════════════════════")
            android.util.Log.d("NewRotationService", "Estaciones activas: ${workstations.size}")
            android.util.Log.d("NewRotationService", "Trabajadores activos: ${workers.size}")
            android.util.Log.d("NewRotationService", "Trabajadores con estaciones asignadas: ${workersWithStations.size}")
            android.util.Log.d("NewRotationService", "Capacidades totales: ${capabilities.size}")
            
            // Paso 1: Asignar líderes a estaciones prioritarias
            workstations.filter { it.isPriority && it.isActive }.forEach { workstation ->
                val leaders = capabilities.filter { 
                    it.workstation_id == workstation.id && 
                    it.can_be_leader && 
                    it.canBeAssigned() &&
                    workersWithStations.contains(it.worker_id) &&
                    !assignedWorkers.contains(it.worker_id)
                }.sortedByDescending { it.calculateSuitabilityScore() }
                
                android.util.Log.d("NewRotationService", "Estación prioritaria: ${workstation.name}, Líderes disponibles: ${leaders.size}")
                
                leaders.take(1).forEach { leader ->
                    assignments.add(RotationAssignment(
                        worker_id = leader.worker_id,
                        workstation_id = workstation.id,
                        rotation_session_id = sessionId,
                        rotation_type = rotationType,
                        priority = 1
                    ))
                    assignedWorkers.add(leader.worker_id)
                    android.util.Log.d("NewRotationService", "  ✅ Líder asignado: Worker ${leader.worker_id}")
                }
            }
            
            // Paso 2: Completar estaciones con mejores candidatos
            workstations.filter { it.isActive }.forEach { workstation ->
                val currentAssigned = assignments.count { it.workstation_id == workstation.id }
                val needed = workstation.requiredWorkers - currentAssigned
                
                android.util.Log.d("NewRotationService", "Estación: ${workstation.name}")
                android.util.Log.d("NewRotationService", "  Requeridos: ${workstation.requiredWorkers}, Asignados: $currentAssigned, Necesarios: $needed")
                
                if (needed > 0) {
                    val candidates = capabilities.filter { 
                        it.workstation_id == workstation.id && 
                        it.canBeAssigned() &&
                        workersWithStations.contains(it.worker_id) &&
                        !assignedWorkers.contains(it.worker_id)
                    }.sortedByDescending { it.calculateSuitabilityScore() }
                    
                    android.util.Log.d("NewRotationService", "  Candidatos disponibles: ${candidates.size}")
                    
                    candidates.take(needed).forEach { candidate ->
                        assignments.add(RotationAssignment(
                            worker_id = candidate.worker_id,
                            workstation_id = workstation.id,
                            rotation_session_id = sessionId,
                            rotation_type = rotationType,
                            priority = if (candidate.can_train) 2 else 3
                        ))
                        assignedWorkers.add(candidate.worker_id)
                        android.util.Log.d("NewRotationService", "  ✅ Trabajador asignado: Worker ${candidate.worker_id}")
                    }
                    
                    if (candidates.size < needed) {
                        android.util.Log.w("NewRotationService", "  ⚠️ ADVERTENCIA: Faltan ${needed - candidates.size} trabajadores para completar la estación")
                    }
                }
            }
            
            android.util.Log.d("NewRotationService", "═══════════════════════════════════════")
            android.util.Log.d("NewRotationService", "✅ Total de asignaciones creadas: ${assignments.size}")
            android.util.Log.d("NewRotationService", "✅ Trabajadores únicos asignados: ${assignedWorkers.size}")
            android.util.Log.d("NewRotationService", "═══════════════════════════════════════")
            
            // Insertar todas las asignaciones
            assignmentDao.insertAll(assignments)
            updateSessionCounts(sessionId)
            
            Result.success(assignments.size)
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // 🔄 TRANSICIÓN ENTRE ROTACIONES
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    /**
     * Promociona la siguiente rotación a rotación actual
     */
    suspend fun promoteNextToCurrent(sessionId: Long): Boolean = withContext(Dispatchers.IO) {
        try {
            // Completar rotación actual
            assignmentDao.completeCurrentRotation(sessionId)
            
            // Promocionar siguiente a actual
            assignmentDao.promoteNextToCurrent(sessionId)
            
            updateSessionCounts(sessionId)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Copia la rotación actual a la siguiente
     */
    suspend fun copyCurrentToNext(sessionId: Long): Boolean = withContext(Dispatchers.IO) {
        try {
            // Limpiar siguiente rotación
            assignmentDao.clearRotationType(sessionId, RotationAssignment.TYPE_NEXT)
            
            // Obtener asignaciones actuales
            val currentAssignments = assignmentDao.getBySessionAndType(sessionId, RotationAssignment.TYPE_CURRENT)
            
            // Crear copias para siguiente rotación
            val nextAssignments = currentAssignments.map { current ->
                current.copy(
                    id = 0, // Nuevo ID
                    rotation_type = RotationAssignment.TYPE_NEXT,
                    assigned_at = System.currentTimeMillis(),
                    started_at = null,
                    completed_at = null
                )
            }
            
            assignmentDao.insertAll(nextAssignments)
            updateSessionCounts(sessionId)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Limpia todas las asignaciones de una rotación específica.
     * Útil para regenerar rotaciones o empezar desde cero.
     * 
     * @param sessionId ID de la sesión
     * @param rotationType Tipo de rotación (CURRENT o NEXT)
     * @return true si se limpió exitosamente
     */
    suspend fun clearRotation(sessionId: Long, rotationType: String): Boolean = withContext(Dispatchers.IO) {
        try {
            android.util.Log.d("NewRotationService", "🧹 Limpiando rotación $rotationType de sesión $sessionId")
            
            // Limpiar asignaciones del tipo especificado
            assignmentDao.clearRotationType(sessionId, rotationType)
            
            // Actualizar contadores de la sesión
            updateSessionCounts(sessionId)
            
            android.util.Log.d("NewRotationService", "✅ Rotación $rotationType limpiada exitosamente")
            true
        } catch (e: Exception) {
            android.util.Log.e("NewRotationService", "❌ Error al limpiar rotación: ${e.message}", e)
            false
        }
    }
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // 🛠️ FUNCIONES AUXILIARES
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    /**
     * Actualiza los contadores de la sesión
     */
    private suspend fun updateSessionCounts(sessionId: Long) {
        val workerCount = assignmentDao.getUniqueWorkerCount(sessionId, RotationAssignment.TYPE_CURRENT) +
                         assignmentDao.getUniqueWorkerCount(sessionId, RotationAssignment.TYPE_NEXT)
        val workstationCount = assignmentDao.getUniqueWorkstationCount(sessionId, RotationAssignment.TYPE_CURRENT) +
                              assignmentDao.getUniqueWorkstationCount(sessionId, RotationAssignment.TYPE_NEXT)
        
        sessionDao.updateSessionCounts(sessionId, workerCount, workstationCount)
    }
    
    /**
     * Valida una operación de drag & drop
     */
    suspend fun validateDragOperation(
        workerId: Long,
        targetWorkstationId: Long,
        rotationType: String
    ): RotationDragOperation = withContext(Dispatchers.IO) {
        
        val worker = workerDao.getWorkerById(workerId)
        val capability = capabilityDao.getByWorkerAndWorkstation(workerId, targetWorkstationId)
        
        val isValid = worker != null && 
                     worker.isActive && 
                     capability != null && 
                     capability.canBeAssigned()
        
        val message = when {
            worker == null -> "Trabajador no encontrado"
            !worker.isActive -> "Trabajador inactivo"
            capability == null -> "Sin capacidad para esta estación"
            !capability.canBeAssigned() -> "No cumple requisitos mínimos"
            else -> null
        }
        
        RotationDragOperation(
            workerId = workerId,
            workerName = worker?.name ?: "Desconocido",
            sourceWorkstationId = null, // Se determinará en la UI
            targetWorkstationId = targetWorkstationId,
            rotationType = rotationType,
            isValid = isValid,
            validationMessage = message
        )
    }
}