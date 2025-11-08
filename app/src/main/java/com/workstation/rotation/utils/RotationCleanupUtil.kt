package com.workstation.rotation.utils

import android.content.Context
import com.workstation.rotation.data.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 🧹 UTILIDAD DE LIMPIEZA DE ROTACIONES
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 
 * 🎯 PROPÓSITO:
 * Limpia asignaciones de rotación inválidas donde los trabajadores no tienen capacidades activas.
 * 
 * 📋 USO:
 * Esta utilidad debe ejecutarse:
 * • Después de sincronizar capacidades
 * • Antes de generar nuevas rotaciones
 * • Cuando se detecten inconsistencias en las asignaciones
 * 
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 */
object RotationCleanupUtil {
    
    /**
     * Limpia todas las asignaciones de trabajadores sin capacidades activas.
     * 
     * @param context Contexto de la aplicación
     * @return Resultado de la limpieza con estadísticas
     */
    suspend fun cleanOrphanedAssignments(context: Context): CleanupResult = withContext(Dispatchers.IO) {
        android.util.Log.d("RotationCleanupUtil", "═══════════════════════════════════════════════════════")
        android.util.Log.d("RotationCleanupUtil", "🧹 INICIANDO LIMPIEZA DE ASIGNACIONES HUÉRFANAS")
        android.util.Log.d("RotationCleanupUtil", "═══════════════════════════════════════════════════════")
        
        val database = AppDatabase.getDatabase(context)
        val assignmentDao = database.rotationAssignmentDao()
        val capabilityDao = database.workerWorkstationCapabilityDao()
        val workerDao = database.workerDao()
        
        var assignmentsChecked = 0
        var assignmentsRemoved = 0
        var errors = 0
        val removedWorkerNames = mutableListOf<String>()
        
        try {
            // Obtener todas las asignaciones activas
            val allAssignments = assignmentDao.getAllActiveAssignments()
            assignmentsChecked = allAssignments.size
            
            android.util.Log.d("RotationCleanupUtil", "📊 Total de asignaciones activas: $assignmentsChecked")
            
            allAssignments.forEach { assignment ->
                try {
                    // Verificar si el trabajador tiene capacidad activa para esta estación
                    val capability = capabilityDao.getByWorkerAndWorkstation(
                        assignment.worker_id,
                        assignment.workstation_id
                    )
                    
                    val shouldRemove = when {
                        capability == null -> {
                            android.util.Log.w("RotationCleanupUtil", "⚠️ Sin capacidad registrada")
                            true
                        }
                        !capability.is_active -> {
                            android.util.Log.w("RotationCleanupUtil", "⚠️ Capacidad inactiva")
                            true
                        }
                        !capability.canBeAssigned() -> {
                            android.util.Log.w("RotationCleanupUtil", "⚠️ No cumple requisitos mínimos")
                            true
                        }
                        else -> false
                    }
                    
                    if (shouldRemove) {
                        val worker = workerDao.getWorkerById(assignment.worker_id)
                        val workerName = worker?.name ?: "Desconocido (ID: ${assignment.worker_id})"
                        
                        android.util.Log.d("RotationCleanupUtil", "")
                        android.util.Log.d("RotationCleanupUtil", "🗑️ Removiendo asignación inválida:")
                        android.util.Log.d("RotationCleanupUtil", "   Trabajador: $workerName")
                        android.util.Log.d("RotationCleanupUtil", "   Estación ID: ${assignment.workstation_id}")
                        android.util.Log.d("RotationCleanupUtil", "   Tipo: ${assignment.rotation_type}")
                        android.util.Log.d("RotationCleanupUtil", "   Sesión: ${assignment.rotation_session_id}")
                        
                        // Eliminar la asignación
                        assignmentDao.delete(assignment)
                        assignmentsRemoved++
                        
                        if (!removedWorkerNames.contains(workerName)) {
                            removedWorkerNames.add(workerName)
                        }
                    }
                    
                } catch (e: Exception) {
                    errors++
                    android.util.Log.e("RotationCleanupUtil", "❌ Error procesando asignación: ${e.message}", e)
                }
            }
            
            android.util.Log.d("RotationCleanupUtil", "")
            android.util.Log.d("RotationCleanupUtil", "═══════════════════════════════════════════════════════")
            android.util.Log.d("RotationCleanupUtil", "✅ LIMPIEZA COMPLETADA")
            android.util.Log.d("RotationCleanupUtil", "═══════════════════════════════════════════════════════")
            android.util.Log.d("RotationCleanupUtil", "📊 ESTADÍSTICAS:")
            android.util.Log.d("RotationCleanupUtil", "   • Asignaciones verificadas: $assignmentsChecked")
            android.util.Log.d("RotationCleanupUtil", "   • Asignaciones removidas: $assignmentsRemoved")
            android.util.Log.d("RotationCleanupUtil", "   • Trabajadores afectados: ${removedWorkerNames.size}")
            android.util.Log.d("RotationCleanupUtil", "   • Errores: $errors")
            
            if (removedWorkerNames.isNotEmpty()) {
                android.util.Log.d("RotationCleanupUtil", "")
                android.util.Log.d("RotationCleanupUtil", "👥 TRABAJADORES REMOVIDOS:")
                removedWorkerNames.forEach { name ->
                    android.util.Log.d("RotationCleanupUtil", "   - $name")
                }
            }
            
            android.util.Log.d("RotationCleanupUtil", "═══════════════════════════════════════════════════════")
            
        } catch (e: Exception) {
            android.util.Log.e("RotationCleanupUtil", "❌ ERROR CRÍTICO en limpieza: ${e.message}", e)
            errors++
        }
        
        CleanupResult(
            assignmentsChecked = assignmentsChecked,
            assignmentsRemoved = assignmentsRemoved,
            workersAffected = removedWorkerNames.size,
            removedWorkerNames = removedWorkerNames,
            errors = errors
        )
    }
    
