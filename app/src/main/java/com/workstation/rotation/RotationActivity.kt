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
import com.workstation.rotation.utils.UIUtils
import com.workstation.rotation.viewmodels.RotationViewModel
import com.workstation.rotation.viewmodels.RotationViewModelFactory
import kotlinx.coroutines.launch

class RotationActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityRotationBinding
    
    private val viewModel: RotationViewModel by viewModels {
        RotationViewModelFactory(
            AppDatabase.getDatabase(this).workerDao(),
            AppDatabase.getDatabase(this).workstationDao(),
            AppDatabase.getDatabase(this).workerRestrictionDao()
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
    
    private fun setupButtons() {
        binding.btnGenerateRotation.setOnClickListener {
            lifecycleScope.launch {
                try {
                    val success = viewModel.generateRotation()
                    if (success) {
                        showDownloadButton()
                        Snackbar.make(binding.root, "Rotacion generada exitosamente", Snackbar.LENGTH_LONG).show()
                    } else {
                        hideDownloadButton()
                        Snackbar.make(binding.root, "No se pudo generar la rotacion", Snackbar.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    hideDownloadButton()
                    Snackbar.make(binding.root, "Error: ${e.message}", Snackbar.LENGTH_LONG).show()
                }
            }
        }
        
        binding.btnClearRotation.setOnClickListener {
            viewModel.clearRotation()
            updateWorkerCount()
            hideDownloadButton()
            Snackbar.make(binding.root, "Rotacion limpiada", Snackbar.LENGTH_SHORT).show()
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
        
        updateWorkerCount()
    }
    
    private fun setupRotationTable(rotationTable: RotationTable) {
        clearRotationTable()
        
        val workstations = rotationTable.workstations
        if (workstations.isEmpty()) return
        
        // Debug: verificar que tenemos datos
        val currentTotal = rotationTable.currentPhase.values.sumOf { it.size }
        val nextTotal = rotationTable.nextPhase.values.sumOf { it.size }
        println("DEBUG: Current phase workers: $currentTotal, Next phase workers: $nextTotal")
        
        val columnWidth = 200
        val layoutParams = LinearLayout.LayoutParams(
            columnWidth,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(4, 4, 4, 4)
        }
        
        setupWorkstationHeaders(workstations, layoutParams)
        setupCapacityRequirements(workstations, rotationTable, layoutParams)
        setupCurrentPhase(workstations, rotationTable, layoutParams)
        setupNextPhase(workstations, rotationTable, layoutParams)
        
        // Mostrar botón de descarga cuando hay rotación
        showDownloadButton()
    }
    
    private fun setupWorkstationHeaders(workstations: List<com.workstation.rotation.data.entities.Workstation>, layoutParams: LinearLayout.LayoutParams) {
        workstations.forEach { workstation ->
            val headerView = createWorkstationHeader(workstation, layoutParams)
            binding.layoutWorkstationHeaders.addView(headerView)
        }
    }
    
    private fun setupCapacityRequirements(
        workstations: List<com.workstation.rotation.data.entities.Workstation>, 
        rotationTable: RotationTable, 
        layoutParams: LinearLayout.LayoutParams
    ) {
        workstations.forEach { workstation ->
            val currentWorkers = rotationTable.currentPhase[workstation.id] ?: emptyList()
            val capacityView = createCapacityRequirement(workstation, currentWorkers, layoutParams)
            binding.layoutCapacityRequirements.addView(capacityView)
        }
    }
    
    private fun setupCurrentPhase(
        workstations: List<com.workstation.rotation.data.entities.Workstation>, 
        rotationTable: RotationTable, 
        layoutParams: LinearLayout.LayoutParams
    ) {
        workstations.forEach { workstation ->
            val workers = rotationTable.currentPhase[workstation.id] ?: emptyList()
            println("DEBUG: Current phase - ${workstation.name}: ${workers.size} workers")
            val columnView = createWorkerColumn(workers, layoutParams, true)
            binding.layoutCurrentPhase.addView(columnView)
        }
    }
    
    private fun setupNextPhase(
        workstations: List<com.workstation.rotation.data.entities.Workstation>, 
        rotationTable: RotationTable, 
        layoutParams: LinearLayout.LayoutParams
    ) {
        workstations.forEach { workstation ->
            val workers = rotationTable.nextPhase[workstation.id] ?: emptyList()
            println("DEBUG: Next phase - ${workstation.name}: ${workers.size} workers")
            val columnView = createWorkerColumn(workers, layoutParams, false)
            binding.layoutNextPhase.addView(columnView)
        }
    }
    
    private fun createWorkstationHeader(workstation: com.workstation.rotation.data.entities.Workstation, layoutParams: LinearLayout.LayoutParams): TextView {
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
    
    private fun createCapacityRequirement(
        workstation: com.workstation.rotation.data.entities.Workstation, 
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
        
        val statusView = TextView(this).apply {
            text = if (isFullyStaffed) "COMPLETA" else "INCOMPLETA"
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
    
    private fun createWorkerView(worker: Worker, isCurrentPhase: Boolean): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(8, 8, 8, 8)
            
            val bgColor = if (isCurrentPhase) R.color.current_phase_light else R.color.next_phase_light
            
            background = ContextCompat.getDrawable(this@RotationActivity, R.drawable.worker_card_background)
            backgroundTintList = ContextCompat.getColorStateList(this@RotationActivity, bgColor)
            
            val nameView = TextView(this@RotationActivity).apply {
                text = worker.name
                textSize = 13f
                setTextColor(ContextCompat.getColor(this@RotationActivity, R.color.text_primary))
                gravity = android.view.Gravity.CENTER
                setTypeface(null, android.graphics.Typeface.BOLD)
                setPadding(4, 4, 4, 2)
            }
            addView(nameView)
            
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
    
    private fun getWorkerStatusIndicators(worker: Worker): String {
        val indicators = mutableListOf<String>()
        
        when {
            worker.isTrainer -> indicators.add("ENTRENADOR")
            worker.isTrainee -> indicators.add("ENTRENADO")
        }
        
        if (worker.availabilityPercentage < 100) {
            indicators.add("${worker.availabilityPercentage}%")
        }
        
        if (worker.restrictionNotes.isNotEmpty()) {
            indicators.add("RESTRINGIDO")
        }
        
        return indicators.joinToString(" ")
    }
    
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
                binding.tvRotationInfo.text = "Trabajadores elegibles: $count"
            } catch (e: Exception) {
                binding.tvRotationInfo.text = "Error al contar trabajadores"
            }
        }
    }
    
    private fun showDownloadButton() {
        binding.btnDownloadImage?.visibility = android.view.View.VISIBLE
        binding.btnDownloadImage?.text = "📷"
    }
    
    private fun hideDownloadButton() {
        binding.btnDownloadImage?.visibility = android.view.View.GONE
        binding.layoutRotationStats?.visibility = android.view.View.GONE
        binding.tvTimestamp?.visibility = android.view.View.GONE
    }
    
    private fun downloadRotationAsImage() {
        lifecycleScope.launch {
            try {
                binding.btnDownloadImage?.isEnabled = false
                binding.btnDownloadImage?.text = "📸 Capturando..."
                
                // Capturar toda la tabla incluyendo scroll vertical y horizontal
                val cardView = binding.cardRotationTable ?: return@launch
                val bitmap = ImageUtils.captureCompleteScrollableContent(cardView)
                
                val filename = ImageUtils.generateRotationFilename("rotacion_actual_y_siguiente")
                val uri = ImageUtils.saveBitmapToGallery(this@RotationActivity, bitmap, filename)
                
                if (uri != null) {
                    showImageSavedOptions(bitmap, filename)
                } else {
                    ImageUtils.showErrorMessage(this@RotationActivity, "No se pudo guardar la imagen")
                }
                
            } catch (e: Exception) {
                ImageUtils.showErrorMessage(this@RotationActivity, "Error al generar imagen: ${e.message}")
            } finally {
                binding.btnDownloadImage?.isEnabled = true
                binding.btnDownloadImage?.text = "📷"
            }
        }
    }
    
    private fun showImageSavedOptions(bitmap: android.graphics.Bitmap, filename: String) {
        lifecycleScope.launch {
            try {
                val shareUri = ImageUtils.saveBitmapForSharing(this@RotationActivity, bitmap, filename)
                
                if (shareUri != null) {
                    val snackbar = Snackbar.make(
                        binding.root,
                        "Imagen guardada en galeria",
                        Snackbar.LENGTH_LONG
                    )
                    
                    snackbar.setAction("Compartir") {
                        shareRotationImage(shareUri)
                    }
                    
                    snackbar.show()
                } else {
                    ImageUtils.showSuccessMessage(this@RotationActivity, "Imagen guardada en galeria")
                }
                
            } catch (e: Exception) {
                ImageUtils.showSuccessMessage(this@RotationActivity, "Imagen guardada en galeria")
            }
        }
    }
    
    private fun shareRotationImage(uri: android.net.Uri) {
        try {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Rotacion Inteligente")
                putExtra(Intent.EXTRA_TEXT, "Rotacion generada automaticamente")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(shareIntent, "Compartir"))
        } catch (e: Exception) {
            ImageUtils.showErrorMessage(this, "Error al compartir")
        }
    }
}