# Plan de Eliminación del Código Antiguo

## 📋 Resumen

Ahora tienes DOS versiones de la app:
- **Antigua** (v4.x): Carpeta `app/` - Solo Android
- **Nueva** (v5.0): Carpetas `shared/`, `androidApp/`, `desktopApp/` - Multiplataforma

## ⚠️ Recomendación: NO ELIMINAR TODAVÍA

Es mejor mantener ambas versiones hasta que la nueva esté 100% funcional.

## 🗂️ Archivos/Carpetas a Eliminar Eventualmente

### 1. Módulo Android Antiguo
```
app/                                    # TODO EL MÓDULO
├── src/
│   ├── main/
│   │   ├── java/com/workstation/rotation/
│   │   │   ├── MainActivity.kt         ❌
│   │   │   ├── WorkerActivity.kt       ❌
│   │   │   ├── WorkstationActivity.kt  ❌
│   │   │   ├── adapters/               ❌ (RecyclerView adapters)
│   │   │   ├── data/
│   │   │   │   ├── database/AppDatabase.kt  ❌ (Room)
│   │   │   │   └── dao/                ❌ (Room DAOs)
│   │   │   └── ...
│   │   └── res/
│   │       └── layout/                 ❌ (XML layouts)
│   └── test/
└── build.gradle                        ❌
```

### 2. Archivos de Configuración Antiguos
```
settings.gradle                         ❌ (reemplazado por settings.gradle.kts)
build.gradle                            ❌ (reemplazado por build.gradle.kts)
```

### 3. Documentación Antigua (Opcional)
```
ARCHITECTURE.md                         ⚠️ (mantener como referencia)
RELEASE_NOTES_v4.*.md                   ⚠️ (mantener como historial)
```

## ✅ Archivos a MANTENER

```
.git/                                   ✅ (historial)
.github/                                ✅ (CI/CD - actualizar)
gradle/                                 ✅
gradlew, gradlew.bat                    ✅
README.md                               ✅ (actualizar)
CHANGELOG.md                            ✅ (actualizar)
keystore.properties                     ✅
```

## 📅 Cronograma Sugerido

### Semana 1-2: Desarrollo
- ✅ Fase 1 completada
- ⏳ Implementar pantallas restantes
- ⏳ Testing básico

### Semana 3: Verificación
- ⏳ Probar todas las funciones en Android
- ⏳ Probar todas las funciones en Desktop
- ⏳ Comparar con versión antigua

### Semana 4: Transición
- ⏳ Migrar datos de usuarios (si es necesario)
- ⏳ Crear script de migración
- ⏳ Backup de código antiguo

### Semana 5: Eliminación
- ⏳ Eliminar carpeta `app/`
- ⏳ Eliminar archivos .gradle antiguos
- ⏳ Actualizar documentación
- ⏳ Commit final

## 🔄 Proceso de Eliminación Seguro

### Paso 1: Crear Backup
```bash
# Crear rama con código antiguo
git checkout -b backup/v4-android-only
git push origin backup/v4-android-only

# Volver a main
git checkout main
```

### Paso 2: Crear Tag de Versión Antigua
```bash
git tag -a v4.1.0-final -m "Última versión Android nativa antes de KMP"
git push origin v4.1.0-final
```

### Paso 3: Eliminar Archivos
```bash
# Eliminar módulo antiguo
git rm -r app/

# Eliminar configuración antigua
git rm settings.gradle
git rm build.gradle

# Renombrar nuevos archivos
git mv settings.gradle.kts settings.gradle.kts
git mv build.gradle.kts build.gradle.kts
git mv README_KMP.md README.md

# Commit
git commit -m "Migración completa a KMP v5.0.0 - Eliminado código Android antiguo"
git push
```

## 📊 Checklist Antes de Eliminar

Verificar que la nueva versión tiene:

### Funcionalidad
- [ ] Gestión de trabajadores
- [ ] Gestión de estaciones
- [ ] Generación de rotación
- [ ] Historial de rotaciones
- [ ] Sistema de seguridad (si lo necesitas)
- [ ] Backup/Restore
- [ ] Todas las funciones críticas

### Plataformas
- [ ] Android funciona correctamente
- [ ] Desktop funciona correctamente
- [ ] iOS (si es necesario)

### Testing
- [ ] Tests unitarios migrados
- [ ] Tests de integración funcionan
- [ ] Probado en dispositivos reales

### Documentación
- [ ] README actualizado
- [ ] Guías de usuario actualizadas
- [ ] Documentación técnica actualizada

### Datos
- [ ] Script de migración de datos creado
- [ ] Datos de prueba migrados exitosamente
- [ ] Backup de datos antiguos realizado

## 🎯 Estrategia Recomendada

### Opción A: Eliminación Gradual (RECOMENDADO)
1. Mantener ambas versiones 2-4 semanas
2. Usar nueva versión como principal
3. Mantener antigua como fallback
4. Eliminar cuando estés 100% seguro

### Opción B: Eliminación Inmediata
1. Solo si la nueva versión está 100% completa
2. Crear backup primero
3. Eliminar todo de una vez
4. Más riesgoso pero más limpio

### Opción C: Coexistencia Permanente
1. Mantener ambas versiones
2. Antigua para Android legacy
3. Nueva para todas las plataformas
4. Más complejo de mantener

## 💡 Recomendación Final

**NO ELIMINES NADA TODAVÍA**

Razones:
1. La nueva versión solo tiene 1 pantalla completa (Trabajadores)
2. Faltan funciones críticas (Rotación, Historial, etc.)
3. No se ha probado exhaustivamente
4. Los usuarios actuales dependen de la versión antigua

**Elimina cuando:**
- ✅ Todas las pantallas estén implementadas
- ✅ Todas las funciones críticas funcionen
- ✅ Se haya probado en producción 2-4 semanas
- ✅ Los usuarios estén satisfechos
- ✅ Tengas backup completo

## 📞 Siguiente Paso

Continuar con **Fase 2**: Implementar las pantallas restantes antes de pensar en eliminar código antiguo.
