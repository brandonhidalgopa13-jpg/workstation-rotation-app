package com.workstation.rotation.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.workstation.rotation.domain.models.WorkstationModel
import com.workstation.rotation.presentation.viewmodels.WorkstationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkstationsScreen(viewModel: WorkstationViewModel) {
    val workstations by viewModel.workstations.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    var showAddDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Estaciones") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Text("+", style = MaterialTheme.typography.headlineMedium)
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                isLoading && workstations.isEmpty() -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                error != null -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Error: $error")
                        Button(onClick = { viewModel.clearError() }) {
                            Text("Reintentar")
                        }
                    }
                }
                workstations.isEmpty() -> {
                    Text(
                        "No hay estaciones",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(workstations) { workstation ->
                            WorkstationItem(
                                workstation = workstation,
                                onDelete = { viewModel.deleteWorkstation(workstation.id) }
                            )
                        }
                    }
                }
            }
        }
    }
    
    if (showAddDialog) {
        AddWorkstationDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { workstation ->
                viewModel.addWorkstation(workstation)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun WorkstationItem(
    workstation: WorkstationModel,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = workstation.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Código: ${workstation.code}",
                    style = MaterialTheme.typography.bodySmall
                )
                workstation.description?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Text(
                    text = "Trabajadores requeridos: ${workstation.requiredWorkers}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            IconButton(onClick = onDelete) {
                Text("🗑️")
            }
        }
    }
}

@Composable
fun AddWorkstationDialog(
    onDismiss: () -> Unit,
    onConfirm: (WorkstationModel) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Agregar Estación") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text("Código") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción (opcional)") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && code.isNotBlank()) {
                        onConfirm(
                            WorkstationModel(
                                name = name,
                                code = code,
                                description = description.ifBlank { null }
                            )
                        )
                    }
                },
                enabled = name.isNotBlank() && code.isNotBlank()
            ) {
                Text("Agregar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
