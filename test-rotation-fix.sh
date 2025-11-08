#!/bin/bash

echo "═══════════════════════════════════════════════════════════════════════════════"
echo "🧪 TEST: Corrección de Rotación No Aparece - v4.0.8"
echo "═══════════════════════════════════════════════════════════════════════════════"
echo ""

echo "📋 Paso 1: Limpiar proyecto..."
./gradlew clean
if [ $? -ne 0 ]; then
    echo "❌ Error al limpiar proyecto"
    exit 1
fi
echo "✅ Proyecto limpiado"
echo ""

echo "📋 Paso 2: Compilar aplicación..."
./gradlew assembleDebug
if [ $? -ne 0 ]; then
    echo "❌ Error al compilar"
    exit 1
fi
echo "✅ Aplicación compilada"
echo ""

echo "📋 Paso 3: Verificar dispositivo conectado..."
adb devices
echo ""

echo "📋 Paso 4: Instalar aplicación..."
adb install -r app/build/outputs/apk/debug/app-debug.apk
if [ $? -ne 0 ]; then
    echo "❌ Error al instalar"
    exit 1
fi
echo "✅ Aplicación instalada"
echo ""

echo "═══════════════════════════════════════════════════════════════════════════════"
echo "✅ INSTALACIÓN COMPLETADA"
echo "═══════════════════════════════════════════════════════════════════════════════"
echo ""
echo "📱 Ahora:"
echo "   1. Abre la aplicación en el dispositivo"
echo "   2. Navega a 'Nueva Rotación'"
echo "   3. Verifica que aparezcan las estaciones"
echo "   4. Prueba 'Generar Automático'"
echo ""
echo "🔍 Para ver los logs en tiempo real, ejecuta:"
echo "   adb logcat | grep -E '(NewRotationService|NewRotationViewModel|NewRotationActivity|DataInitService)'"
echo ""
echo "═══════════════════════════════════════════════════════════════════════════════"
