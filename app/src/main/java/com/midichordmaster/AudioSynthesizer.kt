package com.midichordmaster

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import kotlinx.coroutines.*
import kotlin.math.*

class AudioSynthesizer {
    
    // Optimized for low latency - inspired by mgenner-droid
    private val sampleRate = 44100
    private val bufferSize = AudioTrack.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_OUT_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    ).coerceAtLeast(1024) // Ensure minimum viable buffer size
    
    private var audioTrack: AudioTrack? = null
    private val activeNotes = mutableMapOf<Int, NoteData>()
    private var synthesisJob: Job? = null
    private var isPlaying = false
    private var isInitialized = false
    private var latencyDebugEnabled = true
    
    // Use smaller render buffer like mgenner-droid (128 samples)
    private val renderBufferSize = 256 // Small buffer for low latency
    
    data class NoteData(
        val frequency: Double,
        val velocity: Int,
        var phase: Double = 0.0,
        var envelope: Double = 1.0,
        var startTime: Long = System.nanoTime() // For latency measurement
    )
    
    init {
        initializeAudio()
    }
    
    private fun initializeAudio() {
        val startTime = System.nanoTime()
        try {
            println("🔧 Starting AudioSynthesizer initialization...")
            println("🔧 Sample rate: $sampleRate, Optimized buffer size: $bufferSize")
            
            // Check if audio is available
            if (sampleRate <= 0 || bufferSize <= 0) {
                throw IllegalStateException("Invalid audio parameters: sampleRate=$sampleRate, bufferSize=$bufferSize")
            }
            
            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setFlags(AudioAttributes.FLAG_LOW_LATENCY) // Enable low latency mode
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize) // Use smaller buffer
                .setPerformanceMode(AudioTrack.PERFORMANCE_MODE_LOW_LATENCY) // Enable low latency performance
                .build()
                
            println("✅ AudioTrack created successfully")
            println("🔧 AudioTrack state: ${audioTrack?.state}")
            
            // Check if AudioTrack was created successfully
            if (audioTrack?.state != AudioTrack.STATE_INITIALIZED) {
                throw IllegalStateException("AudioTrack initialization failed. State: ${audioTrack?.state}")
            }
            
            println("🔧 AudioTrack playback state: ${audioTrack?.playState}")
            
            audioTrack?.play()
            println("✅ AudioTrack play() called")
            println("🔧 AudioTrack playback state after play(): ${audioTrack?.playState}")
            
            startSynthesis()
            isInitialized = true
            
            val initTime = (System.nanoTime() - startTime) / 1_000_000.0
            println("✅ AudioSynthesizer initialized successfully in ${String.format("%.2f", initTime)}ms")
        } catch (e: Exception) {
            println("ERROR: Failed to initialize AudioSynthesizer: ${e.message}")
            println("ERROR: Exception type: ${e.javaClass.simpleName}")
            e.printStackTrace()
            
            // Clean up any partially created resources
            try {
                audioTrack?.release()
            } catch (cleanupException: Exception) {
                println("ERROR: Failed to cleanup AudioTrack: ${cleanupException.message}")
            }
            audioTrack = null
            isInitialized = false
        }
    }
    
    private fun startSynthesis() {
        synthesisJob = CoroutineScope(Dispatchers.Default).launch {
            // Use small buffer like mgenner-droid
            val buffer = ShortArray(renderBufferSize)
            
            println("🎵 Audio synthesis started with buffer size: ${buffer.size}")
            
            while (isActive && isInitialized) {
                try {
                    val synthesisStart = System.nanoTime()
                    
                    // Always generate audio to keep the stream flowing
                    generateAudioBuffer(buffer)
                    
                    // Write to AudioTrack
                    val writeResult = audioTrack?.write(buffer, 0, buffer.size) ?: -1
                    
                    if (writeResult < 0) {
                        println("❌ AudioTrack write failed: $writeResult")
                        delay(10) // Wait before retrying
                        continue
                    }
                    
                    if (latencyDebugEnabled && activeNotes.isNotEmpty()) {
                        val synthesisTime = (System.nanoTime() - synthesisStart) / 1_000_000.0
                        if (synthesisTime > 3.0) { // Only log if synthesis takes more than 3ms
                            println("🎯 Audio synthesis: ${String.format("%.2f", synthesisTime)}ms")
                        }
                    }
                    
                    // No delay - continuous audio streaming like mgenner-droid
                } catch (e: Exception) {
                    println("❌ Audio synthesis error: ${e.message}")
                    delay(10) // Brief pause before retry
                }
            }
            
            println("🛑 Audio synthesis stopped")
        }
    }
    
    private fun generateAudioBuffer(buffer: ShortArray) {
        val frameTime = 1.0 / sampleRate
        
        // Always clear buffer first
        buffer.fill(0)
        
        // Generate audio even if no notes (silence)
        if (activeNotes.isEmpty()) {
            return // Silent buffer already filled with zeros
        }
        
        // Limit max active notes to prevent performance issues
        val maxActiveNotes = 6
        val notesToProcess = if (activeNotes.size > maxActiveNotes) {
            // Keep the most recently added notes
            activeNotes.entries.sortedByDescending { it.value.startTime }
                .take(maxActiveNotes)
                .associate { it.key to it.value }
        } else {
            activeNotes
        }
        
        for (i in buffer.indices) {
            var sample = 0.0
            
            // Mix active notes
            notesToProcess.values.forEach { note ->
                // Simple piano-like envelope
                val amplitude = (note.velocity / 127.0) * note.envelope * 0.15 // Increased volume
                
                // Simple sine wave with harmonic for richer sound
                val fundamental = sin(note.phase)
                val harmonic = sin(note.phase * 2) * 0.2
                val waveform = fundamental + harmonic
                
                sample += waveform * amplitude
                
                // Update phase
                note.phase += 2 * PI * note.frequency * frameTime
                if (note.phase > 2 * PI) {
                    note.phase -= 2 * PI
                }
                
                // Natural envelope decay
                note.envelope *= 0.9998 // Slower decay for longer sustain
            }
            
            // Soft limiting to prevent clipping
            sample = sample.coerceIn(-0.9, 0.9)
            buffer[i] = (sample * Short.MAX_VALUE * 0.8).toInt().toShort() // Slightly reduced to prevent distortion
        }
        
        // Remove notes with very low envelope (memory cleanup)
        val notesToRemove = activeNotes.entries.filter { it.value.envelope < 0.005 }
        notesToRemove.forEach { 
            activeNotes.remove(it.key)
            if (latencyDebugEnabled) {
                println("🧹 Removed quiet note ${it.key}")
            }
        }
        
        // Emergency cleanup if too many notes
        if (activeNotes.size > 10) {
            val oldestNotes = activeNotes.entries
                .sortedBy { it.value.startTime }
                .take(activeNotes.size - 6)
            oldestNotes.forEach { activeNotes.remove(it.key) }
            println("⚠️ Emergency cleanup - removed ${oldestNotes.size} oldest notes")
        }
    }
    
    fun playNote(midiNote: Int, velocity: Int) {
        val noteStartTime = System.nanoTime()
        
        if (latencyDebugEnabled) {
            println("🎹 playNote called - note: $midiNote, velocity: $velocity")
        }
        
        if (!isInitialized) {
            println("❌ AudioSynthesizer not initialized")
            return
        }
        
        if (audioTrack?.state != AudioTrack.STATE_INITIALIZED || 
            audioTrack?.playState != AudioTrack.PLAYSTATE_PLAYING) {
            println("❌ AudioTrack not ready - state: ${audioTrack?.state}, playState: ${audioTrack?.playState}")
            return
        }
        
        try {
            val frequency = midiNoteToFrequency(midiNote)
            val noteData = NoteData(
                frequency = frequency,
                velocity = velocity.coerceIn(1, 127),
                startTime = noteStartTime
            )
            
            activeNotes[midiNote] = noteData
            
            if (latencyDebugEnabled) {
                val processingTime = (System.nanoTime() - noteStartTime) / 1_000_000.0
                println("🎵 Note $midiNote added in ${String.format("%.2f", processingTime)}ms, active notes: ${activeNotes.size}")
            }
        } catch (e: Exception) {
            println("❌ Error playing note $midiNote: ${e.message}")
        }
    }
    
    fun stopNote(midiNote: Int) {
        val stopTime = System.nanoTime()
        
        if (latencyDebugEnabled) {
            println("🛑 stopNote called - note: $midiNote")
        }
        
        val removedNote = activeNotes.remove(midiNote)
        
        if (latencyDebugEnabled && removedNote != null) {
            val noteDuration = (stopTime - removedNote.startTime) / 1_000_000.0
            println("🎵 Note $midiNote stopped after ${String.format("%.2f", noteDuration)}ms, remaining: ${activeNotes.size}")
        }
    }
    
    fun playChord(notes: Set<Int>, velocity: Int) {
        notes.forEach { note ->
            playNote(note, velocity)
        }
    }
    
    fun stopAllNotes() {
        activeNotes.clear()
        isPlaying = false
    }
    
    private fun midiNoteToFrequency(midiNote: Int): Double {
        // A4 (MIDI note 69) = 440 Hz
        return 440.0 * 2.0.pow((midiNote - 69) / 12.0)
    }
    
    fun getDebugInfo(): String {
        return buildString {
            appendLine("🔊 AudioSynthesizer 调试信息:")
            appendLine("- 初始化状态: $isInitialized")
            appendLine("- AudioTrack: ${if (audioTrack != null) "已创建" else "NULL"}")
            appendLine("- AudioTrack状态: ${audioTrack?.state}")
            appendLine("- 播放状态: ${audioTrack?.playState}")
            appendLine("- 采样率: $sampleRate Hz")
            appendLine("- 缓冲区大小: $bufferSize")
            appendLine("- 活跃音符数: ${activeNotes.size}")
            appendLine("- 正在播放: $isPlaying")
            appendLine("- 合成任务: ${if (synthesisJob?.isActive == true) "运行中" else "已停止"}")
            
            // 添加系统音频信息
            try {
                appendLine("- 系统音频状态: 可用")
                val minBufferSize = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )
                appendLine("- 最小缓冲区大小: $minBufferSize")
            } catch (e: Exception) {
                appendLine("- 系统音频状态: 错误 - ${e.message}")
            }
            
            // 活跃音符详情
            if (activeNotes.isNotEmpty()) {
                appendLine("- 活跃音符详情:")
                activeNotes.forEach { (note, data) ->
                    appendLine("  * 音符$note: 频率${data.frequency}Hz, 音量${data.velocity}, 包络${String.format("%.3f", data.envelope)}")
                }
            }
        }
    }
    
    fun toggleLatencyDebug(): Boolean {
        latencyDebugEnabled = !latencyDebugEnabled
        println("🔧 Latency debugging: ${if (latencyDebugEnabled) "ENABLED" else "DISABLED"}")
        return latencyDebugEnabled
    }
    
    fun getLatencyInfo(): String {
        return buildString {
            appendLine("🔧 音频延迟信息:")
            appendLine("- 采样率: ${sampleRate}Hz")
            appendLine("- 缓冲区大小: $bufferSize 采样")
            appendLine("- 理论延迟: ${String.format("%.2f", (bufferSize.toDouble() / sampleRate) * 1000)}ms")
            appendLine("- 延迟调试: ${if (latencyDebugEnabled) "启用" else "禁用"}")
            appendLine("- 音频模式: 低延迟优化")
            if (activeNotes.isNotEmpty()) {
                appendLine("- 活跃音符详情:")
                activeNotes.forEach { (note, data) ->
                    val noteAge = (System.nanoTime() - data.startTime) / 1_000_000.0
                    appendLine("  * 音符$note: 频率${String.format("%.1f", data.frequency)}Hz, 持续${String.format("%.1f", noteAge)}ms")
                }
            }
        }
    }
    
    fun release() {
        try {
            synthesisJob?.cancel()
            audioTrack?.stop()
            audioTrack?.release()
            isInitialized = false
            println("DEBUG: AudioSynthesizer released")
        } catch (e: Exception) {
            println("ERROR: Failed to release AudioSynthesizer: ${e.message}")
        }
    }
}
