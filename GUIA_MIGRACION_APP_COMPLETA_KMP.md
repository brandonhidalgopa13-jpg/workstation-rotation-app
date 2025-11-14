# Guía de Migración: App Completa a KMP (Android + PC)

## Objetivo
Migrar toda la funcionalidad del módulo `:app` (Android tradicional) al proyecto KMP para que funcione tanto en Android como en PC (Desktop).

---

## Estado Actual

### ✅ Lo que ya funciona
- Proyecto KMP configurado correctamente
- Compilación exitosa de Android y Desktop
- UI básica compartida con Compose Multiplatform
- Eventos y estado reactivo funcionando

### ❌ Lo que falta
- Base de datos (Room → SQLDelight)
- ViewModels compartidos
- Lógica de negocio (Workers, Workstations, Rotations)
- Navegación entre pantallas
- Persistencia de datos

---

## ✅ Fase 1: Configurar SQLDelight (Base de Datos Multiplataforma) - COMPLETADO

**Estado:** ✅ COMPLETADO  
**Fecha:** 13 de noviembre de 2025  
**Detalles:** Ver `PASO1_SQLDELIGHT_COMPLETADO.md`

### 1.1 Crear el esquema de base de datos ✅

Archivo creado: `shared/src/commonMain/sqldelight/com/workstation/rotation/database/AppDatabase.sq`

```sql
-- Tabla de Workers
CREATE TABLE Worker (
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    name TEXT NOT NULL,
    employeeId TEXT NOT NULL UNIQUE,
    isActive INTEGER AS Boolean NOT NULL DEFAULT 1,
    photoPath TEXT,
    createdAt INTEGER NOT NULL,
    updatedAt INTEGER NOT NULL
);

-- Tabla de Workstations
CREATE TABLE Workstation (
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    name TEXT NOT NULL,
    code TEXT NOT NULL UNIQUE,
    description TEXT,
    isActive INTEGER AS Boolean NOT NULL DEFAULT 1,
    requiredWorkers INTEGER NOT NULL DEFAULT 1,
    createdAt INTEGER NOT NULL,
    updatedAt INTEGER NOT NULL
);

-- Tabla de Capacidades (relación Worker-Workstation)
CREATE TABLE WorkerWorkstationCapability (
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    workerId INTEGER NOT NULL,
    workstationId INTEGER NOT NULL,
    proficiencyLevel INTEGER NOT NULL DEFAULT 1,
    certificationDate INTEGER,
    FOREIGN KEY (workerId) REFERENCES Worker(id) ON DELETE CASCADE,
    FOREIGN KEY (workstationId) REFERENCES Workstation(id) ON DELETE CASCADE,
    UNIQUE(workerId, workstationId)
);

-- Tabla de Sesiones de Rotación
CREATE TABLE RotationSession (
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    name TEXT NOT NULL,
    startDate INTEGER NOT NULL,
    endDate INTEGER,
    isActive INTEGER AS Boolean NOT NULL DEFAULT 1,
    createdAt INTEGER NOT NULL
);

-- Tabla de Asignaciones
CREATE TABLE RotationAssignment (
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    sessionId INTEGER NOT NULL,
    workerId INTEGER NOT NULL,
    workstationId INTEGER NOT NULL,
    position INTEGER NOT NULL,
    assignedAt INTEGER NOT NULL,
    FOREIGN KEY (sessionId) REFERENCES RotationSession(id) ON DELETE CASCADE,
    FOREIGN KEY (workerId) REFERENCES Worker(id) ON DELETE CASCADE,
    FOREIGN KEY (workstationId) REFERENCES Workstation(id) ON DELETE CASCADE
);

-- Queries para Workers
getAllWorkers:
SELECT * FROM Worker ORDER BY name ASC;

getActiveWorkers:
SELECT * FROM Worker WHERE isActive = 1 ORDER BY name ASC;

getWorkerById:
SELECT * FROM Worker WHERE id = ?;

insertWorker:
INSERT INTO Worker(name, employeeId, isActive, photoPath, createdAt, updatedAt)
VALUES (?, ?, ?, ?, ?, ?);

updateWorker:
UPDATE Worker 
SET name = ?, employeeId = ?, isActive = ?, photoPath = ?, updatedAt = ?
WHERE id = ?;

deleteWorker:
DELETE FROM Worker WHERE id = ?;

-- Queries para Workstations
getAllWorkstations:
SELECT * FROM Workstation ORDER BY name ASC;

getActiveWorkstations:
SELECT * FROM Workstation WHERE isActive = 1 ORDER BY name ASC;

getWorkstationById:
SELECT * FROM Workstation WHERE id = ?;

insertWorkstation:
INSERT INTO Workstation(name, code, description, isActive, requiredWorkers, createdAt, updatedAt)
VALUES (?, ?, ?, ?, ?, ?, ?);

updateWorkstation:
UPDATE Workstation 
SET name = ?, code = ?, description = ?, isActive = ?, requiredWorkers = ?, updatedAt = ?
WHERE id = ?;

deleteWorkstation:
DELETE FROM Workstation WHERE id = ?;

-- Queries para Capacidades
getCapabilitiesByWorker:
SELECT * FROM WorkerWorkstationCapability WHERE workerId = ?;

getCapabilitiesByWorkstation:
SELECT * FROM WorkerWorkstationCapability WHERE workstationId = ?;

insertCapability:
INSERT INTO WorkerWorkstationCapability(workerId, workstationId, proficiencyLevel, certificationDate)
VALUES (?, ?, ?, ?);

deleteCapability:
DELETE FROM WorkerWorkstationCapability WHERE id = ?;

-- Queries para Rotaciones
getAllSessions:
SELECT * FROM RotationSession ORDER BY createdAt DESC;

getActiveSession:
SELECT * FROM RotationSession WHERE isActive = 1 LIMIT 1;

insertSession:
INSERT INTO RotationSession(name, startDate, endDate, isActive, createdAt)
VALUES (?, ?, ?, ?, ?);

getAssignmentsBySession:
SELECT * FROM RotationAssignment WHERE sessionId = ? ORDER BY position ASC;

insertAssignment:
INSERT INTO RotationAssignment(sessionId, workerId, workstationId, position, assignedAt)
VALUES (?, ?, ?, ?, ?);

deleteAssignmentsBySession:
DELETE FROM RotationAssignment WHERE sessionId = ?;
```

