package com.workstation.rotation.tutorial

import android.content.Context
import android.graphics.*
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.workstation.rotation.R

/**
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 💡 PISTA FLOTANTE - AYUDA CONTEXTUAL VISUAL
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 
 * Muestra pistas flotantes que apuntan a elementos específicos de la UI.
 * Proporciona ayuda contextual sin interrumpir el flujo de trabajo.
 */
class FloatingHint(context: Context) : FrameLayout(context) {
    
    private val hintText: TextView
    private val arrowView: View
    private var targetView: View? = null
    
    init {
        // Configurar el contenedor principal
        try {
            setBackgroundResource(R.drawable.floating_hint_background)
        } catch (e: Exception) {
            // Fallback a color sólido si el drawable no existe
            setBackgroundColor(ContextCompat.getColor(context, R.color.primary_blue))
        }
        elevation = 8f
        
        // Crear texto de la pista
        hintText = TextView(context).apply {
            textSize = 14f
            setTextColor(ContextCompat.getColor(context, R.color.white))
            gravity = Gravity.CENTER
            setPadding(24, 16, 24, 16)
        }
        
        // Crear flecha indicadora
        arrowView = View(context).apply {
            try {
                setBackgroundResource(R.drawable.hint_arrow)
            } catch (e: Exception) {
                // Fallback a color sólido si el drawable no existe
                setBackgroundColor(ContextCompat.getColor(context, R.color.primary_blue))
            }
        }
        
        // Agregar vistas al contenedor
        addView(hintText)
        addView(arrowView)
    }
    
    /**
     * Configura la pista con texto y vista objetivo.
     */
    fun setupHint(text: String, target: View, position: HintPosition = HintPosition.ABOVE) {
        targetView = target
        hintText.text = text
        
        // Configurar posición de la flecha según la posición de la pista
        val arrowParams = FrameLayout.LayoutParams(32, 16)
        when (position) {
            HintPosition.ABOVE -> {
                arrowParams.gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                arrowView.rotation = 180f
            }
            HintPosition.BELOW -> {
                arrowParams.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
                arrowView.rotation = 0f
            }
            HintPosition.LEFT -> {
                arrowParams.gravity = Gravity.CENTER_VERTICAL or Gravity.END
                arrowParams.width = 16
                arrowParams.height = 32
                arrowView.rotation = 90f
            }
            HintPosition.RIGHT -> {
                arrowParams.gravity = Gravity.CENTER_VERTICAL or Gravity.START
                arrowParams.width = 16
                arrowParams.height = 32
                arrowView.rotation = -90f
            }
        }
        arrowView.layoutParams = arrowParams
    }
    
    /**
     * Posiciona la pista relativa a la vista objetivo.
     */
    fun positionRelativeToTarget(position: HintPosition = HintPosition.ABOVE) {
        targetView?.let { target ->
            val targetLocation = IntArray(2)
            target.getLocationOnScreen(targetLocation)
            
            val parentLocation = IntArray(2)
            (parent as? View)?.getLocationOnScreen(parentLocation) ?: return
            
            val relativeX = targetLocation[0] - parentLocation[0]
            val relativeY = targetLocation[1] - parentLocation[1]
            
            when (position) {
                HintPosition.ABOVE -> {
                    x = (relativeX + target.width / 2 - width / 2).toFloat()
                    y = (relativeY - height - 16).toFloat()
                }
                HintPosition.BELOW -> {
                    x = (relativeX + target.width / 2 - width / 2).toFloat()
                    y = (relativeY + target.height + 16).toFloat()
                }
                HintPosition.LEFT -> {
                    x = (relativeX - width - 16).toFloat()
                    y = (relativeY + target.height / 2 - height / 2).toFloat()
                }
                HintPosition.RIGHT -> {
                    x = (relativeX + target.width + 16).toFloat()
                    y = (relativeY + target.height / 2 - height / 2).toFloat()
                }
            }
        }
    }
    
    /**
     * Muestra la pista con animación.
     */
    fun show() {
        alpha = 0f
        scaleX = 0.8f
        scaleY = 0.8f
        
        animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(300)
            .start()
    }
    
    /**
     * Oculta la pista con animación.
     */
    fun hide(onComplete: (() -> Unit)? = null) {
        animate()
            .alpha(0f)
            .scaleX(0.8f)
            .scaleY(0.8f)
            .setDuration(200)
            .withEndAction {
                (parent as? ViewGroup)?.removeView(this)
                onComplete?.invoke()
            }
            .start()
    }
    
    enum class HintPosition {
        ABOVE, BELOW, LEFT, RIGHT
    }
    
    companion object {
        /**
         * Crea y muestra una pista flotante.
         */
        fun show(
            context: Context,
            parent: ViewGroup,
            text: String,
            target: View,
            position: HintPosition = HintPosition.ABOVE,
            duration: Long = 3000
        ): FloatingHint {
            val hint = FloatingHint(context)
            hint.setupHint(text, target, position)
            
            // Agregar al padre
            val layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
            parent.addView(hint, layoutParams)
            
            // Posicionar después de que se agregue al layout
            hint.post {
                hint.positionRelativeToTarget(position)
                hint.show()
                
                // Auto-ocultar después del tiempo especificado
                if (duration > 0) {
                    hint.postDelayed({
                        hint.hide()
                    }, duration)
                }
            }
            
            return hint
        }
    }
}