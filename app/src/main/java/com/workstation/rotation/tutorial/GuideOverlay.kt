package com.workstation.rotation.tutorial

import android.content.Context
import android.graphics.*
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.workstation.rotation.R

/**
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 🎯 OVERLAY DE GUÍA CONTEXTUAL
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 
 * Crea un overlay visual que resalta elementos específicos de la UI durante el tutorial.
 * Oscurece el resto de la pantalla y muestra pistas visuales.
 */
class GuideOverlay(context: Context) : View(context) {
    
    private val paint = Paint().apply {
        color = Color.BLACK
        alpha = 180 // Semi-transparente
    }
    
    private val clearPaint = Paint().apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }
    
    private val highlightPaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.accent_orange)
        style = Paint.Style.STROKE
        strokeWidth = 8f
        pathEffect = DashPathEffect(floatArrayOf(20f, 10f), 0f)
    }
    
    private var targetView: View? = null
    private var targetRect = RectF()
    private var pulseAnimation = 0f
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        // Dibujar overlay oscuro
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        
        // Crear "agujero" transparente alrededor del elemento objetivo
        targetView?.let { target ->
            val location = IntArray(2)
            target.getLocationOnScreen(location)
            
            val overlayLocation = IntArray(2)
            getLocationOnScreen(overlayLocation)
            
            val left = (location[0] - overlayLocation[0] - 20).toFloat()
            val top = (location[1] - overlayLocation[1] - 20).toFloat()
            val right = left + target.width + 40f
            val bottom = top + target.height + 40f
            
            targetRect.set(left, top, right, bottom)
            
            // Crear área transparente
            canvas.drawRoundRect(targetRect, 20f, 20f, clearPaint)
            
            // Dibujar borde animado
            val animatedPaint = Paint(highlightPaint).apply {
                alpha = (255 * (0.5f + 0.5f * kotlin.math.sin(pulseAnimation))).toInt()
                strokeWidth = 8f + 4f * kotlin.math.sin(pulseAnimation)
            }
            canvas.drawRoundRect(targetRect, 20f, 20f, animatedPaint)
        }
    }
    
    /**
     * Establece la vista objetivo a resaltar.
     */
    fun setTargetView(view: View?) {
        targetView = view
        invalidate()
    }
    
    /**
     * Inicia la animación de pulso.
     */
    fun startPulseAnimation() {
        val animator = android.animation.ValueAnimator.ofFloat(0f, 2f * Math.PI.toFloat())
        animator.duration = 2000
        animator.repeatCount = android.animation.ValueAnimator.INFINITE
        animator.addUpdateListener { animation ->
            pulseAnimation = animation.animatedValue as Float
            invalidate()
        }
        animator.start()
    }
    
    companion object {
        /**
         * Agrega un overlay de guía a la actividad.
         */
        fun addToActivity(activity: android.app.Activity, targetView: View?): GuideOverlay {
            val overlay = GuideOverlay(activity)
            overlay.setTargetView(targetView)
            
            val rootView = activity.findViewById<ViewGroup>(android.R.id.content)
            val layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            
            rootView.addView(overlay, layoutParams)
            overlay.startPulseAnimation()
            
            return overlay
        }
        
        /**
         * Remueve el overlay de la actividad.
         */
        fun removeFromActivity(activity: android.app.Activity, overlay: GuideOverlay) {
            val rootView = activity.findViewById<ViewGroup>(android.R.id.content)
            rootView.removeView(overlay)
        }
    }
}