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
import kotlinx.coroutines.launch
import java.io.File

/**
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * ⚙️ ACTIVIDAD DE CONFIGURACIONES - CENTRO DE PERSONALIZACIÓN
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 
 * 📋 FUNCIONES DE ESTA ACTIVIDAD:
 * 
 * 🌙 MODO OSCURO:
 *    - Alternar entre tema claro y oscuro
 *    - Guardar preferencia del usuario
 *    - Aplicar cambios inmediatamente
 * 
 * 💾 RESPALDO Y SINCRONIZACIÓN:
 *    - Crear respaldos de datos
 *    - Exportar configuración completa
 *    - Importar datos desde archivo
 *    - Validar integridad de respaldos
 * 
 * 📚 TUTORIAL Y AYUDA:
 *    - Reiniciar tutorial interactivo
 *    - Configurar pistas y ayuda
 *    - Acceso a documentación
 * 
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 */
class SettingsActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivitySettingsBinding
    private lateinit var prefs: SharedPreferences
    private lateinit var backupManager: BackupManager
    
    companion object {
        private const val PREFS_NAME = "app_settings"
        private const val KEY_DARK_MODE = "dark_mode_enabled"
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
    }
    
    private fun setupListeners() {
        // Modo oscuro
        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            toggleDarkMode(isChecked)
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
        
        // Tutorial
        binding.btnResetTutorial.setOnClickListener {
            resetTutorial()
        }
        
        // Información de la app
        binding.btnAppInfo.setOnClickListener {
            showAppInfo()
        }
    }
    
    /**
     * Alterna el modo oscuro.
     */
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
        
        // Mostrar mensaje
        val message = if (enabled) "Modo oscuro activado 🌙" else "Modo claro activado ☀️"
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
    
    /**
     * Actualiza el texto del modo oscuro.
     */
    private fun updateDarkModeText(isDarkMode: Boolean) {
        val text = if (isDarkMode) {
            "🌙 Modo Oscuro Activado\nTema oscuro para mejor visualización nocturna"
        } else {
            "☀️ Modo Claro Activado\nTema claro para mejor visualización diurna"
        }
        binding.tvDarkModeDescription.text = text
    }
    
    /**
     * Crea un respaldo de los datos actuales.
     */
    private fun createBackup() {
        lifecycleScope.launch {
            try {
                binding.btnCreateBackup.isEnabled = false
                binding.btnCreateBackup.text = "Creando respaldo..."
                
                val database = AppDatabase.getDatabase(this@SettingsActivity)
                
                // Obtener todos los datos
                val workers = database.workerDao().getAllWorkersSync()
                val workstations = database.workstationDao().getAllWorkstationsSync()
                val workerWorkstations = database.workerDao().getAllWorkerWorkstationsSync()
                
                // Crear respaldo
                val backupJson = backupManager.createBackup(workers, workstations, workerWorkstations)
                
                // Guardar archivo
                val file = backupManager.saveBackupToFile(backupJson)
                
                Toast.makeText(
                    this@SettingsActivity,
                    "Respaldo creado: ${file.name}",
                    Toast.LENGTH_LONG
                ).show()
                
            } catch (e: Exception) {
                Toast.makeText(
                    this@SettingsActivity,
                    "Error al crear respaldo: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                binding.btnCreateBackup.isEnabled = true
                binding.btnCreateBackup.text = "💾 Crear Respaldo"
            }
        }
    }
    
    /**
     * Exporta un respaldo a la ubicación seleccionada.
     */
    private fun exportBackup(uri: Uri) {
        lifecycleScope.launch {
            try {
                val database = AppDatabase.getDatabase(this@SettingsActivity)
                
                val workers = database.workerDao().getAllWorkersSync()
                val workstations = database.workstationDao().getAllWorkstationsSync()
                val workerWorkstations = database.workerDao().getAllWorkerWorkstationsSync()
                
                val backupJson = backupManager.createBackup(workers, workstations, workerWorkstations)
                
                // Escribir al URI seleccionado
                contentResolver.openOutputStream(uri)?.use { output ->
                    output.write(backupJson.toByteArray())
                }
                
                Toast.makeText(this@SettingsActivity, "Respaldo exportado exitosamente", Toast.LENGTH_SHORT).show()
                
            } catch (e: Exception) {
                Toast.makeText(this@SettingsActivity, "Error al exportar: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    /**
     * Importa un respaldo desde el archivo seleccionado.
     */
    private fun importBackup(uri: Uri) {
        lifecycleScope.launch {
            try {
                // Leer archivo
                val backupJson = contentResolver.openInputStream(uri)?.use { input ->
                    input.readBytes().toString(Charsets.UTF_8)
                } ?: return@launch
                
                // Validar respaldo
                val backupData = backupManager.restoreBackup(backupJson)
                val validation = backupManager.validateBackup(backupData)
                
                if (!validation.isValid) {
                    Toast.makeText(
                        this@SettingsActivity,
                        "Respaldo inválido: ${validation.errors.joinToString(", ")}",
                        Toast.LENGTH_LONG
                    ).show()
                    return@launch
                }
                
                // Mostrar confirmación
                androidx.appcompat.app.AlertDialog.Builder(this@SettingsActivity)
                    .setTitle("Confirmar Importación")
                    .setMessage(
                        "¿Deseas importar este respaldo?\n\n" +
                        "Trabajadores: ${validation.workerCount}\n" +
                        "Estaciones: ${validation.workstationCount}\n\n" +
                        "⚠️ Esto reemplazará todos los datos actuales."
                    )
                    .setPositiveButton("Importar") { _, _ ->
                        performImport(backupData)
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
                
            } catch (e: Exception) {
                Toast.makeText(this@SettingsActivity, "Error al leer respaldo: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    /**
     * Realiza la importación de datos.
     */
    private fun performImport(backupData: BackupManager.BackupData) {
        lifecycleScope.launch {
            try {
                val database = AppDatabase.getDatabase(this@SettingsActivity)
                
                // Limpiar datos existentes
                database.workerDao().deleteAllWorkerWorkstations()
                database.workerDao().deleteAllWorkers()
                database.workstationDao().deleteAllWorkstations()
                
                // Importar nuevos datos
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
                
                Toast.makeText(this@SettingsActivity, "Datos importados exitosamente", Toast.LENGTH_SHORT).show()
                
            } catch (e: Exception) {
                Toast.makeText(this@SettingsActivity, "Error al importar datos: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    /**
     * Reinicia el tutorial.
     */
    private fun resetTutorial() {
        val tutorialPrefs = getSharedPreferences("tutorial_prefs", Context.MODE_PRIVATE)
        tutorialPrefs.edit()
            .putBoolean("tutorial_completed", false)
            .putBoolean("tutorial_enabled", true)
            .apply()
        
        Toast.makeText(this, "Tutorial reiniciado. Se mostrará en la próxima apertura.", Toast.LENGTH_SHORT).show()
    }
    
    /**
     * Muestra información de la aplicación.
     */
    private fun showAppInfo() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("📱 Información de la Aplicación")
            .setMessage(
                "Sistema de Rotación Inteligente\n" +
                "Versión: 2.1.0\n" +
                "Desarrollador: Brandon Josué Hidalgo Paz\n\n" +
                "Funcionalidades:\n" +
                "• Gestión de trabajadores y estaciones\n" +
                "• Sistema de entrenamiento avanzado\n" +
                "• Rotación inteligente automática\n" +
                "• Certificación de trabajadores\n" +
                "• Tutorial interactivo\n" +
                "• Modo oscuro\n" +
                "• Respaldo y sincronización\n\n" +
                "© 2024 - Todos los derechos reservados"
            )
            .setPositiveButton("OK", null)
            .show()
    }
}