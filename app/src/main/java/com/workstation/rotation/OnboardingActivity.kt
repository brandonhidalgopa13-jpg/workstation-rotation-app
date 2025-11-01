package com.workstation.rotation

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.workstation.rotation.adapters.OnboardingAdapter
import com.workstation.rotation.databinding.ActivityOnboardingBinding
import com.workstation.rotation.models.OnboardingPage

/**
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 🎯 ACTIVIDAD DE ONBOARDING - TUTORIAL INICIAL INTERACTIVO
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 
 * 📋 FUNCIONES DE ESTA ACTIVIDAD:
 * 
 * 🎓 TUTORIAL INTERACTIVO:
 *    - Guía paso a paso para nuevos usuarios
 *    - Explicación visual de funcionalidades principales
 *    - Navegación intuitiva con indicadores de progreso
 * 
 * 🎨 EXPERIENCIA VISUAL:
 *    - Animaciones suaves entre páginas
 *    - Iconos ilustrativos para cada función
 *    - Diseño moderno y atractivo
 * 
 * 🔧 CONFIGURACIÓN:
 *    - Se ejecuta solo en la primera instalación
 *    - Opción para saltar el tutorial
 *    - Guardado de preferencias de usuario
 * 
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 */
class OnboardingActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityOnboardingBinding
    private lateinit var onboardingAdapter: OnboardingAdapter
    private lateinit var prefs: SharedPreferences
    
    companion object {
        private const val PREFS_NAME = "onboarding_prefs"
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
        
        fun shouldShowOnboarding(context: Context): Boolean {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return !prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
        }
        
        fun markOnboardingCompleted(context: Context) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, true).apply()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        
        setupOnboarding()
        setupListeners()
        startEntranceAnimation()
    }
    
    private fun setupOnboarding() {
        val pages = createOnboardingPages()
        onboardingAdapter = OnboardingAdapter(pages)
        
        binding.viewPager.adapter = onboardingAdapter
        binding.dotsIndicator.attachTo(binding.viewPager)
        
        // Configurar ViewPager
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateNavigationButtons(position, pages.size)
                animatePageTransition()
            }
        })
        
        updateNavigationButtons(0, pages.size)
    }
    
    private fun createOnboardingPages(): List<OnboardingPage> {
        return listOf(
            OnboardingPage(
                title = "¡Bienvenido a REWS! 🎉",
                description = "Sistema de Rotación y Estaciones de Trabajo\n\nOptimiza la gestión de tu equipo con rotaciones inteligentes y herramientas profesionales.",
                iconRes = R.drawable.ic_onboarding_welcome,
                backgroundColor = R.color.background_light
            ),
            OnboardingPage(
                title = "Gestiona tu Equipo 👥",
                description = "Registra trabajadores con roles específicos:\n\n• 👨‍🏫 Entrenadores experimentados\n• 🎯 Trabajadores en entrenamiento\n• 👑 Líderes de estación\n• 📊 Control de disponibilidad",
                iconRes = R.drawable.ic_onboarding_workers,
                backgroundColor = R.color.background_light
            ),
            OnboardingPage(
                title = "Configura Estaciones 🏭",
                description = "Define tus áreas de trabajo:\n\n• 📋 Capacidades requeridas\n• 👥 Número de trabajadores\n• ⭐ Estaciones prioritarias\n• 🔧 Configuración flexible",
                iconRes = R.drawable.ic_onboarding_workstations,
                backgroundColor = R.color.background_light
            ),
            OnboardingPage(
                title = "Rotación Inteligente 🔄",
                description = "Algoritmo avanzado que considera:\n\n• 👑 Prioridad de líderes\n• 🎓 Parejas de entrenamiento\n• 📊 Disponibilidad del personal\n• 🚫 Restricciones específicas",
                iconRes = R.drawable.ic_onboarding_rotation,
                backgroundColor = R.color.background_light
            ),
            OnboardingPage(
                title = "¡Todo Listo! ✨",
                description = "Ya puedes comenzar a usar REWS:\n\n• 🏭 Crea tus estaciones\n• 👥 Registra tu equipo\n• 🔄 Genera rotaciones\n• 📊 Analiza resultados",
                iconRes = R.drawable.ic_onboarding_complete,
                backgroundColor = R.color.background_light
            )
        )
    }
    
    private fun setupListeners() {
        binding.btnNext.setOnClickListener {
            val currentItem = binding.viewPager.currentItem
            if (currentItem < onboardingAdapter.itemCount - 1) {
                binding.viewPager.currentItem = currentItem + 1
            } else {
                completeOnboarding()
            }
        }
        
        binding.btnPrevious.setOnClickListener {
            val currentItem = binding.viewPager.currentItem
            if (currentItem > 0) {
                binding.viewPager.currentItem = currentItem - 1
            }
        }
        
        binding.btnSkip.setOnClickListener {
            showSkipConfirmation()
        }
        
        binding.btnGetStarted.setOnClickListener {
            completeOnboarding()
        }
    }
    
    private fun updateNavigationButtons(position: Int, totalPages: Int) {
        // Botón anterior
        binding.btnPrevious.visibility = if (position == 0) View.INVISIBLE else View.VISIBLE
        
        // Botón siguiente/comenzar
        if (position == totalPages - 1) {
            binding.btnNext.visibility = View.GONE
            binding.btnGetStarted.visibility = View.VISIBLE
            binding.btnSkip.visibility = View.GONE
        } else {
            binding.btnNext.visibility = View.VISIBLE
            binding.btnGetStarted.visibility = View.GONE
            binding.btnSkip.visibility = View.VISIBLE
        }
        
        // Actualizar texto del botón siguiente
        binding.btnNext.text = if (position == totalPages - 2) "Finalizar" else "Siguiente"
    }
    
    private fun animatePageTransition() {
        val fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in)
        binding.dotsIndicator.startAnimation(fadeIn)
    }
    
    private fun startEntranceAnimation() {
        val slideUp = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left)
        binding.viewPager.startAnimation(slideUp)
        
        val fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in)
        binding.navigationContainer.startAnimation(fadeIn)
    }
    
    private fun showSkipConfirmation() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("⏭️ Saltar Tutorial")
            .setMessage("¿Estás seguro de que quieres saltar el tutorial?\n\nPuedes acceder a la guía más tarde desde Configuraciones.")
            .setPositiveButton("Sí, saltar") { _, _ ->
                completeOnboarding()
            }
            .setNegativeButton("Continuar tutorial", null)
            .show()
    }
    
    private fun completeOnboarding() {
        // Marcar onboarding como completado
        markOnboardingCompleted(this)
        
        // Animación de salida
        val slideOut = AnimationUtils.loadAnimation(this, android.R.anim.slide_out_right)
        binding.root.startAnimation(slideOut)
        
        // Ir a MainActivity
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        
        // Animación de transición
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }
    
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        val currentItem = binding.viewPager.currentItem
        if (currentItem > 0) {
            binding.viewPager.currentItem = currentItem - 1
        } else {
            showSkipConfirmation()
        }
    }
}