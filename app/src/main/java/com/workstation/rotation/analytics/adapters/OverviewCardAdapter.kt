package com.workstation.rotation.analytics.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.workstation.rotation.R
import com.workstation.rotation.analytics.fragments.OverviewCard
import com.workstation.rotation.analytics.models.TrendDirection

/**
 * Adapter para las cards de resumen en Analytics Overview
 */
class OverviewCardAdapter : RecyclerView.Adapter<OverviewCardAdapter.OverviewCardViewHolder>() {

    private var cards: List<OverviewCard> = emptyList()

    fun updateCards(newCards: List<OverviewCard>) {
        cards = newCards
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OverviewCardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_overview_card, parent, false)
        return OverviewCardViewHolder(view)
    }

    override fun onBindViewHolder(holder: OverviewCardViewHolder, position: Int) {
        holder.bind(cards[position])
    }

    override fun getItemCount(): Int = cards.size

    class OverviewCardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardView: CardView = itemView.findViewById(R.id.cardView)
        private val iconText: TextView = itemView.findViewById(R.id.tvIcon)
        private val titleText: TextView = itemView.findViewById(R.id.tvTitle)
        private val valueText: TextView = itemView.findViewById(R.id.tvValue)
        private val subtitleText: TextView = itemView.findViewById(R.id.tvSubtitle)
        private val trendText: TextView = itemView.findViewById(R.id.tvTrend)

        fun bind(card: OverviewCard) {
            iconText.text = card.icon
            titleText.text = card.title
            valueText.text = card.value
            subtitleText.text = card.subtitle

            // Configurar trend
            val trendIcon = when (card.trend) {
                TrendDirection.IMPROVING -> "↗️"
                TrendDirection.DECLINING -> "↘️"
                TrendDirection.STABLE -> "➡️"
            }
            trendText.text = trendIcon

            // Configurar color de la card
            try {
                cardView.setCardBackgroundColor(Color.parseColor(card.color))
                // Ajustar color del texto para contraste
                val textColor = if (isColorDark(card.color)) Color.WHITE else Color.BLACK
                titleText.setTextColor(textColor)
                valueText.setTextColor(textColor)
                subtitleText.setTextColor(textColor)
            } catch (e: Exception) {
                // Color por defecto si hay error
                cardView.setCardBackgroundColor(Color.parseColor("#2196F3"))
            }

            // Animación de entrada
            itemView.alpha = 0f
            itemView.animate()
                .alpha(1f)
                .setDuration(300)
                .setStartDelay((adapterPosition * 100).toLong())
                .start()
        }

        private fun isColorDark(colorString: String): Boolean {
            return try {
                val color = Color.parseColor(colorString)
                val darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255
                darkness >= 0.5
            } catch (e: Exception) {
                false
            }
        }
    }
}