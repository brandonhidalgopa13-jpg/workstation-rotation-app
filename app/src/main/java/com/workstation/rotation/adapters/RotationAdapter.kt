package com.workstation.rotation.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.workstation.rotation.databinding.ItemRotationBinding
import com.workstation.rotation.models.RotationItem

class RotationAdapter : ListAdapter<RotationItem, RotationAdapter.RotationViewHolder>(RotationDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RotationViewHolder {
        val binding = ItemRotationBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return RotationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RotationViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class RotationViewHolder(
        private val binding: ItemRotationBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(rotationItem: RotationItem) {
            binding.apply {
                tvRotationOrder.text = rotationItem.rotationOrder.toString()
                tvWorkerName.text = rotationItem.workerName
                tvCurrentWorkstation.text = rotationItem.currentWorkstation
                tvNextWorkstation.text = rotationItem.nextWorkstation
            }
        }
    }

    private class RotationDiffCallback : DiffUtil.ItemCallback<RotationItem>() {
        override fun areItemsTheSame(oldItem: RotationItem, newItem: RotationItem): Boolean {
            return oldItem.workerName == newItem.workerName && oldItem.rotationOrder == newItem.rotationOrder
        }

        override fun areContentsTheSame(oldItem: RotationItem, newItem: RotationItem): Boolean {
            return oldItem == newItem
        }
    }
}