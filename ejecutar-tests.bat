@echo off
echo ========================================
echo Ejecutando Tests Unitarios
echo ========================================
echo.

echo Ejecutando 23 tests...
call gradlew :shared:test

echo.
echo ========================================
if %ERRORLEVEL% EQU 0 (
    echo ✅ TODOS LOS TESTS PASARON
    echo.
    echo Reporte disponible en:
    echo shared\build\reports\tests\test\index.html
) else (
    echo ❌ ALGUNOS TESTS FALLARON
    echo.
    echo Revisa el reporte en:
    echo shared\build\reports\tests\test\index.html
)
echo ========================================
echo.
pause
