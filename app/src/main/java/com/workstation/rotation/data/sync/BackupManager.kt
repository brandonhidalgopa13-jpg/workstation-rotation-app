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
        val workerWorkstations: List<SerializableWorkerWorkstation>,
        val workerRestrictions: List<SerializableWorkerRestriction> = emptyList() // Default para compatibilidad
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
        val isCertified: Boolean = false,
        val certificationDate: Long? = null,
        // Campos críticos de liderazgo que faltaban
        val isLeader: Boolean = false,
        val leaderWorkstationId: Long? = null,
        val leadershipType: String = "BOTH",
        // Campos de seguimiento de rotación
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
    
    @Serializable
    data class SerializableWorkerRestriction(
        val workerId: Long,
        val workstationId: Long,
        val restrictionType: String, // Serializar como String para compatibilidad
        val notes: String,
        val isActive: Boolean,
        val createdAt: Long,
        val expiresAt: Long?
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
            version = "2.1.0",
            timestamp = System.currentTimeMillis(),
            appVersion = "2.1.0",
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
        
        // Validar datos críticos para sistema SQL
        validateSqlRotationData(backupData, errors)
        
        return BackupValidationResult(
            isValid = errors.isEmpty(),
            errors = errors,
            workerCount = backupData.workers.size,
            workstationCount = backupData.workstations.size,
            timestamp = backupData.timestamp
        )
    }
    
    /**
     * Valida datos específicos para el sistema de rotación SQL.
     */
    private fun validateSqlRotationData(backupData: BackupData, errors: MutableList<String>) {
        val workstationIds = backupData.workstations.map { it.id }.toSet()
        
        // Validar líderes
        val leaders = backupData.workers.filter { it.isLeader }
        leaders.forEach { leader ->
            if (leader.leaderWorkstationId == null) {
                errors.add("Líder '${leader.name}' sin estación de liderazgo asignada")
            } else if (!workstationIds.contains(leader.leaderWorkstationId)) {
                errors.add("Líder '${leader.name}' asignado a estación inexistente: ${leader.leaderWorkstationId}")
            }
            
            if (leader.leadershipType !in listOf("BOTH", "FIRST_HALF", "SECOND_HALF")) {
                errors.add("Líder '${leader.name}' tiene tipo de liderazgo inválido: ${leader.leadershipType}")
            }
        }
        
        // Validar parejas de entrenamiento
        val trainees = backupData.workers.filter { it.isTrainee }
        val trainerIds = backupData.workers.filter { it.isTrainer }.map { it.id }.toSet()
        
        trainees.forEach { trainee ->
            if (trainee.trainerId == null) {
                errors.add("Entrenado '${trainee.name}' sin entrenador asignado")
            } else if (!trainerIds.contains(trainee.trainerId)) {
                errors.add("Entrenado '${trainee.name}' asignado a entrenador inexistente: ${trainee.trainerId}")
            }
            
            if (trainee.trainingWorkstationId == null) {
                errors.add("Entrenado '${trainee.name}' sin estación de entrenamiento")
            } else if (!workstationIds.contains(trainee.trainingWorkstationId)) {
                errors.add("Entrenado '${trainee.name}' asignado a estación de entrenamiento inexistente: ${trainee.trainingWorkstationId}")
            }
        }
        
        // Validar que hay al menos algunos trabajadores con estaciones asignadas
        if (backupData.workerWorkstations.isEmpty() && backupData.workers.isNotEmpty()) {
            errors.add("No hay relaciones trabajador-estación definidas")
        }
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
    
    /**
     * Repara automáticamente un respaldo con datos incompletos.
     * Útil para respaldos de versiones anteriores que no incluían campos de liderazgo.
     */
    fun repairBackupData(backupData: BackupData): BackupData {
        val repairedWorkers = backupData.workers.map { worker ->
            // Si el worker no tiene los campos de liderazgo, usar valores por defecto
            worker.copy(
                isLeader = worker.isLeader,
                leaderWorkstationId = worker.leaderWorkstationId,
                leadershipType = if (worker.leadershipType.isEmpty()) "BOTH" else worker.leadershipType,
                isCertified = worker.isCertified,
                certificationDate = worker.certificationDate
            )
        }
        
        return backupData.copy(
            workers = repairedWorkers,
            version = "3.0.0" // Actualizar versión después de reparar
        )
    }
    
    /**
     * Crea un respaldo de migración que incluye todos los campos necesarios.
     */
    suspend fun createMigrationBackup(
        workers: List<Worker>,
        workstations: List<Workstation>,
        workerWorkstations: List<WorkerWorkstation>
    ): String = withContext(Dispatchers.IO) {
        
        val backupData = BackupData(
            version = "3.0.0",
            timestamp = System.currentTimeMillis(),
            appVersion = "3.0.0",
            workers = workers.map { it.toSerializable() },
            workstations = workstations.map { it.toSerializable() },
            workerWorkstations = workerWorkstations.map { it.toSerializable() }
        )
        
        // Validar antes de serializar
        val validation = validateBackup(backupData)
        if (!validation.isValid) {
            throw IllegalStateException("Respaldo inválido: ${validation.errors.joinToString()}")
        }
        
        return@withContext json.encodeToString(backupData)
    }
    
    // Funciones de extensión para conversión
    private fun Worker.toSerializable() = SerializableWorker(
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
        isCertified = isCertified,
        certificationDate = certificationDate,
        // Campos críticos de liderazgo
        isLeader = isLeader,
        leaderWorkstationId = leaderWorkstationId,
        leadershipType = leadershipType,
        // Campos de seguimiento
        currentWorkstationId = currentWorkstationId,
        rotationsInCurrentStation = rotationsInCurrentStation,
        lastRotationTimestamp = lastRotationTimestamp
    )
    
    private fun Workstation.toSerializable() = SerializableWorkstation(
        id, name, requiredWorkers, isPriority, isActive
    )
    
    private fun WorkerWorkstation.toSerializable() = SerializableWorkerWorkstation(
        workerId, workstationId
    )
    
    /**
     * Funciones de conversión desde datos serializables a entidades.
     */
    fun SerializableWorker.toEntity() = Worker(
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
        isCertified = isCertified,
        certificationDate = certificationDate,
        // Campos críticos de liderazgo
        isLeader = isLeader,
        leaderWorkstationId = leaderWorkstationId,
        leadershipType = leadershipType,
        // Campos de seguimiento
        currentWorkstationId = currentWorkstationId,
        rotationsInCurrentStation = rotationsInCurrentStation,
        lastRotationTimestamp = lastRotationTimestamp
    )
    
    fun SerializableWorkstation.toEntity() = Workstation(
        id = id,
        name = name,
        requiredWorkers = requiredWorkers,
        isPriority = isPriority,
        isActive = isActive
    )
    
    fun SerializableWorkerWorkstation.toEntity() = WorkerWorkstation(
        workerId = workerId,
        workstationId = workstationId
    )
}