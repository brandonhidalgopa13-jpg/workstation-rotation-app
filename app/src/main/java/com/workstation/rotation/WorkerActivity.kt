package com.workstation.rotation

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.textfield.TextInputEditText
import com.workstation.rotation.adapters.WorkerAdapter
import com.workstation.rotation.adapters.WorkstationCheckboxAdapter
import com.workstation.rotation.adapters.WorkstationCheckItem
import com.workstation.rotation.data.database.AppDatabase
import com.workstation.rotation.data.entities.Worker
import com.workstation.rotation.databinding.ActivityWorkerBinding
import com.workstation.rotation.databinding.DialogAddWorkerBinding
import com.workstation.rotation.viewmodels.WorkerViewModel
import com.workstation.rotation.viewmodels.WorkerViewModelFactory
import kotlinx.coroutines.launch

class WorkerActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityWorkerBinding
    private lateinit var adapter: WorkerAdapter
    
    private val viewModel: WorkerViewModel by viewModels {
        WorkerViewModelFactory(
            AppDatabase.getDatabase(this).workerDao(),
            AppDatabase.getDatabase(this).workstationDao()
        )
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWorkerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupRecyclerView()
        setupFab()
        observeWorkers()
    }
    
    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }
    
    private fun setupRecyclerView() {
        adapter = WorkerAdapter(
            onEditClick = { worker -> showEditDialog(worker) },
            onStatusChange = { worker, isActive -> 
                lifecycleScope.launch {
                    viewModel.updateWorkerStatus(worker.id, isActive)
                }
            }
        )
        
        binding.recyclerViewWorkers.apply {
            layoutManager = LinearLayoutManager(this@WorkerActivity)
            adapter = this@WorkerActivity.adapter
        }
    }
    
    private fun setupFab() {
        binding.fabAddWorker.setOnClickListener {
            showAddDialog()
        }
    }
    
    private fun observeWorkers() {
        viewModel.allWorkers.observe(this) { workers ->
            adapter.submitList(workers)
        }
    }
    
    private fun showAddDialog() {
        val dialogBinding = DialogAddWorkerBinding.inflate(layoutInflater)
        val workstationAdapter = WorkstationCheckboxAdapter { item, isChecked ->
            item.isChecked = isChecked
        }
        
        dialogBinding.recyclerViewWorkstations.apply {
            layoutManager = LinearLayoutManager(this@WorkerActivity)
            adapter = workstationAdapter
        }
        
        // Load workstations
        viewModel.activeWorkstations.observe(this) { workstations ->
            val checkItems = workstations.map { WorkstationCheckItem(it, false) }
            workstationAdapter.submitList(checkItems)
        }
        
        AlertDialog.Builder(this)
            .setTitle("Agregar Trabajador")
            .setView(dialogBinding.root)
            .setPositiveButton("Guardar") { _, _ ->
                val name = dialogBinding.etWorkerName.text.toString().trim()
                val email = dialogBinding.etWorkerEmail.text.toString().trim()
                val availabilityText = dialogBinding.etAvailabilityPercentage.text.toString().trim()
                val availability = availabilityText.toIntOrNull()?.coerceIn(0, 100) ?: 100
                val restrictionNotes = dialogBinding.etRestrictionNotes.text.toString().trim()
                
                if (name.isNotEmpty()) {
                    val selectedWorkstations = workstationAdapter.currentList
                        .filter { it.isChecked }
                        .map { it.workstation.id }
                    
                    lifecycleScope.launch {
                        viewModel.insertWorkerWithWorkstations(
                            Worker(
                                name = name, 
                                email = email,
                                availabilityPercentage = availability,
                                restrictionNotes = restrictionNotes
                            ),
                            selectedWorkstations
                        )
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    
    private fun showEditDialog(worker: Worker) {
        val dialogBinding = DialogAddWorkerBinding.inflate(layoutInflater)
        val workstationAdapter = WorkstationCheckboxAdapter { item, isChecked ->
            item.isChecked = isChecked
        }
        
        dialogBinding.apply {
            etWorkerName.setText(worker.name)
            etWorkerEmail.setText(worker.email)
            etAvailabilityPercentage.setText(worker.availabilityPercentage.toString())
            etRestrictionNotes.setText(worker.restrictionNotes)
            
            recyclerViewWorkstations.apply {
                layoutManager = LinearLayoutManager(this@WorkerActivity)
                adapter = workstationAdapter
            }
        }
        
        // Load workstations with current assignments
        lifecycleScope.launch {
            val assignedIds = viewModel.getWorkerWorkstationIds(worker.id)
            viewModel.activeWorkstations.observe(this@WorkerActivity) { workstations ->
                val checkItems = workstations.map { workstation ->
                    WorkstationCheckItem(workstation, assignedIds.contains(workstation.id))
                }
                workstationAdapter.submitList(checkItems)
            }
        }
        
        AlertDialog.Builder(this)
            .setTitle("Editar Trabajador")
            .setView(dialogBinding.root)
            .setPositiveButton("Guardar") { _, _ ->
                val name = dialogBinding.etWorkerName.text.toString().trim()
                val email = dialogBinding.etWorkerEmail.text.toString().trim()
                val availabilityText = dialogBinding.etAvailabilityPercentage.text.toString().trim()
                val availability = availabilityText.toIntOrNull()?.coerceIn(0, 100) ?: 100
                val restrictionNotes = dialogBinding.etRestrictionNotes.text.toString().trim()
                
                if (name.isNotEmpty()) {
                    val selectedWorkstations = workstationAdapter.currentList
                        .filter { it.isChecked }
                        .map { it.workstation.id }
                    
                    lifecycleScope.launch {
                        viewModel.updateWorkerWithWorkstations(
                            worker.copy(
                                name = name, 
                                email = email,
                                availabilityPercentage = availability,
                                restrictionNotes = restrictionNotes
                            ),
                            selectedWorkstations
                        )
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}