package com.workstation.rotation.animations

import android.animation.*
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.view.animation.*
import androidx.core.animation.doOnEnd
import androidx.core.view.isVisible
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.recyclerview.widget.RecyclerView

/**
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 🎨 GESTOR DE ANIMACIONES AVANZADAS - REWS v3.1.0
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 
 * Sistema centralizado de animaciones modernas y fluidas para mejorar la experiencia de usuario.
 * Incluye animaciones de entrada, salida, transiciones y micro-interacciones.
 * 
 * 🎯 TIPOS DE ANIMACIONES:
 * • Fade In/Out con diferentes duraciones y delays
 * • Slide In/Out desde diferentes direcciones
 * • Scale In/Out con efectos de rebote
 * • Rotation y flip effects para elementos interactivos
 * • Stagger animations para listas y grids
 * • Morphing animations para transiciones de estado
 * 
 * 🚀 CARACTERÍSTICAS:
 * • Interpoladores optimizados para sensación natural
 * • Animaciones en cadena con callbacks
 * • Soporte para animaciones simultáneas
 * • Configuración de duración y delay personalizables
 * • Animaciones responsivas según el contexto
 * 
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 */

object AnimationManager {
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // 🎯 CONSTANTES DE CONFIGURACIÓN
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    const val DURATION_SHORT = 200L
    const val DURATION_MEDIUM = 300L
    const val DURATION_LONG = 500L
    const val DURATION_EXTRA_LONG = 800L
    
    const val DELAY_SHORT = 50L
    const val DELAY_MEDIUM = 100L
    const val DELAY_LONG = 150L
    
    // Interpoladores optimizados
    val FAST_OUT_SLOW_IN = FastOutSlowInInterpolator()
    val BOUNCE_INTERPOLATOR = BounceInterpolator()
    val OVERSHOOT_INTERPOLATOR = OvershootInterpolator(1.2f)
    val ANTICIPATE_OVERSHOOT = AnticipateOvershootInterpolator(1.0f)
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // 🎭 ANIMACIONES DE FADE (DESVANECIMIENTO)
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    /**
     * Fade In suave con configuración personalizable
     */
    fun fadeIn(
        view: View,
        duration: Long = DURATION_MEDIUM,
        delay: Long = 0L,
        startAlpha: Float = 0f,
        endAlpha: Float = 1f,
        onComplete: (() -> Unit)? = null
    ) {
        view.alpha = startAlpha
        view.isVisible = true
        
        view.animate()
            .alpha(endAlpha)
            .setDuration(duration)
            .setStartDelay(delay)
            .setInterpolator(FAST_OUT_SLOW_IN)
            .withEndAction { onComplete?.invoke() }
            .start()
    }
    
    /**
     * Fade Out suave con opción de ocultar vista
     */
    fun fadeOut(
        view: View,
        duration: Long = DURATION_MEDIUM,
        delay: Long = 0L,
        endAlpha: Float = 0f,
        hideOnComplete: Boolean = true,
        onComplete: (() -> Unit)? = null
    ) {
        view.animate()
            .alpha(endAlpha)
            .setDuration(duration)
            .setStartDelay(delay)
            .setInterpolator(FAST_OUT_SLOW_IN)
            .withEndAction {
                if (hideOnComplete) view.isVisible = false
                onComplete?.invoke()
            }
            .start()
    }
    
    /**
     * Crossfade entre dos vistas
     */
    fun crossfade(
        viewOut: View,
        viewIn: View,
        duration: Long = DURATION_MEDIUM,
        onComplete: (() -> Unit)? = null
    ) {
        fadeOut(viewOut, duration, hideOnComplete = true)
        fadeIn(viewIn, duration, delay = duration / 4) {
            onComplete?.invoke()
        }
    }
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // 🎢 ANIMACIONES DE SLIDE (DESLIZAMIENTO)
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    /**
     * Slide In desde la izquierda
     */
    fun slideInFromLeft(
        view: View,
        duration: Long = DURATION_MEDIUM,
        delay: Long = 0L,
        distance: Float? = null,
        onComplete: (() -> Unit)? = null
    ) {
        val slideDistance = distance ?: view.width.toFloat()
        view.translationX = -slideDistance
        view.isVisible = true
        
        view.animate()
            .translationX(0f)
            .setDuration(duration)
            .setStartDelay(delay)
            .setInterpolator(FAST_OUT_SLOW_IN)
            .withEndAction { onComplete?.invoke() }
            .start()
    }
    
