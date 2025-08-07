@echo off
chcp 65001 >nul
cls
echo ==========================================
echo   MidiChordMaster APK Build Script
echo ==========================================
echo.

echo Setting up Java environment...
rem Force use Java 11 since we downgraded Gradle plugin
set "JAVA_HOME=C:\Program Files\Microsoft\jdk-11.0.28.6-hotspot"
set "PATH=%JAVA_HOME%\bin;%PATH%"

echo Using Java 11 (compatible with Gradle 7.4.2)
echo Current JAVA_HOME: %JAVA_HOME%
echo.

echo Verifying Java version...
if exist "%JAVA_HOME%\bin\java.exe" (
    echo Java executable found: %JAVA_HOME%\bin\java.exe
    "%JAVA_HOME%\bin\java.exe" -version
    if %errorlevel% neq 0 (
        echo [ERROR] Cannot run Java
        echo Please check if JDK is properly installed
        pause
        exit /b 1
    )
) else (
    echo [ERROR] Java executable not found: %JAVA_HOME%\bin\java.exe
    echo.
    echo Please verify your JDK 11 installation path.
    echo Expected location: C:\Program Files\Microsoft\jdk-11.0.28.6-hotspot
    echo.
    echo If Java is installed elsewhere, please update JAVA_HOME in this script.
    pause
    exit /b 1
)

echo.
echo [SUCCESS] Java environment configured
echo.

echo Clearing any previous Gradle daemon...
call gradlew.bat --stop 2>nul

echo Starting APK build...
echo This may take several minutes for first build (downloading dependencies)...
echo.

echo Checking for gradlew.bat...
if exist "gradlew.bat" (
    echo Found gradlew.bat, starting build...
    echo.
    echo Running: gradlew.bat clean assembleDebug
    call gradlew.bat clean assembleDebug
) else (
    echo [ERROR] gradlew.bat not found
    echo Please make sure you are running this script from project root directory
    pause
    exit /b 1
)

if %errorlevel% equ 0 (
    echo.
    echo ==========================================
    echo   BUILD SUCCESSFUL!
    echo ==========================================
    echo.
    echo APK file location:
    echo   app\build\outputs\apk\debug\app-debug.apk
    echo.
    echo App Information:
    echo   Name: MidiChordMaster
    echo   Package: com.midichordmaster
    echo   Version: 1.0
    echo   Target: Android 7.0+ (API 24-34)
    echo.

    echo Checking if APK file was generated...
    if exist "app\build\outputs\apk\debug\app-debug.apk" (
        echo ✓ APK file successfully generated!
        echo.
        echo File details:
        dir "app\build\outputs\apk\debug\app-debug.apk"
        echo.
        echo You can now:
        echo 1. Copy APK to your Android device
        echo 2. Enable "Install unknown apps" in Android settings
        echo 3. Install the APK file
    ) else (
        echo ! Build completed but APK file not found at expected location
        echo Searching for APK files...
        dir app\build\outputs\apk\ /s /b *.apk 2>nul
    )
) else (
    echo.
    echo ==========================================
    echo   BUILD FAILED
    echo ==========================================
    echo.
    echo Common solutions:
    echo 1. Check network connection (for downloading dependencies)
    echo 2. Run: check_java_env.bat to verify Java setup
    echo 3. Try: gradlew.bat --info assembleDebug for detailed logs
    echo 4. Use Android Studio as alternative
    echo.
    echo If build keeps failing, you can also try:
    echo - gradlew.bat build --refresh-dependencies
    echo - Delete .gradle folder and retry
    echo.
)

pause
