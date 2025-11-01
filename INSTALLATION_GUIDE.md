# 📱 Guía de Instalación - REWS v3.0.0

Esta guía completa te ayudará a instalar y configurar **REWS** (Sistema de Rotación y Estaciones de Trabajo) en tu dispositivo Android de manera segura y eficiente.

## 🎯 Características Principales de v3.0.0

### 👑 **Sistema de Liderazgo Inteligente**
- **Líderes "BOTH"**: Supervisores permanentes en su estación en ambas rotaciones
- **Líderes "FIRST_HALF"**: Activos solo en primera mitad, rotan en segunda
- **Líderes "SECOND_HALF"**: Activos solo en segunda mitad, rotan en primera
- **Prioridad Absoluta**: Pueden superar límites de capacidad cuando es necesario
- **Identificación Visual**: Interfaz distintiva con colores púrpura y iconografía especial

### 🚫 **Sistema de Restricciones Avanzado**
- **PROHIBITED**: Trabajadores que NO pueden trabajar en estaciones específicas
- **LIMITED**: Trabajadores con limitaciones especiales en ciertas estaciones
- **TEMPORARY**: Restricciones temporales con fechas de expiración automática
- **Aplicación Automática**: Filtrado inteligente en todas las asignaciones

### 🎓 **Gestión de Certificaciones**
- **Proceso de Entrenamiento**: Sistema completo de capacitación por estaciones
- **Certificación Automática**: Transición de "entrenado" a "certificado"
- **Seguimiento Completo**: Historial detallado de certificaciones

## 📋 Requisitos del Sistema

### Requisitos Mínimos
- **Sistema Operativo**: Android 7.0 (API 24) o superior
- **RAM**: 2 GB mínimo
- **Almacenamiento**: 100 MB de espacio libre
- **Procesador**: ARM64 o x86_64

### Requisitos Recomendados
- **Sistema Operativo**: Android 10.0 (API 29) o superior
- **RAM**: 4 GB o más
- **Almacenamiento**: 200 MB de espacio libre
- **Procesador**: ARM64 de 8 núcleos o superior

### Compatibilidad
- ✅ **Teléfonos Android**: Completamente compatible
- ✅ **Tablets Android**: Interfaz optimizada para pantallas grandes
- ✅ **Android TV**: Soporte básico (no recomendado)
- ❌ **iOS**: No compatible
- ❌ **Windows Phone**: No compatible

## 📦 Métodos de Instalación

### 🎯 Método 1: Descarga Oficial (Recomendado)

