package com.midichordmaster

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
    viewModel: ChordDisplayViewModel = viewModel()
) {
    val currentChord by viewModel.currentChord.collectAsState()
    val currentChordNotes by viewModel.currentChordNotes.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val pressedKeys by viewModel.pressedKeys.collectAsState()
    val isMidiConnected by viewModel.isMidiConnected.collectAsState()
    val debugLogs by viewModel.debugLogs.collectAsState()
    val memoryUsage by viewModel.memoryUsage.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
    ) {
        // Compact top control bar (fixed height)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            elevation = 4.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // App title and current chord (single row)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "MIDI Chord Master",
                        style = MaterialTheme.typography.h6,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colors.primary
                    )
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "♪ ",
                            style = MaterialTheme.typography.h5,
                            color = MaterialTheme.colors.primary
                        )
                        Text(
                            text = if (currentChord.isNotEmpty()) currentChord else "Play keys...",
                            style = MaterialTheme.typography.h5,
                            fontWeight = FontWeight.Bold,
                            color = if (isPlaying) MaterialTheme.colors.primary else MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                        )
                        if (currentChordNotes.isNotEmpty()) {
                            Text(
                                text = " (${currentChordNotes.joinToString(", ")})",
                                style = MaterialTheme.typography.body2,
                                color = MaterialTheme.colors.onSurface.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Status and controls row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Status indicators (compact)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StatusIndicator(
                            label = "MIDI",
                            isConnected = isMidiConnected
                        )
                        StatusIndicator(
                            label = "Audio",
                            isConnected = true
                        )
                        StatusIndicator(
                            label = "Virtual",
                            isConnected = true
                        )
                    }
                    
                    // Control buttons
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.connectMidi() },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("Connect", style = MaterialTheme.typography.caption)
                        }
                        Button(
                            onClick = { viewModel.testAudioSynthesis() },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("Test Audio", style = MaterialTheme.typography.caption)
                        }
                        Button(
                            onClick = { viewModel.clearNotes() },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("Clear", style = MaterialTheme.typography.caption)
                        }
                    }
                }
            }
        }
        
        // Main content area with piano and debug
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            // Left side - Full 88-key scrollable piano (takes most space)
            Column(
                modifier = Modifier
                    .weight(3f)
                    .fillMaxHeight()
                    .padding(end = 8.dp)
            ) {
                Text(
                    text = "88-Key Piano (A0-C8) - Scroll to navigate",
                    style = MaterialTheme.typography.subtitle2,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.8f),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                // Enhanced scrollable 88-key piano
                ScrollablePianoKeyboard(
                    modifier = Modifier.fillMaxSize(),
                    pressedKeys = pressedKeys,
                    onKeyPress = { note -> viewModel.playNote(note) },
                    onKeyRelease = { note -> viewModel.stopNote(note) }
                )
            }
            
            // Right side - Debug console (compact)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                Text(
                    text = "Debug Console",
                    style = MaterialTheme.typography.subtitle2,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.8f),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Card(
                    modifier = Modifier.fillMaxSize(),
                    elevation = 2.dp,
                    backgroundColor = Color.Black.copy(alpha = 0.9f)
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        state = rememberLazyListState(),
                        reverseLayout = true
                    ) {
                        items(debugLogs.takeLast(100)) { log ->
                            Text(
                                text = log,
                                style = MaterialTheme.typography.caption,
                                color = when {
                                    log.contains("❌") -> Color.Red
                                    log.contains("✅") -> Color.Green
                                    log.contains("🔊") || log.contains("🎵") || log.contains("🎹") -> Color.Cyan
                                    log.contains("🔧") || log.contains("🛑") -> Color.Yellow
                                    log.contains("🎯") -> Color.Magenta
                                    else -> Color.White
                                },
                                fontSize = 10.sp,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                modifier = Modifier.padding(vertical = 1.dp)
                            )
                        }
                    }
                }
            }
        }
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
