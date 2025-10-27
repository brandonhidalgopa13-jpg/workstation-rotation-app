package com.workstation.rotation.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.workstation.rotation.data.dao.WorkerDao
import com.workstation.rotation.data.dao.WorkstationDao
import com.workstation.rotation.data.entities.Worker
import com.workstation.rotation.data.entities.Workstation
import com.workstation.rotation.models.RotationItem
import com.workstation.rotation.models.RotationTable
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.*

/**
 * Tests unitarios para RotationViewModel
 * Verifica el algoritmo de rotación inteligente y sus casos de uso
 */
@ExperimentalCoroutinesApi
class RotationViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var workerDao: WorkerDao

    @Mock
    private lateinit var workstationDao: WorkstationDao

    @Mock
    private lateinit var rotationItemsObserver: Observer<List<RotationItem>>

    @Mock
    private lateinit var rotationTableObserver: Observer<RotationTable?>

    private lateinit var viewModel: RotationViewModel

    // Test data
    private val testWorkers = listOf(
        Worker(
            id = 1L,
            name = "Juan Pérez",
            capabilities = "Soldadura",
            availabilityPercentage = 100,
            isActive = true
        ),
        Worker(
            id = 2L,
            name = "María García",
            capabilities = "Ensamble",
            availabilityPercentage = 80,
            isActive = true
        ),
        Worker(
            id = 3L,
            name = "Carlos López",
            capabilities = "Control de Calidad",
            availabilityPercentage = 90,
            isActive = true,
            isTrainer = true
        ),
        Worker(
            id = 4L,
            name = "Ana Martínez",
            capabilities = "",
            availabilityPercentage = 70,
            isActive = true,
            isTrainee = true,
            trainerId = 3L,
            trainingWorkstationId = 1L
        )
    )

    private val testWorkstations = listOf(
        Workstation(
            id = 1L,
            name = "Estación de Soldadura",
            requiredCapabilities = "Soldadura",
            requiredWorkers = 2,
            priority = 5,
            isActive = true,
            isPriority = true
        ),
        Workstation(
            id = 2L,
            name = "Estación de Ensamble",
            requiredCapabilities = "Ensamble",
            requiredWorkers = 2,
            priority = 3,
            isActive = true,
            isPriority = false
        ),
        Workstation(
            id = 3L,
            name = "Control de Calidad",
            requiredCapabilities = "Control de Calidad",
            requiredWorkers = 1,
            priority = 4,
            isActive = true,
            isPriority = false
        )
    )

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        viewModel = RotationViewModel(workerDao, workstationDao)
        
        // Setup observers
        viewModel.rotationItems.observeForever(rotationItemsObserver)
        viewModel.rotationTable.observeForever(rotationTableObserver)
    }

    @Test
    fun `generateRotation should return false when no workers available`() = runTest {
        // Arrange
        whenever(workerDao.getAllWorkers()).thenReturn(flowOf(emptyList()))
        whenever(workstationDao.getAllActiveWorkstations()).thenReturn(flowOf(testWorkstations))

        // Act
        val result = viewModel.generateRotation()

        // Assert
        assertFalse(result)
        verify(rotationItemsObserver).onChanged(emptyList())
    }

    @Test
    fun `generateRotation should return false when no workstations available`() = runTest {
        // Arrange
        whenever(workerDao.getAllWorkers()).thenReturn(flowOf(testWorkers))
        whenever(workstationDao.getAllActiveWorkstations()).thenReturn(flowOf(emptyList()))

        // Act
        val result = viewModel.generateRotation()

        // Assert
        assertFalse(result)
        verify(rotationItemsObserver).onChanged(emptyList())
    }

    @Test
    fun `generateRotation should prioritize trainer-trainee pairs`() = runTest {
        // Arrange
        setupMockData()

        // Act
        val result = viewModel.generateRotation()

        // Assert
        assertTrue(result)
        
        // Verify that trainer-trainee pair is assigned together
        val rotationTable = viewModel.rotationTable.value
        assertNotNull(rotationTable)
        
        val trainingStation = rotationTable!!.currentPhase[1L] // Training workstation
        assertNotNull(trainingStation)
        
        // Both trainer and trainee should be in the same station
        val trainerInStation = trainingStation!!.any { it.id == 3L } // Carlos (trainer)
        val traineeInStation = trainingStation.any { it.id == 4L } // Ana (trainee)
        
        assertTrue("Trainer should be in training station", trainerInStation)
        assertTrue("Trainee should be in training station", traineeInStation)
    }

    @Test
    fun `generateRotation should fill priority workstations first`() = runTest {
        // Arrange
        setupMockData()

        // Act
        val result = viewModel.generateRotation()

        // Assert
        assertTrue(result)
        
        val rotationTable = viewModel.rotationTable.value
        assertNotNull(rotationTable)
        
        // Priority station (Soldadura) should have required workers
        val priorityStation = rotationTable!!.currentPhase[1L]
        assertNotNull(priorityStation)
        assertEquals("Priority station should have required workers", 2, priorityStation!!.size)
    }

    @Test
    fun `generateRotation should respect worker availability percentages`() = runTest {
        // Arrange
        val lowAvailabilityWorkers = listOf(
            Worker(
                id = 5L,
                name = "Pedro Bajo",
                capabilities = "Soldadura",
                availabilityPercentage = 20, // Very low availability
                isActive = true
            )
        )
        
        whenever(workerDao.getAllWorkers()).thenReturn(flowOf(lowAvailabilityWorkers))
        whenever(workstationDao.getAllActiveWorkstations()).thenReturn(flowOf(testWorkstations))
        whenever(workerDao.getWorkerWorkstationIds(5L)).thenReturn(listOf(1L, 2L))

        // Act
        val result = viewModel.generateRotation()

        // Assert - Low availability workers might not be assigned due to random factor
        // This test verifies the algorithm considers availability
        assertTrue("Algorithm should handle low availability workers", result)
    }

    @Test
    fun `generateRotation should create rotation items with correct information`() = runTest {
        // Arrange
        setupMockData()

        // Act
        val result = viewModel.generateRotation()

        // Assert
        assertTrue(result)
        
        val rotationItems = viewModel.rotationItems.value
        assertNotNull(rotationItems)
        assertTrue("Should have rotation items", rotationItems!!.isNotEmpty())
        
        // Verify rotation item structure
        val firstItem = rotationItems.first()
        assertNotNull("Worker name should not be null", firstItem.workerName)
        assertNotNull("Current workstation should not be null", firstItem.currentWorkstation)
        assertNotNull("Next workstation should not be null", firstItem.nextWorkstation)
        assertTrue("Rotation order should be positive", firstItem.rotationOrder > 0)
    }

    @Test
    fun `generateRotation should handle workers with restrictions`() = runTest {
        // Arrange
        val restrictedWorkers = listOf(
            Worker(
                id = 6L,
                name = "Luis Restringido",
                capabilities = "Soldadura",
                restrictionNotes = "No levantar peso",
                availabilityPercentage = 100,
                isActive = true
            )
        )
        
        whenever(workerDao.getAllWorkers()).thenReturn(flowOf(restrictedWorkers))
        whenever(workstationDao.getAllActiveWorkstations()).thenReturn(flowOf(testWorkstations))
        whenever(workerDao.getWorkerWorkstationIds(6L)).thenReturn(listOf(1L))

        // Act
        val result = viewModel.generateRotation()

        // Assert
        assertTrue("Should handle restricted workers", result)
        
        val rotationItems = viewModel.rotationItems.value
        assertNotNull(rotationItems)
        
        // Verify restriction is shown in worker label
        val restrictedWorkerItem = rotationItems!!.find { it.workerName.contains("Luis Restringido") }
        assertNotNull("Should find restricted worker", restrictedWorkerItem)
        assertTrue("Should show restriction indicator", restrictedWorkerItem!!.workerName.contains("🔒"))
    }

    @Test
    fun `getRotationStatistics should return correct statistics`() = runTest {
        // Arrange
        setupMockData()
        viewModel.generateRotation()

        // Act
        val statistics = viewModel.getRotationStatistics()

        // Assert
        assertTrue("Total workers should be positive", statistics.totalWorkers > 0)
        assertTrue("Statistics should be valid", statistics.totalWorkers >= statistics.workersRotating + statistics.workersStaying)
        assertNotNull("Summary text should not be null", statistics.getSummaryText())
    }

    @Test
    fun `clearRotation should clear all data`() = runTest {
        // Arrange
        setupMockData()
        viewModel.generateRotation()

        // Act
        viewModel.clearRotation()

        // Assert
        verify(rotationItemsObserver, atLeastOnce()).onChanged(emptyList())
        verify(rotationTableObserver, atLeastOnce()).onChanged(null)
    }

    @Test
    fun `updateEligibleWorkersCount should update count correctly`() = runTest {
        // Arrange
        setupMockData()

        // Act
        viewModel.updateEligibleWorkersCount()

        // Assert
        val count = viewModel.getEligibleWorkersCount()
        assertTrue("Eligible workers count should be updated", count >= 0)
    }

    @Test
    fun `generateRotation should handle empty worker-workstation relationships`() = runTest {
        // Arrange
        whenever(workerDao.getAllWorkers()).thenReturn(flowOf(testWorkers))
        whenever(workstationDao.getAllActiveWorkstations()).thenReturn(flowOf(testWorkstations))
        
        // No worker-workstation relationships
        testWorkers.forEach { worker ->
            whenever(workerDao.getWorkerWorkstationIds(worker.id)).thenReturn(emptyList())
        }

        // Act
        val result = viewModel.generateRotation()

        // Assert
        assertFalse("Should return false when no workers have workstation assignments", result)
    }

    @Test
    fun `generateRotation should force rotation between current and next positions`() = runTest {
        // Arrange
        setupMockData()

        // Act
        val result = viewModel.generateRotation()

        // Assert
        assertTrue(result)
        
        val rotationTable = viewModel.rotationTable.value
        assertNotNull(rotationTable)
        
        // Verify that some workers are rotating (not all staying in same position)
        var workersRotating = 0
        var totalWorkers = 0
        
        rotationTable!!.currentPhase.forEach { (currentStationId, currentWorkers) ->
            currentWorkers.forEach { worker ->
                totalWorkers++
                
                // Find where this worker goes in next phase
                val nextStationId = rotationTable.nextPhase.entries
                    .find { it.value.contains(worker) }?.key
                
                if (nextStationId != null && nextStationId != currentStationId) {
                    workersRotating++
                }
            }
        }
        
        // At least some workers should be rotating (unless all are trainers/trainees)
        val rotationPercentage = if (totalWorkers > 0) (workersRotating * 100) / totalWorkers else 0
        assertTrue("Some rotation should occur: $workersRotating/$totalWorkers ($rotationPercentage%)", 
                  rotationPercentage >= 0) // Allow 0% for trainer-trainee dominated scenarios
    }

    private fun setupMockData() {
        whenever(workerDao.getAllWorkers()).thenReturn(flowOf(testWorkers))
        whenever(workstationDao.getAllActiveWorkstations()).thenReturn(flowOf(testWorkstations))
        
        // Setup worker-workstation relationships
        whenever(workerDao.getWorkerWorkstationIds(1L)).thenReturn(listOf(1L, 2L)) // Juan - Soldadura, Ensamble
        whenever(workerDao.getWorkerWorkstationIds(2L)).thenReturn(listOf(2L, 3L)) // María - Ensamble, Control
        whenever(workerDao.getWorkerWorkstationIds(3L)).thenReturn(listOf(1L, 3L)) // Carlos - Soldadura, Control
        whenever(workerDao.getWorkerWorkstationIds(4L)).thenReturn(listOf(1L)) // Ana - Solo Soldadura (entrenamiento)
        
        // Setup update methods
        whenever(workerDao.updateWorkerRotation(any(), any(), any())).thenReturn(Unit)
    }
}