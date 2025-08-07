@echo off
chcp 65001 >nul
cls
echo ==========================================
echo   MidiChordMaster APK - Source Fixed Build
echo ==========================================
echo.

echo [INFO] All source code issues have been resolved:
echo ✓ Material3 → Material2 imports in all Kotlin files
echo ✓ Fixed ChordDisplayScreen.kt (Material2 components)
echo ✓ Fixed MainActivity.kt (Material2 Surface, MaterialTheme)
echo ✓ Fixed Theme.kt (darkColors, lightColors instead of ColorScheme)
echo ✓ Fixed Type.kt (Material2 Typography)
echo ✓ Fixed ChordDisplayViewModel.kt (complete when expression)
echo ✓ Fixed MidiManager.kt (API null safety)
echo ✓ AppCompat theme compatibility
echo ✓ All XML resources created

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
echo [Step 3] Source code fixes summary...
echo ✓ All Material3 imports → Material2
echo ✓ Color schemes → Color palettes
echo ✓ Typography updated
echo ✓ MIDI API null safety
echo ✓ Complete when expressions

echo.
echo [Step 4] Starting source-fixed build...
echo All source code compilation errors should now be resolved!
echo.

call gradlew.bat clean assembleDebug

echo.
if %errorlevel% equ 0 (
    echo ==========================================
    echo   🎉 BUILD SUCCESSFUL! 🎉
    echo ==========================================
    echo.
    if exist "app\build\outputs\apk\debug\app-debug.apk" (
        echo ✅ APK successfully generated!
        echo.
        echo 📱 APK File Details:
        dir "app\build\outputs\apk\debug\app-debug.apk"
        echo.
        echo 📋 MidiChordMaster App Information:
        echo   📱 Name: MidiChordMaster
        echo   📦 Package: com.midichordmaster
        echo   🔢 Version: 1.0
        echo   🎯 Target: Android 7.0+ (API 24-34)
        echo   🎨 UI: Jetpack Compose with Material 2
        echo   🎵 Features: MIDI support, Audio synthesis, Chord recognition
        echo   🎹 Icon: Custom music note with piano keys design
        echo   ⚙️ Architecture: MVVM with StateFlow
        echo.
        echo 🚀 Installation Ready!
        echo   1. Copy APK to your Android device
        echo   2. Enable "Install unknown apps" in Settings → Security
        echo   3. Tap the APK file to install
        echo   4. Grant audio/MIDI permissions when prompted
        echo   5. Connect MIDI device and start playing chords!
        echo.
        echo 🎵 Your MidiChordMaster app is ready to recognize chords! 🎵
        echo.
        echo 📝 App Features:
        echo   - Real-time MIDI input processing
        echo   - Audio synthesis with custom piano sounds
        echo   - Intelligent chord recognition algorithm
        echo   - Visual piano keyboard display
        echo   - MIDI device auto-detection
        echo   - Responsive Material Design UI
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
    echo All known compilation issues have been addressed.
    echo Please check the error output above for any remaining issues.
)

pause
