package com.workstation.rotation

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.workstation.rotation.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupClickListeners()
    }
    
    private fun setupClickListeners() {
        binding.btnWorkstations.setOnClickListener {
            startActivity(Intent(this, WorkstationActivity::class.java))
        }
        
        binding.btnWorkers.setOnClickListener {
            startActivity(Intent(this, WorkerActivity::class.java))
        }
        
        binding.btnRotation.setOnClickListener {
            startActivity(Intent(this, RotationActivity::class.java))
        }
    }
}