    /**
     * Verifica si hay asignaciones que necesitan limpieza.
     * 
     * @param context Contexto de la aplicación
     * @return true si hay asignaciones inválidas
     */
    suspend fun needsCleanup(context: Context): Boolean = withContext(Dispatchers.IO) {
        try {
            val database = AppDatabase.getDatabase(context)
            val assignmentDao = database.rotationAssignmentDao()
            val capabilityDao = database.workerWorkstationCapabilityDao()
            
            val allAssignments = assignmentDao.getAllActiveAssignments()
            
            allAssignments.any { assignment ->
                val capability = capabilityDao.getByWorkerAndWorkstation(
                    assignment.worker_id,
                    assignment.workstation_id
                )
                
                capability == null || !capability.is_active || !capability.canBeAssigned()
            }
        } catch (e: Exception) {
            android.util.Log.e("RotationCleanupUtil", "Error verificando limpieza: ${e.message}", e)
            false
        }
    }
}

/**
 * Resultado de la limpieza de asignaciones
 */
data class CleanupResult(
    val assignmentsChecked: Int,
    val assignmentsRemoved: Int,
    val workersAffected: Int,
    val removedWorkerNames: List<String>,
    val errors: Int
) {
    val isSuccessful: Boolean
        get() = errors == 0
    
    val hadChanges: Boolean
        get() = assignmentsRemoved > 0
    
    fun getSummary(): String {
        return """
            Limpieza ${if (isSuccessful) "exitosa" else "con errores"}:
            • Asignaciones verificadas: $assignmentsChecked
            • Asignaciones removidas: $assignmentsRemoved
            • Trabajadores afectados: $workersAffected
            ${if (errors > 0) "• Errores: $errors" else ""}
            ${if (removedWorkerNames.isNotEmpty()) "\nTrabajadores removidos:\n${removedWorkerNames.joinToString("\n") { "  - $it" }}" else ""}
        """.trimIndent()
    }
}
