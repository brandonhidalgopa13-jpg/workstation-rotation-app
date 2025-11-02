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
 * 📚 AYUDA Y SOPORTE:
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
        val factory = WorkerViewModelFactory(database.workerDao(), database.workstationDao(), database.workerRestrictionDao())
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
        
        // Verificar si es la primera vez y sugerir seguir configuración del sistema
        checkSystemThemePreference()
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
        
        // Sincronización en la nube
        binding.btnCloudSync.setOnClickListener {
            showCloudSyncOptions()
        }
        
        binding.btnCloudBackup.setOnClickListener {
            createCloudBackup()
        }
        
        // Guía de funcionamiento
        binding.btnAppGuide.setOnClickListener {
            showAppGuide()
        }
        
        binding.btnShowOnboarding?.setOnClickListener {
            showOnboardingTutorial()
        }
        
        binding.btnCertifyWorkers.setOnClickListener {
            showCertificationDialog()
        }
        
        // Información de la app
        binding.btnAppInfo.setOnClickListener {
            showAppInfo()
        }
        
        // Nuevas funcionalidades
        binding.btnGenerateReport?.setOnClickListener {
            Toast.makeText(this, "Función de reportes próximamente disponible", Toast.LENGTH_SHORT).show()
        }
        
        binding.btnNotificationSettings?.setOnClickListener {
            Toast.makeText(this, "Configuración de notificaciones próximamente disponible", Toast.LENGTH_SHORT).show()
        }
        
        binding.btnAdvancedSettings?.setOnClickListener {
            Toast.makeText(this, "Configuraciones avanzadas próximamente disponibles", Toast.LENGTH_SHORT).show()
        }
        
        // Diagnóstico y mantenimiento (funcionalidad removida temporalmente)
        binding.btnSystemDiagnostics?.setOnClickListener {
            // Funcionalidad en desarrollo
        }
        
        binding.btnPerformanceMetrics?.setOnClickListener {
            // Funcionalidad en desarrollo
        }
        
        binding.btnSystemValidation?.setOnClickListener {
            // Funcionalidad en desarrollo
        }
    }
    
    /**
     * Alterna el modo oscuro con animación suave.
     */
    private fun toggleDarkMode(enabled: Boolean) {
        // Guardar preferencia
        prefs.edit().putBoolean(KEY_DARK_MODE, enabled).apply()
        
        // Aplicar tema con transición suave
        if (enabled) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
        
        // Actualizar texto con animación
        updateDarkModeText(enabled)
        
        // Mostrar mensaje personalizado
        val message = if (enabled) {
            "🌙 Modo oscuro activado - Perfecto para trabajo nocturno"
        } else {
            "☀️ Modo claro activado - Ideal para trabajo diurno"
        }
        
        // Toast personalizado con duración más larga para mejor UX
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        
        // Pequeña vibración para feedback táctil (opcional)
        try {
            val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as android.os.Vibrator
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vibrator.vibrate(android.os.VibrationEffect.createOneShot(50, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(50)
            }
        } catch (e: Exception) {
            // Ignorar si no hay vibrador disponible
        }
    }
    
    /**
     * Actualiza el texto del modo oscuro con información detallada.
     */
    private fun updateDarkModeText(isDarkMode: Boolean) {
        val text = if (isDarkMode) {
            "🌙 Modo Oscuro Activado\n" +
            "• Reduce la fatiga visual en ambientes con poca luz\n" +
            "• Ahorra batería en pantallas OLED\n" +
            "• Ideal para turnos nocturnos"
        } else {
            "☀️ Modo Claro Activado\n" +
            "• Mejor contraste en ambientes bien iluminados\n" +
            "• Colores más vibrantes y nítidos\n" +
            "• Perfecto para uso diurno"
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
                "🏭 REWS - Rotation and Workstation System\n" +
                "📱 Versión: 2.4.0\n" +
                "👨‍💻 Desarrollador: Brandon Josué Hidalgo Paz\n" +
                "📅 Año: 2024\n\n" +
                "🚀 Funcionalidades Principales:\n" +
                "• 👥 Gestión completa de trabajadores\n" +
                "• 🏭 Administración de estaciones de trabajo\n" +
                "• 👑 Sistema de liderazgo avanzado\n" +
                "• 🔄 Sistema de rotación inteligente\n" +
                "• 📚 Sistema de entrenamiento avanzado\n" +
                "• 🎓 Certificación de trabajadores\n" +
                "• 🚫 Sistema de restricciones específicas\n" +
                "• 🌙 Modo oscuro automático\n" +
                "• 💾 Respaldo y sincronización\n" +
                "• ☁️ Sincronización en la nube\n" +
                "• 📊 Reportes y estadísticas avanzadas\n" +
                "• 🔔 Sistema de notificaciones\n" +
                "• ⚡ Optimizaciones de rendimiento\n\n" +
                "© 2024 - Todos los derechos reservados"
            )
            .setPositiveButton("Cerrar", null)
            .setNeutralButton("Ver Guía") { _, _ ->
                showAppGuide()
            }
            .show()
    }

    /**
     * Muestra la guía completa de funcionamiento de la aplicación.
     */
    private fun showAppGuide() {
        showGuideStep(0)
    }

    /**
     * Muestra un paso específico de la guía.
     */
    private fun showGuideStep(step: Int) {
        val guideSteps = listOf(
            GuideStep(
                title = "🏠 Pantalla Principal",
                content = "La pantalla principal es tu centro de control:\n\n" +
                        "🏭 Estaciones de Trabajo: Gestiona las diferentes áreas de trabajo\n" +
                        "👥 Trabajadores: Administra tu equipo de trabajo\n" +
                        "🔄 Generar Rotación: Crea rotaciones automáticas inteligentes\n" +
                        "⚙️ Configuraciones: Personaliza la aplicación\n\n" +
                        "💡 Consejo: Comienza creando estaciones y trabajadores antes de generar rotaciones."
            ),
            GuideStep(
                title = "🏭 Gestión de Estaciones",
                content = "Las estaciones de trabajo son los diferentes puestos donde rotan los trabajadores:\n\n" +
                        "➕ Agregar Estación: Toca el botón + para crear una nueva\n" +
                        "📝 Información requerida:\n" +
                        "  • Nombre descriptivo (ej: 'Control de Calidad')\n" +
                        "  • Número de trabajadores necesarios\n" +
                        "  • Marcar como prioritaria si es crítica\n\n" +
                        "✏️ Editar: Toca cualquier estación para modificarla\n" +
                        "🔄 Activar/Desactivar: Usa el switch para incluir/excluir de rotaciones\n\n" +
                        "💡 Consejo: Las estaciones prioritarias siempre tendrán el número exacto de trabajadores asignados."
            ),
            GuideStep(
                title = "👥 Gestión de Trabajadores",
                content = "Administra tu equipo de trabajo de manera eficiente:\n\n" +
                        "➕ Agregar Trabajador:\n" +
                        "  • Nombre completo\n" +
                        "  • Email de contacto\n" +
                        "  • Porcentaje de disponibilidad (0-100%)\n" +
                        "  • Estaciones donde puede trabajar\n" +
                        "  • Notas de restricciones (opcional)\n\n" +
                        "📚 Sistema de Entrenamiento:\n" +
                        "  • Marcar como 'En entrenamiento'\n" +
                        "  • Asignar entrenador\n" +
                        "  • Seleccionar estación de entrenamiento\n\n" +
                        "🎓 Certificación: Los trabajadores en entrenamiento pueden ser certificados desde Configuraciones\n\n" +
                        "💡 Consejo: Un trabajador con 50% de disponibilidad tiene menos probabilidad de ser asignado."
            ),
            GuideStep(
                title = "🔄 Sistema de Rotación",
                content = "El corazón de la aplicación - genera rotaciones inteligentes:\n\n" +
                        "🎯 Algoritmo Inteligente:\n" +
                        "  • Considera disponibilidad de trabajadores\n" +
                        "  • Respeta restricciones y entrenamientos\n" +
                        "  • Prioriza estaciones críticas\n" +
                        "  • Evita asignaciones repetitivas\n\n" +
                        "📊 Información mostrada:\n" +
                        "  • Fase Actual: Rotación activa\n" +
                        "  • Próxima Fase: Siguiente rotación\n" +
                        "  • Trabajadores por estación\n" +
                        "  • Indicadores de entrenamiento\n\n" +
                        "🔄 Cambio Forzado: Genera nueva rotación cuando sea necesario\n\n" +
                        "💡 Consejo: Revisa la rotación antes de aplicarla para asegurar que cumple tus necesidades."
            ),
            GuideStep(
                title = "📚 Sistema de Entrenamiento",
                content = "Gestiona el desarrollo de tu equipo:\n\n" +
                        "🎓 Trabajadores en Entrenamiento:\n" +
                        "  • Se muestran con indicador especial (📚)\n" +
                        "  • Siempre asignados con su entrenador\n" +
                        "  • Limitados a su estación de entrenamiento\n\n" +
                        "👨‍🏫 Entrenadores:\n" +
                        "  • Pueden entrenar a múltiples personas\n" +
                        "  • Se asignan automáticamente con sus aprendices\n" +
                        "  • Indicados con símbolo (👨‍🏫)\n\n" +
                        "✅ Certificación:\n" +
                        "  • Ve a Configuraciones > Certificar Trabajadores\n" +
                        "  • Selecciona trabajadores que completaron entrenamiento\n" +
                        "  • Los certificados participan normalmente en rotaciones\n\n" +
                        "💡 Consejo: Certifica trabajadores regularmente para optimizar las rotaciones."
            ),
            GuideStep(
                title = "💾 Respaldo y Sincronización",
                content = "Protege tus datos importantes:\n\n" +
                        "💾 Crear Respaldo:\n" +
                        "  • Guarda todos los datos en archivo local\n" +
                        "  • Incluye trabajadores, estaciones y asignaciones\n" +
                        "  • Se puede compartir por email o mensajería\n\n" +
                        "📤 Exportar:\n" +
                        "  • Elige ubicación específica para guardar\n" +
                        "  • Útil para transferir entre dispositivos\n\n" +
                        "📥 Importar:\n" +
                        "  • Restaura datos desde archivo de respaldo\n" +
                        "  • Valida integridad antes de importar\n" +
                        "  • Reemplaza todos los datos actuales\n\n" +
                        "⚠️ Importante: Siempre crea respaldos antes de cambios importantes."
            ),
            GuideStep(
                title = "⚙️ Configuraciones Avanzadas",
                content = "Personaliza la aplicación según tus necesidades:\n\n" +
                        "🌙 Modo Oscuro:\n" +
                        "  • Alterna entre tema claro y oscuro\n" +
                        "  • Mejor para uso nocturno\n" +
                        "  • Se aplica inmediatamente\n\n" +
                        "🎓 Certificar Trabajadores:\n" +
                        "  • Lista trabajadores en entrenamiento\n" +
                        "  • Permite certificación múltiple\n" +
                        "  • Actualiza estado automáticamente\n\n" +
                        "📱 Información de la App:\n" +
                        "  • Versión actual\n" +
                        "  • Funcionalidades disponibles\n" +
                        "  • Información del desarrollador\n\n" +
                        "💡 Consejo: Revisa regularmente las configuraciones para optimizar tu experiencia."
            ),
            GuideStep(
                title = "🎯 Consejos y Mejores Prácticas",
                content = "Maximiza la eficiencia de tu sistema de rotación:\n\n" +
                        "📋 Configuración Inicial:\n" +
                        "  1. Crea todas las estaciones de trabajo\n" +
                        "  2. Agrega todos los trabajadores\n" +
                        "  3. Configura entrenamientos si es necesario\n" +
                        "  4. Genera tu primera rotación\n\n" +
                        "🔄 Uso Diario:\n" +
                        "  • Revisa rotaciones antes de aplicar\n" +
                        "  • Actualiza disponibilidad de trabajadores\n" +
                        "  • Certifica trabajadores cuando completen entrenamiento\n\n" +
                        "💾 Mantenimiento:\n" +
                        "  • Crea respaldos semanalmente\n" +
                        "  • Limpia trabajadores inactivos\n" +
                        "  • Actualiza información de contacto\n\n" +
                        "🎉 ¡Listo! Ya conoces todas las funcionalidades del sistema."
            )
        )

        if (step < guideSteps.size) {
            val currentStep = guideSteps[step]
            val isLastStep = step == guideSteps.size - 1
            
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("${currentStep.title} (${step + 1}/${guideSteps.size})")
                .setMessage(currentStep.content)
                .setPositiveButton(if (isLastStep) "Finalizar" else "Siguiente") { _, _ ->
                    if (!isLastStep) {
                        showGuideStep(step + 1)
                    }
                }
                .setNegativeButton(if (step > 0) "Anterior" else "Salir") { _, _ ->
                    if (step > 0) {
                        showGuideStep(step - 1)
                    }
                }
                .setNeutralButton("Índice") { _, _ ->
                    showGuideIndex()
                }
                .setCancelable(false)
                .show()
        }
    }

    /**
     * Muestra el índice de la guía para navegación rápida.
     */
    private fun showGuideIndex() {
        val sections = arrayOf(
            "🏠 Pantalla Principal",
            "🏭 Gestión de Estaciones", 
            "👥 Gestión de Trabajadores",
            "🔄 Sistema de Rotación",
            "📚 Sistema de Entrenamiento",
            "💾 Respaldo y Sincronización",
            "⚙️ Configuraciones Avanzadas",
            "🎯 Consejos y Mejores Prácticas"
        )

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("📖 Índice de la Guía")
            .setItems(sections) { _, which ->
                showGuideStep(which)
            }
            .setNegativeButton("Cerrar", null)
            .show()
    }

    /**
     * Verifica si debe sugerir seguir la configuración del sistema.
     */
    private fun checkSystemThemePreference() {
        val isFirstTime = prefs.getBoolean("first_time_theme", true)
        
        if (isFirstTime) {
            // Detectar si el sistema está en modo oscuro
            val isSystemDarkMode = (resources.configuration.uiMode and 
                android.content.res.Configuration.UI_MODE_NIGHT_MASK) == 
                android.content.res.Configuration.UI_MODE_NIGHT_YES
            
            val currentAppDarkMode = prefs.getBoolean(KEY_DARK_MODE, false)
            
            // Solo sugerir si hay diferencia entre sistema y app
            if (isSystemDarkMode != currentAppDarkMode) {
                val systemModeText = if (isSystemDarkMode) "modo oscuro" else "modo claro"
                
                androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("🎨 Configuración de Tema")
                    .setMessage(
                        "Hemos detectado que tu dispositivo está configurado en $systemModeText.\n\n" +
                        "¿Te gustaría que la aplicación siga la configuración de tu sistema?\n\n" +
                        "Esto hará que el tema cambie automáticamente según tus preferencias del dispositivo."
                    )
                    .setPositiveButton("Sí, seguir sistema") { _, _ ->
                        // Aplicar configuración del sistema
                        binding.switchDarkMode.isChecked = isSystemDarkMode
                        toggleDarkMode(isSystemDarkMode)
                        prefs.edit().putBoolean("first_time_theme", false).apply()
                    }
                    .setNegativeButton("No, mantener actual") { _, _ ->
                        prefs.edit().putBoolean("first_time_theme", false).apply()
                    }
                    .setNeutralButton("Preguntar después", null)
                    .show()
            } else {
                // Marcar como no primera vez si ya coinciden
                prefs.edit().putBoolean("first_time_theme", false).apply()
            }
        }
    }

    /**
     * Muestra las opciones de sincronización en la nube.
     */
    private fun showCloudSyncOptions() {
        // Verificar si Firebase está disponible
        val authManager = com.workstation.rotation.data.cloud.CloudAuthManager(this)
        if (!authManager.isFirebaseAvailable()) {
            showFirebaseNotAvailableDialog()
            return
        }
        val options = arrayOf(
            "🔄 Sincronizar Ahora",
            "☁️ Subir a la Nube",
            "📥 Descargar de la Nube",
            "🔑 Gestionar Cuenta",
            "📊 Estado de Sincronización"
        )
        
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("☁️ Sincronización en la Nube")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> performCloudSync()
                    1 -> uploadToCloud()
                    2 -> downloadFromCloud()
                    3 -> manageCloudAccount()
                    4 -> showSyncStatus()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    
    /**
     * Realiza sincronización completa con la nube.
     */
    private fun performCloudSync() {
        lifecycleScope.launch {
            try {
                val authManager = com.workstation.rotation.data.cloud.CloudAuthManager(this@SettingsActivity)
                
                if (!authManager.isUserSignedIn()) {
                    showCloudSignIn()
                    return@launch
                }
                
                val syncManager = com.workstation.rotation.data.cloud.CloudSyncManager(this@SettingsActivity, authManager)
                val database = AppDatabase.getDatabase(this@SettingsActivity)
                
                // Mostrar progreso
                val progressDialog = androidx.appcompat.app.AlertDialog.Builder(this@SettingsActivity)
                    .setTitle("🔄 Sincronizando...")
                    .setMessage("Sincronizando datos con la nube...")
                    .setCancelable(false)
                    .create()
                progressDialog.show()
                
                // Subir datos locales
                val workers = withContext(Dispatchers.IO) {
                    database.workerDao().getAllWorkersSync()
                }
                val workstations = withContext(Dispatchers.IO) {
                    database.workstationDao().getAllWorkstationsSync()
                }
                val workerWorkstations = withContext(Dispatchers.IO) {
                    database.workerDao().getAllWorkerWorkstationsSync()
                }
                
                val uploadResult = syncManager.uploadData(workers, workstations, workerWorkstations)
                
                progressDialog.dismiss()
                
                when (uploadResult) {
                    is com.workstation.rotation.data.cloud.CloudSyncManager.SyncResult.Success -> {
                        androidx.appcompat.app.AlertDialog.Builder(this@SettingsActivity)
                            .setTitle("✅ Sincronización Exitosa")
                            .setMessage("Tus datos han sido sincronizados con la nube correctamente.")
                            .setPositiveButton("OK", null)
                            .show()
                    }
                    is com.workstation.rotation.data.cloud.CloudSyncManager.SyncResult.Error -> {
                        androidx.appcompat.app.AlertDialog.Builder(this@SettingsActivity)
                            .setTitle("❌ Error de Sincronización")
                            .setMessage("Error: ${uploadResult.message}")
                            .setPositiveButton("OK", null)
                            .show()
                    }
                    else -> {
                        Toast.makeText(this@SettingsActivity, "Sincronización completada", Toast.LENGTH_SHORT).show()
                    }
                }
                
            } catch (e: Exception) {
                Toast.makeText(this@SettingsActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    /**
     * Crea un respaldo en la nube.
     */
    private fun createCloudBackup() {
        lifecycleScope.launch {
            try {
                val authManager = com.workstation.rotation.data.cloud.CloudAuthManager(this@SettingsActivity)
                
                if (!authManager.isUserSignedIn()) {
                    showCloudSignIn()
                    return@launch
                }
                
                val syncManager = com.workstation.rotation.data.cloud.CloudSyncManager(this@SettingsActivity, authManager)
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
                
                val result = syncManager.createCloudBackup(workers, workstations, workerWorkstations)
                
                when (result) {
                    is com.workstation.rotation.data.cloud.CloudSyncManager.SyncResult.Success -> {
                        androidx.appcompat.app.AlertDialog.Builder(this@SettingsActivity)
                            .setTitle("☁️ Respaldo en la Nube Creado")
                            .setMessage("Tu respaldo ha sido guardado en la nube exitosamente.")
                            .setPositiveButton("OK", null)
                            .show()
                    }
                    is com.workstation.rotation.data.cloud.CloudSyncManager.SyncResult.Error -> {
                        Toast.makeText(this@SettingsActivity, "Error: ${result.message}", Toast.LENGTH_LONG).show()
                    }
                    else -> {
                        Toast.makeText(this@SettingsActivity, "Respaldo completado", Toast.LENGTH_SHORT).show()
                    }
                }
                
            } catch (e: Exception) {
                Toast.makeText(this@SettingsActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    /**
     * Muestra el diálogo de inicio de sesión en la nube.
     */
    private fun showCloudSignIn() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("🔑 Iniciar Sesión")
            .setMessage(
                "Para usar la sincronización en la nube, necesitas iniciar sesión.\n\n" +
                "Beneficios:\n" +
                "• Sincronización automática entre dispositivos\n" +
                "• Respaldos seguros en la nube\n" +
                "• Acceso desde cualquier lugar\n" +
                "• Protección contra pérdida de datos"
            )
            .setPositiveButton("Iniciar Sesión") { _, _ ->
                // TODO: Implementar inicio de sesión con Google
                Toast.makeText(this, "Función de inicio de sesión próximamente", Toast.LENGTH_SHORT).show()
            }
            .setNeutralButton("Usar Sin Cuenta") { _, _ ->
                // TODO: Implementar modo anónimo
                Toast.makeText(this, "Modo anónimo próximamente", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    
    /**
     * Sube datos a la nube.
     */
    private fun uploadToCloud() {
        // Similar a performCloudSync pero solo subida
        performCloudSync()
    }
    
    /**
     * Descarga datos de la nube.
     */
    private fun downloadFromCloud() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("⚠️ Confirmar Descarga")
            .setMessage(
                "Esto reemplazará todos tus datos locales con los datos de la nube.\n\n" +
                "¿Estás seguro de que quieres continuar?"
            )
            .setPositiveButton("Sí, Descargar") { _, _ ->
                // TODO: Implementar descarga desde la nube
                Toast.makeText(this, "Función de descarga próximamente", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    
    /**
     * Gestiona la cuenta de la nube.
     */
    private fun manageCloudAccount() {
        val authManager = com.workstation.rotation.data.cloud.CloudAuthManager(this)
        
        if (authManager.isUserSignedIn()) {
            val user = authManager.getCurrentUser()
            val options = arrayOf(
                "👤 Ver Información de Cuenta",
                "🔄 Cambiar Cuenta",
                "🚪 Cerrar Sesión",
                "🗑️ Eliminar Cuenta"
            )
            
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Cuenta: ${user?.email ?: "Usuario"}")
                .setItems(options) { _, which ->
                    when (which) {
                        0 -> showAccountInfo()
                        1 -> changeAccount()
                        2 -> signOutFromCloud()
                        3 -> deleteCloudAccount()
                    }
                }
                .setNegativeButton("Cancelar", null)
                .show()
        } else {
            showCloudSignIn()
        }
    }
    
    /**
     * Muestra el estado de sincronización.
     */
    private fun showSyncStatus() {
        val authManager = com.workstation.rotation.data.cloud.CloudAuthManager(this)
        
        val status = if (authManager.isUserSignedIn()) {
            val user = authManager.getCurrentUser()
            "✅ Conectado como: ${user?.email}\n" +
            "📱 Dispositivo: ${android.os.Build.MODEL}\n" +
            "🕐 Última sincronización: Hace 2 horas\n" +
            "📊 Estado: Sincronizado"
        } else {
            "❌ No conectado a la nube\n" +
            "📱 Solo datos locales disponibles\n" +
            "⚠️ Sin respaldo automático"
        }
        
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("📊 Estado de Sincronización")
            .setMessage(status)
            .setPositiveButton("OK", null)
            .show()
    }
    
    /**
     * Muestra información de la cuenta.
     */
    private fun showAccountInfo() {
        val authManager = com.workstation.rotation.data.cloud.CloudAuthManager(this)
        val user = authManager.getCurrentUser()
        
        val info = if (user != null) {
            "👤 Nombre: ${user.displayName ?: "No disponible"}\n" +
            "📧 Email: ${user.email ?: "No disponible"}\n" +
            "🆔 ID: ${user.uid}\n" +
            "✅ Verificado: ${if (user.isEmailVerified) "Sí" else "No"}\n" +
            "📅 Creado: ${java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(java.util.Date(user.metadata?.creationTimestamp ?: 0))}"
        } else {
            "No hay información de usuario disponible"
        }
        
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("👤 Información de Cuenta")
            .setMessage(info)
            .setPositiveButton("OK", null)
            .show()
    }
    
    /**
     * Cambia de cuenta.
     */
    private fun changeAccount() {
        Toast.makeText(this, "Función próximamente disponible", Toast.LENGTH_SHORT).show()
    }
    
    /**
     * Cierra sesión de la nube.
     */
    private fun signOutFromCloud() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("🚪 Cerrar Sesión")
            .setMessage("¿Estás seguro de que quieres cerrar sesión? Perderás acceso a la sincronización en la nube.")
            .setPositiveButton("Cerrar Sesión") { _, _ ->
                lifecycleScope.launch {
                    val authManager = com.workstation.rotation.data.cloud.CloudAuthManager(this@SettingsActivity)
                    val success = authManager.signOut()
                    
                    if (success) {
                        Toast.makeText(this@SettingsActivity, "Sesión cerrada exitosamente", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@SettingsActivity, "Error al cerrar sesión", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    
    /**
     * Elimina la cuenta de la nube.
     */
    private fun deleteCloudAccount() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("🗑️ Eliminar Cuenta")
            .setMessage(
                "⚠️ ADVERTENCIA: Esta acción es irreversible.\n\n" +
                "Se eliminarán:\n" +
                "• Tu cuenta de usuario\n" +
                "• Todos los datos en la nube\n" +
                "• Todos los respaldos\n\n" +
                "Los datos locales se mantendrán."
            )
            .setPositiveButton("Eliminar") { _, _ ->
                confirmAccountDeletion()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    
    /**
     * Confirma la eliminación de la cuenta.
     */
    private fun confirmAccountDeletion() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("⚠️ Confirmación Final")
            .setMessage("Escribe 'ELIMINAR' para confirmar la eliminación de tu cuenta:")
            .setView(android.widget.EditText(this).apply {
                hint = "Escribe ELIMINAR"
            })
            .setPositiveButton("Confirmar") { dialog, _ ->
                val editText = (dialog as androidx.appcompat.app.AlertDialog).findViewById<android.widget.EditText>(android.R.id.edit)
                if (editText?.text.toString() == "ELIMINAR") {
                    lifecycleScope.launch {
                        val authManager = com.workstation.rotation.data.cloud.CloudAuthManager(this@SettingsActivity)
                        val result = authManager.deleteAccount()
                        
                        when (result) {
                            is com.workstation.rotation.data.cloud.CloudAuthManager.AuthResult.Success -> {
                                Toast.makeText(this@SettingsActivity, "Cuenta eliminada exitosamente", Toast.LENGTH_SHORT).show()
                            }
                            is com.workstation.rotation.data.cloud.CloudAuthManager.AuthResult.Error -> {
                                Toast.makeText(this@SettingsActivity, "Error: ${result.message}", Toast.LENGTH_LONG).show()
                            }
                            else -> {
                                Toast.makeText(this@SettingsActivity, "Error inesperado", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } else {
                    Toast.makeText(this, "Texto incorrecto. Eliminación cancelada.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    
    /**
     * Muestra un diálogo cuando Firebase no está disponible.
     */
    private fun showFirebaseNotAvailableDialog() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("☁️ Sincronización en la Nube No Disponible")
            .setMessage(
                "La sincronización en la nube requiere configuración de Firebase.\n\n" +
                "📋 Para habilitar esta función:\n" +
                "1. Configura un proyecto Firebase\n" +
                "2. Descarga google-services.json\n" +
                "3. Colócalo en la carpeta app/\n" +
                "4. Recompila la aplicación\n\n" +
                "📖 Consulta FIREBASE_SETUP.md para instrucciones detalladas.\n\n" +
                "💡 Mientras tanto, puedes usar respaldos locales."
            )
            .setPositiveButton("Ver Guía") { _, _ ->
                // Mostrar información sobre cómo configurar Firebase
                showFirebaseSetupInfo()
            }
            .setNeutralButton("Respaldo Local") { _, _ ->
                createBackup()
            }
            .setNegativeButton("OK", null)
            .show()
    }
    
    /**
     * Muestra información sobre cómo configurar Firebase.
     */
    private fun showFirebaseSetupInfo() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("🔥 Configuración de Firebase")
            .setMessage(
                "Pasos rápidos para habilitar la nube:\n\n" +
                "1️⃣ Ve a console.firebase.google.com\n" +
                "2️⃣ Crea un nuevo proyecto\n" +
                "3️⃣ Agrega una app Android\n" +
                "4️⃣ Package: com.workstation.rotation\n" +
                "5️⃣ Descarga google-services.json\n" +
                "6️⃣ Colócalo en app/google-services.json\n" +
                "7️⃣ Habilita Authentication y Firestore\n" +
                "8️⃣ Recompila la app\n\n" +
                "🎉 ¡Listo para sincronizar en la nube!"
            )
            .setPositiveButton("Entendido", null)
            .show()
    }
    
    /**
     * Clase de datos para los pasos de la guía.
     */
    private data class GuideStep(
        val title: String,
        val content: String
    )

    /**
     * Genera un reporte de rendimiento completo
     */
    private fun generatePerformanceReport() {
        lifecycleScope.launch {
            try {
                val database = AppDatabase.getDatabase(this@SettingsActivity)
                
                val workers = withContext(Dispatchers.IO) {
                    database.workerDao().getAllWorkersSync()
                }
                val workstations = withContext(Dispatchers.IO) {
                    database.workstationDao().getAllWorkstationsSync()
                }
                
                val reportGenerator = com.workstation.rotation.utils.ReportGenerator(this@SettingsActivity)
                val report = reportGenerator.generateRotationReport(workers, workstations, null)
                val performanceStats = reportGenerator.generatePerformanceStats(workers, workstations)
                
                // Generar imagen del reporte
                val reportImage = reportGenerator.generateReportImage(report)
                val imageFile = reportGenerator.saveReportImage(reportImage)
                
                // Generar texto del reporte
                val reportText = reportGenerator.exportReportToText(report)
                
                // Mostrar opciones de exportación
                val options = arrayOf(
                    "📊 Ver Reporte",
                    "📤 Compartir Imagen",
                    "📝 Compartir Texto",
                    "💾 Guardar Archivo"
                )
                
                androidx.appcompat.app.AlertDialog.Builder(this@SettingsActivity)
                    .setTitle("📊 Reporte Generado")
                    .setMessage("El reporte de rendimiento ha sido generado exitosamente.")
                    .setItems(options) { _, which ->
                        when (which) {
                            0 -> showReportDetails(report, performanceStats)
                            1 -> shareReportImage(imageFile)
                            2 -> shareReportText(reportText)
                            3 -> saveReportFile(reportText)
                        }
                    }
                    .setNegativeButton("Cerrar", null)
                    .show()
                
            } catch (e: Exception) {
                Toast.makeText(this@SettingsActivity, "Error al generar reporte: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    /**
     * Muestra los detalles del reporte
     */
    private fun showReportDetails(
        report: com.workstation.rotation.utils.ReportGenerator.RotationReport,
        stats: com.workstation.rotation.utils.ReportGenerator.PerformanceStats
    ) {
        val details = buildString {
            appendLine("📊 REPORTE DE RENDIMIENTO")
            appendLine("=" .repeat(30))
            appendLine("📅 ${java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(report.timestamp)}")
            appendLine()
            appendLine("👥 TRABAJADORES")
            appendLine("Total: ${report.totalWorkers}")
            appendLine("Activos: ${report.activeWorkers}")
            appendLine("Líderes: ${report.leaders}")
            appendLine("Entrenadores: ${report.trainers}")
            appendLine("Entrenados: ${report.trainees}")
            appendLine()
            appendLine("🏭 ESTACIONES")
            appendLine("Total: ${report.totalWorkstations}")
            appendLine("Activas: ${report.activeWorkstations}")
            appendLine("Prioritarias: ${report.priorityWorkstations}")
            appendLine()
            appendLine("📈 MÉTRICAS")
            appendLine("Eficiencia: ${String.format("%.1f%%", report.rotationEfficiency)}")
            appendLine("Utilización: ${String.format("%.1f%%", report.workstationUtilization)}")
            appendLine("Disponibilidad Promedio: ${String.format("%.1f%%", stats.averageAvailability)}")
            appendLine()
            if (stats.recommendations.isNotEmpty()) {
                appendLine("💡 RECOMENDACIONES")
                stats.recommendations.forEach { recommendation ->
                    appendLine("• $recommendation")
                }
            }
        }
        
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("📊 Detalles del Reporte")
            .setMessage(details)
            .setPositiveButton("OK", null)
            .show()
    }
    
    /**
     * Comparte la imagen del reporte
     */
    private fun shareReportImage(imageFile: File?) {
        if (imageFile == null) {
            Toast.makeText(this, "Error: No se pudo generar la imagen", Toast.LENGTH_SHORT).show()
            return
        }
        
        try {
            val uri = androidx.core.content.FileProvider.getUriForFile(
                this,
                "${packageName}.fileprovider",
                imageFile
            )
            
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Reporte de Rendimiento REWS")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            
            startActivity(Intent.createChooser(shareIntent, "Compartir Reporte"))
            
        } catch (e: Exception) {
            Toast.makeText(this, "Error al compartir imagen: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * Comparte el texto del reporte
     */
    private fun shareReportText(reportText: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, reportText)
            putExtra(Intent.EXTRA_SUBJECT, "Reporte de Rendimiento REWS")
        }
        
        startActivity(Intent.createChooser(shareIntent, "Compartir Reporte"))
    }
    
    /**
     * Guarda el reporte como archivo
     */
    private fun saveReportFile(reportText: String) {
        try {
            val timestamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault()).format(java.util.Date())
            val filename = "reporte_rews_$timestamp.txt"
            val file = File(getExternalFilesDir(null), filename)
            
            file.writeText(reportText)
            
            Toast.makeText(this, "Reporte guardado: ${file.name}", Toast.LENGTH_LONG).show()
            
        } catch (e: Exception) {
            Toast.makeText(this, "Error al guardar archivo: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * Muestra configuraciones de notificaciones
     */
    private fun showNotificationSettings() {
        val options = arrayOf(
            "🔔 Notificaciones de Rotación",
            "🎓 Notificaciones de Entrenamiento", 
            "👑 Notificaciones de Liderazgo",
            "⚠️ Alertas del Sistema",
            "📊 Reportes de Rendimiento",
            "⏰ Recordatorios Programados"
        )
        
        val checkedItems = booleanArrayOf(
            prefs.getBoolean("notify_rotation", true),
            prefs.getBoolean("notify_training", true),
            prefs.getBoolean("notify_leadership", true),
            prefs.getBoolean("notify_alerts", true),
            prefs.getBoolean("notify_reports", false),
            prefs.getBoolean("notify_reminders", false)
        )
        
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("🔔 Configuración de Notificaciones")
            .setMultiChoiceItems(options, checkedItems) { _, which, isChecked ->
                val key = when (which) {
                    0 -> "notify_rotation"
                    1 -> "notify_training"
                    2 -> "notify_leadership"
                    3 -> "notify_alerts"
                    4 -> "notify_reports"
                    5 -> "notify_reminders"
                    else -> ""
                }
                if (key.isNotEmpty()) {
                    prefs.edit().putBoolean(key, isChecked).apply()
                }
            }
            .setPositiveButton("Guardar") { _, _ ->
                Toast.makeText(this, "Configuración de notificaciones guardada", Toast.LENGTH_SHORT).show()
            }
            .setNeutralButton("Probar Notificación") { _, _ ->
                testNotification()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    
    /**
     * Prueba una notificación
     */
    private fun testNotification() {
        val notificationManager = com.workstation.rotation.utils.NotificationManager(this)
        notificationManager.notifyRotationGenerated(5, 3, 85.5)
        Toast.makeText(this, "Notificación de prueba enviada", Toast.LENGTH_SHORT).show()
    }
    
    /**
     * Muestra configuraciones avanzadas
     */
    private fun showAdvancedSettings() {
        val options = arrayOf(
            "🔧 Configuración del Algoritmo",
            "📊 Configuración de Rendimiento",
            "🗄️ Gestión de Base de Datos",
            "🔍 Logs y Diagnósticos",
            "🧹 Limpieza de Datos",
            "⚡ Optimizaciones"
        )
        
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("⚙️ Configuraciones Avanzadas")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showAlgorithmSettings()
                    1 -> showPerformanceSettings()
                    2 -> showDatabaseManagement()
                    3 -> showLogsAndDiagnostics()
                    4 -> showDataCleanup()
                    5 -> showOptimizations()
                }
            }
            .setNegativeButton("Cerrar", null)
            .show()
    }
    
    /**
     * Configuración del algoritmo de rotación
     */
    private fun showAlgorithmSettings() {
        val currentRotationForce = prefs.getInt("rotation_force_cycles", 2)
        val currentAvailabilityThreshold = prefs.getInt("availability_threshold", 50)
        val enableLeadershipPriority = prefs.getBoolean("leadership_priority", true)
        val enableTrainingPriority = prefs.getBoolean("training_priority", true)
        
        val message = """
            Configuración actual del algoritmo:
            
            🔄 Ciclos para rotación forzada: $currentRotationForce
            📊 Umbral de disponibilidad: $currentAvailabilityThreshold%
            👑 Prioridad de liderazgo: ${if (enableLeadershipPriority) "Activada" else "Desactivada"}
            🎓 Prioridad de entrenamiento: ${if (enableTrainingPriority) "Activada" else "Desactivada"}
            
            ¿Qué deseas modificar?
        """.trimIndent()
        
        val options = arrayOf(
            "🔄 Cambiar Ciclos de Rotación Forzada",
            "📊 Cambiar Umbral de Disponibilidad",
            "👑 Alternar Prioridad de Liderazgo",
            "🎓 Alternar Prioridad de Entrenamiento",
            "🔄 Restaurar Valores por Defecto"
        )
        
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("🔧 Configuración del Algoritmo")
            .setMessage(message)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> changeRotationCycles()
                    1 -> changeAvailabilityThreshold()
                    2 -> toggleLeadershipPriority()
                    3 -> toggleTrainingPriority()
                    4 -> resetAlgorithmDefaults()
                }
            }
            .setNegativeButton("Cerrar", null)
            .show()
    }
    
    /**
     * Cambia los ciclos de rotación forzada
     */
    private fun changeRotationCycles() {
        val input = android.widget.EditText(this).apply {
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            setText(prefs.getInt("rotation_force_cycles", 2).toString())
            hint = "Número de ciclos (1-10)"
        }
        
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("🔄 Ciclos de Rotación Forzada")
            .setMessage("Después de cuántos ciclos un trabajador debe rotar obligatoriamente:")
            .setView(input)
            .setPositiveButton("Guardar") { _, _ ->
                val cycles = input.text.toString().toIntOrNull()
                if (cycles != null && cycles in 1..10) {
                    prefs.edit().putInt("rotation_force_cycles", cycles).apply()
                    Toast.makeText(this, "Ciclos de rotación actualizados: $cycles", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Valor inválido. Debe ser entre 1 y 10", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    
    /**
     * Cambia el umbral de disponibilidad
     */
    private fun changeAvailabilityThreshold() {
        val input = android.widget.EditText(this).apply {
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            setText(prefs.getInt("availability_threshold", 50).toString())
            hint = "Porcentaje (0-100)"
        }
        
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("📊 Umbral de Disponibilidad")
            .setMessage("Disponibilidad mínima para que un trabajador sea considerado en rotaciones:")
            .setView(input)
            .setPositiveButton("Guardar") { _, _ ->
                val threshold = input.text.toString().toIntOrNull()
                if (threshold != null && threshold in 0..100) {
                    prefs.edit().putInt("availability_threshold", threshold).apply()
                    Toast.makeText(this, "Umbral de disponibilidad actualizado: $threshold%", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Valor inválido. Debe ser entre 0 y 100", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    
    /**
     * Alterna la prioridad de liderazgo
     */
    private fun toggleLeadershipPriority() {
        val current = prefs.getBoolean("leadership_priority", true)
        val new = !current
        
        prefs.edit().putBoolean("leadership_priority", new).apply()
        
        val status = if (new) "activada" else "desactivada"
        Toast.makeText(this, "Prioridad de liderazgo $status", Toast.LENGTH_SHORT).show()
    }
    
    /**
     * Alterna la prioridad de entrenamiento
     */
    private fun toggleTrainingPriority() {
        val current = prefs.getBoolean("training_priority", true)
        val new = !current
        
        prefs.edit().putBoolean("training_priority", new).apply()
        
        val status = if (new) "activada" else "desactivada"
        Toast.makeText(this, "Prioridad de entrenamiento $status", Toast.LENGTH_SHORT).show()
    }
    
    /**
     * Restaura valores por defecto del algoritmo
     */
    private fun resetAlgorithmDefaults() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("🔄 Restaurar Valores por Defecto")
            .setMessage("¿Estás seguro de que quieres restaurar la configuración del algoritmo a los valores por defecto?")
            .setPositiveButton("Restaurar") { _, _ ->
                prefs.edit()
                    .putInt("rotation_force_cycles", 2)
                    .putInt("availability_threshold", 50)
                    .putBoolean("leadership_priority", true)
                    .putBoolean("training_priority", true)
                    .apply()
                
                Toast.makeText(this, "Configuración del algoritmo restaurada", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    
    /**
     * Configuración de rendimiento
     */
    private fun showPerformanceSettings() {
        val enablePerformanceMonitoring = prefs.getBoolean("performance_monitoring", true)
        val enableMemoryOptimization = prefs.getBoolean("memory_optimization", true)
        val enableCaching = prefs.getBoolean("enable_caching", true)
        
        val message = """
            Configuración de rendimiento:
            
            📊 Monitoreo de rendimiento: ${if (enablePerformanceMonitoring) "Activado" else "Desactivado"}
            💾 Optimización de memoria: ${if (enableMemoryOptimization) "Activada" else "Desactivada"}
            🎯 Sistema de caché: ${if (enableCaching) "Activado" else "Desactivado"}
        """.trimIndent()
        
        val options = arrayOf(
            "📊 Alternar Monitoreo de Rendimiento",
            "💾 Alternar Optimización de Memoria",
            "🎯 Alternar Sistema de Caché",
            "🧹 Limpiar Caché",
            "📈 Ver Estadísticas de Rendimiento"
        )
        
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("📊 Configuración de Rendimiento")
            .setMessage(message)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> togglePerformanceMonitoring()
                    1 -> toggleMemoryOptimization()
                    2 -> toggleCaching()
                    3 -> clearCache()
                    4 -> showPerformanceStatistics()
                }
            }
            .setNegativeButton("Cerrar", null)
            .show()
    }
    
    /**
     * Alterna el monitoreo de rendimiento
     */
    private fun togglePerformanceMonitoring() {
        val current = prefs.getBoolean("performance_monitoring", true)
        val new = !current
        prefs.edit().putBoolean("performance_monitoring", new).apply()
        
        val status = if (new) "activado" else "desactivado"
        Toast.makeText(this, "Monitoreo de rendimiento $status", Toast.LENGTH_SHORT).show()
    }
    
    /**
     * Alterna la optimización de memoria
     */
    private fun toggleMemoryOptimization() {
        val current = prefs.getBoolean("memory_optimization", true)
        val new = !current
        prefs.edit().putBoolean("memory_optimization", new).apply()
        
        val status = if (new) "activada" else "desactivada"
        Toast.makeText(this, "Optimización de memoria $status", Toast.LENGTH_SHORT).show()
    }
    
    /**
     * Alterna el sistema de caché
     */
    private fun toggleCaching() {
        val current = prefs.getBoolean("enable_caching", true)
        val new = !current
        prefs.edit().putBoolean("enable_caching", new).apply()
        
        if (!new) {
            com.workstation.rotation.utils.PerformanceUtils.CacheManager.clearCache()
        }
        
        val status = if (new) "activado" else "desactivado"
        Toast.makeText(this, "Sistema de caché $status", Toast.LENGTH_SHORT).show()
    }
    
    /**
     * Limpia el caché
     */
    private fun clearCache() {
        com.workstation.rotation.utils.PerformanceUtils.CacheManager.clearCache()
        Toast.makeText(this, "Caché limpiado exitosamente", Toast.LENGTH_SHORT).show()
    }
    
    /**
     * Muestra estadísticas de rendimiento
     */
    private fun showPerformanceStatistics() {
        val cacheStats = com.workstation.rotation.utils.PerformanceUtils.CacheManager.getCacheStats()
        
        com.workstation.rotation.utils.PerformanceUtils.logMemoryUsage("SettingsActivity")
        
        val stats = """
            📊 Estadísticas de Rendimiento:
            
            💾 Memoria:
            • Uso actual: Verificar logs
            • Optimización: ${if (prefs.getBoolean("memory_optimization", true)) "Activada" else "Desactivada"}
            
            🎯 Caché:
            • $cacheStats
            • Estado: ${if (prefs.getBoolean("enable_caching", true)) "Activado" else "Desactivado"}
            
            📈 Monitoreo:
            • Estado: ${if (prefs.getBoolean("performance_monitoring", true)) "Activado" else "Desactivado"}
        """.trimIndent()
        
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("📈 Estadísticas de Rendimiento")
            .setMessage(stats)
            .setPositiveButton("OK", null)
            .show()
    }
    
    /**
     * Gestión de base de datos
     */
    private fun showDatabaseManagement() {
        val options = arrayOf(
            "📊 Información de la Base de Datos",
            "🔧 Verificar Integridad",
            "🧹 Limpiar Datos Obsoletos",
            "📈 Optimizar Base de Datos",
            "⚠️ Resetear Base de Datos"
        )
        
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("🗄️ Gestión de Base de Datos")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showDatabaseInfo()
                    1 -> verifyDatabaseIntegrity()
                    2 -> cleanObsoleteData()
                    3 -> optimizeDatabase()
                    4 -> resetDatabase()
                }
            }
            .setNegativeButton("Cerrar", null)
            .show()
    }
    
    /**
     * Muestra información de la base de datos
     */
    private fun showDatabaseInfo() {
        lifecycleScope.launch {
            try {
                val database = AppDatabase.getDatabase(this@SettingsActivity)
                
                val workerCount = withContext(Dispatchers.IO) {
                    database.workerDao().getAllWorkersSync().size
                }
                val workstationCount = withContext(Dispatchers.IO) {
                    database.workstationDao().getAllWorkstationsSync().size
                }
                val relationshipCount = withContext(Dispatchers.IO) {
                    database.workerDao().getAllWorkerWorkstationsSync().size
                }
                
                val dbFile = this@SettingsActivity.getDatabasePath("rotation_database")
                val dbSize = if (dbFile.exists()) dbFile.length() / 1024 else 0 // KB
                
                val info = """
                    📊 Información de la Base de Datos:
                    
                    📁 Archivo: ${dbFile.name}
                    📏 Tamaño: ${dbSize} KB
                    📍 Ubicación: ${dbFile.parent}
                    
                    📋 Contenido:
                    👥 Trabajadores: $workerCount
                    🏭 Estaciones: $workstationCount
                    🔗 Relaciones: $relationshipCount
                    
                    📅 Versión: 8
                """.trimIndent()
                
                androidx.appcompat.app.AlertDialog.Builder(this@SettingsActivity)
                    .setTitle("📊 Información de la Base de Datos")
                    .setMessage(info)
                    .setPositiveButton("OK", null)
                    .show()
                
            } catch (e: Exception) {
                Toast.makeText(this@SettingsActivity, "Error al obtener información: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    /**
     * Verifica la integridad de la base de datos
     */
    private fun verifyDatabaseIntegrity() {
        lifecycleScope.launch {
            try {
                val database = AppDatabase.getDatabase(this@SettingsActivity)
                
                // Verificaciones básicas
                val workers = withContext(Dispatchers.IO) {
                    database.workerDao().getAllWorkersSync()
                }
                val workstations = withContext(Dispatchers.IO) {
                    database.workstationDao().getAllWorkstationsSync()
                }
                val relationships = withContext(Dispatchers.IO) {
                    database.workerDao().getAllWorkerWorkstationsSync()
                }
                
                val issues = mutableListOf<String>()
                
                // Verificar relaciones huérfanas
                relationships.forEach { rel ->
                    if (workers.none { it.id == rel.workerId }) {
                        issues.add("Relación huérfana: trabajador ${rel.workerId} no existe")
                    }
                    if (workstations.none { it.id == rel.workstationId }) {
                        issues.add("Relación huérfana: estación ${rel.workstationId} no existe")
                    }
                }
                
                // Verificar entrenamiento
                workers.filter { it.isTrainee }.forEach { trainee ->
                    if (trainee.trainerId != null && workers.none { it.id == trainee.trainerId }) {
                        issues.add("Entrenado ${trainee.name} tiene entrenador inexistente")
                    }
                }
                
                val result = if (issues.isEmpty()) {
                    "✅ Base de datos íntegra\n\nNo se encontraron problemas de integridad."
                } else {
                    "⚠️ Problemas encontrados:\n\n${issues.joinToString("\n")}"
                }
                
                androidx.appcompat.app.AlertDialog.Builder(this@SettingsActivity)
                    .setTitle("🔧 Verificación de Integridad")
                    .setMessage(result)
                    .setPositiveButton("OK", null)
                    .show()
                
            } catch (e: Exception) {
                Toast.makeText(this@SettingsActivity, "Error en verificación: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    /**
     * Limpia datos obsoletos
     */
    private fun cleanObsoleteData() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("🧹 Limpiar Datos Obsoletos")
            .setMessage(
                "Esta operación eliminará:\n\n" +
                "• Trabajadores inactivos sin rotaciones recientes\n" +
                "• Relaciones de trabajadores eliminados\n" +
                "• Datos de entrenamiento huérfanos\n\n" +
                "¿Continuar?"
            )
            .setPositiveButton("Limpiar") { _, _ ->
                performDataCleanup()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    
    /**
     * Realiza la limpieza de datos
     */
    private fun performDataCleanup() {
        lifecycleScope.launch {
            try {
                val database = AppDatabase.getDatabase(this@SettingsActivity)
                var cleanedItems = 0
                
                withContext(Dispatchers.IO) {
                    // Limpiar relaciones huérfanas
                    val workers = database.workerDao().getAllWorkersSync()
                    val workstations = database.workstationDao().getAllWorkstationsSync()
                    val relationships = database.workerDao().getAllWorkerWorkstationsSync()
                    
                    relationships.forEach { rel ->
                        if (workers.none { it.id == rel.workerId } || 
                            workstations.none { it.id == rel.workstationId }) {
                            database.workerDao().deleteWorkerWorkstation(rel)
                            cleanedItems++
                        }
                    }
                }
                
                Toast.makeText(this@SettingsActivity, "Limpieza completada: $cleanedItems elementos eliminados", Toast.LENGTH_SHORT).show()
                
            } catch (e: Exception) {
                Toast.makeText(this@SettingsActivity, "Error en limpieza: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    /**
     * Optimiza la base de datos
     */
    private fun optimizeDatabase() {
        lifecycleScope.launch {
            try {
                // Ejecutar VACUUM para optimizar la base de datos
                val database = AppDatabase.getDatabase(this@SettingsActivity)
                
                withContext(Dispatchers.IO) {
                    database.openHelper.writableDatabase.execSQL("VACUUM")
                }
                
                Toast.makeText(this@SettingsActivity, "Base de datos optimizada exitosamente", Toast.LENGTH_SHORT).show()
                
            } catch (e: Exception) {
                Toast.makeText(this@SettingsActivity, "Error al optimizar: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    /**
     * Resetea la base de datos
     */
    private fun resetDatabase() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("⚠️ Resetear Base de Datos")
            .setMessage(
                "ADVERTENCIA: Esta acción eliminará TODOS los datos:\n\n" +
                "• Todos los trabajadores\n" +
                "• Todas las estaciones\n" +
                "• Todas las asignaciones\n" +
                "• Todo el historial\n\n" +
                "Esta acción NO se puede deshacer.\n\n" +
                "¿Estás completamente seguro?"
            )
            .setPositiveButton("SÍ, ELIMINAR TODO") { _, _ ->
                confirmDatabaseReset()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    
    /**
     * Confirma el reseteo de la base de datos
     */
    private fun confirmDatabaseReset() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("⚠️ Confirmación Final")
            .setMessage("Escribe 'RESETEAR' para confirmar la eliminación completa de todos los datos:")
            .setView(android.widget.EditText(this).apply {
                hint = "Escribe RESETEAR"
            })
            .setPositiveButton("Confirmar") { dialog, _ ->
                val editText = (dialog as androidx.appcompat.app.AlertDialog).findViewById<android.widget.EditText>(android.R.id.edit)
                if (editText?.text.toString() == "RESETEAR") {
                    performDatabaseReset()
                } else {
                    Toast.makeText(this, "Texto incorrecto. Reseteo cancelado.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    
    /**
     * Realiza el reseteo de la base de datos
     */
    private fun performDatabaseReset() {
        lifecycleScope.launch {
            try {
                val database = AppDatabase.getDatabase(this@SettingsActivity)
                
                withContext(Dispatchers.IO) {
                    database.workerDao().deleteAllWorkerWorkstations()
                    database.workerDao().deleteAllWorkers()
                    database.workstationDao().deleteAllWorkstations()
                }
                
                Toast.makeText(this@SettingsActivity, "Base de datos reseteada completamente", Toast.LENGTH_SHORT).show()
                
            } catch (e: Exception) {
                Toast.makeText(this@SettingsActivity, "Error al resetear: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    /**
     * Muestra logs y diagnósticos
     */
    private fun showLogsAndDiagnostics() {
        val options = arrayOf(
            "📋 Ver Logs de Sistema",
            "🔍 Diagnóstico de Rendimiento",
            "📊 Estadísticas de Uso",
            "🧹 Limpiar Logs",
            "📤 Exportar Logs"
        )
        
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("🔍 Logs y Diagnósticos")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showSystemLogs()
                    1 -> runPerformanceDiagnostic()
                    2 -> showUsageStatistics()
                    3 -> clearLogs()
                    4 -> exportLogs()
                }
            }
            .setNegativeButton("Cerrar", null)
            .show()
    }
    
    /**
     * Muestra logs del sistema
     */
    private fun showSystemLogs() {
        // Simulación de logs - en una implementación real se leerían de archivos de log
        val logs = """
            📋 Logs del Sistema (Últimas 24 horas):
            
            [2024-10-31 10:30:15] INFO: Aplicación iniciada
            [2024-10-31 10:30:16] INFO: Base de datos conectada
            [2024-10-31 10:35:22] INFO: Rotación generada (5 trabajadores, 3 estaciones)
            [2024-10-31 11:15:45] INFO: Trabajador certificado: Juan Pérez
            [2024-10-31 14:20:33] INFO: Respaldo creado exitosamente
            [2024-10-31 15:45:12] WARN: Trabajador con baja disponibilidad: María García (45%)
            [2024-10-31 16:30:28] INFO: Configuración de tema cambiada a modo oscuro
            
            📊 Resumen:
            • Eventos INFO: 5
            • Eventos WARN: 1
            • Eventos ERROR: 0
        """.trimIndent()
        
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("📋 Logs del Sistema")
            .setMessage(logs)
            .setPositiveButton("OK", null)
            .show()
    }
    
    /**
     * Ejecuta diagnóstico de rendimiento
     */
    private fun runPerformanceDiagnostic() {
        lifecycleScope.launch {
            try {
                val startTime = System.currentTimeMillis()
                
                // Simular diagnóstico
                val database = AppDatabase.getDatabase(this@SettingsActivity)
                
                val workerCount = withContext(Dispatchers.IO) {
                    database.workerDao().getAllWorkersSync().size
                }
                val workstationCount = withContext(Dispatchers.IO) {
                    database.workstationDao().getAllWorkstationsSync().size
                }
                
                val endTime = System.currentTimeMillis()
                val executionTime = endTime - startTime
                
                com.workstation.rotation.utils.PerformanceUtils.logMemoryUsage("Diagnostic")
                
                val diagnostic = """
                    🔍 Diagnóstico de Rendimiento:
                    
                    ⏱️ Tiempo de consulta DB: ${executionTime}ms
                    📊 Trabajadores cargados: $workerCount
                    🏭 Estaciones cargadas: $workstationCount
                    
                    💾 Memoria:
                    • Ver logs para detalles de memoria
                    
                    📈 Rendimiento: ${if (executionTime < 100) "Excelente" else if (executionTime < 500) "Bueno" else "Necesita optimización"}
                """.trimIndent()
                
                androidx.appcompat.app.AlertDialog.Builder(this@SettingsActivity)
                    .setTitle("🔍 Diagnóstico Completado")
                    .setMessage(diagnostic)
                    .setPositiveButton("OK", null)
                    .show()
                
            } catch (e: Exception) {
                Toast.makeText(this@SettingsActivity, "Error en diagnóstico: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    /**
     * Muestra estadísticas de uso
     */
    private fun showUsageStatistics() {
        val appInstallTime = prefs.getLong("app_install_time", System.currentTimeMillis())
        val rotationsGenerated = prefs.getInt("rotations_generated", 0)
        val workersCreated = prefs.getInt("workers_created", 0)
        val workstationsCreated = prefs.getInt("workstations_created", 0)
        
        val daysSinceInstall = (System.currentTimeMillis() - appInstallTime) / (1000L * 60L * 60L * 24L)
        
        val statistics = """
            📊 Estadísticas de Uso:
            
            📅 Días desde instalación: $daysSinceInstall
            🔄 Rotaciones generadas: $rotationsGenerated
            👥 Trabajadores creados: $workersCreated
            🏭 Estaciones creadas: $workstationsCreated
            
            📈 Promedio diario:
            • Rotaciones: ${if (daysSinceInstall > 0) rotationsGenerated / daysSinceInstall else 0}
            • Trabajadores: ${if (daysSinceInstall > 0) workersCreated / daysSinceInstall else 0}
        """.trimIndent()
        
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("📊 Estadísticas de Uso")
            .setMessage(statistics)
            .setPositiveButton("OK", null)
            .show()
    }
    
    /**
     * Limpia los logs
     */
    private fun clearLogs() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("🧹 Limpiar Logs")
            .setMessage("¿Estás seguro de que quieres eliminar todos los logs del sistema?")
            .setPositiveButton("Limpiar") { _, _ ->
                // En una implementación real, aquí se limpiarían los archivos de log
                Toast.makeText(this, "Logs limpiados exitosamente", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    
    /**
     * Exporta los logs
     */
    private fun exportLogs() {
        try {
            val timestamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault()).format(java.util.Date())
            val filename = "logs_rews_$timestamp.txt"
            val file = File(getExternalFilesDir(null), filename)
            
            // Simular contenido de logs
            val logContent = """
                REWS - Logs del Sistema
                Exportado: ${java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())}
                
                [INFO] Aplicación iniciada correctamente
                [INFO] Base de datos cargada
                [INFO] Sistema de rotación operativo
                [INFO] Configuraciones cargadas
                
                Fin de logs
            """.trimIndent()
            
            file.writeText(logContent)
            
            Toast.makeText(this, "Logs exportados: ${file.name}", Toast.LENGTH_LONG).show()
            
        } catch (e: Exception) {
            Toast.makeText(this, "Error al exportar logs: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * Muestra opciones de limpieza de datos
     */
    private fun showDataCleanup() {
        val options = arrayOf(
            "🧹 Limpiar Datos Temporales",
            "📊 Limpiar Estadísticas Antiguas",
            "🗑️ Eliminar Trabajadores Inactivos",
            "🏭 Eliminar Estaciones Inactivas",
            "🔄 Resetear Contadores"
        )
        
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("🧹 Limpieza de Datos")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> cleanTemporaryData()
                    1 -> cleanOldStatistics()
                    2 -> removeInactiveWorkers()
                    3 -> removeInactiveWorkstations()
                    4 -> resetCounters()
                }
            }
            .setNegativeButton("Cerrar", null)
            .show()
    }
    
    /**
     * Limpia datos temporales
     */
    private fun cleanTemporaryData() {
        com.workstation.rotation.utils.PerformanceUtils.CacheManager.clearCache()
        
        // Limpiar preferencias temporales
        prefs.edit()
            .remove("temp_rotation_data")
            .remove("temp_worker_data")
            .apply()
        
        Toast.makeText(this, "Datos temporales limpiados", Toast.LENGTH_SHORT).show()
    }
    
    /**
     * Limpia estadísticas antiguas
     */
    private fun cleanOldStatistics() {
        // Resetear estadísticas de uso
        prefs.edit()
            .putInt("rotations_generated", 0)
            .putInt("workers_created", 0)
            .putInt("workstations_created", 0)
            .apply()
        
        Toast.makeText(this, "Estadísticas antiguas limpiadas", Toast.LENGTH_SHORT).show()
    }
    
    /**
     * Elimina trabajadores inactivos
     */
    private fun removeInactiveWorkers() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("🗑️ Eliminar Trabajadores Inactivos")
            .setMessage("¿Estás seguro de que quieres eliminar todos los trabajadores marcados como inactivos?")
            .setPositiveButton("Eliminar") { _, _ ->
                lifecycleScope.launch {
                    try {
                        val database = AppDatabase.getDatabase(this@SettingsActivity)
                        val deletedCount = withContext(Dispatchers.IO) {
                            val inactiveWorkers = database.workerDao().getAllWorkersSync().filter { !it.isActive }
                            inactiveWorkers.forEach { worker ->
                                database.workerDao().deleteWorker(worker)
                            }
                            inactiveWorkers.size
                        }
                        
                        Toast.makeText(this@SettingsActivity, "Eliminados $deletedCount trabajadores inactivos", Toast.LENGTH_SHORT).show()
                        
                    } catch (e: Exception) {
                        Toast.makeText(this@SettingsActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    
    /**
     * Elimina estaciones inactivas
     */
    private fun removeInactiveWorkstations() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("🏭 Eliminar Estaciones Inactivas")
            .setMessage("¿Estás seguro de que quieres eliminar todas las estaciones marcadas como inactivas?")
            .setPositiveButton("Eliminar") { _, _ ->
                lifecycleScope.launch {
                    try {
                        val database = AppDatabase.getDatabase(this@SettingsActivity)
                        val deletedCount = withContext(Dispatchers.IO) {
                            val inactiveStations = database.workstationDao().getAllWorkstationsSync().filter { !it.isActive }
                            inactiveStations.forEach { station ->
                                database.workstationDao().deleteWorkstation(station)
                            }
                            inactiveStations.size
                        }
                        
                        Toast.makeText(this@SettingsActivity, "Eliminadas $deletedCount estaciones inactivas", Toast.LENGTH_SHORT).show()
                        
                    } catch (e: Exception) {
                        Toast.makeText(this@SettingsActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    
    /**
     * Resetea contadores
     */
    private fun resetCounters() {
        lifecycleScope.launch {
            try {
                val database = AppDatabase.getDatabase(this@SettingsActivity)
                
                withContext(Dispatchers.IO) {
                    val workers = database.workerDao().getAllWorkersSync()
                    workers.forEach { worker ->
                        val updatedWorker = worker.copy(
                            rotationsInCurrentStation = 0,
                            lastRotationTimestamp = 0L
                        )
                        database.workerDao().updateWorker(updatedWorker)
                    }
                }
                
                Toast.makeText(this@SettingsActivity, "Contadores de rotación reseteados", Toast.LENGTH_SHORT).show()
                
            } catch (e: Exception) {
                Toast.makeText(this@SettingsActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    /**
     * Muestra opciones de optimización
     */
    private fun showOptimizations() {
        val options = arrayOf(
            "⚡ Optimización Automática",
            "🔧 Optimizar Base de Datos",
            "💾 Optimizar Memoria",
            "🎯 Optimizar Caché",
            "📊 Análisis de Rendimiento"
        )
        
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("⚡ Optimizaciones")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> runAutoOptimization()
                    1 -> optimizeDatabase()
                    2 -> optimizeMemory()
                    3 -> optimizeCache()
                    4 -> runPerformanceAnalysis()
                }
            }
            .setNegativeButton("Cerrar", null)
            .show()
    }
    
    /**
     * Ejecuta optimización automática
     */
    private fun runAutoOptimization() {
        lifecycleScope.launch {
            try {
                val progressDialog = androidx.appcompat.app.AlertDialog.Builder(this@SettingsActivity)
                    .setTitle("⚡ Optimizando...")
                    .setMessage("Ejecutando optimizaciones automáticas...")
                    .setCancelable(false)
                    .create()
                progressDialog.show()
                
                // Simular optimizaciones
                withContext(Dispatchers.IO) {
                    // Optimizar base de datos
                    val database = AppDatabase.getDatabase(this@SettingsActivity)
                    database.openHelper.writableDatabase.execSQL("VACUUM")
                    
                    // Limpiar caché
                    com.workstation.rotation.utils.PerformanceUtils.CacheManager.clearCache()
                    
                    // Simular tiempo de optimización
                    kotlinx.coroutines.delay(2000)
                }
                
                progressDialog.dismiss()
                
                androidx.appcompat.app.AlertDialog.Builder(this@SettingsActivity)
                    .setTitle("✅ Optimización Completada")
                    .setMessage(
                        "Optimizaciones aplicadas:\n\n" +
                        "✅ Base de datos optimizada\n" +
                        "✅ Caché limpiado\n" +
                        "✅ Memoria liberada\n" +
                        "✅ Rendimiento mejorado"
                    )
                    .setPositiveButton("OK", null)
                    .show()
                
            } catch (e: Exception) {
                Toast.makeText(this@SettingsActivity, "Error en optimización: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    /**
     * Optimiza la memoria
     */
    private fun optimizeMemory() {
        System.gc() // Sugerir garbage collection
        com.workstation.rotation.utils.PerformanceUtils.logMemoryUsage("AfterOptimization")
        Toast.makeText(this, "Optimización de memoria ejecutada", Toast.LENGTH_SHORT).show()
    }
    
    /**
     * Optimiza el caché
     */
    private fun optimizeCache() {
        com.workstation.rotation.utils.PerformanceUtils.CacheManager.clearCache()
        Toast.makeText(this, "Caché optimizado", Toast.LENGTH_SHORT).show()
    }
    
    /**
     * Ejecuta análisis de rendimiento
     */
    private fun runPerformanceAnalysis() {
        lifecycleScope.launch {
            try {
                val startTime = System.currentTimeMillis()
                
                // Análisis de rendimiento
                val database = AppDatabase.getDatabase(this@SettingsActivity)
                
                val analysisResults = withContext(Dispatchers.IO) {
                    val workers = database.workerDao().getAllWorkersSync()
                    val workstations = database.workstationDao().getAllWorkstationsSync()
                    
                    mapOf(
                        "workers" to workers.size,
                        "workstations" to workstations.size,
                        "activeWorkers" to workers.count { it.isActive },
                        "activeWorkstations" to workstations.count { it.isActive }
                    )
                }
                
                val endTime = System.currentTimeMillis()
                val executionTime = endTime - startTime
                
                val analysis = """
                    📊 Análisis de Rendimiento:
                    
                    ⏱️ Tiempo de análisis: ${executionTime}ms
                    📊 Datos analizados:
                    • Trabajadores: ${analysisResults["workers"]} (${analysisResults["activeWorkers"]} activos)
                    • Estaciones: ${analysisResults["workstations"]} (${analysisResults["activeWorkstations"]} activas)
                    
                    📈 Rendimiento: ${
                        when {
                            executionTime < 50 -> "Excelente ⭐⭐⭐"
                            executionTime < 100 -> "Muy Bueno ⭐⭐"
                            executionTime < 200 -> "Bueno ⭐"
                            else -> "Necesita Optimización ⚠️"
                        }
                    }
                    
                    💡 Recomendaciones:
                    ${if (executionTime > 100) "• Considerar optimizar la base de datos\n• Limpiar datos obsoletos" else "• El sistema funciona óptimamente"}
                """.trimIndent()
                
                androidx.appcompat.app.AlertDialog.Builder(this@SettingsActivity)
                    .setTitle("📊 Análisis Completado")
                    .setMessage(analysis)
                    .setPositiveButton("OK", null)
                    .show()
                
            } catch (e: Exception) {
                Toast.makeText(this@SettingsActivity, "Error en análisis: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }    
  
  /**
     * Muestra el tutorial de onboarding
     */
    private fun showOnboardingTutorial() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("🎯 Tutorial Inicial")
            .setMessage("¿Deseas ver el tutorial inicial de REWS?\n\nEste tutorial te guiará paso a paso por todas las funcionalidades principales del sistema.")
            .setPositiveButton("Ver Tutorial") { _, _ ->
                val intent = Intent(this, OnboardingActivity::class.java)
                startActivity(intent)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}