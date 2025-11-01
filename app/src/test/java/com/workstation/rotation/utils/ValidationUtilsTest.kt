package com.workstation.rotation.utils

import org.junit.Test
import org.junit.Assert.*

/**
 * Tests unitarios para ValidationUtils
 * Verifica que las validaciones funcionen correctamente
 */
class ValidationUtilsTest {

    @Test
    fun `validateWorkerName should return ValidationResult for valid names`() {
        // Arrange & Act & Assert
        val result1 = ValidationUtils.validateWorkerName("Juan Pérez")
        val result2 = ValidationUtils.validateWorkerName("María José")
        val result3 = ValidationUtils.validateWorkerName("Carlos")
        val result4 = ValidationUtils.validateWorkerName("Ana-Sofía")
        
        assertTrue(result1.isValid)
        assertTrue(result2.isValid)
        assertTrue(result3.isValid)
        assertTrue(result4.isValid)
    }

    @Test
    fun `validateWorkerName should return ValidationResult for invalid names`() {
        // Arrange & Act & Assert
        val result1 = ValidationUtils.validateWorkerName("")
        val result2 = ValidationUtils.validateWorkerName("   ")
        val result3 = ValidationUtils.validateWorkerName("A")
        val result4 = ValidationUtils.validateWorkerName("Juan123")
        
        assertFalse(result1.isValid)
        assertFalse(result2.isValid)
        assertFalse(result3.isValid)
        assertFalse(result4.isValid)
        assertNotNull(result1.errorMessage)
    }

    @Test
    fun `validateWorkstationName should return ValidationResult for valid names`() {
        // Arrange & Act & Assert
        val result1 = ValidationUtils.validateWorkstationName("Estación 1")
        val result2 = ValidationUtils.validateWorkstationName("Línea A")
        val result3 = ValidationUtils.validateWorkstationName("Empaque")
        val result4 = ValidationUtils.validateWorkstationName("Control-Calidad")
        
        assertTrue(result1.isValid)
        assertTrue(result2.isValid)
        assertTrue(result3.isValid)
        assertTrue(result4.isValid)
    }

    @Test
    fun `validateWorkstationName should return ValidationResult for invalid names`() {
        // Arrange & Act & Assert
        val result1 = ValidationUtils.validateWorkstationName("")
        val result2 = ValidationUtils.validateWorkstationName("   ")
        val result3 = ValidationUtils.validateWorkstationName("A")
        
        assertFalse(result1.isValid)
        assertFalse(result2.isValid)
        assertFalse(result3.isValid)
        assertNotNull(result1.errorMessage)
    }

    @Test
    fun `validateEmail should return ValidationResult for valid emails`() {
        // Arrange & Act & Assert
        val result1 = ValidationUtils.validateEmail("")
        val result2 = ValidationUtils.validateEmail("test@example.com")
        val result3 = ValidationUtils.validateEmail("user.name@domain.co")
        
        assertTrue(result1.isValid) // Empty email is valid (optional)
        assertTrue(result2.isValid)
        assertTrue(result3.isValid)
    }

    @Test
    fun `validateEmail should return ValidationResult for invalid emails`() {
        // Arrange & Act & Assert
        val result1 = ValidationUtils.validateEmail("invalid")
        val result2 = ValidationUtils.validateEmail("@domain.com")
        val result3 = ValidationUtils.validateEmail("test@")
        
        assertFalse(result1.isValid)
        assertFalse(result2.isValid)
        assertFalse(result3.isValid)
        assertNotNull(result1.errorMessage)
    }

    @Test
    fun `validateAvailabilityPercentage should return ValidationResult for valid percentages`() {
        // Arrange & Act & Assert
        val result1 = ValidationUtils.validateAvailabilityPercentage(0)
        val result2 = ValidationUtils.validateAvailabilityPercentage(50)
        val result3 = ValidationUtils.validateAvailabilityPercentage(100)
        
        assertTrue(result1.isValid)
        assertTrue(result2.isValid)
        assertTrue(result3.isValid)
    }

    @Test
    fun `validateAvailabilityPercentage should return ValidationResult for invalid percentages`() {
        // Arrange & Act & Assert
        val result1 = ValidationUtils.validateAvailabilityPercentage(-1)
        val result2 = ValidationUtils.validateAvailabilityPercentage(101)
        
        assertFalse(result1.isValid)
        assertFalse(result2.isValid)
        assertNotNull(result1.errorMessage)
        assertNotNull(result2.errorMessage)
    }

    @Test
    fun `validateRequiredWorkers should return ValidationResult for valid counts`() {
        // Arrange & Act & Assert
        val result1 = ValidationUtils.validateRequiredWorkers(1)
        val result2 = ValidationUtils.validateRequiredWorkers(5)
        val result3 = ValidationUtils.validateRequiredWorkers(100)
        
        assertTrue(result1.isValid)
        assertTrue(result2.isValid)
        assertTrue(result3.isValid)
    }

    @Test
    fun `validateRequiredWorkers should return ValidationResult for invalid counts`() {
        // Arrange & Act & Assert
        val result1 = ValidationUtils.validateRequiredWorkers(0)
        val result2 = ValidationUtils.validateRequiredWorkers(101)
        
        assertFalse(result1.isValid)
        assertFalse(result2.isValid)
        assertNotNull(result1.errorMessage)
        assertNotNull(result2.errorMessage)
    }
}