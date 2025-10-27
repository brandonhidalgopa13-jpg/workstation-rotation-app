# 🎨 Guía de Personalización - Sistema de Rotación Inteligente

## 🎯 **Personaliza tu Aplicación**

Esta guía te ayudará a personalizar colores, funciones y comportamientos de la aplicación según tus necesidades específicas.

---

## 🌈 **PERSONALIZACIÓN DE COLORES**

### Ubicación de Archivos de Color
Los colores se definen en: `app/src/main/res/values/colors.xml`

### Colores Principales Personalizables

#### 🔵 Colores Primarios
```xml
<!-- Cambia estos valores para personalizar el tema principal -->
<color name="primary_blue">#FF1976D2</color>          <!-- Azul principal -->
<color name="primary_blue_dark">#FF0D47A1</color>     <!-- Azul oscuro -->
<color name="primary_blue_light">#FF42A5F5</color>    <!-- Azul claro -->
```

#### 🟠 Colores de Acento
```xml
<!-- Personaliza los colores de acento -->
<color name="accent_orange">#FFFF9800</color>         <!-- Naranja principal -->
<color name="accent_green">#FF4CAF50</color>          <!-- Verde de éxito -->
<color name="accent_red">#FFF44336</color>            <!-- Rojo de error -->
```

#### 🎨 Colores de Fondo
```xml
<!-- Personaliza los fondos -->
<color name="background_light">#FFFAFAFA</color>      <!-- Fondo claro -->
<color name="background_dark">#FF121212</color>       <!-- Fondo oscuro -->
<color name="background_card">#FFFFFFFF</color>       <!-- Fondo de tarjetas -->
```

### Paletas de Color Sugeridas

#### 🏢 Tema Corporativo (Azul/Gris)
```xml
<color name="primary_blue">#FF2E3B4E</color>
<color name="accent_orange">#FF4A90E2</color>
<color name="accent_green">#FF7ED321</color>
```

#### 🏭 Tema Industrial (Verde/Naranja)
```xml
<color name="primary_blue">#FF2E7D32</color>
<color name="accent_orange">#FFFF6F00</color>
<color name="accent_green">#FF388E3C</color>
```

#### 🌙 Tema Nocturno (Púrpura/Azul)
```xml
<color name="primary_blue">#FF3F51B5</color>
<color name="accent_orange">#FF9C27B0</color>
<color name="accent_green">#FF00BCD4</color>
```

#### 🔥 Tema Energético (Rojo/Naranja)
```xml
<color name="primary_blue">#FFD32F2F</color>
<color name="accent_orange">#FFFF5722</color>
<color name="accent_green">#FFFF9800</color>
```

---

## ⚙️ **PERSONALIZACIÓN DE FUNCIONES**

### Configurar Límites del Sistema

#### Archivo: `app/src/main/java/com/workstation/rotation/utils/Constants.kt`
```kotlin
object Constants {
    // Personaliza estos valores según tus necesidades
    
    // Límites de trabajadores por estación
    const val MIN_REQUIRED_WORKERS = 1        // Mínimo: 1-5
    const val MAX_REQUIRED_WORKERS = 20       // Máximo: 10-50
    const val DEFAULT_REQUIRED_WORKERS = 2    // Por defecto: 1-5
    
    // Límites de nombres
    const val MIN_NAME_LENGTH = 2             // Mínimo: 2-5
    const val MAX_NAME_LENGTH = 50            // Máximo: 30-100
    
    // Configuración de rotación
    const val MAX_ROTATIONS_SAME_STATION = 3  // Rotaciones antes de cambio forzado
    const val MIN_AVAILABILITY_PERCENTAGE = 10 // Disponibilidad mínima para rotar
    
    // Configuración de UI
    const val RECYCLER_VIEW_CACHE_SIZE = 20   // Cache de RecyclerView
    const val ANIMATION_DURATION = 300        // Duración de animaciones (ms)
}
```

### Personalizar Algoritmo de Rotación

#### Archivo: `app/src/main/java/com/workstation/rotation/viewmodels/RotationViewModel.kt`

**Cambiar Criterios de Disponibilidad:**
```kotlin
// Línea ~495: Función canWorkerRotate()
private fun canWorkerRotate(worker: Worker): Boolean {
    // PERSONALIZABLE: Cambia el umbral de disponibilidad
    if (worker.availabilityPercentage < 30) return false  // Cambiar 30 por tu valor
    
    // PERSONALIZABLE: Agregar más criterios
    // if (worker.hasSpecialSkill) return true
    // if (worker.isTemporary) return false
    
    return true
}
```

