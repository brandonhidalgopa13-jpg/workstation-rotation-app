package com.workstation.rotation.analytics.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.workstation.rotation.R
import com.workstation.rotation.analytics.models.PatternType
import com.workstation.rotation.analytics.models.RotationPattern

class PatternsAdapter : RecyclerView.Adapter<PatternsAdapter.PatternViewHolder>() {

    private var patterns: List<RotationPattern> = emptyList()

    fun updatePatterns(newPatterns: List<RotationPattern>) {
        patterns = newPatterns
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PatternViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pattern, parent, false)
        return PatternViewHolder(view)
    }

    override fun onBindViewHolder(holder: PatternViewHolder, position: Int) {
        holder.bind(patterns[position])
    }

    override fun getItemCount(): Int = patterns.size

    class PatternViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardView: CardView = itemView.findViewById(R.id.cardPattern)
        private val typeText: TextView = itemView.findViewById(R.id.tvPatternType)
        private val workstationText: TextView = itemView.findViewById(R.id.tvWorkstation)
        private val workerText: TextView = itemView.findViewById(R.id.tvWorker)
        private val efficiencyText: TextView = itemView.findViewById(R.id.tvEfficiency)
        private val confidenceText: TextView = itemView.findViewById(R.id.tvConfidence)

        fun bind(pattern: RotationPattern) {
            val (typeIcon, typeName, color) = when (pattern.patternType) {
                PatternType.OPTIMAL_SEQUENCE -> Triple("✅", "Secuencia Óptima", "#4CAF50")
                PatternType.BOTTLENECK -> Triple("🚫", "Cuello de Botella", "#F44336")
                PatternType.HIGH_EFFICIENCY -> Triple("⚡", "Alta Eficiencia", "#FF9800")
                PatternType.SKILL_MISMATCH -> Triple("⚠️", "Desajuste de Habilidades", "#FFC107")
                PatternType.FATIGUE_INDICATOR -> Triple("😴", "Indicador de Fatiga", "#9C27B0")
                PatternType.PREFERENCE_PATTERN -> Triple("❤️", "Patrón de Preferencia", "#E91E63")
            }

            typeText.text = "$typeIcon $typeName"
            workstationText.text = "Estación ${pattern.workstationId}"
            workerText.text = if (pattern.workerId > 0) "Trabajador ${pattern.workerId}" else "Todos los trabajadores"
            efficiencyText.text = "${String.format("%.1f", pattern.efficiency * 100)}%"
            confidenceText.text = "${String.format("%.1f", pattern.confidence * 100)}%"

            cardView.setCardBackgroundColor(Color.parseColor(color + "20"))
        }
    }
}