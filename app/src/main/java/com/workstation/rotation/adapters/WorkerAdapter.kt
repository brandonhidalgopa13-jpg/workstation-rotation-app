package com.workstation.rotation.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.workstation.rotation.data.entities.Worker
import com.workstation.rotation.databinding.ItemWorkerBinding

data class WorkerWithWorkstationCount(
    val worker: Worker,
    val workstationCount: Int = 0
)

class WorkerAdapter(
    private val onEditClick: (Worker) -> Unit,
    private val onDeleteClick: (Worker) -> Unit,
    private val onRestrictionsClick: (Worker) -> Unit,
    private val onStatusChange: (Worker, Boolean) -> Unit
) : ListAdapter<WorkerWithWorkstationCount, WorkerAdapter.WorkerViewHolder>(WorkerDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkerViewHolder {
        val binding = ItemWorkerBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return WorkerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WorkerViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class WorkerViewHolder(
        private val binding: ItemWorkerBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(workerWithCount: WorkerWithWorkstationCount) {
            val worker = workerWithCount.worker
            binding.apply {
                bindBasicInfo(worker, workerWithCount.workstationCount)
                bindAvailabilityInfo(worker)
                bindTrainingStatus(worker)
                bindRestrictions(worker)
                setupClickListeners(worker)
            }
        }
        
        /**
         * Binds basic worker information.
         */
        private fun bindBasicInfo(worker: Worker, workstationCount: Int) {
            binding.apply {
                tvWorkerName.text = worker.name
                // Email removido por simplicidad
                tvAssignedWorkstations.text = "Estaciones: $workstationCount asignadas"
                switchActive.isChecked = worker.isActive
            }
        }
        
        /**
         * Binds availability information with appropriate colors.
         */
        private fun bindAvailabilityInfo(worker: Worker) {
            binding.tvAvailability.apply {
                text = "📊 Disponibilidad: ${worker.availabilityPercentage}%"
                setTextColor(getAvailabilityColor(worker.availabilityPercentage))
            }
        }
        
        /**
         * Returns appropriate color based on availability percentage.
         */
        private fun getAvailabilityColor(percentage: Int): Int {
            return when {
                percentage >= 80 -> android.graphics.Color.parseColor("#FF018786")
                percentage >= 50 -> android.graphics.Color.parseColor("#FFFF9800")
                else -> android.graphics.Color.parseColor("#FFF44336")
            }
        }
        
        /**
         * Binds training status badges.
         */
        private fun bindTrainingStatus(worker: Worker) {
            binding.apply {
                tvTrainerStatus.visibility = if (worker.isTrainer) android.view.View.VISIBLE else android.view.View.GONE
                tvTraineeStatus.visibility = if (worker.isTrainee) android.view.View.VISIBLE else android.view.View.GONE
            }
        }
        
        /**
         * Binds restriction information.
         */
        private fun bindRestrictions(worker: Worker) {
            binding.tvRestrictionStatus.apply {
                if (worker.restrictionNotes.isNotEmpty()) {
                    visibility = android.view.View.VISIBLE
                    text = "🚫 Con restricciones específicas"
                } else {
                    visibility = android.view.View.GONE
                }
            }
        }
        
        /**
         * Sets up click listeners for interactive elements.
         */
        private fun setupClickListeners(worker: Worker) {
            binding.apply {
                switchActive.setOnCheckedChangeListener { _, isChecked ->
                    onStatusChange(worker, isChecked)
                }
                
                btnEdit.setOnClickListener {
                    onEditClick(worker)
                }
                
                btnRestrictions.setOnClickListener {
                    onRestrictionsClick(worker)
                }
                
                btnDelete.setOnClickListener {
                    onDeleteClick(worker)
                }
            }
        }
    }

    private class WorkerDiffCallback : DiffUtil.ItemCallback<WorkerWithWorkstationCount>() {
        override fun areItemsTheSame(oldItem: WorkerWithWorkstationCount, newItem: WorkerWithWorkstationCount): Boolean {
            return oldItem.worker.id == newItem.worker.id
        }

        override fun areContentsTheSame(oldItem: WorkerWithWorkstationCount, newItem: WorkerWithWorkstationCount): Boolean {
            return oldItem == newItem
        }
    }
}