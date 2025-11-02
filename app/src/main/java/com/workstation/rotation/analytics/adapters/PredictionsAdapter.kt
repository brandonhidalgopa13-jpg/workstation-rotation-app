package com.workstation.rotation.analytics.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.workstation.rotation.R
import com.workstation.rotation.analytics.models.RotationPrediction
import com.workstation.rotation.analytics.models.RiskSeverity
import java.text.SimpleDateFormat
import java.util.*

/**
 * Adapter para mostrar predicciones de rotación
 */
class PredictionsAdapter : RecyclerView.Adapter<PredictionsAdapter.PredictionViewHolder>() {

    private var predictions: List<RotationPrediction> = emptyList()
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    fun updatePredictions(newPredictions: List<RotationPrediction>) {
        predictions = newPredictions.sortedBy { it.targetDate }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PredictionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_prediction, parent, false)
        return PredictionViewHolder(view)
    }

    override fun onBindViewHolder(holder: PredictionViewHolder, position: Int) {
        holder.bind(predictions[position])
    }

    override fun getItemCount(): Int = predictions.size

    class PredictionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardView: CardView = itemView.findViewById(R.id.cardPrediction)
        private val dateText: TextView = itemView.findViewById(R.id.tvPredictionDate)
        private val workstationText: TextView = itemView.findViewById(R.id.tvWorkstation)
        private val workerText: TextView = itemView.findViewById(R.id.tvPredictedWorker)
        private val confidenceText: TextView = itemView.findViewById(R.id.tvConfidence)
        private val durationText: TextView = itemView.findViewById(R.id.tvExpectedDuration)
        private val riskIndicator: TextView = itemView.findViewById(R.id.tvRiskIndicator)
        private val recommendationsText: TextView = itemView.findViewById(R.id.tvRecommendations)

        fun bind(prediction: RotationPrediction) {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            
            dateText.text = dateFormat.format(prediction.targetDate)
            workstationText.text = "Estación ${prediction.workstationId}"
            workerText.text = "Trabajador ${prediction.predictedWorkerId}"
            confidenceText.text = "${String.format("%.1f", prediction.confidence * 100)}%"
            
            // Duración esperada
            val hours = prediction.expectedDuration / 60
            val minutes = prediction.expectedDuration % 60
            durationText.text = "${hours}h ${minutes}m"

            // Configurar indicador de riesgo
            val maxRiskSeverity = prediction.riskFactors.maxByOrNull { it.severity.ordinal }?.severity
            val (riskText, riskColor) = when (maxRiskSeverity) {
                RiskSeverity.CRITICAL -> "🔴 CRÍTICO" to "#F44336"
                RiskSeverity.HIGH -> "🟠 ALTO" to "#FF9800"
                RiskSeverity.MEDIUM -> "🟡 MEDIO" to "#FFC107"
                RiskSeverity.LOW -> "🟢 BAJO" to "#4CAF50"
                null -> "✅ SIN RIESGO" to "#4CAF50"
            }
            riskIndicator.text = riskText
            riskIndicator.setTextColor(Color.parseColor(riskColor))

            // Mostrar recomendaciones (primera recomendación)
            recommendationsText.text = if (prediction.recommendations.isNotEmpty()) {
                prediction.recommendations.first()
            } else {
                "Sin recomendaciones específicas"
            }

            // Color de la card según confianza
            val cardColor = when {
                prediction.confidence >= 0.8 -> "#E8F5E8" // Verde claro
                prediction.confidence >= 0.6 -> "#FFF3E0" // Naranja claro
                else -> "#FFEBEE" // Rojo claro
            }
            cardView.setCardBackgroundColor(Color.parseColor(cardColor))

            // Animación de entrada
            itemView.alpha = 0f
            itemView.translationX = 100f
            itemView.animate()
                .alpha(1f)
                .translationX(0f)
                .setDuration(300)
                .setStartDelay((adapterPosition * 50).toLong())
                .start()
        }
    }
}