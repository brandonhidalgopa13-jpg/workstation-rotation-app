package com.workstation.rotation.dashboard.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.workstation.rotation.R
import com.workstation.rotation.dashboard.models.TrendData
import com.workstation.rotation.animations.setupMicroInteractions
import com.workstation.rotation.animations.AnimationManager

/**
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 📈 ADAPTADOR DE GRÁFICOS DE TENDENCIAS - DASHBOARD EJECUTIVO - REWS v3.1.0
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 
 * Adaptador para mostrar gráficos de tendencias en formato horizontal
 * con visualización simplificada de datos y navegación a detalles.
 * 
 * 🎯 CARACTERÍSTICAS:
 * • Visualización simplificada de tendencias
 * • Indicadores de cambio porcentual
 * • Colores dinámicos según el tipo de dato
 * • Animaciones de entrada suaves
 * • Click para navegación a vista detallada
 * • Soporte para diferentes tipos de gráfico
 * 
 * NOTA: Esta implementación usa una visualización simplificada.
 * Para gráficos completos, se recomienda integrar una librería
 * como MPAndroidChart o similar.
 * 
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 */

class TrendChartAdapter(
    private val onTrendClick: (TrendData) -> Unit
) : ListAdapter<TrendData, TrendChartAdapter.TrendViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrendViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_trend_chart, parent, false)
        return TrendViewHolder(view)
    }

    override fun onBindViewHolder(holder: TrendViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TrendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        
        private val cardContainer: MaterialCardView = itemView.findViewById(R.id.cardTrend)
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTrendTitle)
        private val tvPeriod: TextView = itemView.findViewById(R.id.tvTrendPeriod)
        private val tvCurrentValue: TextView = itemView.findViewById(R.id.tvCurrentValue)
        private val tvChangePercent: TextView = itemView.findViewById(R.id.tvChangePercent)
        private val tvChartType: TextView = itemView.findViewById(R.id.tvChartType)
        private val viewTrendIndicator: View = itemView.findViewById(R.id.viewTrendIndicator)
        private val viewSimpleChart: View = itemView.findViewById(R.id.viewSimpleChart)

        fun bind(trend: TrendData) {
            // Configurar contenido básico
            tvTitle.text = trend.title
            tvPeriod.text = trend.period
            
            // Valor actual (último punto de datos)
            val currentValue = trend.dataPoints.lastOrNull() ?: 0.0
            tvCurrentValue.text = "${String.format("%.1f", currentValue)}${trend.unit}"
            
            // Cambio porcentual
            val changePercent = trend.getPercentageChange()
            tvChangePercent.text = "${if (changePercent >= 0) "+" else ""}${String.format("%.1f", changePercent)}%"
            
            // Color del cambio porcentual
            val changeColor = if (changePercent >= 0) Color.parseColor("#4CAF50") else Color.parseColor("#F44336")
            tvChangePercent.setTextColor(changeColor)
            
            // Tipo de gráfico
            tvChartType.text = when (trend.chartType) {
                TrendData.ChartType.LINE -> "📈"
                TrendData.ChartType.BAR -> "📊"
                TrendData.ChartType.AREA -> "📉"
                TrendData.ChartType.PIE -> "🥧"
                TrendData.ChartType.DONUT -> "🍩"
            }
            
            // Configurar colores
            try {
                val color = Color.parseColor(trend.color)
                viewTrendIndicator.setBackgroundColor(color)
                
                // Simular gráfico simple con color de fondo
                viewSimpleChart.setBackgroundColor(Color.parseColor(trend.color + "20")) // 20% opacity
                
            } catch (e: IllegalArgumentException) {
                // Color por defecto
                viewTrendIndicator.setBackgroundColor(Color.parseColor("#1976D2"))
                viewSimpleChart.setBackgroundColor(Color.parseColor("#1976D220"))
            }
            
            // Configurar micro-interacciones
            cardContainer.setupMicroInteractions()
            
            // Click listener
            cardContainer.setOnClickListener {
                AnimationManager.clickFeedback(cardContainer)
                onTrendClick(trend)
            }
            
            // Animación de entrada
            if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                AnimationManager.animateRecyclerViewItem(
                    view = itemView,
                    animationType = AnimationManager.StaggerType.SLIDE_IN_FROM_LEFT,
                    duration = AnimationManager.DURATION_MEDIUM
                )
            }
            
            // Simular animación de gráfico
            simulateChartAnimation()
        }
        
        private fun simulateChartAnimation() {
            // Animación simple para simular un gráfico cargando
            viewSimpleChart.alpha = 0f
            viewSimpleChart.animate()
                .alpha(1f)
                .setDuration(AnimationManager.DURATION_LONG)
                .setStartDelay(200L)
                .setInterpolator(AnimationManager.FAST_OUT_SLOW_IN)
                .start()
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<TrendData>() {
        override fun areItemsTheSame(oldItem: TrendData, newItem: TrendData): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: TrendData, newItem: TrendData): Boolean {
            return oldItem == newItem
        }
    }
}