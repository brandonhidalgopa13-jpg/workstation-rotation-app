package com.workstation.rotation.sql

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.workstation.rotation.data.database.AppDatabase
import com.workstation.rotation.data.entities.Worker
import com.workstation.rotation.data.entities.Workstation
import com.workstation.rotation.data.entities.WorkerWorkstation
import com.workstation.rotation.viewmodels.SqlRotationViewModel
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Test de diagnóstico para identificar problemas en el sistema de rotación SQL.
 */
@RunWith(AndroidJUnit4::class)
class SqlRotationDiagnosticTest {
    
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    
    private lateinit var database: AppDatabase
    private lateinit var viewModel: SqlRotationViewModel
    
    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        
        viewModel = SqlRotationViewModel(
            database.rotationDao(),
            database.workerDao(),
            database.workstationDao()
        )
    }
    
    @After
    fun tearDown() {
        database.close()
    }
    
    @Test
    fun diagnosticoCompleto() = runBlocking {
        println("🔍 INICIANDO DIAGNÓSTICO COMPLETO DEL SISTEMA SQL")
        
        // Paso 1: Verificar estado inicial de la base de datos
        verificarEstadoInicialBaseDatos()
        
        // Paso 2: Insertar datos de prueba
        insertarDatosPrueba()
        
        // Paso 3: Verificar datos insertados
        verificarDatosInsertados()
        
        // Paso 4: Probar consultas SQL individuales
        probarConsultasIndividuales()
        
        // Paso 5: Intentar generar rotación
        intentarGenerarRotacion()
        
        println("🔍 DIAGNÓSTICO COMPLETADO")
    }
    
    private suspend fun verificarEstadoInicialBaseDatos() {
        println("\n📊 === VERIFICANDO ESTADO INICIAL DE BASE DE DATOS ===")
        
        val workersCount = database.workerDao().getAllWorkersSync().size
        val workstationsCount = database.workstationDao().getAllWorkstationsSync().size
        val relationsCount = database.workerDao().getAllWorkerWorkstationsSync().size
        
        println("📊 Trabajadores en BD: $workersCount")
        println("📊 Estaciones en BD: $workstationsCount")
        println("📊 Relaciones en BD: $relationsCount")
        
        if (workersCount == 0) println("⚠️ NO HAY TRABAJADORES EN LA BASE DE DATOS")
        if (workstationsCount == 0) println("⚠️ NO HAY ESTACIONES EN LA BASE DE DATOS")
        if (relationsCount == 0) println("⚠️ NO HAY RELACIONES TRABAJADOR-ESTACIÓN")
    }
    
    private suspend fun insertarDatosPrueba() {
        println("\n🔧 === INSERTANDO DATOS DE PRUEBA ===")
        
        // Insertar estaciones de prueba
        val estacion1 = Workstation(
            id = 1,
            name = "Estación A",
            requiredWorkers = 2,
            isPriority = true,
            isActive = true
        )
        
        val estacion2 = Workstation(
            id = 2,
            name = "Estación B", 
            requiredWorkers = 2,
            isPriority = false,
            isActive = true
        )
        
        database.workstationDao().insertWorkstation(estacion1)
        database.workstationDao().insertWorkstation(estacion2)
        println("✅ Estaciones insertadas: ${estacion1.name}, ${estacion2.name}")
        
        // Insertar trabajadores de prueba
        val trabajador1 = Worker(
            id = 1,
            name = "Juan Pérez",
            isActive = true,
            isLeader = true,
            leaderWorkstationId = 1,
            leadershipType = "BOTH"
        )
        
        val trabajador2 = Worker(
            id = 2,
            name = "María García",
            isActive = true,
            isTrainer = true
        )
        
        val trabajador3 = Worker(
            id = 3,
            name = "Carlos López",
            isActive = true,
            isTrainee = true,
            trainerId = 2,
            trainingWorkstationId = 2
        )
        
        val trabajador4 = Worker(
            id = 4,
            name = "Ana Martínez",
            isActive = true
        )
        
        database.workerDao().insertWorker(trabajador1)
        database.workerDao().insertWorker(trabajador2)
        database.workerDao().insertWorker(trabajador3)
        database.workerDao().insertWorker(trabajador4)
        println("✅ Trabajadores insertados: 4 trabajadores")
        
        // Insertar relaciones trabajador-estación
        val relaciones = listOf(
            WorkerWorkstation(1, 1), // Juan -> Estación A
            WorkerWorkstation(1, 2), // Juan -> Estación B
            WorkerWorkstation(2, 1), // María -> Estación A
            WorkerWorkstation(2, 2), // María -> Estación B
            WorkerWorkstation(3, 2), // Carlos -> Estación B
            WorkerWorkstation(4, 1), // Ana -> Estación A
            WorkerWorkstation(4, 2)  // Ana -> Estación B
        )
        
        relaciones.forEach { relacion ->
            database.workerDao().insertWorkerWorkstation(relacion)
        }
        println("✅ Relaciones insertadas: ${relaciones.size} relaciones")
    }
    
    private suspend fun verificarDatosInsertados() {
        println("\n✅ === VERIFICANDO DATOS INSERTADOS ===")
        
        val workers = database.workerDao().getAllWorkersSync()
        val workstations = database.workstationDao().getAllWorkstationsSync()
        val relations = database.workerDao().getAllWorkerWorkstationsSync()
        
        println("✅ Trabajadores activos: ${workers.count { it.isActive }}")
        println("✅ Estaciones activas: ${workstations.count { it.isActive }}")
        println("✅ Relaciones totales: ${relations.size}")
        
        workers.forEach { worker ->
            val workstationIds = database.workerDao().getWorkerWorkstationIds(worker.id)
            println("   - ${worker.name}: puede trabajar en estaciones ${workstationIds.joinToString()}")
        }
    }
    
    private suspend fun probarConsultasIndividuales() {
        println("\n🧪 === PROBANDO CONSULTAS SQL INDIVIDUALES ===")
        
        try {
            // Probar getAllEligibleWorkers
            val eligibleWorkers = database.rotationDao().getAllEligibleWorkers()
            println("🧪 getAllEligibleWorkers(): ${eligibleWorkers.size} trabajadores")
            eligibleWorkers.forEach { worker ->
                println("   - ${worker.name} (Líder: ${worker.isLeader}, Entrenador: ${worker.isTrainer}, Entrenado: ${worker.isTrainee})")
            }
            
            // Probar getAllActiveWorkstationsOrdered
            val workstations = database.rotationDao().getAllActiveWorkstationsOrdered()
            println("🧪 getAllActiveWorkstationsOrdered(): ${workstations.size} estaciones")
            workstations.forEach { station ->
                println("   - ${station.name} (Requiere: ${station.requiredWorkers}, Prioritaria: ${station.isPriority})")
            }
            
            // Probar getActiveLeadersForRotationFixed
            val leaders = database.rotationDao().getActiveLeadersForRotationFixed(true)
            println("🧪 getActiveLeadersForRotationFixed(true): ${leaders.size} líderes")
            leaders.forEach { leader ->
                println("   - ${leader.name} -> Estación ${leader.leaderWorkstationId}")
            }
            
            // Probar getValidTrainingPairs
            val trainingPairs = database.rotationDao().getValidTrainingPairs()
            println("🧪 getValidTrainingPairs(): ${trainingPairs.size} parejas")
            trainingPairs.forEach { trainee ->
                println("   - ${trainee.name} (Entrenador ID: ${trainee.trainerId}) -> Estación ${trainee.trainingWorkstationId}")
            }
            
            // Probar canWorkerWorkAtStationFixed
            val canWork = database.rotationDao().canWorkerWorkAtStationFixed(1, 1)
            println("🧪 canWorkerWorkAtStationFixed(1, 1): $canWork")
            
            // Probar getWorkersForStationFixed
            val workersForStation = database.rotationDao().getWorkersForStationFixed(1)
            println("🧪 getWorkersForStationFixed(1): ${workersForStation.size} trabajadores")
            
        } catch (e: Exception) {
            println("❌ ERROR en consultas SQL: ${e.message}")
            e.printStackTrace()
        }
    }
    
    private suspend fun intentarGenerarRotacion() {
        println("\n🚀 === INTENTANDO GENERAR ROTACIÓN ===")
        
        try {
            val resultado = viewModel.generateOptimizedRotation()
            println("🚀 Resultado de generateOptimizedRotation(): $resultado")
            
            if (resultado) {
                println("✅ ROTACIÓN GENERADA EXITOSAMENTE")
            } else {
                println("❌ FALLO AL GENERAR ROTACIÓN")
            }
            
        } catch (e: Exception) {
            println("❌ EXCEPCIÓN al generar rotación: ${e.message}")
            e.printStackTrace()
        }
    }
}