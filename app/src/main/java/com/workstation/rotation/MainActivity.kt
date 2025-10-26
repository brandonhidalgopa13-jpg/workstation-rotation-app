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
        setSupportActionBar(findViewById(androidx.appcompat.R.id.action_bar))
        supportActionBar?.title = "Sistema de Rotación"
    }
    
    /**
     * Configura el sistema de tutorial.
     */
    private fun setupTutorial() {
        tutorialManager = TutorialManager(this)
        
        // Mostrar tutorial automáticamente si es la primera vez
        if (tutorialManager.shouldShowTutorial()) {
            // Pequeño delay para que la UI se cargue completamente
            binding.root.postDelayed({
                startInteractiveTutorial()
            }, 500)
        }
    }
    
    /**
     * Inicia el tutorial interactivo.
     */
    private fun startInteractiveTutorial() {
        tutorialManager.startTutorial { step ->
            handleTutorialStep(step)
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
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_tutorial -> {
                tutorialManager.showTutorialSettings()
                true
            }
            R.id.action_start_tutorial -> {
                startInteractiveTutorial()
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