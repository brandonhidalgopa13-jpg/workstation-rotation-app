package com.workstation.rotation.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.workstation.rotation.R
import com.workstation.rotation.models.RotationGridRow

/**
 * Adaptador para las columnas de estaciones en formato tabla horizontal
 * Cada columna muestra el nombre de la estación (header rosado) y los trabajadores en vertical
 */
class RotationStationColumnAdapter(
    private val onWorkerClick: (workerId: Long, workstationId: Long) -> Unit
) : RecyclerView.Adapter<RotationStationColumnAdapter.StationColumnViewHolder>() {

    private var stations = listOf<RotationGridRow>()
    private var rotationType: String = "CURRENT"

    fun submitList(newStations: List<RotationGridRow>, type: String) {
        stations = newStations
        rotationType = type
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StationColumnViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_rotation_station_column, parent, false)
        return StationColumnViewHolder(view, onWorkerClick)
    }

    override fun onBindViewHolder(holder: StationColumnViewHolder, position: Int) {
        holder.bind(stations[position], rotationType)
    }

    override fun getItemCount(): Int = stations.size

    class StationColumnViewHolder(
        itemView: View,
        private val onWorkerClick: (workerId: Long, workstationId: Long) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val tvStationName: TextView = itemView.findViewById(R.id.tvStationName)
        private val recyclerWorkers: RecyclerView = itemView.findViewById(R.id.recyclerWorkers)
        private val workersAdapter = RotationWorkerCellAdapter(onWorkerClick)

        init {
            recyclerWorkers.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = workersAdapter
                isNestedScrollingEnabled = false
            }
        }

        fun bind(station: RotationGridRow, rotationType: String) {
            tvStationName.text = station.workstationName

            // Seleccionar las asignaciones según el tipo de rotación
            val assignments = if (rotationType == "CURRENT") {
                station.currentAssignments
            } else {
                station.nextAssignments
            }

            workersAdapter.submitList(assignments, station.workstationId)
        }
    }
}
