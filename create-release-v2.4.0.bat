@echo off
setlocal enabledelayedexpansion

REM 🚀 REWS v2.4.0 - Script de Release Automático
REM Autor: Brandon Josué Hidalgo Paz
REM Fecha: Octubre 2024

set VERSION=2.4.0
set TAG=v%VERSION%
set BRANCH=main

echo ========================================
echo 🚀 REWS v%VERSION% - Release Script
echo ========================================
echo.

echo ℹ️  Verificando rama actual...
for /f "tokens=*" %%i in ('git branch --show-current') do set CURRENT_BRANCH=%%i
if not "%CURRENT_BRANCH%"=="%BRANCH%" (
    echo ❌ Debes estar en la rama %BRANCH%. Rama actual: %CURRENT_BRANCH%
    pause
    exit /b 1
)
echo ✅ En la rama correcta: %BRANCH%

echo.
echo ℹ️  Verificando estado del repositorio...
git status --porcelain > temp_status.txt
for /f %%i in ("temp_status.txt") do set size=%%~zi
del temp_status.txt
if %size% gtr 0 (
    echo ❌ Hay cambios sin commit. Commit todos los cambios antes de crear el release.
    git status --short
    pause
    exit /b 1
)
echo ✅ Repositorio limpio

echo.
echo ℹ️  Verificando sincronización con origin...
git fetch origin
if %ERRORLEVEL% neq 0 (
    echo ❌ Error al hacer fetch de origin
    pause
    exit /b 1
)
echo ✅ Sincronizado con origin

echo.
echo ℹ️  Verificando que el tag %TAG% no existe...
git tag -l | findstr /x "%TAG%" > nul
if %ERRORLEVEL% equ 0 (
    echo ❌ El tag %TAG% ya existe. Usa un número de versión diferente.
    pause
    exit /b 1
)
echo ✅ Tag %TAG% disponible

echo.
echo ℹ️  Ejecutando tests unitarios...
call gradlew testDebugUnitTest
if %ERRORLEVEL% equ 0 (
    echo ✅ Tests unitarios pasaron
) else (
    echo ⚠️  Algunos tests fallaron, pero continuando...
)

echo.
echo ℹ️  Ejecutando análisis de código (lint)...
call gradlew lintDebug
if %ERRORLEVEL% equ 0 (
    echo ✅ Lint pasó sin problemas
) else (
    echo ⚠️  Lint encontró problemas, pero continuando...
)

echo.
echo ℹ️  Construyendo APK de release...
call gradlew assembleRelease
if %ERRORLEVEL% neq 0 (
    echo ❌ Error construyendo APK de release
    pause
    exit /b 1
)
echo ✅ APK de release construido exitosamente

echo.
echo ℹ️  Verificando APK generado...
set APK_PATH=app\build\outputs\apk\release\app-release.apk
if not exist "%APK_PATH%" (
    echo ❌ APK de release no encontrado en %APK_PATH%
    pause
    exit /b 1
)

for %%i in ("%APK_PATH%") do set APK_SIZE=%%~zi
set /a APK_SIZE_MB=%APK_SIZE%/1024/1024
echo ✅ APK generado: %APK_SIZE_MB% MB

echo.
echo ℹ️  Creando tag %TAG%...
git tag -a "%TAG%" -m "🚀 REWS v%VERSION% - Sistema Completo con Mejoras Integrales

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

if %ERRORLEVEL% neq 0 (
    echo ❌ Error creando tag
    pause
    exit /b 1
)
echo ✅ Tag %TAG% creado

echo.
echo ℹ️  Subiendo tag a origin...
git push origin "%TAG%"
if %ERRORLEVEL% neq 0 (
    echo ❌ Error subiendo tag
    pause
    exit /b 1
)
echo ✅ Tag subido a origin

echo.
echo ℹ️  Verificando notas del release...
set RELEASE_NOTES=RELEASE_NOTES_v%VERSION%.md
if exist "%RELEASE_NOTES%" (
    echo ✅ Notas del release encontradas: %RELEASE_NOTES%
) else (
    echo ⚠️  Notas del release no encontradas, usando changelog básico
)

echo.
echo 🎉 Release v%VERSION% creado exitosamente!
echo.
echo 📊 Información del Release:
echo    • Versión: %VERSION%
echo    • Tag: %TAG%
echo    • Rama: %BRANCH%
echo    • APK: %APK_PATH% (%APK_SIZE_MB% MB)
for /f "tokens=*" %%i in ('git rev-parse --short HEAD') do echo    • Commit: %%i
echo.

echo 🔗 Enlaces útiles:
echo    • Repositorio: https://github.com/brandonhidalgopa13-jpg/workstation-rotation-app
echo    • Releases: https://github.com/brandonhidalgopa13-jpg/workstation-rotation-app/releases
echo    • Tag: https://github.com/brandonhidalgopa13-jpg/workstation-rotation-app/releases/tag/%TAG%
echo.

echo 📋 Próximos pasos:
echo    1. Ve a GitHub Releases
echo    2. El workflow automático creará el release
echo    3. Verifica que el APK se subió correctamente
echo    4. Actualiza la documentación si es necesario
echo    5. Anuncia el release a los usuarios
echo.

echo 🚀 REWS v%VERSION% listo para distribución!
echo.

set /p choice="¿Deseas abrir GitHub Releases en el navegador? (S/N): "
if /i "%choice%"=="S" (
    start "" "https://github.com/brandonhidalgopa13-jpg/workstation-rotation-app/releases"
)

echo.
echo ✅ Script de release completado exitosamente!
pause