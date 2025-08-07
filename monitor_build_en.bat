@echo off
chcp 65001 >nul
title MidiChordMaster APK Build Monitor
color 0A

:loop
cls
echo ================================================
echo     MidiChordMaster APK Build Monitor
echo ================================================
echo.
echo Project: MidiChordMaster
echo Target: Android APK (Debug)
echo Time: %time%
echo.

echo Checking build status...
if exist "app\build" (
    echo [INFO] Build directory created
    if exist "app\build\outputs" (
        echo [INFO] Output directory created
        if exist "app\build\outputs\apk" (
            echo [INFO] APK directory created
            if exist "app\build\outputs\apk\debug" (
                echo [INFO] Debug directory created
                if exist "app\build\outputs\apk\debug\app-debug.apk" (
                    echo.
                    echo ================================================
                    echo               BUILD COMPLETE!
                    echo ================================================
                    echo.
                    echo APK Generated:
                    dir app\build\outputs\apk\debug\app-debug.apk
                    echo.
                    echo File size: 
                    for %%A in (app\build\outputs\apk\debug\app-debug.apk) do echo %%~zA bytes
                    echo.
                    echo Full path:
                    echo %CD%\app\build\outputs\apk\debug\app-debug.apk
                    echo.
                    echo Installation Steps:
                    echo 1. Copy APK to Android device
                    echo 2. Enable "Unknown sources" installation
                    echo 3. Tap APK file to install
                    echo.
                    echo App Features:
                    echo - Real-time MIDI signal processing
                    echo - Intelligent chord recognition
                    echo - Piano sound synthesis
                    echo - Material Design interface
                    echo.
                    pause
                    exit /b 0
                ) else (
                    echo [WAIT] APK file generating...
                )
            ) else (
                echo [WAIT] Debug directory creating...
            )
        ) else (
            echo [WAIT] APK directory creating...
        )
    ) else (
        echo [WAIT] Output directory creating...
    )
) else (
    echo [WAIT] Build directory creating...
)

echo.
echo Gradle process check:
tasklist /fi "imagename eq java.exe" /fo table 2>nul | find "java.exe" >nul
if %errorlevel% equ 0 (
    echo [RUNNING] Java/Gradle process active
) else (
    echo [WARNING] No Java process detected
    echo Please check if Gradle is running properly
)

echo.
echo Press Ctrl+C to stop monitoring
echo Auto-refreshing in 5 seconds...
timeout /t 5 /nobreak >nul
goto loop
