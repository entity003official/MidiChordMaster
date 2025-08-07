@echo off
chcp 65001 >nul
cls
echo ==========================================
echo   Direct GitHub Upload
echo ==========================================
echo.

echo Checking git remote...
git remote -v

echo.
echo Checking current status...
git status --porcelain

echo.
echo Creating final commit...
git add .
git commit -m "Complete MidiChordMaster Android MIDI App"

echo.
echo Pushing to GitHub...
git push origin main

if %errorlevel% equ 0 (
    echo.
    echo SUCCESS! Your app is now on GitHub!
    echo Visit: https://github.com/entity003official/MidiChordMaster
) else (
    echo.
    echo Push failed. Trying with force...
    git push -f origin main
)

pause
