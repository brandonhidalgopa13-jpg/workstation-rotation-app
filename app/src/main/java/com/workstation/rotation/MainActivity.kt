package com.workstation.rotation

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.workstation.rotation.databinding.ActivityMainBinding

/**
 * Main activity that serves as the entry point for the Workstation Rotation app.
 * Provides navigation to the three main sections: Workstations, Workers, and Rotation System.
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