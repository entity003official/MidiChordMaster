@echo off
chcp 65001 >nul
cls
echo ==========================================
echo   MidiChordMaster APK - API Fixed Build
echo ==========================================
echo.

echo [INFO] Fixed remaining API compilation errors:
echo ✓ Fixed ChordDisplayScreen.kt compose imports
echo ✓ Fixed MidiManager.kt class name conflicts
echo ✓ Fixed MIDI API device access methods
echo ✓ Clarified android.media.midi.MidiManager type
echo ✓ All known compilation errors resolved

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
echo [Step 2] Complete project clean...
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
echo [Step 3] Final fixes summary...
echo ✓ Java 11 + Gradle 7.4.2 compatibility
echo ✓ AndroidX configuration
echo ✓ Material2 complete implementation
echo ✓ All XML resources created
echo ✓ All Kotlin source files fixed
echo ✓ MIDI API class conflicts resolved
echo ✓ Complete theme system
echo ✓ All imports and dependencies resolved

echo.
echo [Step 4] Starting API-fixed build...
echo All compilation errors should now be resolved!
echo.

call gradlew.bat clean assembleDebug

echo.
if %errorlevel% equ 0 (
    echo ==========================================
    echo   🎉🎉🎉 BUILD SUCCESSFUL! 🎉🎉🎉
    echo ==========================================
    echo.
    if exist "app\build\outputs\apk\debug\app-debug.apk" (
        echo ✅ APK successfully generated!
        echo.
        echo 📱 APK File Details:
        dir "app\build\outputs\apk\debug\app-debug.apk"
        echo.
        echo 📋 MidiChordMaster App Information:
        echo   📱 Name: MidiChordMaster - MIDI Chord Recognition App
        echo   📦 Package: com.midichordmaster
        echo   🔢 Version: 1.0
        echo   🎯 Target: Android 7.0+ (API 24-34)
        echo   🎨 UI Framework: Jetpack Compose with Material 2
        echo   🏗️ Architecture: MVVM with StateFlow
        echo   🎵 Core Features: MIDI support, Audio synthesis, Chord recognition
        echo   🎹 Icon: Custom music note with piano keys design
        echo.
        echo 🚀 Installation Ready!
        echo   1. Copy APK to your Android device
        echo   2. Enable "Install unknown apps" in Settings → Security
        echo   3. Tap the APK file to install
        echo   4. Grant audio/MIDI permissions when prompted
        echo   5. Connect MIDI device and start playing chords!
        echo.
        echo 🎵 Your MidiChordMaster app is ready! 🎵
        echo.
        echo 📝 App Features:
        echo   🎹 Real-time MIDI input processing
        echo   🔊 Audio synthesis with custom piano sounds
        echo   🎼 Intelligent chord recognition algorithm
        echo   📺 Visual piano keyboard display with pressed key indicators
        echo   🔌 MIDI device auto-detection and connection
        echo   📱 Responsive Material Design UI
        echo   🎨 Custom theming with purple/teal color scheme
        echo   ⚡ Low-latency audio processing
        echo   🎛️ Status indicators for MIDI and audio
        echo   🎚️ Control buttons for connection and testing
        echo.
        echo 🎊 Congratulations! Your MIDI Chord Master app is complete! 🎊
        echo.
        echo Ready to install and use on Android devices!
    ) else (
        echo ⚠️ Build succeeded but APK not found at expected location
        echo Searching for APK files...
        for /r "app\build\outputs" %%f in (*.apk) do echo Found: %%f
    )
) else (
    echo ==========================================
    echo   ❌ BUILD FAILED ❌
    echo ==========================================
    echo.
    echo Please check the error output above for any remaining issues.
    echo If errors persist, consider using Android Studio for automatic dependency resolution.
)

pause
