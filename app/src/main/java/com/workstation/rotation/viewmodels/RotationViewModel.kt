package com.workstation.rotation.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.workstation.rotation.data.dao.WorkerDao
import com.workstation.rotation.data.dao.WorkstationDao
import com.workstation.rotation.data.dao.WorkerRestrictionDao
import com.workstation.rotation.data.entities.Worker
import com.workstation.rotation.data.entities.Workstation
import com.workstation.rotation.data.entities.RestrictionType
import com.workstation.rotation.models.RotationItem
import com.workstation.rotation.models.RotationTable
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 🏭 SISTEMA DE ROTACIÓN INTELIGENTE DE ESTACIONES DE TRABAJO
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 
 * 📋 FUNCIONES PRINCIPALES DEL SISTEMA:
 * 
 * 🎯 1. GESTIÓN DE ROTACIONES AUTOMÁTICAS
 *    - Genera rotaciones inteligentes basadas en múltiples criterios
 *    - Considera disponibilidad, capacitación y restricciones de trabajadores
 *    - Optimiza la distribución de personal en estaciones de trabajo
 * 
 * 👥 2. SISTEMA DE ENTRENAMIENTO INTEGRADO
 *    - Asigna automáticamente parejas entrenador-entrenado
 *    - Garantiza que entrenados siempre estén con sus entrenadores asignados
 *    - Prioriza estaciones de entrenamiento solicitadas
 * 
 * ⭐ 3. MANEJO DE ESTACIONES PRIORITARIAS
 *    - Asegura capacidad completa en estaciones críticas
 *    - Asigna los mejores trabajadores disponibles a estaciones prioritarias
 *    - Mantiene continuidad operativa en áreas clave
 * 
 * 📊 4. ALGORITMO DE OPTIMIZACIÓN
 *    - Balancea carga de trabajo entre estaciones
 *    - Considera porcentajes de disponibilidad individual
 *    - Aplica variación aleatoria para evitar patrones repetitivos
 * 
 * 🔄 5. ROTACIÓN DUAL (ACTUAL + SIGUIENTE)
 *    - Genera posición actual y siguiente rotación simultáneamente
 *    - Permite planificación anticipada de movimientos
 *    - Facilita transiciones suaves entre turnos
 * 
 * 🎨 6. VISUALIZACIÓN AVANZADA
 *    - Crea elementos visuales con indicadores de estado
 *    - Muestra información de capacidad y disponibilidad
 *    - Identifica visualmente parejas de entrenamiento activas
 * 
 * 🛡️ 7. VALIDACIÓN Y RESTRICCIONES
 *    - Respeta restricciones individuales de trabajadores
 *    - Valida capacidades de estaciones antes de asignación
 *    - Maneja casos especiales y excepciones
 * 
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 💻 Desarrollado por: Brandon Josué Hidalgo Paz
 * 🏷️ Versión: Sistema de Rotación Inteligente v2.0
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 */

