package com.workstation.rotation.backup

import android.content.Context
import androidx.work.*
import com.workstation.rotation.data.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 💾 SISTEMA DE BACKUP Y RECUPERACIÓN AUTOMÁTICA
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 
 * Este sistema proporciona:
 * - Backups automáticos programados
 * - Backup antes de operaciones críticas
 * - Recuperación automática en caso de errores
 * - Versionado de backups
 * - Limpieza automática de backups antiguos
 * - Validación de integridad de backups
 * 
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 */
class AutoBackupManager(private val context: Context) {
    
    private val json = Json { 
        prettyPrint = true
        ignoreUnknownKeys = true
    }
    
    companion object {
        private const val BACKUP_WORK_NAME = "auto_backup_work"
        private const val BACKUP_FOLDER = "rotation_backups"
        private const val MAX_BACKUPS = 10
        private const val BACKUP_INTERVAL_HOURS = 6L
        
        @Volatile
        private var INSTANCE: AutoBackupManager? = null
        
        fun getInstance(context: Context): AutoBackupManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AutoBackupManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    /**
     * Inicializa el sistema de backup automático.
     */
    fun initializeAutoBackup() {
        schedulePeriodicBackup()
        println("AUTO_BACKUP: Sistema de backup automático inicializado")
    }
    
    /**
     * Programa backups periódicos automáticos.
     */
    private fun schedulePeriodicBackup() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .setRequiresBatteryNotLow(true)
            .build()
        
        val backupRequest = PeriodicWorkRequestBuilder<AutoBackupWorker>(
            BACKUP_INTERVAL_HOURS, TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()
        
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                BACKUP_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                backupRequest
            )
    }
    
    /**
     * Crea un backup completo del sistema.
     */
    suspend fun createBackup(reason: BackupReason = BackupReason.MANUAL): BackupResult {
        return withContext(Dispatchers.IO) {
            try {
                println("AUTO_BACKUP: Iniciando backup - Razón: $reason")
                
                val database = AppDatabase.getDatabase(context)
                
                // Obtener todos los datos
                val workers = database.workerDao().getAllWorkers().first()
                val workstations = database.workstationDao().getAllActiveWorkstations().first()
                val workerWorkstations = database.workerDao().getAllWorkerWorkstations()
                val workerRestrictions = database.workerRestrictionDao().getAllRestrictions()
                
                // Crear objeto de backup
                val backup = SystemBackup(
                    version = "1.0",
                    createdAt = System.currentTimeMillis(),
                    reason = reason,
                    workers = workers,
                    workstations = workstations,
                    workerWorkstations = workerWorkstations,
                    workerRestrictions = workerRestrictions,
                    metadata = BackupMetadata(
                        appVersion = getAppVersion(),
                        deviceInfo = getDeviceInfo(),
                        totalWorkers = workers.size,
                        totalWorkstations = workstations.size,
                        checksum = calculateChecksum(workers, workstations)
                    )
                )
                
                // Guardar backup
                val backupFile = saveBackupToFile(backup)
                
                // Limpiar backups antiguos
                cleanOldBackups()
                
                println("AUTO_BACKUP: Backup creado exitosamente - ${backupFile.name}")
                
                BackupResult.Success(
                    backupFile = backupFile,
                    backup = backup
                )
                
            } catch (e: Exception) {
                println("AUTO_BACKUP: Error creando backup - ${e.message}")
                e.printStackTrace()
                BackupResult.Error(e.message ?: "Error desconocido")
            }
        }
    }
    
    /**
     * Restaura el sistema desde un backup.
     */
    suspend fun restoreFromBackup(backupFile: File): RestoreResult {
        return withContext(Dispatchers.IO) {
            try {
                println("AUTO_BACKUP: Iniciando restauración desde ${backupFile.name}")
                
                // Leer y validar backup
                val backupJson = backupFile.readText()
                val backup = json.decodeFromString<SystemBackup>(backupJson)
                
                // Validar integridad
                val validationResult = validateBackup(backup)
                if (!validationResult.isValid) {
                    return@withContext RestoreResult.Error("Backup corrupto: ${validationResult.errors.joinToString()}")
                }
                
                // Crear backup de seguridad antes de restaurar
                val safetyBackup = createBackup(BackupReason.BEFORE_RESTORE)
                
                val database = AppDatabase.getDatabase(context)
                
                // Restaurar datos (en transacción)
                database.runInTransaction {
                    // Limpiar datos existentes
                    database.workerRestrictionDao().deleteAllRestrictions()
                    database.workerDao().deleteAllWorkerWorkstations()
                    database.workerDao().deleteAllWorkers()
                    database.workstationDao().deleteAllWorkstations()
                    
                    // Insertar datos del backup
                    database.workstationDao().insertWorkstations(backup.workstations)
                    database.workerDao().insertWorkers(backup.workers)
                    database.workerDao().insertWorkerWorkstations(backup.workerWorkstations)
                    database.workerRestrictionDao().insertRestrictions(backup.workerRestrictions)
                }
                
                println("AUTO_BACKUP: Restauración completada exitosamente")
                
                RestoreResult.Success(
                    backup = backup,
                    safetyBackup = safetyBackup
                )
                
            } catch (e: Exception) {
                println("AUTO_BACKUP: Error en restauración - ${e.message}")
                e.printStackTrace()
                RestoreResult.Error(e.message ?: "Error desconocido")
            }
        }
    }
    
