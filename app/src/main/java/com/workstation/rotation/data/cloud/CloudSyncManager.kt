package com.workstation.rotation.data.cloud

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.MetadataChanges
import com.workstation.rotation.data.entities.Worker
import com.workstation.rotation.data.entities.Workstation
import com.workstation.rotation.data.entities.WorkerWorkstation
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.Date

/**
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * ☁️ GESTOR DE SINCRONIZACIÓN EN LA NUBE
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 
 * 📋 FUNCIONES PRINCIPALES:
 * 
 * 🔄 1. SINCRONIZACIÓN AUTOMÁTICA
 *    - Subida automática de cambios locales
 *    - Descarga de cambios remotos
 *    - Resolución de conflictos
 * 
 * 💾 2. RESPALDO EN LA NUBE
 *    - Respaldo completo de datos
 *    - Restauración desde la nube
 *    - Versionado de respaldos
 * 
 * 🔄 3. SINCRONIZACIÓN EN TIEMPO REAL
 *    - Escucha cambios en tiempo real
 *    - Notificaciones de cambios
 *    - Sincronización entre dispositivos
 * 
 * 🔒 4. SEGURIDAD Y PRIVACIDAD
 *    - Datos encriptados
 *    - Acceso por usuario
 *    - Permisos granulares
 * 
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 */
class CloudSyncManager(
    private val context: Context,
    private val authManager: CloudAuthManager
) {
    
    private val firestore = try { FirebaseFirestore.getInstance() } catch (e: Exception) { null }
    private val json = Json { ignoreUnknownKeys = true }
    
    companion object {
        private const val COLLECTION_USERS = "users"
        private const val COLLECTION_WORKSPACES = "workspaces"
        private const val COLLECTION_WORKERS = "workers"
        private const val COLLECTION_WORKSTATIONS = "workstations"
        private const val COLLECTION_WORKER_WORKSTATIONS = "worker_workstations"
        private const val COLLECTION_BACKUPS = "backups"
    }
    
    /**
     * Verifica si Firebase está disponible.
     */
    fun isFirebaseAvailable(): Boolean = firestore != null && authManager.isFirebaseAvailable()
    
    /**
     * Obtiene la referencia del workspace del usuario actual.
     */
    private fun getUserWorkspaceRef() = authManager.getCurrentUserId()?.let { userId ->
        firestore?.collection(COLLECTION_USERS)
            .document(userId)
            .collection(COLLECTION_WORKSPACES)
            .document("default")
    }
    
    /**
     * Sube los datos locales a la nube.
     */
    suspend fun uploadData(
        workers: List<Worker>,
        workstations: List<Workstation>,
        workerWorkstations: List<WorkerWorkstation>
    ): SyncResult {
        if (!isFirebaseAvailable()) {
            return SyncResult.Error("Firebase no está disponible. Configura google-services.json")
        }
        return try {
            val workspaceRef = getUserWorkspaceRef()
                ?: return SyncResult.Error("Usuario no autenticado")
            
            val batch = firestore!!.batch()
            val timestamp = System.currentTimeMillis()
            
            // Subir trabajadores
            workers.forEach { worker ->
                val workerRef = workspaceRef.collection(COLLECTION_WORKERS).document(worker.id.toString())
                batch.set(workerRef, worker.toCloudData(timestamp))
            }
            
            // Subir estaciones
            workstations.forEach { workstation ->
                val stationRef = workspaceRef.collection(COLLECTION_WORKSTATIONS).document(workstation.id.toString())
                batch.set(stationRef, workstation.toCloudData(timestamp))
            }
            
            // Subir relaciones trabajador-estación
            workerWorkstations.forEach { relation ->
                val relationRef = workspaceRef.collection(COLLECTION_WORKER_WORKSTATIONS)
                    .document("${relation.workerId}_${relation.workstationId}")
                batch.set(relationRef, relation.toCloudData(timestamp))
            }
            
            // Actualizar metadatos del workspace
            batch.set(workspaceRef, mapOf(
                "lastSync" to timestamp,
                "lastSyncDate" to Date(),
                "deviceInfo" to getDeviceInfo(),
                "dataVersion" to 1
            ))
            
            batch.commit().await()
            SyncResult.Success("Datos subidos exitosamente")
            
        } catch (e: Exception) {
            SyncResult.Error("Error al subir datos: ${e.message}")
        }
    }
    
    /**
     * Descarga los datos desde la nube.
     */
    suspend fun downloadData(): SyncResult {
        return try {
            val workspaceRef = getUserWorkspaceRef()
                ?: return SyncResult.Error("Usuario no autenticado")
            
            // Descargar trabajadores
            val workersSnapshot = workspaceRef.collection(COLLECTION_WORKERS).get().await()
            val workers = workersSnapshot.documents.mapNotNull { doc ->
                doc.data?.let { CloudWorker.fromMap(it).toLocalWorker() }
            }
            
            // Descargar estaciones
            val stationsSnapshot = workspaceRef.collection(COLLECTION_WORKSTATIONS).get().await()
            val workstations = stationsSnapshot.documents.mapNotNull { doc ->
                doc.data?.let { CloudWorkstation.fromMap(it).toLocalWorkstation() }
            }
            
            // Descargar relaciones
            val relationsSnapshot = workspaceRef.collection(COLLECTION_WORKER_WORKSTATIONS).get().await()
            val workerWorkstations = relationsSnapshot.documents.mapNotNull { doc ->
                doc.data?.let { CloudWorkerWorkstation.fromMap(it).toLocalWorkerWorkstation() }
            }
            
            val cloudData = CloudData(workers, workstations, workerWorkstations)
            SyncResult.DataDownloaded(cloudData)
            
        } catch (e: Exception) {
            SyncResult.Error("Error al descargar datos: ${e.message}")
        }
    }
    
    /**
     * Escucha cambios en tiempo real desde la nube.
     */
    fun listenToCloudChanges(): Flow<CloudChangeEvent> = callbackFlow {
        val workspaceRef = getUserWorkspaceRef()
        if (workspaceRef == null) {
            trySend(CloudChangeEvent.Error("Usuario no autenticado"))
            close()
            return@callbackFlow
        }
        
        val listeners = mutableListOf<ListenerRegistration>()
        
        // Escuchar cambios en trabajadores
        val workersListener = workspaceRef.collection(COLLECTION_WORKERS)
            .addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, error ->
                if (error != null) {
                    trySend(CloudChangeEvent.Error("Error en trabajadores: ${error.message}"))
                    return@addSnapshotListener
                }
                
                snapshot?.documentChanges?.forEach { change ->
                    val worker = change.document.data.let { CloudWorker.fromMap(it).toLocalWorker() }
                    when (change.type) {
                        com.google.firebase.firestore.DocumentChange.Type.ADDED,
                        com.google.firebase.firestore.DocumentChange.Type.MODIFIED -> {
                            trySend(CloudChangeEvent.WorkerChanged(worker))
                        }
                        com.google.firebase.firestore.DocumentChange.Type.REMOVED -> {
                            trySend(CloudChangeEvent.WorkerDeleted(worker.id))
                        }
                    }
                }
            }
        listeners.add(workersListener)
        
        // Escuchar cambios en estaciones
        val stationsListener = workspaceRef.collection(COLLECTION_WORKSTATIONS)
            .addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, error ->
                if (error != null) {
                    trySend(CloudChangeEvent.Error("Error en estaciones: ${error.message}"))
                    return@addSnapshotListener
                }
                
                snapshot?.documentChanges?.forEach { change ->
                    val workstation = change.document.data.let { CloudWorkstation.fromMap(it).toLocalWorkstation() }
                    when (change.type) {
                        com.google.firebase.firestore.DocumentChange.Type.ADDED,
                        com.google.firebase.firestore.DocumentChange.Type.MODIFIED -> {
                            trySend(CloudChangeEvent.WorkstationChanged(workstation))
                        }
                        com.google.firebase.firestore.DocumentChange.Type.REMOVED -> {
                            trySend(CloudChangeEvent.WorkstationDeleted(workstation.id))
                        }
                    }
                }
            }
        listeners.add(stationsListener)
        
        awaitClose {
            listeners.forEach { it.remove() }
        }
    }
    
    /**
     * Crea un respaldo completo en la nube.
     */
    suspend fun createCloudBackup(
        workers: List<Worker>,
        workstations: List<Workstation>,
        workerWorkstations: List<WorkerWorkstation>
    ): SyncResult {
        return try {
            val userId = authManager.getCurrentUserId()
                ?: return SyncResult.Error("Usuario no autenticado")
            
            val backup = CloudBackup(
                id = System.currentTimeMillis().toString(),
                userId = userId,
                timestamp = System.currentTimeMillis(),
                deviceInfo = getDeviceInfo(),
                workers = workers.map { it.toCloudData() },
                workstations = workstations.map { it.toCloudData() },
                workerWorkstations = workerWorkstations.map { it.toCloudData() }
            )
            
            firestore?.collection(COLLECTION_BACKUPS)
                ?.document(backup.id)
                ?.set(backup)
                ?.await()
            
            SyncResult.Success("Respaldo creado en la nube: ${backup.id}")
            
        } catch (e: Exception) {
            SyncResult.Error("Error al crear respaldo: ${e.message}")
        }
    }
    
    /**
     * Lista los respaldos disponibles en la nube.
     */
    suspend fun listCloudBackups(): SyncResult {
        return try {
            val userId = authManager.getCurrentUserId()
                ?: return SyncResult.Error("Usuario no autenticado")
            
            val backupsSnapshot = firestore?.collection(COLLECTION_BACKUPS)
                ?.whereEqualTo("userId", userId)
                ?.orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                ?.limit(10)
                ?.get()
                ?.await()
                ?: return SyncResult.Error("Firestore no disponible")
            
            val backups = backupsSnapshot.documents.mapNotNull { doc ->
                doc.toObject(CloudBackup::class.java)
            }
            
            SyncResult.BackupsListed(backups)
            
        } catch (e: Exception) {
            SyncResult.Error("Error al listar respaldos: ${e.message}")
        }
    }
    
    /**
     * Restaura datos desde un respaldo en la nube.
     */
    suspend fun restoreFromCloudBackup(backupId: String): SyncResult {
        return try {
            val backupDoc = firestore?.collection(COLLECTION_BACKUPS)
                ?.document(backupId)
                ?.get()
                ?.await()
                ?: return SyncResult.Error("Firestore no disponible")
            
            val backup = backupDoc.toObject(CloudBackup::class.java)
                ?: return SyncResult.Error("Respaldo no encontrado")
            
            val workers = backup.workers.map { it.toLocalWorker() }
            val workstations = backup.workstations.map { it.toLocalWorkstation() }
            val workerWorkstations = backup.workerWorkstations.map { it.toLocalWorkerWorkstation() }
            
            val cloudData = CloudData(workers, workstations, workerWorkstations)
            SyncResult.DataDownloaded(cloudData)
            
        } catch (e: Exception) {
            SyncResult.Error("Error al restaurar respaldo: ${e.message}")
        }
    }
    
    /**
     * Obtiene información del dispositivo actual.
     */
    private fun getDeviceInfo(): Map<String, Any> {
        return mapOf(
            "model" to android.os.Build.MODEL,
            "manufacturer" to android.os.Build.MANUFACTURER,
            "version" to android.os.Build.VERSION.RELEASE,
            "app" to "Workstation Rotation v2.1.0"
        )
    }
    
    /**
     * Resultado de las operaciones de sincronización.
     */
    sealed class SyncResult {
        data class Success(val message: String) : SyncResult()
        data class Error(val message: String) : SyncResult()
        data class DataDownloaded(val data: CloudData) : SyncResult()
        data class BackupsListed(val backups: List<CloudBackup>) : SyncResult()
    }
    
    /**
     * Eventos de cambios en la nube.
     */
    sealed class CloudChangeEvent {
        data class WorkerChanged(val worker: Worker) : CloudChangeEvent()
        data class WorkerDeleted(val workerId: Long) : CloudChangeEvent()
        data class WorkstationChanged(val workstation: Workstation) : CloudChangeEvent()
        data class WorkstationDeleted(val workstationId: Long) : CloudChangeEvent()
        data class Error(val message: String) : CloudChangeEvent()
    }
    
    /**
     * Datos descargados desde la nube.
     */
    data class CloudData(
        val workers: List<Worker>,
        val workstations: List<Workstation>,
        val workerWorkstations: List<WorkerWorkstation>
    )
}

// Extensiones para convertir entidades locales a formato de nube
private fun Worker.toCloudData(timestamp: Long = System.currentTimeMillis()) = CloudWorker(
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
    lastRotationTimestamp = lastRotationTimestamp,
    lastModified = timestamp
)

private fun Workstation.toCloudData(timestamp: Long = System.currentTimeMillis()) = CloudWorkstation(
    id = id,
    name = name,
    requiredWorkers = requiredWorkers,
    isPriority = isPriority,
    isActive = isActive,
    lastModified = timestamp
)

private fun WorkerWorkstation.toCloudData(timestamp: Long = System.currentTimeMillis()) = CloudWorkerWorkstation(
    workerId = workerId,
    workstationId = workstationId,
    lastModified = timestamp
)