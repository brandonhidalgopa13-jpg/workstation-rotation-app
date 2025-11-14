@echo off
echo ========================================
echo Verificando Estructura del Proyecto KMP
echo ========================================
echo.

set ERROR=0

echo [1/10] Verificando archivos de configuracion...
if exist "settings.gradle.kts" (
    echo   ✓ settings.gradle.kts
) else (
    echo   ✗ settings.gradle.kts NO ENCONTRADO
    set ERROR=1
)

if exist "build.gradle.kts" (
    echo   ✓ build.gradle.kts
) else (
    echo   ✗ build.gradle.kts NO ENCONTRADO
    set ERROR=1
)

if exist "gradle.properties" (
    echo   ✓ gradle.properties
) else (
    echo   ✗ gradle.properties NO ENCONTRADO
    set ERROR=1
)

echo.
echo [2/10] Verificando modulo shared...
if exist "shared\build.gradle.kts" (
    echo   ✓ shared\build.gradle.kts
) else (
    echo   ✗ shared\build.gradle.kts NO ENCONTRADO
    set ERROR=1
)

if exist "shared\src\commonMain" (
    echo   ✓ shared\src\commonMain
) else (
    echo   ✗ shared\src\commonMain NO ENCONTRADO
    set ERROR=1
)

echo.
echo [3/10] Verificando modulo androidApp...
if exist "androidApp\build.gradle.kts" (
    echo   ✓ androidApp\build.gradle.kts
) else (
    echo   ✗ androidApp\build.gradle.kts NO ENCONTRADO
    set ERROR=1
)

if exist "androidApp\src\main\AndroidManifest.xml" (
    echo   ✓ androidApp\src\main\AndroidManifest.xml
) else (
    echo   ✗ androidApp\src\main\AndroidManifest.xml NO ENCONTRADO
    set ERROR=1
)

echo.
echo [4/10] Verificando modulo desktopApp...
if exist "desktopApp\build.gradle.kts" (
    echo   ✓ desktopApp\build.gradle.kts
) else (
    echo   ✗ desktopApp\build.gradle.kts NO ENCONTRADO
    set ERROR=1
)

echo.
echo [5/10] Verificando base de datos SQLDelight...
if exist "shared\src\commonMain\sqldelight" (
    echo   ✓ shared\src\commonMain\sqldelight
) else (
    echo   ✗ shared\src\commonMain\sqldelight NO ENCONTRADO
    set ERROR=1
)

echo.
echo [6/10] Verificando modelos de dominio...
if exist "shared\src\commonMain\kotlin\com\workstation\rotation\domain\models" (
    echo   ✓ domain\models
) else (
    echo   ✗ domain\models NO ENCONTRADO
    set ERROR=1
)

echo.
echo [7/10] Verificando repositorio...
if exist "shared\src\commonMain\kotlin\com\workstation\rotation\domain\repository" (
    echo   ✓ domain\repository
) else (
    echo   ✗ domain\repository NO ENCONTRADO
    set ERROR=1
)

echo.
echo [8/10] Verificando pantallas UI...
if exist "shared\src\commonMain\kotlin\com\workstation\rotation\presentation\screens" (
    echo   ✓ presentation\screens
) else (
    echo   ✗ presentation\screens NO ENCONTRADO
    set ERROR=1
)

echo.
echo [9/10] Verificando ViewModels...
if exist "shared\src\commonMain\kotlin\com\workstation\rotation\presentation\viewmodels" (
    echo   ✓ presentation\viewmodels
) else (
    echo   ✗ presentation\viewmodels NO ENCONTRADO
    set ERROR=1
)

echo.
echo [10/10] Verificando scripts...
if exist "run-desktop.bat" (
    echo   ✓ run-desktop.bat
) else (
    echo   ✗ run-desktop.bat NO ENCONTRADO
    set ERROR=1
)

if exist "build-multiplatform.bat" (
    echo   ✓ build-multiplatform.bat
) else (
    echo   ✗ build-multiplatform.bat NO ENCONTRADO
    set ERROR=1
)

echo.
echo ========================================
if %ERROR%==0 (
    echo ✅ ESTRUCTURA CORRECTA
    echo.
    echo Todos los archivos necesarios estan presentes.
    echo Puedes continuar con la compilacion.
    echo.
    echo Siguiente paso:
    echo   1. Abrir proyecto en Android Studio
    echo   2. Esperar sincronizacion de Gradle
    echo   3. Ejecutar: build-multiplatform.bat
) else (
    echo ❌ ESTRUCTURA INCOMPLETA
    echo.
    echo Algunos archivos estan faltando.
    echo Revisa los errores arriba.
)
echo ========================================
echo.
pause
