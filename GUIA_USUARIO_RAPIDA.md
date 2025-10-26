# 📱 GUÍA RÁPIDA DEL USUARIO - Sistema de Rotación Inteligente

## 🚀 INICIO RÁPIDO

### 1. Primera Vez Usando la App
- Al abrir la aplicación, se iniciará automáticamente el **Tutorial Interactivo** 📚
- Sigue los pasos para aprender todas las funciones
- Puedes saltar el tutorial y activarlo después desde el menú principal

### 2. Navegación Básica 🧭
**Menú Principal** tiene 4 opciones principales:
- **🏭 Estaciones de Trabajo**: Crear y gestionar estaciones
- **👥 Trabajadores**: Registrar y administrar personal
- **🔄 Sistema de Rotación**: Generar y ver rotaciones
- **⚙️ Configuraciones**: Ajustes y herramientas adicionales

**Navegación**: Usa la **flecha ←** o **botón "Atrás"** para regresar al menú principal desde cualquier pantalla.

### 3. Configuración Básica (5 minutos)

#### Paso 1: Crear Estaciones de Trabajo 🏭
1. Toca **"🏭 Estaciones de Trabajo"**
2. Presiona el botón **"+"** (agregar)
3. Completa la información:
   - **Nombre**: Ej. "Línea de Producción A"
   - **Trabajadores Necesarios**: Ej. 3
   - **Estación Prioritaria**: ✓ si es crítica
4. Toca **"Guardar"** para crear la estación
5. **Repite los pasos 2-4** para crear 3-5 estaciones
6. Una vez creadas todas las estaciones, **regresa al menú principal** tocando la flecha ← o el botón "Atrás"

#### Paso 2: Registrar Trabajadores 👥
1. **Desde el menú principal**, toca **"👥 Trabajadores"**
2. Presiona el botón **"+"** (agregar)
3. Completa la información:
   - **Nombre y Email**: Información básica del trabajador
   - **Disponibilidad**: 100% = siempre disponible, 80% = disponibilidad reducida
   - **Rol de Entrenamiento**: 
     - **Entrenador** 👨‍🏫: Puede capacitar a otros
     - **En Entrenamiento** 🎯: Está aprendiendo (selecciona entrenador y estación)
   - **Estaciones**: ✅ Marca las estaciones donde puede trabajar
4. Toca **"Guardar"** para crear el trabajador
5. **Repite los pasos 2-4** para crear 5-10 trabajadores
6. **Regresa al menú principal** tocando la flecha ← o el botón "Atrás"

#### Paso 3: Generar Primera Rotación 🔄
1. **Desde el menú principal**, toca **"🔄 Sistema de Rotación"**
2. Presiona **"🔄 Generar Rotación"**
3. **¡Listo!** El sistema asigna automáticamente a todos los trabajadores
4. **Revisa la tabla de rotación** para ver las asignaciones actuales y próximas
5. **Usa "🗑️ Limpiar"** si quieres generar una nueva rotación

> **💡 Consejo**: Si no ves trabajadores en la rotación, verifica que:
> - Tengas trabajadores activos creados
> - Los trabajadores tengan estaciones asignadas
> - Las estaciones estén activas

### 🎯 Próximos Pasos
Una vez completada la configuración básica:

1. **🔄 Genera varias rotaciones** para ver cómo funciona el algoritmo
2. **👥 Experimenta con diferentes disponibilidades** (80%, 90%, 100%)
3. **🎓 Configura parejas de entrenamiento** si tienes personal nuevo
4. **⭐ Marca estaciones prioritarias** para áreas críticas
5. **🏆 Certifica trabajadores** cuando completen su entrenamiento
6. **📊 Observa los patrones** de rotación para optimizar tu operación

---

## 🎓 SISTEMA DE ENTRENAMIENTO

### Configurar Entrenamiento
1. Al crear/editar un trabajador, marca **"Está en Entrenamiento"** ✓
2. Selecciona su **entrenador** de la lista
3. Elige la **estación de entrenamiento**
4. El sistema los mantendrá **siempre juntos** 🤝

### Certificar Trabajadores (Completar Entrenamiento)
1. Ve a **"👥 Trabajadores"** desde el menú principal
2. **Toca el trabajador** que completó su entrenamiento
3. En el diálogo de edición, verás la sección **"🏆 Sistema de Certificación"**
4. Marca **"🏆 Certificar Trabajador"** ✓
5. **Automáticamente se activará** la estación donde se entrenó
6. Toca **"Guardar"** para completar la certificación
7. ¡Ya es un trabajador certificado! 🎉

