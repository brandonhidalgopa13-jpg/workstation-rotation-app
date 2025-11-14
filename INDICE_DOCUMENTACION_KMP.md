# 📚 Índice de Documentación - Kotlin Multiplatform

## 🎯 Guías de Inicio Rápido

### Para Empezar
1. **[RESUMEN_EJECUTIVO_MIGRACION_KMP.md](RESUMEN_EJECUTIVO_MIGRACION_KMP.md)** ⭐
   - Resumen general de la migración
   - Resultados y beneficios
   - Estado actual del proyecto
   - **Leer primero**

2. **[VERIFICAR_INSTALACION.md](VERIFICAR_INSTALACION.md)** ⭐
   - Cómo verificar que todo funciona
   - Compilar y ejecutar
   - Solución de problemas
   - **Leer segundo**

3. **[GUIA_RAPIDA_KMP.md](GUIA_RAPIDA_KMP.md)** ⭐
   - Comandos básicos
   - Estructura simplificada
   - Cómo agregar funcionalidad
   - **Referencia rápida**

## 📖 Documentación Técnica

### Migración y Arquitectura
4. **[MIGRACION_KMP_v5.0.0.md](MIGRACION_KMP_v5.0.0.md)**
   - Guía completa de migración
   - Nueva estructura del proyecto
   - Cambios principales (Room → SQLDelight, XML → Compose)
   - Cómo compilar para cada plataforma

5. **[RESUMEN_MIGRACION_FASE1.md](RESUMEN_MIGRACION_FASE1.md)**
   - Detalles técnicos de Fase 1
   - Archivos creados
   - Funcionalidad implementada
   - Próximos pasos

### Desarrollo
6. **[SIGUIENTE_PASO_DESARROLLO.md](SIGUIENTE_PASO_DESARROLLO.md)**
   - Qué implementar a continuación
   - Plantillas de código
   - Orden recomendado
   - Consejos de desarrollo

### Limpieza y Mantenimiento
7. **[PLAN_ELIMINACION_CODIGO_ANTIGUO.md](PLAN_ELIMINACION_CODIGO_ANTIGUO.md)**
   - Cuándo eliminar código antiguo
   - Qué archivos eliminar
   - Proceso seguro de eliminación
   - Checklist antes de eliminar

## 📱 Documentación por Plataforma

### Android
- **Ubicación:** `androidApp/`
- **Configuración:** `androidApp/build.gradle.kts`
- **Manifest:** `androidApp/src/main/AndroidManifest.xml`
- **Compilar:** `./gradlew :androidApp:assembleDebug`
- **Instalar:** `./gradlew :androidApp:installDebug`

### Desktop (Windows/Mac/Linux)
- **Ubicación:** `desktopApp/`
- **Configuración:** `desktopApp/build.gradle.kts`
- **Ejecutar:** `run-desktop.bat` o `./gradlew :desktopApp:run`
- **Compilar ejecutable:** `./gradlew :desktopApp:packageMsi` (Windows)

### iOS
- **Ubicación:** `iosApp/` (pendiente)
- **Estado:** Preparado, pendiente de implementar
- **Requisitos:** macOS + Xcode

### Shared (Código Común)
- **Ubicación:** `shared/`
- **Configuración:** `shared/build.gradle.kts`
- **Contiene:** 90% del código (UI + lógica + datos)

## 🗂️ Estructura de Archivos

### Archivos de Configuración
```
├── settings.gradle.kts          # Configuración de módulos
├── build.gradle.kts             # Configuración raíz
├── gradle.properties            # Propiedades de Gradle
└── gradle/                      # Wrapper de Gradle
```

### Módulos
```
├── shared/                      # Código compartido (90%)
│   ├── src/
│   │   ├── commonMain/         # Común a todas las plataformas
│   │   ├── androidMain/        # Específico Android
│   │   ├── iosMain/            # Específico iOS
│   │   └── desktopMain/        # Específico Desktop
│   └── build.gradle.kts
├── androidApp/                  # App Android (5%)
├── desktopApp/                  # App Desktop (5%)
└── iosApp/                      # App iOS (pendiente)
```

