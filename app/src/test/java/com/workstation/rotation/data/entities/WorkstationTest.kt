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
            requiredCapabilities = "Soldadura",
            priority = 3
        )

        // Assert
        assertEquals("Estación de Soldadura", workstation.name)
        assertEquals("Soldadura", workstation.requiredCapabilities)
        assertEquals(3, workstation.priority)
        assertTrue(workstation.isActive)
        assertTrue(workstation.createdAt > 0)
        assertTrue(workstation.updatedAt > 0)
    }

    @Test
    fun `workstation priority should be within valid range`() {
        // Arrange & Act
        val lowPriority = Workstation(name = "Low Priority", priority = 1)
        val mediumPriority = Workstation(name = "Medium Priority", priority = 3)
        val highPriority = Workstation(name = "High Priority", priority = 5)

        // Assert
        assertEquals(1, lowPriority.priority)
        assertEquals(3, mediumPriority.priority)
        assertEquals(5, highPriority.priority)
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
    fun `workstation required capabilities should be stored as string`() {
        // Arrange & Act
        val workstation = Workstation(
            name = "Multi-skill Station",
            requiredCapabilities = "Soldadura, Control de Calidad"
        )

        // Assert
        assertTrue(workstation.requiredCapabilities.contains("Soldadura"))
        assertTrue(workstation.requiredCapabilities.contains("Control de Calidad"))
    }

    @Test
    fun `workstation can have empty required capabilities`() {
        // Arrange & Act
        val workstation = Workstation(
            name = "General Station",
            requiredCapabilities = ""
        )

        // Assert
        assertEquals("", workstation.requiredCapabilities)
    }

    @Test
    fun `workstation timestamps should be set on creation`() {
        // Arrange
        val beforeCreation = System.currentTimeMillis()
        
        // Act
        val workstation = Workstation(name = "Test Station")
        
        // Assert
        val afterCreation = System.currentTimeMillis()
        assertTrue(workstation.createdAt >= beforeCreation)
        assertTrue(workstation.createdAt <= afterCreation)
        assertTrue(workstation.updatedAt >= beforeCreation)
        assertTrue(workstation.updatedAt <= afterCreation)
    }

    @Test
    fun `workstation priority should affect sorting order`() {
        // Arrange
        val stations = listOf(
            Workstation(name = "Low", priority = 1),
            Workstation(name = "High", priority = 5),
            Workstation(name = "Medium", priority = 3)
        )

        // Act - Sort by priority descending (high priority first)
        val sortedStations = stations.sortedByDescending { it.priority }

        // Assert
        assertEquals("High", sortedStations[0].name)
        assertEquals("Medium", sortedStations[1].name)
        assertEquals("Low", sortedStations[2].name)
    }

    @Test
    fun `workstation should handle special characters in name`() {
        // Arrange & Act
        val workstation = Workstation(
            name = "Estación #1 - Control/Calidad (Área A)",
            requiredCapabilities = "Control de Calidad"
        )

        // Assert
        assertEquals("Estación #1 - Control/Calidad (Área A)", workstation.name)
    }

    @Test
    fun `workstation should maintain data integrity`() {
        // Arrange
        val originalWorkstation = Workstation(
            name = "Original Station",
            requiredCapabilities = "Original Skills",
            priority = 2,
            isActive = true
        )

        // Act - Update workstation
        val updatedWorkstation = originalWorkstation.copy(
            name = "Updated Station",
            priority = 4,
            updatedAt = System.currentTimeMillis()
        )

        // Assert - ID and creation time should remain the same
        assertEquals(originalWorkstation.id, updatedWorkstation.id)
        assertEquals(originalWorkstation.createdAt, updatedWorkstation.createdAt)
        
        // Assert - Updated fields should change
        assertEquals("Updated Station", updatedWorkstation.name)
        assertEquals(4, updatedWorkstation.priority)
        assertTrue(updatedWorkstation.updatedAt > originalWorkstation.updatedAt)
    }
}