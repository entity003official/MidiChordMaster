package com.midichordmaster

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChordDisplayScreen(
    viewModel: ChordDisplayViewModel = viewModel()
) {
    val currentChord by viewModel.currentChord.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val pressedKeys by viewModel.pressedKeys.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Text(
            text = "MidiChordMaster",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Current Chord Display
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isPlaying) 
                    MaterialTheme.colorScheme.primaryContainer 
                else 
                    MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Current Chord",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = currentChord.ifEmpty { "No Chord" },
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Piano Keys Display
        Text(
            text = "Piano Keys",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Medium
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        PianoKeysDisplay(
            pressedKeys = pressedKeys,
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Status and Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatusIndicator(
                label = "MIDI",
                isActive = viewModel.isMidiConnected.collectAsState().value,
                activeColor = Color.Green,
                inactiveColor = Color.Red
            )
            
            StatusIndicator(
                label = "Audio",
                isActive = isPlaying,
                activeColor = Color.Blue,
                inactiveColor = Color.Gray
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Control Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = { viewModel.connectMidi() },
                modifier = Modifier.weight(1f)
            ) {
                Text("Connect MIDI")
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Button(
                onClick = { viewModel.testAudio() },
                modifier = Modifier.weight(1f)
            ) {
                Text("Test Audio")
            }
        }
    }
}

@Composable
fun PianoKeysDisplay(
    pressedKeys: Set<Int>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        drawPianoKeys(pressedKeys)
    }
}

private fun DrawScope.drawPianoKeys(pressedKeys: Set<Int>) {
    val whiteKeyWidth = size.width / 7
    val blackKeyWidth = whiteKeyWidth * 0.6f
    val whiteKeyHeight = size.height
    val blackKeyHeight = size.height * 0.6f
    
    // White keys (C, D, E, F, G, A, B)
    val whiteKeys = listOf(0, 2, 4, 5, 7, 9, 11) // MIDI notes relative to C
    for (i in whiteKeys.indices) {
        val x = i * whiteKeyWidth
        val isPressed = pressedKeys.contains(whiteKeys[i] + 60) // C4 = 60
        
        drawRect(
            color = if (isPressed) Color.Blue else Color.White,
            topLeft = Offset(x, 0f),
            size = androidx.compose.ui.geometry.Size(whiteKeyWidth - 2, whiteKeyHeight)
        )
        
        drawRect(
            color = Color.Black,
            topLeft = Offset(x, 0f),
            size = androidx.compose.ui.geometry.Size(whiteKeyWidth - 2, whiteKeyHeight),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
        )
    }
    
    // Black keys (C#, D#, F#, G#, A#)
    val blackKeys = listOf(1, 3, 6, 8, 10) // MIDI notes relative to C
    val blackKeyPositions = listOf(0.7f, 1.7f, 3.7f, 4.7f, 5.7f)
    
    for (i in blackKeys.indices) {
        val x = blackKeyPositions[i] * whiteKeyWidth - blackKeyWidth / 2
        val isPressed = pressedKeys.contains(blackKeys[i] + 60) // C4 = 60
        
        drawRect(
            color = if (isPressed) Color.Blue else Color.Black,
            topLeft = Offset(x, 0f),
            size = androidx.compose.ui.geometry.Size(blackKeyWidth, blackKeyHeight)
        )
    }
}

@Composable
fun StatusIndicator(
    label: String,
    isActive: Boolean,
    activeColor: Color,
    inactiveColor: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(if (isActive) activeColor else inactiveColor)
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
