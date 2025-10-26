package com.workstation.rotation

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.workstation.rotation.databinding.ActivityMainBinding

/**
 * Actividad principal del sistema de rotación.
 * Proporciona navegación a las diferentes secciones de la aplicación.
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
     * Configura la interfaz de usuario y los listeners de los botones.
     */
    private fun setupUI() {
        binding.apply {
            btnWorkstations.setOnClickListener {
                startActivity(Intent(this@MainActivity, WorkstationActivity::class.java))
            }
            
            btnWorkers.setOnClickListener {
                startActivity(Intent(this@MainActivity, WorkerActivity::class.java))
            }
            
            btnRotation.setOnClickListener {
                startActivity(Intent(this@MainActivity, RotationActivity::class.java))
            }
        }
    }
}