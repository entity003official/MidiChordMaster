@echo off
echo 检查APK构建状态...
echo.

if exist "app\build\outputs\apk\debug\app-debug.apk" (
    echo [成功] APK已生成！
    echo.
    echo 文件信息:
    dir app\build\outputs\apk\debug\app-debug.apk
    echo.
    echo APK位置: %CD%\app\build\outputs\apk\debug\app-debug.apk
    echo.
    echo 安装方法:
    echo 1. 将APK文件传输到Android设备
    echo 2. 在设备设置中启用"未知来源"应用安装
    echo 3. 点击APK文件安装MidiChordMaster
    echo.
    echo 应用功能:
    echo - 实时MIDI信号接收
    echo - 和弦识别与显示
    echo - 钢琴音色合成
    echo - 可视化钢琴键盘
    echo.
) else (
    echo [等待] APK正在构建中...
    echo.
    echo 请耐心等待Gradle下载依赖包并编译项目。
    echo 这可能需要几分钟时间，特别是首次构建。
    echo.
    echo 如果长时间无响应，请：
    echo 1. 检查网络连接
    echo 2. 重新运行: .\gradlew.bat assembleDebug
    echo 3. 或使用Android Studio打开项目
    echo.
)

pause
