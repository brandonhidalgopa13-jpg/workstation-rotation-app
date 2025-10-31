#!/bin/bash
# ═══════════════════════════════════════════════════════════════════════════════════════════════
# 🚀 SCRIPT DE BUILD PARA RELEASE - REWS v2.2.0
# ═══════════════════════════════════════════════════════════════════════════════════════════════
# Automatiza el proceso completo de testing, calidad y generación de APK

set -e  # Exit on any error

echo ""
echo "═══════════════════════════════════════════════════════════════════════════════════════════════"
echo "🏭 REWS - ROTATION AND WORKSTATION SYSTEM - BUILD PARA RELEASE"
echo "═══════════════════════════════════════════════════════════════════════════════════════════════"
echo ""

# Verificar que estamos en el directorio correcto
if [ ! -f "gradlew" ]; then
    echo "❌ Error: No se encontró gradlew"
    echo "   Asegúrate de ejecutar este script desde la raíz del proyecto"
    exit 1
fi

# Hacer gradlew ejecutable
chmod +x gradlew

echo "📋 Iniciando proceso de build completo..."
echo ""

# ═══════════════════════════════════════════════════════════════════════════════════════════════
# 🧹 LIMPIEZA
# ═══════════════════════════════════════════════════════════════════════════════════════════════
echo "🧹 Paso 1/6: Limpiando proyecto..."
./gradlew clean
echo "✅ Limpieza completada"
echo ""

# ═══════════════════════════════════════════════════════════════════════════════════════════════
# 🧪 TESTS UNITARIOS
# ═══════════════════════════════════════════════════════════════════════════════════════════════
echo "🧪 Paso 2/6: Ejecutando tests unitarios..."
if ! ./gradlew testDebugUnitTest; then
    echo "❌ Error en tests unitarios"
    echo "📊 Revisa los reportes en: app/build/reports/tests/testDebugUnitTest/index.html"
    exit 1
fi
echo "✅ Tests unitarios completados"
echo ""

# ═══════════════════════════════════════════════════════════════════════════════════════════════
# 🔍 ANÁLISIS DE CALIDAD
# ═══════════════════════════════════════════════════════════════════════════════════════════════
echo "🔍 Paso 3/6: Ejecutando análisis de calidad (Lint)..."
if ! ./gradlew lint; then
    echo "⚠️  Advertencia: Se encontraron issues de lint"
    echo "📊 Revisa los reportes en: app/build/reports/lint/lint-results.html"
    read -p "🤔 ¿Continuar con el build? (s/N): " continue
    if [[ ! $continue =~ ^[Ss]$ ]]; then
        echo "❌ Build cancelado por el usuario"
        exit 1
    fi
fi
echo "✅ Análisis de calidad completado"
echo ""

# ═══════════════════════════════════════════════════════════════════════════════════════════════
# 📊 COBERTURA DE CÓDIGO
# ═══════════════════════════════════════════════════════════════════════════════════════════════
echo "📊 Paso 4/6: Generando reporte de cobertura..."
if ! ./gradlew jacocoTestReport; then
    echo "⚠️  Advertencia: Error generando reporte de cobertura"
    echo "📊 Revisa los logs para más detalles"
fi
echo "✅ Reporte de cobertura generado"
echo ""

# ═══════════════════════════════════════════════════════════════════════════════════════════════
# 🔧 BUILD DEBUG (VERIFICACIÓN)
# ═══════════════════════════════════════════════════════════════════════════════════════════════
echo "🔧 Paso 5/6: Verificando build debug..."
if ! ./gradlew assembleDebug; then
    echo "❌ Error en build debug"
    exit 1
fi
echo "✅ Build debug verificado"
echo ""

# ═══════════════════════════════════════════════════════════════════════════════════════════════
# 🚀 BUILD RELEASE
# ═══════════════════════════════════════════════════════════════════════════════════════════════
echo "🚀 Paso 6/6: Generando APK de release..."

# Verificar si existe keystore.properties
if [ ! -f "keystore.properties" ]; then
    echo "⚠️  Advertencia: No se encontró keystore.properties"
    echo "   Se usará keystore de debug para firmar la APK"
    echo "   Para producción, configura keystore.properties siguiendo keystore.properties.example"
    echo ""
fi

if ! ./gradlew assembleRelease; then
    echo "❌ Error en build release"
    echo "💡 Posibles causas:"
    echo "   - Problemas con keystore.properties"
    echo "   - Errores de ProGuard"
    echo "   - Dependencias faltantes"
    exit 1
fi

echo "✅ APK de release generada exitosamente"
echo ""

# ═══════════════════════════════════════════════════════════════════════════════════════════════
# 📋 RESUMEN FINAL
# ═══════════════════════════════════════════════════════════════════════════════════════════════
echo "═══════════════════════════════════════════════════════════════════════════════════════════════"
echo "🎉 BUILD COMPLETADO EXITOSAMENTE"
echo "═══════════════════════════════════════════════════════════════════════════════════════════════"
echo ""
echo "📱 APK de release generada en:"
echo "   app/build/outputs/apk/release/app-release.apk"
echo ""
echo "📊 Reportes disponibles:"
echo "   🧪 Tests: app/build/reports/tests/testDebugUnitTest/index.html"
echo "   🔍 Lint: app/build/reports/lint/lint-results.html"
echo "   📊 Cobertura: app/build/reports/jacoco/jacocoTestReport/html/index.html"
echo ""
echo "📋 Información de la APK:"

# Mostrar información de la APK si existe
APK_PATH="app/build/outputs/apk/release/app-release.apk"
if [ -f "$APK_PATH" ]; then
    APK_SIZE=$(stat -f%z "$APK_PATH" 2>/dev/null || stat -c%s "$APK_PATH" 2>/dev/null || echo "N/A")
    echo "   📦 Tamaño: $APK_SIZE bytes"
    echo "   📅 Generada: $(date)"
    echo "   🏷️  Versión: 2.2.0"
    echo "   🎯 Target SDK: 34"
    echo "   📱 Min SDK: 24"
fi

echo ""
echo "🚀 ¡La APK está lista para distribución!"
echo ""
echo "═══════════════════════════════════════════════════════════════════════════════════════════════"