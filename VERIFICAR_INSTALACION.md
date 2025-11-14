# ✅ Verificar Instalación y Compilación

## 🎯 Objetivo

Verificar que la migración a KMP se instaló correctamente y que puedes compilar y ejecutar la aplicación.

## 📋 Checklist de Verificación

### 1. Archivos Creados ✅

Verifica que existen estos archivos/carpetas:

```
✅ settings.gradle.kts
✅ build.gradle.kts
✅ gradle.properties
✅ shared/
   ✅ build.gradle.kts
   ✅ src/commonMain/
   ✅ src/androidMain/
   ✅ src/desktopMain/
   ✅ src/iosMain/
✅ androidApp/
   ✅ build.gradle.kts
   ✅ src/main/AndroidManifest.xml
✅ desktopApp/
   ✅ build.gradle.kts
   ✅ src/main/kotlin/
✅ run-desktop.bat
✅ build-multiplatform.bat
```

### 2. Sincronizar Gradle

**En Android Studio / IntelliJ IDEA:**

1. Abrir el proyecto
2. Esperar a que Gradle sincronice automáticamente
3. Si no sincroniza, hacer clic en "Sync Now" o:
   - File → Sync Project with Gradle Files

**Desde línea de comandos:**

```bash
./gradlew --refresh-dependencies
```

### 3. Compilar Módulo Shared

```bash
./gradlew :shared:build
```

**Resultado esperado:**
```
BUILD SUCCESSFUL in Xs
```

**Si hay errores:**
- Verificar que JDK 17+ está instalado
- Verificar conexión a internet (descarga dependencias)
- Limpiar y reintentar: `./gradlew clean :shared:build`

### 4. Compilar App Android

```bash
./gradlew :androidApp:assembleDebug
```

**Resultado esperado:**
```
BUILD SUCCESSFUL in Xs
```

**APK generado en:**
```
androidApp/build/outputs/apk/debug/androidApp-debug.apk
```

### 5. Compilar App Desktop

```bash
./gradlew :desktopApp:build
```

**Resultado esperado:**
```
BUILD SUCCESSFUL in Xs
```

### 6. Ejecutar en Desktop

```bash
# Windows
run-desktop.bat

# Linux/Mac
./gradlew :desktopApp:run
```

**Resultado esperado:**
- Se abre una ventana con la aplicación
- Ves el menú principal con 4 opciones
- Puedes navegar a "Trabajadores"

### 7. Instalar en Android

**Conectar dispositivo Android o iniciar emulador, luego:**

```bash
./gradlew :androidApp:installDebug
```

**Resultado esperado:**
- APK instalado en el dispositivo
- App aparece en el launcher
- Al abrir, ves el menú principal

## 🔧 Solución de Problemas Comunes

### Error: "Cannot find module 'shared'"

**Solución:**
```bash
./gradlew clean
./gradlew :shared:build
./gradlew build
```

### Error: "Unsupported class file major version"

**Causa:** JDK incorrecto

**Solución:**
1. Verificar versión de Java:
   ```bash
   java -version
   ```
2. Debe ser JDK 17 o superior
3. Configurar en Android Studio:
   - File → Project Structure → SDK Location
   - Seleccionar JDK 17+

### Error: "Could not resolve dependencies"

**Causa:** Problema de red o repositorios

**Solución:**
```bash
./gradlew --refresh-dependencies
./gradlew clean build
```

### Error: "Execution failed for task ':shared:compileKotlinAndroid'"

**Causa:** Error de sintaxis en código Kotlin

**Solución:**
1. Revisar el error específico en la consola
2. Verificar que todos los archivos .kt están correctos
3. Limpiar y recompilar:
   ```bash
   ./gradlew clean
   ./gradlew :shared:build
   ```

### Error: "No cached version of ... available for offline mode"

**Causa:** Gradle en modo offline

**Solución:**
1. Android Studio: File → Settings → Build → Gradle
2. Desmarcar "Offline work"
3. Sync Project

### Desktop no inicia

