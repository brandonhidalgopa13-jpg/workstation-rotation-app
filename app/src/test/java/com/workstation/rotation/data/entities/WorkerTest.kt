package com.workstation.rotation.data.entities

import org.junit.Test
import org.junit.Assert.*

/**
 * Tests unitarios para la entidad Worker
 * Verifica la lógica de negocio de los trabajadores
 */
class WorkerTest {

    @Test
    fun `worker creation should set correct default values`() {
        // Arrange & Act
        val worker = Worker(
            name = "Juan Pérez",
            capabilities = "Soldadura, Ensamble",
            restrictions = "No levantar peso"
        )

        // Assert
        assertEquals("Juan Pérez", worker.name)
        assertEquals("Soldadura, Ensamble", worker.capabilities)
        assertEquals("No levantar peso", worker.restrictions)
        assertTrue(worker.isAvailable)
        assertFalse(worker.isInTraining)
        assertNull(worker.trainerId)
        assertNull(worker.lastWorkstationId)
        assertTrue(worker.createdAt > 0)
        assertTrue(worker.updatedAt > 0)
    }

    @Test
    fun `worker in training should have trainer id`() {
        // Arrange & Act
        val trainer = Worker(name = "Trainer", capabilities = "Experto")
        val trainee = Worker(
            name = "Aprendiz",
            capabilities = "",
            isInTraining = true,
            trainerId = trainer.id
        )

        // Assert
        assertTrue(trainee.isInTraining)
        assertEquals(trainer.id, trainee.trainerId)
    }

    @Test
    fun `worker should be able to graduate from training`() {
        // Arrange
        val worker = Worker(
            name = "Aprendiz",
            capabilities = "",
            isInTraining = true,
            trainerId = 1L
        )

        // Act - Simulate graduation
        val graduatedWorker = worker.copy(
            isInTraining = false,
            trainerId = null,
            capabilities = "Soldadura Básica"
        )

        // Assert
        assertFalse(graduatedWorker.isInTraining)
        assertNull(graduatedWorker.trainerId)
        assertEquals("Soldadura Básica", graduatedWorker.capabilities)
    }

    @Test
    fun `worker availability should be manageable`() {
        // Arrange
        val worker = Worker(name = "Test Worker")

        // Act & Assert - Initially available
        assertTrue(worker.isAvailable)

        // Act - Make unavailable
        val unavailableWorker = worker.copy(isAvailable = false)
        assertFalse(unavailableWorker.isAvailable)

        // Act - Make available again
        val availableWorker = unavailableWorker.copy(isAvailable = true)
        assertTrue(availableWorker.isAvailable)
    }

    @Test
    fun `worker should track last workstation`() {
        // Arrange
        val worker = Worker(name = "Test Worker")
        val workstationId = 5L

        // Act
        val updatedWorker = worker.copy(lastWorkstationId = workstationId)

        // Assert
        assertEquals(workstationId, updatedWorker.lastWorkstationId)
    }

    @Test
    fun `worker capabilities should be stored as string`() {
        // Arrange & Act
        val worker = Worker(
            name = "Multi-skill Worker",
            capabilities = "Soldadura, Ensamble, Control de Calidad, Empaque"
        )

        // Assert
        assertTrue(worker.capabilities.contains("Soldadura"))
        assertTrue(worker.capabilities.contains("Ensamble"))
        assertTrue(worker.capabilities.contains("Control de Calidad"))
        assertTrue(worker.capabilities.contains("Empaque"))
    }

    @Test
    fun `worker restrictions should be optional`() {
        // Arrange & Act
        val workerWithRestrictions = Worker(
            name = "Restricted Worker",
            restrictions = "No levantar peso, Solo turno diurno"
        )
        
        val workerWithoutRestrictions = Worker(
            name = "Unrestricted Worker",
            restrictions = ""
        )

        // Assert
        assertEquals("No levantar peso, Solo turno diurno", workerWithRestrictions.restrictions)
        assertEquals("", workerWithoutRestrictions.restrictions)
    }

    @Test
    fun `worker timestamps should be set on creation`() {
        // Arrange
        val beforeCreation = System.currentTimeMillis()
        
        // Act
        val worker = Worker(name = "Test Worker")
        
        // Assert
        val afterCreation = System.currentTimeMillis()
        assertTrue(worker.createdAt >= beforeCreation)
        assertTrue(worker.createdAt <= afterCreation)
        assertTrue(worker.updatedAt >= beforeCreation)
        assertTrue(worker.updatedAt <= afterCreation)
    }
}