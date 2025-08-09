package com.midichordmaster

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import kotlinx.coroutines.*
import kotlin.math.*

class AudioSynthesizer {
    
    private val sampleRate = 44100
    private val bufferSize = AudioTrack.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_OUT_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    ).coerceAtLeast(1024)
    
    private var audioTrack: AudioTrack? = null
    private val activeNotes = mutableMapOf<Int, NoteData>()
    private var synthesisJob: Job? = null
    private var isPlaying = false
    private var isInitialized = false
    
    data class NoteData(
        val frequency: Double,
        val velocity: Int,
        var phase: Double = 0.0,
        var envelope: Double = 1.0
    )
    
    init {
        initializeAudio()
    }
    
    private fun initializeAudio() {
        try {
            println("DEBUG: Starting AudioSynthesizer initialization...")
            println("DEBUG: Sample rate: $sampleRate, Buffer size: $bufferSize")
            
            // Check if audio is available
            if (sampleRate <= 0 || bufferSize <= 0) {
                throw IllegalStateException("Invalid audio parameters: sampleRate=$sampleRate, bufferSize=$bufferSize")
            }
            
            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize * 2)
                .build()
                
            println("DEBUG: AudioTrack created successfully")
            println("DEBUG: AudioTrack state: ${audioTrack?.state}")
            
            // Check if AudioTrack was created successfully
            if (audioTrack?.state != AudioTrack.STATE_INITIALIZED) {
                throw IllegalStateException("AudioTrack initialization failed. State: ${audioTrack?.state}")
            }
            
            println("DEBUG: AudioTrack playback state: ${audioTrack?.playState}")
            
            audioTrack?.play()
            println("DEBUG: AudioTrack play() called")
            println("DEBUG: AudioTrack playback state after play(): ${audioTrack?.playState}")
            
            startSynthesis()
            isInitialized = true
            println("SUCCESS: AudioSynthesizer initialized successfully")
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
            val buffer = ShortArray(bufferSize)
            
            while (isActive && isInitialized) {
                try {
                    if (activeNotes.isNotEmpty()) {
                        generateAudioBuffer(buffer)
                        audioTrack?.write(buffer, 0, buffer.size)
                    } else {
                        // Generate silence when no notes are playing
                        buffer.fill(0)
                        audioTrack?.write(buffer, 0, buffer.size)
                    }
                    
                    delay(10) // Small delay to prevent excessive CPU usage
                } catch (e: Exception) {
                    println("ERROR: Audio synthesis error: ${e.message}")
                    break
                }
            }
        }
    }
    
    private fun generateAudioBuffer(buffer: ShortArray) {
        val frameTime = 1.0 / sampleRate
        
        // Clear buffer first
        buffer.fill(0)
        
        if (activeNotes.isEmpty()) return
        
        // Limit max active notes to prevent memory issues
        val maxActiveNotes = 8
        val notesToProcess = if (activeNotes.size > maxActiveNotes) {
            // Keep the most recently added notes
            activeNotes.entries.sortedByDescending { it.value.envelope }
                .take(maxActiveNotes)
                .associate { it.key to it.value }
        } else {
            activeNotes
        }
        
        for (i in buffer.indices) {
            var sample = 0.0
            
            // Mix active notes with volume limiting
            notesToProcess.values.forEach { note ->
                val amplitude = (note.velocity / 127.0) * note.envelope * 0.08 // Reduced volume
                
                // Simpler waveform to reduce CPU usage
                val waveform = sin(note.phase) + sin(note.phase * 2) * 0.3
                sample += waveform * amplitude
                
                // Update phase
                note.phase += 2 * PI * note.frequency * frameTime
                if (note.phase > 2 * PI) {
                    note.phase -= 2 * PI
                }
                
                // Faster envelope decay to free memory sooner
                note.envelope *= 0.9995
            }
            
            // Soft limiting to prevent clipping
            sample = sample.coerceIn(-0.8, 0.8)
            buffer[i] = (sample * Short.MAX_VALUE).toInt().toShort()
        }
        
        // Remove notes with very low envelope (memory cleanup)
        activeNotes.entries.removeAll { it.value.envelope < 0.01 }
        
        // Emergency cleanup if too many notes
        if (activeNotes.size > 12) {
            val notesToRemove = activeNotes.entries
                .sortedBy { it.value.envelope }
                .take(activeNotes.size - 8)
            notesToRemove.forEach { activeNotes.remove(it.key) }
            println("DEBUG: Emergency note cleanup - removed ${notesToRemove.size} notes")
        }
    }
    
    fun playNote(midiNote: Int, velocity: Int) {
        println("DEBUG: playNote called - note: $midiNote, velocity: $velocity")
        println("DEBUG: isInitialized: $isInitialized")
        println("DEBUG: audioTrack state: ${audioTrack?.state}")
        println("DEBUG: audioTrack playback state: ${audioTrack?.playState}")
        
        if (!isInitialized) {
            println("WARNING: AudioSynthesizer not initialized, cannot play note $midiNote")
            return
        }
        
        if (audioTrack == null) {
            println("ERROR: AudioTrack is null!")
            return
        }
        
        try {
            val frequency = midiNoteToFrequency(midiNote)
            activeNotes[midiNote] = NoteData(frequency, velocity)
            isPlaying = true
            println("SUCCESS: Added note $midiNote at frequency $frequency Hz to active notes")
            println("DEBUG: Active notes count: ${activeNotes.size}")
        } catch (e: Exception) {
            println("ERROR: Failed to play note $midiNote: ${e.message}")
            e.printStackTrace()
        }
    }
    
    fun stopNote(midiNote: Int) {
        if (!isInitialized) return
        
        try {
            activeNotes.remove(midiNote)
            if (activeNotes.isEmpty()) {
                isPlaying = false
            }
            println("DEBUG: Stopped note $midiNote")
        } catch (e: Exception) {
            println("ERROR: Failed to stop note $midiNote: ${e.message}")
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
