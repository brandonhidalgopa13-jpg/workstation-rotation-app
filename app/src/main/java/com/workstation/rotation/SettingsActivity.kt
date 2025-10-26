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
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
        val factory = WorkerViewModelFactory(database.workerDao(), database.workstationDao())
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
    }
    
    private fun setupListeners() {
        // Modo oscuro
        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            toggleDarkMode(isChecked)
        }
        
        // Respaldo y sincronización
        binding.btnCreateBackup.setOnClickListener {
            requestStoragePermissionIfNeeded {
                createBackup()
            }
        }
        
        binding.btnExportBackup.setOnClickListener {
            exportBackupLauncher.launch("backup_rotacion_${System.currentTimeMillis()}.json")
        }
        
        binding.btnImportBackup.setOnClickListener {
            importBackupLauncher.launch("application/json")
        }
        
        // Tutorial eliminado - funcionalidad no disponible
        
        binding.btnCertifyWorkers.setOnClickListener {
            showCertificationDialog()
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
     * Verifica si tenemos permisos de almacenamiento.
     */
    private fun hasStoragePermission(): Boolean {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            // Android 11+ usa Scoped Storage
            true
        } else {
            // Android 10 y anteriores
            androidx.core.content.ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        }
    }
    
    /**
     * Solicita permisos de almacenamiento si es necesario.
     */
    private fun requestStoragePermissionIfNeeded(onGranted: () -> Unit) {
        if (hasStoragePermission()) {
            onGranted()
        } else {
            if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.R) {
                androidx.core.app.ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    STORAGE_PERMISSION_REQUEST_CODE
                )
            } else {
                onGranted() // En Android 11+ no necesitamos permisos para archivos de la app
            }
        }
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
                
                // Obtener todos los datos en hilo de background
                val workers = withContext(Dispatchers.IO) {
                    database.workerDao().getAllWorkersSync()
                }
                val workstations = withContext(Dispatchers.IO) {
                    database.workstationDao().getAllWorkstationsSync()
                }
                val workerWorkstations = withContext(Dispatchers.IO) {
                    database.workerDao().getAllWorkerWorkstationsSync()
                }
                
                // Crear respaldo
                val backupJson = backupManager.createBackup(workers, workstations, workerWorkstations)
                
                // Guardar archivo
                val file = backupManager.saveBackupToFile(backupJson)
                
                androidx.appcompat.app.AlertDialog.Builder(this@SettingsActivity)
                    .setTitle("✅ Respaldo Creado")
                    .setMessage(
                        "Respaldo guardado exitosamente:\n\n" +
                        "📁 Archivo: ${file.name}\n" +
                        "📍 Ubicación: ${file.parent}\n" +
                        "📊 Tamaño: ${file.length() / 1024} KB\n\n" +
                        "Puedes encontrar el archivo en la carpeta de archivos de la aplicación."
                    )
                    .setPositiveButton("OK", null)
                    .setNeutralButton("Compartir") { _, _ ->
                        shareBackupFile(file)
                    }
                    .show()
                
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
                
                val workers = withContext(Dispatchers.IO) {
                    database.workerDao().getAllWorkersSync()
                }
                val workstations = withContext(Dispatchers.IO) {
                    database.workstationDao().getAllWorkstationsSync()
                }
                val workerWorkstations = withContext(Dispatchers.IO) {
                    database.workerDao().getAllWorkerWorkstationsSync()
                }
                
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
                
                withContext(Dispatchers.IO) {
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
                }
                
                Toast.makeText(this@SettingsActivity, "Datos importados exitosamente", Toast.LENGTH_SHORT).show()
                
            } catch (e: Exception) {
                Toast.makeText(this@SettingsActivity, "Error al importar datos: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    // Función resetTutorial eliminada - funcionalidad no disponible
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        if (requestCode == STORAGE_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                createBackup()
            } else {
                Toast.makeText(
                    this,
                    "Se necesitan permisos de almacenamiento para crear respaldos",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
    
    /**
     * Comparte un archivo de respaldo.
     */
    private fun shareBackupFile(file: File) {
        try {
            val uri = androidx.core.content.FileProvider.getUriForFile(
                this,
                "${packageName}.fileprovider",
                file
            )
            
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/json"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Respaldo Sistema de Rotación")
                putExtra(Intent.EXTRA_TEXT, "Respaldo de datos del Sistema de Rotación Inteligente")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            
            startActivity(Intent.createChooser(shareIntent, "Compartir respaldo"))
            
        } catch (e: Exception) {
            Toast.makeText(this, "Error al compartir archivo: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * Muestra el diálogo para certificar trabajadores (remover estado de entrenamiento).
     */
    private fun showCertificationDialog() {
        lifecycleScope.launch {
            try {
                val workersInTraining = withContext(Dispatchers.IO) {
                    workerViewModel.getWorkersInTraining()
                }
                
                if (workersInTraining.isEmpty()) {
                    androidx.appcompat.app.AlertDialog.Builder(this@SettingsActivity)
                        .setTitle("🎓 Certificación de Trabajadores")
                        .setMessage("No hay trabajadores en entrenamiento para certificar.")
                        .setPositiveButton("OK", null)
                        .show()
                    return@launch
                }
                
                val workerNames = workersInTraining.map { worker ->
                    "${worker.name} - ${worker.email}"
                }.toTypedArray()
                
                val selectedWorkers = BooleanArray(workersInTraining.size) { false }
                
                androidx.appcompat.app.AlertDialog.Builder(this@SettingsActivity)
                    .setTitle("🎓 Certificar Trabajadores")
                    .setMessage(
                        "Selecciona los trabajadores que han completado su entrenamiento y están listos para ser certificados:\n\n" +
                        "✅ Al certificar, el trabajador:\n" +
                        "• Deja de estar 'en entrenamiento'\n" +
                        "• Ya no necesita estar con su entrenador\n" +
                        "• Puede participar normalmente en rotaciones\n" +
                        "• Se convierte en trabajador completamente capacitado"
                    )
                    .setMultiChoiceItems(workerNames, selectedWorkers) { dialog, which, isChecked ->
                        selectedWorkers[which] = isChecked
                    }
                    .setPositiveButton("Certificar Seleccionados") { dialog, which ->
                        lifecycleScope.launch {
                            performCertification(workersInTraining, selectedWorkers)
                        }
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
                
            } catch (e: Exception) {
                Toast.makeText(
                    this@SettingsActivity,
                    "Error al cargar trabajadores en entrenamiento: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
    
    /**
     * Realiza la certificación de los trabajadores seleccionados.
     */
    private suspend fun performCertification(
        workersInTraining: List<com.workstation.rotation.data.entities.Worker>,
        selectedWorkers: BooleanArray
    ) {
        try {
            var certifiedCount = 0
            
            withContext(Dispatchers.IO) {
                selectedWorkers.forEachIndexed { index, isSelected ->
                    if (isSelected) {
                        workerViewModel.certifyWorker(workersInTraining[index].id)
                        certifiedCount++
                    }
                }
            }
            
            if (certifiedCount > 0) {
                androidx.appcompat.app.AlertDialog.Builder(this@SettingsActivity)
                    .setTitle("✅ Certificación Completada")
                    .setMessage(
                        "Se han certificado $certifiedCount trabajador(es) exitosamente.\n\n" +
                        "🎉 Los trabajadores certificados:\n" +
                        "• Ya no están en entrenamiento\n" +
                        "• Pueden participar normalmente en rotaciones\n" +
                        "• Son considerados trabajadores completamente capacitados\n\n" +
                        "Los cambios se aplicarán en la próxima rotación generada."
                    )
                    .setPositiveButton("Entendido", null)
                    .show()
            } else {
                Toast.makeText(
                    this@SettingsActivity,
                    "No se seleccionaron trabajadores para certificar",
                    Toast.LENGTH_SHORT
                ).show()
            }
            
        } catch (e: Exception) {
            Toast.makeText(
                this@SettingsActivity,
                "Error al certificar trabajadores: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
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
                "• Rotación inteligente con cambio forzado\n" +
                "• Certificación centralizada de trabajadores\n" +
                "• Tutorial interactivo guiado\n" +
                "• Modo oscuro automático\n" +
                "• Respaldo y sincronización completa\n\n" +
                "© 2024 - Todos los derechos reservados"
            )
            .setPositiveButton("OK", null)
            .show()
    }
}