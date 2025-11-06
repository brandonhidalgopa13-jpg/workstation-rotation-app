package com.workstation.rotation.monitoring.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.workstation.rotation.R
import com.workstation.rotation.databinding.ItemAlertBinding
import com.workstation.rotation.monitoring.Alert
import com.workstation.rotation.monitoring.AlertSeverity
import java.text.SimpleDateFormat
import java.util.*

/**
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 🚨 ADAPTER DE ALERTAS - WorkStation Rotation v4.0.2+
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 
 * Adapter para mostrar alertas del sistema en tiempo real.
 * Incluye diferentes niveles de severidad con indicadores visuales.
 * 
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 */

class MonitoringAlertsAdapter(
    private val onAlertClick: (Alert) -> Unit
) : ListAdapter<Alert, MonitoringAlertsAdapter.AlertViewHolder>(AlertDiffCallback()) {
    
    private val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlertViewHolder {
        val binding = ItemAlertBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AlertViewHolder(binding, onAlertClick, dateFormat)
    }
    
    override fun onBindViewHolder(holder: AlertViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    fun updateAlerts(alerts: List<Alert>) {
        submitList(alerts.sortedByDescending { it.timestamp })
    }
    
    class AlertViewHolder(
        private val binding: ItemAlertBinding,
        private val onAlertClick: (Alert) -> Unit,
        private val dateFormat: SimpleDateFormat
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(alert: Alert) {
            binding.apply {
                // Mensaje de la alerta
                tvAlertMessage.text = alert.message
                
                // Timestamp
                tvAlertTime.text = dateFormat.format(Date(alert.timestamp))
                
                // Tipo de alerta
                tvAlertType.text = getAlertTypeText(alert.type.name)
                
                // Valor si está disponible
                if (alert.value != 0.0) {
                    tvAlertValue.text = String.format("%.2f", alert.value)
                    tvAlertValue.visibility = android.view.View.VISIBLE
                } else {
                    tvAlertValue.visibility = android.view.View.GONE
                }
                
                // Configurar colores según severidad
                setupSeverityColors(alert.severity)
                
                // Click listener
                root.setOnClickListener {
                    onAlertClick(alert)
                }
                
                // Animación de entrada
                root.alpha = 0f
                root.animate()
                    .alpha(1f)
                    .setDuration(300)
                    .start()
            }
        }
        
        private fun setupSeverityColors(severity: AlertSeverity) {
            val context = binding.root.context
            
            val (backgroundColor, textColor, iconResource) = when (severity) {
                AlertSeverity.CRITICAL -> Triple(
                    ContextCompat.getColor(context, R.color.status_error),
                    ContextCompat.getColor(context, android.R.color.white),
                    R.drawable.ic_warning
                )
                AlertSeverity.WARNING -> Triple(
                    ContextCompat.getColor(context, R.color.status_warning),
                    ContextCompat.getColor(context, android.R.color.black),
                    R.drawable.ic_warning
                )
                AlertSeverity.INFO -> Triple(
                    ContextCompat.getColor(context, R.color.status_info),
                    ContextCompat.getColor(context, android.R.color.white),
                    R.drawable.ic_info
                )
            }
            
            binding.apply {
                cardAlert.setCardBackgroundColor(backgroundColor)
                tvAlertMessage.setTextColor(textColor)
                tvAlertTime.setTextColor(textColor)
                tvAlertType.setTextColor(textColor)
                tvAlertValue.setTextColor(textColor)
                ivAlertIcon.setImageResource(iconResource)
                ivAlertIcon.setColorFilter(textColor)
            }
        }
        
        private fun getAlertTypeText(type: String): String {
            return when (type) {
                "HIGH_MEMORY_USAGE" -> "Memoria Alta"
                "HIGH_CPU_USAGE" -> "CPU Alto"
                "HIGH_ERROR_RATE" -> "Errores Altos"
                "SLOW_RESPONSE" -> "Respuesta Lenta"
                "DATABASE_SLOW" -> "BD Lenta"
                "CACHE_MISS_HIGH" -> "Cache Bajo"
                else -> type.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
            }
        }
    }
    
    private class AlertDiffCallback : DiffUtil.ItemCallback<Alert>() {
        override fun areItemsTheSame(oldItem: Alert, newItem: Alert): Boolean {
            return oldItem.timestamp == newItem.timestamp && oldItem.type == newItem.type
        }
        
        override fun areContentsTheSame(oldItem: Alert, newItem: Alert): Boolean {
            return oldItem == newItem
        }
    }
}