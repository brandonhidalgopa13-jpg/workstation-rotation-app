# Migración a Kotlin Multiplatform v5.0.0

## 🎯 Objetivo
Convertir la aplicación Android nativa en una aplicación multiplataforma que funcione en:
- ✅ **Android** (móvil y tablet)
- ✅ **iOS** (iPhone y iPad)
- ✅ **Desktop** (Windows, macOS, Linux)

## 📁 Nueva Estructura del Proyecto

```
WorkstationRotation/
├── shared/                          # Código compartido (lógica + UI)
│   ├── src/
│   │   ├── commonMain/             # Código común a todas las plataformas
│   │   │   ├── kotlin/
│   │   │   │   └── com/workstation/rotation/
│   │   │   │       ├── domain/     # Lógica de negocio
│   │   │   │       │   ├── models/
│   │   │   │       │   ├── repository/
│   │   │   │       │   └── service/
│   │   │   │       └── presentation/  # UI con Compose
│   │   │   │           ├── screens/
│   │   │   │           └── viewmodels/
│   │   │   └── sqldelight/         # Esquemas de base de datos
│   │   ├── androidMain/            # Código específico Android
│   │   ├── iosMain/                # Código específico iOS
│   │   └── desktopMain/            # Código específico Desktop
│   └── build.gradle.kts
├── androidApp/                      # App Android
│   └── src/main/
│       ├── AndroidManifest.xml
│       └── kotlin/
│           └── MainActivity.kt
├── desktopApp/                      # App Desktop (PC)
│   └── src/main/kotlin/
│       └── Main.kt
├── iosApp/                          # App iOS (a crear)
└── build.gradle.kts
```

## 🔄 Cambios Principales

### 1. Base de Datos: Room → SQLDelight
- **Antes**: Room (solo Android)
- **Ahora**: SQLDelight (multiplataforma)
- Los esquemas están en `shared/src/commonMain/sqldelight/`

### 2. UI: XML → Compose Multiplatform
- **Antes**: Activities + XML layouts
- **Ahora**: Composables adaptativos
- UI se adapta automáticamente a móvil/tablet/desktop

### 3. Arquitectura
- **Shared Module**: Toda la lógica de negocio y UI
- **Platform Modules**: Solo código específico de plataforma

## 🚀 Compilar y Ejecutar

### Android
```bash
./gradlew :androidApp:assembleDebug
./gradlew :androidApp:installDebug
```

### Desktop (Windows)
```bash
./gradlew :desktopApp:run
```

### Crear ejecutable Desktop
```bash
# Windows (.msi)
./gradlew :desktopApp:packageMsi

# macOS (.dmg)
./gradlew :desktopApp:packageDmg

# Linux (.deb)
./gradlew :desktopApp:packageDeb
```

### iOS (requiere macOS + Xcode)
```bash
./gradlew :shared:linkDebugFrameworkIosArm64
# Luego abrir iosApp/iosApp.xcodeproj en Xcode
```

## 📱 UI Adaptativa

La UI se adapta automáticamente según el tamaño de pantalla:

### Móvil (Compact)
- Layout vertical
- Lista simple
- FAB para acciones principales

### Tablet (Medium)
- Layout mixto
- Grid de 2 columnas

### Desktop (Expanded)
- Layout horizontal
- Grid adaptativo
- Menús en barra superior

## 🔧 Tecnologías Utilizadas

- **Kotlin Multiplatform**: 1.9.21
- **Compose Multiplatform**: 1.5.11
- **SQLDelight**: 2.0.1
- **Coroutines**: 1.7.3
- **Serialization**: 1.6.2

## 📝 Próximos Pasos

1. ✅ Estructura base creada
2. ✅ Base de datos SQLDelight configurada
3. ✅ Pantalla de trabajadores implementada
4. ⏳ Implementar pantalla de estaciones
5. ⏳ Implementar generación de rotación
6. ⏳ Implementar historial
7. ⏳ Migrar funciones de seguridad
8. ⏳ Crear app iOS
9. ⏳ Testing multiplataforma

## 🗑️ Archivos Antiguos a Eliminar

Una vez verificada la migración, eliminar:
- `app/` (módulo Android antiguo)
- Todos los archivos XML de layouts
- Activities antiguas
- Adapters de RecyclerView
- Código específico de Room

## ⚠️ Notas Importantes

1. **Gradual**: La migración se hace por fases
2. **Coexistencia**: Ambas versiones pueden coexistir temporalmente
3. **Testing**: Probar cada pantalla en todas las plataformas
4. **Datos**: La base de datos se migra automáticamente

## 🎨 Ventajas de la Nueva Arquitectura

- ✅ Un solo código para todas las plataformas
- ✅ UI moderna con Material Design 3
- ✅ Mejor rendimiento
- ✅ Más fácil de mantener
- ✅ Animaciones fluidas
- ✅ Modo oscuro automático
- ✅ Responsive design nativo
