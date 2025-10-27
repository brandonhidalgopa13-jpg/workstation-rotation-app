package com.workstation.rotation.data.database

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.workstation.rotation.data.entities.Worker
import com.workstation.rotation.data.entities.Workstation
import com.workstation.rotation.data.entities.WorkerWorkstationCrossRef
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

/**
 * Tests de integración para la base de datos Room
 * Verifica que las operaciones CRUD funcionen correctamente
 */
@RunWith(AndroidJUnit4::class)
class AppDatabaseTest {

    private lateinit var database: AppDatabase
    private lateinit var workerDao: com.workstation.rotation.data.dao.WorkerDao
    private lateinit var workstationDao: com.workstation.rotation.data.dao.WorkstationDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        
        workerDao = database.workerDao()
        workstationDao = database.workstationDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertAndRetrieveWorker() = runTest {
        // Arrange
        val worker = Worker(
            name = "Test Worker",
            capabilities = "Test Skills",
            availabilityPercentage = 100
        )

        // Act
        val workerId = workerDao.insertWorker(worker)
        val retrievedWorkers = workerDao.getAllWorkers().first()

        // Assert
        assertTrue("Worker ID should be positive", workerId > 0)
        assertEquals("Should have one worker", 1, retrievedWorkers.size)
        assertEquals("Worker name should match", "Test Worker", retrievedWorkers[0].name)
        assertEquals("Worker capabilities should match", "Test Skills", retrievedWorkers[0].capabilities)
    }

    @Test
    fun insertAndRetrieveWorkstation() = runTest {
        // Arrange
        val workstation = Workstation(
            name = "Test Station",
            requiredCapabilities = "Test Skills",
            requiredWorkers = 2,
            priority = 3
        )

        // Act
        val stationId = workstationDao.insertWorkstation(workstation)
        val retrievedStations = workstationDao.getAllActiveWorkstations().first()

        // Assert
        assertTrue("Station ID should be positive", stationId > 0)
        assertEquals("Should have one station", 1, retrievedStations.size)
        assertEquals("Station name should match", "Test Station", retrievedStations[0].name)
        assertEquals("Required workers should match", 2, retrievedStations[0].requiredWorkers)
    }

    @Test
    fun workerWorkstationRelationship() = runTest {
        // Arrange
        val worker = Worker(name = "Test Worker", capabilities = "Skills")
        val workstation = Workstation(name = "Test Station", requiredCapabilities = "Skills")

        // Act
        val workerId = workerDao.insertWorker(worker)
        val stationId = workstationDao.insertWorkstation(workstation)
        
        // Create relationship
        workerDao.insertWorkerWorkstationCrossRef(
            WorkerWorkstationCrossRef(workerId, stationId)
        )

        // Retrieve relationships
        val workerStationIds = workerDao.getWorkerWorkstationIds(workerId)
        val stationWorkerIds = workstationDao.getWorkstationWorkerIds(stationId)

        // Assert
        assertEquals("Worker should be assigned to one station", 1, workerStationIds.size)
        assertEquals("Station should have one worker", 1, stationWorkerIds.size)
        assertEquals("Station ID should match", stationId, workerStationIds[0])
        assertEquals("Worker ID should match", workerId, stationWorkerIds[0])
    }

    @Test
    fun updateWorker() = runTest {
        // Arrange
        val worker = Worker(name = "Original Name", capabilities = "Original Skills")
        val workerId = workerDao.insertWorker(worker)

        // Act
        val updatedWorker = worker.copy(
            id = workerId,
            name = "Updated Name",
            capabilities = "Updated Skills"
        )
        workerDao.updateWorker(updatedWorker)

        // Assert
        val retrievedWorkers = workerDao.getAllWorkers().first()
        assertEquals("Should still have one worker", 1, retrievedWorkers.size)
        assertEquals("Name should be updated", "Updated Name", retrievedWorkers[0].name)
        assertEquals("Capabilities should be updated", "Updated Skills", retrievedWorkers[0].capabilities)
    }

