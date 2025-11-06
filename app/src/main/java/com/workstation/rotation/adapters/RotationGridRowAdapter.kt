package com.workstation.rotation.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.workstation.rotation.databinding.ItemRotationGridRowBinding
import com.workstation.rotation.models.RotationGridRow
import com.workstation.rotation.models.RotationGridCell

/**
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 📱 ADAPTER PARA FILAS DEL GRID DE ROTACIÓN - v4.0
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 
 * 🎯 PROPÓSITO:
 * Adapter para mostrar las filas del grid de rotación. Cada fila representa una estación
 * con sus asignaciones actuales y siguientes.
 * 
 * 📋 CARACTERÍSTICAS:
 * • Información de estación y capacidad
 * • Barras de progreso de ocupación
 * • RecyclerViews horizontales para asignaciones
 * • Indicadores visuales de estado
 * 
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 */

class RotationGridRowAdapter(
    private var rows: List<RotationGridRow> = emptyList(),
    private val onCellClick: (RotationGridCell, Int, String) -> Unit = { _, _, _ -> },
    private val onCellLongClick: (RotationGridCell, Int, String) -> Boolean = { _, _, _ -> false }
) : RecyclerView.Adapter<RotationGridRowAdapter.RowViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RowViewHolder {
        val binding = ItemRotationGridRowBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return RowViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RowViewHolder, position: Int) {
        holder.bind(rows[position])
    }

    override fun getItemCount(): Int = rows.size

    fun updateRows(newRows: List<RotationGridRow>) {
        rows = newRows
        notifyDataSetChanged()
    }

    inner class RowViewHolder(
        private val binding: ItemRotationGridRowBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private lateinit var currentAdapter: RotationGridCellAdapter
        private lateinit var nextAdapter: RotationGridCellAdapter

        fun bind(row: RotationGridRow) {
            with(binding) {
                // Configurar información de la estación
                setupWorkstationInfo(row)
                
                // Configurar barras de progreso
                setupProgressBars(row)
                
                // Configurar RecyclerViews de asignaciones
                setupAssignmentRecyclers(row)
            }
        }

        private fun setupWorkstationInfo(row: RotationGridRow) {
            with(binding) {
                tvWorkstationName.text = row.workstationName
                
                val currentCount = row.getCurrentAssignedCount()
                val nextCount = row.getNextAssignedCount()
                val required = row.requiredWorkers
                
                tvCapacityInfo.text = "$currentCount/$required | $nextCount/$required"
            }
        }

        private fun setupProgressBars(row: RotationGridRow) {
            with(binding) {
                // Barra de progreso actual
                progressCurrentCapacity.progress = row.getCurrentUtilizationPercentage().toInt()
                progressCurrentCapacity.progressTintList = 
                    android.content.res.ColorStateList.valueOf(
                        Color.parseColor(row.getCurrentCapacityColor())
                    )
                
                // Barra de progreso siguiente
                progressNextCapacity.progress = row.getNextUtilizationPercentage().toInt()
                progressNextCapacity.progressTintList = 
                    android.content.res.ColorStateList.valueOf(
                        Color.parseColor(row.getNextCapacityColor())
                    )
            }
        }

        private fun setupAssignmentRecyclers(row: RotationGridRow) {
            with(binding) {
                // RecyclerView para asignaciones actuales
                currentAdapter = RotationGridCellAdapter(
                    cells = row.currentAssignments,
                    onCellClick = { cell, position -> 
                        onCellClick(cell, position, "CURRENT")
                    },
                    onCellLongClick = { cell, position -> 
                        onCellLongClick(cell, position, "CURRENT")
                    }
                )
                
                recyclerCurrentAssignments.apply {
                    layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                    adapter = currentAdapter
                    setHasFixedSize(true)
                }
                
                // RecyclerView para siguientes asignaciones
                nextAdapter = RotationGridCellAdapter(
                    cells = row.nextAssignments,
                    onCellClick = { cell, position -> 
                        onCellClick(cell, position, "NEXT")
                    },
                    onCellLongClick = { cell, position -> 
                        onCellLongClick(cell, position, "NEXT")
                    }
                )
                
                recyclerNextAssignments.apply {
                    layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                    adapter = nextAdapter
                    setHasFixedSize(true)
                }
            }
        }
    }
}