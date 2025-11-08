# 🔄 Corrección: Sincronización de Capacidades de Trabajadores

## 📋 Problema Identificado

Los trabajadores creados o editados en el sistema no aparecían en las rotaciones automáticas, incluso cuando tenían estaciones asignadas. Esto se debía a una **desincronización entre dos tablas**:

### Tablas Involucradas:

1. **`worker_workstations`** (Tabla Legacy)
   - Almacena las relaciones trabajador-estación
   - Se actualiza correctamente al crear/editar trabajadores
   - Usada por el sistema antiguo de rotaciones

2. **`worker_workstation_capabilities`** (Nueva Arquitectura v4.0)
   - Almacena capacidades detalladas de trabajadores por estación
   - Incluye nivel de competencia, certificaciones, etc.
   - **Usada por el nuevo sistema de rotaciones**
   - **NO se actualizaba automáticamente** ❌

### Causa Raíz:

Cuando se creaba o editaba un trabajador:
- ✅ Se actualizaba `worker_workstations`
- ❌ NO se creaban/actualizaban las entradas en `worker_workstation_capabilities`
- ❌ El servicio `NewRotationService` solo consulta `worker_workstation_capabilities`
- ❌ Resultado: Trabajadores "invisibles" para el sistema de rotaciones

---

## ✅ Solución Implementada

### 1. Modificación del `WorkerViewModel`

#### Cambios en el Constructor:
```kotlin
class WorkerViewModel(
    private val workerDao: WorkerDao,
    private val workstationDao: WorkstationDao,
    private val workerRestrictionDao: WorkerRestrictionDao,
    private val capabilityDao: WorkerWorkstationCapabilityDao  // ← NUEVO
) : ViewModel()
```

#### Nueva Función de Sincronización:
```kotlin
private suspend fun syncWorkerCapabilities(workerId: Long, workstationIds: List<Long>)
```

Esta función:
- ✅ Crea capacidades para estaciones nuevas
- ✅ Desactiva capacidades para estaciones removidas
- ✅ Reactiva capacidades existentes pero inactivas
- ✅ Asigna nivel de competencia según el estado del trabajador:
  - `BEGINNER` (1) → Trabajadores en entrenamiento
  - `BASIC` (2) → Trabajadores normales
  - `INTERMEDIATE` (3) → Trabajadores certificados
  - `ADVANCED` (4) → Entrenadores
- ✅ Configura flags de liderazgo y entrenamiento
- ✅ Registra logs detallados para debugging

#### Integración en Métodos Existentes:

**`insertWorkerWithWorkstations()`:**
```kotlin
suspend fun insertWorkerWithWorkstations(worker: Worker, workstationIds: List<Long>) {
    val workerId = workerDao.insertWorker(worker)
    
    // Insertar relaciones legacy
    workstationIds.forEach { workstationId ->
        workerDao.insertWorkerWorkstation(WorkerWorkstation(workerId, workstationId))
    }
    
    // SINCRONIZACIÓN AUTOMÁTICA ← NUEVO
    syncWorkerCapabilities(workerId, workstationIds)
}
```

**`updateWorkerWithWorkstations()`:**
```kotlin
suspend fun updateWorkerWithWorkstations(worker: Worker, workstationIds: List<Long>) {
    // ... actualizar trabajador y relaciones ...
    
    // SINCRONIZACIÓN AUTOMÁTICA ← NUEVO
    syncWorkerCapabilities(worker.id, workstationIds)
}
```

### 2. Actualización del `WorkerViewModelFactory`

```kotlin
class WorkerViewModelFactory(
    private val workerDao: WorkerDao,
    private val workstationDao: WorkstationDao,
    private val workerRestrictionDao: WorkerRestrictionDao,
    private val capabilityDao: WorkerWorkstationCapabilityDao  // ← NUEVO
) : ViewModelProvider.Factory
```

### 3. Actualización de Actividades

**`WorkerActivity.kt`:**
```kotlin
private val viewModel: WorkerViewModel by viewModels {
    WorkerViewModelFactory(
        AppDatabase.getDatabase(this).workerDao(),
        AppDatabase.getDatabase(this).workstationDao(),
        AppDatabase.getDatabase(this).workerRestrictionDao(),
        AppDatabase.getDatabase(this).workerWorkstationCapabilityDao()  // ← NUEVO
    )
}
```

