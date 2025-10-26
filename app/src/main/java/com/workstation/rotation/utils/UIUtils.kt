package com.workstation.rotation.utils

import android.content.Context
import android.graphics.Color
import android.view.View
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar

/**
 * Utility class for common UI operations.
 */
object UIUtils {
    
    /**
     * Shows a short toast message.
     */
    fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
    
    /**
     * Shows a long toast message.
     */
    fun showLongToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
    
    /**
     * Shows a success snackbar.
     */
    fun showSuccessSnackbar(view: View, message: String) {
        Snackbar.make(view, message, Snackbar.LENGTH_SHORT)
            .setBackgroundTint(Color.parseColor(Constants.COLOR_HIGH_AVAILABILITY))
            .show()
    }
    
    /**
     * Shows an error snackbar.
     */
    fun showErrorSnackbar(view: View, message: String) {
        Snackbar.make(view, message, Snackbar.LENGTH_LONG)
            .setBackgroundTint(Color.parseColor(Constants.COLOR_LOW_AVAILABILITY))
            .show()
    }
    
    /**
     * Shows a warning snackbar.
     */
    fun showWarningSnackbar(view: View, message: String) {
        Snackbar.make(view, message, Snackbar.LENGTH_LONG)
            .setBackgroundTint(Color.parseColor(Constants.COLOR_MEDIUM_AVAILABILITY))
            .show()
    }
    
    /**
     * Gets color based on availability percentage.
     */
    fun getAvailabilityColor(percentage: Int): Int {
        return when {
            percentage >= 80 -> Color.parseColor(Constants.COLOR_HIGH_AVAILABILITY)
            percentage >= 50 -> Color.parseColor(Constants.COLOR_MEDIUM_AVAILABILITY)
            else -> Color.parseColor(Constants.COLOR_LOW_AVAILABILITY)
        }
    }
    
    /**
     * Animates view visibility with fade effect.
     */
    fun animateVisibility(view: View, visible: Boolean) {
        val alpha = if (visible) 1f else 0f
        val visibility = if (visible) View.VISIBLE else View.GONE
        
        view.animate()
            .alpha(alpha)
            .setDuration(Constants.ANIMATION_DURATION_MEDIUM)
            .withEndAction {
                view.visibility = visibility
            }
            .start()
    }
}