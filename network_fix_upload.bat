@echo off
chcp 65001 >nul
cls
echo ==========================================
echo   GitHub Upload - Network Fix
echo ==========================================
echo.

echo Trying different upload methods...
echo.

echo [Method 1] Using SSH instead of HTTPS...
git remote set-url origin git@github.com:entity003official/MidiChordMaster.git
git push origin main
if %errorlevel% equ 0 (
    echo SSH push successful!
    goto success
)

echo.
echo [Method 2] Using different HTTPS URL...
git remote set-url origin https://github.com/entity003official/MidiChordMaster.git
git push origin main
if %errorlevel% equ 0 (
    echo HTTPS push successful!
    goto success
)

echo.
echo [Method 3] Setting longer timeout...
git config --global http.postBuffer 524288000
git config --global http.lowSpeedLimit 0
git config --global http.lowSpeedTime 999999
git push origin main
if %errorlevel% equ 0 (
    echo Timeout-extended push successful!
    goto success
)

echo.
echo Network connection failed. Using alternative methods...
echo.
echo Alternative solutions:
echo 1. Use GitHub Desktop (recommended)
echo 2. Use different network (mobile hotspot)
echo 3. Upload via GitHub web interface
echo 4. Try VPN if behind firewall
echo.
goto end

:success
echo.
echo ==========================================
echo   SUCCESS! Upload Complete!
echo ==========================================
echo.
echo Your MidiChordMaster app is now on GitHub!
echo Repository: https://github.com/entity003official/MidiChordMaster
echo.

:end
pause
