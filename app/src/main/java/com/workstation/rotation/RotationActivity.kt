package com.workstation.rotation

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.workstation.rotation.adapters.RotationAdapter
import com.workstation.rotation.data.database.AppDatabase
import com.workstation.rotation.databinding.ActivityRotationBinding
import com.workstation.rotation.models.RotationItem
import com.workstation.rotation.viewmodels.RotationViewModel
import com.workstation.rotation.viewmodels.RotationViewModelFactory
import kotlinx.coroutines.launch

class RotationActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityRotationBinding
    private lateinit var adapter: RotationAdapter
    
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
        setupRecyclerView()
        setupButtons()
        observeRotation()
    }
    
    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }
    
    private fun setupRecyclerView() {
        adapter = RotationAdapter()
        
        binding.recyclerViewRotation.apply {
            layoutManager = LinearLayoutManager(this@RotationActivity)
            adapter = this@RotationActivity.adapter
        }
    }
    
    private fun setupButtons() {
        binding.btnGenerateRotation.setOnClickListener {
            lifecycleScope.launch {
                try {
                    val rotationGenerated = viewModel.generateRotation()
                    if (rotationGenerated) {
                        Snackbar.make(binding.root, "Rotación generada exitosamente", Snackbar.LENGTH_SHORT).show()
                        updateWorkerCount()
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
        viewModel.rotationItems.observe(this) { items ->
            adapter.submitList(items)
            updateWorkerCount()
        }
        
        // Initial count update
        updateWorkerCount()
    }
    
    private fun updateWorkerCount() {
        lifecycleScope.launch {
            try {
                viewModel.updateEligibleWorkersCount()
                val count = viewModel.getEligibleWorkersCount()
                binding.tvRotationInfo.text = "Trabajadores elegibles: $count | " +
                        "Rotación inteligente con control de capacidad y prioridades"
            } catch (e: Exception) {
                binding.tvRotationInfo.text = "Error al contar trabajadores elegibles"
            }
        }
    }
}