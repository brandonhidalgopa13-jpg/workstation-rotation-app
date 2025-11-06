package com.workstation.rotation.services

import android.content.Context
import com.workstation.rotation.data.database.AppDatabase
import com.workstation.rotation.data.entities.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 🚀 SERVICIO DE INICIALIZACIÓN DE DATOS - v4.0
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 
 * 🎯 PROPÓSITO:
 * Servicio para inicializar datos de prueba en la nueva arquitectura de rotación.
 * Crea trabajadores, estaciones y capacidades para testing y demostración.
 * 
 * 📋 CARACTERÍSTICAS:
 * • Creación de datos de prueba realistas
 * • Configuración de capacidades trabajador-estación
 * • Asignación de competencias y certificaciones
 * • Datos balanceados para testing completo
 * 
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 */

class DataInitializationService(private val context: Context) {
    
    private val database = AppDatabase.getDatabase(context)
    private val workerDao = database.workerDao()
    private val workstationDao = database.workstationDao()
    private val capabilityDao = database.workerWorkstationCapabilityDao()
    
    /**
     * Inicializa todos los datos de prueba
     */
    suspend fun initializeTestData(): Boolean = withContext(Dispatchers.IO) {
        try {
            // Verificar si ya hay datos
            val existingWorkers = workerDao.getAllWorkersSync()
            val existingWorkstations = workstationDao.getAllWorkstationsSync()
            
            if (existingWorkers.isNotEmpty() && existingWorkstations.isNotEmpty()) {
                // Ya hay datos, solo crear capacidades si no existen
                initializeCapabilitiesIfNeeded()
                return@withContext true
            }
            
            // Crear datos desde cero
            createSampleWorkstations()
            createSampleWorkers()
            createWorkerCapabilities()
            
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * Crea estaciones de trabajo de ejemplo
     */
    private suspend fun createSampleWorkstations() {
        val workstations = listOf(
            Workstation(
                name = "Ensamblaje A",
                description = "Línea principal de ensamblaje",
                requiredWorkers = 3,
                isPriority = true,
                isActive = true
            ),
            Workstation(
                name = "Ensamblaje B",
                description = "Línea secundaria de ensamblaje",
                requiredWorkers = 2,
                isPriority = false,
                isActive = true
            ),
            Workstation(
                name = "Control de Calidad",
                description = "Inspección y control de calidad",
                requiredWorkers = 2,
                isPriority = true,
                isActive = true
            ),
            Workstation(
                name = "Empaque",
                description = "Empaque y preparación para envío",
                requiredWorkers = 2,
                isPriority = false,
                isActive = true
            ),
            Workstation(
                name = "Mantenimiento",
                description = "Mantenimiento de equipos",
                requiredWorkers = 1,
                isPriority = false,
                isActive = true
            ),
            Workstation(
                name = "Almacén",
                description = "Gestión de inventario",
                requiredWorkers = 2,
                isPriority = false,
                isActive = true
            )
        )
        
        workstations.forEach { workstation ->
            workstationDao.insertWorkstation(workstation)
        }
    }
    
    /**
     * Crea trabajadores de ejemplo
     */
    private suspend fun createSampleWorkers() {
        val workers = listOf(
            Worker(
                name = "Juan Pérez",
                employeeId = "EMP001",
                isActive = true,
                isTrainer = true,
                isTrainee = false
            ),
            Worker(
                name = "María García",
                employeeId = "EMP002",
                isActive = true,
                isTrainer = true,
                isTrainee = false
            ),
            Worker(
                name = "Carlos López",
                employeeId = "EMP003",
                isActive = true,
                isTrainer = false,
                isTrainee = false
            ),
            Worker(
                name = "Ana Martínez",
                employeeId = "EMP004",
                isActive = true,
                isTrainer = false,
                isTrainee = true
            ),
            Worker(
                name = "Pedro Rodríguez",
                employeeId = "EMP005",
                isActive = true,
                isTrainer = true,
                isTrainee = false
            ),
            Worker(
                name = "Laura Sánchez",
                employeeId = "EMP006",
                isActive = true,
                isTrainer = false,
                isTrainee = false
            ),
            Worker(
                name = "Miguel Torres",
                employeeId = "EMP007",
                isActive = true,
                isTrainer = false,
                isTrainee = true
            ),
            Worker(
                name = "Carmen Ruiz",
                employeeId = "EMP008",
                isActive = true,
                isTrainer = false,
                isTrainee = false
            ),
            Worker(
                name = "Roberto Díaz",
                employeeId = "EMP009",
                isActive = true,
                isTrainer = true,
                isTrainee = false
            ),
            Worker(
                name = "Isabel Moreno",
                employeeId = "EMP010",
                isActive = true,
                isTrainer = false,
                isTrainee = true
            )
        )
        
        workers.forEach { worker ->
            workerDao.insertWorker(worker)
        }
    }
    
    /**
     * Crea capacidades trabajador-estación
     */
    private suspend fun createWorkerCapabilities() {
        val workers = workerDao.getAllWorkersSync()
        val workstations = workstationDao.getAllWorkstationsSync()
        
        val capabilities = mutableListOf<WorkerWorkstationCapability>()
        
        workers.forEachIndexed { workerIndex, worker ->
            workstations.forEachIndexed { stationIndex, workstation ->
                
                // Determinar si el trabajador puede trabajar en esta estación
                val canWork = when {
                    // Todos pueden trabajar en almacén (nivel básico)
                    workstation.name == "Almacén" -> true
                    // Entrenadores pueden trabajar en la mayoría de estaciones
                    worker.isTrainer -> true
                    // Algunos trabajadores tienen capacidades específicas
                    else -> (workerIndex + stationIndex) % 3 != 0 // Patrón para variedad
                }
                
                if (canWork) {
                    // Determinar nivel de competencia
                    val competencyLevel = when {
                        worker.isTrainer && workstation.isPriority -> 5 // Experto
                        worker.isTrainer -> 4 // Avanzado
                        worker.isTrainee -> 2 // Básico
                        workstation.name == "Almacén" -> 3 // Intermedio para almacén
                        else -> listOf(2, 3, 4).random() // Aleatorio entre básico y avanzado
                    }
                    
                    // Determinar certificaciones y roles
                    val isCertified = competencyLevel >= 3 && !worker.isTrainee
                    val canBeLeader = worker.isTrainer && competencyLevel >= 4
                    val canTrain = worker.isTrainer && competencyLevel >= 4
                    
                    // Calcular experiencia (simulada)
                    val experienceHours = when (competencyLevel) {
                        5 -> (500..1000).random()
                        4 -> (200..500).random()
                        3 -> (100..200).random()
                        2 -> (50..100).random()
                        else -> (10..50).random()
                    }
                    
                    val capability = WorkerWorkstationCapability(
                        worker_id = worker.id,
                        workstation_id = workstation.id,
                        competency_level = competencyLevel,
                        is_active = true,
                        is_certified = isCertified,
                        can_be_leader = canBeLeader,
                        can_train = canTrain,
                        certified_at = if (isCertified) System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000L) else null,
                        certification_expires_at = if (isCertified) System.currentTimeMillis() + (365 * 24 * 60 * 60 * 1000L) else null,
                        last_evaluated_at = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L),
                        last_evaluation_score = if (competencyLevel >= 3) (70..100).random() / 10.0 else (50..70).random() / 10.0,
                        experience_hours = experienceHours
                    )
                    
                    capabilities.add(capability)
                }
            }
        }
        
        // Insertar todas las capacidades
        capabilityDao.insertAll(capabilities)
    }
    
    /**
     * Inicializa capacidades si no existen pero hay trabajadores y estaciones
     */
    private suspend fun initializeCapabilitiesIfNeeded() {
        // Verificar si existen capacidades usando una consulta directa
        val existingCapabilities = capabilityDao.getCertifiedCapabilities()
        if (existingCapabilities.isEmpty()) {
            createWorkerCapabilities()
        }
    }
    
    /**
     * Limpia todos los datos de prueba
     */
    suspend fun clearAllData(): Boolean = withContext(Dispatchers.IO) {
        try {
            // Limpiar en orden para evitar problemas de foreign key
            database.clearAllTables()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * Verifica si hay datos inicializados
     */
    suspend fun hasInitializedData(): Boolean = withContext(Dispatchers.IO) {
        try {
            val workers = workerDao.getAllWorkersSync()
            val workstations = workstationDao.getAllWorkstationsSync()
            val capabilities = capabilityDao.getCertifiedCapabilities()
            
            workers.isNotEmpty() && workstations.isNotEmpty() && capabilities.isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }
}