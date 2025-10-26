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
                tvWorkstationName.text = workstation.name
                tvWorkstationDescription.text = workstation.description
                switchActive.isChecked = workstation.isActive
                
                switchActive.setOnCheckedChangeListener { _, isChecked ->
                    onStatusChange(workstation, isChecked)
                }
                
                btnEdit.setOnClickListener {
                    onEditClick(workstation)
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