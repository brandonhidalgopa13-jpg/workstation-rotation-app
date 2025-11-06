package com.workstation.rotation.security

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.workstation.rotation.MainActivity
import com.workstation.rotation.databinding.ActivityLoginBinding

/**
 * Actividad de login simplificada
 */
class LoginActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityLoginBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupUI()
    }
    
    private fun setupUI() {
        binding.btnLogin.setOnClickListener {
            performLogin()
        }
        
        binding.btnBiometricLogin.setOnClickListener {
            performBiometricLogin()
        }
    }
    
    private fun performLogin() {
        val username = binding.etUsername.text.toString()
        val password = binding.etPassword.text.toString()
        
        if (username.isNotEmpty() && password.isNotEmpty()) {
            // Simulación de login exitoso
            navigateToMainActivity()
        } else {
            Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun performBiometricLogin() {
        // Simulación de login biométrico exitoso
        navigateToMainActivity()
    }
    
    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}

data class LoginResult(
    val isSuccess: Boolean,
    val userRole: UserRole?,
    val errorMessage: String?
)