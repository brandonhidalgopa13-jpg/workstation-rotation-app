package com.workstation.rotation.data.sync

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import com.workstation.rotation.data.entities.Worker
import com.workstation.rotation.data.entities.Workstation
import com.workstation.rotation.data.entities.WorkerWorkstation

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 💾 GESTOR DE RESPALDO Y SINCRONIZACIÓN
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 
 * Maneja el respaldo y restauración de datos de la aplicación.
 * Permite exportar/importar configuraciones completas del sistema.
 * 
 * 🔧 FUNCIONALIDADES:
 * - Exportar datos a archivo JSON
 * - Importar datos desde archivo JSON
 * - Crear respaldos automáticos
 * - Validar integridad de datos
 * - Sincronización básica entre dispositivos
 * 
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 */
class BackupManager(private val context: Context) {
    
    private val json = Json { 
        prettyPrint = true
        ignoreUnknownKeys = true
    }
    
    /**
     * Estructura de datos para el respaldo completo.
     */
    @Serializable
    data class BackupData(
        val version: String,
        val timestamp: Long,
        val appVersion: String,
        val workers: List<SerializableWorker>,
        val workstations: List<SerializableWorkstation>,
        val workerWorkstations: List<SerializableWorkerWorkstation>
    )
    
    @Serializable
    data class SerializableWorker(
        val id: Long,
        val name: String,
        val email: String,
        val availabilityPercentage: Int,
        val restrictionNotes: String,
        val isTrainer: Boolean,
        val isTrainee: Boolean,
        val trainerId: Long?,
        val trainingWorkstationId: Long?,
        val isActive: Boolean,
        val currentWorkstationId: Long?,
        val rotationsInCurrentStation: Int,
        val lastRotationTimestamp: Long
    )
    
    @Serializable
    data class SerializableWorkstation(
        val id: Long,
        val name: String,
        val requiredWorkers: Int,
        val isPriority: Boolean,
        val isActive: Boolean
    )
    
    @Serializable
    data class SerializableWorkerWorkstation(
        val workerId: Long,
        val workstationId: Long
    )
    

    
    /**
     * Crea un respaldo completo de los datos.
     */
    suspend fun createBackup(
        workers: List<Worker>,
        workstations: List<Workstation>,
        workerWorkstations: List<WorkerWorkstation>
    ): String = withContext(Dispatchers.IO) {
        
        val backupData = BackupData(
            version = "2.0.0",
            timestamp = System.currentTimeMillis(),
            appVersion = "2.0.0",
            workers = workers.map { it.toSerializable() },
            workstations = workstations.map { it.toSerializable() },
            workerWorkstations = workerWorkstations.map { it.toSerializable() }
        )
        
        return@withContext json.encodeToString(backupData)
    }
    
    /**
     * Restaura datos desde un respaldo.
     */
    suspend fun restoreBackup(backupJson: String): BackupData = withContext(Dispatchers.IO) {
        return@withContext json.decodeFromString<BackupData>(backupJson)
    }
    
    /**
     * Guarda un respaldo en el almacenamiento interno.
     */
    suspend fun saveBackupToFile(backupJson: String): File = withContext(Dispatchers.IO) {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "backup_rotacion_$timestamp.json"
        val file = File(context.getExternalFilesDir(null), fileName)
        
        FileOutputStream(file).use { output ->
            output.write(backupJson.toByteArray())
        }
        
        return@withContext file
    }
    
    /**
     * Carga un respaldo desde un archivo.
     */
    suspend fun loadBackupFromFile(file: File): String = withContext(Dispatchers.IO) {
        return@withContext FileInputStream(file).use { input ->
            input.readBytes().toString(Charsets.UTF_8)
        }
    }
    
    /**
     * Valida la integridad de un respaldo.
     */
    fun validateBackup(backupData: BackupData): BackupValidationResult {
        val errors = mutableListOf<String>()
        
        // Validar versión
        if (backupData.version.isEmpty()) {
            errors.add("Versión de respaldo no especificada")
        }
        
        // Validar trabajadores
        if (backupData.workers.any { it.name.isBlank() }) {
            errors.add("Algunos trabajadores no tienen nombre")
        }
        
        // Validar estaciones
        if (backupData.workstations.any { it.name.isBlank() || it.requiredWorkers <= 0 }) {
            errors.add("Algunas estaciones tienen configuración inválida")
        }
        
        // Validar relaciones
        val workerIds = backupData.workers.map { it.id }.toSet()
        val workstationIds = backupData.workstations.map { it.id }.toSet()
        
        backupData.workerWorkstations.forEach { relation ->
            if (!workerIds.contains(relation.workerId)) {
                errors.add("Relación con trabajador inexistente: ${relation.workerId}")
            }
            if (!workstationIds.contains(relation.workstationId)) {
                errors.add("Relación con estación inexistente: ${relation.workstationId}")
            }
        }
        
        return BackupValidationResult(
            isValid = errors.isEmpty(),
            errors = errors,
            workerCount = backupData.workers.size,
            workstationCount = backupData.workstations.size,
            timestamp = backupData.timestamp
        )
    }
    
    /**
     * Resultado de validación de respaldo.
     */
    data class BackupValidationResult(
        val isValid: Boolean,
        val errors: List<String>,
        val workerCount: Int,
        val workstationCount: Int,
        val timestamp: Long
    )
    
    // Funciones de extensión para conversión
    private fun Worker.toSerializable() = SerializableWorker(
        id, name, email, availabilityPercentage, restrictionNotes,
        isTrainer, isTrainee, trainerId, trainingWorkstationId, isActive,
        currentWorkstationId, rotationsInCurrentStation, lastRotationTimestamp
    )
    
    private fun Workstation.toSerializable() = SerializableWorkstation(
        id, name, requiredWorkers, isPriority, isActive
    )
    
    private fun WorkerWorkstation.toSerializable() = SerializableWorkerWorkstation(
        workerId, workstationId
    )
}