package com.workstation.rotation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun App() {
    var clickCount by remember { mutableStateOf(0) }
    var showMessage by remember { mutableStateOf(false) }
    
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Workstation Rotation",
                    style = MaterialTheme.typography.headlineLarge
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Running on: ${getPlatform().name}",
                    style = MaterialTheme.typography.bodyLarge
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Button(onClick = { 
                    clickCount++
                    showMessage = true
                }) {
                    Text("Get Started")
                }
                
                if (showMessage) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "¡Botón presionado $clickCount ${if (clickCount == 1) "vez" else "veces"}!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "✅ La aplicación KMP funciona correctamente",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
