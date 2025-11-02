package com.workstation.rotation.animations

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

/**
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 🎨 EXTENSIONES DE ANIMACIÓN PARA VIEWS - REWS v3.1.0
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 
 * Extensiones de Kotlin que hacen que las animaciones sean más fáciles de usar
 * con una sintaxis fluida y natural.
 * 
 * 🚀 CARACTERÍSTICAS:
 * • Sintaxis fluida con extension functions
 * • Métodos encadenables para animaciones complejas
 * • Configuración simplificada con parámetros por defecto
 * • Integración perfecta con el AnimationManager
 * 
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 */

// ═══════════════════════════════════════════════════════════════════════════════════════════════
// 🎭 EXTENSIONES DE FADE
// ═══════════════════════════════════════════════════════════════════════════════════════════════

/**
 * Fade in con sintaxis simplificada
 */
fun View.fadeIn(
    duration: Long = AnimationManager.DURATION_MEDIUM,
    delay: Long = 0L,
    onComplete: (() -> Unit)? = null
) = AnimationManager.fadeIn(this, duration, delay, onComplete = onComplete)

/**
 * Fade out con sintaxis simplificada
 */
fun View.fadeOut(
    duration: Long = AnimationManager.DURATION_MEDIUM,
    delay: Long = 0L,
    hideOnComplete: Boolean = true,
    onComplete: (() -> Unit)? = null
) = AnimationManager.fadeOut(this, duration, delay, hideOnComplete = hideOnComplete, onComplete = onComplete)

/**
 * Crossfade entre esta vista y otra
 */
fun View.crossfadeTo(
    targetView: View,
    duration: Long = AnimationManager.DURATION_MEDIUM,
    onComplete: (() -> Unit)? = null
) = AnimationManager.crossfade(this, targetView, duration, onComplete)

// ═══════════════════════════════════════════════════════════════════════════════════════════════
// 🎢 EXTENSIONES DE SLIDE
// ═══════════════════════════════════════════════════════════════════════════════════════════════

/**
 * Slide in desde la izquierda
 */
fun View.slideInFromLeft(
    duration: Long = AnimationManager.DURATION_MEDIUM,
    delay: Long = 0L,
    onComplete: (() -> Unit)? = null
) = AnimationManager.slideInFromLeft(this, duration, delay, onComplete = onComplete)

/**
 * Slide in desde la derecha
 */
fun View.slideInFromRight(
    duration: Long = AnimationManager.DURATION_MEDIUM,
    delay: Long = 0L,
    onComplete: (() -> Unit)? = null
) = AnimationManager.slideInFromRight(this, duration, delay, onComplete = onComplete)

/**
 * Slide in desde arriba
 */
fun View.slideInFromTop(
    duration: Long = AnimationManager.DURATION_MEDIUM,
    delay: Long = 0L,
    onComplete: (() -> Unit)? = null
) = AnimationManager.slideInFromTop(this, duration, delay, onComplete = onComplete)

/**
 * Slide in desde abajo
 */
fun View.slideInFromBottom(
    duration: Long = AnimationManager.DURATION_MEDIUM,
    delay: Long = 0L,
    onComplete: (() -> Unit)? = null
) = AnimationManager.slideInFromBottom(this, duration, delay, onComplete = onComplete)

/**
 * Slide out hacia la izquierda
 */
fun View.slideOutToLeft(
    duration: Long = AnimationManager.DURATION_MEDIUM,
    delay: Long = 0L,
    hideOnComplete: Boolean = true,
    onComplete: (() -> Unit)? = null
) = AnimationManager.slideOutToLeft(this, duration, delay, hideOnComplete = hideOnComplete, onComplete = onComplete)

/**
 * Slide out hacia la derecha
 */
fun View.slideOutToRight(
    duration: Long = AnimationManager.DURATION_MEDIUM,
    delay: Long = 0L,
    hideOnComplete: Boolean = true,
    onComplete: (() -> Unit)? = null
) = AnimationManager.slideOutToRight(this, duration, delay, hideOnComplete = hideOnComplete, onComplete = onComplete)

// ═══════════════════════════════════════════════════════════════════════════════════════════════
// 📏 EXTENSIONES DE SCALE
// ═══════════════════════════════════════════════════════════════════════════════════════════════

/**
 * Scale in con efecto de rebote
 */
fun View.scaleIn(
    duration: Long = AnimationManager.DURATION_MEDIUM,
    delay: Long = 0L,
    withBounce: Boolean = true,
    onComplete: (() -> Unit)? = null
) = AnimationManager.scaleIn(this, duration, delay, withBounce = withBounce, onComplete = onComplete)

/**
 * Scale out suave
 */
