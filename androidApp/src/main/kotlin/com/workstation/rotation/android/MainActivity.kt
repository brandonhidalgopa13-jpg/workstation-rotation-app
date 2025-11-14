package com.workstation.rotation.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.workstation.rotation.android.di.AppContainer
import com.workstation.rotation.android.theme.WorkstationRotationTheme
import com.workstation.rotation.presentation.screens.*
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    
    private lateinit var appContainer: AppContainer
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        appContainer = AppContainer(applicationContext)
        
        setContent {
            WorkstationRotationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(appContainer)
                }
            }
        }
    }
}

@Composable
fun AppNavigation(appContainer: AppContainer) {
    val navController = rememberNavController()
    val context = androidx.compose.ui.platform.LocalContext.current
    
    NavHost(navController = navController, startDestination = "main") {
        composable("main") {
            MainScreen(
                onNavigateToWorkers = { navController.navigate("workers") },
                onNavigateToWorkstations = { navController.navigate("workstations") },
                onNavigateToRotation = { navController.navigate("rotation") },
                onNavigateToHistory = { navController.navigate("history") },
                windowSizeClass = WindowSizeClass.Compact
            )
        }
        
        composable("workers") {
            WorkersScreen(
                viewModel = appContainer.workerViewModel,
                onBack = { navController.popBackStack() },
                windowSizeClass = WindowSizeClass.Compact
            )
        }
        
        composable("workstations") {
            WorkstationsScreen(
                viewModel = appContainer.workstationViewModel,
                onBack = { navController.popBackStack() },
                windowSizeClass = WindowSizeClass.Compact
            )
        }
        
        composable("rotation") {
            RotationScreen(
                viewModel = appContainer.rotationViewModel,
                onBack = { navController.popBackStack() },
                windowSizeClass = WindowSizeClass.Compact
            )
        }
        
        composable("history") {
            HistoryScreen(
                viewModel = appContainer.historyViewModel,
                onBack = { navController.popBackStack() },
                onViewDetails = { sessionId ->
                    navController.navigate("rotation_detail/$sessionId")
                },
                windowSizeClass = WindowSizeClass.Compact
            )
        }
        
        composable(
            route = "rotation_detail/{sessionId}",
            arguments = listOf(navArgument("sessionId") { type = NavType.LongType })
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getLong("sessionId") ?: 0L
            RotationDetailScreen(
                sessionId = sessionId,
                rotationService = appContainer.rotationService,
                repository = appContainer.repository,
                onBack = { navController.popBackStack() },
                onExport = { session, assignments ->
                    // Mostrar diálogo de exportación
                    // Por ahora, solo compartir como texto
                    shareRotation(context, session, assignments, appContainer.exportService)
                },
                windowSizeClass = WindowSizeClass.Compact
            )
        }
    }
}

private fun shareRotation(
    context: android.content.Context,
    session: com.workstation.rotation.domain.models.RotationSessionModel,
    assignments: List<com.workstation.rotation.domain.models.RotationAssignmentModel>,
    exportService: com.workstation.rotation.domain.service.ExportService
) {
    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
        try {
            val content = exportService.exportToText(session, assignments)
            val sendIntent = android.content.Intent().apply {
                action = android.content.Intent.ACTION_SEND
                putExtra(android.content.Intent.EXTRA_TEXT, content)
                type = "text/plain"
            }
            val shareIntent = android.content.Intent.createChooser(sendIntent, "Compartir Rotación")
            context.startActivity(shareIntent)
        } catch (e: Exception) {
            android.widget.Toast.makeText(context, "Error al exportar: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
        }
    }
}
