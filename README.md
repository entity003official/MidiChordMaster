# MidiChordMaster

🎹 **专业级Android MIDI和弦识别应用**

一个功能强大的Android应用，能够实时处理MIDI输入信号，识别和弦，并提供可视化的钢琴键盘显示。

## ✨ 主要功能

- 🎵 **实时MIDI处理** - 支持USB和蓝牙MIDI设备
- 🎼 **智能和弦识别** - 自动识别主要和弦类型
- 🎹 **可视化钢琴键盘** - 实时显示按下的键位
- 🔊 **音频合成** - 内置钢琴音色合成器
- 📱 **现代化界面** - 使用Jetpack Compose构建
- 🎨 **Material Design** - 支持深色/浅色主题

## 📱 系统要求

- Android 7.0+ (API 24)
- 支持MIDI的Android设备
- 音频权限和MIDI权限

## 🏗️ 技术架构

- **UI框架**: Jetpack Compose with Material 2
- **架构模式**: MVVM with StateFlow
- **开发语言**: Kotlin
- **构建工具**: Gradle 7.4.2
- **最低SDK**: API 24 (Android 7.0)
- **目标SDK**: API 34 (Android 14)

## 📦 核心组件

### MidiManager
- MIDI设备连接和管理
- MIDI消息解析和路由
- 设备自动检测

### AudioSynthesizer
- 实时音频合成
- 自定义钢琴音色
- 低延迟音频处理

### ChordAnalyzer
- 和弦识别算法
- 音乐理论分析
- 实时和弦检测

### ChordDisplayViewModel
- 状态管理
- 响应式数据流
- UI业务逻辑

## 🚀 快速开始

### 构建APK

1. **前提条件**
   ```bash
   # 确保安装了JDK 11
   java -version
   ```

2. **克隆项目**
   ```bash
   git clone https://github.com/yourusername/MidiChordMaster.git
   cd MidiChordMaster
   ```

3. **构建APK**
   ```bash
   # Windows
   .\build_midi_api_fixed.bat
   
   # 或使用Gradle命令
   .\gradlew assembleDebug
   ```

4. **安装APK**
   - APK位置: `app/build/outputs/apk/debug/app-debug.apk`
   - 传输到Android设备并安装

### 使用Android Studio

1. 打开Android Studio
2. 选择 "Open existing project"
3. 选择项目文件夹
4. 等待同步完成
5. 点击 Run 按钮

## 📖 使用说明

1. **安装应用**后，授予音频和MIDI权限
2. **连接MIDI设备** - 支持USB或蓝牙MIDI键盘
3. **开始演奏** - 应用会实时显示和弦名称
4. **可视化反馈** - 钢琴键盘显示按下的键位
5. **测试功能** - 使用内置测试按钮验证功能

## 🎯 功能演示

- **实时和弦识别**: 演奏C大调和弦，应用显示"C Major"
- **键位可视化**: 按下的键在虚拟钢琴上高亮显示
- **设备状态**: 实时显示MIDI连接和音频状态
- **响应式UI**: 支持不同屏幕尺寸和方向

## 🛠️ 开发环境

### 依赖配置

```groovy
dependencies {
    implementation 'androidx.core:core-ktx:1.9.0'
    implementation 'androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2'
    implementation 'androidx.activity:activity-compose:1.7.2'
    implementation platform('androidx.compose:compose-bom:2022.10.00')
    implementation 'androidx.compose.material:material'
    implementation 'androidx.appcompat:appcompat:1.6.1'
}
```

### 权限要求

```xml
<uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS" />
<uses-feature android:name="android.software.midi" android:required="false" />
```

## 🎼 和弦识别算法

应用能识别以下和弦类型：
- 大三和弦 (Major)
- 小三和弦 (Minor)
- 减三和弦 (Diminished)
- 增三和弦 (Augmented)
- 属七和弦 (Dominant 7th)
- 大七和弦 (Major 7th)
- 小七和弦 (Minor 7th)

## 🔧 故障排除

### 常见问题

**Q: 应用无法检测到MIDI设备**
A: 确保设备支持USB Host模式，并授予相关权限

**Q: 没有声音输出**
A: 检查音频权限和设备音量设置

**Q: 和弦识别不准确**
A: 确保同时按下和弦的所有音符

### 构建问题

如果遇到构建错误：
1. 检查JDK版本 (需要JDK 11+)
2. 清理项目缓存: `.\gradlew clean`
3. 确保网络连接正常 (下载依赖)

## 📄 许可证

MIT License - 详见 [LICENSE](LICENSE) 文件

## 🤝 贡献

欢迎提交Issue和Pull Request！

1. Fork项目
2. 创建特性分支: `git checkout -b feature/AmazingFeature`
3. 提交更改: `git commit -m 'Add some AmazingFeature'`
4. 推送分支: `git push origin feature/AmazingFeature`
5. 提交Pull Request

## 👨‍💻 作者

- GitHub: [@yourusername](https://github.com/yourusername)

## 📱 应用截图

_TODO: 添加应用截图展示主要功能_

## 🔮 未来计划

- [ ] 更多和弦类型支持
- [ ] 和弦进行记录功能
- [ ] MIDI文件导入导出
- [ ] 网络MIDI支持
- [ ] 自定义音色库
- [ ] 和弦学习模式

---

**享受音乐创作的乐趣！** 🎵
