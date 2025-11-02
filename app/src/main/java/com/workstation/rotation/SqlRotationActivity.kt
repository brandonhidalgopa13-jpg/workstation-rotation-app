package com.workstation.rotation

import android.os.Bundle
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
 * Actividad principal para el sistema de rotación SQL optimizado.
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
        binding.toolbar.title = "Sistema de Rotación"
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }
    
    private fun setupButtons() {
        binding.btnGenerateRotation.setOnClickListener {
            generateSqlRotation()
        }
        
        binding.btnClearRotation.setOnClickListener {
            clearRotation()
        }
    }
    
    private fun observeViewModel() {
        sqlViewModel.rotationItems.observe(this) { items ->
            updateRotationInfo(items.size)
        }
        
        sqlViewModel.rotationTable.observe(this) { rotationTable ->
            if (rotationTable != null) {
                setupRotationTable(rotationTable)
            } else {
                clearRotationTable()
            }
        }
        
        sqlViewModel.isLoading.observe(this) { isLoading ->
            binding.btnGenerateRotation.isEnabled = !isLoading
        }
        
        sqlViewModel.errorMessage.observe(this) { errorMessage ->
            errorMessage?.let {
                Snackbar.make(binding.root, "Error: $it", Snackbar.LENGTH_LONG)
                    .setBackgroundTint(ContextCompat.getColor(this, R.color.status_error))
                    .show()
            }
        }
    }
    
    private fun generateSqlRotation() {
        lifecycleScope.launch {
            try {
                val success = sqlViewModel.generateOptimizedRotation()
                
                if (success) {
                    Snackbar.make(binding.root, "✅ Rotación generada exitosamente", Snackbar.LENGTH_LONG)
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
    
    private fun clearRotation() {
        sqlViewModel.clearRotation()
        Snackbar.make(binding.root, "🧹 Rotación limpiada", Snackbar.LENGTH_SHORT).show()
    }
    
    private fun setupRotationTable(rotationTable: com.workstation.rotation.models.RotationTable) {
        clearRotationTable()
        
        val workstations = rotationTable.workstations
        if (workstations.isEmpty()) return
        
        // Implementación simplificada de la tabla
        updateRotationInfo(rotationTable.currentPhase.values.sumOf { it.size })
    }
    
    private fun clearRotationTable() {
        binding.layoutWorkstationHeaders.removeAllViews()
        binding.layoutCapacityRequirements.removeAllViews()
        binding.layoutCurrentPhase.removeAllViews()
        binding.layoutNextPhase.removeAllViews()
    }
    
    private fun updateRotationInfo(itemCount: Int) {
        binding.tvRotationInfo.text = "Rotación SQL: $itemCount asignaciones generadas"
    }
}