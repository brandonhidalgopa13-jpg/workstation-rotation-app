# 🏗️ Arquitectura del Sistema - REWS v3.0.0

Este documento describe la arquitectura técnica completa del Sistema de Rotación y Estaciones de Trabajo (REWS), incluyendo patrones de diseño, estructura de datos, y decisiones arquitectónicas.

## 📋 Tabla de Contenidos

1. [Visión General](#-visión-general)
2. [Arquitectura de Alto Nivel](#-arquitectura-de-alto-nivel)
3. [Patrón MVVM](#-patrón-mvvm)
4. [Capa de Datos](#-capa-de-datos)
5. [Lógica de Negocio](#-lógica-de-negocio)
6. [Interfaz de Usuario](#-interfaz-de-usuario)
7. [Algoritmo de Rotación](#-algoritmo-de-rotación)
8. [Gestión de Estado](#-gestión-de-estado)
9. [Testing](#-testing)
10. [Rendimiento](#-rendimiento)
11. [Seguridad](#-seguridad)

## 🎯 Visión General

REWS está construido siguiendo principios de **Clean Architecture** y **SOLID**, utilizando el patrón **MVVM** (Model-View-ViewModel) para garantizar separación de responsabilidades, testabilidad y mantenibilidad del código.

### Principios Arquitectónicos

- **Separación de Responsabilidades**: Cada capa tiene una responsabilidad específica
- **Inversión de Dependencias**: Las capas superiores no dependen de las inferiores
- **Testabilidad**: Arquitectura diseñada para facilitar pruebas unitarias
- **Escalabilidad**: Estructura que permite crecimiento y nuevas funcionalidades
- **Mantenibilidad**: Código limpio y bien organizado

## 🏛️ Arquitectura de Alto Nivel

```
┌─────────────────────────────────────────────────────────────┐
│                    PRESENTATION LAYER                       │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐        │
│  │ Activities  │  │ Fragments   │  │ Adapters    │        │
│  └─────────────┘  └─────────────┘  └─────────────┘        │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                     VIEWMODEL LAYER                         │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐        │
│  │WorkerViewModel│ │RotationVM  │  │SettingsVM   │        │
│  └─────────────┘  └─────────────┘  └─────────────┘        │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                    REPOSITORY LAYER                         │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐        │
│  │WorkerRepo   │  │WorkstationR │  │ RotationRepo│        │
│  └─────────────┘  └─────────────┘  └─────────────┘        │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                      DATA LAYER                             │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐        │
│  │ Room DAO    │  │ Entities    │  │ Database    │        │
│  └─────────────┘  └─────────────┘  └─────────────┘        │
└─────────────────────────────────────────────────────────────┘
```

## 🎭 Patrón MVVM

### Model (Modelo)
Representa los datos y la lógica de negocio:

```kotlin
// Entidades de datos
@Entity(tableName = "workers")
data class Worker(
    @PrimaryKey val id: Long = 0,
    val name: String,
    val availability: Int,
    val isLeader: Boolean = false,
    val leadershipType: LeadershipType = LeadershipType.NONE,
    val isTraining: Boolean = false,
    val isCertified: Boolean = false
)

@Entity(tableName = "workstations")
data class Workstation(
    @PrimaryKey val id: Long = 0,
    val name: String,
    val capacity: Int,
    val isPriority: Boolean = false,
    val isActive: Boolean = true
)
```

### View (Vista)
Activities y Fragments que muestran la UI:

```kotlin
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        observeViewModel()
    }
    
    private fun observeViewModel() {
        viewModel.workers.observe(this) { workers ->
            updateWorkersUI(workers)
        }
    }
}
```

### ViewModel
Maneja la lógica de presentación y estado:

```kotlin
class WorkerViewModel @Inject constructor(
    private val workerRepository: WorkerRepository
) : ViewModel() {
    
    private val _workers = MutableLiveData<List<Worker>>()
    val workers: LiveData<List<Worker>> = _workers
    
    private val _uiState = MutableLiveData<UiState>()
    val uiState: LiveData<UiState> = _uiState
    
    fun loadWorkers() {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading
                val workerList = workerRepository.getAllWorkers()
                _workers.value = workerList
                _uiState.value = UiState.Success
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}
```

## 🗄️ Capa de Datos

### Room Database
Base de datos local usando Room:

```kotlin
@Database(
    entities = [
        Worker::class,
        Workstation::class,
        WorkerRestriction::class,
        RotationHistory::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun workerDao(): WorkerDao
    abstract fun workstationDao(): WorkstationDao
    abstract fun restrictionDao(): WorkerRestrictionDao
    abstract fun rotationDao(): RotationHistoryDao
}
```

### Data Access Objects (DAOs)
Interfaces para acceso a datos:

```kotlin
@Dao
interface WorkerDao {
    @Query("SELECT * FROM workers WHERE isActive = 1")
    suspend fun getAllActiveWorkers(): List<Worker>
    
    @Query("SELECT * FROM workers WHERE isLeader = 1")
    suspend fun getAllLeaders(): List<Worker>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorker(worker: Worker): Long
    
    @Update
    suspend fun updateWorker(worker: Worker)
    
    @Delete
    suspend fun deleteWorker(worker: Worker)
}
```

### Repository Pattern
Abstracción de fuentes de datos:

```kotlin
class WorkerRepository @Inject constructor(
    private val workerDao: WorkerDao
) {
    suspend fun getAllWorkers(): List<Worker> {
        return workerDao.getAllActiveWorkers()
    }
    
    suspend fun getLeaders(): List<Worker> {
        return workerDao.getAllLeaders()
    }
    
    suspend fun insertWorker(worker: Worker): Long {
        return workerDao.insertWorker(worker)
    }
    
    suspend fun updateWorker(worker: Worker) {
        workerDao.updateWorker(worker)
    }
}
```

## 🧠 Lógica de Negocio

### Algoritmo de Rotación
El núcleo del sistema es el algoritmo de rotación inteligente:

```kotlin
class RotationAlgorithm @Inject constructor(
    private val workerRepository: WorkerRepository,
    private val workstationRepository: WorkstationRepository,
    private val restrictionRepository: RestrictionRepository
) {
    
    suspend fun generateRotation(isFirstHalf: Boolean): RotationResult {
        val workers = workerRepository.getAllActiveWorkers()
        val workstations = workstationRepository.getAllActiveWorkstations()
        val restrictions = restrictionRepository.getAllActiveRestrictions()
        
        return RotationGenerator(workers, workstations, restrictions)
            .generateOptimalRotation(isFirstHalf)
    }
}
```

### Fases del Algoritmo

1. **Fase 0.5**: Asignación forzada de líderes "BOTH"
2. **Fase 1**: Asignación de líderes específicos por parte
3. **Fase 2**: Asignación de trabajadores en entrenamiento
4. **Fase 3**: Distribución de trabajadores regulares
5. **Fase 4**: Balanceado y optimización final

### Sistema de Restricciones
Manejo inteligente de restricciones:

```kotlin
enum class RestrictionType {
    PROHIBITED,  // No puede trabajar en la estación
    LIMITED,     // Puede trabajar con limitaciones
    TEMPORARY    // Restricción temporal con fecha de expiración
}

class RestrictionValidator @Inject constructor() {
    
    fun canWorkerBeAssigned(
        worker: Worker,
        workstation: Workstation,
        restrictions: List<WorkerRestriction>
    ): Boolean {
        val activeRestrictions = restrictions.filter { 
            it.workerId == worker.id && 
            it.workstationId == workstation.id &&
            isRestrictionActive(it)
        }
        
        return activeRestrictions.none { it.type == RestrictionType.PROHIBITED }
    }
}
```

## 🎨 Interfaz de Usuario

### Material Design 3
Implementación completa de Material Design 3:

```xml
<!-- Tema principal -->
<style name="Theme.REWS" parent="Theme.Material3.DayNight">
    <item name="colorPrimary">@color/primary</item>
    <item name="colorOnPrimary">@color/on_primary</item>
    <item name="colorSecondary">@color/secondary</item>
    <item name="colorSurface">@color/surface</item>
    <item name="android:windowBackground">@color/background</item>
</style>
```

### Componentes Reutilizables
Componentes UI modulares y reutilizables:

```kotlin
class WorkerCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {
    
    private val binding: ViewWorkerCardBinding
    
    init {
        binding = ViewWorkerCardBinding.inflate(
            LayoutInflater.from(context), this, true
        )
    }
    
    fun setWorker(worker: Worker) {
        binding.apply {
            textWorkerName.text = worker.name
            textAvailability.text = "${worker.availability}%"
            
            if (worker.isLeader) {
                cardWorker.setCardBackgroundColor(
                    ContextCompat.getColor(context, R.color.leader_background)
                )
                iconLeader.visibility = View.VISIBLE
            }
        }
    }
}
```

### Responsive Design
Soporte para diferentes tamaños de pantalla:

```
res/
├── layout/                 # Teléfonos (default)
├── layout-sw320dp/        # Pantallas pequeñas
├── layout-sw360dp/        # Pantallas medianas
├── layout-sw600dp/        # Tablets pequeñas
├── layout-sw720dp/        # Tablets grandes
└── layout-land/           # Orientación horizontal
```

## 🔄 Algoritmo de Rotación

### Estructura del Algoritmo

```kotlin
class RotationGenerator(
    private val workers: List<Worker>,
    private val workstations: List<Workstation>,
    private val restrictions: List<WorkerRestriction>
) {
    
    fun generateOptimalRotation(isFirstHalf: Boolean): RotationResult {
        val rotation = mutableMapOf<Long, MutableList<Worker>>()
        val availableWorkers = workers.toMutableList()
        
        // Fase 0.5: Líderes BOTH (solo en rotación actual)
        if (!isFirstHalf) {
            assignBothLeaders(rotation, availableWorkers)
        }
        
        // Fase 1: Líderes específicos por parte
        assignPartSpecificLeaders(rotation, availableWorkers, isFirstHalf)
        
        // Fase 2: Trabajadores en entrenamiento
        assignTrainingWorkers(rotation, availableWorkers)
        
        // Fase 3: Distribución regular
        distributeRemainingWorkers(rotation, availableWorkers)
        
        // Fase 4: Balanceado final
        balanceRotation(rotation)
        
        return RotationResult(rotation, generateStatistics(rotation))
    }
}
```

### Prioridades del Algoritmo

1. **Prioridad 1**: Líderes "BOTH" (prioridad absoluta)
2. **Prioridad 2**: Líderes específicos por parte
3. **Prioridad 3**: Parejas de entrenamiento
4. **Prioridad 4**: Trabajadores regulares

### Validaciones

```kotlin
class RotationValidator {
    
    fun validateRotation(rotation: Map<Long, List<Worker>>): ValidationResult {
        val errors = mutableListOf<ValidationError>()
        
        // Validar capacidades
        rotation.forEach { (workstationId, workers) ->
            val workstation = getWorkstationById(workstationId)
            if (workers.size > workstation.capacity && !hasLeaderOverride(workers)) {
                errors.add(ValidationError.CapacityExceeded(workstationId))
            }
        }
        
        // Validar restricciones
        validateRestrictions(rotation, errors)
        
        // Validar liderazgo
        validateLeadership(rotation, errors)
        
        return ValidationResult(errors.isEmpty(), errors)
    }
}
```

## 📊 Gestión de Estado

### Estados de UI
Manejo centralizado de estados:

```kotlin
sealed class UiState {
    object Loading : UiState()
    object Success : UiState()
    data class Error(val message: String) : UiState()
    object Empty : UiState()
}

sealed class RotationState {
    object Idle : RotationState()
    object Generating : RotationState()
    data class Generated(val rotation: RotationResult) : RotationState()
    data class Error(val error: String) : RotationState()
}
```

### LiveData y StateFlow
Observación reactiva de datos:

```kotlin
class RotationViewModel @Inject constructor(
    private val rotationRepository: RotationRepository
) : ViewModel() {
    
    private val _rotationState = MutableStateFlow<RotationState>(RotationState.Idle)
    val rotationState: StateFlow<RotationState> = _rotationState.asStateFlow()
    
    private val _currentRotation = MutableLiveData<RotationResult>()
    val currentRotation: LiveData<RotationResult> = _currentRotation
    
    fun generateRotation(isFirstHalf: Boolean) {
        viewModelScope.launch {
            _rotationState.value = RotationState.Generating
            
            try {
                val result = rotationRepository.generateRotation(isFirstHalf)
                _rotationState.value = RotationState.Generated(result)
                _currentRotation.value = result
            } catch (e: Exception) {
                _rotationState.value = RotationState.Error(e.message ?: "Unknown error")
            }
        }
    }
}
```

## 🧪 Testing

### Arquitectura de Testing

```
src/
├── test/                   # Unit Tests
│   ├── java/
│   │   ├── algorithms/     # Tests del algoritmo
│   │   ├── repositories/   # Tests de repositorios
│   │   ├── viewmodels/     # Tests de ViewModels
│   │   └── utils/          # Tests de utilidades
│   └── resources/
└── androidTest/            # Integration Tests
    └── java/
        ├── database/       # Tests de base de datos
        ├── ui/            # Tests de UI
        └── flows/         # Tests de flujos completos
```

### Unit Tests
Pruebas unitarias con MockK:

```kotlin
@ExtendWith(MockKExtension::class)
class RotationAlgorithmTest {
    
    @MockK
    private lateinit var workerRepository: WorkerRepository
    
    @MockK
    private lateinit var workstationRepository: WorkstationRepository
    
    private lateinit var rotationAlgorithm: RotationAlgorithm
    
    @BeforeEach
    fun setup() {
        rotationAlgorithm = RotationAlgorithm(
            workerRepository,
            workstationRepository,
            mockk()
        )
    }
    
    @Test
    fun `should assign BOTH leaders to their designated stations`() = runTest {
        // Given
        val workers = listOf(
            Worker(1, "Leader", 100, true, LeadershipType.BOTH)
        )
        val workstations = listOf(
            Workstation(1, "Station A", 2)
        )
        
        coEvery { workerRepository.getAllActiveWorkers() } returns workers
        coEvery { workstationRepository.getAllActiveWorkstations() } returns workstations
        
        // When
        val result = rotationAlgorithm.generateRotation(false)
        
        // Then
        assertThat(result.assignments[1L]).contains(workers[0])
    }
}
```

### Integration Tests
Pruebas de integración con Room:

```kotlin
@RunWith(AndroidJUnit4::class)
class AppDatabaseTest {
    
    private lateinit var database: AppDatabase
    private lateinit var workerDao: WorkerDao
    
    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).build()
        
        workerDao = database.workerDao()
    }
    
    @Test
    fun insertAndRetrieveWorker() = runTest {
        // Given
        val worker = Worker(1, "Test Worker", 100)
        
        // When
        workerDao.insertWorker(worker)
        val retrieved = workerDao.getAllActiveWorkers()
        
        // Then
        assertThat(retrieved).contains(worker)
    }
}
```

## ⚡ Rendimiento

### Optimizaciones Implementadas

1. **Database Indexing**:
```kotlin
@Entity(
    tableName = "workers",
    indices = [
        Index(value = ["isActive"]),
        Index(value = ["isLeader"]),
        Index(value = ["availability"])
    ]
)
```

2. **Lazy Loading**:
```kotlin
class WorkerAdapter : RecyclerView.Adapter<WorkerViewHolder>() {
    
    private val workers = mutableListOf<Worker>()
    
    fun updateWorkers(newWorkers: List<Worker>) {
        val diffCallback = WorkerDiffCallback(workers, newWorkers)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        
        workers.clear()
        workers.addAll(newWorkers)
        diffResult.dispatchUpdatesTo(this)
    }
}
```

3. **Memory Management**:
```kotlin
class RotationViewModel : ViewModel() {
    
    override fun onCleared() {
        super.onCleared()
        // Limpiar recursos
        rotationJob?.cancel()
        _rotationState.value = RotationState.Idle
    }
}
```

### Métricas de Rendimiento

- **Tiempo de inicio**: <2 segundos en dispositivos modernos
- **Generación de rotación**: <1 segundo para 100 trabajadores
- **Uso de memoria**: 50-100 MB durante operación normal
- **Tamaño de APK**: ~15-20 MB

## 🔒 Seguridad

### Principios de Seguridad

1. **Datos Locales**: Toda la información se almacena localmente
2. **Sin Red**: Funcionamiento completamente offline
3. **Permisos Mínimos**: Solo permisos esenciales
4. **Validación Robusta**: Validación de todas las entradas

### Implementación de Seguridad

```kotlin
class SecurityUtils {
    
    companion object {
        fun validateInput(input: String): Boolean {
            return input.isNotBlank() && 
                   input.length <= MAX_INPUT_LENGTH &&
                   !containsSqlInjection(input)
        }
        
        fun sanitizeInput(input: String): String {
            return input.trim()
                       .replace(Regex("[<>\"'&]"), "")
                       .take(MAX_INPUT_LENGTH)
        }
    }
}
```

### Análisis de Seguridad Automatizado

```yaml
# .github/workflows/security.yml
name: Security Scan
on: [push, pull_request]

jobs:
  security:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Run Snyk Security Scan
        uses: snyk/actions/gradle@master
        env:
          SNYK_TOKEN: ${{ secrets.SNYK_TOKEN }}
```

## 📈 Métricas y Monitoreo

### Logging
Sistema de logging estructurado:

```kotlin
class Logger {
    companion object {
        private const val TAG = "REWS"
        
        fun d(message: String, tag: String = TAG) {
            if (BuildConfig.DEBUG) {
                Log.d(tag, message)
            }
        }
        
        fun e(message: String, throwable: Throwable? = null, tag: String = TAG) {
            Log.e(tag, message, throwable)
            // En producción, enviar a sistema de crash reporting
        }
    }
}
```

### Performance Monitoring
Monitoreo de rendimiento:

```kotlin
class PerformanceMonitor {
    
    fun measureRotationGeneration(block: suspend () -> RotationResult): RotationResult {
        val startTime = System.currentTimeMillis()
        
        return runBlocking {
            val result = block()
            val duration = System.currentTimeMillis() - startTime
            
            Logger.d("Rotation generated in ${duration}ms")
            
            if (duration > PERFORMANCE_THRESHOLD) {
                Logger.w("Slow rotation generation: ${duration}ms")
            }
            
            result
        }
    }
}
```

## 🔮 Evolución Futura

### Arquitectura Modular
Preparación para modularización:

```
app/
├── core/                   # Módulo core
├── feature-workers/        # Módulo de trabajadores
├── feature-rotations/      # Módulo de rotaciones
├── feature-settings/       # Módulo de configuración
└── shared/                 # Recursos compartidos
```

### Microservicios
Preparación para arquitectura distribuida:

```kotlin
interface RotationService {
    suspend fun generateRotation(request: RotationRequest): RotationResponse
    suspend fun validateRotation(rotation: Rotation): ValidationResult
}

// Implementación local
class LocalRotationService : RotationService

// Implementación remota (futura)
class RemoteRotationService : RotationService
```

---

## 📚 Referencias y Recursos

### Documentación Técnica
- [Android Architecture Guide](https://developer.android.com/guide/components/activities/activity-lifecycle)
- [Room Database](https://developer.android.com/training/data-storage/room)
- [Material Design 3](https://m3.material.io/)
- [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html)

### Herramientas de Desarrollo
- **Android Studio**: IDE principal
- **Gradle**: Sistema de build
- **Git**: Control de versiones
- **GitHub Actions**: CI/CD

### Librerías Principales
- **Room**: Base de datos local
- **Lifecycle**: Componentes conscientes del ciclo de vida
- **Material Components**: Componentes de UI
- **Coroutines**: Programación asíncrona
- **Dagger Hilt**: Inyección de dependencias

---

**© 2024-2025 Brandon Josué Hidalgo Paz. Todos los derechos reservados.**

*Esta documentación corresponde a REWS v3.0.0 y será actualizada con cada versión mayor del sistema.*