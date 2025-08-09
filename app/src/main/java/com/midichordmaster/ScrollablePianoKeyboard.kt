package com.midichordmaster

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun ScrollablePianoKeyboard(
    modifier: Modifier = Modifier,
    pressedKeys: Set<Int>,
    onKeyPress: (Int) -> Unit,
    onKeyRelease: (Int) -> Unit
) {
    val density = LocalDensity.current
    var scrollOffset by remember { mutableStateOf(0f) }
    val scrollState = rememberScrollState()
    
    // Piano parameters
    val whiteKeyWidth = with(density) { 48.dp.toPx() }
    val blackKeyWidth = with(density) { 32.dp.toPx() }
    val keyboardHeight = with(density) { 120.dp.toPx() }
    val whiteKeyHeight = keyboardHeight
    val blackKeyHeight = keyboardHeight * 0.6f
    
    // 88 keys: A0 (21) to C8 (108)
    val startNote = 21 // A0
    val endNote = 108 // C8
    val totalKeys = endNote - startNote + 1
    
    // Calculate total width needed
    val whiteKeysPerOctave = 7
    val octaves = (totalKeys / 12.0)
    val totalWhiteKeys = (octaves * whiteKeysPerOctave).roundToInt()
    val totalWidth = totalWhiteKeys * whiteKeyWidth
    
    // Visible width
    val visibleWidth = with(density) { 
        androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp.dp.toPx()
    }
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
            .background(Color.Black)
    ) {
        Canvas(
            modifier = Modifier
                .width(with(density) { totalWidth.toDp() })
                .height(120.dp)
                .horizontalScroll(scrollState)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            val keyPressed = getKeyFromPosition(offset.x + scrollState.value, whiteKeyWidth, blackKeyWidth, startNote)
                            keyPressed?.let { onKeyPress(it) }
                        },
                        onDragEnd = {
                            // Release all keys on drag end
                            pressedKeys.forEach { onKeyRelease(it) }
                        },
                        onDrag = { change, _ ->
                            val keyPressed = getKeyFromPosition(change.position.x + scrollState.value, whiteKeyWidth, blackKeyWidth, startNote)
                            keyPressed?.let { 
                                if (!pressedKeys.contains(it)) {
                                    onKeyPress(it)
                                }
                            }
                        }
                    )
                }
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            when (event.type) {
                                PointerEventType.Press -> {
                                    event.changes.forEach { change ->
                                        val adjustedX = change.position.x + scrollState.value
                                        val keyPressed = getKeyFromPosition(adjustedX, whiteKeyWidth, blackKeyWidth, startNote)
                                        keyPressed?.let { onKeyPress(it) }
                                        change.consume()
                                    }
                                }
                                PointerEventType.Release -> {
                                    event.changes.forEach { change ->
                                        val adjustedX = change.position.x + scrollState.value
                                        val keyReleased = getKeyFromPosition(adjustedX, whiteKeyWidth, blackKeyWidth, startNote)
                                        keyReleased?.let { onKeyRelease(it) }
                                        change.consume()
                                    }
                                }
                            }
                        }
                    }
                }
        ) {
            drawPianoKeys(
                startNote = startNote,
                endNote = endNote,
                pressedKeys = pressedKeys,
                whiteKeyWidth = whiteKeyWidth,
                blackKeyWidth = blackKeyWidth,
                whiteKeyHeight = whiteKeyHeight,
                blackKeyHeight = blackKeyHeight
            )
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawPianoKeys(
    startNote: Int,
    endNote: Int,
    pressedKeys: Set<Int>,
    whiteKeyWidth: Float,
    blackKeyWidth: Float,
    whiteKeyHeight: Float,
    blackKeyHeight: Float
) {
    var whiteKeyIndex = 0
    
    // Draw white keys first
    for (midiNote in startNote..endNote) {
        if (isWhiteKey(midiNote)) {
            val isPressed = pressedKeys.contains(midiNote)
            val keyColor = if (isPressed) Color.Blue else Color.White
            val borderColor = Color.Black
            
            val left = whiteKeyIndex * whiteKeyWidth
            val right = left + whiteKeyWidth
            
            // Draw white key
            drawRect(
                color = keyColor,
                topLeft = Offset(left, 0f),
                size = androidx.compose.ui.geometry.Size(whiteKeyWidth, whiteKeyHeight)
            )
            
            // Draw border
            drawRect(
                color = borderColor,
                topLeft = Offset(left, 0f),
                size = androidx.compose.ui.geometry.Size(whiteKeyWidth, whiteKeyHeight),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
            )
            
            whiteKeyIndex++
        }
    }
    
    // Reset and draw black keys on top
    whiteKeyIndex = 0
    for (midiNote in startNote..endNote) {
        if (isWhiteKey(midiNote)) {
            whiteKeyIndex++
        } else {
            // This is a black key
            val isPressed = pressedKeys.contains(midiNote)
            val keyColor = if (isPressed) Color.Blue else Color.Black
            
            // Position black key between white keys
            val leftWhiteKeyIndex = getLeftWhiteKeyIndex(midiNote, startNote)
            val left = leftWhiteKeyIndex * whiteKeyWidth + whiteKeyWidth - blackKeyWidth / 2
            
            drawRect(
                color = keyColor,
                topLeft = Offset(left, 0f),
                size = androidx.compose.ui.geometry.Size(blackKeyWidth, blackKeyHeight)
            )
            
            // Draw border for black keys too
            drawRect(
                color = Color.Gray,
                topLeft = Offset(left, 0f),
                size = androidx.compose.ui.geometry.Size(blackKeyWidth, blackKeyHeight),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1f)
            )
        }
    }
}

private fun isWhiteKey(midiNote: Int): Boolean {
    val noteInOctave = midiNote % 12
    return when (noteInOctave) {
        0, 2, 4, 5, 7, 9, 11 -> true // C, D, E, F, G, A, B
        else -> false // C#, D#, F#, G#, A#
    }
}

private fun getLeftWhiteKeyIndex(midiNote: Int, startNote: Int): Int {
    var whiteKeyCount = 0
    for (note in startNote until midiNote) {
        if (isWhiteKey(note)) {
            whiteKeyCount++
        }
    }
    return whiteKeyCount
}

private fun getKeyFromPosition(
    x: Float, 
    whiteKeyWidth: Float, 
    blackKeyWidth: Float, 
    startNote: Int
): Int? {
    // First check black keys (they're on top)
    var whiteKeyIndex = 0
    for (midiNote in startNote..108) {
        if (isWhiteKey(midiNote)) {
            whiteKeyIndex++
        } else {
            // Check if click is on this black key
            val leftWhiteKeyIndex = getLeftWhiteKeyIndex(midiNote, startNote)
            val blackKeyLeft = leftWhiteKeyIndex * whiteKeyWidth + whiteKeyWidth - blackKeyWidth / 2
            val blackKeyRight = blackKeyLeft + blackKeyWidth
            
            if (x >= blackKeyLeft && x <= blackKeyRight) {
                return midiNote
            }
        }
    }
    
    // If not on black key, check white keys
    val whiteKeyIndexClicked = (x / whiteKeyWidth).toInt()
    var currentWhiteKeyIndex = 0
    
    for (midiNote in startNote..108) {
        if (isWhiteKey(midiNote)) {
            if (currentWhiteKeyIndex == whiteKeyIndexClicked) {
                return midiNote
            }
            currentWhiteKeyIndex++
        }
    }
    
    return null
}