**Modificar Prioridades de Asignación:**
```kotlin
// Personalizar el orden de prioridades en executeRotationAlgorithm()
// 1. Cambiar orden de fases
// 2. Agregar nuevos criterios de selección
// 3. Modificar algoritmos de distribución
```

### Personalizar Validaciones

#### Archivo: `app/src/main/java/com/workstation/rotation/utils/ValidationUtils.kt`
```kotlin
object ValidationUtils {
    
    // PERSONALIZABLE: Validación de nombres
    fun isValidWorkerName(name: String): Boolean {
        return name.length >= MIN_NAME_LENGTH && 
               name.length <= MAX_NAME_LENGTH &&
               name.matches(Regex("[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+")) // Solo letras y espacios
    }
    
    // PERSONALIZABLE: Validación de email
    fun isValidEmail(email: String): Boolean {
        return if (email.isEmpty()) true // Email opcional
        else android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    
    // PERSONALIZABLE: Validación de disponibilidad
    fun isValidAvailability(percentage: Int): Boolean {
        return percentage in 0..100 // Cambiar rango si necesario
    }
}
```

---

## 🎭 **PERSONALIZACIÓN DE INTERFAZ**

### Modificar Textos y Mensajes

#### Archivo: `app/src/main/res/values/strings.xml`
```xml
<!-- Personaliza los textos de la aplicación -->
<string name="app_name">Tu Nombre de App</string>
<string name="workstations">Tus Estaciones</string>
<string name="workers">Tu Equipo</string>

<!-- Mensajes personalizados -->
<string name="rotation_generated">¡Rotación lista para tu empresa!</string>
<string name="no_eligible_workers">Configura tu equipo primero</string>
```

### Personalizar Estilos

#### Archivo: `app/src/main/res/values/styles.xml`
```xml
<!-- Personalizar botones -->
<style name="PrimaryButtonStyle" parent="Widget.Material3.Button">
    <item name="backgroundTint">@color/primary_blue</item>
    <item name="cornerRadius">16dp</item>              <!-- Cambiar redondez -->
    <item name="android:textSize">18sp</item>          <!-- Cambiar tamaño texto -->
    <item name="android:textStyle">bold</item>         <!-- Cambiar estilo -->
</style>

<!-- Personalizar tarjetas -->
<style name="CardStyle" parent="Widget.Material3.CardView.Elevated">
    <item name="cardCornerRadius">20dp</item>          <!-- Más redondeadas -->
    <item name="cardElevation">12dp</item>             <!-- Más sombra -->
    <item name="android:layout_margin">16dp</item>     <!-- Más espacio -->
</style>
```

### Personalizar Iconos

#### Cambiar Iconos de la App
1. Reemplaza los archivos en `app/src/main/res/mipmap-*/`
2. Usa Android Asset Studio para generar iconos
3. Mantén los nombres de archivo existentes

#### Personalizar Iconos de Funciones
- Ubicación: `app/src/main/res/drawable/`
- Modifica los archivos `.xml` existentes
- O reemplaza con tus propios iconos SVG

---

## 🔧 **CONFIGURACIONES AVANZADAS**

### Personalizar Base de Datos

#### Archivo: `app/src/main/java/com/workstation/rotation/data/database/AppDatabase.kt`
```kotlin
@Database(
    entities = [Worker::class, Workstation::class, WorkerWorkstation::class],
    version = 2, // PERSONALIZABLE: Incrementar para cambios de esquema
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    
    companion object {
        // PERSONALIZABLE: Cambiar nombre de base de datos
        private const val DATABASE_NAME = "tu_empresa_rotation_database"
    }
}
```

### Configurar Respaldos Automáticos

#### Archivo: `app/src/main/java/com/workstation/rotation/data/sync/BackupManager.kt`
```kotlin
class BackupManager(private val context: Context) {
    
    companion object {
        // PERSONALIZABLE: Configurar respaldos
        private const val BACKUP_FOLDER = "TuEmpresa_Respaldos"
        private const val MAX_BACKUP_FILES = 10  // Máximo archivos a mantener
        private const val AUTO_BACKUP_INTERVAL = 7 // Días entre respaldos automáticos
    }
}
```

### Personalizar Notificaciones

#### Agregar en `app/src/main/java/com/workstation/rotation/utils/NotificationUtils.kt`
```kotlin
object NotificationUtils {
    
    // PERSONALIZABLE: Configurar notificaciones
    fun showRotationReminder(context: Context) {
        // Notificación para recordar generar rotación
    }
    
    fun showBackupReminder(context: Context) {
        // Notificación para recordar crear respaldo
    }
    
    fun showTrainingComplete(context: Context, workerName: String) {
        // Notificación cuando se completa entrenamiento
    }
}
```

