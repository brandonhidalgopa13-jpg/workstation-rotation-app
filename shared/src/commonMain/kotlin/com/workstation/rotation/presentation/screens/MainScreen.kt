package com.workstation.rotation.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onNavigateToWorkers: () -> Unit,
    onNavigateToWorkstations: () -> Unit,
    onNavigateToRotation: () -> Unit,
    onNavigateToHistory: () -> Unit,
    windowSizeClass: WindowSizeClass = WindowSizeClass.Compact
) {
    val isExpandedScreen = windowSizeClass == WindowSizeClass.Expanded
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Rotación de Estaciones") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        if (isExpandedScreen) {
            // Layout para PC/Tablet (horizontal)
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                MainMenuCard(
                    title = "Trabajadores",
                    icon = Icons.Default.Person,
                    onClick = onNavigateToWorkers,
                    modifier = Modifier.weight(1f)
                )
                MainMenuCard(
                    title = "Estaciones",
                    icon = Icons.Default.Build,
                    onClick = onNavigateToWorkstations,
                    modifier = Modifier.weight(1f)
                )
                MainMenuCard(
                    title = "Nueva Rotación",
                    icon = Icons.Default.Refresh,
                    onClick = onNavigateToRotation,
                    modifier = Modifier.weight(1f)
                )
                MainMenuCard(
                    title = "Historial",
                    icon = Icons.Default.DateRange,
                    onClick = onNavigateToHistory,
                    modifier = Modifier.weight(1f)
                )
            }
        } else {
            // Layout para móvil (vertical)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                MainMenuCard(
                    title = "Trabajadores",
                    icon = Icons.Default.Person,
                    onClick = onNavigateToWorkers,
                    modifier = Modifier.fillMaxWidth()
                )
                MainMenuCard(
                    title = "Estaciones",
                    icon = Icons.Default.Build,
                    onClick = onNavigateToWorkstations,
                    modifier = Modifier.fillMaxWidth()
                )
                MainMenuCard(
                    title = "Nueva Rotación",
                    icon = Icons.Default.Refresh,
                    onClick = onNavigateToRotation,
                    modifier = Modifier.fillMaxWidth()
                )
                MainMenuCard(
                    title = "Historial",
                    icon = Icons.Default.DateRange,
                    onClick = onNavigateToHistory,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun MainMenuCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(120.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

enum class WindowSizeClass {
    Compact,  // Móvil
    Medium,   // Tablet
    Expanded  // PC/Desktop
}
