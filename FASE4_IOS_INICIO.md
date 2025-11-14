# 🍎 Fase 4: iOS - Guía de Implementación

## 🎯 Objetivo

Crear la aplicación iOS utilizando el framework compartido de Kotlin Multiplatform, permitiendo que la app funcione en iPhone y iPad.

## 📋 Requisitos

### Hardware/Software
- ✅ Mac con macOS 10.14 o superior
- ✅ Xcode 14.0 o superior
- ✅ CocoaPods o Swift Package Manager
- ✅ Cuenta de desarrollador de Apple (para dispositivos reales)

### Conocimientos
- Básico de Swift/SwiftUI
- Básico de Xcode
- Entendimiento de KMP

## 🏗️ Estructura del Proyecto iOS

```
iosApp/
├── iosApp/
│   ├── ContentView.swift          # Vista principal
│   ├── iosAppApp.swift            # Entry point
│   ├── Info.plist                 # Configuración
│   └── Assets.xcassets/           # Recursos
├── iosApp.xcodeproj/              # Proyecto Xcode
└── Podfile                        # Dependencias (si usas CocoaPods)
```

## 🔧 Pasos de Implementación

### Paso 1: Generar Framework iOS

El módulo `shared` ya está configurado para generar un framework iOS.

```bash
# Compilar framework para iOS
./gradlew :shared:linkDebugFrameworkIosArm64

# Para simulador
./gradlew :shared:linkDebugFrameworkIosX64
```

**Ubicación del framework:**
```
shared/build/bin/iosArm64/debugFramework/shared.framework
shared/build/bin/iosX64/debugFramework/shared.framework
```

### Paso 2: Crear Proyecto Xcode

1. **Abrir Xcode**
2. **File → New → Project**
3. **Seleccionar:** iOS → App
4. **Configurar:**
   - Product Name: `WorkstationRotation`
   - Team: Tu equipo
   - Organization Identifier: `com.workstation.rotation`
   - Interface: SwiftUI
   - Language: Swift
5. **Guardar en:** `iosApp/`

### Paso 3: Integrar Framework Shared

#### Opción A: Manual (Recomendado para desarrollo)

1. En Xcode, seleccionar el proyecto
2. Target → General → Frameworks, Libraries, and Embedded Content
3. Click en "+" → Add Other → Add Files
4. Navegar a `shared/build/bin/iosArm64/debugFramework/`
5. Seleccionar `shared.framework`
6. Marcar "Embed & Sign"

#### Opción B: CocoaPods

Crear `iosApp/Podfile`:

```ruby
platform :ios, '14.0'
use_frameworks!

target 'iosApp' do
    pod 'shared', :path => '../shared'
end
```

Ejecutar:
```bash
cd iosApp
pod install
```

### Paso 4: Configurar Build Phases

En Xcode:

1. Target → Build Phases
2. Click "+" → New Run Script Phase
3. Agregar script:

```bash
cd "$SRCROOT/.."
./gradlew :shared:embedAndSignAppleFrameworkForXcode
```

### Paso 5: Crear ContentView.swift

```swift
import SwiftUI
import shared

struct ContentView: View {
    @StateObject private var viewModel = WorkerViewModelWrapper()
    
    var body: some View {
        NavigationView {
            VStack {
                Text("Workstation Rotation")
                    .font(.largeTitle)
                    .padding()
                
                // Lista de trabajadores
                List(viewModel.workers, id: \.id) { worker in
                    HStack {
                        Text(worker.name)
                        Spacer()
                        Text(worker.code)
                            .foregroundColor(.gray)
                    }
                }
            }
            .navigationTitle("Trabajadores")
        }
    }
}

// Wrapper para ViewModel de KMP
class WorkerViewModelWrapper: ObservableObject {
    @Published var workers: [WorkerModel] = []
    
    private let viewModel: WorkerViewModel
    
    init() {
        // Inicializar ViewModel de KMP
        // TODO: Configurar repository y database
        self.viewModel = WorkerViewModel(repository: /* ... */)
        
        // Observar cambios
        // TODO: Implementar observación de Flow
    }
}
```

### Paso 6: Configurar Database para iOS

En `shared/src/iosMain/kotlin/`:

```kotlin
// DatabaseDriver.ios.kt
package com.workstation.rotation.database

import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.workstation.rotation.database.AppDatabase

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        return NativeSqliteDriver(
            AppDatabase.Schema,
            "workstation_rotation.db"
        )
    }
}
```

