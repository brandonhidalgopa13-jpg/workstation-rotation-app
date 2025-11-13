# 🔧 CORRECCIÓN ALGORITMO DE ROTACIÓN v4.0.20

## 🐛 Problemas Identificados

### Problema 1: Los Trabajadores NO Rotan
**Síntoma:** Los mismos trabajadores aparecen en las mismas estaciones en ambas rotaciones

**Causa Raíz:**
```kotlin
// ❌ CÓDIGO ACTUAL (INCORRECTO)
val previousAssignmentMap = allPreviousAssignments
    .filter { it.is_active }
    .associate { it.worker_id to it.workstation_id }
```

**Por qué falla:**
- `associate` crea un Map que solo guarda **UNA** entrada por `worker_id`
- Si un trabajador tiene múltiples asignaciones, solo se guarda la última
- El algoritmo no puede detectar correctamente si el trabajador estuvo en una estación antes

**Ejemplo del problema:**
```
Trabajador Carlos (ID: 1):
- Asignación 1: Estación Anneling (ID: 1) - CURRENT
- Asignación 2: Estación Forming (ID: 2) - NEXT

previousAssignmentMap = { 1 -> 2 }  // ❌ Solo guarda la última!

Cuando genera NEXT para Anneling:
- Busca si Carlos estuvo en Anneling antes
- Compara: previousAssignmentMap[1] == 1  // false (porque guardó 2)
- Resultado: Carlos se considera "NUEVO" para Anneling
- ❌ Carlos se asigna a Anneling nuevamente!
```

### Problema 2: Trabajadores Nuevos No Aparecen
**Causa:** Las capacidades no se están sincronizando correctamente al crear trabajadores

### Problema 3: Sistema de Porcentaje No Se Aplica
**Causa:** El cálculo de probabilidad se hace pero no se usa para la selección

## ✅ Soluciones

### Solución 1: Corregir Detección de Asignaciones Previas

**Cambiar de Map a Set:**
```kotlin
// ✅ CÓDIGO CORRECTO
// Crear un Set de pares (worker_id, workstation_id) para detectar asignaciones previas
val previousAssignments = allPreviousAssignments
    .filter { it.is_active }
    .map { Pair(it.worker_id, it.workstation_id) }
    .toSet()

// Verificar si un trabajador estuvo en una estación antes
val wasHereBefore = previousAssignments.contains(Pair(worker.id, workstation.id))
```

**Por qué funciona:**
- Guarda TODAS las combinaciones trabajador-estación
- Puede detectar correctamente si un trabajador estuvo en una estación específica
- No pierde información por sobrescritura

### Solución 2: Mejorar Sincronización de Capacidades

**Verificar en WorkerViewModel:**
```kotlin
suspend fun insertWorkerWithWorkstations(worker: Worker, workstationIds: List<Long>) {
    val workerId = workerDao.insertWorker(worker)
    
    // Insertar relaciones
    workstationIds.forEach { workstationId ->
        workerDao.insertWorkerWorkstation(WorkerWorkstation(workerId, workstationId))
    }
    
    // CRÍTICO: Sincronizar capacidades
    syncWorkerCapabilities(workerId, workstationIds)
    
    // NUEVO: Verificar que se crearon correctamente
    val createdCapabilities = capabilityDao.getByWorker(workerId)
    if (createdCapabilities.size != workstationIds.size) {
        android.util.Log.e("WorkerViewModel", "❌ ERROR: Capacidades no sincronizadas correctamente")
        // Reintentar sincronización
        syncWorkerCapabilities(workerId, workstationIds)
    }
}
```

### Solución 3: Implementar Sistema de Porcentaje Real

