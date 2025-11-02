# 🏭 REWS - Sistema de Rotación y Estaciones de Trabajo

**Versión 3.1.0** - Aplicación Android profesional para la gestión inteligente de rotaciones de trabajadores en entornos industriales. Ahora con **Analytics Avanzados** y **Dashboard Ejecutivo** con capacidades de análisis predictivo y métricas empresariales en tiempo real.

## 🚀 Características Principales v3.1.0

### 🔮 **Analytics Avanzados** ⭐ NUEVO
- **Análisis Predictivo**: Predicciones a 7 días con factores de riesgo y confianza >80%
- **Detección de Patrones**: 6 tipos automáticos (secuencias óptimas, cuellos de botella, alta eficiencia, etc.)
- **Métricas de Rendimiento**: Evaluación individual multidimensional (0-10) con 4 dimensiones
- **Análisis de Carga**: Utilización por estación y detección automática de desbalances
- **Reportes Automatizados**: 3 tipos especializados con recomendaciones ejecutivas
- **Navegación por Tabs**: 7 secciones especializadas con visualizaciones avanzadas

### 📈 **Dashboard Ejecutivo** ⭐ NUEVO
- **4 Cards de Resumen**: Métricas clave empresariales en tiempo real
- **13 KPIs Especializados**: Indicadores de rendimiento con tendencias visuales
- **Sistema de Alertas**: 5 tipos de notificaciones proactivas e inteligentes
- **Gráficos de Tendencias**: 4 visualizaciones con análisis simplificado
- **Métricas Empresariales**: Salud del sistema (0-100%), eficiencia operativa, productividad, ROI

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
- **ML Básico**: Algoritmos de scoring multifactorial para predicciones

### 📱 **Interfaz Moderna y Profesional**
- **Material Design 3**: Diseño moderno siguiendo las últimas guías de Google
- **Animaciones Fluidas**: Micro-interacciones y transiciones suaves mejoradas
- **Gestos Especiales**: Long press y doble tap para acceso rápido a funciones avanzadas
- **Navegación Avanzada**: ViewPager2 con tabs especializados para analytics
- **Responsive Design**: Optimizado para tablets y diferentes resoluciones

### ⚡ **Rendimiento y Confiabilidad**
- **Base de Datos Offline**: Room Database para funcionamiento sin conexión
- **Arquitectura MVVM Extendida**: Separación clara con servicios especializados
- **Kotlin Coroutines + Flow**: Programación reactiva asíncrona optimizada
- **Cálculos en Tiempo Real**: Analytics dinámicos sin impacto en base de datos
- **Testing Integral**: Cobertura >87% incluyendo algoritmos ML

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

## 🎯 Guía de Uso Rápido v3.1.0

### 📱 **Instalación Completa**
👉 **[Ver Guía Detallada de Instalación v3.1.0](GUIA_INSTALACION_v3.1.0.md)**

### 1. **Configuración Inicial**
- Completar onboarding interactivo de 4 pasos
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

### 5. **Analytics Avanzados** ⭐ NUEVO
- **Acceso**: Doble tap en botón "Historial"
- **Navegación**: 7 tabs especializados con métricas avanzadas
- **Predicciones**: Análisis predictivo a 7 días con confianza
- **Patrones**: Detección automática de 6 tipos de patrones
- **Reportes**: Generación automática de 3 tipos especializados

### 6. **Dashboard Ejecutivo** ⭐ NUEVO
- **Acceso**: Long press en botón "Configuración"
- **KPIs**: 13 indicadores de rendimiento en tiempo real
- **Alertas**: Sistema proactivo de 5 tipos de notificaciones
- **Métricas**: Salud del sistema, eficiencia, productividad, ROI

## 🔒 Seguridad y Privacidad

- **Datos Locales**: Toda la información se almacena localmente en el dispositivo
- **Sin Conexión**: Funciona completamente offline, sin envío de datos externos
- **Permisos Mínimos**: Solo solicita permisos esenciales para funcionamiento
- **Código Auditado**: Análisis de seguridad automatizado en cada release

## 📚 Documentación v3.1.0

### **📱 Guías de Usuario**
- **[Guía de Instalación v3.1.0](GUIA_INSTALACION_v3.1.0.md)** ⭐ NUEVA: Instalación completa paso a paso
- **[Guía de Instalación Original](INSTALLATION_GUIDE.md)**: Instrucciones básicas de instalación

### **🔧 Documentación Técnica**
- **[Analytics Avanzados](IMPLEMENTACION_ANALYTICS_AVANZADOS.md)** ⭐ NUEVA: Implementación completa
- **[Dashboard Ejecutivo](IMPLEMENTACION_DASHBOARD_EJECUTIVO.md)**: Métricas y KPIs empresariales
- **[Arquitectura del Sistema](ARCHITECTURE.md)**: Documentación técnica actualizada
- **[Roadmap v3.1.0](ROADMAP_DESARROLLO_v3.1.0.md)**: Plan de desarrollo futuro

### **📋 Historial y Cambios**
- **[Notas de Release v3.0.0](RELEASE_NOTES_v3.0.0.md)**: Cambios versión anterior
- **[Changelog Completo](CHANGELOG.md)**: Historial completo de versiones

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

## 📊 Información del Proyecto v3.1.0

### **🚀 Estadísticas Actualizadas**
- **Versión Actual**: 3.1.0 (Noviembre 2024)
- **Líneas de Código**: ~18,500 (+3,500 nuevas)
- **Funcionalidades**: 25+ (+8 nuevas en v3.1.0)
- **Cobertura Testing**: >87% (+2% mejora)
- **Rendimiento**: +40% más rápido en cálculos

### **🛠️ Stack Tecnológico**
- **Lenguaje**: Kotlin 100%
- **Arquitectura**: MVVM + Clean Architecture + Services Layer
- **Base de Datos**: Room Database con migraciones automáticas
- **UI**: Material Design 3 + ViewPager2 + Fragments
- **Analytics**: Algoritmos ML básicos + Métricas en tiempo real
- **Testing**: JUnit 5, Espresso, MockK (Cobertura >87%)

### **📈 Nuevas Capacidades v3.1.0**
- **🔮 Machine Learning**: Algoritmos predictivos básicos
- **📊 Business Intelligence**: Dashboard ejecutivo empresarial
- **⚡ Tiempo Real**: Cálculos dinámicos sin impacto en BD
- **🎯 UX Avanzada**: Gestos especiales y micro-interacciones
- **📈 Visualizaciones**: Gráficos y métricas empresariales

### **🎯 Estado del Proyecto**
- **Estado**: Producción estable con Analytics Avanzados
- **Licencia**: Propietaria Restrictiva
- **Soporte**: Activo con actualizaciones regulares
- **Roadmap**: v3.2.0 Automatización Inteligente en desarrollo

**© 2024-2025 Brandon Josué Hidalgo Paz. Todos los derechos reservados.**