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
            email = "juan@example.com",
            restrictionNotes = "No levantar peso"
        )

        // Assert
        assertEquals("Juan Pérez", worker.name)
        assertEquals("juan@example.com", worker.email)
        assertEquals("No levantar peso", worker.restrictionNotes)
        assertTrue(worker.isActive)
        assertFalse(worker.isTrainee)
        assertNull(worker.trainerId)
        assertNull(worker.currentWorkstationId)
        assertEquals(100, worker.availabilityPercentage)
        assertFalse(worker.isCertified)
    }

    @Test
    fun `worker in training should have trainer id`() {
        // Arrange & Act
        val trainer = Worker(name = "Trainer", isTrainer = true)
        val trainee = Worker(
            name = "Aprendiz",
            isTrainee = true,
            trainerId = trainer.id
        )

        // Assert
        assertTrue(trainee.isTrainee)
        assertEquals(trainer.id, trainee.trainerId)
        assertTrue(trainer.isTrainer)
    }

    @Test
    fun `worker should be able to graduate from training`() {
        // Arrange
        val worker = Worker(
            name = "Aprendiz",
            isTrainee = true,
            trainerId = 1L
        )

        // Act - Simulate graduation
        val graduatedWorker = worker.copy(
            isTrainee = false,
            trainerId = null,
            isCertified = true,
            certificationDate = System.currentTimeMillis()
        )

        // Assert
        assertFalse(graduatedWorker.isTrainee)
        assertNull(graduatedWorker.trainerId)
        assertTrue(graduatedWorker.isCertified)
        assertNotNull(graduatedWorker.certificationDate)
    }

    @Test
    fun `worker availability should be manageable`() {
        // Arrange
        val worker = Worker(name = "Test Worker")

        // Act & Assert - Initially active
        assertTrue(worker.isActive)

        // Act - Make inactive
        val inactiveWorker = worker.copy(isActive = false)
        assertFalse(inactiveWorker.isActive)

        // Act - Make active again
        val activeWorker = inactiveWorker.copy(isActive = true)
        assertTrue(activeWorker.isActive)
    }

    @Test
    fun `worker should track current workstation`() {
        // Arrange
        val worker = Worker(name = "Test Worker")
        val workstationId = 5L

        // Act
        val updatedWorker = worker.copy(currentWorkstationId = workstationId)

        // Assert
        assertEquals(workstationId, updatedWorker.currentWorkstationId)
    }

    @Test
    fun `worker display name should include status icons`() {
        // Arrange & Act
        val leader = Worker(name = "Leader", isLeader = true)
        val trainer = Worker(name = "Trainer", isTrainer = true)
        val trainee = Worker(name = "Trainee", isTrainee = true)
        val certified = Worker(name = "Certified", isCertified = true)
        val regular = Worker(name = "Regular")

        // Assert
        assertEquals("Leader 👑", leader.getDisplayName())
        assertEquals("Trainer 👨‍🏫", trainer.getDisplayName())
        assertEquals("Trainee 🎯", trainee.getDisplayName())
        assertEquals("Certified 🏆", certified.getDisplayName())
        assertEquals("Regular", regular.getDisplayName())
    }

    @Test
    fun `worker restrictions should be optional`() {
        // Arrange & Act
        val workerWithRestrictions = Worker(
            name = "Restricted Worker",
            restrictionNotes = "No levantar peso, Solo turno diurno"
        )
        
        val workerWithoutRestrictions = Worker(
            name = "Unrestricted Worker"
        )

        // Assert
        assertTrue(workerWithRestrictions.hasRestrictions())
        assertFalse(workerWithoutRestrictions.hasRestrictions())
        assertEquals("No levantar peso, Solo turno diurno", workerWithRestrictions.restrictionNotes)
        assertEquals("", workerWithoutRestrictions.restrictionNotes)
    }

    @Test
    fun `worker availability status should be descriptive`() {
        // Arrange & Act
        val highAvailability = Worker(name = "High", availabilityPercentage = 90)
        val mediumAvailability = Worker(name = "Medium", availabilityPercentage = 65)
        val lowAvailability = Worker(name = "Low", availabilityPercentage = 30)

        // Assert
        assertEquals("Alta", highAvailability.getAvailabilityStatus())
        assertEquals("Media", mediumAvailability.getAvailabilityStatus())
        assertEquals("Baja", lowAvailability.getAvailabilityStatus())
    }

    @Test
    fun `worker rotation priority should be calculated correctly`() {
        // Arrange & Act
        val leader = Worker(name = "Leader", isLeader = true)
        val trainedWorker = Worker(name = "Trained", rotationsInCurrentStation = 3)
        val regularWorker = Worker(name = "Regular")
        val trainer = Worker(name = "Trainer", isTrainer = true)

        // Assert
        assertEquals(200, leader.getRotationPriority())
        assertEquals(103, trainedWorker.getRotationPriority()) // 100 + 3 rotations
        assertEquals(50, regularWorker.getRotationPriority())
        assertEquals(10, trainer.getRotationPriority())
    }

    @Test
    fun `worker leadership type should be descriptive`() {
        // Arrange & Act
        val bothHalves = Worker(name = "Both", isLeader = true, leadershipType = "BOTH")
        val firstHalf = Worker(name = "First", isLeader = true, leadershipType = "FIRST_HALF")
        val secondHalf = Worker(name = "Second", isLeader = true, leadershipType = "SECOND_HALF")

        // Assert
        assertEquals("Ambas partes", bothHalves.getLeadershipTypeDescription())
        assertEquals("Primera parte", firstHalf.getLeadershipTypeDescription())
        assertEquals("Segunda parte", secondHalf.getLeadershipTypeDescription())
    }
}