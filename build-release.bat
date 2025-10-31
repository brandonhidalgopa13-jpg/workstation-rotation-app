@echo off
REM ═══════════════════════════════════════════════════════════════════════════════════════════════
REM 🚀 SCRIPT DE BUILD PARA RELEASE - REWS v2.2.0
REM ═══════════════════════════════════════════════════════════════════════════════════════════════
REM Automatiza el proceso completo de testing, calidad y generación de APK

echo.
echo ═══════════════════════════════════════════════════════════════════════════════════════════════
echo 🏭 REWS - ROTATION AND WORKSTATION SYSTEM - BUILD PARA RELEASE
echo ═══════════════════════════════════════════════════════════════════════════════════════════════
echo.

REM Verificar que estamos en el directorio correcto
if not exist "gradlew.bat" (
    echo ❌ Error: No se encontró gradlew.bat
    echo    Asegúrate de ejecutar este script desde la raíz del proyecto
    pause
    exit /b 1
)

echo 📋 Iniciando proceso de build completo...
echo.

REM ═══════════════════════════════════════════════════════════════════════════════════════════════
REM 🧹 LIMPIEZA
REM ═══════════════════════════════════════════════════════════════════════════════════════════════
echo 🧹 Paso 1/6: Limpiando proyecto...
call gradlew clean
if %ERRORLEVEL% neq 0 (
    echo ❌ Error en la limpieza del proyecto
    pause
    exit /b 1
)
echo ✅ Limpieza completada
echo.

REM ═══════════════════════════════════════════════════════════════════════════════════════════════
REM 🧪 TESTS UNITARIOS
REM ═══════════════════════════════════════════════════════════════════════════════════════════════
echo 🧪 Paso 2/6: Ejecutando tests unitarios...
call gradlew testDebugUnitTest
if %ERRORLEVEL% neq 0 (
    echo ❌ Error en tests unitarios
    echo 📊 Revisa los reportes en: app\build\reports\tests\testDebugUnitTest\index.html
    pause
    exit /b 1
)
echo ✅ Tests unitarios completados
echo.

REM ═══════════════════════════════════════════════════════════════════════════════════════════════
REM 🔍 ANÁLISIS DE CALIDAD
REM ═══════════════════════════════════════════════════════════════════════════════════════════════
echo 🔍 Paso 3/6: Ejecutando análisis de calidad (Lint)...
call gradlew lint
if %ERRORLEVEL% neq 0 (
    echo ⚠️  Advertencia: Se encontraron issues de lint
    echo 📊 Revisa los reportes en: app\build\reports\lint\lint-results.html
    echo 🤔 ¿Continuar con el build? (S/N)
    set /p continue=
    if /i not "%continue%"=="S" (
        echo ❌ Build cancelado por el usuario
        pause
        exit /b 1
    )
)
echo ✅ Análisis de calidad completado
echo.

REM ═══════════════════════════════════════════════════════════════════════════════════════════════
REM 📊 COBERTURA DE CÓDIGO
REM ═══════════════════════════════════════════════════════════════════════════════════════════════
echo 📊 Paso 4/6: Generando reporte de cobertura...
call gradlew jacocoTestReport
if %ERRORLEVEL% neq 0 (
    echo ⚠️  Advertencia: Error generando reporte de cobertura
    echo 📊 Revisa los logs para más detalles
)
echo ✅ Reporte de cobertura generado
echo.

REM ═══════════════════════════════════════════════════════════════════════════════════════════════
REM 🔧 BUILD DEBUG (VERIFICACIÓN)
REM ═══════════════════════════════════════════════════════════════════════════════════════════════
echo 🔧 Paso 5/6: Verificando build debug...
call gradlew assembleDebug
if %ERRORLEVEL% neq 0 (
    echo ❌ Error en build debug
    pause
    exit /b 1
)
echo ✅ Build debug verificado
echo.

REM ═══════════════════════════════════════════════════════════════════════════════════════════════
REM 🚀 BUILD RELEASE
REM ═══════════════════════════════════════════════════════════════════════════════════════════════
echo 🚀 Paso 6/6: Generando APK de release...

REM Verificar si existe keystore.properties
if not exist "keystore.properties" (
    echo ⚠️  Advertencia: No se encontró keystore.properties
    echo    Se usará keystore de debug para firmar la APK
    echo    Para producción, configura keystore.properties siguiendo keystore.properties.example
    echo.
)

call gradlew assembleRelease
if %ERRORLEVEL% neq 0 (
    echo ❌ Error en build release
    echo 💡 Posibles causas:
    echo    - Problemas con keystore.properties
    echo    - Errores de ProGuard
    echo    - Dependencias faltantes
    pause
    exit /b 1
)

echo ✅ APK de release generada exitosamente
echo.

REM ═══════════════════════════════════════════════════════════════════════════════════════════════
REM 📋 RESUMEN FINAL
REM ═══════════════════════════════════════════════════════════════════════════════════════════════
echo ═══════════════════════════════════════════════════════════════════════════════════════════════
echo 🎉 BUILD COMPLETADO EXITOSAMENTE
echo ═══════════════════════════════════════════════════════════════════════════════════════════════
echo.
echo 📱 APK de release generada en:
echo    app\build\outputs\apk\release\app-release.apk
echo.
echo 📊 Reportes disponibles:
echo    🧪 Tests: app\build\reports\tests\testDebugUnitTest\index.html
echo    🔍 Lint: app\build\reports\lint\lint-results.html
echo    📊 Cobertura: app\build\reports\jacoco\jacocoTestReport\html\index.html
echo.
echo 📋 Información de la APK:
for %%f in (app\build\outputs\apk\release\app-release.apk) do echo    📦 Tamaño: %%~zf bytes

REM Mostrar información adicional si existe
if exist "app\build\outputs\apk\release\app-release.apk" (
    echo    📅 Generada: %date% %time%
    echo    🏷️  Versión: 2.2.0
    echo    🎯 Target SDK: 34
    echo    📱 Min SDK: 24
)

echo.
echo 🚀 ¡La APK está lista para distribución!
echo.
echo ═══════════════════════════════════════════════════════════════════════════════════════════════
pause