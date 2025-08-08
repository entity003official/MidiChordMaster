package com.midichordmaster

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun ChordDisplayScreen(
    viewModel: ChordDisplayViewModel = (LocalContext.current as MainActivity).getChordDisplayViewModel()
) {
    val currentChord by viewModel.currentChord.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val pressedKeys by viewModel.pressedKeys.collectAsState()
    val isMidiConnected by viewModel.isMidiConnected.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
    ) {
        // Top content area (chord info and controls)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Left panel - Chord display and status
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // App title
                Text(
                    text = "MIDI Chord Master",
                    style = MaterialTheme.typography.h5,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colors.primary,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                // Current chord display
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    elevation = 8.dp,
                    backgroundColor = if (isPlaying) {
                        MaterialTheme.colors.primary.copy(alpha = 0.1f)
                    } else {
                        MaterialTheme.colors.surface
                    }
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (currentChord.isNotEmpty()) currentChord else "Play some keys...",
                            style = MaterialTheme.typography.h4,
                            fontWeight = FontWeight.Bold,
                            color = if (isPlaying) {
                                MaterialTheme.colors.primary
                            } else {
                                MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                            },
                            textAlign = TextAlign.Center
                        )
                        
                        if (isPlaying) {
                            Text(
                                text = "♪ Playing",
                                style = MaterialTheme.typography.caption,
                                color = MaterialTheme.colors.primary
                            )
                        }
                    }
                }

                // Connection status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatusIndicator(
                        label = "MIDI",
                        isConnected = isMidiConnected,
                        modifier = Modifier.weight(1f)
                    )
                    
                    StatusIndicator(
                        label = "Audio",
                        isConnected = true, // Audio is always ready
                        modifier = Modifier.weight(1f)
                    )
                    
                    StatusIndicator(
                        label = "Virtual",
                        isConnected = true, // Virtual piano is always ready
                        modifier = Modifier.weight(1f)
                    )
                }

                // Control buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = { viewModel.connectMidi() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = if (isMidiConnected) Color.Green else MaterialTheme.colors.primary
                        )
                    ) {
                        Text(
                            text = if (isMidiConnected) "MIDI Connected" else "Connect MIDI",
                            color = Color.White
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Button(
                        onClick = { viewModel.testAudio() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Test Audio")
                    }
                }
            }

            // Right panel - Visual piano display
            Card(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                elevation = 4.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Piano Visualization",
                            style = MaterialTheme.typography.h6,
                            color = MaterialTheme.colors.onSurface,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        
                        // Visual piano keyboard (for display only)
                        VisualPianoKeyboard(
                            pressedKeys = pressedKeys,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = if (pressedKeys.isNotEmpty()) {
                                val sortedNotes = pressedKeys.sorted()
                                val noteNames = sortedNotes.map { midiNoteToNoteName(it) }
                                "Notes: ${noteNames.joinToString(" + ")}"
                            } else {
                                "Touch piano below or connect MIDI"
                            },
                            style = MaterialTheme.typography.body2,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // Bottom interactive piano
        InteractivePianoKeyboard(
            modifier = Modifier.fillMaxWidth(),
            pressedKeys = pressedKeys,
            onKeyPress = { note -> viewModel.onVirtualKeyPress(note) },
            onKeyRelease = { note -> viewModel.onVirtualKeyRelease(note) },
            startOctave = 3,
            octaveCount = 3
        )
    }
}

@Composable
fun StatusIndicator(
    label: String,
    isConnected: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(RoundedCornerShape(50))
                .background(
                    if (isConnected) Color.Green else Color.Red
                )
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = label,
            style = MaterialTheme.typography.caption,
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun VisualPianoKeyboard(
    pressedKeys: Set<Int>,
    modifier: Modifier = Modifier,
    startOctave: Int = 3,
    octaveCount: Int = 3
) {
    Canvas(modifier = modifier) {
        val keyboardWidth = size.width
        val keyboardHeight = size.height
        val whiteKeyCount = octaveCount * 7
        val whiteKeyWidth = keyboardWidth / whiteKeyCount
        val blackKeyWidth = whiteKeyWidth * 0.6f
        val blackKeyHeight = keyboardHeight * 0.65f
        
        var whiteKeyIndex = 0
        
        // Draw white keys first
        for (octave in startOctave until startOctave + octaveCount) {
            val whiteNotes = listOf(0, 2, 4, 5, 7, 9, 11) // C, D, E, F, G, A, B
            for (noteInOctave in whiteNotes) {
                val midiNote = octave * 12 + noteInOctave
                val x = whiteKeyIndex * whiteKeyWidth
                val isPressed = pressedKeys.contains(midiNote)
                
                val keyColor = if (isPressed) Color(0xFF4CAF50) else Color.White
                val strokeColor = Color.Black
                
                // Draw key
                drawRect(
                    color = keyColor,
                    topLeft = Offset(x, 0f),
                    size = androidx.compose.ui.geometry.Size(whiteKeyWidth - 1, keyboardHeight)
                )
                
                // Draw border
                drawRect(
                    color = strokeColor,
                    topLeft = Offset(x, 0f),
                    size = androidx.compose.ui.geometry.Size(whiteKeyWidth - 1, keyboardHeight),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
                )
                
                whiteKeyIndex++
            }
        }
        
        // Draw black keys on top
        for (octave in startOctave until startOctave + octaveCount) {
            val octaveStartX = (octave - startOctave) * 7 * whiteKeyWidth
            val blackKeyPositions = listOf(
                0.75f to 1,  // C#
                1.75f to 3,  // D#
                3.75f to 6,  // F#
                4.75f to 8,  // G#
                5.75f to 10  // A#
            )
            
            for ((position, noteInOctave) in blackKeyPositions) {
                val midiNote = octave * 12 + noteInOctave
                val x = octaveStartX + position * whiteKeyWidth - blackKeyWidth / 2
                val isPressed = pressedKeys.contains(midiNote)
                
                val keyColor = if (isPressed) Color(0xFF2196F3) else Color.Black
                
                // Draw black key
                drawRect(
                    color = keyColor,
                    topLeft = Offset(x, 0f),
                    size = androidx.compose.ui.geometry.Size(blackKeyWidth, blackKeyHeight)
                )
            }
        }
    }
}

// Helper function to convert MIDI note number to note name
private fun midiNoteToNoteName(midiNote: Int): String {
    val noteNames = arrayOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
    val octave = midiNote / 12 - 1
    val noteIndex = midiNote % 12
    return "${noteNames[noteIndex]}$octave"
}
