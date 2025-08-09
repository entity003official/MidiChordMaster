package com.midichordmaster

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.Button
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.midichordmaster.ui.theme.MidiChordMasterTheme

class MainActivity : ComponentActivity() {
    
    companion object {
        private const val TAG = "MidiChordMaster_DEBUG"
        private const val DEBUG_MODE = true // 调试模式开关
    }
    
    // 调试信息状态
    private var debugLogs = mutableStateListOf<String>()
    private var isInitialized = mutableStateOf(false)
    private var showDebugScreen = mutableStateOf(true) // 默认显示调试界面
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        debugLog("权限请求结果: $permissions")
        
        // Handle permission results
        val bluetoothGranted = permissions[Manifest.permission.BLUETOOTH_CONNECT] ?: false
        val audioGranted = permissions[Manifest.permission.RECORD_AUDIO] ?: false
        
        debugLog("蓝牙权限: $bluetoothGranted, 音频权限: $audioGranted")
        
        if (bluetoothGranted || Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            // Initialize MIDI and audio components
            initializeMidiAndAudio()
        } else {
            debugLog("权限被拒绝，但继续初始化（仅限基本功能）")
            initializeMidiAndAudio()
        }
    }
    
    private fun debugLog(message: String) {
        val timestamp = java.text.SimpleDateFormat("HH:mm:ss.SSS", java.util.Locale.getDefault()).format(java.util.Date())
        val logMessage = "[$timestamp] $message"
        
        // 添加到调试日志列表
        debugLogs.add(logMessage)
        if (debugLogs.size > 100) {
            debugLogs.removeAt(0) // 保持最多100条日志
        }
        
        // 输出到系统日志
        Log.d(TAG, logMessage)
        println("DEBUG: $logMessage")
        
        // 显示Toast（仅关键信息）
        if (message.contains("ERROR") || message.contains("失败")) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        debugLog("🚀 应用启动开始")
        debugLog("Android版本: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
        debugLog("设备信息: ${Build.MANUFACTURER} ${Build.MODEL}")
        
        try {
            debugLog("开始设置全屏显示...")
            
            // Enable edge-to-edge display
            WindowCompat.setDecorFitsSystemWindows(window, false)
            
            // Hide system bars for immersive experience
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                window.insetsController?.let { controller ->
                    controller.hide(WindowInsets.Type.systemBars())
                    controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                }
                debugLog("✅ 使用Android 11+全屏API")
            } else {
                @Suppress("DEPRECATION")
                window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                )
                debugLog("✅ 使用传统全屏API")
            }
            
            // Keep screen on during use
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            debugLog("✅ 设置屏幕常亮")
            
            debugLog("开始设置UI...")
            
            // Set content with debug screen
            setContent {
                MidiChordMasterTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colors.background
                    ) {
                        if (showDebugScreen.value) {
                            DebugScreen()
                        } else {
                            ChordDisplayScreen(viewModel = getChordDisplayViewModel())
                        }
                    }
                }
            }
            
            debugLog("✅ UI设置完成")
            
            // Initialize components after UI is ready
            debugLog("开始初始化组件...")
            initializeMidiAndAudio()
            
            // Check and request permissions
            debugLog("开始检查权限...")
            checkPermissions()
            
            debugLog("✅ MainActivity onCreate() 完成")
        } catch (e: Exception) {
            debugLog("❌ ERROR: onCreate() 失败: ${e.message}")
            debugLog("❌ 异常类型: ${e.javaClass.simpleName}")
            e.printStackTrace()
            
            // Show error screen
            setContent {
                MidiChordMasterTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = Color.Red.copy(alpha = 0.1f)
                    ) {
                        ErrorScreen(e.message ?: "未知错误")
                    }
                }
            }
        }
    }
    
    @Composable
    private fun DebugScreen() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "🔧 MidiChordMaster 调试控制台",
                fontSize = 20.sp,
                color = Color.Green,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // 系统信息
            Text(
                text = "📱 系统信息:",
                fontSize = 16.sp,
                color = Color.Blue,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})\n" +
                      "${Build.MANUFACTURER} ${Build.MODEL}\n" +
                      "内存: ${getAvailableMemory()}MB",
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // 组件状态
            Text(
                text = "⚙️ 组件状态:",
                fontSize = 16.sp,
                color = Color.Blue,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "MidiManager: ${if (midiManager != null) "✅ 已初始化" else "❌ 未初始化"}\n" +
                      "AudioSynthesizer: ${if (audioSynthesizer != null) "✅ 已初始化" else "❌ 未初始化"}\n" +
                      "ChordAnalyzer: ${if (chordAnalyzer != null) "✅ 已初始化" else "❌ 未初始化"}",
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // 控制按钮
            Button(
                onClick = {
                    debugLog("用户点击：切换到正常界面")
                    showDebugScreen.value = false
                },
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Text("🎹 切换到正常界面")
            }
            
            Button(
                onClick = {
                    debugLog("用户点击：重新初始化组件")
                    initializeMidiAndAudio()
                },
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Text("🔄 重新初始化组件")
            }
            
            Button(
                onClick = {
                    debugLog("用户点击：测试音频")
                    testAudio()
                },
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Text("🔊 测试音频")
            }
            
            // 调试日志
            Text(
                text = "📋 调试日志:",
                fontSize = 16.sp,
                color = Color.Blue,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                Text(
                    text = debugLogs.takeLast(20).joinToString("\n"),
                    fontSize = 10.sp,
                    color = Color.Black,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
    
    @Composable
    private fun ErrorScreen(errorMessage: String) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "❌ 应用崩溃了",
                fontSize = 24.sp,
                color = Color.Red,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Text(
                text = "错误信息:",
                fontSize = 16.sp,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Text(
                text = errorMessage,
                fontSize = 14.sp,
                color = Color.Red,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Text(
                text = "调试日志:",
                fontSize = 16.sp,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Text(
                text = debugLogs.joinToString("\n"),
                fontSize = 10.sp,
                color = Color.Black,
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            )
        }
    }
    
    private fun getAvailableMemory(): Long {
        val runtime = Runtime.getRuntime()
        return (runtime.maxMemory() - runtime.totalMemory() + runtime.freeMemory()) / (1024 * 1024)
    }
    
    private fun testAudio() {
        try {
            debugLog("🔊 开始测试音频...")
            audioSynthesizer?.let { synth ->
                debugLog("✅ AudioSynthesizer可用，播放测试音符")
                synth.playNote(60, 100) // 播放C4音符
                // 2秒后停止
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    synth.stopNote(60)
                    debugLog("✅ 音频测试完成")
                }, 2000)
            } ?: run {
                debugLog("❌ AudioSynthesizer不可用")
            }
        } catch (e: Exception) {
            debugLog("❌ 音频测试失败: ${e.message}")
        }
    }
    private fun checkPermissions() {
        debugLog("🔐 开始检查权限...")
        val permissionsNeeded = mutableListOf<String>()
        
        // Only request BLUETOOTH_CONNECT for MIDI devices
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) 
                != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.BLUETOOTH_CONNECT)
                debugLog("需要蓝牙权限")
            } else {
                debugLog("✅ 蓝牙权限已授予")
            }
        } else {
            debugLog("✅ Android版本低于12，无需蓝牙权限")
        }
        
        // Check RECORD_AUDIO for completeness (though not strictly needed for AudioTrack)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) 
            != PackageManager.PERMISSION_GRANTED) {
            debugLog("录音权限未授予（可选）")
        } else {
            debugLog("✅ 录音权限已授予")
        }
        
        if (permissionsNeeded.isNotEmpty()) {
            debugLog("请求权限: ${permissionsNeeded.joinToString()}")
            requestPermissionLauncher.launch(permissionsNeeded.toTypedArray())
        } else {
            debugLog("✅ 所有必要权限已授予")
            // Already initialized in onCreate, no need to call again
        }
    }
    
    private fun initializeMidiAndAudio() {
        debugLog("⚙️ 开始初始化组件...")
        
        try {
            // Initialize MidiManager
            midiManager = try {
                debugLog("初始化MidiManager...")
                MidiManager(this).also {
                    debugLog("✅ MidiManager初始化成功")
                }
            } catch (e: Exception) {
                debugLog("❌ MidiManager初始化失败: ${e.message}")
                e.printStackTrace()
                null
            }
            
            // Initialize AudioSynthesizer
            audioSynthesizer = try {
                debugLog("初始化AudioSynthesizer...")
                AudioSynthesizer().also { synth ->
                    debugLog("✅ AudioSynthesizer初始化成功")
                    // Get debug info
                    try {
                        val info = synth.getDebugInfo()
                        debugLog("AudioSynthesizer状态: $info")
                    } catch (e: Exception) {
                        debugLog("获取AudioSynthesizer状态失败: ${e.message}")
                    }
                }
            } catch (e: Exception) {
                debugLog("❌ AudioSynthesizer初始化失败: ${e.message}")
                e.printStackTrace()
                null
            }
            
            // Initialize ChordAnalyzer
            chordAnalyzer = try {
                debugLog("初始化ChordAnalyzer...")
                ChordAnalyzer().also {
                    debugLog("✅ ChordAnalyzer初始化成功")
                }
            } catch (e: Exception) {
                debugLog("❌ ChordAnalyzer初始化失败: ${e.message}")
                e.printStackTrace()
                null
            }
            
            debugLog("✅ 组件初始化完成")
            isInitialized.value = true
            
        } catch (e: Exception) {
            debugLog("❌ 组件初始化过程发生错误: ${e.message}")
            e.printStackTrace()
        }
    }
    
    // Add these properties to the class
    private var midiManager: MidiManager? = null
    private var audioSynthesizer: AudioSynthesizer? = null
    private var chordAnalyzer: ChordAnalyzer? = null
    
    // Add method to get ViewModel with initialized components
    private fun getChordDisplayViewModel(): ChordDisplayViewModel {
        return try {
            debugLog("📱 创建ChordDisplayViewModel...")
            val viewModel = ChordDisplayViewModel()
            
            // Initialize ViewModel with components if available
            if (midiManager != null && audioSynthesizer != null && chordAnalyzer != null) {
                viewModel.initializeComponents(midiManager!!, audioSynthesizer!!, chordAnalyzer!!)
                debugLog("✅ ViewModel已使用所有组件初始化")
            } else {
                debugLog("⚠️ 某些组件为null，使用默认值初始化ViewModel")
                debugLog("组件状态: MidiManager=${midiManager != null}, AudioSynthesizer=${audioSynthesizer != null}, ChordAnalyzer=${chordAnalyzer != null}")
            }
            
            viewModel
        } catch (e: Exception) {
            debugLog("❌ 创建ViewModel失败: ${e.message}")
            e.printStackTrace()
            ChordDisplayViewModel() // Return empty ViewModel as fallback
        }
    }
}
