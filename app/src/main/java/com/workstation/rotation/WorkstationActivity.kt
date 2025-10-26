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
        
        // Set default value for required workers
        etRequiredWorkers.setText(com.workstation.rotation.utils.Constants.DEFAULT_REQUIRED_WORKERS.toString())
        
        // Add real-time validation
        setupRealTimeValidation(etName, etRequiredWorkers)
        
        val dialog = AlertDialog.Builder(this)
            .setTitle("Agregar Estación de Trabajo")
            .setView(dialogView)
            .setPositiveButton("Guardar", null) // Set to null initially
            .setNegativeButton("Cancelar", null)
            .create()
        
        dialog.show()
        
        // Override the positive button to add validation
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            if (validateAndSaveWorkstation(etName, etRequiredWorkers, checkboxPriority, null)) {
                dialog.dismiss()
            }
        }
    }
    
    /**
     * Validates input and saves workstation data.
     * @return true if validation passed and save was attempted, false otherwise
     */
    private fun validateAndSaveWorkstation(
        etName: TextInputEditText,
        etRequiredWorkers: TextInputEditText,
        checkboxPriority: CheckBox,
        existingWorkstation: Workstation?
    ): Boolean {
        val name = etName.text.toString().trim()
        val requiredWorkersText = etRequiredWorkers.text.toString().trim()
        val requiredWorkers = requiredWorkersText.toIntOrNull()?.coerceIn(
            com.workstation.rotation.utils.Constants.MIN_REQUIRED_WORKERS,
            com.workstation.rotation.utils.Constants.MAX_REQUIRED_WORKERS
        ) ?: com.workstation.rotation.utils.Constants.DEFAULT_REQUIRED_WORKERS
        val isPriority = checkboxPriority.isChecked
        
        // Clear previous errors
        etName.error = null
        etRequiredWorkers.error = null
        
        when {
            name.isEmpty() -> {
                etName.error = "El nombre es requerido"
                etName.requestFocus()
                return false
            }
            name.length < com.workstation.rotation.utils.Constants.MIN_NAME_LENGTH -> {
                etName.error = "El nombre debe tener al menos ${com.workstation.rotation.utils.Constants.MIN_NAME_LENGTH} caracteres"
                etName.requestFocus()
                return false
            }
            name.length > com.workstation.rotation.utils.Constants.MAX_NAME_LENGTH -> {
                etName.error = "El nombre no puede exceder ${com.workstation.rotation.utils.Constants.MAX_NAME_LENGTH} caracteres"
                etName.requestFocus()
                return false
            }
            requiredWorkersText.isEmpty() -> {
                etRequiredWorkers.error = "El número de trabajadores es requerido"
                etRequiredWorkers.requestFocus()
                return false
            }
            requiredWorkers < com.workstation.rotation.utils.Constants.MIN_REQUIRED_WORKERS -> {
                etRequiredWorkers.error = "Debe requerir al menos ${com.workstation.rotation.utils.Constants.MIN_REQUIRED_WORKERS} trabajador"
                etRequiredWorkers.requestFocus()
                return false
            }
            requiredWorkers > com.workstation.rotation.utils.Constants.MAX_REQUIRED_WORKERS -> {
                etRequiredWorkers.error = "No puede exceder ${com.workstation.rotation.utils.Constants.MAX_REQUIRED_WORKERS} trabajadores"
                etRequiredWorkers.requestFocus()
                return false
            }
        }
        
        // Launch coroutine for database operation
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
                // Success - no need to show message, the list will update automatically
            } catch (e: Exception) {
                // Show error message to user
                androidx.appcompat.app.AlertDialog.Builder(this@WorkstationActivity)
                    .setTitle("Error")
                    .setMessage("No se pudo guardar la estación: ${e.message}")
                    .setPositiveButton("OK", null)
                    .show()
            }
        }
        
        // Return true to indicate validation passed (database operation is async)
        return true
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
        
        // Add real-time validation
        setupRealTimeValidation(etName, etRequiredWorkers)
        
        val dialog = AlertDialog.Builder(this)
            .setTitle("Editar Estación de Trabajo")
            .setView(dialogView)
            .setPositiveButton("Guardar", null) // Set to null initially
            .setNegativeButton("Cancelar", null)
            .create()
        
        dialog.show()
        
        // Override the positive button to add validation
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            if (validateAndSaveWorkstation(etName, etRequiredWorkers, checkboxPriority, workstation)) {
                dialog.dismiss()
            }
        }
    }
    
    /**
     * Sets up real-time validation for input fields.
     */
    private fun setupRealTimeValidation(
        etName: TextInputEditText,
        etRequiredWorkers: TextInputEditText
    ) {
        // Name validation
        etName.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val name = etName.text.toString().trim()
                when {
                    name.isEmpty() -> etName.error = "El nombre es requerido"
                    name.length < com.workstation.rotation.utils.Constants.MIN_NAME_LENGTH -> 
                        etName.error = "Mínimo ${com.workstation.rotation.utils.Constants.MIN_NAME_LENGTH} caracteres"
                    name.length > com.workstation.rotation.utils.Constants.MAX_NAME_LENGTH -> 
                        etName.error = "Máximo ${com.workstation.rotation.utils.Constants.MAX_NAME_LENGTH} caracteres"
                    else -> etName.error = null
                }
            }
        }
        
        // Required workers validation
        etRequiredWorkers.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val text = etRequiredWorkers.text.toString().trim()
                val workers = text.toIntOrNull()
                when {
                    text.isEmpty() -> etRequiredWorkers.error = "Número requerido"
                    workers == null -> etRequiredWorkers.error = "Debe ser un número válido"
                    workers < com.workstation.rotation.utils.Constants.MIN_REQUIRED_WORKERS -> 
                        etRequiredWorkers.error = "Mínimo ${com.workstation.rotation.utils.Constants.MIN_REQUIRED_WORKERS}"
                    workers > com.workstation.rotation.utils.Constants.MAX_REQUIRED_WORKERS -> 
                        etRequiredWorkers.error = "Máximo ${com.workstation.rotation.utils.Constants.MAX_REQUIRED_WORKERS}"
                    else -> etRequiredWorkers.error = null
                }
            }
        }
    }
}