# Instrucciones de Prueba - v4.0.17

## 🎯 Objetivo

Verificar que los tres problemas críticos han sido corregidos:
1. ✅ Los trabajadores rotan entre estaciones
2. ✅ Los nuevos trabajadores aparecen en rotaciones
3. ✅ Los líderes son asignados a sus estaciones designadas

## 📱 Preparación

### 1. Instalar APK
```bash
# Conectar dispositivo Android por USB
adb install -r app/build/outputs/apk/release/app-release-unsigned.apk
```

### 2. Habilitar Logs
```bash
# En una terminal separada, ejecutar:
adb logcat | grep "NewRotationService\|WorkerViewModel"
```

Esto mostrará todos los logs detallados del sistema de rotación.

## 🧪 Prueba 1: Crear Nuevo Trabajador

### Objetivo
Verificar que los nuevos trabajadores aparecen en las rotaciones generadas.

### Pasos
1. **Abrir la aplicación**
2. **Ir a "Trabajadores"**
3. **Crear nuevo trabajador**:
   - Nombre: "Prueba Rotación"
   - ID Empleado: "TEST001"
   - Asignar 3 estaciones diferentes
   - NO marcar como líder
   - Guardar

4. **Verificar en Logcat**:
   ```
   ═══════════════════════════════════════════
   🆕 CREANDO TRABAJADOR CON ESTACIONES
   ═══════════════════════════════════════════
   Trabajador: Prueba Rotación
   Estaciones a asignar: [1, 2, 3]
   ✅ Trabajador creado con ID: X
   ✅ Relaciones worker_workstations creadas: 3
   📊 Verificación de capacidades:
     • Capacidades activas: 3
     • Estaciones asignadas: 3
   ✅ Sincronización verificada correctamente
   ```

5. **Ir a "Nueva Rotación"**
6. **Generar Rotación Actual**
7. **Buscar al trabajador "Prueba Rotación"**

### ✅ Resultado Esperado
- El trabajador aparece en UNA de las 3 estaciones asignadas
- En Logcat se ve: `✅ Asignado: Prueba Rotación`

### ❌ Si Falla
- Verificar en Logcat si hay mensaje: `⚠️ Trabajador excluido - sin capacidades activas`
- Verificar que las 3 estaciones están activas
- Verificar que el trabajador está activo

## 🧪 Prueba 2: Crear Líder

### Objetivo
Verificar que los líderes son asignados a sus estaciones designadas.

### Pasos
1. **Ir a "Trabajadores"**
2. **Crear nuevo trabajador líder**:
   - Nombre: "Líder Prueba"
   - ID Empleado: "LEAD001"
   - Asignar 5 estaciones diferentes
   - ✅ Marcar como "Es Líder"
   - Seleccionar estación de liderazgo: "Estación 1"
   - Tipo de liderazgo: "Ambas rotaciones"
   - Guardar

3. **Verificar en Logcat**:
   ```
   🆕 CREANDO TRABAJADOR CON ESTACIONES
   Trabajador: Líder Prueba
     • Es líder: true
     • Estación de liderazgo: 1
     • Tipo de liderazgo: BOTH
   ...
   ✅ Capacidad creada: Trabajador X -> Estación 1
      • Puede ser líder: true
   ```

4. **Ir a "Nueva Rotación"**
5. **Generar Rotación Actual**

6. **Verificar en Logcat**:
   ```
   ═══ PASO 1: ASIGNANDO LÍDERES ═══
   🔍 Procesando líder: Líder Prueba (ID: X)
     • Estación designada: 1
     • Capacidad encontrada: true
     • Puede ser líder: true
     • Debe estar en esta rotación: true
   ✅ 👑 LÍDER ASIGNADO: Líder Prueba → Estación 1
   ```

7. **Buscar al líder en la rotación**

### ✅ Resultado Esperado
- El líder aparece SIEMPRE en "Estación 1"
- NO aparece en ninguna otra estación
- En Logcat se ve: `✅ 👑 LÍDER ASIGNADO`

### ❌ Si Falla
- Verificar que la estación de liderazgo está activa
- Verificar que el tipo de liderazgo es correcto
- Verificar en Logcat si hay: `⚠️ Líder no tiene capacidad para su estación designada`

## 🧪 Prueba 3: Rotación Múltiple

### Objetivo
Verificar que los trabajadores rotan entre estaciones en diferentes generaciones.

### Pasos
1. **Ir a "Nueva Rotación"**
2. **Generar Rotación Actual**
3. **Anotar las asignaciones**:
   - Trabajador A → Estación X
   - Trabajador B → Estación Y
   - Trabajador C → Estación Z

