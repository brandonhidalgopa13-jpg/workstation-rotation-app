package com.workstation.rotation.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.workstation.rotation.domain.models.WorkerModel
import com.workstation.rotation.presentation.viewmodels.WorkerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkersScreen(viewModel: WorkerViewModel) {
    val workers by viewModel.workers.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    var showAddDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Trabajadores") }
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
                isLoading && workers.isEmpty() -> {
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
                workers.isEmpty() -> {
                    Text(
                        "No hay trabajadores",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(workers) { worker ->
                            WorkerItem(
                                worker = worker,
                                onDelete = { viewModel.deleteWorker(worker.id) }
                            )
                        }
                    }
                }
            }
        }
    }
    
    if (showAddDialog) {
        AddWorkerDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { worker ->
                viewModel.addWorker(worker)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun WorkerItem(
    worker: WorkerModel,
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
                    text = worker.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "ID: ${worker.employeeId}",
                    style = MaterialTheme.typography.bodySmall
                )
                if (!worker.isActive) {
                    Text(
                        text = "Inactivo",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            IconButton(onClick = onDelete) {
                Text("🗑️")
            }
        }
    }
}

@Composable
fun AddWorkerDialog(
    onDismiss: () -> Unit,
    onConfirm: (WorkerModel) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var employeeId by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Agregar Trabajador") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = employeeId,
                    onValueChange = { employeeId = it },
                    label = { Text("ID Empleado") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && employeeId.isNotBlank()) {
                        onConfirm(WorkerModel(name = name, employeeId = employeeId))
                    }
                },
                enabled = name.isNotBlank() && employeeId.isNotBlank()
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
