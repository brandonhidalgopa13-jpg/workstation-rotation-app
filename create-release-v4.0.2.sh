#!/bin/bash

echo "========================================"
echo " WorkStation Rotation v4.0.2 Release"
echo "========================================"
echo

echo "[1/6] Limpiando proyecto..."
./gradlew clean --no-daemon
if [ $? -ne 0 ]; then
    echo "ERROR: Fallo en la limpieza del proyecto"
    exit 1
fi

echo
echo "[2/6] Ejecutando pruebas unitarias..."
./gradlew testDebugUnitTest --no-daemon
if [ $? -ne 0 ]; then
    echo "ERROR: Fallo en las pruebas unitarias"
    exit 1
fi

echo
echo "[3/6] Generando APK Debug..."
./gradlew assembleDebug --no-daemon
if [ $? -ne 0 ]; then
    echo "ERROR: Fallo en la generación del APK Debug"
    exit 1
fi

echo
echo "[4/6] Generando APK Release..."
./gradlew assembleRelease --no-daemon
if [ $? -ne 0 ]; then
    echo "ERROR: Fallo en la generación del APK Release"
    exit 1
fi

echo
echo "[5/6] Generando reporte de cobertura..."
./gradlew jacocoTestReport --no-daemon
if [ $? -ne 0 ]; then
    echo "WARNING: Fallo en la generación del reporte de cobertura"
fi

echo
echo "[6/6] Copiando archivos de release..."
mkdir -p "release-v4.0.2"

cp "app/build/outputs/apk/debug/app-debug.apk" "release-v4.0.2/workstation-rotation-v4.0.2-debug.apk"
cp "app/build/outputs/apk/release/app-release-unsigned.apk" "release-v4.0.2/workstation-rotation-v4.0.2-release.apk"

if [ -f "app/build/reports/jacoco/jacocoTestReport/html/index.html" ]; then
    cp -r "app/build/reports/jacoco/jacocoTestReport/html" "release-v4.0.2/coverage-report/"
fi

if [ -f "app/build/reports/lint/lint-results.html" ]; then
    cp "app/build/reports/lint/lint-results.html" "release-v4.0.2/lint-report.html"
fi

cp "RELEASE_NOTES_v4.0.2.md" "release-v4.0.2/"
cp "ESTADO_ACTUAL_v4.0.2.md" "release-v4.0.2/"
cp "README.md" "release-v4.0.2/"

echo
echo "========================================"
echo " RELEASE v4.0.2 COMPLETADO EXITOSAMENTE"
echo "========================================"
echo
echo "Archivos generados en: release-v4.0.2/"
echo
echo "APKs disponibles:"
echo "- workstation-rotation-v4.0.2-debug.apk"
echo "- workstation-rotation-v4.0.2-release.apk"
echo
echo "Reportes incluidos:"
echo "- coverage-report/ (Cobertura de código)"
echo "- lint-report.html (Análisis de código)"
echo "- RELEASE_NOTES_v4.0.2.md (Notas de versión)"
echo "- ESTADO_ACTUAL_v4.0.2.md (Estado del proyecto)"
echo
echo "¡La aplicación está lista para distribución!"
echo