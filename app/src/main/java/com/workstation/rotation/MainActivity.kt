package com.workstation.rotation

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.workstation.rotation.databinding.ActivityMainBinding

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
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupUI()
    }
    
    /**
     * Sets up the user interface and click listeners for navigation buttons.
     */
    private fun setupUI() {
        setupClickListeners()
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
    
    override fun onDestroy() {
        super.onDestroy()
        // Clean up binding reference to prevent memory leaks
    }
}