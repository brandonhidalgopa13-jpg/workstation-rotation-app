# 🏭 REWS - Sistema de Rotación y Estaciones de Trabajo

**Versión 3.0.0** - Aplicación Android profesional para la gestión inteligente de rotaciones de trabajadores en entornos industriales. Desarrollada con las últimas tecnologías de Android, ofrece una solución integral y robusta para la administración eficiente de personal y estaciones de trabajo.

## 🚀 Características Principales

### 👑 **Sistema de Liderazgo Inteligente**
- **Líderes "BOTH"**: Supervisores permanentes que permanecen en su estación en ambas rotaciones
- **Líderes "FIRST_HALF"**: Activos solo en la primera mitad, rotan normalmente en la segunda
- **Líderes "SECOND_HALF"**: Activos solo en la segunda mitad, rotan normalmente en la primera
- **Identificación Visual**: Interfaz distintiva con colores púrpura y iconografía especial
- **Prioridad Absoluta**: Los líderes pueden superar límites de capacidad cuando es necesario

### 🚫 **Sistema de Restricciones Avanzado**
- **PROHIBITED**: Trabajadores que NO pueden trabajar en estaciones específicas
- **LIMITED**: Trabajadores con limitaciones especiales en ciertas estaciones
- **TEMPORARY**: Restricciones temporales con fechas de expiración automática
- **Aplicación Automática**: Filtrado inteligente en todas las asignaciones

### 🎓 **Gestión de Certificaciones**
- **Proceso de Entrenamiento**: Sistema completo de capacitación por estaciones
- **Certificación Automática**: Transición de "entrenado" a "certificado"
- **Estaciones de Entrenamiento**: Asignación automática a estaciones designadas
- **Seguimiento Completo**: Historial detallado de certificaciones

### 📊 **Algoritmo de Rotación Optimizado**
- **Distribución Inteligente**: Balanceado automático considerando capacidades y restricciones
- **Rotación Dual**: Sistema independiente para primera y segunda parte
- **Validación Robusta**: Verificación en tiempo real de todas las reglas de negocio
- **Flexibilidad Total**: Soporte para múltiples estaciones por trabajador

### 📱 **Interfaz Moderna y Profesional**
- **Material Design 3**: Diseño moderno siguiendo las últimas guías de Google
- **Modo Oscuro Completo**: Soporte nativo para tema oscuro
- **Responsive Design**: Optimizado para tablets y diferentes resoluciones
- **Navegación Intuitiva**: Flujo de trabajo diseñado para máxima eficiencia

### ⚡ **Rendimiento y Confiabilidad**
- **Base de Datos Offline**: Room Database para funcionamiento sin conexión
- **Arquitectura MVVM**: Separación clara de responsabilidades
- **Kotlin Coroutines**: Operaciones asíncronas sin bloqueos
- **Testing Integral**: Cobertura completa de pruebas unitarias e integración

## 📋 Requisitos del Sistema

- **Android**: 7.0 (API 24) o superior
- **RAM**: 2 GB mínimo, 4 GB recomendado
- **Almacenamiento**: 100 MB de espacio libre
- **Procesador**: ARM64 o x86_64 compatible

## 🛠️ Stack Tecnológico

- **Lenguaje**: Kotlin 100% (Null Safety, Coroutines)
- **UI Framework**: Material Design 3, XML Layouts responsivos
- **Base de Datos**: Room Database con migraciones automáticas
- **Arquitectura**: MVVM + Repository Pattern
- **Testing**: JUnit 5, Espresso, MockK
- **Build System**: Gradle 8.0+ con Kotlin DSL
- **CI/CD**: GitHub Actions con análisis de seguridad

## 📦 Instalación

