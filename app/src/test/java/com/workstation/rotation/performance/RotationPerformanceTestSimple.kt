package com.workstation.rotation.performance

import com.workstation.rotation.data.entities.Worker
import com.workstation.rotation.data.entities.Workstation
import org.junit.Test
import org.junit.Assert.*

/**
 * Tests simplificados de rendimiento para el sistema de rotación
 */
class RotationPerformanceTestSimple {

    @Test
    fun `worker creation performance should be acceptable`() {
        // Arrange
        val startTime = System.currentTimeMillis()
        
        // Act - Create many workers
        val workers = (1..1000).map { i ->
            Worker(
                id = i.toLong(),
                name = "Worker $i",
                availabilityPercentage = 80 + (i % 20),
                isActive = true
            )
        }
        
        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime
        
        // Assert
        assertEquals(1000, workers.size)
        assertTrue("Worker creation took too long: ${duration}ms", duration < 1000)
    }

    @Test
    fun `workstation creation performance should be acceptable`() {
        // Arrange
        val startTime = System.currentTimeMillis()
        
        // Act - Create many workstations
        val workstations = (1..100).map { i ->
            Workstation(
                id = i.toLong(),
                name = "Station $i",
                requiredWorkers = 1 + (i % 5),
                isPriority = i % 10 == 0,
                isActive = true
            )
        }
        
        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime
        
        // Assert
        assertEquals(100, workstations.size)
        assertTrue("Workstation creation took too long: ${duration}ms", duration < 500)
    }

    @Test
    fun `worker display name generation should be fast`() {
        // Arrange
        val workers = listOf(
            Worker(name = "Regular Worker"),
            Worker(name = "Leader Worker", isLeader = true),
            Worker(name = "Trainer Worker", isTrainer = true),
            Worker(name = "Trainee Worker", isTrainee = true),
            Worker(name = "Certified Worker", isCertified = true)
        )
        
        val startTime = System.currentTimeMillis()
        
        // Act
        val displayNames = workers.map { it.getDisplayName() }
        
        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime
        
        // Assert
        assertEquals(5, displayNames.size)
        assertEquals("Regular Worker", displayNames[0])
        assertEquals("Leader Worker 👑", displayNames[1])
        assertEquals("Trainer Worker 👨‍🏫", displayNames[2])
        assertEquals("Trainee Worker 🎯", displayNames[3])
        assertEquals("Certified Worker 🏆", displayNames[4])
        assertTrue("Display name generation took too long: ${duration}ms", duration < 100)
    }

    @Test
    fun `workstation capacity calculations should be fast`() {
        // Arrange
        val workstation = Workstation(
            name = "Test Station",
            requiredWorkers = 5
        )
        
        val startTime = System.currentTimeMillis()
        
        // Act - Test multiple capacity calculations
        val results = (0..10).map { currentWorkers ->
            workstation.getCapacityInfo() to workstation.isFullyOccupied(currentWorkers)
        }
        
        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime
        
        // Assert
        assertEquals(11, results.size)
        assertEquals("(0/5)" to false, results[0])
        assertEquals("(5/5)" to true, results[5])
        assertEquals("(10/5)" to true, results[10])
        assertTrue("Capacity calculations took too long: ${duration}ms", duration < 50)
    }

    @Test
    fun `worker rotation priority calculation should be consistent`() {
        // Arrange
        val workers = listOf(
            Worker(name = "Leader", isLeader = true),
            Worker(name = "Trained High Rotations", rotationsInCurrentStation = 5), // isTrainedWorker() = true, needsToRotate() = true
            Worker(name = "Trained Low Rotations", rotationsInCurrentStation = 1), // isTrainedWorker() = true, needsToRotate() = false
            Worker(name = "Regular Worker"), // isTrainedWorker() = true, needsToRotate() = false
            Worker(name = "Trainer", isTrainer = true) // isTrainedWorker() = false
        )
        
        // Act
        val priorities = workers.map { it.getRotationPriority() }
        
        // Assert
        // Leader: 200, Trained High (needs rotate): 105, Trained Low: 50, Regular: 50, Trainer: 10
        assertEquals(listOf(200, 105, 50, 50, 10), priorities)
        
        // Verify sorting by priority works correctly
        val sortedWorkers = workers.sortedByDescending { it.getRotationPriority() }
        assertEquals("Leader", sortedWorkers[0].name)
        assertEquals("Trained High Rotations", sortedWorkers[1].name)
        assertEquals("Trainer", sortedWorkers[4].name)
    }
}