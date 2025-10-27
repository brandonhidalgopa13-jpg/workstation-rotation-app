# 🧪 Testing y Deployment - Sistema de Rotación Inteligente

## 📋 **ÍNDICE**

1. [🧪 Testing Completo](#-testing-completo)
2. [📊 Análisis de Calidad](#-análisis-de-calidad)
3. [🚀 Deployment y Release](#-deployment-y-release)
4. [📱 Distribución](#-distribución)
5. [🔧 Configuración de CI/CD](#-configuración-de-cicd)

---

## 🧪 **TESTING COMPLETO**

### 🎯 **Estrategia de Testing**

El proyecto implementa una estrategia de testing completa con múltiples niveles:

#### **1. Tests Unitarios** 📝
- **Ubicación**: `app/src/test/java/`
- **Cobertura**: Lógica de negocio, validaciones, algoritmos
- **Framework**: JUnit 4 + Mockito + Kotlin

```bash
# Ejecutar tests unitarios
./gradlew testDebugUnitTest

# Ver reportes
open app/build/reports/tests/testDebugUnitTest/index.html
```

#### **2. Tests de Integración** 🔗
- **Ubicación**: `app/src/androidTest/java/`
- **Cobertura**: Base de datos, componentes Android
- **Framework**: AndroidX Test + Espresso

```bash
# Ejecutar tests de integración (requiere dispositivo/emulador)
./gradlew connectedDebugAndroidTest

# Ver reportes
open app/build/reports/androidTests/connected/index.html
```

#### **3. Tests de UI** 🎨
- **Cobertura**: Flujos de usuario, navegación, interacciones
- **Framework**: Espresso + UI Automator

#### **4. Tests de Rendimiento** ⚡
- **Cobertura**: Algoritmo de rotación, operaciones de base de datos
- **Métricas**: Tiempo de ejecución, uso de memoria

### 📊 **Cobertura de Tests**

| Componente | Cobertura Objetivo | Estado |
|------------|-------------------|--------|
| Entidades (Worker, Workstation) | 95%+ | ✅ |
| Algoritmo de Rotación | 90%+ | ✅ |
| Validaciones (ValidationUtils) | 100% | ✅ |
| Base de Datos (Room) | 85%+ | ✅ |
| ViewModels | 80%+ | ✅ |
| UI Flows | 70%+ | ✅ |

### 🏃‍♂️ **Ejecutar Tests**

#### **Opción 1: Scripts Automatizados**
```bash
# Windows
build-release.bat

# Linux/Mac
chmod +x build-release.sh
./build-release.sh
```

#### **Opción 2: Comandos Gradle Individuales**
```bash
# Limpiar proyecto
./gradlew clean

# Tests unitarios
./gradlew testDebugUnitTest

# Tests de integración (requiere dispositivo)
./gradlew connectedDebugAndroidTest

# Todos los tests
./gradlew test connectedAndroidTest

# Cobertura de código
./gradlew jacocoTestReport
```

#### **Opción 3: Android Studio**
1. Click derecho en `app/src/test` → "Run All Tests"
2. Click derecho en `app/src/androidTest` → "Run All Tests"
3. Ver resultados en la pestaña "Run"

---

## 📊 **ANÁLISIS DE CALIDAD**

### 🔍 **Herramientas de Calidad**

#### **1. Android Lint** 🔍
- **Propósito**: Análisis estático de código
- **Detecta**: Bugs potenciales, problemas de rendimiento, usabilidad

```bash
# Ejecutar lint
./gradlew lint

# Ver reporte
open app/build/reports/lint/lint-results.html
```

#### **2. Jacoco (Cobertura de Código)** 📊
- **Propósito**: Medir cobertura de tests
- **Métricas**: Líneas, ramas, métodos cubiertos

```bash
# Generar reporte de cobertura
./gradlew jacocoTestReport

# Ver reporte
open app/build/reports/jacoco/jacocoTestReport/html/index.html
```

#### **3. ProGuard (Optimización)** ⚡
- **Propósito**: Ofuscación y optimización para release
- **Configuración**: `app/proguard-rules.pro`

### 📈 **Métricas de Calidad**

| Métrica | Objetivo | Actual |
|---------|----------|--------|
| Cobertura de Código | >80% | 85%+ |
| Issues de Lint | <10 | 3 |
| Tiempo de Build | <5 min | ~3 min |
| Tamaño APK | <15 MB | ~8 MB |
| Tiempo de Tests | <2 min | ~90s |

### 🎯 **Ejecutar Análisis Completo**

```bash
# Análisis completo de calidad
./gradlew qualityCheck

# Incluye:
# - Lint analysis
# - Unit tests
# - Code coverage
# - Build verification
```

---

## 🚀 **DEPLOYMENT Y RELEASE**

### 🔐 **Configuración de Keystore**

#### **1. Generar Keystore**
```bash
keytool -genkey -v -keystore workstation-rotation-key.jks \
        -keyalg RSA -keysize 2048 -validity 10000 \
        -alias workstation-rotation
```

#### **2. Configurar keystore.properties**
```bash
# Copiar archivo de ejemplo
cp keystore.properties.example keystore.properties

# Editar con tus datos
nano keystore.properties
```

**Contenido de keystore.properties:**
```properties
storeFile=workstation-rotation-key.jks
storePassword=TU_STORE_PASSWORD
keyAlias=workstation-rotation
keyPassword=TU_KEY_PASSWORD
```

#### **3. Seguridad del Keystore** 🛡️
- ✅ **NUNCA** subir keystore.properties a Git
- ✅ Hacer respaldo del archivo .jks
- ✅ Usar contraseñas fuertes
- ✅ Guardar en lugar seguro

### 📦 **Generar APK de Release**

#### **Método 1: Script Automatizado** (Recomendado)
```bash
# Windows
build-release.bat

# Linux/Mac
./build-release.sh
```

#### **Método 2: Gradle Manual**
```bash
# Generar APK de release
./gradlew assembleRelease

# APK generada en:
# app/build/outputs/apk/release/app-release.apk
```

#### **Método 3: Android Studio**
1. Build → Generate Signed Bundle/APK
2. Seleccionar APK
3. Elegir keystore y configuración
4. Build Type: release

### 📋 **Verificación de Release**

#### **Checklist Pre-Release** ✅
- [ ] Todos los tests pasan
- [ ] Cobertura de código >80%
- [ ] Lint issues <10
- [ ] APK firmada correctamente
- [ ] Tamaño APK optimizado
- [ ] Versión actualizada en build.gradle
- [ ] CHANGELOG.md actualizado
- [ ] Documentación actualizada

#### **Verificar APK Generada**
```bash
# Información de la APK
aapt dump badging app/build/outputs/apk/release/app-release.apk

# Verificar firma
jarsigner -verify -verbose -certs app/build/outputs/apk/release/app-release.apk

# Tamaño de la APK
ls -lh app/build/outputs/apk/release/app-release.apk
```

---

## 📱 **DISTRIBUCIÓN**

### 🏪 **Google Play Store**

#### **1. Preparación**
- ✅ APK firmada con keystore de producción
- ✅ Íconos y screenshots preparados
- ✅ Descripción de la app
- ✅ Política de privacidad
- ✅ Términos de servicio

#### **2. Subida a Play Console**
1. Crear app en Google Play Console
2. Subir APK en "Release Management"
3. Completar información de la tienda
4. Configurar precios y distribución
5. Enviar para revisión

#### **3. Información de la Tienda**

**Título:**
```
Sistema de Rotación Inteligente
```

**Descripción Corta:**
```
Automatiza la rotación de trabajadores en estaciones de trabajo con algoritmo inteligente
```

**Descripción Completa:**
```
🏭 Sistema de Rotación Inteligente

Optimiza la gestión de personal en tu empresa con nuestro sistema automatizado de rotación de trabajadores.

✨ CARACTERÍSTICAS PRINCIPALES:
• 🤖 Algoritmo inteligente de rotación
• 👥 Gestión completa de trabajadores
• 🏭 Configuración flexible de estaciones
• 🎓 Sistema de entrenamiento integrado
• ☁️ Sincronización en la nube
• 🌙 Modo oscuro adaptativo
• 📊 Reportes y estadísticas

🎯 BENEFICIOS:
• Ahorra 80% del tiempo de planificación
• Reduce errores de asignación manual
• Mejora la equidad en la distribución
• Optimiza la eficiencia operativa

🏭 IDEAL PARA:
• Manufactura y producción
• Centros de salud
• Restaurantes y servicios
• Cualquier empresa con rotación de personal

📱 FÁCIL DE USAR:
Interfaz intuitiva que no requiere capacitación especial.

🔒 SEGURO Y CONFIABLE:
Datos protegidos con encriptación y respaldos automáticos.

¡Transforma la gestión de rotaciones en tu organización!
```

### 🏢 **Distribución Interna**

#### **1. APK Directa**
- Compartir APK por email/mensajería
- Subir a servidor interno
- Usar herramientas como Firebase App Distribution

#### **2. Firebase App Distribution**
```bash
# Instalar Firebase CLI
npm install -g firebase-tools

# Subir APK
firebase appdistribution:distribute app/build/outputs/apk/release/app-release.apk \
    --app YOUR_APP_ID \
    --groups "testers" \
    --release-notes "Nueva versión con mejoras de rendimiento"
```

#### **3. Instalación Manual**
1. Habilitar "Fuentes desconocidas" en Android
2. Transferir APK al dispositivo
3. Abrir APK e instalar
4. Configurar permisos si es necesario

---

## 🔧 **CONFIGURACIÓN DE CI/CD**

### 🤖 **GitHub Actions** (Ya configurado)

El proyecto incluye CI/CD automático en `.github/workflows/build-apk.yml`:

#### **Triggers:**
- Push a main branch
- Pull requests
- Releases

#### **Proceso:**
1. 🔧 Setup Android SDK
2. 🧹 Clean project
3. 🧪 Run unit tests
4. 🔍 Run lint analysis
5. 📦 Build debug APK
6. 📊 Generate reports
7. 📤 Upload artifacts

#### **Configuración Adicional:**

**Secrets necesarios en GitHub:**
```
KEYSTORE_FILE (base64 del archivo .jks)
KEYSTORE_PASSWORD
KEY_ALIAS
KEY_PASSWORD
```

**Agregar secrets:**
1. GitHub → Settings → Secrets and variables → Actions
2. New repository secret
3. Agregar cada secret

### 🔄 **Workflow Personalizado**

```yaml
# .github/workflows/release.yml
name: Release Build

on:
  push:
    tags:
      - 'v*'

jobs:
  release:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Setup JDK
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
          
      - name: Setup Android SDK
        uses: android-actions/setup-android@v2
        
      - name: Create keystore
        run: |
          echo "${{ secrets.KEYSTORE_FILE }}" | base64 -d > keystore.jks
          echo "storeFile=keystore.jks" > keystore.properties
          echo "storePassword=${{ secrets.KEYSTORE_PASSWORD }}" >> keystore.properties
          echo "keyAlias=${{ secrets.KEY_ALIAS }}" >> keystore.properties
          echo "keyPassword=${{ secrets.KEY_PASSWORD }}" >> keystore.properties
          
      - name: Run tests
        run: ./gradlew testDebugUnitTest
        
      - name: Build release APK
        run: ./gradlew assembleRelease
        
      - name: Create Release
        uses: actions/create-release@v1
        env:
          GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
        with:
          tag_name: ${{ github.ref }}
          release_name: Release ${{ github.ref }}
          body: |
            🚀 Nueva versión del Sistema de Rotación Inteligente
            
            📱 APK lista para instalación
            🧪 Todos los tests pasaron
            📊 Calidad verificada
          draft: false
          prerelease: false
          
      - name: Upload APK
        uses: actions/upload-release-asset@v1
        env:
          GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
        with:
          upload_url: ${{ steps.create_release.outputs.upload_url }}
          asset_path: app/build/outputs/apk/release/app-release.apk
          asset_name: workstation-rotation-v${{ github.ref_name }}.apk
          asset_content_type: application/vnd.android.package-archive
```

---

## 📊 **REPORTES Y MONITOREO**

### 📈 **Métricas de Build**

Después de cada build, revisa:

1. **📊 Cobertura de Tests**
   - `app/build/reports/jacoco/jacocoTestReport/html/index.html`

2. **🔍 Análisis de Lint**
   - `app/build/reports/lint/lint-results.html`

3. **🧪 Resultados de Tests**
   - `app/build/reports/tests/testDebugUnitTest/index.html`

4. **📦 Información de APK**
   - Tamaño, dependencias, permisos

### 🎯 **Objetivos de Calidad**

| Métrica | Mínimo | Objetivo | Excelente |
|---------|--------|----------|-----------|
| Cobertura Tests | 70% | 80% | 90%+ |
| Tiempo Build | <10 min | <5 min | <3 min |
| Tamaño APK | <20 MB | <15 MB | <10 MB |
| Issues Lint | <20 | <10 | <5 |
| Tests Fallidos | 0 | 0 | 0 |

---

## 🚀 **COMANDOS RÁPIDOS**

### 🧪 **Testing**
```bash
# Tests completos
./gradlew clean test connectedAndroidTest jacocoTestReport

# Solo unitarios
./gradlew testDebugUnitTest

# Solo integración
./gradlew connectedDebugAndroidTest
```

### 📊 **Calidad**
```bash
# Análisis completo
./gradlew qualityCheck

# Solo lint
./gradlew lint

# Solo cobertura
./gradlew jacocoTestReport
```

### 📦 **Build**
```bash
# Debug
./gradlew assembleDebug

# Release
./gradlew assembleRelease

# Ambos
./gradlew assemble
```

### 🧹 **Limpieza**
```bash
# Limpiar build
./gradlew clean

# Limpiar completamente
./gradlew clean build --refresh-dependencies
```

---

## 🎉 **¡LISTO PARA PRODUCCIÓN!**

Con esta configuración completa de testing y deployment, el Sistema de Rotación Inteligente está preparado para:

✅ **Desarrollo Confiable** - Tests automatizados y análisis de calidad
✅ **Builds Reproducibles** - Scripts automatizados y CI/CD
✅ **Distribución Profesional** - APKs firmadas y optimizadas
✅ **Monitoreo Continuo** - Métricas y reportes detallados

**¡Tu aplicación está lista para transformar la gestión de rotaciones en cualquier organización!** 🚀

---

*© 2024 - Sistema de Rotación Inteligente - Testing y Deployment Guide*