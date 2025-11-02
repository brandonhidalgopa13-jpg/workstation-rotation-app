package com.workstation.rotation.animations

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import android.view.View
import com.workstation.rotation.R

/**
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 🎬 TRANSICIONES ENTRE ACTIVIDADES - REWS v3.1.0
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 
 * Sistema de transiciones fluidas entre actividades para mejorar la experiencia de navegación.
 * Incluye transiciones predefinidas y personalizables para diferentes tipos de navegación.
 * 
 * 🎯 TIPOS DE TRANSICIONES:
 * • Slide transitions (deslizamiento)
 * • Fade transitions (desvanecimiento)
 * • Scale transitions (escalado)
 * • Shared element transitions (elementos compartidos)
 * • Custom transitions (personalizadas)
 * 
 * 🚀 CARACTERÍSTICAS:
 * • Transiciones suaves y naturales
 * • Configuración simple con extension functions
 * • Soporte para shared elements
 * • Transiciones contextuales según el flujo de navegación
 * • Optimización para diferentes versiones de Android
 * 
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 */

object ActivityTransitions {
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // 🎬 TRANSICIONES PREDEFINIDAS
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    /**
     * Transición de slide desde la derecha (navegación hacia adelante)
     */
    fun slideFromRight(activity: Activity) {
        activity.overridePendingTransition(
            R.anim.slide_in_right,
            R.anim.slide_out_left
        )
    }
    
    /**
     * Transición de slide hacia la derecha (navegación hacia atrás)
     */
    fun slideToRight(activity: Activity) {
        activity.overridePendingTransition(
            R.anim.slide_in_left,
            R.anim.slide_out_right
        )
    }
    
    /**
     * Transición de slide desde abajo (modal/dialog)
     */
    fun slideFromBottom(activity: Activity) {
        activity.overridePendingTransition(
            R.anim.slide_in_bottom,
            R.anim.fade_out
        )
    }
    
    /**
     * Transición de slide hacia abajo (cerrar modal)
     */
    fun slideToBottom(activity: Activity) {
        activity.overridePendingTransition(
            R.anim.fade_in,
            R.anim.slide_out_bottom
        )
    }
    
    /**
     * Transición de fade suave
     */
    fun fade(activity: Activity) {
        activity.overridePendingTransition(
            R.anim.fade_in,
            R.anim.fade_out
        )
    }
    
    /**
     * Transición de scale (zoom)
     */
    fun scale(activity: Activity) {
        activity.overridePendingTransition(
            R.anim.scale_in,
            R.anim.scale_out
        )
    }
    
    /**
     * Sin transición (instantáneo)
     */
    fun none(activity: Activity) {
        activity.overridePendingTransition(0, 0)
    }
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // 🎭 TRANSICIONES CONTEXTUALES
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    /**
     * Transición para navegación principal (entre secciones principales)
     */
    fun mainNavigation(activity: Activity) = slideFromRight(activity)
    
    /**
     * Transición para navegación hacia atrás
     */
    fun backNavigation(activity: Activity) = slideToRight(activity)
    
    /**
     * Transición para abrir configuraciones
     */
    fun openSettings(activity: Activity) = slideFromBottom(activity)
    
    /**
     * Transición para cerrar configuraciones
     */
    fun closeSettings(activity: Activity) = slideToBottom(activity)
    
    /**
     * Transición para abrir detalles
     */
    fun openDetails(activity: Activity) = scale(activity)
    
    /**
     * Transición para cerrar detalles
     */
    fun closeDetails(activity: Activity) = fade(activity)
    
    /**
     * Transición para navegación rápida
     */
    fun quickNavigation(activity: Activity) = fade(activity)
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // 🔗 SHARED ELEMENT TRANSITIONS
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    /**
     * Crea opciones para shared element transition
     */
    fun createSharedElementOptions(
        activity: Activity,
        vararg sharedElements: Pair<View, String>
    ): ActivityOptionsCompat {
        return ActivityOptionsCompat.makeSceneTransitionAnimation(
            activity,
            *sharedElements
        )
    }
    
    /**
     * Shared element transition para un solo elemento
     */
    fun createSharedElementOptions(
        activity: Activity,
        sharedElement: View,
        transitionName: String
    ): ActivityOptionsCompat {
        return ActivityOptionsCompat.makeSceneTransitionAnimation(
            activity,
            sharedElement,
            transitionName
        )
    }
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // 🎪 TRANSICIONES ESPECIALES
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    /**
     * Transición circular reveal (para elementos específicos)
     */
    fun circularReveal(activity: Activity) {
        activity.overridePendingTransition(
            R.anim.circular_reveal,
            R.anim.fade_out
        )
    }
    