### Scripts
```
├── run-desktop.bat              # Ejecutar Desktop (Windows)
├── build-multiplatform.bat      # Compilar todo (Windows)
└── gradlew / gradlew.bat        # Gradle wrapper
```

## 📊 Documentación por Tema

### 🏗️ Arquitectura
- [MIGRACION_KMP_v5.0.0.md](MIGRACION_KMP_v5.0.0.md) - Nueva arquitectura
- [ARCHITECTURE.md](ARCHITECTURE.md) - Arquitectura original (referencia)

### 💾 Base de Datos
- **SQLDelight:** `shared/src/commonMain/sqldelight/`
- **Tablas:**
  - `Worker.sq` - Trabajadores
  - `Workstation.sq` - Estaciones
  - `RotationSession.sq` - Sesiones
  - `RotationAssignment.sq` - Asignaciones

### 🎨 UI y Pantallas
- **Ubicación:** `shared/src/commonMain/kotlin/.../presentation/screens/`
- **Implementadas:**
  - `MainScreen.kt` - Menú principal
  - `WorkersScreen.kt` - Gestión de trabajadores
- **Pendientes:**
  - `WorkstationsScreen.kt`
  - `RotationScreen.kt`
  - `HistoryScreen.kt`

### 🧠 Lógica de Negocio
- **Ubicación:** `shared/src/commonMain/kotlin/.../domain/`
- **Componentes:**
  - `models/` - Modelos de datos
  - `repository/` - Acceso a datos
  - `service/` - Lógica de negocio

### 🧪 Testing
- **Ubicación:** `shared/src/commonTest/`
- **Estado:** Pendiente de implementar

## 🔍 Buscar Información Específica

### ¿Cómo compilar?
→ [VERIFICAR_INSTALACION.md](VERIFICAR_INSTALACION.md) - Sección "Compilar"

### ¿Cómo agregar una pantalla?
→ [SIGUIENTE_PASO_DESARROLLO.md](SIGUIENTE_PASO_DESARROLLO.md) - Sección "Plantilla"

### ¿Qué tecnologías se usan?
→ [RESUMEN_EJECUTIVO_MIGRACION_KMP.md](RESUMEN_EJECUTIVO_MIGRACION_KMP.md) - Sección "Tecnologías"

### ¿Cuándo eliminar código antiguo?
→ [PLAN_ELIMINACION_CODIGO_ANTIGUO.md](PLAN_ELIMINACION_CODIGO_ANTIGUO.md)

### ¿Cómo funciona la UI adaptativa?
→ [MIGRACION_KMP_v5.0.0.md](MIGRACION_KMP_v5.0.0.md) - Sección "UI Adaptativa"

### ¿Problemas de compilación?
→ [VERIFICAR_INSTALACION.md](VERIFICAR_INSTALACION.md) - Sección "Solución de Problemas"

## 📝 Documentación Original (Referencia)

### Versión 4.x (Android Nativo)
- [ARCHITECTURE.md](ARCHITECTURE.md) - Arquitectura original
- [INSTALLATION_GUIDE.md](INSTALLATION_GUIDE.md) - Guía de instalación v4
- [CHANGELOG.md](CHANGELOG.md) - Historial de cambios
- [README.md](README.md) - README original

### Documentación Técnica v4
- [SQL_ROTATION_ARCHITECTURE.md](SQL_ROTATION_ARCHITECTURE.md)
- [ALGORITMO_ROTACION_CON_PRIORIDADES.md](ALGORITMO_ROTACION_CON_PRIORIDADES.md)
- [SISTEMA_REPORTES_METRICAS.md](SISTEMA_REPORTES_METRICAS.md)

