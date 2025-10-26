package com.workstation.rotation.tutorial

import android.content.Context
import android.content.SharedPreferences

/**
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 📊 PROGRESO DEL TUTORIAL - SEGUIMIENTO DE AVANCE
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 
 * Rastrea el progreso del usuario a través del tutorial interactivo.
 * Permite reanudar desde donde se quedó y mostrar estadísticas de completitud.
 */
class TutorialProgress(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    companion object {
        private const val PREFS_NAME = "tutorial_progress"
        private const val KEY_CURRENT_STEP = "current_step"
        private const val KEY_STEPS_COMPLETED = "steps_completed"
        private const val KEY_WORKSTATIONS_CREATED = "workstations_created"
        private const val KEY_WORKERS_CREATED = "workers_created"
        private const val KEY_ROTATIONS_GENERATED = "rotations_generated"
        private const val KEY_TUTORIAL_START_TIME = "tutorial_start_time"
    }
    
    /**
     * Obtiene el paso actual del tutorial.
     */
    fun getCurrentStep(): TutorialStep? {
        val stepName = prefs.getString(KEY_CURRENT_STEP, null)
        return stepName?.let { 
            try {
                TutorialStep.valueOf(it)
            } catch (e: IllegalArgumentException) {
                null
            }
        }
    }
    
    /**
     * Establece el paso actual del tutorial.
     */
    fun setCurrentStep(step: TutorialStep) {
        prefs.edit().putString(KEY_CURRENT_STEP, step.name).apply()
    }
    
    /**
     * Marca un paso como completado.
     */
    fun markStepCompleted(step: TutorialStep) {
        val completedSteps = getCompletedSteps().toMutableSet()
        completedSteps.add(step.name)
        prefs.edit().putStringSet(KEY_STEPS_COMPLETED, completedSteps).apply()
    }
    
    /**
     * Verifica si un paso está completado.
     */
    fun isStepCompleted(step: TutorialStep): Boolean {
        return getCompletedSteps().contains(step.name)
    }
    
    /**
     * Obtiene todos los pasos completados.
     */
    private fun getCompletedSteps(): Set<String> {
        return prefs.getStringSet(KEY_STEPS_COMPLETED, emptySet()) ?: emptySet()
    }
    
    /**
     * Obtiene el porcentaje de completitud del tutorial.
     */
    fun getCompletionPercentage(): Int {
        val totalSteps = TutorialStep.values().size
        val completedSteps = getCompletedSteps().size
        return (completedSteps * 100) / totalSteps
    }
    
    /**
     * Registra la creación de una estación durante el tutorial.
     */
    fun recordWorkstationCreated() {
        val count = prefs.getInt(KEY_WORKSTATIONS_CREATED, 0)
        prefs.edit().putInt(KEY_WORKSTATIONS_CREATED, count + 1).apply()
    }
    
    /**
     * Registra la creación de un trabajador durante el tutorial.
     */
    fun recordWorkerCreated() {
        val count = prefs.getInt(KEY_WORKERS_CREATED, 0)
        prefs.edit().putInt(KEY_WORKERS_CREATED, count + 1).apply()
    }
    
    /**
     * Registra la generación de una rotación durante el tutorial.
     */
    fun recordRotationGenerated() {
        val count = prefs.getInt(KEY_ROTATIONS_GENERATED, 0)
        prefs.edit().putInt(KEY_ROTATIONS_GENERATED, count + 1).apply()
    }
    
    /**
     * Obtiene estadísticas del progreso del tutorial.
     */
    fun getTutorialStats(): TutorialStats {
        return TutorialStats(
            completionPercentage = getCompletionPercentage(),
            workstationsCreated = prefs.getInt(KEY_WORKSTATIONS_CREATED, 0),
            workersCreated = prefs.getInt(KEY_WORKERS_CREATED, 0),
            rotationsGenerated = prefs.getInt(KEY_ROTATIONS_GENERATED, 0),
            currentStep = getCurrentStep(),
            stepsCompleted = getCompletedSteps().size,
            totalSteps = TutorialStep.values().size
        )
    }
    
    /**
     * Inicia el seguimiento del tutorial.
     */
    fun startTutorial() {
        prefs.edit().putLong(KEY_TUTORIAL_START_TIME, System.currentTimeMillis()).apply()
        setCurrentStep(TutorialStep.WELCOME)
    }
    
    /**
     * Reinicia el progreso del tutorial.
     */
    fun resetProgress() {
        prefs.edit().clear().apply()
    }
    
    /**
     * Verifica si el usuario ha creado suficientes elementos para continuar.
     */
    fun hasMinimumRequirements(): Boolean {
        return prefs.getInt(KEY_WORKSTATIONS_CREATED, 0) >= 2 &&
               prefs.getInt(KEY_WORKERS_CREATED, 0) >= 3
    }
    
    /**
     * Obtiene recomendaciones basadas en el progreso actual.
     */
    fun getRecommendations(): List<String> {
        val recommendations = mutableListOf<String>()
        
        val workstations = prefs.getInt(KEY_WORKSTATIONS_CREATED, 0)
        val workers = prefs.getInt(KEY_WORKERS_CREATED, 0)
        val rotations = prefs.getInt(KEY_ROTATIONS_GENERATED, 0)
        
        when {
            workstations < 3 -> recommendations.add("Crea al menos 3 estaciones para tener variedad")
            workers < 5 -> recommendations.add("Agrega más trabajadores para rotaciones efectivas")
            rotations == 0 -> recommendations.add("Genera tu primera rotación para ver el sistema en acción")
            else -> recommendations.add("¡Excelente progreso! Continúa explorando las funcionalidades")
        }
        
        return recommendations
    }
    
    /**
     * Clase de datos para estadísticas del tutorial.
     */
    data class TutorialStats(
        val completionPercentage: Int,
        val workstationsCreated: Int,
        val workersCreated: Int,
        val rotationsGenerated: Int,
        val currentStep: TutorialStep?,
        val stepsCompleted: Int,
        val totalSteps: Int
    ) {
        fun getProgressText(): String {
            return "Progreso: $stepsCompleted/$totalSteps pasos ($completionPercentage%)"
        }
        
        fun getActivitySummary(): String {
            return "Creaste: $workstationsCreated estaciones, $workersCreated trabajadores, $rotationsGenerated rotaciones"
        }
    }
}