fun View.scaleOut(
    duration: Long = AnimationManager.DURATION_MEDIUM,
    delay: Long = 0L,
    hideOnComplete: Boolean = true,
    onComplete: (() -> Unit)? = null
) = AnimationManager.scaleOut(this, duration, delay, hideOnComplete = hideOnComplete, onComplete = onComplete)

/**
 * Pulse para llamar la atención
 */
fun View.pulse(
    duration: Long = AnimationManager.DURATION_SHORT,
    scaleAmount: Float = 1.1f,
    repeatCount: Int = 1
) = AnimationManager.pulse(this, duration, scaleAmount, repeatCount)

// ═══════════════════════════════════════════════════════════════════════════════════════════════
// 🌀 EXTENSIONES DE ROTACIÓN
// ═══════════════════════════════════════════════════════════════════════════════════════════════

/**
 * Rotación simple
 */
fun View.rotate(
    fromDegrees: Float = 0f,
    toDegrees: Float = 360f,
    duration: Long = AnimationManager.DURATION_MEDIUM,
    delay: Long = 0L,
    onComplete: (() -> Unit)? = null
) = AnimationManager.rotate(this, fromDegrees, toDegrees, duration, delay, onComplete)

/**
 * Flip horizontal
 */
fun View.flipHorizontal(
    duration: Long = AnimationManager.DURATION_MEDIUM,
    onHalfway: (() -> Unit)? = null,
    onComplete: (() -> Unit)? = null
) = AnimationManager.flipHorizontal(this, duration, onHalfway, onComplete)

/**
 * Flip vertical
 */
fun View.flipVertical(
    duration: Long = AnimationManager.DURATION_MEDIUM,
    onHalfway: (() -> Unit)? = null,
    onComplete: (() -> Unit)? = null
) = AnimationManager.flipVertical(this, duration, onHalfway, onComplete)

// ═══════════════════════════════════════════════════════════════════════════════════════════════
// 🎪 EXTENSIONES DE ANIMACIONES COMPLEJAS
// ═══════════════════════════════════════════════════════════════════════════════════════════════

/**
 * Entrada espectacular
 */
fun View.spectacularEntrance(
    duration: Long = AnimationManager.DURATION_LONG,
    delay: Long = 0L,
    onComplete: (() -> Unit)? = null
) = AnimationManager.spectacularEntrance(this, duration, delay, onComplete)

/**
 * Salida espectacular
 */
fun View.spectacularExit(
    duration: Long = AnimationManager.DURATION_LONG,
    delay: Long = 0L,
    hideOnComplete: Boolean = true,
    onComplete: (() -> Unit)? = null
) = AnimationManager.spectacularExit(this, duration, delay, hideOnComplete, onComplete)

/**
 * Shake para errores
 */
fun View.shake(
    duration: Long = AnimationManager.DURATION_MEDIUM,
    intensity: Float = 10f
) = AnimationManager.shake(this, duration, intensity)

/**
 * Wobble divertido
 */
fun View.wobble(
    duration: Long = AnimationManager.DURATION_MEDIUM,
    intensity: Float = 15f
) = AnimationManager.wobble(this, duration, intensity)

// ═══════════════════════════════════════════════════════════════════════════════════════════════
// 🎮 EXTENSIONES DE MICRO-INTERACCIONES
// ═══════════════════════════════════════════════════════════════════════════════════════════════

/**
 * Feedback de click
 */
fun View.clickFeedback(
    scaleDown: Float = 0.95f,
    duration: Long = AnimationManager.DURATION_SHORT
) = AnimationManager.clickFeedback(this, scaleDown, duration)

/**
 * Efecto hover
 */
fun View.hoverEffect(
    isHovered: Boolean,
    duration: Long = AnimationManager.DURATION_SHORT
) = AnimationManager.hoverEffect(this, isHovered, duration)

/**
 * Rotación de loading
 */
fun View.loadingRotation(isLoading: Boolean) = AnimationManager.loadingRotation(this, isLoading)

// ═══════════════════════════════════════════════════════════════════════════════════════════════
// 🔧 EXTENSIONES DE UTILIDADES
// ═══════════════════════════════════════════════════════════════════════════════════════════════

/**
 * Cancela todas las animaciones
 */
fun View.cancelAnimations() = AnimationManager.cancelAllAnimations(this)

/**
 * Resetea propiedades de transformación
 */
fun View.resetProperties() = AnimationManager.resetViewProperties(this)

/**
 * Verifica si tiene animaciones activas
 */
fun View.hasAnimations(): Boolean = AnimationManager.hasActiveAnimations(this)

// ═══════════════════════════════════════════════════════════════════════════════════════════════
// 📋 EXTENSIONES PARA VIEWGROUP Y LISTAS
// ═══════════════════════════════════════════════════════════════════════════════════════════════

/**
 * Anima todos los hijos de un ViewGroup con stagger
 */
