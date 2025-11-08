# 🧪 INSTRUCCIONES DE PRUEBA - Corrección Rotación v4.0.8

---

## 📋 PREPARACIÓN

### Requisitos
- ✅ Android Studio instalado
- ✅ Dispositivo Android o emulador conectado
- ✅ ADB configurado en PATH
- ✅ Proyecto sincronizado con Gradle

### Verificar Dispositivo
```bash
adb devices
```

Debe mostrar al menos un dispositivo:
```
List of devices attached
emulator-5554    device
```

---

## 🚀 OPCIÓN 1: Script Automático (Recomendado)

### Windows
```bash
test-rotation-fix.bat
```

### Linux/Mac
```bash
chmod +x test-rotation-fix.sh
./test-rotation-fix.sh
```

El script automáticamente:
1. ✅ Limpia el proyecto
2. ✅ Compila la aplicación
3. ✅ Instala en el dispositivo
4. ✅ Muestra instrucciones de prueba

---

## 🔧 OPCIÓN 2: Pasos Manuales

### Paso 1: Limpiar Proyecto
```bash
./gradlew clean
```

### Paso 2: Compilar
```bash
./gradlew assembleDebug
```

### Paso 3: Instalar
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

## 📱 PRUEBAS EN LA APLICACIÓN

### Test 1: Verificar Inicialización de Datos

1. **Abrir la aplicación**
2. **Navegar a "Nueva Rotación"** (desde menú principal)
3. **Observar la pantalla**

**✅ Resultado Esperado:**
- Aparece mensaje "Inicializando sistema de rotación..."
- Luego "Creando datos de prueba..." (si es primera vez)
- Finalmente "Cargando sesión de rotación..."

**❌ Si falla:**
- Verificar logs: `adb logcat | grep DataInitService`

---

### Test 2: Verificar Estaciones Visibles

**✅ Resultado Esperado:**
- Se muestran **6 columnas de estaciones** en scroll horizontal
- Cada columna muestra:
  - Nombre de la estación
  - Capacidad requerida
  - Sección "Rotación Actual" (vacía inicialmente)
  - Sección "Siguiente Rotación" (vacía inicialmente)
  - Progreso 0/X

**Estaciones esperadas:**
1. 📍 Ensamblaje A (3 trabajadores)
2. 📍 Ensamblaje B (2 trabajadores)
3. 📍 Control de Calidad (2 trabajadores)
4. 📍 Empaque (2 trabajadores)
5. 📍 Mantenimiento (1 trabajador)
6. 📍 Almacén (2 trabajadores)

**❌ Si no aparecen:**
- Verificar logs: `adb logcat | grep NewRotationService`

---

### Test 3: Verificar Métricas en Header

**✅ Resultado Esperado:**
En el header superior debe mostrar:
- **Actual:** 0
- **Siguiente:** 0
- **Requeridos:** 24 (suma de todos los requeridos × 2)

**❌ Si muestra todo en 0:**
- Verificar logs: `adb logcat | grep NewRotationActivity`

---

### Test 4: Generar Rotación Automática

1. **Click en botón "Generar Automático"**
2. **Seleccionar opción** en el diálogo:
   - "Generar Actual"
   - "Generar Siguiente"
   - "Generar Ambas" ⭐ (recomendado)

**✅ Resultado Esperado:**
- Aparece mensaje "Generando rotación optimizada..."
- Después de 1-2 segundos: "Rotación generada: X asignaciones creadas"
- Las columnas de estaciones se llenan con trabajadores
- Las métricas se actualizan:
  - **Actual:** ~12 (si generaste actual)
  - **Siguiente:** ~12 (si generaste siguiente)
  - **Requeridos:** 24

**Trabajadores esperados:**
- 👤 Juan Pérez
- 👤 María García
- 👤 Carlos López
- 👤 Ana Martínez
- 👤 Pedro Rodríguez
- 👤 Laura Sánchez
- 👤 Miguel Torres
- 👤 Carmen Ruiz
- 👤 Roberto Díaz
- 👤 Isabel Moreno

**❌ Si no genera:**
- Verificar logs: `adb logcat | grep NewRotationService`
- Buscar errores: `adb logcat | grep -E "(ERROR|Exception)"`

---

### Test 5: Interacciones con Trabajadores

#### 5.1 Click en Trabajador Asignado

1. **Click en cualquier trabajador** en una estación
2. **Debe aparecer diálogo** con opciones:
   - Ver detalles
   - Mover a otra estación
   - Remover de rotación

**✅ Resultado Esperado:**
- Diálogo se muestra correctamente
- Opciones son clickeables

#### 5.2 Click en Slot Vacío

