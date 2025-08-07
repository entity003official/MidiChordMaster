@echo off
chcp 65001 >nul
cls
echo ==========================================
echo   Java Environment Diagnostic Tool
echo ==========================================
echo.

echo Checking system Java installations...
echo.

echo === System PATH Java ===
where java 2>nul
if %errorlevel% neq 0 (
    echo No Java found in system PATH
) else (
    echo System Java version:
    java -version 2>&1
)

echo.
echo === Environment Variables ===
echo JAVA_HOME: %JAVA_HOME%
echo.

echo === Manual JDK Detection ===
echo Checking common JDK installation paths...

echo Checking Microsoft JDK 17...
for /d %%i in ("C:\Program Files\Microsoft\jdk-17*") do (
    echo Found: %%i
    if exist "%%i\bin\java.exe" (
        echo   - Java executable exists
        "%%i\bin\java.exe" -version 2>&1 | findstr "version"
    )
)

echo.
echo Checking Microsoft JDK 11...
for /d %%i in ("C:\Program Files\Microsoft\jdk-11*") do (
    echo Found: %%i
    if exist "%%i\bin\java.exe" (
        echo   - Java executable exists
        "%%i\bin\java.exe" -version 2>&1 | findstr "version"
    )
)

echo.
echo Checking Oracle JDK...
for /d %%i in ("C:\Program Files\Java\jdk*") do (
    echo Found: %%i
    if exist "%%i\bin\java.exe" (
        echo   - Java executable exists
        "%%i\bin\java.exe" -version 2>&1 | findstr "version"
    )
)

echo.
echo === Current Working Directory ===
echo %CD%

echo.
echo === Gradle Files Check ===
if exist "gradlew.bat" (
    echo ✓ gradlew.bat found
) else (
    echo ✗ gradlew.bat NOT found
)

if exist "gradle\wrapper\gradle-wrapper.properties" (
    echo ✓ gradle-wrapper.properties found
    echo Gradle version:
    findstr "distributionUrl" gradle\wrapper\gradle-wrapper.properties
) else (
    echo ✗ gradle-wrapper.properties NOT found
)

echo.
echo === Network Connectivity Test ===
echo Testing internet connection...
ping -n 1 8.8.8.8 >nul 2>&1
if %errorlevel% equ 0 (
    echo ✓ Internet connection OK
) else (
    echo ✗ No internet connection - this may cause Gradle download issues
)

echo.
echo === Process Check ===
echo Checking for running Java processes...
tasklist /FI "IMAGENAME eq java.exe" 2>nul | findstr "java.exe"
if %errorlevel% neq 0 (
    echo No Java processes running
)

echo.
pause
