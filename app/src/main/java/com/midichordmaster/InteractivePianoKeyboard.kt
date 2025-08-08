package com.midichordmaster

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

data class PianoKey(
    val note: Int,
    val isBlack: Boolean,
    val x: Float,
    val width: Float,
    val isPressed: Boolean = false
)

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
    val whiteKeyHeight = keyboardHeight
    val blackKeyHeight = keyboardHeight * 0.65f
    
    // Generate piano keys
    val pianoKeys = remember(startOctave, octaveCount) {
        generatePianoKeys(startOctave, octaveCount)
    }
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(keyboardHeight)
            .background(
                Color.Black,
                RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
            )
            .padding(4.dp)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectPianoTouch(
                        pianoKeys = pianoKeys,
                        whiteKeyHeight = whiteKeyHeight.toPx(),
                        blackKeyHeight = blackKeyHeight.toPx(),
                        onKeyPress = onKeyPress,
                        onKeyRelease = onKeyRelease
                    )
                }
        ) {
            drawPianoKeyboard(
                pianoKeys = pianoKeys,
                pressedKeys = pressedKeys,
                whiteKeyHeight = whiteKeyHeight.toPx(),
                blackKeyHeight = blackKeyHeight.toPx()
            )
        }
    }
}

private fun generatePianoKeys(startOctave: Int, octaveCount: Int): List<PianoKey> {
    val keys = mutableListOf<PianoKey>()
    val whiteKeyPattern = listOf(0, 2, 4, 5, 7, 9, 11) // C, D, E, F, G, A, B
    val blackKeyPattern = listOf(1, 3, 6, 8, 10) // C#, D#, F#, G#, A#
    
    var whiteKeyIndex = 0
    val whiteKeyWidth = 100f / (octaveCount * 7) // 7 white keys per octave
    
    // Draw white keys first
    for (octave in startOctave until startOctave + octaveCount) {
        for (noteInOctave in whiteKeyPattern) {
            val midiNote = octave * 12 + noteInOctave
            keys.add(
                PianoKey(
                    note = midiNote,
                    isBlack = false,
                    x = whiteKeyIndex * whiteKeyWidth,
                    width = whiteKeyWidth
                )
            )
            whiteKeyIndex++
        }
    }
    
    // Add black keys
    for (octave in startOctave until startOctave + octaveCount) {
        val octaveStartX = (octave - startOctave) * 7 * whiteKeyWidth
        val blackKeyPositions = listOf(
            0.75f to 1, // C# between C and D
            1.75f to 3, // D# between D and E
            3.75f to 6, // F# between F and G
            4.75f to 8, // G# between G and A
            5.75f to 10 // A# between A and B
        )
        
        for ((position, noteInOctave) in blackKeyPositions) {
            val midiNote = octave * 12 + noteInOctave
            keys.add(
                PianoKey(
                    note = midiNote,
                    isBlack = true,
                    x = octaveStartX + position * whiteKeyWidth,
                    width = whiteKeyWidth * 0.6f
                )
            )
        }
    }
    
    return keys.sortedBy { it.x }
}

private fun DrawScope.drawPianoKeyboard(
    pianoKeys: List<PianoKey>,
    pressedKeys: Set<Int>,
    whiteKeyHeight: Float,
    blackKeyHeight: Float
) {
    val keyboardWidth = size.width
    
    // Draw white keys first
    pianoKeys.filter { !it.isBlack }.forEach { key ->
        val x = (key.x / 100f) * keyboardWidth
        val width = (key.width / 100f) * keyboardWidth
        val isPressed = pressedKeys.contains(key.note)
        
        val keyColor = when {
            isPressed -> Color(0xFF4CAF50) // Green when pressed
            else -> Color.White
        }
        
        val shadowColor = if (isPressed) Color.Gray else Color(0xFF666666)
        
        // Draw shadow
        drawRect(
            color = shadowColor,
            topLeft = Offset(x + 2, 2f),
            size = Size(width - 4, whiteKeyHeight - 4)
        )
        
        // Draw key
        drawRect(
            color = keyColor,
            topLeft = Offset(x + 1, 1f),
            size = Size(width - 2, whiteKeyHeight - 2)
        )
        
        // Draw key border
        drawRect(
            color = Color.Black,
            topLeft = Offset(x, 0f),
            size = Size(width, whiteKeyHeight),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
        )
    }
    
    // Draw black keys on top
    pianoKeys.filter { it.isBlack }.forEach { key ->
        val x = (key.x / 100f) * keyboardWidth
        val width = (key.width / 100f) * keyboardWidth
        val isPressed = pressedKeys.contains(key.note)
        
        val keyColor = when {
            isPressed -> Color(0xFF2196F3) // Blue when pressed
            else -> Color.Black
        }
        
        // Draw black key
        drawRect(
            color = keyColor,
            topLeft = Offset(x, 0f),
            size = Size(width, blackKeyHeight)
        )
        
        // Draw subtle highlight on black keys
        if (!isPressed) {
            drawRect(
                color = Color(0xFF333333),
                topLeft = Offset(x + 2, 2f),
                size = Size(width - 4, blackKeyHeight * 0.3f)
            )
        }
    }
}

// Extension function for touch detection
private suspend fun androidx.compose.ui.input.pointer.PointerInputScope.detectPianoTouch(
    pianoKeys: List<PianoKey>,
    whiteKeyHeight: Float,
    blackKeyHeight: Float,
    onKeyPress: (Int) -> Unit,
    onKeyRelease: (Int) -> Unit
) {
    var pressedKey: Int? = null
    
    awaitPointerEventScope {
        while (true) {
            val event = awaitPointerEvent()
            val change = event.changes.firstOrNull()
            
            if (change != null) {
                val position = change.position
                val keyboardWidth = size.width
                val x = position.x
                val y = position.y
                
                when {
                    change.pressed -> {
                        val hitKey = findKeyAtPosition(
                            x, y, pianoKeys, keyboardWidth, whiteKeyHeight, blackKeyHeight
                        )
                        
                        if (hitKey != pressedKey) {
                            pressedKey?.let { onKeyRelease(it) }
                            hitKey?.let { 
                                onKeyPress(it)
                                pressedKey = it
                            }
                        }
                    }
                    
                    else -> {
                        pressedKey?.let { 
                            onKeyRelease(it)
                            pressedKey = null
                        }
                    }
                }
                
                if (change.pressed) {
                    change.consume()
                }
            } else {
                pressedKey?.let { 
                    onKeyRelease(it)
                    pressedKey = null
                }
            }
        }
    }
}

private fun findKeyAtPosition(
    x: Float,
    y: Float,
    pianoKeys: List<PianoKey>,
    keyboardWidth: Float,
    whiteKeyHeight: Float,
    blackKeyHeight: Float
): Int? {
    // Check black keys first (they are on top)
    pianoKeys.filter { it.isBlack }.forEach { key ->
        val keyX = (key.x / 100f) * keyboardWidth
        val keyWidth = (key.width / 100f) * keyboardWidth
        
        if (x >= keyX && x <= keyX + keyWidth && y <= blackKeyHeight) {
            return key.note
        }
    }
    
    // Check white keys
    pianoKeys.filter { !it.isBlack }.forEach { key ->
        val keyX = (key.x / 100f) * keyboardWidth
        val keyWidth = (key.width / 100f) * keyboardWidth
        
        if (x >= keyX && x <= keyX + keyWidth && y <= whiteKeyHeight) {
            return key.note
        }
    }
    
    return null
}
