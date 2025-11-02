# 📊 SISTEMA DE REPORTES Y MÉTRICAS CON SQL

## 🎯 Objetivo del Sistema

El Sistema de Reportes y Métricas proporciona análisis detallados de cada trabajador, incluyendo **porcentajes de permanencia por estación** calculados mediante consultas SQL optimizadas.

## 🏗️ Arquitectura del Sistema

### 📁 Componentes Principales

#### 1. **ReportsDao.kt** - Consultas SQL Optimizadas
```sql
-- Ejemplo: Métricas de permanencia por estación
SELECT 
    ws.id as stationId,
    ws.name as stationName,
    ws.requiredWorkers,
    ws.isPriority,
    CASE WHEN w.currentWorkstationId = ws.id 
         THEN w.rotationsInCurrentStation 
         ELSE 0 END as rotationsInStation
FROM workstations ws
CROSS JOIN workers w
WHERE w.id = :workerId AND w.isActive = 1 AND ws.isActive = 1
```

#### 2. **ReportsService.kt** - Lógica de Negocio
- Procesamiento de datos SQL
- Cálculo de porcentajes de permanencia
- Generación de análisis y recomendaciones
- Exportación de reportes

#### 3. **ReportsActivity.kt** - Interfaz de Usuario
- Visualización de métricas generales
- Lista de trabajadores con resúmenes
- Reportes detallados individuales
- Exportación y compartir reportes

## 📊 Métricas Calculadas

### 🎯 **Métricas Individuales por Trabajador**

#### Información Básica
- **Nombre y Email**: Identificación del trabajador
- **Tipo de Trabajador**: LÍDER, ENTRENADOR, ENTRENADO, REGULAR
- **Disponibilidad**: Porcentaje de disponibilidad laboral
- **Estaciones Asignadas**: Número total de estaciones donde puede trabajar

#### Porcentaje de Permanencia por Estación
```kotlin
val percentage = if (totalRotations > 0) {
    (rotationsInStation.toDouble() / totalRotations) * 100
} else {
    // Distribución equitativa si no hay historial
    if (canWork) 100.0 / eligibleStations else 0.0
}
```

#### Análisis de Rendimiento
- **Puntaje de Versatilidad**: Basado en estaciones asignadas
- **Puntaje de Disponibilidad**: Porcentaje de disponibilidad
- **Puntaje General**: Combinación de métricas con bonificaciones por roles
- **Nivel de Rendimiento**: EXCELENTE, BUENO, REGULAR, NECESITA MEJORA

### 📈 **Métricas del Sistema**

#### Estadísticas Generales
- **Total de Trabajadores Activos**
- **Utilización del Sistema**: Porcentaje de capacidad utilizada
- **Disponibilidad Promedio**: Media de disponibilidad de todos los trabajadores
- **Cobertura**: Porcentaje de trabajadores con estaciones asignadas

#### Distribución de Roles
- **Líderes**: Porcentaje de trabajadores con rol de liderazgo
- **Entrenadores**: Porcentaje de trabajadores entrenadores
- **Entrenados**: Porcentaje de trabajadores en entrenamiento
- **Con Restricciones**: Porcentaje con limitaciones específicas

#### Utilización por Estación
- **Trabajadores Asignados vs Requeridos**
- **Porcentaje de Utilización por Estación**
- **Identificación de Estaciones Prioritarias**

## 🔍 Consultas SQL Principales

### 1. **Métricas de Trabajador Individual**
```sql
SELECT 
    w.id, w.name, w.email, w.availabilityPercentage,
    w.isLeader, w.isTrainer, w.isTrainee,
    w.rotationsInCurrentStation,
    COUNT(DISTINCT ww.workstationId) as totalStationsAssigned,
    GROUP_CONCAT(DISTINCT ws.name) as stationNames
FROM workers w
LEFT JOIN worker_workstations ww ON w.id = ww.workerId
LEFT JOIN workstations ws ON ww.workstationId = ws.id AND ws.isActive = 1
WHERE w.id = :workerId AND w.isActive = 1
GROUP BY w.id
```

### 2. **Permanencia por Estación**
```sql
SELECT 
    ws.id as stationId,
    ws.name as stationName,
    ws.requiredWorkers,
    ws.isPriority,
    CASE WHEN EXISTS (
        SELECT 1 FROM worker_workstations ww 
        WHERE ww.workerId = :workerId AND ww.workstationId = ws.id
    ) THEN 1 ELSE 0 END as canWork,
    CASE WHEN w.currentWorkstationId = ws.id 
         THEN w.rotationsInCurrentStation 
         ELSE 0 END as rotationsInStation
FROM workstations ws
CROSS JOIN workers w
WHERE w.id = :workerId AND w.isActive = 1 AND ws.isActive = 1
```

### 3. **Utilización por Estación**
```sql
SELECT 
    ws.id as stationId,
    ws.name as stationName,
    ws.requiredWorkers,
    COUNT(DISTINCT ww.workerId) as assignedWorkers,
    ROUND((COUNT(DISTINCT ww.workerId) * 100.0) / NULLIF(ws.requiredWorkers, 0), 2) as utilizationPercentage
FROM workstations ws
LEFT JOIN worker_workstations ww ON ws.id = ww.workstationId
LEFT JOIN workers w ON ww.workerId = w.id AND w.isActive = 1
WHERE ws.isActive = 1
GROUP BY ws.id
```

