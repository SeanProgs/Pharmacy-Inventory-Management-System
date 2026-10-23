@echo off
setlocal
echo ========================================================
echo Starting HealthFirst Pharmacy Inventory Management System
echo ========================================================

REM Check if java is in PATH
where java >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    set JAVA_EXE=java
    goto RUN
)

REM Check JetBrains IntelliJ bundled JBR
if exist "C:\Program Files\JetBrains\IntelliJ IDEA 2026.1.1\jbr\bin\java.exe" (
    set JAVA_EXE="C:\Program Files\JetBrains\IntelliJ IDEA 2026.1.1\jbr\bin\java.exe"
    goto RUN
)

REM Check default Java installations
if exist "%JAVA_HOME%\bin\java.exe" (
    set JAVA_EXE="%JAVA_HOME%\bin\java.exe"
    goto RUN
)

echo ERROR: No Java Runtime Environment found. Please install Java 17+ or configure JAVA_HOME.
pause
exit /b 1

:RUN
echo Using Java: %JAVA_EXE%
%JAVA_EXE% -cp "target\classes;lib\*" Pharmacy
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo Application exited with error code %ERRORLEVEL%.
    pause
)
endlocal
