# 🚀 ChordieApp 快速启动指南

## 最简单的运行方式

### 1. 准备Android设备
- 在Android手机上启用"开发者选项"
- 启用"USB调试"
- 用USB线连接手机到电脑

### 2. 在VS Code中运行
**方法A - 使用任务面板：**
1. 按 `Ctrl + Shift + P`
2. 输入 "Tasks: Run Task"
3. 选择 "Build and Run ChordieApp"

**方法B - 使用快捷键：**
1. 按 `Ctrl + Shift + P`
2. 输入 "Tasks: Run Build Task"
3. 按 Enter（这会运行默认构建任务）

**方法C - 使用终端：**
```bash
# 检查设备连接
adb devices

# 一键构建并安装
.\gradlew.bat installDebug

# 启动应用
adb shell am start -n com.chordieapp/.MainActivity
```

### 3. 验证运行
应用成功启动后，您应该看到：
- "ChordieApp"标题
- "Current Chord" 显示区域
- 钢琴键盘可视化
- "Connect MIDI" 和 "Test Audio" 按钮
- MIDI和Audio状态指示器

### 4. 测试功能
1. **测试音频**：点击"Test Audio"按钮，应该听到C大调和弦
2. **连接MIDI**：点击"Connect MIDI"按钮连接MIDI设备
3. **演奏测试**：如果有MIDI键盘，演奏时界面会实时显示和弦

## 常见问题快速解决

❌ **"adb不是内部或外部命令"**
- 安装Android Studio并确保ADB在PATH中

❌ **"no devices/emulators found"**
- 检查USB连接和调试权限

❌ **构建失败**
- 确保网络连接正常（需要下载依赖）
- 运行：`.\gradlew.bat clean` 然后重试

✅ **成功标志**
- 手机上出现ChordieApp图标
- 应用可以正常打开并显示界面
