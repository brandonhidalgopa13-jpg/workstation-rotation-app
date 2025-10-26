@echo off
echo ========================================
echo  SUBIR PROYECTO A GITHUB
echo ========================================
echo.

echo 1. Inicializando repositorio Git...
git init
if %errorlevel% neq 0 (
    echo ERROR: Git no está instalado. Descarga desde: https://git-scm.com/
    pause
    exit /b 1
)

echo.
echo 2. Agregando archivos al repositorio...
git add .
git commit -m "Initial commit: Android Workstation Rotation App"

echo.
echo 3. INSTRUCCIONES PARA SUBIR A GITHUB:
echo.
echo    a) Ve a https://github.com y crea una cuenta si no tienes
echo    b) Haz clic en "New repository"
echo    c) Nombre sugerido: "workstation-rotation-app"
echo    d) NO marques "Initialize with README" (ya tenemos uno)
echo    e) Haz clic en "Create repository"
echo.
echo 4. Después de crear el repositorio, ejecuta estos comandos:
echo.
echo    git remote add origin https://github.com/TU_USUARIO/workstation-rotation-app.git
echo    git branch -M main
echo    git push -u origin main
echo.
echo 5. GitHub Actions compilará automáticamente el APK
echo    Lo encontrarás en: Repositorio → Actions → Build APK → Artifacts
echo.

pause