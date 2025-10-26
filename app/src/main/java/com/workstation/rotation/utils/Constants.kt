package com.workstation.rotation.utils

/**
 * Application-wide constants for the Workstation Rotation app.
 */
object Constants {
    
    // Database
    const val DATABASE_NAME = "workstation_rotation_database"
    const val DATABASE_VERSION = 6
    
    // Worker constraints
    const val MIN_AVAILABILITY_PERCENTAGE = 0
    const val MAX_AVAILABILITY_PERCENTAGE = 100
    const val DEFAULT_AVAILABILITY_PERCENTAGE = 100
    
    // Workstation constraints
    const val MIN_REQUIRED_WORKERS = 1
    const val MAX_REQUIRED_WORKERS = 100  // Aumentado de 50 a 100
    const val DEFAULT_REQUIRED_WORKERS = 1
    
    // System limits (increased for better scalability)
    const val MAX_WORKSTATIONS = 200      // Máximo de estaciones de trabajo
    const val MAX_WORKERS = 500           // Máximo de trabajadores
    const val MAX_ROTATION_ITEMS = 1000   // Máximo de elementos en rotación
    
    // Validation
    const val MIN_NAME_LENGTH = 2         // Reducido de 3 a 2 para más flexibilidad
    const val MAX_NAME_LENGTH = 100       // Aumentado de 50 a 100 para nombres largos
    
    // UI
    const val ANIMATION_DURATION_SHORT = 200L
    const val ANIMATION_DURATION_MEDIUM = 300L
    
    // RecyclerView optimization
    const val RECYCLER_VIEW_CACHE_SIZE = 20        // Tamaño de caché para mejor rendimiento
    const val MAX_RECYCLER_HEIGHT_DP = 300         // Altura máxima para RecyclerViews en diálogos
    const val ITEM_ANIMATION_DURATION = 250L       // Duración de animaciones de elementos
    
    // Rotation
    const val ROTATION_VARIETY_FACTOR = 30
    
    // Training
    const val SPINNER_NO_SELECTION = 0
    
    // Colors (as hex strings for consistency)
    const val COLOR_HIGH_AVAILABILITY = "#FF018786"
    const val COLOR_MEDIUM_AVAILABILITY = "#FFFF9800"
    const val COLOR_LOW_AVAILABILITY = "#FFF44336"
}