@echo off
echo Starting ChordieApp build and installation...
echo.

REM Check if Android device is connected
echo Checking for connected Android devices...
adb devices

echo.
echo Building the application...
call gradlew.bat assembleDebug
if %ERRORLEVEL% neq 0 (
    echo Build failed!
    pause
    exit /b 1
)

echo.
echo Installing application to device...
call gradlew.bat installDebug
if %ERRORLEVEL% neq 0 (
    echo Installation failed! Make sure your Android device is connected and USB debugging is enabled.
    pause
    exit /b 1
)

echo.
echo Application installed successfully!
echo.
echo To launch the app:
echo 1. Find "ChordieApp" in your Android device's app drawer
echo 2. Or run: adb shell am start -n com.chordieapp/.MainActivity
echo.
pause
