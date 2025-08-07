@echo off
chcp 65001 >nul
cls
echo ==========================================
echo   MidiChordMaster APK - Anti-Hang Build
echo ==========================================
echo.

echo [Step 1] Setting up Java environment...
set "JAVA_HOME=C:\Program Files\Microsoft\jdk-11.0.28.6-hotspot"
set "PATH=%JAVA_HOME%\bin;%PATH%"

if not exist "%JAVA_HOME%\bin\java.exe" (
    echo [ERROR] Java not found at: %JAVA_HOME%
    echo Please run check_java_env.bat first to verify Java installation
    pause
    exit /b 1
)

echo [SUCCESS] Java found: %JAVA_HOME%
"%JAVA_HOME%\bin\java.exe" -version | findstr "version"

echo.
echo [Step 2] Stopping any existing Gradle daemon...
call gradlew.bat --stop >nul 2>&1
timeout /t 2 /nobreak >nul

echo.
echo [Step 3] Cleaning project cache...
if exist ".gradle" (
    echo Removing .gradle cache...
    rmdir /s /q ".gradle" >nul 2>&1
)

if exist "app\build" (
    echo Removing app\build directory...
    rmdir /s /q "app\build" >nul 2>&1
)

echo.
echo [Step 4] Starting build with timeout protection...
echo This will show progress every 30 seconds to prevent hanging...
echo.

echo Starting Gradle build...
start /b cmd /c "gradlew.bat clean assembleDebug > build_output.log 2>&1 & echo Build process completed > build_done.flag"

:wait_loop
timeout /t 30 /nobreak >nul
if exist "build_done.flag" (
    goto build_finished
)

echo [%time%] Build still running... checking progress...
if exist "build_output.log" (
    echo Last few lines from build log:
    powershell "Get-Content 'build_output.log' | Select-Object -Last 3"
    echo.
)

echo Press Ctrl+C if you want to cancel the build
goto wait_loop

:build_finished
echo.
echo [Step 5] Build process completed, checking results...

if exist "build_output.log" (
    echo.
    echo === BUILD OUTPUT ===
    type build_output.log
    echo.
    echo === END BUILD OUTPUT ===
)

if exist "app\build\outputs\apk\debug\app-debug.apk" (
    echo.
    echo ==========================================
    echo   BUILD SUCCESSFUL!
    echo ==========================================
    echo.
    echo APK Location: app\build\outputs\apk\debug\app-debug.apk
    dir "app\build\outputs\apk\debug\app-debug.apk"
    echo.
    echo The APK is ready for installation on Android devices!
) else (
    echo.
    echo ==========================================
    echo   BUILD FAILED OR INCOMPLETE
    echo ==========================================
    echo.
    echo Checking for any APK files...
    dir app\build\outputs\apk\ /s /b *.apk 2>nul
    if %errorlevel% neq 0 (
        echo No APK files found.
    )
    echo.
    echo Please check the build output above for error details.
)

rem Clean up temp files
del build_done.flag >nul 2>&1
del build_output.log >nul 2>&1

echo.
pause
