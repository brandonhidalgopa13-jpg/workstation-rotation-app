package com.workstation.rotation.utils

import android.content.Context
import com.workstation.rotation.data.database.AppDatabase
import com.workstation.rotation.data.entities.WorkerWorkstationCapability
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 🔄 UTILIDAD DE SINCRONIZACIÓN DE CAPACIDADES
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 
 * 🎯 PROPÓSITO:
 * Sincroniza las asignaciones de estaciones (worker_workstations) con las capacidades
 * (worker_workstation_capabilities) para trabajadores existentes.
 * 
 * 📋 USO:
 * Esta utilidad debe ejecutarse:
 * • Al actualizar la aplicación a la nueva versión
 * • Cuando se detecte desincronización entre las tablas
 * • Como parte del proceso de migración de datos
 * 
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 */
object CapabilitySyncUtil {
    
    /**
     * Sincroniza todas las capacidades de trabajadores existentes.
     * 
     * @param context Contexto de la aplicación
     * @return Resultado de la sincronización con estadísticas
     */
    suspend fun syncAllWorkerCapabilities(context: Context): SyncResult = withContext(Dispatchers.IO) {
        android.util.Log.d("CapabilitySyncUtil", "═══════════════════════════════════════════════════════")
        android.util.Log.d("CapabilitySyncUtil", "🔄 INICIANDO SINCRONIZACIÓN GLOBAL DE CAPACIDADES")
        android.util.Log.d("CapabilitySyncUtil", "═══════════════════════════════════════════════════════")
        
        val database = AppDatabase.getDatabase(context)
        val workerDao = database.workerDao()
        val capabilityDao = database.workerWorkstationCapabilityDao()
        
        var workersProcessed = 0
        var capabilitiesCreated = 0
        var capabilitiesUpdated = 0
        var capabilitiesDeactivated = 0
        var errors = 0
        
        try {
            // Obtener todos los trabajadores
            val workers = workerDao.getAllWorkersSync()
            android.util.Log.d("CapabilitySyncUtil", "📊 Total de trabajadores: ${workers.size}")
            
            workers.forEach { worker ->
                try {
                    android.util.Log.d("CapabilitySyncUtil", "")
                    android.util.Log.d("CapabilitySyncUtil", "👤 Procesando: ${worker.name} (ID: ${worker.id})")
                    
                    // Obtener estaciones asignadas en worker_workstations
                    val assignedWorkstationIds = workerDao.getWorkerWorkstationIds(worker.id)
                    android.util.Log.d("CapabilitySyncUtil", "   Estaciones asignadas: ${assignedWorkstationIds.size}")
                    
                    if (assignedWorkstationIds.isEmpty()) {
                        android.util.Log.d("CapabilitySyncUtil", "   ⚠️ Sin estaciones asignadas - omitiendo")
                        workersProcessed++
                        return@forEach
                    }
                    
                    // Obtener capacidades existentes
                    val existingCapabilities = capabilityDao.getByWorker(worker.id)
                    val existingWorkstationIds = existingCapabilities.map { it.workstation_id }.toSet()
                    
                    android.util.Log.d("CapabilitySyncUtil", "   Capacidades existentes: ${existingCapabilities.size}")
                    
                    // Determinar nivel de competencia base
                    val baseCompetencyLevel = when {
                        worker.isTrainee -> WorkerWorkstationCapability.LEVEL_BEGINNER
                        worker.isCertified -> WorkerWorkstationCapability.LEVEL_INTERMEDIATE
                        worker.isTrainer -> WorkerWorkstationCapability.LEVEL_ADVANCED
                        else -> WorkerWorkstationCapability.LEVEL_BASIC
                    }
                    
                    // Estaciones a agregar (nuevas)
                    val workstationsToAdd = assignedWorkstationIds.filter { !existingWorkstationIds.contains(it) }
                    
                    // Estaciones a desactivar (ya no asignadas)
                    val workstationsToDeactivate = existingWorkstationIds.filter { !assignedWorkstationIds.contains(it) }
                    
                    // Estaciones a reactivar (ya existían pero estaban inactivas)
                    val workstationsToReactivate = assignedWorkstationIds.filter { workstationId ->
                        existingCapabilities.any { it.workstation_id == workstationId && !it.is_active }
                    }
                    
                    android.util.Log.d("CapabilitySyncUtil", "   📝 A agregar: ${workstationsToAdd.size}")
                    android.util.Log.d("CapabilitySyncUtil", "   ⚠️ A desactivar: ${workstationsToDeactivate.size}")
                    android.util.Log.d("CapabilitySyncUtil", "   🔄 A reactivar: ${workstationsToReactivate.size}")
                    
                    // Agregar nuevas capacidades
                    workstationsToAdd.forEach { workstationId ->
                        val capability = WorkerWorkstationCapability(
                            worker_id = worker.id,
                            workstation_id = workstationId,
                            competency_level = baseCompetencyLevel,
                            is_active = true,
                            is_certified = worker.isCertified,
                            can_be_leader = worker.isLeader && worker.leaderWorkstationId == workstationId,
                            can_train = worker.isTrainer,
                            certified_at = if (worker.isCertified) worker.certificationDate else null,
                            notes = "Capacidad sincronizada automáticamente"
                        )
                        
                        capabilityDao.insert(capability)
                        capabilitiesCreated++
                        android.util.Log.d("CapabilitySyncUtil", "   ✅ Capacidad creada: Estación $workstationId")
                    }
                    
                    // Desactivar capacidades que ya no aplican
                    workstationsToDeactivate.forEach { workstationId ->
                        val capability = existingCapabilities.find { it.workstation_id == workstationId }
                        capability?.let {
                            val updated = it.copy(
                                is_active = false,
                                updated_at = System.currentTimeMillis()
                            )
                            capabilityDao.update(updated)
                            capabilitiesDeactivated++
                            android.util.Log.d("CapabilitySyncUtil", "   ⚠️ Capacidad desactivada: Estación $workstationId")
                        }
                    }
                    
                    // Reactivar capacidades existentes
                    workstationsToReactivate.forEach { workstationId ->
                        val capability = existingCapabilities.find { it.workstation_id == workstationId }
                        capability?.let {
                            val updated = it.copy(
                                is_active = true,
                                competency_level = baseCompetencyLevel,
                                is_certified = worker.isCertified,
                                can_be_leader = worker.isLeader && worker.leaderWorkstationId == workstationId,
                                can_train = worker.isTrainer,
                                updated_at = System.currentTimeMillis()
                            )
                            capabilityDao.update(updated)
                            capabilitiesUpdated++
                            android.util.Log.d("CapabilitySyncUtil", "   🔄 Capacidad reactivada: Estación $workstationId")
                        }
                    }
                    
                    workersProcessed++
                    android.util.Log.d("CapabilitySyncUtil", "   ✅ Trabajador procesado exitosamente")
                    
                } catch (e: Exception) {
                    errors++
                    android.util.Log.e("CapabilitySyncUtil", "   ❌ Error procesando trabajador ${worker.name}: ${e.message}", e)
                }
            }
            
            android.util.Log.d("CapabilitySyncUtil", "")
            android.util.Log.d("CapabilitySyncUtil", "═══════════════════════════════════════════════════════")
            android.util.Log.d("CapabilitySyncUtil", "✅ SINCRONIZACIÓN COMPLETADA")
            android.util.Log.d("CapabilitySyncUtil", "═══════════════════════════════════════════════════════")
            android.util.Log.d("CapabilitySyncUtil", "📊 ESTADÍSTICAS:")
            android.util.Log.d("CapabilitySyncUtil", "   • Trabajadores procesados: $workersProcessed")
            android.util.Log.d("CapabilitySyncUtil", "   • Capacidades creadas: $capabilitiesCreated")
            android.util.Log.d("CapabilitySyncUtil", "   • Capacidades actualizadas: $capabilitiesUpdated")
            android.util.Log.d("CapabilitySyncUtil", "   • Capacidades desactivadas: $capabilitiesDeactivated")
            android.util.Log.d("CapabilitySyncUtil", "   • Errores: $errors")
            android.util.Log.d("CapabilitySyncUtil", "═══════════════════════════════════════════════════════")
            
        } catch (e: Exception) {
            android.util.Log.e("CapabilitySyncUtil", "❌ ERROR CRÍTICO en sincronización: ${e.message}", e)
            errors++
        }
        
        SyncResult(
            workersProcessed = workersProcessed,
            capabilitiesCreated = capabilitiesCreated,
            capabilitiesUpdated = capabilitiesUpdated,
            capabilitiesDeactivated = capabilitiesDeactivated,
            errors = errors
        )
    }
    
