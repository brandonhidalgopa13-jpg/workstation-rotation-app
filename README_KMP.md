# Workstation Rotation - Multiplataforma v5.0.0

Sistema de rotación de trabajadores en estaciones de trabajo, ahora disponible para **Android**, **iOS** y **PC** (Windows/Mac/Linux).

## 🌟 Características

- ✅ **Multiplataforma**: Un solo código para todas las plataformas
- ✅ **UI Adaptativa**: Se adapta automáticamente a móvil, tablet y desktop
- ✅ **Base de datos local**: SQLDelight (funciona offline)
- ✅ **Material Design 3**: Interfaz moderna y fluida
- ✅ **Modo oscuro**: Automático según preferencias del sistema
- ✅ **Alto rendimiento**: Código nativo en todas las plataformas

## 📱 Plataformas Soportadas

| Plataforma | Estado | Versión Mínima |
|------------|--------|----------------|
| Android    | ✅ Listo | Android 7.0 (API 24) |
| Desktop    | ✅ Listo | Windows 10, macOS 10.14, Ubuntu 20.04 |
| iOS        | 🚧 En desarrollo | iOS 14.0+ |

## 🚀 Instalación y Ejecución

### Requisitos
- JDK 17 o superior
- Android Studio (para Android)
- Xcode (para iOS, solo en macOS)

### Android
```bash
# Compilar e instalar
./gradlew :androidApp:installDebug

# O usar Android Studio
# File → Open → Seleccionar carpeta del proyecto
```

### Desktop (Windows)
```bash
# Ejecutar directamente
run-desktop.bat

# O compilar ejecutable
./gradlew :desktopApp:packageMsi
```

### Desktop (Mac/Linux)
```bash
# Ejecutar
./gradlew :desktopApp:run

# Compilar ejecutable
./gradlew :desktopApp:packageDmg  # macOS
./gradlew :desktopApp:packageDeb  # Linux
```

## 🏗️ Arquitectura

```
┌─────────────────────────────────────────┐
│         Shared Module (KMP)             │
│  ┌───────────────────────────────────┐  │
│  │  Presentation (Compose UI)        │  │
│  │  - Screens                        │  │
│  │  - ViewModels                     │  │
│  └───────────────────────────────────┘  │
│  ┌───────────────────────────────────┐  │
│  │  Domain (Business Logic)          │  │
│  │  - Models                         │  │
│  │  - Repository                     │  │
│  │  - Services                       │  │
│  └───────────────────────────────────┘  │
│  ┌───────────────────────────────────┐  │
│  │  Data (SQLDelight)                │  │
│  │  - Database Schema                │  │
│  │  - Queries                        │  │
│  └───────────────────────────────────┘  │
└─────────────────────────────────────────┘
         │           │           │
    ┌────┴───┐  ┌────┴────┐  ┌──┴─────┐
    │Android │  │ Desktop │  │  iOS   │
    │  App   │  │   App   │  │  App   │
    └────────┘  └─────────┘  └────────┘
```

## 📂 Estructura del Proyecto

```
WorkstationRotation/
├── shared/                    # Módulo compartido
│   ├── src/
│   │   ├── commonMain/       # Código común
│   │   ├── androidMain/      # Específico Android
│   │   ├── iosMain/          # Específico iOS
│   │   └── desktopMain/      # Específico Desktop
│   └── build.gradle.kts
├── androidApp/               # App Android
├── desktopApp/               # App Desktop
├── iosApp/                   # App iOS (próximamente)
└── build.gradle.kts
```

## 🛠️ Tecnologías

- **Kotlin Multiplatform**: 1.9.21
- **Compose Multiplatform**: 1.5.11
- **SQLDelight**: 2.0.1
- **Coroutines**: 1.7.3
- **Material 3**: Última versión

## 📖 Documentación

- [Guía de Migración](MIGRACION_KMP_v5.0.0.md)
- [Guía Rápida](GUIA_RAPIDA_KMP.md)
- [Arquitectura Original](ARCHITECTURE.md)

## 🔄 Migración desde v4.x

La versión 5.0 es una reescritura completa usando Kotlin Multiplatform. Los datos se migran automáticamente.

Ver [MIGRACION_KMP_v5.0.0.md](MIGRACION_KMP_v5.0.0.md) para más detalles.

## 🤝 Contribuir

1. Fork el proyecto
2. Crea una rama (`git checkout -b feature/nueva-funcionalidad`)
3. Commit tus cambios (`git commit -am 'Agregar nueva funcionalidad'`)
4. Push a la rama (`git push origin feature/nueva-funcionalidad`)
5. Crea un Pull Request

## 📝 Licencia

Este proyecto es privado y propietario.

## 👥 Autores

- Equipo de desarrollo Workstation Rotation

## 📞 Soporte

Para reportar bugs o solicitar funcionalidades, crear un issue en el repositorio.
