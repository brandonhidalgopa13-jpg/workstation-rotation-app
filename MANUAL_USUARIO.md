# 📱 Manual de Usuario - Sistema de Rotación Inteligente

## 🎯 **Bienvenido al Sistema de Rotación Inteligente v2.1.0**

Esta aplicación te ayuda a gestionar la rotación de trabajadores en estaciones de trabajo de manera automática e inteligente, optimizando la distribución de personal y mejorando la eficiencia operativa.

---

## 📋 **ÍNDICE**

1. [Primeros Pasos](#primeros-pasos)
2. [Gestión de Estaciones](#gestión-de-estaciones)
3. [Gestión de Trabajadores](#gestión-de-trabajadores)
4. [Sistema de Rotación](#sistema-de-rotación)
5. [Sistema de Entrenamiento](#sistema-de-entrenamiento)
6. [Configuraciones](#configuraciones)
7. [Respaldos y Sincronización](#respaldos-y-sincronización)
8. [Solución de Problemas](#solución-de-problemas)

---

## 🚀 **PRIMEROS PASOS**

### Instalación
1. Descarga e instala la aplicación en tu dispositivo Android
2. Abre la aplicación
3. Verás la pantalla principal con 4 opciones principales

### Pantalla Principal
La pantalla principal contiene:
- **🏭 Estaciones de Trabajo**: Gestiona las áreas de trabajo
- **👥 Trabajadores**: Administra tu equipo
- **🔄 Sistema de Rotación**: Genera rotaciones automáticas
- **⚙️ Configuraciones**: Personaliza la aplicación

### Configuración Inicial Recomendada
1. **Crear Estaciones**: Mínimo 3 estaciones de trabajo
2. **Agregar Trabajadores**: Mínimo 5 trabajadores
3. **Asignar Capacidades**: Definir qué trabajadores pueden trabajar en qué estaciones
4. **Generar Primera Rotación**: Probar el sistema

---

## 🏭 **GESTIÓN DE ESTACIONES**

### Crear Nueva Estación
1. Toca **"🏭 Estaciones de Trabajo"**
2. Toca el botón **"+"** (azul) en la esquina inferior
3. Completa la información:
   - **Nombre**: Descripción clara (ej: "Control de Calidad")
   - **Trabajadores Necesarios**: Número de personas requeridas (1-10)
   - **Estación Prioritaria**: ✓ si es crítica para la operación

### Características de Estaciones Prioritarias
- **Capacidad Garantizada**: Siempre tendrán el número exacto de trabajadores
- **Asignación Preferente**: Se llenan primero en cada rotación
- **Indicador Visual**: Aparecen marcadas con ⭐

### Editar Estación
1. En la lista de estaciones, toca cualquier estación
2. Modifica la información necesaria
3. Toca **"Guardar"**

### Activar/Desactivar Estaciones
- Usa el **switch** junto a cada estación
- Las estaciones inactivas no participan en rotaciones
- Útil para mantenimiento o cierre temporal

---

## 👥 **GESTIÓN DE TRABAJADORES**

### Agregar Nuevo Trabajador
1. Toca **"👥 Trabajadores"**
2. Toca el botón **"+"** (naranja)
3. Completa la información:

#### Información Básica
- **Nombre**: Nombre completo del trabajador
- **Email**: Correo electrónico (opcional)
- **Disponibilidad**: Porcentaje de disponibilidad (0-100%)

#### Capacidades de Trabajo
- **Estaciones Asignadas**: Selecciona las estaciones donde puede trabajar
- **Restricciones**: Notas sobre limitaciones (médicas, horarios, etc.)

#### Sistema de Entrenamiento
- **En Entrenamiento**: ✓ si es un trabajador nuevo
- **Entrenador**: Selecciona quién lo entrenará
- **Estación de Entrenamiento**: Dónde recibirá el entrenamiento

### Tipos de Trabajadores

#### 👨‍🏫 Entrenadores
- Pueden entrenar a otros trabajadores
- Se asignan automáticamente con sus aprendices
- Identificados con el ícono 👨‍🏫

#### 🎯 Trabajadores en Entrenamiento
- Siempre asignados con su entrenador
- Limitados a su estación de entrenamiento
- Identificados con el ícono 🎯

#### 🏆 Trabajadores Certificados
- Han completado su entrenamiento
- Pueden trabajar en cualquier estación asignada
- Participan normalmente en rotaciones

### Gestión de Disponibilidad
- **100%**: Disponible para cualquier turno
- **75%**: Alta probabilidad de asignación
- **50%**: Probabilidad media
- **25%**: Baja probabilidad
- **0%**: No disponible (inactivo)

---

## 🔄 **SISTEMA DE ROTACIÓN**

### Generar Rotación
1. Ve a **"🔄 Sistema de Rotación"**
2. Toca **"Generar Rotación"**
3. El sistema creará automáticamente:
   - **Fase Actual**: Asignaciones para el turno presente
   - **Próxima Fase**: Asignaciones para el siguiente turno

### Algoritmo Inteligente
El sistema considera automáticamente:

#### Prioridades (de mayor a menor)
1. **Parejas Entrenador-Aprendiz** en estaciones prioritarias
2. **Parejas Entrenador-Aprendiz** en estaciones normales
3. **Trabajadores individuales** en estaciones prioritarias
4. **Trabajadores individuales** en estaciones normales

#### Factores de Optimización
- **Disponibilidad**: Porcentaje de cada trabajador
- **Capacidades**: Estaciones donde puede trabajar
- **Restricciones**: Limitaciones médicas o de horario
- **Rotación Forzada**: Evita que alguien permanezca mucho tiempo en una estación
- **Balance de Carga**: Distribuye equitativamente el trabajo

### Interpretar Resultados
La tabla de rotación muestra:
- **Columnas**: Una por cada estación de trabajo
- **Filas Superiores**: Capacidad requerida por estación
- **Fase Actual**: Trabajadores asignados ahora (fondo azul)
- **Próxima Fase**: Trabajadores para el siguiente turno (fondo naranja)

#### Indicadores Especiales
- **⭐ COMPLETA**: Estación prioritaria con capacidad completa
- **👨‍🏫🤝 [ENTRENANDO]**: Entrenador con su aprendiz
- **🎯🤝 [EN ENTRENAMIENTO]**: Aprendiz con su entrenador
- **🔄 [ROTANDO]**: Trabajador que cambia de estación
- **📍 [PERMANECE]**: Trabajador que se queda en la misma estación

### Limpiar Rotación
- Toca **"Limpiar"** para resetear y generar una nueva rotación
- Útil cuando los resultados no son satisfactorios

---

## 📚 **SISTEMA DE ENTRENAMIENTO**

### Configurar Entrenamiento
1. Al agregar un trabajador nuevo, marca **"En Entrenamiento"**
2. Selecciona un **Entrenador** de la lista de trabajadores experimentados
3. Elige la **Estación de Entrenamiento** donde aprenderá

### Funcionamiento Automático
- **Asignación Conjunta**: Entrenador y aprendiz siempre juntos
- **Estación Fija**: Solo trabajan en la estación de entrenamiento
- **Prioridad Absoluta**: Tienen prioridad sobre otras asignaciones

### Certificar Trabajadores
Cuando un trabajador completa su entrenamiento:
1. Ve a **Configuraciones** → **"🎓 Certificar Trabajadores"**
2. Selecciona los trabajadores que completaron el entrenamiento
3. Toca **"Certificar Seleccionados"**

#### Efectos de la Certificación
- El trabajador deja de estar "en entrenamiento"
- Ya no necesita estar con su entrenador
- Puede participar normalmente en rotaciones
- Se convierte en trabajador completamente capacitado

---

## ⚙️ **CONFIGURACIONES**

### Acceder a Configuraciones
Toca **"⚙️ Configuraciones"** desde la pantalla principal

### Secciones Disponibles

#### 🎨 Apariencia
**Modo Oscuro**
- **Activar**: Tema oscuro para uso nocturno
- **Desactivar**: Tema claro para uso diurno
- **Beneficios del Modo Oscuro**:
  - Reduce fatiga visual en ambientes con poca luz
  - Ahorra batería en pantallas OLED
  - Ideal para turnos nocturnos

#### 💾 Respaldo y Sincronización
**Respaldos Locales**
- **Crear Respaldo**: Guarda todos los datos en archivo local
- **Exportar**: Elige ubicación específica para guardar
- **Importar**: Restaura datos desde archivo de respaldo

**Sincronización en la Nube** (requiere configuración)
- **Sincronizar**: Sube y descarga datos automáticamente
- **Respaldo en la Nube**: Guarda en Firebase Storage
- **Gestión de Cuenta**: Controla tu cuenta de Google

#### 📚 Tutorial y Ayuda
- **Guía de Funcionamiento**: Tutorial paso a paso completo
- **Certificar Trabajadores**: Gradúa trabajadores en entrenamiento

#### ℹ️ Información
- **Acerca de la App**: Versión, desarrollador y funcionalidades

---

## 💾 **RESPALDOS Y SINCRONIZACIÓN**

### Respaldos Locales

#### Crear Respaldo
1. Ve a **Configuraciones** → **"💾 Crear Respaldo"**
2. El sistema guardará un archivo JSON con todos tus datos
3. Ubicación: Carpeta de archivos de la aplicación
4. Puedes compartir el archivo por email o mensajería

#### Exportar Respaldo
1. Toca **"📤 Exportar"**
2. Elige la ubicación donde guardar
3. Útil para transferir entre dispositivos

#### Importar Respaldo
1. Toca **"📥 Importar"**
2. Selecciona el archivo de respaldo
3. **⚠️ Advertencia**: Reemplazará todos los datos actuales

### Sincronización en la Nube

#### Configuración Inicial
1. Configura Firebase según `FIREBASE_SETUP.md`
2. Coloca `google-services.json` en la carpeta `app/`
3. Recompila la aplicación

#### Usar Sincronización
1. Ve a **Configuraciones** → **"🔄 Sincronizar"**
2. Inicia sesión con tu cuenta de Google
3. La sincronización será automática

#### Beneficios de la Nube
- **Sincronización Automática**: Entre múltiples dispositivos
- **Respaldos Seguros**: En servidores de Google
- **Acceso Remoto**: Desde cualquier lugar con internet
- **Colaboración**: Múltiples usuarios pueden acceder

---

## 🎯 **CONSEJOS Y MEJORES PRÁCTICAS**

### Configuración Inicial Óptima
1. **Crea 3-5 estaciones** con nombres descriptivos
2. **Agrega 5-10 trabajadores** con capacidades variadas
3. **Define 1-2 estaciones prioritarias** para áreas críticas
4. **Configura entrenamientos** para trabajadores nuevos

### Uso Diario Eficiente
- **Revisa rotaciones** antes de aplicarlas
- **Actualiza disponibilidad** según horarios y ausencias
- **Certifica trabajadores** cuando completen entrenamiento
- **Crea respaldos** semanalmente

### Optimización del Sistema
- **Balancea capacidades**: Asegúrate de que varios trabajadores puedan trabajar en cada estación
- **Usa restricciones**: Para limitaciones médicas o de horario
- **Ajusta disponibilidades**: Según turnos y preferencias
- **Mantén datos actualizados**: Elimina trabajadores inactivos

---

## 🔧 **SOLUCIÓN DE PROBLEMAS**

### Problemas Comunes

#### "No se pudo generar la rotación"
**Causas posibles:**
- No hay trabajadores con estaciones asignadas
- Todas las estaciones están inactivas
- No hay suficientes trabajadores disponibles

**Soluciones:**
1. Verifica que los trabajadores tengan estaciones asignadas
2. Activa al menos una estación de trabajo
3. Aumenta la disponibilidad de los trabajadores
4. Agrega más trabajadores al sistema

#### "No hay trabajadores elegibles"
**Causas:**
- Todos los trabajadores están inactivos
- Ningún trabajador tiene estaciones asignadas
- Disponibilidad muy baja en todos los trabajadores

**Soluciones:**
1. Activa trabajadores en la lista
2. Asigna estaciones a los trabajadores
3. Aumenta porcentajes de disponibilidad

#### Rotación no satisfactoria
**Mejoras:**
1. Ajusta la disponibilidad de trabajadores específicos
2. Revisa las restricciones de trabajadores
3. Verifica las capacidades asignadas
4. Considera agregar más trabajadores

### Problemas de Sincronización

#### "Firebase no está disponible"
- La sincronización en la nube requiere configuración
- Consulta `FIREBASE_SETUP.md` para instrucciones
- Mientras tanto, usa respaldos locales

#### Error de autenticación
- Verifica conexión a internet
- Intenta cerrar sesión y volver a iniciar
- Revisa que el proyecto Firebase esté configurado correctamente

### Rendimiento

#### La app va lenta
- Cierra otras aplicaciones
- Reinicia la aplicación
- Verifica espacio de almacenamiento disponible

#### Problemas de memoria
- Limpia trabajadores inactivos
- Reduce el número de estaciones si es excesivo
- Crea respaldos y limpia datos antiguos

---

## 📞 **SOPORTE Y CONTACTO**

### Información del Desarrollador
- **Desarrollador**: Brandon Josué Hidalgo Paz
- **Versión**: Sistema de Rotación Inteligente v2.1.0
- **Año**: 2024

### Recursos Adicionales
- **FIREBASE_SETUP.md**: Configuración de sincronización en la nube
- **FUNCIONES_DEL_SISTEMA.md**: Documentación técnica detallada
- **CHANGELOG.md**: Historial de cambios y actualizaciones

### Reportar Problemas
Si encuentras algún problema:
1. Anota los pasos que causaron el error
2. Toma capturas de pantalla si es posible
3. Verifica la versión de la aplicación
4. Crea un respaldo de tus datos antes de reportar

---

## 🎉 **¡LISTO PARA USAR!**

Ya tienes toda la información necesaria para usar eficientemente el Sistema de Rotación Inteligente. 

### Flujo de Trabajo Recomendado
1. **Configuración**: Estaciones → Trabajadores → Capacidades
2. **Primera Rotación**: Generar → Revisar → Aplicar
3. **Uso Diario**: Actualizar disponibilidad → Generar nueva rotación
4. **Mantenimiento**: Certificar trabajadores → Crear respaldos → Optimizar

¡Disfruta de una gestión de rotaciones más eficiente y automatizada!

---

*© 2024 - Sistema de Rotación Inteligente - Todos los derechos reservados*