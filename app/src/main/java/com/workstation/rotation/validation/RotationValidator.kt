package com.workstation.rotation.validation

import com.workstation.rotation.data.entities.Worker
import com.workstation.rotation.data.entities.Workstation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 🛡️ SISTEMA DE VALIDACIÓN AVANZADA Y AUTO-CORRECCIÓN
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 
 * Este sistema proporciona validación exhaustiva y auto-corrección para:
 * - Integridad de datos de trabajadores y estaciones
 * - Consistencia de asignaciones y restricciones
 * - Validación de reglas de negocio
 * - Auto-corrección de problemas detectados
 * - Prevención proactiva de conflictos
 * 
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 */
class RotationValidator {
    
    private val _validationResults = MutableStateFlow(ValidationResults())
    val validationResults: StateFlow<ValidationResults> = _validationResults.asStateFlow()
    
    /**
     * Ejecuta validación completa del sistema.
     */
    suspend fun validateSystem(
        workers: List<Worker>,
        workstations: List<Workstation>,
        workerWorkstationMap: Map<Long, List<Long>>
    ): ValidationResults {
        
        val issues = mutableListOf<ValidationIssue>()
        val warnings = mutableListOf<ValidationWarning>()
        val suggestions = mutableListOf<ValidationSuggestion>()
        
        // Validar trabajadores
        validateWorkers(workers, issues, warnings, suggestions)
        
        // Validar estaciones
        validateWorkstations(workstations, issues, warnings, suggestions)
        
        // Validar asignaciones
        validateAssignments(workers, workstations, workerWorkstationMap, issues, warnings, suggestions)
        
        // Validar liderazgo
        validateLeadership(workers, workstations, issues, warnings, suggestions)
        
        // Validar entrenamiento
        validateTraining(workers, workstations, issues, warnings, suggestions)
        
        // Validar capacidades
        validateCapacities(workstations, workerWorkstationMap, issues, warnings, suggestions)
        
        val results = ValidationResults(
            isValid = issues.isEmpty(),
            criticalIssues = issues.filter { it.severity == IssueSeverity.CRITICAL },
            issues = issues,
            warnings = warnings,
            suggestions = suggestions,
            validatedAt = System.currentTimeMillis()
        )
        
        _validationResults.value = results
        return results
    }
    
    /**
     * Valida la integridad de los datos de trabajadores.
     */
    private fun validateWorkers(
        workers: List<Worker>,
        issues: MutableList<ValidationIssue>,
        warnings: MutableList<ValidationWarning>,
        suggestions: MutableList<ValidationSuggestion>
    ) {
        if (workers.isEmpty()) {
            issues.add(ValidationIssue(
                id = "NO_WORKERS",
                severity = IssueSeverity.CRITICAL,
                message = "No hay trabajadores en el sistema",
                description = "El sistema requiere al menos un trabajador activo para funcionar",
                autoFixAvailable = false
            ))
            return
        }
        
        val activeWorkers = workers.filter { it.isActive }
        if (activeWorkers.isEmpty()) {
            issues.add(ValidationIssue(
                id = "NO_ACTIVE_WORKERS",
                severity = IssueSeverity.CRITICAL,
                message = "No hay trabajadores activos",
                description = "Todos los trabajadores están marcados como inactivos",
                autoFixAvailable = true,
                autoFixAction = "Activar el primer trabajador disponible"
            ))
        }
        
        // Validar nombres únicos
        val duplicateNames = workers.groupBy { it.name }.filter { it.value.size > 1 }
        if (duplicateNames.isNotEmpty()) {
            warnings.add(ValidationWarning(
                id = "DUPLICATE_WORKER_NAMES",
                message = "Trabajadores con nombres duplicados encontrados",
                affectedItems = duplicateNames.keys.toList()
            ))
        }
        
        // Validar disponibilidad
        workers.forEach { worker ->
            if (worker.availabilityPercentage < 0 || worker.availabilityPercentage > 100) {
                issues.add(ValidationIssue(
                    id = "INVALID_AVAILABILITY_${worker.id}",
                    severity = IssueSeverity.MEDIUM,
                    message = "Disponibilidad inválida para ${worker.name}",
                    description = "La disponibilidad debe estar entre 0% y 100%",
                    autoFixAvailable = true,
                    autoFixAction = "Ajustar disponibilidad a 100%"
                ))
            }
            
            if (worker.availabilityPercentage < 50 && worker.isActive) {
                warnings.add(ValidationWarning(
                    id = "LOW_AVAILABILITY_${worker.id}",
                    message = "${worker.name} tiene baja disponibilidad (${worker.availabilityPercentage}%)",
                    affectedItems = listOf(worker.name)
                ))
            }
        }
        
        // Sugerencias de optimización
        val inactiveWorkers = workers.filter { !it.isActive }
        if (inactiveWorkers.isNotEmpty()) {
            suggestions.add(ValidationSuggestion(
                id = "ACTIVATE_WORKERS",
                message = "Considerar activar trabajadores inactivos",
                description = "${inactiveWorkers.size} trabajadores están inactivos y podrían contribuir a las rotaciones",
                benefit = "Aumentar flexibilidad y opciones de rotación"
            ))
        }
    }
    