### 1.2 Actualizar shared/build.gradle.kts ✅

Configuración verificada y funcionando:

```kotlin
sqldelight {
    databases {
        create("AppDatabase") {
            packageName.set("com.workstation.rotation.database")
        }
    }
}
```

### 1.3 Crear DatabaseDriverFactory ✅

**Archivos creados y verificados:**

**Archivo:** `shared/src/commonMain/kotlin/com/workstation/rotation/data/DatabaseDriverFactory.kt`

```kotlin
package com.workstation.rotation.data

import app.cash.sqldelight.db.SqlDriver

expect class DatabaseDriverFactory {
    fun createDriver(): SqlDriver
}
```

**Archivo:** `shared/src/androidMain/kotlin/com/workstation/rotation/data/DatabaseDriverFactory.android.kt`

```kotlin
package com.workstation.rotation.data

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.workstation.rotation.database.AppDatabase

actual class DatabaseDriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(AppDatabase.Schema, context, "workstation_rotation.db")
    }
}
```

**Archivo:** `shared/src/desktopMain/kotlin/com/workstation/rotation/data/DatabaseDriverFactory.desktop.kt`

```kotlin
package com.workstation.rotation.data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.workstation.rotation.database.AppDatabase
import java.io.File

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        val databasePath = File(System.getProperty("user.home"), ".workstation-rotation")
        databasePath.mkdirs()
        val databaseFile = File(databasePath, "workstation_rotation.db")
        
        val driver = JdbcSqliteDriver("jdbc:sqlite:${databaseFile.absolutePath}")
        AppDatabase.Schema.create(driver)
        return driver
    }
}
```

---

## Fase 2: Crear Modelos de Dominio

**Archivo:** `shared/src/commonMain/kotlin/com/workstation/rotation/domain/models/Models.kt`

```kotlin
package com.workstation.rotation.domain.models

data class WorkerModel(
    val id: Long = 0,
    val name: String,
    val employeeId: String,
    val isActive: Boolean = true,
    val photoPath: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class WorkstationModel(
    val id: Long = 0,
    val name: String,
    val code: String,
    val description: String? = null,
    val isActive: Boolean = true,
    val requiredWorkers: Int = 1,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class CapabilityModel(
    val id: Long = 0,
    val workerId: Long,
    val workstationId: Long,
    val proficiencyLevel: Int = 1,
    val certificationDate: Long? = null
)

data class RotationSessionModel(
    val id: Long = 0,
    val name: String,
    val startDate: Long,
    val endDate: Long? = null,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

data class RotationAssignmentModel(
    val id: Long = 0,
    val sessionId: Long,
    val workerId: Long,
    val workstationId: Long,
    val position: Int,
    val assignedAt: Long = System.currentTimeMillis()
)
```

