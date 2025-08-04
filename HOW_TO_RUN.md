# 如何运行ChordieApp

## 方法一：使用Android Studio（推荐）

1. **安装Android Studio**
   - 下载并安装最新版本的Android Studio
   - 确保安装了Android SDK (API 24+)

2. **打开项目**
   - 启动Android Studio
   - 选择 "Open an existing project"
   - 导航到 `d:\Work\2025_8_4chrodapp` 文件夹并打开

3. **同步项目**
   - Android Studio会自动提示同步Gradle
   - 等待所有依赖下载完成

4. **连接设备**
   - **真机方式**：
     - 在Android手机上启用"开发者选项"
     - 启用"USB调试"
     - 用USB线连接手机到电脑
   - **模拟器方式**：
     - 在Android Studio中创建一个AVD (Android Virtual Device)
     - 选择API 24或更高版本

5. **运行应用**
   - 点击绿色的"Run"按钮 ▶️
   - 或按快捷键 `Shift + F10`
   - 选择目标设备并等待应用安装

## 方法二：使用命令行

1. **确保环境配置**
   ```cmd
   # 检查Java环境
   java -version
   
   # 检查Android设备连接
   adb devices
   ```

2. **构建并安装**
   ```cmd
   # 进入项目目录
   cd d:\Work\2025_8_4chrodapp
   
   # 构建Debug版本
   .\gradlew.bat assembleDebug
   
   # 安装到连接的设备
   .\gradlew.bat installDebug
   ```

3. **启动应用**
   ```cmd
   # 通过ADB启动应用
   adb shell am start -n com.chordieapp/.MainActivity
   ```

## 方法三：使用VS Code (当前环境)

1. **使用内置任务**
   - 按 `Ctrl + Shift + P` 打开命令面板
   - 输入 "Tasks: Run Task"
   - 选择 "Build Android App"

2. **手动运行脚本**
   - 在VS Code终端中运行：
   ```cmd
   .\run_app.bat
   ```

## 故障排除

### 常见问题

**1. 设备未识别**
- 确保USB调试已启用
- 尝试不同的USB线
- 安装手机厂商的USB驱动

**2. 构建失败**
- 检查网络连接（下载依赖）
- 确保Java版本兼容
- 清理项目：`.\gradlew.bat clean`

**3. 权限问题**
- 应用启动后会请求蓝牙和音频权限
- 请允许这些权限以使用完整功能

### 测试应用功能

1. **启动应用后**：
   - 应该看到"ChordieApp"主界面
   - 有"Connect MIDI"和"Test Audio"按钮

2. **测试音频**：
   - 点击"Test Audio"按钮
   - 应该听到C大调和弦的钢琴声音
   - 界面应显示"C Major"和高亮的琴键

3. **连接MIDI设备**：
   - 点击"Connect MIDI"按钮
   - 连接USB MIDI键盘或蓝牙MIDI设备
   - 状态指示器应变为绿色

## 下一步

成功运行后，您可以：
- 连接MIDI键盘进行实时演奏
- 观看和弦实时识别
- 体验钢琴音色合成
- 查看可视化钢琴界面
