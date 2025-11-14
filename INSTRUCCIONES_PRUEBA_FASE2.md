# 🧪 Instrucciones de Prueba - Fase 2

## 🚀 Inicio Rápido

### Opción 1: Script Automático (Recomendado)
```bash
probar-fase2.bat
```

### Opción 2: Manual
```bash
# Compilar
build-multiplatform.bat

# Ejecutar
run-desktop.bat
```

## 📋 Guía de Prueba Completa

### Prueba 1: Gestión de Trabajadores (5 min)

**Objetivo:** Verificar CRUD de trabajadores

1. **Abrir app**
   - Ejecutar `run-desktop.bat`
   - Esperar a que se abra la ventana

2. **Ir a Trabajadores**
   - Click en "Trabajadores"
   - Verificar pantalla vacía con mensaje

3. **Agregar trabajador**
   - Click en botón "+" (esquina superior derecha)
   - Ingresar:
     - Nombre: "Juan Pérez"
     - Código: "JP001"
   - Click "Agregar"
   - ✅ Verificar que aparece en la lista

4. **Agregar más trabajadores**
   - Repetir para:
     - "María García" - "MG002"
     - "Carlos López" - "CL003"
     - "Ana Martínez" - "AM004"
   - ✅ Verificar que todos aparecen

5. **Desactivar trabajador**
   - Click en el switch de "Juan Pérez"
   - ✅ Verificar que se pone gris

6. **Reactivar trabajador**
   - Click nuevamente en el switch
   - ✅ Verificar que vuelve a color normal

7. **Eliminar trabajador**
   - Click en icono de basura de "Ana Martínez"
   - ✅ Verificar que desaparece

8. **Volver al menú**
   - Click en flecha atrás
   - ✅ Verificar que regresa al menú principal

**Resultado esperado:** 3 trabajadores activos

---

### Prueba 2: Gestión de Estaciones (5 min)

**Objetivo:** Verificar CRUD de estaciones

1. **Ir a Estaciones**
   - Desde menú principal
   - Click en "Estaciones"

2. **Agregar estaciones**
   - Click en "+"
   - Agregar:
     - "Ensamblaje A" - "EST-001"
     - "Control de Calidad" - "EST-002"
     - "Empaque" - "EST-003"
     - "Almacén" - "EST-004"
   - ✅ Verificar que todas aparecen

3. **Verificar iconos**
   - ✅ Cada estación debe tener icono de herramienta
   - ✅ Código debe ser visible

4. **Desactivar estación**
   - Desactivar "Almacén"
   - ✅ Verificar que se pone gris

5. **Eliminar estación**
   - Eliminar "Empaque"
   - ✅ Verificar que desaparece

6. **Volver al menú**
   - Click en flecha atrás

**Resultado esperado:** 3 estaciones (2 activas, 1 inactiva)

---

### Prueba 3: Generación de Rotación (10 min)

**Objetivo:** Verificar generación de rotación

1. **Ir a Nueva Rotación**
   - Desde menú principal
   - Click en "Nueva Rotación"

2. **Verificar información**
   - ✅ Debe mostrar:
     - "Trabajadores activos: 3"
     - "Estaciones activas: 2"

3. **Configurar rotación**
   - Nombre: "Rotación Turno Mañana"
   - Intervalo: "60" minutos
   - ✅ Verificar que campos aceptan texto

4. **Generar rotación**
   - Click en "Generar Rotación"
   - ✅ Debe mostrar spinner "Generando..."
   - ✅ Debe aparecer tabla de resultados

