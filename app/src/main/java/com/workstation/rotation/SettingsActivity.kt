package com.workstation.rotation

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import com.workstation.rotation.data.database.AppDatabase
import com.workstation.rotation.data.sync.BackupManager
import com.workstation.rotation.databinding.ActivitySettingsBinding
import com.workstation.rotation.viewmodels.WorkerViewModel
import com.workstation.rotation.viewmodels.WorkerViewModelFactory
import com.workstation.rotation.notifications.NotificationSettingsActivity
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Actividad de configuraciones del sistema
 */
class SettingsActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivitySettingsBinding
    private lateinit var prefs: SharedPreferences
    private lateinit var backupManager: BackupManager
    private lateinit var workerViewModel: WorkerViewModel
    
    companion object {
        private const val PREFS_NAME = "app_settings"
        private const val KEY_DARK_MODE = "dark_mode_enabled"
        private const val STORAGE_PERMISSION_REQUEST_CODE = 100
    }
    
    // Launcher para seleccionar archivo de respaldo
    private val importBackupLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { importBackup(it) }
    }
    
    // Launcher para guardar archivo de respaldo
    private val exportBackupLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let { exportBackup(it) }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupComponents()
        setupUI()
        setupListeners()
    }
    
    private fun setupComponents() {
        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        backupManager = BackupManager(this)
        
        // Inicializar WorkerViewModel
        val database = AppDatabase.getDatabase(this)
        val factory = WorkerViewModelFactory(
            database.workerDao(), 
            database.workstationDao(), 
            database.workerRestrictionDao(),
            database.workerWorkstationCapabilityDao()
        )
        workerViewModel = factory.create(WorkerViewModel::class.java)
    }
    
    private fun setupUI() {
        // Configurar toolbar
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
        
        // Configurar estado inicial del switch de modo oscuro
        val isDarkMode = prefs.getBoolean(KEY_DARK_MODE, false)
        binding.switchDarkMode.isChecked = isDarkMode
        
        // Actualizar texto según el modo actual
        updateDarkModeText(isDarkMode)
        
        // Configurar estado inicial de seguridad
        setupSecurityUI()
    }
    
    private fun setupSecurityUI() {
        val isSecurityEnabled = com.workstation.rotation.security.SecurityConfig.isSecurityEnabled(this)
        val isBiometricEnabled = com.workstation.rotation.security.SecurityConfig.isBiometricEnabled(this)
        
        binding.switchSecurity.isChecked = isSecurityEnabled
        binding.switchBiometric.isChecked = isBiometricEnabled
        
        // Mostrar/ocultar opciones de seguridad según el estado
        binding.layoutBiometric.visibility = if (isSecurityEnabled) android.view.View.VISIBLE else android.view.View.GONE
        binding.btnSecuritySettings.visibility = if (isSecurityEnabled) android.view.View.VISIBLE else android.view.View.GONE
    }
    
    private fun setupListeners() {
        // Modo oscuro
        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            toggleDarkMode(isChecked)
        }
        
        // Sistema de seguridad
        binding.switchSecurity.setOnCheckedChangeListener { _, isChecked ->
            toggleSecurity(isChecked)
        }
        
        binding.switchBiometric.setOnCheckedChangeListener { _, isChecked ->
            toggleBiometric(isChecked)
        }
        
        binding.btnSecuritySettings.setOnClickListener {
            showSecuritySettings()
        }
        
        // Respaldo y sincronización
        binding.btnCreateBackup.setOnClickListener {
            createBackup()
        }
        
        binding.btnExportBackup.setOnClickListener {
            exportBackupLauncher.launch("backup_rotacion_${System.currentTimeMillis()}.json")
        }
        
        binding.btnImportBackup.setOnClickListener {
            importBackupLauncher.launch("application/json")
        }
        
        // Notificaciones
        binding.btnNotificationSettings.setOnClickListener {
            startActivity(Intent(this, NotificationSettingsActivity::class.java))
        }
        
        // Información de la app
        binding.btnAppInfo.setOnClickListener {
            showAppInfo()
        }
    }
    
    private fun toggleDarkMode(enabled: Boolean) {
        // Guardar preferencia
        prefs.edit().putBoolean(KEY_DARK_MODE, enabled).apply()
        
        // Aplicar tema
        if (enabled) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
        
        // Actualizar texto
        updateDarkModeText(enabled)
        
        val message = if (enabled) {
            "🌙 Modo oscuro activado"
        } else {
            "☀️ Modo claro activado"
        }
        
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
    
    private fun updateDarkModeText(isDarkMode: Boolean) {
        val text = if (isDarkMode) {
            "🌙 Modo Oscuro Activado"
        } else {
            "☀️ Modo Claro Activado"
        }
        binding.tvDarkModeDescription.text = text
    }
    
    private fun toggleSecurity(enabled: Boolean) {
        com.workstation.rotation.security.SecurityConfig.setSecurityEnabled(this, enabled)
        
        // Mostrar/ocultar opciones de seguridad
        binding.layoutBiometric.visibility = if (enabled) android.view.View.VISIBLE else android.view.View.GONE
        binding.btnSecuritySettings.visibility = if (enabled) android.view.View.VISIBLE else android.view.View.GONE
        
        val message = if (enabled) {
            "🔐 Sistema de seguridad activado. Deberás iniciar sesión la próxima vez."
        } else {
            "🔓 Sistema de seguridad desactivado"
        }
        
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        
        // Si se activa, mostrar información de credenciales de prueba
        if (enabled) {
            showSecurityInfoDialog()
        }
    }
    
    private fun toggleBiometric(enabled: Boolean) {
        com.workstation.rotation.security.SecurityConfig.setBiometricEnabled(this, enabled)
        
        val message = if (enabled) {
            "👆 Autenticación biométrica habilitada"
        } else {
            "🔑 Solo login con contraseña"
        }
        
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
    
    private fun showSecuritySettings() {
        val config = com.workstation.rotation.security.SecurityConfig.getConfigSummary(this)
        
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("⚙️ Configuración de Seguridad")
            .setMessage(config)
            .setPositiveButton("Entendido", null)
            .show()
    }
    
    private fun showSecurityInfoDialog() {
        val message = """
            🔐 Sistema de Seguridad Activado
            
            Credenciales de prueba:
            
            👤 Usuario: admin
            🔑 Contraseña: admin123
            🎭 Rol: Super Admin
            
            👤 Usuario: supervisor
            🔑 Contraseña: super123
            🎭 Rol: Supervisor
            
            👤 Usuario: viewer
            🔑 Contraseña: view123
            🎭 Rol: Visualizador
            
            ⚠️ Nota: Estas son credenciales de prueba. En producción deberías cambiarlas.
        """.trimIndent()
        
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("🔐 Información de Seguridad")
            .setMessage(message)
            .setPositiveButton("Entendido", null)
            .show()
    }
    
    private fun createBackup() {
        lifecycleScope.launch {
            try {
                binding.btnCreateBackup.isEnabled = false
                binding.btnCreateBackup.text = "Creando respaldo..."
                
                val database = AppDatabase.getDatabase(this@SettingsActivity)
                
                val workers = withContext(Dispatchers.IO) {
                    database.workerDao().getAllWorkersSync()
                }
                val workstations = withContext(Dispatchers.IO) {
                    database.workstationDao().getAllWorkstationsSync()
                }
                val workerWorkstations = withContext(Dispatchers.IO) {
                    database.workerDao().getAllWorkerWorkstationsSync()
                }
                
                val backupJson = backupManager.createMigrationBackup(workers, workstations, workerWorkstations)
                val file = backupManager.saveBackupToFile(backupJson)
                
                Toast.makeText(this@SettingsActivity, "Respaldo creado: ${file.name}", Toast.LENGTH_SHORT).show()
                
            } catch (e: Exception) {
                Toast.makeText(this@SettingsActivity, "Error al crear respaldo: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                binding.btnCreateBackup.isEnabled = true
                binding.btnCreateBackup.text = "💾 Crear Respaldo"
            }
        }
    }
    
    private fun exportBackup(uri: Uri) {
        lifecycleScope.launch {
            try {
                val database = AppDatabase.getDatabase(this@SettingsActivity)
                
                val workers = withContext(Dispatchers.IO) {
                    database.workerDao().getAllWorkersSync()
                }
                val workstations = withContext(Dispatchers.IO) {
                    database.workstationDao().getAllWorkstationsSync()
                }
                val workerWorkstations = withContext(Dispatchers.IO) {
                    database.workerDao().getAllWorkerWorkstationsSync()
                }
                
                val backupJson = backupManager.createMigrationBackup(workers, workstations, workerWorkstations)
                
                contentResolver.openOutputStream(uri)?.use { output ->
                    output.write(backupJson.toByteArray())
                }
                
                Toast.makeText(this@SettingsActivity, "Respaldo exportado exitosamente", Toast.LENGTH_SHORT).show()
                
            } catch (e: Exception) {
                Toast.makeText(this@SettingsActivity, "Error al exportar: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun importBackup(uri: Uri) {
        lifecycleScope.launch {
            try {
                val backupJson = contentResolver.openInputStream(uri)?.use { input ->
                    input.readBytes().toString(Charsets.UTF_8)
                } ?: return@launch
                
                val backupData = backupManager.restoreBackup(backupJson)
                val validation = backupManager.validateBackup(backupData)
                
                if (!validation.isValid) {
                    Toast.makeText(this@SettingsActivity, "Respaldo inválido", Toast.LENGTH_LONG).show()
                    return@launch
                }
                
                performImport(backupData)
                
            } catch (e: Exception) {
                Toast.makeText(this@SettingsActivity, "Error al leer respaldo: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun performImport(backupData: BackupManager.BackupData) {
        lifecycleScope.launch {
            try {
                val database = AppDatabase.getDatabase(this@SettingsActivity)
                
                withContext(Dispatchers.IO) {
                    // Limpiar datos existentes
                    database.workerDao().deleteAllWorkerWorkstations()
                    database.workerDao().deleteAllWorkers()
                    database.workstationDao().deleteAllWorkstations()
                    
                    // Importar datos
                    backupData.workstations.forEach { ws ->
                        database.workstationDao().insertWorkstation(
                            com.workstation.rotation.data.entities.Workstation(
                                id = ws.id,
                                name = ws.name,
                                requiredWorkers = ws.requiredWorkers,
                                isPriority = ws.isPriority,
                                isActive = ws.isActive
                            )
                        )
                    }
                    
                    backupData.workers.forEach { w ->
                        database.workerDao().insertWorker(
                            com.workstation.rotation.data.entities.Worker(
                                id = w.id,
                                name = w.name,
                                email = w.email,
                                availabilityPercentage = w.availabilityPercentage,
                                restrictionNotes = w.restrictionNotes,
                                isTrainer = w.isTrainer,
                                isTrainee = w.isTrainee,
                                trainerId = w.trainerId,
                                trainingWorkstationId = w.trainingWorkstationId,
                                isActive = w.isActive,
                                isCertified = w.isCertified,
                                certificationDate = w.certificationDate,
                                isLeader = w.isLeader,
                                leaderWorkstationId = w.leaderWorkstationId,
                                leadershipType = w.leadershipType,
                                currentWorkstationId = w.currentWorkstationId,
                                rotationsInCurrentStation = w.rotationsInCurrentStation,
                                lastRotationTimestamp = w.lastRotationTimestamp
                            )
                        )
                    }
                    
                    backupData.workerWorkstations.forEach { ww ->
                        database.workerDao().insertWorkerWorkstation(
                            com.workstation.rotation.data.entities.WorkerWorkstation(
                                workerId = ww.workerId,
                                workstationId = ww.workstationId
                            )
                        )
                    }
                }
                
                Toast.makeText(this@SettingsActivity, "Datos importados exitosamente", Toast.LENGTH_SHORT).show()
                
            } catch (e: Exception) {
                Toast.makeText(this@SettingsActivity, "Error al importar datos: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun showAppInfo() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("📱 Información de la Aplicación")
            .setMessage(
                "🏭 REWS - Rotation and Workstation System\n" +
                "📱 Versión: 3.1.0\n" +
                "👨‍💻 Desarrollador: Brandon Josué Hidalgo Paz\n" +
                "📅 Año: 2024\n\n" +
                "🚀 Funcionalidades:\n" +
                "• Analytics Avanzados\n" +
                "• Dashboard Ejecutivo\n" +
                "• Sistema de rotación inteligente\n" +
                "• Gestión completa de trabajadores\n" +
                "• Respaldo y sincronización\n\n" +
                "© 2024 - Todos los derechos reservados"
            )
            .setPositiveButton("Cerrar", null)
            .show()
    }
}