    /**
     * Slide In desde la derecha
     */
    fun slideInFromRight(
        view: View,
        duration: Long = DURATION_MEDIUM,
        delay: Long = 0L,
        distance: Float? = null,
        onComplete: (() -> Unit)? = null
    ) {
        val slideDistance = distance ?: view.width.toFloat()
        view.translationX = slideDistance
        view.isVisible = true
        
        view.animate()
            .translationX(0f)
            .setDuration(duration)
            .setStartDelay(delay)
            .setInterpolator(FAST_OUT_SLOW_IN)
            .withEndAction { onComplete?.invoke() }
            .start()
    }
    
    /**
     * Slide In desde arriba
     */
    fun slideInFromTop(
        view: View,
        duration: Long = DURATION_MEDIUM,
        delay: Long = 0L,
        distance: Float? = null,
        onComplete: (() -> Unit)? = null
    ) {
        val slideDistance = distance ?: view.height.toFloat()
        view.translationY = -slideDistance
        view.isVisible = true
        
        view.animate()
            .translationY(0f)
            .setDuration(duration)
            .setStartDelay(delay)
            .setInterpolator(FAST_OUT_SLOW_IN)
            .withEndAction { onComplete?.invoke() }
            .start()
    }
    
    /**
     * Slide In desde abajo
     */
    fun slideInFromBottom(
        view: View,
        duration: Long = DURATION_MEDIUM,
        delay: Long = 0L,
        distance: Float? = null,
        onComplete: (() -> Unit)? = null
    ) {
        val slideDistance = distance ?: view.height.toFloat()
        view.translationY = slideDistance
        view.isVisible = true
        
        view.animate()
            .translationY(0f)
            .setDuration(duration)
            .setStartDelay(delay)
            .setInterpolator(FAST_OUT_SLOW_IN)
            .withEndAction { onComplete?.invoke() }
            .start()
    }
    
    /**
     * Slide Out hacia la izquierda
     */
    fun slideOutToLeft(
        view: View,
        duration: Long = DURATION_MEDIUM,
        delay: Long = 0L,
        distance: Float? = null,
        hideOnComplete: Boolean = true,
        onComplete: (() -> Unit)? = null
    ) {
        val slideDistance = distance ?: view.width.toFloat()
        
        view.animate()
            .translationX(-slideDistance)
            .setDuration(duration)
            .setStartDelay(delay)
            .setInterpolator(FAST_OUT_SLOW_IN)
            .withEndAction {
                if (hideOnComplete) view.isVisible = false
                onComplete?.invoke()
            }
            .start()
    }
    
    /**
     * Slide Out hacia la derecha
     */
    fun slideOutToRight(
        view: View,
        duration: Long = DURATION_MEDIUM,
        delay: Long = 0L,
        distance: Float? = null,
        hideOnComplete: Boolean = true,
        onComplete: (() -> Unit)? = null
    ) {
        val slideDistance = distance ?: view.width.toFloat()
        
        view.animate()
            .translationX(slideDistance)
            .setDuration(duration)
            .setStartDelay(delay)
            .setInterpolator(FAST_OUT_SLOW_IN)
            .withEndAction {
                if (hideOnComplete) view.isVisible = false
                onComplete?.invoke()
            }
            .start()
    }
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // 📏 ANIMACIONES DE SCALE (ESCALADO)
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    /**
     * Scale In con efecto de rebote
     */
    fun scaleIn(
        view: View,
        duration: Long = DURATION_MEDIUM,
        delay: Long = 0L,
        startScale: Float = 0f,
        endScale: Float = 1f,
        withBounce: Boolean = true,
        onComplete: (() -> Unit)? = null
    ) {
        view.scaleX = startScale
        view.scaleY = startScale
        view.isVisible = true
        
        val interpolator = if (withBounce) OVERSHOOT_INTERPOLATOR else FAST_OUT_SLOW_IN
        
        view.animate()
            .scaleX(endScale)
            .scaleY(endScale)
            .setDuration(duration)
            .setStartDelay(delay)
            .setInterpolator(interpolator)
            .withEndAction { onComplete?.invoke() }
            .start()
    }
    
