package com.midichordmaster

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun InteractivePianoKeyboard(
    modifier: Modifier = Modifier,
    pressedKeys: Set<Int>,
    onKeyPress: (Int) -> Unit,
    onKeyRelease: (Int) -> Unit,
    startOctave: Int = 3,
    octaveCount: Int = 3
) {
    val density = LocalDensity.current
    val keyboardHeight = 120.dp
    
    // Generate piano keys
    val pianoKeys = remember(startOctave, octaveCount) {
        generatePianoKeys(startOctave, octaveCount)
    }
    
    // Track multiple pressed keys by pointer ID
    var activeTouches by remember { mutableStateOf<Map<PointerId, Int>>(emptyMap()) }
    
    Box(modifier = modifier.height(keyboardHeight)) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            
                            event.changes.forEach { change ->
                                val position = change.position
                                val pointerId = change.id
                                
                                when {
                                    change.pressed && !activeTouches.containsKey(pointerId) -> {
                                        // New touch down
                                        val key = findKeyAtPosition(
                                            position, 
                                            pianoKeys, 
                                            size.width.toFloat(), 
                                            size.height.toFloat()
                                        )
                                        key?.let { 
                                            onKeyPress(it)
                                            activeTouches = activeTouches + (pointerId to it)
                                        }
                                        change.consume()
                                    }
                                    !change.pressed && activeTouches.containsKey(pointerId) -> {
                                        // Touch released
                                        activeTouches[pointerId]?.let { key ->
                                            onKeyRelease(key)
                                        }
                                        activeTouches = activeTouches - pointerId
                                        change.consume()
                                    }
                                    change.pressed && activeTouches.containsKey(pointerId) -> {
                                        // Touch moved - check if moved to different key
                                        val currentKey = findKeyAtPosition(
                                            position, 
                                            pianoKeys, 
                                            size.width.toFloat(), 
                                            size.height.toFloat()
                                        )
                                        val previousKey = activeTouches[pointerId]
                                        
                                        if (currentKey != previousKey) {
                                            // Moved to different key
                                            previousKey?.let { onKeyRelease(it) }
                                            currentKey?.let { 
                                                onKeyPress(it)
                                                activeTouches = activeTouches + (pointerId to it)
                                            } ?: run {
                                                activeTouches = activeTouches - pointerId
                                            }
                                        }
                                        change.consume()
                                    }
                                }
                            }
                        }
                    }
                }
        ) {
            drawPianoKeyboard(
                pianoKeys = pianoKeys,
                pressedKeys = pressedKeys,
                canvasWidth = size.width,
                canvasHeight = size.height
            )
        }
    }
}

private fun findKeyAtPosition(
    offset: Offset,
    pianoKeys: List<PianoKey>,
    canvasWidth: Float,
    canvasHeight: Float
): Int? {
    val x = offset.x
    val y = offset.y
    
    // Check black keys first (they are on top)
    pianoKeys.filter { it.isBlack }.forEach { key ->
        val keyX = (key.x / 100f) * canvasWidth
        val keyWidth = (key.width / 100f) * canvasWidth
        val keyHeight = canvasHeight * 0.65f
        
        if (x >= keyX && x <= keyX + keyWidth && y <= keyHeight) {
            return key.midiNote
        }
    }
    
    // Check white keys
    pianoKeys.filter { !it.isBlack }.forEach { key ->
        val keyX = (key.x / 100f) * canvasWidth
        val keyWidth = (key.width / 100f) * canvasWidth
        
        if (x >= keyX && x <= keyX + keyWidth && y <= canvasHeight) {
            return key.midiNote
        }
    }
    
    return null
}

private fun DrawScope.drawPianoKeyboard(
    pianoKeys: List<PianoKey>,
    pressedKeys: Set<Int>,
    canvasWidth: Float,
    canvasHeight: Float
) {
    val whiteKeyHeight = canvasHeight
    val blackKeyHeight = canvasHeight * 0.65f
    
    // Draw white keys first
    pianoKeys.filter { !it.isBlack }.forEach { key ->
        val keyX = (key.x / 100f) * canvasWidth
        val keyWidth = (key.width / 100f) * canvasWidth
        val isPressed = pressedKeys.contains(key.midiNote)
        
        val keyColor = when {
            isPressed -> Color(0xFF81C784) // Light green when pressed
            else -> Color.White
        }
        
        // Draw key background
        drawRect(
            color = keyColor,
            topLeft = Offset(keyX + 2, 2f),
            size = Size(keyWidth - 4, whiteKeyHeight - 4)
        )
        
        // Draw key border
        drawRect(
            color = Color.Black,
            topLeft = Offset(keyX, 0f),
            size = Size(keyWidth, whiteKeyHeight),
            style = Stroke(width = 2.dp.toPx())
        )
    }
    
    // Draw black keys on top
    pianoKeys.filter { it.isBlack }.forEach { key ->
        val keyX = (key.x / 100f) * canvasWidth
        val keyWidth = (key.width / 100f) * canvasWidth
        val isPressed = pressedKeys.contains(key.midiNote)
        
        val keyColor = when {
            isPressed -> Color(0xFF42A5F5) // Light blue when pressed
            else -> Color.Black
        }
        
        // Draw black key
        drawRect(
            color = keyColor,
            topLeft = Offset(keyX + 2, 2f),
            size = Size(keyWidth - 4, blackKeyHeight - 4)
        )
        
        // Draw thin highlight on black keys
        drawRect(
            color = Color.Gray,
            topLeft = Offset(keyX + 2, 2f),
            size = Size(keyWidth - 4, blackKeyHeight * 0.3f)
        )
    }
}

data class PianoKey(
    val midiNote: Int,
    val x: Float, // Position as percentage of total width
    val width: Float, // Width as percentage of total width
    val isBlack: Boolean
)

private fun generatePianoKeys(startOctave: Int, octaveCount: Int): List<PianoKey> {
    val keys = mutableListOf<PianoKey>()
    val whiteKeyWidth = 100f / (octaveCount * 7) // 7 white keys per octave
    var whiteKeyIndex = 0
    
    for (octave in startOctave until startOctave + octaveCount) {
        val whiteNotes = listOf(0, 2, 4, 5, 7, 9, 11) // C, D, E, F, G, A, B
        val blackNotes = listOf(1, 3, 6, 8, 10) // C#, D#, F#, G#, A#
        
        // Add white keys
        whiteNotes.forEach { noteInOctave ->
            val midiNote = octave * 12 + noteInOctave
            keys.add(
                PianoKey(
                    midiNote = midiNote,
                    x = whiteKeyIndex * whiteKeyWidth,
                    width = whiteKeyWidth,
                    isBlack = false
                )
            )
            whiteKeyIndex++
        }
        
        // Add black keys
        val blackKeyPositions = listOf(
            0.75f to 1,  // C#
            1.75f to 3,  // D#
            3.75f to 6,  // F#
            4.75f to 8,  // G#
            5.75f to 10  // A#
        )
        
        val octaveStartX = (octave - startOctave) * 7 * whiteKeyWidth
        blackKeyPositions.forEach { (position, noteInOctave) ->
            val midiNote = octave * 12 + noteInOctave
            keys.add(
                PianoKey(
                    midiNote = midiNote,
                    x = octaveStartX + position * whiteKeyWidth - whiteKeyWidth * 0.3f,
                    width = whiteKeyWidth * 0.6f,
                    isBlack = true
                )
            )
        }
    }
    
    return keys
}
