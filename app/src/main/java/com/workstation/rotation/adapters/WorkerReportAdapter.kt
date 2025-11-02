package com.workstation.rotation.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.workstation.rotation.R
import com.workstation.rotation.databinding.ItemWorkerReportBinding
import com.workstation.rotation.services.WorkerSummary

/**
 * Adaptador para mostrar resúmenes de trabajadores en reportes.
 */
class WorkerReportAdapter(
    private val onWorkerClick: (WorkerSummary) -> Unit
) : ListAdapter<WorkerSummary, WorkerReportAdapter.WorkerReportViewHolder>(WorkerReportDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkerReportViewHolder {
        val binding = ItemWorkerReportBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return WorkerReportViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: WorkerReportViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class WorkerReportViewHolder(
        private val binding: ItemWorkerReportBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(workerSummary: WorkerSummary) {
            binding.apply {
                // Información básica
                tvWorkerName.text = workerSummary.name
                tvWorkerEmail.text = workerSummary.email
                tvWorkerType.text = workerSummary.workerType
                
                // Métricas
                tvAvailability.text = "${workerSummary.availabilityPercentage}%"
                tvStationsCount.text = workerSummary.totalStationsAssigned.toString()
                tvRotationsCount.text = workerSummary.currentRotations.toString()
                tvUtilizationScore.text = String.format("%.1f", workerSummary.utilizationScore)
                
                // Indicadores visuales
                setupAvailabilityIndicator(workerSummary.availabilityPercentage)
                setupUtilizationIndicator(workerSummary.utilizationScore)
                setupTypeIndicator(workerSummary.workerType)
                setupRestrictionIndicator(workerSummary.hasRestrictions)
                
                // Click listener
                root.setOnClickListener {
                    onWorkerClick(workerSummary)
                }
            }
        }
        
        private fun setupAvailabilityIndicator(availability: Int) {
            val color = when {
                availability >= 90 -> R.color.status_success
                availability >= 70 -> R.color.status_warning
                else -> R.color.status_error
            }
            
            binding.indicatorAvailability.setBackgroundColor(
                ContextCompat.getColor(binding.root.context, color)
            )
        }
        
        private fun setupUtilizationIndicator(score: Double) {
            val color = when {
                score >= 80 -> R.color.status_success
                score >= 60 -> R.color.status_warning
                else -> R.color.status_error
            }
            
            binding.indicatorUtilization.setBackgroundColor(
                ContextCompat.getColor(binding.root.context, color)
            )
        }
        
        private fun setupTypeIndicator(type: String) {
            val (icon, color) = when {
                type.contains("LÍDER") -> "👑" to R.color.leader_color
                type.contains("ENTRENADOR") -> "🎓" to R.color.trainer_color
                type.contains("ENTRENADO") -> "📚" to R.color.trainee_color
                else -> "👤" to R.color.regular_worker_color
            }
            
            binding.tvWorkerTypeIcon.text = icon
            binding.tvWorkerType.setTextColor(
                ContextCompat.getColor(binding.root.context, color)
            )
        }
        
        private fun setupRestrictionIndicator(hasRestrictions: Boolean) {
            if (hasRestrictions) {
                binding.iconRestriction.text = "⚠️"
                binding.iconRestriction.setTextColor(
                    ContextCompat.getColor(binding.root.context, R.color.status_warning)
                )
            } else {
                binding.iconRestriction.text = "✅"
                binding.iconRestriction.setTextColor(
                    ContextCompat.getColor(binding.root.context, R.color.status_success)
                )
            }
        }
    }
}

class WorkerReportDiffCallback : DiffUtil.ItemCallback<WorkerSummary>() {
    override fun areItemsTheSame(oldItem: WorkerSummary, newItem: WorkerSummary): Boolean {
        return oldItem.id == newItem.id
    }
    
    override fun areContentsTheSame(oldItem: WorkerSummary, newItem: WorkerSummary): Boolean {
        return oldItem == newItem
    }
}