    @Test
    fun deleteWorker() = runTest {
        // Arrange
        val worker = Worker(name = "Test Worker", capabilities = "Skills")
        val workerId = workerDao.insertWorker(worker)

        // Act
        workerDao.deleteWorker(worker.copy(id = workerId))
        val retrievedWorkers = workerDao.getAllWorkers().first()

        // Assert
        assertEquals("Should have no workers after deletion", 0, retrievedWorkers.size)
    }

    @Test
    fun softDeleteWorker() = runTest {
        // Arrange
        val worker = Worker(name = "Test Worker", capabilities = "Skills", isActive = true)
        val workerId = workerDao.insertWorker(worker)

        // Act - Soft delete by setting isActive to false
        val deactivatedWorker = worker.copy(id = workerId, isActive = false)
        workerDao.updateWorker(deactivatedWorker)

        // Assert
        val allWorkers = workerDao.getAllWorkers().first()
        val activeWorkers = allWorkers.filter { it.isActive }
        
        assertEquals("Should still have one worker in total", 1, allWorkers.size)
        assertEquals("Should have no active workers", 0, activeWorkers.size)
        assertFalse("Worker should be inactive", allWorkers[0].isActive)
    }

    @Test
    fun trainerTraineeRelationship() = runTest {
        // Arrange
        val trainer = Worker(name = "Trainer", capabilities = "Expert Skills", isTrainer = true)
        val trainee = Worker(
            name = "Trainee", 
            capabilities = "", 
            isTrainee = true,
            trainingWorkstationId = 1L
        )

        // Act
        val trainerId = workerDao.insertWorker(trainer)
        val traineeWithTrainer = trainee.copy(trainerId = trainerId)
        val traineeId = workerDao.insertWorker(traineeWithTrainer)

        // Assert
        val allWorkers = workerDao.getAllWorkers().first()
        val retrievedTrainer = allWorkers.find { it.id == trainerId }
        val retrievedTrainee = allWorkers.find { it.id == traineeId }

        assertNotNull("Trainer should exist", retrievedTrainer)
        assertNotNull("Trainee should exist", retrievedTrainee)
        assertTrue("Trainer should be marked as trainer", retrievedTrainer!!.isTrainer)
        assertTrue("Trainee should be marked as trainee", retrievedTrainee!!.isTrainee)
        assertEquals("Trainee should have correct trainer ID", trainerId, retrievedTrainee.trainerId)
    }

    @Test
    fun workstationPriorityOrdering() = runTest {
        // Arrange
        val lowPriority = Workstation(name = "Low Priority", priority = 1)
        val highPriority = Workstation(name = "High Priority", priority = 5, isPriority = true)
        val mediumPriority = Workstation(name = "Medium Priority", priority = 3)

        // Act
        workstationDao.insertWorkstation(lowPriority)
        workstationDao.insertWorkstation(highPriority)
        workstationDao.insertWorkstation(mediumPriority)

        val allStations = workstationDao.getAllActiveWorkstations().first()
        val sortedByPriority = allStations.sortedByDescending { it.priority }

        // Assert
        assertEquals("Should have 3 stations", 3, allStations.size)
        assertEquals("First should be high priority", "High Priority", sortedByPriority[0].name)
        assertEquals("Second should be medium priority", "Medium Priority", sortedByPriority[1].name)
        assertEquals("Third should be low priority", "Low Priority", sortedByPriority[2].name)
        assertTrue("High priority station should be marked as priority", sortedByPriority[0].isPriority)
    }

    @Test
    fun workerAvailabilityFiltering() = runTest {
        // Arrange
        val highAvailability = Worker(name = "High Availability", availabilityPercentage = 90)
        val mediumAvailability = Worker(name = "Medium Availability", availabilityPercentage = 60)
        val lowAvailability = Worker(name = "Low Availability", availabilityPercentage = 30)

        // Act
        workerDao.insertWorker(highAvailability)
        workerDao.insertWorker(mediumAvailability)
        workerDao.insertWorker(lowAvailability)

        val allWorkers = workerDao.getAllWorkers().first()
        val highAvailabilityWorkers = allWorkers.filter { it.availabilityPercentage >= 80 }
        val mediumAvailabilityWorkers = allWorkers.filter { it.availabilityPercentage >= 50 }

        // Assert
        assertEquals("Should have 3 workers total", 3, allWorkers.size)
        assertEquals("Should have 1 high availability worker", 1, highAvailabilityWorkers.size)
        assertEquals("Should have 2 medium+ availability workers", 2, mediumAvailabilityWorkers.size)
    }