**Opción A - Rotación Equitativa (Recomendado):**
```kotlin
// Distribuir trabajadores equitativamente entre estaciones
// Cada trabajador tiene la misma probabilidad de ser asignado a cualquier estación disponible

val workerRotationCount = mutableMapOf<Long, Int>()

workstations.forEach { workstation ->
    // Ordenar candidatos por menor cantidad de rotaciones
    val sortedCandidates = allCandidates.sortedBy { 
        workerRotationCount.getOrDefault(it.worker_id, 0) 
    }
    
    // Seleccionar los que tienen menos rotaciones
    val selected = sortedCandidates.take(needed)
    selected.forEach { candidate ->
        workerRotationCount[candidate.worker_id] = 
            workerRotationCount.getOrDefault(candidate.worker_id, 0) + 1
    }
}
```

**Opción B - Rotación Aleatoria Ponderada:**
```kotlin
// Asignar probabilidades basadas en historial
val weights = allCandidates.map { candidate ->
    val timesAssigned = previousAssignments.count { 
        it.first == candidate.worker_id 
    }
    // Menor peso para trabajadores con más asignaciones
    1.0 / (timesAssigned + 1.0)
}

// Seleccionar usando pesos
val selected = weightedRandomSelection(allCandidates, weights, needed)
```

## 🔧 Implementación de Correcciones

### Paso 1: Corregir NewRotationService.kt

```kotlin
// Línea ~600
// ANTES:
val previousAssignmentMap = allPreviousAssignments
    .filter { it.is_active }
    .associate { it.worker_id to it.workstation_id }

// DESPUÉS:
val previousAssignments = allPreviousAssignments
    .filter { it.is_active }
    .map { Pair(it.worker_id, it.workstation_id) }
    .toSet()

android.util.Log.d("NewRotationService", "📊 Asignaciones previas (pares): ${previousAssignments.size}")
previousAssignments.forEach { (workerId, workstationId) ->
    android.util.Log.d("NewRotationService", "  • Worker $workerId -> Workstation $workstationId")
}
```

```kotlin
// Línea ~635
// ANTES:
val candidatesNotHereBefore = allCandidates.filter { capability ->
    previousAssignmentMap[capability.worker_id] != workstation.id
}

val candidatesHereBefore = allCandidates.filter { capability ->
    previousAssignmentMap[capability.worker_id] == workstation.id
}

// DESPUÉS:
val candidatesNotHereBefore = allCandidates.filter { capability ->
    !previousAssignments.contains(Pair(capability.worker_id, workstation.id))
}

val candidatesHereBefore = allCandidates.filter { capability ->
    previousAssignments.contains(Pair(capability.worker_id, workstation.id))
}
```

```kotlin
// Línea ~685
// ANTES:
val wasHereBefore = previousAssignmentMap[candidate.worker_id] == workstation.id

// DESPUÉS:
val wasHereBefore = previousAssignments.contains(Pair(candidate.worker_id, workstation.id))
```

### Paso 2: Agregar Verificación en WorkerViewModel.kt

```kotlin
// Después de syncWorkerCapabilities()
private suspend fun verifyCapabilitiesCreated(workerId: Long, expectedCount: Int): Boolean {
    val capabilities = capabilityDao.getByWorker(workerId)
    val activeCapabilities = capabilities.filter { it.is_active }
    
    if (activeCapabilities.size != expectedCount) {
        android.util.Log.e("WorkerViewModel", 
            "❌ Capacidades no sincronizadas: esperadas=$expectedCount, creadas=${activeCapabilities.size}")
        return false
    }
    
    return true
}
```

### Paso 3: Agregar Logs Detallados

