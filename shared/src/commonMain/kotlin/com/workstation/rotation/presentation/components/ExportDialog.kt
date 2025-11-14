package com.workstation.rotation.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.workstation.rotation.domain.models.RotationSessionModel
import com.workstation.rotation.domain.models.RotationAssignmentModel
import com.workstation.rotation.domain.service.ExportService
import kotlinx.coroutines.launch

enum class ExportFormat {
    TEXT, CSV, MARKDOWN
}

@Composable
fun ExportDialog(
    session: RotationSessionModel,
    assignments: List<RotationAssignmentModel>,
    exportService: ExportService,
    onDismiss: () -> Unit,
    onExported: (String, ExportFormat) -> Unit
) {
    var selectedFormat by remember { mutableStateOf(ExportFormat.TEXT) }
    var isExporting by remember { mutableStateOf(false) }
    var exportedContent by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    
    val scope = rememberCoroutineScope()
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Exportar Rotación") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Selecciona el formato de exportación:",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                // Opciones de formato
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ExportFormatOption(
                        format = ExportFormat.TEXT,
                        selected = selectedFormat == ExportFormat.TEXT,
                        onClick = { selectedFormat = ExportFormat.TEXT },
                        icon = Icons.Default.Info,
                        title = "Texto Plano",
                        description = "Formato legible para imprimir"
                    )
                    
                    ExportFormatOption(
                        format = ExportFormat.CSV,
                        selected = selectedFormat == ExportFormat.CSV,
                        onClick = { selectedFormat = ExportFormat.CSV },
                        icon = Icons.Default.List,
                        title = "CSV",
                        description = "Para Excel o Google Sheets"
                    )
                    
                    ExportFormatOption(
                        format = ExportFormat.MARKDOWN,
                        selected = selectedFormat == ExportFormat.MARKDOWN,
                        onClick = { selectedFormat = ExportFormat.MARKDOWN },
                        icon = Icons.Default.Edit,
                        title = "Markdown",
                        description = "Para documentación"
                    )
                }
                
                if (error != null) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            error!!,
                            modifier = Modifier.padding(8.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                
                if (exportedContent != null) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                "¡Exportado exitosamente!",
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    scope.launch {
                        try {
                            isExporting = true
                            error = null
                            
                            val content = when (selectedFormat) {
                                ExportFormat.TEXT -> exportService.exportToText(session, assignments)
                                ExportFormat.CSV -> exportService.exportToCSV(session, assignments)
                                ExportFormat.MARKDOWN -> exportService.exportToMarkdown(session, assignments)
                            }
                            
                            exportedContent = content
                            onExported(content, selectedFormat)
                        } catch (e: Exception) {
                            error = e.message ?: "Error al exportar"
                        } finally {
                            isExporting = false
                        }
                    }
                },
                enabled = !isExporting
            ) {
                if (isExporting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(if (isExporting) "Exportando..." else "Exportar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}

@Composable
fun ExportFormatOption(
    format: ExportFormat,
    selected: Boolean,
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (selected) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (selected) 
                    MaterialTheme.colorScheme.onPrimaryContainer 
                else 
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall,
                    color = if (selected) 
                        MaterialTheme.colorScheme.onPrimaryContainer 
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    description,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (selected) 
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
            
            if (selected) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}
