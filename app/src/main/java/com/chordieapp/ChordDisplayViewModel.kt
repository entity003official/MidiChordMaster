package com.chordieapp

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
                    
                    // Re-analyze chord
                    val chord = if (newKeys.isNotEmpty()) {
                        chordAnalyzer?.analyzeChord(newKeys) ?: ""
                    } else {
                        ""
                    }
                    _currentChord.value = chord
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
    
    override fun onCleared() {
        super.onCleared()
        audioSynthesizer?.release()
        midiManager?.disconnect()
    }
}
