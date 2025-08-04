package com.chordieapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.chordieapp.ui.theme.ChordieAppTheme

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
            ChordieAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ChordDisplayScreen()
                }
            }
        }
    }
    
    private fun checkPermissions() {
        val permissionsNeeded = mutableListOf<String>()
        
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) 
            != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.BLUETOOTH_CONNECT)
        }
        
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) 
            != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.RECORD_AUDIO)
        }
        
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
        
        // Initialize ViewModel with components
        // This would typically be done with dependency injection
        // For now, we'll set up a basic initialization
    }
}