> **📝 Nota**: La opción de certificación solo aparece para trabajadores que estuvieron en entrenamiento.

---

## 🔄 GENERANDO ROTACIONES

### Cómo Funciona el Algoritmo
El sistema asigna automáticamente considerando:
1. **Parejas de Entrenamiento** (prioridad máxima) 🤝
2. **Estaciones Prioritarias** (siempre completas) ⭐
3. **Disponibilidad de Trabajadores** (porcentajes) 📊
4. **Rotación Forzada** (trabajadores entrenados rotan cada 2 ciclos) 🔄

### Interpretar Resultados
- **👨‍🏫🤝 [ENTRENANDO]**: Entrenador con su entrenado
- **🎯🤝 [EN ENTRENAMIENTO]**: Entrenado con su entrenador  
- **⭐ COMPLETA**: Estación prioritaria con capacidad completa
- **🔒**: Trabajador con restricciones
- **⚠️**: Baja disponibilidad

---

## 💡 CONSEJOS PRÁCTICOS

### Para Mejores Resultados:
- ✅ Crea **3-5 estaciones** variadas
- ✅ Registra **5-10 trabajadores** mínimo
- ✅ Varía las **disponibilidades** (80%, 90%, 100%)
- ✅ Usa **estaciones prioritarias** para áreas críticas
- ✅ Configura **parejas de entrenamiento** cuando sea necesario
- ✅ **Certifica trabajadores** cuando completen su entrenamiento

### Solución de Problemas Comunes:

#### 🚫 "No se generan rotaciones"
- ✅ Verifica que tengas **trabajadores activos** creados
- ✅ Confirma que las **estaciones estén activas**
- ✅ Asegúrate de que los **trabajadores tengan estaciones asignadas**

#### 🏭 "Estaciones aparecen vacías"
- ✅ Revisa que los trabajadores tengan **esas estaciones marcadas** ✓
- ✅ Verifica la **disponibilidad** de los trabajadores (debe ser > 0%)
- ✅ Confirma que no todos estén **en entrenamiento** en otras estaciones

#### 👥 "Parejas de entrenamiento separadas"
- ✅ Verifica que la **configuración de entrenamiento** esté correcta
- ✅ Confirma que el **entrenador esté activo** y disponible
- ✅ Revisa que la **estación de entrenamiento** esté activa

#### 🔄 "La app se queda en una pantalla"
- ✅ Usa la **flecha ←** para regresar al menú principal
- ✅ Si no responde, **cierra y abre** la aplicación
- ✅ Verifica que hayas **guardado** los cambios antes de navegar

---

## 🎯 FLUJO DE TRABAJO DIARIO

### Rutina Recomendada:
1. **Revisar Personal** 👥
   - Verificar disponibilidades del día
   - Certificar trabajadores que completaron entrenamiento
   - Actualizar restricciones si hay cambios

2. **Generar Rotación** 🔄
   - Clic en "Generar Rotación"
   - Revisar asignaciones automáticas
   - Verificar que estaciones prioritarias estén completas

3. **Implementar** 📋
   - Comunicar asignaciones al personal
   - Supervisar transiciones
   - Preparar siguiente rotación

---

## 📚 AYUDA ADICIONAL

### Acceder al Tutorial:
- **Menú Principal** → **⋮** → **"📚 Iniciar Tutorial"**
- **Configurar Tutorial** → **⋮** → **"⚙️ Configurar Tutorial"**

### Funciones del Tutorial:
- **Reiniciar**: Volver a ver toda la guía
- **Activar/Desactivar**: Controlar si se muestra automáticamente
- **Pistas**: Ayuda contextual durante el uso normal

---

## 🏆 ¡LISTO PARA USAR!

Con esta configuración básica ya puedes:
- ✅ Generar rotaciones automáticas
- ✅ Manejar entrenamientos
- ✅ Optimizar distribución de personal
- ✅ Certificar trabajadores completados
- ✅ Mantener estaciones prioritarias operativas

**¡El sistema hace todo el trabajo pesado por ti!** 🚀

---

*Desarrollado por Brandon Josué Hidalgo Paz - Sistema de Rotación Inteligente v2.0.0*