package com.workstation.rotation.animations

import android.view.View
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView

/**
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 🎬 ANIMADOR PERSONALIZADO PARA RECYCLERVIEW - REWS v3.1.0
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 
 * ItemAnimator personalizado que proporciona animaciones fluidas y modernas
 * para elementos de RecyclerView con efectos de entrada, salida y cambios.
 * 
 * 🎯 CARACTERÍSTICAS:
 * • Animaciones de entrada con slide y fade
 * • Animaciones de salida suaves
 * • Animaciones de cambio con morphing
 * • Duraciones optimizadas para sensación natural
 * • Soporte para diferentes tipos de animación según contexto
 * 
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 */

class RecyclerViewAnimator : DefaultItemAnimator() {
    
    init {
        // Configurar duraciones optimizadas
        addDuration = AnimationManager.DURATION_MEDIUM
        removeDuration = AnimationManager.DURATION_MEDIUM
        moveDuration = AnimationManager.DURATION_MEDIUM
        changeDuration = AnimationManager.DURATION_MEDIUM
    }
    
    override fun animateAdd(holder: RecyclerView.ViewHolder): Boolean {
        // Configurar estado inicial para animación de entrada
        holder.itemView.apply {
            alpha = 0f
            scaleX = 0.8f
            scaleY = 0.8f
            translationY = 100f
        }
        
        // Animar entrada
        holder.itemView.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .translationY(0f)
            .setDuration(addDuration)
            .setInterpolator(AnimationManager.FAST_OUT_SLOW_IN)
            .withEndAction {
                dispatchAddFinished(holder)
            }
            .start()
        
        return true
    }
    
    override fun animateRemove(holder: RecyclerView.ViewHolder): Boolean {
        // Animar salida
        holder.itemView.animate()
            .alpha(0f)
            .scaleX(0.8f)
            .scaleY(0.8f)
            .translationX(holder.itemView.width.toFloat())
            .setDuration(removeDuration)
            .setInterpolator(AnimationManager.FAST_OUT_SLOW_IN)
            .withEndAction {
                dispatchRemoveFinished(holder)
            }
            .start()
        
        return true
    }
    
    override fun animateChange(
        oldHolder: RecyclerView.ViewHolder,
        newHolder: RecyclerView.ViewHolder,
        fromLeft: Int,
        fromTop: Int,
        toLeft: Int,
        toTop: Int
    ): Boolean {
        
        if (oldHolder == newHolder) {
            // Mismo ViewHolder, animar cambio in-place
            animateChangeInPlace(oldHolder)
        } else {
            // Diferentes ViewHolders, animar transición
            animateChangeTransition(oldHolder, newHolder)
        }
        
        return true
    }
    
    private fun animateChangeInPlace(holder: RecyclerView.ViewHolder) {
        // Animación de "pulse" para indicar cambio
        holder.itemView.animate()
            .scaleX(1.05f)
            .scaleY(1.05f)
            .setDuration(changeDuration / 2)
            .setInterpolator(AnimationManager.FAST_OUT_SLOW_IN)
            .withEndAction {
                holder.itemView.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(changeDuration / 2)
                    .setInterpolator(AnimationManager.FAST_OUT_SLOW_IN)
                    .withEndAction {
                        dispatchChangeFinished(holder, true)
                    }
                    .start()
            }
            .start()
    }
    
    private fun animateChangeTransition(
        oldHolder: RecyclerView.ViewHolder,
        newHolder: RecyclerView.ViewHolder
    ) {
        // Animar salida del viejo holder
        oldHolder.itemView.animate()
            .alpha(0f)
            .scaleX(0.8f)
            .scaleY(0.8f)
            .setDuration(changeDuration / 2)
            .setInterpolator(AnimationManager.FAST_OUT_SLOW_IN)
            .withEndAction {
                dispatchChangeFinished(oldHolder, true)
            }
            .start()
        
        // Configurar estado inicial del nuevo holder
        newHolder.itemView.apply {
            alpha = 0f
            scaleX = 0.8f
            scaleY = 0.8f
        }
        
        // Animar entrada del nuevo holder con delay
        newHolder.itemView.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(changeDuration / 2)
            .setStartDelay(changeDuration / 4)
            .setInterpolator(AnimationManager.FAST_OUT_SLOW_IN)
            .withEndAction {
                dispatchChangeFinished(newHolder, false)
            }
            .start()
    }
    
    override fun animateMove(
        holder: RecyclerView.ViewHolder,
        fromX: Int,
        fromY: Int,
        toX: Int,
        toY: Int
    ): Boolean {
        
        val deltaX = toX - fromX
        val deltaY = toY - fromY
        
        if (deltaX == 0 && deltaY == 0) {
            dispatchMoveFinished(holder)
            return false
        }
        
        // Configurar posición inicial
        holder.itemView.translationX = -deltaX.toFloat()
        holder.itemView.translationY = -deltaY.toFloat()
        
        // Animar movimiento
        holder.itemView.animate()
            .translationX(0f)
            .translationY(0f)
            .setDuration(moveDuration)
            .setInterpolator(AnimationManager.FAST_OUT_SLOW_IN)
            .withEndAction {
                dispatchMoveFinished(holder)
            }
            .start()
        
        return true
    }
    
    override fun endAnimation(item: RecyclerView.ViewHolder) {
        super.endAnimation(item)
        
        // Resetear propiedades de la vista
        item.itemView.apply {
            animate().cancel()
            alpha = 1f
            scaleX = 1f
            scaleY = 1f
            translationX = 0f
            translationY = 0f
        }
    }
    
    override fun endAnimations() {
        super.endAnimations()
        
        // Limpiar cualquier animación pendiente
        // Este método se llama cuando se necesita cancelar todas las animaciones
    }
}

/**
 * Extension function para aplicar el animador personalizado a un RecyclerView
 */
fun RecyclerView.setCustomItemAnimator() {
    itemAnimator = RecyclerViewAnimator()
}

/**
 * Extension function para animar la aparición inicial de items en RecyclerView
 */
fun RecyclerView.animateItemsOnFirstLoad(
    animationType: AnimationManager.StaggerType = AnimationManager.StaggerType.SLIDE_IN_FROM_BOTTOM,
    staggerDelay: Long = AnimationManager.DELAY_SHORT
) {
    // Esperar a que el RecyclerView se layout
    post {
        val visibleItems = mutableListOf<View>()
        
        for (i in 0 until childCount) {
            visibleItems.add(getChildAt(i))
        }
        
        if (visibleItems.isNotEmpty()) {
            AnimationManager.staggeredListAnimation(
                views = visibleItems,
                animationType = animationType,
                baseDuration = AnimationManager.DURATION_MEDIUM,
                staggerDelay = staggerDelay
            )
        }
    }
}