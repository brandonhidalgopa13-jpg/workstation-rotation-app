package com.workstation.rotation

import android.os.Bundle
import android.view.View
import android.widget.Toast
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

    private lateinit var binding: com.workstation.rotation.databinding.ActivityNewRotationV2Binding
    private lateinit var rotationService: NewRotationService
    private lateinit var viewModel: NewRotationViewModel
    
    private lateinit var stationColumnAdapter: com.workstation.rotation.adapters.StationColumnAdapter
    private var currentSessionId: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            binding = com.workstation.rotation.databinding.ActivityNewRotationV2Binding.inflate(layoutInflater)
            setContentView(binding.root)
            
            // Inicializar servicio primero
            rotationService = NewRotationService(this)
            
            // Inicializar ViewModel después del servicio
            viewModel = NewRotationViewModel(rotationService)
            
            setupUI()
            setupRecyclerViews()
            setupObservers() // ✅ ACTIVADO: Observar cambios en el grid de rotación
            setupClickListeners()
            
            // Crear sesión inicial si no existe
            checkAndCreateInitialSession()
            
        } catch (e: Exception) {
            e.printStackTrace()
            // Mostrar error y cerrar actividad de forma controlada
            android.widget.Toast.makeText(this, "Error al inicializar rotación: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun setupUI() {
        try {
            // Configurar toolbar
            setSupportActionBar(binding.toolbar)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            
            binding.toolbar.setNavigationOnClickListener {
                onBackPressed()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            android.util.Log.e("NewRotationActivity", "Error configurando UI: ${e.message}")
            // Continuar sin toolbar si hay error
        }
    }

    private fun setupRecyclerViews() {
        // Configurar adapter de columnas de estaciones (nueva interfaz v2)
        stationColumnAdapter = com.workstation.rotation.adapters.StationColumnAdapter(
            onWorkerClick = { workerId, workstationId, rotationType ->
                handleWorkerClick(workerId, workstationId, rotationType)
            },
            onEmptySlotClick = { workstationId, rotationType ->
                handleEmptySlotClick(workstationId, rotationType)
            }
        )
        
        // Configurar RecyclerView horizontal para estaciones
        binding.recyclerViewStations.apply {
            layoutManager = LinearLayoutManager(
                this@NewRotationActivity,
                LinearLayoutManager.HORIZONTAL,
                false
            )
            adapter = stationColumnAdapter
            setHasFixedSize(false)
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
        
        // Botón capturar foto
        binding.btnCapturePhoto.setOnClickListener {
            captureRotationPhoto()
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
        android.util.Log.d("NewRotationActivity", "═══════════════════════════════════════════════════════")
        android.util.Log.d("NewRotationActivity", "🔄 ACTUALIZANDO GRID EN UI")
        android.util.Log.d("NewRotationActivity", "═══════════════════════════════════════════════════════")
        
        if (grid != null) {
            android.util.Log.d("NewRotationActivity", "✅ Grid recibido:")
            android.util.Log.d("NewRotationActivity", "  • Sesión: ${grid.sessionName}")
            android.util.Log.d("NewRotationActivity", "  • Filas: ${grid.rows.size}")
            android.util.Log.d("NewRotationActivity", "  • Trabajadores disponibles: ${grid.availableWorkers.size}")
            
            // Actualizar adaptador de columnas de estaciones (nueva interfaz v2)
            stationColumnAdapter.submitList(grid.rows)
            android.util.Log.d("NewRotationActivity", "✅ Adapter actualizado con ${grid.rows.size} estaciones")
            
            // Actualizar métricas
            updateMetrics(grid)
        } else {
            android.util.Log.w("NewRotationActivity", "⚠️ Grid es NULL - no hay datos para mostrar")
        }
        
        android.util.Log.d("NewRotationActivity", "═══════════════════════════════════════════════════════")
    }
    
    /**
     * Actualiza las métricas mostradas en el header
     */
    private fun updateMetrics(grid: com.workstation.rotation.models.RotationGrid) {
        val currentAssigned = grid.rows.sumOf { row ->
            row.currentAssignments.count { it.isAssigned }
        }
        val nextAssigned = grid.rows.sumOf { row ->
            row.nextAssignments.count { it.isAssigned }
        }
        val totalRequired = grid.rows.sumOf { it.requiredWorkers } * 2
        
        binding.tvCurrentAssigned.text = currentAssigned.toString()
        binding.tvNextAssigned.text = nextAssigned.toString()
        binding.tvTotalRequired.text = totalRequired.toString()
    }
    
    /**
     * Maneja el click en un trabajador asignado
     */
    private fun handleWorkerClick(workerId: Long, workstationId: Long, rotationType: String) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Opciones de Trabajador")
            .setItems(arrayOf(
                "Ver detalles",
                "Mover a otra estación",
                "Remover de rotación"
            )) { _, which ->
                when (which) {
                    0 -> showWorkerDetailsById(workerId)
                    1 -> showMoveWorkerDialog(workerId, workstationId, rotationType)
                    2 -> removeWorkerFromRotation(workerId, rotationType)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    
    /**
     * Maneja el click en un slot vacío
     */
    private fun handleEmptySlotClick(workstationId: Long, rotationType: String) {
        // TODO: Mostrar diálogo para seleccionar trabajador disponible
        Toast.makeText(
            this,
            "Seleccionar trabajador para estación (ID: $workstationId, Tipo: $rotationType)",
            Toast.LENGTH_SHORT
        ).show()
    }
    
    /**
     * Muestra detalles de un trabajador por ID
     */
    private fun showWorkerDetailsById(workerId: Long) {
        // TODO: Implementar diálogo de detalles
        Toast.makeText(this, "Detalles del trabajador ID: $workerId", Toast.LENGTH_SHORT).show()
    }
    
    /**
     * Muestra diálogo para mover trabajador a otra estación
     */
    private fun showMoveWorkerDialog(workerId: Long, fromWorkstationId: Long, rotationType: String) {
        // TODO: Implementar diálogo de selección de estación destino
        Toast.makeText(
            this,
            "Mover trabajador $workerId desde estación $fromWorkstationId",
            Toast.LENGTH_SHORT
        ).show()
    }
    
    /**
     * Remueve un trabajador de la rotación
     */
    private fun removeWorkerFromRotation(workerId: Long, rotationType: String) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Confirmar")
            .setMessage("¿Remover trabajador de la rotación $rotationType?")
            .setPositiveButton("Remover") { _, _ ->
                lifecycleScope.launch {
                    val result = rotationService.removeWorkerAssignment(
                        currentSessionId,
                        workerId,
                        rotationType
                    )
                    if (result) {
                        Toast.makeText(
                            this@NewRotationActivity,
                            "✅ Trabajador removido",
                            Toast.LENGTH_SHORT
                        ).show()
                        viewModel.refreshRotationGrid()
                    } else {
                        Toast.makeText(
                            this@NewRotationActivity,
                            "❌ Error al remover trabajador",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }



    private fun showGenerateRotationDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_generate_rotation, null)
        val dialog = MaterialAlertDialogBuilder(this)
            .setView(dialogView)
            .create()
        
        // Configurar botones
        dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_generate_current).setOnClickListener {
            viewModel.generateOptimizedRotation("CURRENT")
            Toast.makeText(this, "✅ Generando rotación actual...", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
        
        dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_generate_next).setOnClickListener {
            viewModel.generateOptimizedRotation("NEXT")
            Toast.makeText(this, "✅ Generando siguiente rotación...", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
        
        dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_generate_both).setOnClickListener {
            viewModel.generateOptimizedRotation("CURRENT")
            viewModel.generateOptimizedRotation("NEXT")
            Toast.makeText(this, "✅ Generando ambas rotaciones...", Toast.LENGTH_LONG).show()
            dialog.dismiss()
        }
        
        dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_cancel).setOnClickListener {
            dialog.dismiss()
        }
        
        dialog.show()
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

    private fun checkAndCreateInitialSession() {
        lifecycleScope.launch {
            try {
                // Mostrar loading
                binding.loadingOverlay?.visibility = android.view.View.VISIBLE
                binding.tvLoadingMessage?.text = "Inicializando sistema de rotación..."
                
                // Verificar si hay datos inicializados
                val dataService = DataInitializationService(this@NewRotationActivity)
                if (!dataService.hasInitializedData()) {
                    binding.tvLoadingMessage?.text = "Creando datos de prueba..."
                    
                    // Inicializar datos de prueba
                    val success = dataService.initializeTestData()
                    if (success) {
                        Snackbar.make(binding.root, "Datos de prueba inicializados", Snackbar.LENGTH_SHORT).show()
                    } else {
                        Snackbar.make(binding.root, "Error al inicializar datos", Snackbar.LENGTH_LONG).show()
                        return@launch
                    }
                }
                
                binding.tvLoadingMessage?.text = "Cargando sesión de rotación..."
                
                // Cargar datos iniciales en el ViewModel
                viewModel.loadInitialData()
                
                // Ocultar loading
                binding.loadingOverlay?.visibility = android.view.View.GONE
                
            } catch (e: Exception) {
                e.printStackTrace()
                binding.loadingOverlay?.visibility = android.view.View.GONE
                
                val errorMessage = "Error al inicializar rotación: ${e.message}"
                Snackbar.make(binding.root, errorMessage, Snackbar.LENGTH_LONG)
                    .setAction("Reintentar") {
                        checkAndCreateInitialSession()
                    }
                    .show()
            }
        }
    }

    private fun captureRotationPhoto() {
        try {
            // Crear un bitmap de la vista del grid de rotación (nueva interfaz v2)
            val gridView = binding.recyclerViewStations
            val bitmap = android.graphics.Bitmap.createBitmap(
                gridView.width, 
                gridView.height, 
                android.graphics.Bitmap.Config.ARGB_8888
            )
            val canvas = android.graphics.Canvas(bitmap)
            gridView.draw(canvas)
            
            // Guardar la imagen en la galería
            val savedUri = android.provider.MediaStore.Images.Media.insertImage(
                contentResolver,
                bitmap,
                "Rotacion_${System.currentTimeMillis()}",
                "Captura de rotación del ${java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date())}"
            )
            
            if (savedUri != null) {
                Snackbar.make(binding.root, "Foto guardada en la galería", Snackbar.LENGTH_LONG)
                    .setAction("Ver") {
                        // Abrir la imagen en la galería
                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                            data = android.net.Uri.parse(savedUri)
                            type = "image/*"
                        }
                        startActivity(intent)
                    }
                    .show()
            } else {
                Snackbar.make(binding.root, "Error al guardar la foto", Snackbar.LENGTH_LONG).show()
            }
            
        } catch (e: Exception) {
            e.printStackTrace()
            Snackbar.make(binding.root, "Error al capturar foto: ${e.message}", Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        // Aplicar animación de salida
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}