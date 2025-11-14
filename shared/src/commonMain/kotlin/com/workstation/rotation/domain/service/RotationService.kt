package com.workstation.rotation.domain.service

import com.workstation.rotation.domain.models.*
import com.workstation.rotation.domain.repository.RotationRepository
import kotlinx.coroutines.flow.first

class RotationService(private val repository: RotationRepository) {
    
    suspend fun generateRotation(
        sessionName: String,
        intervalMinutes: Int = 60
    ): Result<RotationSessionModel> {
        return try {
            val workers = repository.getActiveWorkers().first()
            val workstations = repository.getActiveWorkstations().first()
            
            if (workers.isEmpty()) {
                return Result.failure(Exception("No hay trabajadores activos"))
            }
            
            if (workstations.isEmpty()) {
                return Result.failure(Exception("No hay estaciones activas"))
            }
            
            // Crear sesión
            val session = RotationSessionModel(
                name = sessionName,
                rotationIntervalMinutes = intervalMinutes
            )
            val sessionId = repository.insertSession(session)
            
            // Algoritmo de rotación equitativa
            val assignments = generateEquitableAssignments(
                workers = workers,
                workstations = workstations,
                sessionId = sessionId
            )
            
            // Guardar asignaciones
            assignments.forEach { assignment ->
                repository.insertAssignment(assignment)
            }
            
            Result.success(session.copy(id = sessionId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun generateEquitableAssignments(
        workers: List<WorkerModel>,
        workstations: List<WorkstationModel>,
        sessionId: Long
    ): List<RotationAssignmentModel> {
        val assignments = mutableListOf<RotationAssignmentModel>()
        val workerRotationCount = mutableMapOf<Long, Int>()
        
        // Inicializar contadores
        workers.forEach { worker ->
            workerRotationCount[worker.id] = 0
        }
        
        // Calcular número de rotaciones
        val totalRotations = maxOf(workers.size, workstations.size)
        
        for (rotationOrder in 0 until totalRotations) {
            // Asignar trabajadores a estaciones de forma equitativa
            workstations.forEachIndexed { index, workstation ->
                // Seleccionar trabajador con menos rotaciones
                val availableWorker = workers
                    .filter { worker ->
                        // Verificar capacidades si es necesario
                        workstation.requiredCapabilities.isEmpty() ||
                        worker.capabilities.any { it in workstation.requiredCapabilities }
                    }
                    .minByOrNull { worker ->
                        workerRotationCount[worker.id] ?: 0
                    }
                
                availableWorker?.let { worker ->
                    assignments.add(
                        RotationAssignmentModel(
                            sessionId = sessionId,
                            workerId = worker.id,
                            workstationId = workstation.id,
                            rotationOrder = rotationOrder
                        )
                    )
                    workerRotationCount[worker.id] = (workerRotationCount[worker.id] ?: 0) + 1
                }
            }
        }
        
        return assignments
    }
    
    suspend fun getRotationHistory(): List<RotationSessionModel> {
        return repository.getAllSessions().first()
    }
    
    suspend fun getSessionDetails(sessionId: Long): Pair<RotationSessionModel?, List<RotationAssignmentModel>> {
        val sessions = repository.getAllSessions().first()
        val session = sessions.find { it.id == sessionId }
        val assignments = repository.getAssignmentsBySession(sessionId)
        return Pair(session, assignments)
    }
}
