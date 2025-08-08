package com.midichordmaster

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.midichordmaster.ui.theme.MidiChordMasterTheme

class MainActivity : ComponentActivity() {
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Handle permission results
        val bluetoothGranted = permissions[Manifest.permission.BLUETOOTH_CONNECT] ?: false
        val audioGranted = permissions[Manifest.permission.RECORD_AUDIO] ?: false
        
        if (bluetoothGranted && audioGranted) {
            // Initialize MIDI and audio components
            initializeMidiAndAudio()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Check and request permissions
        checkPermissions()
        
        setContent {
            MidiChordMasterTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    ChordDisplayScreen(viewModel = getChordDisplayViewModel())
                }
            }
        }
    }
    
    private fun checkPermissions() {
        val permissionsNeeded = mutableListOf<String>()
        
        // Only request BLUETOOTH_CONNECT for MIDI devices
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) 
                != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.BLUETOOTH_CONNECT)
            }
        }
        
        // No need for RECORD_AUDIO permission for audio playback
        // AudioTrack doesn't require special permissions for output
        
        if (permissionsNeeded.isNotEmpty()) {
            requestPermissionLauncher.launch(permissionsNeeded.toTypedArray())
        } else {
            initializeMidiAndAudio()
        }
    }
    
    private fun initializeMidiAndAudio() {
        // Initialize components
        val midiManager = MidiManager(this)
        val audioSynthesizer = AudioSynthesizer()
        val chordAnalyzer = ChordAnalyzer()
        
        // Store components for ViewModel access
        this.midiManager = midiManager
        this.audioSynthesizer = audioSynthesizer
        this.chordAnalyzer = chordAnalyzer
    }
    
    // Add these properties to the class
    private var midiManager: MidiManager? = null
    private var audioSynthesizer: AudioSynthesizer? = null
    private var chordAnalyzer: ChordAnalyzer? = null
    
    // Add method to get ViewModel with initialized components
    fun getChordDisplayViewModel(): ChordDisplayViewModel {
        return try {
            val viewModel = ChordDisplayViewModel()
            midiManager?.let { midi ->
                audioSynthesizer?.let { audio ->
                    chordAnalyzer?.let { analyzer ->
                        viewModel.initializeComponents(midi, audio, analyzer)
                        println("DEBUG: ViewModel initialized with components")
                    }
                }
            }
            viewModel
        } catch (e: Exception) {
            println("ERROR: Failed to initialize ViewModel: ${e.message}")
            e.printStackTrace()
            ChordDisplayViewModel() // Return empty ViewModel as fallback
        }
    }
}
