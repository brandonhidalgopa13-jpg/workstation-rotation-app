package com.workstation.rotation.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.workstation.rotation.domain.models.RotationAssignmentModel
import com.workstation.rotation.domain.models.WorkerModel
import com.workstation.rotation.domain.models.WorkstationModel
import com.workstation.rotation.presentation.viewmodels.RotationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RotationScreen(
    viewModel: RotationViewModel,
    onBack: () -> Unit,
    windowSizeClass: WindowSizeClass = WindowSizeClass.Compact
) {
    val workers by viewModel.workers.collectAsState()
    val workstations by viewModel.workstations.collectAsState()
    val rotationResult by viewModel.rotationResult.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val error by viewModel.error.collectAsState()
    
    var sessionName by remember { mutableStateOf("Rotación ${System.currentTimeMillis()}") }
    var intervalMinutes by remember { mutableStateOf("60") }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nueva Rotación") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (rotationResult != null) {
                // Mostrar resultado
                RotationResultView(
                    result = rotationResult!!,
                    onClose = { viewModel.clearResult() },
                    windowSizeClass = windowSizeClass
                )
            } else {
                // Formulario de configuración
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Información
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Column {
                                Text(
                                    "Trabajadores activos: ${workers.size}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    "Estaciones activas: ${workstations.size}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                    
                    // Configuración
                    OutlinedTextField(
                        value = sessionName,
                        onValueChange = { sessionName = it },
                        label = { Text("Nombre de la Sesión") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    OutlinedTextField(
                        value = intervalMinutes,
                        onValueChange = { if (it.all { char -> char.isDigit() }) intervalMinutes = it },
                        label = { Text("Intervalo (minutos)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    // Error
                    if (error != null) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Text(
                                    error!!,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                    
                    // Botón generar
                    Button(
                        onClick = {
                            val interval = intervalMinutes.toIntOrNull() ?: 60
                            viewModel.generateRotation(sessionName, interval)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isGenerating && workers.isNotEmpty() && workstations.isNotEmpty()
                    ) {
                        if (isGenerating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(if (isGenerating) "Generando..." else "Generar Rotación")
                    }
                    
                    // Advertencias
                    if (workers.isEmpty()) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Text(
                                "⚠️ No hay trabajadores activos. Agrega trabajadores primero.",
                                modifier = Modifier.padding(16.dp),
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                    
                    if (workstations.isEmpty()) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Text(
                                "⚠️ No hay estaciones activas. Agrega estaciones primero.",
                                modifier = Modifier.padding(16.dp),
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RotationResultView(
    result: com.workstation.rotation.presentation.viewmodels.RotationResult,
    onClose: () -> Unit,
    windowSizeClass: WindowSizeClass
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        result.session.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Intervalo: ${result.session.rotationIntervalMinutes} minutos",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "Asignaciones: ${result.assignments.size}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, "Cerrar")
                }
            }
        }
        
        // Tabla de rotación
        Card(modifier = Modifier.weight(1f)) {
            LazyColumn(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Header de tabla
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "Rotación",
                            modifier = Modifier.weight(1f),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Trabajador",
                            modifier = Modifier.weight(2f),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Estación",
                            modifier = Modifier.weight(2f),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                // Agrupar por rotationOrder
                val groupedAssignments = result.assignments.groupBy { it.rotationOrder }
                
                items(groupedAssignments.keys.sorted()) { rotationOrder ->
                    val assignments = groupedAssignments[rotationOrder] ?: emptyList()
                    
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.outline,
                                MaterialTheme.shapes.small
                            )
                    ) {
                        assignments.forEach { assignment ->
                            val worker = result.workers.find { it.id == assignment.workerId }
                            val workstation = result.workstations.find { it.id == assignment.workstationId }
                            
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "#${rotationOrder + 1}",
                                    modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    worker?.name ?: "Desconocido",
                                    modifier = Modifier.weight(2f),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    workstation?.name ?: "Desconocida",
                                    modifier = Modifier.weight(2f),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            
                            if (assignment != assignments.last()) {
                                Divider()
                            }
                        }
                    }
                }
            }
        }
        
        // Botones de acción
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onClose,
                modifier = Modifier.weight(1f)
            ) {
                Text("Cerrar")
            }
            Button(
                onClick = { /* TODO: Exportar */ },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Share, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Exportar")
            }
        }
    }
}