---

## Fase 3: Crear Repositorios

**Archivo:** `shared/src/commonMain/kotlin/com/workstation/rotation/domain/repository/WorkerRepository.kt`

```kotlin
package com.workstation.rotation.domain.repository

import com.workstation.rotation.database.AppDatabase
import com.workstation.rotation.domain.models.WorkerModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class WorkerRepository(private val database: AppDatabase) {
    
    fun getAllWorkers(): Flow<List<WorkerModel>> {
        return database.appDatabaseQueries.getAllWorkers()
            .asFlow()
            .mapToList()
            .map { workers ->
                workers.map { it.toModel() }
            }
    }
    
    fun getActiveWorkers(): Flow<List<WorkerModel>> {
        return database.appDatabaseQueries.getActiveWorkers()
            .asFlow()
            .mapToList()
            .map { workers ->
                workers.map { it.toModel() }
            }
    }
    
    suspend fun insertWorker(worker: WorkerModel) {
        database.appDatabaseQueries.insertWorker(
            name = worker.name,
            employeeId = worker.employeeId,
            isActive = worker.isActive,
            photoPath = worker.photoPath,
            createdAt = worker.createdAt,
            updatedAt = worker.updatedAt
        )
    }
    
    suspend fun updateWorker(worker: WorkerModel) {
        database.appDatabaseQueries.updateWorker(
            name = worker.name,
            employeeId = worker.employeeId,
            isActive = worker.isActive,
            photoPath = worker.photoPath,
            updatedAt = System.currentTimeMillis(),
            id = worker.id
        )
    }
    
    suspend fun deleteWorker(workerId: Long) {
        database.appDatabaseQueries.deleteWorker(workerId)
    }
    
    private fun Worker.toModel() = WorkerModel(
        id = id,
        name = name,
        employeeId = employeeId,
        isActive = isActive,
        photoPath = photoPath,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
```

Crear repositorios similares para:
- `WorkstationRepository.kt`
- `CapabilityRepository.kt`
- `RotationRepository.kt`

---

## Fase 4: Crear ViewModels Compartidos

**Archivo:** `shared/src/commonMain/kotlin/com/workstation/rotation/presentation/viewmodels/WorkerViewModel.kt`

```kotlin
package com.workstation.rotation.presentation.viewmodels

import com.workstation.rotation.domain.models.WorkerModel
import com.workstation.rotation.domain.repository.WorkerRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WorkerViewModel(
    private val repository: WorkerRepository,
    private val scope: CoroutineScope
) {
    private val _workers = MutableStateFlow<List<WorkerModel>>(emptyList())
    val workers: StateFlow<List<WorkerModel>> = _workers.asStateFlow()
    
    init {
        loadWorkers()
    }
    
    private fun loadWorkers() {
        scope.launch {
            repository.getAllWorkers().collect { workerList ->
                _workers.value = workerList
            }
        }
    }
    
    fun addWorker(worker: WorkerModel) {
        scope.launch {
            repository.insertWorker(worker)
        }
    }
    
    fun updateWorker(worker: WorkerModel) {
        scope.launch {
            repository.updateWorker(worker)
        }
    }
    
    fun deleteWorker(workerId: Long) {
        scope.launch {
            repository.deleteWorker(workerId)
        }
    }
}
```

---

## Fase 5: Crear Pantallas Compartidas

**Archivo:** `shared/src/commonMain/kotlin/com/workstation/rotation/presentation/screens/WorkersScreen.kt`

```kotlin
package com.workstation.rotation.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.workstation.rotation.domain.models.WorkerModel
import com.workstation.rotation.presentation.viewmodels.WorkerViewModel

@Composable
fun WorkersScreen(viewModel: WorkerViewModel) {
    val workers by viewModel.workers.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Text("+")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            items(workers) { worker ->
                WorkerItem(
                    worker = worker,
                    onEdit = { /* TODO */ },
                    onDelete = { viewModel.deleteWorker(worker.id) }
                )
            }
        }
    }
    
    if (showDialog) {
        AddWorkerDialog(
            onDismiss = { showDialog = false },
            onConfirm = { worker ->
                viewModel.addWorker(worker)
                showDialog = false
            }
        )
    }
}

@Composable
fun WorkerItem(
    worker: WorkerModel,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(worker.name, style = MaterialTheme.typography.titleMedium)
                Text(worker.employeeId, style = MaterialTheme.typography.bodySmall)
            }
            Row {
                IconButton(onClick = onEdit) {
                    Text("✏️")
                }
                IconButton(onClick = onDelete) {
                    Text("🗑️")
                }
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
            Column {
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = employeeId,
                    onValueChange = { employeeId = it },
                    label = { Text("ID Empleado") }
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                if (name.isNotBlank() && employeeId.isNotBlank()) {
                    onConfirm(WorkerModel(name = name, employeeId = employeeId))
                }
            }) {
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
```

