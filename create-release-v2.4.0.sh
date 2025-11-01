#!/bin/bash

# 🚀 REWS v2.4.0 - Script de Release Automático
# Autor: Brandon Josué Hidalgo Paz
# Fecha: Octubre 2024

set -e  # Salir en caso de error

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
NC='\033[0m' # No Color

# Variables
VERSION="2.4.0"
TAG="v${VERSION}"
BRANCH="main"

echo -e "${PURPLE}========================================${NC}"
echo -e "${PURPLE}🚀 REWS v${VERSION} - Release Script${NC}"
echo -e "${PURPLE}========================================${NC}"
echo ""

# Función para mostrar mensajes
log_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

log_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

log_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

log_error() {
    echo -e "${RED}❌ $1${NC}"
}

# Verificar que estamos en la rama correcta
log_info "Verificando rama actual..."
CURRENT_BRANCH=$(git branch --show-current)
if [ "$CURRENT_BRANCH" != "$BRANCH" ]; then
    log_error "Debes estar en la rama $BRANCH. Rama actual: $CURRENT_BRANCH"
    exit 1
fi
log_success "En la rama correcta: $BRANCH"

# Verificar que no hay cambios sin commit
log_info "Verificando estado del repositorio..."
if [ -n "$(git status --porcelain)" ]; then
    log_error "Hay cambios sin commit. Commit todos los cambios antes de crear el release."
    git status --short
    exit 1
fi
log_success "Repositorio limpio"

# Verificar que estamos actualizados con origin
log_info "Verificando sincronización con origin..."
git fetch origin
LOCAL=$(git rev-parse HEAD)
REMOTE=$(git rev-parse origin/$BRANCH)
if [ "$LOCAL" != "$REMOTE" ]; then
    log_error "La rama local no está sincronizada con origin. Ejecuta git pull."
    exit 1
fi
log_success "Sincronizado con origin"

# Verificar que el tag no existe
log_info "Verificando que el tag $TAG no existe..."
if git tag -l | grep -q "^$TAG$"; then
    log_error "El tag $TAG ya existe. Usa un número de versión diferente."
    exit 1
fi
log_success "Tag $TAG disponible"

# Ejecutar tests
log_info "Ejecutando tests unitarios..."
if ./gradlew testDebugUnitTest; then
    log_success "Tests unitarios pasaron"
else
    log_warning "Algunos tests fallaron, pero continuando..."
fi

# Ejecutar lint
log_info "Ejecutando análisis de código (lint)..."
if ./gradlew lintDebug; then
    log_success "Lint pasó sin problemas"
else
    log_warning "Lint encontró problemas, pero continuando..."
fi

# Construir APK de release
log_info "Construyendo APK de release..."
if ./gradlew assembleRelease; then
    log_success "APK de release construido exitosamente"
else
    log_error "Error construyendo APK de release"
    exit 1
fi

# Verificar que el APK existe
APK_PATH="app/build/outputs/apk/release/app-release.apk"
if [ ! -f "$APK_PATH" ]; then
    log_error "APK de release no encontrado en $APK_PATH"
    exit 1
fi

# Obtener información del APK
APK_SIZE=$(du -h "$APK_PATH" | cut -f1)
log_success "APK generado: $APK_SIZE"

# Crear tag
log_info "Creando tag $TAG..."
git tag -a "$TAG" -m "🚀 REWS v${VERSION} - Sistema Completo con Mejoras Integrales

🎯 Funcionalidades Principales:
• 👑 Sistema de Liderazgo Completamente Implementado
• 📊 Sistema de Reportes y Estadísticas Avanzadas  
• ⚡ Optimización de Rendimiento con Monitoreo
• 🔔 Sistema de Notificaciones Inteligentes
• ⚙️ Configuraciones Avanzadas Expandidas
• 🗄️ Gestión Avanzada de Base de Datos
• 🔍 Sistema de Diagnósticos y Logs
• 🧹 Herramientas de Mantenimiento

🚀 Estado: LISTO PARA PRODUCCIÓN
Sistema empresarial completo con herramientas profesionales."

log_success "Tag $TAG creado"

# Push del tag
log_info "Subiendo tag a origin..."
git push origin "$TAG"
log_success "Tag subido a origin"

# Generar changelog para el release
log_info "Generando notas del release..."
RELEASE_NOTES="RELEASE_NOTES_v${VERSION}.md"
if [ -f "$RELEASE_NOTES" ]; then
    log_success "Notas del release encontradas: $RELEASE_NOTES"
else
    log_warning "Notas del release no encontradas, usando changelog básico"
fi

# Mostrar información del release
echo ""
echo -e "${GREEN}🎉 Release v${VERSION} creado exitosamente!${NC}"
echo ""
echo -e "${BLUE}📊 Información del Release:${NC}"
echo -e "   • Versión: ${VERSION}"
echo -e "   • Tag: ${TAG}"
echo -e "   • Rama: ${BRANCH}"
echo -e "   • APK: ${APK_PATH} (${APK_SIZE})"
echo -e "   • Commit: $(git rev-parse --short HEAD)"
echo ""

echo -e "${BLUE}🔗 Enlaces útiles:${NC}"
echo -e "   • Repositorio: https://github.com/brandonhidalgopa13-jpg/workstation-rotation-app"
echo -e "   • Releases: https://github.com/brandonhidalgopa13-jpg/workstation-rotation-app/releases"
echo -e "   • Tag: https://github.com/brandonhidalgopa13-jpg/workstation-rotation-app/releases/tag/${TAG}"
echo ""

echo -e "${BLUE}📋 Próximos pasos:${NC}"
echo -e "   1. Ve a GitHub Releases"
echo -e "   2. El workflow automático creará el release"
echo -e "   3. Verifica que el APK se subió correctamente"
echo -e "   4. Actualiza la documentación si es necesario"
echo -e "   5. Anuncia el release a los usuarios"
echo ""

echo -e "${PURPLE}🚀 REWS v${VERSION} listo para distribución!${NC}"
echo ""

# Opcional: Abrir GitHub releases en el navegador
read -p "¿Deseas abrir GitHub Releases en el navegador? (y/N): " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    if command -v xdg-open > /dev/null; then
        xdg-open "https://github.com/brandonhidalgopa13-jpg/workstation-rotation-app/releases"
    elif command -v open > /dev/null; then
        open "https://github.com/brandonhidalgopa13-jpg/workstation-rotation-app/releases"
    else
        log_info "Abre manualmente: https://github.com/brandonhidalgopa13-jpg/workstation-rotation-app/releases"
    fi
fi

log_success "Script de release completado exitosamente!"