    /**
     * Scale Out suave
     */
    fun scaleOut(
        view: View,
        duration: Long = DURATION_MEDIUM,
        delay: Long = 0L,
        endScale: Float = 0f,
        hideOnComplete: Boolean = true,
        onComplete: (() -> Unit)? = null
    ) {
        view.animate()
            .scaleX(endScale)
            .scaleY(endScale)
            .setDuration(duration)
            .setStartDelay(delay)
            .setInterpolator(FAST_OUT_SLOW_IN)
            .withEndAction {
                if (hideOnComplete) view.isVisible = false
                onComplete?.invoke()
            }
            .start()
    }
    
    /**
     * Pulse animation para llamar la atención
     */
    fun pulse(
        view: View,
        duration: Long = DURATION_SHORT,
        scaleAmount: Float = 1.1f,
        repeatCount: Int = 1
    ) {
        val scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, scaleAmount, 1f)
        val scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, scaleAmount, 1f)
        
        val animatorSet = AnimatorSet().apply {
            playTogether(scaleX, scaleY)
            this.duration = duration
            interpolator = FAST_OUT_SLOW_IN
            if (repeatCount > 1) {
                // Para múltiples repeticiones, usar ValueAnimator con repeat
                val pulseAnimator = ValueAnimator.ofFloat(1f, scaleAmount, 1f).apply {
                    this.duration = duration
                    this.repeatCount = repeatCount - 1
                    this.repeatMode = ValueAnimator.RESTART
                    addUpdateListener { animation ->
                        val scale = animation.animatedValue as Float
                        view.scaleX = scale
                        view.scaleY = scale
                    }
                }
                pulseAnimator.start()
                return
            }
        }
        
        animatorSet.start()
    }
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // 🌀 ANIMACIONES DE ROTACIÓN
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    /**
     * Rotación simple
     */
    fun rotate(
        view: View,
        fromDegrees: Float = 0f,
        toDegrees: Float = 360f,
        duration: Long = DURATION_MEDIUM,
        delay: Long = 0L,
        onComplete: (() -> Unit)? = null
    ) {
        view.animate()
            .rotation(toDegrees)
            .setDuration(duration)
            .setStartDelay(delay)
            .setInterpolator(FAST_OUT_SLOW_IN)
            .withStartAction { view.rotation = fromDegrees }
            .withEndAction { onComplete?.invoke() }
            .start()
    }
    
    /**
     * Flip horizontal
     */
    fun flipHorizontal(
        view: View,
        duration: Long = DURATION_MEDIUM,
        onHalfway: (() -> Unit)? = null,
        onComplete: (() -> Unit)? = null
    ) {
        view.animate()
            .rotationY(90f)
            .setDuration(duration / 2)
            .setInterpolator(FAST_OUT_SLOW_IN)
            .withEndAction {
                onHalfway?.invoke()
                view.animate()
                    .rotationY(0f)
                    .setDuration(duration / 2)
                    .setInterpolator(FAST_OUT_SLOW_IN)
                    .withEndAction { onComplete?.invoke() }
                    .start()
            }
            .start()
    }
    
    /**
     * Flip vertical
     */
    fun flipVertical(
        view: View,
        duration: Long = DURATION_MEDIUM,
        onHalfway: (() -> Unit)? = null,
        onComplete: (() -> Unit)? = null
    ) {
        view.animate()
            .rotationX(90f)
            .setDuration(duration / 2)
            .setInterpolator(FAST_OUT_SLOW_IN)
            .withEndAction {
                onHalfway?.invoke()
                view.animate()
                    .rotationX(0f)
                    .setDuration(duration / 2)
                    .setInterpolator(FAST_OUT_SLOW_IN)
                    .withEndAction { onComplete?.invoke() }
                    .start()
            }
            .start()
    }
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // 🎪 ANIMACIONES COMBINADAS Y COMPLEJAS
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    /**
     * Entrada espectacular combinando fade, scale y slide
     */
    fun spectacularEntrance(
        view: View,
        duration: Long = DURATION_LONG,
        delay: Long = 0L,
        onComplete: (() -> Unit)? = null
    ) {
        // Estado inicial
        view.alpha = 0f
        view.scaleX = 0.3f
        view.scaleY = 0.3f
        view.translationY = 100f
        view.isVisible = true
        
        // Animación combinada
        view.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .translationY(0f)
            .setDuration(duration)
            .setStartDelay(delay)
            .setInterpolator(OVERSHOOT_INTERPOLATOR)
            .withEndAction { onComplete?.invoke() }
            .start()
    }
    
    /**
     * Salida espectacular
     */
    fun spectacularExit(
        view: View,
        duration: Long = DURATION_LONG,
        delay: Long = 0L,
        hideOnComplete: Boolean = true,
        onComplete: (() -> Unit)? = null
    ) {
        view.animate()
            .alpha(0f)
            .scaleX(0.3f)
            .scaleY(0.3f)
            .translationY(-100f)
            .setDuration(duration)
            .setStartDelay(delay)
            .setInterpolator(FAST_OUT_SLOW_IN)
            .withEndAction {
                if (hideOnComplete) view.isVisible = false
                onComplete?.invoke()
            }
            .start()
    }
    
    /**
     * Shake animation para errores o atención
     */
    fun shake(
        view: View,
        duration: Long = DURATION_MEDIUM,
        intensity: Float = 10f
    ) {
        val shake = ObjectAnimator.ofFloat(
            view, "translationX",
            0f, intensity, -intensity, intensity, -intensity, intensity/2, -intensity/2, 0f
        ).apply {
            this.duration = duration
            interpolator = LinearInterpolator()
        }
        shake.start()
    }
    
    /**
     * Wobble animation divertida
     */
    fun wobble(
        view: View,
        duration: Long = DURATION_MEDIUM,
        intensity: Float = 15f
    ) {
        val wobble = ObjectAnimator.ofFloat(
            view, "rotation",
            0f, intensity, -intensity, intensity/2, -intensity/2, 0f
        ).apply {
            this.duration = duration
            interpolator = LinearInterpolator()
        }
        wobble.start()
    }
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // 📋 ANIMACIONES PARA LISTAS Y RECYCLERVIEW
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    /**
     * Stagger animation para elementos de lista
     */
    fun staggeredListAnimation(
        views: List<View>,
        animationType: StaggerType = StaggerType.SLIDE_IN_FROM_BOTTOM,
        baseDuration: Long = DURATION_MEDIUM,
        staggerDelay: Long = DELAY_MEDIUM,
        onComplete: (() -> Unit)? = null
    ) {
        views.forEachIndexed { index, view ->
            val delay = index * staggerDelay
            val isLast = index == views.size - 1
            
            when (animationType) {
                StaggerType.FADE_IN -> {
                    fadeIn(view, baseDuration, delay) {
                        if (isLast) onComplete?.invoke()
                    }
                }
                StaggerType.SLIDE_IN_FROM_BOTTOM -> {
                    slideInFromBottom(view, baseDuration, delay) {
                        if (isLast) onComplete?.invoke()
                    }
                }
                StaggerType.SLIDE_IN_FROM_LEFT -> {
                    slideInFromLeft(view, baseDuration, delay) {
                        if (isLast) onComplete?.invoke()
                    }
                }
                StaggerType.SCALE_IN -> {
                    scaleIn(view, baseDuration, delay, withBounce = true) {
                        if (isLast) onComplete?.invoke()
                    }
                }
                StaggerType.SPECTACULAR -> {
                    spectacularEntrance(view, baseDuration, delay) {
                        if (isLast) onComplete?.invoke()
                    }
                }
            }
        }
    }
    
    /**
     * Animación para RecyclerView items
     */
    fun animateRecyclerViewItem(
        view: View,
        animationType: StaggerType = StaggerType.SLIDE_IN_FROM_BOTTOM,
        duration: Long = DURATION_MEDIUM
    ) {
        when (animationType) {
            StaggerType.FADE_IN -> fadeIn(view, duration)
            StaggerType.SLIDE_IN_FROM_BOTTOM -> slideInFromBottom(view, duration)
            StaggerType.SLIDE_IN_FROM_LEFT -> slideInFromLeft(view, duration)
            StaggerType.SCALE_IN -> scaleIn(view, duration, withBounce = true)
            StaggerType.SPECTACULAR -> spectacularEntrance(view, duration)
        }
    }
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // 🎮 MICRO-INTERACCIONES Y FEEDBACK
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    /**
     * Animación de click/tap feedback
     */
    fun clickFeedback(
        view: View,
        scaleDown: Float = 0.95f,
        duration: Long = DURATION_SHORT
    ) {
        view.animate()
            .scaleX(scaleDown)
            .scaleY(scaleDown)
            .setDuration(duration / 2)
            .setInterpolator(FAST_OUT_SLOW_IN)
            .withEndAction {
                view.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(duration / 2)
                    .setInterpolator(FAST_OUT_SLOW_IN)
                    .start()
            }
            .start()
    }
    
    /**
     * Animación de hover/focus
     */
    fun hoverEffect(
        view: View,
        isHovered: Boolean,
        duration: Long = DURATION_SHORT
    ) {
        val targetScale = if (isHovered) 1.05f else 1f
        val targetElevation = if (isHovered) 8f else 4f
        
        view.animate()
            .scaleX(targetScale)
            .scaleY(targetScale)
            .translationZ(targetElevation)
            .setDuration(duration)
            .setInterpolator(FAST_OUT_SLOW_IN)
            .start()
    }
    
    /**
     * Animación de loading/progress
     */
    fun loadingRotation(
        view: View,
        isLoading: Boolean
    ) {
        if (isLoading) {
            val rotation = ObjectAnimator.ofFloat(view, "rotation", 0f, 360f).apply {
                duration = 1000L
                repeatCount = ObjectAnimator.INFINITE
                repeatMode = ObjectAnimator.RESTART
                interpolator = LinearInterpolator()
            }
            view.tag = rotation
            rotation.start()
        } else {
            (view.tag as? ObjectAnimator)?.cancel()
            view.rotation = 0f
        }
    }
    
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    // 🔧 UTILIDADES Y HELPERS
    // ═══════════════════════════════════════════════════════════════════════════════════════════
    
    /**
     * Cancela todas las animaciones de una vista
     */
    fun cancelAllAnimations(view: View) {
        view.animate().cancel()
        view.clearAnimation()
    }
    
    /**
     * Resetea todas las propiedades de transformación de una vista
     */
    fun resetViewProperties(view: View) {
        view.alpha = 1f
        view.scaleX = 1f
        view.scaleY = 1f
        view.translationX = 0f
        view.translationY = 0f
        view.translationZ = 0f
        view.rotation = 0f
        view.rotationX = 0f
        view.rotationY = 0f
    }
    
    /**
     * Verifica si una vista tiene animaciones activas
     */
    fun hasActiveAnimations(view: View): Boolean {
        return view.animate() != null || view.animation != null
    }
    
    enum class StaggerType {
        FADE_IN,
        SLIDE_IN_FROM_BOTTOM,
        SLIDE_IN_FROM_LEFT,
        SCALE_IN,
        SPECTACULAR
    }
}