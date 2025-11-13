# ⚡ INSTRUCCIONES RÁPIDAS - Reparar Rotación

## 🎯 Tu Problema
Creaste 5 trabajadores pero solo aparecen 2 en la rotación.

## ✅ Solución en 3 Pasos

### Paso 1: Abrir Diagnóstico
Desde tu código, ejecuta:

```kotlin
startActivity(Intent(this, DiagnosticActivity::class.java))
```

O agrega un botón temporal en tu MainActivity:

```kotlin
// En onCreate() después de setContentView()
findViewById<Button>(R.id.btnTemp)?.setOnClickListener {
    startActivity(Intent(this, DiagnosticActivity::class.java))
}
```

### Paso 2: Reparar
1. La actividad se abrirá y mostrará el diagnóstico automáticamente
2. Verás cuántos trabajadores tienen problemas
3. Presiona el botón **"Reparar Sincronización"**
4. Espera a que termine (verás un mensaje de éxito)

### Paso 3: Verificar
1. Regresa a la pantalla de rotación
2. Presiona **"Generar Rotación"**
3. Ahora deberían aparecer los 5 trabajadores

## 🔧 Si No Tienes Botón para Abrir Diagnóstico

### Opción A: Desde Android Studio
1. Abre tu app en el emulador/dispositivo
2. En Android Studio, ve a: **Run > Edit Configurations**
3. En "Launch Options", selecciona "Specified Activity"
4. Escribe: `com.workstation.rotation.DiagnosticActivity`
5. Presiona Run

### Opción B: Agregar Botón Temporal
Agrega esto en tu `activity_main.xml`:

```xml
<Button
    android:id="@+id/btnDiagnostic"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="DIAGNÓSTICO"
    android:backgroundTint="@color/error" />
```

Y en tu `MainActivity.kt`:

```kotlin
binding.btnDiagnostic.setOnClickListener {
    startActivity(Intent(this, DiagnosticActivity::class.java))
}
```

## 📊 Qué Verás

### Antes de Reparar:
```
⚠️ Se detectaron problemas en 3 trabajadores

Trabajadores que DEBERÍAN aparecer en rotación: 2
  ✅ Maritza: 3 estaciones disponibles
  ✅ Oscar: 3 estaciones disponibles

⚠️ PROBLEMA DETECTADO:
  3 trabajadores activos NO aparecerán en rotación
```

### Después de Reparar:
```
✅ REPARACIÓN COMPLETADA
  • Capacidades creadas: 9
  • Capacidades reactivadas: 0

Trabajadores que DEBERÍAN aparecer en rotación: 5
  ✅ Maritza: 3 estaciones disponibles
  ✅ Oscar: 3 estaciones disponibles
  ✅ Trabajador3: 3 estaciones disponibles
  ✅ Trabajador4: 3 estaciones disponibles
  ✅ Trabajador5: 3 estaciones disponibles
```

## 🎉 Resultado Final

Después de reparar:
- ✅ Los 5 trabajadores aparecerán en la rotación
- ✅ El algoritmo de rotación funcionará correctamente
- ✅ Los trabajadores rotarán entre estaciones

## ⚠️ Si Aún No Funciona

1. Presiona **"Resetear Capacidades"** (elimina y recrea todo)
2. Verifica que los trabajadores estén **activos** (switch verde)
3. Verifica que las estaciones estén **activas**
4. Revisa los logs en Logcat con filtro: `NewRotationService`

## 📝 Nota Importante

Este problema ocurre cuando las capacidades (relación trabajador-estación) no se crean correctamente. La herramienta de diagnóstico las crea automáticamente basándose en las estaciones que asignaste a cada trabajador.

## 🔮 Prevenir en el Futuro

Siempre que crees trabajadores, verifica en los logs que aparezca:
```
✅ Sincronización verificada correctamente
```

Si no aparece, ejecuta el diagnóstico y repara.
