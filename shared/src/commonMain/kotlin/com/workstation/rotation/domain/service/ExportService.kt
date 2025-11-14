package com.workstation.rotation.domain.service

import com.workstation.rotation.domain.models.*
import com.workstation.rotation.domain.repository.RotationRepository
import kotlinx.coroutines.flow.first

class ExportService(private val repository: RotationRepository) {
    
    suspend fun exportToText(
        session: RotationSessionModel,
        assignments: List<RotationAssignmentModel>
    ): String {
        val workers = repository.getAllWorkers().first()
        val workstations = repository.getAllWorkstations().first()
        
        return buildString {
            appendLine("=" .repeat(60))
            appendLine("ROTACIÓN DE ESTACIONES")
            appendLine("=" .repeat(60))
            appendLine()
            appendLine("Sesión: ${session.name}")
            appendLine("Fecha: ${formatTimestamp(session.createdAt)}")
            appendLine("Intervalo: ${session.rotationIntervalMinutes} minutos")
            appendLine("Estado: ${if (session.isActive) "ACTIVA" else "INACTIVA"}")
            appendLine()
            appendLine("-" .repeat(60))
            appendLine()
            
            val groupedAssignments = assignments.groupBy { it.rotationOrder }.toSortedMap()
            
            groupedAssignments.forEach { (rotationOrder, rotationAssignments) ->
                appendLine("ROTACIÓN #${rotationOrder + 1}")
                appendLine("-" .repeat(60))
                
                rotationAssignments.forEach { assignment ->
                    val worker = workers.find { it.id == assignment.workerId }
                    val workstation = workstations.find { it.id == assignment.workstationId }
                    
                    appendLine("  ${worker?.name ?: "Desconocido"} → ${workstation?.name ?: "Desconocida"}")
                }
                appendLine()
            }
            
            appendLine("=" .repeat(60))
            appendLine("Total de asignaciones: ${assignments.size}")
            appendLine("Trabajadores: ${assignments.map { it.workerId }.distinct().size}")
            appendLine("Estaciones: ${assignments.map { it.workstationId }.distinct().size}")
            appendLine("=" .repeat(60))
        }
    }
    
    suspend fun exportToCSV(
        session: RotationSessionModel,
        assignments: List<RotationAssignmentModel>
    ): String {
        val workers = repository.getAllWorkers().first()
        val workstations = repository.getAllWorkstations().first()
        
        return buildString {
            // Header
            appendLine("Rotación,Trabajador,Código Trabajador,Estación,Código Estación")
            
            // Data
            assignments.sortedBy { it.rotationOrder }.forEach { assignment ->
                val worker = workers.find { it.id == assignment.workerId }
                val workstation = workstations.find { it.id == assignment.workstationId }
                
                appendLine(
                    "${assignment.rotationOrder + 1}," +
                    "\"${worker?.name ?: "Desconocido"}\"," +
                    "\"${worker?.code ?: "N/A"}\"," +
                    "\"${workstation?.name ?: "Desconocida"}\"," +
                    "\"${workstation?.code ?: "N/A"}\""
                )
            }
        }
    }
    
    suspend fun exportToMarkdown(
        session: RotationSessionModel,
        assignments: List<RotationAssignmentModel>
    ): String {
        val workers = repository.getAllWorkers().first()
        val workstations = repository.getAllWorkstations().first()
        
        return buildString {
            appendLine("# ${session.name}")
            appendLine()
            appendLine("**Fecha:** ${formatTimestamp(session.createdAt)}")
            appendLine("**Intervalo:** ${session.rotationIntervalMinutes} minutos")
            appendLine("**Estado:** ${if (session.isActive) "✅ ACTIVA" else "⏸️ INACTIVA"}")
            appendLine()
            appendLine("---")
            appendLine()
            
            val groupedAssignments = assignments.groupBy { it.rotationOrder }.toSortedMap()
            
            groupedAssignments.forEach { (rotationOrder, rotationAssignments) ->
                appendLine("## Rotación #${rotationOrder + 1}")
                appendLine()
                appendLine("| Trabajador | Estación |")
                appendLine("|------------|----------|")
                
                rotationAssignments.forEach { assignment ->
                    val worker = workers.find { it.id == assignment.workerId }
                    val workstation = workstations.find { it.id == assignment.workstationId }
                    
                    appendLine("| ${worker?.name ?: "Desconocido"} | ${workstation?.name ?: "Desconocida"} |")
                }
                appendLine()
            }
            
            appendLine("---")
            appendLine()
            appendLine("**Estadísticas:**")
            appendLine("- Total de asignaciones: ${assignments.size}")
            appendLine("- Trabajadores: ${assignments.map { it.workerId }.distinct().size}")
            appendLine("- Estaciones: ${assignments.map { it.workstationId }.distinct().size}")
        }
    }
    
    private fun formatTimestamp(timestamp: Long): String {
        // Formato simple para exportación
        return kotlinx.datetime.Instant.fromEpochMilliseconds(timestamp)
            .toString()
            .replace("T", " ")
            .substringBefore(".")
    }
}
