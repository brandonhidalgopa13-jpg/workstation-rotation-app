package com.workstation.rotation.utils

import org.junit.Test
import org.junit.Assert.*

/**
 * Tests unitarios para ValidationUtils
 * Verifica que las validaciones funcionen correctamente
 */
class ValidationUtilsTest {

    @Test
    fun `validateWorkerName should return true for valid names`() {
        // Arrange & Act & Assert
        assertTrue(ValidationUtils.validateWorkerName("Juan Pérez"))
        assertTrue(ValidationUtils.validateWorkerName("María José"))
        assertTrue(ValidationUtils.validateWorkerName("Carlos"))
        assertTrue(ValidationUtils.validateWorkerName("Ana-Sofía"))
    }

    @Test
    fun `validateWorkerName should return false for invalid names`() {
        // Arrange & Act & Assert
        assertFalse(ValidationUtils.validateWorkerName(""))
        assertFalse(ValidationUtils.validateWorkerName("   "))
        assertFalse(ValidationUtils.validateWorkerName("A"))
        assertFalse(ValidationUtils.validateWorkerName("123"))
        assertFalse(ValidationUtils.validateWorkerName("Juan123"))
    }

    @Test
    fun `validateWorkstationName should return true for valid names`() {
        // Arrange & Act & Assert
        assertTrue(ValidationUtils.validateWorkstationName("Estación 1"))
        assertTrue(ValidationUtils.validateWorkstationName("Línea A"))
        assertTrue(ValidationUtils.validateWorkstationName("Empaque"))
        assertTrue(ValidationUtils.validateWorkstationName("Control-Calidad"))
    }

    @Test
    fun `validateWorkstationName should return false for invalid names`() {
        // Arrange & Act & Assert
        assertFalse(ValidationUtils.validateWorkstationName(""))
        assertFalse(ValidationUtils.validateWorkstationName("   "))
        assertFalse(ValidationUtils.validateWorkstationName("A"))
    }

    @Test
    fun `validateCapabilities should return true for valid capabilities`() {
        // Arrange & Act & Assert
        assertTrue(ValidationUtils.validateCapabilities("Soldadura, Ensamble"))
        assertTrue(ValidationUtils.validateCapabilities("Control de Calidad"))
        assertTrue(ValidationUtils.validateCapabilities(""))
        assertTrue(ValidationUtils.validateCapabilities("   "))
    }

    @Test
    fun `validateRestrictions should return true for valid restrictions`() {
        // Arrange & Act & Assert
        assertTrue(ValidationUtils.validateRestrictions("No levantar peso"))
        assertTrue(ValidationUtils.validateRestrictions("Solo turno diurno"))
        assertTrue(ValidationUtils.validateRestrictions(""))
        assertTrue(ValidationUtils.validateRestrictions("   "))
    }

    @Test
    fun `isValidEmail should return true for valid emails`() {
        // Arrange & Act & Assert
        assertTrue(ValidationUtils.isValidEmail("test@example.com"))
        assertTrue(ValidationUtils.isValidEmail("user.name@domain.co"))
        assertTrue(ValidationUtils.isValidEmail("test123@test-domain.org"))
    }

    @Test
    fun `isValidEmail should return false for invalid emails`() {
        // Arrange & Act & Assert
        assertFalse(ValidationUtils.isValidEmail(""))
        assertFalse(ValidationUtils.isValidEmail("invalid"))
        assertFalse(ValidationUtils.isValidEmail("@domain.com"))
        assertFalse(ValidationUtils.isValidEmail("test@"))
        assertFalse(ValidationUtils.isValidEmail("test.domain.com"))
    }

    @Test
    fun `sanitizeInput should clean dangerous characters`() {
        // Arrange & Act & Assert
        assertEquals("Test Name", ValidationUtils.sanitizeInput("Test Name"))
        assertEquals("Test-Name", ValidationUtils.sanitizeInput("Test-Name"))
        assertEquals("Test Name", ValidationUtils.sanitizeInput("Test<script>Name"))
        assertEquals("Test Name", ValidationUtils.sanitizeInput("Test&lt;Name"))
        assertEquals("Test Name", ValidationUtils.sanitizeInput("  Test Name  "))
    }

    @Test
    fun `isValidBackupData should validate backup structure`() {
        // Arrange
        val validBackup = """
            {
                "version": "2.1.0",
                "timestamp": "2024-10-26T10:00:00Z",
                "workers": [],
                "workstations": []
            }
        """.trimIndent()

        val invalidBackup = """
            {
                "invalid": "data"
            }
        """.trimIndent()

        // Act & Assert
        assertTrue(ValidationUtils.isValidBackupData(validBackup))
        assertFalse(ValidationUtils.isValidBackupData(invalidBackup))
        assertFalse(ValidationUtils.isValidBackupData(""))
        assertFalse(ValidationUtils.isValidBackupData("invalid json"))
    }
}