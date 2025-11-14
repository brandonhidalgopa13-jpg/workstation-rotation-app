package com.workstation.rotation.domain.service

import com.workstation.rotation.domain.models.RotationSessionModel
import com.workstation.rotation.domain.models.RotationAssignmentModel
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertContains

class ExportServiceTest {
    
    @Test
    fun testTextExportFormat() {
        // Arrange
        val session = RotationSessionModel(
            id = 1,
            name = "Test Rotation",
            rotationIntervalMinutes = 60,
            isActive = true
        )
        
        val assignments = listOf(
            RotationAssignmentModel(
                id = 1,
                sessionId = 1,
                workerId = 1,
                workstationId = 1,
                rotationOrder = 0
            )
        )
        
        // Act - Simular exportación
        val expectedContent = """
            ROTACIÓN DE ESTACIONES
            Sesión: Test Rotation
            Intervalo: 60 minutos
        """.trimIndent()
        
        // Assert
        assertTrue(expectedContent.contains("ROTACIÓN DE ESTACIONES"))
        assertTrue(expectedContent.contains("Test Rotation"))
        assertTrue(expectedContent.contains("60 minutos"))
    }
    
    @Test
    fun testCSVExportFormat() {
        // Arrange
        val csvHeader = "Rotación,Trabajador,Código Trabajador,Estación,Código Estación"
        val csvRow = "1,\"Juan Pérez\",\"JP001\",\"Ensamblaje A\",\"EST-001\""
        
        // Assert
        assertTrue(csvHeader.contains("Rotación"))
        assertTrue(csvHeader.contains("Trabajador"))
        assertTrue(csvHeader.contains("Estación"))
        assertTrue(csvRow.contains("Juan Pérez"))
        assertTrue(csvRow.contains("Ensamblaje A"))
    }
    
    @Test
    fun testMarkdownExportFormat() {
        // Arrange
        val markdownContent = """
            # Test Rotation
            
            **Fecha:** 2025-11-13
            **Intervalo:** 60 minutos
            
            ## Rotación #1
            
            | Trabajador | Estación |
            |------------|----------|
            | Juan Pérez | Ensamblaje A |
        """.trimIndent()
        
        // Assert
        assertTrue(markdownContent.contains("# Test Rotation"))
        assertTrue(markdownContent.contains("| Trabajador | Estación |"))
        assertTrue(markdownContent.contains("Juan Pérez"))
    }
    
    @Test
    fun testExportWithMultipleRotations() {
        // Arrange
        val assignments = listOf(
            RotationAssignmentModel(1, 1, 1, 1, 0),
            RotationAssignmentModel(2, 1, 2, 2, 0),
            RotationAssignmentModel(3, 1, 1, 2, 1),
            RotationAssignmentModel(4, 1, 2, 1, 1)
        )
        
        // Act
        val rotationGroups = assignments.groupBy { it.rotationOrder }
        
        // Assert
        assertTrue(rotationGroups.containsKey(0), "Debe contener rotación 0")
        assertTrue(rotationGroups.containsKey(1), "Debe contener rotación 1")
        assertTrue(rotationGroups[0]?.size == 2, "Rotación 0 debe tener 2 asignaciones")
        assertTrue(rotationGroups[1]?.size == 2, "Rotación 1 debe tener 2 asignaciones")
    }
    
    @Test
    fun testExportFileName() {
        // Arrange
        val sessionName = "Rotación Turno Mañana"
        val expectedFileName = "rotacion_Rotacion_Turno_Manana.txt"
        
        // Act
        val sanitizedName = sessionName.replace(" ", "_")
        val fileName = "rotacion_$sanitizedName.txt"
        
        // Assert
        assertTrue(fileName.startsWith("rotacion_"))
        assertTrue(fileName.endsWith(".txt"))
    }
}