Crear pantallas similares para:
- `WorkstationsScreen.kt`
- `RotationScreen.kt`
- `HistoryScreen.kt`

---

## Fase 6: Actualizar App.kt con Navegación

**Archivo:** `shared/src/commonMain/kotlin/com/workstation/rotation/App.kt`

```kotlin
package com.workstation.rotation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

enum class Screen {
    HOME, WORKERS, WORKSTATIONS, ROTATION, HISTORY
}

@Composable
fun App(
    workerViewModel: WorkerViewModel,
    workstationViewModel: WorkstationViewModel,
    rotationViewModel: RotationViewModel
) {
    var currentScreen by remember { mutableStateOf(Screen.HOME) }
    
    MaterialTheme {
        Scaffold(
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentScreen == Screen.WORKERS,
                        onClick = { currentScreen = Screen.WORKERS },
                        icon = { Text("👷") },
                        label = { Text("Workers") }
                    )
                    NavigationBarItem(
                        selected = currentScreen == Screen.WORKSTATIONS,
                        onClick = { currentScreen = Screen.WORKSTATIONS },
                        icon = { Text("🏭") },
                        label = { Text("Stations") }
                    )
                    NavigationBarItem(
                        selected = currentScreen == Screen.ROTATION,
                        onClick = { currentScreen = Screen.ROTATION },
                        icon = { Text("🔄") },
                        label = { Text("Rotation") }
                    )
                }
            }
        ) { padding ->
            Box(modifier = Modifier.padding(padding)) {
                when (currentScreen) {
                    Screen.HOME -> HomeScreen { currentScreen = it }
                    Screen.WORKERS -> WorkersScreen(workerViewModel)
                    Screen.WORKSTATIONS -> WorkstationsScreen(workstationViewModel)
                    Screen.ROTATION -> RotationScreen(rotationViewModel)
                    Screen.HISTORY -> HistoryScreen()
                }
            }
        }
    }
}
```

---

## Fase 7: Inicializar en Android y Desktop

### Android: `androidApp/src/main/kotlin/.../MainActivity.kt`

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val driverFactory = DatabaseDriverFactory(applicationContext)
        val database = AppDatabase(driverFactory.createDriver())
        val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
        
        val workerRepo = WorkerRepository(database)
        val workerViewModel = WorkerViewModel(workerRepo, scope)
        
        // Crear otros ViewModels...
        
        setContent {
            App(workerViewModel, workstationViewModel, rotationViewModel)
        }
    }
}
```

### Desktop: `desktopApp/src/main/kotlin/.../Main.kt`

```kotlin
fun main() = application {
    val driverFactory = DatabaseDriverFactory()
    val database = AppDatabase(driverFactory.createDriver())
    val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    val workerRepo = WorkerRepository(database)
    val workerViewModel = WorkerViewModel(workerRepo, scope)
    
    // Crear otros ViewModels...
    
    Window(
        onCloseRequest = ::exitApplication,
        title = "Workstation Rotation"
    ) {
        App(workerViewModel, workstationViewModel, rotationViewModel)
    }
}
```

---

## Resumen de Pasos

1. ✅ **SQLDelight:** Crear esquema de base de datos - **COMPLETADO**
2. ✅ **DatabaseDriverFactory:** Implementar para Android y Desktop - **COMPLETADO**
3. ⏳ **Modelos:** Crear data classes compartidas - **PENDIENTE**
4. ⏳ **Repositorios:** Implementar lógica de acceso a datos - **PENDIENTE**
5. ⏳ **ViewModels:** Crear ViewModels compartidos - **PENDIENTE**
6. ⏳ **Pantallas:** Crear UI con Compose Multiplatform - **PENDIENTE**
7. ⏳ **Navegación:** Implementar navegación entre pantallas - **PENDIENTE**
8. ⏳ **Inicialización:** Configurar MainActivity y Main.kt - **PENDIENTE**

---

## Compilar y Probar

```cmd
# Android
.\gradlew :androidApp:assembleDebug

# Desktop
.\gradlew :desktopApp:run

# Ambos
.\gradlew :androidApp:assembleDebug :desktopApp:packageDistributionForCurrentOS
```

---

**Fecha:** 13 de noviembre de 2025  
**Versión:** 5.0.0