fun ViewGroup.animateChildrenStaggered(
    animationType: AnimationManager.StaggerType = AnimationManager.StaggerType.SLIDE_IN_FROM_BOTTOM,
    baseDuration: Long = AnimationManager.DURATION_MEDIUM,
    staggerDelay: Long = AnimationManager.DELAY_MEDIUM,
    onComplete: (() -> Unit)? = null
) {
    val children = (0 until childCount).map { getChildAt(it) }
    AnimationManager.staggeredListAnimation(children, animationType, baseDuration, staggerDelay, onComplete)
}

/**
 * Fade in todos los hijos
 */
fun ViewGroup.fadeInChildren(
    duration: Long = AnimationManager.DURATION_MEDIUM,
    staggerDelay: Long = AnimationManager.DELAY_MEDIUM,
    onComplete: (() -> Unit)? = null
) = animateChildrenStaggered(AnimationManager.StaggerType.FADE_IN, duration, staggerDelay, onComplete)

/**
 * Scale in todos los hijos
 */
fun ViewGroup.scaleInChildren(
    duration: Long = AnimationManager.DURATION_MEDIUM,
    staggerDelay: Long = AnimationManager.DELAY_MEDIUM,
    onComplete: (() -> Unit)? = null
) = animateChildrenStaggered(AnimationManager.StaggerType.SCALE_IN, duration, staggerDelay, onComplete)

/**
 * Slide in todos los hijos desde abajo
 */
fun ViewGroup.slideInChildrenFromBottom(
    duration: Long = AnimationManager.DURATION_MEDIUM,
    staggerDelay: Long = AnimationManager.DELAY_MEDIUM,
    onComplete: (() -> Unit)? = null
) = animateChildrenStaggered(AnimationManager.StaggerType.SLIDE_IN_FROM_BOTTOM, duration, staggerDelay, onComplete)

// ═══════════════════════════════════════════════════════════════════════════════════════════════
// 🎯 EXTENSIONES PARA CASOS DE USO ESPECÍFICOS
// ═══════════════════════════════════════════════════════════════════════════════════════════════

/**
 * Animación de aparición para cards
 */
fun View.cardAppearance(
    delay: Long = 0L,
    onComplete: (() -> Unit)? = null
) {
    alpha = 0f
    scaleX = 0.9f
    scaleY = 0.9f
    translationY = 50f
    isVisible = true
    
    animate()
        .alpha(1f)
        .scaleX(1f)
        .scaleY(1f)
        .translationY(0f)
        .setDuration(AnimationManager.DURATION_MEDIUM)
        .setStartDelay(delay)
        .setInterpolator(AnimationManager.FAST_OUT_SLOW_IN)
        .withEndAction { onComplete?.invoke() }
        .start()
}

/**
 * Animación de desaparición para cards
 */
fun View.cardDisappearance(
    delay: Long = 0L,
    hideOnComplete: Boolean = true,
    onComplete: (() -> Unit)? = null
) {
    animate()
        .alpha(0f)
        .scaleX(0.9f)
        .scaleY(0.9f)
        .translationY(-50f)
        .setDuration(AnimationManager.DURATION_MEDIUM)
        .setStartDelay(delay)
        .setInterpolator(AnimationManager.FAST_OUT_SLOW_IN)
        .withEndAction {
            if (hideOnComplete) isVisible = false
            onComplete?.invoke()
        }
        .start()
}

/**
 * Animación de éxito (verde con check)
 */
fun View.successAnimation(onComplete: (() -> Unit)? = null) {
    pulse(AnimationManager.DURATION_SHORT, 1.2f, 2)
    // Aquí podrías cambiar el color de fondo temporalmente si es necesario
}

/**
 * Animación de error (rojo con shake)
 */
fun View.errorAnimation(onComplete: (() -> Unit)? = null) {
    shake(AnimationManager.DURATION_MEDIUM, 15f)
    // Aquí podrías cambiar el color de fondo temporalmente si es necesario
}

/**
 * Animación de loading suave
 */
fun View.breathingAnimation(isActive: Boolean) {
    if (isActive) {
        animate()
            .scaleX(1.05f)
            .scaleY(1.05f)
            .setDuration(1000L)
            .setInterpolator(AnimationManager.FAST_OUT_SLOW_IN)
            .withEndAction {
                if (tag == "breathing_active") {
                    animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(1000L)
                        .setInterpolator(AnimationManager.FAST_OUT_SLOW_IN)
                        .withEndAction {
                            if (tag == "breathing_active") {
                                breathingAnimation(true)
                            }
                        }
                        .start()
                }
            }
            .start()
        tag = "breathing_active"
    } else {
        tag = null
        animate().cancel()
        scaleX = 1f
        scaleY = 1f
    }
}