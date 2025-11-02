package com.workstation.rotation.animations

import android.view.MotionEvent
import android.view.View
import android.widget.Button
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.workstation.rotation.R

/**
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 🎮 MICRO-INTERACCIONES AVANZADAS - REWS v3.1.0
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 
 * Sistema de micro-interacciones que proporciona feedback visual inmediato
 * para mejorar la experiencia de usuario con animaciones sutiles y responsivas.
 * 
 * 🎯 TIPOS DE MICRO-INTERACCIONES:
 * • Press/Release feedback para botones
 * • Hover effects para elementos interactivos
 * • Loading states con animaciones
 * • Success/Error feedback visual
 * • Ripple effects personalizados
 * • State transitions suaves
 * 
 * 🚀 CARACTERÍSTICAS:
 * • Feedback inmediato y natural
 * • Animaciones optimizadas para performance
 * • Configuración automática según tipo de elemento
 * • Soporte para diferentes estados (normal, pressed, disabled)
 * • Integración perfecta con Material Design
 * 
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 */

object MicroInteractions {
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // 🎯 CONFIGURACIÓN DE MICRO-INTERACCIONES PARA BOTONES
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    /**
     * Configura micro-interacciones para MaterialButton
     */
    fun setupButtonInteractions(button: MaterialButton) {
        button.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // Feedback inmediato al presionar
                    view.animate()
                        .scaleX(0.95f)
                        .scaleY(0.95f)
                        .setDuration(100L)
                        .setInterpolator(AnimationManager.FAST_OUT_SLOW_IN)
                        .start()
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    // Restaurar al soltar
                    view.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(150L)
                        .setInterpolator(AnimationManager.OVERSHOOT_INTERPOLATOR)
                        .start()
                }
            }
            false // Permitir que el evento continúe
        }
    }
    
    /**
     * Configura micro-interacciones para FloatingActionButton
     */
    fun setupFabInteractions(fab: FloatingActionButton) {
        fab.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    view.animate()
                        .scaleX(0.9f)
                        .scaleY(0.9f)
                        .setDuration(100L)
                        .setInterpolator(AnimationManager.FAST_OUT_SLOW_IN)
                        .start()
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    view.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(200L)
                        .setInterpolator(AnimationManager.BOUNCE_INTERPOLATOR)
                        .start()
                }
            }
            false
        }
    }
    
    /**
     * Configura micro-interacciones para MaterialCardView
     */
    fun setupCardInteractions(card: MaterialCardView) {
        val originalElevation = card.cardElevation
        
        card.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    view.animate()
                        .scaleX(0.98f)
                        .scaleY(0.98f)
                        .setDuration(100L)
                        .setInterpolator(AnimationManager.FAST_OUT_SLOW_IN)
                        .start()
                    
                    // Aumentar elevación
                    card.animate()
                        .translationZ(originalElevation + 4f)
                        .setDuration(100L)
                        .start()
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    view.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(150L)
                        .setInterpolator(AnimationManager.FAST_OUT_SLOW_IN)
                        .start()
                    
                    // Restaurar elevación
                    card.animate()
                        .translationZ(0f)
                        .setDuration(150L)
                        .start()
                }
            }
            false
        }
    }
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // 🎨 EFECTOS DE HOVER Y FOCUS
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    /**
     * Configura efecto hover para cualquier vista
     */
    fun setupHoverEffect(view: View, scaleAmount: Float = 1.05f) {
        view.setOnHoverListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_HOVER_ENTER -> {
                    v.animate()
                        .scaleX(scaleAmount)
                        .scaleY(scaleAmount)
                        .setDuration(200L)
                        .setInterpolator(AnimationManager.FAST_OUT_SLOW_IN)
                        .start()
                }
                MotionEvent.ACTION_HOVER_EXIT -> {
                    v.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(200L)
                        .setInterpolator(AnimationManager.FAST_OUT_SLOW_IN)
                        .start()
                }
            }
            false
        }
    }
    
    /**
     * Configura efecto de focus para elementos navegables
     */
    fun setupFocusEffect(view: View) {
        view.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                v.animate()
                    .scaleX(1.05f)
                    .scaleY(1.05f)
                    .setDuration(200L)
                    .setInterpolator(AnimationManager.FAST_OUT_SLOW_IN)
                    .start()
            } else {
                v.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(200L)
                    .setInterpolator(AnimationManager.FAST_OUT_SLOW_IN)
                    .start()
            }
        }
    }
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // 🔄 ESTADOS DE LOADING Y FEEDBACK
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    /**
     * Animación de loading para botones
     */
    fun setButtonLoadingState(button: MaterialButton, isLoading: Boolean, loadingText: String = "Cargando...") {
        if (isLoading) {
            button.isEnabled = false
            val originalText = button.text
            button.text = loadingText
            button.tag = originalText // Guardar texto original
            
            // Animación de rotación sutil
            button.animate()
                .rotation(360f)
                .setDuration(1000L)
                .setInterpolator(AnimationManager.FAST_OUT_SLOW_IN)
                .withEndAction {
                    if (!button.isEnabled) {
                        // Continuar rotación si sigue cargando
                        button.rotation = 0f
                        setButtonLoadingState(button, true, loadingText)
                    }
                }
                .start()
        } else {
            button.isEnabled = true
            button.animate().cancel()
            button.rotation = 0f
            
            // Restaurar texto original
            val originalText = button.tag as? CharSequence
            if (originalText != null) {
                button.text = originalText
                button.tag = null
            }
        }
    }
    
    /**
     * Feedback visual de éxito
     */
    fun showSuccessFeedback(view: View, onComplete: (() -> Unit)? = null) {
        // Cambiar color temporalmente y hacer pulse
        val originalBackground = view.background
        
        view.animate()
            .scaleX(1.1f)
            .scaleY(1.1f)
            .setDuration(150L)
            .setInterpolator(AnimationManager.FAST_OUT_SLOW_IN)
            .withEndAction {
                view.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(150L)
                    .setInterpolator(AnimationManager.FAST_OUT_SLOW_IN)
                    .withEndAction {
                        onComplete?.invoke()
                    }
                    .start()
            }
            .start()
    }
    
    /**
     * Feedback visual de error
     */
    fun showErrorFeedback(view: View, onComplete: (() -> Unit)? = null) {
        // Shake animation para indicar error
        AnimationManager.shake(view, AnimationManager.DURATION_MEDIUM, 15f)
        
        // Cambiar color temporalmente si es posible
        val originalBackground = view.background
        
        view.postDelayed({
            onComplete?.invoke()
        }, AnimationManager.DURATION_MEDIUM)
    }
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // 🎪 TRANSICIONES DE ESTADO
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    /**
     * Transición suave entre estados de visibilidad
     */
    fun transitionVisibility(
        view: View,
        isVisible: Boolean,
        animationType: VisibilityTransition = VisibilityTransition.FADE,
        onComplete: (() -> Unit)? = null
    ) {
        when (animationType) {
            VisibilityTransition.FADE -> {
                if (isVisible) {
                    AnimationManager.fadeIn(view, onComplete = onComplete)
                } else {
                    AnimationManager.fadeOut(view, onComplete = onComplete)
                }
            }
            VisibilityTransition.SCALE -> {
                if (isVisible) {
                    AnimationManager.scaleIn(view, onComplete = onComplete)
                } else {
                    AnimationManager.scaleOut(view, onComplete = onComplete)
                }
            }
            VisibilityTransition.SLIDE_VERTICAL -> {
                if (isVisible) {
                    AnimationManager.slideInFromBottom(view, onComplete = onComplete)
                } else {
                    view.animate()
                        .translationY(view.height.toFloat())
                        .alpha(0f)
                        .setDuration(AnimationManager.DURATION_MEDIUM)
                        .setInterpolator(AnimationManager.FAST_OUT_SLOW_IN)
                        .withEndAction {
                            view.visibility = View.GONE
                            onComplete?.invoke()
                        }
                        .start()
                }
            }
        }
    }
    
    /**
     * Transición entre diferentes contenidos de una vista
     */
    fun transitionContent(
        container: View,
        newContentSetup: () -> Unit,
        animationType: ContentTransition = ContentTransition.CROSSFADE,
        onComplete: (() -> Unit)? = null
    ) {
        when (animationType) {
            ContentTransition.CROSSFADE -> {
                container.animate()
                    .alpha(0f)
                    .setDuration(AnimationManager.DURATION_SHORT)
                    .setInterpolator(AnimationManager.FAST_OUT_SLOW_IN)
                    .withEndAction {
                        newContentSetup()
                        container.animate()
                            .alpha(1f)
                            .setDuration(AnimationManager.DURATION_SHORT)
                            .setInterpolator(AnimationManager.FAST_OUT_SLOW_IN)
                            .withEndAction { onComplete?.invoke() }
                            .start()
                    }
                    .start()
            }
            ContentTransition.FLIP -> {
                AnimationManager.flipHorizontal(
                    view = container,
                    onHalfway = { newContentSetup() },
                    onComplete = onComplete
                )
            }
            ContentTransition.SCALE -> {
                container.animate()
                    .scaleX(0f)
                    .scaleY(0f)
                    .setDuration(AnimationManager.DURATION_SHORT)
                    .setInterpolator(AnimationManager.FAST_OUT_SLOW_IN)
                    .withEndAction {
                        newContentSetup()
                        container.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(AnimationManager.DURATION_SHORT)
                            .setInterpolator(AnimationManager.OVERSHOOT_INTERPOLATOR)
                            .withEndAction { onComplete?.invoke() }
                            .start()
                    }
                    .start()
            }
        }
    }
    
    enum class VisibilityTransition {
        FADE,
        SCALE,
        SLIDE_VERTICAL
    }
    
    enum class ContentTransition {
        CROSSFADE,
        FLIP,
        SCALE
    }
}