4. **Limpiar Rotación Actual**:
   - Menú → "Limpiar Rotación Actual"

5. **Generar Rotación Actual nuevamente**

6. **Verificar en Logcat**:
   ```
   🎲 Rotación balanceada:
     • Total candidatos: N
     • Probabilidad por candidato: X%
   ✅ Asignado: Trabajador A
   ✅ Asignado: Trabajador B
   ✅ Asignado: Trabajador C
   ```

7. **Comparar las nuevas asignaciones con las anteriores**

### ✅ Resultado Esperado
- Al menos el 50% de los trabajadores están en estaciones DIFERENTES
- Los trabajadores con múltiples estaciones rotan entre ellas
- Los líderes SIEMPRE están en sus estaciones designadas

### ❌ Si Falla
- Verificar que los trabajadores tienen múltiples estaciones asignadas
- Verificar en Logcat el porcentaje de probabilidad por candidato
- Si todos están en las mismas estaciones, hay un problema con `shuffled()`

## 🧪 Prueba 4: Trabajador con 5 Estaciones

### Objetivo
Verificar el sistema de probabilidades equitativas (20% cada estación).

### Pasos
1. **Crear trabajador con 5 estaciones**:
   - Nombre: "Multi Estación"
   - Asignar estaciones: 1, 2, 3, 4, 5

2. **Generar 10 rotaciones diferentes**:
   - Generar rotación
   - Anotar en qué estación aparece "Multi Estación"
   - Limpiar rotación
   - Repetir 10 veces

3. **Contar apariciones**:
   - Estación 1: X veces
   - Estación 2: X veces
   - Estación 3: X veces
   - Estación 4: X veces
   - Estación 5: X veces

### ✅ Resultado Esperado
- El trabajador aparece en TODAS las 5 estaciones al menos 1 vez
- La distribución es aproximadamente equitativa (±20% por estación)
- En Logcat se ve: `Probabilidad por candidato: 20%`

### ❌ Si Falla
- Si aparece siempre en la misma estación, el algoritmo no está rotando
- Si no aparece en alguna estación, verificar que está activa

## 📊 Tabla de Resultados

| Prueba | Estado | Notas |
|--------|--------|-------|
| 1. Nuevo Trabajador | ⬜ | |
| 2. Líder | ⬜ | |
| 3. Rotación Múltiple | ⬜ | |
| 4. 5 Estaciones | ⬜ | |

Marcar con:
- ✅ Pasó
- ❌ Falló
- ⚠️ Parcial

## 🔍 Comandos Útiles de Diagnóstico

### Ver logs en tiempo real
```bash
adb logcat | grep "NewRotationService\|WorkerViewModel"
```

### Ver solo logs de creación de trabajadores
```bash
adb logcat | grep "CREANDO TRABAJADOR"
```

### Ver solo logs de generación de rotación
```bash
adb logcat | grep "GENERANDO ROTACIÓN"
```

### Ver solo logs de líderes
```bash
adb logcat | grep "LÍDER"
```

### Limpiar logs
```bash
adb logcat -c
```

## 🐛 Problemas Comunes

### Problema: Trabajador no aparece en rotación
**Solución**:
1. Verificar que el trabajador está activo
2. Verificar que tiene al menos 1 estación asignada
3. Verificar en Logcat: `Capacidades activas: X`
4. Si es 0, hay problema de sincronización

### Problema: Líder no va a su estación
**Solución**:
1. Verificar que la estación de liderazgo está activa
2. Verificar en Logcat: `Puede ser líder: true`
3. Si es false, la capacidad no se configuró correctamente

### Problema: Trabajadores no rotan
**Solución**:
1. Verificar que tienen múltiples estaciones asignadas
2. Verificar en Logcat: `Probabilidad por candidato: X%`
3. Si siempre es 100%, solo tienen 1 estación

## 📝 Reporte de Resultados

Después de completar las pruebas, reportar:

1. **Pruebas que pasaron**: X/4
2. **Pruebas que fallaron**: X/4
3. **Logs relevantes**: (copiar de Logcat)
4. **Capturas de pantalla**: (si hay problemas visuales)
5. **Observaciones adicionales**: (cualquier comportamiento inesperado)

## ✅ Criterios de Aceptación

Para considerar la versión 4.0.17 como exitosa:

- ✅ Todas las pruebas (1-4) deben pasar
- ✅ Los logs deben mostrar mensajes de éxito (✅)
- ✅ No debe haber warnings (⚠️) en Logcat
- ✅ No debe haber errores (❌) en Logcat

---

**Versión**: 4.0.17  
**Fecha**: 12/11/2025  
**Tiempo estimado de pruebas**: 30-45 minutos
