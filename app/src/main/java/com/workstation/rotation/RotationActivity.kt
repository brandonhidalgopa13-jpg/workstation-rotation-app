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
                val rotationGenerated = viewModel.generateRotation()
                if (rotationGenerated) {
                    Snackbar.make(binding.root, "Rotación generada exitosamente", Snackbar.LENGTH_SHORT).show()
                } else {
                    Snackbar.make(binding.root, "No hay trabajadores elegibles para rotación", Snackbar.LENGTH_LONG).show()
                }
            }
        }
        
        binding.btnClearRotation.setOnClickListener {
            viewModel.clearRotation()
            Snackbar.make(binding.root, "Rotación limpiada", Snackbar.LENGTH_SHORT).show()
        }
    }
    
    private fun observeRotation() {
        viewModel.rotationItems.observe(this) { items ->
            adapter.submitList(items)
            
            val eligibleCount = viewModel.getEligibleWorkersCount()
            binding.tvRotationInfo.text = "Trabajadores elegibles para rotación: $eligibleCount " +
                    "(Solo aquellos con estaciones asignadas)"
        }
        
        lifecycleScope.launch {
            val count = viewModel.getEligibleWorkersCount()
            binding.tvRotationInfo.text = "Trabajadores elegibles para rotación: $count " +
                    "(Solo aquellos con estaciones asignadas)"
        }
    }
}