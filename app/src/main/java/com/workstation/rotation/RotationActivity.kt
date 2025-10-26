package com.workstation.rotation

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.workstation.rotation.R
import com.workstation.rotation.data.database.AppDatabase
import com.workstation.rotation.data.entities.Worker
import com.workstation.rotation.databinding.ActivityRotationBinding
import com.workstation.rotation.models.RotationTable
import com.workstation.rotation.models.WorkstationColumn
import com.workstation.rotation.utils.UIUtils
import com.workstation.rotation.viewmodels.RotationViewModel
import com.workstation.rotation.viewmodels.RotationViewModelFactory
import kotlinx.coroutines.launch

/**
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 🔄 ACTIVIDAD DE ROTACIÓN - MOTOR PRINCIPAL DEL SISTEMA
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 
 * 📋 FUNCIONES PRINCIPALES DE ESTA ACTIVIDAD:
 * 
 * 🎯 1. GENERACIÓN DE ROTACIONES INTELIGENTES
 *    - Ejecuta el algoritmo de rotación con un solo clic
 *    - Procesa todos los trabajadores y estaciones activos
 *    - Aplica reglas de negocio complejas automáticamente
 * 
 * 📊 2. VISUALIZACIÓN AVANZADA DE RESULTADOS
 *    - Muestra tabla de rotación con dos fases (actual y siguiente)
 *    - Presenta información de capacidad y ocupación por estación
 *    - Indica visualmente parejas de entrenamiento activas
 *    - Resalta estaciones prioritarias y trabajadores especiales
 * 
 * 🎨 3. INTERFAZ DINÁMICA Y RESPONSIVE
 *    - Genera columnas dinámicamente según estaciones disponibles
 *    - Scroll horizontal para manejar múltiples estaciones
 *    - Colores diferenciados para fases actual y siguiente
 *    - Indicadores visuales de estado y capacidad
 * 
 * 🔧 4. CONTROLES DE GESTIÓN
 *    - Botón "Generar Rotación": Ejecuta algoritmo completo
 *    - Botón "Limpiar": Resetea resultados para nueva generación
 *    - Información en tiempo real de trabajadores elegibles
 * 
 * 👥 5. SISTEMA DE ENTRENAMIENTO VISUAL
 *    - Identifica parejas entrenador-entrenado con iconos especiales
 *    - Muestra estado de entrenamiento activo (🤝 [ENTRENANDO])
 *    - Confirma asignación correcta a estaciones de entrenamiento
 * 
 * ⭐ 6. MANEJO DE ESTACIONES PRIORITARIAS
 *    - Resalta estaciones críticas con indicador ⭐ COMPLETA
 *    - Asegura capacidad completa en áreas prioritarias
 *    - Prioriza mejores trabajadores para estas estaciones
 * 
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 🔧 COMPONENTES TÉCNICOS:
 * 
 * • ViewModel con Factory para inyección de dependencias
 * • Corrutinas para operaciones asíncronas de base de datos
 * • LiveData para observación reactiva de cambios
 * • View Binding para acceso seguro a elementos UI
 * • Generación dinámica de layouts para flexibilidad
 * 
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 */

class RotationActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityRotationBinding
    
    private val viewModel: RotationViewModel by viewModels {
        RotationViewModelFactory(
            AppDatabase.getDatabase(this).workerDao(),
            AppDatabase.getDatabase(this).workstationDao()
        )
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRotationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupButtons()
        observeRotation()
    }
    
    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }
    
    /**
     * Sets up the rotation table view with workstations as columns.
     */
    private fun setupRotationTable(rotationTable: RotationTable) {
        // Clear existing views
        binding.layoutWorkstationHeaders.removeAllViews()
        binding.layoutCapacityRequirements.removeAllViews()
        binding.layoutCurrentPhase.removeAllViews()
        binding.layoutNextPhase.removeAllViews()
        
        val workstationColumns = rotationTable.workstations.map { workstation ->
            WorkstationColumn(
                workstation = workstation,
                currentWorkers = rotationTable.currentPhase[workstation.id] ?: emptyList(),
                nextWorkers = rotationTable.nextPhase[workstation.id] ?: emptyList()
            )
        }
        
        // Create columns for each workstation
        workstationColumns.forEach { column ->
            createWorkstationColumn(column)
        }
    }
    
    /**
     * Creates a column for a workstation in the rotation table.
     */
    private fun createWorkstationColumn(column: WorkstationColumn) {
        val columnWidth = 160 // dp
        val layoutParams = LinearLayout.LayoutParams(
            (columnWidth * resources.displayMetrics.density).toInt(),
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(8, 0, 8, 0)
        }
        
        // Header
        createWorkstationHeader(column, layoutParams)
        
        // Capacity requirement
        createCapacityRequirement(column, layoutParams)
        
        // Current phase workers
        createWorkerColumn(column.currentWorkers, binding.layoutCurrentPhase, layoutParams, true)
        
        // Next phase workers
        createWorkerColumn(column.nextWorkers, binding.layoutNextPhase, layoutParams, false)
    }
    
    /**
     * Creates the header for a workstation column.
     */
    private fun createWorkstationHeader(column: WorkstationColumn, layoutParams: LinearLayout.LayoutParams) {
        val headerView = TextView(this).apply {
            text = column.workstation.getDisplayName()
            textSize = 14f
            setTextColor(ContextCompat.getColor(this@RotationActivity, R.color.white))
            gravity = android.view.Gravity.CENTER
            setPadding(12, 12, 12, 12)
            background = ContextCompat.getDrawable(this@RotationActivity, R.drawable.icon_background_blue)
            this.layoutParams = layoutParams
        }
        binding.layoutWorkstationHeaders.addView(headerView)
    }
    
    /**
     * Creates the capacity requirement display for a workstation.
     */
    private fun createCapacityRequirement(column: WorkstationColumn, layoutParams: LinearLayout.LayoutParams) {
        val capacityView = TextView(this).apply {
            text = "Requiere: ${column.workstation.requiredWorkers}"
            textSize = 12f
            setTextColor(ContextCompat.getColor(this@RotationActivity, R.color.text_primary))
            gravity = android.view.Gravity.CENTER
            setPadding(8, 8, 8, 8)
            background = ContextCompat.getDrawable(this@RotationActivity, R.drawable.status_badge_orange)
            this.layoutParams = layoutParams
        }
        binding.layoutCapacityRequirements.addView(capacityView)
    }
    
    /**
     * Creates a column of workers for current or next phase.
     */
    private fun createWorkerColumn(
        workers: List<Worker>, 
        parentLayout: LinearLayout, 
        layoutParams: LinearLayout.LayoutParams,
        isCurrentPhase: Boolean
    ) {
        val columnLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(4, 4, 4, 4)
            this.layoutParams = layoutParams
        }
        
        workers.forEach { worker ->
            val workerView = createWorkerView(worker, isCurrentPhase)
            columnLayout.addView(workerView)
        }
        
        parentLayout.addView(columnLayout)
    }
    
    /**
     * Creates a view for an individual worker.
     */
    private fun createWorkerView(worker: Worker, isCurrentPhase: Boolean): TextView {
        return TextView(this).apply {
            text = worker.getDisplayName()
            textSize = 11f
            setTextColor(ContextCompat.getColor(this@RotationActivity, R.color.text_primary))
            gravity = android.view.Gravity.CENTER
            setPadding(8, 6, 8, 6)
            
            val bgColor = if (isCurrentPhase) R.color.primary_blue_light else R.color.accent_orange
            background = ContextCompat.getDrawable(this@RotationActivity, R.drawable.status_badge_green)
            backgroundTintList = ContextCompat.getColorStateList(this@RotationActivity, bgColor)
            
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(2, 2, 2, 2)
            }
            layoutParams = params
        }
    }
    
    private fun setupButtons() {
        binding.btnGenerateRotation.setOnClickListener {
            lifecycleScope.launch {
                try {
                    val rotationGenerated = viewModel.generateRotation()
                    if (rotationGenerated) {
                        val stats = viewModel.getRotationStatistics()
                        Snackbar.make(
                            binding.root, 
                            "✅ Rotación generada: ${stats.getSummaryText()}", 
                            Snackbar.LENGTH_LONG
                        ).show()
                        updateWorkerCount()
                        updateRotationInfo(stats)
                    } else {
                        val count = viewModel.getEligibleWorkersCount()
                        val message = if (count == 0) {
                            "No hay trabajadores con estaciones asignadas. Agrega trabajadores y asigna estaciones primero."
                        } else {
                            "No se pudo generar la rotación. Verifica que haya estaciones activas y capacidad suficiente."
                        }
                        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    Snackbar.make(binding.root, "Error al generar rotación: ${e.message}", Snackbar.LENGTH_LONG).show()
                }
            }
        }
        
        binding.btnClearRotation.setOnClickListener {
            viewModel.clearRotation()
            updateWorkerCount()
            Snackbar.make(binding.root, "Rotación limpiada", Snackbar.LENGTH_SHORT).show()
        }
    }
    
    private fun observeRotation() {
        viewModel.rotationTable.observe(this) { rotationTable ->
            if (rotationTable != null) {
                setupRotationTable(rotationTable)
            } else {
                clearRotationTable()
            }
            updateWorkerCount()
        }
        
        // Initial count update
        updateWorkerCount()
    }
    
    /**
     * Clears the rotation table display.
     */
    private fun clearRotationTable() {
        binding.layoutWorkstationHeaders.removeAllViews()
        binding.layoutCapacityRequirements.removeAllViews()
        binding.layoutCurrentPhase.removeAllViews()
        binding.layoutNextPhase.removeAllViews()
    }
    
    private fun updateWorkerCount() {
        lifecycleScope.launch {
            try {
                viewModel.updateEligibleWorkersCount()
                val count = viewModel.getEligibleWorkersCount()
                binding.tvRotationInfo.text = "Trabajadores elegibles: $count | " +
                        "💡 Presiona 'Generar Rotación' para crear asignaciones automáticas"
            } catch (e: Exception) {
                binding.tvRotationInfo.text = "Error al contar trabajadores elegibles"
            }
        }
    }
    
    /**
     * Updates the rotation information display with statistics.
     */
    private fun updateRotationInfo(stats: RotationViewModel.RotationStatistics) {
        val baseInfo = "Trabajadores elegibles: ${viewModel.getEligibleWorkersCount()}"
        val rotationInfo = if (stats.totalWorkers > 0) {
            "\n🔄 ${stats.getSummaryText()}"
        } else {
            ""
        }
        
        binding.tvRotationInfo.text = "$baseInfo$rotationInfo"
    }
}