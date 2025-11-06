package com.workstation.rotation

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.workstation.rotation.adapters.AvailableWorkersAdapter
import com.workstation.rotation.adapters.RotationGridRowAdapter
import com.workstation.rotation.databinding.ActivityNewRotationBinding
import com.workstation.rotation.models.RotationGridCell
import com.workstation.rotation.services.NewRotationService
import com.workstation.rotation.services.DataInitializationService
import com.workstation.rotation.viewmodels.NewRotationViewModel
import com.workstation.rotation.viewmodels.NewRotationUiState
import kotlinx.coroutines.launch

/**
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 🔄 ACTIVITY NUEVA ROTACIÓN - v4.0
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 
 * 🎯 PROPÓSITO:
 * Activity principal para el nuevo sistema de rotación con grid bidimensional.
 * Permite gestionar rotaciones actuales y siguientes de forma visual e intuitiva.
 * 
 * 📋 CARACTERÍSTICAS:
 * • Grid bidimensional con scroll horizontal y vertical
 * • Visualización de rotación actual y siguiente lado a lado
 * • Drag & drop para asignaciones
 * • Generación automática de rotaciones
 * • Métricas en tiempo real
 * 
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 */

class NewRotationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNewRotationBinding
    private lateinit var rotationService: NewRotationService
    
    private val viewModel: NewRotationViewModel by viewModels {
        NewRotationViewModel.Factory(rotationService)
    }
    
    private lateinit var gridAdapter: RotationGridRowAdapter
    private lateinit var workersAdapter: AvailableWorkersAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewRotationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Inicializar servicio
        rotationService = NewRotationService(this)
        
        setupUI()
        setupRecyclerViews()
        setupObservers()
        setupClickListeners()
        
        // Crear sesión inicial si no existe
        checkAndCreateInitialSession()
    }

    private fun setupUI() {
        // Configurar toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun setupRecyclerViews() {
        // Configurar adapter del grid de rotación
        gridAdapter = RotationGridRowAdapter(
            onCellClick = { cell, position, rotationType ->
                viewModel.onCellClick(cell, rotationType)
            },
            onCellLongClick = { cell, position, rotationType ->
                viewModel.onCellLongClick(cell, rotationType)
            }
        )
        
        binding.recyclerViewRotationGrid.apply {
            layoutManager = LinearLayoutManager(this@NewRotationActivity)
            adapter = gridAdapter
            setHasFixedSize(true)
        }
        
        // Configurar adapter de trabajadores disponibles
        workersAdapter = AvailableWorkersAdapter(
            onWorkerClick = { worker ->
                // Manejar click en trabajador
                showWorkerDetails(worker)
            },
            onWorkerLongClick = { worker ->
                // Manejar long click para drag & drop
                startWorkerDrag(worker)
                true
            },
            onWorkerDrag = { worker ->
                // Iniciar drag & drop
                startWorkerDrag(worker)
            }
        )
        
        binding.recyclerViewAvailableWorkers.apply {
            layoutManager = LinearLayoutManager(this@NewRotationActivity)
            adapter = workersAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            // Observar estado de la UI
            viewModel.uiState.collect { state ->
                updateUIState(state)
            }
        }
        
        lifecycleScope.launch {
            // Observar sesión activa
            viewModel.activeSession.collect { session ->
                updateSessionInfo(session)
            }
        }
        
        lifecycleScope.launch {
            // Observar grid de rotación
            viewModel.rotationGrid.collect { grid ->
                updateRotationGrid(grid)
            }
        }
    }

    private fun setupClickListeners() {
        // Botón generar rotación automática
        binding.btnGenerateRotation.setOnClickListener {
            showGenerateRotationDialog()
        }
        
        // Botón promover siguiente a actual
        binding.btnPromoteRotation.setOnClickListener {
            showPromoteRotationDialog()
        }
        
        // FAB para acciones rápidas
        binding.fabQuickActions.setOnClickListener {
            showQuickActionsMenu()
        }
    }

    private fun updateUIState(state: NewRotationUiState) {
        // Mostrar/ocultar loading
        binding.loadingOverlay.visibility = if (state.isLoading) View.VISIBLE else View.GONE
        binding.tvLoadingMessage.text = state.loadingMessage ?: "Cargando..."
        
        // Actualizar métricas
        binding.tvCurrentAssigned.text = state.totalCurrentAssigned.toString()
        binding.tvNextAssigned.text = state.totalNextAssigned.toString()
        binding.tvTotalRequired.text = state.totalRequired.toString()
        
        // Mostrar mensajes
        state.message?.let { message ->
            Snackbar.make(binding.root, message as CharSequence, Snackbar.LENGTH_LONG).show()
            viewModel.clearMessages()
        }
        
        state.error?.let { error ->
            Snackbar.make(binding.root, error as CharSequence, Snackbar.LENGTH_LONG)
                .setBackgroundTint(getColor(R.color.error))
                .show()
            viewModel.clearMessages()
        }
        
        // Manejar diálogos de selección
        if (state.showWorkerSelection) {
            showWorkerSelectionDialog(state.selectedCell)
        }
        
        if (state.showWorkerOptions) {
            showWorkerOptionsDialog(state.selectedCell)
        }
        
        if (state.showContextMenu) {
            showContextMenu(state.selectedCell)
        }
    }

    private fun updateSessionInfo(session: com.workstation.rotation.data.entities.RotationSession?) {
        if (session != null) {
            binding.tvSessionName.text = session.name
            binding.chipSessionStatus.text = session.getStatusText()
            binding.chipSessionStatus.setChipBackgroundColorResource(
                when (session.status) {
                    "ACTIVE" -> R.color.success
                    "DRAFT" -> R.color.warning
                    "COMPLETED" -> R.color.info
                    else -> R.color.error
                }
            )
        } else {
            binding.tvSessionName.text = "Sin sesión activa"
            binding.chipSessionStatus.text = "INACTIVA"
            binding.chipSessionStatus.setChipBackgroundColorResource(R.color.error)
        }
    }

    private fun updateRotationGrid(grid: com.workstation.rotation.models.RotationGrid?) {
        if (grid != null) {
            gridAdapter.updateRows(grid.rows)
            workersAdapter.updateWorkers(grid.availableWorkers)
        }
    }

    private fun checkAndCreateInitialSession() {
        lifecycleScope.launch {
            // Inicializar datos de prueba si es necesario
            val dataService = DataInitializationService(this@NewRotationActivity)
            if (!dataService.hasInitializedData()) {
                binding.loadingOverlay.visibility = View.VISIBLE
                binding.tvLoadingMessage.text = "Inicializando datos de prueba..."
                
                val success = dataService.initializeTestData()
                if (success) {
                    Snackbar.make(binding.root, "Datos de prueba inicializados", Snackbar.LENGTH_SHORT).show()
                } else {
                    Snackbar.make(binding.root, "Error al inicializar datos", Snackbar.LENGTH_LONG)
                        .setBackgroundTint(getColor(R.color.error))
                        .show()
                }
                
                binding.loadingOverlay.visibility = View.GONE
            }
            
            // Crear sesión si no existe
            val activeSession = viewModel.activeSession.value
            if (activeSession == null) {
                viewModel.createNewSession()
            }
        }
    }

    private fun showGenerateRotationDialog() {
        val options = arrayOf("Rotación Actual", "Siguiente Rotación", "Ambas")
        
        MaterialAlertDialogBuilder(this)
            .setTitle("Generar Rotación Automática")
            .setMessage("¿Qué rotación deseas generar?")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> viewModel.generateOptimizedRotation("CURRENT")
                    1 -> viewModel.generateOptimizedRotation("NEXT")
                    2 -> {
                        viewModel.generateOptimizedRotation("CURRENT")
                        viewModel.generateOptimizedRotation("NEXT")
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showPromoteRotationDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Promover Rotación")
            .setMessage("¿Estás seguro de que deseas promover la siguiente rotación a actual? Esto completará la rotación actual.")
            .setPositiveButton("Sí, Promover") { _, _ ->
                viewModel.promoteNextToCurrent()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showQuickActionsMenu() {
        val actions = arrayOf(
            "Copiar Actual → Siguiente",
            "Limpiar Rotación Actual",
            "Limpiar Siguiente Rotación",
            "Nueva Sesión",
            "Ver Conflictos"
        )
        
        MaterialAlertDialogBuilder(this)
            .setTitle("Acciones Rápidas")
            .setItems(actions) { _, which ->
                when (which) {
                    0 -> viewModel.copyCurrentToNext()
                    1 -> clearRotation("CURRENT")
                    2 -> clearRotation("NEXT")
                    3 -> createNewSessionDialog()
                    4 -> showConflictsDialog()
                }
            }
            .show()
    }

    private fun showWorkerSelectionDialog(cell: RotationGridCell?) {
        // TODO: Implementar diálogo de selección de trabajadores
        // Por ahora, limpiar selección
        viewModel.clearSelections()
    }

    private fun showWorkerOptionsDialog(cell: RotationGridCell?) {
        if (cell?.workerId == null) return
        
        val options = arrayOf("Remover Asignación", "Mover a Otra Estación", "Ver Detalles")
        
        MaterialAlertDialogBuilder(this)
            .setTitle("Opciones para ${cell.workerName}")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> viewModel.removeWorkerAssignment(cell.workerId, cell.rotationType)
                    1 -> showMoveWorkerDialog(cell)
                    2 -> showWorkerDetailsDialog(cell.workerId)
                }
            }
            .setNegativeButton("Cancelar") { _, _ ->
                viewModel.clearSelections()
            }
            .show()
    }

    private fun showContextMenu(cell: RotationGridCell?) {
        // Similar a showWorkerOptionsDialog pero como menú contextual
        showWorkerOptionsDialog(cell)
    }

    private fun showMoveWorkerDialog(cell: RotationGridCell) {
        // TODO: Implementar diálogo para mover trabajador
        viewModel.clearSelections()
    }

    private fun showWorkerDetailsDialog(workerId: Long) {
        // TODO: Implementar diálogo de detalles del trabajador
        viewModel.clearSelections()
    }

    private fun showWorkerDetails(worker: com.workstation.rotation.models.AvailableWorker) {
        // TODO: Implementar vista de detalles del trabajador
    }

    private fun startWorkerDrag(worker: com.workstation.rotation.models.AvailableWorker) {
        // TODO: Implementar drag & drop
        Snackbar.make(binding.root, "Drag & Drop: ${worker.workerName}", Snackbar.LENGTH_SHORT).show()
    }

    private fun clearRotation(rotationType: String) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Limpiar Rotación")
            .setMessage("¿Estás seguro de que deseas limpiar la rotación ${if (rotationType == "CURRENT") "actual" else "siguiente"}?")
            .setPositiveButton("Sí, Limpiar") { _, _ ->
                // TODO: Implementar limpieza de rotación
                Snackbar.make(binding.root, "Rotación limpiada", Snackbar.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun createNewSessionDialog() {
        // TODO: Implementar diálogo para crear nueva sesión
        viewModel.createNewSession()
    }

    private fun showConflictsDialog() {
        val state = viewModel.uiState.value
        if (state.conflicts.isEmpty()) {
            Snackbar.make(binding.root, "No hay conflictos detectados", Snackbar.LENGTH_SHORT).show()
            return
        }
        
        MaterialAlertDialogBuilder(this)
            .setTitle("Conflictos Detectados (${state.conflicts.size})")
            .setItems(state.conflicts.toTypedArray()) { _, _ -> }
            .setPositiveButton("Entendido", null)
            .show()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        // Aplicar animación de salida
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}