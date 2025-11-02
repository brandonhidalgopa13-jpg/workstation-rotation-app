package com.workstation.rotation.analytics.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.workstation.rotation.analytics.fragments.*
import com.workstation.rotation.analytics.models.*

/**
 * Adapter para el ViewPager de Analytics Avanzados
 * Gestiona los diferentes fragmentos de análisis
 */
class AnalyticsViewPagerAdapter(
    fragmentActivity: FragmentActivity
) : FragmentStateAdapter(fragmentActivity) {

    private val fragments = mutableListOf<Fragment>()
    
    // Datos compartidos entre fragmentos
    private var patterns: List<RotationPattern> = emptyList()
    private var predictions: List<RotationPrediction> = emptyList()
    private var performanceMetrics: List<WorkerPerformanceMetrics> = emptyList()
    private var workloadAnalysis: List<WorkloadAnalysis> = emptyList()
    private var bottleneckAnalysis: List<BottleneckAnalysis> = emptyList()
    private var reports: List<AdvancedReport> = emptyList()

    init {
        setupFragments()
    }

    private fun setupFragments() {
        fragments.clear()
        fragments.addAll(
            listOf(
                AnalyticsOverviewFragment(),      // 0 - Resumen
                RotationPatternsFragment(),       // 1 - Patrones
                PredictionsFragment(),            // 2 - Predicciones
                PerformanceMetricsFragment(),     // 3 - Rendimiento
                WorkloadAnalysisFragment(),       // 4 - Carga de Trabajo
                BottleneckAnalysisFragment(),     // 5 - Cuellos de Botella
                AdvancedReportsFragment()         // 6 - Reportes
            )
        )
    }

    override fun getItemCount(): Int = fragments.size

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> AnalyticsOverviewFragment.newInstance(
                patterns, predictions, performanceMetrics, workloadAnalysis, bottleneckAnalysis
            )
            1 -> RotationPatternsFragment.newInstance(patterns)
            2 -> PredictionsFragment.newInstance(predictions)
            3 -> PerformanceMetricsFragment.newInstance(performanceMetrics)
            4 -> WorkloadAnalysisFragment.newInstance(workloadAnalysis)
            5 -> BottleneckAnalysisFragment.newInstance(bottleneckAnalysis)
            6 -> AdvancedReportsFragment.newInstance(reports)
            else -> fragments[position]
        }
    }

    // Métodos para actualizar datos
    fun updatePatterns(newPatterns: List<RotationPattern>) {
        patterns = newPatterns
        notifyDataSetChanged()
    }

    fun updatePredictions(newPredictions: List<RotationPrediction>) {
        predictions = newPredictions
        notifyDataSetChanged()
    }

    fun updatePerformanceMetrics(newMetrics: List<WorkerPerformanceMetrics>) {
        performanceMetrics = newMetrics
        notifyDataSetChanged()
    }

    fun updateWorkloadAnalysis(newAnalysis: List<WorkloadAnalysis>) {
        workloadAnalysis = newAnalysis
        notifyDataSetChanged()
    }

    fun updateBottleneckAnalysis(newBottlenecks: List<BottleneckAnalysis>) {
        bottleneckAnalysis = newBottlenecks
        notifyDataSetChanged()
    }

    fun updateReports(newReports: List<AdvancedReport>) {
        reports = newReports
        notifyDataSetChanged()
    }
}