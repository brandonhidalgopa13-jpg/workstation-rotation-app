# 📱 REWS v2.4.0 - Guía de Instalación

## 🎯 Opciones de Instalación

### 📦 **Opción 1: APK Pre-compilado (Recomendado)**

#### 📋 **Requisitos del Sistema**
- **Android**: 7.0 (API 24) o superior
- **RAM**: Mínimo 2 GB recomendado
- **Almacenamiento**: 50 MB de espacio libre
- **Permisos**: Almacenamiento, Notificaciones (opcional)

#### 🔽 **Pasos de Instalación**
1. **Descargar APK**:
   - Ve a [Releases](https://github.com/brandonhidalgopa13-jpg/workstation-rotation-app/releases)
   - Descarga `REWS-v2.4.0-release.apk`

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

#### 3️⃣ **Configurar Liderazgo (Opcional)**
```
1. Edita un trabajador existente
2. Marca "Es Líder"
3. Selecciona tipo de liderazgo:
   • Ambas partes: Líder en toda la rotación
   • Primera parte: Solo primera mitad
   • Segunda parte: Solo segunda mitad
4. Selecciona estación de liderazgo
5. Guarda los cambios
```

#### 4️⃣ **Generar Primera Rotación**
```
1. Ve a la pantalla principal
2. Toca "Generar Rotación"
3. Revisa la rotación generada
4. Verifica asignaciones de líderes (fondo púrpura)
5. Confirma si está correcta
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
- [ ] App se abre sin errores
- [ ] Puede crear estaciones de trabajo
- [ ] Puede registrar trabajadores
- [ ] Puede generar rotaciones
- [ ] Rotaciones muestran trabajadores correctamente

#### 👑 **Sistema de Liderazgo**
- [ ] Puede designar líderes
- [ ] Líderes aparecen con fondo púrpura
- [ ] Mensaje "LÍDER DE ESTACIÓN" visible
- [ ] Líderes asignados a estaciones correctas

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
Solución:
1. Verifica que estaciones estén activas
2. Confirma que trabajadores tengan estaciones asignadas
3. Revisa que haya suficientes trabajadores disponibles
4. Ve a Configuraciones > Avanzado > Verificar Integridad
```

#### **"Líderes no se muestran correctamente"**
```
Solución:
1. Confirma que trabajador esté marcado como líder
2. Verifica que tenga estación de liderazgo asignada
3. Regenera la rotación
4. Revisa configuración de prioridad de liderazgo
```

#### **"App funciona lenta"**
```
Solución:
1. Ve a Configuraciones > Avanzado > Optimizaciones
2. Ejecuta "Optimización Automática"
3. Limpia caché en Configuraciones > Rendimiento
4. Reinicia la aplicación
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
- **Proyecto**: REWS v2.4.0
- **GitHub**: [Repositorio del Proyecto](https://github.com/brandonhidalgopa13-jpg/workstation-rotation-app)

---

## 🎉 ¡Instalación Completada!

Tu sistema REWS v2.4.0 está ahora listo para uso en producción. 

**Próximos pasos recomendados:**
1. Completa la configuración inicial
2. Crea tu primer respaldo
3. Configura notificaciones
4. Explora las funcionalidades avanzadas
5. Genera tu primer reporte

---

*© 2024 Brandon Josué Hidalgo Paz - REWS v2.4.0*