    @Test
    fun complexWorkerWorkstationRelationships() = runTest {
        // Arrange
        val worker1 = Worker(name = "Multi-skill Worker", capabilities = "Skill A, Skill B")
        val worker2 = Worker(name = "Specialist Worker", capabilities = "Skill A")
        
        val station1 = Workstation(name = "Station A", requiredCapabilities = "Skill A")
        val station2 = Workstation(name = "Station B", requiredCapabilities = "Skill B")
        val station3 = Workstation(name = "Station C", requiredCapabilities = "Skill C")

        // Act
        val worker1Id = workerDao.insertWorker(worker1)
        val worker2Id = workerDao.insertWorker(worker2)
        val station1Id = workstationDao.insertWorkstation(station1)
        val station2Id = workstationDao.insertWorkstation(station2)
        val station3Id = workstationDao.insertWorkstation(station3)

        // Multi-skill worker can work at stations A and B
        workerDao.insertWorkerWorkstationCrossRef(WorkerWorkstationCrossRef(worker1Id, station1Id))
        workerDao.insertWorkerWorkstationCrossRef(WorkerWorkstationCrossRef(worker1Id, station2Id))
        
        // Specialist worker can only work at station A
        workerDao.insertWorkerWorkstationCrossRef(WorkerWorkstationCrossRef(worker2Id, station1Id))

        // Assert
        val worker1Stations = workerDao.getWorkerWorkstationIds(worker1Id)
        val worker2Stations = workerDao.getWorkerWorkstationIds(worker2Id)
        val station1Workers = workstationDao.getWorkstationWorkerIds(station1Id)
        val station2Workers = workstationDao.getWorkstationWorkerIds(station2Id)
        val station3Workers = workstationDao.getWorkstationWorkerIds(station3Id)

        assertEquals("Multi-skill worker should work at 2 stations", 2, worker1Stations.size)
        assertEquals("Specialist worker should work at 1 station", 1, worker2Stations.size)
        assertEquals("Station A should have 2 workers", 2, station1Workers.size)
        assertEquals("Station B should have 1 worker", 1, station2Workers.size)
        assertEquals("Station C should have 0 workers", 0, station3Workers.size)
    }

    @Test
    fun databaseMigrationCompatibility() = runTest {
        // This test ensures that the database schema is consistent
        // and can handle the expected data types and constraints
        
        // Arrange & Act
        val worker = Worker(
            name = "Test Worker with Special Characters: áéíóú ñ",
            capabilities = "Skill 1, Skill 2, Skill 3",
            restrictionNotes = "Special restriction with symbols: @#$%",
            availabilityPercentage = 85,
            isTrainer = true,
            isTrainee = false,
            isActive = true
        )

        val workstation = Workstation(
            name = "Station with Special Characters: áéíóú ñ",
            requiredCapabilities = "Complex skill set with symbols: @#$%",
            requiredWorkers = 5,
            priority = 4,
            isPriority = true,
            isActive = true
        )

        // Act
        val workerId = workerDao.insertWorker(worker)
        val stationId = workstationDao.insertWorkstation(workstation)

        // Assert
        assertTrue("Worker ID should be valid", workerId > 0)
        assertTrue("Station ID should be valid", stationId > 0)

        val retrievedWorker = workerDao.getAllWorkers().first().find { it.id == workerId }
        val retrievedStation = workstationDao.getAllActiveWorkstations().first().find { it.id == stationId }

        assertNotNull("Worker should be retrievable", retrievedWorker)
        assertNotNull("Station should be retrievable", retrievedStation)
        
        assertEquals("Special characters should be preserved in worker name", 
                    worker.name, retrievedWorker!!.name)
        assertEquals("Special characters should be preserved in station name", 
                    workstation.name, retrievedStation!!.name)
    }
}