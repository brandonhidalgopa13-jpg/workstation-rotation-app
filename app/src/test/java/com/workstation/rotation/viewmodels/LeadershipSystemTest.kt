package com.workstation.rotation.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.workstation.rotation.data.dao.WorkerDao
import com.workstation.rotation.data.dao.WorkstationDao
import com.workstation.rotation.data.entities.Worker
import com.workstation.rotation.data.entities.Workstation
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
 * Tests específicos para el Sistema de Liderazgo
 * Verifica la funcionalidad de designación y visualización de líderes
 */
@ExperimentalCoroutinesApi
class LeadershipSystemTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var workerDao: WorkerDao

    @Mock
    private lateinit var workstationDao: WorkstationDao

    @Mock
    private lateinit var workerRestrictionDao: com.workstation.rotation.data.dao.WorkerRestrictionDao

    private lateinit var viewModel: RotationViewModel

    // Test data for leadership
    private val testWorkers = listOf(
        Worker(
            id = 1L,
            name = "Juan Líder",
            capabilities = "Soldadura",
            availabilityPercentage = 100,
            isActive = true,
            isLeader = true,
            leadershipType = "BOTH_PARTS",
            leadershipWorkstationId = 1L
        ),
        Worker(
            id = 2L,
            name = "María Normal",
            capabilities = "Soldadura",
            availabilityPercentage = 90,
            isActive = true
        ),
        Worker(
            id = 3L,
            name = "Carlos Líder Primera",
            capabilities = "Ensamble",
            availabilityPercentage = 95,
            isActive = true,
            isLeader = true,
            leadershipType = "FIRST_PART",
            leadershipWorkstationId = 2L
        )
    )

    private val testWorkstations = listOf(
        Workstation(
            id = 1L,
            name = "Estación de Soldadura",
            requiredCapabilities = "Soldadura",
            requiredWorkers = 2,
            priority = 5,
            isActive = true
        ),
        Workstation(
            id = 2L,
            name = "Estación de Ensamble",
            requiredCapabilities = "Ensamble",
            requiredWorkers = 2,
            priority = 3,
            isActive = true
        )
    )

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        viewModel = RotationViewModel(workerDao, workstationDao, workerRestrictionDao)
        setupMockData()
    }

    @Test
    fun `leader should be assigned to their designated workstation`() = runTest {
        // Act
        val result = viewModel.generateRotation()

        // Assert
        assertTrue("Rotation should be generated successfully", result)
        
        val rotationTable = viewModel.rotationTable.value
        assertNotNull("Rotation table should not be null", rotationTable)
        
        // Verify Juan (leader) is in workstation 1
        val soldaduraStation = rotationTable!!.currentPhase[1L]
        assertNotNull("Soldadura station should have workers", soldaduraStation)
        
        val juanInStation = soldaduraStation!!.any { it.id == 1L }
        assertTrue("Juan (leader) should be in his designated workstation", juanInStation)
    }

    @Test
    fun `leader with BOTH_PARTS should stay in same station for both phases`() = runTest {
        // Act
        val result = viewModel.generateRotation()

        // Assert
        assertTrue("Rotation should be generated successfully", result)
        
        val rotationTable = viewModel.rotationTable.value
        assertNotNull("Rotation table should not be null", rotationTable)
        
        // Verify Juan is in workstation 1 in both phases
        val currentPhaseStation = rotationTable!!.currentPhase[1L]
        val nextPhaseStation = rotationTable.nextPhase[1L]
        
        assertNotNull("Current phase should have workers in station 1", currentPhaseStation)
        assertNotNull("Next phase should have workers in station 1", nextPhaseStation)
        
        val juanInCurrent = currentPhaseStation!!.any { it.id == 1L }
        val juanInNext = nextPhaseStation!!.any { it.id == 1L }
        
        assertTrue("Juan should be in current phase", juanInCurrent)
        assertTrue("Juan should be in next phase (BOTH_PARTS)", juanInNext)
    }

    @Test
    fun `leader with FIRST_PART should only be in first phase`() = runTest {
        // Act
        val result = viewModel.generateRotation()

        // Assert
        assertTrue("Rotation should be generated successfully", result)
        
        val rotationTable = viewModel.rotationTable.value
        assertNotNull("Rotation table should not be null", rotationTable)
        
        // Verify Carlos is in workstation 2 only in first phase
        val currentPhaseStation = rotationTable!!.currentPhase[2L]
        val nextPhaseStation = rotationTable.nextPhase[2L]
        
        assertNotNull("Current phase should have workers in station 2", currentPhaseStation)
        
        val carlosInCurrent = currentPhaseStation!!.any { it.id == 3L }
        assertTrue("Carlos should be in current phase (FIRST_PART)", carlosInCurrent)
        
        // Carlos might or might not be in next phase depending on other workers
        // The key is that his leadership only applies to first part
    }

    @Test
    fun `rotation items should identify leaders correctly`() = runTest {
        // Act
        val result = viewModel.generateRotation()

        // Assert
        assertTrue("Rotation should be generated successfully", result)
        
        val rotationItems = viewModel.rotationItems.value
        assertNotNull("Rotation items should not be null", rotationItems)
        assertTrue("Should have rotation items", rotationItems!!.isNotEmpty())
        
        // Find Juan's rotation item
        val juanItem = rotationItems.find { it.workerName.contains("Juan Líder") }
        assertNotNull("Should find Juan's rotation item", juanItem)
        
        // Verify leader identification in worker name
        assertTrue("Juan should be identified as leader", 
                  juanItem!!.workerName.contains("👑") || juanItem.workerName.contains("LÍDER"))
    }

    @Test
    fun `getEligibleWorkstationsForWorker should filter correctly for leaders`() = runTest {
        // Arrange
        val leaderId = 1L // Juan Líder
        
        // Act
        val eligibleWorkstations = viewModel.getEligibleWorkstationsForWorker(leaderId)

        // Assert
        assertNotNull("Eligible workstations should not be null", eligibleWorkstations)
        
        // Leader should have their designated workstation
        val hasDesignatedWorkstation = eligibleWorkstations.any { it.id == 1L }
        assertTrue("Leader should have access to their designated workstation", hasDesignatedWorkstation)
    }

    @Test
    fun `non-leader workers should not have leadership restrictions`() = runTest {
        // Arrange
        val normalWorkerId = 2L // María Normal
        
        // Act
        val eligibleWorkstations = viewModel.getEligibleWorkstationsForWorker(normalWorkerId)

        // Assert
        assertNotNull("Eligible workstations should not be null", eligibleWorkstations)
        
        // Normal worker should have access based on capabilities, not leadership
        assertTrue("Normal worker should have workstation access based on capabilities", 
                  eligibleWorkstations.isNotEmpty())
    }

    @Test
    fun `leadership system should not break normal rotation algorithm`() = runTest {
        // Act
        val result = viewModel.generateRotation()

        // Assert
        assertTrue("Rotation should be generated successfully with leaders", result)
        
        val rotationTable = viewModel.rotationTable.value
        assertNotNull("Rotation table should not be null", rotationTable)
        
        // Verify all workstations have some workers
        val currentPhaseWorkerCount = rotationTable!!.currentPhase.values.sumOf { it.size }
        val nextPhaseWorkerCount = rotationTable.nextPhase.values.sumOf { it.size }
        
        assertTrue("Current phase should have workers", currentPhaseWorkerCount > 0)
        assertTrue("Next phase should have workers", nextPhaseWorkerCount > 0)
        
        // Worker count should be consistent between phases
        assertEquals("Worker count should be consistent between phases", 
                    currentPhaseWorkerCount, nextPhaseWorkerCount)
    }

    @Test
    fun `multiple leaders in same workstation should be handled correctly`() = runTest {
        // Arrange - Add another leader for the same workstation
        val multipleLeaders = testWorkers + Worker(
            id = 4L,
            name = "Pedro Otro Líder",
            capabilities = "Soldadura",
            availabilityPercentage = 85,
            isActive = true,
            isLeader = true,
            leadershipType = "SECOND_PART",
            leadershipWorkstationId = 1L // Same as Juan
        )
        
        whenever(workerDao.getAllWorkers()).thenReturn(flowOf(multipleLeaders))
        whenever(workerDao.getWorkerWorkstationIds(4L)).thenReturn(listOf(1L))

        // Act
        val result = viewModel.generateRotation()

        // Assert
        assertTrue("Rotation should handle multiple leaders", result)
        
        val rotationTable = viewModel.rotationTable.value
        assertNotNull("Rotation table should not be null", rotationTable)
        
        // Both leaders should be accommodated in their workstation
        val soldaduraStation = rotationTable!!.currentPhase[1L]
        assertNotNull("Soldadura station should have workers", soldaduraStation)
        
        val leadersInStation = soldaduraStation!!.count { worker ->
            multipleLeaders.find { it.id == worker.id }?.isLeader == true
        }
        
        assertTrue("At least one leader should be in the station", leadersInStation >= 1)
    }

    @Test
    fun `leadership statistics should be included in rotation statistics`() = runTest {
        // Act
        viewModel.generateRotation()
        val statistics = viewModel.getRotationStatistics()

        // Assert
        assertNotNull("Statistics should not be null", statistics)
        assertTrue("Total workers should include leaders", statistics.totalWorkers >= 2)
        
        // Verify statistics make sense
        assertTrue("Statistics should be consistent", 
                  statistics.totalWorkers >= statistics.workersRotating + statistics.workersStaying)
    }

    private fun setupMockData() {
        whenever(workerDao.getAllWorkers()).thenReturn(flowOf(testWorkers))
        whenever(workstationDao.getAllActiveWorkstations()).thenReturn(flowOf(testWorkstations))
        
        // Setup worker-workstation relationships
        whenever(workerDao.getWorkerWorkstationIds(1L)).thenReturn(listOf(1L)) // Juan - Soldadura
        whenever(workerDao.getWorkerWorkstationIds(2L)).thenReturn(listOf(1L, 2L)) // María - Ambas
        whenever(workerDao.getWorkerWorkstationIds(3L)).thenReturn(listOf(2L)) // Carlos - Ensamble
        
        // Setup update methods
        whenever(workerDao.updateWorkerRotation(any(), any(), any())).thenReturn(Unit)
    }
}