### Paso 7: Crear AppDelegate o App Entry Point

```swift
import SwiftUI
import shared

@main
struct WorkstationRotationApp: App {
    init() {
        // Inicializar KMP
        KotlinDependencies.shared.initialize()
    }
    
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}

// Helper para inicializar dependencias de Kotlin
class KotlinDependencies {
    static let shared = KotlinDependencies()
    
    private init() {}
    
    func initialize() {
        // Inicializar database, repository, etc.
    }
}
```

## 🎨 UI en iOS

### Opción 1: SwiftUI (Recomendado)

Usar SwiftUI para crear la UI nativa de iOS, consumiendo los ViewModels de KMP.

**Ventajas:**
- UI nativa de iOS
- Mejor rendimiento
- Acceso a todas las APIs de iOS

**Desventajas:**
- Código UI no compartido
- Más trabajo de desarrollo

### Opción 2: Compose Multiplatform para iOS (Experimental)

Usar Compose directamente en iOS.

**Ventajas:**
- UI compartida 100%
- Menos código

**Desventajas:**
- Experimental
- Posibles problemas de rendimiento
- Limitaciones de APIs

## 🔄 Flujo de Datos iOS

```
SwiftUI View
    ↓
ObservableObject Wrapper
    ↓
KMP ViewModel (Kotlin)
    ↓
Repository (Kotlin)
    ↓
SQLDelight Database (Kotlin)
```

## 🧪 Testing en iOS

### Simulador
```bash
# Compilar para simulador
./gradlew :shared:linkDebugFrameworkIosX64

# Abrir en Xcode y ejecutar (Cmd+R)
```

### Dispositivo Real
```bash
# Compilar para dispositivo
./gradlew :shared:linkDebugFrameworkIosArm64

# Conectar iPhone/iPad
# En Xcode: Product → Destination → Tu dispositivo
# Ejecutar (Cmd+R)
```

## 📱 Pantallas a Implementar

1. **MainView** - Menú principal
2. **WorkersView** - Lista de trabajadores
3. **WorkstationsView** - Lista de estaciones
4. **RotationView** - Generar rotación
5. **HistoryView** - Historial
6. **DetailView** - Detalles de rotación

## 🎯 Estado Actual

- ✅ Framework compartido listo
- ✅ Configuración de iOS en shared
- ✅ SQLDelight configurado para iOS
- ⏳ Proyecto Xcode pendiente
- ⏳ UI SwiftUI pendiente
- ⏳ Integración pendiente

## 📊 Progreso Estimado

```
Configuración inicial:    ████████░░░░░░░░░░░░  40%
UI SwiftUI:              ░░░░░░░░░░░░░░░░░░░░   0%
Integración KMP:         ░░░░░░░░░░░░░░░░░░░░   0%
Testing:                 ░░░░░░░░░░░░░░░░░░░░   0%
Publicación:             ░░░░░░░░░░░░░░░░░░░░   0%

Total Fase 4: 30%
```

## ⏱️ Tiempo Estimado

- **Configuración inicial:** 2-3 horas
- **UI SwiftUI (6 pantallas):** 8-12 horas
- **Integración KMP:** 4-6 horas
- **Testing:** 4-6 horas
- **Ajustes finales:** 2-4 horas

**Total:** 20-31 horas (~3-4 semanas part-time)

## 🚧 Limitaciones Conocidas

1. **Compose para iOS es experimental** - Mejor usar SwiftUI
2. **Flow de Kotlin** - Necesita wrapper para SwiftUI
3. **Coroutines** - Necesita adaptación para Swift
4. **Biometría** - Implementación específica de iOS

## 📝 Próximos Pasos

1. ✅ Documentación creada
2. ⏳ Crear proyecto Xcode
3. ⏳ Integrar framework shared
4. ⏳ Implementar MainView
5. ⏳ Implementar WorkersView
6. ⏳ Continuar con demás pantallas

## 🔗 Recursos Útiles

- [Kotlin Multiplatform for iOS](https://kotlinlang.org/docs/multiplatform-mobile-getting-started.html)
- [SQLDelight iOS Setup](https://cashapp.github.io/sqldelight/multiplatform_sqlite/)
- [SwiftUI Documentation](https://developer.apple.com/documentation/swiftui/)
- [Xcode Documentation](https://developer.apple.com/documentation/xcode)

---

**Estado:** 🚧 En progreso  
**Progreso Fase 4:** 30%  
**Requiere:** macOS + Xcode
