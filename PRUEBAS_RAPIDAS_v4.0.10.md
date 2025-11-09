# ⚡ Pruebas Rápidas - v4.0.10

## 🎯 Checklist de Pruebas (5 minutos)

### ✅ Prueba 1: Scroll Horizontal (30 segundos)
```
1. Abrir pantalla de rotación
2. Deslizar dedo de derecha a izquierda
3. Verificar:
   ☐ Se mueven las estaciones
   ☐ Scrollbar horizontal visible
   ☐ Efecto de rebote al final
```

### ✅ Prueba 2: Scroll Vertical (30 segundos)
```
1. Buscar una estación con varios trabajadores
2. Deslizar dedo de abajo hacia arriba dentro de la columna
3. Verificar:
   ☐ Se mueven los trabajadores
   ☐ Scrollbar vertical visible
   ☐ No afecta el scroll horizontal
```

### ✅ Prueba 3: Scroll Bidireccional (30 segundos)
```
1. Deslizar horizontalmente
2. Luego deslizar verticalmente
3. Verificar:
   ☐ Ambos scrolls funcionan
   ☐ No hay conflictos
   ☐ Experiencia fluida
```

### ✅ Prueba 4: Captura Simple (1 minuto)
```
1. Presionar botón "Capturar" 📸
2. Esperar loading
3. Presionar "Ver" en el mensaje
4. Verificar en la imagen:
   ☐ Título: "Sistema de Rotación - Vista Completa"
   ☐ Fecha y hora
   ☐ "ROTACIÓN 1 - ACTUAL"
   ☐ "ROTACIÓN 2 - SIGUIENTE"
   ☐ Todas las estaciones visibles
```

### ✅ Prueba 5: Captura con Scroll (1 minuto)
```
1. Deslizar para ver estaciones ocultas
2. Presionar "Capturar" 📸
3. Verificar en la imagen:
   ☐ Estaciones que estaban ocultas aparecen
   ☐ Imagen más ancha que la pantalla
   ☐ Todo el contenido capturado
```

### ✅ Prueba 6: Compartir (1 minuto)
```
1. Capturar foto
2. Esperar 2 segundos
3. Verificar:
   ☐ Aparece diálogo "¿Compartir?"
   ☐ Presionar "Compartir"
   ☐ Se abre selector de apps
   ☐ Funciona compartir por WhatsApp/Email
```

---

## 🐛 Problemas Comunes y Soluciones

### Problema: "No se ve el scroll vertical"
**Solución:** Asegúrate de que la estación tenga más de 3-4 trabajadores

### Problema: "La imagen no captura todo"
**Solución:** Espera a que termine el loading completamente

### Problema: "No aparece el diálogo de compartir"
**Solución:** Espera 2 segundos después de capturar

### Problema: "El scroll horizontal no funciona"
**Solución:** Asegúrate de tener más de 2-3 estaciones

---

## ✅ Resultado Esperado

Si todas las pruebas pasan:
- ✅ Scroll bidireccional funciona perfectamente
- ✅ Captura incluye ambas rotaciones completas
- ✅ Todas las estaciones y trabajadores están en la imagen
- ✅ Se puede compartir fácilmente

---

## 📸 Ejemplo de Imagen Capturada

```
┌─────────────────────────────────────────────┐
│ Sistema de Rotación - Vista Completa        │
│ Fecha: 09/01/2025 14:30                     │
├─────────────────────────────────────────────┤
│ ROTACIÓN 1 - ACTUAL                         │
│ [Todas las estaciones con sus trabajadores] │
├─────────────────────────────────────────────┤
│ ROTACIÓN 2 - SIGUIENTE                      │
│ [Todas las estaciones con sus trabajadores] │
└─────────────────────────────────────────────┘
```

---

## 🚀 Comandos Útiles

### Compilar:
```bash
./gradlew assembleDebug
```

### Instalar en dispositivo:
```bash
./gradlew installDebug
```

### Ver logs:
```bash
adb logcat | grep "NewRotationActivity"
```

---

**Tiempo Total de Pruebas:** ~5 minutos
**Estado:** ✅ Listo para Probar