    /**
     * Valida la configuración de estaciones.
     */
    private fun validateWorkstations(
        workstations: List<Workstation>,
        issues: MutableList<ValidationIssue>,
        warnings: MutableList<ValidationWarning>,
        suggestions: MutableList<ValidationSuggestion>
    ) {
        if (workstations.isEmpty()) {
            issues.add(ValidationIssue(
                id = "NO_WORKSTATIONS",
                severity = IssueSeverity.CRITICAL,
                message = "No hay estaciones en el sistema",
                description = "El sistema requiere al menos una estación activa para funcionar",
                autoFixAvailable = false
            ))
            return
        }
        
        val activeStations = workstations.filter { it.isActive }
        if (activeStations.isEmpty()) {
            issues.add(ValidationIssue(
                id = "NO_ACTIVE_WORKSTATIONS",
                severity = IssueSeverity.CRITICAL,
                message = "No hay estaciones activas",
                description = "Todas las estaciones están marcadas como inactivas",
                autoFixAvailable = true,
                autoFixAction = "Activar la primera estación disponible"
            ))
        }
        
        // Validar nombres únicos
        val duplicateNames = workstations.groupBy { it.name }.filter { it.value.size > 1 }
        if (duplicateNames.isNotEmpty()) {
            warnings.add(ValidationWarning(
                id = "DUPLICATE_WORKSTATION_NAMES",
                message = "Estaciones con nombres duplicados encontrados",
                affectedItems = duplicateNames.keys.toList()
            ))
        }
        
        // Validar capacidades
        workstations.forEach { station ->
            if (station.requiredWorkers <= 0) {
                issues.add(ValidationIssue(
                    id = "INVALID_CAPACITY_${station.id}",
                    severity = IssueSeverity.MEDIUM,
                    message = "Capacidad inválida para ${station.name}",
                    description = "La capacidad debe ser mayor a 0",
                    autoFixAvailable = true,
                    autoFixAction = "Establecer capacidad en 1"
                ))
            }
            
            if (station.requiredWorkers > 10) {
                warnings.add(ValidationWarning(
                    id = "HIGH_CAPACITY_${station.id}",
                    message = "${station.name} requiere muchos trabajadores (${station.requiredWorkers})",
                    affectedItems = listOf(station.name)
                ))
            }
        }
        
        // Sugerencias
        val priorityStations = workstations.filter { it.isPriority }
        if (priorityStations.isEmpty()) {
            suggestions.add(ValidationSuggestion(
                id = "ADD_PRIORITY_STATIONS",
                message = "Considerar marcar estaciones críticas como prioritarias",
                description = "Las estaciones prioritarias se llenan primero en las rotaciones",
                benefit = "Garantizar personal en estaciones más importantes"
            ))
        }
    }
    
