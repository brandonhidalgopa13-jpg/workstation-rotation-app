package com.workstation.rotation.tutorial

import android.content.Context
import android.content.SharedPreferences
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.google.android.material.card.MaterialCardView
import com.workstation.rotation.R

/**
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 📚 GESTOR DE TUTORIAL INTERACTIVO
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 
 * Maneja la guía práctica interactiva del usuario para el funcionamiento de la aplicación.
 * Permite activar/desactivar el tutorial y guiar paso a paso al usuario.
 */
class TutorialManager(private val context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private var currentStep = 0
    private var isActive = false
    
    companion object {
        private const val PREFS_NAME = "tutorial_prefs"
        private const val KEY_TUTORIAL_COMPLETED = "tutorial_completed"
        private const val KEY_TUTORIAL_ENABLED = "tutorial_enabled"
        private const val KEY_SHOW_HINTS = "show_hints"
    }
    
    /**
     * Verifica si el tutorial debe mostrarse automáticamente.
     */
    fun shouldShowTutorial(): Boolean {
        return !isTutorialCompleted() && isTutorialEnabled()
    }
    
    /**
     * Verifica si el tutorial está habilitado.
     */
    fun isTutorialEnabled(): Boolean {
        return prefs.getBoolean(KEY_TUTORIAL_ENABLED, true)
    }
    
    /**
     * Verifica si el tutorial ya fue completado.
     */
    fun isTutorialCompleted(): Boolean {
        return prefs.getBoolean(KEY_TUTORIAL_COMPLETED, false)
    }
    
    /**
     * Verifica si se deben mostrar las pistas.
     */
    fun shouldShowHints(): Boolean {
        return prefs.getBoolean(KEY_SHOW_HINTS, true)
    }
    
    /**
     * Habilita o deshabilita el tutorial.
     */
    fun setTutorialEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_TUTORIAL_ENABLED, enabled).apply()
    }
    
    /**
     * Marca el tutorial como completado.
     */
    fun markTutorialCompleted() {
        prefs.edit().putBoolean(KEY_TUTORIAL_COMPLETED, true).apply()
    }
    
    /**
     * Habilita o deshabilita las pistas.
     */
    fun setShowHints(show: Boolean) {
        prefs.edit().putBoolean(KEY_SHOW_HINTS, show).apply()
    }
    
    /**
     * Reinicia el tutorial.
     */
    fun resetTutorial() {
        prefs.edit()
            .putBoolean(KEY_TUTORIAL_COMPLETED, false)
            .putBoolean(KEY_TUTORIAL_ENABLED, true)
            .putBoolean(KEY_SHOW_HINTS, true)
            .apply()
        currentStep = 0
    }
    
    /**
     * Inicia el tutorial interactivo.
     */
    fun startTutorial(onStepComplete: (TutorialStep) -> Unit, onNavigate: (TutorialStep) -> Unit = {}) {
        if (!isTutorialEnabled()) return
        
        isActive = true
        currentStep = 0
        showTutorialStep(TutorialStep.WELCOME, onStepComplete, onNavigate)
    }
    
    /**
     * Muestra un paso específico del tutorial.
     */
    private fun showTutorialStep(step: TutorialStep, onStepComplete: (TutorialStep) -> Unit, onNavigate: (TutorialStep) -> Unit) {
        val dialog = AlertDialog.Builder(context)
            .setTitle(step.title)
            .setMessage(step.description)
            .setPositiveButton(step.actionText) { _, _ ->
                onStepComplete(step)
                
                // Si el paso requiere navegación, ejecutar navegación
                if (step.requiresNavigation()) {
                    onNavigate(step)
                    // Continuar el tutorial después de la navegación
                    markStepCompleted(step)
                } else {
                    // Continuar inmediatamente al siguiente paso
                    val nextStep = step.getNextStep()
                    if (nextStep != null) {
                        showTutorialStep(nextStep, onStepComplete, onNavigate)
                    } else {
                        completeTutorial()
                    }
                }
            }
            .setNegativeButton("Saltar Tutorial") { _, _ ->
                skipTutorial()
            }
            .setNeutralButton("Pausar") { _, _ ->
                pauseTutorial()
            }
            .setCancelable(false)
            .create()
        
        dialog.show()
    }
    
    /**
     * Continúa el tutorial después de una navegación.
     */
    fun continueAfterNavigation(currentStep: TutorialStep, onStepComplete: (TutorialStep) -> Unit, onNavigate: (TutorialStep) -> Unit) {
        val nextStep = currentStep.getNextStep()
        if (nextStep != null && isActive) {
            // Pequeño delay para que la nueva actividad se cargue
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                showTutorialStep(nextStep, onStepComplete, onNavigate)
            }, 1000)
        } else {
            completeTutorial()
        }
    }
    
    /**
     * Marca un paso como completado.
     */
    private fun markStepCompleted(step: TutorialStep) {
        prefs.edit().putBoolean("step_${step.name}_completed", true).apply()
    }
    
    /**
     * Pausa el tutorial.
     */
    private fun pauseTutorial() {
        isActive = false
        AlertDialog.Builder(context)
            .setTitle("Tutorial Pausado")
            .setMessage("El tutorial ha sido pausado. Puedes continuarlo desde el menú principal.")
            .setPositiveButton("OK", null)
            .show()
    }
    
    /**
     * Completa el tutorial.
     */
    private fun completeTutorial() {
        isActive = false
        markTutorialCompleted()
        
        AlertDialog.Builder(context)
            .setTitle("🎉 ¡Tutorial Completado!")
            .setMessage("Has completado la guía interactiva. Ahora puedes usar la aplicación con confianza.\n\nPuedes reactivar el tutorial desde el menú de configuración en cualquier momento.")
            .setPositiveButton("Entendido", null)
            .show()
    }
    
    /**
     * Omite el tutorial.
     */
    private fun skipTutorial() {
        isActive = false
        markTutorialCompleted()
        
        AlertDialog.Builder(context)
            .setTitle("Tutorial Omitido")
            .setMessage("Puedes reactivar el tutorial desde el menú principal en cualquier momento.")
            .setPositiveButton("OK", null)
            .show()
    }
    
    /**
     * Muestra una pista contextual.
     */
    fun showHint(view: View, message: String) {
        if (!shouldShowHints() || !isActive) return
        
        // Crear tooltip o highlight visual
        showTooltip(view, message)
    }
    
    /**
     * Muestra un tooltip sobre una vista.
     */
    private fun showTooltip(view: View, message: String) {
        // Implementación simple de tooltip
        val tooltip = AlertDialog.Builder(context)
            .setMessage("💡 $message")
            .setPositiveButton("Entendido", null)
            .create()
        
        tooltip.show()
    }
    
    /**
     * Resalta una vista durante el tutorial.
     */
    fun highlightView(view: View, highlight: Boolean = true) {
        if (!isActive) return
        
        if (highlight) {
            view.elevation = 8f
            view.scaleX = 1.05f
            view.scaleY = 1.05f
            
            // Agregar borde si es una CardView
            if (view is MaterialCardView) {
                view.strokeWidth = 4
                view.strokeColor = androidx.core.content.ContextCompat.getColor(context, R.color.accent_orange)
            }
        } else {
            view.elevation = 0f
            view.scaleX = 1.0f
            view.scaleY = 1.0f
            
            if (view is MaterialCardView) {
                view.strokeWidth = 0
            }
        }
    }
    
    /**
     * Muestra el menú de configuración del tutorial.
     */
    fun showTutorialSettings() {
        val options = arrayOf(
            "Reiniciar Tutorial",
            "Activar/Desactivar Tutorial",
            "Activar/Desactivar Pistas",
            "Cancelar"
        )
        
        AlertDialog.Builder(context)
            .setTitle("⚙️ Configuración del Tutorial")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        resetTutorial()
                        AlertDialog.Builder(context)
                            .setTitle("Tutorial Reiniciado")
                            .setMessage("El tutorial se mostrará la próxima vez que abras la aplicación.")
                            .setPositiveButton("OK", null)
                            .show()
                    }
                    1 -> {
                        val newState = !isTutorialEnabled()
                        setTutorialEnabled(newState)
                        val message = if (newState) "Tutorial activado" else "Tutorial desactivado"
                        AlertDialog.Builder(context)
                            .setTitle("Configuración Actualizada")
                            .setMessage(message)
                            .setPositiveButton("OK", null)
                            .show()
                    }
                    2 -> {
                        val newState = !shouldShowHints()
                        setShowHints(newState)
                        val message = if (newState) "Pistas activadas" else "Pistas desactivadas"
                        AlertDialog.Builder(context)
                            .setTitle("Configuración Actualizada")
                            .setMessage(message)
                            .setPositiveButton("OK", null)
                            .show()
                    }
                }
            }
            .show()
    }
}