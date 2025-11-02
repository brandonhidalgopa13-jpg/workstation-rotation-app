package com.workstation.rotation.data.entities

import org.junit.Test
import org.junit.Assert.*

/**
 * Tests unitarios para la entidad Workstation
 * Verifica la lógica de negocio de las estaciones de trabajo
 */
class WorkstationTest {

    @Test
    fun `workstation creation should set correct default values`() {
        // Arrange & Act
        val workstation = Workstation(
            name = "Estación de Soldadura",
            requiredWorkers = 2
        )

        // Assert
        assertEquals("Estación de Soldadura", workstation.name)
        assertEquals(2, workstation.requiredWorkers)
        assertFalse(workstation.isPriority)
        assertTrue(workstation.isActive)
    }

    @Test
    fun `workstation priority should be manageable`() {
        // Arrange & Act
        val regularStation = Workstation(name = "Regular Station", isPriority = false)
        val priorityStation = Workstation(name = "Priority Station", isPriority = true)

        // Assert
        assertFalse(regularStation.isPriority)
        assertTrue(priorityStation.isPriority)
    }

    @Test
    fun `workstation can be activated and deactivated`() {
        // Arrange
        val workstation = Workstation(name = "Test Station")

        // Act & Assert - Initially active
        assertTrue(workstation.isActive)

        // Act - Deactivate
        val deactivatedStation = workstation.copy(isActive = false)
        assertFalse(deactivatedStation.isActive)

        // Act - Reactivate
        val reactivatedStation = deactivatedStation.copy(isActive = true)
        assertTrue(reactivatedStation.isActive)
    }

    @Test
    fun `workstation display name should include priority indicator`() {
        // Arrange & Act
        val regularStation = Workstation(name = "Regular Station")
        val priorityStation = Workstation(name = "Priority Station", isPriority = true)

        // Assert
        assertEquals("Regular Station", regularStation.getDisplayName())
        assertEquals("Priority Station ⭐", priorityStation.getDisplayName())
    }

    @Test
    fun `workstation capacity info should be formatted correctly`() {
        // Arrange & Act
        val workstation = Workstation(name = "Test Station", requiredWorkers = 3, maxWorkers = 3)
        val workstationRange = Workstation(name = "Test Station Range", requiredWorkers = 2, maxWorkers = 4)

        // Assert
        assertEquals("3 trabajadores", workstation.getCapacityInfo())
        assertEquals("2-4 trabajadores", workstationRange.getCapacityInfo())
    }

    @Test
    fun `workstation should detect when at capacity`() {
        // Arrange & Act
        val workstation = Workstation(name = "Test Station", requiredWorkers = 2)

        // Assert
        assertFalse(workstation.isFullyOccupied(0))
        assertFalse(workstation.isFullyOccupied(1))
        assertTrue(workstation.isFullyOccupied(2))
        assertTrue(workstation.isFullyOccupied(3)) // Over capacity
    }

    @Test
    fun `workstation should handle different required worker counts`() {
        // Arrange & Act
        val singleWorker = Workstation(name = "Single", requiredWorkers = 1)
        val multiWorker = Workstation(name = "Multi", requiredWorkers = 5)

        // Assert
        assertEquals(1, singleWorker.requiredWorkers)
        assertEquals(5, multiWorker.requiredWorkers)
    }

    @Test
    fun `workstation should handle special characters in name`() {
        // Arrange & Act
        val workstation = Workstation(
            name = "Estación #1 - Control/Calidad (Área A)",
            requiredWorkers = 2
        )

        // Assert
        assertEquals("Estación #1 - Control/Calidad (Área A)", workstation.name)
    }

    @Test
    fun `workstation should maintain data integrity`() {
        // Arrange
        val originalWorkstation = Workstation(
            name = "Original Station",
            requiredWorkers = 2,
            isPriority = false,
            isActive = true
        )

        // Act - Update workstation
        val updatedWorkstation = originalWorkstation.copy(
            name = "Updated Station",
            isPriority = true
        )

        // Assert - ID should remain the same
        assertEquals(originalWorkstation.id, updatedWorkstation.id)
        
        // Assert - Updated fields should change
        assertEquals("Updated Station", updatedWorkstation.name)
        assertTrue(updatedWorkstation.isPriority)
        assertEquals(originalWorkstation.requiredWorkers, updatedWorkstation.requiredWorkers)
    }
}