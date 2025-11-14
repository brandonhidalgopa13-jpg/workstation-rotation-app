# 🔄 Workstation Rotation - Multiplataforma

> Sistema inteligente de rotación de trabajadores en estaciones de trabajo  
> **Ahora disponible para Android, iOS y PC**

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.21-blue.svg)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Compose-1.5.11-green.svg)](https://www.jetbrains.com/lp/compose-multiplatform/)
[![License](https://img.shields.io/badge/License-Proprietary-red.svg)]()

## 🌟 Características

- ✅ **Multiplataforma**: Android, iOS, Windows, macOS, Linux
- ✅ **UI Adaptativa**: Se adapta automáticamente a móvil, tablet y desktop
- ✅ **Algoritmo Inteligente**: Rotación equitativa y optimizada
- ✅ **Offline First**: Base de datos local, funciona sin internet
- ✅ **Material Design 3**: Interfaz moderna y fluida
- ✅ **Modo Oscuro**: Automático según preferencias del sistema

## 📱 Plataformas

| Plataforma | Estado | Versión Mínima |
|------------|--------|----------------|
| 🤖 Android | ✅ Disponible | Android 7.0 (API 24) |
| 💻 Desktop | ✅ Disponible | Windows 10, macOS 10.14, Ubuntu 20.04 |
| 🍎 iOS | 🚧 En desarrollo | iOS 14.0+ |

## 🚀 Inicio Rápido

### Requisitos
- JDK 17 o superior
- Android Studio (para Android)
- Xcode (para iOS, solo en macOS)

### Ejecutar Desktop
```bash
# Windows
run-desktop.bat

# Linux/Mac
./gradlew :desktopApp:run
```

### Instalar en Android
```bash
./gradlew :androidApp:installDebug
```

### Compilar Todo
```bash
# Windows
build-multiplatform.bat

# Linux/Mac
./gradlew build
```

## 📖 Documentación

### 🎯 Empezar Aquí
1. **[Resumen Ejecutivo](RESUMEN_EJECUTIVO_MIGRACION_KMP.md)** - Visión general del proyecto
2. **[Verificar Instalación](VERIFICAR_INSTALACION.md)** - Compilar y ejecutar
3. **[Guía Rápida](GUIA_RAPIDA_KMP.md)** - Comandos y referencia

### 📚 Documentación Completa
- [Índice de Documentación](INDICE_DOCUMENTACION_KMP.md) - Todos los documentos
- [Guía de Migración](MIGRACION_KMP_v5.0.0.md) - Detalles técnicos
- [Siguiente Paso](SIGUIENTE_PASO_DESARROLLO.md) - Continuar desarrollo

## 🏗️ Arquitectura

```
┌─────────────────────────────────────────┐
│         Shared Module (90%)             │
│  ┌───────────────────────────────────┐  │
│  │  UI (Compose Multiplatform)       │  │
│  └───────────────────────────────────┘  │
│  ┌───────────────────────────────────┐  │
│  │  Business Logic                   │  │
│  └───────────────────────────────────┘  │
│  ┌───────────────────────────────────┐  │
│  │  Database (SQLDelight)            │  │
│  └───────────────────────────────────┘  │
└─────────────────────────────────────────┘
         │           │           │
    ┌────┴───┐  ┌────┴────┐  ┌──┴─────┐
    │Android │  │ Desktop │  │  iOS   │
    │  (5%)  │  │   (5%)  │  │  (5%)  │
    └────────┘  └─────────┘  └────────┘
```

## 🛠️ Tecnologías

- **Kotlin Multiplatform** 1.9.21
- **Compose Multiplatform** 1.5.11
- **SQLDelight** 2.0.1
- **Coroutines** 1.7.3
- **Material 3** Latest

## 📂 Estructura del Proyecto

```
WorkstationRotation/
├── shared/              # Código compartido (90%)
│   ├── commonMain/     # Común a todas las plataformas
│   ├── androidMain/    # Específico Android
│   ├── iosMain/        # Específico iOS
│   └── desktopMain/    # Específico Desktop
├── androidApp/         # App Android
├── desktopApp/         # App Desktop
└── iosApp/            # App iOS (próximamente)
```

## ✨ Funcionalidades

### ✅ Implementado (v5.0.0-rc)
- Gestión de trabajadores (CRUD completo)
- Gestión de estaciones (CRUD completo)
- Generación de rotación inteligente
- Historial de rotaciones
- Vista detallada de rotación con estadísticas
- Exportación de rotaciones (Texto, CSV, Markdown)
- Compartir rotaciones (Android)
- UI adaptativa para móvil y desktop
- Base de datos local
- Navegación completa
- 23 tests unitarios

### 🚧 En Desarrollo
- Sistema de seguridad (login)
- Sincronización en la nube
- Notificaciones
- App iOS completa
- Publicación en stores

## 🎨 Capturas de Pantalla

### Desktop
```
┌─────────────────────────────────────┐
│  Rotación de Estaciones             │
├─────────────────────────────────────┤
│  ┌──────┐  ┌──────┐  ┌──────┐      │
│  │Trabaj│  │Estac.│  │Nueva │      │
│  │adores│  │iones │  │Rotac.│      │
│  └──────┘  └──────┘  └──────┘      │
└─────────────────────────────────────┘
```

### Móvil
```
┌──────────────┐
│ Rotación     │
├──────────────┤
│ Trabajadores │
│ Estaciones   │
│ Nueva Rotac. │
│ Historial    │
└──────────────┘
```

## 🔄 Migración desde v4.x

La versión 5.0 es una reescritura completa usando Kotlin Multiplatform.

**Cambios principales:**
- Room → SQLDelight
- XML → Compose
- Solo Android → Multiplataforma

Ver [Guía de Migración](MIGRACION_KMP_v5.0.0.md) para más detalles.

## 🤝 Contribuir

1. Fork el proyecto
2. Crea una rama (`git checkout -b feature/nueva-funcionalidad`)
3. Commit tus cambios (`git commit -am 'Agregar funcionalidad'`)
4. Push a la rama (`git push origin feature/nueva-funcionalidad`)
5. Crea un Pull Request

## 📊 Estado del Proyecto

**Versión Actual:** 5.0.0-rc (Release Candidate)  
**Progreso:** 65% completado

- ✅ Arquitectura: 100%
- ✅ Base de datos: 100%
- ✅ UI Framework: 100%
- ✅ Pantallas: 100%
- ✅ Funciones básicas: 100%
- ✅ Funciones avanzadas: 100%
- ✅ Tests unitarios: 23 tests
- ⏳ iOS: 30%

## 📝 Changelog

### v5.0.0-rc (2025-11-13)
- ✨ Migración completa a Kotlin Multiplatform
- ✨ Soporte para Desktop (Windows/Mac/Linux)
- ✨ Nueva UI con Compose Multiplatform
- ✨ Base de datos SQLDelight
- ✨ UI adaptativa automática
- ✨ Gestión de trabajadores completa
- ✨ Gestión de estaciones completa
- ✨ Generación de rotación inteligente
- ✨ Historial de rotaciones
- ✨ Vista detallada de rotación
- ✨ Exportación en 3 formatos (Texto, CSV, Markdown)
- ✨ Compartir rotaciones (Android)
- ✨ 23 tests unitarios
- ✨ Todas las funciones básicas y avanzadas

### v4.1.0 (Anterior)
- Ver [CHANGELOG.md](CHANGELOG.md) para historial completo

## 📞 Soporte

- **Documentación:** [Índice completo](INDICE_DOCUMENTACION_KMP.md)
- **Problemas:** Crear un issue en el repositorio
- **Preguntas:** Consultar la documentación primero

## 👥 Equipo

- Equipo de desarrollo Workstation Rotation

## 📄 Licencia

Este proyecto es privado y propietario.

---

## 🎯 Próximos Pasos

1. **Desarrolladores:** Lee [SIGUIENTE_PASO_DESARROLLO.md](SIGUIENTE_PASO_DESARROLLO.md)
2. **Usuarios:** Descarga la última versión de [Releases]()
3. **Contribuidores:** Revisa las [Issues abiertas]()

## 🌟 ¿Por qué Kotlin Multiplatform?

- **90% código compartido** entre plataformas
- **Desarrollo más rápido** - Escribe una vez, funciona en todas partes
- **Mejor rendimiento** - Código nativo en cada plataforma
- **Mantenimiento reducido** - Un solo código base
- **Futuro asegurado** - Tecnología respaldada por JetBrains y Google

---

**Hecho con ❤️ usando Kotlin Multiplatform**
