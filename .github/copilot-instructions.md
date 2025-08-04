<!-- Use this file to provide workspace-specific custom instructions to Copilot. For more details, visit https://code.visualstudio.com/docs/copilot/copilot-customization#_use-a-githubcopilotinstructionsmd-file -->

# ChordieApp开发指南

这是一个Android和弦显示应用项目，使用Kotlin和Jetpack Compose开发。

## 项目特点

- **MIDI支持**: 实时接收和处理MIDI信号
- **音频合成**: 自定义钢琴音色合成器
- **和弦识别**: 智能和弦分析算法
- **现代UI**: 使用Jetpack Compose构建响应式界面

## 代码规范

- 使用Kotlin官方代码风格
- 遵循Android开发最佳实践
- 使用MVVM架构模式
- 优先使用StateFlow进行响应式编程

## 关键组件

1. **MidiManager**: 处理MIDI设备连接和消息解析
2. **AudioSynthesizer**: 实时音频合成和播放
3. **ChordAnalyzer**: 和弦识别和分析
4. **ChordDisplayViewModel**: 状态管理和业务逻辑

## 开发注意事项

- 确保音频处理在后台线程进行
- MIDI消息处理需要实时响应
- UI更新使用Compose的状态管理
- 处理Android权限请求流程
