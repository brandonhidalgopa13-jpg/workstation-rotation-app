package com.workstation.rotation.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.workstation.rotation.R
import com.workstation.rotation.models.RotationGridCell

/**
 * Adaptador para las celdas de trabajadores en cada columna de estación
 * Muestra los trabajadores en vertical
 */
class RotationWorkerCellAdapter(
    private val onWorkerClick: (workerId: Long, workstationId: Long) -> Unit
) : RecyclerView.Adapter<RotationWorkerCellAdapter.WorkerCellViewHolder>() {

    private var workers = listOf<RotationGridCell>()
    private var workstationId: Long = 0

    fun submitList(newWorkers: List<RotationGridCell>, stationId: Long) {
        workers = newWorkers
        workstationId = stationId
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkerCellViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_rotation_worker_cell, parent, false)
        return WorkerCellViewHolder(view, onWorkerClick)
    }

    override fun onBindViewHolder(holder: WorkerCellViewHolder, position: Int) {
        holder.bind(workers[position], workstationId)
    }

    override fun getItemCount(): Int = workers.size

    class WorkerCellViewHolder(
        itemView: View,
        private val onWorkerClick: (workerId: Long, workstationId: Long) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val workerCell: LinearLayout = itemView.findViewById(R.id.workerCell)
        private val tvWorkerName: TextView = itemView.findViewById(R.id.tvWorkerName)
        private val capacityIndicator: View = itemView.findViewById(R.id.capacityIndicator)

        fun bind(cell: RotationGridCell, workstationId: Long) {
            if (cell.isAssigned && cell.workerName != null) {
                // Trabajador asignado
                tvWorkerName.text = cell.workerName
                tvWorkerName.setTextColor(Color.parseColor("#212121"))
                workerCell.setBackgroundColor(Color.WHITE)

                // Indicador de capacidad según nivel de competencia
                when (cell.competencyLevel) {
                    5 -> {
                        capacityIndicator.visibility = View.VISIBLE
                        capacityIndicator.setBackgroundColor(Color.parseColor("#4CAF50")) // Verde - Experto
                    }
                    4 -> {
                        capacityIndicator.visibility = View.VISIBLE
                        capacityIndicator.setBackgroundColor(Color.parseColor("#8BC34A")) // Verde claro - Avanzado
                    }
                    3 -> {
                        capacityIndicator.visibility = View.VISIBLE
                        capacityIndicator.setBackgroundColor(Color.parseColor("#FFC107")) // Amarillo - Intermedio
                    }
                    2 -> {
                        capacityIndicator.visibility = View.VISIBLE
                        capacityIndicator.setBackgroundColor(Color.parseColor("#FF9800")) // Naranja - Básico
                    }
                    else -> {
                        capacityIndicator.visibility = View.GONE
                    }
                }

                // Resaltar si es líder o entrenador
                if (cell.canBeLeader) {
                    workerCell.setBackgroundColor(Color.parseColor("#FFF9C4")) // Amarillo claro
                } else if (cell.canTrain) {
                    workerCell.setBackgroundColor(Color.parseColor("#E1F5FE")) // Azul claro
                }

                // Click listener
                workerCell.setOnClickListener {
                    cell.workerId?.let { workerId ->
                        onWorkerClick(workerId, workstationId)
                    }
                }

            } else {
                // Celda vacía
                tvWorkerName.text = ""
                tvWorkerName.setTextColor(Color.parseColor("#9E9E9E"))
                workerCell.setBackgroundColor(Color.parseColor("#F5F5F5"))
                capacityIndicator.visibility = View.GONE

                // Click para asignar trabajador
                workerCell.setOnClickListener {
                    onWorkerClick(0, workstationId)
                }
            }
        }
    }
}
