package com.workstation.rotation.desktop

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import androidx.compose.ui.unit.dp
import com.workstation.rotation.desktop.di.DesktopAppContainer
import com.workstation.rotation.desktop.theme.WorkstationRotationTheme
import com.workstation.rotation.presentation.screens.*

fun main() = application {
    val windowState = rememberWindowState(width = 1200.dp, height = 800.dp)
    
    Window(
        onCloseRequest = ::exitApplication,
        title = "Rotación de Estaciones",
        state = windowState
    ) {
        WorkstationRotationTheme {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                DesktopApp()
            }
        }
    }
}

@Composable
fun DesktopApp() {
    val appContainer = androidx.compose.runtime.remember { DesktopAppContainer() }
    var currentScreen by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<Screen>(Screen.Main) }
    
    when (currentScreen) {
        Screen.Main -> {
            MainScreen(
                onNavigateToWorkers = { currentScreen = Screen.Workers },
                onNavigateToWorkstations = { currentScreen = Screen.Workstations },
                onNavigateToRotation = { currentScreen = Screen.Rotation },
                onNavigateToHistory = { currentScreen = Screen.History },
                windowSizeClass = WindowSizeClass.Expanded
            )
        }
        Screen.Workers -> {
            WorkersScreen(
                viewModel = appContainer.workerViewModel,
                onBack = { currentScreen = Screen.Main },
                windowSizeClass = WindowSizeClass.Expanded
            )
        }
        Screen.Workstations -> {
            WorkstationsScreen(
                viewModel = appContainer.workstationViewModel,
                onBack = { currentScreen = Screen.Main },
                windowSizeClass = WindowSizeClass.Expanded
            )
        }
        Screen.Rotation -> {
            RotationScreen(
                viewModel = appContainer.rotationViewModel,
                onBack = { currentScreen = Screen.Main },
                windowSizeClass = WindowSizeClass.Expanded
            )
        }
        Screen.History -> {
            HistoryScreen(
                viewModel = appContainer.historyViewModel,
                onBack = { currentScreen = Screen.Main },
                onViewDetails = { sessionId ->
                    currentScreen = Screen.RotationDetail(sessionId)
                },
                windowSizeClass = WindowSizeClass.Expanded
            )
        }
        is Screen.RotationDetail -> {
            RotationDetailScreen(
                sessionId = (currentScreen as Screen.RotationDetail).sessionId,
                rotationService = appContainer.rotationService,
                repository = appContainer.repository,
                onBack = { currentScreen = Screen.History },
                onExport = { session, assignments ->
                    saveRotationToFile(session, assignments, appContainer.exportService)
                },
                windowSizeClass = WindowSizeClass.Expanded
            )
        }
    }
}

private fun saveRotationToFile(
    session: com.workstation.rotation.domain.models.RotationSessionModel,
    assignments: List<com.workstation.rotation.domain.models.RotationAssignmentModel>,
    exportService: com.workstation.rotation.domain.service.ExportService
) {
    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
        try {
            val content = exportService.exportToText(session, assignments)
            val fileName = "rotacion_${session.name.replace(" ", "_")}.txt"
            val file = java.io.File(System.getProperty("user.home"), fileName)
            file.writeText(content)
            println("Rotación exportada a: ${file.absolutePath}")
        } catch (e: Exception) {
            println("Error al exportar: ${e.message}")
        }
    }
}

sealed class Screen {
    object Main : Screen()
    object Workers : Screen()
    object Workstations : Screen()
    object Rotation : Screen()
    object History : Screen()
    data class RotationDetail(val sessionId: Long) : Screen()
}