    /**
     * Obtiene lista de backups disponibles.
     */
    suspend fun getAvailableBackups(): List<BackupInfo> {
        return withContext(Dispatchers.IO) {
            try {
                val backupDir = getBackupDirectory()
                if (!backupDir.exists()) return@withContext emptyList()
                
                backupDir.listFiles { file ->
                    file.isFile && file.name.endsWith(".backup.json")
                }?.mapNotNull { file ->
                    try {
                        val backupJson = file.readText()
                        val backup = json.decodeFromString<SystemBackup>(backupJson)
                        
                        BackupInfo(
                            file = file,
                            createdAt = backup.createdAt,
                            reason = backup.reason,
                            size = file.length(),
                            metadata = backup.metadata,
                            isValid = validateBackup(backup).isValid
                        )
                    } catch (e: Exception) {
                        println("AUTO_BACKUP: Error leyendo backup ${file.name} - ${e.message}")
                        null
                    }
                }?.sortedByDescending { it.createdAt } ?: emptyList()
                
            } catch (e: Exception) {
                println("AUTO_BACKUP: Error obteniendo backups - ${e.message}")
                emptyList()
            }
        }
    }
    
    /**
     * Valida la integridad de un backup.
     */
    private fun validateBackup(backup: SystemBackup): BackupValidation {
        val errors = mutableListOf<String>()
        
        // Validar versión
        if (backup.version.isEmpty()) {
            errors.add("Versión de backup faltante")
        }
        
        // Validar timestamp
        if (backup.createdAt <= 0) {
            errors.add("Timestamp inválido")
        }
        
        // Validar datos
        if (backup.workers.isEmpty() && backup.workstations.isEmpty()) {
            errors.add("Backup vacío - no contiene datos")
        }
        
        // Validar checksum si está disponible
        backup.metadata.checksum?.let { expectedChecksum ->
            val actualChecksum = calculateChecksum(backup.workers, backup.workstations)
            if (actualChecksum != expectedChecksum) {
                errors.add("Checksum no coincide - posible corrupción de datos")
            }
        }
        
        // Validar relaciones
        val workerIds = backup.workers.map { it.id }.toSet()
        val workstationIds = backup.workstations.map { it.id }.toSet()
        
        backup.workerWorkstations.forEach { ww ->
            if (!workerIds.contains(ww.workerId)) {
                errors.add("Referencia a trabajador inexistente: ${ww.workerId}")
            }
            if (!workstationIds.contains(ww.workstationId)) {
                errors.add("Referencia a estación inexistente: ${ww.workstationId}")
            }
        }
        
        return BackupValidation(
            isValid = errors.isEmpty(),
            errors = errors
        )
    }
    
    /**
     * Guarda un backup en archivo.
     */
    private fun saveBackupToFile(backup: SystemBackup): File {
        val backupDir = getBackupDirectory()
        if (!backupDir.exists()) {
            backupDir.mkdirs()
        }
        
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            .format(Date(backup.createdAt))
        val filename = "backup_${timestamp}_${backup.reason.name.lowercase()}.backup.json"
        
        val backupFile = File(backupDir, filename)
        val backupJson = json.encodeToString(SystemBackup.serializer(), backup)
        
        backupFile.writeText(backupJson)
        return backupFile
    }
    
    /**
     * Limpia backups antiguos manteniendo solo los más recientes.
     */
    private fun cleanOldBackups() {
        try {
            val backupDir = getBackupDirectory()
            if (!backupDir.exists()) return
            
            val backupFiles = backupDir.listFiles { file ->
                file.isFile && file.name.endsWith(".backup.json")
            }?.sortedByDescending { it.lastModified() } ?: return
            
            if (backupFiles.size > MAX_BACKUPS) {
                val filesToDelete = backupFiles.drop(MAX_BACKUPS)
                filesToDelete.forEach { file ->
                    if (file.delete()) {
                        println("AUTO_BACKUP: Backup antiguo eliminado - ${file.name}")
                    }
                }
            }
        } catch (e: Exception) {
            println("AUTO_BACKUP: Error limpiando backups antiguos - ${e.message}")
        }
    }
    
