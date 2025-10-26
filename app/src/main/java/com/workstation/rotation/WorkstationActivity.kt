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
import com.workstation.rotation.tutorial.TutorialManager
import com.workstation.rotation.tutorial.TutorialStep
import com.workstation.rotation.tutorial.GuideOverlay
import kotlinx.coroutines.launch

class WorkstationActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityWorkstationBinding
    private lateinit var adapter: WorkstationAdapter
    private var tutorialManager: TutorialManager? = null
    private var guideOverlay: GuideOverlay? = null
    private var isTutorialActive = false
    
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
        checkTutorialMode()
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
    
    /**
     * Verifica si el tutorial está activo y configura la guía.
     */
    private fun checkTutorialMode() {
        isTutorialActive = intent.getBooleanExtra("tutorial_active", false)
        val tutorialStep = intent.getStringExtra("tutorial_step")
        
        if (isTutorialActive && tutorialStep != null) {
            tutorialManager = TutorialManager(this)
            
            // Pequeño delay para que la UI se cargue
            binding.root.postDelayed({
                startGuidedTutorial(tutorialStep)
            }, 500)
        }
    }
    
    /**
     * Inicia el tutorial guiado específico para esta actividad.
     */
    private fun startGuidedTutorial(stepName: String) {
        when (stepName) {
            "WORKSTATIONS_INTRO" -> {
                showWorkstationIntroGuide()
            }
        }
    }
    
    /**
     * Muestra la guía de introducción a estaciones.
     */
    private fun showWorkstationIntroGuide() {
        // Resaltar el botón FAB
        guideOverlay = GuideOverlay.addToActivity(this, binding.fabAddWorkstation)
        
        // Mostrar diálogo explicativo
        AlertDialog.Builder(this)
            .setTitle("🏭 ¡Bienvenido a Estaciones!")
            .setMessage(
                "Aquí puedes gestionar todas las estaciones de trabajo.\n\n" +
                "👆 Toca el botón + (resaltado) para crear tu primera estación.\n\n" +
                "💡 Consejo: Crea al menos 3 estaciones diferentes para tener variedad en las rotaciones."
            )
            .setPositiveButton("Crear Estación") { _, _ ->
                removeGuideOverlay()
                // Abrir diálogo de creación automáticamente
                showAddDialog()
                showCreateStationGuide()
            }
            .setNegativeButton("Continuar Tutorial") { _, _ ->
                removeGuideOverlay()
                continueTutorial()
            }
            .setCancelable(false)
            .show()
    }
    
    /**
     * Muestra la guía para crear estación.
     */
    private fun showCreateStationGuide() {
        AlertDialog.Builder(this)
            .setTitle("📝 Creando tu Primera Estación")
            .setMessage(
                "Completa la información:\n\n" +
                "• Nombre: Ej. 'Control de Calidad'\n" +
                "• Trabajadores: Ej. 2\n" +
                "• Prioritaria: ✓ si es crítica\n\n" +
                "Después de crear esta estación, crea 2 más para tener variedad."
            )
            .setPositiveButton("Entendido") { _, _ ->
                // El usuario continuará creando la estación
            }
            .show()
    }
    
    /**
     * Continúa el tutorial a la siguiente actividad.
     */
    private fun continueTutorial() {
        tutorialManager?.continueAfterNavigation(
            TutorialStep.WORKSTATIONS_INTRO,
            onStepComplete = { },
            onNavigate = { step ->
                // Navegar a la siguiente actividad del tutorial
                val targetActivity = step.getTargetActivity()
                if (targetActivity != null) {
                    val intent = android.content.Intent(this, targetActivity)
                    intent.putExtra("tutorial_active", true)
                    intent.putExtra("tutorial_step", step.name)
                    startActivity(intent)
                    finish()
                }
            }
        )
    }
    
    /**
     * Remueve el overlay de guía.
     */
    private fun removeGuideOverlay() {
        guideOverlay?.let { overlay ->
            GuideOverlay.removeFromActivity(this, overlay)
            guideOverlay = null
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        removeGuideOverlay()
    }
}