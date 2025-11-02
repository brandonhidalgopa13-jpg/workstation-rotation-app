package com.workstation.rotation.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.workstation.rotation.R
import com.workstation.rotation.models.BenchmarkResult

/**
 * Adapter para mostrar resultados de benchmark en RecyclerView.
 */
class BenchmarkResultAdapter(
    private val results: List<BenchmarkResult>
) : RecyclerView.Adapter<BenchmarkResultAdapter.BenchmarkViewHolder>() {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BenchmarkViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_benchmark_result, parent, false)
        return BenchmarkViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: BenchmarkViewHolder, position: Int) {
        holder.bind(results[position])
    }
    
    override fun getItemCount(): Int = results.size
    
    class BenchmarkViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvAlgorithmName: TextView = itemView.findViewById(R.id.tvAlgorithmName)
        private val tvExecutionTime: TextView = itemView.findViewById(R.id.tvExecutionTime)
        private val tvMemoryUsage: TextView = itemView.findViewById(R.id.tvMemoryUsage)
        private val tvAssignments: TextView = itemView.findViewById(R.id.tvAssignments)
        private val tvSuccessRate: TextView = itemView.findViewById(R.id.tvSuccessRate)
        private val tvTimestamp: TextView = itemView.findViewById(R.id.tvTimestamp)
        
        fun bind(result: BenchmarkResult) {
            tvAlgorithmName.text = result.algorithmName
            tvExecutionTime.text = "⏱️ ${result.getFormattedExecutionTime()}"
            tvMemoryUsage.text = "💾 ${result.getFormattedMemoryUsage()}"
            tvAssignments.text = "📋 ${result.assignmentsCount} asignaciones"
            tvSuccessRate.text = "✅ ${result.getFormattedSuccessRate()}"
            tvTimestamp.text = "🕐 ${result.getFormattedTimestamp()}"
            
            // Colorear según el tipo de algoritmo
            val backgroundColor = when {
                result.algorithmName.contains("SQL") -> R.color.sql_algorithm_bg
                result.algorithmName.contains("Original") -> R.color.original_algorithm_bg
                result.algorithmName.contains("Promedio") -> R.color.average_result_bg
                else -> R.color.default_result_bg
            }
            
            itemView.setBackgroundColor(
                ContextCompat.getColor(itemView.context, backgroundColor)
            )
            
            // Resaltar el mejor resultado si hay comparación
            if (result.algorithmName.contains("SQL") && result.executionTimeMs > 0) {
                tvExecutionTime.setTextColor(
                    ContextCompat.getColor(itemView.context, R.color.status_success)
                )
            }
        }
    }
}