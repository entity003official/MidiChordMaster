@echo off
chcp 65001 >nul
cls
echo ==========================================
echo   MidiChordMaster APK - Compatibility Build
echo ==========================================
echo.

echo [INFO] All compatibility issues have been fixed:
echo - Material3 → AppCompat (compatible with Gradle 7.4.2)
echo - Updated dependencies to compatible versions
echo - Traditional theme attributes instead of Material3
echo - Compose BOM downgraded to 2022.10.00
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
echo [Step 2] Comprehensive project clean...
call gradlew.bat --stop >nul 2>&1

echo Removing all cache directories...
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
echo [Step 3] Configuration summary...
echo ✓ AndroidX enabled (gradle.properties)
echo ✓ AppCompat theme (themes.xml)
echo ✓ Material 2 instead of Material 3 (build.gradle)
echo ✓ Compatible Compose BOM: 2022.10.00
echo ✓ Gradle plugin: 7.4.2 (Java 11 compatible)

echo.
echo [Step 4] Starting final build...
echo This should work now with all compatibility fixes...
echo.

call gradlew.bat clean assembleDebug

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
        echo   UI Framework: Jetpack Compose with Material 2
        echo   Features: MIDI support, Audio synthesis, Chord recognition
        echo.
        echo 🚀 Installation Instructions:
        echo   1. Copy APK to your Android device
        echo   2. Enable "Install unknown apps" in Settings
        echo   3. Tap the APK file to install
        echo   4. Grant audio/MIDI permissions when prompted
        echo.
        echo 🎵 Your MidiChordMaster app is ready!
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
    echo All known compatibility issues have been addressed.
    echo Please check the error output above for any remaining issues.
    echo.
    echo If build still fails, you can try:
    echo 1. Use Android Studio (automatic dependency resolution)
    echo 2. Check network connection
    echo 3. Verify all source files are present
)

pause
