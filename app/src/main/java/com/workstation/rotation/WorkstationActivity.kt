package com.workstation.rotation

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.textfield.TextInputEditText
import com.workstation.rotation.adapters.WorkstationAdapter
import com.workstation.rotation.data.database.AppDatabase
import com.workstation.rotation.data.entities.Workstation
import com.workstation.rotation.databinding.ActivityWorkstationBinding
import com.workstation.rotation.viewmodels.WorkstationViewModel
import com.workstation.rotation.viewmodels.WorkstationViewModelFactory
import kotlinx.coroutines.launch

class WorkstationActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityWorkstationBinding
    private lateinit var adapter: WorkstationAdapter
    
    private val viewModel: WorkstationViewModel by viewModels {
        WorkstationViewModelFactory(AppDatabase.getDatabase(this).workstationDao())
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWorkstationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupRecyclerView()
        setupFab()
        observeWorkstations()
    }
    
    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }
    
    private fun setupRecyclerView() {
        adapter = WorkstationAdapter(
            onEditClick = { workstation -> showEditDialog(workstation) },
            onStatusChange = { workstation, isActive -> 
                lifecycleScope.launch {
                    viewModel.updateWorkstationStatus(workstation.id, isActive)
                }
            }
        )
        
        binding.recyclerViewWorkstations.apply {
            layoutManager = LinearLayoutManager(this@WorkstationActivity)
            adapter = this@WorkstationActivity.adapter
        }
    }
    
    private fun setupFab() {
        binding.fabAddWorkstation.setOnClickListener {
            showAddDialog()
        }
    }
    
    private fun observeWorkstations() {
        viewModel.allWorkstations.observe(this) { workstations ->
            adapter.submitList(workstations)
        }
    }
    
    private fun showAddDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_workstation, null)
        val etName = dialogView.findViewById<TextInputEditText>(R.id.etWorkstationName)
        val etRequiredWorkers = dialogView.findViewById<TextInputEditText>(R.id.etRequiredWorkers)
        val checkboxPriority = dialogView.findViewById<CheckBox>(R.id.checkboxPriority)
        
        AlertDialog.Builder(this)
            .setTitle("Agregar Estación de Trabajo")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                val name = etName.text.toString().trim()
                val requiredWorkersText = etRequiredWorkers.text.toString().trim()
                val requiredWorkers = requiredWorkersText.toIntOrNull() ?: 1
                val isPriority = checkboxPriority.isChecked
                
                if (name.isNotEmpty() && requiredWorkers > 0) {
                    lifecycleScope.launch {
                        viewModel.insertWorkstation(
                            Workstation(
                                name = name, 
                                requiredWorkers = requiredWorkers,
                                isPriority = isPriority
                            )
                        )
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    
    private fun showEditDialog(workstation: Workstation) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_workstation, null)
        val etName = dialogView.findViewById<TextInputEditText>(R.id.etWorkstationName)
        val etRequiredWorkers = dialogView.findViewById<TextInputEditText>(R.id.etRequiredWorkers)
        val checkboxPriority = dialogView.findViewById<CheckBox>(R.id.checkboxPriority)
        
        etName.setText(workstation.name)
        etRequiredWorkers.setText(workstation.requiredWorkers.toString())
        checkboxPriority.isChecked = workstation.isPriority
        
        AlertDialog.Builder(this)
            .setTitle("Editar Estación de Trabajo")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                val name = etName.text.toString().trim()
                val requiredWorkersText = etRequiredWorkers.text.toString().trim()
                val requiredWorkers = requiredWorkersText.toIntOrNull() ?: 1
                val isPriority = checkboxPriority.isChecked
                
                if (name.isNotEmpty() && requiredWorkers > 0) {
                    lifecycleScope.launch {
                        viewModel.updateWorkstation(
                            workstation.copy(
                                name = name, 
                                requiredWorkers = requiredWorkers,
                                isPriority = isPriority
                            )
                        )
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}