### 4. **Distribución de Roles**
```sql
SELECT 
    'LÍDERES' as category,
    COUNT(*) as count,
    ROUND(COUNT(*) * 100.0 / (SELECT COUNT(*) FROM workers WHERE isActive = 1), 2) as percentage
FROM workers 
WHERE isActive = 1 AND isLeader = 1

UNION ALL

SELECT 
    'ENTRENADORES' as category,
    COUNT(*) as count,
    ROUND(COUNT(*) * 100.0 / (SELECT COUNT(*) FROM workers WHERE isActive = 1), 2) as percentage
FROM workers 
WHERE isActive = 1 AND isTrainer = 1
```

## 🎨 Interfaz de Usuario

### 📱 **Pantalla Principal de Reportes**

#### Métricas del Sistema (Cards Superiores)
- **Trabajadores Totales**: Número con icono 👥
- **Utilización del Sistema**: Porcentaje con indicador visual
- **Disponibilidad Promedio**: Porcentaje con código de colores
- **Cobertura**: Porcentaje de trabajadores asignados

#### Distribución de Roles
- **Líderes**: Porcentaje con icono 👑
- **Entrenadores**: Porcentaje con icono 🎓
- **Entrenados**: Porcentaje con icono 📚
- **Con Restricciones**: Porcentaje con icono ⚠️

#### Lista de Trabajadores
- **Card por Trabajador** con métricas resumidas
- **Indicadores Visuales** de disponibilidad y utilización
- **Tap para Ver Detalle** - Reporte completo individual

### 📊 **Reporte Detallado Individual**

#### Información Básica
- Nombre, email y tipo de trabajador
- Disponibilidad y estaciones asignadas
- Rotaciones actuales y restricciones

#### Permanencia por Estación
```
Estación A ⭐: 45.2% (12 rotaciones)
Estación B 👑: 30.1% (8 rotaciones)  
Estación C 🎓: 24.7% (6 rotaciones)
```

#### Análisis de Rendimiento
- **Nivel**: EXCELENTE / BUENO / REGULAR / NECESITA MEJORA
- **Puntajes**: General, Versatilidad, Disponibilidad
- **Roles Especiales**: Liderazgo, Entrenamiento

#### Recomendaciones Personalizadas
- Sugerencias basadas en métricas
- Oportunidades de mejora
- Reconocimientos por buen rendimiento

## 🚀 Funcionalidades Avanzadas

### 📤 **Exportación de Reportes**
- **Reporte Individual**: Texto formateado completo
- **Reporte General**: Resumen del sistema
- **Compartir**: Via Intent de Android (email, WhatsApp, etc.)

### 🔄 **Actualización en Tiempo Real**
- **Botón Actualizar**: Recarga datos desde base de datos
- **Generación Automática**: Cálculos dinámicos con SQL
- **Fecha de Generación**: Timestamp visible

### 📊 **Indicadores Visuales**
- **Códigos de Color**: Verde (bueno), Amarillo (regular), Rojo (necesita atención)
- **Iconos Descriptivos**: Roles y estados claramente identificados
- **Badges**: Tipos de trabajadores con colores distintivos

## 🔧 Configuración y Uso

### 📍 **Acceso al Sistema**
1. **Configuraciones** → **📊 Generar Reportes**
2. **Pantalla Principal** → **Reportes y Métricas**

### 📋 **Flujo de Uso**
1. **Cargar Datos**: Sistema carga automáticamente al abrir
2. **Ver Resumen**: Métricas generales en cards superiores
3. **Explorar Trabajadores**: Lista con métricas resumidas
4. **Reporte Detallado**: Tap en trabajador para ver detalles
5. **Exportar**: Compartir reportes individuales o generales

### 🔄 **Actualización de Datos**
- **Automática**: Al abrir la pantalla
- **Manual**: Botón "🔄 Actualizar"
- **Tiempo Real**: Basado en datos actuales de la base de datos

## 📈 Beneficios del Sistema

### Para Administradores
- **Visibilidad Completa**: Métricas detalladas de cada trabajador
- **Toma de Decisiones**: Datos objetivos para asignaciones
- **Identificación de Oportunidades**: Trabajadores para promoción o capacitación
- **Optimización**: Mejor distribución de recursos humanos

### Para Supervisores
- **Seguimiento Individual**: Progreso de cada trabajador
- **Identificación de Necesidades**: Capacitación o apoyo adicional
- **Reconocimiento**: Trabajadores de alto rendimiento
- **Planificación**: Rotaciones más efectivas

### Para el Sistema
- **Eficiencia**: Consultas SQL optimizadas
- **Escalabilidad**: Maneja grandes volúmenes de datos
- **Precisión**: Cálculos exactos basados en datos reales
- **Flexibilidad**: Fácil extensión con nuevas métricas

---

**🎯 Resultado**: Sistema completo de reportes que proporciona insights valiosos sobre el rendimiento individual y del sistema, facilitando la toma de decisiones basada en datos y la optimización continua del proceso de rotación.