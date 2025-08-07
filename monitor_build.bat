@echo off
title MidiChordMaster APK构建监控
color 0A

:loop
cls
echo ================================================
echo     MidiChordMaster APK构建监控
echo ================================================
echo.
echo 项目: MidiChordMaster
echo 目标: Android APK (Debug版本)
echo 时间: %time%
echo.

echo 检查构建状态...
if exist "app\build" (
    echo [信息] 构建目录已创建
    if exist "app\build\outputs" (
        echo [信息] 输出目录已创建
        if exist "app\build\outputs\apk" (
            echo [信息] APK目录已创建
            if exist "app\build\outputs\apk\debug" (
                echo [信息] Debug目录已创建
                if exist "app\build\outputs\apk\debug\app-debug.apk" (
                    echo.
                    echo ================================================
                    echo               构建完成！
                    echo ================================================
                    echo.
                    echo APK文件已生成:
                    dir app\build\outputs\apk\debug\app-debug.apk
                    echo.
                    echo 文件大小: 
                    for %%A in (app\build\outputs\apk\debug\app-debug.apk) do echo %%~zA bytes
                    echo.
                    echo 完整路径:
                    echo %CD%\app\build\outputs\apk\debug\app-debug.apk
                    echo.
                    echo 安装到Android设备的步骤:
                    echo 1. 复制APK到手机
                    echo 2. 启用"未知来源"安装
                    echo 3. 点击APK安装
                    echo.
                    echo 应用特性:
                    echo - MIDI信号实时处理
                    echo - 智能和弦识别
                    echo - 钢琴音色合成
                    echo - Material Design界面
                    echo.
                    pause
                    exit /b 0
                ) else (
                    echo [等待] APK文件生成中...
                )
            ) else (
                echo [等待] Debug目录创建中...
            )
        ) else (
            echo [等待] APK目录创建中...
        )
    ) else (
        echo [等待] 输出目录创建中...
    )
) else (
    echo [等待] 构建目录创建中...
)

echo.
echo Gradle进程检查:
tasklist /fi "imagename eq java.exe" /fo table 2>nul | find "java.exe" >nul
if %errorlevel% equ 0 (
    echo [运行中] Java/Gradle进程活跃
) else (
    echo [警告] 未检测到Java进程
    echo 请检查Gradle是否正常运行
)

echo.
echo 按 Ctrl+C 停止监控
echo 自动刷新中...
timeout /t 5 /nobreak >nul
goto loop
