package com.workstation.rotation.utils

/**
 * Application-wide constants for the Workstation Rotation app.
 */
object Constants {
    
    // Database
    const val DATABASE_NAME = "workstation_rotation_database"
    const val DATABASE_VERSION = 5
    
    // Worker constraints
    const val MIN_AVAILABILITY_PERCENTAGE = 0
    const val MAX_AVAILABILITY_PERCENTAGE = 100
    const val DEFAULT_AVAILABILITY_PERCENTAGE = 100
    
    // Workstation constraints
    const val MIN_REQUIRED_WORKERS = 1
    const val MAX_REQUIRED_WORKERS = 50
    const val DEFAULT_REQUIRED_WORKERS = 1
    
    // Validation
    const val MIN_NAME_LENGTH = 3
    const val MAX_NAME_LENGTH = 50
    
    // UI
    const val ANIMATION_DURATION_SHORT = 200L
    const val ANIMATION_DURATION_MEDIUM = 300L
    
    // Rotation
    const val ROTATION_VARIETY_FACTOR = 30
    
    // Training
    const val SPINNER_NO_SELECTION = 0
    
    // Colors (as hex strings for consistency)
    const val COLOR_HIGH_AVAILABILITY = "#FF018786"
    const val COLOR_MEDIUM_AVAILABILITY = "#FFFF9800"
    const val COLOR_LOW_AVAILABILITY = "#FFF44336"
}