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
                
                // Show capacity info based on priority and assignment type
                when {
                    rotationItem.workerName.contains("[PRIORITARIO]") -> {
                        tvCapacityInfo.visibility = android.view.View.VISIBLE
                        tvCapacityInfo.text = "🔒 ESTACIÓN PRIORITARIA - Capacidad garantizada"
                        tvCapacityInfo.setTextColor(android.graphics.Color.parseColor("#FF6200EE"))
                    }
                    rotationItem.currentWorkstation.contains("COMPLETA") || rotationItem.nextWorkstation.contains("COMPLETA") -> {
                        tvCapacityInfo.visibility = android.view.View.VISIBLE
                        tvCapacityInfo.text = "⭐ Estación con capacidad completa asegurada"
                        tvCapacityInfo.setTextColor(android.graphics.Color.parseColor("#FF018786"))
                    }
                    rotationItem.currentWorkstation.contains("/") && rotationItem.nextWorkstation.contains("/") -> {
                        tvCapacityInfo.visibility = android.view.View.VISIBLE
                        tvCapacityInfo.text = "✅ Asignación inteligente - Capacidad controlada"
                        tvCapacityInfo.setTextColor(android.graphics.Color.parseColor("#FF018786"))
                    }
                    else -> {
                        tvCapacityInfo.visibility = android.view.View.GONE
                    }
                }
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