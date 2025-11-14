package com.workstation.rotation.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.workstation.rotation.domain.models.RotationSessionModel
import com.workstation.rotation.domain.models.RotationAssignmentModel
import com.workstation.rotation.domain.models.WorkerModel
import com.workstation.rotation.domain.models.WorkstationModel
import com.workstation.rotation.domain.repository.RotationRepository
import com.workstation.rotation.domain.service.RotationService
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RotationDetailScreen(
    sessionId: Long,
    rotationService: RotationService,
    repository: RotationRepository,
    onBack: () -> Unit,
    onExport: (RotationSessionModel, List<RotationAssignmentModel>) -> Unit,
    windowSizeClass: WindowSizeClass = WindowSizeClass.Compact
) {
    var session by remember { mutableStateOf<RotationSessionModel?>(null) }
    var assignments by remember { mutableStateOf<List<RotationAssignmentModel>>(emptyList()) }
    var workers by remember { mutableStateOf<List<WorkerModel>>(emptyList()) }
    var workstations by remember { mutableStateOf<List<WorkstationModel>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(sessionId) {
        scope.launch {
            try {
                isLoading = true
                val (sessionData, assignmentsData) = rotationService.getSessionDetails(sessionId)
                session = sessionData
                assignments = assignmentsData
                
                // Cargar trabajadores y estaciones
                repository.getAllWorkers().collect { workerList ->
                    workers = workerList
                }
                repository.getAllWorkstations().collect { workstationList ->
                    workstations = workstationList
                }
            } catch (e: Exception) {
                error = e.message
            } finally {
                isLoading = false
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(session?.name ?: "Detalle de Rotación") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                },
                actions = {
                    if (session != null) {
                        IconButton(
                            onClick = { onExport(session!!, assignments) }
                        ) {
                            Icon(Icons.Default.Share, "Exportar")
                        }
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
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                error != null -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Error: $error")
                        Button(onClick = onBack) {
                            Text("Volver")
                        }
                    }
                }
                session == null -> {
                    Text(
                        "Rotación no encontrada",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    RotationDetailContent(
                        session = session!!,
                        assignments = assignments,
                        workers = workers,
                        workstations = workstations,
                        windowSizeClass = windowSizeClass
                    )
                }
            }
        }
    }
}

@Composable
fun RotationDetailContent(
    session: RotationSessionModel,
    assignments: List<RotationAssignmentModel>,
    workers: List<WorkerModel>,
    workstations: List<WorkstationModel>,
    windowSizeClass: WindowSizeClass
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Información de la sesión
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            session.name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        if (session.isActive) {
                            Surface(
                                color = MaterialTheme.colorScheme.primary,
                                shape = MaterialTheme.shapes.small
                            ) {
                                Text(
                                    "ACTIVA",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }
                    
                    Divider()
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        InfoItem(
                            label = "Fecha de creación",
                            value = formatDate(session.createdAt)
                        )
                        InfoItem(
                            label = "Intervalo",
                            value = "${session.rotationIntervalMinutes} min"
                        )
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        InfoItem(
                            label = "Total asignaciones",
                            value = assignments.size.toString()
                        )
                        InfoItem(
                            label = "Rotaciones",
                            value = (assignments.maxOfOrNull { it.rotationOrder } ?: 0).plus(1).toString()
                        )
                    }
                }
            }
        }
        
        // Estadísticas
        item {
            Card {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Estadísticas",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Divider()
                    
                    val workerAssignments = assignments.groupBy { it.workerId }
                    val workstationAssignments = assignments.groupBy { it.workstationId }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatItem(
                            icon = Icons.Default.Person,
                            label = "Trabajadores",
                            value = workerAssignments.size.toString()
                        )
                        StatItem(
                            icon = Icons.Default.Build,
                            label = "Estaciones",
                            value = workstationAssignments.size.toString()
                        )
                    }
                    
                    // Trabajador con más asignaciones
                    val topWorker = workerAssignments.maxByOrNull { it.value.size }
                    if (topWorker != null) {
                        val worker = workers.find { it.id == topWorker.key }
                        Text(
                            "Trabajador más asignado: ${worker?.name ?: "Desconocido"} (${topWorker.value.size} veces)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        
        // Tabla de asignaciones
        item {
            Card {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Asignaciones por Rotación",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Divider()
                }
            }
        }
        
        // Agrupar por rotationOrder
        val groupedAssignments = assignments.groupBy { it.rotationOrder }.toSortedMap()
        
        items(groupedAssignments.keys.toList()) { rotationOrder ->
            val rotationAssignments = groupedAssignments[rotationOrder] ?: emptyList()
            
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Rotación #${rotationOrder + 1}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    
                    Divider()
                    
                    rotationAssignments.forEach { assignment ->
                        val worker = workers.find { it.id == assignment.workerId }
                        val workstation = workstations.find { it.id == assignment.workstationId }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    worker?.name ?: "Desconocido",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            
                            Icon(
                                Icons.Default.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    Icons.Default.Build,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    workstation?.name ?: "Desconocida",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InfoItem(label: String, value: String) {
    Column {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun StatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Column {
            Text(
                value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
