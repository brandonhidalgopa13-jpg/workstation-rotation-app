package com.workstation.rotation

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.workstation.rotation.utils.ImageUtils
import java.text.SimpleDateFormat
import java.util.*
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
        
        // Mostrar botón de descarga
        showDownloadButton()
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
     * Creates the header for a workstation column with enhanced design.
     */
    private fun createWorkstationHeader(column: WorkstationColumn, layoutParams: LinearLayout.LayoutParams) {
        val headerLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(8, 12, 8, 12)
            
            // Fondo con gradiente
            background = ContextCompat.getDrawable(this@RotationActivity, R.drawable.gradient_primary)
            this.layoutParams = layoutParams
        }
        
        // Nombre de la estación
        val nameView = TextView(this).apply {
            text = column.workstation.name
            textSize = 14f
            setTextColor(ContextCompat.getColor(this@RotationActivity, R.color.white))
            gravity = android.view.Gravity.CENTER
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(4, 4, 4, 2)
        }
        headerLayout.addView(nameView)
        
        // Indicador de prioridad
        if (column.workstation.isPriority) {
            val priorityView = TextView(this).apply {
                text = "⭐ PRIORITARIA"
                textSize = 10f
                setTextColor(ContextCompat.getColor(this@RotationActivity, R.color.priority_gold))
                gravity = android.view.Gravity.CENTER
                setTypeface(null, android.graphics.Typeface.BOLD)
                setPadding(4, 2, 4, 4)
            }
            headerLayout.addView(priorityView)
        }
        
        binding.layoutWorkstationHeaders.addView(headerLayout)
    }
    
    /**
     * Creates the capacity requirement display for a workstation with visual indicators.
     */
    private fun createCapacityRequirement(column: WorkstationColumn, layoutParams: LinearLayout.LayoutParams) {
        val currentCount = column.currentWorkers.size
        val requiredCount = column.workstation.requiredWorkers
        val isFullyStaffed = currentCount >= requiredCount
        
        val capacityLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(8, 8, 8, 8)
            this.layoutParams = layoutParams
        }
        
        // Indicador de capacidad
        val capacityView = TextView(this).apply {
            text = "$currentCount/$requiredCount"
            textSize = 14f
            setTextColor(ContextCompat.getColor(this@RotationActivity, R.color.white))
            gravity = android.view.Gravity.CENTER
            setPadding(8, 6, 8, 6)
            setTypeface(null, android.graphics.Typeface.BOLD)
            
            val bgColor = if (isFullyStaffed) R.color.status_success else R.color.status_warning
            background = ContextCompat.getDrawable(this@RotationActivity, R.drawable.status_badge_green)
            backgroundTintList = ContextCompat.getColorStateList(this@RotationActivity, bgColor)
        }
        capacityLayout.addView(capacityView)
        
        // Estado de la estación
        val statusView = TextView(this).apply {
            text = if (isFullyStaffed) "✅ COMPLETA" else "⚠️ INCOMPLETA"
            textSize = 9f
            setTextColor(if (isFullyStaffed) 
                ContextCompat.getColor(this@RotationActivity, R.color.status_success) 
                else ContextCompat.getColor(this@RotationActivity, R.color.status_warning))
            gravity = android.view.Gravity.CENTER
            setPadding(4, 2, 4, 2)
            setTypeface(null, android.graphics.Typeface.BOLD)
        }
        capacityLayout.addView(statusView)
        
        binding.layoutCapacityRequirements.addView(capacityLayout)
    }
    
    /**
     * Creates a workstation header view.
     */
    private fun createWorkstationHeader(workstation: Workstation, layoutParams: LinearLayout.LayoutParams): TextView {
        return TextView(this).apply {
            text = workstation.name
            textSize = 14f
            setTextColor(ContextCompat.getColor(this@RotationActivity, R.color.white))
            gravity = android.view.Gravity.CENTER
            setPadding(8, 12, 8, 12)
            setTypeface(null, android.graphics.Typeface.BOLD)
            this.layoutParams = layoutParams
            
            val bgColor = if (workstation.isPriority) R.color.priority_header else R.color.normal_header
            background = ContextCompat.getDrawable(this@RotationActivity, R.drawable.gradient_primary)
            backgroundTintList = ContextCompat.getColorStateList(this@RotationActivity, bgColor)
        }
    }
    
    /**
     * Creates a capacity requirement view.
     */
    private fun createCapacityRequirement(
        workstation: Workstation, 
        currentWorkers: List<Worker>, 
        layoutParams: LinearLayout.LayoutParams
    ): LinearLayout {
        val currentCount = currentWorkers.size
        val requiredCount = workstation.requiredWorkers
        val isFullyStaffed = currentCount >= requiredCount
        
        val capacityLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(8, 8, 8, 8)
            this.layoutParams = layoutParams
        }
        
        // Indicador de capacidad
        val capacityView = TextView(this).apply {
            text = "$currentCount/$requiredCount"
            textSize = 14f
            setTextColor(ContextCompat.getColor(this@RotationActivity, R.color.white))
            gravity = android.view.Gravity.CENTER
            setPadding(8, 6, 8, 6)
            setTypeface(null, android.graphics.Typeface.BOLD)
            
            val bgColor = if (isFullyStaffed) R.color.status_success else R.color.status_warning
            background = ContextCompat.getDrawable(this@RotationActivity, R.drawable.status_badge_green)
            backgroundTintList = ContextCompat.getColorStateList(this@RotationActivity, bgColor)
        }
        capacityLayout.addView(capacityView)
        
        // Estado de la estación
        val statusView = TextView(this).apply {
            text = if (isFullyStaffed) "✅ COMPLETA" else "⚠️ INCOMPLETA"
            textSize = 9f
            setTextColor(if (isFullyStaffed) 
                ContextCompat.getColor(this@RotationActivity, R.color.status_success) 
                else ContextCompat.getColor(this@RotationActivity, R.color.status_warning))
            gravity = android.view.Gravity.CENTER
            setPadding(4, 2, 4, 2)
            setTypeface(null, android.graphics.Typeface.BOLD)
        }
        capacityLayout.addView(statusView)
        
        return capacityLayout
    }
    
    /**
     * Creates a column of workers for current or next phase.
     */
    private fun createWorkerColumn(
        workers: List<Worker>, 
        layoutParams: LinearLayout.LayoutParams,
        isCurrentPhase: Boolean
    ): LinearLayout {
        val columnLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(4, 4, 4, 4)
            this.layoutParams = layoutParams
        }
        
        workers.forEach { worker ->
            val workerView = createWorkerView(worker, isCurrentPhase)
            columnLayout.addView(workerView)
        }
        
        return columnLayout
    }
    
    /**
     * Creates a view for an individual worker with enhanced visual design.
     */
    private fun createWorkerView(worker: Worker, isCurrentPhase: Boolean): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(8, 8, 8, 8)
            
            // Fondo con colores diferenciados
            val bgColor = if (isCurrentPhase) R.color.current_phase_light else R.color.next_phase_light
            
            background = ContextCompat.getDrawable(this@RotationActivity, R.drawable.worker_card_background)
            backgroundTintList = ContextCompat.getColorStateList(this@RotationActivity, bgColor)
            
            // Nombre del trabajador
            val nameView = TextView(this@RotationActivity).apply {
                text = worker.name
                textSize = 13f
                setTextColor(ContextCompat.getColor(this@RotationActivity, R.color.text_primary))
                gravity = android.view.Gravity.CENTER
                setTypeface(null, android.graphics.Typeface.BOLD)
                setPadding(4, 4, 4, 2)
            }
            addView(nameView)
            
            // Indicadores de estado
            val statusView = TextView(this@RotationActivity).apply {
                text = getWorkerStatusIndicators(worker)
                textSize = 10f
                setTextColor(ContextCompat.getColor(this@RotationActivity, R.color.text_secondary))
                gravity = android.view.Gravity.CENTER
                setPadding(4, 2, 4, 4)
            }
            addView(statusView)
        }
    }
    
    /**
     * Gets status indicators for a worker.
     */
    private fun getWorkerStatusIndicators(worker: Worker): String {
        val indicators = mutableListOf<String>()
        
        // Training status
        when {
            worker.isTrainer -> indicators.add("👨‍🏫")
            worker.isTrainee -> indicators.add("🎯")
        }
        
        // Availability status
        if (worker.availabilityPercentage < 100) {
            indicators.add("${worker.availabilityPercentage}%")
        }
        
        // Restriction status
        if (worker.restrictionNotes.isNotEmpty()) {
            indicators.add("🔒")
        }
        
        return indicators.joinToString(" ")
            
            // Disponibilidad
            if (worker.availabilityPercentage < 100) {
                val availabilityView = TextView(this@RotationActivity).apply {
                    text = "${worker.availabilityPercentage}%"
                    textSize = 9f
                    setTextColor(getAvailabilityColor(worker.availabilityPercentage))
                    gravity = android.view.Gravity.CENTER
                    setPadding(4, 2, 4, 2)
                    setTypeface(null, android.graphics.Typeface.BOLD)
                }
                addView(availabilityView)
            }
            
            val params = LinearLayout.LayoutParams(
                (140 * resources.displayMetrics.density).toInt(),
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(4, 4, 4, 4)
            }
            layoutParams = params
        }
    }
    
    /**
     * Obtiene indicadores de estado para un trabajador.
     */
    private fun getWorkerStatusIndicators(worker: Worker): String {
        val indicators = mutableListOf<String>()
        
        if (worker.isTrainer) indicators.add("👨‍🏫")
        if (worker.isTrainee) indicators.add("🎯")
        if (worker.restrictionNotes.isNotEmpty()) indicators.add("⚠️")
        
        return indicators.joinToString(" ")
    }
    
    /**
     * Obtiene el color apropiado según el porcentaje de disponibilidad.
     */
    private fun getAvailabilityColor(percentage: Int): Int {
        return when {
            percentage >= 80 -> ContextCompat.getColor(this, R.color.status_success)
            percentage >= 50 -> ContextCompat.getColor(this, R.color.status_warning)
            else -> ContextCompat.getColor(this, R.color.status_error)
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
            hideDownloadButton()
            Snackbar.make(binding.root, "Rotación limpiada", Snackbar.LENGTH_SHORT).show()
        }
        
        binding.btnDownloadImage?.setOnClickListener {
            downloadRotationAsImage()
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
     * Sets up the rotation table display with current and next phases.
     */
    private fun setupRotationTable(rotationTable: RotationTable) {
        clearRotationTable()
        
        val workstations = rotationTable.workstations
        if (workstations.isEmpty()) return
        
        // Create column layout parameters
        val columnWidth = 200 // Fixed width for each column
        val layoutParams = LinearLayout.LayoutParams(
            columnWidth,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(4, 4, 4, 4)
        }
        
        // Setup headers for workstations
        setupWorkstationHeaders(workstations, layoutParams)
        
        // Setup capacity requirements
        setupCapacityRequirements(workstations, rotationTable, layoutParams)
        
        // Setup current phase (rotation actual)
        setupCurrentPhase(workstations, rotationTable, layoutParams)
        
        // Setup next phase (próxima rotación)
        setupNextPhase(workstations, rotationTable, layoutParams)
    }
    
    /**
     * Sets up workstation headers.
     */
    private fun setupWorkstationHeaders(workstations: List<Workstation>, layoutParams: LinearLayout.LayoutParams) {
        workstations.forEach { workstation ->
            val headerView = createWorkstationHeader(workstation, layoutParams)
            binding.layoutWorkstationHeaders.addView(headerView)
        }
    }
    
    /**
     * Sets up capacity requirements display.
     */
    private fun setupCapacityRequirements(
        workstations: List<Workstation>, 
        rotationTable: RotationTable, 
        layoutParams: LinearLayout.LayoutParams
    ) {
        workstations.forEach { workstation ->
            val currentWorkers = rotationTable.currentPhase[workstation.id] ?: emptyList()
            val capacityView = createCapacityRequirement(workstation, currentWorkers, layoutParams)
            binding.layoutCapacityRequirements.addView(capacityView)
        }
    }
    
    /**
     * Sets up current phase display.
     */
    private fun setupCurrentPhase(
        workstations: List<Workstation>, 
        rotationTable: RotationTable, 
        layoutParams: LinearLayout.LayoutParams
    ) {
        workstations.forEach { workstation ->
            val workers = rotationTable.currentPhase[workstation.id] ?: emptyList()
            val columnView = createWorkerColumn(workers, layoutParams, true)
            binding.layoutCurrentPhase.addView(columnView)
        }
    }
    
    /**
     * Sets up next phase display.
     */
    private fun setupNextPhase(
        workstations: List<Workstation>, 
        rotationTable: RotationTable, 
        layoutParams: LinearLayout.LayoutParams
    ) {
        workstations.forEach { workstation ->
            val workers = rotationTable.nextPhase[workstation.id] ?: emptyList()
            val columnView = createWorkerColumn(workers, layoutParams, false)
            binding.layoutNextPhase.addView(columnView)
        }
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
        // Actualizar información básica
        binding.tvRotationInfo.text = "🎯 Rotación generada exitosamente con algoritmo inteligente"
        
        // Mostrar estadísticas
        binding.layoutRotationStats?.visibility = android.view.View.VISIBLE
        binding.tvStatsWorkers?.text = "👥 ${stats.totalWorkers} trabajadores"
        binding.tvStatsRotating?.text = "🔄 ${stats.rotationPercentage}% rotando"
        binding.tvStatsTraining?.text = "🎓 ${stats.trainerTraineePairs} entrenamientos"
        
        // Mostrar timestamp
        val timestamp = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
        binding.tvTimestamp?.text = timestamp
        binding.tvTimestamp?.visibility = android.view.View.VISIBLE
        
        // Mostrar botón de descarga
        showDownloadButton()
    }
    
    /**
     * Muestra el botón de descarga cuando hay una rotación generada.
     */
    private fun showDownloadButton() {
        binding.btnDownloadImage?.visibility = android.view.View.VISIBLE
    }
    
    /**
     * Oculta el botón de descarga cuando no hay rotación.
     */
    private fun hideDownloadButton() {
        binding.btnDownloadImage?.visibility = android.view.View.GONE
        binding.layoutRotationStats?.visibility = android.view.View.GONE
        binding.tvTimestamp?.visibility = android.view.View.GONE
    }
    
    /**
     * Descarga la rotación como imagen capturando toda la tabla completa.
     */
    private fun downloadRotationAsImage() {
        lifecycleScope.launch {
            try {
                // Mostrar progreso
                binding.btnDownloadImage?.isEnabled = false
                binding.btnDownloadImage?.text = "📷 Generando..."
                
                // Capturar toda la tabla de rotación completa (CardView con header y contenido)
                val cardView = binding.cardRotationTable ?: return@launch
                val bitmap = ImageUtils.captureCompleteRotationTable(cardView)
                
                // Guardar en galería
                val filename = ImageUtils.generateRotationFilename("rotacion_completa")
                val uri = ImageUtils.saveBitmapToGallery(this@RotationActivity, bitmap, filename)
                
                if (uri != null) {
                    // Éxito - mostrar opciones
                    showImageSavedOptions(bitmap, filename)
                } else {
                    ImageUtils.showErrorMessage(this@RotationActivity, "No se pudo guardar la imagen")
                }
                
            } catch (e: Exception) {
                ImageUtils.showErrorMessage(this@RotationActivity, "Error al generar imagen: ${e.message}")
            } finally {
                // Restaurar botón
                binding.btnDownloadImage?.isEnabled = true
                binding.btnDownloadImage?.text = "📷"
            }
        }
    }
    
    /**
     * Muestra opciones después de guardar la imagen.
     */
    private fun showImageSavedOptions(bitmap: android.graphics.Bitmap, filename: String) {
        lifecycleScope.launch {
            try {
                // Crear URI para compartir
                val shareUri = ImageUtils.saveBitmapForSharing(this@RotationActivity, bitmap, filename)
                
                if (shareUri != null) {
                    // Mostrar Snackbar con opción de compartir
                    val snackbar = Snackbar.make(
                        binding.root,
                        "✅ Imagen guardada en galería",
                        Snackbar.LENGTH_LONG
                    )
                    
                    snackbar.setAction("📤 Compartir") {
                        shareRotationImage(shareUri)
                    }
                    
                    snackbar.show()
                } else {
                    ImageUtils.showSuccessMessage(this@RotationActivity, "Imagen guardada en galería")
                }
                
            } catch (e: Exception) {
                ImageUtils.showSuccessMessage(this@RotationActivity, "Imagen guardada en galería")
            }
        }
    }
    
    /**
     * Comparte la imagen de rotación.
     */
    private fun shareRotationImage(uri: android.net.Uri) {
        try {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Rotacion Inteligente de Trabajadores")
                putExtra(Intent.EXTRA_TEXT, 
                    "Rotacion generada con el Sistema de Rotacion Inteligente\n" +
                    "Optimizacion automatica de personal en estaciones de trabajo\n" +
                    "Generada el ${SimpleDateFormat("dd/MM/yyyy 'a las' HH:mm", Locale.getDefault()).format(Date())}"
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            
            startActivity(Intent.createChooser(shareIntent, "Compartir Rotacion"))
            
        } catch (e: Exception) {
            ImageUtils.showErrorMessage(this, "Error al compartir imagen")
        }
    }
}