package com.workstation.rotation.dashboard

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.workstation.rotation.R
import com.workstation.rotation.animations.closeDetails
import com.workstation.rotation.animations.AnimationManager
import com.workstation.rotation.animations.slideInChildrenFromBottom
import com.workstation.rotation.dashboard.adapters.KPICardAdapter
import com.workstation.rotation.dashboard.adapters.TrendChartAdapter
import com.workstation.rotation.dashboard.adapters.AlertsAdapter
import com.workstation.rotation.dashboard.models.KPICard
import com.workstation.rotation.dashboard.models.TrendData
import com.workstation.rotation.dashboard.models.AlertItem
import com.workstation.rotation.dashboard.viewmodels.ExecutiveDashboardViewModel
import android.widget.TextView
import android.widget.ProgressBar
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton

/**
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 📊 DASHBOARD EJECUTIVO - PANEL DE CONTROL GERENCIAL - REWS v3.1.0
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 
 * Panel de control ejecutivo que proporciona una vista completa del estado del sistema
 * con KPIs en tiempo real, tendencias, alertas y métricas de rendimiento.
 * 
 * 🎯 FUNCIONALIDADES PRINCIPALES:
 * • KPIs en tiempo real con indicadores visuales
 * • Gráficos de tendencias y análisis histórico
 * • Alertas proactivas y notificaciones críticas
 * • Métricas de ROI y eficiencia operativa
 * • Comparativas por período y benchmarking
 * • Exportación de reportes ejecutivos
 * 
 * 📈 MÉTRICAS INCLUIDAS:
 * • Eficiencia de rotaciones (%)
 * • Tiempo promedio de rotación
 * • Utilización de estaciones
 * • Productividad por trabajador
 * • Entrenamientos completados
 * • Alertas críticas activas
 * • ROI del sistema
 * • Tendencias semanales/mensuales
 * 
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 */

class ExecutiveDashboardActivity : AppCompatActivity() {
    
    private val viewModel: ExecutiveDashboardViewModel by viewModels()
    
    // Views principales
    private lateinit var loadingOverlay: View
    private lateinit var tvLastUpdate: TextView
    private lateinit var btnRefresh: MaterialButton
    private lateinit var fabExport: ExtendedFloatingActionButton
    
    // RecyclerViews para diferentes secciones
    private lateinit var recyclerKPIs: RecyclerView
    private lateinit var recyclerTrends: RecyclerView
    private lateinit var recyclerAlerts: RecyclerView
    
    // Adaptadores
    private lateinit var kpiAdapter: KPICardAdapter
    private lateinit var trendAdapter: TrendChartAdapter
    private lateinit var alertsAdapter: AlertsAdapter
    