```kotlin
// En generateOptimizedRotation, después de crear asignaciones
android.util.Log.d("NewRotationService", "═══════════════════════════════════════")
android.util.Log.d("NewRotationService", "📊 RESUMEN DE ROTACIÓN GENERADA:")
android.util.Log.d("NewRotationService", "  • Total asignaciones: ${assignments.size}")
android.util.Log.d("NewRotationService", "  • Trabajadores únicos: ${assignedWorkers.size}")

// Verificar rotación por trabajador
assignedWorkers.forEach { workerId ->
    val worker = workers.find { it.id == workerId }
    val assignment = assignments.find { it.worker_id == workerId }
    val workstation = workstations.find { it.id == assignment?.workstation_id }
    
    val wasHereBefore = previousAssignments.contains(
        Pair(workerId, assignment?.workstation_id ?: 0)
    )
    
    val status = if (wasHereBefore) "🔁 REPETIDO" else "🆕 NUEVO"
    android.util.Log.d("NewRotationService", 
        "  $status ${worker?.name} → ${workstation?.name}")
}
```

## 📊 Verificación de Correcciones

### Test 1: Verificar Detección de Asignaciones Previas
```kotlin
@Test
fun `test detectar asignaciones previas correctamente`() {
    val assignments = listOf(
        RotationAssignment(worker_id = 1, workstation_id = 1, rotation_type = "CURRENT"),
        RotationAssignment(worker_id = 1, workstation_id = 2, rotation_type = "NEXT"),
        RotationAssignment(worker_id = 2, workstation_id = 1, rotation_type = "CURRENT")
    )
    
    // Método correcto
    val previousAssignments = assignments
        .map { Pair(it.worker_id, it.workstation_id) }
        .toSet()
    
    // Verificar
    assertTrue(previousAssignments.contains(Pair(1L, 1L)))
    assertTrue(previousAssignments.contains(Pair(1L, 2L)))
    assertTrue(previousAssignments.contains(Pair(2L, 1L)))
    assertEquals(3, previousAssignments.size)
}
```

### Test 2: Verificar Rotación Real
```kotlin
@Test
fun `test trabajadores rotan entre estaciones`() {
    // Generar rotación CURRENT
    val currentAssignments = generateRotation("CURRENT")
    
    // Generar rotación NEXT
    val nextAssignments = generateRotation("NEXT")
    
    // Verificar que los trabajadores están en estaciones diferentes
    currentAssignments.forEach { current ->
        val next = nextAssignments.find { it.worker_id == current.worker_id }
        assertNotNull(next)
        assertNotEquals(
            current.workstation_id, 
            next?.workstation_id,
            "Trabajador ${current.worker_id} debería rotar a otra estación"
        )
    }
}
```

## 🎯 Resultado Esperado

### Antes (Incorrecto):
```
ROTACIÓN 1:
- Carlos → Anneling
- Oscar → Forming
- Brandon → Loop

ROTACIÓN 2:
- Carlos → Anneling  ❌ NO ROTÓ
- Oscar → Forming    ❌ NO ROTÓ
- Brandon → Loop     ❌ NO ROTÓ
```

### Después (Correcto):
```
ROTACIÓN 1:
- Carlos → Anneling
- Oscar → Forming
- Brandon → Loop

ROTACIÓN 2:
- Carlos → Forming   ✅ ROTÓ
- Oscar → Loop       ✅ ROTÓ
- Brandon → Anneling ✅ ROTÓ
```

## 📝 Checklist de Implementación

- [ ] Cambiar `previousAssignmentMap` a `previousAssignments` (Set de Pares)
- [ ] Actualizar todas las referencias a `previousAssignmentMap`
- [ ] Agregar logs detallados de asignaciones previas
- [ ] Agregar verificación de capacidades en WorkerViewModel
- [ ] Crear tests de verificación
- [ ] Probar con datos reales
- [ ] Documentar cambios
- [ ] Subir a GitHub

## 🚀 Próximos Pasos

1. Implementar correcciones en NewRotationService.kt
2. Agregar verificación en WorkerViewModel.kt
3. Crear tests unitarios
4. Probar con escenario real
5. Documentar resultados
6. Subir cambios

---

**Versión:** v4.0.20  
**Fecha:** 13/11/2025  
**Prioridad:** CRÍTICA  
**Estado:** Pendiente de implementación