### Implementaciones v4
- [IMPLEMENTACION_SEGURIDAD_FASE1_v4.0.4.md](IMPLEMENTACION_SEGURIDAD_FASE1_v4.0.4.md)
- [IMPLEMENTACION_ANALYTICS_AVANZADOS.md](IMPLEMENTACION_ANALYTICS_AVANZADOS.md)
- [IMPLEMENTACION_DASHBOARD_EJECUTIVO.md](IMPLEMENTACION_DASHBOARD_EJECUTIVO.md)

## 🎯 Rutas de Aprendizaje

### Para Nuevos Desarrolladores
1. [RESUMEN_EJECUTIVO_MIGRACION_KMP.md](RESUMEN_EJECUTIVO_MIGRACION_KMP.md)
2. [GUIA_RAPIDA_KMP.md](GUIA_RAPIDA_KMP.md)
3. [VERIFICAR_INSTALACION.md](VERIFICAR_INSTALACION.md)
4. [SIGUIENTE_PASO_DESARROLLO.md](SIGUIENTE_PASO_DESARROLLO.md)

### Para Desarrolladores Experimentados
1. [MIGRACION_KMP_v5.0.0.md](MIGRACION_KMP_v5.0.0.md)
2. [RESUMEN_MIGRACION_FASE1.md](RESUMEN_MIGRACION_FASE1.md)
3. Revisar código en `shared/src/commonMain/`

### Para Arquitectos/Tech Leads
1. [RESUMEN_EJECUTIVO_MIGRACION_KMP.md](RESUMEN_EJECUTIVO_MIGRACION_KMP.md)
2. [MIGRACION_KMP_v5.0.0.md](MIGRACION_KMP_v5.0.0.md)
3. [PLAN_ELIMINACION_CODIGO_ANTIGUO.md](PLAN_ELIMINACION_CODIGO_ANTIGUO.md)

## 🔗 Enlaces Externos Útiles

### Kotlin Multiplatform
- [Documentación Oficial](https://kotlinlang.org/docs/multiplatform.html)
- [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/)
- [SQLDelight](https://cashapp.github.io/sqldelight/)

### Tutoriales
- [KMP Getting Started](https://kotlinlang.org/docs/multiplatform-get-started.html)
- [Compose Multiplatform Tutorial](https://github.com/JetBrains/compose-multiplatform)

## 📞 Soporte

### Problemas Técnicos
1. Revisar [VERIFICAR_INSTALACION.md](VERIFICAR_INSTALACION.md)
2. Buscar en la documentación relevante
3. Revisar logs de compilación
4. Crear issue con detalles del error

### Preguntas de Desarrollo
1. Revisar [SIGUIENTE_PASO_DESARROLLO.md](SIGUIENTE_PASO_DESARROLLO.md)
2. Consultar código de ejemplo en `shared/`
3. Preguntar con contexto específico

## 📅 Actualizaciones

Este índice se actualiza con cada nueva versión de la documentación.

**Última actualización:** 13 de Noviembre, 2025  
**Versión:** 5.0.0-alpha  
**Documentos totales:** 8 nuevos + documentación original

---

## 🎯 Inicio Rápido (TL;DR)

**¿Primera vez aquí?**

1. Lee [RESUMEN_EJECUTIVO_MIGRACION_KMP.md](RESUMEN_EJECUTIVO_MIGRACION_KMP.md) (5 min)
2. Ejecuta [VERIFICAR_INSTALACION.md](VERIFICAR_INSTALACION.md) (10 min)
3. Consulta [GUIA_RAPIDA_KMP.md](GUIA_RAPIDA_KMP.md) cuando necesites algo

**¿Listo para desarrollar?**

→ [SIGUIENTE_PASO_DESARROLLO.md](SIGUIENTE_PASO_DESARROLLO.md)

**¿Problemas?**

→ [VERIFICAR_INSTALACION.md](VERIFICAR_INSTALACION.md) - Sección "Solución de Problemas"
