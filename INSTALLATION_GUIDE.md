# 📱 REWS v2.6.2 - Guía de Instalación Completa

## 🏭 ¿Qué es REWS?

**REWS (Rotation Efficient Workstation System)** es un sistema inteligente de rotación de trabajadores en estaciones de trabajo que optimiza la distribución de personal considerando múltiples factores como liderazgo, entrenamiento, disponibilidad y restricciones laborales.

### 🎯 Características Principales

#### 👑 **Sistema de Liderazgo Avanzado**
- **Líderes "BOTH"**: Permanecen en su estación asignada en AMBAS partes de la rotación
- **Líderes "FIRST_HALF"**: Solo activos en la primera mitad de la rotación
- **Líderes "SECOND_HALF"**: Solo activos en la segunda mitad de la rotación
- **Identificación visual**: Líderes se muestran con fondo púrpura y mensaje "👑 LÍDER DE ESTACIÓN"

#### 🎓 **Sistema de Entrenamiento Integrado**
- **Parejas entrenador-entrenado**: Siempre asignados juntos en la estación de entrenamiento
- **Certificación automática**: Al completar entrenamiento, el trabajador puede rotar libremente
- **Prioridad máxima**: Las parejas de entrenamiento tienen prioridad absoluta en asignaciones

#### 🔄 **Algoritmo de Rotación Inteligente**
- **Rotación dual**: Genera posición actual y siguiente simultáneamente
- **Optimización automática**: Considera disponibilidad, restricciones y capacidades
- **Estaciones prioritarias**: Asegura capacidad completa en áreas críticas
- **Variación aleatoria**: Evita patrones repetitivos en las rotaciones

#### 📊 **Gestión Avanzada**
- **Reportes detallados**: Exportación en múltiples formatos (imagen, texto, archivo)
- **Respaldos automáticos**: Sistema de backup y restauración de datos
- **Sincronización en la nube**: Opcional con Firebase
- **Notificaciones inteligentes**: Alertas para rotaciones, entrenamientos y liderazgo

## 🎯 Opciones de Instalación

### 📦 **Opción 1: APK Pre-compilado (Recomendado)**

#### 📋 **Requisitos del Sistema**
- **Android**: 7.0 (API 24) o superior
- **RAM**: Mínimo 2 GB recomendado (4 GB para mejor rendimiento)
- **Almacenamiento**: 100 MB de espacio libre (incluye datos y respaldos)
- **Permisos**: Almacenamiento, Notificaciones, Acceso a archivos
- **Procesador**: ARM64 o x86 (compatible con la mayoría de dispositivos)
- **Conectividad**: WiFi o datos móviles (opcional para sincronización)

