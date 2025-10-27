package com.workstation.rotation.data.cloud

import com.workstation.rotation.data.entities.Worker
import com.workstation.rotation.data.entities.Workstation
import com.workstation.rotation.data.entities.WorkerWorkstation
import kotlinx.serialization.Serializable

/**
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * ☁️ MODELOS DE DATOS PARA LA NUBE
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 
 * 📋 PROPÓSITO:
 * Clases de datos optimizadas para almacenamiento y sincronización en Firebase Firestore.
 * Incluyen metadatos adicionales para control de versiones y sincronización.
 * 
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 */

/**
 * Modelo de trabajador para la nube con metadatos de sincronización.
 */
@Serializable
data class CloudWorker(
    val id: Long = 0,
    val name: String = "",
    val email: String = "",
    val availabilityPercentage: Int = 100,
    val restrictionNotes: String = "",
    val isTrainer: Boolean = false,
    val isTrainee: Boolean = false,
    val trainerId: Long? = null,
    val trainingWorkstationId: Long? = null,
    val isActive: Boolean = true,
    val currentWorkstationId: Long? = null,
    val rotationsInCurrentStation: Int = 0,
    val lastRotationTimestamp: Long = 0,
    val lastModified: Long = System.currentTimeMillis(),
    val version: Int = 1
) {
    /**
     * Convierte a entidad local.
     */
    fun toLocalWorker(): Worker {
        return Worker(
            id = id,
            name = name,
            email = email,
            availabilityPercentage = availabilityPercentage,
            restrictionNotes = restrictionNotes,
            isTrainer = isTrainer,
            isTrainee = isTrainee,
            trainerId = trainerId,
            trainingWorkstationId = trainingWorkstationId,
            isActive = isActive,
            currentWorkstationId = currentWorkstationId,
            rotationsInCurrentStation = rotationsInCurrentStation,
            lastRotationTimestamp = lastRotationTimestamp
        )
    }
    
    companion object {
        /**
         * Crea desde un Map de Firestore.
         */
        fun fromMap(map: Map<String, Any>): CloudWorker {
            return CloudWorker(
                id = (map["id"] as? Number)?.toLong() ?: 0,
                name = map["name"] as? String ?: "",
                email = map["email"] as? String ?: "",
                availabilityPercentage = (map["availabilityPercentage"] as? Number)?.toInt() ?: 100,
                restrictionNotes = map["restrictionNotes"] as? String ?: "",
                isTrainer = map["isTrainer"] as? Boolean ?: false,
                isTrainee = map["isTrainee"] as? Boolean ?: false,
                trainerId = (map["trainerId"] as? Number)?.toLong(),
                trainingWorkstationId = (map["trainingWorkstationId"] as? Number)?.toLong(),
                isActive = map["isActive"] as? Boolean ?: true,
                currentWorkstationId = (map["currentWorkstationId"] as? Number)?.toLong(),
                rotationsInCurrentStation = (map["rotationsInCurrentStation"] as? Number)?.toInt() ?: 0,
                lastRotationTimestamp = (map["lastRotationTimestamp"] as? Number)?.toLong() ?: 0,
                lastModified = (map["lastModified"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                version = (map["version"] as? Number)?.toInt() ?: 1
            )
        }
    }
}

/**
 * Modelo de estación de trabajo para la nube.
 */
@Serializable
data class CloudWorkstation(
    val id: Long = 0,
    val name: String = "",
    val requiredWorkers: Int = 1,
    val isPriority: Boolean = false,
    val isActive: Boolean = true,
    val lastModified: Long = System.currentTimeMillis(),
    val version: Int = 1
) {
    /**
     * Convierte a entidad local.
     */
    fun toLocalWorkstation(): Workstation {
        return Workstation(
            id = id,
            name = name,
            requiredWorkers = requiredWorkers,
            isPriority = isPriority,
            isActive = isActive
        )
    }
    
    companion object {
        /**
         * Crea desde un Map de Firestore.
         */
        fun fromMap(map: Map<String, Any>): CloudWorkstation {
            return CloudWorkstation(
                id = (map["id"] as? Number)?.toLong() ?: 0,
                name = map["name"] as? String ?: "",
                requiredWorkers = (map["requiredWorkers"] as? Number)?.toInt() ?: 1,
                isPriority = map["isPriority"] as? Boolean ?: false,
                isActive = map["isActive"] as? Boolean ?: true,
                lastModified = (map["lastModified"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                version = (map["version"] as? Number)?.toInt() ?: 1
            )
        }
    }
}

/**
 * Modelo de relación trabajador-estación para la nube.
 */
@Serializable
data class CloudWorkerWorkstation(
    val workerId: Long = 0,
    val workstationId: Long = 0,
    val lastModified: Long = System.currentTimeMillis(),
    val version: Int = 1
) {
    /**
     * Convierte a entidad local.
     */
    fun toLocalWorkerWorkstation(): WorkerWorkstation {
        return WorkerWorkstation(
            workerId = workerId,
            workstationId = workstationId
        )
    }
    
    companion object {
        /**
         * Crea desde un Map de Firestore.
         */
        fun fromMap(map: Map<String, Any>): CloudWorkerWorkstation {
            return CloudWorkerWorkstation(
                workerId = (map["workerId"] as? Number)?.toLong() ?: 0,
                workstationId = (map["workstationId"] as? Number)?.toLong() ?: 0,
                lastModified = (map["lastModified"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                version = (map["version"] as? Number)?.toInt() ?: 1
            )
        }
    }
}

/**
 * Modelo de respaldo completo en la nube.
 */
@Serializable
data class CloudBackup(
    val id: String = "",
    val userId: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val deviceInfo: Map<String, String> = emptyMap(),
    val workers: List<CloudWorker> = emptyList(),
    val workstations: List<CloudWorkstation> = emptyList(),
    val workerWorkstations: List<CloudWorkerWorkstation> = emptyList(),
    val version: Int = 1
) {
    /**
     * Obtiene el tamaño del respaldo en elementos.
     */
    fun getSize(): Int = workers.size + workstations.size + workerWorkstations.size
    
    /**
     * Obtiene una descripción legible del respaldo.
     */
    fun getDescription(): String {
        val date = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
            .format(java.util.Date(timestamp))
        val device = deviceInfo["model"] ?: "Dispositivo desconocido"
        return "$date - $device (${workers.size} trabajadores, ${workstations.size} estaciones)"
    }
}

/**
 * Información de sincronización del workspace.
 */
@Serializable
data class WorkspaceInfo(
    val lastSync: Long = 0,
    val deviceInfo: Map<String, String> = emptyMap(),
    val dataVersion: Int = 1,
    val totalWorkers: Int = 0,
    val totalWorkstations: Int = 0,
    val isOnline: Boolean = false
) {
    /**
     * Verifica si los datos están actualizados.
     */
    fun isUpToDate(localTimestamp: Long): Boolean {
        return lastSync >= localTimestamp
    }
    
    /**
     * Obtiene el tiempo transcurrido desde la última sincronización.
     */
    fun getTimeSinceLastSync(): String {
        val now = System.currentTimeMillis()
        val diff = now - lastSync
        
        return when {
            diff < 60_000 -> "Hace menos de 1 minuto"
            diff < 3600_000 -> "Hace ${diff / 60_000} minutos"
            diff < 86400_000 -> "Hace ${diff / 3600_000} horas"
            else -> "Hace ${diff / 86400_000} días"
        }
    }
}

/**
 * Estado de sincronización.
 */
enum class SyncStatus {
    IDLE,           // Sin actividad
    SYNCING,        // Sincronizando
    UPLOADING,      // Subiendo datos
    DOWNLOADING,    // Descargando datos
    CONFLICT,       // Conflicto de datos
    ERROR,          // Error en sincronización
    SUCCESS         // Sincronización exitosa
}

/**
 * Evento de sincronización.
 */
data class SyncEvent(
    val status: SyncStatus,
    val message: String,
    val progress: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
) {
    /**
     * Verifica si es un evento de error.
     */
    fun isError(): Boolean = status == SyncStatus.ERROR
    
    /**
     * Verifica si es un evento de éxito.
     */
    fun isSuccess(): Boolean = status == SyncStatus.SUCCESS
    
    /**
     * Verifica si la sincronización está en progreso.
     */
    fun isInProgress(): Boolean = status in listOf(
        SyncStatus.SYNCING, 
        SyncStatus.UPLOADING, 
        SyncStatus.DOWNLOADING
    )
}