1. **Click en un espacio vacío** en una estación
2. **Debe aparecer mensaje** o diálogo para asignar trabajador

**✅ Resultado Esperado:**
- Toast o diálogo indicando que se puede asignar trabajador

---

### Test 6: Promover Rotación

1. **Generar "Siguiente Rotación"** si no está generada
2. **Click en botón "Siguiente → Actual"**
3. **Confirmar** en el diálogo

**✅ Resultado Esperado:**
- Mensaje "Promoviendo siguiente rotación..."
- La rotación "Siguiente" se mueve a "Actual"
- La sección "Siguiente" queda vacía
- Métricas se actualizan

---

### Test 7: Capturar Foto

1. **Click en botón "Capturar"**

**✅ Resultado Esperado:**
- Se captura una imagen del grid de rotación
- Mensaje "Foto guardada en la galería"
- Opción para ver la foto

---

### Test 8: Acciones Rápidas (FAB)

1. **Click en FAB "Acciones"** (esquina inferior derecha)
2. **Debe aparecer menú** con opciones:
   - Copiar Actual → Siguiente
   - Limpiar Rotación Actual
   - Limpiar Siguiente Rotación
   - Nueva Sesión
   - Ver Conflictos

**✅ Resultado Esperado:**
- Menú se muestra correctamente
- Todas las opciones son clickeables

---

## 🔍 VERIFICACIÓN DE LOGS

### Ver Logs en Tiempo Real

```bash
adb logcat | grep -E "(NewRotationService|NewRotationViewModel|NewRotationActivity|DataInitService)"
```

### Logs Esperados (Secuencia Completa)

```
D/DataInitService: ═══════════════════════════════════════════════════════
D/DataInitService: 🔧 CREANDO CAPACIDADES
D/DataInitService:   • Trabajadores: 10
D/DataInitService:   • Estaciones: 6
D/DataInitService: ✅ Capacidades creadas: 45
D/DataInitService: ✅ Capacidades insertadas en BD
D/DataInitService: ═══════════════════════════════════════════════════════

D/NewRotationService: ═══════════════════════════════════════════════════════
D/NewRotationService: 🔍 CONSTRUYENDO GRID DE ROTACIÓN
D/NewRotationService: ═══════════════════════════════════════════════════════
D/NewRotationService: 📊 Datos recibidos:
D/NewRotationService:   • Estaciones: 6
D/NewRotationService:   • Asignaciones: 0
D/NewRotationService:   • Trabajadores: 10
D/NewRotationService:   • Capacidades: 45
D/NewRotationService:   📍 Estación: Ensamblaje A (ID: 1, Req: 3)
D/NewRotationService:   📍 Estación: Ensamblaje B (ID: 2, Req: 2)
D/NewRotationService:   📍 Estación: Control de Calidad (ID: 3, Req: 2)
D/NewRotationService:   📍 Estación: Empaque (ID: 4, Req: 2)
D/NewRotationService:   📍 Estación: Mantenimiento (ID: 5, Req: 1)
D/NewRotationService:   📍 Estación: Almacén (ID: 6, Req: 2)
D/NewRotationService:   👤 Trabajador: Juan Pérez (ID: 1, Caps activas: 5)
D/NewRotationService:   👤 Trabajador: María García (ID: 2, Caps activas: 5)
D/NewRotationService:   👤 Trabajador: Carlos López (ID: 3, Caps activas: 4)
D/NewRotationService:   👤 Trabajador: Ana Martínez (ID: 4, Caps activas: 4)
D/NewRotationService:   👤 Trabajador: Pedro Rodríguez (ID: 5, Caps activas: 5)
D/NewRotationService:   👤 Trabajador: Laura Sánchez (ID: 6, Caps activas: 4)
D/NewRotationService:   👤 Trabajador: Miguel Torres (ID: 7, Caps activas: 4)
D/NewRotationService:   👤 Trabajador: Carmen Ruiz (ID: 8, Caps activas: 4)
D/NewRotationService:   👤 Trabajador: Roberto Díaz (ID: 9, Caps activas: 5)
D/NewRotationService:   👤 Trabajador: Isabel Moreno (ID: 10, Caps activas: 4)
D/NewRotationService: ═══════════════════════════════════════════════════════
D/NewRotationService: ✅ GRID CONSTRUIDO:
D/NewRotationService:   • Filas (estaciones): 6
D/NewRotationService:   • Trabajadores disponibles: 10
D/NewRotationService:   📍 Ensamblaje A: 0/3 actual, 0/3 siguiente
D/NewRotationService:   📍 Ensamblaje B: 0/2 actual, 0/2 siguiente
D/NewRotationService:   📍 Control de Calidad: 0/2 actual, 0/2 siguiente
D/NewRotationService:   📍 Empaque: 0/2 actual, 0/2 siguiente
D/NewRotationService:   📍 Mantenimiento: 0/1 actual, 0/1 siguiente
D/NewRotationService:   📍 Almacén: 0/2 actual, 0/2 siguiente
D/NewRotationService: ═══════════════════════════════════════════════════════

D/NewRotationViewModel: 🔍 Observando grid de rotación para sesión: 1
D/NewRotationViewModel: 📊 Grid recibido en ViewModel:
D/NewRotationViewModel:   • Filas: 6
D/NewRotationViewModel:   • Trabajadores: 10

D/NewRotationActivity: ═══════════════════════════════════════════════════════
D/NewRotationActivity: 🔄 ACTUALIZANDO GRID EN UI
D/NewRotationActivity: ═══════════════════════════════════════════════════════
D/NewRotationActivity: ✅ Grid recibido:
D/NewRotationActivity:   • Sesión: Sesión Inicial
D/NewRotationActivity:   • Filas: 6
D/NewRotationActivity:   • Trabajadores disponibles: 10
D/NewRotationActivity: ✅ Adapter actualizado con 6 estaciones
D/NewRotationActivity: ═══════════════════════════════════════════════════════
```

