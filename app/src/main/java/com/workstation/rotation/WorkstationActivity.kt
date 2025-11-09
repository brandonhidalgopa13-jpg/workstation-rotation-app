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
// Tutorial imports removed - functionality not available
import kotlinx.coroutines.launch

class WorkstationActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityWorkstationBinding
    private lateinit var adapter: WorkstationAdapter
    // Tutorial variables removed - functionality not available
    
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
        // Tutorial mode check removed - functionality not available
    }
    
    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
        
        binding.btnDeleteAllWorkstations.setOnClickListener {
            showDeleteAllWorkstationsDialog()
        }
    }
    
    private fun setupRecyclerView() {
        adapter = WorkstationAdapter(
            onEditClick = { workstation -> showEditDialog(workstation) },
            onDeleteClick = { workstation -> showDeleteWorkstationDialog(workstation) },
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
    
    /**
     * Muestra un diálogo de confirmación para eliminar una estación.
     */
    private fun showDeleteWorkstationDialog(workstation: Workstation) {
        // Verificar si la estación está siendo usada para entrenamiento
        lifecycleScope.launch {
            try {
                val isUsedForTraining = viewModel.isWorkstationUsedForTraining(workstation.id)
                val message = if (isUsedForTraining) {
                    "¿Estás seguro de que deseas eliminar la estación '${workstation.name}'?\n\n⚠️ ADVERTENCIA: Esta estación está siendo usada para entrenamiento. Al eliminarla, se afectarán los trabajadores en entrenamiento.\n\nEsta acción no se puede deshacer y se eliminarán todas las asignaciones de trabajadores a esta estación."
                } else {
                    "¿Estás seguro de que deseas eliminar la estación '${workstation.name}'?\n\nEsta acción no se puede deshacer y se eliminarán todas las asignaciones de trabajadores a esta estación."
                }
                
                AlertDialog.Builder(this@WorkstationActivity)
                    .setTitle("Eliminar Estación")
                    .setMessage(message)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton("Eliminar") { _, _ ->
                        lifecycleScope.launch {
                            try {
                                viewModel.deleteWorkstation(workstation)
                                // Mostrar mensaje de confirmación
                                android.widget.Toast.makeText(
                                    this@WorkstationActivity,
                                    "Estación '${workstation.name}' eliminada correctamente",
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                            } catch (e: Exception) {
                                // Mostrar mensaje de error
                                AlertDialog.Builder(this@WorkstationActivity)
                                    .setTitle("Error")
                                    .setMessage("No se pudo eliminar la estación: ${e.message}")
                                    .setPositiveButton("OK", null)
                                    .show()
                            }
                        }
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            } catch (e: Exception) {
                // Si hay error verificando, mostrar diálogo simple
                AlertDialog.Builder(this@WorkstationActivity)
                    .setTitle("Eliminar Estación")
                    .setMessage("¿Estás seguro de que deseas eliminar la estación '${workstation.name}'?\n\nEsta acción no se puede deshacer y se eliminarán todas las asignaciones de trabajadores a esta estación.")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton("Eliminar") { _, _ ->
                        lifecycleScope.launch {
                            try {
                                viewModel.deleteWorkstation(workstation)
                                android.widget.Toast.makeText(
                                    this@WorkstationActivity,
                                    "Estación '${workstation.name}' eliminada correctamente",
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                            } catch (e: Exception) {
                                AlertDialog.Builder(this@WorkstationActivity)
                                    .setTitle("Error")
                                    .setMessage("No se pudo eliminar la estación: ${e.message}")
                                    .setPositiveButton("OK", null)
                                    .show()
                            }
                        }
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }
        }
    }
    
    // Tutorial methods removed - functionality not available
    
    /**
     * Muestra un diálogo de confirmación para eliminar todas las estaciones.
     */
    private fun showDeleteAllWorkstationsDialog() {
        lifecycleScope.launch {
            try {
                val workstationCount = viewModel.allWorkstations.value?.size ?: 0
                
                if (workstationCount == 0) {
                    android.widget.Toast.makeText(
                        this@WorkstationActivity,
                        "No hay estaciones para eliminar",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                    return@launch
                }
                
                AlertDialog.Builder(this@WorkstationActivity)
                    .setTitle("⚠️ Eliminar Todas las Estaciones")
                    .setMessage(
                        "¿Estás COMPLETAMENTE SEGURO de que deseas eliminar TODAS las estaciones de trabajo?\n\n" +
                        "📊 Total de estaciones: $workstationCount\n\n" +
                        "⚠️ ADVERTENCIA CRÍTICA:\n" +
                        "• Esta acción NO SE PUEDE DESHACER\n" +
                        "• Se eliminarán TODAS las asignaciones de trabajadores\n" +
                        "• Se perderán TODAS las configuraciones de estaciones\n" +
                        "• Se afectarán TODAS las rotaciones existentes\n" +
                        "• El sistema quedará sin estaciones de trabajo\n\n" +
                        "Esta es una operación DESTRUCTIVA y PERMANENTE."
                    )
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton("SÍ, ELIMINAR TODO") { _, _ ->
                        // Segundo diálogo de confirmación
                        AlertDialog.Builder(this@WorkstationActivity)
                            .setTitle("⚠️ Confirmación Final")
                            .setMessage(
                                "Esta es tu ÚLTIMA OPORTUNIDAD para cancelar.\n\n" +
                                "¿Confirmas que deseas eliminar las $workstationCount estaciones?\n\n" +
                                "Esta acción es IRREVERSIBLE."
                            )
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setPositiveButton("CONFIRMAR ELIMINACIÓN") { _, _ ->
                                lifecycleScope.launch {
                                    try {
                                        viewModel.deleteAllWorkstations()
                                        android.widget.Toast.makeText(
                                            this@WorkstationActivity,
                                            "✅ Todas las estaciones han sido eliminadas",
                                            android.widget.Toast.LENGTH_LONG
                                        ).show()
                                    } catch (e: Exception) {
                                        AlertDialog.Builder(this@WorkstationActivity)
                                            .setTitle("Error")
                                            .setMessage("No se pudieron eliminar las estaciones: ${e.message}")
                                            .setPositiveButton("OK", null)
                                            .show()
                                    }
                                }
                            }
                            .setNegativeButton("Cancelar", null)
                            .show()
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            } catch (e: Exception) {
                AlertDialog.Builder(this@WorkstationActivity)
                    .setTitle("Error")
                    .setMessage("Error al verificar estaciones: ${e.message}")
                    .setPositiveButton("OK", null)
                    .show()
            }
        }
    }
}