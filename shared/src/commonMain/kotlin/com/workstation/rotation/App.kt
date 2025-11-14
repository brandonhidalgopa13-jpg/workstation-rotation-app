package com.workstation.rotation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.workstation.rotation.presentation.screens.WorkersScreen
import com.workstation.rotation.presentation.screens.WorkstationsScreen
import com.workstation.rotation.presentation.viewmodels.WorkerViewModel
import com.workstation.rotation.presentation.viewmodels.WorkstationViewModel

enum class Screen {
    WORKERS, WORKSTATIONS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(
    workerViewModel: WorkerViewModel,
    workstationViewModel: WorkstationViewModel
) {
    var currentScreen by remember { mutableStateOf(Screen.WORKERS) }
    
    MaterialTheme {
        Scaffold(
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentScreen == Screen.WORKERS,
                        onClick = { currentScreen = Screen.WORKERS },
                        icon = { Text("👷") },
                        label = { Text("Trabajadores") }
                    )
                    NavigationBarItem(
                        selected = currentScreen == Screen.WORKSTATIONS,
                        onClick = { currentScreen = Screen.WORKSTATIONS },
                        icon = { Text("🏭") },
                        label = { Text("Estaciones") }
                    )
                }
            }
        ) { padding ->
            Box(modifier = Modifier.padding(padding)) {
                when (currentScreen) {
                    Screen.WORKERS -> WorkersScreen(workerViewModel)
                    Screen.WORKSTATIONS -> WorkstationsScreen(workstationViewModel)
                }
            }
        }
    }
}
