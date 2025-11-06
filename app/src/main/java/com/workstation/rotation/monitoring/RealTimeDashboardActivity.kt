package com.workstation.rotation.monitoring

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.workstation.rotation.databinding.ActivityRealTimeDashboardBinding
import com.workstation.rotation.monitoring.adapters.MonitoringAlertsAdapter
import com.workstation.rotation.monitoring.adapters.MetricsCardsAdapter
import com.workstation.rotation.monitoring.viewmodels.RealTimeDashboardViewModel
import kotlinx.coroutines.launch

/**
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 📊 DASHBOARD EN TIEMPO REAL - WorkStation Rotation v4.0.2+
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 
 * Dashboard interactivo que muestra métricas del sistema en tiempo real.
 * Incluye gráficos en vivo, alertas y estadísticas de rendimiento.
 * 
 * 🎯 CARACTERÍSTICAS:
 * • Métricas actualizadas cada segundo
 * • Gráficos interactivos en tiempo real
 * • Sistema de alertas visual
 * • Histórico de métricas con zoom
 * • Exportación de datos
 * 
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 */

class RealTimeDashboardActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityRealTimeDashboardBinding
    private val viewModel: RealTimeDashboardViewModel by viewModels()
    
    private lateinit var metricsAdapter: MetricsCardsAdapter
    private lateinit var alertsAdapter: MonitoringAlertsAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRealTimeDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupUI()
        setupRecyclerViews()
        setupObservers()
        setupClickListeners()
        
        // Iniciar monitoreo
        viewModel.startMonitoring()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        viewModel.stopMonitoring()
    }
    
    private fun setupUI() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Dashboard en Tiempo Real"
        
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }
    
    private fun setupRecyclerViews() {
        // Adapter para cards de métricas
        metricsAdapter = MetricsCardsAdapter()
        binding.recyclerViewMetrics.apply {
            layoutManager = LinearLayoutManager(this@RealTimeDashboardActivity)
            adapter = metricsAdapter
            setHasFixedSize(true)
        }
        
        // Adapter para alertas
        alertsAdapter = MonitoringAlertsAdapter { alert ->
            // Manejar click en alerta
            viewModel.dismissAlert(alert)
        }
        binding.recyclerViewAlerts.apply {
            layoutManager = LinearLayoutManager(this@RealTimeDashboardActivity)
            adapter = alertsAdapter
            setHasFixedSize(true)
        }
    }
    
    private fun setupObservers() {
        lifecycleScope.launch {
            // Observar métricas del sistema
            viewModel.systemMetrics.collect { metrics ->
                updateSystemMetrics(metrics)
            }
        }
        
        lifecycleScope.launch {
            // Observar métricas de rotación
            viewModel.rotationMetrics.collect { metrics ->
                updateRotationMetrics(metrics)
            }
        }
        
        lifecycleScope.launch {
            // Observar métricas de rendimiento
            viewModel.performanceMetrics.collect { metrics ->
                updatePerformanceMetrics(metrics)
            }
        }
        
        lifecycleScope.launch {
            // Observar alertas
            viewModel.alerts.collect { alerts ->
                updateAlerts(alerts)
            }
        }
        
        lifecycleScope.launch {
            // Observar estado de conexión
            viewModel.isConnected.collect { isConnected ->
                updateConnectionStatus(isConnected)
            }
        }
        
        lifecycleScope.launch {
            // Observar cards de métricas
            viewModel.metricsCards.collect { cards ->
                metricsAdapter.updateCards(cards)
            }
        }
    }
    
    private fun setupClickListeners() {
        // Botón de refresh manual
        binding.btnRefresh.setOnClickListener {
            viewModel.forceRefresh()
        }
        
        // Botón de exportar datos
        binding.btnExport.setOnClickListener {
            viewModel.exportMetrics()
        }
        
        // Botón de configuración de alertas
        binding.btnAlertSettings.setOnClickListener {
            // TODO: Abrir configuración de alertas
        }
        
        // Switch para pausar/reanudar monitoreo
        binding.switchMonitoring.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewModel.startMonitoring()
            } else {
                viewModel.pauseMonitoring()
            }
        }
    }
    
    private fun updateSystemMetrics(metrics: SystemMetrics) {
        binding.apply {
            // Memoria
            tvMemoryUsed.text = "${metrics.memoryUsed} MB"
            tvMemoryTotal.text = "${metrics.memoryTotal} MB"
            progressMemory.progress = ((metrics.memoryUsed.toFloat() / metrics.memoryTotal.toFloat()) * 100).toInt()
            
            // CPU
            tvCpuUsage.text = "${String.format("%.1f", metrics.cpuUsage)}%"
            progressCpu.progress = metrics.cpuUsage.toInt()
            
            // Threads
            tvActiveThreads.text = metrics.activeThreads.toString()
            
            // Uptime
            tvUptime.text = formatUptime(metrics.uptime)
            
            // Actualizar indicadores visuales
            updateMemoryIndicator(metrics.memoryUsed, metrics.memoryTotal)
            updateCpuIndicator(metrics.cpuUsage)
        }
    }
    
    private fun updateRotationMetrics(metrics: RotationMetrics) {
        // TODO: Implementar cuando se agreguen los elementos al layout
        // Por ahora solo actualizar las cards de métricas
        updateRotationChart(metrics)
    }
    
    private fun updatePerformanceMetrics(metrics: PerformanceMetrics) {
        // TODO: Implementar cuando se agreguen los elementos al layout
        // Por ahora solo actualizar indicadores
        updatePerformanceIndicators(metrics)
    }
    
    private fun updateAlerts(alerts: List<Alert>) {
        alertsAdapter.updateAlerts(alerts)
        
        // Mostrar/ocultar sección de alertas
        binding.cardAlerts.visibility = if (alerts.isNotEmpty()) View.VISIBLE else View.GONE
        
        // Actualizar contador de alertas
        binding.tvAlertsCount.text = alerts.size.toString()
        
        // Actualizar color del indicador según severidad
        val criticalAlerts = alerts.count { it.severity == AlertSeverity.CRITICAL }
        val warningAlerts = alerts.count { it.severity == AlertSeverity.WARNING }
        
        binding.indicatorAlerts.setBackgroundColor(
            when {
                criticalAlerts > 0 -> getColor(android.R.color.holo_red_dark)
                warningAlerts > 0 -> getColor(android.R.color.holo_orange_dark)
                else -> getColor(android.R.color.holo_green_dark)
            }
        )
    }
    
    private fun updateConnectionStatus(isConnected: Boolean) {
        binding.apply {
            indicatorConnection.setBackgroundColor(
                if (isConnected) getColor(android.R.color.holo_green_dark)
                else getColor(android.R.color.holo_red_dark)
            )
            
            tvConnectionStatus.text = if (isConnected) "Conectado" else "Desconectado"
            
            // Habilitar/deshabilitar controles según conexión
            btnRefresh.isEnabled = isConnected
            switchMonitoring.isEnabled = isConnected
        }
    }
    
    private fun updateMemoryIndicator(used: Long, total: Long) {
        val percentage = (used.toFloat() / total.toFloat()) * 100
        val color = when {
            percentage > 90 -> getColor(android.R.color.holo_red_dark)
            percentage > 75 -> getColor(android.R.color.holo_orange_dark)
            else -> getColor(android.R.color.holo_green_dark)
        }
        binding.indicatorMemory.setBackgroundColor(color)
    }
    
    private fun updateCpuIndicator(usage: Double) {
        val color = when {
            usage > 80 -> getColor(android.R.color.holo_red_dark)
            usage > 60 -> getColor(android.R.color.holo_orange_dark)
            else -> getColor(android.R.color.holo_green_dark)
        }
        binding.indicatorCpu.setBackgroundColor(color)
    }
    
    private fun updatePerformanceIndicators(metrics: PerformanceMetrics) {
        // TODO: Implementar cuando se agreguen los indicadores al layout
        // Por ahora solo log para debug
        println("Performance - Response: ${metrics.averageResponseTime}ms, Error Rate: ${metrics.errorRate}%")
    }
    
    private fun updateRotationChart(metrics: RotationMetrics) {
        // TODO: Implementar actualización de gráfico en tiempo real
        // Usar biblioteca como MPAndroidChart para gráficos dinámicos
    }
    
    private fun formatUptime(uptime: Long): String {
        val seconds = uptime / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24
        
        return when {
            days > 0 -> "${days}d ${hours % 24}h"
            hours > 0 -> "${hours}h ${minutes % 60}m"
            minutes > 0 -> "${minutes}m ${seconds % 60}s"
            else -> "${seconds}s"
        }
    }
}