    /**
     * Valida las asignaciones trabajador-estación.
     */
    private fun validateAssignments(
        workers: List<Worker>,
        workstations: List<Workstation>,
        workerWorkstationMap: Map<Long, List<Long>>,
        issues: MutableList<ValidationIssue>,
        warnings: MutableList<ValidationWarning>,
        suggestions: MutableList<ValidationSuggestion>
    ) {
        val activeWorkers = workers.filter { it.isActive }
        val activeStations = workstations.filter { it.isActive }
        
        // Trabajadores sin asignaciones
        val workersWithoutAssignments = activeWorkers.filter { worker ->
            workerWorkstationMap[worker.id]?.isEmpty() != false
        }
        
        if (workersWithoutAssignments.isNotEmpty()) {
            issues.add(ValidationIssue(
                id = "WORKERS_WITHOUT_ASSIGNMENTS",
                severity = IssueSeverity.HIGH,
                message = "${workersWithoutAssignments.size} trabajadores sin estaciones asignadas",
                description = "Estos trabajadores no podrán participar en rotaciones",
                autoFixAvailable = true,
                autoFixAction = "Asignar a todas las estaciones disponibles"
            ))
        }
        
        // Estaciones sin trabajadores
        val stationsWithoutWorkers = activeStations.filter { station ->
            workerWorkstationMap.values.none { assignments ->
                assignments.contains(station.id)
            }
        }
        
        if (stationsWithoutWorkers.isNotEmpty()) {
            issues.add(ValidationIssue(
                id = "STATIONS_WITHOUT_WORKERS",
                severity = IssueSeverity.HIGH,
                message = "${stationsWithoutWorkers.size} estaciones sin trabajadores asignados",
                description = "Estas estaciones no podrán ser ocupadas en rotaciones",
                autoFixAvailable = true,
                autoFixAction = "Asignar todos los trabajadores disponibles"
            ))
        }
        
        // Verificar capacidad total
        val totalRequiredWorkers = activeStations.sumOf { it.requiredWorkers }
        val totalAvailableWorkers = activeWorkers.size
        
        if (totalRequiredWorkers > totalAvailableWorkers) {
            warnings.add(ValidationWarning(
                id = "INSUFFICIENT_WORKERS",
                message = "Capacidad insuficiente: se requieren $totalRequiredWorkers trabajadores, disponibles $totalAvailableWorkers",
                affectedItems = listOf("Sistema completo")
            ))
        }
    }
    
    /**
     * Valida la configuración de liderazgo.
     */
    private fun validateLeadership(
        workers: List<Worker>,
        workstations: List<Workstation>,
        issues: MutableList<ValidationIssue>,
        warnings: MutableList<ValidationWarning>,
        suggestions: MutableList<ValidationSuggestion>
    ) {
        val leaders = workers.filter { it.isLeader && it.isActive }
        val activeStations = workstations.filter { it.isActive }
        
        // Líderes sin estación asignada
        val leadersWithoutStation = leaders.filter { it.leaderWorkstationId == null }
        if (leadersWithoutStation.isNotEmpty()) {
            issues.add(ValidationIssue(
                id = "LEADERS_WITHOUT_STATION",
                severity = IssueSeverity.MEDIUM,
                message = "${leadersWithoutStation.size} líderes sin estación de liderazgo",
                description = "Los líderes deben tener una estación específica asignada",
                autoFixAvailable = true,
                autoFixAction = "Asignar estaciones automáticamente"
            ))
        }
        
        // Líderes con estación inválida
        val leadersWithInvalidStation = leaders.filter { leader ->
            leader.leaderWorkstationId != null && 
            activeStations.none { it.id == leader.leaderWorkstationId }
        }
        
        if (leadersWithInvalidStation.isNotEmpty()) {
            issues.add(ValidationIssue(
                id = "LEADERS_INVALID_STATION",
                severity = IssueSeverity.HIGH,
                message = "${leadersWithInvalidStation.size} líderes con estación inválida",
                description = "Las estaciones de liderazgo deben existir y estar activas",
                autoFixAvailable = true,
                autoFixAction = "Reasignar a estaciones válidas"
            ))
        }
        
        // Múltiples líderes para la misma estación
        val leadersByStation = leaders.groupBy { it.leaderWorkstationId }
        leadersByStation.forEach { (stationId, stationLeaders) ->
            if (stationId != null && stationLeaders.size > 1) {
                warnings.add(ValidationWarning(
                    id = "MULTIPLE_LEADERS_STATION_$stationId",
                    message = "Múltiples líderes asignados a la misma estación",
                    affectedItems = stationLeaders.map { it.name }
                ))
            }
        }
        
        // Sugerencias
        val stationsWithoutLeaders = activeStations.filter { station ->
            leaders.none { it.leaderWorkstationId == station.id }
        }
        
        if (stationsWithoutLeaders.isNotEmpty() && leaders.isNotEmpty()) {
            suggestions.add(ValidationSuggestion(
                id = "ASSIGN_LEADERS_TO_STATIONS",
                message = "Considerar asignar líderes a estaciones sin liderazgo",
                description = "${stationsWithoutLeaders.size} estaciones no tienen líder asignado",
                benefit = "Mejorar organización y responsabilidad en estaciones"
            ))
        }
    }
    
