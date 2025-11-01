# 🧪 TEST DE CERTIFICACIÓN DE TRABAJADORES

## 📋 PROCEDIMIENTO DE TESTING

### 🎯 OBJETIVO
Verificar que al certificar un trabajador en entrenamiento, la estación de entrenamiento se activa correctamente en las rotaciones posteriores.

### 📝 PASOS DEL TEST

#### 1. **PREPARACIÓN**
```
1. Crear una estación de trabajo (ej: "Estación A")
2. Crear un entrenador y asignarlo a "Estación A"
3. Crear un trabajador en entrenamiento:
   - Asignar entrenador
   - Asignar "Estación A" como estación de entrenamiento
   - NO asignar "Estación A" en las estaciones regulares
```

#### 2. **VERIFICACIÓN PRE-CERTIFICACIÓN**
```
1. Generar rotación
2. Verificar que el entrenado aparece SOLO con su entrenador
3. Verificar que NO aparece en rotaciones individuales
```

#### 3. **PROCESO DE CERTIFICACIÓN**
```
1. Ir a WorkerActivity
2. Buscar el trabajador en entrenamiento
3. Hacer clic en "Certificar" 
4. Confirmar certificación
5. Verificar mensaje de éxito
```

#### 4. **VERIFICACIÓN POST-CERTIFICACIÓN**
```
1. Ir a RotationActivity
2. Generar nueva rotación
3. VERIFICAR: El trabajador certificado debe aparecer en "Estación A"
4. VERIFICAR: Ya no aparece como "en entrenamiento"
5. VERIFICAR: Aparece con icono de certificado 🏆
```

### 🔍 PUNTOS DE VERIFICACIÓN

#### ✅ **ANTES DE CERTIFICAR**
- [ ] Trabajador tiene `isTrainee = true`
- [ ] Trabajador tiene `isCertified = false`
- [ ] Trabajador tiene `trainerId` asignado
- [ ] Trabajador tiene `trainingWorkstationId` asignado
- [ ] Trabajador NO tiene la estación de entrenamiento en asignaciones regulares
- [ ] En rotaciones aparece SOLO con entrenador

#### ✅ **DESPUÉS DE CERTIFICAR**
- [ ] Trabajador tiene `isTrainee = false`
- [ ] Trabajador tiene `isCertified = true`
- [ ] Trabajador tiene `trainerId = null`
- [ ] Trabajador tiene `trainingWorkstationId = null`
- [ ] Trabajador SÍ tiene la estación de entrenamiento en asignaciones regulares
- [ ] En rotaciones puede aparecer individualmente en la estación de entrenamiento

### 🐛 DEBUGGING

#### **Si el trabajador NO aparece en la estación después de certificar:**

1. **Verificar logs de certificación:**
```
Buscar en logcat:
- "CERTIFICANDO TRABAJADOR"
- "ACTUALIZANDO TRABAJADOR CON ESTACIONES"
- "Caché del RotationViewModel limpiado"
```

2. **Verificar estado del trabajador:**
```
Usar método debugWorkerCertificationState() para ver:
- Estado de certificación
- Estaciones asignadas
- Datos de entrenamiento
```

3. **Verificar elegibilidad para rotación:**
```
Usar método debugWorkerEligibilityForRotation() para ver:
- Si el trabajador es elegible
- Qué estaciones tiene asignadas
- Estado del caché
```

### 📊 RESULTADOS ESPERADOS

#### **ANTES (Trabajador en entrenamiento)**
```
Rotación generada:
- Estación A: [Entrenador Juan, Entrenado Pedro] 🎯🤝
- Estación B: [Trabajador Ana, Trabajador Luis]
- Estación C: [Trabajador María]
```

#### **DESPUÉS (Trabajador certificado)**
```
Rotación generada:
- Estación A: [Pedro 🏆, Trabajador Ana]
- Estación B: [Entrenador Juan, Trabajador Luis]  
- Estación C: [Trabajador María]
```

### 🚨 CASOS EDGE A PROBAR

1. **Certificar trabajador sin estación de entrenamiento**
2. **Certificar trabajador que ya tiene la estación asignada**
3. **Certificar múltiples trabajadores consecutivamente**
4. **Generar rotación inmediatamente después de certificar**
5. **Certificar y luego editar el trabajador**

### 📝 NOTAS TÉCNICAS

- El caché del RotationViewModel se limpia automáticamente después de certificar
- Los logs detallados permiten seguir todo el proceso
- La certificación es irreversible desde la interfaz (pero se puede editar manualmente)
- La estación de entrenamiento se agrega automáticamente a las asignaciones regulares

---

**Desarrollado por**: Brandon Josué Hidalgo Paz  
**Versión**: 2.5.2  
**Fecha**: Noviembre 2025