    /**
     * Verifica si hay trabajadores que necesitan sincronización.
     * 
     * @param context Contexto de la aplicación
     * @return true si hay trabajadores desincronizados
     */
    suspend fun needsSynchronization(context: Context): Boolean = withContext(Dispatchers.IO) {
        try {
            val database = AppDatabase.getDatabase(context)
            val workerDao = database.workerDao()
            val capabilityDao = database.workerWorkstationCapabilityDao()
            
            val workers = workerDao.getAllWorkersSync()
            
            workers.any { worker ->
                val assignedWorkstationIds = workerDao.getWorkerWorkstationIds(worker.id)
                if (assignedWorkstationIds.isEmpty()) return@any false
                
                val existingCapabilities = capabilityDao.getByWorker(worker.id)
                val activeCapabilities = existingCapabilities.filter { it.is_active }
                
                // Hay desincronización si el número de capacidades activas no coincide
                // con el número de estaciones asignadas
                assignedWorkstationIds.size != activeCapabilities.size
            }
        } catch (e: Exception) {
            android.util.Log.e("CapabilitySyncUtil", "Error verificando sincronización: ${e.message}", e)
            false
        }
    }
}

/**
 * Resultado de la sincronización de capacidades
 */
data class SyncResult(
    val workersProcessed: Int,
    val capabilitiesCreated: Int,
    val capabilitiesUpdated: Int,
    val capabilitiesDeactivated: Int,
    val errors: Int
) {
    val isSuccessful: Boolean
        get() = errors == 0
    
    val totalChanges: Int
        get() = capabilitiesCreated + capabilitiesUpdated + capabilitiesDeactivated
    
    fun getSummary(): String {
        return """
            Sincronización ${if (isSuccessful) "exitosa" else "con errores"}:
            • Trabajadores procesados: $workersProcessed
            • Capacidades creadas: $capabilitiesCreated
            • Capacidades actualizadas: $capabilitiesUpdated
            • Capacidades desactivadas: $capabilitiesDeactivated
            • Total de cambios: $totalChanges
            ${if (errors > 0) "• Errores: $errors" else ""}
        """.trimIndent()
    }
}