    /**
     * Obtiene el directorio de backups.
     */
    private fun getBackupDirectory(): File {
        return File(context.filesDir, BACKUP_FOLDER)
    }
    
    /**
     * Calcula checksum para validación de integridad.
     */
    private fun calculateChecksum(workers: List<Any>, workstations: List<Any>): String {
        val data = "${workers.size}_${workstations.size}_${System.currentTimeMillis() / 1000}"
        return data.hashCode().toString()
    }
    
    /**
     * Obtiene información de la versión de la app.
     */
    private fun getAppVersion(): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            "${packageInfo.versionName} (${packageInfo.versionCode})"
        } catch (e: Exception) {
            "Unknown"
        }
    }
    
    /**
     * Obtiene información del dispositivo.
     */
    private fun getDeviceInfo(): String {
        return "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL} (API ${android.os.Build.VERSION.SDK_INT})"
    }
    
    /**
     * Crea backup antes de operaciones críticas.
     */
    suspend fun createSafetyBackup(operation: String): BackupResult {
        return createBackup(BackupReason.BEFORE_OPERATION)
    }
    
    /**
     * Recuperación automática en caso de error.
     */
    suspend fun autoRecover(): RecoveryResult {
        return try {
            val backups = getAvailableBackups()
            val latestValidBackup = backups.firstOrNull { it.isValid }
            
            if (latestValidBackup != null) {
                println("AUTO_BACKUP: Iniciando recuperación automática desde ${latestValidBackup.file.name}")
                
                when (val restoreResult = restoreFromBackup(latestValidBackup.file)) {
                    is RestoreResult.Success -> {
                        RecoveryResult.Success("Sistema recuperado desde backup del ${Date(latestValidBackup.createdAt)}")
                    }
                    is RestoreResult.Error -> {
                        RecoveryResult.Error("Error en recuperación automática: ${restoreResult.message}")
                    }
                }
            } else {
                RecoveryResult.Error("No hay backups válidos disponibles para recuperación")
            }
        } catch (e: Exception) {
            RecoveryResult.Error("Error en recuperación automática: ${e.message}")
        }
    }
}

/**
 * Worker para backups automáticos en background.
 */
class AutoBackupWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        return try {
            val backupManager = AutoBackupManager.getInstance(applicationContext)
            val result = backupManager.createBackup(BackupReason.SCHEDULED)
            
            when (result) {
                is BackupResult.Success -> {
                    println("AUTO_BACKUP_WORKER: Backup automático completado")
                    Result.success()
                }
                is BackupResult.Error -> {
                    println("AUTO_BACKUP_WORKER: Error en backup automático - ${result.message}")
                    Result.retry()
                }
            }
        } catch (e: Exception) {
            println("AUTO_BACKUP_WORKER: Excepción en backup automático - ${e.message}")
            Result.failure()
        }
    }
}

/**
 * Data classes para backup
 */
@Serializable
data class SystemBackup(
    val version: String,
    val createdAt: Long,
    val reason: BackupReason,
    val workers: List<com.workstation.rotation.data.entities.Worker>,
    val workstations: List<com.workstation.rotation.data.entities.Workstation>,
    val workerWorkstations: List<com.workstation.rotation.data.entities.WorkerWorkstation>,
    val workerRestrictions: List<com.workstation.rotation.data.entities.WorkerRestriction>,
    val metadata: BackupMetadata
)

@Serializable
data class BackupMetadata(
    val appVersion: String,
    val deviceInfo: String,
    val totalWorkers: Int,
    val totalWorkstations: Int,
    val checksum: String? = null
)

data class BackupInfo(
    val file: File,
    val createdAt: Long,
    val reason: BackupReason,
    val size: Long,
    val metadata: BackupMetadata,
    val isValid: Boolean
)

data class BackupValidation(
    val isValid: Boolean,
    val errors: List<String>
)

sealed class BackupResult {
    data class Success(
        val backupFile: File,
        val backup: SystemBackup
    ) : BackupResult()
    
    data class Error(val message: String) : BackupResult()
}

sealed class RestoreResult {
    data class Success(
        val backup: SystemBackup,
        val safetyBackup: BackupResult
    ) : RestoreResult()
    
    data class Error(val message: String) : RestoreResult()
}

sealed class RecoveryResult {
    data class Success(val message: String) : RecoveryResult()
    data class Error(val message: String) : RecoveryResult()
}

@Serializable
enum class BackupReason {
    MANUAL,
    SCHEDULED,
    BEFORE_OPERATION,
    BEFORE_RESTORE,
    EMERGENCY
}