    // Cards de resumen
    private lateinit var cardSystemHealth: MaterialCardView
    private lateinit var cardEfficiency: MaterialCardView
    private lateinit var cardProductivity: MaterialCardView
    private lateinit var cardROI: MaterialCardView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_executive_dashboard)
        
        setupToolbar()
        initializeViews()
        setupRecyclerViews()
        setupClickListeners()
        observeViewModel()
        setupAnimations()
        
        // Cargar datos iniciales
        viewModel.loadDashboardData()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Dashboard Ejecutivo"
    }
    
    private fun initializeViews() {
        loadingOverlay = findViewById(R.id.loadingOverlay)
        tvLastUpdate = findViewById(R.id.tvLastUpdate)
        btnRefresh = findViewById(R.id.btnRefresh)
        fabExport = findViewById(R.id.fabExport)
        
        // RecyclerViews
        recyclerKPIs = findViewById(R.id.recyclerKPIs)
        recyclerTrends = findViewById(R.id.recyclerTrends)
        recyclerAlerts = findViewById(R.id.recyclerAlerts)
        
        // Cards de resumen
        cardSystemHealth = findViewById(R.id.cardSystemHealth)
        cardEfficiency = findViewById(R.id.cardEfficiency)
        cardProductivity = findViewById(R.id.cardProductivity)
        cardROI = findViewById(R.id.cardROI)
    }
    
    private fun setupRecyclerViews() {
        // KPIs Grid Layout
        kpiAdapter = KPICardAdapter { kpi ->
            showKPIDetails(kpi)
        }
        recyclerKPIs.apply {
            layoutManager = GridLayoutManager(this@ExecutiveDashboardActivity, 2)
            adapter = kpiAdapter
        }
        
        // Trends Horizontal Layout
        trendAdapter = TrendChartAdapter { trend ->
            showTrendDetails(trend)
        }
        recyclerTrends.apply {
            layoutManager = LinearLayoutManager(this@ExecutiveDashboardActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = trendAdapter
        }
        
        // Alerts Vertical Layout
        alertsAdapter = AlertsAdapter(
            onAlertClick = { alert -> handleAlert(alert) },
            onAlertDismiss = { alert -> dismissAlert(alert) }
        )
        recyclerAlerts.apply {
            layoutManager = LinearLayoutManager(this@ExecutiveDashboardActivity)
            adapter = alertsAdapter
        }
    }
    
    private fun setupClickListeners() {
        btnRefresh.setOnClickListener {
            AnimationManager.clickFeedback(btnRefresh)
            viewModel.refreshDashboard()
        }
        
        fabExport.setOnClickListener {
            AnimationManager.clickFeedback(fabExport)
            showExportOptions()
        }
        
        // Cards de resumen
        cardSystemHealth.setOnClickListener {
            AnimationManager.clickFeedback(cardSystemHealth)
            showSystemHealthDetails()
        }
        
        cardEfficiency.setOnClickListener {
            AnimationManager.clickFeedback(cardEfficiency)
            showEfficiencyDetails()
        }
        
        cardProductivity.setOnClickListener {
            AnimationManager.clickFeedback(cardProductivity)
            showProductivityDetails()
        }
        
        cardROI.setOnClickListener {
            AnimationManager.clickFeedback(cardROI)
            showROIDetails()
        }
    }
    
    private fun observeViewModel() {
        // Estado de carga
        viewModel.isLoading.observe(this) { isLoading ->
            loadingOverlay.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
        
        // Última actualización
        viewModel.lastUpdate.observe(this) { timestamp ->
            tvLastUpdate.text = "Última actualización: ${formatTimestamp(timestamp)}"
        }
        
        // KPIs principales
        viewModel.kpiCards.observe(this) { kpis ->
            kpiAdapter.submitList(kpis)
        }
        
        // Datos de tendencias
        viewModel.trendData.observe(this) { trends ->
            trendAdapter.submitList(trends)
        }
        
        // Alertas activas
        viewModel.activeAlerts.observe(this) { alerts ->
            alertsAdapter.submitList(alerts)
        }
        
        // Métricas de resumen
        viewModel.systemHealthScore.observe(this) { score ->
            updateSystemHealthCard(score)
        }
        
        viewModel.overallEfficiency.observe(this) { efficiency ->
            updateEfficiencyCard(efficiency)
        }
        
        viewModel.productivityIndex.observe(this) { productivity ->
            updateProductivityCard(productivity)
        }
        
        viewModel.roiMetrics.observe(this) { roi ->
            updateROICard(roi)
        }
        
        // Mensajes de error
        viewModel.errorMessage.observe(this) { error ->
            error?.let {
                showErrorMessage(it)
                viewModel.clearError()
            }
        }
    }
    
    private fun setupAnimations() {
        // Animar entrada de las secciones principales
        val mainSections = listOf(
            cardSystemHealth,
            cardEfficiency,
            cardProductivity,
            cardROI,
            recyclerKPIs,
            recyclerTrends,
            recyclerAlerts
        )
        
        AnimationManager.staggeredListAnimation(
            views = mainSections,
            animationType = AnimationManager.StaggerType.SLIDE_IN_FROM_BOTTOM,
            baseDuration = AnimationManager.DURATION_MEDIUM,
            staggerDelay = AnimationManager.DELAY_MEDIUM
        )
        
        // Animar FAB con entrada espectacular
        AnimationManager.spectacularEntrance(
            view = fabExport,
            duration = AnimationManager.DURATION_LONG,
            delay = AnimationManager.DELAY_LONG * 4
        )
    }
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // 📊 ACTUALIZACIÓN DE CARDS DE RESUMEN
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    private fun updateSystemHealthCard(score: Double) {
        val healthText = cardSystemHealth.findViewById<TextView>(R.id.tvHealthScore)
        val healthProgress = cardSystemHealth.findViewById<ProgressBar>(R.id.progressHealth)
        val healthStatus = cardSystemHealth.findViewById<TextView>(R.id.tvHealthStatus)
        
        healthText.text = "${score.toInt()}%"
        healthProgress.progress = score.toInt()
        
        healthStatus.text = when {
            score >= 90 -> "Excelente"
            score >= 75 -> "Bueno"
            score >= 60 -> "Regular"
            else -> "Crítico"
        }
        
        // Animar cambio de valor
        AnimationManager.pulse(healthText, repeatCount = 1)
    }
    
    private fun updateEfficiencyCard(efficiency: Double) {
        val efficiencyText = cardEfficiency.findViewById<TextView>(R.id.tvEfficiencyValue)
        val efficiencyProgress = cardEfficiency.findViewById<ProgressBar>(R.id.progressEfficiency)
        val efficiencyTrend = cardEfficiency.findViewById<TextView>(R.id.tvEfficiencyTrend)
        
        efficiencyText.text = "${String.format("%.1f", efficiency)}%"
        efficiencyProgress.progress = efficiency.toInt()
        
        // Mostrar tendencia (simulada por ahora)
        val trend = if (efficiency > 75) "↗️ +2.3%" else "↘️ -1.1%"
        efficiencyTrend.text = trend
        
        AnimationManager.pulse(efficiencyText, repeatCount = 1)
    }
    
    private fun updateProductivityCard(productivity: Double) {
        val productivityText = cardProductivity.findViewById<TextView>(R.id.tvProductivityValue)
        val productivityIndicator = cardProductivity.findViewById<View>(R.id.indicatorProductivity)
        
        productivityText.text = "${String.format("%.2f", productivity)}"
        
        // Cambiar color del indicador según productividad
        val color = when {
            productivity >= 8.0 -> R.color.success
            productivity >= 6.0 -> R.color.warning
            else -> R.color.error
        }
        productivityIndicator.setBackgroundResource(color)
        
        AnimationManager.pulse(productivityText, repeatCount = 1)
    }
    
    private fun updateROICard(roi: Double) {
        val roiText = cardROI.findViewById<TextView>(R.id.tvROIValue)
        val roiStatus = cardROI.findViewById<TextView>(R.id.tvROIStatus)
        
        roiText.text = "${String.format("%.1f", roi)}%"
        
        roiStatus.text = when {
            roi >= 15 -> "Excelente ROI"
            roi >= 10 -> "Buen ROI"
            roi >= 5 -> "ROI Moderado"
            else -> "ROI Bajo"
        }
        
        AnimationManager.pulse(roiText, repeatCount = 1)
    }
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // 🔍 DETALLES Y ACCIONES
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    private fun showKPIDetails(kpi: KPICard) {
        // TODO: Implementar diálogo de detalles de KPI
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(kpi.title)
            .setMessage("Valor actual: ${kpi.value}\nTendencia: ${kpi.trend}\n\nDetalles completos próximamente...")
            .setPositiveButton("OK", null)
            .show()
    }
    
    private fun showTrendDetails(trend: TrendData) {
        // TODO: Implementar vista detallada de tendencias
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Tendencia: ${trend.title}")
            .setMessage("Análisis detallado de tendencias próximamente...")
            .setPositiveButton("OK", null)
            .show()
    }
    
    private fun handleAlert(alert: AlertItem) {
        // TODO: Implementar manejo de alertas
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Alerta: ${alert.title}")
            .setMessage(alert.description)
            .setPositiveButton("Resolver", null)
            .setNegativeButton("Descartar") { _, _ ->
                dismissAlert(alert)
            }
            .show()
    }
    
    private fun dismissAlert(alert: AlertItem) {
        viewModel.dismissAlert(alert.id)
    }
    
    private fun showSystemHealthDetails() {
        // TODO: Implementar vista detallada de salud del sistema
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Salud del Sistema")
            .setMessage("Análisis detallado de la salud del sistema próximamente...")
            .setPositiveButton("OK", null)
            .show()
    }
    
    private fun showEfficiencyDetails() {
        // TODO: Implementar vista detallada de eficiencia
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Análisis de Eficiencia")
            .setMessage("Análisis detallado de eficiencia próximamente...")
            .setPositiveButton("OK", null)
            .show()
    }
    
    private fun showProductivityDetails() {
        // TODO: Implementar vista detallada de productividad
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Métricas de Productividad")
            .setMessage("Análisis detallado de productividad próximamente...")
            .setPositiveButton("OK", null)
            .show()
    }
    
    private fun showROIDetails() {
        // TODO: Implementar vista detallada de ROI
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Análisis de ROI")
            .setMessage("Análisis detallado de ROI próximamente...")
            .setPositiveButton("OK", null)
            .show()
    }
    
    private fun showExportOptions() {
        val options = arrayOf(
            "Exportar Reporte PDF",
            "Exportar Datos Excel",
            "Enviar por Email",
            "Compartir Dashboard"
        )
        
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Opciones de Exportación")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> exportToPDF()
                    1 -> exportToExcel()
                    2 -> sendByEmail()
                    3 -> shareDashboard()
                }
            }
            .show()
    }
    
    private fun exportToPDF() {
        // TODO: Implementar exportación a PDF
        showMessage("Exportación a PDF próximamente...")
    }
    
    private fun exportToExcel() {
        // TODO: Implementar exportación a Excel
        showMessage("Exportación a Excel próximamente...")
    }
    
    private fun sendByEmail() {
        // TODO: Implementar envío por email
        showMessage("Envío por email próximamente...")
    }
    
    private fun shareDashboard() {
        // TODO: Implementar compartir dashboard
        showMessage("Compartir dashboard próximamente...")
    }
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // 🔧 UTILIDADES
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    private fun formatTimestamp(timestamp: Long): String {
        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(timestamp))
    }
    
    private fun showMessage(message: String) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
    }
    
    private fun showErrorMessage(error: String) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Error")
            .setMessage(error)
            .setPositiveButton("OK", null)
            .show()
    }
    
    override fun onSupportNavigateUp(): Boolean {
        closeDetails()
        return true
    }
    
    override fun onResume() {
        super.onResume()
        // Actualizar datos cuando se regresa a la pantalla
        viewModel.refreshDashboard()
    }
}