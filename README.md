# 🏭 REWS - Rotation and Workstation System

**Versión 2.5.1** - Una aplicación Android avanzada para gestionar estaciones de trabajo y rotación inteligente de trabajadores con sistema de liderazgo, entrenamiento integrado y diseño gráfico moderno estabilizado.

## ✨ Características Principales

- **🏭 Gestión Avanzada de Estaciones**: Crear, editar y configurar estaciones prioritarias
- **👥 Sistema Completo de Trabajadores**: Gestión de personal con roles de entrenamiento y liderazgo
- **👑 Sistema de Liderazgo**: Designación de líderes con identificación visual distintiva
- **🎓 Sistema de Entrenamiento**: Parejas entrenador-entrenado con prioridad absoluta
- **🔄 Rotación Inteligente**: Algoritmo avanzado que considera múltiples factores
- **🏆 Certificación de Trabajadores**: Proceso para completar entrenamientos
- **🚫 Sistema de Restricciones**: Control granular por trabajador y estación
- **🎯 Sistema de Onboarding**: Tutorial interactivo de primera instalación
- **🎨 Diseño Gráfico Moderno**: Interfaz renovada con gradientes y animaciones
- **📚 Tutorial Interactivo**: Guía paso a paso para nuevos usuarios
- **💾 Base de Datos Local**: Funciona completamente offline
- **☁️ Sincronización en la Nube**: Backup y restauración con Firebase
- **🌙 Modo Oscuro**: Tema adaptativo para mejor experiencia

## 🚀 Funcionalidades Detalladas

### 🏭 Gestión de Estaciones de Trabajo
- **Creación y Edición**: Configurar estaciones con capacidades específicas
- **Estaciones Prioritarias**: Marcar estaciones críticas que siempre mantienen capacidad completa
- **Estado Operativo**: Activar/desactivar estaciones según necesidades operativas
- **Interfaz Visual**: Lista organizada con cards y indicadores de estado

### 👥 Gestión Avanzada de Trabajadores
- **Información Completa**: Datos personales, disponibilidad y restricciones
- **Sistema de Disponibilidad**: Porcentajes de disponibilidad (0-100%) con indicadores visuales
- **Roles de Entrenamiento**: Configurar entrenadores (👨‍🏫) y entrenados (🎯)
- **👑 Sistema de Liderazgo**: Designar líderes con tipos configurables (ambas partes, primera, segunda)
- **Identificación Visual**: Líderes destacados con colores púrpura y mensaje especial
- **Asignación Flexible**: Múltiples estaciones por trabajador con sistema de checkboxes
- **🚫 Restricciones Específicas**: Control granular por estación (PROHIBIDO, LIMITADO, TEMPORAL)
- **Certificación**: Proceso para completar entrenamientos y remover estado de entrenado

### 🔄 Sistema de Rotación Inteligente
- **Algoritmo Avanzado**: Considera entrenamiento, liderazgo, disponibilidad, prioridades y restricciones
- **👑 Prioridad de Líderes**: Líderes asignados a sus estaciones designadas con identificación visual
- **Parejas de Entrenamiento**: Entrenador y entrenado siempre juntos con prioridad absoluta
- **Rotación Forzada**: Trabajadores entrenados rotan automáticamente cada 2 ciclos
- **Visualización Dual**: Rotación actual y próxima rotación simultáneamente
- **Indicadores Visuales**: Estados claros de liderazgo, entrenamiento, disponibilidad y capacidad
- **🚫 Respeto de Restricciones**: El algoritmo automáticamente evita asignaciones prohibidas

### 📚 Tutorial y Ayuda
- **Tutorial Interactivo**: 9 pasos detallados para nuevos usuarios
- **Configuración Flexible**: Activar/desactivar tutorial y pistas
- **Ayuda Contextual**: Resaltado visual de elementos durante la guía
- **Documentación Integrada**: Acceso a guías desde la aplicación

## Tecnologías

- **Kotlin** - Lenguaje principal
- **Room Database** - Persistencia local
- **Material Design 3** - Interfaz de usuario
- **MVVM Architecture** - Patrón arquitectónico
- **Coroutines** - Programación asíncrona
- **ViewBinding** - Vinculación de vistas

## Instalación

### Opción 1: Descargar APK
1. Ve a la sección [Releases](../../releases)
2. Descarga el archivo `app-debug.apk`
3. Instala en tu dispositivo Android

### Opción 2: Compilar desde código
1. Clona este repositorio
2. Abre en Android Studio
3. Ejecuta el proyecto

## 📱 Requisitos del Sistema

- **Android**: 7.0 (API 24) o superior
- **Espacio**: ~20 MB de espacio libre
- **Conectividad**: No requiere conexión a internet (funciona offline)
- **RAM**: Mínimo 2 GB recomendado para rendimiento óptimo

## 📖 Guía de Uso Rápido

1. **🏭 Configurar Estaciones**: Crea 3-5 estaciones variadas, marca las prioritarias
2. **👥 Registrar Trabajadores**: Agrega 5-10 trabajadores con diferentes disponibilidades
3. **🎓 Configurar Entrenamiento**: Establece parejas entrenador-entrenado si es necesario
4. **🔄 Generar Rotación**: El algoritmo inteligente creará la rotación óptima
5. **🏆 Certificar Trabajadores**: Completa entrenamientos cuando sea apropiado

### 📚 Tutorial Integrado
La aplicación incluye un tutorial interactivo que se ejecuta automáticamente en el primer uso. Puedes reactivarlo desde el menú principal en cualquier momento.

### 📋 Documentación Completa
- **GUIA_USUARIO_RAPIDA.md**: Manual práctico para usuarios finales
- **FUNCIONES_DEL_SISTEMA.md**: Documentación técnica detallada
- **CHANGELOG.md**: Historial de versiones y cambios

## Capturas de Pantalla

*Las capturas se agregarán próximamente*

## Contribuir

1. Fork el proyecto
2. Crea una rama para tu feature (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abre un Pull Request

## Licencia

Este proyecto está bajo la Licencia MIT - ver el archivo [LICENSE](LICENSE) para detalles.

## 👨‍💻 Desarrollador

**Brandon Josué Hidalgo Paz**  
*REWS - Rotation and Workstation System v2.3.0*

## 📞 Contacto

Si tienes preguntas, sugerencias o encuentras algún problema, no dudes en:
- Abrir un [Issue](../../issues)
- Revisar la [documentación completa](FUNCIONES_DEL_SISTEMA.md)
- Consultar la [guía de usuario](GUIA_USUARIO_RAPIDA.md)

---

## 📊 Estadísticas del Proyecto

- **Versión Actual**: 2.5.1
- **Lenguaje Principal**: Kotlin (100%)
- **Arquitectura**: MVVM + Room Database
- **Funcionalidades**: 30+ características implementadas
- **Documentación**: Completa con guías y tutoriales
- **Diseño**: Moderno con onboarding interactivo estabilizado
- **Estado**: Versión estable lista para producción