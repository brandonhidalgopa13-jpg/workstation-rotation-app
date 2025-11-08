package com.workstation.rotation.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.workstation.rotation.R
import com.workstation.rotation.models.RotationGridCell

/**
 * Adaptador para las tarjetas de trabajadores en cada columna de estación.
 * Muestra trabajadores asignados o slots vacíos.
 */
class WorkerCardAdapter(
    private val onWorkerClick: (workerId: Long, workstationId: Long, rotationType: String) -> Unit,
    private val onEmptySlotClick: (workstationId: Long, rotationType: String) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var workers = listOf<RotationGridCell>()
    private var workstationId: Long = 0
    private var rotationType: String = ""

    companion object {
        private const val VIEW_TYPE_WORKER = 1
        private const val VIEW_TYPE_EMPTY = 2
    }

    fun submitList(newWorkers: List<RotationGridCell>, stationId: Long, type: String) {
        workers = newWorkers
        workstationId = stationId
        rotationType = type
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return if (workers[position].isAssigned) VIEW_TYPE_WORKER else VIEW_TYPE_EMPTY
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_WORKER -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_worker_card_v2, parent, false)
                WorkerCardViewHolder(view, onWorkerClick)
            }
            else -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_empty_worker_slot_v2, parent, false)
                EmptySlotViewHolder(view, onEmptySlotClick)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is WorkerCardViewHolder -> holder.bind(workers[position], workstationId, rotationType)
            is EmptySlotViewHolder -> holder.bind(workstationId, rotationType)
        }
    }

    override fun getItemCount(): Int = workers.size

    /**
     * ViewHolder para trabajadores asignados
     */
    class WorkerCardViewHolder(
        itemView: View,
        private val onWorkerClick: (workerId: Long, workstationId: Long, rotationType: String) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val tvWorkerIcon: TextView = itemView.findViewById(R.id.tvWorkerIcon)
        private val tvWorkerName: TextView = itemView.findViewById(R.id.tvWorkerName)
        private val btnOptions: ImageView = itemView.findViewById(R.id.btnOptions)
        private val layoutCompetencyIndicator: LinearLayout = itemView.findViewById(R.id.layoutCompetencyIndicator)
        private val dotCompetency1: View = itemView.findViewById(R.id.dotCompetency1)
        private val dotCompetency2: View = itemView.findViewById(R.id.dotCompetency2)
        private val dotCompetency3: View = itemView.findViewById(R.id.dotCompetency3)
        private val dotCompetency4: View = itemView.findViewById(R.id.dotCompetency4)
        private val dotCompetency5: View = itemView.findViewById(R.id.dotCompetency5)
        private val layoutTags: LinearLayout = itemView.findViewById(R.id.layoutTags)
        private val chipLeader: Chip = itemView.findViewById(R.id.chipLeader)
        private val chipTrainer: Chip = itemView.findViewById(R.id.chipTrainer)
        private val layoutConflictIndicator: LinearLayout = itemView.findViewById(R.id.layoutConflictIndicator)

        fun bind(worker: RotationGridCell, workstationId: Long, rotationType: String) {
            // Nombre del trabajador
            tvWorkerName.text = worker.workerName ?: "Desconocido"

            // Icono según rol
            tvWorkerIcon.text = when {
                worker.canBeLeader -> "👑"
                worker.canTrain -> "👨‍🏫"
                else -> "👤"
            }

            // Indicador de competencia
            if (worker.competencyLevel != null) {
                layoutCompetencyIndicator.visibility = View.VISIBLE
                updateCompetencyDots(worker.competencyLevel)
            } else {
                layoutCompetencyIndicator.visibility = View.GONE
            }

            // Tags de rol
            val hasLeader = worker.canBeLeader
            val hasTrainer = worker.canTrain

            if (hasLeader || hasTrainer) {
                layoutTags.visibility = View.VISIBLE
                chipLeader.visibility = if (hasLeader) View.VISIBLE else View.GONE
                chipTrainer.visibility = if (hasTrainer) View.VISIBLE else View.GONE
            } else {
                layoutTags.visibility = View.GONE
            }

            // Indicador de asignación óptima (cambiar color de fondo)
            if (worker.isOptimalAssignment) {
                itemView.setBackgroundColor(
                    ContextCompat.getColor(itemView.context, R.color.success_light)
                )
            } else {
                itemView.setBackgroundColor(
                    ContextCompat.getColor(itemView.context, android.R.color.transparent)
                )
            }

            // Click listeners
            itemView.setOnClickListener {
                worker.workerId?.let { workerId ->
                    onWorkerClick(workerId, workstationId, rotationType)
                }
            }

            btnOptions.setOnClickListener {
                // TODO: Mostrar menú de opciones
            }
        }

        private fun updateCompetencyDots(level: Int) {
            val dots = listOf(dotCompetency1, dotCompetency2, dotCompetency3, dotCompetency4, dotCompetency5)
            val activeColor = ContextCompat.getColor(itemView.context, R.color.success)
            val inactiveColor = ContextCompat.getColor(itemView.context, R.color.divider)

            dots.forEachIndexed { index, dot ->
                dot.setBackgroundColor(if (index < level) activeColor else inactiveColor)
            }
        }
    }

    /**
     * ViewHolder para slots vacíos
     */
    class EmptySlotViewHolder(
        itemView: View,
        private val onEmptySlotClick: (workstationId: Long, rotationType: String) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        fun bind(workstationId: Long, rotationType: String) {
            itemView.setOnClickListener {
                onEmptySlotClick(workstationId, rotationType)
            }
        }
    }
}