**`SettingsActivity.kt`:**
```kotlin
val factory = WorkerViewModelFactory(
    database.workerDao(), 
    database.workstationDao(), 
    database.workerRestrictionDao(),
    database.workerWorkstationCapabilityDao()  // ← NUEVO
)
```

### 4. Utilidad de Sincronización Masiva

**Nuevo archivo:** `app/src/main/java/com/workstation/rotation/utils/CapabilitySyncUtil.kt`

#### Funciones Principales:

**`syncAllWorkerCapabilities(context: Context): SyncResult`**
- Sincroniza TODOS los trabajadores existentes
- Procesa cada trabajador individualmente
- Genera logs detallados del proceso
- Retorna estadísticas completas

**`needsSynchronization(context: Context): Boolean`**
- Verifica si hay trabajadores desincronizados
- Compara `worker_workstations` vs `worker_workstation_capabilities`
- Retorna `true` si se detecta desincronización

#### Clase de Resultado:
```kotlin
data class SyncResult(
    val workersProcessed: Int,
    val capabilitiesCreated: Int,
    val capabilitiesUpdated: Int,
    val capabilitiesDeactivated: Int,
    val errors: Int
)
```

### 5. Sincronización Automática en `MainActivity`

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    // ...
    
    // Verificar y sincronizar capacidades si es necesario
    checkAndSyncCapabilities()  // ← NUEVO
    
    setupUI()
    setupAnimations()
}

