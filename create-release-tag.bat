@echo off
REM Script para crear tag de release v2.2.0

echo 🏷️ Creando tag de release v2.2.0...

REM Crear tag anotado con información detallada
git tag -a v2.2.0 -m "REWS v2.2.0 - Rotation and Workstation System

🎯 FUNCIONALIDADES PRINCIPALES:
✨ Sistema de entrenamiento avanzado con filtrado inteligente
✨ Restricciones específicas por estación (PROHIBIDO/LIMITADO/TEMPORAL)
✨ Funciones de captura profesional con compartir instantáneo
✨ Rebranding completo a REWS con nueva identidad visual

🔧 CORRECCIONES CRÍTICAS:
✅ Filtrado de estaciones por entrenador completamente funcional
✅ Sistema de entrenamiento con validaciones robustas
✅ Parejas entrenador-entrenado con prioridad absoluta

📚 DOCUMENTACIÓN COMPLETA:
📖 Manual de usuario renovado con todas las funcionalidades
📋 Guías de actualización y release notes detalladas
🔧 Documentación técnica actualizada

🎯 Información Técnica:
- Versión: v2.2.0 (versionCode: 4)
- Android: 7.0+ (API 24-34)
- Arquitectura: MVVM + Room Database
- Desarrollador: Brandon Josué Hidalgo Paz
- Fecha: Octubre 2024

🚀 Estado: LISTO PARA PRODUCCIÓN"

REM Subir el tag al repositorio remoto
git push origin v2.2.0

echo ✅ Tag v2.2.0 creado y subido exitosamente!
echo 🔗 Ahora puedes crear un release en GitHub usando este tag
pause