package com.workstation.rotation.analytics.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.workstation.rotation.R
import com.workstation.rotation.analytics.adapters.OverviewCardAdapter
import com.workstation.rotation.analytics.adapters.QuickInsightsAdapter
import com.workstation.rotation.analytics.models.*

/**
 * Fragment de resumen general de Analytics
 * Muestra métricas clave y insights rápidos
 */
class AnalyticsOverviewFragment : Fragment() {

    private lateinit var overviewCardsRecycler: RecyclerView
    private lateinit var quickInsightsRecycler: RecyclerView
    
    private lateinit var overviewAdapter: OverviewCardAdapter
    private lateinit var insightsAdapter: QuickInsightsAdapter

    companion object {
        fun newInstance(
            patterns: List<RotationPattern>,
            predictions: List<RotationPrediction>,
            performanceMetrics: List<WorkerPerformanceMetrics>,
            workloadAnalysis: List<WorkloadAnalysis>,
            bottleneckAnalysis: List<BottleneckAnalysis>
        ): AnalyticsOverviewFragment {
            val fragment = AnalyticsOverviewFragment()
            val args = Bundle().apply {
                // Pasar datos como argumentos si es necesario
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_analytics_overview, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerViews(view)
        loadOverviewData()
    }

    private fun setupRecyclerViews(view: View) {
        // Cards de resumen
        overviewCardsRecycler = view.findViewById(R.id.recyclerOverviewCards)
        overviewCardsRecycler.layoutManager = GridLayoutManager(context, 2)
        overviewAdapter = OverviewCardAdapter()
        overviewCardsRecycler.adapter = overviewAdapter

        // Insights rápidos
        quickInsightsRecycler = view.findViewById(R.id.recyclerQuickInsights)
        quickInsightsRecycler.layoutManager = LinearLayoutManager(context)
        insightsAdapter = QuickInsightsAdapter()
        quickInsightsRecycler.adapter = insightsAdapter
    }

    private fun loadOverviewData() {
        // Generar cards de resumen
        val overviewCards = generateOverviewCards()
        overviewAdapter.updateCards(overviewCards)

        // Generar insights rápidos
        val quickInsights = generateQuickInsights()
        insightsAdapter.updateInsights(quickInsights)
    }

    private fun generateOverviewCards(): List<OverviewCard> {
        return listOf(
            OverviewCard(
                title = "Patrones Detectados",
                value = "12",
                subtitle = "Últimos 30 días",
                icon = "🔍",
                trend = TrendDirection.IMPROVING,
                color = "#4CAF50"
            ),
            OverviewCard(
                title = "Predicciones Activas",
                value = "7",
                subtitle = "Próximos 7 días",
                icon = "🔮",
                trend = TrendDirection.STABLE,
                color = "#2196F3"
            ),
            OverviewCard(
                title = "Rendimiento Promedio",
                value = "8.2",
                subtitle = "Escala 1-10",
                icon = "⚡",
                trend = TrendDirection.IMPROVING,
                color = "#FF9800"
            ),
            OverviewCard(
                title = "Cuellos de Botella",
                value = "3",
                subtitle = "Requieren atención",
                icon = "🚫",
                trend = TrendDirection.DECLINING,
                color = "#F44336"
            )
        )
    }

    private fun generateQuickInsights(): List<QuickInsight> {
        return listOf(
            QuickInsight(
                type = InsightType.RECOMMENDATION,
                title = "Optimización Detectada",
                description = "La estación 3 muestra un patrón de alta eficiencia con el trabajador 5",
                priority = Priority.HIGH,
                actionable = true
            ),
            QuickInsight(
                type = InsightType.WARNING,
                title = "Riesgo de Cuello de Botella",
                description = "La estación 7 presenta tiempos de rotación 30% superiores al promedio",
                priority = Priority.MEDIUM,
                actionable = true
            ),
            QuickInsight(
                type = InsightType.INSIGHT,
                title = "Patrón de Mejora",
                description = "El rendimiento general ha mejorado 15% en las últimas 2 semanas",
                priority = Priority.LOW,
                actionable = false
            ),
            QuickInsight(
                type = InsightType.PREDICTION,
                title = "Predicción de Carga",
                description = "Se espera un aumento del 20% en la carga de trabajo la próxima semana",
                priority = Priority.MEDIUM,
                actionable = true
            )
        )
    }
}

// Modelos para el overview
data class OverviewCard(
    val title: String,
    val value: String,
    val subtitle: String,
    val icon: String,
    val trend: TrendDirection,
    val color: String
)

data class QuickInsight(
    val type: InsightType,
    val title: String,
    val description: String,
    val priority: Priority,
    val actionable: Boolean
)

enum class InsightType {
    RECOMMENDATION,
    WARNING,
    INSIGHT,
    PREDICTION
}