# Workstation Rotation - iOS App

## 📱 Requisitos

- macOS 10.14 o superior
- Xcode 14.0 o superior
- CocoaPods instalado
- Cuenta de desarrollador de Apple (para dispositivos reales)

## 🚀 Configuración Inicial

### 1. Instalar CocoaPods (si no está instalado)

```bash
sudo gem install cocoapods
```

### 2. Compilar Framework Shared

Desde la raíz del proyecto:

```bash
# Para dispositivo físico (iPhone/iPad)
./gradlew :shared:linkDebugFrameworkIosArm64

# Para simulador
./gradlew :shared:linkDebugFrameworkIosX64
```

### 3. Instalar Dependencias

```bash
cd iosApp
pod install
```

### 4. Abrir Proyecto en Xcode

```bash
open iosApp.xcworkspace
```

**Nota:** Siempre abrir el archivo `.xcworkspace`, NO el `.xcodeproj`

## 🏗️ Estructura del Proyecto

```
iosApp/
├── iosApp/
│   ├── ContentView.swift          # Vista principal
│   ├── iosAppApp.swift            # Entry point
│   ├── Views/                     # Vistas SwiftUI
│   │   ├── MainView.swift
│   │   ├── WorkersView.swift
│   │   ├── WorkstationsView.swift
│   │   ├── RotationView.swift
│   │   └── HistoryView.swift
│   ├── ViewModels/                # Wrappers de ViewModels KMP
│   │   ├── WorkerViewModelWrapper.swift
│   │   └── ...
│   └── Utils/                     # Utilidades
│       └── KotlinDependencies.swift
├── iosApp.xcodeproj/              # Proyecto Xcode
├── iosApp.xcworkspace/            # Workspace (usar este)
├── Podfile                        # Dependencias
└── README.md                      # Este archivo
```

## 🔧 Compilación

### Simulador

1. En Xcode: Product → Destination → iPhone Simulator
2. Cmd+R para ejecutar

### Dispositivo Real

1. Conectar iPhone/iPad
2. En Xcode: Product → Destination → Tu dispositivo
3. Cmd+R para ejecutar

## 🧪 Testing

```bash
# Tests unitarios
# En Xcode: Cmd+U

# O desde terminal
xcodebuild test -workspace iosApp.xcworkspace -scheme iosApp -destination 'platform=iOS Simulator,name=iPhone 14'
```

## 📦 Distribución

### TestFlight

1. Archive: Product → Archive
2. Distribute App → App Store Connect
3. Subir a TestFlight

### App Store

1. Preparar metadata en App Store Connect
2. Archive y distribuir
3. Enviar para revisión

## 🐛 Problemas Comunes

### Error: "Framework not found shared"

**Solución:**
```bash
cd ..
./gradlew :shared:linkDebugFrameworkIosArm64
cd iosApp
pod install
```

### Error: "Module 'shared' not found"

**Solución:**
1. Limpiar build: Cmd+Shift+K
2. Recompilar: Cmd+B

### Error de firma de código

**Solución:**
1. Xcode → Preferences → Accounts
2. Agregar tu Apple ID
3. En proyecto: Signing & Capabilities → Team

## 📝 Notas

- El framework `shared` contiene toda la lógica de negocio
- La UI se implementa en SwiftUI (nativa de iOS)
- Los ViewModels de Kotlin se envuelven en ObservableObject
- La base de datos SQLDelight funciona nativamente en iOS

## 🔗 Enlaces Útiles

- [Documentación KMP](https://kotlinlang.org/docs/multiplatform.html)
- [SwiftUI Tutorials](https://developer.apple.com/tutorials/swiftui)
- [CocoaPods](https://cocoapods.org/)

---

**Estado:** 🚧 En desarrollo  
**Versión:** 5.0.0-rc  
**Requiere:** macOS + Xcode
