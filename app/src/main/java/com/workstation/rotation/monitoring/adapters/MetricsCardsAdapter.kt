package com.workstation.rotation.monitoring.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.workstation.rotation.R
import com.workstation.rotation.databinding.ItemMetricCardBinding
import com.workstation.rotation.monitoring.viewmodels.MetricCard
import com.workstation.rotation.monitoring.viewmodels.MetricItem
import com.workstation.rotation.monitoring.viewmodels.MetricStatus

/**
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 📊 ADAPTER CARDS DE MÉTRICAS - WorkStation Rotation v4.0.2+
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 
 * Adapter para mostrar cards de métricas en el dashboard en tiempo real.
 * Cada card contiene múltiples métricas relacionadas con indicadores visuales.
 * 
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 */

class MetricsCardsAdapter : ListAdapter<MetricCard, MetricsCardsAdapter.MetricCardViewHolder>(MetricCardDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MetricCardViewHolder {
        val binding = ItemMetricCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MetricCardViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: MetricCardViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    fun updateCards(cards: List<MetricCard>) {
        submitList(cards)
    }
    
    class MetricCardViewHolder(
        private val binding: ItemMetricCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(card: MetricCard) {
            binding.apply {
                // Título de la card
                tvCardTitle.text = card.title
                
                // Icono según el tipo
                ivCardIcon.setImageResource(getIconResource(card.icon))
                
                // Limpiar métricas anteriores
                containerMetrics.removeAllViews()
                
                // Agregar métricas dinámicamente
                card.metrics.forEach { metric ->
                    addMetricView(metric)
                }
            }
        }
        
        private fun addMetricView(metric: MetricItem) {
            val context = binding.root.context
            val inflater = LayoutInflater.from(context)
            
            // Inflar layout para métrica individual
            val metricBinding = com.workstation.rotation.databinding.ItemMetricBinding.inflate(
                inflater,
                binding.containerMetrics,
                false
            )
            
            metricBinding.apply {
                tvMetricLabel.text = metric.label
                tvMetricValue.text = metric.value
                
                // Aplicar color según estado
                val color = when (metric.status) {
                    MetricStatus.NORMAL -> ContextCompat.getColor(context, R.color.status_success)
                    MetricStatus.WARNING -> ContextCompat.getColor(context, R.color.status_warning)
                    MetricStatus.CRITICAL -> ContextCompat.getColor(context, R.color.status_error)
                }
                
                tvMetricValue.setTextColor(color)
                indicatorStatus.setBackgroundColor(color)
                
                // Agregar al contenedor
                binding.containerMetrics.addView(root)
            }
        }
        
        private fun getIconResource(iconName: String): Int {
            return when (iconName) {
                "system" -> R.drawable.ic_settings
                "rotation" -> R.drawable.ic_rotation
                "performance" -> R.drawable.ic_auto_awesome
                "database" -> R.drawable.ic_storage
                else -> R.drawable.ic_info
            }
        }
    }
    
    private class MetricCardDiffCallback : DiffUtil.ItemCallback<MetricCard>() {
        override fun areItemsTheSame(oldItem: MetricCard, newItem: MetricCard): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: MetricCard, newItem: MetricCard): Boolean {
            return oldItem == newItem
        }
    }
}