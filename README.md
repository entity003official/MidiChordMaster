# 🎹 MidiChordMaster - Android和弦显示应用

一个专业的Android MIDI和弦识别应用，可以实时接收MIDI信号，智能识别和显示和弦，并播放高质量钢琴音色。

![Android](https://img.shields.io/badge/Android-API%2024+-brightgreen)
![Kotlin](https://img.shields.io/badge/Kotlin-1.8.20-blue)
![Compose](https://img.shields.io/badge/Jetpack%20Compose-UI-orange)
![License](https://img.shields.io/badge/License-MIT-yellow)

## ✨ 功能特性

- 🎹 **实时MIDI信号接收**: 支持USB MIDI设备和蓝牙MIDI连接
- 🎵 **智能和弦识别**: 自动分析并显示当前演奏的和弦
- 🔊 **钢琴音色合成**: 内置高质量钢琴音色，实时播放接收到的音符
- 📱 **现代化界面**: 使用Jetpack Compose构建的响应式用户界面
- 🎯 **可视化钢琴**: 实时显示按下的琴键

## 🎮 支持的和弦类型

- **基础三和弦**: Major, Minor, Diminished, Augmented
- **七和弦**: Major 7, Minor 7, Dominant 7
- **扩展和弦**: add9, 9th, 11th, 13th
- **挂留和弦**: Sus2, Sus4
- **强力和弦**: Power chord (5)

## 🚀 快速开始

### 环境要求
- Android Studio Flamingo (2022.2.1) 或更新版本
- Android SDK API 24 (Android 7.0) 或更高
- Java 11+ 或 Kotlin 1.8+

### 安装步骤

1. **克隆仓库**
   ```bash
   git clone https://github.com/entity003official/MidiChordMaster.git
   cd MidiChordMaster
   ```

2. **在Android Studio中打开项目**
   - 启动Android Studio
   - 选择 "Open an existing project"
   - 选择MidiChordMaster文件夹

3. **运行应用**
   - 连接Android设备或启动模拟器
   - 点击绿色运行按钮或按 `Ctrl+R`

## 🎯 使用说明

1. **权限设置**: 首次启动时允许蓝牙和音频权限
2. **连接MIDI设备**: 点击"Connect MIDI"按钮
3. **测试功能**: 点击"Test Audio"播放测试和弦
4. **开始演奏**: 连接MIDI键盘后，界面会实时显示和弦

## 🏗️ 技术架构

- **语言**: Kotlin
- **UI框架**: Jetpack Compose
- **架构**: MVVM + StateFlow
- **MIDI处理**: Android MIDI API
- **音频合成**: 自定义AudioTrack合成器

## 📁 项目结构

```
app/src/main/java/com/chordieapp/
├── MainActivity.kt              # 主活动和权限处理
├── ChordDisplayScreen.kt        # 主界面Compose组件
├── ChordDisplayViewModel.kt     # 状态管理
├── MidiManager.kt              # MIDI设备连接和消息处理
├── AudioSynthesizer.kt         # 实时音频合成
├── ChordAnalyzer.kt            # 和弦识别算法
├── MidiEvent.kt               # MIDI事件数据类
└── ui/theme/                  # UI主题配置
```

## 🛠️ 开发指南

### 核心组件说明

- **MidiManager**: 处理MIDI设备连接和消息解析
- **AudioSynthesizer**: 实时音频合成引擎，支持多音符播放
- **ChordAnalyzer**: 智能和弦分析算法，支持各种和弦类型
- **ChordDisplayViewModel**: 状态管理和业务逻辑

### 代码规范
- 使用Kotlin官方代码风格
- 遵循Android开发最佳实践
- 使用MVVM架构模式
- 优先使用StateFlow进行响应式编程

## 🐛 故障排除

### MIDI连接问题
- 确保USB MIDI设备支持Android
- 检查USB连接线是否支持数据传输
- 尝试重新连接设备

### 音频问题
- 检查设备音量设置
- 确保没有其他应用占用音频
- 重启应用重新初始化音频

## 🤝 贡献

欢迎提交Issue和Pull Request！

1. Fork 项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开 Pull Request

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 📞 联系方式

如有问题或建议，请创建 [Issue](https://github.com/your-username/```
MidiChordMaster/issues)。

---

⭐ 如果这个项目对您有帮助，请给一个星标！
  - 减和弦 (Diminished, Diminished 7)
  - 增和弦 (Augmented)
  - 挂留和弦 (Sus2, Sus4)
  - 扩展和弦 (9th, 11th, 13th)
  - 强力和弦 (Power chords)
- 🎼 **钢琴键盘显示**: 实时显示按下的琴键
- 🔊 **钢琴音色合成**: 内置钢琴音色生成器
- 📱 **现代UI设计**: 使用Jetpack Compose构建的响应式界面

## 技术栈

- **语言**: Kotlin
- **UI框架**: Jetpack Compose
- **MIDI处理**: Android MIDI API
- **音频合成**: AudioTrack + 自定义合成器
- **架构**: MVVM with StateFlow

## 系统要求

- Android 7.0 (API Level 24) 或更高版本
- 支持MIDI功能 (推荐)
- 音频输出设备

## 权限要求

- `BLUETOOTH` 和 `BLUETOOTH_CONNECT`: 用于蓝牙MIDI设备连接
- `RECORD_AUDIO`: 用于音频处理
- `MODIFY_AUDIO_SETTINGS`: 用于音频设置调整

## 安装和使用

1. **克隆项目**:
   ```bash
   git clone <repository-url>
   cd ChordieApp
   ```

2. **在Android Studio中打开项目**

3. **构建和运行**:
   ```bash
   ./gradlew assembleDebug
   ```

4. **连接MIDI设备**:
   - 通过USB连接MIDI键盘
   - 或配对蓝牙MIDI设备
   - 在应用中点击"Connect MIDI"

5. **开始演奏**:
   - 在连接的MIDI设备上弹奏和弦
   - 应用将实时显示识别的和弦名称
   - 同时播放对应的钢琴音色

## 项目结构

```
app/src/main/java/com/chordieapp/
├── MainActivity.kt              # 主Activity
├── ChordDisplayScreen.kt        # 主界面UI
├── ChordDisplayViewModel.kt     # 视图模型
├── MidiManager.kt               # MIDI信号处理
├── AudioSynthesizer.kt          # 音频合成器
├── ChordAnalyzer.kt             # 和弦分析引擎
├── MidiEvent.kt                 # MIDI事件数据类
└── ui/theme/                    # 主题和样式
```

## 核心类说明

### MidiManager
- 处理MIDI设备连接和断开
- 解析MIDI消息 (Note On/Off, Control Change等)
- 将MIDI事件转换为应用内部事件

### AudioSynthesizer
- 实时音频合成
- 多音符同时播放支持
- 钢琴音色模拟 (基础波形 + 谐波)
- 音符包络控制

### ChordAnalyzer
- 和弦模式识别
- 支持转位识别
- 音程分析
- 实时和弦名称生成

### ChordDisplayViewModel
- 管理应用状态
- 协调MIDI输入和音频输出
- 响应式UI状态更新

## 开发计划

- [ ] 添加更多和弦类型支持
- [ ] 实现和弦进行录制功能
- [ ] 添加节拍器功能
- [ ] 支持不同音色选择
- [ ] 添加和弦学习模式
- [ ] 实现MIDI文件导入/导出

## 贡献

欢迎提交Issue和Pull Request来改进这个项目！

## 许可证

本项目采用MIT许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 联系方式

如有问题或建议，请创建Issue或联系开发者。