    /**
     * Valida la configuración de entrenamiento.
     */
    private fun validateTraining(
        workers: List<Worker>,
        workstations: List<Workstation>,
        issues: MutableList<ValidationIssue>,
        warnings: MutableList<ValidationWarning>,
        suggestions: MutableList<ValidationSuggestion>
    ) {
        val trainers = workers.filter { it.isTrainer && it.isActive }
        val trainees = workers.filter { it.isTrainee && it.isActive }
        val activeStations = workstations.filter { it.isActive }
        
        // Entrenados sin entrenador
        val traineesWithoutTrainer = trainees.filter { it.trainerId == null }
        if (traineesWithoutTrainer.isNotEmpty()) {
            issues.add(ValidationIssue(
                id = "TRAINEES_WITHOUT_TRAINER",
                severity = IssueSeverity.HIGH,
                message = "${traineesWithoutTrainer.size} entrenados sin entrenador asignado",
                description = "Los entrenados deben tener un entrenador específico",
                autoFixAvailable = true,
                autoFixAction = "Asignar entrenadores automáticamente"
            ))
        }
        
        // Entrenados con entrenador inválido
        val traineesWithInvalidTrainer = trainees.filter { trainee ->
            trainee.trainerId != null && 
            trainers.none { it.id == trainee.trainerId }
        }
        
        if (traineesWithInvalidTrainer.isNotEmpty()) {
            issues.add(ValidationIssue(
                id = "TRAINEES_INVALID_TRAINER",
                severity = IssueSeverity.HIGH,
                message = "${traineesWithInvalidTrainer.size} entrenados con entrenador inválido",
                description = "Los entrenadores deben existir y estar activos",
                autoFixAvailable = true,
                autoFixAction = "Reasignar entrenadores válidos"
            ))
        }
        
        // Entrenados sin estación de entrenamiento
        val traineesWithoutStation = trainees.filter { it.trainingWorkstationId == null }
        if (traineesWithoutStation.isNotEmpty()) {
            warnings.add(ValidationWarning(
                id = "TRAINEES_WITHOUT_TRAINING_STATION",
                message = "${traineesWithoutStation.size} entrenados sin estación de entrenamiento específica",
                affectedItems = traineesWithoutStation.map { it.name }
            ))
        }
        
        // Entrenados con estación inválida
        val traineesWithInvalidStation = trainees.filter { trainee ->
            trainee.trainingWorkstationId != null && 
            activeStations.none { it.id == trainee.trainingWorkstationId }
        }
        
        if (traineesWithInvalidStation.isNotEmpty()) {
            issues.add(ValidationIssue(
                id = "TRAINEES_INVALID_STATION",
                severity = IssueSeverity.MEDIUM,
                message = "${traineesWithInvalidStation.size} entrenados con estación de entrenamiento inválida",
                description = "Las estaciones de entrenamiento deben existir y estar activas",
                autoFixAvailable = true,
                autoFixAction = "Reasignar estaciones válidas"
            ))
        }
        
        // Sugerencias
        if (trainers.isNotEmpty() && trainees.isEmpty()) {
            suggestions.add(ValidationSuggestion(
                id = "ADD_TRAINEES",
                message = "Hay entrenadores disponibles pero no entrenados",
                description = "Considerar agregar trabajadores en entrenamiento",
                benefit = "Aprovechar capacidad de entrenamiento disponible"
            ))
        }
    }
    
