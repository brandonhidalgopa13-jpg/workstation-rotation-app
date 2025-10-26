# App de Rotación de Estaciones de Trabajo

Una aplicación Android para gestionar estaciones de trabajo y rotación de trabajadores.

## Características

- **Gestión de Estaciones de Trabajo**: Agregar, editar y activar/desactivar estaciones
- **Gestión de Trabajadores**: Asignar trabajadores a múltiples estaciones usando checkboxes
- **Sistema de Rotación**: Generar rotaciones automáticas solo para trabajadores con estaciones asignadas
- **Interfaz Material Design**: UI moderna y fácil de usar
- **Base de datos local**: Funciona completamente offline

## Funcionalidades

### Estaciones de Trabajo
- Crear nuevas estaciones con nombre y descripción
- Editar estaciones existentes
- Activar/desactivar estaciones según necesidad
- Lista visual con cards

### Trabajadores
- Agregar trabajadores con información básica
- Asignar múltiples estaciones por trabajador
- Sistema de checkboxes para selección fácil
- Ver número de estaciones asignadas
- Solo trabajadores con estaciones participan en rotación

### Sistema de Rotación
- Generación automática de rotaciones
- Exclusión automática de trabajadores sin estaciones
- Vista clara del flujo: estación actual → próxima estación
- Contador de trabajadores elegibles

## Tecnologías

- **Kotlin** - Lenguaje principal
- **Room Database** - Persistencia local
- **Material Design 3** - Interfaz de usuario
- **MVVM Architecture** - Patrón arquitectónico
- **Coroutines** - Programación asíncrona
- **ViewBinding** - Vinculación de vistas

## Instalación

### Opción 1: Descargar APK
1. Ve a la sección [Releases](../../releases)
2. Descarga el archivo `app-debug.apk`
3. Instala en tu dispositivo Android

### Opción 2: Compilar desde código
1. Clona este repositorio
2. Abre en Android Studio
3. Ejecuta el proyecto

## Requisitos del Sistema

- Android 7.0 (API 24) o superior
- ~15 MB de espacio libre
- No requiere conexión a internet

## Uso

1. **Configurar Estaciones**: Agrega las estaciones de trabajo disponibles
2. **Registrar Trabajadores**: Agrega trabajadores y asigna sus estaciones
3. **Generar Rotación**: El sistema creará automáticamente la rotación

## Capturas de Pantalla

*Las capturas se agregarán próximamente*

## Contribuir

1. Fork el proyecto
2. Crea una rama para tu feature (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abre un Pull Request

## Licencia

Este proyecto está bajo la Licencia MIT - ver el archivo [LICENSE](LICENSE) para detalles.

## Contacto

Si tienes preguntas o sugerencias, no dudes en abrir un issue.