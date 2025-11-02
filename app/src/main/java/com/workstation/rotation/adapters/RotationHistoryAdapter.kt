package com.workstation.rotation.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.workstation.rotation.R
import com.workstation.rotation.data.entities.RotationHistory
import java.text.SimpleDateFormat
import java.util.*

/**
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 📋 ADAPTADOR HISTORIAL DE ROTACIONES - VISUALIZACIÓN OPTIMIZADA
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 
 * 🎯 CARACTERÍSTICAS:
 * • ListAdapter con DiffUtil para actualizaciones eficientes
 * • Diseño moderno con Material Design
 * • Indicadores visuales de estado (activa/completada)
 * • Acciones contextuales para rotaciones activas
 * • Formateo inteligente de fechas y duraciones
 * 
 * 🔄 FUNCIONALIDADES:
 * • Click listeners para acciones específicas
 * • Colores dinámicos según tipo de rotación
 * • Mostrar/ocultar elementos según estado
 * • Formateo automático de métricas
 * 
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 */

class RotationHistoryAdapter(
    private val onItemClick: (RotationHistory) -> Unit = {},
    private val onFinishRotation: (RotationHistory) -> Unit = {},
    private val onAddScore: (RotationHistory) -> Unit = {}
) : ListAdapter<RotationHistoryItem, RotationHistoryAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_rotation_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        
        private val statusIndicator: View = itemView.findViewById(R.id.statusIndicator)
        private val tvWorkerName: TextView = itemView.findViewById(R.id.tvWorkerName)
        private val tvWorkstationName: TextView = itemView.findViewById(R.id.tvWorkstationName)
        private val chipRotationType: Chip = itemView.findViewById(R.id.chipRotationType)
        private val tvStartTime: TextView = itemView.findViewById(R.id.tvStartTime)
        private val tvStartDate: TextView = itemView.findViewById(R.id.tvStartDate)
        private val tvDuration: TextView = itemView.findViewById(R.id.tvDuration)
        private val tvPerformanceScore: TextView = itemView.findViewById(R.id.tvPerformanceScore)
        private val tvNotes: TextView = itemView.findViewById(R.id.tvNotes)
        private val layoutActions: View = itemView.findViewById(R.id.layoutActions)
        private val btnFinishRotation: MaterialButton = itemView.findViewById(R.id.btnFinishRotation)
        private val btnAddScore: MaterialButton = itemView.findViewById(R.id.btnAddScore)

        private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        fun bind(item: RotationHistoryItem) {
            val rotation = item.rotationHistory
            
            // Información básica
            tvWorkerName.text = item.workerName ?: "Trabajador #${rotation.worker_id}"
            tvWorkstationName.text = item.workstationName ?: "Estación #${rotation.workstation_id}"
            
            // Tipo de rotación
            chipRotationType.text = rotation.rotation_type
            chipRotationType.setChipBackgroundColorResource(getRotationTypeColor(rotation.rotation_type))
            
            // Fechas y tiempos
            val startDate = Date(rotation.rotation_date)
            tvStartTime.text = timeFormat.format(startDate)
            tvStartDate.text = dateFormat.format(startDate)
            
            // Estado y duración
            if (rotation.isActive()) {
                // Rotación activa
                statusIndicator.backgroundTintList = ContextCompat.getColorStateList(
                    itemView.context, R.color.accent
                )
                tvDuration.text = "En curso"
                tvDuration.setTextColor(ContextCompat.getColor(itemView.context, R.color.accent))
                layoutActions.visibility = View.VISIBLE
                
                // Calcular duración actual
                val currentDuration = RotationHistory.calculateDuration(
                    rotation.rotation_date, 
                    System.currentTimeMillis()
                )
                tvDuration.text = "${currentDuration}min (activa)"
                
            } else {
                // Rotación completada
                statusIndicator.backgroundTintList = ContextCompat.getColorStateList(
                    itemView.context, R.color.success
                )
                
                val duration = rotation.getCalculatedDuration() ?: rotation.duration_minutes
                tvDuration.text = if (duration != null) "${duration}min" else "N/A"
                tvDuration.setTextColor(ContextCompat.getColor(itemView.context, R.color.text_primary))
                layoutActions.visibility = View.GONE
            }
            
            // Performance Score
            val score = rotation.performance_score
            if (score != null) {
                tvPerformanceScore.text = String.format("%.1f", score)
                tvPerformanceScore.setTextColor(getScoreColor(score))
            } else {
                tvPerformanceScore.text = "N/A"
                tvPerformanceScore.setTextColor(ContextCompat.getColor(itemView.context, R.color.text_secondary))
            }
            
            // Notas
            if (!rotation.notes.isNullOrBlank()) {
                tvNotes.text = rotation.notes
                tvNotes.visibility = View.VISIBLE
            } else {
                tvNotes.visibility = View.GONE
            }
            
            // Click listeners
            itemView.setOnClickListener { onItemClick(rotation) }
            btnFinishRotation.setOnClickListener { onFinishRotation(rotation) }
            btnAddScore.setOnClickListener { onAddScore(rotation) }
        }
        
        private fun getRotationTypeColor(type: String): Int {
            return when (type) {
                RotationHistory.TYPE_MANUAL -> R.color.primary_light
                RotationHistory.TYPE_AUTOMATIC -> R.color.accent_light
                RotationHistory.TYPE_EMERGENCY -> R.color.error_light
                RotationHistory.TYPE_SCHEDULED -> R.color.success_light
                else -> R.color.text_secondary
            }
        }
        
        private fun getScoreColor(score: Double): Int {
            return when {
                score >= 8.0 -> ContextCompat.getColor(itemView.context, R.color.success)
                score >= 6.0 -> ContextCompat.getColor(itemView.context, R.color.warning)
                else -> ContextCompat.getColor(itemView.context, R.color.error)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<RotationHistoryItem>() {
        override fun areItemsTheSame(oldItem: RotationHistoryItem, newItem: RotationHistoryItem): Boolean {
            return oldItem.rotationHistory.id == newItem.rotationHistory.id
        }

        override fun areContentsTheSame(oldItem: RotationHistoryItem, newItem: RotationHistoryItem): Boolean {
            return oldItem == newItem
        }
    }
}

/**
 * Data class que combina RotationHistory con nombres legibles
 */
data class RotationHistoryItem(
    val rotationHistory: RotationHistory,
    val workerName: String? = null,
    val workstationName: String? = null
)