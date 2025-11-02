package com.workstation.rotation

import android.content.Intent
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.workstation.rotation.databinding.ActivityMainBinding

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
    }
    
    /**
     * Configura la interfaz de usuario y los listeners de los botones con feedback táctil.
     */
    private fun setupUI() {
        binding.apply {
            btnWorkstations.setOnClickListener {
                provideTactileFeedback()
                startActivity(Intent(this@MainActivity, WorkstationActivity::class.java))
            }
            
            btnWorkers.setOnClickListener {
                provideTactileFeedback()
                startActivity(Intent(this@MainActivity, WorkerActivity::class.java))
            }
            
            btnRotation.setOnClickListener {
                provideTactileFeedback()
                startActivity(Intent(this@MainActivity, SqlRotationActivity::class.java))
            }
            
            btnSettings.setOnClickListener {
                provideTactileFeedback()
                startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
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