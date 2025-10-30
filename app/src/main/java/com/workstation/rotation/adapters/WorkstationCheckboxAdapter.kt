package com.workstation.rotation.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.workstation.rotation.data.entities.Workstation
import com.workstation.rotation.databinding.ItemWorkstationCheckboxBinding

data class WorkstationCheckItem(
    val workstation: Workstation,
    var isChecked: Boolean = false
)

class WorkstationCheckboxAdapter(
    private val onCheckChanged: (WorkstationCheckItem, Boolean) -> Unit
) : ListAdapter<WorkstationCheckItem, WorkstationCheckboxAdapter.ViewHolder>(DiffCallback()) {
    
    override fun submitList(list: List<WorkstationCheckItem>?) {
        android.util.Log.d("WorkstationCheckboxAdapter", "submitList called with ${list?.size ?: 0} items")
        list?.forEachIndexed { index, item ->
            android.util.Log.d("WorkstationCheckboxAdapter", "Item $index: ${item.workstation.name}, checked: ${item.isChecked}")
        }
        super.submitList(list)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        android.util.Log.d("WorkstationCheckboxAdapter", "Creating ViewHolder")
        val binding = ItemWorkstationCheckboxBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        android.util.Log.d("WorkstationCheckboxAdapter", "Binding item at position $position: ${item.workstation.name}, checked: ${item.isChecked}")
        holder.bind(item)
    }

    inner class ViewHolder(
        private val binding: ItemWorkstationCheckboxBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: WorkstationCheckItem) {
            binding.apply {
                val workstation = item.workstation
                val displayText = buildString {
                    append(workstation.name)
                    append(" (${workstation.requiredWorkers} trabajadores)")
                    if (workstation.isPriority) {
                        append(" ⭐")
                    }
                }
                
                checkboxWorkstation.text = displayText
                checkboxWorkstation.isChecked = item.isChecked
                
                // Cambiar el color de fondo de la tarjeta según el estado
                val cardView = binding.root as com.google.android.material.card.MaterialCardView
                if (item.isChecked) {
                    cardView.setCardBackgroundColor(
                        binding.root.context.getColor(android.R.color.holo_blue_light)
                    )
                    cardView.alpha = 0.9f
                } else {
                    cardView.setCardBackgroundColor(
                        binding.root.context.getColor(com.workstation.rotation.R.color.background_card)
                    )
                    cardView.alpha = 1.0f
                }
                
                checkboxWorkstation.setOnCheckedChangeListener { _, isChecked ->
                    onCheckChanged(item, isChecked)
                }
                
                // Hacer que toda la tarjeta sea clickeable
                binding.root.setOnClickListener {
                    checkboxWorkstation.isChecked = !checkboxWorkstation.isChecked
                }
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<WorkstationCheckItem>() {
        override fun areItemsTheSame(oldItem: WorkstationCheckItem, newItem: WorkstationCheckItem): Boolean {
            return oldItem.workstation.id == newItem.workstation.id
        }

        override fun areContentsTheSame(oldItem: WorkstationCheckItem, newItem: WorkstationCheckItem): Boolean {
            return oldItem == newItem
        }
    }
}