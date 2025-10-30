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
            // Optimizaciones para manejar muchas estaciones
            isNestedScrollingEnabled = true
            setHasFixedSize(true)  // Mejora rendimiento cuando el tamaño es fijo
            setItemViewCacheSize(com.workstation.rotation.utils.Constants.RECYCLER_VIEW_CACHE_SIZE)
            
            // Mejorar el scroll suave
            isVerticalScrollBarEnabled = true
            scrollBarStyle = android.view.View.SCROLLBARS_OUTSIDE_OVERLAY
            
            // Agregar separadores sutiles entre elementos
            val divider = androidx.recyclerview.widget.DividerItemDecoration(
                this@WorkerActivity, 
                androidx.recyclerview.widget.DividerItemDecoration.VERTICAL
            )
            // Personalizar el drawable del divisor para que sea más sutil
            divider.setDrawable(
                androidx.core.content.ContextCompat.getDrawable(
                    this@WorkerActivity, 
                    com.workstation.rotation.R.drawable.recycler_divider
                ) ?: androidx.core.content.ContextCompat.getDrawable(
                    this@WorkerActivity, 
                    android.R.drawable.divider_horizontal_bright
                )!!
            )
            addItemDecoration(divider)
        }
        
        // Setup training system
        setupTrainingSystem(dialogBinding)
        
        // Hide certification section for new workers
        dialogBinding.cardCertification.visibility = View.GONE
        
        // Load workstations directly from database
        lifecycleScope.launch {
            try {
                val workstations = viewModel.getActiveWorkstationsSync()
                val checkItems = workstations.map { WorkstationCheckItem(it, false) }
                workstationAdapter.submitList(checkItems)
                
                // Setup training workstation spinner
                val workstationNames = listOf("Seleccionar estación...") + workstations.map { it.name }
                val spinnerAdapter = ArrayAdapter(this@WorkerActivity, android.R.layout.simple_spinner_item, workstationNames)
                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                dialogBinding.spinnerTrainingWorkstation.adapter = spinnerAdapter
            } catch (e: Exception) {
                // Handle error loading workstations
                android.widget.Toast.makeText(
                    this@WorkerActivity,
                    "Error cargando estaciones: ${e.message}",
                    android.widget.Toast.LENGTH_SHORT
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
                
                var trainerId: Long? = null
                var trainingWorkstationId: Long? = null
                
                // Handle training data synchronously to avoid concurrency issues
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
                            viewModel.activeWorkstations.value?.let { workstations ->
                                if (workstationPosition <= workstations.size) {
                                    trainingWorkstationId = workstations[workstationPosition - 1].id
                                }
                            }
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
                                trainingWorkstationId = trainingWorkstationId
                            ),
                            selectedWorkstations
                        )
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
                val trainerAdapter = ArrayAdapter(
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
                val workstationNames: List<String> = listOf("Seleccionar estación...") + trainerWorkstations.map { it.name }
                val workstationAdapter = ArrayAdapter<String>(
                    this@WorkerActivity,
                    android.R.layout.simple_spinner_item,
                    workstationNames
                )
                workstationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                dialogBinding.spinnerTrainingWorkstation.adapter = workstationAdapter
            } catch (e: Exception) {
                // Handle error loading trainer workstations
            }
        }
    }
    
    /**
     * Limpia el spinner de estaciones cuando no hay entrenador seleccionado.
     */
    private fun clearWorkstationSpinner(dialogBinding: DialogAddWorkerBinding) {
        val emptyAdapter = ArrayAdapter(
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
                    loadTrainersForEditSpinner(dialogBinding, worker)
                    setupTrainerSelectionListenerForEdit(dialogBinding, worker)
                }
            }
            
            // Prevent trainer and trainee from being selected simultaneously
            checkboxIsTrainer.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    checkboxIsTrainee.isChecked = false
                    layoutTrainingDetails.visibility = View.GONE
                }
            }
            
            // If worker is currently a trainee, show training details
            if (worker.isTrainee) {
                layoutTrainingDetails.visibility = View.VISIBLE
                loadTrainersForEditSpinner(dialogBinding, worker)
                setupTrainerSelectionListenerForEdit(dialogBinding, worker)
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
                val trainerAdapter = ArrayAdapter(
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
                        dialogBinding.spinnerTrainingWorkstation.setSelection(workstationIndex + 1) // +1 because of "Seleccionar..." option
                    }
                }
            } catch (e: Exception) {
                // Handle error loading trainer workstations
            }
        }
    }
    
    private fun showEditDialog(worker: Worker) {
        val dialogBinding = DialogAddWorkerBinding.inflate(layoutInflater)
        val workstationAdapter = WorkstationCheckboxAdapter { item, isChecked ->
            item.isChecked = isChecked
        }
        
        dialogBinding.apply {
            etWorkerName.setText(worker.name)
            // Email removido por simplicidad
            etAvailabilityPercentage.setText(worker.availabilityPercentage.toString())
            etRestrictionNotes.setText(worker.restrictionNotes)
            
            // Setup training status
            checkboxIsTrainer.isChecked = worker.isTrainer
            checkboxIsTrainee.isChecked = worker.isTrainee
            checkboxIsCertified.isChecked = worker.isCertified
            
            // Setup training system for edit dialog
            setupTrainingSystemForEdit(dialogBinding, worker)
            
            // Show certification option if worker can be certified
            if (worker.canBeCertified()) {
                cardCertification.visibility = View.VISIBLE
                
                // Show training station info
                worker.trainingWorkstationId?.let { trainingStationId ->
                    lifecycleScope.launch {
                        val trainingStation = viewModel.getWorkstationById(trainingStationId)
                        trainingStation?.let { station ->
                            tvTrainingStationInfo.text = "🏭 Se activará automáticamente la estación: ${station.name}"
                            tvTrainingStationInfo.visibility = View.VISIBLE
                        }
                    }
                }
            } else {
                cardCertification.visibility = View.GONE
            }
            
            // Setup certification checkbox listener
            checkboxIsCertified.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    // When certifying, remove trainee status
                    checkboxIsTrainee.isChecked = false
                    layoutTrainingDetails.visibility = View.GONE
                    
                    // Automatically activate the training workstation
                    worker.trainingWorkstationId?.let { trainingStationId ->
                        val currentList = workstationAdapter.currentList.toMutableList()
                        val trainingStationIndex = currentList.indexOfFirst { 
                            it.workstation.id == trainingStationId 
                        }
                        if (trainingStationIndex >= 0) {
                            currentList[trainingStationIndex].isChecked = true
                            workstationAdapter.submitList(currentList)
                            
                            // Show confirmation message
                            val stationName = currentList[trainingStationIndex].workstation.name
                            android.widget.Toast.makeText(
                                this@WorkerActivity,
                                "✅ Estación '$stationName' activada automáticamente",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
            
            recyclerViewWorkstations.apply {
                layoutManager = LinearLayoutManager(this@WorkerActivity)
                adapter = workstationAdapter
                // Optimizaciones para manejar muchas estaciones
                isNestedScrollingEnabled = true
                setHasFixedSize(true)
                setItemViewCacheSize(com.workstation.rotation.utils.Constants.RECYCLER_VIEW_CACHE_SIZE)
                
                // Mejorar el scroll suave
                isVerticalScrollBarEnabled = true
                scrollBarStyle = android.view.View.SCROLLBARS_OUTSIDE_OVERLAY
                
                // Agregar separadores sutiles entre elementos
                val divider = androidx.recyclerview.widget.DividerItemDecoration(
                    this@WorkerActivity, 
                    androidx.recyclerview.widget.DividerItemDecoration.VERTICAL
                )
                // Personalizar el drawable del divisor para que sea más sutil
                divider.setDrawable(
                    androidx.core.content.ContextCompat.getDrawable(
                        this@WorkerActivity, 
                        com.workstation.rotation.R.drawable.recycler_divider
                    ) ?: androidx.core.content.ContextCompat.getDrawable(
                        this@WorkerActivity, 
                        android.R.drawable.divider_horizontal_bright
                    )!!
                )
                addItemDecoration(divider)
            }
        }
        
        // Load workstations with current assignments
        lifecycleScope.launch {
            try {
                val assignedIds = viewModel.getWorkerWorkstationIds(worker.id)
                val workstations = viewModel.getActiveWorkstationsSync()
                val checkItems = workstations.map { workstation ->
                    WorkstationCheckItem(workstation, assignedIds.contains(workstation.id))
                }
                workstationAdapter.submitList(checkItems)
            } catch (e: Exception) {
                // Handle error loading workstations
                android.widget.Toast.makeText(
                    this@WorkerActivity,
                    "Error cargando estaciones: ${e.message}",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        }
        
        AlertDialog.Builder(this)
            .setTitle("Editar Trabajador")
            .setView(dialogBinding.root)
            .setPositiveButton("Guardar") { _, _ ->
                val name = dialogBinding.etWorkerName.text.toString().trim()
                val availabilityText = dialogBinding.etAvailabilityPercentage.text.toString().trim()
                val availability = availabilityText.toIntOrNull()?.coerceIn(0, 100) ?: 100
                val restrictionNotes = dialogBinding.etRestrictionNotes.text.toString().trim()
                val isTrainer = dialogBinding.checkboxIsTrainer.isChecked
                val isTrainee = dialogBinding.checkboxIsTrainee.isChecked
                val isCertified = dialogBinding.checkboxIsCertified.isChecked
                
                if (name.isNotEmpty()) {
                    val selectedWorkstations = workstationAdapter.currentList
                        .filter { it.isChecked }
                        .map { it.workstation.id }
                    
                    // Handle training data for edit dialog
                    var trainerId: Long? = null
                    var trainingWorkstationId: Long? = null
                    
                    if (isTrainee) {
                        // Get selected trainer and workstation
                        val trainerPosition = dialogBinding.spinnerTrainer.selectedItemPosition
                        val workstationPosition = dialogBinding.spinnerTrainingWorkstation.selectedItemPosition
                        
                        lifecycleScope.launch {
                            if (trainerPosition > 0) {
                                val trainers = viewModel.getTrainers()
                                if (trainerPosition <= trainers.size) {
                                    trainerId = trainers[trainerPosition - 1].id
                                }
                            }
                            
                            if (workstationPosition > 0) {
                                viewModel.activeWorkstations.value?.let { workstations ->
                                    if (workstationPosition <= workstations.size) {
                                        trainingWorkstationId = workstations[workstationPosition - 1].id
                                    }
                                }
                            }
                            
                            val updatedWorker = worker.copy(
                                name = name, 
                                email = worker.email, // Mantener email existente
                                availabilityPercentage = availability,
                                restrictionNotes = restrictionNotes,
                                isTrainer = isTrainer,
                                isTrainee = isTrainee,
                                isCertified = isCertified,
                                certificationDate = if (isCertified && !worker.isCertified) {
                                    System.currentTimeMillis()
                                } else {
                                    worker.certificationDate
                                },
                                // Update training data based on current selections
                                trainerId = if (isCertified) null else (if (isTrainee) trainerId else null),
                                trainingWorkstationId = if (isCertified) null else (if (isTrainee) trainingWorkstationId else null)
                            )
                            
                            viewModel.updateWorkerWithWorkstations(updatedWorker, selectedWorkstations)
                        }
                    } else {
                        // Not a trainee, clear training data
                        lifecycleScope.launch {
                            val updatedWorker = worker.copy(
                                name = name, 
                                email = worker.email, // Mantener email existente
                                availabilityPercentage = availability,
                                restrictionNotes = restrictionNotes,
                                isTrainer = isTrainer,
                                isTrainee = isTrainee,
                                isCertified = isCertified,
                                certificationDate = if (isCertified && !worker.isCertified) {
                                    System.currentTimeMillis()
                                } else {
                                    worker.certificationDate
                                },
                                // Clear training data if not trainee or certified
                                trainerId = null,
                                trainingWorkstationId = null
                            )
                            
                            viewModel.updateWorkerWithWorkstations(updatedWorker, selectedWorkstations)
                        }
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    
    /**
     * Muestra un diálogo de confirmación para eliminar un trabajador.
     */
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
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, restrictionTypes)
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

}