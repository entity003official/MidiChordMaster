@echo off
chcp 65001 >nul
cls
echo ==========================================
echo   MidiChordMaster APK - Fixed Build
echo ==========================================
echo.

echo [Step 1] Setting up Java environment...
set "JAVA_HOME=C:\Program Files\Microsoft\jdk-11.0.28.6-hotspot"
set "PATH=%JAVA_HOME%\bin;%PATH%"

if not exist "%JAVA_HOME%\bin\java.exe" (
    echo [ERROR] Java not found at: %JAVA_HOME%
    pause
    exit /b 1
)

echo [SUCCESS] Java found and configured
"%JAVA_HOME%\bin\java.exe" -version | findstr "version"

echo.
echo [Step 2] AndroidX configuration added to gradle.properties
echo - android.useAndroidX=true (fixes the AndroidX dependency issue)
echo - android.suppressUnsupportedCompileSdk=34 (suppresses SDK warnings)

echo.
echo [Step 3] Cleaning previous build...
call gradlew.bat --stop >nul 2>&1
if exist ".gradle" rmdir /s /q ".gradle" >nul 2>&1
if exist "app\build" rmdir /s /q "app\build" >nul 2>&1

echo.
echo [Step 4] Starting fresh build...
echo This should work now with the AndroidX configuration fix...
echo.

call gradlew.bat clean assembleDebug

if %errorlevel% equ 0 (
    echo.
    echo ==========================================
    echo   BUILD SUCCESSFUL!
    echo ==========================================
    echo.
    if exist "app\build\outputs\apk\debug\app-debug.apk" (
        echo ✓ APK file successfully generated!
        echo.
        echo Location: app\build\outputs\apk\debug\app-debug.apk
        dir "app\build\outputs\apk\debug\app-debug.apk"
        echo.
        echo App Details:
        echo - Name: MidiChordMaster
        echo - Package: com.midichordmaster
        echo - Version: 1.0
        echo - Target: Android 7.0+ (API 24-34)
        echo.
        echo Ready for installation on Android devices!
    ) else (
        echo Build completed but APK not found at expected location
        echo Searching for APK files...
        dir app\build\outputs\apk\ /s /b *.apk 2>nul
    )
) else (
    echo.
    echo ==========================================
    echo   BUILD FAILED
    echo ==========================================
    echo.
    echo Please check the error messages above.
    echo The AndroidX issue should now be fixed.
)

pause
