package com.workstation.rotation

import android.content.Intent
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.workstation.rotation.databinding.ActivityMainBinding
import com.workstation.rotation.animations.ActivityTransitions
import com.workstation.rotation.animations.AnimationManager
import com.workstation.rotation.security.SecurityConfig
import com.workstation.rotation.security.LoginActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Actividad principal del sistema de rotación.
 * Proporciona navegación a las diferentes secciones de la aplicación con feedback táctil mejorado.
 */
class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var vibrator: Vibrator
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Verificar si el sistema de seguridad está activado
        if (SecurityConfig.isSecurityEnabled(this)) {
            // Verificar si hay una sesión activa válida
            val hasValidSession = checkValidSession()
            if (!hasValidSession) {
                // Redirigir a LoginActivity
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
                return
            }
        }
        
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
        
        // Verificar y sincronizar capacidades si es necesario
        checkAndSyncCapabilities()
        
        setupUI()
        setupAnimations()
    }
    
    /**
     * Verifica si hay una sesión activa válida
     */
    private fun checkValidSession(): Boolean {
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val sessionToken = prefs.getString("current_session_token", null)
        
        // Si no hay token, no hay sesión
        if (sessionToken == null) {
            return false
        }
        
        // Aquí podrías validar el token con SessionManager
        // Por ahora, simplemente verificamos que exista
        return true
    }
    
    /**
     * Configura la interfaz de usuario y los listeners de los botones con feedback táctil.
     */
    private fun setupUI() {
        binding.apply {
            btnWorkstations?.setOnClickListener {
                provideTactileFeedback()
                btnWorkstations?.let { AnimationManager.clickFeedback(it) }
                startActivity(Intent(this@MainActivity, WorkstationActivity::class.java))
                ActivityTransitions.mainNavigation(this@MainActivity)
            }
            
            btnWorkers?.setOnClickListener {
                provideTactileFeedback()
                btnWorkers?.let { AnimationManager.clickFeedback(it) }
                startActivity(Intent(this@MainActivity, WorkerActivity::class.java))
                ActivityTransitions.mainNavigation(this@MainActivity)
            }
            
            btnRotation?.setOnClickListener {
                provideTactileFeedback()
                btnRotation?.let { AnimationManager.clickFeedback(it) }
                // Usar la nueva arquitectura de rotación v4.0
                startActivity(Intent(this@MainActivity, NewRotationActivity::class.java))
                ActivityTransitions.openDetails(this@MainActivity)
            }
            
            // Acceso al dashboard ejecutivo (long press)
            btnSettings?.setOnLongClickListener {
                provideTactileFeedback()
                startActivity(Intent(this@MainActivity, com.workstation.rotation.dashboard.ExecutiveDashboardActivity::class.java))
                ActivityTransitions.openDetails(this@MainActivity)
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
                    startActivity(Intent(this@MainActivity, com.workstation.rotation.analytics.AdvancedAnalyticsActivity::class.java))
                    ActivityTransitions.openDetails(this@MainActivity)
                } else {
                    // Click simple - abrir historial normal
                    provideTactileFeedback()
                    btnHistory?.let { AnimationManager.clickFeedback(it) }
                    startActivity(Intent(this@MainActivity, RotationHistoryActivity::class.java))
                    ActivityTransitions.openDetails(this@MainActivity)
                }
                lastHistoryClickTime = currentTime
            }
            
            // Acceso al Dashboard en Tiempo Real (triple tap en Settings)
            var settingsClickCount = 0
            var lastSettingsClickTime = 0L
            btnSettings?.setOnClickListener {
                val currentTime = System.currentTimeMillis()
                
                if (currentTime - lastSettingsClickTime < 500) {
                    settingsClickCount++
                } else {
                    settingsClickCount = 1
                }
                
                lastSettingsClickTime = currentTime
                
                if (settingsClickCount >= 3) {
                    // Triple tap detectado - abrir Dashboard en Tiempo Real
                    provideTactileFeedback()
                    btnSettings?.let { AnimationManager.clickFeedback(it) }
                    startActivity(Intent(this@MainActivity, com.workstation.rotation.monitoring.RealTimeDashboardActivity::class.java))
                    ActivityTransitions.openDetails(this@MainActivity)
                    settingsClickCount = 0
                } else {
                    // Click normal - abrir configuraciones después de un delay
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        if (settingsClickCount < 3) {
                            provideTactileFeedback()
                            btnSettings?.let { AnimationManager.clickFeedback(it) }
                            startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
                            ActivityTransitions.openSettings(this@MainActivity)
                        }
                    }, 600)
                }
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
     * Verifica y sincroniza las capacidades de trabajadores si es necesario.
     * Esta función se ejecuta en segundo plano al iniciar la aplicación.
     */
    private fun checkAndSyncCapabilities() {
        // Ejecutar en segundo plano
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                android.util.Log.d("MainActivity", "🔍 Verificando sincronización de capacidades...")
                
                // Verificar si es necesario sincronizar
                val needsSync = com.workstation.rotation.utils.CapabilitySyncUtil.needsSynchronization(this@MainActivity)
                
                if (needsSync) {
                    android.util.Log.d("MainActivity", "⚠️ Se detectó desincronización - iniciando sincronización automática...")
                    
                    // Ejecutar sincronización
                    val result = com.workstation.rotation.utils.CapabilitySyncUtil.syncAllWorkerCapabilities(this@MainActivity)
                    
                    // Mostrar resultado en el log
                    android.util.Log.d("MainActivity", "✅ Sincronización completada:")
                    android.util.Log.d("MainActivity", result.getSummary())
                    
                    // Mostrar notificación al usuario si hubo cambios
                    if (result.totalChanges > 0) {
                        withContext(Dispatchers.Main) {
                            android.widget.Toast.makeText(
                                this@MainActivity,
                                "✅ Se sincronizaron ${result.totalChanges} capacidades de trabajadores",
                                android.widget.Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                } else {
                    android.util.Log.d("MainActivity", "✅ Capacidades sincronizadas correctamente")
                }
                
            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "❌ Error verificando sincronización: ${e.message}", e)
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