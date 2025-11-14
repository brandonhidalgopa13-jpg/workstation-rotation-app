@echo off
echo ========================================
echo Probando Fase 2 - Workstation Rotation
echo ========================================
echo.

echo [1/3] Compilando proyecto...
call gradlew :shared:build
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Fallo al compilar shared
    pause
    exit /b 1
)

echo.
echo [2/3] Compilando app Desktop...
call gradlew :desktopApp:build
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Fallo al compilar desktop
    pause
    exit /b 1
)

echo.
echo [3/3] Ejecutando app Desktop...
echo.
echo ========================================
echo ✅ Compilacion exitosa!
echo ========================================
echo.
echo La app se abrira en unos segundos...
echo.
echo PRUEBA ESTAS FUNCIONES:
echo 1. Agregar trabajadores
echo 2. Agregar estaciones
echo 3. Generar rotacion
echo 4. Ver historial
echo.
echo ========================================
echo.

call gradlew :desktopApp:run
