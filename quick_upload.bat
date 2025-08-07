@echo off
chcp 65001 >nul
cls
echo ==========================================
echo   Quick GitHub Upload Fix
echo ==========================================
echo.

echo Completing the GitHub upload for MidiChordMaster...
echo.

echo [Step 1] Creating commit with simple message...
git commit -m "Initial commit: MidiChordMaster Android App"
if %errorlevel% neq 0 (
    echo Trying to commit again...
    git commit -m "Add MidiChordMaster files"
)

echo.
echo [Step 2] Pushing to GitHub...
git branch -M main
git push -u origin main

if %errorlevel% equ 0 (
    echo.
    echo ==========================================
    echo   SUCCESS! Upload Complete!
    echo ==========================================
    echo.
    echo ✅ Your MidiChordMaster app is now on GitHub!
    echo 📱 Repository: https://github.com/entity003official/MidiChordMaster
    echo.
    echo Next steps:
    echo 1. Visit your repository on GitHub
    echo 2. Add topics: android, kotlin, midi, music
    echo 3. Create a release with the APK file
    echo.
) else (
    echo.
    echo Upload failed. Let's try alternative method...
    echo.
    echo Manual steps:
    echo 1. Open GitHub Desktop or use git bash
    echo 2. Or upload files directly via GitHub web interface
    echo.
)

pause
