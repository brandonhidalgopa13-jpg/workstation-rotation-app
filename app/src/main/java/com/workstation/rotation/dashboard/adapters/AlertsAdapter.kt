package com.workstation.rotation.dashboard.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.workstation.rotation.R
import com.workstation.rotation.dashboard.models.AlertItem
import com.workstation.rotation.animations.setupMicroInteractions
import com.workstation.rotation.animations.AnimationManager
import java.text.SimpleDateFormat
import java.util.*

/**
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 🚨 ADAPTADOR DE ALERTAS - DASHBOARD EJECUTIVO - REWS v3.1.0
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 
 * Adaptador para mostrar alertas del sistema con diferentes niveles de severidad,
 * acciones contextuales y diseño visual diferenciado.
 * 
 * 🎯 CARACTERÍSTICAS:
 * • Alertas con niveles de severidad visual
 * • Iconos contextuales según categoría
 * • Timestamps relativos y absolutos
 * • Acciones de resolución y descarte
 * • Colores dinámicos según severidad
 * • Animaciones de entrada y salida
 * • Indicadores de alertas recientes
 * 
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 */

class AlertsAdapter(
    private val onAlertClick: (AlertItem) -> Unit,
    private val onAlertDismiss: (AlertItem) -> Unit
) : ListAdapter<AlertItem, AlertsAdapter.AlertViewHolder>(DiffCallback()) {

    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val dateFormat = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlertViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_alert, parent, false)
        return AlertViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlertViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class AlertViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        
        private val cardContainer: MaterialCardView = itemView.findViewById(R.id.cardAlert)
        private val tvSeverityIcon: TextView = itemView.findViewById(R.id.tvSeverityIcon)
        private val tvCategoryIcon: TextView = itemView.findViewById(R.id.tvCategoryIcon)
        private val tvTitle: TextView = itemView.findViewById(R.id.tvAlertTitle)
        private val tvDescription: TextView = itemView.findViewById(R.id.tvAlertDescription)
        private val tvTimestamp: TextView = itemView.findViewById(R.id.tvAlertTimestamp)
        private val tvSeverityText: TextView = itemView.findViewById(R.id.tvSeverityText)
        private val tvSource: TextView = itemView.findViewById(R.id.tvAlertSource)
        private val btnDismiss: MaterialButton = itemView.findViewById(R.id.btnDismissAlert)
        private val btnAction: MaterialButton = itemView.findViewById(R.id.btnAlertAction)
        private val viewSeverityIndicator: View = itemView.findViewById(R.id.viewSeverityIndicator)
        private val viewRecentIndicator: View = itemView.findViewById(R.id.viewRecentIndicator)

        fun bind(alert: AlertItem) {
            // Configurar contenido básico
            tvTitle.text = alert.title
            tvDescription.text = alert.description
            tvSeverityText.text = alert.getSeverityText()
            tvCategoryIcon.text = alert.getCategoryIcon()
            tvSource.text = alert.source ?: "Sistema"
            
            // Configurar timestamp
            tvTimestamp.text = if (alert.isRecent()) {
                "Hace ${getRelativeTime(alert.timestamp)}"
            } else {
                dateFormat.format(Date(alert.timestamp))
            }
            
            // Configurar severidad visual
            configureSeverityVisuals(alert)
            
            // Configurar indicador de alerta reciente
            viewRecentIndicator.visibility = if (alert.isRecent()) View.VISIBLE else View.GONE
            
            // Configurar botones de acción
            configureActionButtons(alert)
            
            // Configurar micro-interacciones
            cardContainer.setupMicroInteractions()
            
            // Click listeners
            cardContainer.setOnClickListener {
                AnimationManager.clickFeedback(cardContainer)
                onAlertClick(alert)
            }
            
            btnDismiss.setOnClickListener {
                AnimationManager.clickFeedback(btnDismiss)
                dismissAlert(alert)
            }
            
            btnAction.setOnClickListener {
                AnimationManager.clickFeedback(btnAction)
                onAlertClick(alert) // Misma acción que click en card por ahora
            }
            
            // Animación de entrada
            if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                AnimationManager.animateRecyclerViewItem(
                    view = itemView,
                    animationType = AnimationManager.StaggerType.SLIDE_IN_FROM_LEFT,
                    duration = AnimationManager.DURATION_MEDIUM
                )
            }
        }
        
        private fun configureSeverityVisuals(alert: AlertItem) {
            try {
                val severityColor = Color.parseColor(alert.getSeverityColor())
                viewSeverityIndicator.setBackgroundColor(severityColor)
                tvSeverityText.setTextColor(severityColor)
                
                // Icono de severidad
                tvSeverityIcon.text = when (alert.severity) {
                    AlertItem.Severity.LOW -> "ℹ️"
                    AlertItem.Severity.MEDIUM -> "⚠️"
                    AlertItem.Severity.HIGH -> "🚨"
                    AlertItem.Severity.CRITICAL -> "🔥"
                }
                
                // Configurar elevación de card según severidad
                cardContainer.cardElevation = when (alert.severity) {
                    AlertItem.Severity.LOW -> 2f
                    AlertItem.Severity.MEDIUM -> 4f
                    AlertItem.Severity.HIGH -> 6f
                    AlertItem.Severity.CRITICAL -> 8f
                }
                
            } catch (e: IllegalArgumentException) {
                // Color por defecto
                viewSeverityIndicator.setBackgroundColor(Color.parseColor("#FF9800"))
            }
        }
        
        private fun configureActionButtons(alert: AlertItem) {
            // Botón de acción principal
            if (alert.actionRequired) {
                btnAction.visibility = View.VISIBLE
                btnAction.text = when (alert.severity) {
                    AlertItem.Severity.CRITICAL -> "Resolver Ahora"
                    AlertItem.Severity.HIGH -> "Revisar"
                    else -> "Ver Detalles"
                }
            } else {
                btnAction.visibility = View.GONE
            }
            
            // Botón de descartar siempre visible
            btnDismiss.text = "Descartar"
        }
        
        private fun dismissAlert(alert: AlertItem) {
            // Animar salida antes de descartar
            AnimationManager.slideOutToRight(
                view = itemView,
                duration = AnimationManager.DURATION_MEDIUM,
                hideOnComplete = false
            ) {
                onAlertDismiss(alert)
            }
        }
        
        private fun getRelativeTime(timestamp: Long): String {
            val now = System.currentTimeMillis()
            val diff = now - timestamp
            
            return when {
                diff < 60 * 1000 -> "unos segundos"
                diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)} min"
                diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)} h"
                else -> "${diff / (24 * 60 * 60 * 1000)} días"
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<AlertItem>() {
        override fun areItemsTheSame(oldItem: AlertItem, newItem: AlertItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: AlertItem, newItem: AlertItem): Boolean {
            return oldItem == newItem
        }
    }
}