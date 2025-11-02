package com.workstation.rotation

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.workstation.rotation.data.database.AppDatabase
import com.workstation.rotation.databinding.ActivityRotationBinding
import com.workstation.rotation.viewmodels.SqlRotationViewModel
import com.workstation.rotation.viewmodels.SqlRotationViewModelFactory
import kotlinx.coroutines.launch

/**
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 🚀 ACTIVITY PARA ROTACIÓN SQL SIMPLIFICADA Y ROBUSTA
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 
 * Esta Activity utiliza el nuevo SqlRotationViewModel que garantiza:
 * 1. Rotaciones sin conflictos
 * 2. Líderes siempre en sus estaciones
 * 3. Parejas de entrenamiento nunca separadas
 * 4. Asignaciones consistentes y predecibles
 * 5. Mejor rendimiento con consultas SQL optimizadas
 * 
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 */
class SqlRotationActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityRotationBinding
    
    private val sqlViewModel: SqlRotationViewModel by viewModels {
        val database = AppDatabase.getDatabase(this)
        SqlRotationViewModelFactory(
            database.rotationDao(),
            database.workerDao(),
            database.workstationDao()
        )
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRotationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupButtons()
        observeViewModel()
    }
    
    private fun setupToolbar() {
        binding.toolbar.title = "🚀 Rotación SQL Optimizada"
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }
    
    private fun setupButtons() {
        // Botón principal para generar rotación
        binding.btnGenerateRotation.text = "🚀 Generar Rotación SQL"
        binding.btnGenerateRotation.setOnClickListener {
            generateSqlRotation()
        }
        
        // Botón para alternar tipo de rotación (long press)
        binding.btnGenerateRotation.setOnLongClickListener {
            toggleRotationHalf()
            true
        }
        
        // Botón para limpiar rotación
        binding.btnClearRotation.setOnClickListener {
            clearRotation()
        }
        
        // Botón para descargar imagen (si existe)
        binding.btnDownloadImage?.setOnClickListener {
            // Funcionalidad de descarga se mantiene igual
            Snackbar.make(binding.root, "Funcionalidad de descarga disponible", Snackbar.LENGTH_SHORT).show()
        }
    }
    
    private fun observeViewModel() {
        // Observar elementos de rotación
        sqlViewModel.rotationItems.observe(this) { items ->
            // La UI se actualiza automáticamente
            updateRotationInfo(items.size)
        }
        
        // Observar tabla de rotación
        sqlViewModel.rotationTable.observe(this) { rotationTable ->
            if (rotationTable != null) {
                setupRotationTable(rotationTable)
                showDownloadButton()
            } else {
                clearRotationTable()
                hideDownloadButton()
            }
        }
        
        // Observar estado de carga
        sqlViewModel.isLoading.observe(this) { isLoading ->
            binding.btnGenerateRotation.isEnabled = !isLoading
            binding.btnGenerateRotation.text = if (isLoading) {
                "⏳ Generando..."
            } else {
                "🚀 Generar Rotación SQL"
            }
        }
        
        // Observar errores
        sqlViewModel.errorMessage.observe(this) { errorMessage ->
            errorMessage?.let {
                Snackbar.make(binding.root, "❌ $it", Snackbar.LENGTH_LONG)
                    .setBackgroundTint(ContextCompat.getColor(this, R.color.status_error))
                    .show()
            }
        }
    }
    
    /**
     * Genera una rotación usando el algoritmo SQL optimizado.
     */
    private fun generateSqlRotation() {
        lifecycleScope.launch {
            try {
                val success = sqlViewModel.generateOptimizedRotation()
                
                if (success) {
                    Snackbar.make(binding.root, "✅ Rotación SQL generada exitosamente", Snackbar.LENGTH_LONG)
                        .setBackgroundTint(ContextCompat.getColor(this@SqlRotationActivity, R.color.status_success))
                        .show()
                } else {
                    Snackbar.make(binding.root, "❌ No se pudo generar la rotación", Snackbar.LENGTH_LONG)
                        .setBackgroundTint(ContextCompat.getColor(this@SqlRotationActivity, R.color.status_error))
                        .show()
                }
            } catch (e: Exception) {
                Snackbar.make(binding.root, "❌ Error: ${e.message}", Snackbar.LENGTH_LONG)
                    .setBackgroundTint(ContextCompat.getColor(this@SqlRotationActivity, R.color.status_error))
                    .show()
            }
        }
    }
    
    /**
     * Alterna entre primera y segunda parte de la rotación.
     */
    private fun toggleRotationHalf() {
        sqlViewModel.toggleRotationHalf()
        val currentHalf = sqlViewModel.getCurrentRotationHalf()
        
        Snackbar.make(binding.root, "🔄 Cambiado a: $currentHalf", Snackbar.LENGTH_LONG)
            .setAction("Generar") {
                generateSqlRotation()
            }
            .show()
    }
    
    /**
     * Limpia la rotación actual.
     */
    private fun clearRotation() {
        sqlViewModel.clearRotation()
        Snackbar.make(binding.root, "🧹 Rotación limpiada", Snackbar.LENGTH_SHORT).show()
    }
    
    /**
     * Configura la tabla de rotación en la UI.
     * Reutiliza la lógica existente de RotationActivity.
     */
    private fun setupRotationTable(rotationTable: com.workstation.rotation.models.RotationTable) {
        clearRotationTable()
        
        val workstations = rotationTable.workstations
        if (workstations.isEmpty()) return
        
        // Verificar que tenemos datos
        val currentTotal = rotationTable.currentPhase.values.sumOf { it.size }
        val nextTotal = rotationTable.nextPhase.values.sumOf { it.size }
        println("SQL_DEBUG: Current phase workers: $currentTotal, Next phase workers: $nextTotal")
        
        val columnWidth = 280
        val layoutParams = android.widget.LinearLayout.LayoutParams(
            columnWidth,
            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(6, 6, 6, 6)
        }
        
        setupWorkstationHeaders(workstations, layoutParams)
        setupCapacityRequirements(workstations, rotationTable, layoutParams)
        setupCurrentPhase(workstations, rotationTable, layoutParams)
        setupNextPhase(workstations, rotationTable, layoutParams)
    }
    
    private fun setupWorkstationHeaders(
        workstations: List<com.workstation.rotation.data.entities.Workstation>, 
        layoutParams: android.widget.LinearLayout.LayoutParams
    ) {
        workstations.forEach { workstation ->
            val headerView = createWorkstationHeader(workstation, layoutParams)
            binding.layoutWorkstationHeaders.addView(headerView)
        }
    }
    
    private fun setupCapacityRequirements(
        workstations: List<com.workstation.rotation.data.entities.Workstation>, 
        rotationTable: com.workstation.rotation.models.RotationTable, 
        layoutParams: android.widget.LinearLayout.LayoutParams
    ) {
        workstations.forEach { workstation ->
            val currentWorkers = rotationTable.currentPhase[workstation.id] ?: emptyList()
            val capacityView = createCapacityRequirement(workstation, currentWorkers, layoutParams)
            binding.layoutCapacityRequirements.addView(capacityView)
        }
    }
    
    private fun setupCurrentPhase(
        workstations: List<com.workstation.rotation.data.entities.Workstation>, 
        rotationTable: com.workstation.rotation.models.RotationTable, 
        layoutParams: android.widget.LinearLayout.LayoutParams
    ) {
        workstations.forEach { workstation ->
            val workers = rotationTable.currentPhase[workstation.id] ?: emptyList()
            val columnView = createWorkerColumn(workers, layoutParams, true)
            binding.layoutCurrentPhase.addView(columnView)
        }
    }
    
    private fun setupNextPhase(
        workstations: List<com.workstation.rotation.data.entities.Workstation>, 
        rotationTable: com.workstation.rotation.models.RotationTable, 
        layoutParams: android.widget.LinearLayout.LayoutParams
    ) {
        workstations.forEach { workstation ->
            val workers = rotationTable.nextPhase[workstation.id] ?: emptyList()
            val columnView = createWorkerColumn(workers, layoutParams, false)
            binding.layoutNextPhase.addView(columnView)
        }
    }
    
    private fun createWorkstationHeader(
        workstation: com.workstation.rotation.data.entities.Workstation, 
        layoutParams: android.widget.LinearLayout.LayoutParams
    ): android.widget.TextView {
        return android.widget.TextView(this).apply {
            text = workstation.name
            textSize = 14f
            setTextColor(ContextCompat.getColor(this@SqlRotationActivity, R.color.white))
            gravity = android.view.Gravity.CENTER
            setPadding(12, 16, 12, 16)
            setTypeface(null, android.graphics.Typeface.BOLD)
            
            maxLines = 2
            ellipsize = null
            isSingleLine = false
            
            val adjustedLayoutParams = android.widget.LinearLayout.LayoutParams(
                maxOf(layoutParams.width, 250),
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(4, 4, 4, 4)
            }
            this.layoutParams = adjustedLayoutParams
            
            val bgColor = if (workstation.isPriority) R.color.priority_header else R.color.normal_header
            background = ContextCompat.getDrawable(this@SqlRotationActivity, R.drawable.gradient_primary)
            backgroundTintList = ContextCompat.getColorStateList(this@SqlRotationActivity, bgColor)
        }
    }
    
    private fun createCapacityRequirement(
        workstation: com.workstation.rotation.data.entities.Workstation, 
        currentWorkers: List<com.workstation.rotation.data.entities.Worker>, 
        layoutParams: android.widget.LinearLayout.LayoutParams
    ): android.widget.LinearLayout {
        val currentCount = currentWorkers.size
        val requiredCount = workstation.requiredWorkers
        val isFullyStaffed = currentCount >= requiredCount
        
        val capacityLayout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(12, 8, 12, 8)
            
            val adjustedLayoutParams = android.widget.LinearLayout.LayoutParams(
                maxOf(layoutParams.width, 250),
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(4, 4, 4, 4)
            }
            this.layoutParams = adjustedLayoutParams
        }
        
        val capacityView = android.widget.TextView(this).apply {
            text = "$currentCount/$requiredCount"
            textSize = 16f
            setTextColor(ContextCompat.getColor(this@SqlRotationActivity, R.color.white))
            gravity = android.view.Gravity.CENTER
            setPadding(12, 8, 12, 8)
            setTypeface(null, android.graphics.Typeface.BOLD)
            maxLines = 1
            
            val bgColor = if (isFullyStaffed) R.color.status_success else R.color.status_warning
            background = ContextCompat.getDrawable(this@SqlRotationActivity, R.drawable.status_badge_green)
            backgroundTintList = ContextCompat.getColorStateList(this@SqlRotationActivity, bgColor)
        }
        capacityLayout.addView(capacityView)
        
        val statusView = android.widget.TextView(this).apply {
            text = if (isFullyStaffed) "COMPLETA" else "INCOMPLETA"
            textSize = 10f
            setTextColor(if (isFullyStaffed) 
                ContextCompat.getColor(this@SqlRotationActivity, R.color.status_success) 
                else ContextCompat.getColor(this@SqlRotationActivity, R.color.status_warning))
            gravity = android.view.Gravity.CENTER
            setPadding(4, 4, 4, 4)
            setTypeface(null, android.graphics.Typeface.BOLD)
            maxLines = 1
        }
        capacityLayout.addView(statusView)
        
        return capacityLayout
    }
    
    private fun createWorkerColumn(
        workers: List<com.workstation.rotation.data.entities.Worker>, 
        layoutParams: android.widget.LinearLayout.LayoutParams,
        isCurrentPhase: Boolean
    ): android.widget.LinearLayout {
        val columnLayout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(8, 8, 8, 8)
            
            val adjustedLayoutParams = android.widget.LinearLayout.LayoutParams(
                maxOf(layoutParams.width, 250),
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(4, 4, 4, 4)
            }
            this.layoutParams = adjustedLayoutParams
        }
        
        workers.forEach { worker ->
            val workerView = createWorkerView(worker, isCurrentPhase)
            columnLayout.addView(workerView)
        }
        
        return columnLayout
    }
    
    private fun createWorkerView(
        worker: com.workstation.rotation.data.entities.Worker, 
        isCurrentPhase: Boolean
    ): android.widget.LinearLayout {
        return android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(12, 12, 12, 12)
            
            val cardLayoutParams = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(4, 4, 4, 4)
            }
            this.layoutParams = cardLayoutParams
            
            val bgColor = if (isCurrentPhase) R.color.current_phase_light else R.color.next_phase_light
            
            background = ContextCompat.getDrawable(this@SqlRotationActivity, R.drawable.worker_card_background)
            backgroundTintList = ContextCompat.getColorStateList(this@SqlRotationActivity, bgColor)
            
            val nameView = android.widget.TextView(this@SqlRotationActivity).apply {
                text = worker.getDisplayName()
                textSize = 14f
                setTextColor(ContextCompat.getColor(this@SqlRotationActivity, R.color.text_primary))
                gravity = android.view.Gravity.CENTER
                setTypeface(null, android.graphics.Typeface.BOLD)
                setPadding(8, 8, 8, 4)
                
                maxLines = 2
                ellipsize = null
                isSingleLine = false
                minWidth = 200
            }
            addView(nameView)
            
            val statusText = getWorkerStatusIndicators(worker)
            if (statusText.isNotEmpty()) {
                val statusView = android.widget.TextView(this@SqlRotationActivity).apply {
                    text = statusText
                    textSize = 10f
                    setTextColor(ContextCompat.getColor(this@SqlRotationActivity, R.color.text_secondary))
                    gravity = android.view.Gravity.CENTER
                    setPadding(8, 4, 8, 8)
                    
                    maxLines = 2
                    ellipsize = null
                    isSingleLine = false
                }
                addView(statusView)
            }
        }
    }
    
    private fun getWorkerStatusIndicators(worker: com.workstation.rotation.data.entities.Worker): String {
        val indicators = mutableListOf<String>()
        
        when {
            worker.isLeader -> indicators.add("LÍDER")
            worker.isTrainer -> indicators.add("ENTRENADOR")
            worker.isTrainee -> indicators.add("ENTRENADO")
        }
        
        if (worker.availabilityPercentage < 100) {
            indicators.add("${worker.availabilityPercentage}%")
        }
        
        if (worker.restrictionNotes.isNotEmpty()) {
            indicators.add("RESTRINGIDO")
        }
        
        return indicators.joinToString(" • ")
    }
    
    private fun clearRotationTable() {
        binding.layoutWorkstationHeaders.removeAllViews()
        binding.layoutCapacityRequirements.removeAllViews()
        binding.layoutCurrentPhase.removeAllViews()
        binding.layoutNextPhase.removeAllViews()
    }
    
    private fun updateRotationInfo(itemCount: Int) {
        binding.tvRotationInfo.text = "🚀 Rotación SQL: $itemCount asignaciones generadas"
    }
    
    private fun showDownloadButton() {
        binding.btnDownloadImage?.visibility = View.VISIBLE
        binding.btnDownloadImage?.text = "📷 Descargar"
    }
    
    private fun hideDownloadButton() {
        binding.btnDownloadImage?.visibility = View.GONE
    }
}