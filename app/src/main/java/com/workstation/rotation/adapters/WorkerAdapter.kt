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
                tvWorkerName.text = worker.name
                tvWorkerEmail.text = if (worker.email.isNotEmpty()) worker.email else "Sin email"
                tvAssignedWorkstations.text = "Estaciones: ${workerWithCount.workstationCount} asignadas"
                switchActive.isChecked = worker.isActive
                
                // Mostrar disponibilidad con color según porcentaje
                tvAvailability.text = "📊 Disponibilidad: ${worker.availabilityPercentage}%"
                tvAvailability.setTextColor(when {
                    worker.availabilityPercentage >= 80 -> android.graphics.Color.parseColor("#FF018786")
                    worker.availabilityPercentage >= 50 -> android.graphics.Color.parseColor("#FFFF9800")
                    else -> android.graphics.Color.parseColor("#FFF44336")
                })
                
                // Mostrar restricciones si existen
                if (worker.restrictionNotes.isNotEmpty()) {
                    tvRestrictionStatus.visibility = android.view.View.VISIBLE
                    tvRestrictionStatus.text = "⚠️ ${worker.restrictionNotes}"
                } else {
                    tvRestrictionStatus.visibility = android.view.View.GONE
                }
                
                // Mostrar estado de entrenador
                if (worker.isTrainer) {
                    tvTrainerStatus.visibility = android.view.View.VISIBLE
                } else {
                    tvTrainerStatus.visibility = android.view.View.GONE
                }
                
                // Mostrar estado de entrenamiento
                if (worker.isTrainee) {
                    tvTraineeStatus.visibility = android.view.View.VISIBLE
                } else {
                    tvTraineeStatus.visibility = android.view.View.GONE
                }
                
                switchActive.setOnCheckedChangeListener { _, isChecked ->
                    onStatusChange(worker, isChecked)
                }
                
                btnEdit.setOnClickListener {
                    onEditClick(worker)
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