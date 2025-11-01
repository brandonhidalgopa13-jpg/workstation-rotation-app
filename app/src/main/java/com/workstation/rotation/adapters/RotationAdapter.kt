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
                
                // Identificar y colorear líderes
                val isLeader = rotationItem.workerName.contains("👑") || rotationItem.workerName.contains("[LÍDER]")
                
                if (isLeader) {
                    // Aplicar estilo especial para líderes
                    val cardView = binding.root as com.google.android.material.card.MaterialCardView
                    cardView.setCardBackgroundColor(android.graphics.Color.parseColor("#FFF3E5F5")) // Color púrpura claro
                    cardView.strokeColor = android.graphics.Color.parseColor("#FF9C27B0") // Borde púrpura
                    cardView.strokeWidth = 4
                    
                    // Cambiar color del nombre del trabajador
                    tvWorkerName.setTextColor(android.graphics.Color.parseColor("#FF6A1B9A")) // Púrpura oscuro
                    
                    // Cambiar color del número de rotación
                    tvRotationOrder.setBackgroundColor(android.graphics.Color.parseColor("#FFFFD700")) // Dorado para líderes
                    tvRotationOrder.setTextColor(android.graphics.Color.parseColor("#FF000000")) // Texto negro
                } else {
                    // Estilo normal para trabajadores regulares
                    val cardView = binding.root as com.google.android.material.card.MaterialCardView
                    cardView.setCardBackgroundColor(android.graphics.Color.parseColor("#FFFFFFFF")) // Blanco
                    cardView.strokeColor = android.graphics.Color.parseColor("#FFE0E0E0") // Borde gris claro
                    cardView.strokeWidth = 1
                    
                    // Color normal del nombre
                    tvWorkerName.setTextColor(android.graphics.Color.parseColor("#FF212121")) // Negro
                    
                    // Color normal del número de rotación
                    tvRotationOrder.setBackgroundColor(android.graphics.Color.parseColor("#FF1976D2")) // Azul normal
                    tvRotationOrder.setTextColor(android.graphics.Color.parseColor("#FFFFFFFF")) // Texto blanco
                }
                
                // Show capacity info based on priority and assignment type
                when {
                    isLeader -> {
                        tvCapacityInfo.visibility = android.view.View.VISIBLE
                        tvCapacityInfo.text = "👑 LÍDER DE ESTACIÓN - Prioridad máxima"
                        tvCapacityInfo.setTextColor(android.graphics.Color.parseColor("#FF9C27B0"))
                    }
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