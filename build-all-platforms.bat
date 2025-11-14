@echo off
echo ========================================
echo Compilando Workstation Rotation - Todas las Plataformas
echo ========================================
echo.

echo [1/3] Compilando modulo compartido...
call .\gradlew :shared:build
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Fallo al compilar modulo compartido
    pause
    exit /b 1
)

echo.
echo [2/3] Compilando app Android...
call .\gradlew :androidApp:assembleDebug
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Fallo al compilar app Android
    pause
    exit /b 1
)

echo.
echo [3/3] Compilando app Desktop...
call .\gradlew :desktopApp:packageDistributionForCurrentOS
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Fallo al compilar app Desktop
    pause
    exit /b 1
)

echo.
echo ========================================
echo Compilacion exitosa!
echo ========================================
echo.
echo Archivos generados:
echo - Android APK: androidApp\build\outputs\apk\debug\
echo - Desktop: desktopApp\build\compose\binaries\main\
echo.
pause
