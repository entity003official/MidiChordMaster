package com.midichordmaster

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

class ChordDisplayViewModel : ViewModel() {
    
    private val _currentChord = MutableStateFlow("")
    val currentChord: StateFlow<String> = _currentChord.asStateFlow()
    
    private val _currentChordNotes = MutableStateFlow<List<String>>(emptyList())
    val currentChordNotes: StateFlow<List<String>> = _currentChordNotes.asStateFlow()
    
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()
    
    private val _pressedKeys = MutableStateFlow<Set<Int>>(emptySet())
    val pressedKeys: StateFlow<Set<Int>> = _pressedKeys.asStateFlow()
    
    private val _isMidiConnected = MutableStateFlow(false)
    val isMidiConnected: StateFlow<Boolean> = _isMidiConnected.asStateFlow()
    
    private val _debugLogs = MutableStateFlow<List<String>>(emptyList())
    val debugLogs: StateFlow<List<String>> = _debugLogs.asStateFlow()
    
    private val _memoryUsage = MutableStateFlow("")
    val memoryUsage: StateFlow<String> = _memoryUsage.asStateFlow()
    
    private var midiManager: MidiManager? = null
    private var audioSynthesizer: AudioSynthesizer? = null
    private var chordAnalyzer: ChordAnalyzer? = null
    
    private fun addDebugLog(message: String) {
        val timestamp = java.text.SimpleDateFormat("HH:mm:ss.SSS", java.util.Locale.getDefault()).format(java.util.Date())
        val logMessage = "[$timestamp] $message"
        val currentLogs = _debugLogs.value.toMutableList()
        currentLogs.add(logMessage)
        
        // Keep only last 50 log entries
        if (currentLogs.size > 50) {
            currentLogs.removeAt(0)
        }
        
        _debugLogs.value = currentLogs
        println("DEBUG_LOG: $logMessage")
    }
    
    private fun updateChordAnalysis(notes: Set<Int>) {
        try {
            if (notes.isEmpty()) {
                _currentChord.value = ""
                _currentChordNotes.value = emptyList()
                return
            }
            
            chordAnalyzer?.let { analyzer ->
                val result = analyzer.analyzeChord(notes)
                _currentChord.value = result.chordName
                _currentChordNotes.value = result.noteNames
                addDebugLog("� Chord: ${result.chordName} | Notes: ${result.noteNames.joinToString(", ")}")
            } ?: run {
                val noteNames = notes.sorted().map { midiNoteToNoteName(it) }
                _currentChord.value = noteNames.joinToString("-")
                _currentChordNotes.value = noteNames
                addDebugLog("❌ ChordAnalyzer is null, showing note names")
            }
        } catch (e: Exception) {
            addDebugLog("❌ Chord analysis failed: ${e.message}")
            _currentChord.value = "Analysis Error"
            _currentChordNotes.value = emptyList()
        }
    }
    
    private fun updateMemoryUsage() {
        try {
            val runtime = Runtime.getRuntime()
            val totalMemory = runtime.totalMemory() / (1024 * 1024) // MB
            val freeMemory = runtime.freeMemory() / (1024 * 1024) // MB
            val usedMemory = totalMemory - freeMemory
            val maxMemory = runtime.maxMemory() / (1024 * 1024) // MB
            
            _memoryUsage.value = "Memory: ${usedMemory}/${maxMemory}MB (${(usedMemory * 100 / maxMemory)}%)"
            
            if (usedMemory > maxMemory * 0.8) {
                addDebugLog("⚠️ High memory usage: ${usedMemory}MB/${maxMemory}MB")
            }
        } catch (e: Exception) {
            addDebugLog("❌ Memory check failed: ${e.message}")
        }
    }
    
    fun clearDebugLogs() {
        _debugLogs.value = emptyList()
        addDebugLog("Debug logs cleared")
    }
    
    fun forceGarbageCollection() {
        try {
            addDebugLog("🧹 Running garbage collection...")
            System.gc()
            // Wait a moment for GC to complete
            Thread.sleep(100)
            updateMemoryUsage()
            addDebugLog("✅ Garbage collection completed")
        } catch (e: Exception) {
            addDebugLog("❌ Garbage collection failed: ${e.message}")
        }
    }
    
    fun getDiagnosticInfo() {
        viewModelScope.launch {
            addDebugLog("🔍 Running diagnostic check...")
            
            try {
                // Check components
                addDebugLog("📋 Component Status:")
                addDebugLog("- MidiManager: ${midiManager != null}")
                addDebugLog("- AudioSynthesizer: ${audioSynthesizer != null}")
                addDebugLog("- ChordAnalyzer: ${chordAnalyzer != null}")
                
                // Get AudioSynthesizer details
                audioSynthesizer?.let { synth ->
                    addDebugLog("🔊 AudioSynthesizer Details:")
                    val debugInfo = synth.getDebugInfo()
                    debugInfo.lines().forEach { line ->
                        if (line.isNotBlank()) {
                            addDebugLog("  $line")
                        }
                    }
                } ?: addDebugLog("❌ AudioSynthesizer is null!")
                
                // Check Android Audio system
                addDebugLog("📱 System Audio Info:")
                addDebugLog("- Android Version: ${android.os.Build.VERSION.SDK_INT}")
                addDebugLog("- Hardware: ${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}")
                
            } catch (e: Exception) {
                addDebugLog("❌ Diagnostic failed: ${e.message}")
            }
        }
    }
    
