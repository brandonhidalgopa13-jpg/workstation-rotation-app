package com.workstation.rotation.analytics

import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.workstation.rotation.R
import com.workstation.rotation.analytics.adapters.*
import com.workstation.rotation.analytics.models.*
import com.workstation.rotation.analytics.viewmodels.AdvancedAnalyticsViewModel
import com.workstation.rotation.analytics.viewmodels.AnalysisType
import com.workstation.rotation.data.database.AppDatabase
import kotlinx.coroutines.launch

/**
 * Activity principal para Analytics Avanzados
 * Proporciona análisis predictivo, patrones y métricas ML básicas
 */
class AdvancedAnalyticsActivity : AppCompatActivity() {

    private val viewModel: AdvancedAnalyticsViewModel by viewModels {
        AdvancedAnalyticsViewModelFactory(AppDatabase.getDatabase(this))
    }

    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var analyticsAdapter: AnalyticsViewPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_advanced_analytics)

        setupToolbar()
        setupViewPager()
        setupObservers()
        
        // Cargar datos iniciales
        viewModel.loadAllAnalytics()
    }

    private fun setupToolbar() {
        supportActionBar?.apply {
            title = "Analytics Avanzados"
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }
    }

    private fun setupViewPager() {
        viewPager = findViewById(R.id.viewPager)
        tabLayout = findViewById(R.id.tabLayout)

        analyticsAdapter = AnalyticsViewPagerAdapter(this)
        viewPager.adapter = analyticsAdapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "📊 Resumen"
                1 -> "🔍 Patrones"
                2 -> "🔮 Predicciones"
                3 -> "⚡ Rendimiento"
                4 -> "📈 Carga de Trabajo"
                5 -> "🚫 Cuellos de Botella"
                6 -> "📋 Reportes"
                else -> "Tab $position"
            }
        }.attach()
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                handleUiState(state)
            }
        }

        lifecycleScope.launch {
            viewModel.rotationPatterns.collect { patterns ->
                analyticsAdapter.updatePatterns(patterns)
            }
        }

        lifecycleScope.launch {
            viewModel.predictions.collect { predictions ->
                analyticsAdapter.updatePredictions(predictions)
            }
        }

        lifecycleScope.launch {
            viewModel.performanceMetrics.collect { metrics ->
                analyticsAdapter.updatePerformanceMetrics(metrics)
            }
        }

        lifecycleScope.launch {
            viewModel.workloadAnalysis.collect { analysis ->
                analyticsAdapter.updateWorkloadAnalysis(analysis)
            }
        }

        lifecycleScope.launch {
            viewModel.bottleneckAnalysis.collect { bottlenecks ->
                analyticsAdapter.updateBottleneckAnalysis(bottlenecks)
            }
        }

        lifecycleScope.launch {
            viewModel.advancedReports.collect { reports ->
                analyticsAdapter.updateReports(reports)
            }
        }
    }

    private fun handleUiState(state: com.workstation.rotation.analytics.viewmodels.AdvancedAnalyticsUiState) {
        // Manejar loading state
        if (state.isLoading) {
            // Mostrar indicador de carga
        }

        // Manejar errores
        state.error?.let { error ->
            Toast.makeText(this, error, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }

        // Manejar generación de reportes
        if (state.isGeneratingReport) {
            Toast.makeText(this, "Generando reporte...", Toast.LENGTH_SHORT).show()
        }

        // Notificar cuando se complete un reporte
        state.lastReportGenerated?.let { report ->
            Toast.makeText(this, "Reporte '${report.title}' generado exitosamente", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}

/**
 * Factory para crear el ViewModel con dependencias
 */
class AdvancedAnalyticsViewModelFactory(
    private val database: AppDatabase
) : androidx.lifecycle.ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdvancedAnalyticsViewModel::class.java)) {
            return AdvancedAnalyticsViewModel(database) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}