package com.workstation.rotation.analytics.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.workstation.rotation.R
import com.workstation.rotation.analytics.fragments.InsightType
import com.workstation.rotation.analytics.fragments.QuickInsight
import com.workstation.rotation.analytics.models.Priority

/**
 * Adapter para los insights rápidos en Analytics Overview
 */
class QuickInsightsAdapter : RecyclerView.Adapter<QuickInsightsAdapter.QuickInsightViewHolder>() {

    private var insights: List<QuickInsight> = emptyList()

    fun updateInsights(newInsights: List<QuickInsight>) {
        insights = newInsights
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuickInsightViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_quick_insight, parent, false)
        return QuickInsightViewHolder(view)
    }

    override fun onBindViewHolder(holder: QuickInsightViewHolder, position: Int) {
        holder.bind(insights[position])
    }

    override fun getItemCount(): Int = insights.size

    class QuickInsightViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardView: CardView = itemView.findViewById(R.id.cardInsight)
        private val typeIcon: TextView = itemView.findViewById(R.id.tvTypeIcon)
        private val titleText: TextView = itemView.findViewById(R.id.tvInsightTitle)
        private val descriptionText: TextView = itemView.findViewById(R.id.tvInsightDescription)
        private val priorityBadge: TextView = itemView.findViewById(R.id.tvPriorityBadge)
        private val actionIndicator: TextView = itemView.findViewById(R.id.tvActionIndicator)

        fun bind(insight: QuickInsight) {
            titleText.text = insight.title
            descriptionText.text = insight.description

            // Configurar icono según tipo
            val (icon, color) = when (insight.type) {
                InsightType.RECOMMENDATION -> "💡" to "#4CAF50"
                InsightType.WARNING -> "⚠️" to "#FF9800"
                InsightType.INSIGHT -> "🔍" to "#2196F3"
                InsightType.PREDICTION -> "🔮" to "#9C27B0"
            }
            typeIcon.text = icon

            // Configurar prioridad
            val (priorityText, priorityColor) = when (insight.priority) {
                Priority.HIGH -> "ALTA" to "#F44336"
                Priority.MEDIUM -> "MEDIA" to "#FF9800"
                Priority.LOW -> "BAJA" to "#4CAF50"
                Priority.URGENT -> "URGENTE" to "#D32F2F"
            }
            priorityBadge.text = priorityText
            priorityBadge.setBackgroundColor(Color.parseColor(priorityColor))

            // Indicador de acción
            actionIndicator.visibility = if (insight.actionable) View.VISIBLE else View.GONE
            actionIndicator.text = "🎯"

            // Color de borde según tipo
            try {
                cardView.setCardBackgroundColor(Color.parseColor(color + "20")) // 20% opacity
            } catch (e: Exception) {
                cardView.setCardBackgroundColor(Color.parseColor("#F5F5F5"))
            }

            // Animación de entrada
            itemView.alpha = 0f
            itemView.translationY = 50f
            itemView.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(300)
                .setStartDelay((adapterPosition * 100).toLong())
                .start()

            // Click listener para insights accionables
            if (insight.actionable) {
                itemView.setOnClickListener {
                    // TODO: Implementar navegación a detalles o acciones
                }
            }
        }
    }
}