---

## 🎨 **TEMAS PERSONALIZADOS COMPLETOS**

### Crear Tema Personalizado

#### 1. Definir Colores Personalizados
```xml
<!-- En colors.xml -->
<color name="mi_empresa_primary">#FF1A237E</color>
<color name="mi_empresa_secondary">#FFFF6F00</color>
<color name="mi_empresa_accent">#FF00C853</color>
```

#### 2. Crear Tema Personalizado
```xml
<!-- En themes.xml -->
<style name="Theme.MiEmpresa" parent="Theme.Material3.DayNight">
    <item name="colorPrimary">@color/mi_empresa_primary</item>
    <item name="colorSecondary">@color/mi_empresa_secondary</item>
    <item name="colorAccent">@color/mi_empresa_accent</item>
</style>
```

#### 3. Aplicar Tema
```xml
<!-- En AndroidManifest.xml -->
<application
    android:theme="@style/Theme.MiEmpresa">
```

---

## 📱 **PERSONALIZACIÓN POR TIPO DE EMPRESA**

### 🏭 Manufactura/Fábrica
```xml
<color name="primary_blue">#FF1B5E20</color>      <!-- Verde industrial -->
<color name="accent_orange">#FFFF6F00</color>     <!-- Naranja seguridad -->
<color name="accent_green">#FF2E7D32</color>      <!-- Verde éxito -->
```

### 🏥 Sector Salud
```xml
<color name="primary_blue">#FF0D47A1</color>      <!-- Azul médico -->
<color name="accent_orange">#FF00ACC1</color>     <!-- Cyan salud -->
<color name="accent_green">#FF43A047</color>      <!-- Verde vida -->
```

### 🏢 Oficinas/Corporativo
```xml
<color name="primary_blue">#FF37474F</color>      <!-- Gris corporativo -->
<color name="accent_orange">#FF5E35B1</color>     <!-- Púrpura elegante -->
<color name="accent_green">#FF00897B</color>      <!-- Verde teal -->
```

### 🍽️ Restaurantes/Servicios
```xml
<color name="primary_blue">#FFD84315</color>      <!-- Naranja cálido -->
<color name="accent_orange">#FFFF5722</color>     <!-- Rojo fuego -->
<color name="accent_green">#FF689F38</color>      <!-- Verde natural -->
```

---

## 🔄 **APLICAR PERSONALIZACIONES**

### Pasos para Aplicar Cambios

1. **Editar Archivos**: Modifica los archivos según tus necesidades
2. **Limpiar Proyecto**: `./gradlew clean`
3. **Recompilar**: `./gradlew assembleDebug`
4. **Probar**: Instala y prueba los cambios
5. **Iterar**: Ajusta según los resultados

### Herramientas Recomendadas

#### Generadores de Color
- **Material Design Color Tool**: material.io/design/color
- **Coolors.co**: Paletas de colores automáticas
- **Adobe Color**: color.adobe.com

#### Editores de Iconos
- **Android Asset Studio**: romannurik.github.io/AndroidAssetStudio
- **Material Design Icons**: material.io/design/iconography
- **Flaticon**: flaticon.com

### Backup Antes de Personalizar
```bash
# Crear respaldo de archivos originales
cp app/src/main/res/values/colors.xml colors_original.xml
cp app/src/main/res/values/themes.xml themes_original.xml
cp app/src/main/res/values/styles.xml styles_original.xml
```

---

## 🎯 **CONSEJOS DE PERSONALIZACIÓN**

### Mejores Prácticas
1. **Mantén Consistencia**: Usa la misma paleta en toda la app
2. **Prueba Accesibilidad**: Asegúrate de que los colores tengan buen contraste
3. **Considera el Contexto**: Colores apropiados para tu industria
4. **Documenta Cambios**: Mantén registro de personalizaciones

### Evitar Problemas Comunes
- **No cambies nombres de archivos** sin actualizar referencias
- **Mantén valores por defecto** para casos no contemplados
- **Prueba en modo oscuro** si lo usas
- **Verifica en diferentes tamaños** de pantalla

### Personalización Gradual
1. **Fase 1**: Solo colores principales
2. **Fase 2**: Textos y mensajes
3. **Fase 3**: Iconos y estilos
4. **Fase 4**: Funcionalidades avanzadas

---

¡Con esta guía puedes personalizar completamente la aplicación según las necesidades específicas de tu empresa u organización!

---

*© 2024 - Sistema de Rotación Inteligente - Guía de Personalización*