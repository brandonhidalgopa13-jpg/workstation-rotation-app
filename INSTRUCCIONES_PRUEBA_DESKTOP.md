# 🖥️ Instrucciones de Prueba - Aplicación Desktop

**Fecha:** 13 de noviembre de 2025  
**Versión:** 5.0.0  
**Plataforma:** Windows Desktop

---

## 🚀 Aplicación Iniciada

La aplicación de escritorio se está ejecutando en segundo plano.

### Comando ejecutado:
```cmd
.\gradlew :desktopApp:run
```

---

## ✅ Funcionalidades para Probar

### 1. Pantalla de Trabajadores (Tab Inicial)

**Verificar:**
- ✅ Se muestra la lista vacía con mensaje "No hay trabajadores"
- ✅ Botón flotante "+" visible en la esquina inferior derecha
- ✅ TopAppBar con título "Trabajadores"
- ✅ NavigationBar en la parte inferior con 2 tabs

**Acciones a probar:**
1. **Agregar Trabajador:**
   - Click en botón "+"
   - Se abre diálogo "Agregar Trabajador"
   - Llenar campos:
     - Nombre: "Juan Pérez"
     - ID Empleado: "EMP001"
   - Click en "Agregar"
   - Verificar que aparece en la lista

2. **Ver Trabajador:**
   - Verificar que se muestra:
     - Nombre del trabajador
     - ID del empleado
     - Estado (activo/inactivo)

3. **Eliminar Trabajador:**
   - Click en icono 🗑️
   - Verificar que desaparece de la lista

4. **Agregar Múltiples:**
   - Agregar 3-5 trabajadores
   - Verificar que todos aparecen en la lista
   - Verificar scroll si es necesario

### 2. Pantalla de Estaciones

**Cambiar de tab:**
- Click en tab "Estaciones" (🏭) en NavigationBar

**Verificar:**
- ✅ Se muestra la lista vacía con mensaje "No hay estaciones"
- ✅ Botón flotante "+" visible
- ✅ TopAppBar con título "Estaciones"

**Acciones a probar:**
1. **Agregar Estación:**
   - Click en botón "+"
   - Se abre diálogo "Agregar Estación"
   - Llenar campos:
     - Nombre: "Ensamblaje A"
     - Código: "EST-001"
     - Descripción: "Línea de ensamblaje principal" (opcional)
   - Click en "Agregar"
   - Verificar que aparece en la lista

2. **Ver Estación:**
   - Verificar que se muestra:
     - Nombre de la estación
     - Código
     - Descripción (si se agregó)
     - Trabajadores requeridos: 1

3. **Eliminar Estación:**
   - Click en icono 🗑️
   - Verificar que desaparece de la lista

### 3. Navegación

**Probar:**
- ✅ Cambiar entre tabs "Trabajadores" y "Estaciones"
- ✅ Verificar que los datos persisten al cambiar de tab
- ✅ Verificar que el tab activo se resalta

---

## 🎨 Aspectos Visuales a Verificar

### Material 3 Design
- ✅ Colores del tema Material 3
- ✅ Tipografía correcta
- ✅ Espaciado consistente
- ✅ Sombras en Cards
- ✅ Animaciones suaves

### Componentes
- ✅ Cards con bordes redondeados
- ✅ FloatingActionButton circular
- ✅ NavigationBar con indicador de selección
- ✅ Diálogos modales centrados
- ✅ TextFields con outline
- ✅ Botones con colores del tema

### Responsividad
- ✅ Ventana redimensionable
- ✅ Contenido se adapta al tamaño
- ✅ Scroll funciona correctamente

---

## 🗄️ Base de Datos

**Ubicación:**
```
C:\Users\[TuUsuario]\.workstation-rotation\workstation_rotation.db
```

**Verificar:**
- ✅ Se crea automáticamente al iniciar
- ✅ Los datos persisten al cerrar y reabrir la app
- ✅ SQLite funciona correctamente

---