    /**
     * Transición de flip horizontal
     */
    fun flipHorizontal(activity: Activity) {
        activity.overridePendingTransition(
            R.anim.flip_in_horizontal,
            R.anim.flip_out_horizontal
        )
    }
    
    /**
     * Transición de flip vertical
     */
    fun flipVertical(activity: Activity) {
        activity.overridePendingTransition(
            R.anim.flip_in_vertical,
            R.anim.flip_out_vertical
        )
    }
    
    /**
     * Transición de rotación
     */
    fun rotate(activity: Activity) {
        activity.overridePendingTransition(
            R.anim.rotate_in,
            R.anim.rotate_out
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════════════════════════
// 🔧 EXTENSION FUNCTIONS PARA ACTIVITY
// ═══════════════════════════════════════════════════════════════════════════════════════════════

/**
 * Inicia una actividad con transición de slide desde la derecha
 */
fun Activity.startActivityWithSlideFromRight(intent: Intent) {
    startActivity(intent)
    ActivityTransitions.slideFromRight(this)
}

/**
 * Inicia una actividad con transición de fade
 */
fun Activity.startActivityWithFade(intent: Intent) {
    startActivity(intent)
    ActivityTransitions.fade(this)
}

/**
 * Inicia una actividad con transición de scale
 */
fun Activity.startActivityWithScale(intent: Intent) {
    startActivity(intent)
    ActivityTransitions.scale(this)
}

/**
 * Inicia una actividad con transición desde abajo
 */
fun Activity.startActivityWithSlideFromBottom(intent: Intent) {
    startActivity(intent)
    ActivityTransitions.slideFromBottom(this)
}

/**
 * Inicia una actividad con shared element
 */
fun Activity.startActivityWithSharedElement(
    intent: Intent,
    sharedElement: View,
    transitionName: String
) {
    val options = ActivityTransitions.createSharedElementOptions(this, sharedElement, transitionName)
    startActivity(intent, options.toBundle())
}

/**
 * Inicia una actividad con múltiples shared elements
 */
fun Activity.startActivityWithSharedElements(
    intent: Intent,
    vararg sharedElements: Pair<View, String>
) {
    val options = ActivityTransitions.createSharedElementOptions(this, *sharedElements)
    startActivity(intent, options.toBundle())
}

/**
 * Finaliza la actividad con transición hacia la derecha
 */
fun Activity.finishWithSlideToRight() {
    finish()
    ActivityTransitions.slideToRight(this)
}

/**
 * Finaliza la actividad con transición hacia abajo
 */
fun Activity.finishWithSlideToBottom() {
    finish()
    ActivityTransitions.slideToBottom(this)
}

/**
 * Finaliza la actividad con fade
 */
fun Activity.finishWithFade() {
    finish()
    ActivityTransitions.fade(this)
}

/**
 * Finaliza la actividad con scale
 */
fun Activity.finishWithScale() {
    finish()
    ActivityTransitions.scale(this)
}

// ═══════════════════════════════════════════════════════════════════════════════════════════════
// 🎯 EXTENSION FUNCTIONS PARA CONTEXTOS ESPECÍFICOS
// ═══════════════════════════════════════════════════════════════════════════════════════════════

/**
 * Navegación principal entre secciones
 */
fun Activity.navigateToMainSection(intent: Intent) {
    startActivity(intent)
    ActivityTransitions.mainNavigation(this)
}

/**
 * Abrir configuraciones
 */
fun Activity.openSettings(intent: Intent) {
    startActivity(intent)
    ActivityTransitions.openSettings(this)
}

/**
 * Abrir detalles
 */
fun Activity.openDetails(intent: Intent) {
    startActivity(intent)
    ActivityTransitions.openDetails(this)
}

/**
 * Navegación rápida
 */
fun Activity.quickNavigate(intent: Intent) {
    startActivity(intent)
    ActivityTransitions.quickNavigation(this)
}

/**
 * Cerrar con navegación hacia atrás
 */
fun Activity.closeWithBackNavigation() {
    finish()
    ActivityTransitions.backNavigation(this)
}

/**
 * Cerrar configuraciones
 */
fun Activity.closeSettings() {
    finish()
    ActivityTransitions.closeSettings(this)
}

/**
 * Cerrar detalles
 */
fun Activity.closeDetails() {
    finish()
    ActivityTransitions.closeDetails(this)
}