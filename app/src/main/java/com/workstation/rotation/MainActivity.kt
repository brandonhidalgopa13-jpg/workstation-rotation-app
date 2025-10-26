package com.workstation.rotation

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.workstation.rotation.databinding.ActivityMainBinding
import com.workstation.rotation.tutorial.TutorialManager
import com.workstation.rotation.tutorial.TutorialStep

/**
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 🏠 ACTIVIDAD PRINCIPAL - CENTRO DE NAVEGACIÓN DEL SISTEMA
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 
 * 📋 FUNCIONES DE ESTA ACTIVIDAD:
 * 
 * 🎯 1. PUNTO DE ENTRADA PRINCIPAL
 *    - Primera pantalla que ve el usuario al abrir la aplicación
 *    - Presenta una interfaz limpia y organizada con las opciones principales
 *    - Muestra el título y descripción del sistema de rotación
 * 
 * 🧭 2. CENTRO DE NAVEGACIÓN
 *    - Proporciona acceso directo a las 3 secciones principales del sistema:
 *      • 🏭 Estaciones de Trabajo: Gestión de estaciones y capacidades
 *      • 👥 Trabajadores: Gestión de personal y disponibilidad  
 *      • 🔄 Sistema de Rotación: Generación de rotaciones inteligentes
 * 
 * 🎨 3. INTERFAZ DE USUARIO INTUITIVA
 *    - Diseño con tarjetas (Material Cards) para cada sección
 *    - Iconos distintivos para identificación rápida
 *    - Colores diferenciados para cada módulo del sistema
 *    - Botones de acción claros y accesibles
 * 
 * 💻 4. MARCA DEL DESARROLLADOR
 *    - Incluye marca de agua discreta con el nombre del creador
 *    - Reconocimiento profesional integrado elegantemente
 * 
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 🔧 COMPONENTES TÉCNICOS:
 * 
 * • View Binding para acceso seguro a elementos de UI
 * • Navegación mediante Intents a actividades específicas
 * • Gestión adecuada del ciclo de vida de la actividad
 * • Prevención de memory leaks con limpieza de referencias
 * 
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 */
class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var tutorialManager: TutorialManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupUI()
        setupTutorial()
    }
    
    /**
     * Sets up the user interface and click listeners for navigation buttons.
     */
    private fun setupUI() {
        setupClickListeners()
        setupToolbar()
    }
    
    /**
     * Configura la toolbar con el menú de tutorial.
     */
    private fun setupToolbar() {
        // Material3 no usa ActionBar por defecto, el título se maneja en el layout
        // Si necesitamos una toolbar, se debe agregar al layout
    }
    
    /**
     * Configura el sistema de tutorial.
     */
    private fun setupTutorial() {
        try {
            tutorialManager = TutorialManager(this)
            
            // Mostrar tutorial automáticamente si es la primera vez
            if (tutorialManager.shouldShowTutorial()) {
                // Pequeño delay para que la UI se cargue completamente
                binding.root.postDelayed({
                    try {
                        startInteractiveTutorial()
                    } catch (e: Exception) {
                        // Si hay error en el tutorial, continuar sin él
                        e.printStackTrace()
                    }
                }, 500)
            }
        } catch (e: Exception) {
            // Si hay error inicializando el tutorial, continuar sin él
            e.printStackTrace()
        }
    }
    
    /**
     * Inicia el tutorial interactivo.
     */
    private fun startInteractiveTutorial() {
        tutorialManager.startTutorial(
            onStepComplete = { step ->
                handleTutorialStep(step)
            },
            onNavigate = { step ->
                navigateForTutorial(step)
            }
        )
    }
    
    /**
     * Navega a la actividad correspondiente durante el tutorial.
     */
    private fun navigateForTutorial(step: TutorialStep) {
        val targetActivity = step.getTargetActivity()
        if (targetActivity != null) {
            val intent = Intent(this, targetActivity)
            intent.putExtra("tutorial_active", true)
            intent.putExtra("tutorial_step", step.name)
            startActivity(intent)
        }
    }
    
    /**
     * Maneja cada paso del tutorial.
     */
    private fun handleTutorialStep(step: TutorialStep) {
        when (step) {
            TutorialStep.MAIN_SCREEN -> {
                // Resaltar las tarjetas principales
                highlightMainCards()
            }
            TutorialStep.WORKSTATIONS_INTRO -> {
                // Resaltar botón de estaciones
                tutorialManager.highlightView(binding.btnWorkstations)
            }
            TutorialStep.WORKERS_INTRO -> {
                // Resaltar botón de trabajadores
                tutorialManager.highlightView(binding.btnWorkers)
            }
            TutorialStep.ROTATION_INTRO -> {
                // Resaltar botón de rotación
                tutorialManager.highlightView(binding.btnRotation)
            }
            else -> {
                // Quitar todos los resaltados
                clearHighlights()
            }
        }
    }
    
    /**
     * Resalta las tarjetas principales.
     */
    private fun highlightMainCards() {
        // Resaltar los botones principales en lugar de las tarjetas
        tutorialManager.highlightView(binding.btnWorkstations)
        tutorialManager.highlightView(binding.btnWorkers)
        tutorialManager.highlightView(binding.btnRotation)
    }
    
    /**
     * Quita todos los resaltados.
     */
    private fun clearHighlights() {
        // Quitar resaltados de los botones
        tutorialManager.highlightView(binding.btnWorkstations, false)
        tutorialManager.highlightView(binding.btnWorkers, false)
        tutorialManager.highlightView(binding.btnRotation, false)
    }
    
    /**
     * Configures click listeners for the main navigation buttons.
     */
    private fun setupClickListeners() {
        with(binding) {
            btnWorkstations.setOnClickListener {
                navigateToActivity(WorkstationActivity::class.java)
            }
            
            btnWorkers.setOnClickListener {
                navigateToActivity(WorkerActivity::class.java)
            }
            
            btnRotation.setOnClickListener {
                navigateToActivity(RotationActivity::class.java)
            }
        }
    }
    
    /**
     * Helper method to navigate to a specific activity.
     * @param activityClass The class of the activity to navigate to
     */
    private fun navigateToActivity(activityClass: Class<*>) {
        startActivity(Intent(this, activityClass))
    }
    
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Solo inflar menú si tenemos ActionBar
        return if (supportActionBar != null) {
            menuInflater.inflate(R.menu.main_menu, menu)
            true
        } else {
            false
        }
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_tutorial -> {
                try {
                    tutorialManager.showTutorialSettings()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                true
            }
            R.id.action_start_tutorial -> {
                try {
                    startInteractiveTutorial()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                true
            }
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        clearHighlights()
        // Clean up binding reference to prevent memory leaks
    }
}