### 🎯 Descarga Oficial (Recomendada)
1. Visita [Releases](https://github.com/brandonhidalgopa13-jpg/workstation-rotation-app/releases)
2. Descarga `REWS-v3.0.0-release.apk` de la versión más reciente
3. Habilita "Instalar aplicaciones desconocidas" en Configuración > Seguridad
4. Instala el APK y sigue las instrucciones en pantalla

### 🔧 Compilación desde Código Fuente
```bash
# Clonar repositorio
git clone https://github.com/brandonhidalgopa13-jpg/workstation-rotation-app.git
cd workstation-rotation-app

# Configurar keystore (opcional para release)
cp keystore.properties.example keystore.properties
# Editar keystore.properties con tus credenciales

# Compilar APK de release
./gradlew assembleRelease

# APK generado en: app/build/outputs/apk/release/
```

## 🎯 Guía de Uso Rápido

### 1. **Configuración Inicial**
- Crear estaciones de trabajo con capacidades específicas
- Registrar trabajadores con sus habilidades y certificaciones
- Definir restricciones por trabajador-estación si es necesario

### 2. **Gestión de Liderazgo**
- Asignar líderes a estaciones específicas
- Configurar tipo de liderazgo (BOTH, FIRST_HALF, SECOND_HALF)
- Verificar identificación visual en las rotaciones

### 3. **Proceso de Rotación**
- Generar rotación automática con un clic
- Alternar entre primera y segunda parte
- Verificar distribución balanceada y cumplimiento de restricciones

### 4. **Certificaciones**
- Marcar trabajadores como "en entrenamiento"
- Completar proceso de certificación
- Seguimiento automático del progreso

## 🔒 Seguridad y Privacidad

- **Datos Locales**: Toda la información se almacena localmente en el dispositivo
- **Sin Conexión**: Funciona completamente offline, sin envío de datos externos
- **Permisos Mínimos**: Solo solicita permisos esenciales para funcionamiento
- **Código Auditado**: Análisis de seguridad automatizado en cada release

## 📚 Documentación

- **[Guía de Instalación](INSTALLATION_GUIDE.md)**: Instrucciones detalladas de instalación
- **[Notas de Release](RELEASE_NOTES_v3.0.0.md)**: Cambios y mejoras de la versión actual
- **[Changelog](CHANGELOG.md)**: Historial completo de versiones
- **[Arquitectura](ARCHITECTURE.md)**: Documentación técnica del sistema

## 🤝 Contribución

**⚠️ IMPORTANTE**: Este es un proyecto con licencia propietaria restrictiva. Las contribuciones están limitadas y requieren autorización previa del autor.

Para consultas sobre contribuciones:
1. Abre un [Issue](https://github.com/brandonhidalgopa13-jpg/workstation-rotation-app/issues) describiendo tu propuesta
2. Espera aprobación antes de realizar cambios
3. Todas las contribuciones quedan sujetas a la licencia propietaria

## 📄 Licencia

Este proyecto está bajo una **Licencia Propietaria Restrictiva** - ver el archivo [LICENSE](LICENSE) para detalles completos.

**⚠️ AVISO LEGAL**: 
- Solo el autor original tiene derechos de distribución
- Prohibida la redistribución, modificación o uso comercial sin autorización expresa
- Uso permitido solo para evaluación personal y organizacional autorizada
- Violaciones pueden resultar en acciones legales

## 👨‍💻 Desarrollador

**Brandon Josué Hidalgo Paz**  
*Desarrollador Principal - REWS v3.0.0*

## 📞 Contacto y Soporte

Para consultas, soporte técnico o licenciamiento:
- **Issues**: [GitHub Issues](https://github.com/brandonhidalgopa13-jpg/workstation-rotation-app/issues)
- **Documentación**: Consulta los archivos de documentación incluidos
- **Licenciamiento**: Contacta al desarrollador para consultas comerciales

---

## 📊 Información del Proyecto

- **Versión Actual**: 3.0.0 (Enero 2025)
- **Lenguaje**: Kotlin 100%
- **Arquitectura**: MVVM + Clean Architecture
- **Base de Datos**: Room Database
- **UI**: Material Design 3
- **Testing**: Cobertura >80%
- **Estado**: Producción estable
- **Licencia**: Propietaria Restrictiva

**© 2024-2025 Brandon Josué Hidalgo Paz. Todos los derechos reservados.**