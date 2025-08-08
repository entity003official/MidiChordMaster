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
        
        for (i in buffer.indices) {
            var sample = 0.0
            
            // Mix all active notes
            activeNotes.values.forEach { note ->
                val amplitude = (note.velocity / 127.0) * note.envelope * 0.1 // Reduce volume
                
                // Generate piano-like tone using multiple harmonics
                val fundamental = sin(note.phase)
                val harmonic2 = sin(note.phase * 2) * 0.5
                val harmonic3 = sin(note.phase * 3) * 0.25
                val harmonic4 = sin(note.phase * 4) * 0.125
                
                val waveform = fundamental + harmonic2 + harmonic3 + harmonic4
                sample += waveform * amplitude
                
                // Update phase
                note.phase += 2 * PI * note.frequency * frameTime
                if (note.phase > 2 * PI) {
                    note.phase -= 2 * PI
                }
                
                // Apply envelope (simple decay)
                note.envelope *= 0.9999 // Slow decay
            }
            
            // Clamp and convert to 16-bit
            sample = sample.coerceIn(-1.0, 1.0)
            buffer[i] = (sample * Short.MAX_VALUE).toInt().toShort()
        }
        
        // Remove notes with very low envelope
        activeNotes.entries.removeAll { it.value.envelope < 0.001 }
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
            appendLine("AudioSynthesizer Debug Info:")
            appendLine("- Initialized: $isInitialized")
            appendLine("- AudioTrack: ${if (audioTrack != null) "Created" else "NULL"}")
            appendLine("- AudioTrack State: ${audioTrack?.state}")
            appendLine("- Playback State: ${audioTrack?.playState}")
            appendLine("- Sample Rate: $sampleRate")
            appendLine("- Buffer Size: $bufferSize")
            appendLine("- Active Notes: ${activeNotes.size}")
            appendLine("- Is Playing: $isPlaying")
            appendLine("- Synthesis Job: ${if (synthesisJob?.isActive == true) "Running" else "Stopped"}")
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
