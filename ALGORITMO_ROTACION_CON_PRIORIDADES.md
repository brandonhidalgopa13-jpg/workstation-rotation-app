# 🎯 ALGORITMO DE ROTACIÓN CON PRIORIDADES ESTRICTAS

## 📋 Objetivo del Sistema

Implementar un sistema de rotación verdadera donde los trabajadores **cambian de estación** entre la primera y segunda rotación, respetando estrictamente las prioridades del negocio.

## 🏆 Jerarquía de Prioridades

### 1. 🎯 **ENTRENAMIENTOS** (Prioridad Máxima)
- **Parejas entrenador-entrenado** permanecen **SIEMPRE juntas**
- Se asignan a su **estación de entrenamiento designada**
- Aparecen en **ambas rotaciones** para continuidad del proceso
- **No rotan** - mantienen la misma estación para efectividad del entrenamiento

### 2. 👑 **LÍDERES** (Alta Prioridad)
- **Líderes FIRST_HALF**: Solo en primera rotación
- **Líderes SECOND_HALF**: Solo en segunda rotación  
- **Líderes BOTH**: En ambas rotaciones
- Permanecen **fijos en su estación de liderazgo**
- **No rotan** - mantienen autoridad y continuidad

### 3. ♿ **TRABAJADORES CON RESTRICCIONES** (Prioridad Media)
- Trabajadores con **discapacidades** o **limitaciones**
- Trabajadores con **disponibilidad < 100%**
- Trabajadores con **notas de restricción**
- Se asignan considerando sus **limitaciones específicas**
- Pueden rotar solo entre **estaciones compatibles**

### 4. 👤 **TRABAJADORES REGULARES** (Prioridad Normal)
- Trabajadores sin restricciones especiales
- **Rotan entre diferentes estaciones** entre primera y segunda rotación
- Implementan la **rotación verdadera** del sistema
- Balancean la carga de trabajo entre estaciones

## 🔄 Algoritmo de Rotación Verdadera

### Fase 1: Asignación de Entrenamientos
```
PARA cada pareja de entrenamiento:
  ├── Verificar compatibilidad con estación de entrenamiento
  ├── Asignar a AMBAS rotaciones en la MISMA estación
  └── Garantizar continuidad del proceso de entrenamiento
```

### Fase 2: Asignación de Líderes
```
PARA cada líder:
  ├── Si es FIRST_HALF → Solo primera rotación
  ├── Si es SECOND_HALF → Solo segunda rotación
  ├── Si es BOTH → Ambas rotaciones
  └── SIEMPRE en su estación de liderazgo designada
```

### Fase 3: Trabajadores con Restricciones
```
PARA cada trabajador con restricciones:
  ├── Evaluar limitaciones específicas
  ├── Filtrar estaciones compatibles
  ├── Priorizar estaciones que más necesiten trabajadores
  └── Asignar considerando disponibilidad y capacidades
```

### Fase 4: Rotación de Trabajadores Regulares
```
PARA cada trabajador regular:
  ├── Si puede trabajar en ≥2 estaciones:
  │   ├── Estación A → Primera rotación
  │   └── Estación B → Segunda rotación (ROTACIÓN VERDADERA)
  └── Si solo puede trabajar en 1 estación:
      └── Asignar a la rotación que más necesite trabajadores
```

### Fase 5: Finalización y Balance
```
├── Completar estaciones con capacidad insuficiente
├── Balancear cargas entre rotaciones
├── Verificar cobertura completa
└── Reportar estado final con prioridades
```

## 🎯 Características del Sistema

### ✅ Garantías del Algoritmo

1. **Entrenamientos Protegidos**: Parejas nunca se separan
2. **Liderazgo Continuo**: Líderes en sus estaciones designadas
3. **Inclusión Adaptativa**: Trabajadores con restricciones incluidos apropiadamente
4. **Rotación Verdadera**: Trabajadores regulares cambian de estación
5. **Cobertura Completa**: Todas las estaciones cubiertas según capacidad

### 🔍 Validaciones Automáticas

- **Compatibilidad**: Solo asignaciones a estaciones donde pueden trabajar
- **Capacidad**: Respeto a límites de trabajadores por estación
- **Restricciones**: Verificación de limitaciones específicas
- **Prioridades**: Orden estricto de asignación
- **Balance**: Distribución equitativa de cargas

## 📊 Ejemplo de Rotación

### Trabajador Regular: Juan
- **Puede trabajar en**: Estación A, B, C
- **Primera rotación**: Estación A
- **Segunda rotación**: Estación B
- **Resultado**: ✅ Rotación verdadera implementada

### Pareja de Entrenamiento: María (entrenadora) + Pedro (entrenado)
- **Estación de entrenamiento**: Estación C
- **Primera rotación**: Ambos en Estación C
- **Segunda rotación**: Ambos en Estación C
- **Resultado**: ✅ Continuidad de entrenamiento garantizada

### Líder BOTH: Carlos
- **Estación de liderazgo**: Estación A
- **Primera rotación**: Estación A (como líder)
- **Segunda rotación**: Estación A (como líder)
- **Resultado**: ✅ Liderazgo continuo mantenido

## 🚀 Beneficios del Sistema

### Para el Negocio
- **Continuidad de Procesos**: Entrenamientos y liderazgo sin interrupciones
- **Desarrollo de Habilidades**: Trabajadores regulares aprenden múltiples estaciones
- **Inclusión**: Trabajadores con restricciones participan apropiadamente
- **Eficiencia**: Cobertura óptima de todas las estaciones

### Para los Trabajadores
- **Justicia**: Sistema transparente y predecible
- **Desarrollo**: Oportunidades de aprender nuevas habilidades
- **Inclusión**: Consideración de limitaciones individuales
- **Estabilidad**: Procesos críticos (entrenamiento/liderazgo) protegidos

## 🔧 Configuración y Mantenimiento

### Datos Requeridos
- **Trabajadores**: Roles, restricciones, disponibilidad
- **Estaciones**: Capacidad, prioridad, requisitos
- **Relaciones**: Worker-Workstation, entrenamientos, liderazgos
- **Restricciones**: Limitaciones específicas por trabajador

### Monitoreo
- **Cobertura**: Porcentaje de estaciones completamente cubiertas
- **Balance**: Distribución equitativa entre rotaciones
- **Prioridades**: Cumplimiento de jerarquía establecida
- **Rotación**: Efectividad del cambio de estaciones

---

**Resultado**: Sistema de rotación inteligente que balancea las necesidades operativas con el desarrollo del personal, garantizando continuidad de procesos críticos mientras maximiza las oportunidades de aprendizaje y crecimiento.