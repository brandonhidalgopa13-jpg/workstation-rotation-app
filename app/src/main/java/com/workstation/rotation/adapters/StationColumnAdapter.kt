package com.workstation.rotation.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.workstation.rotation.R
import com.workstation.rotation.models.RotationGridRow

/**
 * Adaptador para las columnas de estaciones en la vista de rotación v2.
 * Cada columna muestra una estación con sus trabajadores actuales y siguientes
 * en scroll vertical independiente.
 */
class StationColumnAdapter(
    private val onWorkerClick: (workerId: Long, workstationId: Long, rotationType: String) -> Unit,
    private val onEmptySlotClick: (workstationId: Long, rotationType: String) -> Unit
) : RecyclerView.Adapter<StationColumnAdapter.StationColumnViewHolder>() {

    private var stations = listOf<RotationGridRow>()

    fun submitList(newStations: List<RotationGridRow>) {
        stations = newStations
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StationColumnViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_station_column_v2, parent, false)
        return StationColumnViewHolder(view, onWorkerClick, onEmptySlotClick)
    }

    override fun onBindViewHolder(holder: StationColumnViewHolder, position: Int) {
        holder.bind(stations[position])
    }

    override fun getItemCount(): Int = stations.size

    class StationColumnViewHolder(
        itemView: View,
        private val onWorkerClick: (workerId: Long, workstationId: Long, rotationType: String) -> Unit,
        private val onEmptySlotClick: (workstationId: Long, rotationType: String) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val tvStationName: TextView = itemView.findViewById(R.id.tvStationName)
        private val tvStationCapacity: TextView = itemView.findViewById(R.id.tvStationCapacity)
        private val recyclerCurrentWorkers: RecyclerView = itemView.findViewById(R.id.recyclerCurrentWorkers)
        private val recyclerNextWorkers: RecyclerView = itemView.findViewById(R.id.recyclerNextWorkers)
        private val tvCurrentCount: TextView = itemView.findViewById(R.id.tvCurrentCount)
        private val tvNextCount: TextView = itemView.findViewById(R.id.tvNextCount)
        private val progressCurrentCapacity: ProgressBar = itemView.findViewById(R.id.progressCurrentCapacity)
        private val progressNextCapacity: ProgressBar = itemView.findViewById(R.id.progressNextCapacity)

        private val currentWorkersAdapter = WorkerCardAdapter(onWorkerClick, onEmptySlotClick)
        private val nextWorkersAdapter = WorkerCardAdapter(onWorkerClick, onEmptySlotClick)

        init {
            // Configurar RecyclerView para trabajadores actuales
            recyclerCurrentWorkers.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = currentWorkersAdapter
                isNestedScrollingEnabled = false
            }

            // Configurar RecyclerView para trabajadores siguientes
            recyclerNextWorkers.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = nextWorkersAdapter
                isNestedScrollingEnabled = false
            }
        }

        fun bind(station: RotationGridRow) {
            // Información de la estación
            tvStationName.text = station.workstationName
            tvStationCapacity.text = "Capacidad: ${station.requiredWorkers} trabajadores"

            // Trabajadores actuales
            currentWorkersAdapter.submitList(
                station.currentAssignments,
                station.workstationId,
                "CURRENT"
            )

            // Trabajadores siguientes
            nextWorkersAdapter.submitList(
                station.nextAssignments,
                station.workstationId,
                "NEXT"
            )

            // Actualizar contadores y progreso
            val currentAssigned = station.currentAssignments.count { it.isAssigned }
            val nextAssigned = station.nextAssignments.count { it.isAssigned }

            tvCurrentCount.text = "$currentAssigned/${station.requiredWorkers}"
            tvNextCount.text = "$nextAssigned/${station.requiredWorkers}"

            val currentProgress = if (station.requiredWorkers > 0) {
                (currentAssigned * 100) / station.requiredWorkers
            } else 0

            val nextProgress = if (station.requiredWorkers > 0) {
                (nextAssigned * 100) / station.requiredWorkers
            } else 0

            progressCurrentCapacity.progress = currentProgress
            progressNextCapacity.progress = nextProgress
        }
    }
}