---

## ❌ TROUBLESHOOTING

### Problema: No aparecen estaciones

**Verificar:**
```bash
adb logcat | grep "NewRotationService.*Estaciones:"
```

**Debe mostrar:**
```
D/NewRotationService:   • Estaciones: 6
```

**Si muestra 0:**
- Limpiar datos de la app: `adb shell pm clear com.workstation.rotation`
- Reinstalar: `adb install -r app/build/outputs/apk/debug/app-debug.apk`

---

### Problema: No aparecen trabajadores

**Verificar:**
```bash
adb logcat | grep "NewRotationService.*Trabajadores:"
```

**Debe mostrar:**
```
D/NewRotationService:   • Trabajadores: 10
```

**Si muestra 0:**
- Verificar creación de datos: `adb logcat | grep DataInitService`

---

### Problema: Capacidades en 0

**Verificar:**
```bash
adb logcat | grep "Caps activas:"
```

**Debe mostrar:**
```
D/NewRotationService:   👤 Trabajador: Juan Pérez (ID: 1, Caps activas: 5)
```

**Si todas muestran 0:**
- Problema con `is_active` en capacidades
- Verificar: `adb logcat | grep "is_active"`

---

### Problema: Grid no se actualiza en UI

**Verificar:**
```bash
adb logcat | grep "NewRotationActivity.*ACTUALIZANDO GRID"
```

**Si no aparece:**
- `setupObservers()` no está activo
- Verificar código en `NewRotationActivity.kt`

---

## ✅ CHECKLIST DE PRUEBAS

- [ ] Aplicación compila sin errores
- [ ] Aplicación se instala correctamente
- [ ] Aparecen 6 estaciones en scroll horizontal
- [ ] Métricas muestran valores correctos
- [ ] Botón "Generar Automático" funciona
- [ ] Se crean asignaciones visibles
- [ ] Click en trabajador muestra opciones
- [ ] Click en slot vacío funciona
- [ ] Botón "Siguiente → Actual" funciona
- [ ] Botón "Capturar" guarda foto
- [ ] FAB "Acciones" muestra menú
- [ ] Logs muestran secuencia completa

---

## 📊 CRITERIOS DE ÉXITO

### ✅ PRUEBA EXITOSA SI:

1. **Datos Iniciales**
   - ✅ 6 estaciones visibles
   - ✅ 10 trabajadores disponibles
   - ✅ ~45 capacidades creadas

2. **Generación Automática**
   - ✅ Crea ~12 asignaciones por rotación
   - ✅ Trabajadores aparecen en estaciones
   - ✅ Métricas se actualizan correctamente

3. **Interacciones**
   - ✅ Clicks funcionan
   - ✅ Diálogos se muestran
   - ✅ Acciones se ejecutan

4. **Logs**
   - ✅ Secuencia completa visible
   - ✅ Sin errores o excepciones
   - ✅ Valores correctos en cada paso

---

## 📝 REPORTE DE RESULTADOS

Después de las pruebas, documentar:

```
FECHA: _______________
DISPOSITIVO: _______________
VERSIÓN ANDROID: _______________

RESULTADOS:
[ ] ✅ Todas las pruebas pasaron
[ ] ⚠️ Algunas pruebas fallaron (especificar abajo)
[ ] ❌ Pruebas críticas fallaron

DETALLES:
_________________________________
_________________________________
_________________________________

LOGS RELEVANTES:
_________________________________
_________________________________
_________________________________
```

---

**¡Buena suerte con las pruebas! 🚀**