**Solución:**
```bash
# Ver logs detallados
./gradlew :desktopApp:run --info

# O compilar primero
./gradlew :desktopApp:build
./gradlew :desktopApp:run
```

## 🧪 Tests de Funcionalidad

### Test 1: Navegación Básica

1. ✅ Abrir app (Desktop o Android)
2. ✅ Ver menú principal con 4 opciones
3. ✅ Click en "Trabajadores"
4. ✅ Ver pantalla de trabajadores (vacía)
5. ✅ Click en botón "Volver"
6. ✅ Regresar al menú principal

### Test 2: Agregar Trabajador

1. ✅ Ir a "Trabajadores"
2. ✅ Click en botón "+" (FAB en móvil, botón en desktop)
3. ✅ Ver diálogo "Agregar Trabajador"
4. ✅ Ingresar nombre: "Juan Pérez"
5. ✅ Ingresar código: "JP001"
6. ✅ Click "Agregar"
7. ✅ Ver trabajador en la lista

### Test 3: Activar/Desactivar Trabajador

1. ✅ Tener al menos un trabajador
2. ✅ Click en el switch del trabajador
3. ✅ Ver que cambia de color (activo/inactivo)
4. ✅ Click nuevamente
5. ✅ Ver que vuelve al estado original

### Test 4: Eliminar Trabajador

1. ✅ Tener al menos un trabajador
2. ✅ Click en icono de eliminar (🗑️)
3. ✅ Trabajador desaparece de la lista

### Test 5: UI Adaptativa (Desktop)

1. ✅ Abrir en Desktop
2. ✅ Ir a "Trabajadores"
3. ✅ Agregar varios trabajadores (3-4)
4. ✅ Ver que se muestran en grid (no en lista)
5. ✅ Redimensionar ventana
6. ✅ Ver que el grid se adapta

## 📊 Resultados Esperados

### Compilación Exitosa

```
> Task :shared:compileKotlinAndroid
> Task :shared:compileKotlinDesktop
> Task :androidApp:assembleDebug
> Task :desktopApp:build

BUILD SUCCESSFUL in 45s
```

### Ejecución Exitosa

**Desktop:**
- Ventana de 1200x800 px
- Menú principal visible
- Navegación funcional

**Android:**
- App instalada
- Icono en launcher
- Funcionalidad básica operativa

## ✅ Checklist Final

Marca cada item cuando lo verifiques:

- [ ] Gradle sincroniza sin errores
- [ ] `./gradlew :shared:build` exitoso
- [ ] `./gradlew :androidApp:assembleDebug` exitoso
- [ ] `./gradlew :desktopApp:build` exitoso
- [ ] Desktop app ejecuta correctamente
- [ ] Android app instala correctamente
- [ ] Navegación funciona
- [ ] Agregar trabajador funciona
- [ ] Activar/desactivar funciona
- [ ] Eliminar trabajador funciona
- [ ] UI se adapta a diferentes tamaños

## 🎉 Si Todo Funciona

**¡Felicidades!** La migración a KMP está funcionando correctamente.

**Próximos pasos:**
1. Leer `SIGUIENTE_PASO_DESARROLLO.md`
2. Implementar WorkstationsScreen
3. Continuar con las demás pantallas

## 🆘 Si Algo No Funciona

1. **Revisar logs:** Buscar el error específico
2. **Limpiar proyecto:** `./gradlew clean`
3. **Invalidar caché:** File → Invalidate Caches / Restart
4. **Verificar JDK:** Debe ser 17+
5. **Verificar internet:** Necesario para descargar dependencias

**Si persisten los errores:**
- Copiar el mensaje de error completo
- Buscar en la documentación
- Preguntar con el error específico

## 📝 Notas

- La primera compilación tarda más (descarga dependencias)
- Compilaciones subsecuentes son más rápidas
- Desktop compila más rápido que Android
- Usa Desktop para desarrollo rápido, Android para testing final

---

**Última actualización:** 13 de Noviembre, 2025  
**Versión:** 5.0.0-alpha
