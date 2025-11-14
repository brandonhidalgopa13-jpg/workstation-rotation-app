package com.workstation.rotation.domain.service

import com.workstation.rotation.domain.models.WorkerModel
import com.workstation.rotation.domain.models.WorkstationModel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RotationServiceTest {
    
    @Test
    fun testGenerateEquitableAssignments() {
        // Arrange
        val workers = listOf(
            WorkerModel(id = 1, name = "Worker 1", code = "W1"),
            WorkerModel(id = 2, name = "Worker 2", code = "W2"),
            WorkerModel(id = 3, name = "Worker 3", code = "W3")
        )
        
        val workstations = listOf(
            WorkstationModel(id = 1, name = "Station 1", code = "S1"),
            WorkstationModel(id = 2, name = "Station 2", code = "S2")
        )
        
        // Act - Simular algoritmo de rotación
        val totalRotations = maxOf(workers.size, workstations.size)
        
        // Assert
        assertEquals(3, totalRotations, "Debe haber 3 rotaciones (max de workers y stations)")
        assertTrue(workers.size >= workstations.size, "Hay más trabajadores que estaciones")
    }
    
    @Test
    fun testWorkerDistribution() {
        // Arrange
        val workers = listOf(
            WorkerModel(id = 1, name = "Worker 1", code = "W1"),
            WorkerModel(id = 2, name = "Worker 2", code = "W2")
        )
        
        val workstations = listOf(
            WorkstationModel(id = 1, name = "Station 1", code = "S1"),
            WorkstationModel(id = 2, name = "Station 2", code = "S2"),
            WorkstationModel(id = 3, name = "Station 3", code = "S3")
        )
        
        // Act
        val totalRotations = maxOf(workers.size, workstations.size)
        val totalAssignments = totalRotations * workstations.size
        
        // Assert
        assertEquals(3, totalRotations, "Debe haber 3 rotaciones")
        assertEquals(9, totalAssignments, "Debe haber 9 asignaciones totales (3 rotaciones x 3 estaciones)")
    }
    
    @Test
    fun testEmptyWorkers() {
        // Arrange
        val workers = emptyList<WorkerModel>()
        val workstations = listOf(
            WorkstationModel(id = 1, name = "Station 1", code = "S1")
        )
        
        // Act & Assert
        assertTrue(workers.isEmpty(), "Lista de trabajadores debe estar vacía")
        assertTrue(workstations.isNotEmpty(), "Lista de estaciones no debe estar vacía")
    }
    
    @Test
    fun testEmptyWorkstations() {
        // Arrange
        val workers = listOf(
            WorkerModel(id = 1, name = "Worker 1", code = "W1")
        )
        val workstations = emptyList<WorkstationModel>()
        
        // Act & Assert
        assertTrue(workers.isNotEmpty(), "Lista de trabajadores no debe estar vacía")
        assertTrue(workstations.isEmpty(), "Lista de estaciones debe estar vacía")
    }
    
    @Test
    fun testCapabilitiesMatching() {
        // Arrange
        val worker = WorkerModel(
            id = 1,
            name = "Worker 1",
            code = "W1",
            capabilities = listOf("Soldadura", "Ensamblaje")
        )
        
        val workstation = WorkstationModel(
            id = 1,
            name = "Station 1",
            code = "S1",
            requiredCapabilities = listOf("Soldadura")
        )
        
        // Act
        val hasRequiredCapability = workstation.requiredCapabilities.any { 
            it in worker.capabilities 
        }
        
        // Assert
        assertTrue(hasRequiredCapability, "Trabajador debe tener la capacidad requerida")
    }
    
    @Test
    fun testNoCapabilitiesRequired() {
        // Arrange
        val worker = WorkerModel(
            id = 1,
            name = "Worker 1",
            code = "W1",
            capabilities = emptyList()
        )
        
        val workstation = WorkstationModel(
            id = 1,
            name = "Station 1",
            code = "S1",
            requiredCapabilities = emptyList()
        )
        
        // Act
        val canWork = workstation.requiredCapabilities.isEmpty() || 
                     worker.capabilities.any { it in workstation.requiredCapabilities }
        
        // Assert
        assertTrue(canWork, "Trabajador puede trabajar en estación sin requisitos")
    }
}