class RotationViewModel(
    private val workerDao: WorkerDao,
    private val workstationDao: WorkstationDao,
    private val workerRestrictionDao: WorkerRestrictionDao
) : ViewModel() {
    
    // Cache for worker-workstation relationships to avoid multiple DB calls
    private var workerWorkstationCache: Map<Long, List<Long>> = emptyMap()
    
    /**
     * Limpia el caché de asignaciones trabajador-estación para forzar recarga desde BD.
     */
    fun clearWorkerWorkstationCache() {
        println("DEBUG: Limpiando caché de asignaciones trabajador-estación")
        workerWorkstationCache = emptyMap()
    }
    
    /**
     * Gets worker workstation IDs from cache or database.
     * Always loads fresh data if cache is empty.
     */
    private suspend fun getWorkerWorkstationIds(workerId: Long): List<Long> {
        val cachedIds = workerWorkstationCache[workerId]
        return if (cachedIds != null && workerWorkstationCache.isNotEmpty()) {
            println("DEBUG: Worker $workerId usando caché: $cachedIds")
            cachedIds
        } else {
            // Si no está en caché o el caché está vacío, obtener de la base de datos
            val ids = workerDao.getWorkerWorkstationIds(workerId)
            println("DEBUG: Worker $workerId cargado desde BD: $ids")
            
            // Actualizar caché para este trabajador
            val mutableCache = workerWorkstationCache.toMutableMap()
            mutableCache[workerId] = ids
            workerWorkstationCache = mutableCache
            
            ids
        }
    }
    
    private val _rotationItems = MutableLiveData<List<RotationItem>>()
    val rotationItems: LiveData<List<RotationItem>> = _rotationItems
    
    private val _rotationTable = MutableLiveData<RotationTable?>()
    val rotationTable: LiveData<RotationTable?> = _rotationTable
    
    private var eligibleWorkersCount = 0
    
    // Propiedad para rastrear si es primera o segunda parte de la rotación
    private var isFirstHalfRotation = true
    
    /**
     * Alterna entre primera y segunda parte de la rotación para el sistema de liderazgo.
     */
    fun toggleRotationHalf() {
        isFirstHalfRotation = !isFirstHalfRotation
        println("DEBUG: Rotation half toggled to: ${if (isFirstHalfRotation) "FIRST_HALF" else "SECOND_HALF"}")
    }
    
    /**
     * Obtiene el estado actual de la rotación (primera o segunda parte).
     */
    fun getCurrentRotationHalf(): String {
        return if (isFirstHalfRotation) "FIRST_HALF" else "SECOND_HALF"
    }
    
    /**
     * Obtiene una descripción legible del estado actual de la rotación.
     */
    fun getCurrentRotationHalfDescription(): String {
        return if (isFirstHalfRotation) "Primera Parte" else "Segunda Parte"
    }
    
    /**
     * Obtiene información sobre los líderes activos en la rotación actual.
     */
    suspend fun getActiveLeadersInfo(): String {
        val allWorkers = workerDao.getAllWorkers().first().filter { it.isActive && it.isLeader }
        val activeLeaders = allWorkers.filter { it.shouldBeLeaderInRotation(isFirstHalfRotation) }
        val inactiveLeaders = allWorkers.filter { !it.shouldBeLeaderInRotation(isFirstHalfRotation) }
        
        val info = StringBuilder()
        info.append("🔄 SISTEMA DE LIDERAZGO - ${getCurrentRotationHalfDescription()}\n\n")
        
        if (activeLeaders.isNotEmpty()) {
            info.append("👑 LÍDERES ACTIVOS (${activeLeaders.size}):\n")
            activeLeaders.forEach { leader ->
                val stationName = if (leader.leaderWorkstationId != null) {
                    workstationDao.getAllActiveWorkstations().first()
                        .find { it.id == leader.leaderWorkstationId }?.name ?: "Estación desconocida"
                } else "Sin estación asignada"
                info.append("• ${leader.name} - $stationName (${leader.getLeadershipTypeDescription()})\n")
            }
            info.append("\n")
        }
        
        if (inactiveLeaders.isNotEmpty()) {
            info.append("⏸️ LÍDERES INACTIVOS (${inactiveLeaders.size}):\n")
            inactiveLeaders.forEach { leader ->
                info.append("• ${leader.name} (${leader.getLeadershipTypeDescription()})\n")
            }
        }
        
        return info.toString()
    }
    
    /**
     * Generates an intelligent rotation considering priorities, availability, training relationships, and rotation cycles.
     * 
     * PRIORITY HIERARCHY (from highest to lowest):
     * 1. MAXIMUM PRIORITY: Trainer-trainee pairs in PRIORITY workstations
     * 2. HIGH PRIORITY: Trainer-trainee pairs in normal workstations  
     * 3. MEDIUM-HIGH PRIORITY: Trained workers needing forced rotation (been in same station too long)
     * 4. MEDIUM PRIORITY: Individual workers in priority workstations
     * 5. NORMAL PRIORITY: Individual workers in normal workstations
     * 
     * Key Features:
     * - ABSOLUTE PRIORITY for trainer-trainee pairs: When a trainee has an assigned trainer, 
     *   both are ALWAYS placed together at the trainee's requested training workstation
     * - SPECIAL PRIORITY for training in priority workstations: These get assigned FIRST
     * - FORCED ROTATION for trained workers: Workers who are not trainers/trainees and have been
     *   in the same station for multiple rotations are forced to change stations
     * - Training relationships override ALL other constraints (capacity, availability, workstation restrictions)
     * - Maintains priority workstation capacity requirements for non-training assignments
     * - Considers worker availability percentages and restrictions for individual assignments
     * - Ensures proper rotation variety while guaranteeing training continuity
     * - Tracks rotation history to prevent stagnation and promote skill development
     * 
     * @return Boolean indicating if rotation was successfully generated
     */
    suspend fun generateRotation(): Boolean {
        return try {
            println("DEBUG: ===== INICIANDO GENERACIÓN DE ROTACIÓN =====")
            println("DEBUG: Generando rotación para: ${getCurrentRotationHalf()}")
            
            // Verificar integridad de datos antes de proceder
            verifyDataIntegrity()
            
            val rotationData = prepareRotationData()
            if (!rotationData.isValid()) {
                println("DEBUG: ERROR - Rotation data is not valid")
                _rotationItems.value = emptyList()
                _rotationTable.value = null
                return false
            }
            
            val (rotationItems, rotationTable) = executeRotationAlgorithm(rotationData)
            _rotationItems.value = rotationItems.sortedBy { it.rotationOrder }
            _rotationTable.value = rotationTable
            
            println("DEBUG: Rotation generation completed - ${rotationItems.size} items generated")
            println("DEBUG: ============================================")
            
            rotationItems.isNotEmpty()
            
        } catch (e: Exception) {
            println("DEBUG: ERROR in generateRotation: ${e.message}")
            e.printStackTrace()
            _rotationItems.value = emptyList()
            _rotationTable.value = null
            eligibleWorkersCount = 0
            false
        }
    }
    
    /**
     * Prepares and validates data needed for rotation generation.
     * CORRECCIÓN: Mejora la verificación de asignaciones y proporciona más información de debug.
     */
    private suspend fun prepareRotationData(): RotationData {
        println("DEBUG: ===== PREPARANDO DATOS DE ROTACIÓN =====")
        
        val allWorkers = workerDao.getAllWorkers().first().filter { it.isActive }
        val eligibleWorkers = mutableListOf<Worker>()
        
        // Pre-load all worker-workstation relationships to avoid multiple DB calls
        val workerWorkstationMap = mutableMapOf<Long, List<Long>>()
        
        println("DEBUG: Procesando ${allWorkers.size} trabajadores activos")
        
        var workersWithAssignments = 0
        var totalAssignments = 0
        
        for (worker in allWorkers) {
            val workstationIds = workerDao.getWorkerWorkstationIds(worker.id)
            workerWorkstationMap[worker.id] = workstationIds
            
            // Debug: Log worker assignments with detailed info
            val workerType = when {
                worker.isTrainer -> "ENTRENADOR"
                worker.isTrainee -> "ENTRENADO"
                worker.isLeader -> "LÍDER (${worker.leadershipType})"
                worker.isCertified -> "CERTIFICADO"
                else -> "REGULAR"
            }
            
            if (workstationIds.isNotEmpty()) {
                eligibleWorkers.add(worker)
                workersWithAssignments++
                totalAssignments += workstationIds.size
                println("DEBUG: ✅ ${worker.name} [$workerType] → ${workstationIds.size} estaciones: $workstationIds")
            } else {
                println("DEBUG: ❌ ${worker.name} [$workerType] → SIN ESTACIONES ASIGNADAS - EXCLUIDO")
            }
        }
        
        // Cache the relationships for use in other functions
        workerWorkstationCache = workerWorkstationMap
        
        println("DEBUG: === RESUMEN DE ASIGNACIONES ===")
        println("DEBUG: Trabajadores con asignaciones: $workersWithAssignments/${allWorkers.size}")
        println("DEBUG: Total de asignaciones: $totalAssignments")
        println("DEBUG: Promedio de estaciones por trabajador: ${if (workersWithAssignments > 0) totalAssignments.toFloat() / workersWithAssignments else 0}")
        println("DEBUG: Caché actualizado con ${workerWorkstationMap.size} trabajadores")
        
        eligibleWorkersCount = eligibleWorkers.size
        val allWorkstations = workstationDao.getAllActiveWorkstations().first()
        
        println("DEBUG: === ESTACIONES DISPONIBLES ===")
        allWorkstations.forEach { station ->
            val assignedWorkers = workerWorkstationMap.count { it.value.contains(station.id) }
            println("DEBUG: ${station.name} (ID: ${station.id}) → $assignedWorkers trabajadores asignados, requiere ${station.requiredWorkers}")
        }
        
        println("DEBUG: Trabajadores elegibles: ${eligibleWorkers.size}")
        println("DEBUG: Estaciones activas: ${allWorkstations.size}")
        
        if (eligibleWorkers.isEmpty()) {
            println("DEBUG: ❌ ERROR CRÍTICO - No hay trabajadores elegibles para rotación!")
            println("DEBUG: Posibles causas:")
            println("DEBUG:   1. Ningún trabajador tiene estaciones asignadas")
            println("DEBUG:   2. Todos los trabajadores están inactivos")
            println("DEBUG:   3. Error en la base de datos")
        }
        
        if (allWorkstations.isEmpty()) {
            println("DEBUG: ❌ ERROR CRÍTICO - No hay estaciones activas!")
        }
        
        println("DEBUG: ========================================")
        
        return RotationData(eligibleWorkers, allWorkstations)
    }
    
    /**
     * Data class to hold rotation preparation data.
     */
    private data class RotationData(
        val eligibleWorkers: List<Worker>,
        val allWorkstations: List<Workstation>
    ) {
        fun isValid(): Boolean {
            val hasWorkers = eligibleWorkers.isNotEmpty()
            val hasWorkstations = allWorkstations.isNotEmpty()
            
            println("DEBUG: RotationData validation - Workers: $hasWorkers (${eligibleWorkers.size}), Workstations: $hasWorkstations (${allWorkstations.size})")
            
            return hasWorkers && hasWorkstations
        }
    }
    
    /**
     * Verifica la integridad de los datos de trabajadores y estaciones.
     */
    private suspend fun verifyDataIntegrity() {
        println("DEBUG: ===== VERIFICACIÓN DE INTEGRIDAD DE DATOS =====")
        
        // Verificar trabajadores activos
        val allWorkers = workerDao.getAllWorkers().first()
        val activeWorkers = allWorkers.filter { it.isActive }
        println("DEBUG: Total workers: ${allWorkers.size}, Active workers: ${activeWorkers.size}")
        
        // Verificar sistema de liderazgo
        verifyLeadershipSystem(activeWorkers)
        
        // Verificar estaciones activas
        val allWorkstations = workstationDao.getAllActiveWorkstations().first()
        println("DEBUG: Active workstations: ${allWorkstations.size}")
        allWorkstations.forEach { station ->
            println("DEBUG: Workstation: ${station.name} (ID: ${station.id}, Required: ${station.requiredWorkers}, Priority: ${station.isPriority})")
        }
        
        // Verificar asignaciones trabajador-estación
        var workersWithAssignments = 0
        var totalAssignments = 0
        
        for (worker in activeWorkers) {
            val assignments = workerDao.getWorkerWorkstationIds(worker.id)
            if (assignments.isNotEmpty()) {
                workersWithAssignments++
                totalAssignments += assignments.size
                println("DEBUG: Worker ${worker.name} assigned to ${assignments.size} stations: $assignments")
            } else {
                println("DEBUG: WARNING - Worker ${worker.name} has NO station assignments!")
            }
        }
        
        println("DEBUG: Workers with assignments: $workersWithAssignments/${activeWorkers.size}")
        println("DEBUG: Total assignments: $totalAssignments")
        println("DEBUG: ================================================")
    }
    
    /**
     * Verifica el estado del sistema de liderazgo.
     */
    private suspend fun verifyLeadershipSystem(activeWorkers: List<Worker>) {
        println("DEBUG: === VERIFICACIÓN SISTEMA DE LIDERAZGO ===")
        println("DEBUG: Rotación actual: ${getCurrentRotationHalf()}")
        
        val allLeaders = activeWorkers.filter { it.isLeader }
        println("DEBUG: Total líderes: ${allLeaders.size}")
        
        if (allLeaders.isEmpty()) {
            println("DEBUG: No hay líderes configurados en el sistema")
            return
        }
        
        val activeLeaders = allLeaders.filter { it.shouldBeLeaderInRotation(isFirstHalfRotation) }
        val inactiveLeaders = allLeaders.filter { !it.shouldBeLeaderInRotation(isFirstHalfRotation) }
        
        println("DEBUG: Líderes ACTIVOS para ${getCurrentRotationHalf()}: ${activeLeaders.size}")
        activeLeaders.forEach { leader ->
            println("DEBUG: - ${leader.name} (Estación: ${leader.leaderWorkstationId}, Tipo: ${leader.leadershipType})")
        }
        
        println("DEBUG: Líderes INACTIVOS para ${getCurrentRotationHalf()}: ${inactiveLeaders.size}")
        inactiveLeaders.forEach { leader ->
            println("DEBUG: - ${leader.name} (Estación: ${leader.leaderWorkstationId}, Tipo: ${leader.leadershipType})")
        }
        
        // Verificar conflictos de liderazgo (múltiples líderes para la misma estación)
        val leadersByStation = activeLeaders.groupBy { it.leaderWorkstationId }
        leadersByStation.forEach { (stationId, leaders) ->
            if (leaders.size > 1) {
                println("DEBUG: WARNING - Múltiples líderes activos para estación $stationId:")
                leaders.forEach { leader ->
                    println("DEBUG:   - ${leader.name} (${leader.leadershipType})")
                }
            }
        }
        
        println("DEBUG: ==========================================")
    }
    
    /**
     * Initializes assignment tracking maps for all workstations.
     */
    private fun initializeAssignments(workstations: List<Workstation>): MutableMap<Long, MutableList<Worker>> {
        return workstations.associate { it.id to mutableListOf<Worker>() }.toMutableMap()
    }

    /**
     * Assigns trainer-trainee pairs with ABSOLUTE PRIORITY, especially for priority workstations.
     * 
     * PRIORITY HIERARCHY:
     * 1. MAXIMUM PRIORITY: Trainer-trainee pairs in PRIORITY workstations
     * 2. HIGH PRIORITY: Trainer-trainee pairs in normal workstations
     * 3. All other assignments come after training relationships
     * 
     * This method ensures that training relationships ALWAYS take absolute priority over ALL other constraints:
     * - Ignores workstation capacity limits (will exceed if necessary for training)
     * - Ignores worker availability percentages (training is mandatory)
     * - Ignores workstation restrictions for trainers (they can work anywhere their trainee needs training)
     * - Guarantees that every trainee with an assigned trainer will be placed together
     * 
     * Training continuity is considered more important than operational efficiency constraints.
     */
    private fun assignTrainerTraineePairsWithPriority(
        eligibleWorkers: List<Worker>,
        assignments: MutableMap<Long, MutableList<Worker>>,
        allWorkstations: List<Workstation>,
        unassignedWorkers: MutableList<Worker>
    ) {
        val traineesWithTrainers = unassignedWorkers.filter { worker ->
            worker.isTrainee && worker.trainerId != null && worker.trainingWorkstationId != null
        }
        
        // Sort trainees by priority: priority workstations first, then normal workstations
        val sortedTrainees = traineesWithTrainers.sortedByDescending { trainee ->
            val trainingStation = allWorkstations.find { it.id == trainee.trainingWorkstationId }
            trainingStation?.isPriority ?: false
        }
        
        for (trainee in sortedTrainees) {
            val trainer = eligibleWorkers.find { it.id == trainee.trainerId }
            
            if (trainer != null && 
                unassignedWorkers.contains(trainer) && 
                unassignedWorkers.contains(trainee)) {
                
                val trainingStation = allWorkstations.find { it.id == trainee.trainingWorkstationId }
                
                trainingStation?.let { station ->
                    // FORCE assignment regardless of ALL constraints
                    // Priority workstations with training get ABSOLUTE priority
                    assignments[station.id]?.addAll(listOf(trainee, trainer))
                    unassignedWorkers.removeAll(listOf(trainee, trainer))
                    
                    // Log priority assignment for debugging
                    if (station.isPriority) {
                        // This is the HIGHEST priority assignment in the entire system
                    }
                }
            }
        }
    }

    /**
     * Legacy method for backward compatibility - now calls the priority-aware version.
     */
    private fun assignTrainerTraineePairs(
        eligibleWorkers: List<Worker>,
        assignments: MutableMap<Long, MutableList<Worker>>,
        allWorkstations: List<Workstation>,
        unassignedWorkers: MutableList<Worker>
    ) {
        assignTrainerTraineePairsWithPriority(eligibleWorkers, assignments, allWorkstations, unassignedWorkers)
    }
    
    /**
     * Assigns trainer-trainee pairs to the NEXT rotation with ABSOLUTE PRIORITY.
     * 
     * CRITICAL BUSINESS RULE: Trainer-trainee pairs MUST stay together in BOTH rotations
     * until the trainee is certified. This ensures continuous training and mentorship.
     * 
     * This function ensures that:
     * - Trainer and trainee are ALWAYS assigned to the SAME station in next rotation
     * - They are assigned to their designated training workstation
     * - Training continuity is maintained across rotation cycles
     * - No other constraints can separate a training pair
     */
    private fun assignTrainerTraineePairsToNextRotation(
        eligibleWorkers: List<Worker>,
        nextAssignments: MutableMap<Long, MutableList<Worker>>,
        allWorkstations: List<Workstation>,
        remainingWorkers: MutableList<Worker>
    ) {
        val traineesWithTrainers = remainingWorkers.filter { worker ->
            worker.isTrainee && worker.trainerId != null && worker.trainingWorkstationId != null && !worker.isCertified
        }
        
        // Sort trainees by priority: priority workstations first, then normal workstations
        val sortedTrainees = traineesWithTrainers.sortedByDescending { trainee ->
            val trainingStation = allWorkstations.find { it.id == trainee.trainingWorkstationId }
            trainingStation?.isPriority ?: false
        }
        
        for (trainee in sortedTrainees) {
            val trainer = eligibleWorkers.find { it.id == trainee.trainerId }
            
            if (trainer != null && 
                remainingWorkers.contains(trainer) && 
                remainingWorkers.contains(trainee)) {
                
                val trainingStation = allWorkstations.find { it.id == trainee.trainingWorkstationId }
                
                trainingStation?.let { station ->
                    // FORCE assignment to next rotation regardless of ALL constraints
                    // Training pairs have ABSOLUTE PRIORITY over capacity limits
                    nextAssignments[station.id]?.addAll(listOf(trainee, trainer))
                    remainingWorkers.removeAll(listOf(trainee, trainer))
                    
                    // CRITICAL: Training pairs override capacity limits
                    // If this causes the station to exceed capacity, that's acceptable
                    // because training continuity is more important than operational efficiency
                }
            }
        }
    }

    /**
     * Assigns workers to priority workstations ensuring full capacity.
     * Respects trainer-trainee pairs that may already be assigned.
     */
    private suspend fun assignPriorityWorkstations(
        priorityWorkstations: List<Workstation>,
        eligibleWorkers: List<Worker>,
        assignments: MutableMap<Long, MutableList<Worker>>
    ) {
        println("DEBUG: Assigning ${priorityWorkstations.size} priority workstations")
        
        for (station in priorityWorkstations) {
            val currentlyAssigned = assignments[station.id]?.size ?: 0
            val remainingCapacity = station.requiredWorkers - currentlyAssigned
            
            println("DEBUG: Priority station ${station.name}: ${currentlyAssigned}/${station.requiredWorkers} assigned, remaining capacity: $remainingCapacity")
            
            if (remainingCapacity > 0) {
                val availableWorkers = eligibleWorkers.filter { worker ->
                    val workerStations = getWorkerWorkstationIds(worker.id)
                    val canWorkHere = workerStations.contains(station.id)
                    val alreadyAssigned = assignments.values.any { it.contains(worker) }
                    
                    println("DEBUG: Worker ${worker.name} - can work at ${station.name}: $canWorkHere, already assigned: $alreadyAssigned")
                    
                    canWorkHere && !alreadyAssigned
                }
                
                println("DEBUG: Found ${availableWorkers.size} available workers for ${station.name}")
                
                // Separate leaders for this station considering leadership type and rotation half
                println("DEBUG: === ANÁLISIS DE LIDERAZGO PARA ${station.name} ===")
                println("DEBUG: Rotación actual: ${getCurrentRotationHalf()} (isFirstHalf: $isFirstHalfRotation)")
                
                val allLeadersForStation = availableWorkers.filter { worker ->
                    worker.isLeader && worker.leaderWorkstationId == station.id
                }
                
                println("DEBUG: Líderes totales para ${station.name}: ${allLeadersForStation.size}")
                allLeadersForStation.forEach { leader ->
                    val shouldBeActive = leader.shouldBeLeaderInRotation(isFirstHalfRotation)
                    println("DEBUG: - ${leader.name}: Tipo=${leader.leadershipType}, Activo=${shouldBeActive}")
                }
                
                // CRITICAL FIX: Separate BOTH leaders from other leaders
                val bothLeadersForStation = availableWorkers.filter { worker ->
                    worker.isLeader && 
                    worker.leaderWorkstationId == station.id &&
                    worker.leadershipType == "BOTH"
                }
                
                val activeLeadersForStation = availableWorkers.filter { worker ->
                    worker.isLeader && 
                    worker.leaderWorkstationId == station.id &&
                    worker.leadershipType != "BOTH" &&
                    worker.shouldBeLeaderInRotation(isFirstHalfRotation)
                }
                
                // Combine BOTH leaders (always active) with active leaders for this rotation
                val leadersForThisStation = bothLeadersForStation + activeLeadersForStation
                
                val otherWorkers = availableWorkers.filter { worker ->
                    !leadersForThisStation.contains(worker)
                }
                
                println("DEBUG: Líderes activos: ${leadersForThisStation.size}")
                println("DEBUG: Otros trabajadores: ${otherWorkers.size}")
                println("DEBUG: ==============================================")
                
                // Sort workers with leaders getting highest priority
                val sortedWorkers = (leadersForThisStation + otherWorkers.sortedWith(
                    compareByDescending<Worker> { it.isTrainer }
                        .thenByDescending { it.availabilityPercentage }
                ))
                
                val workersToAssign = minOf(remainingCapacity, sortedWorkers.size)
                val assignedWorkers = sortedWorkers.take(workersToAssign)
                
                assignments[station.id]?.addAll(assignedWorkers)
                
                println("DEBUG: Assigned ${assignedWorkers.size} workers to ${station.name}: ${assignedWorkers.map { it.name }}")
                if (leadersForThisStation.isNotEmpty()) {
                    println("DEBUG: Leaders assigned to ${station.name} for ${getCurrentRotationHalf()}: ${leadersForThisStation.map { "${it.name} (${it.getLeadershipTypeDescription()})" }}")
                }
                
                // Log leaders that were excluded due to leadership type
                val excludedLeaders = availableWorkers.filter { worker ->
                    worker.isLeader && 
                    worker.leaderWorkstationId == station.id &&
                    !worker.shouldBeLeaderInRotation(isFirstHalfRotation)
                }
                if (excludedLeaders.isNotEmpty()) {
                    println("DEBUG: Leaders excluded from ${station.name} for ${getCurrentRotationHalf()}: ${excludedLeaders.map { "${it.name} (${it.getLeadershipTypeDescription()})" }}")
                }
            }
        }
    }

    /**
     * Assigns remaining workers to normal workstations with availability checks.
     */
    private suspend fun assignNormalWorkstations(
        normalWorkstations: List<Workstation>,
        eligibleWorkers: List<Worker>,
        assignments: MutableMap<Long, MutableList<Worker>>
    ) {
        val unassignedWorkers = eligibleWorkers.filter { worker ->
            assignments.values.none { it.contains(worker) }
        }.toMutableList()
        
        println("DEBUG: Assigning ${normalWorkstations.size} normal workstations to ${unassignedWorkers.size} unassigned workers")
        
        // First assign trainer-trainee pairs
        assignTrainerTraineePairs(eligibleWorkers, assignments, normalWorkstations, unassignedWorkers)
        
        // Then handle forced rotation for trained workers
        assignTrainedWorkersWithRotation(normalWorkstations, unassignedWorkers, assignments)
        
        // Finally assign remaining workers with leadership priority considering rotation half
        println("DEBUG: === ASIGNACIÓN DE TRABAJADORES RESTANTES ===")
        println("DEBUG: Trabajadores sin asignar: ${unassignedWorkers.size}")
        
        // CRITICAL FIX: Include BOTH leaders always, plus active leaders for current rotation
        val bothLeaders = unassignedWorkers.filter { worker ->
            worker.isLeader && worker.leadershipType == "BOTH"
        }
        
        val activeLeaders = unassignedWorkers.filter { worker ->
            worker.isLeader && 
            worker.leadershipType != "BOTH" && 
            worker.shouldBeLeaderInRotation(isFirstHalfRotation)
        }
        
        val leadersToAssign = bothLeaders + activeLeaders
        val regularWorkers = unassignedWorkers.filter { worker ->
            !leadersToAssign.contains(worker)
        }
        
        println("DEBUG: Líderes activos sin asignar: ${leadersToAssign.size}")
        leadersToAssign.forEach { leader ->
            println("DEBUG: - ${leader.name} (Estación líder: ${leader.leaderWorkstationId}, Tipo: ${leader.leadershipType})")
        }
        
        val sortedWorkers = unassignedWorkers.sortedWith(
            compareByDescending<Worker> { worker ->
                // HIGHEST PRIORITY: BOTH leaders (always active) - BUT they should already be assigned in Phase 0.5
                if (worker.isLeader && worker.leadershipType == "BOTH") 4
                // HIGH PRIORITY: Active leaders for current rotation
                else if (worker.isLeader && worker.shouldBeLeaderInRotation(isFirstHalfRotation)) 3
                // MEDIUM PRIORITY: Trainers
                else if (worker.isTrainer) 2
                // NORMAL PRIORITY: Regular workers
                else 1
            }
                .thenByDescending { worker ->
                    worker.availabilityPercentage + Random.nextInt(0, 30)
                }
        )
        
        println("DEBUG: ==============================================")
        
        println("DEBUG: Attempting to assign ${sortedWorkers.size} remaining workers to normal stations")
        
        for (worker in sortedWorkers) {
            val availabilityRoll = Random.nextInt(1, 101)
            val isAvailable = availabilityRoll <= worker.availabilityPercentage
            
            println("DEBUG: Worker ${worker.name} availability check: $availabilityRoll <= ${worker.availabilityPercentage} = $isAvailable")
            
            if (isAvailable) {
                assignWorkerToOptimalStation(worker, normalWorkstations, assignments)
            } else {
                println("DEBUG: Worker ${worker.name} not available for this rotation (availability check failed)")
            }
        }
    }

    /**
     * Handles forced rotation for trained workers who have been in the same station too long.
     * Trained workers (not trainers, not trainees) must rotate after being in the same station
     * for half the rotation cycle to ensure skill development and prevent stagnation.
     */
    private suspend fun assignTrainedWorkersWithRotation(
        availableStations: List<Workstation>,
        unassignedWorkers: MutableList<Worker>,
        assignments: MutableMap<Long, MutableList<Worker>>
    ) {
        // Get trained workers who need to rotate (sorted by priority)
        val trainedWorkersNeedingRotation = unassignedWorkers
            .filter { it.isTrainedWorker() }
            .sortedByDescending { it.getRotationPriority() }
        
        for (worker in trainedWorkersNeedingRotation) {
            val workerStationIds = workerDao.getWorkerWorkstationIds(worker.id)
            val compatibleStations = availableStations.filter { station ->
                station.id in workerStationIds &&
                (assignments[station.id]?.size ?: 0) < station.requiredWorkers
            }
            
            val targetStation = if (worker.needsToRotate()) {
                // Force rotation: avoid current station if possible
                val alternativeStations = compatibleStations.filter { 
                    it.id != worker.currentWorkstationId 
                }
                
                if (alternativeStations.isNotEmpty()) {
                    // Assign to different station with least workers
                    alternativeStations.minByOrNull { assignments[it.id]?.size ?: 0 }
                } else {
                    // If no alternatives, assign to any compatible station
                    compatibleStations.minByOrNull { assignments[it.id]?.size ?: 0 }
                }
            } else {
                // Normal assignment for trained workers
                compatibleStations.minByOrNull { assignments[it.id]?.size ?: 0 }
            }
            
            targetStation?.let { station ->
                assignments[station.id]?.add(worker)
                unassignedWorkers.remove(worker)
                
                // Update rotation tracking
                updateWorkerRotationTracking(worker, station.id)
            }
        }
    }

    /**
     * Updates rotation tracking for a worker assigned to a station.
     */
    private suspend fun updateWorkerRotationTracking(worker: Worker, stationId: Long) {
        val currentTime = System.currentTimeMillis()
        workerDao.updateWorkerRotation(worker.id, stationId, currentTime)
    }

    /**
     * Assigns a worker to the most suitable available station.
     * CORRECCIÓN: Usa getWorkerWorkstationIds para obtener asignaciones actualizadas.
     */
    private suspend fun assignWorkerToOptimalStation(
        worker: Worker,
        availableStations: List<Workstation>,
        assignments: MutableMap<Long, MutableList<Worker>>
    ) {
        // CORRECCIÓN: Usar el método que maneja caché correctamente
        val workerStationIds = getWorkerWorkstationIds(worker.id)
        
        println("DEBUG: === ASIGNANDO TRABAJADOR A ESTACIÓN ÓPTIMA ===")
        println("DEBUG: Trabajador: ${worker.name} (ID: ${worker.id})")
        println("DEBUG: Estaciones asignadas al trabajador: $workerStationIds")
        println("DEBUG: Estaciones disponibles: ${availableStations.map { "${it.name}(${it.id})" }}")
        
        if (workerStationIds.isEmpty()) {
            println("DEBUG: ❌ ERROR - Trabajador ${worker.name} NO tiene estaciones asignadas!")
            println("DEBUG: Este trabajador no debería estar en la lista de elegibles")
            return
        }
        
        val compatibleStations = availableStations.filter { station ->
            val canWorkHere = station.id in workerStationIds
            val hasCapacity = (assignments[station.id]?.size ?: 0) < station.requiredWorkers
            val currentAssigned = assignments[station.id]?.size ?: 0
            
            println("DEBUG: Estación ${station.name} (ID: ${station.id}):")
            println("DEBUG:   - Puede trabajar aquí: $canWorkHere")
            println("DEBUG:   - Capacidad actual: $currentAssigned/${station.requiredWorkers}")
            println("DEBUG:   - Tiene espacio: $hasCapacity")
            
            canWorkHere && hasCapacity
        }
        
        println("DEBUG: Estaciones compatibles encontradas: ${compatibleStations.size}")
        compatibleStations.forEach { station ->
            println("DEBUG:   - ${station.name} (${assignments[station.id]?.size ?: 0}/${station.requiredWorkers})")
        }
        
        if (compatibleStations.isEmpty()) {
            println("DEBUG: ❌ ERROR - No hay estaciones compatibles para ${worker.name}")
            println("DEBUG: Posibles causas:")
            println("DEBUG:   1. Todas las estaciones asignadas están llenas")
            println("DEBUG:   2. El trabajador no está asignado a ninguna estación disponible")
            return
        }
        
        val targetStation = when {
            // HIGHEST PRIORITY: BOTH leaders - ALWAYS go to their leadership station
            worker.isLeader && worker.leaderWorkstationId != null && worker.leadershipType == "BOTH" -> {
                println("DEBUG: Trabajador ${worker.name} es líder BOTH (SIEMPRE ACTIVO)")
                println("DEBUG: Buscando estación de liderazgo ID: ${worker.leaderWorkstationId}")
                val leadershipStation = compatibleStations.find { it.id == worker.leaderWorkstationId }
                if (leadershipStation != null) {
                    println("DEBUG: ✅ Encontrada estación de liderazgo ${leadershipStation.name}")
                    leadershipStation
                } else {
                    println("DEBUG: ⚠️ Estación de liderazgo no disponible, usando estación con menos trabajadores")
                    compatibleStations.minByOrNull { assignments[it.id]?.size ?: 0 }
                }
            }
            // HIGH PRIORITY: Active leaders for current rotation half
            worker.isLeader && worker.leaderWorkstationId != null && worker.shouldBeLeaderInRotation(isFirstHalfRotation) -> {
                println("DEBUG: Trabajador ${worker.name} es líder ACTIVO para ${getCurrentRotationHalf()}")
                println("DEBUG: Buscando estación de liderazgo ID: ${worker.leaderWorkstationId}")
                val leadershipStation = compatibleStations.find { it.id == worker.leaderWorkstationId }
                if (leadershipStation != null) {
                    println("DEBUG: ✅ Encontrada estación de liderazgo ${leadershipStation.name}")
                    leadershipStation
                } else {
                    println("DEBUG: ⚠️ Estación de liderazgo no disponible, usando estación con menos trabajadores")
                    compatibleStations.minByOrNull { assignments[it.id]?.size ?: 0 }
                }
            }
            // INACTIVE leaders - assign to any compatible station
            worker.isLeader && worker.leaderWorkstationId != null && !worker.shouldBeLeaderInRotation(isFirstHalfRotation) -> {
                println("DEBUG: Trabajador ${worker.name} es líder INACTIVO para ${getCurrentRotationHalf()} (${worker.leadershipType})")
                compatibleStations.minByOrNull { assignments[it.id]?.size ?: 0 }
            }
            worker.isTrainee && worker.trainingWorkstationId != null -> {
                println("DEBUG: Trabajador ${worker.name} es entrenado, buscando estación de entrenamiento ID: ${worker.trainingWorkstationId}")
                val trainingStation = compatibleStations.find { it.id == worker.trainingWorkstationId }
                if (trainingStation != null) {
                    println("DEBUG: ✅ Encontrada estación de entrenamiento ${trainingStation.name}")
                    trainingStation
                } else {
                    println("DEBUG: ⚠️ Estación de entrenamiento no disponible, usando estación con menos trabajadores")
                    compatibleStations.minByOrNull { assignments[it.id]?.size ?: 0 }
                }
            }
            else -> {
                val selected = compatibleStations.minByOrNull { assignments[it.id]?.size ?: 0 }
                println("DEBUG: Trabajador regular, seleccionando estación con menos trabajadores: ${selected?.name}")
                selected
            }
        }
        
        targetStation?.let { station ->
            assignments[station.id]?.add(worker)
            println("DEBUG: ✅ ÉXITO - ${worker.name} asignado a ${station.name}")
            println("DEBUG: Nueva capacidad: ${assignments[station.id]?.size}/${station.requiredWorkers}")
            // Update rotation tracking for all workers
            updateWorkerRotationTracking(worker, station.id)
        } ?: run {
            println("DEBUG: ❌ ERROR CRÍTICO - No se pudo encontrar estación adecuada para ${worker.name}")
        }
        
        println("DEBUG: ===============================================")
    }

    /**
     * Assigns workers with forced rotation - ensures workers change stations between current and next rotation.
     * This is the core improvement that guarantees true rotation of personnel.
     * 
     * ROTATION RULES:
     * 1. Trainer-trainee pairs are EXEMPT from forced rotation (they stay together)
     * 2. All other workers MUST change stations between current and next rotation
     * 3. Priority stations maintain their capacity requirements
     * 4. Workers can only be assigned to stations they're qualified for
     * 5. If no alternative station is available, worker stays in current (exception case)
     */
    private suspend fun assignWorkersWithForcedRotation(
        eligibleWorkers: List<Worker>,
        currentAssignments: Map<Long, List<Worker>>,
        nextAssignments: MutableMap<Long, MutableList<Worker>>,
        allWorkstations: List<Workstation>,
        priorityWorkstations: List<Workstation>
    ) {
        // Get all workers who are already assigned in current rotation
        val workersInCurrentRotation = currentAssignments.values.flatten()
        
        // Separate workers by type
        val traineeWorkers = workersInCurrentRotation.filter { it.isTrainee && it.trainerId != null }
        val trainerWorkers = workersInCurrentRotation.filter { worker ->
            worker.isTrainer && traineeWorkers.any { trainee -> trainee.trainerId == worker.id }
        }
        val regularWorkers = workersInCurrentRotation.filter { worker ->
            !traineeWorkers.contains(worker) && !trainerWorkers.contains(worker)
        }
        
        // Phase 1: Handle priority stations first (maintain capacity)
        assignPriorityStationsWithRotation(
            priorityWorkstations,
            regularWorkers,
            currentAssignments,
            nextAssignments,
            allWorkstations
        )
        
        // Phase 2: Handle remaining regular workers with forced rotation
        assignRemainingWorkersWithRotation(
            regularWorkers,
            currentAssignments,
            nextAssignments,
            allWorkstations
        )
        
        // Phase 3: Fill any remaining capacity with unassigned workers
        fillRemainingCapacity(
            eligibleWorkers,
            nextAssignments,
            allWorkstations
        )
    }
    
    /**
     * Assigns workers to priority stations ensuring capacity while forcing rotation.
     */
    private suspend fun assignPriorityStationsWithRotation(
        priorityWorkstations: List<Workstation>,
        regularWorkers: List<Worker>,
        currentAssignments: Map<Long, List<Worker>>,
        nextAssignments: MutableMap<Long, MutableList<Worker>>,
        allWorkstations: List<Workstation>
    ) {
        for (station in priorityWorkstations) {
            val currentCapacity = nextAssignments[station.id]?.size ?: 0
            val remainingCapacity = station.requiredWorkers - currentCapacity
            
            if (remainingCapacity > 0) {
                // Get workers who can work at this station but are NOT currently assigned to it
                val eligibleWorkers = getEligibleWorkersForStation(regularWorkers, station.id)
                val availableWorkers = eligibleWorkers.filter { worker ->
                    // Worker is NOT currently assigned to this station
                    !isWorkerCurrentlyAssignedToStation(worker, station.id, currentAssignments) &&
                    // Worker is not already assigned to next rotation
                    !nextAssignments.values.any { it.contains(worker) }
                }
                
                // Sort by availability and trainer status
                val sortedWorkers = availableWorkers.sortedWith(
                    compareByDescending<Worker> { it.isTrainer }
                        .thenByDescending { it.availabilityPercentage }
                )
                
                // Assign workers up to remaining capacity
                val workersToAssign = sortedWorkers.take(remainingCapacity)
                nextAssignments[station.id]?.addAll(workersToAssign)
            }
        }
    }
    
    /**
     * Determines if a worker can rotate based on availability and general restrictions.
     * Workers who cannot rotate will stay in their current station.
     */
    private fun canWorkerRotate(worker: Worker): Boolean {
        // Only workers with very low availability (below 30%) should stay in place
        if (worker.availabilityPercentage < 30) return false
        
        // Most workers can rotate freely - specific station restrictions are handled separately
        return true
    }
    
    /**
     * Verifica si un trabajador puede trabajar en una estación específica.
     * Considera las restricciones específicas por estación.
     */
    private suspend fun canWorkerWorkAtStation(workerId: Long, workstationId: Long): Boolean {
        // Verificar si el trabajador tiene restricciones prohibitivas para esta estación
        val hasProhibitedRestriction = workerRestrictionDao.hasRestriction(
            workerId, 
            workstationId, 
            RestrictionType.PROHIBITED
        )
        
        if (hasProhibitedRestriction) {
            return false
        }
        
        // Verificar si el trabajador está asignado a esta estación
        val workerWorkstations = getWorkerWorkstationIds(workerId)
        return workerWorkstations.contains(workstationId)
    }
    
    /**
     * Obtiene los trabajadores elegibles para una estación específica.
     * Filtra por restricciones específicas y asignaciones de estación.
     * CORRECCIÓN: Asegura que se carguen las asignaciones desde BD si no están en caché.
     */
    private suspend fun getEligibleWorkersForStation(
        workers: List<Worker>, 
        workstationId: Long
    ): List<Worker> {
        val eligibleWorkers = mutableListOf<Worker>()
        
        for (worker in workers) {
            // Obtener estaciones asignadas al trabajador (desde caché o BD)
            val workerWorkstations = getWorkerWorkstationIds(worker.id)
            
            // Verificar si el trabajador puede trabajar en esta estación
            if (workerWorkstations.contains(workstationId)) {
                // Verificar restricciones adicionales
                val hasProhibitedRestriction = try {
                    workerRestrictionDao.hasRestriction(
                        worker.id, 
                        workstationId, 
                        RestrictionType.PROHIBITED
                    )
                } catch (e: Exception) {
                    println("DEBUG: Error verificando restricciones para worker ${worker.id}: ${e.message}")
                    false
                }
                
                if (!hasProhibitedRestriction) {
                    eligibleWorkers.add(worker)
                    println("DEBUG: Worker ${worker.name} es elegible para estación $workstationId")
                } else {
                    println("DEBUG: Worker ${worker.name} tiene restricción prohibitiva para estación $workstationId")
                }
            } else {
                println("DEBUG: Worker ${worker.name} NO está asignado a estación $workstationId (asignado a: $workerWorkstations)")
            }
        }
        
        return eligibleWorkers
    }

    /**
     * Assigns remaining workers ensuring they rotate to different stations.
     * Workers who cannot rotate stay in their current station.
     */
    private suspend fun assignRemainingWorkersWithRotation(
        regularWorkers: List<Worker>,
        currentAssignments: Map<Long, List<Worker>>,
        nextAssignments: MutableMap<Long, MutableList<Worker>>,
        allWorkstations: List<Workstation>
    ) {
        // Get workers not yet assigned to next rotation
        val unassignedWorkers = regularWorkers.filter { worker ->
            !nextAssignments.values.any { it.contains(worker) }
        }
        
        for (worker in unassignedWorkers) {
            // Find current station of this worker
            val currentStationId = findWorkerCurrentStation(worker, currentAssignments)
            
            // Check if worker can rotate (availability >= 50%, no restrictions, not trainer/trainee)
            if (!canWorkerRotate(worker)) {
                // Worker cannot rotate - keep in current station for safety and continuity
                var workerAssigned = false
                
                currentStationId?.let { stationId ->
                    val currentStation = allWorkstations.find { it.id == stationId }
                    currentStation?.let { station ->
                        // Check if current station has capacity
                        if ((nextAssignments[station.id]?.size ?: 0) < station.requiredWorkers) {
                            nextAssignments[station.id]?.add(worker)
                            updateWorkerRotationTracking(worker, station.id)
                            workerAssigned = true
                        }
                    }
                }
                
                // If current station is full, try to find any available station
                if (!workerAssigned) {
                    val qualifiedStationIds = workerDao.getWorkerWorkstationIds(worker.id)
                    val availableStation = allWorkstations.find { station ->
                        station.id in qualifiedStationIds &&
                        (nextAssignments[station.id]?.size ?: 0) < station.requiredWorkers
                    }
                    
                    availableStation?.let { station ->
                        nextAssignments[station.id]?.add(worker)
                        updateWorkerRotationTracking(worker, station.id)
                    }
                }
            } else {
                // Worker can rotate - proceed with normal rotation logic
                val qualifiedStationIds = workerDao.getWorkerWorkstationIds(worker.id)
                val qualifiedStations = allWorkstations.filter { it.id in qualifiedStationIds }
                
                // Find alternative stations (different from current)
                val alternativeStations = qualifiedStations.filter { station ->
                    station.id != currentStationId &&
                    (nextAssignments[station.id]?.size ?: 0) < station.requiredWorkers
                }
                
                // Assign to best alternative station
                val targetStation = if (alternativeStations.isNotEmpty()) {
                    // Prefer stations with fewer assigned workers
                    alternativeStations.minByOrNull { nextAssignments[it.id]?.size ?: 0 }
                } else {
                    // Fallback: if no alternatives, find any available station (including current)
                    qualifiedStations.find { station ->
                        (nextAssignments[station.id]?.size ?: 0) < station.requiredWorkers
                    }
                }
                
                targetStation?.let { station ->
                    nextAssignments[station.id]?.add(worker)
                    
                    // Update rotation tracking
                    updateWorkerRotationTracking(worker, station.id)
                }
            }
        }
    }
    
    /**
     * Fills remaining capacity with any unassigned eligible workers.
     */
    private suspend fun fillRemainingCapacity(
        eligibleWorkers: List<Worker>,
        nextAssignments: MutableMap<Long, MutableList<Worker>>,
        allWorkstations: List<Workstation>
    ) {
        // Get workers not assigned to any rotation yet
        val completelyUnassigned = eligibleWorkers.filter { worker ->
            !nextAssignments.values.any { it.contains(worker) }
        }
        
        for (worker in completelyUnassigned) {
            // Find stations with available capacity
            val qualifiedStationIds = workerDao.getWorkerWorkstationIds(worker.id)
            val availableStations = allWorkstations.filter { station ->
                station.id in qualifiedStationIds &&
                (nextAssignments[station.id]?.size ?: 0) < station.requiredWorkers
            }
            
            // Assign to station with least workers
            val targetStation = availableStations.minByOrNull { 
                nextAssignments[it.id]?.size ?: 0 
            }
            
            targetStation?.let { station ->
                nextAssignments[station.id]?.add(worker)
                updateWorkerRotationTracking(worker, station.id)
            }
        }
    }
    
    /**
     * Checks if a worker is currently assigned to a specific station.
     */
    private fun isWorkerCurrentlyAssignedToStation(
        worker: Worker,
        stationId: Long,
        currentAssignments: Map<Long, List<Worker>>
    ): Boolean {
        return currentAssignments[stationId]?.contains(worker) ?: false
    }
    
    /**
     * Finds the current station ID for a worker.
     */
    private fun findWorkerCurrentStation(
        worker: Worker,
        currentAssignments: Map<Long, List<Worker>>
    ): Long? {
        return currentAssignments.entries
            .find { it.value.contains(worker) }
            ?.key
    }
    
    /**
     * Executes the main rotation algorithm with all business logic.
     * Returns both the rotation items list and the rotation table.
     */
    private suspend fun executeRotationAlgorithm(data: RotationData): Pair<List<RotationItem>, RotationTable> {
        val (eligibleWorkers, allWorkstations) = data
        
        // Initialize assignment tracking
        val currentAssignments = initializeAssignments(allWorkstations)
        val nextAssignments = initializeAssignments(allWorkstations)
        
        // Separate workstation types
        val (priorityWorkstations, normalWorkstations) = allWorkstations.partition { it.isPriority }
        
        // PHASE 0: ABSOLUTE PRIORITY - Assign ALL trainer-trainee pairs FIRST
        // This happens BEFORE any other assignment, including priority workstations
        val allUnassignedWorkers = eligibleWorkers.toMutableList()
        assignTrainerTraineePairsWithPriority(eligibleWorkers, currentAssignments, allWorkstations, allUnassignedWorkers)
        
        // PHASE 0.5: CRITICAL FIX - Force assign BOTH leaders to current rotation
        // This ensures BOTH leaders are ALWAYS in their station in CURRENT rotation
        assignBothLeadersToCurrentRotation(eligibleWorkers, currentAssignments, allWorkstations)
            
        // Phase 1: Assign remaining workers to PRIORITY workstations (current positions)
        assignPriorityWorkstations(priorityWorkstations, eligibleWorkers, currentAssignments)
        
        // Phase 2: Assign remaining workers to NORMAL workstations (current positions)
        assignNormalWorkstations(normalWorkstations, eligibleWorkers, currentAssignments)
        
        // Phase 3: Generate next rotation - SIMPLIFIED APPROACH
        // Always generate a next rotation by rotating workers to different stations
        generateNextRotationSimple(eligibleWorkers, currentAssignments, nextAssignments, allWorkstations)
        
        // Phase 4: Create rotation items and table
        val rotationItems = createRotationItems(allWorkstations, currentAssignments, nextAssignments)
        val rotationTable = createRotationTable(allWorkstations, currentAssignments, nextAssignments)
        
        return Pair(rotationItems, rotationTable)
    }

    /**
     * Assigns BOTH type leaders to their designated stations with ABSOLUTE PRIORITY.
     * This ensures BOTH leaders are ALWAYS in their station in CURRENT rotation.
     * CRITICAL FIX: Addresses the issue where BOTH leaders were not guaranteed in current rotation.
     */
    private suspend fun assignBothLeadersToCurrentRotation(
        eligibleWorkers: List<Worker>,
        currentAssignments: MutableMap<Long, MutableList<Worker>>,
        allWorkstations: List<Workstation>
    ) {
        println("DEBUG: === ASIGNACIÓN FORZADA DE LÍDERES BOTH (ROTACIÓN ACTUAL) ===")
        println("DEBUG: Rotación actual: ${getCurrentRotationHalf()}")
        
        val bothTypeLeaders = eligibleWorkers.filter { 
            it.isLeader && it.leadershipType == "BOTH" && it.leaderWorkstationId != null 
        }
        
        println("DEBUG: Líderes BOTH encontrados: ${bothTypeLeaders.size}")
        
        for (bothLeader in bothTypeLeaders) {
            bothLeader.leaderWorkstationId?.let { leaderStationId ->
                val leaderStation = allWorkstations.find { it.id == leaderStationId }
                leaderStation?.let { station ->
                    // Check if leader is already assigned
                    val alreadyAssigned = currentAssignments.values.any { it.contains(bothLeader) }
                    
                    if (!alreadyAssigned) {
                        // FORCE assignment - BOTH leaders override capacity limits
                        currentAssignments[station.id]?.add(bothLeader)
                        println("DEBUG: ✅ Líder BOTH ${bothLeader.name} FORZADO en ${station.name} (ROTACIÓN ACTUAL)")
                        println("DEBUG: Nueva capacidad: ${currentAssignments[station.id]?.size}/${station.requiredWorkers}")
                        
                        // Log if we exceeded capacity (this is acceptable for BOTH leaders)
                        if ((currentAssignments[station.id]?.size ?: 0) > station.requiredWorkers) {
                            println("DEBUG: ⚠️ CAPACIDAD EXCEDIDA en ${station.name} por líder BOTH (ACEPTABLE)")
                        }
                    } else {
                        println("DEBUG: ✅ Líder BOTH ${bothLeader.name} ya asignado en rotación actual")
                    }
                } ?: run {
                    println("DEBUG: ❌ ERROR - Estación de liderazgo ${leaderStationId} no encontrada para ${bothLeader.name}")
                }
            }
        }
        
        println("DEBUG: ========================================================")
    }

    /**
     * Generates next rotation with simple rotation logic - ALWAYS generates a next rotation.
     * CRITICAL: Ensures trainer-trainee pairs stay together in BOTH rotations.
     */
    private suspend fun generateNextRotationSimple(
        eligibleWorkers: List<Worker>,
        currentAssignments: Map<Long, List<Worker>>,
        nextAssignments: MutableMap<Long, MutableList<Worker>>,
        allWorkstations: List<Workstation>
    ) {
        // PHASE 0: ABSOLUTE PRIORITY - Assign trainer-trainee pairs FIRST in next rotation
        // This ensures they stay together in BOTH current AND next rotations
        val remainingWorkers = eligibleWorkers.toMutableList()
        assignTrainerTraineePairsToNextRotation(eligibleWorkers, nextAssignments, allWorkstations, remainingWorkers)
        
        // Get all workers currently assigned (excluding those already assigned as pairs)
        val allCurrentWorkers = currentAssignments.values.flatten()
        val workersToRotate = allCurrentWorkers.filter { remainingWorkers.contains(it) }
        
        println("DEBUG: === ASIGNANDO LÍDERES A PRÓXIMA ROTACIÓN ===")
        println("DEBUG: Rotación actual: ${getCurrentRotationHalf()}")
        
        // CRITICAL: Handle ALL leaders, but with different priorities
        val allLeaders = workersToRotate.filter { it.isLeader }
        
        // HIGHEST PRIORITY: Leaders type "BOTH" - MUST be in their station in BOTH rotations
        val bothTypeLeaders = allLeaders.filter { it.leadershipType == "BOTH" }
        println("DEBUG: Líderes tipo BOTH (máxima prioridad): ${bothTypeLeaders.size}")
        
        for (bothLeader in bothTypeLeaders) {
            bothLeader.leaderWorkstationId?.let { leaderStationId ->
                val leaderStation = allWorkstations.find { it.id == leaderStationId }
                leaderStation?.let { station ->
                    // FORCE assignment - BOTH leaders MUST be in their station
                    if (nextAssignments[station.id]?.contains(bothLeader) != true) {
                        // For BOTH leaders, we FORCE assignment even if station is full
                        nextAssignments[station.id]?.add(bothLeader)
                        println("DEBUG: ✅ Líder BOTH ${bothLeader.name} FORZADO en ${station.name} (AMBAS ROTACIONES)")
                    } else {
                        println("DEBUG: ✅ Líder BOTH ${bothLeader.name} ya asignado en ${station.name}")
                    }
                }
            }
        }
        
        // HIGH PRIORITY: Leaders active in current rotation half
        val activeLeaders = allLeaders.filter { leader ->
            leader.leadershipType != "BOTH" && leader.shouldBeLeaderInRotation(isFirstHalfRotation)
        }
        println("DEBUG: Líderes activos para ${getCurrentRotationHalf()}: ${activeLeaders.size}")
        
        for (leader in activeLeaders) {
            leader.leaderWorkstationId?.let { leaderStationId ->
                val leaderStation = allWorkstations.find { it.id == leaderStationId }
                leaderStation?.let { station ->
                    if (nextAssignments[station.id]?.contains(leader) != true) {
                        if ((nextAssignments[station.id]?.size ?: 0) < station.requiredWorkers) {
                            nextAssignments[station.id]?.add(leader)
                            println("DEBUG: ✅ Líder ${leader.name} (${leader.leadershipType}) asignado a ${station.name}")
                        } else {
                            println("DEBUG: ⚠️ Estación ${station.name} llena para líder ${leader.name}")
                        }
                    }
                }
            }
        }
        
        // Log inactive leaders for debugging
        val inactiveLeaders = allLeaders.filter { leader ->
            leader.leadershipType != "BOTH" && !leader.shouldBeLeaderInRotation(isFirstHalfRotation)
        }
        if (inactiveLeaders.isNotEmpty()) {
            println("DEBUG: Líderes INACTIVOS para ${getCurrentRotationHalf()}: ${inactiveLeaders.size}")
            inactiveLeaders.forEach { leader ->
                println("DEBUG: - ${leader.name} (${leader.leadershipType}) - NO asignado")
            }
        }
        
        // Remove assigned leaders from workers to rotate
        val assignedLeaders = nextAssignments.values.flatten().filter { it.isLeader }.toSet()
        val remainingWorkersToRotate = workersToRotate.filter { !assignedLeaders.contains(it) }
        
        // For each remaining worker, try to assign them to a different station
        for (worker in remainingWorkersToRotate) {
            val currentStationId = findWorkerCurrentStation(worker, currentAssignments)
            val qualifiedStationIds = getWorkerWorkstationIds(worker.id)
            val qualifiedStations = allWorkstations.filter { it.id in qualifiedStationIds }
            
            // Try to find a different station first (for rotation)
            val alternativeStations = qualifiedStations.filter { station ->
                station.id != currentStationId &&
                (nextAssignments[station.id]?.size ?: 0) < station.requiredWorkers
            }
            
            val targetStation = if (alternativeStations.isNotEmpty()) {
                // Prefer stations with fewer workers assigned
                alternativeStations.minByOrNull { nextAssignments[it.id]?.size ?: 0 }
            } else {
                // If no alternatives, find any available station
                qualifiedStations.find { station ->
                    (nextAssignments[station.id]?.size ?: 0) < station.requiredWorkers
                }
            }
            
            targetStation?.let { station ->
                nextAssignments[station.id]?.add(worker)
            }
        }
        
        // Fill remaining capacity with any unassigned workers
        val assignedInNext = nextAssignments.values.flatten().toSet()
        val unassignedWorkers = eligibleWorkers.filter { !assignedInNext.contains(it) }
        
        for (worker in unassignedWorkers) {
            val qualifiedStationIds = getWorkerWorkstationIds(worker.id)
            val availableStation = allWorkstations.find { station ->
                station.id in qualifiedStationIds &&
                (nextAssignments[station.id]?.size ?: 0) < station.requiredWorkers
            }
            
            availableStation?.let { station ->
                nextAssignments[station.id]?.add(worker)
            }
        }
    }

    /**
     * Generates a simple next rotation when the complex algorithm fails to assign workers.
     * This ensures that there's always a next rotation generated.
     */
    private suspend fun generateSimpleNextRotation(
        eligibleWorkers: List<Worker>,
        currentAssignments: Map<Long, List<Worker>>,
        nextAssignments: MutableMap<Long, MutableList<Worker>>,
        allWorkstations: List<Workstation>
    ) {
        // Get all workers currently assigned
        val allCurrentWorkers = currentAssignments.values.flatten()
        
        // For each worker, try to assign them to a different station
        for (worker in allCurrentWorkers) {
            val currentStationId = findWorkerCurrentStation(worker, currentAssignments)
            val qualifiedStationIds = workerDao.getWorkerWorkstationIds(worker.id)
            val qualifiedStations = allWorkstations.filter { it.id in qualifiedStationIds }
            
            // Try to find a different station first
            val alternativeStations = qualifiedStations.filter { station ->
                station.id != currentStationId &&
                (nextAssignments[station.id]?.size ?: 0) < station.requiredWorkers
            }
            
            val targetStation = if (alternativeStations.isNotEmpty()) {
                // Assign to different station with least workers
                alternativeStations.minByOrNull { nextAssignments[it.id]?.size ?: 0 }
            } else {
                // If no alternatives, assign to any available station
                qualifiedStations.find { station ->
                    (nextAssignments[station.id]?.size ?: 0) < station.requiredWorkers
                }
            }
            
            targetStation?.let { station ->
                nextAssignments[station.id]?.add(worker)
            }
        }
        
        // Fill remaining capacity with unassigned workers
        val unassignedWorkers = eligibleWorkers.filter { worker ->
            !nextAssignments.values.any { it.contains(worker) }
        }
        
        for (worker in unassignedWorkers) {
            val qualifiedStationIds = workerDao.getWorkerWorkstationIds(worker.id)
            val availableStation = allWorkstations.find { station ->
                station.id in qualifiedStationIds &&
                (nextAssignments[station.id]?.size ?: 0) < station.requiredWorkers
            }
            
            availableStation?.let { station ->
                nextAssignments[station.id]?.add(worker)
            }
        }
    }

    /**
     * Creates rotation items for display.
     */
    private fun createRotationItems(
        allWorkstations: List<Workstation>,
        currentAssignments: Map<Long, List<Worker>>,
        nextAssignments: Map<Long, List<Worker>>
    ): List<RotationItem> {
        val rotationItems = mutableListOf<RotationItem>()
        
        for (station in allWorkstations) {
            val currentWorkers = currentAssignments[station.id] ?: emptyList()
            
            for (worker in currentWorkers) {
                val nextStation = findNextStation(worker, nextAssignments, allWorkstations) ?: station
                val workerLabel = createWorkerLabel(worker, station, nextStation, currentWorkers)
                val currentInfo = createStationInfo(station, currentWorkers)
                val nextInfo = createStationInfo(nextStation, nextAssignments[nextStation.id] ?: emptyList())
                
                rotationItems.add(
                    RotationItem(
                        workerName = workerLabel,
                        currentWorkstation = currentInfo,
                        nextWorkstation = nextInfo,
                        rotationOrder = rotationItems.size + 1
                    )
                )
            }
        }
        
        return rotationItems.sortedBy { it.rotationOrder }
    }

    /**
     * Finds the next station for a worker.
     */
    private fun findNextStation(
        worker: Worker,
        nextAssignments: Map<Long, List<Worker>>,
        allWorkstations: List<Workstation>
    ): Workstation? {
        return nextAssignments.entries
            .find { it.value.contains(worker) }
            ?.let { entry -> allWorkstations.find { it.id == entry.key } }
    }

    /**
     * Creates a formatted worker label with status indicators including rotation status.
     */
    private fun createWorkerLabel(
        worker: Worker,
        currentStation: Workstation,
        nextStation: Workstation,
        currentWorkers: List<Worker>
    ): String {
        val isPriorityWorker = currentStation.isPriority || nextStation.isPriority
        val availabilityStatus = getAvailabilityStatus(worker)
        val restrictionStatus = if (worker.restrictionNotes.isNotEmpty()) " 🔒" else ""
        val trainingStatus = getTrainingStatus(worker, currentWorkers)
        val leadershipStatus = getLeadershipStatus(worker, currentStation, nextStation)
        val rotationStatus = getRotationStatus(currentStation, nextStation, worker)
        
        val baseName = if (isPriorityWorker) {
            "${worker.name} [PRIORITARIO]"
        } else {
            worker.name
        }
        
        return "$baseName$availabilityStatus$restrictionStatus$trainingStatus$leadershipStatus$rotationStatus"
    }
    
    /**
     * Gets leadership status indicator for workers who are leaders.
     */
    private fun getLeadershipStatus(
        worker: Worker,
        currentStation: Workstation,
        nextStation: Workstation
    ): String {
        if (!worker.isLeader || worker.leaderWorkstationId == null) return ""
        
        val isLeaderInCurrent = worker.leaderWorkstationId == currentStation.id
        val isLeaderInNext = worker.leaderWorkstationId == nextStation.id
        val shouldBeActiveNow = worker.shouldBeLeaderInRotation(isFirstHalfRotation)
        
        val leadershipTypeIndicator = when (worker.leadershipType) {
            "BOTH" -> "👑"
            "FIRST_HALF" -> if (isFirstHalfRotation) "👑" else "⏸️"
            "SECOND_HALF" -> if (!isFirstHalfRotation) "👑" else "⏸️"
            else -> "❓"
        }
        
        return when {
            isLeaderInCurrent && isLeaderInNext && shouldBeActiveNow -> " $leadershipTypeIndicator [LÍDER ACTIVO - ${getCurrentRotationHalfDescription()}]"
            isLeaderInCurrent && shouldBeActiveNow -> " $leadershipTypeIndicator [LÍDER ACTUAL - ${getCurrentRotationHalfDescription()}]"
            isLeaderInNext && shouldBeActiveNow -> " $leadershipTypeIndicator [LÍDER SIGUIENTE - ${getCurrentRotationHalfDescription()}]"
            worker.isLeader && !shouldBeActiveNow -> " ⏸️ [LÍDER INACTIVO - ${worker.getLeadershipTypeDescription()}]"
            worker.isLeader && shouldBeActiveNow && !isLeaderInCurrent && !isLeaderInNext -> " 👑⚠️ [LÍDER SIN ESTACIÓN ASIGNADA]"
            else -> ""
        }
    }
    
    /**
     * Gets rotation status indicator showing if worker is rotating or staying.
     */
    private fun getRotationStatus(
        currentStation: Workstation,
        nextStation: Workstation,
        worker: Worker
    ): String {
        return when {
            // Trainer-trainee pairs are exempt from rotation indicators
            worker.isTrainee && worker.trainerId != null -> ""
            worker.isTrainer -> ""
            // Leaders get special rotation status
            worker.isLeader -> ""
            // Show rotation status for regular workers
            currentStation.id != nextStation.id -> " 🔄 [ROTANDO]"
            else -> " 📍 [PERMANECE]"
        }
    }

    /**
     * Gets availability status indicator for a worker.
     */
    private fun getAvailabilityStatus(worker: Worker): String {
        return when {
            worker.availabilityPercentage >= 80 -> ""
            worker.availabilityPercentage >= 50 -> " [${worker.availabilityPercentage}%]"
            else -> " [${worker.availabilityPercentage}% ⚠️]"
        }
    }

    /**
     * Gets training status indicator for a worker.
     */
    private fun getTrainingStatus(worker: Worker, currentWorkers: List<Worker>): String {
        return when {
            worker.isTrainer -> {
                val hasTraineeInSameStation = currentWorkers.any { otherWorker ->
                    otherWorker.isTrainee && otherWorker.trainerId == worker.id
                }
                if (hasTraineeInSameStation) " 👨‍🏫🤝 [ENTRENANDO]" else " 👨‍🏫"
            }
            worker.isTrainee -> {
                val hasTrainerInSameStation = worker.trainerId != null && 
                    currentWorkers.any { otherWorker -> otherWorker.id == worker.trainerId }
                if (hasTrainerInSameStation) " 🎯🤝 [EN ENTRENAMIENTO]" else " 🎯"
            }
            else -> ""
        }
    }

    /**
     * Creates formatted station information.
     */
    private fun createStationInfo(station: Workstation, workers: List<Worker>): String {
        val baseInfo = "${station.name} (${workers.size}/${station.requiredWorkers})"
        return if (station.isPriority) "$baseInfo ⭐ COMPLETA" else baseInfo
    }

    /**
     * Creates the rotation table for display.
     */
    private fun createRotationTable(
        allWorkstations: List<Workstation>,
        currentAssignments: Map<Long, List<Worker>>,
        nextAssignments: Map<Long, List<Worker>>
    ): RotationTable {
        // Debug: Verificar asignaciones finales
        println("DEBUG: ===== ASIGNACIONES FINALES =====")
        
        var totalCurrentWorkers = 0
        var totalNextWorkers = 0
        
        allWorkstations.forEach { station ->
            val currentWorkers = currentAssignments[station.id] ?: emptyList()
            val nextWorkers = nextAssignments[station.id] ?: emptyList()
            
            totalCurrentWorkers += currentWorkers.size
            totalNextWorkers += nextWorkers.size
            
            println("DEBUG: ${station.name}:")
            println("DEBUG:   Current: ${currentWorkers.size}/${station.requiredWorkers} - ${currentWorkers.map { it.name }}")
            println("DEBUG:   Next: ${nextWorkers.size}/${station.requiredWorkers} - ${nextWorkers.map { it.name }}")
        }
        
        println("DEBUG: Total workers assigned - Current: $totalCurrentWorkers, Next: $totalNextWorkers")
        println("DEBUG: ================================")
        
        // Mostrar resumen del sistema de liderazgo
        showLeadershipSummary(allWorkstations, currentAssignments, nextAssignments)
        
        return RotationTable(
            workstations = allWorkstations,
            currentPhase = currentAssignments.mapValues { it.value.toList() },
            nextPhase = nextAssignments.mapValues { it.value.toList() }
        )
    }
    
    /**
     * Muestra un resumen del sistema de liderazgo después de generar la rotación.
     */
    private fun showLeadershipSummary(
        allWorkstations: List<Workstation>,
        currentAssignments: Map<Long, List<Worker>>,
        nextAssignments: Map<Long, List<Worker>>
    ) {
        println("DEBUG: === RESUMEN SISTEMA DE LIDERAZGO ===")
        println("DEBUG: Rotación: ${getCurrentRotationHalfDescription()}")
        
        val allAssignedWorkers = (currentAssignments.values.flatten() + nextAssignments.values.flatten()).distinct()
        val assignedLeaders = allAssignedWorkers.filter { it.isLeader }
        
        if (assignedLeaders.isEmpty()) {
            println("DEBUG: No hay líderes asignados en esta rotación")
            return
        }
        
        println("DEBUG: Líderes en rotación actual:")
        currentAssignments.forEach { (stationId, workers) ->
            val station = allWorkstations.find { it.id == stationId }
            val leadersInStation = workers.filter { it.isLeader }
            
            if (leadersInStation.isNotEmpty()) {
                println("DEBUG: Estación ${station?.name}:")
                leadersInStation.forEach { leader ->
                    val isActive = leader.shouldBeLeaderInRotation(isFirstHalfRotation)
                    val isCorrectStation = leader.leaderWorkstationId == stationId
                    val status = when {
                        isActive && isCorrectStation -> "✅ CORRECTO"
                        isActive && !isCorrectStation -> "⚠️ ESTACIÓN INCORRECTA"
                        !isActive -> "⏸️ INACTIVO"
                        else -> "❓ DESCONOCIDO"
                    }
                    println("DEBUG:   - ${leader.name} (${leader.leadershipType}) $status")
                }
            }
        }
        
        println("DEBUG: Líderes en próxima rotación:")
        nextAssignments.forEach { (stationId, workers) ->
            val station = allWorkstations.find { it.id == stationId }
            val leadersInStation = workers.filter { it.isLeader }
            
            if (leadersInStation.isNotEmpty()) {
                println("DEBUG: Estación ${station?.name}:")
                leadersInStation.forEach { leader ->
                    val isActive = leader.shouldBeLeaderInRotation(isFirstHalfRotation)
                    val isCorrectStation = leader.leaderWorkstationId == stationId
                    val status = when {
                        isActive && isCorrectStation -> "✅ CORRECTO"
                        isActive && !isCorrectStation -> "⚠️ ESTACIÓN INCORRECTA"
                        !isActive -> "⏸️ INACTIVO"
                        else -> "❓ DESCONOCIDO"
                    }
                    println("DEBUG:   - ${leader.name} (${leader.leadershipType}) $status")
                }
            }
        }
        
        println("DEBUG: ======================================")
    }
    
    /**
     * Updates the count of eligible workers asynchronously.
     */
    fun updateEligibleWorkersCount() {
        viewModelScope.launch {
            eligibleWorkersCount = try {
                val activeWorkers = workerDao.getAllWorkers().first().filter { it.isActive }
                activeWorkers.count { worker ->
                    workerDao.getWorkerWorkstationIds(worker.id).isNotEmpty()
                }
            } catch (e: Exception) {
                0
            }
        }
    }
    
    /**
     * Validates that all priority stations have their required capacity.
     */
    private fun validatePriorityStationsCapacity(
        priorityStations: List<Workstation>,
        assignments: Map<Long, List<Worker>>
    ): Boolean {
        return priorityStations.all { station ->
            (assignments[station.id]?.size ?: 0) == station.requiredWorkers
        }
    }
    
    fun clearRotation() {
        _rotationItems.value = emptyList()
        _rotationTable.value = null
    }
    
    fun getEligibleWorkersCount(): Int {
        return eligibleWorkersCount
    }
    
    /**
     * Método de testing para verificar si un trabajador específico aparecerá en rotaciones.
     * MEJORADO: Proporciona información más detallada sobre asignaciones y problemas.
     */
    suspend fun debugWorkerEligibilityForRotation(workerId: Long): String {
        println("DEBUG: === VERIFICANDO ELEGIBILIDAD PARA ROTACIÓN ===")
        
        val worker = workerDao.getWorkerById(workerId)
        if (worker == null) {
            return "❌ ERROR: Trabajador no encontrado con ID: $workerId"
        }
        
        val workstationIds = getWorkerWorkstationIds(workerId)
        val allWorkstations = workstationDao.getAllActiveWorkstations().first()
        val assignedWorkstations = allWorkstations.filter { workstationIds.contains(it.id) }
        
        val report = StringBuilder()
        report.append("🔍 DIAGNÓSTICO DE ELEGIBILIDAD PARA ROTACIÓN\n")
        report.append("=" .repeat(50) + "\n")
        report.append("👤 INFORMACIÓN DEL TRABAJADOR:\n")
        report.append("   Nombre: ${worker.name}\n")
        report.append("   ID: ${worker.id}\n")
        report.append("   Estado: ${if (worker.isActive) "✅ Activo" else "❌ Inactivo"}\n")
        report.append("   Disponibilidad: ${worker.availabilityPercentage}%\n")
        
        report.append("\n🎭 ROLES Y CARACTERÍSTICAS:\n")
        report.append("   Es entrenador: ${if (worker.isTrainer) "✅ Sí" else "❌ No"}\n")
        report.append("   Es entrenado: ${if (worker.isTrainee) "✅ Sí" else "❌ No"}\n")
        report.append("   Es líder: ${if (worker.isLeader) "✅ Sí (${worker.leadershipType})" else "❌ No"}\n")
        report.append("   Está certificado: ${if (worker.isCertified) "✅ Sí" else "❌ No"}\n")
        
        if (worker.isTrainee && worker.trainerId != null) {
            val trainer = workerDao.getWorkerById(worker.trainerId!!)
            report.append("   Entrenador asignado: ${trainer?.name ?: "Desconocido"} (ID: ${worker.trainerId})\n")
            report.append("   Estación de entrenamiento: ${worker.trainingWorkstationId}\n")
        }
        
        if (worker.isLeader && worker.leaderWorkstationId != null) {
            val leaderStation = allWorkstations.find { it.id == worker.leaderWorkstationId }
            report.append("   Estación de liderazgo: ${leaderStation?.name ?: "Desconocida"} (ID: ${worker.leaderWorkstationId})\n")
        }
        
        report.append("\n🏭 ASIGNACIONES DE ESTACIONES:\n")
        report.append("   Total estaciones asignadas: ${workstationIds.size}\n")
        
        if (workstationIds.isEmpty()) {
            report.append("   ❌ PROBLEMA CRÍTICO: Sin estaciones asignadas\n")
            report.append("   📝 SOLUCIÓN: Ir a 'Trabajadores' → Editar → Seleccionar estaciones\n")
        } else {
            report.append("   ✅ Tiene estaciones asignadas:\n")
            assignedWorkstations.forEach { station ->
                val status = if (station.isActive) "✅ Activa" else "❌ Inactiva"
                val priority = if (station.isPriority) "⭐ Prioritaria" else "📍 Normal"
                report.append("     - ${station.name} (ID: ${station.id}) - $status - $priority\n")
                report.append("       Capacidad requerida: ${station.requiredWorkers} trabajadores\n")
            }
        }
        
        // Verificar restricciones
        report.append("\n🚫 VERIFICACIÓN DE RESTRICCIONES:\n")
        var hasRestrictions = false
        for (stationId in workstationIds) {
            try {
                val hasProhibition = workerRestrictionDao.hasRestriction(
                    workerId, 
                    stationId, 
                    RestrictionType.PROHIBITED
                )
                if (hasProhibition) {
                    val station = allWorkstations.find { it.id == stationId }
                    report.append("   ⚠️ Restricción prohibitiva en: ${station?.name ?: "ID: $stationId"}\n")
                    hasRestrictions = true
                }
            } catch (e: Exception) {
                report.append("   ❓ Error verificando restricciones para estación $stationId\n")
            }
        }
        if (!hasRestrictions) {
            report.append("   ✅ Sin restricciones prohibitivas\n")
        }
        
        // Verificar caché
        report.append("\n💾 ESTADO DEL CACHÉ:\n")
        val cachedIds = workerWorkstationCache[workerId]
        if (cachedIds != null) {
            report.append("   Caché encontrado: $cachedIds\n")
            if (cachedIds != workstationIds) {
                report.append("   ⚠️ ADVERTENCIA: Caché desactualizado!\n")
                report.append("   BD actual: $workstationIds\n")
            } else {
                report.append("   ✅ Caché sincronizado con BD\n")
            }
        } else {
            report.append("   ❌ No encontrado en caché (se cargará desde BD)\n")
        }
        
        // Veredicto final
        report.append("\n🎯 VEREDICTO FINAL:\n")
        val isEligible = worker.isActive && workstationIds.isNotEmpty()
        if (isEligible) {
            report.append("   ✅ ELEGIBLE PARA ROTACIÓN\n")
            report.append("   📊 Aparecerá en las rotaciones generadas\n")
        } else {
            report.append("   ❌ NO ELEGIBLE PARA ROTACIÓN\n")
            if (!worker.isActive) {
                report.append("   📝 Motivo: Trabajador inactivo\n")
            }
            if (workstationIds.isEmpty()) {
                report.append("   📝 Motivo: Sin estaciones asignadas\n")
            }
        }
        
        report.append("=" .repeat(50) + "\n")
        
        val finalReport = report.toString()
        println("DEBUG: $finalReport")
        println("DEBUG: ============================================")
        
        return finalReport
    }
    
    /**
     * Método para diagnosticar problemas comunes en el sistema de rotación.
     */
    suspend fun diagnoseRotationIssues(): String {
        println("DEBUG: === DIAGNÓSTICO GENERAL DEL SISTEMA ===")
        
        val report = StringBuilder()
        report.append("🔧 DIAGNÓSTICO DEL SISTEMA DE ROTACIÓN\n")
        report.append("=" .repeat(50) + "\n")
        
        // Verificar trabajadores
        val allWorkers = workerDao.getAllWorkers().first()
        val activeWorkers = allWorkers.filter { it.isActive }
        val workersWithStations = activeWorkers.filter { worker ->
            workerDao.getWorkerWorkstationIds(worker.id).isNotEmpty()
        }
        
        report.append("👥 TRABAJADORES:\n")
        report.append("   Total: ${allWorkers.size}\n")
        report.append("   Activos: ${activeWorkers.size}\n")
        report.append("   Con estaciones asignadas: ${workersWithStations.size}\n")
        report.append("   Elegibles para rotación: ${workersWithStations.size}\n")
        
        if (workersWithStations.isEmpty()) {
            report.append("   ❌ PROBLEMA: Ningún trabajador tiene estaciones asignadas\n")
        }
        
        // Verificar estaciones
        val allWorkstations = workstationDao.getAllActiveWorkstations().first()
        val priorityStations = allWorkstations.filter { it.isPriority }
        
        report.append("\n🏭 ESTACIONES:\n")
        report.append("   Total activas: ${allWorkstations.size}\n")
        report.append("   Prioritarias: ${priorityStations.size}\n")
        report.append("   Normales: ${allWorkstations.size - priorityStations.size}\n")
        
        // Verificar asignaciones
        var totalAssignments = 0
        allWorkstations.forEach { station ->
            val assignedWorkers = activeWorkers.count { worker ->
                workerDao.getWorkerWorkstationIds(worker.id).contains(station.id)
            }
            totalAssignments += assignedWorkers
            
            if (assignedWorkers == 0) {
                report.append("   ⚠️ Estación sin trabajadores: ${station.name}\n")
            }
        }
        
        report.append("   Total asignaciones: $totalAssignments\n")
        
        // Verificar caché
        report.append("\n💾 CACHÉ:\n")
        report.append("   Trabajadores en caché: ${workerWorkstationCache.size}\n")
        if (workerWorkstationCache.isEmpty()) {
            report.append("   ℹ️ Caché vacío (se cargará en próxima rotación)\n")
        }
        
        val finalReport = report.toString()
        println("DEBUG: $finalReport")
        
        return finalReport
    }
    
    /**
     * Gets rotation statistics for the current rotation.
     */
    fun getRotationStatistics(): RotationStatistics {
        val rotationTable = _rotationTable.value
        val rotationItems = _rotationItems.value ?: emptyList()
        
        if (rotationTable == null) {
            return RotationStatistics()
        }
        
        var totalWorkers = 0
        var workersRotating = 0
        var workersStaying = 0
        var trainerTraineePairs = 0
        
        // Count workers in current phase
        rotationTable.currentPhase.values.forEach { workers ->
            totalWorkers += workers.size
        }
        
        // Analyze rotation patterns
        rotationTable.currentPhase.forEach { (currentStationId, currentWorkers) ->
            currentWorkers.forEach { worker ->
                // Find where this worker goes in next phase
                val nextStationId = rotationTable.nextPhase.entries
                    .find { it.value.contains(worker) }?.key
                
                when {
                    worker.isTrainee && worker.trainerId != null -> {
                        // Count trainer-trainee pairs
                        val trainer = rotationTable.currentPhase.values.flatten()
                            .find { it.id == worker.trainerId }
                        if (trainer != null) {
                            trainerTraineePairs++
                        }
                    }
                    nextStationId != null && nextStationId != currentStationId -> {
                        workersRotating++
                    }
                    nextStationId == currentStationId -> {
                        workersStaying++
                    }
                }
            }
        }
        
        // Avoid double counting trainer-trainee pairs
        trainerTraineePairs = trainerTraineePairs / 2
        
        return RotationStatistics(
            totalWorkers = totalWorkers,
            workersRotating = workersRotating,
            workersStaying = workersStaying,
            trainerTraineePairs = trainerTraineePairs,
            rotationPercentage = if (totalWorkers > 0) (workersRotating * 100) / totalWorkers else 0
        )
    }
    
    /**
     * Data class for rotation statistics.
     */
    data class RotationStatistics(
        val totalWorkers: Int = 0,
        val workersRotating: Int = 0,
        val workersStaying: Int = 0,
        val trainerTraineePairs: Int = 0,
        val rotationPercentage: Int = 0
    ) {
        fun getSummaryText(): String {
            return "Rotación: $workersRotating/$totalWorkers trabajadores ($rotationPercentage%) • " +
                   "Permanecen: $workersStaying • Entrenamientos: $trainerTraineePairs parejas"
        }
    }
}

class RotationViewModelFactory(
    private val workerDao: WorkerDao,
    private val workstationDao: WorkstationDao,
    private val workerRestrictionDao: WorkerRestrictionDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RotationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RotationViewModel(workerDao, workstationDao, workerRestrictionDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}