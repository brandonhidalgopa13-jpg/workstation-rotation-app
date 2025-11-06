@echo off
echo ========================================
echo  WorkStation Rotation v4.0.2 Release
echo ========================================
echo.

echo [1/6] Limpiando proyecto...
call gradlew clean --no-daemon
if %ERRORLEVEL% neq 0 (
    echo ERROR: Fallo en la limpieza del proyecto
    pause
    exit /b 1
)

echo.
echo [2/6] Ejecutando pruebas unitarias...
call gradlew testDebugUnitTest --no-daemon
if %ERRORLEVEL% neq 0 (
    echo ERROR: Fallo en las pruebas unitarias
    pause
    exit /b 1
)

echo.
echo [3/6] Generando APK Debug...
call gradlew assembleDebug --no-daemon
if %ERRORLEVEL% neq 0 (
    echo ERROR: Fallo en la generacion del APK Debug
    pause
    exit /b 1
)

echo.
echo [4/6] Generando APK Release...
call gradlew assembleRelease --no-daemon
if %ERRORLEVEL% neq 0 (
    echo ERROR: Fallo en la generacion del APK Release
    pause
    exit /b 1
)

echo.
echo [5/6] Generando reporte de cobertura...
call gradlew jacocoTestReport --no-daemon
if %ERRORLEVEL% neq 0 (
    echo WARNING: Fallo en la generacion del reporte de cobertura
)

echo.
echo [6/6] Copiando archivos de release...
if not exist "release-v4.0.2" mkdir "release-v4.0.2"

copy "app\build\outputs\apk\debug\app-debug.apk" "release-v4.0.2\workstation-rotation-v4.0.2-debug.apk"
copy "app\build\outputs\apk\release\app-release-unsigned.apk" "release-v4.0.2\workstation-rotation-v4.0.2-release.apk"

if exist "app\build\reports\jacoco\jacocoTestReport\html\index.html" (
    xcopy "app\build\reports\jacoco\jacocoTestReport\html" "release-v4.0.2\coverage-report\" /E /I /Y
)

if exist "app\build\reports\lint\lint-results.html" (
    copy "app\build\reports\lint\lint-results.html" "release-v4.0.2\lint-report.html"
)

copy "RELEASE_NOTES_v4.0.2.md" "release-v4.0.2\"
copy "ESTADO_ACTUAL_v4.0.2.md" "release-v4.0.2\"
copy "README.md" "release-v4.0.2\"

echo.
echo ========================================
echo  RELEASE v4.0.2 COMPLETADO EXITOSAMENTE
echo ========================================
echo.
echo Archivos generados en: release-v4.0.2\
echo.
echo APKs disponibles:
echo - workstation-rotation-v4.0.2-debug.apk
echo - workstation-rotation-v4.0.2-release.apk
echo.
echo Reportes incluidos:
echo - coverage-report\ (Cobertura de codigo)
echo - lint-report.html (Analisis de codigo)
echo - RELEASE_NOTES_v4.0.2.md (Notas de version)
echo - ESTADO_ACTUAL_v4.0.2.md (Estado del proyecto)
echo.
echo La aplicacion esta lista para distribucion!
echo.
pause