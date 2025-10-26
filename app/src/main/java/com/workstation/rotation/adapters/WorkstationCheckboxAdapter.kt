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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemWorkstationCheckboxBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemWorkstationCheckboxBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: WorkstationCheckItem) {
            binding.apply {
                checkboxWorkstation.text = item.workstation.name
                checkboxWorkstation.isChecked = item.isChecked
                
                checkboxWorkstation.setOnCheckedChangeListener { _, isChecked ->
                    onCheckChanged(item, isChecked)
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