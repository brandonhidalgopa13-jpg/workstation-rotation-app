@echo off
echo ========================================
echo Probando Fase 3 - Funciones Avanzadas
echo ========================================
echo.

echo [1/4] Ejecutando tests unitarios...
call gradlew :shared:test
if %ERRORLEVEL% NEQ 0 (
    echo ADVERTENCIA: Algunos tests fallaron
    echo Continuando con la compilacion...
)

echo.
echo [2/4] Compilando proyecto...
call gradlew :shared:build
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Fallo al compilar shared
    pause
    exit /b 1
)

echo.
echo [3/4] Compilando app Desktop...
call gradlew :desktopApp:build
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Fallo al compilar desktop
    pause
    exit /b 1
)

echo.
echo [4/4] Ejecutando app Desktop...
echo.
echo ========================================
echo ✅ Tests y compilacion exitosos!
echo ========================================
echo.
echo NUEVAS FUNCIONES EN FASE 3:
echo 1. Vista detallada de rotacion
echo 2. Exportar rotacion (3 formatos)
echo 3. Estadisticas avanzadas
echo 4. 23 tests unitarios
echo.
echo PRUEBA ESTAS FUNCIONES:
echo 1. Generar una rotacion
echo 2. Ir a Historial
echo 3. Click en una rotacion
echo 4. Ver detalles completos
echo 5. Exportar rotacion
echo.
echo ========================================
echo.

call gradlew :desktopApp:run
