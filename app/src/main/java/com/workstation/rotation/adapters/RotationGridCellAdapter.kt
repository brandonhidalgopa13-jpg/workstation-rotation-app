package com.workstation.rotation.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.workstation.rotation.databinding.ItemRotationGridCellBinding
import com.workstation.rotation.models.RotationGridCell

/**
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 📱 ADAPTER PARA CELDAS DEL GRID DE ROTACIÓN - v4.0
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 
 * 🎯 PROPÓSITO:
 * Adapter para mostrar las celdas individuales del grid de rotación.
 * Cada celda puede contener un trabajador asignado o estar vacía.
 * 
 * 📋 CARACTERÍSTICAS:
 * • Visualización de trabajadores asignados
 * • Estados visuales diferenciados
 * • Indicadores de competencia y roles
 * • Soporte para drag & drop
 * • Manejo de celdas vacías
 * 
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 */

class RotationGridCellAdapter(
    private var cells: List<RotationGridCell> = emptyList(),
    private val onCellClick: (RotationGridCell, Int) -> Unit = { _, _ -> },
    private val onCellLongClick: (RotationGridCell, Int) -> Boolean = { _, _ -> false }
) : RecyclerView.Adapter<RotationGridCellAdapter.CellViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CellViewHolder {
        val binding = ItemRotationGridCellBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return CellViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CellViewHolder, position: Int) {
        holder.bind(cells[position])
    }

    override fun getItemCount(): Int = cells.size

    fun updateCells(newCells: List<RotationGridCell>) {
        cells = newCells
        notifyDataSetChanged()
    }

    inner class CellViewHolder(
        private val binding: ItemRotationGridCellBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    onCellClick(cells[adapterPosition], adapterPosition)
                }
            }
            
            binding.root.setOnLongClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    onCellLongClick(cells[adapterPosition], adapterPosition)
                } else false
            }
        }

        fun bind(cell: RotationGridCell) {
            with(binding) {
                if (cell.isAssigned && cell.workerName != null) {
                    // Celda con trabajador asignado
                    bindAssignedCell(cell)
                } else {
                    // Celda vacía
                    bindEmptyCell(cell)
                }
                
                // Aplicar colores de fondo y texto
                root.setBackgroundColor(Color.parseColor(cell.getBackgroundColor()))
                tvWorkerName.setTextColor(Color.parseColor(cell.getTextColor()))
                
                // Mostrar indicador de conflicto si existe
                if (cell.conflictReason != null) {
                    layoutConflictIndicator.visibility = View.VISIBLE
                } else {
                    layoutConflictIndicator.visibility = View.GONE
                }
            }
        }

        private fun bindAssignedCell(cell: RotationGridCell) {
            with(binding) {
                // Mostrar nombre del trabajador
                tvWorkerName.text = cell.workerName
                tvWorkerName.visibility = View.VISIBLE
                tvEmptyMessage.visibility = View.GONE
                
                // Mostrar icono si existe
                cell.getIcon()?.let { icon ->
                    tvCellIcon.text = icon
                    tvCellIcon.visibility = View.VISIBLE
                } ?: run {
                    tvCellIcon.visibility = View.GONE
                }
                
                // Mostrar indicador de competencia
                if (cell.competencyLevel != null) {
                    setupCompetencyIndicator(cell.competencyLevel)
                    layoutCompetencyIndicator.visibility = View.VISIBLE
                } else {
                    layoutCompetencyIndicator.visibility = View.GONE
                }
            }
        }

        private fun bindEmptyCell(cell: RotationGridCell) {
            with(binding) {
                tvWorkerName.visibility = View.GONE
                tvCellIcon.visibility = View.GONE
                layoutCompetencyIndicator.visibility = View.GONE
                
                tvEmptyMessage.text = "Toca para\nasignar"
                tvEmptyMessage.visibility = View.VISIBLE
            }
        }

        private fun setupCompetencyIndicator(level: Int) {
            with(binding.layoutCompetencyIndicator) {
                // Obtener todas las vistas de puntos (asumiendo que hay 5)
                val dots = (0 until childCount).map { getChildAt(it) }
                
                dots.forEachIndexed { index, dot ->
                    val color = if (index < level) {
                        when {
                            level >= 4 -> Color.parseColor("#2196F3") // Azul - Experto/Avanzado
                            level >= 3 -> Color.parseColor("#4CAF50") // Verde - Intermedio
                            else -> Color.parseColor("#FF9800")       // Naranja - Básico/Principiante
                        }
                    } else {
                        Color.parseColor("#E0E0E0") // Gris - No alcanzado
                    }
                    dot.setBackgroundColor(color)
                }
            }
        }
    }
}