package com.workstation.rotation.utils

/**
 * Utility class for input validation across the application.
 */
object ValidationUtils {
    
    /**
     * Validates worker name input.
     * @param name The name to validate
     * @return ValidationResult with success status and error message if any
     */
    fun validateWorkerName(name: String): ValidationResult {
        return when {
            name.isBlank() -> ValidationResult(false, "El nombre es requerido")
            name.length < Constants.MIN_NAME_LENGTH -> 
                ValidationResult(false, "El nombre debe tener al menos ${Constants.MIN_NAME_LENGTH} caracteres")
            name.length > Constants.MAX_NAME_LENGTH -> 
                ValidationResult(false, "El nombre no puede exceder ${Constants.MAX_NAME_LENGTH} caracteres")
            !name.matches(Regex("^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$")) -> 
                ValidationResult(false, "El nombre solo puede contener letras y espacios")
            else -> ValidationResult(true)
        }
    }
    
    /**
     * Validates workstation name input.
     * @param name The name to validate
     * @return ValidationResult with success status and error message if any
     */
    fun validateWorkstationName(name: String): ValidationResult {
        return when {
            name.isBlank() -> ValidationResult(false, "El nombre de la estación es requerido")
            name.length < Constants.MIN_NAME_LENGTH -> 
                ValidationResult(false, "El nombre debe tener al menos ${Constants.MIN_NAME_LENGTH} caracteres")
            name.length > Constants.MAX_NAME_LENGTH -> 
                ValidationResult(false, "El nombre no puede exceder ${Constants.MAX_NAME_LENGTH} caracteres")
            else -> ValidationResult(true)
        }
    }
    
    /**
     * Validates email format.
     * @param email The email to validate
     * @return ValidationResult with success status and error message if any
     */
    fun validateEmail(email: String): ValidationResult {
        if (email.isBlank()) return ValidationResult(true) // Email is optional
        
        val emailPattern = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
        return if (email.matches(emailPattern)) {
            ValidationResult(true)
        } else {
            ValidationResult(false, "Formato de email inválido")
        }
    }
    
    /**
     * Validates availability percentage.
     * @param percentage The percentage to validate
     * @return ValidationResult with success status and error message if any
     */
    fun validateAvailabilityPercentage(percentage: Int): ValidationResult {
        return when {
            percentage < Constants.MIN_AVAILABILITY_PERCENTAGE -> 
                ValidationResult(false, "La disponibilidad no puede ser menor a ${Constants.MIN_AVAILABILITY_PERCENTAGE}%")
            percentage > Constants.MAX_AVAILABILITY_PERCENTAGE -> 
                ValidationResult(false, "La disponibilidad no puede ser mayor a ${Constants.MAX_AVAILABILITY_PERCENTAGE}%")
            else -> ValidationResult(true)
        }
    }
    
    /**
     * Validates required workers count.
     * @param count The count to validate
     * @return ValidationResult with success status and error message if any
     */
    fun validateRequiredWorkers(count: Int): ValidationResult {
        return when {
            count < Constants.MIN_REQUIRED_WORKERS -> 
                ValidationResult(false, "Debe requerir al menos ${Constants.MIN_REQUIRED_WORKERS} trabajador")
            count > Constants.MAX_REQUIRED_WORKERS -> 
                ValidationResult(false, "No puede requerir más de ${Constants.MAX_REQUIRED_WORKERS} trabajadores")
            else -> ValidationResult(true)
        }
    }
}

/**
 * Data class representing the result of a validation operation.
 */
data class ValidationResult(
    val isValid: Boolean,
    val errorMessage: String? = null
)