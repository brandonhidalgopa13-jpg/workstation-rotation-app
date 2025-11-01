package com.workstation.rotation

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.Spinner
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
import com.workstation.rotation.data.entities.Workstation
import com.workstation.rotation.databinding.ActivityWorkerBinding
import com.workstation.rotation.databinding.DialogAddWorkerBinding
import com.workstation.rotation.viewmodels.WorkerViewModel
import com.workstation.rotation.viewmodels.WorkerViewModelFactory
import kotlinx.coroutines.launch

/**
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 👥 ACTIVIDAD DE GESTIÓN DE TRABAJADORES - CENTRO DE RECURSOS HUMANOS
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 
 * 📋 FUNCIONES PRINCIPALES DE ESTA ACTIVIDAD:
 * 
 * 👤 1. GESTIÓN COMPLETA DE PERSONAL
 *    - Crear, editar y eliminar trabajadores del sistema
 *    - Configurar información básica (nombre, email, estado activo)
 *    - Gestionar disponibilidad y restricciones laborales
 * 
 * 🎓 2. SISTEMA DE ENTRENAMIENTO AVANZADO
 *    - Configurar trabajadores como entrenadores (👨‍🏫)
 *    - Asignar trabajadores como entrenados (🎯)
 *    - Establecer relaciones entrenador-entrenado
 *    - Seleccionar estaciones específicas para entrenamiento
 * 
 * 🏭 3. ASIGNACIÓN DE ESTACIONES DE TRABAJO
 *    - Seleccionar múltiples estaciones donde puede trabajar cada empleado
 *    - Sistema de checkboxes para selección flexible
 *    - Validación de que cada trabajador tenga al menos una estación
 * 
 * 📊 4. CONFIGURACIÓN DE DISPONIBILIDAD
 *    - Establecer porcentaje de disponibilidad (0-100%)
 *    - Agregar notas sobre restricciones específicas
 *    - Sistema visual de indicadores de disponibilidad
 * 
 * 📱 5. INTERFAZ INTUITIVA Y FUNCIONAL
 *    - Lista dinámica con RecyclerView para rendimiento óptimo
 *    - Diálogos modales para agregar/editar trabajadores
 *    - Floating Action Button para acceso rápido a creación
 *    - Indicadores visuales de estado y roles
 * 
 * 🔄 6. INTEGRACIÓN CON SISTEMA DE ROTACIÓN
 *    - Los trabajadores configurados aquí alimentan el algoritmo de rotación
 *    - Validación de datos para asegurar rotaciones exitosas
 *    - Sincronización automática con el motor de rotaciones
 * 
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 🔧 COMPONENTES TÉCNICOS:
 * 
 * • RecyclerView con adaptador personalizado para lista de trabajadores
 * • Diálogos personalizados con View Binding para formularios
 * • ViewModel con LiveData para gestión reactiva de estado
 * • Validación de formularios con feedback visual
 * • Operaciones CRUD asíncronas con corrutinas
 * 
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 */

class WorkerActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityWorkerBinding
    private lateinit var adapter: WorkerAdapter
    
    private val viewModel: WorkerViewModel by viewModels {
        WorkerViewModelFactory(
            AppDatabase.getDatabase(this).workerDao(),
            AppDatabase.getDatabase(this).workstationDao(),
            AppDatabase.getDatabase(this).workerRestrictionDao()
        )
    }
    
    // RotationViewModel para limpiar caché después de certificación
    private val rotationViewModel: com.workstation.rotation.viewmodels.RotationViewModel by viewModels {
        com.workstation.rotation.viewmodels.RotationViewModelFactory(
            AppDatabase.getDatabase(this).workerDao(),
            AppDatabase.getDatabase(this).workstationDao(),
            AppDatabase.getDatabase(this).workerRestrictionDao()
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
            onDeleteClick = { worker -> showDeleteWorkerDialog(worker) },
            onRestrictionsClick = { worker -> showRestrictionsDialog(worker) },
            onCertifyClick = { worker -> showCertifyWorkerDialog(worker) },
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
            // Actualizar spinner de liderazgo cuando cambien las selecciones
            if (dialogBinding.checkboxIsLeader.isChecked) {
                loadWorkstationsForLeadershipSpinner(dialogBinding)
            }
        }
        
        // Configuración básica del RecyclerView
        dialogBinding.recyclerViewWorkstations.apply {
            layoutManager = LinearLayoutManager(this@WorkerActivity)
            adapter = workstationAdapter
            visibility = android.view.View.VISIBLE
            
            // Debug: Verificar configuración inicial
            android.util.Log.d("WorkerActivity", "RecyclerView configurado - LayoutManager: ${layoutManager != null}, Adapter: ${adapter != null}")
        }
        
        // Setup training system
        setupTrainingSystem(dialogBinding)
        
        // Hide certification section for new workers
        dialogBinding.cardCertification.visibility = View.GONE
        
        // Load workstations directly from database
        lifecycleScope.launch {
            try {
                val workstations = viewModel.getActiveWorkstationsSync()
                
                // Debug: Log workstation count
                android.util.Log.d("WorkerActivity", "Cargadas ${workstations.size} estaciones activas")
                
                if (workstations.isEmpty()) {
                    android.widget.Toast.makeText(
                        this@WorkerActivity,
                        "⚠️ No hay estaciones activas. Crea estaciones primero.",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                } else {
                    // Debug: Log workstation names
                    workstations.forEach { station ->
                        android.util.Log.d("WorkerActivity", "Estación: ${station.name} (ID: ${station.id}, Activa: ${station.isActive})")
                    }
                }
                
                val checkItems = workstations.map { WorkstationCheckItem(it, false) }
                android.util.Log.d("WorkerActivity", "Enviando ${checkItems.size} items al adapter")
                
                // Verificar que el RecyclerView esté configurado
                android.util.Log.d("WorkerActivity", "RecyclerView adapter: ${dialogBinding.recyclerViewWorkstations.adapter}")
                android.util.Log.d("WorkerActivity", "RecyclerView layoutManager: ${dialogBinding.recyclerViewWorkstations.layoutManager}")
                android.util.Log.d("WorkerActivity", "RecyclerView visibility: ${dialogBinding.recyclerViewWorkstations.visibility}")
                
                workstationAdapter.submitList(checkItems)
                
                // Verificar que el adapter recibió los datos
                android.util.Log.d("WorkerActivity", "Adapter tiene ${workstationAdapter.itemCount} items después de submitList")
                
                // Forzar actualización del RecyclerView
                dialogBinding.recyclerViewWorkstations.post {
                    workstationAdapter.notifyDataSetChanged()
                    android.util.Log.d("WorkerActivity", "RecyclerView actualizado - Items visibles: ${dialogBinding.recyclerViewWorkstations.childCount}")
                }
                
                // Setup training workstation spinner
                val workstationNames = listOf("Seleccionar estación...") + workstations.map { it.name }
                val spinnerAdapter = ArrayAdapter<String>(this@WorkerActivity, android.R.layout.simple_spinner_item, workstationNames)
                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                dialogBinding.spinnerTrainingWorkstation.adapter = spinnerAdapter
                
                // Debug: Confirm adapter setup
                android.util.Log.d("WorkerActivity", "Adapter configurado con ${workstationNames.size} elementos")
                
            } catch (e: Exception) {
                // Handle error loading workstations
                android.util.Log.e("WorkerActivity", "Error cargando estaciones", e)
                android.widget.Toast.makeText(
                    this@WorkerActivity,
                    "Error cargando estaciones: ${e.message}",
                    android.widget.Toast.LENGTH_LONG
                ).show()
            }
        }
        
        AlertDialog.Builder(this)
            .setTitle("Agregar Trabajador")
            .setView(dialogBinding.root)
            .setPositiveButton("Guardar") { _, _ ->
                val name = dialogBinding.etWorkerName.text.toString().trim()
                val availabilityText = dialogBinding.etAvailabilityPercentage.text.toString().trim()
                val availability = availabilityText.toIntOrNull()?.coerceIn(0, 100) ?: 100
                val restrictionNotes = dialogBinding.etRestrictionNotes.text.toString().trim()
                val isTrainer = dialogBinding.checkboxIsTrainer.isChecked
                val isTrainee = dialogBinding.checkboxIsTrainee.isChecked
                val isLeader = dialogBinding.checkboxIsLeader.isChecked
                
                var trainerId: Long? = null
                var trainingWorkstationId: Long? = null
                var leaderWorkstationId: Long? = null
                var leadershipType = "BOTH"
                
                // Handle training and leadership data synchronously to avoid concurrency issues
                lifecycleScope.launch {
                    if (isTrainee) {
                        // Get selected trainer and workstation
                        val trainerPosition = dialogBinding.spinnerTrainer.selectedItemPosition
                        val workstationPosition = dialogBinding.spinnerTrainingWorkstation.selectedItemPosition
                        
                        if (trainerPosition > 0) {
                            val trainers = viewModel.getTrainers()
                            if (trainerPosition <= trainers.size) {
                                trainerId = trainers[trainerPosition - 1].id
                            }
                        }
                        
                        if (workstationPosition > 0) {
                            // Obtener las estaciones del entrenador seleccionado
                            val trainers = viewModel.getTrainers()
                            if (trainerPosition <= trainers.size) {
                                val selectedTrainer = trainers[trainerPosition - 1]
                                val trainerWorkstations = viewModel.getTrainerWorkstations(selectedTrainer.id)
                                if (workstationPosition <= trainerWorkstations.size) {
                                    trainingWorkstationId = trainerWorkstations[workstationPosition - 1].id
                                }
                            }
                        }
                    }
                    
                    if (isLeader) {
                        // Get selected leadership workstation
                        val leaderWorkstationPosition = dialogBinding.spinnerLeaderWorkstation.selectedItemPosition
                        if (leaderWorkstationPosition > 0) {
                            val workstations = viewModel.getActiveWorkstationsSync()
                            if (leaderWorkstationPosition <= workstations.size) {
                                leaderWorkstationId = workstations[leaderWorkstationPosition - 1].id
                            }
                        }
                        
                        // Get leadership type
                        leadershipType = when (dialogBinding.radioGroupLeadershipType.checkedRadioButtonId) {
                            dialogBinding.radioLeadershipFirst.id -> "FIRST_HALF"
                            dialogBinding.radioLeadershipSecond.id -> "SECOND_HALF"
                            else -> "BOTH"
                        }
                    }
                    
                    // Validate and save worker data
                    if (name.isNotEmpty()) {
                        val selectedWorkstations = workstationAdapter.currentList
                            .filter { it.isChecked }
                            .map { it.workstation.id }
                        
                        viewModel.insertWorkerWithWorkstations(
                            Worker(
                                name = name, 
                                email = "", // Email removido por simplicidad
                                availabilityPercentage = availability,
                                restrictionNotes = restrictionNotes,
                                isTrainer = isTrainer,
                                isTrainee = isTrainee,
                                trainerId = trainerId,
                                trainingWorkstationId = trainingWorkstationId,
                                isLeader = isLeader,
                                leaderWorkstationId = leaderWorkstationId,
                                leadershipType = leadershipType
                            ),
                            selectedWorkstations
                        )
                        
                        // Limpiar caché del RotationViewModel después de crear trabajador
                        rotationViewModel.clearWorkerWorkstationCache()
                        android.util.Log.d("WorkerActivity", "Caché del RotationViewModel limpiado después de crear trabajador")
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    
    /**
     * Sets up the training system UI components and their interactions.
     */
    private fun setupTrainingSystem(dialogBinding: DialogAddWorkerBinding) {
        with(dialogBinding) {
            // Setup trainee checkbox listener
            checkboxIsTrainee.setOnCheckedChangeListener { _, isChecked ->
                layoutTrainingDetails.visibility = if (isChecked) View.VISIBLE else View.GONE
                
                if (isChecked) {
                    loadTrainersForSpinner(dialogBinding)
                    setupTrainerSelectionListener(dialogBinding)
                }
            }
            
            // Prevent trainer and trainee from being selected simultaneously
            checkboxIsTrainer.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    checkboxIsTrainee.isChecked = false
                    layoutTrainingDetails.visibility = View.GONE
                }
            }
            
            // Setup leadership system
            setupLeadershipSystem(dialogBinding)
        }
    }
    

    
    /**
     * Sets up the leadership system UI components and their interactions.
     */
    private fun setupLeadershipSystem(dialogBinding: DialogAddWorkerBinding, workerId: Long? = null) {
        with(dialogBinding) {
            // Setup leader checkbox listener
            checkboxIsLeader.setOnCheckedChangeListener { _, isChecked ->
                layoutLeadershipDetails.visibility = if (isChecked) View.VISIBLE else View.GONE
                
                if (isChecked) {
                    loadWorkstationsForLeadershipSpinner(dialogBinding, workerId)
                }
            }
        }
    }
    
    /**
     * Loads available workstations for leadership assignment.
     * Only shows workstations where the worker is assigned.
     */
    private fun loadWorkstationsForLeadershipSpinner(dialogBinding: DialogAddWorkerBinding, workerId: Long? = null) {
        lifecycleScope.launch {
            try {
                val availableWorkstations = if (workerId != null) {
                    // Para edición: filtrar solo estaciones asignadas al trabajador
                    val assignedWorkstationIds = viewModel.getWorkerWorkstationIds(workerId)
                    val allWorkstations = viewModel.getActiveWorkstationsSync()
                    allWorkstations.filter { assignedWorkstationIds.contains(it.id) }
                } else {
                    // Para nuevo trabajador: mostrar estaciones seleccionadas en el diálogo
                    val selectedWorkstations = dialogBinding.recyclerViewWorkstations.adapter?.let { adapter ->
                        if (adapter is WorkstationCheckboxAdapter) {
                            adapter.currentList.filter { it.isChecked }.map { it.workstation }
                        } else emptyList()
                    } ?: emptyList()
                    selectedWorkstations
                }
                
                val workstationNames = listOf("Seleccionar estación...") + availableWorkstations.map { it.name }
                val leaderWorkstationAdapter = ArrayAdapter<String>(
                    this@WorkerActivity,
                    android.R.layout.simple_spinner_item,
                    workstationNames
                )
                leaderWorkstationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                dialogBinding.spinnerLeaderWorkstation.adapter = leaderWorkstationAdapter
                
                android.util.Log.d("WorkerActivity", "Cargadas ${availableWorkstations.size} estaciones para liderazgo")
            } catch (e: Exception) {
                android.util.Log.e("WorkerActivity", "Error loading workstations for leadership", e)
            }
        }
    }
    
    /**
     * Loads available trainers into the spinner.
     */
    private fun loadTrainersForSpinner(dialogBinding: DialogAddWorkerBinding) {
        lifecycleScope.launch {
            try {
                val trainers = viewModel.getTrainers()
                val trainerNames = listOf("Seleccionar entrenador...") + trainers.map { it.name }
                val trainerAdapter = ArrayAdapter<String>(
                    this@WorkerActivity,
                    android.R.layout.simple_spinner_item,
                    trainerNames
                )
                trainerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                dialogBinding.spinnerTrainer.adapter = trainerAdapter
            } catch (e: Exception) {
                // Handle error loading trainers
            }
        }
    }
    
    /**
     * Configura el listener del spinner de entrenadores para filtrar estaciones.
     */
    private fun setupTrainerSelectionListener(dialogBinding: DialogAddWorkerBinding) {
        dialogBinding.spinnerTrainer.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                if (position > 0) { // Si no es "Seleccionar entrenador..."
                    lifecycleScope.launch {
                        try {
                            val trainers = viewModel.getTrainers()
                            if (position <= trainers.size) {
                                val selectedTrainer = trainers[position - 1]
                                loadTrainerWorkstations(dialogBinding, selectedTrainer.id)
                            }
                        } catch (e: Exception) {
                            // Handle error
                        }
                    }
                } else {
                    // Si no hay entrenador seleccionado, limpiar estaciones
                    clearWorkstationSpinner(dialogBinding)
                }
            }
            
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {
                clearWorkstationSpinner(dialogBinding)
            }
        }
    }
    
    /**
     * Carga las estaciones donde puede trabajar el entrenador seleccionado.
     */
    private fun loadTrainerWorkstations(dialogBinding: DialogAddWorkerBinding, trainerId: Long) {
        lifecycleScope.launch {
            try {
                val trainerWorkstations = viewModel.getTrainerWorkstations(trainerId)
                android.util.Log.d("WorkerActivity", "Entrenador $trainerId tiene ${trainerWorkstations.size} estaciones disponibles")
                trainerWorkstations.forEach { station ->
                    android.util.Log.d("WorkerActivity", "- Estación: ${station.name} (ID: ${station.id})")
                }
                
                if (trainerWorkstations.isEmpty()) {
                    // Si el entrenador no tiene estaciones asignadas
                    val emptyAdapter = ArrayAdapter<String>(
                        this@WorkerActivity,
                        android.R.layout.simple_spinner_item,
                        listOf("El entrenador no tiene estaciones asignadas")
                    )
                    emptyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    dialogBinding.spinnerTrainingWorkstation.adapter = emptyAdapter
                    
                    android.widget.Toast.makeText(
                        this@WorkerActivity,
                        "⚠️ El entrenador seleccionado no tiene estaciones asignadas",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                } else {
                    val workstationNames: List<String> = listOf("Seleccionar estación...") + trainerWorkstations.map { it.name }
                    val workstationAdapter = ArrayAdapter<String>(
                        this@WorkerActivity,
                        android.R.layout.simple_spinner_item,
                        workstationNames
                    )
                    workstationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    dialogBinding.spinnerTrainingWorkstation.adapter = workstationAdapter
                }
            } catch (e: Exception) {
                android.util.Log.e("WorkerActivity", "Error cargando estaciones del entrenador", e)
                android.widget.Toast.makeText(
                    this@WorkerActivity,
                    "Error cargando estaciones del entrenador: ${e.message}",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    
    /**
     * Limpia el spinner de estaciones cuando no hay entrenador seleccionado.
     */
    private fun clearWorkstationSpinner(dialogBinding: DialogAddWorkerBinding) {
        val emptyAdapter = ArrayAdapter<String>(
            this@WorkerActivity,
            android.R.layout.simple_spinner_item,
            listOf("Primero selecciona un entrenador")
        )
        emptyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        dialogBinding.spinnerTrainingWorkstation.adapter = emptyAdapter
    }
    
    /**
     * Sets up the training system UI components for edit dialog.
     */
    private fun setupTrainingSystemForEdit(dialogBinding: DialogAddWorkerBinding, worker: Worker) {
        with(dialogBinding) {
            // Setup trainee checkbox listener
            checkboxIsTrainee.setOnCheckedChangeListener { _, isChecked ->
                layoutTrainingDetails.visibility = if (isChecked) View.VISIBLE else View.GONE
                
                if (isChecked) {
                    loadTrainersForSpinner(dialogBinding)
                    setupTrainerSelectionListener(dialogBinding)
                }
            }
            
            // Prevent trainer and trainee from being selected simultaneously
            checkboxIsTrainer.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    checkboxIsTrainee.isChecked = false
                    layoutTrainingDetails.visibility = View.GONE
                }
            }
            
            // Setup leadership system with worker ID for filtering
            setupLeadershipSystem(dialogBinding, worker.id)
            
            // If worker is currently a trainee, show training details
            if (worker.isTrainee) {
                layoutTrainingDetails.visibility = View.VISIBLE
                loadTrainersForSpinner(dialogBinding)
                setupTrainerSelectionListener(dialogBinding)
            }
        }
    }
    
    /**
     * Loads available trainers into the spinner for edit dialog and selects current trainer.
     */
    private fun loadTrainersForEditSpinner(dialogBinding: DialogAddWorkerBinding, worker: Worker) {
        lifecycleScope.launch {
            try {
                val trainers = viewModel.getTrainers()
                val trainerNames = listOf("Seleccionar entrenador...") + trainers.map { it.name }
                val trainerAdapter = ArrayAdapter<String>(
                    this@WorkerActivity,
                    android.R.layout.simple_spinner_item,
                    trainerNames
                )
                trainerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                dialogBinding.spinnerTrainer.adapter = trainerAdapter
                
                // Select current trainer if exists
                worker.trainerId?.let { trainerId ->
                    val trainerIndex = trainers.indexOfFirst { it.id == trainerId }
                    if (trainerIndex >= 0) {
                        dialogBinding.spinnerTrainer.setSelection(trainerIndex + 1) // +1 because of "Seleccionar..." option
                    }
                }
            } catch (e: Exception) {
                // Handle error loading trainers
            }
        }
    }
    
    /**
     * Configura el listener del spinner de entrenadores para el diálogo de edición.
     */
    private fun setupTrainerSelectionListenerForEdit(dialogBinding: DialogAddWorkerBinding, worker: Worker) {
        dialogBinding.spinnerTrainer.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                if (position > 0) { // Si no es "Seleccionar entrenador..."
                    lifecycleScope.launch {
                        try {
                            val trainers = viewModel.getTrainers()
                            if (position <= trainers.size) {
                                val selectedTrainer = trainers[position - 1]
                                loadTrainerWorkstationsForEdit(dialogBinding, selectedTrainer.id, worker)
                            }
                        } catch (e: Exception) {
                            // Handle error
                        }
                    }
                } else {
                    // Si no hay entrenador seleccionado, limpiar estaciones
                    clearWorkstationSpinner(dialogBinding)
                }
            }
            
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {
                clearWorkstationSpinner(dialogBinding)
            }
        }
        
        // Si el trabajador ya tiene un entrenador, cargar sus estaciones inmediatamente
        worker.trainerId?.let { trainerId ->
            lifecycleScope.launch {
                loadTrainerWorkstationsForEdit(dialogBinding, trainerId, worker)
            }
        }
    }
    
    /**
     * Carga las estaciones del entrenador para el diálogo de edición y selecciona la actual si existe.
     */
    private fun loadTrainerWorkstationsForEdit(dialogBinding: DialogAddWorkerBinding, trainerId: Long, worker: Worker) {
        lifecycleScope.launch {
            try {
                val trainerWorkstations = viewModel.getTrainerWorkstations(trainerId)
                android.util.Log.d("WorkerActivity", "Edición - Entrenador $trainerId tiene ${trainerWorkstations.size} estaciones disponibles")
                trainerWorkstations.forEach { station ->
                    android.util.Log.d("WorkerActivity", "- Estación: ${station.name} (ID: ${station.id})")
                }
                
                if (trainerWorkstations.isEmpty()) {
                    // Si el entrenador no tiene estaciones asignadas
                    val emptyAdapter = ArrayAdapter<String>(
                        this@WorkerActivity,
                        android.R.layout.simple_spinner_item,
                        listOf("El entrenador no tiene estaciones asignadas")
                    )
                    emptyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    dialogBinding.spinnerTrainingWorkstation.adapter = emptyAdapter
                    
                    android.widget.Toast.makeText(
                        this@WorkerActivity,
                        "⚠️ El entrenador seleccionado no tiene estaciones asignadas",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                } else {
                    val workstationNames: List<String> = listOf("Seleccionar estación...") + trainerWorkstations.map { it.name }
                    val workstationAdapter = ArrayAdapter<String>(
                        this@WorkerActivity,
                        android.R.layout.simple_spinner_item,
                        workstationNames
                    )
                    workstationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    dialogBinding.spinnerTrainingWorkstation.adapter = workstationAdapter
                    
                    // Select current training workstation if exists and is available for this trainer
                    worker.trainingWorkstationId?.let { trainingWorkstationId ->
                        val workstationIndex = trainerWorkstations.indexOfFirst { it.id == trainingWorkstationId }
                        if (workstationIndex >= 0) {
                            android.util.Log.d("WorkerActivity", "Seleccionando estación actual: ${trainerWorkstations[workstationIndex].name}")
                            dialogBinding.spinnerTrainingWorkstation.setSelection(workstationIndex + 1) // +1 because of "Seleccionar..." option
                        } else {
                            android.util.Log.w("WorkerActivity", "La estación de entrenamiento actual (ID: $trainingWorkstationId) no está disponible para este entrenador")
                            android.widget.Toast.makeText(
                                this@WorkerActivity,
                                "⚠️ La estación de entrenamiento actual no está disponible para este entrenador",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("WorkerActivity", "Error cargando estaciones del entrenador para edición", e)
                android.widget.Toast.makeText(
                    this@WorkerActivity,
                    "Error cargando estaciones del entrenador: ${e.message}",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    
    private fun showDeleteWorkerDialog(worker: Worker) {
        lifecycleScope.launch {
            try {
                val hasTrainees = if (worker.isTrainer) {
                    viewModel.hasTrainees(worker.id)
                } else false
                
                val message = when {
                    worker.isTrainer && hasTrainees -> 
                        "¿Estás seguro de que deseas eliminar a '${worker.name}'?\n\n⚠️ ADVERTENCIA: Este trabajador es entrenador y tiene trabajadores asignados. Al eliminarlo, se afectarán los trabajadores en entrenamiento.\n\nEsta acción no se puede deshacer y se eliminarán todas sus asignaciones de estaciones."
                    worker.isTrainer -> 
                        "¿Estás seguro de que deseas eliminar a '${worker.name}'?\n\n👨‍🏫 Este trabajador es entrenador.\n\nEsta acción no se puede deshacer y se eliminarán todas sus asignaciones de estaciones."
                    worker.isTrainee -> 
                        "¿Estás seguro de que deseas eliminar a '${worker.name}'?\n\n🎯 Este trabajador está en entrenamiento.\n\nEsta acción no se puede deshacer y se eliminarán todas sus asignaciones de estaciones."
                    else -> 
                        "¿Estás seguro de que deseas eliminar a '${worker.name}'?\n\nEsta acción no se puede deshacer y se eliminarán todas sus asignaciones de estaciones."
                }
                
                AlertDialog.Builder(this@WorkerActivity)
                    .setTitle("Eliminar Trabajador")
                    .setMessage(message)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton("Eliminar") { _, _ ->
                        lifecycleScope.launch {
                            try {
                                viewModel.deleteWorker(worker)
                                // Mostrar mensaje de confirmación
                                android.widget.Toast.makeText(
                                    this@WorkerActivity,
                                    "Trabajador '${worker.name}' eliminado correctamente",
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                            } catch (e: Exception) {
                                // Mostrar mensaje de error
                                AlertDialog.Builder(this@WorkerActivity)
                                    .setTitle("Error")
                                    .setMessage("No se pudo eliminar el trabajador: ${e.message}")
                                    .setPositiveButton("OK", null)
                                    .show()
                            }
                        }
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            } catch (e: Exception) {
                // Si hay error verificando, mostrar diálogo simple
                AlertDialog.Builder(this@WorkerActivity)
                    .setTitle("Eliminar Trabajador")
                    .setMessage("¿Estás seguro de que deseas eliminar a '${worker.name}'?\n\nEsta acción no se puede deshacer y se eliminarán todas sus asignaciones de estaciones.")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton("Eliminar") { _, _ ->
                        lifecycleScope.launch {
                            try {
                                viewModel.deleteWorker(worker)
                                android.widget.Toast.makeText(
                                    this@WorkerActivity,
                                    "Trabajador '${worker.name}' eliminado correctamente",
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                            } catch (e: Exception) {
                                AlertDialog.Builder(this@WorkerActivity)
                                    .setTitle("Error")
                                    .setMessage("No se pudo eliminar el trabajador: ${e.message}")
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
    
    /**
     * Muestra el diálogo para gestionar restricciones específicas de estaciones.
     */
    private fun showRestrictionsDialog(worker: Worker) {
        val dialogBinding = com.workstation.rotation.databinding.DialogWorkerRestrictionsBinding.inflate(layoutInflater)
        val restrictionAdapter = com.workstation.rotation.adapters.WorkstationRestrictionAdapter { item, isRestricted ->
            // El adapter maneja el cambio automáticamente
        }
        
        dialogBinding.recyclerViewRestrictions.apply {
            layoutManager = LinearLayoutManager(this@WorkerActivity)
            adapter = restrictionAdapter
        }
        
        // Configurar spinner de tipos de restricción
        val restrictionTypes = arrayOf("Prohibido", "Limitado", "Temporal")
        val spinnerAdapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, restrictionTypes)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        dialogBinding.spinnerRestrictionType.adapter = spinnerAdapter
        
        // Cargar datos
        lifecycleScope.launch {
            try {
                val workstations = viewModel.activeWorkstations.value ?: emptyList()
                val existingRestrictions = viewModel.getWorkerRestrictionsSync(worker.id)
                
                val restrictionItems = workstations.map { workstation ->
                    val existingRestriction = existingRestrictions.find { it.workstationId == workstation.id }
                    com.workstation.rotation.adapters.WorkstationRestrictionItem(
                        workstation = workstation,
                        isRestricted = existingRestriction != null,
                        restriction = existingRestriction
                    )
                }
                
                restrictionAdapter.submitList(restrictionItems)
                
                // Si hay restricciones existentes, usar las notas de la primera
                existingRestrictions.firstOrNull()?.let { restriction ->
                    dialogBinding.etRestrictionNotes.setText(restriction.notes)
                    val typeIndex = when (restriction.restrictionType) {
                        com.workstation.rotation.data.entities.RestrictionType.PROHIBITED -> 0
                        com.workstation.rotation.data.entities.RestrictionType.LIMITED -> 1
                        com.workstation.rotation.data.entities.RestrictionType.TEMPORARY -> 2
                    }
                    dialogBinding.spinnerRestrictionType.setSelection(typeIndex)
                }
                
            } catch (e: Exception) {
                android.widget.Toast.makeText(
                    this@WorkerActivity,
                    "Error cargando restricciones: ${e.message}",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        }
        
        AlertDialog.Builder(this)
            .setTitle("Restricciones de ${worker.name}")
            .setView(dialogBinding.root)
            .setPositiveButton("Guardar") { _, _ ->
                lifecycleScope.launch {
                    try {
                        val restrictedWorkstations = restrictionAdapter.currentList
                            .filter { it.isRestricted }
                            .map { it.workstation.id }
                        
                        val notes = dialogBinding.etRestrictionNotes.text.toString().trim()
                        val restrictionType = when (dialogBinding.spinnerRestrictionType.selectedItemPosition) {
                            0 -> com.workstation.rotation.data.entities.RestrictionType.PROHIBITED
                            1 -> com.workstation.rotation.data.entities.RestrictionType.LIMITED
                            2 -> com.workstation.rotation.data.entities.RestrictionType.TEMPORARY
                            else -> com.workstation.rotation.data.entities.RestrictionType.PROHIBITED
                        }
                        
                        viewModel.saveWorkerRestrictions(worker.id, restrictedWorkstations, restrictionType, notes)
                        
                        // Actualizar el campo restrictionNotes del trabajador para mostrar en la lista
                        val restrictionCount = restrictedWorkstations.size
                        val updatedNotes = if (restrictionCount > 0) {
                            "$restrictionCount estación${if (restrictionCount > 1) "es" else ""} restringida${if (restrictionCount > 1) "s" else ""}"
                        } else {
                            ""
                        }
                        
                        val updatedWorker = worker.copy(restrictionNotes = updatedNotes)
                        viewModel.updateWorkerWithWorkstations(updatedWorker, viewModel.getWorkerWorkstationIds(worker.id))
                        
                        android.widget.Toast.makeText(
                            this@WorkerActivity,
                            "Restricciones guardadas correctamente",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                        
                    } catch (e: Exception) {
                        AlertDialog.Builder(this@WorkerActivity)
                            .setTitle("Error")
                            .setMessage("No se pudieron guardar las restricciones: ${e.message}")
                            .setPositiveButton("OK", null)
                            .show()
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    /**
     * Muestra el diálogo de certificación para un trabajador específico.
     */
    private fun showCertifyWorkerDialog(worker: Worker) {
        // Verificar que el trabajador esté en entrenamiento
        if (!worker.isTrainee) {
            android.widget.Toast.makeText(
                this,
                "Este trabajador no está en entrenamiento",
                android.widget.Toast.LENGTH_SHORT
            ).show()
            return
        }
        
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("🎓 Certificar Trabajador")
            .setMessage(
                "¿Deseas certificar a '${worker.name}'?\n\n" +
                "✅ Al certificar, el trabajador:\n" +
                "• Deja de estar 'en entrenamiento'\n" +
                "• Ya no necesita estar con su entrenador\n" +
                "• Puede participar normalmente en rotaciones\n" +
                "• Se convierte en trabajador completamente capacitado\n" +
                "• Se marca como certificado 🏆\n" +
                "• La estación de entrenamiento se agrega automáticamente\n\n" +
                "Esta acción se puede revertir editando el trabajador."
            )
            .setIcon(android.R.drawable.ic_dialog_info)
            .setPositiveButton("🎓 Certificar") { _, _ ->
                lifecycleScope.launch {
                    try {
                        // Obtener nombre de la estación de entrenamiento antes de certificar
                        val trainingStationName = worker.trainingWorkstationId?.let { stationId ->
                            viewModel.getWorkstationById(stationId)?.name
                        }
                        
                        // Usar el método de certificación completa
                        val success = viewModel.certifyWorkerComplete(worker.id)
                        
                        // CRÍTICO: Limpiar caché del RotationViewModel después de certificación
                        rotationViewModel.clearWorkerWorkstationCache()
                        android.util.Log.d("WorkerActivity", "Caché del RotationViewModel limpiado después de certificación")
                        
                        // Debug: Verificar estado después de certificación
                        val debugReport = viewModel.debugWorkerCertificationState(worker.id)
                        android.util.Log.d("WorkerActivity", "Estado post-certificación:\n$debugReport")
                        
                        if (success) {
                            // Mostrar mensaje de éxito
                            androidx.appcompat.app.AlertDialog.Builder(this@WorkerActivity)
                                .setTitle("✅ Certificación Completada")
                                .setMessage(
                                    "¡Felicitaciones! 🎉\n\n" +
                                    "'${worker.name}' ha sido certificado exitosamente.\n\n" +
                                    "El trabajador:\n" +
                                    "✅ Ya no está en entrenamiento\n" +
                                    "✅ Puede participar normalmente en rotaciones\n" +
                                    "✅ Es considerado completamente capacitado\n" +
                                    "✅ Tiene fecha de certificación registrada\n" +
                                    (if (trainingStationName != null) "✅ Estación '$trainingStationName' agregada automáticamente\n" else "") +
                                    "\nLos cambios se aplicarán en la próxima rotación generada."
                                )
                                .setPositiveButton("🎉 ¡Excelente!", null)
                                .show()
                        } else {
                            // Mostrar mensaje de error
                            androidx.appcompat.app.AlertDialog.Builder(this@WorkerActivity)
                                .setTitle("❌ Error en Certificación")
                                .setMessage(
                                    "No se pudo completar la certificación de '${worker.name}'.\n\n" +
                                    "Posibles causas:\n" +
                                    "• El trabajador no está en entrenamiento\n" +
                                    "• Error en la base de datos\n" +
                                    "• Problema con las asignaciones de estaciones\n\n" +
                                    "Por favor, verifica el estado del trabajador e intenta nuevamente."
                                )
                                .setPositiveButton("OK", null)
                                .show()
                        }
                        
                    } catch (e: Exception) {
                        androidx.appcompat.app.AlertDialog.Builder(this@WorkerActivity)
                            .setTitle("❌ Error")
                            .setMessage("Error inesperado durante la certificación: ${e.message}")
                            .setPositiveButton("OK", null)
                            .show()
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    
    /**
     * Muestra el diálogo de edición para un trabajador específico.
     */
    private fun showEditDialog(worker: Worker) {
        val dialogBinding = DialogAddWorkerBinding.inflate(layoutInflater)
        val workstationAdapter = WorkstationCheckboxAdapter { item, isChecked ->
            item.isChecked = isChecked
        }
        
        // Pre-llenar los campos con los datos actuales del trabajador
        dialogBinding.apply {
            etWorkerName.setText(worker.name)
            etAvailabilityPercentage.setText(worker.availabilityPercentage.toString())
            etRestrictionNotes.setText(worker.restrictionNotes)
            checkboxIsTrainer.isChecked = worker.isTrainer
            checkboxIsTrainee.isChecked = worker.isTrainee
            checkboxIsCertified.isChecked = worker.isCertified
            checkboxIsLeader.isChecked = worker.isLeader
        }
        
        // Configurar RecyclerView
        dialogBinding.recyclerViewWorkstations.apply {
            layoutManager = LinearLayoutManager(this@WorkerActivity)
            adapter = workstationAdapter
            isNestedScrollingEnabled = true
            setHasFixedSize(true)
            setItemViewCacheSize(20)
            isVerticalScrollBarEnabled = true
            scrollBarStyle = android.view.View.SCROLLBARS_OUTSIDE_OVERLAY
        }
        
        // Setup training and leadership systems
        setupTrainingSystemForEdit(dialogBinding, worker)
        
        // Mostrar sección de certificación si aplica
        if (worker.canBeCertified()) {
            dialogBinding.cardCertification.visibility = View.VISIBLE
        } else {
            dialogBinding.cardCertification.visibility = View.GONE
        }
        
        // Cargar datos
        lifecycleScope.launch {
            try {
                val assignedIds = viewModel.getWorkerWorkstationIds(worker.id)
                val workstations = viewModel.getActiveWorkstationsSync()
                
                val checkItems = workstations.map { workstation ->
                    WorkstationCheckItem(workstation, assignedIds.contains(workstation.id))
                }
                workstationAdapter.submitList(checkItems)
                
                // Setup spinners
                setupEditSpinners(dialogBinding, worker, workstations)
                
            } catch (e: Exception) {
                android.widget.Toast.makeText(
                    this@WorkerActivity,
                    "Error cargando datos: ${e.message}",
                    android.widget.Toast.LENGTH_LONG
                ).show()
            }
        }
        
        AlertDialog.Builder(this)
            .setTitle("Editar Trabajador")
            .setView(dialogBinding.root)
            .setPositiveButton("Guardar") { _, _ ->
                saveEditedWorker(dialogBinding, worker, workstationAdapter)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    
    /**
     * Configura los spinners para el diálogo de edición.
     */
    private suspend fun setupEditSpinners(
        dialogBinding: DialogAddWorkerBinding,
        worker: Worker,
        workstations: List<Workstation>
    ) {
        // Setup training workstation spinner
        val workstationNames = listOf("Seleccionar estación...") + workstations.map { it.name }
        val spinnerAdapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, workstationNames)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        dialogBinding.spinnerTrainingWorkstation.adapter = spinnerAdapter
        
        // Setup leadership workstation spinner
        dialogBinding.spinnerLeaderWorkstation.adapter = spinnerAdapter
        
        // Set current selections
        worker.trainingWorkstationId?.let { trainingId ->
            val trainingIndex = workstations.indexOfFirst { it.id == trainingId } + 1
            if (trainingIndex > 0) {
                dialogBinding.spinnerTrainingWorkstation.setSelection(trainingIndex)
            }
        }
        
        worker.leaderWorkstationId?.let { leaderId ->
            val leaderIndex = workstations.indexOfFirst { it.id == leaderId } + 1
            if (leaderIndex > 0) {
                dialogBinding.spinnerLeaderWorkstation.setSelection(leaderIndex)
            }
        }
        
        // Set leadership type
        when (worker.leadershipType) {
            "FIRST_HALF" -> dialogBinding.radioLeadershipFirst.isChecked = true
            "SECOND_HALF" -> dialogBinding.radioLeadershipSecond.isChecked = true
            else -> dialogBinding.radioLeadershipBoth.isChecked = true
        }
        
        // Setup trainer spinner if needed
        if (worker.isTrainee) {
            loadTrainersForSpinner(dialogBinding)
            worker.trainerId?.let { trainerId ->
                val trainers = viewModel.getTrainers()
                val trainerIndex = trainers.indexOfFirst { it.id == trainerId } + 1
                if (trainerIndex > 0) {
                    dialogBinding.spinnerTrainer.setSelection(trainerIndex)
                }
            }
        }
    }
    
    /**
     * Guarda los cambios del trabajador editado.
     */
    private fun saveEditedWorker(
        dialogBinding: DialogAddWorkerBinding,
        originalWorker: Worker,
        workstationAdapter: WorkstationCheckboxAdapter
    ) {
        lifecycleScope.launch {
            try {
                val name = dialogBinding.etWorkerName.text.toString().trim()
                val availabilityText = dialogBinding.etAvailabilityPercentage.text.toString().trim()
                val availability = availabilityText.toIntOrNull()?.coerceIn(0, 100) ?: 100
                val restrictionNotes = dialogBinding.etRestrictionNotes.text.toString().trim()
                val isTrainer = dialogBinding.checkboxIsTrainer.isChecked
                val isTrainee = dialogBinding.checkboxIsTrainee.isChecked
                val isCertified = dialogBinding.checkboxIsCertified.isChecked
                val isLeader = dialogBinding.checkboxIsLeader.isChecked
                
                var trainerId: Long? = null
                var trainingWorkstationId: Long? = null
                var leaderWorkstationId: Long? = null
                var leadershipType = "BOTH"
                
                // Handle training data
                if (isTrainee) {
                    val trainerPosition = dialogBinding.spinnerTrainer.selectedItemPosition
                    val workstationPosition = dialogBinding.spinnerTrainingWorkstation.selectedItemPosition
                    
                    if (trainerPosition > 0) {
                        val trainers = viewModel.getTrainers()
                        if (trainerPosition <= trainers.size) {
                            trainerId = trainers[trainerPosition - 1].id
                        }
                    }
                    
                    if (workstationPosition > 0) {
                        val workstations = viewModel.getActiveWorkstationsSync()
                        if (workstationPosition <= workstations.size) {
                            trainingWorkstationId = workstations[workstationPosition - 1].id
                        }
                    }
                }
                
                // Handle leadership data
                if (isLeader) {
                    val leaderWorkstationPosition = dialogBinding.spinnerLeaderWorkstation.selectedItemPosition
                    if (leaderWorkstationPosition > 0) {
                        val workstations = viewModel.getActiveWorkstationsSync()
                        if (leaderWorkstationPosition <= workstations.size) {
                            leaderWorkstationId = workstations[leaderWorkstationPosition - 1].id
                        }
                    }
                    
                    leadershipType = when (dialogBinding.radioGroupLeadershipType.checkedRadioButtonId) {
                        dialogBinding.radioLeadershipFirst.id -> "FIRST_HALF"
                        dialogBinding.radioLeadershipSecond.id -> "SECOND_HALF"
                        else -> "BOTH"
                    }
                }
                
                if (name.isNotEmpty()) {
                    val selectedWorkstations = workstationAdapter.currentList
                        .filter { it.isChecked }
                        .map { it.workstation.id }
                    
                    val updatedWorker = originalWorker.copy(
                        name = name,
                        availabilityPercentage = availability,
                        restrictionNotes = restrictionNotes,
                        isTrainer = isTrainer,
                        isTrainee = isTrainee,
                        trainerId = trainerId,
                        trainingWorkstationId = trainingWorkstationId,
                        isCertified = isCertified,
                        isLeader = isLeader,
                        leaderWorkstationId = leaderWorkstationId,
                        leadershipType = leadershipType,
                        certificationDate = if (isCertified && !originalWorker.isCertified) {
                            System.currentTimeMillis()
                        } else {
                            originalWorker.certificationDate
                        }
                    )
                    
                    viewModel.updateWorkerWithWorkstations(updatedWorker, selectedWorkstations)
                    
                    // Limpiar caché del RotationViewModel después de actualizar trabajador
                    rotationViewModel.clearWorkerWorkstationCache()
                    android.util.Log.d("WorkerActivity", "Caché del RotationViewModel limpiado después de actualizar trabajador")
                    
                    android.widget.Toast.makeText(
                        this@WorkerActivity,
                        "Trabajador actualizado correctamente",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
                
            } catch (e: Exception) {
                AlertDialog.Builder(this@WorkerActivity)
                    .setTitle("Error")
                    .setMessage("No se pudo actualizar el trabajador: ${e.message}")
                    .setPositiveButton("OK", null)
                    .show()
            }
        }
    }

}
