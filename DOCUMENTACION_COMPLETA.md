# Documentación Completa - App de Rotación de Estaciones de Trabajo

## Tabla de Contenidos
1. [Introducción](#introducción)
2. [Arquitectura del Sistema](#arquitectura-del-sistema)
3. [Modelo de Datos](#modelo-de-datos)
4. [Funcionalidades Principales](#funcionalidades-principales)
5. [Sistema de Rotación Inteligente](#sistema-de-rotación-inteligente)
6. [Interfaz de Usuario](#interfaz-de-usuario)
7. [Guía de Uso](#guía-de-uso)
8. [Instalación y Configuración](#instalación-y-configuración)
9. [Tecnologías Utilizadas](#tecnologías-utilizadas)
10. [Estructura del Proyecto](#estructura-del-proyecto)

---

## Introducción

La **App de Rotación de Estaciones de Trabajo** es una aplicación Android desarrollada para gestionar de manera eficiente la rotación de trabajadores en diferentes estaciones de trabajo. La aplicación implementa un sistema inteligente que considera múltiples factores como capacidades, prioridades, entrenamiento y disponibilidad para generar rotaciones óptimas.

### Características Principales
- ✅ **Gestión completa de trabajadores y estaciones**
- ✅ **Sistema de rotación inteligente con 4 fases**
- ✅ **Gestión de entrenamiento (entrenador-aprendiz)**
- ✅ **Control de prioridades y capacidades**
- ✅ **Interfaz moderna con Material Design 3**
- ✅ **Base de datos local con Room**
- ✅ **Funciona completamente offline**

---

## Arquitectura del Sistema

La aplicación sigue el patrón **MVVM (Model-View-ViewModel)** con las siguientes capas:

### 1. Capa de Presentación (UI)
- **Activities**: Pantallas principales de la aplicación
- **Adapters**: Gestión de listas y RecyclerViews
- **Layouts XML**: Definición de interfaces de usuario

### 2. Capa de Lógica de Negocio
- **ViewModels**: Gestión del estado y lógica de presentación
- **Utils**: Utilidades para validación y UI

### 3. Capa de Datos
- **Entities**: Modelos de datos con Room
- **DAOs**: Acceso a datos
- **Database**: Configuración de base de datos

```
┌─────────────────┐
│   Activities    │ ← Interfaz de Usuario
├─────────────────┤
│   ViewModels    │ ← Lógica de Negocio
├─────────────────┤
│   Repository    │ ← Gestión de Datos
├─────────────────┤
│   Room Database │ ← Persistencia Local
└─────────────────┘
```

---

## Modelo de Datos

### Entidades Principales

#### 1. Worker (Trabajador)
```kotlin
@Entity(tableName = "workers")
data class Worker(
    val id: Long = 0,
    val name: String,
    val email: String = "",
    val availabilityPercentage: Int = 100,
    val restrictionNotes: String = "",
    val isTrainer: Boolean = false,
    val isTrainee: Boolean = false,
    val trainerId: Long? = null,
    val trainingWorkstationId: Long? = null,
    val isActive: Boolean = true
)
```

**Campos importantes:**
- `availabilityPercentage`: Porcentaje de disponibilidad (0-100%)
- `isTrainer`: Indica si puede entrenar a otros
- `isTrainee`: Indica si está en entrenamiento
- `trainerId`: ID del entrenador asignado
- `trainingWorkstationId`: Estación donde ocurre el entrenamiento

#### 2. Workstation (Estación de Trabajo)
```kotlin
@Entity(tableName = "workstations")
data class Workstation(
    val id: Long = 0,
    val name: String,
    val requiredWorkers: Int = 1,
    val isPriority: Boolean = false,
    val isActive: Boolean = true
)
```

**Campos importantes:**
- `requiredWorkers`: Número de trabajadores requeridos
- `isPriority`: Estación de alta prioridad (siempre mantiene capacidad completa)

#### 3. WorkerWorkstation (Relación Many-to-Many)
```kotlin
@Entity(
    tableName = "worker_workstation",
    primaryKeys = ["workerId", "workstationId"]
)
data class WorkerWorkstation(
    val workerId: Long,
    val workstationId: Long
)
```

#### 4. RotationTable (Tabla de Rotación)
```kotlin
@Entity(tableName = "rotation_table")
data class RotationTable(
    val id: Long = 0,
    val workerId: Long,
    val currentWorkstationId: Long?,
    val nextWorkstationId: Long?,
    val rotationDate: Long = System.currentTimeMillis(),
    val phase: String = "NORMAL"
)
```

### Relaciones de Base de Datos
- **Worker ↔ Workstation**: Relación Many-to-Many (un trabajador puede trabajar en múltiples estaciones)
- **Worker → Worker**: Relación entrenador-aprendiz (One-to-Many)
- **RotationTable**: Registra las rotaciones generadas

---

## Funcionalidades Principales

### 1. Gestión de Trabajadores

#### Agregar Trabajador
- Nombre completo (obligatorio)
- Email (opcional)
- Porcentaje de disponibilidad (0-100%)
- Notas de restricciones
- Configuración de entrenamiento

#### Asignación de Estaciones
- Selección múltiple con checkboxes
- Vista clara de estaciones asignadas
- Solo trabajadores con estaciones participan en rotación

#### Sistema de Entrenamiento
- **Entrenadores**: Pueden entrenar a múltiples aprendices
- **Aprendices**: Asignados a un entrenador específico
- **Estación de entrenamiento**: Lugar designado para el entrenamiento
- **Regla**: Entrenador y aprendiz siempre van juntos a la estación de entrenamiento

### 2. Gestión de Estaciones de Trabajo

#### Crear Estaciones
- Nombre descriptivo
- Número de trabajadores requeridos
- Configuración de prioridad
- Estado activo/inactivo

#### Tipos de Estaciones
- **Normales**: Rotación estándar
- **Prioritarias**: Siempre mantienen capacidad completa
- **De entrenamiento**: Designadas para parejas entrenador-aprendiz

### 3. Sistema de Rotación Inteligente

El algoritmo de rotación implementa **4 fases** para generar rotaciones óptimas:

#### Fase 1: Parejas de Entrenamiento
```kotlin
// Asignar parejas entrenador-aprendiz a estaciones de entrenamiento
for (trainee in trainees) {
    val trainer = findTrainer(trainee.trainerId)
    val trainingStation = findStation(trainee.trainingWorkstationId)
    assignPair(trainer, trainee, trainingStation)
}
```

#### Fase 2: Estaciones Prioritarias
```kotlin
// Llenar estaciones prioritarias primero
for (station in priorityStations) {
    while (station.needsMoreWorkers()) {
        val bestWorker = findBestAvailableWorker(station)
        assign(bestWorker, station)
    }
}
```

#### Fase 3: Distribución Normal
```kotlin
// Distribuir trabajadores restantes
for (worker in availableWorkers) {
    val bestStation = findBestStation(worker)
    assign(worker, bestStation)
}
```

#### Fase 4: Preparación Siguiente Rotación
```kotlin
// Calcular próximas asignaciones
for (worker in allWorkers) {
    val nextStation = calculateNextStation(worker)
    updateRotationTable(worker, nextStation)
}
```

### Algoritmo de Selección
El sistema considera múltiples factores:

1. **Disponibilidad**: Probabilidad basada en porcentaje
2. **Capacidades**: Solo estaciones donde el trabajador puede trabajar
3. **Prioridades**: Estaciones críticas primero
4. **Entrenamiento**: Parejas siempre juntas
5. **Balanceo**: Distribución equitativa

---

## Interfaz de Usuario

### Diseño Material Design 3

#### Colores del Tema
```xml
<color name="primary">#1976D2</color>          <!-- Azul profesional -->
<color name="primary_variant">#1565C0</color>   <!-- Azul oscuro -->
<color name="secondary">#03DAC6</color>         <!-- Verde agua -->
<color name="background">#F5F5F5</color>        <!-- Gris claro -->
<color name="surface">#FFFFFF</color>           <!-- Blanco -->
<color name="error">#B00020</color>             <!-- Rojo error -->
```

#### Componentes Principales

**1. Cards con Elevación**
- Sombras sutiles para profundidad
- Bordes redondeados
- Espaciado consistente

**2. Floating Action Buttons (FAB)**
- Acción principal en cada pantalla
- Iconos intuitivos
- Animaciones suaves

**3. Dialogs Modales**
- Formularios centrados
- Validación en tiempo real
- Botones de acción claros

**4. RecyclerViews Optimizados**
- Scroll suave
- ViewHolders eficientes
- Animaciones de elementos

### Pantallas Principales

#### 1. MainActivity - Pantalla Principal
```
┌─────────────────────────────┐
│     Rotación Estaciones     │
├─────────────────────────────┤
│  [👥 Trabajadores]         │
│  [🏭 Estaciones]           │
│  [🔄 Generar Rotación]     │
│  [📊 Ver Rotación Actual]  │
└─────────────────────────────┘
```

#### 2. WorkerActivity - Gestión de Trabajadores
```
┌─────────────────────────────┐
│       Trabajadores          │
├─────────────────────────────┤
│ 🔍 [Buscar...]             │
├─────────────────────────────┤
│ Juan Pérez        👨‍🏫      │
│ 3 estaciones asignadas      │
├─────────────────────────────┤
│ María García      🎯        │
│ 2 estaciones asignadas      │
├─────────────────────────────┤
│              [+] FAB        │
└─────────────────────────────┘
```

#### 3. RotationActivity - Vista de Rotación
```
┌─────────────────────────────┐
│      Rotación Actual        │
├─────────────────────────────┤
│ Trabajadores: 8/10          │
│ Última rotación: 10:30 AM   │
├─────────────────────────────┤
│ Juan → Estación A → Est. B  │
│ María → Estación B → Est. C │
│ Pedro → Estación C → Est. A │
├─────────────────────────────┤
│    [🔄 Nueva Rotación]     │
└─────────────────────────────┘
```

### Componentes Especializados

#### 1. Vista Tabular Horizontal
Para mostrar rotaciones de manera clara:
```xml
<HorizontalScrollView>
    <TableLayout>
        <!-- Columnas de estaciones -->
        <!-- Filas de trabajadores -->
    </TableLayout>
</HorizontalScrollView>
```

#### 2. Checkboxes para Estaciones
Selección múltiple intuitiva:
```xml
<RecyclerView>
    <!-- Items con CheckBox + TextView -->
</RecyclerView>
```

#### 3. Indicadores de Estado
- 👨‍🏫 Entrenador
- 🎯 Aprendiz
- ⭐ Estación prioritaria
- ✅ Activo
- ❌ Inactivo

---

## Guía de Uso

### Configuración Inicial

#### 1. Crear Estaciones de Trabajo
1. Ir a **"Estaciones"**
2. Presionar el botón **"+"**
3. Llenar el formulario:
   - **Nombre**: Descripción de la estación
   - **Trabajadores requeridos**: Capacidad
   - **Prioridad**: Marcar si es crítica
4. Guardar

#### 2. Registrar Trabajadores
1. Ir a **"Trabajadores"**
2. Presionar el botón **"+"**
3. Llenar información:
   - **Nombre completo**
   - **Email** (opcional)
   - **Disponibilidad** (0-100%)
   - **Restricciones** (opcional)
4. **Asignar estaciones** donde puede trabajar
5. Configurar **entrenamiento** si aplica
6. Guardar

#### 3. Configurar Entrenamiento (Opcional)
Para configurar parejas entrenador-aprendiz:

**Crear Entrenador:**
1. Marcar **"Es entrenador"**
2. Guardar trabajador

**Crear Aprendiz:**
1. Marcar **"Es aprendiz"**
2. Seleccionar **entrenador** de la lista
3. Seleccionar **estación de entrenamiento**
4. Guardar

### Operación Diaria

#### Generar Nueva Rotación
1. Ir a **"Rotación"**
2. Presionar **"Generar Nueva Rotación"**
3. El sistema automáticamente:
   - Evalúa disponibilidad de trabajadores
   - Asigna parejas de entrenamiento
   - Llena estaciones prioritarias
   - Distribuye trabajadores restantes
   - Calcula próxima rotación

#### Ver Rotación Actual
La pantalla muestra:
- **Trabajadores elegibles**: Cuántos participan
- **Asignaciones actuales**: Dónde está cada uno
- **Próximas asignaciones**: Dónde irán
- **Hora de generación**: Cuándo se creó

#### Gestionar Cambios
- **Editar trabajador**: Cambiar disponibilidad o estaciones
- **Activar/desactivar**: Incluir/excluir de rotaciones
- **Modificar estaciones**: Cambiar capacidad o prioridad

### Casos de Uso Comunes

#### Caso 1: Trabajador Enfermo
1. Ir a **Trabajadores**
2. Encontrar al trabajador
3. Editar y cambiar **disponibilidad a 0%**
4. Generar nueva rotación

#### Caso 2: Nueva Estación
1. Crear la estación en **Estaciones**
2. Ir a cada trabajador que pueda trabajar ahí
3. Editar y marcar la nueva estación
4. Generar nueva rotación

#### Caso 3: Nuevo Aprendiz
1. Crear trabajador marcando **"Es aprendiz"**
2. Asignar entrenador disponible
3. Seleccionar estación de entrenamiento
4. El sistema automáticamente los mantendrá juntos

---

## Instalación y Configuración

### Requisitos del Sistema
- **Android**: 7.0 (API 24) o superior
- **RAM**: Mínimo 2GB recomendado
- **Almacenamiento**: 15MB libres
- **Conexión**: No requiere internet

### Opción 1: Instalar APK Precompilado
1. Descargar `app-debug.apk` desde [Releases](../../releases)
2. Habilitar **"Fuentes desconocidas"** en Android
3. Instalar el archivo APK
4. Abrir la aplicación

### Opción 2: Compilar desde Código Fuente

#### Prerrequisitos
- **Android Studio** Arctic Fox o superior
- **JDK** 11 o superior
- **Android SDK** API 34

#### Pasos
1. **Clonar repositorio**:
   ```bash
   git clone https://github.com/usuario/workstation-rotation-app.git
   cd workstation-rotation-app
   ```

2. **Abrir en Android Studio**:
   - File → Open → Seleccionar carpeta del proyecto

3. **Sincronizar dependencias**:
   - Android Studio automáticamente descarga dependencias

4. **Compilar y ejecutar**:
   - Conectar dispositivo Android o usar emulador
   - Presionar "Run" (▶️)

#### Configuración de Gradle
El proyecto usa las siguientes dependencias principales:

```gradle
dependencies {
    // Core Android
    implementation 'androidx.core:core-ktx:1.12.0'
    implementation 'androidx.appcompat:appcompat:1.6.1'
    
    // Material Design
    implementation 'com.google.android.material:material:1.11.0'
    
    // Room Database
    implementation 'androidx.room:room-runtime:2.6.1'
    implementation 'androidx.room:room-ktx:2.6.1'
    kapt 'androidx.room:room-compiler:2.6.1'
    
    // ViewModel y LiveData
    implementation 'androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0'
    implementation 'androidx.lifecycle:lifecycle-livedata-ktx:2.7.0'
    
    // Coroutines
    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3'
}
```

### Configuración Inicial de la App
Al abrir por primera vez:

1. **Base de datos**: Se crea automáticamente
2. **Datos de ejemplo**: Opcional, se pueden crear manualmente
3. **Permisos**: No requiere permisos especiales

---

## Tecnologías Utilizadas

### Lenguaje y Framework
- **Kotlin**: Lenguaje principal de desarrollo
- **Android SDK**: Framework de aplicaciones Android
- **Coroutines**: Programación asíncrona

### Arquitectura y Patrones
- **MVVM**: Model-View-ViewModel
- **Repository Pattern**: Abstracción de datos
- **Observer Pattern**: LiveData y ViewModels

### Base de Datos
- **Room**: ORM para SQLite
- **SQLite**: Base de datos local
- **Migrations**: Versionado de esquema

### Interfaz de Usuario
- **Material Design 3**: Sistema de diseño de Google
- **ViewBinding**: Vinculación segura de vistas
- **RecyclerView**: Listas eficientes
- **ConstraintLayout**: Layouts flexibles

### Herramientas de Desarrollo
- **Android Studio**: IDE principal
- **Gradle**: Sistema de construcción
- **Git**: Control de versiones
- **GitHub Actions**: CI/CD automático

### Librerías Principales
```kotlin
// Room Database
androidx.room:room-runtime:2.6.1
androidx.room:room-ktx:2.6.1

// ViewModel y LiveData
androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0
androidx.lifecycle:lifecycle-livedata-ktx:2.7.0

// Material Design
com.google.android.material:material:1.11.0

// Coroutines
kotlinx-coroutines-android:1.7.3
```

---

## Estructura del Proyecto

```
workstation-rotation-app/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/workstation/rotation/
│   │   │   │   ├── data/
│   │   │   │   │   ├── entities/
│   │   │   │   │   │   ├── Worker.kt
│   │   │   │   │   │   ├── Workstation.kt
│   │   │   │   │   │   ├── WorkerWorkstation.kt
│   │   │   │   │   │   └── RotationTable.kt
│   │   │   │   │   ├── dao/
│   │   │   │   │   │   ├── WorkerDao.kt
│   │   │   │   │   │   ├── WorkstationDao.kt
│   │   │   │   │   │   └── RotationDao.kt
│   │   │   │   │   └── database/
│   │   │   │   │       └── AppDatabase.kt
│   │   │   │   ├── viewmodels/
│   │   │   │   │   ├── WorkerViewModel.kt
│   │   │   │   │   ├── WorkstationViewModel.kt
│   │   │   │   │   └── RotationViewModel.kt
│   │   │   │   ├── adapters/
│   │   │   │   │   ├── WorkerAdapter.kt
│   │   │   │   │   ├── WorkstationAdapter.kt
│   │   │   │   │   └── RotationAdapter.kt
│   │   │   │   ├── utils/
│   │   │   │   │   ├── Constants.kt
│   │   │   │   │   ├── ValidationUtils.kt
│   │   │   │   │   └── UIUtils.kt
│   │   │   │   ├── MainActivity.kt
│   │   │   │   ├── WorkerActivity.kt
│   │   │   │   ├── WorkstationActivity.kt
│   │   │   │   └── RotationActivity.kt
│   │   │   └── res/
│   │   │       ├── layout/
│   │   │       │   ├── activity_main.xml
│   │   │       │   ├── activity_worker.xml
│   │   │       │   ├── activity_workstation.xml
│   │   │       │   ├── activity_rotation.xml
│   │   │       │   ├── dialog_add_worker.xml
│   │   │       │   ├── dialog_add_workstation.xml
│   │   │       │   ├── item_worker.xml
│   │   │       │   ├── item_workstation.xml
│   │   │       │   └── item_rotation.xml
│   │   │       ├── values/
│   │   │       │   ├── colors.xml
│   │   │       │   ├── strings.xml
│   │   │       │   └── themes.xml
│   │   │       └── drawable/
│   │   └── androidTest/
│   └── build.gradle
├── gradle/
├── .github/
│   └── workflows/
│       └── build-apk.yml
├── build.gradle
├── settings.gradle
├── README.md
└── LICENSE
```

### Descripción de Componentes

#### `/data/entities/`
Modelos de datos con anotaciones Room:
- **Worker.kt**: Entidad trabajador con métodos auxiliares
- **Workstation.kt**: Entidad estación con validaciones
- **WorkerWorkstation.kt**: Tabla de relación many-to-many
- **RotationTable.kt**: Registro de rotaciones generadas

#### `/data/dao/`
Interfaces de acceso a datos:
- **WorkerDao.kt**: CRUD y consultas complejas de trabajadores
- **WorkstationDao.kt**: Operaciones de estaciones
- **RotationDao.kt**: Gestión de rotaciones

#### `/viewmodels/`
Lógica de presentación:
- **WorkerViewModel.kt**: Estado y operaciones de trabajadores
- **WorkstationViewModel.kt**: Gestión de estaciones
- **RotationViewModel.kt**: Algoritmo de rotación inteligente

#### `/adapters/`
Adaptadores para RecyclerViews:
- **WorkerAdapter.kt**: Lista de trabajadores con acciones
- **WorkstationAdapter.kt**: Lista de estaciones
- **RotationAdapter.kt**: Vista tabular de rotaciones

#### `/utils/`
Utilidades del sistema:
- **Constants.kt**: Constantes globales
- **ValidationUtils.kt**: Validaciones de entrada
- **UIUtils.kt**: Helpers para interfaz

---

## Algoritmo de Rotación Detallado

### Flujo Principal del Algoritmo

```kotlin
fun generateRotation(): List<RotationEntry> {
    // 1. Preparación inicial
    val availableWorkers = getAvailableWorkers()
    val activeStations = getActiveStations()
    val rotationEntries = mutableListOf<RotationEntry>()
    
    // 2. Fase 1: Parejas de entrenamiento
    handleTrainingPairs(availableWorkers, activeStations, rotationEntries)
    
    // 3. Fase 2: Estaciones prioritarias
    handlePriorityStations(availableWorkers, activeStations, rotationEntries)
    
    // 4. Fase 3: Distribución normal
    handleNormalDistribution(availableWorkers, activeStations, rotationEntries)
    
    // 5. Fase 4: Calcular próxima rotación
    calculateNextRotation(rotationEntries)
    
    return rotationEntries
}
```

### Fase 1: Gestión de Entrenamiento

```kotlin
private fun handleTrainingPairs(
    availableWorkers: MutableList<Worker>,
    activeStations: List<Workstation>,
    rotationEntries: MutableList<RotationEntry>
) {
    val trainees = availableWorkers.filter { it.isTrainee && it.isActive }
    
    for (trainee in trainees) {
        val trainer = findTrainerById(trainee.trainerId)
        val trainingStation = findStationById(trainee.trainingWorkstationId)
        
        if (trainer != null && trainingStation != null && 
            trainingStation.hasCapacity() && 
            isWorkerAvailable(trainer) && isWorkerAvailable(trainee)) {
            
            // Asignar ambos a la estación de entrenamiento
            assignWorkerToStation(trainer, trainingStation, rotationEntries, "TRAINING")
            assignWorkerToStation(trainee, trainingStation, rotationEntries, "TRAINING")
            
            // Remover de trabajadores disponibles
            availableWorkers.remove(trainer)
            availableWorkers.remove(trainee)
        }
    }
}
```

### Fase 2: Estaciones Prioritarias

```kotlin
private fun handlePriorityStations(
    availableWorkers: MutableList<Worker>,
    activeStations: List<Workstation>,
    rotationEntries: MutableList<RotationEntry>
) {
    val priorityStations = activeStations.filter { it.isPriority }
        .sortedBy { getCurrentWorkerCount(it.id) }
    
    for (station in priorityStations) {
        while (station.needsMoreWorkers() && availableWorkers.isNotEmpty()) {
            val bestWorker = findBestWorkerForStation(station, availableWorkers)
            
            if (bestWorker != null && isWorkerAvailable(bestWorker)) {
                assignWorkerToStation(bestWorker, station, rotationEntries, "PRIORITY")
                availableWorkers.remove(bestWorker)
            } else {
                break // No hay trabajadores disponibles para esta estación
            }
        }
    }
}
```

### Fase 3: Distribución Normal

```kotlin
private fun handleNormalDistribution(
    availableWorkers: MutableList<Worker>,
    activeStations: List<Workstation>,
    rotationEntries: MutableList<RotationEntry>
) {
    // Ordenar trabajadores por disponibilidad (mayor primero)
    val sortedWorkers = availableWorkers.sortedByDescending { it.availabilityPercentage }
    
    for (worker in sortedWorkers) {
        if (!isWorkerAvailable(worker)) continue
        
        val availableStations = getWorkerStations(worker.id)
            .filter { it.isActive && it.hasCapacity() }
            .sortedBy { getCurrentWorkerCount(it.id) } // Menos ocupadas primero
        
        val bestStation = availableStations.firstOrNull()
        
        if (bestStation != null) {
            assignWorkerToStation(worker, bestStation, rotationEntries, "NORMAL")
        }
    }
}
```

### Algoritmo de Disponibilidad

```kotlin
private fun isWorkerAvailable(worker: Worker): Boolean {
    // Verificar si está activo
    if (!worker.isActive) return false
    
    // Calcular disponibilidad basada en porcentaje
    val random = Random.nextInt(1, 101)
    return random <= worker.availabilityPercentage
}
```

### Cálculo de Próxima Rotación

```kotlin
private fun calculateNextRotation(rotationEntries: List<RotationEntry>) {
    for (entry in rotationEntries) {
        val worker = getWorkerById(entry.workerId)
        val currentStation = entry.currentWorkstationId
        
        // Obtener estaciones donde puede trabajar
        val workerStations = getWorkerStations(worker.id)
            .filter { it.isActive && it.id != currentStation }
        
        // Si es aprendiz, próxima estación es siempre la de entrenamiento
        if (worker.isTrainee) {
            entry.nextWorkstationId = worker.trainingWorkstationId
        } else {
            // Seleccionar siguiente estación con menos carga
            val nextStation = workerStations
                .minByOrNull { getProjectedWorkerCount(it.id) }
            entry.nextWorkstationId = nextStation?.id
        }
    }
}
```

### Métricas y Optimización

El algoritmo considera múltiples métricas para optimizar las asignaciones:

#### 1. Balanceo de Carga
```kotlin
fun getStationLoadBalance(): Double {
    val stations = getActiveStations()
    val loads = stations.map { getCurrentWorkerCount(it.id).toDouble() / it.requiredWorkers }
    val average = loads.average()
    val variance = loads.map { (it - average).pow(2) }.average()
    return sqrt(variance) // Desviación estándar como métrica de balance
}
```

#### 2. Eficiencia de Capacidad
```kotlin
fun getCapacityEfficiency(): Double {
    val stations = getActiveStations()
    val totalRequired = stations.sumOf { it.requiredWorkers }
    val totalAssigned = stations.sumOf { getCurrentWorkerCount(it.id) }
    return totalAssigned.toDouble() / totalRequired
}
```

#### 3. Satisfacción de Prioridades
```kotlin
fun getPrioritySatisfaction(): Double {
    val priorityStations = getActiveStations().filter { it.isPriority }
    val satisfiedStations = priorityStations.count { 
        getCurrentWorkerCount(it.id) >= it.requiredWorkers 
    }
    return satisfiedStations.toDouble() / priorityStations.size
}
```

---

## Casos de Prueba y Validación

### Escenarios de Prueba

#### 1. Rotación Básica
```
Trabajadores: 6 activos (100% disponibilidad)
Estaciones: 3 normales (2 trabajadores cada una)
Resultado esperado: Distribución 2-2-2
```

#### 2. Con Entrenamiento
```
Trabajadores: 1 entrenador + 1 aprendiz + 4 normales
Estaciones: 1 entrenamiento + 2 normales
Resultado esperado: Pareja en entrenamiento, resto distribuido
```

#### 3. Con Prioridades
```
Trabajadores: 8 activos
Estaciones: 1 prioritaria (3 req.) + 2 normales (2 req. cada una)
Resultado esperado: Prioritaria llena primero (3), resto 2-3 o 3-2
```

#### 4. Disponibilidad Variable
```
Trabajadores: 10 (50% disponibilidad promedio)
Estaciones: 3 (2 trabajadores cada una)
Resultado esperado: ~5 trabajadores asignados, distribución balanceada
```

### Validaciones Automáticas

El sistema incluye validaciones automáticas:

```kotlin
fun validateRotation(rotation: List<RotationEntry>): ValidationResult {
    val errors = mutableListOf<String>()
    
    // 1. Verificar capacidades
    for (station in getActiveStations()) {
        val assignedCount = rotation.count { it.currentWorkstationId == station.id }
        if (assignedCount > station.requiredWorkers) {
            errors.add("Estación ${station.name} sobrecargada: $assignedCount/${station.requiredWorkers}")
        }
    }
    
    // 2. Verificar parejas de entrenamiento
    val trainees = rotation.filter { getWorkerById(it.workerId).isTrainee }
    for (traineeEntry in trainees) {
        val trainee = getWorkerById(traineeEntry.workerId)
        val trainerEntry = rotation.find { it.workerId == trainee.trainerId }
        
        if (trainerEntry?.currentWorkstationId != traineeEntry.currentWorkstationId) {
            errors.add("Pareja entrenamiento separada: ${trainee.name}")
        }
    }
    
    // 3. Verificar asignaciones válidas
    for (entry in rotation) {
        val worker = getWorkerById(entry.workerId)
        val station = getWorkstationById(entry.currentWorkstationId!!)
        val workerStations = getWorkerStations(worker.id)
        
        if (!workerStations.contains(station)) {
            errors.add("${worker.name} asignado a estación no autorizada: ${station.name}")
        }
    }
    
    return ValidationResult(errors.isEmpty(), errors)
}
```

---

## Mantenimiento y Extensiones Futuras

### Mejoras Planificadas

#### 1. Reportes y Estadísticas
- Historial de rotaciones
- Métricas de eficiencia
- Reportes de disponibilidad
- Análisis de patrones

#### 2. Configuración Avanzada
- Horarios de trabajo
- Turnos múltiples
- Restricciones temporales
- Configuración de algoritmo

#### 3. Integración Externa
- Exportar a Excel/CSV
- API REST para integración
- Sincronización en la nube
- Notificaciones push

#### 4. Optimizaciones de Rendimiento
- Cache inteligente
- Algoritmos más eficientes
- Procesamiento en background
- Optimización de base de datos

### Mantenimiento del Código

#### Convenciones de Código
- **Kotlin Style Guide**: Seguir estándares oficiales
- **Documentación**: KDoc para todas las funciones públicas
- **Testing**: Unit tests para lógica crítica
- **Code Review**: Revisión antes de merge

#### Versionado
- **Semantic Versioning**: MAJOR.MINOR.PATCH
- **Git Flow**: Feature branches + develop + main
- **Releases**: Tags automáticos con GitHub Actions

#### Monitoreo
- **Crash Reporting**: Implementar Firebase Crashlytics
- **Analytics**: Métricas de uso
- **Performance**: Monitoring de rendimiento

---

## Conclusión

La **App de Rotación de Estaciones de Trabajo** es una solución completa y robusta para la gestión eficiente de rotaciones laborales. Su arquitectura modular, algoritmo inteligente y interfaz intuitiva la convierten en una herramienta valiosa para organizaciones que requieren optimizar la distribución de personal.

### Beneficios Clave
- ✅ **Automatización completa** del proceso de rotación
- ✅ **Flexibilidad** para diferentes tipos de organizaciones
- ✅ **Escalabilidad** para crecer con las necesidades
- ✅ **Facilidad de uso** con interfaz moderna
- ✅ **Confiabilidad** con validaciones robustas

### Impacto Organizacional
- **Eficiencia**: Reduce tiempo de planificación manual
- **Equidad**: Distribución justa de cargas de trabajo
- **Flexibilidad**: Adaptación rápida a cambios
- **Transparencia**: Visibilidad clara de asignaciones
- **Calidad**: Mejora en gestión de entrenamiento

La aplicación está diseñada para evolucionar y adaptarse a las necesidades cambiantes de las organizaciones, proporcionando una base sólida para la gestión moderna de recursos humanos.

---

*Documentación generada el 26 de octubre de 2025*
*Versión de la aplicación: 1.0.0*