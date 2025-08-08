package com.midichordmaster

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChordDisplayViewModel : ViewModel() {
    
    private val _currentChord = MutableStateFlow("")
    val currentChord: StateFlow<String> = _currentChord.asStateFlow()
    
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()
    
    private val _pressedKeys = MutableStateFlow<Set<Int>>(emptySet())
    val pressedKeys: StateFlow<Set<Int>> = _pressedKeys.asStateFlow()
    
    private val _isMidiConnected = MutableStateFlow(false)
    val isMidiConnected: StateFlow<Boolean> = _isMidiConnected.asStateFlow()
    
    private var midiManager: MidiManager? = null
    private var audioSynthesizer: AudioSynthesizer? = null
    private var chordAnalyzer: ChordAnalyzer? = null
    
    fun initializeComponents(
        midiManager: MidiManager,
        audioSynthesizer: AudioSynthesizer,
        chordAnalyzer: ChordAnalyzer
    ) {
        this.midiManager = midiManager
        this.audioSynthesizer = audioSynthesizer
        this.chordAnalyzer = chordAnalyzer
        
        // Setup MIDI listener
        midiManager.setMidiListener { midiEvent ->
            handleMidiEvent(midiEvent)
        }
    }
    
    private fun handleMidiEvent(midiEvent: MidiEvent) {
        viewModelScope.launch {
            when (midiEvent.type) {
                MidiEvent.Type.NOTE_ON -> {
                    val newKeys = _pressedKeys.value.toMutableSet()
                    newKeys.add(midiEvent.note)
                    _pressedKeys.value = newKeys
                    
                    // Play the note
                    audioSynthesizer?.playNote(midiEvent.note, midiEvent.velocity)
                    _isPlaying.value = true
                    
                    // Analyze chord
                    val chord = chordAnalyzer?.analyzeChord(newKeys) ?: ""
                    _currentChord.value = chord
                }
                
                MidiEvent.Type.NOTE_OFF -> {
                    val newKeys = _pressedKeys.value.toMutableSet()
                    newKeys.remove(midiEvent.note)
                    _pressedKeys.value = newKeys
                    
                    // Stop the note
                    audioSynthesizer?.stopNote(midiEvent.note)
                    
                    // Update playing status
                    _isPlaying.value = newKeys.isNotEmpty()
                    
                    // Re-analyze chord with remaining keys
                    val chord = if (newKeys.isNotEmpty()) {
                        chordAnalyzer?.analyzeChord(newKeys) ?: ""
                    } else {
                        ""
                    }
                    _currentChord.value = chord
                }

                MidiEvent.Type.CONTROL_CHANGE -> {
                    // Handle control change messages (sustain pedal, modulation, etc.)
                    // For now, we can ignore these or add basic handling
                }

                MidiEvent.Type.PITCH_BEND -> {
                    // Handle pitch bend messages
                    // For now, we can ignore these or add basic handling
                }
            }
        }
    }
    
    fun connectMidi() {
        viewModelScope.launch {
            val connected = midiManager?.connectMidi() ?: false
            _isMidiConnected.value = connected
        }
    }
    
    fun testAudio() {
        viewModelScope.launch {
            // Play a C major chord for testing
            val testChord = setOf(60, 64, 67) // C, E, G
            audioSynthesizer?.playChord(testChord, 80)
            
            _pressedKeys.value = testChord
            _currentChord.value = "C Major"
            _isPlaying.value = true
            
            // Stop after 2 seconds
            kotlinx.coroutines.delay(2000)
            audioSynthesizer?.stopAllNotes()
            _pressedKeys.value = emptySet()
            _currentChord.value = ""
            _isPlaying.value = false
        }
    }
    
    // Virtual piano key handling
    fun onVirtualKeyPress(note: Int) {
        viewModelScope.launch {
            val newKeys = _pressedKeys.value.toMutableSet()
            newKeys.add(note)
            _pressedKeys.value = newKeys
            
            // Play the note
            audioSynthesizer?.playNote(note, 80) // Default velocity 80
            _isPlaying.value = true
            
            // Debug output
            println("DEBUG: Virtual key pressed - note: $note, audioSynthesizer: ${audioSynthesizer != null}")
            
            // Analyze chord
            val chord = chordAnalyzer?.analyzeChord(newKeys) ?: ""
            _currentChord.value = chord
            println("DEBUG: Chord analyzed: $chord from notes: $newKeys")
        }
    }
    
    fun onVirtualKeyRelease(note: Int) {
        viewModelScope.launch {
            val newKeys = _pressedKeys.value.toMutableSet()
            newKeys.remove(note)
            _pressedKeys.value = newKeys
            
            // Stop the note
            audioSynthesizer?.stopNote(note)
            
            // Update playing status
            _isPlaying.value = newKeys.isNotEmpty()
            
            // Re-analyze chord with remaining keys
            val chord = if (newKeys.isNotEmpty()) {
                chordAnalyzer?.analyzeChord(newKeys) ?: ""
            } else {
                ""
            }
            _currentChord.value = chord
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        audioSynthesizer?.release()
        midiManager?.disconnect()
    }
}
