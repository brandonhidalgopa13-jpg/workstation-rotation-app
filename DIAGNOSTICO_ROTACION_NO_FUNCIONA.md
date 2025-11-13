# 🔍 DIAGNÓSTICO: Rotación No Funciona - Solo Aparecen 2 Trabajadores

## 📋 Problema Reportado
- Se crearon 5 trabajadores con todas las estaciones asignadas
- Solo aparecen 2 trabajadores (Maritza y Oscar) en la rotación
- Los trabajadores no están rotando entre estaciones

## 🔎 Análisis del Código

### 1. Flujo de Creación de Trabajadores
```kotlin
// WorkerViewModel.kt - línea 42
suspend fun insertWorkerWithWorkstations(worker: Worker, workstationIds: List<Long>) {
    val workerId = workerDao.insertWorker(worker)
    
    // Crear relaciones en worker_workstations
    workstationIds.forEach { workstationId ->
        workerDao.insertWorkerWorkstation(WorkerWorkstation(workerId, workstationId))
    }
    
    // CRÍTICO: Sincronizar capacidades
    syncWorkerCapabilities(workerId, workstationIds)
}
```

### 2. Filtro en Construcción del Grid
```kotlin
// NewRotationService.kt - línea 280
val availableWorkers = workers.mapNotNull { worker ->
    val workerCapabilities = capabilities.filter { 
        it.worker_id == worker.id && it.is_active 
    }
    
    // ⚠️ VALIDACIÓN: Excluir trabajadores sin capacidades activas
    if (workerCapabilities.isEmpty()) {
        android.util.Log.w("NewRotationService", "   ⚠️ EXCLUIDO - sin capacidades activas")
        return@mapNotNull null
    }
    
    // ... resto del código
}
```

### 3. Filtro en Generación de Rotación
```kotlin
// NewRotationService.kt - línea 540
val workersWithStations = capabilities
    .filter { it.canBeAssigned() }
    .map { it.worker_id }
    .distinct()
```

## 🐛 Posibles Causas

### Causa #1: Capacidades No Se Crearon
- Las capacidades no se sincronizaron correctamente al crear los trabajadores
- La función `syncWorkerCapabilities` falló silenciosamente

### Causa #2: Capacidades Están Inactivas
- Las capacidades se crearon pero con `is_active = false`
- El filtro las está excluyendo

### Causa #3: Nivel de Competencia Insuficiente
- Las capacidades tienen `competency_level` muy bajo
- La función `canBeAssigned()` las está rechazando

### Causa #4: Certificación Expirada
- Las capacidades requieren certificación válida
- La certificación está expirada o no existe

## 🔧 Solución Propuesta

### Paso 1: Verificar Estado de la Base de Datos
Necesitamos ejecutar consultas SQL para ver:
1. ¿Cuántos trabajadores existen?
2. ¿Cuántas relaciones worker_workstations existen?
3. ¿Cuántas capacidades (worker_workstation_capabilities) existen?
4. ¿Cuántas capacidades están activas?

### Paso 2: Forzar Resincronización
Crear una función que:
1. Lea todas las relaciones worker_workstations
2. Verifique que existan capacidades correspondientes
3. Cree las capacidades faltantes
4. Active las capacidades inactivas

### Paso 3: Ajustar Filtros
Si las capacidades existen pero no cumplen requisitos:
1. Reducir el nivel mínimo de competencia requerido
2. Permitir capacidades sin certificación
3. Hacer más permisiva la función `canBeAssigned()`

## 📝 Código de Diagnóstico

Voy a crear una función de diagnóstico que:
1. Muestre el estado completo de trabajadores y capacidades
2. Identifique exactamente qué está fallando
3. Ofrezca reparación automática

## 🎯 Próximos Pasos

1. Ejecutar diagnóstico completo
2. Identificar la causa raíz
3. Aplicar corrección específica
4. Verificar que la rotación funcione correctamente
