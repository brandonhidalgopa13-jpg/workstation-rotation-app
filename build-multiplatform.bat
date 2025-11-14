@echo off
echo ========================================
echo Compilando Workstation Rotation KMP
echo ========================================
echo.

echo [1/3] Compilando modulo compartido...
call gradlew :shared:build
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Fallo al compilar modulo compartido
    exit /b 1
)

echo.
echo [2/3] Compilando app Android...
call gradlew :androidApp:assembleDebug
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Fallo al compilar app Android
    exit /b 1
)

echo.
echo [3/3] Compilando app Desktop...
call gradlew :desktopApp:build
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Fallo al compilar app Desktop
    exit /b 1
)

echo.
echo ========================================
echo ✅ Compilacion exitosa!
echo ========================================
echo.
echo Archivos generados:
echo - Android APK: androidApp\build\outputs\apk\debug\
echo - Desktop JAR: desktopApp\build\libs\
echo.
echo Para ejecutar:
echo - Android: gradlew :androidApp:installDebug
echo - Desktop: gradlew :desktopApp:run
echo.
pause
