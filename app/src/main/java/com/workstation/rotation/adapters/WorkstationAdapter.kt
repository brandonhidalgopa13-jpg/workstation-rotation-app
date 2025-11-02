package com.workstation.rotation.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.workstation.rotation.data.entities.Workstation
import com.workstation.rotation.databinding.ItemWorkstationBinding

class WorkstationAdapter(
    private val onEditClick: (Workstation) -> Unit,
    private val onDeleteClick: (Workstation) -> Unit,
    private val onStatusChange: (Workstation, Boolean) -> Unit
) : ListAdapter<Workstation, WorkstationAdapter.WorkstationViewHolder>(WorkstationDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkstationViewHolder {
        val binding = ItemWorkstationBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return WorkstationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WorkstationViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class WorkstationViewHolder(
        private val binding: ItemWorkstationBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(workstation: Workstation) {
            binding.apply {
                bindBasicInfo(workstation)
                bindCapacityInfo(workstation)
                bindStatusInfo(workstation)
                setupClickListeners(workstation)
            }
        }
        
        /**
         * Binds basic workstation information.
         */
        private fun bindBasicInfo(workstation: Workstation) {
            binding.apply {
                tvWorkstationName.text = workstation.getDisplayName()
                tvWorkstationDescription.text = if (workstation.description.isNotEmpty()) {
                    workstation.description
                } else {
                    "Sin descripción"
                }
                switchActive.isChecked = workstation.isActive
            }
        }
        
        /**
         * Binds capacity and configuration information.
         */
        private fun bindCapacityInfo(workstation: Workstation) {
            binding.apply {
                tvCapacityInfo.text = "👥 Capacidad: ${workstation.getCapacityInfo()}"
                
                // Show priority status
                tvPriorityStatus.apply {
                    if (workstation.isPriority) {
                        visibility = android.view.View.VISIBLE
                        text = "⭐ ESTACIÓN PRIORITARIA"
                    } else {
                        visibility = android.view.View.GONE
                    }
                }
                
                // Show training status
                tvTrainingStatus.apply {
                    if (workstation.isTrainingStation) {
                        visibility = android.view.View.VISIBLE
                        text = "🎓 ESTACIÓN DE ENTRENAMIENTO"
                    } else {
                        visibility = android.view.View.GONE
                    }
                }
            }
        }
        
        /**
         * Binds status and maintenance information.
         */
        private fun bindStatusInfo(workstation: Workstation) {
            binding.apply {
                tvStatusInfo.text = "📊 Estado: ${workstation.getStatusDescription()}"
                
                // Set status color based on workstation state
                val statusColor = when {
                    !workstation.isActive -> android.graphics.Color.parseColor("#FFF44336") // Red
                    workstation.needsMaintenance() -> android.graphics.Color.parseColor("#FFFF9800") // Orange
                    workstation.isPriority -> android.graphics.Color.parseColor("#FF4CAF50") // Green
                    else -> android.graphics.Color.parseColor("#FF2196F3") // Blue
                }
                tvStatusInfo.setTextColor(statusColor)
                
                // Show utilization rate if available
                if (workstation.utilizationRate > 0) {
                    tvUtilizationInfo.apply {
                        visibility = android.view.View.VISIBLE
                        text = "📈 Utilización: ${workstation.utilizationRate.toInt()}%"
                    }
                } else {
                    tvUtilizationInfo.visibility = android.view.View.GONE
                }
            }
        }
        
        /**
         * Sets up click listeners for interactive elements.
         */
        private fun setupClickListeners(workstation: Workstation) {
            binding.apply {
                switchActive.setOnCheckedChangeListener { _, isChecked ->
                    onStatusChange(workstation, isChecked)
                }
                
                btnEdit.setOnClickListener {
                    onEditClick(workstation)
                }
                
                btnDelete.setOnClickListener {
                    onDeleteClick(workstation)
                }
            }
        }
    }

    private class WorkstationDiffCallback : DiffUtil.ItemCallback<Workstation>() {
        override fun areItemsTheSame(oldItem: Workstation, newItem: Workstation): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Workstation, newItem: Workstation): Boolean {
            return oldItem == newItem
        }
    }
}