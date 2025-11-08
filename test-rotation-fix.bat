@echo off
echo ═══════════════════════════════════════════════════════════════════════════════
echo 🧪 TEST: Corrección de Rotación No Aparece - v4.0.8
echo ═══════════════════════════════════════════════════════════════════════════════
echo.

echo 📋 Paso 1: Limpiar proyecto...
call gradlew clean
if %ERRORLEVEL% NEQ 0 (
    echo ❌ Error al limpiar proyecto
    pause
    exit /b 1
)
echo ✅ Proyecto limpiado
echo.

echo 📋 Paso 2: Compilar aplicación...
call gradlew assembleDebug
if %ERRORLEVEL% NEQ 0 (
    echo ❌ Error al compilar
    pause
    exit /b 1
)
echo ✅ Aplicación compilada
echo.

echo 📋 Paso 3: Verificar dispositivo conectado...
adb devices
echo.

echo 📋 Paso 4: Instalar aplicación...
adb install -r app\build\outputs\apk\debug\app-debug.apk
if %ERRORLEVEL% NEQ 0 (
    echo ❌ Error al instalar
    pause
    exit /b 1
)
echo ✅ Aplicación instalada
echo.

echo ═══════════════════════════════════════════════════════════════════════════════
echo ✅ INSTALACIÓN COMPLETADA
echo ═══════════════════════════════════════════════════════════════════════════════
echo.
echo 📱 Ahora:
echo    1. Abre la aplicación en el dispositivo
echo    2. Navega a "Nueva Rotación"
echo    3. Verifica que aparezcan las estaciones
echo    4. Prueba "Generar Automático"
echo.
echo 🔍 Para ver los logs en tiempo real, ejecuta:
echo    adb logcat ^| findstr "NewRotationService NewRotationViewModel NewRotationActivity DataInitService"
echo.
echo ═══════════════════════════════════════════════════════════════════════════════
pause
