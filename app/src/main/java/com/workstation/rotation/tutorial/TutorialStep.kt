package com.workstation.rotation.tutorial

/**
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 📋 PASOS DEL TUTORIAL INTERACTIVO
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 
 * Define todos los pasos de la guía práctica para el usuario.
 */
enum class TutorialStep(
    val title: String,
    val description: String,
    val actionText: String
) {
    
    WELCOME(
        title = "🏭 ¡Bienvenido al Sistema de Rotación!",
        description = """
            Esta guía te ayudará a aprender cómo usar la aplicación paso a paso.
            
            El Sistema de Rotación Inteligente te permite:
            • Gestionar trabajadores y sus habilidades
            • Configurar estaciones de trabajo
            • Generar rotaciones automáticas optimizadas
            • Manejar entrenamientos y capacitaciones
            
            ¿Quieres continuar con la guía interactiva?
        """.trimIndent(),
        actionText = "Comenzar Guía"
    ),
    
    MAIN_SCREEN(
        title = "📱 Pantalla Principal",
        description = """
            Esta es la pantalla principal con tres secciones importantes:
            
            🏭 ESTACIONES DE TRABAJO
            • Configura las estaciones donde trabajarán las personas
            • Define capacidades y prioridades
            
            👥 TRABAJADORES  
            • Registra el personal y sus habilidades
            • Configura entrenamientos y disponibilidad
            
            🔄 SISTEMA DE ROTACIÓN
            • Genera rotaciones inteligentes automáticamente
            • Visualiza asignaciones actuales y siguientes
            
            Empezaremos configurando las estaciones de trabajo.
        """.trimIndent(),
        actionText = "Ir a Estaciones"
    ),
    
    WORKSTATIONS_INTRO(
        title = "🏭 Gestión de Estaciones",
        description = """
            Las estaciones de trabajo son los lugares donde el personal realizará sus tareas.
            
            INFORMACIÓN IMPORTANTE:
            • Nombre: Identifica la estación (ej: "Línea de Producción A")
            • Trabajadores Necesarios: Cuántas personas requiere (1-100)
            • Estación Prioritaria: Si debe mantener capacidad completa siempre
            
            ESTACIONES PRIORITARIAS ⭐:
            • Siempre se llenan con los mejores trabajadores disponibles
            • Tienen prioridad sobre estaciones normales
            • Ideales para áreas críticas de producción
            
            Vamos a crear tu primera estación.
        """.trimIndent(),
        actionText = "Crear Estación"
    ),
    
    WORKSTATION_CREATE(
        title = "➕ Crear Nueva Estación",
        description = """
            Para crear una estación de trabajo:
            
            1. Toca el botón "+" (Agregar)
            2. Ingresa un nombre descriptivo
            3. Define cuántos trabajadores necesita
            4. Marca como prioritaria si es crítica
            5. Guarda la configuración
            
            EJEMPLO PRÁCTICO:
            • Nombre: "Control de Calidad"
            • Trabajadores: 2
            • Prioritaria: ✓ (es área crítica)
            
            Crea al menos 2-3 estaciones para tener variedad en las rotaciones.
        """.trimIndent(),
        actionText = "Entendido"
    ),
    
    WORKERS_INTRO(
        title = "👥 Gestión de Trabajadores",
        description = """
            Ahora configuraremos el personal que participará en las rotaciones.
            
            INFORMACIÓN DEL TRABAJADOR:
            • Nombre y email (opcional)
            • Disponibilidad: Porcentaje de tiempo disponible (0-100%)
            • Restricciones: Limitaciones especiales (lesiones, horarios)
            
            SISTEMA DE ENTRENAMIENTO 🎓:
            • Entrenador 👨‍🏫: Puede capacitar a otros
            • Entrenado 🎯: Está aprendiendo una nueva habilidad
            • Trabajador Normal: Completamente capacitado
            
            ESTACIONES ASIGNADAS:
            • Selecciona dónde puede trabajar cada persona
            • Debe tener al menos una estación asignada
        """.trimIndent(),
        actionText = "Crear Trabajador"
    ),
    
    WORKER_CREATE(
        title = "➕ Crear Nuevo Trabajador",
        description = """
            Para registrar un trabajador:
            
            1. Toca el botón "+" (Agregar)
            2. Completa la información básica
            3. Ajusta la disponibilidad (100% = siempre disponible)
            4. Configura el rol de entrenamiento si aplica
            5. Selecciona las estaciones donde puede trabajar
            6. Guarda el registro
            
            CONSEJOS:
            • Crea al menos 4-5 trabajadores para rotaciones efectivas
            • Varía las disponibilidades (80%, 90%, 100%)
            • Asigna diferentes combinaciones de estaciones
            • Configura al menos un entrenador y un entrenado
        """.trimIndent(),
        actionText = "Entendido"
    ),
    
    TRAINING_SYSTEM(
        title = "🎓 Sistema de Entrenamiento",
        description = """
            El sistema de entrenamiento es una característica avanzada:
            
            CONFIGURACIÓN DE ENTRENAMIENTO:
            • Marca a alguien como "Está en Entrenamiento" 🎯
            • Selecciona su entrenador de la lista
            • Elige la estación donde recibirá entrenamiento
            
            COMPORTAMIENTO AUTOMÁTICO:
            • El entrenado y entrenador SIEMPRE estarán juntos
            • Se asignan automáticamente a la estación de entrenamiento
            • Tienen MÁXIMA PRIORIDAD sobre todas las demás asignaciones
            • Si la estación es prioritaria, tienen prioridad absoluta
            
            Esto garantiza continuidad en la capacitación.
        """.trimIndent(),
        actionText = "Continuar"
    ),
    
    ROTATION_INTRO(
        title = "🔄 Sistema de Rotación Inteligente",
        description = """
            ¡Ahora viene la magia! El sistema genera rotaciones automáticamente.
            
            ALGORITMO INTELIGENTE:
            1. Parejas entrenador-entrenado (prioridad máxima)
            2. Trabajadores entrenados que necesitan rotar
            3. Estaciones prioritarias (capacidad completa)
            4. Distribución optimizada del resto
            
            ROTACIÓN FORZADA:
            • Trabajadores entrenados rotan cada 2 ciclos
            • Previene estancamiento y desarrolla habilidades
            • Mantiene flexibilidad del equipo
            
            VISUALIZACIÓN:
            • Tabla con posición actual y siguiente
            • Indicadores de entrenamiento 🤝
            • Estado de capacidad por estación
        """.trimIndent(),
        actionText = "Generar Rotación"
    ),
    
    ROTATION_GENERATE(
        title = "⚡ Generar Rotación",
        description = """
            Para generar una rotación:
            
            1. Toca "🔄 Generar Rotación"
            2. El sistema procesará automáticamente:
               • Disponibilidad de cada trabajador
               • Restricciones y limitaciones
               • Relaciones de entrenamiento
               • Prioridades de estaciones
               • Historial de rotaciones
            
            3. Verás la tabla con dos fases:
               📍 ROTACIÓN ACTUAL: Dónde está cada uno ahora
               🔄 PRÓXIMA ROTACIÓN: Dónde irán después
            
            4. Puedes generar nuevas rotaciones cuando necesites
            
            ¡El sistema hace todo el trabajo pesado por ti!
        """.trimIndent(),
        actionText = "Entendido"
    ),
    
    CERTIFICATION_SYSTEM(
        title = "🎓 Sistema de Certificación",
        description = """
            Una vez que los trabajadores completan su entrenamiento, puedes certificarlos:
            
            PROCESO DE CERTIFICACIÓN:
            1. Ve a la sección "👥 Trabajadores"
            2. Toca el menú (⋮) en la barra superior
            3. Selecciona "🎓 Certificar Trabajadores"
            4. Marca los trabajadores que completaron su entrenamiento
            5. Confirma la certificación
            
            EFECTOS DE LA CERTIFICACIÓN:
            • El trabajador deja de estar "en entrenamiento"
            • Ya no necesita estar con su entrenador
            • Puede participar normalmente en rotaciones
            • Se convierte en trabajador completamente capacitado
            
            CUÁNDO CERTIFICAR:
            • Cuando el trabajador domina las tareas
            • Al completar el período de entrenamiento establecido
            • Cuando puede trabajar independientemente
        """.trimIndent(),
        actionText = "Continuar"
    ),
    
    TIPS_AND_TRICKS(
        title = "💡 Consejos y Trucos",
        description = """
            CONSEJOS PARA USAR LA APLICACIÓN EFECTIVAMENTE:
            
            🎯 CONFIGURACIÓN INICIAL:
            • Crea 3-5 estaciones variadas
            • Registra 5-10 trabajadores
            • Varía disponibilidades y habilidades
            
            ⭐ ESTACIONES PRIORITARIAS:
            • Úsalas para áreas críticas
            • Siempre mantendrán capacidad completa
            
            🎓 ENTRENAMIENTO Y CERTIFICACIÓN:
            • Configura parejas entrenador-entrenado
            • El sistema los mantendrá siempre juntos
            • Certifica trabajadores cuando completen su entrenamiento
            • Los trabajadores certificados rotan normalmente
            
            🔄 ROTACIONES:
            • Genera nuevas rotaciones regularmente
            • Los trabajadores entrenados rotarán automáticamente cada 2 ciclos
            • Revisa la tabla antes de implementar
            • El algoritmo optimiza automáticamente las asignaciones
            
            📱 NAVEGACIÓN:
            • Usa el tutorial cuando necesites recordar algo
            • Cada sección tiene su propia ayuda contextual
            • Los iconos te guían visualmente por la aplicación
        """.trimIndent(),
        actionText = "Finalizar Tutorial"
    );
    
    /**
     * Obtiene el siguiente paso del tutorial.
     */
    fun getNextStep(): TutorialStep? {
        return when (this) {
            WELCOME -> MAIN_SCREEN
            MAIN_SCREEN -> WORKSTATIONS_INTRO
            WORKSTATIONS_INTRO -> WORKSTATION_CREATE
            WORKSTATION_CREATE -> WORKERS_INTRO
            WORKERS_INTRO -> WORKER_CREATE
            WORKER_CREATE -> TRAINING_SYSTEM
            TRAINING_SYSTEM -> ROTATION_INTRO
            ROTATION_INTRO -> ROTATION_GENERATE
            ROTATION_GENERATE -> CERTIFICATION_SYSTEM
            CERTIFICATION_SYSTEM -> TIPS_AND_TRICKS
            TIPS_AND_TRICKS -> null
        }
    }
    
    /**
     * Indica si este paso requiere navegación a otra actividad.
     */
    fun requiresNavigation(): Boolean {
        return when (this) {
            WORKSTATIONS_INTRO -> true
            WORKERS_INTRO -> true
            ROTATION_INTRO -> true
            else -> false
        }
    }
    
    /**
     * Obtiene la actividad de destino para la navegación.
     */
    fun getTargetActivity(): Class<*>? {
        return when (this) {
            WORKSTATIONS_INTRO -> com.workstation.rotation.WorkstationActivity::class.java
            WORKERS_INTRO -> com.workstation.rotation.WorkerActivity::class.java
            ROTATION_INTRO -> com.workstation.rotation.RotationActivity::class.java
            else -> null
        }
    }
}