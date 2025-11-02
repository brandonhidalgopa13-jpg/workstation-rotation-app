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
import com.workstation.rotation.dashboard.models.KPICard
import com.workstation.rotation.animations.setupMicroInteractions
import com.workstation.rotation.animations.AnimationManager

/**
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 📊 ADAPTADOR DE TARJETAS KPI - DASHBOARD EJECUTIVO - REWS v3.1.0
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 
 * Adaptador especializado para mostrar tarjetas de KPI (Key Performance Indicators)
 * en el Dashboard Ejecutivo con animaciones fluidas y diseño moderno.
 * 
 * 🎯 CARACTERÍSTICAS:
 * • Tarjetas KPI con diseño Material Design
 * • Indicadores visuales de tendencias
 * • Colores dinámicos según el tipo de métrica
 * • Animaciones de entrada y micro-interacciones
 * • Click listeners para navegación a detalles
 * • Actualización eficiente con DiffUtil
 * 
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 */

class KPICardAdapter(
    private val onKPIClick: (KPICard) -> Unit
) : ListAdapter<KPICard, KPICardAdapter.KPIViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KPIViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_kpi_card, parent, false)
        return KPIViewHolder(view)
    }

    override fun onBindViewHolder(holder: KPIViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class KPIViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        
        private val cardContainer: MaterialCardView = itemView.findViewById(R.id.cardKPI)
        private val tvIcon: TextView = itemView.findViewById(R.id.tvKPIIcon)
        private val tvTitle: TextView = itemView.findViewById(R.id.tvKPITitle)
        private val tvValue: TextView = itemView.findViewById(R.id.tvKPIValue)
        private val tvTrend: TextView = itemView.findViewById(R.id.tvKPITrend)
        private val tvTrendIndicator: TextView = itemView.findViewById(R.id.tvTrendIndicator)
        private val viewColorIndicator: View = itemView.findViewById(R.id.viewColorIndicator)

        fun bind(kpi: KPICard) {
            // Configurar contenido
            tvIcon.text = kpi.icon
            tvTitle.text = kpi.title
            tvValue.text = kpi.value
            tvTrend.text = kpi.trend
            tvTrendIndicator.text = kpi.getTrendEmoji()
            
            // Configurar colores
            try {
                val color = Color.parseColor(kpi.color)
                viewColorIndicator.setBackgroundColor(color)
                
                // Color de tendencia
                val trendColor = Color.parseColor(kpi.getTrendColor())
                tvTrend.setTextColor(trendColor)
                tvTrendIndicator.setTextColor(trendColor)
                
            } catch (e: IllegalArgumentException) {
                // Color por defecto si hay error
                viewColorIndicator.setBackgroundColor(Color.parseColor("#1976D2"))
            }
            
            // Configurar micro-interacciones
            cardContainer.setupMicroInteractions()
            
            // Click listener
            cardContainer.setOnClickListener {
                AnimationManager.clickFeedback(cardContainer)
                onKPIClick(kpi)
            }
            
            // Animación de entrada si es necesario
            if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                AnimationManager.animateRecyclerViewItem(
                    view = itemView,
                    animationType = AnimationManager.StaggerType.SCALE_IN,
                    duration = AnimationManager.DURATION_MEDIUM
                )
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<KPICard>() {
        override fun areItemsTheSame(oldItem: KPICard, newItem: KPICard): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: KPICard, newItem: KPICard): Boolean {
            return oldItem == newItem
        }
    }
}