// ═══════════════════════════════════════════════════════════════════════════════════════════════
// 🔧 EXTENSION FUNCTIONS PARA MICRO-INTERACCIONES
// ═══════════════════════════════════════════════════════════════════════════════════════════════

/**
 * Configura micro-interacciones automáticamente según el tipo de vista
 */
fun View.setupMicroInteractions() {
    when (this) {
        is MaterialButton -> MicroInteractions.setupButtonInteractions(this)
        is FloatingActionButton -> MicroInteractions.setupFabInteractions(this)
        is MaterialCardView -> MicroInteractions.setupCardInteractions(this)
        else -> {
            // Para otras vistas, configurar efecto básico de press
            setOnTouchListener { view, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        view.animate()
                            .scaleX(0.97f)
                            .scaleY(0.97f)
                            .setDuration(100L)
                            .start()
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        view.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(150L)
                            .start()
                    }
                }
                false
            }
        }
    }
}

/**
 * Configura efecto hover
 */
fun View.setupHover(scaleAmount: Float = 1.05f) = 
    MicroInteractions.setupHoverEffect(this, scaleAmount)

/**
 * Configura efecto focus
 */
fun View.setupFocus() = MicroInteractions.setupFocusEffect(this)

/**
 * Muestra feedback de éxito
 */
fun View.showSuccess(onComplete: (() -> Unit)? = null) = 
    MicroInteractions.showSuccessFeedback(this, onComplete)

/**
 * Muestra feedback de error
 */
fun View.showError(onComplete: (() -> Unit)? = null) = 
    MicroInteractions.showErrorFeedback(this, onComplete)

/**
 * Transición de visibilidad
 */
fun View.transitionVisibility(
    isVisible: Boolean,
    animationType: MicroInteractions.VisibilityTransition = MicroInteractions.VisibilityTransition.FADE,
    onComplete: (() -> Unit)? = null
) = MicroInteractions.transitionVisibility(this, isVisible, animationType, onComplete)