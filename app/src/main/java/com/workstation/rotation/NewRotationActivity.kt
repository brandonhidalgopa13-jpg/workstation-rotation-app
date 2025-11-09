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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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

    private lateinit var binding: com.workstation.rotation.databinding.ActivityNewRotationV3Binding
    private lateinit var rotationService: NewRotationService
    private lateinit var viewModel: NewRotationViewModel
    
    private lateinit var rotation1Adapter: com.workstation.rotation.adapters.RotationStationColumnAdapter
    private lateinit var rotation2Adapter: com.workstation.rotation.adapters.RotationStationColumnAdapter
    private var currentSessionId: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            binding = com.workstation.rotation.databinding.ActivityNewRotationV3Binding.inflate(layoutInflater)
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
        // Configurar adapter para Rotación 1 (ACTUAL)
        rotation1Adapter = com.workstation.rotation.adapters.RotationStationColumnAdapter(
            onWorkerClick = { workerId, workstationId ->
                handleWorkerClick(workerId, workstationId, "CURRENT")
            }
        )
        
        // Configurar RecyclerView optimizado para 100+ estaciones
        binding.recyclerRotation1.apply {
            layoutManager = LinearLayoutManager(
                this@NewRotationActivity,
                LinearLayoutManager.HORIZONTAL,
                false
            )
            adapter = rotation1Adapter
            
            // Optimizaciones para grandes volúmenes
            setHasFixedSize(false) // Permitir tamaño dinámico
            setItemViewCacheSize(20) // Cache de 20 items
            recycledViewPool.setMaxRecycledViews(0, 30) // Pool de 30 vistas
            
            // Habilitar drawing cache para scroll suave
            isDrawingCacheEnabled = true
            drawingCacheQuality = View.DRAWING_CACHE_QUALITY_HIGH
            
            // Forzar medición después de actualizar datos
            post {
                requestLayout()
            }
        }
        
        // Configurar adapter para Rotación 2 (SIGUIENTE)
        rotation2Adapter = com.workstation.rotation.adapters.RotationStationColumnAdapter(
            onWorkerClick = { workerId, workstationId ->
                handleWorkerClick(workerId, workstationId, "NEXT")
            }
        )
        
        // Configurar RecyclerView optimizado para 100+ estaciones
        binding.recyclerRotation2.apply {
            layoutManager = LinearLayoutManager(
                this@NewRotationActivity,
                LinearLayoutManager.HORIZONTAL,
                false
            )
            adapter = rotation2Adapter
            
            // Optimizaciones para grandes volúmenes
            setHasFixedSize(false) // Permitir tamaño dinámico
            setItemViewCacheSize(20) // Cache de 20 items
            recycledViewPool.setMaxRecycledViews(0, 30) // Pool de 30 vistas
            
            // Habilitar drawing cache para scroll suave
            isDrawingCacheEnabled = true
            drawingCacheQuality = View.DRAWING_CACHE_QUALITY_HIGH
            
            // Forzar medición después de actualizar datos
            post {
                requestLayout()
            }
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
        // Información de sesión se muestra en el toolbar
        supportActionBar?.subtitle = session?.name ?: "Sin sesión activa"
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
            
            // Actualizar Rotación 1 (ACTUAL)
            rotation1Adapter.submitList(grid.rows, "CURRENT")
            android.util.Log.d("NewRotationActivity", "✅ Rotación 1 actualizada con ${grid.rows.size} estaciones")
            
            // Actualizar Rotación 2 (SIGUIENTE)
            rotation2Adapter.submitList(grid.rows, "NEXT")
            android.util.Log.d("NewRotationActivity", "✅ Rotación 2 actualizada con ${grid.rows.size} estaciones")
        } else {
            android.util.Log.w("NewRotationActivity", "⚠️ Grid es NULL - no hay datos para mostrar")
        }
        
        android.util.Log.d("NewRotationActivity", "═══════════════════════════════════════════════════════")
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
        lifecycleScope.launch {
            try {
                // Mostrar loading
                binding.loadingOverlay.visibility = View.VISIBLE
                binding.tvLoadingMessage.text = "Capturando rotaciones completas..."
                
                // Dar tiempo para que se renderice todo
                kotlinx.coroutines.delay(300)
                
                // Obtener ambas rotaciones
                val rotation1Card = binding.cardRotation1
                val rotation2Card = binding.cardRotation2
                val recycler1 = binding.recyclerRotation1
                val recycler2 = binding.recyclerRotation2
                val scroll1 = binding.horizontalScrollRotation1
                val scroll2 = binding.horizontalScrollRotation2
                
                // Forzar medición y layout
                recycler1.measure(
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                )
                recycler2.measure(
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                )
                
                // Calcular dimensiones reales del contenido
                val width1 = maxOf(recycler1.measuredWidth, recycler1.computeHorizontalScrollRange(), 1200)
                val width2 = maxOf(recycler2.measuredWidth, recycler2.computeHorizontalScrollRange(), 1200)
                val maxWidth = maxOf(width1, width2)
                
                val height1 = maxOf(recycler1.measuredHeight, 600)
                val height2 = maxOf(recycler2.measuredHeight, 600)
                
                val headerHeight = 250
                val spacing = 80
                val totalHeight = headerHeight + height1 + spacing + height2 + 150
                
                android.util.Log.d("CapturePhoto", "Dimensiones calculadas:")
                android.util.Log.d("CapturePhoto", "  Width1: $width1, Width2: $width2, MaxWidth: $maxWidth")
                android.util.Log.d("CapturePhoto", "  Height1: $height1, Height2: $height2, TotalHeight: $totalHeight")
                
                // Crear bitmap grande
                val bitmap = android.graphics.Bitmap.createBitmap(
                    maxWidth,
                    totalHeight,
                    android.graphics.Bitmap.Config.ARGB_8888
                )
                
                val canvas = android.graphics.Canvas(bitmap)
                
                // Guardar estados originales
                val originalScroll1X = scroll1.scrollX
                val originalScroll2X = scroll2.scrollX
                
                // Fondo blanco
                canvas.drawColor(android.graphics.Color.WHITE)
                
                // Estilos de texto
                val titlePaint = android.graphics.Paint().apply {
                    textSize = 64f
                    color = android.graphics.Color.BLACK
                    typeface = android.graphics.Typeface.DEFAULT_BOLD
                    isAntiAlias = true
                }
                
                val datePaint = android.graphics.Paint().apply {
                    textSize = 40f
                    color = android.graphics.Color.GRAY
                    isAntiAlias = true
                }
                
                val labelPaint = android.graphics.Paint().apply {
                    textSize = 48f
                    color = android.graphics.Color.parseColor("#FF9800")
                    typeface = android.graphics.Typeface.DEFAULT_BOLD
                    isAntiAlias = true
                }
                
                // Título
                val title = "Sistema de Rotación - Vista Completa"
                val date = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
                
                canvas.drawText(title, 50f, 90f, titlePaint)
                canvas.drawText("Fecha: $date", 50f, 160f, datePaint)
                
                var currentY = headerHeight.toFloat()
                
                // ===== CAPTURAR ROTACIÓN 1 =====
                canvas.save()
                canvas.translate(0f, currentY)
                canvas.drawText("ROTACIÓN 1 - ACTUAL", 50f, 50f, labelPaint)
                canvas.translate(0f, 80f)
                
                // Capturar todo el ancho
                scroll1.scrollTo(0, 0)
                kotlinx.coroutines.delay(100)
                
                // Crear bitmap temporal para la rotación 1
                val rot1Bitmap = android.graphics.Bitmap.createBitmap(
                    width1,
                    height1,
                    android.graphics.Bitmap.Config.ARGB_8888
                )
                val rot1Canvas = android.graphics.Canvas(rot1Bitmap)
                
                // Capturar en secciones
                var capturedWidth = 0
                val sectionWidth = scroll1.width
                while (capturedWidth < width1) {
                    scroll1.scrollTo(capturedWidth, 0)
                    kotlinx.coroutines.delay(50)
                    
                    rot1Canvas.save()
                    rot1Canvas.translate(-capturedWidth.toFloat(), 0f)
                    recycler1.draw(rot1Canvas)
                    rot1Canvas.restore()
                    
                    capturedWidth += sectionWidth
                }
                
                // Dibujar bitmap completo de rotación 1
                canvas.drawBitmap(rot1Bitmap, 0f, 0f, null)
                rot1Bitmap.recycle()
                
                canvas.restore()
                currentY += height1 + spacing
                
                // ===== CAPTURAR ROTACIÓN 2 =====
                canvas.save()
                canvas.translate(0f, currentY)
                canvas.drawText("ROTACIÓN 2 - SIGUIENTE", 50f, 50f, labelPaint)
                canvas.translate(0f, 80f)
                
                // Capturar todo el ancho
                scroll2.scrollTo(0, 0)
                kotlinx.coroutines.delay(100)
                
                // Crear bitmap temporal para la rotación 2
                val rot2Bitmap = android.graphics.Bitmap.createBitmap(
                    width2,
                    height2,
                    android.graphics.Bitmap.Config.ARGB_8888
                )
                val rot2Canvas = android.graphics.Canvas(rot2Bitmap)
                
                // Capturar en secciones
                capturedWidth = 0
                while (capturedWidth < width2) {
                    scroll2.scrollTo(capturedWidth, 0)
                    kotlinx.coroutines.delay(50)
                    
                    rot2Canvas.save()
                    rot2Canvas.translate(-capturedWidth.toFloat(), 0f)
                    recycler2.draw(rot2Canvas)
                    rot2Canvas.restore()
                    
                    capturedWidth += sectionWidth
                }
                
                // Dibujar bitmap completo de rotación 2
                canvas.drawBitmap(rot2Bitmap, 0f, 0f, null)
                rot2Bitmap.recycle()
                
                canvas.restore()
                
                // Restaurar scrolls
                scroll1.scrollTo(originalScroll1X, 0)
                scroll2.scrollTo(originalScroll2X, 0)
                
                // Guardar imagen
                val timestamp = System.currentTimeMillis()
                val fileName = "Rotacion_Completa_$timestamp"
                
                val savedUri = withContext(Dispatchers.IO) {
                    android.provider.MediaStore.Images.Media.insertImage(
                        contentResolver,
                        bitmap,
                        fileName,
                        "Captura completa: Rotación 1 y 2 con todas las estaciones y trabajadores - $date"
                    )
                }
                
                binding.loadingOverlay.visibility = View.GONE
                
                if (savedUri != null) {
                    Snackbar.make(binding.root, "✅ Foto guardada: Ambas rotaciones completas", Snackbar.LENGTH_LONG)
                        .setAction("Ver") {
                            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                                data = android.net.Uri.parse(savedUri)
                                type = "image/*"
                            }
                            startActivity(intent)
                        }
                        .setActionTextColor(getColor(R.color.accent))
                        .show()
                        
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        showSharePhotoDialog(savedUri)
                    }, 2000)
                } else {
                    Snackbar.make(binding.root, "❌ Error al guardar la foto", Snackbar.LENGTH_LONG)
                        .setBackgroundTint(getColor(R.color.error))
                        .show()
                }
                
                bitmap.recycle()
                
            } catch (e: Exception) {
                e.printStackTrace()
                binding.loadingOverlay.visibility = View.GONE
                Snackbar.make(binding.root, "❌ Error: ${e.message}", Snackbar.LENGTH_LONG)
                    .setBackgroundTint(getColor(R.color.error))
                    .show()
            }
        }
    }
    
    /**
     * Muestra diálogo para compartir la foto capturada
     */
    private fun showSharePhotoDialog(imageUri: String) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Foto Capturada")
            .setMessage("¿Deseas compartir la imagen de las rotaciones?")
            .setPositiveButton("Compartir") { _, _ ->
                val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                    type = "image/*"
                    putExtra(android.content.Intent.EXTRA_STREAM, android.net.Uri.parse(imageUri))
                    putExtra(android.content.Intent.EXTRA_TEXT, "Rotaciones del sistema - ${java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(java.util.Date())}")
                }
                startActivity(android.content.Intent.createChooser(shareIntent, "Compartir rotación"))
            }
            .setNegativeButton("Cerrar", null)
            .show()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        // Aplicar animación de salida
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}