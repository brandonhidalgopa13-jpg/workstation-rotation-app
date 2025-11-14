# 🧪 INSTRUCCIONES PARA PROBAR LA ROTACIÓN CORREGIDA

## ✅ Corrección Aplicada - v4.0.20

Se ha corregido el **bug crítico** que impedía que los trabajadores rotaran entre estaciones.

### Problema Corregido
- ❌ **ANTES**: Los trabajadores aparecían en las mismas estaciones en ambas rotaciones
- ✅ **AHORA**: Los trabajadores rotan correctamente a estaciones diferentes

## 🧪 Pasos para Probar

### Paso 1: Limpiar Datos Anteriores (Recomendado)

Para empezar con datos limpios y ver la corrección claramente:

1. Abre la app
2. Ve a **Configuración** o usa el **DiagnosticActivity**
3. Presiona **"Resetear Capacidades"** o limpia la base de datos
4. Esto eliminará las asignaciones anteriores que estaban mal

### Paso 2: Crear Trabajadores de Prueba

Crea al menos 4-5 trabajadores con las siguientes características:

**Trabajador 1 - Carlos:**
- Estaciones: Anneling, Forming, Loop
- Nivel: Normal (no líder, no entrenador)

**Trabajador 2 - Oscar:**
- Estaciones: Anneling, Forming, Loop
- Nivel: Normal

**Trabajador 3 - Brandon:**
- Estaciones: Anneling, Forming, Loop
- Nivel: Normal

**Trabajador 4 - Kevin:**
- Estaciones: Anneling, Forming, Loop
- Nivel: Normal

**Trabajador 5 - María (Opcional):**
- Estaciones: Anneling, Forming, Loop
- Nivel: Normal

### Paso 3: Verificar Capacidades

**IMPORTANTE**: Después de crear cada trabajador, verifica en los logs que aparezca:

```
✅ Sincronización verificada correctamente
✅ Trabajador creado y sincronizado correctamente
```

Si no aparece, usa **DiagnosticActivity** → **"Reparar Sincronización"**

### Paso 4: Generar Primera Rotación

1. Abre **NewRotationActivity** (Sistema de Rotación v4.0)
2. Presiona **"Generar Rotación"**
3. Selecciona **"Generar Rotación Actual"**
4. Observa las asignaciones

**Ejemplo esperado:**
```
ROTACIÓN 1 - ACTUAL:
Anneling: Carlos, Kevin
Forming: Oscar
Loop: Brandon
```

### Paso 5: Generar Segunda Rotación

1. Presiona **"Generar Rotación"** nuevamente
2. Selecciona **"Generar Siguiente Rotación"**
3. **OBSERVA**: Los trabajadores deberían estar en estaciones DIFERENTES

**Ejemplo esperado:**
```
ROTACIÓN 2 - SIGUIENTE:
Anneling: Oscar, Brandon  ✅ DIFERENTES
Forming: Kevin            ✅ DIFERENTE
Loop: Carlos              ✅ DIFERENTE
```

### Paso 6: Verificar en Logs

Abre **Logcat** y filtra por `NewRotationService`. Deberías ver:

```
📊 VERIFICACIÓN DE ROTACIÓN:
  🆕 NUEVO Carlos → Forming
  🆕 NUEVO Oscar → Loop
  🆕 NUEVO Brandon → Anneling
  🆕 NUEVO Kevin → Forming
```

Los íconos indican:
- 🆕 **NUEVO**: El trabajador NO estuvo en esta estación antes (correcto)
- 🔁 **REPETIDO**: El trabajador SÍ estuvo en esta estación antes (solo si no hay otras opciones)

## 🔍 Qué Verificar

### ✅ Rotación Correcta
- [ ] Los trabajadores están en estaciones DIFERENTES entre rotaciones
- [ ] Cada trabajador rota a una nueva estación
- [ ] No hay trabajadores "pegados" a una estación

### ✅ Todos los Trabajadores Aparecen
- [ ] Los 4-5 trabajadores creados aparecen en la rotación
- [ ] No faltan trabajadores
- [ ] Cada estación tiene el número correcto de trabajadores

### ✅ Sistema de Prioridades
- [ ] Los líderes aparecen en sus estaciones designadas
- [ ] Las parejas de entrenamiento están juntas
- [ ] Los trabajadores normales rotan libremente

## 🐛 Si Aún No Funciona

### Problema: Trabajadores No Aparecen

**Solución:**
1. Abre **DiagnosticActivity**
2. Ejecuta diagnóstico
3. Si dice "⚠️ Trabajadores sin capacidades", presiona **"Reparar Sincronización"**
4. Vuelve a generar la rotación

### Problema: Trabajadores No Rotan

**Verificar en Logs:**
```
📊 Asignaciones previas (pares): X
  • Worker 1 -> Workstation 1
  • Worker 1 -> Workstation 2
  ...
```

Si ves `Asignaciones previas (pares): 0`, significa que no hay historial y es la primera rotación.

**Para probar rotación:**
1. Genera **Rotación Actual**
2. Genera **Siguiente Rotación**
3. Compara las asignaciones

### Problema: Logs No Aparecen

**Configurar Logcat:**
1. Abre Android Studio
2. Ve a Logcat
3. Filtra por: `NewRotationService`
4. Nivel: Debug o Verbose

## 📊 Ejemplo Completo de Prueba

### Escenario: 4 Trabajadores, 3 Estaciones

**Configuración:**
- Anneling: requiere 2 trabajadores
- Forming: requiere 1 trabajador
- Loop: requiere 1 trabajador

**Rotación 1 (Generada):**
```
Anneling: Carlos, Kevin
Forming: Oscar
Loop: Brandon
```

**Rotación 2 (Generada después):**
```
Anneling: Oscar, Brandon  ← Carlos y Kevin rotaron
Forming: Kevin            ← Oscar rotó
Loop: Carlos              ← Brandon rotó
```

**Verificación:**
- ✅ Carlos: Anneling → Loop (ROTÓ)
- ✅ Kevin: Anneling → Forming (ROTÓ)
- ✅ Oscar: Forming → Anneling (ROTÓ)
- ✅ Brandon: Loop → Anneling (ROTÓ)

## 🎯 Resultado Esperado

Después de aplicar la corrección:

1. **Los trabajadores ROTAN** entre estaciones
2. **Todos los trabajadores aparecen** en la rotación
3. **El sistema prioriza** trabajadores que NO estuvieron en una estación antes
4. **Los logs muestran** claramente quién es NUEVO y quién es REPETIDO

## 📝 Reportar Resultados

Si después de seguir estos pasos:

### ✅ Funciona Correctamente
Perfecto! El sistema está funcionando como se espera.

### ❌ Aún No Funciona
Por favor reporta:
1. Captura de pantalla de las 2 rotaciones
2. Logs de Logcat (filtro: `NewRotationService`)
3. Resultado del diagnóstico (DiagnosticActivity)
4. Número de trabajadores y estaciones creados

## 🔧 Comandos Útiles

### Limpiar y Reconstruir
```bash
./gradlew clean
./gradlew assembleDebug
```

### Ver Logs en Tiempo Real
```bash
adb logcat | grep "NewRotationService"
```

### Verificar Base de Datos
```bash
adb shell
run-as com.workstation.rotation
cd databases
sqlite3 workstation_rotation.db
SELECT * FROM worker_workstation_capabilities;
```

---

**Versión:** v4.0.20  
**Fecha:** 13/11/2025  
**Estado:** Corrección aplicada y subida a GitHub  
**Commit:** cb97484
