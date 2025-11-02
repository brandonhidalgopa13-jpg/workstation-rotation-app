package com.workstation.rotation

import android.content.Intent
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.workstation.rotation.databinding.ActivityMainBinding
import com.workstation.rotation.animations.navigateToMainSection
import com.workstation.rotation.animations.openSettings
import com.workstation.rotation.animations.openDetails
import com.workstation.rotation.animations.AnimationManager
import com.workstation.rotation.animations.slideInChildrenFromBottom

/**
 * Actividad principal del sistema de rotación.
 * Proporciona navegación a las diferentes secciones de la aplicación con feedback táctil mejorado.
 */
class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var vibrator: Vibrator
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Verificar si debe mostrar onboarding
        if (OnboardingActivity.shouldShowOnboarding(this)) {
            startActivity(Intent(this, OnboardingActivity::class.java))
            finish()
            return
        }
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Inicializar vibrador para feedback táctil
        vibrator = ContextCompat.getSystemService(this, Vibrator::class.java) ?: return
        
        setupUI()
        setupAnimations()
    }
    
    /**
     * Configura la interfaz de usuario y los listeners de los botones con feedback táctil.
     */
    private fun setupUI() {
        binding.apply {
            btnWorkstations?.setOnClickListener {
                provideTactileFeedback()
                btnWorkstations?.let { AnimationManager.clickFeedback(it) }
                navigateToMainSection(Intent(this@MainActivity, WorkstationActivity::class.java))
            }
            
            btnWorkers?.setOnClickListener {
                provideTactileFeedback()
                btnWorkers?.let { AnimationManager.clickFeedback(it) }
                navigateToMainSection(Intent(this@MainActivity, WorkerActivity::class.java))
            }
            
            btnRotation?.setOnClickListener {
                provideTactileFeedback()
                btnRotation?.let { AnimationManager.clickFeedback(it) }
                openDetails(Intent(this@MainActivity, SqlRotationActivity::class.java))
            }
            
            // Acceso al dashboard ejecutivo (long press)
            btnSettings?.setOnLongClickListener {
                provideTactileFeedback()
                openDetails(Intent(this@MainActivity, com.workstation.rotation.dashboard.ExecutiveDashboardActivity::class.java))
                true
            }
            
            // Acceso a Analytics Avanzados (doble tap en History)
            var lastHistoryClickTime = 0L
            btnHistory?.setOnClickListener {
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastHistoryClickTime < 500) {
                    // Doble tap detectado - abrir Analytics Avanzados
                    provideTactileFeedback()
                    btnHistory?.let { AnimationManager.clickFeedback(it) }
                    openDetails(Intent(this@MainActivity, com.workstation.rotation.analytics.AdvancedAnalyticsActivity::class.java))
                } else {
                    // Click simple - abrir historial normal
                    provideTactileFeedback()
                    btnHistory?.let { AnimationManager.clickFeedback(it) }
                    openDetails(Intent(this@MainActivity, RotationHistoryActivity::class.java))
                }
                lastHistoryClickTime = currentTime
            }
            
            btnSettings?.setOnClickListener {
                provideTactileFeedback()
                btnSettings?.let { AnimationManager.clickFeedback(it) }
                openSettings(Intent(this@MainActivity, SettingsActivity::class.java))
            }
        }
    }
    
    /**
     * Configura las animaciones de entrada para los elementos de la UI
     */
    private fun setupAnimations() {
        binding.apply {
            // Animar las cards principales con stagger effect
            val mainCards = listOfNotNull(
                btnWorkstations?.parent as? android.view.View,
                btnWorkers?.parent as? android.view.View,
                btnRotation?.parent as? android.view.View,
                btnHistory?.parent as? android.view.View,
                btnSettings?.parent as? android.view.View
            )
            
            // Animación staggered para las cards principales
            if (mainCards.isNotEmpty()) {
                AnimationManager.staggeredListAnimation(
                    views = mainCards,
                    animationType = AnimationManager.StaggerType.SLIDE_IN_FROM_BOTTOM,
                    baseDuration = AnimationManager.DURATION_MEDIUM,
                    staggerDelay = AnimationManager.DELAY_MEDIUM
                )
            }
        }
    }
    

    
    /**
     * Proporciona feedback táctil al usuario cuando interactúa con los botones.
     */
    private fun provideTactileFeedback() {
        if (::vibrator.isInitialized && vibrator.hasVibrator()) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(50)
            }
        }
    }
}