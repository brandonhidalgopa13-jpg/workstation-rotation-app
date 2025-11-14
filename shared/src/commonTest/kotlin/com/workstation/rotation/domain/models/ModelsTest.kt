package com.workstation.rotation.domain.models

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class ModelsTest {
    
    @Test
    fun testWorkerModelCreation() {
        // Arrange & Act
        val worker = WorkerModel(
            id = 1,
            name = "Juan Pérez",
            code = "JP001",
            isActive = true,
            capabilities = listOf("Soldadura", "Ensamblaje")
        )
        
        // Assert
        assertEquals(1, worker.id)
        assertEquals("Juan Pérez", worker.name)
        assertEquals("JP001", worker.code)
        assertTrue(worker.isActive)
        assertEquals(2, worker.capabilities.size)
    }
    
    @Test
    fun testWorkstationModelCreation() {
        // Arrange & Act
        val workstation = WorkstationModel(
            id = 1,
            name = "Ensamblaje A",
            code = "EST-001",
            isActive = true,
            requiredCapabilities = listOf("Ensamblaje")
        )
        
        // Assert
        assertEquals(1, workstation.id)
        assertEquals("Ensamblaje A", workstation.name)
        assertEquals("EST-001", workstation.code)
        assertTrue(workstation.isActive)
        assertEquals(1, workstation.requiredCapabilities.size)
    }
    
    @Test
    fun testRotationSessionModelCreation() {
        // Arrange & Act
        val session = RotationSessionModel(
            id = 1,
            name = "Rotación Turno Mañana",
            rotationIntervalMinutes = 60,
            isActive = true
        )
        
        // Assert
        assertEquals(1, session.id)
        assertEquals("Rotación Turno Mañana", session.name)
        assertEquals(60, session.rotationIntervalMinutes)
        assertTrue(session.isActive)
    }
    
    @Test
    fun testRotationAssignmentModelCreation() {
        // Arrange & Act
        val assignment = RotationAssignmentModel(
            id = 1,
            sessionId = 1,
            workerId = 1,
            workstationId = 1,
            rotationOrder = 0
        )
        
        // Assert
        assertEquals(1, assignment.id)
        assertEquals(1, assignment.sessionId)
        assertEquals(1, assignment.workerId)
        assertEquals(1, assignment.workstationId)
        assertEquals(0, assignment.rotationOrder)
    }
    
    @Test
    fun testWorkerActiveStatus() {
        // Arrange
        val activeWorker = WorkerModel(id = 1, name = "Active", code = "A1", isActive = true)
        val inactiveWorker = WorkerModel(id = 2, name = "Inactive", code = "I1", isActive = false)
        
        // Assert
        assertTrue(activeWorker.isActive)
        assertFalse(inactiveWorker.isActive)
    }
    
    @Test
    fun testWorkstationWithoutCapabilities() {
        // Arrange & Act
        val workstation = WorkstationModel(
            id = 1,
            name = "Simple Station",
            code = "SS-001",
            requiredCapabilities = emptyList()
        )
        
        // Assert
        assertTrue(workstation.requiredCapabilities.isEmpty())
    }
    
    @Test
    fun testWorkerWithoutCapabilities() {
        // Arrange & Act
        val worker = WorkerModel(
            id = 1,
            name = "Simple Worker",
            code = "SW-001",
            capabilities = emptyList()
        )
        
        // Assert
        assertTrue(worker.capabilities.isEmpty())
    }
    
    @Test
    fun testRotationSessionDefaultInterval() {
        // Arrange & Act
        val session = RotationSessionModel(
            id = 1,
            name = "Test Session",
            rotationIntervalMinutes = 60
        )
        
        // Assert
        assertEquals(60, session.rotationIntervalMinutes)
    }
    
    @Test
    fun testAssignmentOrdering() {
        // Arrange
        val assignments = listOf(
            RotationAssignmentModel(1, 1, 1, 1, 2),
            RotationAssignmentModel(2, 1, 2, 2, 0),
            RotationAssignmentModel(3, 1, 3, 3, 1)
        )
        
        // Act
        val sorted = assignments.sortedBy { it.rotationOrder }
        
        // Assert
        assertEquals(0, sorted[0].rotationOrder)
        assertEquals(1, sorted[1].rotationOrder)
        assertEquals(2, sorted[2].rotationOrder)
    }
}
