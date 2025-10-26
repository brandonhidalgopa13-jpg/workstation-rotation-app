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
        
        // Setup training system
        setupTrainingSystem(dialogBinding)
        
        // Load workstations
        viewModel.activeWorkstations.observe(this) { workstations ->
            val checkItems = workstations.map { WorkstationCheckItem(it, false) }
            workstationAdapter.submitList(checkItems)
            
            // Setup training workstation spinner
            val workstationNames = listOf("Seleccionar estación...") + workstations.map { it.name }
            val workstationAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, workstationNames)
            workstationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            dialogBinding.spinnerTrainingWorkstation.adapter = workstationAdapter
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
                val isTrainer = dialogBinding.checkboxIsTrainer.isChecked
                val isTrainee = dialogBinding.checkboxIsTrainee.isChecked
                
                var trainerId: Long? = null
                var trainingWorkstationId: Long? = null
                
                if (isTrainee) {
                    // Get selected trainer and workstation
                    val trainerPosition = dialogBinding.spinnerTrainer.selectedItemPosition
                    val workstationPosition = dialogBinding.spinnerTrainingWorkstation.selectedItemPosition
                    
                    if (trainerPosition > 0) {
                        lifecycleScope.launch {
                            val trainers = viewModel.getTrainers()
                            if (trainerPosition <= trainers.size) {
                                trainerId = trainers[trainerPosition - 1].id
                            }
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
                val trainerNames = listOf("Seleccionar entrenador...") + trainers.map { "${it.name} (${it.email})" }
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