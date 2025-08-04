package com.chordieapp

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
    )
    
    private val audioTrack = AudioTrack.Builder()
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
    
    private val activeNotes = mutableMapOf<Int, NoteData>()
    private var synthesisJob: Job? = null
    private var isPlaying = false
    
    data class NoteData(
        val frequency: Double,
        val velocity: Int,
        var phase: Double = 0.0,
        var envelope: Double = 1.0
    )
    
    init {
        audioTrack.play()
        startSynthesis()
    }
    
    private fun startSynthesis() {
        synthesisJob = CoroutineScope(Dispatchers.Default).launch {
            val buffer = ShortArray(bufferSize)
            
            while (isActive) {
                if (activeNotes.isNotEmpty()) {
                    generateAudioBuffer(buffer)
                    audioTrack.write(buffer, 0, buffer.size)
                } else {
                    // Generate silence when no notes are playing
                    buffer.fill(0)
                    audioTrack.write(buffer, 0, buffer.size)
                }
                
                delay(10) // Small delay to prevent excessive CPU usage
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
        val frequency = midiNoteToFrequency(midiNote)
        activeNotes[midiNote] = NoteData(frequency, velocity)
        isPlaying = true
    }
    
    fun stopNote(midiNote: Int) {
        activeNotes.remove(midiNote)
        if (activeNotes.isEmpty()) {
            isPlaying = false
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
    
    fun release() {
        synthesisJob?.cancel()
        audioTrack.stop()
        audioTrack.release()
    }
}
