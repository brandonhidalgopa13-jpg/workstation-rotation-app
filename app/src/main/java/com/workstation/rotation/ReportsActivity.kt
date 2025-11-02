package com.workstation.rotation

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.workstation.rotation.adapters.WorkerReportAdapter
import com.workstation.rotation.data.database.AppDatabase
import com.workstation.rotation.databinding.ActivityReportsBinding
import com.workstation.rotation.services.ReportsService
import com.workstation.rotation.services.WorkerSummary
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 📊 ACTIVIDAD DE REPORTES Y MÉTRICAS
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 
 * Esta actividad proporciona una interfaz completa para visualizar:
 * - Reportes individuales por trabajador
 * - Métricas de permanencia por estación
 * - Estadísticas generales del sistema
 * - Análisis de rendimiento y recomendaciones
 * 
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 */
class ReportsActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityReportsBinding
    private lateinit var reportsService: ReportsService
    private lateinit var workerReportAdapter: WorkerReportAdapter
    
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupComponents()
        setupUI()
        setupListeners()
        loadInitialData()
    }
    
    private fun setupComponents() {
        val database = AppDatabase.getDatabase(this)
        reportsService = ReportsService(
            database.reportsDao(),
            database.workerDao(),
            database.workstationDao()
        )
        
        workerReportAdapter = WorkerReportAdapter { workerSummary ->
            showWorkerDetailReport(workerSummary.id)
        }
    }
    
    private fun setupUI() {
        // Configurar toolbar
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
        
        // Configurar RecyclerView
        binding.recyclerViewWorkers.apply {
            layoutManager = LinearLayoutManager(this@ReportsActivity)
            adapter = workerReportAdapter
        }
        
        // Mostrar fecha de generación
        binding.tvGeneratedAt.text = "Generado: ${dateFormat.format(Date())}"
    }
    
    private fun setupListeners() {
        // Botón para generar reporte general
        binding.btnGenerateReport.setOnClickListener {
            generateAllWorkersReport()
        }
        
        // Botón para exportar reportes
        binding.btnExportReports.setOnClickListener {
            exportAllReports()
        }
        
        // Botón para actualizar datos
        binding.btnRefreshData.setOnClickListener {
            loadInitialData()
        }
    }
    
    private fun loadInitialData() {
        lifecycleScope.launch {
            try {
                showLoading(true)
                
                // Cargar reporte general
                val allWorkersReport = reportsService.generateAllWorkersReport()
                
                // Actualizar UI con datos generales
                updateSystemMetrics(allWorkersReport)
                
                // Cargar lista de trabajadores
                workerReportAdapter.submitList(allWorkersReport.workerSummaries)
                
                showLoading(false)
                
            } catch (e: Exception) {
                showLoading(false)
                Toast.makeText(this@ReportsActivity, "Error al cargar datos: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun updateSystemMetrics(report: com.workstation.rotation.services.AllWorkersReport) {
        binding.apply {
            // Métricas generales
            tvTotalWorkers.text = report.totalWorkers.toString()
            tvSystemUtilization.text = "${String.format("%.1f", report.systemMetrics.systemUtilization)}%"
            tvAverageAvailability.text = "${String.format("%.1f", report.systemMetrics.averageAvailability)}%"
            tvCoveragePercentage.text = "${String.format("%.1f", report.systemMetrics.coveragePercentage)}%"
            
            // Distribución de roles
            val roleDistribution = report.roleDistribution
            tvLeadersCount.text = "${roleDistribution["LÍDERES"]?.toInt() ?: 0}%"
            tvTrainersCount.text = "${roleDistribution["ENTRENADORES"]?.toInt() ?: 0}%"
            tvTraineesCount.text = "${roleDistribution["ENTRENADOS"]?.toInt() ?: 0}%"
            tvRestrictionsCount.text = "${roleDistribution["CON RESTRICCIONES"]?.toInt() ?: 0}%"
            
            // Actualizar fecha
            tvGeneratedAt.text = "Generado: ${dateFormat.format(report.generatedAt)}"
        }
    }
    
    private fun generateAllWorkersReport() {
        lifecycleScope.launch {
            try {
                showLoading(true)
                
                val report = reportsService.generateAllWorkersReport()
                
                // Mostrar diálogo con resumen del reporte
                androidx.appcompat.app.AlertDialog.Builder(this@ReportsActivity)
                    .setTitle("📊 Reporte General Generado")
                    .setMessage(
                        "Reporte generado exitosamente:\n\n" +
                        "👥 Total de trabajadores: ${report.totalWorkers}\n" +
                        "📈 Utilización del sistema: ${String.format("%.1f%%", report.systemMetrics.systemUtilization)}\n" +
                        "📊 Disponibilidad promedio: ${String.format("%.1f%%", report.systemMetrics.averageAvailability)}\n" +
                        "🎯 Cobertura: ${String.format("%.1f%%", report.systemMetrics.coveragePercentage)}\n\n" +
                        "¿Deseas exportar este reporte?"
                    )
                    .setPositiveButton("Exportar") { _, _ ->
                        exportGeneralReport(report)
                    }
                    .setNegativeButton("Cerrar", null)
                    .show()
                
                // Actualizar UI
                updateSystemMetrics(report)
                workerReportAdapter.submitList(report.workerSummaries)
                
                showLoading(false)
                
            } catch (e: Exception) {
                showLoading(false)
                Toast.makeText(this@ReportsActivity, "Error al generar reporte: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun showWorkerDetailReport(workerId: Long) {
        lifecycleScope.launch {
            try {
                showLoading(true)
                
                val workerReport = reportsService.generateWorkerReport(workerId)
                
                // Mostrar reporte detallado en diálogo
                showWorkerDetailDialog(workerReport)
                
                showLoading(false)
                
            } catch (e: Exception) {
                showLoading(false)
                Toast.makeText(this@ReportsActivity, "Error al cargar reporte del trabajador: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun exportAllReports() {
        lifecycleScope.launch {
            try {
                showLoading(true)
                
                // Generar reporte general
                val allWorkersReport = reportsService.generateAllWorkersReport()
                
                // Crear texto del reporte
                val reportText = buildString {
                    appendLine("📊 REPORTE GENERAL DEL SISTEMA")
                    appendLine("═".repeat(50))
                    appendLine("📅 Generado: ${dateFormat.format(allWorkersReport.generatedAt)}")
                    appendLine()
                    
                    appendLine("📈 MÉTRICAS DEL SISTEMA")
                    appendLine("-".repeat(30))
                    appendLine("👥 Total de trabajadores: ${allWorkersReport.systemMetrics.totalActiveWorkers}")
                    appendLine("🏭 Total de estaciones: ${allWorkersReport.systemMetrics.totalActiveStations}")
                    appendLine("📊 Utilización del sistema: ${String.format("%.1f%%", allWorkersReport.systemMetrics.systemUtilization)}")
                    appendLine("📈 Disponibilidad promedio: ${String.format("%.1f%%", allWorkersReport.systemMetrics.averageAvailability)}")
                    appendLine("🎯 Cobertura: ${String.format("%.1f%%", allWorkersReport.systemMetrics.coveragePercentage)}")
                    appendLine()
                    
                    appendLine("👑 DISTRIBUCIÓN DE ROLES")
                    appendLine("-".repeat(30))
                    allWorkersReport.roleDistribution.forEach { (role, percentage) ->
                        appendLine("$role: ${String.format("%.1f%%", percentage)}")
                    }
                    appendLine()
                    
                    appendLine("🏭 UTILIZACIÓN POR ESTACIÓN")
                    appendLine("-".repeat(30))
                    allWorkersReport.stationUtilization.forEach { station ->
                        val priority = if (station.isPriority) " ⭐" else ""
                        appendLine("${station.stationName}$priority: ${String.format("%.1f%%", station.utilizationPercentage)} (${station.assignedWorkers}/${station.requiredWorkers})")
                    }
                    appendLine()
                    
                    appendLine("👥 RESUMEN POR TRABAJADOR")
                    appendLine("-".repeat(30))
                    allWorkersReport.workerSummaries.forEach { worker ->
                        appendLine("${worker.name} (${worker.workerType}): ${worker.availabilityPercentage}% disponibilidad, ${worker.totalStationsAssigned} estaciones")
                    }
                }
                
                // Compartir reporte
                shareReport("Reporte General del Sistema", reportText)
                
                showLoading(false)
                
            } catch (e: Exception) {
                showLoading(false)
                Toast.makeText(this@ReportsActivity, "Error al exportar reportes: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun exportGeneralReport(report: com.workstation.rotation.services.AllWorkersReport) {
        val reportText = buildString {
            appendLine("📊 REPORTE GENERAL - ${dateFormat.format(report.generatedAt)}")
            appendLine("═".repeat(50))
            appendLine("Sistema de Rotación Inteligente")
            appendLine()
            appendLine("📈 MÉTRICAS PRINCIPALES:")
            appendLine("• Trabajadores activos: ${report.systemMetrics.totalActiveWorkers}")
            appendLine("• Utilización: ${String.format("%.1f%%", report.systemMetrics.systemUtilization)}")
            appendLine("• Disponibilidad promedio: ${String.format("%.1f%%", report.systemMetrics.averageAvailability)}")
            appendLine("• Cobertura: ${String.format("%.1f%%", report.systemMetrics.coveragePercentage)}")
        }
        
        shareReport("Reporte General", reportText)
    }
    
    private fun shareReport(title: String, content: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, content)
        }
        startActivity(Intent.createChooser(shareIntent, "Compartir reporte"))
    }
    
    private fun showWorkerDetailDialog(workerReport: com.workstation.rotation.services.WorkerReport) {
        val reportText = reportsService.exportWorkerReportToText(workerReport)
        
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("📊 Reporte Detallado - ${workerReport.workerName}")
            .setMessage(reportText)
            .setPositiveButton("Cerrar", null)
            .setNeutralButton("Compartir") { _, _ ->
                shareReport("Reporte de ${workerReport.workerName}", reportText)
            }
            .show()
    }
    
    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnGenerateReport.isEnabled = !show
        binding.btnExportReports.isEnabled = !show
        binding.btnRefreshData.isEnabled = !show
    }
}