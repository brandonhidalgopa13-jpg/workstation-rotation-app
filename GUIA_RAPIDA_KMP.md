# Guía Rápida - Kotlin Multiplatform

## 🚀 Inicio Rápido

### 1. Compilar Todo
```bash
# Windows
build-multiplatform.bat

# Linux/Mac
./build-multiplatform.sh
```

### 2. Ejecutar Desktop
```bash
# Windows
run-desktop.bat

# Linux/Mac
./gradlew :desktopApp:run
```

### 3. Instalar en Android
```bash
./gradlew :androidApp:installDebug
```

## 📱 Estructura Simplificada

```
shared/          → Código compartido (funciona en todas las plataformas)
androidApp/      → App Android
desktopApp/      → App Windows/Mac/Linux
iosApp/          → App iOS (próximamente)
```

## ✏️ Cómo Agregar Funcionalidad

### 1. Crear Modelo (shared/domain/models/)
```kotlin
@Serializable
data class MiModelo(
    val id: Long,
    val nombre: String
)
```

### 2. Crear Tabla SQL (shared/sqldelight/)
```sql
CREATE TABLE MiTabla (
    id INTEGER PRIMARY KEY,
    nombre TEXT NOT NULL
);
```

### 3. Crear ViewModel (shared/presentation/viewmodels/)
```kotlin
class MiViewModel(repository: Repository) : ViewModel() {
    // Lógica aquí
}
```

### 4. Crear Pantalla (shared/presentation/screens/)
```kotlin
@Composable
fun MiPantalla() {
    // UI aquí - funciona en Android, iOS y Desktop
}
```

## 🎨 UI Adaptativa Automática

El mismo código se adapta a diferentes pantallas:

```kotlin
@Composable
fun MiPantalla(windowSize: WindowSizeClass) {
    if (windowSize == WindowSizeClass.Expanded) {
        // Layout para PC (horizontal, grid)
    } else {
        // Layout para móvil (vertical, lista)
    }
}
```

## 🔧 Comandos Útiles

```bash
# Limpiar proyecto
./gradlew clean

# Ver tareas disponibles
./gradlew tasks

# Compilar solo shared
./gradlew :shared:build

# Crear ejecutable Windows
./gradlew :desktopApp:packageMsi

# Crear ejecutable Mac
./gradlew :desktopApp:packageDmg
```

## ❓ Solución de Problemas

### Error: "Cannot find module"
```bash
./gradlew clean
./gradlew build
```

### Error de sincronización Gradle
1. File → Invalidate Caches / Restart
2. Reimportar proyecto

### App no se actualiza
```bash
./gradlew clean
./gradlew :androidApp:installDebug --rerun-tasks
```

## 📚 Recursos

- [Kotlin Multiplatform Docs](https://kotlinlang.org/docs/multiplatform.html)
- [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/)
- [SQLDelight](https://cashapp.github.io/sqldelight/)