#### Paso 1: Descargar el APK
1. Visita la página oficial de releases: [GitHub Releases](https://github.com/brandonhidalgopa13-jpg/workstation-rotation-app/releases)
2. Busca la versión más reciente (v3.0.0)
3. Descarga el archivo `REWS-v3.0.0-release.apk`
4. Verifica que el tamaño del archivo sea aproximadamente 15-20 MB

#### Paso 2: Habilitar Instalación de Fuentes Desconocidas
**Para Android 8.0 y superior:**
1. Ve a **Configuración** > **Aplicaciones y notificaciones**
2. Toca **Acceso especial a aplicaciones**
3. Selecciona **Instalar aplicaciones desconocidas**
4. Elige tu navegador o administrador de archivos
5. Activa **Permitir desde esta fuente**

**Para Android 7.0 - 7.1:**
1. Ve a **Configuración** > **Seguridad**
2. Activa **Fuentes desconocidas**
3. Confirma en el diálogo de advertencia

#### Paso 3: Instalar la Aplicación
1. Abre tu administrador de archivos
2. Navega a la carpeta **Descargas**
3. Toca el archivo `REWS-v3.0.0-release.apk`
4. Toca **Instalar** cuando aparezca el prompt
5. Espera a que se complete la instalación
6. Toca **Abrir** para iniciar la aplicación

### 🔧 Método 2: Compilación desde Código Fuente

#### Requisitos Previos
- **Android Studio**: Versión 2023.1.1 o superior
- **JDK**: OpenJDK 17 o superior
- **Git**: Para clonar el repositorio
- **Gradle**: 8.0 o superior (incluido en Android Studio)

#### Paso 1: Clonar el Repositorio
```bash
# Clonar el repositorio
git clone https://github.com/brandonhidalgopa13-jpg/workstation-rotation-app.git

# Navegar al directorio
cd workstation-rotation-app

# Verificar la rama actual
git branch
```

#### Paso 2: Configurar el Entorno
```bash
# Copiar archivo de configuración de ejemplo
cp keystore.properties.example keystore.properties

# Editar configuración (opcional para debug)
# Para release, necesitarás configurar tu propio keystore
```

#### Paso 3: Compilar la Aplicación
```bash
# Limpiar proyecto
./gradlew clean

# Compilar APK de debug (para desarrollo)
./gradlew assembleDebug

# Compilar APK de release (para producción)
./gradlew assembleRelease
```

#### Paso 4: Localizar el APK
- **Debug APK**: `app/build/outputs/apk/debug/app-debug.apk`
- **Release APK**: `app/build/outputs/apk/release/app-release.apk`

## 🚀 Configuración Inicial

### Primera Ejecución
1. **Abre la aplicación** desde el cajón de aplicaciones
2. **Acepta los permisos** necesarios (solo los esenciales)
3. **Completa el tutorial** interactivo (recomendado)
4. **Configura las preferencias** básicas

### Configuración Básica
1. **Crear Estaciones de Trabajo**:
   - Toca el botón "+" en la pantalla principal
   - Selecciona "Agregar Estación"
   - Define nombre, capacidad y tipo de estación
   - Guarda la configuración

2. **Registrar Trabajadores**:
   - Toca "Gestión de Trabajadores"
   - Agrega trabajadores con sus datos básicos
   - Configura disponibilidad y habilidades
   - Asigna estaciones permitidas

3. **Configurar Liderazgo** (opcional):
   - Selecciona un trabajador
   - Activa "Es Líder"
   - Elige tipo de liderazgo (BOTH, FIRST_HALF, SECOND_HALF)
   - Asigna estación de liderazgo

## 🔧 Configuración Avanzada

### Configuración de Base de Datos
- **Ubicación**: `/data/data/com.workstation.rotation/databases/`
- **Backup Automático**: Habilitado por defecto
- **Migración**: Automática entre versiones

### Configuración de Rendimiento
```kotlin
// Configuraciones recomendadas para dispositivos con poca RAM
- Reducir animaciones en Configuración > Accesibilidad
- Cerrar aplicaciones en segundo plano
- Liberar espacio de almacenamiento regularmente
```

### Configuración de Seguridad
- **Datos Locales**: Toda la información se almacena localmente
- **Sin Conexión**: No requiere internet para funcionar
- **Permisos**: Solo solicita permisos esenciales

## 🛠️ Solución de Problemas

### Problemas Comunes

#### Error: "Aplicación no instalada"
**Causa**: Conflicto con versión anterior o espacio insuficiente
**Solución**:
1. Desinstala versiones anteriores de REWS
2. Libera al menos 200 MB de espacio
3. Reinicia el dispositivo
4. Intenta instalar nuevamente

#### Error: "Análisis del paquete"
**Causa**: APK corrupto o incompatible
**Solución**:
1. Vuelve a descargar el APK desde la fuente oficial
2. Verifica que tu dispositivo cumpla los requisitos mínimos
3. Intenta descargar usando una conexión WiFi estable

#### La aplicación se cierra inesperadamente
**Causa**: Memoria insuficiente o conflicto de aplicaciones
**Solución**:
1. Cierra otras aplicaciones en segundo plano
2. Reinicia el dispositivo
3. Verifica que tengas al menos 2 GB de RAM disponible
4. Actualiza Android a la versión más reciente

#### Rendimiento lento
**Causa**: Dispositivo con especificaciones bajas
**Solución**:
1. Cierra aplicaciones innecesarias
2. Libera espacio de almacenamiento
3. Desactiva animaciones en Configuración del sistema
4. Considera usar un dispositivo con mejores especificaciones

### Logs y Diagnóstico
Para reportar problemas, incluye la siguiente información:
- **Modelo del dispositivo**: Ej. Samsung Galaxy S21
- **Versión de Android**: Ej. Android 12
- **Versión de REWS**: v3.0.0
- **Descripción del problema**: Detallada y específica
- **Pasos para reproducir**: Secuencia exacta de acciones

## 📞 Soporte Técnico

### Canales de Soporte
- **GitHub Issues**: [Reportar problemas](https://github.com/brandonhidalgopa13-jpg/workstation-rotation-app/issues)
- **Documentación**: Consulta los archivos incluidos en el proyecto
- **FAQ**: Revisa las preguntas frecuentes en este documento

### Información para Soporte
Antes de contactar soporte, ten lista la siguiente información:
- Versión de REWS instalada
- Modelo y versión de Android del dispositivo
- Descripción detallada del problema
- Capturas de pantalla (si es relevante)
- Pasos exactos para reproducir el problema

## 🔄 Actualizaciones

### Proceso de Actualización
1. **Verificar nueva versión** en GitHub Releases
2. **Descargar nuevo APK** de la versión más reciente
3. **Instalar sobre la versión existente** (los datos se conservan)
4. **Verificar funcionamiento** después de la actualización

### Migración de Datos
- **Automática**: Los datos se migran automáticamente entre versiones
- **Backup**: Se recomienda hacer backup manual antes de actualizar
- **Compatibilidad**: v3.0.0 es compatible con datos de v2.x.x

## ⚠️ Consideraciones Legales

### Licencia y Uso
- **Licencia Propietaria**: Este software tiene licencia propietaria restrictiva
- **Uso Permitido**: Solo para evaluación personal y organizacional autorizada
- **Restricciones**: Prohibida redistribución, modificación o uso comercial sin autorización
- **Derechos**: Solo el autor original tiene derechos de distribución

### Responsabilidad
- El usuario es responsable del uso adecuado del software
- El desarrollador no se hace responsable por pérdida de datos
- Se recomienda hacer backups regulares de la información importante

---

## 📊 Información Adicional

### Rendimiento Esperado
- **Tiempo de inicio**: 2-3 segundos en dispositivos modernos
- **Generación de rotación**: <1 segundo para 50 trabajadores
- **Uso de RAM**: 50-100 MB durante operación normal
- **Uso de almacenamiento**: 20-50 MB dependiendo de los datos

### Compatibilidad Futura
- **Soporte a largo plazo**: v3.x.x tendrá soporte por al menos 2 años
- **Actualizaciones**: Actualizaciones regulares con mejoras y correcciones
- **Migración**: Herramientas de migración disponibles para futuras versiones

---

**© 2024-2025 Brandon Josué Hidalgo Paz. Todos los derechos reservados.**

*Esta guía corresponde a REWS v3.0.0. Para versiones anteriores, consulta la documentación específica de cada release.*