#### 🔽 **Pasos de Instalación**
1. **Descargar APK**:
   - Ve a [Releases](https://github.com/brandonhidalgopa13-jpg/workstation-rotation-app/releases)
   - Descarga `REWS-v2.6.2-release.apk` (versión más reciente)

2. **Habilitar Fuentes Desconocidas**:
   - Ve a `Configuración > Seguridad`
   - Habilita `Fuentes desconocidas` o `Instalar apps desconocidas`

3. **Instalar APK**:
   - Abre el archivo APK descargado
   - Toca `Instalar`
   - Espera a que complete la instalación

4. **Primera Ejecución**:
   - Abre la app REWS
   - Acepta permisos necesarios
   - Sigue el tutorial inicial

---

### 🛠️ **Opción 2: Compilar desde Código Fuente**

#### 📋 **Requisitos de Desarrollo**
- **Android Studio**: Arctic Fox o superior
- **JDK**: 8 o superior
- **Android SDK**: API 24-34
- **Gradle**: 7.0+
- **Git**: Para clonar el repositorio

#### 🔽 **Pasos de Compilación**

1. **Clonar Repositorio**:
   ```bash
   git clone https://github.com/brandonhidalgopa13-jpg/workstation-rotation-app.git
   cd workstation-rotation-app
   ```

2. **Configurar Android Studio**:
   - Abre Android Studio
   - Selecciona `Open an existing project`
   - Navega a la carpeta clonada
   - Espera a que Gradle sincronice

3. **Configurar SDK**:
   - Ve a `File > Project Structure`
   - Configura Android SDK path
   - Instala SDK API 24-34 si es necesario

4. **Compilar Debug**:
   ```bash
   ./gradlew assembleDebug
   ```

5. **Compilar Release**:
   ```bash
   ./gradlew assembleRelease
   ```

6. **Encontrar APK**:
   - Debug: `app/build/outputs/apk/debug/app-debug.apk`
   - Release: `app/build/outputs/apk/release/app-release.apk`

---

## 🔧 Configuración Inicial

### 🏭 **Configuración Básica**

#### 1️⃣ **Crear Estaciones de Trabajo**
```
1. Toca "Estaciones de Trabajo" en la pantalla principal
2. Toca el botón "+" para agregar nueva estación
3. Completa la información:
   • Nombre: "Control de Calidad"
   • Trabajadores requeridos: 2
   • Capacidades: "Inspección, Medición"
   • Marcar como prioritaria si es crítica
4. Guarda la estación
5. Repite para todas las estaciones necesarias
```

#### 2️⃣ **Registrar Trabajadores**
```
1. Toca "Trabajadores" en la pantalla principal
2. Toca el botón "+" para agregar trabajador
3. Completa la información:
   • Nombre: "Juan Pérez"
   • Email: "juan.perez@empresa.com"
   • Disponibilidad: 90%
   • Selecciona estaciones donde puede trabajar
   • Configura entrenamiento si es necesario
4. Guarda el trabajador
5. Repite para todo el personal
```

#### 3️⃣ **Configurar Sistema de Liderazgo (CRÍTICO)**
```
IMPORTANTE: El sistema de liderazgo es fundamental para las rotaciones.
Los líderes garantizan supervisión y continuidad operativa.

1. Edita un trabajador existente o crea uno nuevo
2. Marca "Es Líder" ✅
3. Selecciona tipo de liderazgo:
   • BOTH (Ambas partes): Líder FIJO en su estación en TODA la rotación
     - Nunca rota, siempre permanece en la misma estación
     - Ideal para supervisores permanentes
   • FIRST_HALF (Primera parte): Solo activo en primera mitad
     - Rota normalmente en segunda parte
   • SECOND_HALF (Segunda parte): Solo activo en segunda mitad
     - Rota normalmente en primera parte
4. Selecciona estación de liderazgo (solo estaciones asignadas al trabajador)
5. Guarda los cambios

NOTA: Los líderes aparecen con fondo PÚRPURA y mensaje "👑 LÍDER DE ESTACIÓN"
```

#### 4️⃣ **Configurar Sistema de Entrenamiento (Opcional)**
```
1. Selecciona un trabajador experimentado como entrenador:
   • Edita trabajador → Marca "Es Entrenador" ✅
   • Entrenadores se muestran con icono 👨‍🏫

2. Configura trabajador en entrenamiento:
   • Crea nuevo trabajador → Marca "En Entrenamiento" ✅
   • Selecciona entrenador asignado
   • Selecciona estación de entrenamiento
   • Trabajadores en entrenamiento se muestran con icono 🎯

3. Sistema automático:
   • Entrenador y entrenado SIEMPRE van juntos
   • Se asignan a la estación de entrenamiento especificada
   • Tienen prioridad MÁXIMA sobre otros trabajadores
```

#### 5️⃣ **Generar Primera Rotación**
```
1. Ve a la pantalla principal
2. Toca "Generar Rotación"
3. El sistema aplicará automáticamente:
   • Líderes "BOTH" fijos en sus estaciones (fondo púrpura)
   • Líderes activos según la parte de rotación
   • Parejas entrenador-entrenado juntas
   • Distribución optimizada del resto de trabajadores

4. Verifica la rotación:
   • Líderes con fondo PÚRPURA y mensaje "👑 LÍDER DE ESTACIÓN"
   • Parejas de entrenamiento en la misma estación
   • Estaciones prioritarias con capacidad completa
   • Distribución equilibrada de personal

5. Para alternar entre partes:
   • Toca "Alternar Parte" para cambiar entre primera/segunda mitad
   • Regenera rotación para ver cambios en liderazgo
```

---

## 👑 Guía Detallada del Sistema de Liderazgo

### 🎯 **¿Por qué es importante el Sistema de Liderazgo?**

El sistema de liderazgo es **CRÍTICO** para el funcionamiento correcto de las rotaciones. Sin líderes configurados adecuadamente:
- ❌ Las estaciones pueden quedar sin supervisión
- ❌ La continuidad operativa se ve comprometida
- ❌ No hay responsables claros en cada área
- ❌ Las rotaciones pueden ser menos eficientes

### 📋 **Tipos de Liderazgo Explicados**

#### 🔒 **Líder "BOTH" (Ambas Partes)**
- **Comportamiento**: Permanece FIJO en su estación asignada
- **Rotación**: NUNCA rota, siempre en la misma estación
- **Uso recomendado**: Supervisores permanentes, responsables de área
- **Identificación visual**: Fondo púrpura, borde grueso, número dorado
- **Ejemplo**: Supervisor de Control de Calidad que debe estar siempre presente

#### 🔄 **Líder "FIRST_HALF" (Primera Parte)**
- **Comportamiento**: Líder solo en la primera mitad de la rotación
- **Rotación**: En segunda parte rota como trabajador normal
- **Uso recomendado**: Líderes de turno matutino
- **Ejemplo**: Supervisor que solo trabaja en el primer turno

#### 🔄 **Líder "SECOND_HALF" (Segunda Parte)**
- **Comportamiento**: Líder solo en la segunda mitad de la rotación
- **Rotación**: En primera parte rota como trabajador normal
- **Uso recomendado**: Líderes de turno vespertino
- **Ejemplo**: Supervisor que solo trabaja en el segundo turno

### 🛠️ **Configuración Paso a Paso**

#### **Paso 1: Identificar Necesidades de Liderazgo**
```
1. Analiza tus estaciones de trabajo
2. Identifica cuáles necesitan supervisión permanente (usa "BOTH")
3. Identifica cuáles necesitan supervisión por turnos (usa "FIRST_HALF"/"SECOND_HALF")
4. Selecciona trabajadores con experiencia y habilidades de liderazgo
```

#### **Paso 2: Configurar Líder "BOTH"**
```
1. Ve a Trabajadores → Selecciona trabajador experimentado
2. Toca "Editar"
3. Marca "Es Líder" ✅
4. Selecciona "BOTH" en tipo de liderazgo
5. Selecciona la estación donde debe liderar
6. Guarda cambios
7. Genera rotación → Verifica que aparece con fondo púrpura
```

#### **Paso 3: Configurar Líderes de Turno**
```
Para líder de primer turno:
1. Configura como "FIRST_HALF"
2. Genera rotación en primera parte → Debe aparecer como líder
3. Alterna a segunda parte → Debe aparecer como trabajador normal

Para líder de segundo turno:
1. Configura como "SECOND_HALF"
2. Genera rotación en primera parte → Debe aparecer como trabajador normal
3. Alterna a segunda parte → Debe aparecer como líder
```

### 🔍 **Verificación del Sistema**

#### **Lista de Verificación Completa**
- [ ] **Líderes "BOTH" configurados**: Al menos uno por estación crítica
- [ ] **Estaciones asignadas**: Cada líder tiene estación específica
- [ ] **Identificación visual**: Fondo púrpura y mensaje "👑 LÍDER DE ESTACIÓN"
- [ ] **Comportamiento "BOTH"**: Permanecen fijos en ambas partes
- [ ] **Comportamiento por turnos**: Cambian según la parte activa
- [ ] **Función "Alternar Parte"**: Funciona correctamente

#### **Prueba Completa del Sistema**
```
1. Configura al menos un líder de cada tipo
2. Genera rotación en primera parte
3. Anota posiciones de todos los líderes
4. Usa "Alternar Parte"
5. Genera nueva rotación
6. Verifica:
   • Líderes "BOTH" en la MISMA posición
   • Líderes "FIRST_HALF" ahora como trabajadores normales
   • Líderes "SECOND_HALF" ahora como líderes
```

### ⚠️ **Problemas Comunes y Soluciones**

#### **"Líder 'BOTH' cambia de estación"**
```
CAUSA: Error en la configuración o problema en el algoritmo
SOLUCIÓN:
1. Verifica que el tipo sea exactamente "BOTH"
2. Confirma que tiene estación de liderazgo asignada
3. Regenera la rotación completamente
4. Si persiste, reporta como bug
```

#### **"Líder no aparece en su turno"**
```
CAUSA: Tipo de liderazgo incorrecto o parte de rotación incorrecta
SOLUCIÓN:
1. Verifica el tipo de liderazgo configurado
2. Confirma que estás en la parte correcta de rotación
3. Usa "Alternar Parte" si es necesario
```

#### **"Múltiples líderes en una estación"**
```
CAUSA: Configuración incorrecta de estaciones de liderazgo
SOLUCIÓN:
1. Revisa que cada líder tenga estación única
2. O configura diferentes tipos de liderazgo
3. Ajusta configuración según necesidades operativas
```

---

## ⚙️ Configuración Avanzada

### 🔔 **Configurar Notificaciones**
```
1. Ve a Configuraciones > Notificaciones
2. Habilita tipos de notificaciones deseadas:
   • Rotaciones: Para nuevas rotaciones
   • Entrenamiento: Para completar entrenamientos
   • Liderazgo: Para cambios de líderes
   • Alertas: Para problemas del sistema
3. Guarda configuración
```

### 📊 **Configurar Reportes**
```
1. Ve a Configuraciones > Generar Reporte
2. Selecciona tipo de reporte:
   • Ver Reporte: Mostrar en pantalla
   • Compartir Imagen: Enviar imagen PNG
   • Compartir Texto: Enviar texto plano
   • Guardar Archivo: Almacenar localmente
```

### ⚡ **Optimizar Rendimiento**
```
1. Ve a Configuraciones > Avanzado > Optimizaciones
2. Ejecuta "Optimización Automática"
3. Configura parámetros del algoritmo:
   • Ciclos de rotación forzada: 2 (recomendado)
   • Umbral de disponibilidad: 50%
   • Prioridad de liderazgo: Activada
   • Prioridad de entrenamiento: Activada
```

---

## 🔒 Configuración de Seguridad

### 💾 **Configurar Respaldos**
```
1. Ve a Configuraciones > Respaldo y Sincronización
2. Crea respaldo inicial:
   • Toca "Crear Respaldo"
   • Espera a que complete
   • Comparte el archivo si es necesario
3. Programa respaldos regulares (recomendado semanal)
```

### ☁️ **Configurar Sincronización en la Nube (Opcional)**
```
1. Configura Firebase (si disponible):
   • Descarga google-services.json
   • Coloca en carpeta app/
   • Recompila la aplicación
2. Ve a Configuraciones > Sincronización en la Nube
3. Inicia sesión con cuenta Google
4. Configura sincronización automática
```

---

## 🧪 Verificación de Instalación

### ✅ **Lista de Verificación**

#### 📱 **Funcionalidad Básica**
- [ ] App se abre sin errores y muestra tutorial inicial
- [ ] Puede crear estaciones de trabajo (normales y prioritarias)
- [ ] Puede registrar trabajadores con diferentes roles
- [ ] Puede generar rotaciones inteligentes
- [ ] Rotaciones muestran trabajadores con indicadores visuales correctos
- [ ] Sistema respeta restricciones y disponibilidad de trabajadores

#### 🎓 **Sistema de Entrenamiento**
- [ ] Puede designar entrenadores (icono 👨‍🏫)
- [ ] Puede configurar trabajadores en entrenamiento (icono 🎯)
- [ ] Parejas entrenador-entrenado aparecen siempre juntas
- [ ] Se asignan a la estación de entrenamiento especificada
- [ ] Proceso de certificación funciona correctamente (icono 🏆)

#### 👑 **Sistema de Liderazgo**
- [ ] Puede designar líderes con diferentes tipos (BOTH/FIRST_HALF/SECOND_HALF)
- [ ] Líderes aparecen con fondo púrpura y borde grueso
- [ ] Mensaje "👑 LÍDER DE ESTACIÓN" visible
- [ ] Líderes "BOTH" permanecen fijos en ambas partes de rotación
- [ ] Líderes "FIRST_HALF" solo aparecen en primera parte
- [ ] Líderes "SECOND_HALF" solo aparecen en segunda parte
- [ ] Función "Alternar Parte" cambia correctamente el liderazgo activo

#### 📊 **Funcionalidades Avanzadas**
- [ ] Puede generar reportes
- [ ] Notificaciones funcionan
- [ ] Configuraciones avanzadas accesibles
- [ ] Respaldos se crean correctamente

#### ⚡ **Rendimiento**
- [ ] App responde rápidamente (< 2 segundos)
- [ ] Rotaciones se generan sin demora
- [ ] No hay crashes o errores
- [ ] Memoria se mantiene estable

---

## 🐛 Solución de Problemas

### ❌ **Problemas Comunes**

#### **"App no se instala"**
```
Solución:
1. Verifica que Android sea 7.0+
2. Habilita "Fuentes desconocidas"
3. Libera espacio de almacenamiento
4. Reinicia el dispositivo
```

#### **"Estaciones no aparecen en rotación"**
```
Diagnóstico completo:

1. Verificar estado de estaciones:
   • Ve a Estaciones de Trabajo
   • Confirma que estén marcadas como "Activas" ✅
   • Verifica que tengan capacidad > 0

2. Verificar asignaciones de trabajadores:
   • Ve a Trabajadores → Selecciona cada trabajador
   • Confirma que tengan estaciones asignadas
   • Al menos un trabajador debe estar asignado a cada estación

3. Verificar disponibilidad:
   • Revisa que trabajadores estén "Activos" ✅
   • Confirma que tengan disponibilidad > 0%
   • Trabajadores con 0% disponibilidad no aparecen en rotaciones

4. Verificar restricciones:
   • Revisa si hay restricciones que impidan asignaciones
   • Ve a Configuraciones → Avanzado → Verificar Integridad
   • Ejecuta diagnóstico automático del sistema
```

#### **"Líderes no se muestran correctamente"**
```
Diagnóstico paso a paso:

1. Verificar configuración del líder:
   • Ve a Trabajadores → Selecciona el líder
   • Confirma que "Es Líder" esté marcado ✅
   • Verifica que tenga estación de liderazgo asignada
   • Confirma el tipo de liderazgo (BOTH/FIRST_HALF/SECOND_HALF)

2. Verificar parte de rotación activa:
   • Líderes "FIRST_HALF" solo aparecen en primera parte
   • Líderes "SECOND_HALF" solo aparecen en segunda parte
   • Líderes "BOTH" aparecen en AMBAS partes
   • Usa "Alternar Parte" para cambiar entre partes

3. Verificar asignaciones de estación:
   • El líder debe estar asignado a la estación donde quiere liderar
   • Ve a Trabajadores → Editar → Estaciones asignadas
   • La estación de liderazgo debe estar en la lista

4. Regenerar rotación:
   • Toca "Generar Rotación" después de verificar configuración
   • Los líderes deben aparecer con fondo PÚRPURA
   • Mensaje "👑 LÍDER DE ESTACIÓN" debe ser visible

5. Si persiste el problema:
   • Ve a Configuraciones → Avanzado → Verificar Integridad
   • Revisa logs en Configuraciones → Debugging
```

#### **"App funciona lenta"**
```
Solución:
1. Ve a Configuraciones > Avanzado > Optimizaciones
2. Ejecuta "Optimización Automática"
3. Limpia caché en Configuraciones > Rendimiento
4. Reinicia la aplicación
```

#### **"Sistema de liderazgo no funciona correctamente"**
```
PROBLEMA CRÍTICO: Este es el problema más reportado. Diagnóstico completo:

1. Verificar configuración básica:
   • Ve a Trabajadores → Selecciona el líder
   • Confirma "Es Líder" marcado ✅
   • Verifica tipo: "BOTH", "FIRST_HALF", o "SECOND_HALF"
   • Confirma estación de liderazgo asignada

2. Verificar asignaciones de estación:
   • El trabajador DEBE estar asignado a la estación donde quiere liderar
   • Ve a Editar Trabajador → Estaciones asignadas
   • La estación de liderazgo debe aparecer en la lista

3. Verificar parte de rotación activa:
   • Líderes "FIRST_HALF" solo aparecen en primera parte
   • Líderes "SECOND_HALF" solo aparecen en segunda parte
   • Líderes "BOTH" aparecen en AMBAS partes
   • Usa botón "Alternar Parte" para cambiar

4. Verificar identificación visual:
   • Líderes deben tener fondo PÚRPURA
   • Borde grueso púrpura
   • Mensaje "👑 LÍDER DE ESTACIÓN"
   • Número de rotación con fondo dorado

5. Solución de emergencia:
   • Ve a Configuraciones → Avanzado → Limpiar Caché
   • Regenera la rotación completamente
   • Si persiste, elimina y recrea el líder
```

#### **"Líderes 'BOTH' rotan cuando no deberían"**
```
PROBLEMA CRÍTICO IDENTIFICADO Y CORREGIDO EN v2.6.1:

Síntomas:
• Líder "BOTH" aparece en estación correcta en primera rotación
• En segunda rotación aparece en estación diferente
• Comportamiento inconsistente

Solución:
1. Actualiza a versión v2.6.2 o superior
2. Verifica que el tipo sea exactamente "BOTH" (no "Both" o "both")
3. Regenera rotación después de actualizar
4. Los líderes "BOTH" ahora tienen prioridad ABSOLUTA y nunca rotan

Verificación:
• Genera rotación en primera parte
• Anota posición del líder "BOTH"
• Alterna a segunda parte y regenera
• Líder "BOTH" debe estar en la MISMA estación
```

#### **"Notificaciones no aparecen"**
```
Solución:
1. Ve a Configuración del sistema > Apps > REWS > Notificaciones
2. Habilita todas las categorías de notificaciones
3. Verifica que no esté en modo "No molestar"
4. Prueba notificación desde Configuraciones > Notificaciones
```

---

## 📞 Soporte Técnico

### 🔗 **Recursos de Ayuda**
- **Documentación**: Ver archivos MD en el repositorio
- **Tutorial Integrado**: Disponible en la app
- **Guía de Usuario**: `GUIA_USUARIO_RAPIDA.md`
- **FAQ**: Preguntas frecuentes en documentación

### 🐛 **Reportar Problemas**
1. Ve a [GitHub Issues](https://github.com/brandonhidalgopa13-jpg/workstation-rotation-app/issues)
2. Busca si el problema ya fue reportado
3. Si no existe, crea un nuevo issue con:
   - Descripción detallada del problema
   - Pasos para reproducir
   - Versión de Android
   - Screenshots si es posible

### 📧 **Contacto**
- **Desarrollador**: Brandon Josué Hidalgo Paz
- **Proyecto**: REWS v2.6.2
- **GitHub**: [Repositorio del Proyecto](https://github.com/brandonhidalgopa13-jpg/workstation-rotation-app)

### 🔄 **Historial de Versiones Importantes**
- **v2.6.2**: Corrección de recursos duplicados y optimizaciones
- **v2.6.1**: Corrección crítica de líderes "BOTH" - ahora funcionan correctamente
- **v2.6.0**: Mejoras en interfaz de rotación y sistema de liderazgo
- **v2.5.3**: Correcciones de errores críticos y estabilidad
- **v2.5.2**: Mejoras en sistema de certificación y liderazgo
- **v2.4.0**: Implementación inicial del sistema de liderazgo avanzado

---

## 🎉 ¡Instalación Completada!

Tu sistema REWS v2.6.2 está ahora listo para uso en producción con el sistema de liderazgo completamente funcional. 

**Próximos pasos recomendados:**
1. **Configuración inicial completa**:
   - Crea todas las estaciones de trabajo necesarias
   - Registra todos los trabajadores con sus roles correctos
   - Configura al menos un líder "BOTH" por estación crítica

2. **Prueba el sistema de liderazgo**:
   - Genera rotación en primera parte
   - Usa "Alternar Parte" y regenera rotación
   - Verifica que líderes "BOTH" permanecen fijos
   - Confirma que líderes de parte específica cambian correctamente

3. **Configura entrenamiento** (si aplica):
   - Designa entrenadores experimentados
   - Configura trabajadores en entrenamiento
   - Verifica que aparezcan juntos en rotaciones

4. **Crea tu primer respaldo**:
   - Ve a Configuraciones → Respaldo y Sincronización
   - Crea respaldo completo de tu configuración

5. **Optimiza el sistema**:
   - Configura notificaciones según tus necesidades
   - Ajusta parámetros de optimización si es necesario
   - Genera reportes para análisis de distribución

6. **Monitoreo continuo**:
   - Revisa regularmente la efectividad de las rotaciones
   - Ajusta tipos de liderazgo según necesidades operativas
   - Actualiza disponibilidad y restricciones de trabajadores

---

## 🚨 Notas Importantes sobre el Sistema de Liderazgo

### ⚠️ **CRÍTICO: Configuración Correcta del Liderazgo**
El sistema de liderazgo es fundamental para el funcionamiento correcto de las rotaciones. **Sin líderes configurados adecuadamente, las rotaciones no considerarán la supervisión necesaria**.

### 🎯 **Recomendaciones Operativas**
1. **Configura al menos un líder "BOTH" por estación crítica**
2. **Usa líderes de turno ("FIRST_HALF"/"SECOND_HALF") para cobertura completa**
3. **Verifica regularmente que los líderes aparezcan con fondo púrpura**
4. **Prueba el sistema alternando entre partes de rotación**

### 🔧 **Si encuentras problemas**
1. **Actualiza a la versión más reciente** (v2.6.2 o superior)
2. **Sigue la guía de diagnóstico** en la sección de problemas comunes
3. **Reporta bugs específicos** en el repositorio de GitHub
4. **Incluye capturas de pantalla** y descripción detallada del problema

---

*© 2025 Brandon Josué Hidalgo Paz - REWS v2.6.2*  
*Sistema de Rotación Inteligente con Liderazgo Avanzado*