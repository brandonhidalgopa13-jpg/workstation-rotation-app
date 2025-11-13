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
        android.util.Log.d("NewRotationService", "═══════════════════════════════════════════════════════")
        android.util.Log.d("NewRotationService", "🔍 CONSTRUYENDO LISTA DE TRABAJADORES DISPONIBLES")
        android.util.Log.d("NewRotationService", "═══════════════════════════════════════════════════════")
        
        val assignedWorkerIds = assignments.filter { it.is_active }.map { it.worker_id }.toSet()
        val availableWorkers = workers.mapNotNull { worker ->
            val workerCapabilities = capabilities.filter { it.worker_id == worker.id && it.is_active }
            
            android.util.Log.d("NewRotationService", "👤 Trabajador: ${worker.name} (ID: ${worker.id})")
            android.util.Log.d("NewRotationService", "   • Activo: ${worker.isActive}")
            android.util.Log.d("NewRotationService", "   • Es líder: ${worker.isLeader}")
            android.util.Log.d("NewRotationService", "   • Estación de liderazgo: ${worker.leaderWorkstationId}")
            android.util.Log.d("NewRotationService", "   • Capacidades activas: ${workerCapabilities.size}")
            
            // ⚠️ VALIDACIÓN: Excluir trabajadores sin capacidades activas
            if (workerCapabilities.isEmpty()) {
                android.util.Log.w("NewRotationService", "   ⚠️ EXCLUIDO - sin capacidades activas")
                return@mapNotNull null
            }
            
            val workstationCapabilities = workerCapabilities.map { capability ->
                val workstation = workstations.find { it.id == capability.workstation_id }
                android.util.Log.d("NewRotationService", "   • Estación: ${workstation?.name} (ID: ${capability.workstation_id})")
                android.util.Log.d("NewRotationService", "     - Nivel: ${capability.competency_level}")
                android.util.Log.d("NewRotationService", "     - Puede ser líder: ${capability.can_be_leader}")
                android.util.Log.d("NewRotationService", "     - Puede entrenar: ${capability.can_train}")
                android.util.Log.d("NewRotationService", "     - Puede ser asignado: ${capability.canBeAssigned()}")
                
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
            
            android.util.Log.d("NewRotationService", "   • Asignación actual: ${currentAssignment?.workstation_id}")
            android.util.Log.d("NewRotationService", "   • Asignación siguiente: ${nextAssignment?.workstation_id}")
            android.util.Log.d("NewRotationService", "   ✅ INCLUIDO en lista de disponibles")
            
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
            
            // Paso 1: Asignar LÍDERES a sus estaciones designadas (PRIORIDAD MÁXIMA)
            android.util.Log.d("NewRotationService", "═══ PASO 1: ASIGNANDO LÍDERES ═══")
            
            workers.filter { it.isLeader && it.isActive }.forEach { leader ->
                val leaderStationId = leader.leaderWorkstationId
                
                android.util.Log.d("NewRotationService", "  🔍 Procesando líder: ${leader.name} (ID: ${leader.id})")
                android.util.Log.d("NewRotationService", "    • Estación designada: $leaderStationId")
                android.util.Log.d("NewRotationService", "    • Tipo de liderazgo: ${leader.leadershipType}")
                
                if (leaderStationId != null) {
                    // Verificar que el líder puede trabajar en su estación
                    val capability = capabilities.find { 
                        it.worker_id == leader.id && 
                        it.workstation_id == leaderStationId &&
                        it.is_active
                    }
                    
                    android.util.Log.d("NewRotationService", "    • Capacidad encontrada: ${capability != null}")
                    if (capability != null) {
                        android.util.Log.d("NewRotationService", "    • Puede ser asignado: ${capability.canBeAssigned()}")
                        android.util.Log.d("NewRotationService", "    • Puede ser líder: ${capability.can_be_leader}")
                    }
                    
                    if (capability != null && capability.canBeAssigned()) {
                        // Verificar si debe estar en esta rotación según su tipo de liderazgo
                        val isFirstHalf = rotationType == "CURRENT"
                        val shouldBeInRotation = when (leader.leadershipType) {
                            "BOTH" -> true
                            "FIRST_HALF" -> isFirstHalf
                            "SECOND_HALF" -> !isFirstHalf
                            else -> true
                        }
                        
                        android.util.Log.d("NewRotationService", "    • Debe estar en esta rotación: $shouldBeInRotation")
                        android.util.Log.d("NewRotationService", "    • Ya asignado: ${assignedWorkers.contains(leader.id)}")
                        
                        if (shouldBeInRotation && !assignedWorkers.contains(leader.id)) {
                            val workstation = workstations.find { it.id == leaderStationId }
                            assignments.add(RotationAssignment(
                                worker_id = leader.id,
                                workstation_id = leaderStationId,
                                rotation_session_id = sessionId,
                                rotation_type = rotationType,
                                priority = 1
                            ))
                            assignedWorkers.add(leader.id)
                            android.util.Log.d("NewRotationService", "  ✅ 👑 LÍDER ASIGNADO: ${leader.name} → ${workstation?.name} (${leader.leadershipType})")
                        } else {
                            android.util.Log.d("NewRotationService", "  ⏭️ Líder ${leader.name} no corresponde a esta rotación (${leader.leadershipType})")
                        }
                    } else {
                        android.util.Log.w("NewRotationService", "  ⚠️ Líder ${leader.name} no tiene capacidad válida para su estación designada")
                    }
                } else {
                    android.util.Log.w("NewRotationService", "  ⚠️ Líder ${leader.name} no tiene estación designada")
                }
            }
            
            // Paso 1.5: Asignar PAREJAS DE ENTRENAMIENTO (PRIORIDAD MÁXIMA)
            android.util.Log.d("NewRotationService", "═══ PASO 1.5: ASIGNANDO ENTRENAMIENTOS ═══")
            
            workers.filter { it.isTrainee && it.isActive }.forEach { trainee ->
                val trainerId = trainee.trainerId
                val trainingStationId = trainee.trainingWorkstationId
                
                if (trainerId != null && trainingStationId != null) {
                    val trainer = workers.find { it.id == trainerId && it.isActive }
                    
                    if (trainer != null && !assignedWorkers.contains(trainee.id) && !assignedWorkers.contains(trainer.id)) {
                        // Verificar que ambos pueden trabajar en la estación
                        val traineeCapability = capabilities.find { 
                            it.worker_id == trainee.id && 
                            it.workstation_id == trainingStationId &&
                            it.canBeAssigned()
                        }
                        val trainerCapability = capabilities.find { 
                            it.worker_id == trainer.id && 
                            it.workstation_id == trainingStationId &&
                            it.canBeAssigned()
                        }
                        
                        if (traineeCapability != null && trainerCapability != null) {
                            val workstation = workstations.find { it.id == trainingStationId }
                            
                            // Asignar entrenador
                            assignments.add(RotationAssignment(
                                worker_id = trainer.id,
                                workstation_id = trainingStationId,
                                rotation_session_id = sessionId,
                                rotation_type = rotationType,
                                priority = 1
                            ))
                            assignedWorkers.add(trainer.id)
                            
                            // Asignar entrenado
                            assignments.add(RotationAssignment(
                                worker_id = trainee.id,
                                workstation_id = trainingStationId,
                                rotation_session_id = sessionId,
                                rotation_type = rotationType,
                                priority = 1
                            ))
                            assignedWorkers.add(trainee.id)
                            
                            android.util.Log.d("NewRotationService", "  🎯 ENTRENAMIENTO: ${trainer.name} + ${trainee.name} → ${workstation?.name}")
                        } else {
                            android.util.Log.w("NewRotationService", "  ⚠️ Pareja ${trainer.name}-${trainee.name} no tiene capacidades para estación de entrenamiento")
                        }
                    }
                }
            }
            
            // Paso 2: Completar estaciones con ROTACIÓN INTELIGENTE CON HISTORIAL GLOBAL
            android.util.Log.d("NewRotationService", "═══ PASO 2: COMPLETANDO ESTACIONES CON ROTACIÓN INTELIGENTE ═══")
            
            // Obtener TODAS las asignaciones previas de esta sesión (ambos tipos de rotación)
            // para evitar que los trabajadores se queden en las mismas estaciones
            val allPreviousAssignments = assignmentDao.getBySession(sessionId)
            
            // ✅ CORRECCIÓN v4.0.20: Usar Set de Pares en lugar de Map
            // Esto permite detectar TODAS las combinaciones trabajador-estación previas
            val previousAssignments = allPreviousAssignments
                .filter { it.is_active }
                .map { Pair(it.worker_id, it.workstation_id) }
                .toSet()
            
            android.util.Log.d("NewRotationService", "📊 Asignaciones previas encontradas (todas las rotaciones): ${allPreviousAssignments.size}")
            android.util.Log.d("NewRotationService", "📊 Pares trabajador-estación previos: ${previousAssignments.size}")
            previousAssignments.take(10).forEach { (workerId, workstationId) ->
                android.util.Log.d("NewRotationService", "  • Worker $workerId -> Workstation $workstationId")
            }
            
            workstations.filter { it.isActive }.forEach { workstation ->
                val currentAssigned = assignments.count { it.workstation_id == workstation.id }
                val needed = workstation.requiredWorkers - currentAssigned
                
                android.util.Log.d("NewRotationService", "📍 Estación: ${workstation.name}")
                android.util.Log.d("NewRotationService", "  • Requeridos: ${workstation.requiredWorkers}")
                android.util.Log.d("NewRotationService", "  • Ya asignados: $currentAssigned")
                android.util.Log.d("NewRotationService", "  • Necesarios: $needed")
                
                if (needed > 0) {
                    // ✨ ROTACIÓN INTELIGENTE CON PRIORIDAD A TRABAJADORES QUE NO ESTUVIERON AQUÍ ANTES
                    // Obtener candidatos elegibles
                    val allCandidates = capabilities.filter { capability ->
                        capability.workstation_id == workstation.id && 
                        capability.is_active &&
                        capability.canBeAssigned() &&
                        workersWithStations.contains(capability.worker_id) &&
                        !assignedWorkers.contains(capability.worker_id)
                    }
                    
                    // Separar candidatos en dos grupos:
                    // 1. Trabajadores que NO estuvieron en esta estación antes (PRIORIDAD ALTA)
                    // 2. Trabajadores que SÍ estuvieron en esta estación antes (PRIORIDAD BAJA)
                    // ✅ CORRECCIÓN v4.0.20: Usar Set.contains() en lugar de Map
                    val candidatesNotHereBefore = allCandidates.filter { capability ->
                        !previousAssignments.contains(Pair(capability.worker_id, workstation.id))
                    }
                    
                    val candidatesHereBefore = allCandidates.filter { capability ->
                        previousAssignments.contains(Pair(capability.worker_id, workstation.id))
                    }
                    
                    android.util.Log.d("NewRotationService", "  • Candidatos totales: ${allCandidates.size}")
                    android.util.Log.d("NewRotationService", "  • Candidatos NUEVOS (no estuvieron aquí): ${candidatesNotHereBefore.size}")
                    android.util.Log.d("NewRotationService", "  • Candidatos REPETIDOS (ya estuvieron aquí): ${candidatesHereBefore.size}")
                    
                    if (allCandidates.isNotEmpty()) {
                        // ESTRATEGIA DE ROTACIÓN INTELIGENTE:
                        // 1. Primero intentar asignar trabajadores que NO estuvieron aquí antes
                        // 2. Si no hay suficientes, usar trabajadores que ya estuvieron aquí
                        
                        val selectedCandidates = mutableListOf<com.workstation.rotation.data.entities.WorkerWorkstationCapability>()
                        
                        // Paso 2.1: Seleccionar trabajadores nuevos (mezclar aleatoriamente)
                        val newWorkersToAssign = candidatesNotHereBefore.shuffled().take(needed)
                        selectedCandidates.addAll(newWorkersToAssign)
                        
                        android.util.Log.d("NewRotationService", "  🔄 Asignando ${newWorkersToAssign.size} trabajadores NUEVOS")
                        
                        // Paso 2.2: Si faltan trabajadores, usar los que ya estuvieron aquí
                        val stillNeeded = needed - selectedCandidates.size
                        if (stillNeeded > 0 && candidatesHereBefore.isNotEmpty()) {
                            val repeatWorkersToAssign = candidatesHereBefore.shuffled().take(stillNeeded)
                            selectedCandidates.addAll(repeatWorkersToAssign)
                            android.util.Log.d("NewRotationService", "  ⚠️ Asignando ${repeatWorkersToAssign.size} trabajadores REPETIDOS (no hay suficientes nuevos)")
                        }
                        
                        // Calcular probabilidad
                        val totalCandidates = allCandidates.size
                        val probabilityPerCandidate = 100.0 / totalCandidates
                        
                        android.util.Log.d("NewRotationService", "  🎲 Rotación inteligente:")
                        android.util.Log.d("NewRotationService", "    • Total candidatos: $totalCandidates")
                        android.util.Log.d("NewRotationService", "    • Probabilidad por candidato: ${probabilityPerCandidate.toInt()}%")
                        android.util.Log.d("NewRotationService", "    • Prioridad: NUEVOS primero, REPETIDOS después")
                        
                        selectedCandidates.forEach { candidate ->
                            val worker = workers.find { it.id == candidate.worker_id }
                            // ✅ CORRECCIÓN v4.0.20: Verificar correctamente si estuvo aquí antes
                            val wasHereBefore = previousAssignments.contains(Pair(candidate.worker_id, workstation.id))
                            
                            assignments.add(RotationAssignment(
                                worker_id = candidate.worker_id,
                                workstation_id = workstation.id,
                                rotation_session_id = sessionId,
                                rotation_type = rotationType,
                                priority = if (candidate.can_train) 2 else 3
                            ))
                            assignedWorkers.add(candidate.worker_id)
                            
                            val statusIcon = if (wasHereBefore) "🔁" else "🆕"
                            android.util.Log.d("NewRotationService", "  ✅ $statusIcon Asignado: ${worker?.name ?: "Worker ${candidate.worker_id}"} (${if (wasHereBefore) "REPETIDO" else "NUEVO"})")
                        }
                    } else {
                        android.util.Log.w("NewRotationService", "  ⚠️ No hay candidatos disponibles para esta estación")
                    }
                    
                    if (allCandidates.size < needed) {
                        android.util.Log.w("NewRotationService", "  ⚠️ ADVERTENCIA: Faltan ${needed - allCandidates.size} trabajadores para completar la estación")
                    }
                } else {
                    android.util.Log.d("NewRotationService", "  ✓ Estación completa")
                }
            }
            
            android.util.Log.d("NewRotationService", "═══════════════════════════════════════")
            android.util.Log.d("NewRotationService", "✅ Total de asignaciones creadas: ${assignments.size}")
            android.util.Log.d("NewRotationService", "✅ Trabajadores únicos asignados: ${assignedWorkers.size}")
            
            // ✅ NUEVO v4.0.20: Verificar rotación por trabajador
            android.util.Log.d("NewRotationService", "═══════════════════════════════════════")
            android.util.Log.d("NewRotationService", "📊 VERIFICACIÓN DE ROTACIÓN:")
            assignedWorkers.forEach { workerId ->
                val worker = workers.find { it.id == workerId }
                val assignment = assignments.find { it.worker_id == workerId }
                val workstation = workstations.find { it.id == assignment?.workstation_id }
                
                val wasHereBefore = previousAssignments.contains(
                    Pair(workerId, assignment?.workstation_id ?: 0)
                )
                
                val status = if (wasHereBefore) "🔁 REPETIDO" else "🆕 NUEVO"
                android.util.Log.d("NewRotationService", 
                    "  $status ${worker?.name ?: "Worker $workerId"} → ${workstation?.name ?: "Estación ${assignment?.workstation_id}"}")
            }
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