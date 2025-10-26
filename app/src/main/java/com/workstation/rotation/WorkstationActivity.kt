package com.workstation.rotation

import android.os.Bundle
import android.widget.CheckBox
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
    
    /**
     * Shows dialog for adding a new workstation with validation.
     */
    private fun showAddDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_workstation, null)
        val etName = dialogView.findViewById<TextInputEditText>(R.id.etWorkstationName)
        val etRequiredWorkers = dialogView.findViewById<TextInputEditText>(R.id.etRequiredWorkers)
        val checkboxPriority = dialogView.findViewById<CheckBox>(R.id.checkboxPriority)
        
        AlertDialog.Builder(this)
            .setTitle("Agregar Estación de Trabajo")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                handleWorkstationSave(etName, etRequiredWorkers, checkboxPriority, null)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    
    /**
     * Handles saving workstation data with proper validation.
     */
    private fun handleWorkstationSave(
        etName: TextInputEditText,
        etRequiredWorkers: TextInputEditText,
        checkboxPriority: CheckBox,
        existingWorkstation: Workstation?
    ) {
        val name = etName.text.toString().trim()
        val requiredWorkersText = etRequiredWorkers.text.toString().trim()
        val requiredWorkers = requiredWorkersText.toIntOrNull()?.coerceIn(1, 50) ?: 1
        val isPriority = checkboxPriority.isChecked
        
        when {
            name.isEmpty() -> {
                etName.error = "El nombre es requerido"
                return
            }
            name.length < 3 -> {
                etName.error = "El nombre debe tener al menos 3 caracteres"
                return
            }
            requiredWorkers < 1 -> {
                etRequiredWorkers.error = "Debe requerir al menos 1 trabajador"
                return
            }
        }
        
        lifecycleScope.launch {
            try {
                if (existingWorkstation != null) {
                    viewModel.updateWorkstation(
                        existingWorkstation.copy(
                            name = name,
                            requiredWorkers = requiredWorkers,
                            isPriority = isPriority
                        )
                    )
                } else {
                    viewModel.insertWorkstation(
                        Workstation(
                            name = name,
                            requiredWorkers = requiredWorkers,
                            isPriority = isPriority
                        )
                    )
                }
            } catch (e: Exception) {
                // Handle error - could show a toast or snackbar
            }
        }
    }
    
    /**
     * Shows dialog for editing an existing workstation.
     */
    private fun showEditDialog(workstation: Workstation) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_workstation, null)
        val etName = dialogView.findViewById<TextInputEditText>(R.id.etWorkstationName)
        val etRequiredWorkers = dialogView.findViewById<TextInputEditText>(R.id.etRequiredWorkers)
        val checkboxPriority = dialogView.findViewById<CheckBox>(R.id.checkboxPriority)
        
        // Pre-populate fields with existing data
        etName.setText(workstation.name)
        etRequiredWorkers.setText(workstation.requiredWorkers.toString())
        checkboxPriority.isChecked = workstation.isPriority
        
        AlertDialog.Builder(this)
            .setTitle("Editar Estación de Trabajo")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                handleWorkstationSave(etName, etRequiredWorkers, checkboxPriority, workstation)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}