5. **Verificar resultado**
   - ✅ Debe mostrar:
     - Nombre de la sesión
     - Intervalo configurado
     - Número de asignaciones
   - ✅ Tabla debe tener:
     - Columna "Rotación" (#1, #2, etc.)
     - Columna "Trabajador"
     - Columna "Estación"
   - ✅ Cada trabajador debe estar asignado

6. **Cerrar resultado**
   - Click en "X" o "Cerrar"
   - ✅ Debe volver al formulario

7. **Volver al menú**
   - Click en flecha atrás

**Resultado esperado:** Rotación generada exitosamente

---

### Prueba 4: Historial (5 min)

**Objetivo:** Verificar historial de rotaciones

1. **Ir a Historial**
   - Desde menú principal
   - Click en "Historial"

2. **Verificar lista**
   - ✅ Debe aparecer "Rotación Turno Mañana"
   - ✅ Debe mostrar:
     - Fecha de creación
     - Intervalo: "60 min"
     - Badge "ACTIVA"

3. **Actualizar historial**
   - Click en icono de actualizar (esquina superior)
   - ✅ Lista debe recargarse

4. **Volver al menú**
   - Click en flecha atrás

**Resultado esperado:** 1 rotación en historial

---

### Prueba 5: UI Adaptativa (5 min)

**Objetivo:** Verificar adaptación de UI

**En Desktop:**

1. **Redimensionar ventana**
   - Hacer ventana más pequeña
   - ✅ UI debe adaptarse

2. **Ir a Trabajadores**
   - ✅ En ventana grande: Grid de tarjetas
   - ✅ En ventana pequeña: Lista vertical

3. **Ir a Estaciones**
   - ✅ Mismo comportamiento adaptativo

**En Android (si tienes dispositivo):**

1. **Instalar app**
   ```bash
   ./gradlew :androidApp:installDebug
   ```

2. **Probar en móvil**
   - ✅ Debe mostrar listas verticales
   - ✅ FAB debe aparecer abajo a la derecha
   - ✅ Navegación debe funcionar

3. **Rotar dispositivo**
   - ✅ UI debe adaptarse a landscape

**Resultado esperado:** UI se adapta automáticamente

---

### Prueba 6: Validaciones (5 min)

**Objetivo:** Verificar validaciones

1. **Rotación sin trabajadores**
   - Eliminar todos los trabajadores
   - Ir a "Nueva Rotación"
   - ✅ Debe mostrar advertencia roja
   - ✅ Botón "Generar" debe estar deshabilitado

2. **Rotación sin estaciones**
   - Agregar trabajadores
   - Eliminar todas las estaciones
   - Ir a "Nueva Rotación"
   - ✅ Debe mostrar advertencia roja
   - ✅ Botón "Generar" debe estar deshabilitado

3. **Campos vacíos**
   - Intentar agregar trabajador sin nombre
   - ✅ Botón "Agregar" debe estar deshabilitado

4. **Intervalo inválido**
   - En rotación, intentar poner letras en intervalo
   - ✅ No debe permitir letras, solo números

**Resultado esperado:** Todas las validaciones funcionan

---

## 🐛 Problemas Comunes

### La app no compila
```bash
./gradlew clean
./gradlew build
```

### La app no se abre
```bash
# Verificar que Java 17+ está instalado
java -version

# Recompilar
./gradlew :desktopApp:build
./gradlew :desktopApp:run
```

### No aparecen los datos
- Los datos se guardan en: `~/.workstation_rotation/database.db`
- Para empezar de cero, eliminar ese archivo

### Error de base de datos
```bash
# Limpiar y recompilar
./gradlew clean
./gradlew :shared:build
./gradlew :desktopApp:run
```

## ✅ Checklist de Prueba

Marca cada item cuando lo pruebes:

- [ ] Agregar trabajador
- [ ] Editar trabajador (activar/desactivar)
- [ ] Eliminar trabajador
- [ ] Agregar estación
- [ ] Editar estación (activar/desactivar)
- [ ] Eliminar estación
- [ ] Generar rotación con datos válidos
- [ ] Ver resultado de rotación
- [ ] Ver historial
- [ ] Validación sin trabajadores
- [ ] Validación sin estaciones
- [ ] UI adaptativa (redimensionar ventana)
- [ ] Navegación entre pantallas
- [ ] Volver al menú desde cada pantalla

## 📊 Reporte de Prueba

Después de probar, completa:

**Fecha de prueba:** _______________

**Plataforma probada:**
- [ ] Desktop Windows
- [ ] Desktop macOS
- [ ] Desktop Linux
- [ ] Android

**Funcionalidades probadas:**
- [ ] Trabajadores: ___/4 funciones OK
- [ ] Estaciones: ___/4 funciones OK
- [ ] Rotación: ___/3 funciones OK
- [ ] Historial: ___/2 funciones OK

**Problemas encontrados:**
1. ___________________________________
2. ___________________________________
3. ___________________________________

**Calificación general:** ___/10

**Comentarios:**
_______________________________________
_______________________________________
_______________________________________

## 🎯 Resultado Esperado

Si todas las pruebas pasan:

✅ **Fase 2 verificada y funcional**

La app está lista para:
- Uso en producción (funciones básicas)
- Continuar con Fase 3 (funciones avanzadas)
- Distribución a usuarios beta

---

**Tiempo total de prueba:** ~35 minutos  
**Dificultad:** Fácil  
**Requisitos:** Desktop con Java 17+
