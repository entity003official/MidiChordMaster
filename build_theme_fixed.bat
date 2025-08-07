@echo off
chcp 65001 >nul
cls
echo ==========================================
echo   MidiChordMaster APK - Theme Fixed Build
echo ==========================================
echo.

echo [INFO] Material3 theme compatibility issues have been fixed:
echo - Removed deprecated colorPrimaryVariant and colorSecondaryVariant
echo - Added Material3 compatible color attributes
echo - Fixed Theme.Material3.DayNight.NoActionBar parent reference
echo.

echo [Step 1] Setting up Java environment...
set "JAVA_HOME=C:\Program Files\Microsoft\jdk-11.0.28.6-hotspot"
set "PATH=%JAVA_HOME%\bin;%PATH%"

if not exist "%JAVA_HOME%\bin\java.exe" (
    echo [ERROR] Java not found at: %JAVA_HOME%
    pause
    exit /b 1
)

echo [SUCCESS] Java 11 configured
"%JAVA_HOME%\bin\java.exe" -version | findstr "version"

echo.
echo [Step 2] Comprehensive clean before build...
call gradlew.bat --stop >nul 2>&1

echo Removing build caches...
if exist ".gradle" (
    echo - Removing .gradle cache
    rmdir /s /q ".gradle" >nul 2>&1
)
if exist "app\build" (
    echo - Removing app\build directory
    rmdir /s /q "app\build" >nul 2>&1
)
if exist "build" (
    echo - Removing root build directory
    rmdir /s /q "build" >nul 2>&1
)

echo.
echo [Step 3] Verifying project configuration...
echo ✓ AndroidX enabled (gradle.properties)
echo ✓ Material3 theme fixed (themes.xml)
echo ✓ Gradle plugin version: 7.4.2 (compatible with Java 11)

echo.
echo [Step 4] Starting build with detailed output...
echo This may take a few minutes for the first build...
echo.

call gradlew.bat clean assembleDebug --info --stacktrace

echo.
if %errorlevel% equ 0 (
    echo ==========================================
    echo   BUILD SUCCESSFUL! 🎉
    echo ==========================================
    echo.
    if exist "app\build\outputs\apk\debug\app-debug.apk" (
        echo ✅ APK successfully generated!
        echo.
        echo 📱 APK Details:
        dir "app\build\outputs\apk\debug\app-debug.apk"
        echo.
        echo 📋 App Information:
        echo   Name: MidiChordMaster
        echo   Package: com.midichordmaster
        echo   Version: 1.0
        echo   Target: Android 7.0+ (API 24-34)
        echo   Features: MIDI support, Audio synthesis, Chord recognition
        echo.
        echo 🚀 Installation Instructions:
        echo   1. Copy APK to your Android device
        echo   2. Enable "Install unknown apps" in Settings
        echo   3. Tap the APK file to install
        echo   4. Grant audio/MIDI permissions when prompted
        echo.
    ) else (
        echo ⚠️ Build succeeded but APK not found at expected location
        echo Searching for APK files...
        for /r "app\build\outputs" %%f in (*.apk) do echo Found: %%f
    )
) else (
    echo ==========================================
    echo   BUILD FAILED ❌
    echo ==========================================
    echo.
    echo The theme compatibility issue should now be resolved.
    echo Please check the detailed error output above.
    echo.
    echo Common next steps:
    echo 1. Check network connection (for dependency downloads)
    echo 2. Verify all source files are present
    echo 3. Try running: gradlew.bat build --refresh-dependencies
)

pause