    fun initializeComponents(
        midiManager: MidiManager,
        audioSynthesizer: AudioSynthesizer,
        chordAnalyzer: ChordAnalyzer
    ) {
        addDebugLog("🔧 Initializing ViewModel components...")
        
        this.midiManager = midiManager
        this.audioSynthesizer = audioSynthesizer
        this.chordAnalyzer = chordAnalyzer
        
        addDebugLog("✅ MidiManager: ${midiManager != null}")
        addDebugLog("✅ AudioSynthesizer: ${audioSynthesizer != null}")
        addDebugLog("✅ ChordAnalyzer: ${chordAnalyzer != null}")
        
        // Check AudioSynthesizer initialization status
        try {
            val isAudioReady = audioSynthesizer.toString().contains("AudioSynthesizer")
            addDebugLog("🔊 AudioSynthesizer status: $isAudioReady")
        } catch (e: Exception) {
            addDebugLog("❌ AudioSynthesizer check failed: ${e.message}")
        }
        
        // Setup MIDI listener
        midiManager.setMidiListener { midiEvent ->
            handleMidiEvent(midiEvent)
        }
        
        addDebugLog("🎹 ViewModel initialization complete")
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
                    updateChordAnalysis(newKeys)
                    updateMemoryUsage()
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
                    updateChordAnalysis(newKeys)
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
            addDebugLog("🎵 Starting audio test...")
            
            try {
                // Check AudioSynthesizer availability
                if (audioSynthesizer == null) {
                    addDebugLog("❌ AudioSynthesizer is null!")
                    return@launch
                }
                
                addDebugLog("✅ AudioSynthesizer found, testing C Major chord...")
                
                // Play a C major chord for testing
                val testChord = setOf(60, 64, 67) // C, E, G
                addDebugLog("🎹 Playing notes: C4(60), E4(64), G4(67)")
                
                audioSynthesizer?.playChord(testChord, 80)
                addDebugLog("📤 playChord() called successfully")
                
                _pressedKeys.value = testChord
                updateChordAnalysis(testChord)
                _isPlaying.value = true
                
                addDebugLog("⏱️ Playing for 2 seconds...")
                
                // Stop after 2 seconds
                kotlinx.coroutines.delay(2000)
                
                addDebugLog("🛑 Stopping all notes...")
                audioSynthesizer?.stopAllNotes()
                _pressedKeys.value = emptySet()
                updateChordAnalysis(emptySet())
                _isPlaying.value = false
                
                addDebugLog("✅ Audio test completed")
                
            } catch (e: Exception) {
                addDebugLog("❌ Audio test failed: ${e.message}")
                addDebugLog("🔍 Stack trace: ${e.stackTraceToString()}")
            }
        }
    }
    
    // Virtual piano key handling
    fun onVirtualKeyPress(note: Int) {
        viewModelScope.launch {
            addDebugLog("🎹 Virtual key press: Note $note")
            
            try {
                val newKeys = _pressedKeys.value.toMutableSet()
                newKeys.add(note)
                _pressedKeys.value = newKeys
                
                // Check AudioSynthesizer before playing
                if (audioSynthesizer == null) {
                    addDebugLog("❌ AudioSynthesizer is null - cannot play note $note")
                    return@launch
                }
                
                addDebugLog("🔊 Calling playNote($note, 80)...")
                
                // Play the note
                audioSynthesizer?.playNote(note, 80) // Default velocity 80
                _isPlaying.value = true
                
                addDebugLog("✅ playNote() called successfully")
                
                // Analyze chord
                updateChordAnalysis(newKeys)
                updateMemoryUsage()
                
            } catch (e: Exception) {
                addDebugLog("❌ Virtual key press failed: ${e.message}")
                addDebugLog("🔍 Stack trace: ${e.stackTraceToString()}")
            }
        }
    }
    
    fun onVirtualKeyRelease(note: Int) {
        viewModelScope.launch {
            addDebugLog("🎹 Virtual key release: Note $note")
            
            try {
                val newKeys = _pressedKeys.value.toMutableSet()
                newKeys.remove(note)
                _pressedKeys.value = newKeys
                
                // Stop the note
                audioSynthesizer?.stopNote(note)
                addDebugLog("🛑 stopNote($note) called")
                
                // Update playing status
                _isPlaying.value = newKeys.isNotEmpty()
                
                // Re-analyze chord with remaining keys
                updateChordAnalysis(newKeys)
                
            } catch (e: Exception) {
                addDebugLog("❌ Virtual key release failed: ${e.message}")
            }
        }
    }
    
    private fun midiNoteToNoteName(midiNote: Int): String {
        val noteNames = arrayOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
        val octave = (midiNote / 12) - 1
        val noteIndex = midiNote % 12
        return "${noteNames[noteIndex]}$octave"
    }
    
    override fun onCleared() {
        super.onCleared()
        audioSynthesizer?.release()
        midiManager?.disconnect()
    }
}
