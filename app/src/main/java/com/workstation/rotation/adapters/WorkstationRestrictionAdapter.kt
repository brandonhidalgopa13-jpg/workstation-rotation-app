package com.workstation.rotation.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.workstation.rotation.data.entities.Workstation
import com.workstation.rotation.data.entities.WorkerRestriction
import com.workstation.rotation.data.entities.RestrictionType
import com.workstation.rotation.databinding.ItemWorkstationRestrictionBinding

data class WorkstationRestrictionItem(
    val workstation: Workstation,
    var isRestricted: Boolean = false,
    var restriction: WorkerRestriction? = null
)

class WorkstationRestrictionAdapter(
    private val onRestrictionChange: (WorkstationRestrictionItem, Boolean) -> Unit
) : ListAdapter<WorkstationRestrictionItem, WorkstationRestrictionAdapter.RestrictionViewHolder>(RestrictionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RestrictionViewHolder {
        val binding = ItemWorkstationRestrictionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return RestrictionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RestrictionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class RestrictionViewHolder(
        private val binding: ItemWorkstationRestrictionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: WorkstationRestrictionItem) {
            binding.apply {
                tvWorkstationName.text = item.workstation.name
                checkboxRestricted.isChecked = item.isRestricted
                
                // Mostrar información de restricción si existe
                if (item.isRestricted && item.restriction != null) {
                    tvRestrictionInfo.visibility = android.view.View.VISIBLE
                    ivRestrictionIcon.visibility = android.view.View.VISIBLE
                    
                    val restrictionText = when (item.restriction!!.restrictionType) {
                        RestrictionType.PROHIBITED -> "🚫 Prohibido"
                        RestrictionType.LIMITED -> "⚠️ Limitado"
                        RestrictionType.TEMPORARY -> "⏰ Temporal"
                    }
                    
                    val notes = if (item.restriction!!.notes.isNotEmpty()) {
                        " - ${item.restriction!!.notes}"
                    } else ""
                    
                    tvRestrictionInfo.text = "$restrictionText$notes"
                } else {
                    tvRestrictionInfo.visibility = android.view.View.GONE
                    ivRestrictionIcon.visibility = android.view.View.GONE
                }
                
                // Listener para cambios en el checkbox
                checkboxRestricted.setOnCheckedChangeListener { _, isChecked ->
                    item.isRestricted = isChecked
                    onRestrictionChange(item, isChecked)
                    
                    // Actualizar visibilidad de información
                    if (!isChecked) {
                        tvRestrictionInfo.visibility = android.view.View.GONE
                        ivRestrictionIcon.visibility = android.view.View.GONE
                    }
                }
            }
        }
    }

    private class RestrictionDiffCallback : DiffUtil.ItemCallback<WorkstationRestrictionItem>() {
        override fun areItemsTheSame(oldItem: WorkstationRestrictionItem, newItem: WorkstationRestrictionItem): Boolean {
            return oldItem.workstation.id == newItem.workstation.id
        }

        override fun areContentsTheSame(oldItem: WorkstationRestrictionItem, newItem: WorkstationRestrictionItem): Boolean {
            return oldItem == newItem
        }
    }
}