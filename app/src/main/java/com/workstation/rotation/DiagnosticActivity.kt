package com.workstation.rotation

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.workstation.rotation.data.database.AppDatabase
import com.workstation.rotation.data.entities.WorkerWorkstationCapability
import com.workstation.rotation.databinding.ActivityDiagnosticsBinding
import kotlinx.coroutines.launch

/**
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 🔍 ACTIVIDAD DE DIAGNÓSTICO Y REPARACIÓN
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 
 * Diagnostica y repara problemas comunes con el sistema de rotación:
 * • Trabajadores sin capacidades
 * • Capacidades inactivas
 * • Desincronización entre worker_workstations y worker_workstation_capabilities
 * 
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 */
class DiagnosticActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDiagnosticsBinding
    private lateinit var database: AppDatabase
    
    private val workerDao by lazy { database.workerDao() }
    private val workstationDao by lazy { database.workstationDao() }
    private val capabilityDao by lazy { database.workerWorkstationCapabilityDao() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDiagnosticsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        database = AppDatabase.getDatabase(this)
        
        setupUI()
        setupClickListeners()
        
        // Ejecutar diagnóstico automáticamente al abrir
        runDiagnostic()
    }

    private fun setupUI() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Diagnóstico del Sistema"
        
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupClickListeners() {
        binding.btnRunDiagnostic.setOnClickListener {
            runDiagnostic()
        }
        
        binding.btnRepairSync.setOnClickListener {
            repairSynchronization()
        }
        
        binding.btnActivateAll.setOnClickListener {
            activateAllCapabilities()
        }
        
        binding.btnResetCapabilities.setOnClickListener {
            showResetConfirmation()
        }
    }

    private fun runDiagnostic() {
        lifecycleScope.launch {
            binding.progressBar.visibility = View.VISIBLE
            binding.tvResults.text = "Ejecutando diagnóstico...\n\n"
            
            try {
                val report = StringBuilder()
                report.append("═══════════════════════════════════════════════════════\n")
                report.append("🔍 DIAGNÓSTICO DEL SISTEMA DE ROTACIÓN\n")
                report.append("═══════════════════════════════════════════════════════\n\n")
                
                // 1. Contar trabajadores
                val workers = workerDao.getAllWorkersSync()
                val activeWorkers = workers.filter { it.isActive }
                report.append("👥 TRABAJADORES:\n")
                report.append("  • Total: ${workers.size}\n")
                report.append("  • Activos: ${activeWorkers.size}\n")
                report.append("  • Inactivos: ${workers.size - activeWorkers.size}\n\n")
                
                // 2. Contar estaciones
                val workstations = workstationDao.getAllWorkstationsSync()
                val activeWorkstations = workstations.filter { it.isActive }
                report.append("📍 ESTACIONES:\n")
                report.append("  • Total: ${workstations.size}\n")
                report.append("  • Activas: ${activeWorkstations.size}\n")
                report.append("  • Inactivas: ${workstations.size - activeWorkstations.size}\n\n")
                
                // 3. Contar relaciones worker_workstations
                val workerWorkstations = workerDao.getAllWorkerWorkstationsSync()
                report.append("🔗 RELACIONES WORKER_WORKSTATIONS:\n")
                report.append("  • Total: ${workerWorkstations.size}\n\n")
                
                // 4. Contar capacidades
                val allCapabilities = capabilityDao.getAllCapabilities()
                val activeCapabilities = allCapabilities.filter { it.is_active }
                val assignableCapabilities = activeCapabilities.filter { it.canBeAssigned() }
                
                report.append("🎯 CAPACIDADES (WORKER_WORKSTATION_CAPABILITIES):\n")
                report.append("  • Total: ${allCapabilities.size}\n")
                report.append("  • Activas: ${activeCapabilities.size}\n")
                report.append("  • Inactivas: ${allCapabilities.size - activeCapabilities.size}\n")
                report.append("  • Asignables: ${assignableCapabilities.size}\n\n")
                
                // 5. Analizar cada trabajador
                report.append("═══════════════════════════════════════════════════════\n")
                report.append("📊 ANÁLISIS DETALLADO POR TRABAJADOR:\n")
                report.append("═══════════════════════════════════════════════════════\n\n")
                
                var workersWithIssues = 0
                
                activeWorkers.forEach { worker ->
                    val workerStations = workerDao.getWorkerWorkstationIds(worker.id)
                    val workerCapabilities = allCapabilities.filter { it.worker_id == worker.id }
                    val workerActiveCapabilities = workerCapabilities.filter { it.is_active }
                    val workerAssignableCapabilities = workerActiveCapabilities.filter { it.canBeAssigned() }
                    
                    val hasIssues = workerStations.size != workerActiveCapabilities.size || 
                                   workerAssignableCapabilities.isEmpty()
                    
                    if (hasIssues) {
                        workersWithIssues++
                        report.append("⚠️ ${worker.name} (ID: ${worker.id}):\n")
                    } else {
                        report.append("✅ ${worker.name} (ID: ${worker.id}):\n")
                    }
                    
                    report.append("   • Estaciones asignadas: ${workerStations.size}\n")
                    report.append("   • Capacidades totales: ${workerCapabilities.size}\n")
                    report.append("   • Capacidades activas: ${workerActiveCapabilities.size}\n")
                    report.append("   • Capacidades asignables: ${workerAssignableCapabilities.size}\n")
                    
                    // Mostrar detalles de capacidades
                    if (workerCapabilities.isNotEmpty()) {
                        workerCapabilities.forEach { cap ->
                            val station = workstations.find { it.id == cap.workstation_id }
                            val status = when {
                                !cap.is_active -> "❌ INACTIVA"
                                !cap.canBeAssigned() -> "⚠️ NO ASIGNABLE"
                                else -> "✅ OK"
                            }
                            report.append("     - ${station?.name ?: "Estación ${cap.workstation_id}"}: " +
                                        "Nivel ${cap.competency_level} $status\n")
                        }
                    }
                    
                    // Detectar desincronización
                    if (workerStations.size > workerCapabilities.size) {
                        report.append("   ⚠️ PROBLEMA: Faltan ${workerStations.size - workerCapabilities.size} capacidades\n")
                    } else if (workerStations.size < workerActiveCapabilities.size) {
                        report.append("   ⚠️ PROBLEMA: Hay ${workerActiveCapabilities.size - workerStations.size} capacidades extra\n")
                    }
                    
                    report.append("\n")
                }
                
                // 6. Resumen de problemas
                report.append("═══════════════════════════════════════════════════════\n")
                report.append("📋 RESUMEN:\n")
                report.append("═══════════════════════════════════════════════════════\n\n")
                
                if (workersWithIssues == 0) {
                    report.append("✅ No se detectaron problemas\n")
                    report.append("   El sistema está correctamente sincronizado\n\n")
                } else {
                    report.append("⚠️ Se detectaron problemas en $workersWithIssues trabajadores\n\n")
                    report.append("ACCIONES RECOMENDADAS:\n")
                    report.append("1. Presiona 'Reparar Sincronización' para crear capacidades faltantes\n")
                    report.append("2. Presiona 'Activar Todas' para activar capacidades inactivas\n")
                    report.append("3. Si persisten problemas, presiona 'Resetear Capacidades'\n\n")
                }
                
                // 7. Verificar si hay trabajadores que deberían aparecer en rotación
                report.append("═══════════════════════════════════════════════════════\n")
                report.append("🔄 TRABAJADORES DISPONIBLES PARA ROTACIÓN:\n")
                report.append("═══════════════════════════════════════════════════════\n\n")
                
                val workersForRotation = activeWorkers.filter { worker ->
                    val caps = allCapabilities.filter { 
                        it.worker_id == worker.id && it.is_active && it.canBeAssigned() 
                    }
                    caps.isNotEmpty()
                }
                
                report.append("Trabajadores que DEBERÍAN aparecer en rotación: ${workersForRotation.size}\n\n")
                workersForRotation.forEach { worker ->
                    val caps = allCapabilities.filter { 
                        it.worker_id == worker.id && it.is_active && it.canBeAssigned() 
                    }
                    report.append("  ✅ ${worker.name}: ${caps.size} estaciones disponibles\n")
                }
                
                if (workersForRotation.size < activeWorkers.size) {
                    report.append("\n⚠️ PROBLEMA DETECTADO:\n")
                    report.append("  ${activeWorkers.size - workersForRotation.size} trabajadores activos NO aparecerán en rotación\n")
                    report.append("  porque no tienen capacidades activas y asignables\n")
                }
                
                binding.tvResults.text = report.toString()
                
            } catch (e: Exception) {
                binding.tvResults.text = "❌ Error en diagnóstico:\n${e.message}\n\n${e.stackTraceToString()}"
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun repairSynchronization() {
        lifecycleScope.launch {
            binding.progressBar.visibility = View.VISIBLE
            
            try {
                val report = StringBuilder()
                report.append("🔧 REPARANDO SINCRONIZACIÓN...\n\n")
                
                val workers = workerDao.getAllWorkersSync()
                val workstations = workstationDao.getAllWorkstationsSync()
                var repaired = 0
                var created = 0
                
                workers.forEach { worker ->
                    val workerStations = workerDao.getWorkerWorkstationIds(worker.id)
                    val existingCapabilities = capabilityDao.getByWorker(worker.id)
                    val existingStationIds = existingCapabilities.map { it.workstation_id }.toSet()
                    
                    // Crear capacidades faltantes
                    workerStations.forEach { stationId ->
                        if (!existingStationIds.contains(stationId)) {
                            val baseLevel = when {
                                worker.isTrainee -> WorkerWorkstationCapability.LEVEL_BEGINNER
                                worker.isCertified -> WorkerWorkstationCapability.LEVEL_INTERMEDIATE
                                worker.isTrainer -> WorkerWorkstationCapability.LEVEL_ADVANCED
                                else -> WorkerWorkstationCapability.LEVEL_BASIC
                            }
                            
                            val capability = WorkerWorkstationCapability(
                                worker_id = worker.id,
                                workstation_id = stationId,
                                competency_level = baseLevel,
                                is_active = true,
                                is_certified = worker.isCertified,
                                can_be_leader = worker.isLeader && worker.leaderWorkstationId == stationId,
                                can_train = worker.isTrainer,
                                notes = "Capacidad creada por reparación automática"
                            )
                            
                            capabilityDao.insert(capability)
                            created++
                            
                            val station = workstations.find { it.id == stationId }
                            report.append("✅ Creada: ${worker.name} → ${station?.name ?: "Estación $stationId"}\n")
                        }
                    }
                    
                    // Reactivar capacidades inactivas que deberían estar activas
                    existingCapabilities.forEach { cap ->
                        if (!cap.is_active && workerStations.contains(cap.workstation_id)) {
                            val updated = cap.copy(
                                is_active = true,
                                updated_at = System.currentTimeMillis()
                            )
                            capabilityDao.update(updated)
                            repaired++
                            
                            val station = workstations.find { it.id == cap.workstation_id }
                            report.append("🔄 Reactivada: ${worker.name} → ${station?.name ?: "Estación ${cap.workstation_id}"}\n")
                        }
                    }
                }
                
                report.append("\n═══════════════════════════════════════════════════════\n")
                report.append("✅ REPARACIÓN COMPLETADA\n")
                report.append("  • Capacidades creadas: $created\n")
                report.append("  • Capacidades reactivadas: $repaired\n")
                report.append("═══════════════════════════════════════════════════════\n")
                
                binding.tvResults.text = report.toString()
                
                // Ejecutar diagnóstico nuevamente
                runDiagnostic()
                
            } catch (e: Exception) {
                binding.tvResults.text = "❌ Error en reparación:\n${e.message}"
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun activateAllCapabilities() {
        lifecycleScope.launch {
            binding.progressBar.visibility = View.VISIBLE
            
            try {
                val allCapabilities = capabilityDao.getAllCapabilities()
                val inactiveCapabilities = allCapabilities.filter { !it.is_active }
                
                var activated = 0
                inactiveCapabilities.forEach { cap ->
                    val updated = cap.copy(
                        is_active = true,
                        updated_at = System.currentTimeMillis()
                    )
                    capabilityDao.update(updated)
                    activated++
                }
                
                binding.tvResults.text = "✅ Activadas $activated capacidades\n\n" +
                                        "Ejecutando diagnóstico nuevamente..."
                
                // Ejecutar diagnóstico nuevamente
                runDiagnostic()
                
            } catch (e: Exception) {
                binding.tvResults.text = "❌ Error al activar capacidades:\n${e.message}"
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun showResetConfirmation() {
        MaterialAlertDialogBuilder(this)
            .setTitle("⚠️ Resetear Capacidades")
            .setMessage("Esta acción eliminará TODAS las capacidades existentes y las recreará desde cero basándose en las asignaciones de estaciones.\n\n¿Estás seguro?")
            .setPositiveButton("Sí, Resetear") { _, _ ->
                resetAllCapabilities()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun resetAllCapabilities() {
        lifecycleScope.launch {
            binding.progressBar.visibility = View.VISIBLE
            
            try {
                val report = StringBuilder()
                report.append("🔄 RESETEANDO TODAS LAS CAPACIDADES...\n\n")
                
                // Eliminar todas las capacidades
                capabilityDao.deleteAll()
                report.append("✅ Capacidades anteriores eliminadas\n\n")
                
                // Recrear capacidades desde worker_workstations
                val workers = workerDao.getAllWorkersSync()
                val workstations = workstationDao.getAllWorkstationsSync()
                var created = 0
                
                workers.forEach { worker ->
                    val workerStations = workerDao.getWorkerWorkstationIds(worker.id)
                    
                    workerStations.forEach { stationId ->
                        val baseLevel = when {
                            worker.isTrainee -> WorkerWorkstationCapability.LEVEL_BEGINNER
                            worker.isCertified -> WorkerWorkstationCapability.LEVEL_INTERMEDIATE
                            worker.isTrainer -> WorkerWorkstationCapability.LEVEL_ADVANCED
                            else -> WorkerWorkstationCapability.LEVEL_BASIC
                        }
                        
                        val capability = WorkerWorkstationCapability(
                            worker_id = worker.id,
                            workstation_id = stationId,
                            competency_level = baseLevel,
                            is_active = true,
                            is_certified = worker.isCertified,
                            can_be_leader = worker.isLeader && worker.leaderWorkstationId == stationId,
                            can_train = worker.isTrainer,
                            notes = "Capacidad recreada por reset completo"
                        )
                        
                        capabilityDao.insert(capability)
                        created++
                        
                        val station = workstations.find { it.id == stationId }
                        report.append("✅ ${worker.name} → ${station?.name ?: "Estación $stationId"}\n")
                    }
                }
                
                report.append("\n═══════════════════════════════════════════════════════\n")
                report.append("✅ RESET COMPLETADO\n")
                report.append("  • Capacidades recreadas: $created\n")
                report.append("═══════════════════════════════════════════════════════\n")
                
                binding.tvResults.text = report.toString()
                
                // Ejecutar diagnóstico nuevamente
                runDiagnostic()
                
            } catch (e: Exception) {
                binding.tvResults.text = "❌ Error en reset:\n${e.message}"
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }
}
