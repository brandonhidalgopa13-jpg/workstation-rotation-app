package com.workstation.rotation.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.workstation.rotation.R
import com.workstation.rotation.databinding.ItemAvailableWorkerBinding
import com.workstation.rotation.models.AvailableWorker

/**
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 📱 ADAPTER PARA TRABAJADORES DISPONIBLES - v4.0
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 
 * 🎯 PROPÓSITO:
 * Adapter para mostrar la lista de trabajadores disponibles para asignación.
 * Incluye información de capacidades, estado actual y estaciones disponibles.
 * 
 * 📋 CARACTERÍSTICAS:
 * • Información completa del trabajador
 * • Chips de estaciones disponibles
 * • Indicadores de asignación actual/siguiente
 * • Estados visuales diferenciados
 * • Soporte para drag & drop
 * 
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 */

class AvailableWorkersAdapter(
    private var workers: List<AvailableWorker> = emptyList(),
    private val onWorkerClick: (AvailableWorker) -> Unit = {},
    private val onWorkerLongClick: (AvailableWorker) -> Boolean = { false },
    private val onWorkerDrag: (AvailableWorker) -> Unit = {}
) : RecyclerView.Adapter<AvailableWorkersAdapter.WorkerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkerViewHolder {
        val binding = ItemAvailableWorkerBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return WorkerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WorkerViewHolder, position: Int) {
        holder.bind(workers[position])
    }

    override fun getItemCount(): Int = workers.size

    fun updateWorkers(newWorkers: List<AvailableWorker>) {
        workers = newWorkers
        notifyDataSetChanged()
    }

    inner class WorkerViewHolder(
        private val binding: ItemAvailableWorkerBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    onWorkerClick(workers[adapterPosition])
                }
            }
            
            binding.root.setOnLongClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    onWorkerLongClick(workers[adapterPosition])
                } else false
            }
            
            binding.btnWorkerAction.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    onWorkerDrag(workers[adapterPosition])
                }
            }
        }

        fun bind(worker: AvailableWorker) {
            with(binding) {
                // Información básica del trabajador
                setupWorkerInfo(worker)
                
                // Avatar del trabajador
                setupWorkerAvatar(worker)
                
                // Estado del trabajador
                setupWorkerStatus(worker)
                
                // Estaciones disponibles
                setupWorkstationChips(worker)
                
                // Indicadores de asignación
                setupAssignmentIndicators(worker)
            }
        }

        private fun setupWorkerInfo(worker: AvailableWorker) {
            with(binding) {
                tvWorkerName.text = worker.workerName
                tvEmployeeId.text = if (worker.employeeId != null) {
                    "ID: ${worker.employeeId}"
                } else {
                    "Sin ID"
                }
            }
        }

        private fun setupWorkerAvatar(worker: AvailableWorker) {
            with(binding) {
                val initials = worker.workerName
                    .split(" ")
                    .take(2)
                    .map { it.firstOrNull()?.uppercaseChar() ?: "" }
                    .joinToString("")
                
                tvWorkerAvatar.text = initials
                
                // Color del avatar según estado
                val avatarColor = Color.parseColor(worker.getStatusColor())
                tvWorkerAvatar.setTextColor(avatarColor)
            }
        }

        private fun setupWorkerStatus(worker: AvailableWorker) {
            with(binding) {
                val statusText = when {
                    !worker.isActive -> "Inactivo"
                    worker.isAssignedInCurrent && worker.isAssignedInNext -> "Completamente Asignado"
                    worker.isAssignedInCurrent -> "Asignado Actual"
                    worker.isAssignedInNext -> "Asignado Siguiente"
                    else -> "Disponible"
                }
                
                val statusColor = when {
                    !worker.isActive -> R.color.error
                    worker.isAssignedInCurrent && worker.isAssignedInNext -> R.color.success
                    worker.isAssignedInCurrent || worker.isAssignedInNext -> R.color.warning
                    else -> R.color.info
                }
                
                chipWorkerStatus.text = statusText
                chipWorkerStatus.setChipBackgroundColorResource(statusColor)
            }
        }

        private fun setupWorkstationChips(worker: AvailableWorker) {
            with(binding.chipGroupWorkstations) {
                removeAllViews()
                
                worker.availableWorkstations.forEach { capability ->
                    val chip = Chip(context).apply {
                        text = capability.workstationName
                        isClickable = false
                        isCheckable = false
                        
                        // Color según nivel de competencia
                        val competencyColor = when (capability.competencyLevel) {
                            5 -> R.color.expert_level      // Azul - Experto
                            4 -> R.color.advanced_level    // Verde - Avanzado
                            3 -> R.color.intermediate_level // Amarillo - Intermedio
                            2 -> R.color.basic_level       // Naranja - Básico
                            1 -> R.color.beginner_level    // Rojo - Principiante
                            else -> R.color.unknown_level  // Gris - Desconocido
                        }
                        
                        setChipBackgroundColorResource(competencyColor)
                        setTextColor(context.getColor(android.R.color.white))
                        
                        // Agregar iconos especiales
                        when {
                            capability.canBeLeader -> chipIcon = context.getDrawable(R.drawable.ic_star)
                            capability.canTrain -> chipIcon = context.getDrawable(R.drawable.ic_school)
                            capability.isCertified -> chipIcon = context.getDrawable(R.drawable.ic_verified)
                        }
                        
                        textSize = 10f
                    }
                    
                    addView(chip)
                }
            }
        }

        private fun setupAssignmentIndicators(worker: AvailableWorker) {
            with(binding) {
                // Indicador de asignación actual
                if (worker.isAssignedInCurrent && worker.currentAssignment != null) {
                    layoutCurrentAssignment.visibility = View.VISIBLE
                    // Aquí podrías obtener el nombre de la estación desde el ID
                    tvCurrentWorkstation.text = "Est. ${worker.currentAssignment}"
                } else {
                    layoutCurrentAssignment.visibility = View.GONE
                }
                
                // Indicador de asignación siguiente
                if (worker.isAssignedInNext && worker.nextAssignment != null) {
                    layoutNextAssignment.visibility = View.VISIBLE
                    // Aquí podrías obtener el nombre de la estación desde el ID
                    tvNextWorkstation.text = "Est. ${worker.nextAssignment}"
                } else {
                    layoutNextAssignment.visibility = View.GONE
                }
            }
        }
    }
}