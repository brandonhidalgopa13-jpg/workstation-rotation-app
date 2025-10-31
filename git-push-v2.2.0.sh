#!/bin/bash
# Script para subir REWS v2.2.0 a GitHub

echo "🚀 Subiendo REWS v2.2.0 a GitHub..."

# Agregar todos los archivos
git add .

# Commit con mensaje descriptivo
git commit -m "🚀 Release REWS v2.2.0 - Sistema Avanzado de Rotaciones

✨ Nuevas Funcionalidades:
- 🎓 Sistema de entrenamiento con filtrado inteligente
- 🚫 Restricciones específicas por estación  
- 📷 Funciones avanzadas de captura y compartir
- 🏷️ Rebranding completo a REWS

🔧 Correcciones Críticas:
- ✅ Filtrado de estaciones por entrenador corregido
- ✅ Sistema de entrenamiento completamente funcional
- ✅ Validaciones robustas implementadas

📚 Documentación:
- 📖 Manual de usuario completamente renovado
- 📋 Guías de actualización y release notes
- 🔧 Documentación técnica actualizada

🎯 Versión: v2.2.0 (versionCode: 4)
👨‍💻 Desarrollador: Brandon Josué Hidalgo Paz"

# Push a la rama principal
git push origin main

echo "✅ REWS v2.2.0 subido exitosamente a GitHub!"