package com.workstation.rotation

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.workstation.rotation.adapters.RotationHistoryAdapter
import com.workstation.rotation.adapters.RotationHistoryItem
import com.workstation.rotation.data.entities.RotationHistory
import com.workstation.rotation.viewmodels.RotationHistoryViewModel
import com.workstation.rotation.animations.closeDetails
import com.workstation.rotation.animations.AnimationManager
import com.workstation.rotation.animations.slideInChildrenFromBottom
import java.text.SimpleDateFormat
import java.util.*

/**
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 📊 ACTIVIDAD HISTORIAL DE ROTACIONES - VISUALIZACIÓN Y GESTIÓN COMPLETA
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 
 * 🎯 FUNCIONALIDADES PRINCIPALES:
 * • Visualización completa del historial de rotaciones
 * • Métricas en tiempo real del sistema
 * • Filtros avanzados por trabajador, estación y fecha
 * • Gestión de rotaciones activas
 * • Acciones rápidas para finalizar rotaciones
 * 
 * 📋 CARACTERÍSTICAS UI:
 * • Material Design con cards y animaciones
 * • RecyclerView optimizado con DiffUtil
 * • Loading states y manejo de errores
 * • FAB con acciones contextuales
 * • Filtros intuitivos con diálogos
 * 
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 */

class RotationHistoryActivity : AppCompatActivity() {
    
    private val viewModel: RotationHistoryViewModel by viewModels()
    private lateinit var adapter: RotationHistoryAdapter
    
