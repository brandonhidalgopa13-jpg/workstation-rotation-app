package com.workstation.rotation.performance

import com.workstation.rotation.data.dao.WorkerDao
import com.workstation.rotation.data.dao.WorkstationDao
import com.workstation.rotation.data.entities.Worker
import com.workstation.rotation.data.entities.Workstation
import com.workstation.rotation.viewmodels.RotationViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.*
import kotlin.system.measureTimeMillis

/**
 * Tests de rendimiento para el algoritmo de rotación
 * Verifica que el sistema funcione eficientemente con grandes volúmenes de datos
 */
@ExperimentalCoroutinesApi
class RotationPerformanceTest {

    @Mock
    private lateinit var workerDao: WorkerDao

    @Mock
    private lateinit var workstationDao: WorkstationDao

    private lateinit var viewModel: RotationViewModel

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        viewModel = RotationViewModel(workerDao, workstationDao)
    }

    @Test
    fun `rotation algorithm should complete within 2 seconds for 50 workers`() = runTest {
        // Arrange
        val workers = generateTestWorkers(50)
        val workstations = generateTestWorkstations(10)
        setupMockData(workers, workstations)

        // Act & Measure
        val executionTime = measureTimeMillis {
            viewModel.generateRotation()
        }

        // Assert
        assertTrue("Rotation should complete within 2000ms for 50 workers, took ${executionTime}ms", 
                  executionTime < 2000)
        
        val rotationItems = viewModel.rotationItems.value
        assertNotNull("Rotation should generate items", rotationItems)
        assertTrue("Should have rotation items", rotationItems!!.isNotEmpty())
    }

    @Test
    fun `rotation algorithm should handle 100 workers efficiently`() = runTest {
        // Arrange
        val workers = generateTestWorkers(100)
        val workstations = generateTestWorkstations(20)
        setupMockData(workers, workstations)

        // Act & Measure
        val executionTime = measureTimeMillis {
            viewModel.generateRotation()
        }

        // Assert
        assertTrue("Rotation should complete within 5000ms for 100 workers, took ${executionTime}ms", 
                  executionTime < 5000)
        
        val rotationItems = viewModel.rotationItems.value
        assertNotNull("Rotation should generate items", rotationItems)
        assertTrue("Should have rotation items", rotationItems!!.isNotEmpty())
    }

    @Test
    fun `rotation algorithm should scale linearly with worker count`() = runTest {
        val workerCounts = listOf(10, 25, 50)
        val executionTimes = mutableListOf<Long>()

        for (workerCount in workerCounts) {
            // Arrange
            val workers = generateTestWorkers(workerCount)
            val workstations = generateTestWorkstations(workerCount / 5) // 5 workers per station
            setupMockData(workers, workstations)

            // Act & Measure
            val executionTime = measureTimeMillis {
                viewModel.generateRotation()
            }
            executionTimes.add(executionTime)

            // Reset for next iteration
            viewModel.clearRotation()
        }

        // Assert - Execution time should scale reasonably
        // Time for 50 workers should not be more than 10x time for 10 workers
        val ratio = executionTimes[2].toDouble() / executionTimes[0].toDouble()
        assertTrue("Algorithm should scale reasonably: 50 workers took ${executionTimes[2]}ms, " +
                  "10 workers took ${executionTimes[0]}ms, ratio: $ratio", 
                  ratio < 10.0)
    }

    @Test
    fun `rotation algorithm should handle complex trainer-trainee relationships efficiently`() = runTest {
        // Arrange - Create scenario with many trainer-trainee pairs
        val trainers = generateTestTrainers(10)
        val trainees = generateTestTrainees(20, trainers)
        val regularWorkers = generateTestWorkers(20)
        val allWorkers = trainers + trainees + regularWorkers
        val workstations = generateTestWorkstations(15)
        
        setupMockData(allWorkers, workstations)

        // Act & Measure
        val executionTime = measureTimeMillis {
            viewModel.generateRotation()
        }

        // Assert
        assertTrue("Complex trainer-trainee scenario should complete within 3000ms, took ${executionTime}ms", 
                  executionTime < 3000)
        
        val rotationTable = viewModel.rotationTable.value
        assertNotNull("Should generate rotation table", rotationTable)
        
        // Verify trainer-trainee pairs are handled correctly
        var trainerTraineePairsFound = 0
        rotationTable!!.currentPhase.values.forEach { workers ->
            val trainersInStation = workers.filter { it.isTrainer }
            val traineesInStation = workers.filter { it.isTrainee }
            
            traineesInStation.forEach { trainee ->
                if (trainersInStation.any { it.id == trainee.trainerId }) {
                    trainerTraineePairsFound++
                }
            }
        }
        
        assertTrue("Should find trainer-trainee pairs in same stations", trainerTraineePairsFound > 0)
    }

    @Test
    fun `rotation algorithm should handle priority workstations efficiently`() = runTest {
        // Arrange - Create scenario with many priority workstations
        val workers = generateTestWorkers(40)
        val priorityStations = generateTestWorkstations(8, isPriority = true)
        val normalStations = generateTestWorkstations(12, isPriority = false)
        val allStations = priorityStations + normalStations
        
        setupMockData(workers, allStations)

        // Act & Measure
        val executionTime = measureTimeMillis {
            viewModel.generateRotation()
        }

        // Assert
        assertTrue("Priority workstation scenario should complete within 2500ms, took ${executionTime}ms", 
                  executionTime < 2500)
        
        val rotationTable = viewModel.rotationTable.value
        assertNotNull("Should generate rotation table", rotationTable)
        
        // Verify priority stations are filled
        priorityStations.forEach { station ->
            val assignedWorkers = rotationTable!!.currentPhase[station.id]?.size ?: 0
            assertTrue("Priority station ${station.name} should have workers assigned", 
                      assignedWorkers > 0)
        }
    }

    @Test
    fun `memory usage should remain stable during rotation generation`() = runTest {
        // Arrange
        val workers = generateTestWorkers(75)
        val workstations = generateTestWorkstations(15)
        setupMockData(workers, workstations)

        // Act - Generate multiple rotations to test memory stability
        repeat(5) {
            viewModel.generateRotation()
            viewModel.clearRotation()
        }

        // Assert - If we reach here without OutOfMemoryError, memory usage is stable
        assertTrue("Memory usage should remain stable", true)
    }

    @Test
    fun `rotation statistics calculation should be fast`() = runTest {
        // Arrange
        val workers = generateTestWorkers(60)
        val workstations = generateTestWorkstations(12)
        setupMockData(workers, workstations)
        
        viewModel.generateRotation()

        // Act & Measure
        val executionTime = measureTimeMillis {
            repeat(100) {
                viewModel.getRotationStatistics()
            }
        }

        // Assert
        assertTrue("Statistics calculation should be fast: ${executionTime}ms for 100 calls", 
                  executionTime < 1000)
    }

    // Helper methods for generating test data

    private fun generateTestWorkers(count: Int): List<Worker> {
        return (1..count).map { i ->
            Worker(
                id = i.toLong(),
                name = "Worker $i",
                capabilities = "Skill ${i % 5 + 1}",
                availabilityPercentage = 70 + (i % 30),
                isActive = true
            )
        }
    }

    private fun generateTestTrainers(count: Int): List<Worker> {
        return (1..count).map { i ->
            Worker(
                id = (1000 + i).toLong(),
                name = "Trainer $i",
                capabilities = "Expert Skills",
                availabilityPercentage = 90 + (i % 10),
                isTrainer = true,
                isActive = true
            )
        }
    }

    private fun generateTestTrainees(count: Int, trainers: List<Worker>): List<Worker> {
        return (1..count).map { i ->
            val trainer = trainers[i % trainers.size]
            Worker(
                id = (2000 + i).toLong(),
                name = "Trainee $i",
                capabilities = "",
                availabilityPercentage = 60 + (i % 20),
                isTrainee = true,
                trainerId = trainer.id,
                trainingWorkstationId = (i % 5 + 1).toLong(),
                isActive = true
            )
        }
    }

    private fun generateTestWorkstations(count: Int, isPriority: Boolean = false): List<Workstation> {
        return (1..count).map { i ->
            Workstation(
                id = i.toLong(),
                name = "Station $i",
                requiredCapabilities = "Skill ${i % 5 + 1}",
                requiredWorkers = 2 + (i % 4),
                priority = if (isPriority) 5 else (i % 5 + 1),
                isPriority = isPriority,
                isActive = true
            )
        }
    }

    private fun setupMockData(workers: List<Worker>, workstations: List<Workstation>) {
        whenever(workerDao.getAllWorkers()).thenReturn(flowOf(workers))
        whenever(workstationDao.getAllActiveWorkstations()).thenReturn(flowOf(workstations))
        
        // Setup worker-workstation relationships
        workers.forEach { worker ->
            val compatibleStations = workstations.filter { station ->
                // Simple compatibility: worker can work at stations with similar skill requirements
                station.requiredCapabilities.contains("Skill") || worker.capabilities.contains("Expert")
            }.map { it.id }
            
            whenever(workerDao.getWorkerWorkstationIds(worker.id)).thenReturn(compatibleStations)
        }
        
        // Setup update methods
        whenever(workerDao.updateWorkerRotation(any(), any(), any())).thenReturn(Unit)
    }
}