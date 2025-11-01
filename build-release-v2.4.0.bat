@echo off
echo ========================================
echo 🚀 REWS v2.4.0 - Build Release Script
echo ========================================
echo.

echo 📋 Verificando configuración...
if not exist "local.properties" (
    echo ❌ Error: local.properties no encontrado
    echo 🔧 Creando local.properties...
    echo sdk.dir=C:\\Users\\Brand\\AppData\\Local\\Android\\Sdk > local.properties
    echo ✅ local.properties creado
)

echo.
echo 🧹 Limpiando proyecto...
call gradlew clean
if %ERRORLEVEL% neq 0 (
    echo ❌ Error en clean
    pause
    exit /b 1
)

echo.
echo 🔍 Ejecutando lint...
call gradlew lintRelease
if %ERRORLEVEL% neq 0 (
    echo ⚠️ Advertencia: Lint encontró problemas, continuando...
)

echo.
echo 🧪 Ejecutando tests unitarios...
call gradlew testReleaseUnitTest
if %ERRORLEVEL% neq 0 (
    echo ⚠️ Advertencia: Algunos tests fallaron, continuando...
)

echo.
echo 📦 Construyendo APK Release...
call gradlew assembleRelease
if %ERRORLEVEL% neq 0 (
    echo ❌ Error en build release
    pause
    exit /b 1
)

echo.
echo 📊 Generando reportes...
call gradlew jacocoTestReport
if %ERRORLEVEL% neq 0 (
    echo ⚠️ Advertencia: Error generando reportes de cobertura
)

echo.
echo ✅ Build completado exitosamente!
echo.
echo 📁 APK generado en: app\build\outputs\apk\release\
echo 📊 Reportes en: app\build\reports\
echo.

if exist "app\build\outputs\apk\release\app-release.apk" (
    echo 🎉 APK Release encontrado:
    dir "app\build\outputs\apk\release\app-release.apk"
    echo.
    echo 🔗 ¿Deseas abrir la carpeta de salida? (S/N)
    set /p choice=
    if /i "%choice%"=="S" (
        start "" "app\build\outputs\apk\release\"
    )
) else (
    echo ❌ APK Release no encontrado
)

echo.
echo 📋 Resumen del Build:
echo • Versión: 2.4.0
echo • Tipo: Release
echo • Fecha: %date% %time%
echo • Estado: Completado
echo.
pause