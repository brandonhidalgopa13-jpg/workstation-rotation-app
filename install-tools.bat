@echo off
echo Instalando herramientas necesarias para compilar Android...

echo.
echo 1. Verificando si winget está disponible...
winget --version >nul 2>&1
if %errorlevel% neq 0 (
    echo winget no está disponible. Instala manualmente:
    echo - Java JDK 17: https://adoptium.net/
    echo - Android Studio: https://developer.android.com/studio
    pause
    exit /b 1
)

echo.
echo 2. Instalando Java JDK 17...
winget install Microsoft.OpenJDK.17

echo.
echo 3. Descarga Android Studio desde: https://developer.android.com/studio
echo 4. Durante la instalación, asegúrate de instalar:
echo    - Android SDK
echo    - Android SDK Platform-Tools
echo    - Android SDK Build-Tools

echo.
echo 5. Después de instalar, configura las variables de entorno:
echo    ANDROID_HOME = C:\Users\%USERNAME%\AppData\Local\Android\Sdk
echo    Agrega al PATH: %%ANDROID_HOME%%\platform-tools

echo.
echo Una vez completado, ejecuta: gradlew assembleDebug
pause