package com.workstation.rotation

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.workstation.rotation.analytics.RotationAnalytics
import com.workstation.rotation.backup.AutoBackupManager
import com.workstation.rotation.data.database.AppDatabase
import com.workstation.rotation.databinding.ActivityDiagnosticsBinding
import com.workstation.rotation.validation.RotationValidator
import com.workstation.rotation.viewmodels.SqlRotationViewModel
import com.workstation.rotation.viewmodels.SqlRotationViewModelFactory
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 🔍 ACTIVITY DE DIAGNÓSTICO Y MONITOREO DEL SISTEMA
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 
 * Esta Activity proporciona:
 * - Diagnóstico completo del sistema
 * - Métricas de rendimiento en tiempo real
 * - Validación de integridad de datos
 * - Gestión de backups
 * - Recomendaciones de optimización
 * - Herramientas de mantenimiento
 * 
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 */
class DiagnosticsActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityDiagnosticsBinding
    
    private val sqlViewModel: SqlRotationViewModel by viewModels {
        val database = AppDatabase.getDatabase(this)
        SqlRotationViewModelFactory(
            database.rotationDao(),
            database.workerDao(),
            database.workstationDao()
        )
    }
    
    private lateinit var analytics: RotationAnalytics
    private lateinit var validator: RotationValidator
    private lateinit var backupManager: AutoBackupManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDiagnosticsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        initializeComponents()
        setupToolbar()
        setupButtons()
        loadDiagnostics()
    }
    
    private fun initializeComponents() {
        analytics = RotationAnalytics.getInstance()
        validator = RotationValidator()
        backupManager = AutoBackupManager.getInstance(this)
    }
    
    private fun setupToolbar() {
        binding.toolbar.title = "🔍 Diagnóstico del Sistema"
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }
    
    private fun setupButtons() {
        binding.btnRefreshDiagnostics.setOnClickListener {
            loadDiagnostics()
        }
        
        binding.btnRunValidation.setOnClickListener {
            runSystemValidation()
        }
        
        binding.btnCreateBackup.setOnClickListener {
            createManualBackup()
        }
        
        binding.btnViewBackups.setOnClickListener {
            showBackupsList()
        }
        
        binding.btnClearMetrics.setOnClickListener {
            clearAllMetrics()
        }
        
        binding.btnExportReport.setOnClickListener {
            exportDiagnosticsReport()
        }
    }
    
    private fun loadDiagnostics() {
        lifecycleScope.launch {
            try {
                binding.progressBar.visibility = View.VISIBLE
                binding.scrollView.visibility = View.GONE
                
                // Cargar métricas de analytics
                loadAnalyticsMetrics()
                
                // Cargar información del sistema
                loadSystemInfo()
                
                // Cargar información de backups
                loadBackupInfo()
                
                binding.progressBar.visibility = View.GONE
                binding.scrollView.visibility = View.VISIBLE
                
            } catch (e: Exception) {
                showError("Error cargando diagnósticos: ${e.message}")
            }
        }
    }
    
    private fun loadAnalyticsMetrics() {
        lifecycleScope.launch {
            try {
                analytics.performanceMetrics.collect { metrics ->
                    updatePerformanceMetrics(metrics)
                }
            } catch (e: Exception) {
                println("DIAGNOSTICS: Error cargando métricas de rendimiento: ${e.message}")
            }
        }
        
        lifecycleScope.launch {
            try {
                analytics.usageMetrics.collect { metrics ->
                    updateUsageMetrics(metrics)
                }
            } catch (e: Exception) {
                println("DIAGNOSTICS: Error cargando métricas de uso: ${e.message}")
            }
        }
        
        lifecycleScope.launch {
            try {
                analytics.qualityMetrics.collect { metrics ->
                    updateQualityMetrics(metrics)
                }
            } catch (e: Exception) {
                println("DIAGNOSTICS: Error cargando métricas de calidad: ${e.message}")
            }
        }
    }
    
    private fun updatePerformanceMetrics(metrics: com.workstation.rotation.analytics.PerformanceMetrics) {
        binding.apply {
            tvSqlRotationTime.text = "${metrics.avgSqlRotationTime.toInt()}ms"
            tvOriginalRotationTime.text = "${metrics.avgOriginalRotationTime.toInt()}ms"
            tvDatabaseQueryTime.text = "${metrics.avgDatabaseQueryTime.toInt()}ms"
            tvTotalQueries.text = metrics.totalDatabaseQueries.toString()
            
            // Calcular mejora de rendimiento
            if (metrics.avgOriginalRotationTime > 0 && metrics.avgSqlRotationTime > 0) {
                val improvement = ((metrics.avgOriginalRotationTime - metrics.avgSqlRotationTime) / metrics.avgOriginalRotationTime) * 100
                tvPerformanceImprovement.text = "${improvement.toInt()}% más rápido"
                tvPerformanceImprovement.setTextColor(
                    ContextCompat.getColor(this@DiagnosticsActivity, R.color.status_success)
                )
            } else {
                tvPerformanceImprovement.text = "N/A"
            }
        }
    }
    
    private fun updateUsageMetrics(metrics: com.workstation.rotation.analytics.UsageMetrics) {
        binding.apply {
            tvTotalRotations.text = metrics.totalRotationsGenerated.toString()
            tvSqlRotations.text = metrics.sqlRotationsGenerated.toString()
            tvOriginalRotations.text = metrics.originalRotationsGenerated.toString()
            tvRotationToggles.text = metrics.rotationHalfToggles.toString()
            tvAppOpens.text = metrics.appOpenCount.toString()
            
            // Mostrar última rotación
            if (metrics.lastRotationTimestamp > 0) {
                val lastRotation = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                    .format(Date(metrics.lastRotationTimestamp))
                tvLastRotation.text = lastRotation
            } else {
                tvLastRotation.text = "Nunca"
            }
        }
    }
    
    private fun updateQualityMetrics(metrics: com.workstation.rotation.analytics.QualityMetrics) {
        binding.apply {
            tvStationCompletion.text = "${metrics.avgStationCompletionRate.toInt()}%"
            tvLeaderAccuracy.text = "${metrics.avgLeaderAccuracy.toInt()}%"
            tvTrainingAccuracy.text = "${metrics.avgTrainingAccuracy.toInt()}%"
            tvQualityMeasurements.text = metrics.totalQualityMeasurements.toString()
            
            // Colores según calidad
            val completionColor = when {
                metrics.avgStationCompletionRate >= 90 -> R.color.status_success
                metrics.avgStationCompletionRate >= 70 -> R.color.status_warning
                else -> R.color.status_error
            }
            tvStationCompletion.setTextColor(ContextCompat.getColor(this@DiagnosticsActivity, completionColor))
            
            val leaderColor = if (metrics.avgLeaderAccuracy >= 95) R.color.status_success else R.color.status_warning
            tvLeaderAccuracy.setTextColor(ContextCompat.getColor(this@DiagnosticsActivity, leaderColor))
            
            val trainingColor = if (metrics.avgTrainingAccuracy >= 95) R.color.status_success else R.color.status_warning
            tvTrainingAccuracy.setTextColor(ContextCompat.getColor(this@DiagnosticsActivity, trainingColor))
        }
    }
    
    private suspend fun loadSystemInfo() {
        try {
            val database = AppDatabase.getDatabase(this)
            
            val totalWorkers = database.workerDao().getAllWorkers().first().size
            val activeWorkers = database.workerDao().getAllWorkers().first().count { it.isActive }
            val totalStations = database.workstationDao().getAllActiveWorkstations().first().size
            val priorityStations = database.workstationDao().getAllActiveWorkstations().first().count { it.isPriority }
            
            binding.apply {
                tvTotalWorkers.text = totalWorkers.toString()
                tvActiveWorkers.text = activeWorkers.toString()
                tvTotalStations.text = totalStations.toString()
                tvPriorityStations.text = priorityStations.toString()
            }
            
        } catch (e: Exception) {
            println("DIAGNOSTICS: Error cargando información del sistema: ${e.message}")
        }
    }
    
    private suspend fun loadBackupInfo() {
        try {
            val backups = backupManager.getAvailableBackups()
            val validBackups = backups.count { it.isValid }
            val totalSize = backups.sumOf { it.size }
            
            binding.apply {
                tvTotalBackups.text = backups.size.toString()
                tvValidBackups.text = validBackups.toString()
                tvBackupsSize.text = formatFileSize(totalSize)
                
                if (backups.isNotEmpty()) {
                    val lastBackup = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                        .format(Date(backups.first().createdAt))
                    tvLastBackup.text = lastBackup
                } else {
                    tvLastBackup.text = "Nunca"
                }
            }
            
        } catch (e: Exception) {
            println("DIAGNOSTICS: Error cargando información de backups: ${e.message}")
        }
    }
    
    private fun runSystemValidation() {
        lifecycleScope.launch {
            try {
                binding.btnRunValidation.isEnabled = false
                binding.btnRunValidation.text = "Validando..."
                
                val database = AppDatabase.getDatabase(this@DiagnosticsActivity)
                val workers = database.workerDao().getAllWorkers().first()
                val workstations = database.workstationDao().getAllActiveWorkstations().first()
                val workerWorkstationMap = workers.associate { worker ->
                    worker.id to database.workerDao().getWorkerWorkstationIds(worker.id)
                }
                
                val results = validator.validateSystem(workers, workstations, workerWorkstationMap)
                
                showValidationResults(results)
                
            } catch (e: Exception) {
                showError("Error en validación: ${e.message}")
            } finally {
                binding.btnRunValidation.isEnabled = true
                binding.btnRunValidation.text = "Ejecutar Validación"
            }
        }
    }
    
    private fun showValidationResults(results: com.workstation.rotation.validation.ValidationResults) {
        val message = buildString {
            appendLine("🔍 RESULTADOS DE VALIDACIÓN")
            appendLine("=" .repeat(30))
            
            if (results.isValid) {
                appendLine("✅ Sistema válido - No se encontraron problemas críticos")
            } else {
                appendLine("❌ Se encontraron ${results.criticalIssues.size} problemas críticos")
            }
            
            if (results.issues.isNotEmpty()) {
                appendLine("\n🚨 PROBLEMAS (${results.issues.size}):")
                results.issues.take(5).forEach { issue ->
                    appendLine("• ${issue.message}")
                }
                if (results.issues.size > 5) {
                    appendLine("... y ${results.issues.size - 5} más")
                }
            }
            
            if (results.warnings.isNotEmpty()) {
                appendLine("\n⚠️ ADVERTENCIAS (${results.warnings.size}):")
                results.warnings.take(3).forEach { warning ->
                    appendLine("• ${warning.message}")
                }
            }
            
            if (results.suggestions.isNotEmpty()) {
                appendLine("\n💡 SUGERENCIAS (${results.suggestions.size}):")
                results.suggestions.take(3).forEach { suggestion ->
                    appendLine("• ${suggestion.message}")
                }
            }
        }
        
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Validación del Sistema")
            .setMessage(message)
            .setPositiveButton("Entendido", null)
            .show()
    }
    
    private fun createManualBackup() {
        lifecycleScope.launch {
            try {
                binding.btnCreateBackup.isEnabled = false
                binding.btnCreateBackup.text = "Creando..."
                
                val result = backupManager.createBackup(com.workstation.rotation.backup.BackupReason.MANUAL)
                
                when (result) {
                    is com.workstation.rotation.backup.BackupResult.Success -> {
                        showSuccess("Backup creado exitosamente: ${result.backupFile.name}")
                        loadBackupInfo() // Actualizar información
                    }
                    is com.workstation.rotation.backup.BackupResult.Error -> {
                        showError("Error creando backup: ${result.message}")
                    }
                }
                
            } catch (e: Exception) {
                showError("Error: ${e.message}")
            } finally {
                binding.btnCreateBackup.isEnabled = true
                binding.btnCreateBackup.text = "Crear Backup"
            }
        }
    }
    
    private fun showBackupsList() {
        lifecycleScope.launch {
            try {
                val backups = backupManager.getAvailableBackups()
                
                if (backups.isEmpty()) {
                    showInfo("No hay backups disponibles")
                    return@launch
                }
                
                val backupNames = backups.map { backup ->
                    val date = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                        .format(Date(backup.createdAt))
                    val status = if (backup.isValid) "✅" else "❌"
                    val size = formatFileSize(backup.size)
                    "$status $date (${backup.reason.name}) - $size"
                }.toTypedArray()
                
                androidx.appcompat.app.AlertDialog.Builder(this@DiagnosticsActivity)
                    .setTitle("Backups Disponibles (${backups.size})")
                    .setItems(backupNames) { _, which ->
                        showBackupDetails(backups[which])
                    }
                    .setNegativeButton("Cerrar", null)
                    .show()
                
            } catch (e: Exception) {
                showError("Error cargando backups: ${e.message}")
            }
        }
    }
    
    private fun showBackupDetails(backup: com.workstation.rotation.backup.BackupInfo) {
        val details = buildString {
            appendLine("📁 DETALLES DEL BACKUP")
            appendLine("=" .repeat(25))
            appendLine("Archivo: ${backup.file.name}")
            appendLine("Fecha: ${SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date(backup.createdAt))}")
            appendLine("Razón: ${backup.reason.name}")
            appendLine("Tamaño: ${formatFileSize(backup.size)}")
            appendLine("Estado: ${if (backup.isValid) "✅ Válido" else "❌ Corrupto"}")
            appendLine()
            appendLine("📊 CONTENIDO:")
            appendLine("Trabajadores: ${backup.metadata.totalWorkers}")
            appendLine("Estaciones: ${backup.metadata.totalWorkstations}")
            appendLine("Versión App: ${backup.metadata.appVersion}")
            appendLine("Dispositivo: ${backup.metadata.deviceInfo}")
        }
        
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Detalles del Backup")
            .setMessage(details)
            .setPositiveButton("Cerrar", null)
            .setNeutralButton("Restaurar") { _, _ ->
                confirmRestoreBackup(backup)
            }
            .show()
    }
    
    private fun confirmRestoreBackup(backup: com.workstation.rotation.backup.BackupInfo) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("⚠️ Confirmar Restauración")
            .setMessage("¿Estás seguro de que quieres restaurar este backup?\n\nEsto reemplazará todos los datos actuales del sistema.")
            .setPositiveButton("Restaurar") { _, _ ->
                restoreBackup(backup)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    
    private fun restoreBackup(backup: com.workstation.rotation.backup.BackupInfo) {
        lifecycleScope.launch {
            try {
                val result = backupManager.restoreFromBackup(backup.file)
                
                when (result) {
                    is com.workstation.rotation.backup.RestoreResult.Success -> {
                        showSuccess("Sistema restaurado exitosamente desde backup")
                        loadDiagnostics() // Recargar todo
                    }
                    is com.workstation.rotation.backup.RestoreResult.Error -> {
                        showError("Error restaurando backup: ${result.message}")
                    }
                }
                
            } catch (e: Exception) {
                showError("Error: ${e.message}")
            }
        }
    }
    
    private fun clearAllMetrics() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("⚠️ Limpiar Métricas")
            .setMessage("¿Estás seguro de que quieres limpiar todas las métricas?\n\nEsta acción no se puede deshacer.")
            .setPositiveButton("Limpiar") { _, _ ->
                analytics.clearAllMetrics()
                showSuccess("Métricas limpiadas exitosamente")
                loadDiagnostics()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    
    private fun exportDiagnosticsReport() {
        lifecycleScope.launch {
            try {
                val report = analytics.getAnalyticsReport()
                
                val reportText = buildString {
                    appendLine("🔍 REPORTE DE DIAGNÓSTICO DEL SISTEMA")
                    appendLine("=" .repeat(50))
                    appendLine("Generado: ${SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())}")
                    appendLine()
                    
                    appendLine("📊 MÉTRICAS DE RENDIMIENTO:")
                    appendLine("Tiempo promedio SQL: ${report.performanceMetrics.avgSqlRotationTime.toInt()}ms")
                    appendLine("Tiempo promedio Original: ${report.performanceMetrics.avgOriginalRotationTime.toInt()}ms")
                    appendLine("Consultas BD promedio: ${report.performanceMetrics.avgDatabaseQueryTime.toInt()}ms")
                    appendLine("Total consultas: ${report.performanceMetrics.totalDatabaseQueries}")
                    appendLine()
                    
                    appendLine("📈 MÉTRICAS DE USO:")
                    appendLine("Total rotaciones: ${report.usageMetrics.totalRotationsGenerated}")
                    appendLine("Rotaciones SQL: ${report.usageMetrics.sqlRotationsGenerated}")
                    appendLine("Rotaciones originales: ${report.usageMetrics.originalRotationsGenerated}")
                    appendLine("Alternancias: ${report.usageMetrics.rotationHalfToggles}")
                    appendLine()
                    
                    appendLine("🎯 MÉTRICAS DE CALIDAD:")
                    appendLine("Completitud estaciones: ${report.qualityMetrics.avgStationCompletionRate.toInt()}%")
                    appendLine("Precisión líderes: ${report.qualityMetrics.avgLeaderAccuracy.toInt()}%")
                    appendLine("Precisión entrenamiento: ${report.qualityMetrics.avgTrainingAccuracy.toInt()}%")
                    appendLine()
                    
                    if (report.recommendations.isNotEmpty()) {
                        appendLine("💡 RECOMENDACIONES:")
                        report.recommendations.forEach { recommendation ->
                            appendLine("• $recommendation")
                        }
                    }
                }
                
                // Aquí podrías guardar el reporte en un archivo o compartirlo
                showInfo("Reporte generado:\n\n$reportText")
                
            } catch (e: Exception) {
                showError("Error generando reporte: ${e.message}")
            }
        }
    }
    
    private fun formatFileSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            else -> "${bytes / (1024 * 1024)} MB"
        }
    }
    
    private fun showSuccess(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setBackgroundTint(ContextCompat.getColor(this, R.color.status_success))
            .show()
    }
    
    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setBackgroundTint(ContextCompat.getColor(this, R.color.status_error))
            .show()
    }
    
    private fun showInfo(message: String) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Información")
            .setMessage(message)
            .setPositiveButton("Entendido", null)
            .show()
    }
}