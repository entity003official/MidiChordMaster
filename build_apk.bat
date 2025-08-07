@echo off
echo ==========================================
echo   MidiChordMaster APK构建脚本
echo ==========================================
echo.

echo 检查Java环境...
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo [错误] 未找到Java环境
    echo.
    echo 请选择以下方案之一：
    echo.
    echo 1. 安装Android Studio（推荐）
    echo    - 访问: https://developer.android.com/studio
    echo    - 会自动安装Java和所有必要工具
    echo.
    echo 2. 安装Java 11+
    echo    - 运行: winget install Microsoft.OpenJDK.11
    echo    - 或访问: https://adoptium.net/
    echo.
    echo 3. 使用便携式Java（临时方案）
    echo    - 下载JDK到项目文件夹
    echo    - 设置JAVA_HOME环境变量
    echo.
    pause
    exit /b 1
)

echo [成功] Java环境检测通过
java -version

echo.
echo 开始构建APK...
echo.

call gradlew.bat assembleDebug

if %errorlevel% equ 0 (
    echo.
    echo ==========================================
    echo   构建成功！
    echo ==========================================
    echo.
    echo APK文件位置:
    echo   app\build\outputs\apk\debug\app-debug.apk
    echo.
    echo 应用信息:
    echo   名称: MidiChordMaster
    echo   包名: com.midichordmaster
    echo   版本: 1.0
    echo.
    echo 安装说明:
    echo 1. 将APK文件复制到Android设备
    echo 2. 允许"未知来源"应用安装
    echo 3. 点击APK文件完成安装
    echo.
    
    echo 是否打开APK文件夹？ (Y/N)
    set /p choice=
    if /i "%choice%"=="Y" (
        explorer app\build\outputs\apk\debug\
    )
) else (
    echo.
    echo ==========================================
    echo   构建失败
    echo ==========================================
    echo.
    echo 请检查错误信息并尝试以下解决方案：
    echo.
    echo 1. 确保网络连接正常（需要下载依赖）
    echo 2. 重新运行构建脚本
    echo 3. 使用Android Studio进行构建
    echo.
)

echo.
pause