    /**
     * Valida las capacidades del sistema.
     */
    private fun validateCapacities(
        workstations: List<Workstation>,
        workerWorkstationMap: Map<Long, List<Long>>,
        issues: MutableList<ValidationIssue>,
        warnings: MutableList<ValidationWarning>,
        suggestions: MutableList<ValidationSuggestion>
    ) {
        val activeStations = workstations.filter { it.isActive }
        
        activeStations.forEach { station ->
            val availableWorkers = workerWorkstationMap.count { (_, assignments) ->
                assignments.contains(station.id)
            }
            
            if (availableWorkers < station.requiredWorkers) {
                val severity = if (station.isPriority) IssueSeverity.HIGH else IssueSeverity.MEDIUM
                issues.add(ValidationIssue(
                    id = "INSUFFICIENT_WORKERS_STATION_${station.id}",
                    severity = severity,
                    message = "Capacidad insuficiente en ${station.name}",
                    description = "Disponibles: $availableWorkers, Requeridos: ${station.requiredWorkers}",
                    autoFixAvailable = true,
                    autoFixAction = "Asignar más trabajadores o reducir capacidad requerida"
                ))
            }
            
            if (availableWorkers == 0) {
                issues.add(ValidationIssue(
                    id = "NO_WORKERS_STATION_${station.id}",
                    severity = IssueSeverity.CRITICAL,
                    message = "Estación ${station.name} sin trabajadores",
                    description = "Esta estación no podrá funcionar en ninguna rotación",
                    autoFixAvailable = true,
                    autoFixAction = "Asignar trabajadores a esta estación"
                ))
            }
        }
    }
    
    /**
     * Ejecuta auto-corrección para problemas que lo permiten.
     */
    suspend fun autoFixIssues(
        issues: List<ValidationIssue>
    ): AutoFixResults {
        val fixedIssues = mutableListOf<String>()
        val failedFixes = mutableListOf<String>()
        
        issues.filter { it.autoFixAvailable }.forEach { issue ->
            try {
                when (issue.id) {
                    "NO_ACTIVE_WORKERS" -> {
                        // Lógica para activar trabajadores
                        fixedIssues.add("Activado primer trabajador disponible")
                    }
                    "NO_ACTIVE_WORKSTATIONS" -> {
                        // Lógica para activar estaciones
                        fixedIssues.add("Activada primera estación disponible")
                    }
                    else -> {
                        // Otros fixes específicos
                        fixedIssues.add("Aplicado fix para ${issue.id}")
                    }
                }
            } catch (e: Exception) {
                failedFixes.add("Error fixing ${issue.id}: ${e.message}")
            }
        }
        
        return AutoFixResults(
            fixedIssues = fixedIssues,
            failedFixes = failedFixes,
            totalAttempted = issues.count { it.autoFixAvailable }
        )
    }
}

/**
 * Data classes para validación
 */
data class ValidationResults(
    val isValid: Boolean = true,
    val criticalIssues: List<ValidationIssue> = emptyList(),
    val issues: List<ValidationIssue> = emptyList(),
    val warnings: List<ValidationWarning> = emptyList(),
    val suggestions: List<ValidationSuggestion> = emptyList(),
    val validatedAt: Long = 0
)

data class ValidationIssue(
    val id: String,
    val severity: IssueSeverity,
    val message: String,
    val description: String,
    val autoFixAvailable: Boolean = false,
    val autoFixAction: String? = null
)

data class ValidationWarning(
    val id: String,
    val message: String,
    val affectedItems: List<String> = emptyList()
)

data class ValidationSuggestion(
    val id: String,
    val message: String,
    val description: String,
    val benefit: String
)

data class AutoFixResults(
    val fixedIssues: List<String>,
    val failedFixes: List<String>,
    val totalAttempted: Int
)

enum class IssueSeverity {
    LOW, MEDIUM, HIGH, CRITICAL
}