private fun checkAndSyncCapabilities() {
    GlobalScope.launch(Dispatchers.IO) {
        // Verificar si es necesario
        val needsSync = CapabilitySyncUtil.needsSynchronization(this@MainActivity)
        
        if (needsSync) {
            // Ejecutar sincronización
            val result = CapabilitySyncUtil.syncAllWorkerCapabilities(this@MainActivity)
            
            // Notificar al usuario
            if (result.totalChanges > 0) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@MainActivity,
                        "✅ Se sincronizaron ${result.totalChanges} capacidades",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}
```

---

## 🎯 Beneficios de la Solución

### ✅ Sincronización Automática
- Los trabajadores nuevos/editados se sincronizan automáticamente
- No requiere intervención manual
- Funciona en tiempo real

### ✅ Migración de Datos Existentes
- La sincronización masiva procesa trabajadores existentes
- Se ejecuta automáticamente al iniciar la app
- Solo se ejecuta si se detecta desincronización

### ✅ Inteligencia en Niveles de Competencia
- Asigna niveles apropiados según el estado del trabajador
- Respeta certificaciones y roles especiales
- Configura correctamente flags de liderazgo/entrenamiento

### ✅ Robustez y Logging
- Logs detallados en cada paso
- Manejo de errores individual por trabajador
- Estadísticas completas del proceso

### ✅ Compatibilidad Retroactiva
- Mantiene la tabla `worker_workstations` para compatibilidad
- No rompe funcionalidad existente
- Migración transparente

---

## 📊 Flujo de Sincronización

```
┌─────────────────────────────────────────────────────────────┐
│ CREAR/EDITAR TRABAJADOR                                     │
└─────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────┐
│ 1. Guardar en tabla 'workers'                               │
└─────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────┐
│ 2. Actualizar 'worker_workstations' (Legacy)                │
└─────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────┐
│ 3. SINCRONIZAR 'worker_workstation_capabilities' ← NUEVO    │
│    • Crear capacidades nuevas                               │
│    • Desactivar capacidades removidas                       │
│    • Reactivar capacidades existentes                       │
│    • Asignar niveles de competencia                         │
│    • Configurar flags (líder, entrenador)                   │
└─────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────┐
│ 4. Trabajador VISIBLE en sistema de rotaciones ✅           │
└─────────────────────────────────────────────────────────────┘
```

---

## 🔍 Verificación de la Solución

### Logs a Revisar:

**Al crear trabajador:**
```
WorkerViewModel: === CREANDO TRABAJADOR CON ESTACIONES ===
WorkerViewModel: Trabajador: Juan Pérez
WorkerViewModel: Estaciones a asignar: [1, 2, 3]
WorkerViewModel: Trabajador creado con ID: 5
WorkerViewModel: Relaciones worker_workstations creadas
WorkerViewModel: === SINCRONIZANDO CAPACIDADES ===
WorkerViewModel: ✅ Capacidad creada: Trabajador 5 -> Estación 1 (Nivel: 2)
WorkerViewModel: ✅ Capacidad creada: Trabajador 5 -> Estación 2 (Nivel: 2)
WorkerViewModel: ✅ Capacidad creada: Trabajador 5 -> Estación 3 (Nivel: 2)
WorkerViewModel: ✅ Sincronización completada exitosamente
```

**Al iniciar la app (si hay desincronización):**
```
MainActivity: 🔍 Verificando sincronización de capacidades...
MainActivity: ⚠️ Se detectó desincronización - iniciando sincronización automática...
CapabilitySyncUtil: 🔄 INICIANDO SINCRONIZACIÓN GLOBAL DE CAPACIDADES
CapabilitySyncUtil: 📊 Total de trabajadores: 10
CapabilitySyncUtil: ✅ SINCRONIZACIÓN COMPLETADA
CapabilitySyncUtil: • Trabajadores procesados: 10
CapabilitySyncUtil: • Capacidades creadas: 25
CapabilitySyncUtil: • Capacidades actualizadas: 5
```

### Consultas SQL para Verificar:

```sql
-- Verificar trabajadores con estaciones pero sin capacidades
SELECT w.id, w.name, 
       COUNT(DISTINCT ww.workstationId) as estaciones_asignadas,
       COUNT(DISTINCT wwc.workstation_id) as capacidades_activas
FROM workers w
LEFT JOIN worker_workstations ww ON w.id = ww.workerId
LEFT JOIN worker_workstation_capabilities wwc ON w.id = wwc.worker_id AND wwc.is_active = 1
GROUP BY w.id, w.name
HAVING estaciones_asignadas > 0 AND estaciones_asignadas != capacidades_activas;

-- Debería retornar 0 filas después de la sincronización
```

---

## 🚀 Próximos Pasos

### Para el Usuario:
1. ✅ Los trabajadores nuevos funcionarán automáticamente
2. ✅ Los trabajadores existentes se sincronizarán al abrir la app
3. ✅ Verificar que los trabajadores aparezcan en las rotaciones

### Para el Desarrollador:
1. ✅ Monitorear logs durante las primeras ejecuciones
2. ✅ Verificar que no haya errores de sincronización
3. ⚠️ Considerar agregar una opción manual de sincronización en Configuración
4. ⚠️ Evaluar deprecar completamente `worker_workstations` en futuras versiones

---

## 📝 Archivos Modificados

1. ✅ `app/src/main/java/com/workstation/rotation/viewmodels/WorkerViewModel.kt`
   - Agregado parámetro `capabilityDao`
   - Agregada función `syncWorkerCapabilities()`
   - Integrada sincronización en `insertWorkerWithWorkstations()`
   - Integrada sincronización en `updateWorkerWithWorkstations()`
   - Actualizado `WorkerViewModelFactory`

2. ✅ `app/src/main/java/com/workstation/rotation/WorkerActivity.kt`
   - Actualizado `WorkerViewModelFactory` con nuevo parámetro

3. ✅ `app/src/main/java/com/workstation/rotation/SettingsActivity.kt`
   - Actualizado `WorkerViewModelFactory` con nuevo parámetro

4. ✅ `app/src/main/java/com/workstation/rotation/MainActivity.kt`
   - Agregada función `checkAndSyncCapabilities()`
   - Integrada verificación automática en `onCreate()`

5. ✅ `app/src/main/java/com/workstation/rotation/utils/CapabilitySyncUtil.kt` (NUEVO)
   - Utilidad de sincronización masiva
   - Función de verificación de desincronización
   - Clase de resultado con estadísticas

---

## ✅ Conclusión

La corrección implementada resuelve completamente el problema de sincronización entre las tablas `worker_workstations` y `worker_workstation_capabilities`. Los trabajadores ahora:

- ✅ Se sincronizan automáticamente al crear/editar
- ✅ Aparecen correctamente en el sistema de rotaciones
- ✅ Tienen niveles de competencia apropiados
- ✅ Mantienen sus roles especiales (líder, entrenador)
- ✅ Se migran automáticamente si ya existían

**Estado:** ✅ IMPLEMENTADO Y FUNCIONAL

**Versión:** v4.0.8

**Fecha:** 2025-11-07
