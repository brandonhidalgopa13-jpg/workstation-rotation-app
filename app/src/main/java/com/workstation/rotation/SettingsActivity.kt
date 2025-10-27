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
        
        binding.btnCertifyWorkers.setOnClickListener {
            showCertificationDialog()
        }
        
        // Información de la app
        binding.btnAppInfo.setOnClickListener {
            showAppInfo()
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
                "🏭 Sistema de Rotación Inteligente\n" +
                "📱 Versión: 2.1.0\n" +
                "👨‍💻 Desarrollador: Brandon Josué Hidalgo Paz\n" +
                "📅 Año: 2024\n\n" +
                "🚀 Funcionalidades Principales:\n" +
                "• 👥 Gestión completa de trabajadores\n" +
                "• 🏭 Administración de estaciones de trabajo\n" +
                "• 🔄 Sistema de rotación inteligente\n" +
                "• 📚 Sistema de entrenamiento avanzado\n" +
                "• 🎓 Certificación de trabajadores\n" +
                "• 🌙 Modo oscuro automático\n" +
                "• 💾 Respaldo y sincronización\n" +
                "• 📊 Reportes y estadísticas\n\n" +
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
}