    // Views
    private lateinit var recyclerViewHistory: RecyclerView
    private lateinit var loadingOverlay: View
    private lateinit var tvTotalRotations: TextView
    private lateinit var tvActiveRotations: TextView
    private lateinit var tvAverageDuration: TextView
    private lateinit var btnFilterByWorker: MaterialButton
    private lateinit var btnFilterByWorkstation: MaterialButton
    private lateinit var btnFilterByDate: MaterialButton
    private lateinit var btnClearFilters: MaterialButton
    private lateinit var btnRefresh: MaterialButton
    private lateinit var fabQuickActions: ExtendedFloatingActionButton
    
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rotation_history)
        
        setupToolbar()
        initializeViews()
        setupRecyclerView()
        setupClickListeners()
        observeViewModel()
        
        // Cargar datos iniciales
        viewModel.loadGeneralMetrics()
        
        // Configurar animaciones de entrada
        setupAnimations()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Historial de Rotaciones"
    }
    
    private fun initializeViews() {
        recyclerViewHistory = findViewById(R.id.recyclerViewHistory)
        loadingOverlay = findViewById(R.id.loadingOverlay)
        tvTotalRotations = findViewById(R.id.tvTotalRotations)
        tvActiveRotations = findViewById(R.id.tvActiveRotations)
        tvAverageDuration = findViewById(R.id.tvAverageDuration)
        btnFilterByWorker = findViewById(R.id.btnFilterByWorker)
        btnFilterByWorkstation = findViewById(R.id.btnFilterByWorkstation)
        btnFilterByDate = findViewById(R.id.btnFilterByDate)
        btnClearFilters = findViewById(R.id.btnClearFilters)
        btnRefresh = findViewById(R.id.btnRefresh)
        fabQuickActions = findViewById(R.id.fabQuickActions)
    }
    
    private fun setupRecyclerView() {
        adapter = RotationHistoryAdapter(
            onItemClick = { rotation -> showRotationDetails(rotation) },
            onFinishRotation = { rotation -> showFinishRotationDialog(rotation) },
            onAddScore = { rotation -> showAddScoreDialog(rotation) }
        )
        
        recyclerViewHistory.layoutManager = LinearLayoutManager(this)
        recyclerViewHistory.adapter = adapter
    }
    
    private fun setupClickListeners() {
        btnFilterByWorker.setOnClickListener { showWorkerFilterDialog() }
        btnFilterByWorkstation.setOnClickListener { showWorkstationFilterDialog() }
        btnFilterByDate.setOnClickListener { showDateRangeDialog() }
        btnClearFilters.setOnClickListener { 
            viewModel.clearFilters()
            Toast.makeText(this, "Filtros limpiados", Toast.LENGTH_SHORT).show()
        }
        btnRefresh.setOnClickListener { 
            viewModel.loadGeneralMetrics()
            Toast.makeText(this, "Datos actualizados", Toast.LENGTH_SHORT).show()
        }
        fabQuickActions.setOnClickListener { showQuickActionsDialog() }
    }
    
    private fun observeViewModel() {
        // Historial completo
        viewModel.allHistory.observe(this) { historyList ->
            val items = historyList.map { rotation ->
                RotationHistoryItem(
                    rotationHistory = rotation,
                    workerName = "Trabajador #${rotation.worker_id}", // TODO: Obtener nombre real
                    workstationName = "Estación #${rotation.workstation_id}" // TODO: Obtener nombre real
                )
            }
            adapter.submitList(items)
        }
        
        // Métricas generales
        viewModel.generalMetrics.observe(this) { metrics ->
            tvTotalRotations.text = metrics.totalRotations.toString()
            tvActiveRotations.text = metrics.activeRotations.toString()
            
            // Calcular duración promedio (simplificado por ahora)
            tvAverageDuration.text = "45min" // TODO: Calcular real
        }
        
        // Estados de loading
        viewModel.isLoading.observe(this) { isLoading ->
            loadingOverlay.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
        
        // Mensajes de error
        viewModel.errorMessage.observe(this) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                viewModel.clearMessages()
            }
        }
        
        // Mensajes de éxito
        viewModel.operationSuccess.observe(this) { successMessage ->
            successMessage?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                viewModel.clearMessages()
            }
        }
        
        // Historial filtrado
        viewModel.filteredHistory.observe(this) { filteredList ->
            if (filteredList.isNotEmpty()) {
                val items = filteredList.map { rotation ->
                    RotationHistoryItem(
                        rotationHistory = rotation,
                        workerName = "Trabajador #${rotation.worker_id}",
                        workstationName = "Estación #${rotation.workstation_id}"
                    )
                }
                adapter.submitList(items)
            }
        }
    }
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // 🔍 DIÁLOGOS DE FILTROS
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    private fun showWorkerFilterDialog() {
        // TODO: Implementar selector de trabajadores
        Toast.makeText(this, "Filtro por trabajador - En desarrollo", Toast.LENGTH_SHORT).show()
    }
    
    private fun showWorkstationFilterDialog() {
        // TODO: Implementar selector de estaciones
        Toast.makeText(this, "Filtro por estación - En desarrollo", Toast.LENGTH_SHORT).show()
    }
    
    private fun showDateRangeDialog() {
        val calendar = Calendar.getInstance()
        
        DatePickerDialog(this, { _, year, month, day ->
            val startDate = Calendar.getInstance().apply {
                set(year, month, day, 0, 0, 0)
            }.time
            
            // Mostrar segundo picker para fecha fin
            DatePickerDialog(this, { _, endYear, endMonth, endDay ->
                val endDate = Calendar.getInstance().apply {
                    set(endYear, endMonth, endDay, 23, 59, 59)
                }.time
                
                viewModel.filterByDateRange(startDate, endDate)
                Toast.makeText(this, 
                    "Filtrado: ${dateFormat.format(startDate)} - ${dateFormat.format(endDate)}", 
                    Toast.LENGTH_SHORT).show()
                
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
            .show()
            
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
        .show()
    }
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // 🔄 ACCIONES DE ROTACIÓN
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    private fun showRotationDetails(rotation: RotationHistory) {
        val message = """
            ID: ${rotation.id}
            Trabajador: #${rotation.worker_id}
            Estación: #${rotation.workstation_id}
            Tipo: ${rotation.rotation_type}
            Inicio: ${dateFormat.format(Date(rotation.rotation_date))}
            Estado: ${if (rotation.isActive()) "Activa" else "Completada"}
            Duración: ${rotation.getCalculatedDuration()?.let { "${it}min" } ?: "N/A"}
            Score: ${rotation.performance_score?.let { "%.1f".format(it) } ?: "N/A"}
            ${if (!rotation.notes.isNullOrBlank()) "\nNotas: ${rotation.notes}" else ""}
        """.trimIndent()
        
        AlertDialog.Builder(this)
            .setTitle("Detalles de Rotación")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }
    
    private fun showFinishRotationDialog(rotation: RotationHistory) {
        AlertDialog.Builder(this)
            .setTitle("Finalizar Rotación")
            .setMessage("¿Desea finalizar esta rotación?")
            .setPositiveButton("Finalizar") { _, _ ->
                viewModel.finishRotation(rotation.id)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    
    private fun showAddScoreDialog(rotation: RotationHistory) {
        val input = TextInputEditText(this).apply {
            hint = "Score (0.0 - 10.0)"
            setText(rotation.performance_score?.toString() ?: "")
        }
        
        AlertDialog.Builder(this)
            .setTitle("Agregar Score de Rendimiento")
            .setView(input)
            .setPositiveButton("Guardar") { _, _ ->
                val scoreText = input.text.toString()
                try {
                    val score = scoreText.toDouble()
                    if (score in 0.0..10.0) {
                        viewModel.finishRotation(rotation.id, score)
                    } else {
                        Toast.makeText(this, "Score debe estar entre 0.0 y 10.0", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: NumberFormatException) {
                    Toast.makeText(this, "Score inválido", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    
    private fun showQuickActionsDialog() {
        val actions = arrayOf(
            "Finalizar todas las rotaciones activas",
            "Limpiar registros antiguos (90+ días)",
            "Generar reporte de productividad",
            "Exportar historial"
        )
        
        AlertDialog.Builder(this)
            .setTitle("Acciones Rápidas")
            .setItems(actions) { _, which ->
                when (which) {
                    0 -> showFinishAllRotationsDialog()
                    1 -> showCleanOldRecordsDialog()
                    2 -> showGenerateReportDialog()
                    3 -> Toast.makeText(this, "Exportar - En desarrollo", Toast.LENGTH_SHORT).show()
                }
            }
            .show()
    }
    
    private fun showFinishAllRotationsDialog() {
        AlertDialog.Builder(this)
            .setTitle("Finalizar Todas las Rotaciones")
            .setMessage("¿Desea finalizar todas las rotaciones activas? Esta acción no se puede deshacer.")
            .setPositiveButton("Finalizar Todas") { _, _ ->
                viewModel.finishAllActiveRotations()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    
    private fun showCleanOldRecordsDialog() {
        AlertDialog.Builder(this)
            .setTitle("Limpiar Registros Antiguos")
            .setMessage("¿Desea eliminar registros de más de 90 días? Esta acción no se puede deshacer.")
            .setPositiveButton("Limpiar") { _, _ ->
                viewModel.cleanOldRecords(90)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    
    private fun showGenerateReportDialog() {
        val calendar = Calendar.getInstance()
        val endDate = calendar.time
        calendar.add(Calendar.DAY_OF_YEAR, -30) // Últimos 30 días
        val startDate = calendar.time
        
        viewModel.generateProductivityReport(startDate, endDate)
        Toast.makeText(this, "Generando reporte de últimos 30 días...", Toast.LENGTH_SHORT).show()
    }
    
    override fun onSupportNavigateUp(): Boolean {
        closeDetails()
        return true
    }
    
    /**
     * Configura las animaciones de entrada para los elementos de la UI
     */
    private fun setupAnimations() {
        // Animar las cards principales con stagger effect
        val mainCards = listOf(
            findViewById<View>(R.id.tvTotalRotations).parent.parent as View, // Métricas card
            findViewById<View>(R.id.btnFilterByWorker).parent.parent as View, // Filtros card
            findViewById<View>(R.id.recyclerViewHistory).parent as View // Historial card
        )
        
        AnimationManager.staggeredListAnimation(
            views = mainCards,
            animationType = AnimationManager.StaggerType.SLIDE_IN_FROM_BOTTOM,
            baseDuration = AnimationManager.DURATION_MEDIUM,
            staggerDelay = AnimationManager.DELAY_LONG
        )
        
        // Animar el FAB con entrada espectacular
        AnimationManager.spectacularEntrance(
            view = fabQuickActions,
            duration = AnimationManager.DURATION_LONG,
            delay = AnimationManager.DELAY_LONG * 3
        )
    }
}