## 🐛 Posibles Problemas y Soluciones

### Problema 1: La ventana no se abre
**Solución:**
- Verificar que no hay errores en el output
- Revisar que Java 17 esté instalado
- Intentar cerrar y reiniciar

### Problema 2: Error al agregar datos
**Solución:**
- Verificar que los campos no estén vacíos
- Revisar que el ID sea único
- Verificar permisos de escritura en carpeta de usuario

### Problema 3: Los datos no persisten
**Solución:**
- Verificar que la base de datos se creó
- Revisar permisos de la carpeta `.workstation-rotation`
- Verificar que no hay errores de SQLite

---

## 📊 Casos de Prueba

### Test 1: CRUD Completo de Trabajadores
1. ✅ Agregar 3 trabajadores
2. ✅ Verificar que aparecen en la lista
3. ✅ Eliminar 1 trabajador
4. ✅ Verificar que quedan 2
5. ✅ Cerrar y reabrir app
6. ✅ Verificar que los 2 trabajadores siguen ahí

### Test 2: CRUD Completo de Estaciones
1. ✅ Agregar 3 estaciones
2. ✅ Verificar que aparecen en la lista
3. ✅ Eliminar 1 estación
4. ✅ Verificar que quedan 2
5. ✅ Cerrar y reabrir app
6. ✅ Verificar que las 2 estaciones siguen ahí

### Test 3: Navegación y Persistencia
1. ✅ Agregar 2 trabajadores
2. ✅ Cambiar a tab Estaciones
3. ✅ Agregar 2 estaciones
4. ✅ Volver a tab Trabajadores
5. ✅ Verificar que los 2 trabajadores siguen ahí
6. ✅ Cambiar a Estaciones
7. ✅ Verificar que las 2 estaciones siguen ahí

### Test 4: Validación de Formularios
1. ✅ Intentar agregar trabajador sin nombre
2. ✅ Verificar que botón "Agregar" está deshabilitado
3. ✅ Llenar nombre pero no ID
4. ✅ Verificar que botón sigue deshabilitado
5. ✅ Llenar ambos campos
6. ✅ Verificar que botón se habilita

---

## 🎯 Resultados Esperados

### Funcionalidad
- ✅ Todas las operaciones CRUD funcionan
- ✅ Los datos persisten correctamente
- ✅ La navegación es fluida
- ✅ No hay crashes ni errores

### Performance
- ✅ La app inicia en menos de 10 segundos
- ✅ Las operaciones son instantáneas
- ✅ No hay lag al cambiar de tabs
- ✅ El scroll es suave

### UX
- ✅ La interfaz es intuitiva
- ✅ Los diálogos son claros
- ✅ Los mensajes de error son útiles
- ✅ La navegación es obvia

---

## 📝 Reporte de Pruebas

### Formato de Reporte:
```
Fecha: [fecha]
Versión: 5.0.0
Plataforma: Windows Desktop

Pruebas Realizadas:
- [ ] Test 1: CRUD Trabajadores
- [ ] Test 2: CRUD Estaciones
- [ ] Test 3: Navegación
- [ ] Test 4: Validación

Problemas Encontrados:
1. [Descripción del problema]
2. [Descripción del problema]

Observaciones:
- [Observación 1]
- [Observación 2]

Estado General: ✅ APROBADO / ⚠️ CON OBSERVACIONES / ❌ RECHAZADO
```

---

## 🔧 Comandos Útiles

### Detener la aplicación:
```cmd
# Cerrar la ventana o presionar Ctrl+C en la terminal
```

### Reiniciar la aplicación:
```cmd
.\gradlew :desktopApp:run
```

### Ver logs:
```cmd
# Los logs aparecen en la terminal donde se ejecutó el comando
```

### Limpiar base de datos:
```cmd
# Eliminar la carpeta:
rmdir /s /q %USERPROFILE%\.workstation-rotation
```

---

**¡Disfruta probando la aplicación KMP!** 🎉
