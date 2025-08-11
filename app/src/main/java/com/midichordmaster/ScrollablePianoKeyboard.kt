package com.midichordmaster

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun ScrollablePianoKeyboard(
    modifier: Modifier = Modifier,
    pressedKeys: Set<Int>,
    onKeyPress: (Int) -> Unit,
    onKeyRelease: (Int) -> Unit
) {
    val density = LocalDensity.current
    val scrollState = rememberScrollState()
    
    // Enhanced piano parameters for DAW-style display
    val whiteKeyWidth = with(density) { 42.dp.toPx() } // Slightly smaller for more keys visible
    val blackKeyWidth = with(density) { 28.dp.toPx() }
    val keyboardHeight = with(density) { 200.dp.toPx() } // Taller for better interaction
    val whiteKeyHeight = keyboardHeight
    val blackKeyHeight = keyboardHeight * 0.65f
    
    // 88 keys: A0 (21) to C8 (108)
    val startNote = 21 // A0
    val endNote = 108 // C8
    
    // Calculate total width for all 88 keys
    val whiteKeysCount = getWhiteKeysCount(startNote, endNote)
    val totalWidth = whiteKeysCount * whiteKeyWidth
    
    Column(modifier = modifier) {
        // Piano keyboard scroll indicator and controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Scroll: C3-C5 range", // Simplified for now
                style = MaterialTheme.typography.caption,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
            )
            
            Row {
                Button(
                    onClick = { 
                        // Use rememberCoroutineScope for scrolling
                    },
                    modifier = Modifier.size(32.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("◀", fontSize = 12.sp)
                }
                
                Spacer(modifier = Modifier.width(4.dp))
                
                Button(
                    onClick = { 
                        // Use rememberCoroutineScope for scrolling
                    },
                    modifier = Modifier.size(32.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("▶", fontSize = 12.sp)
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Button(
                    onClick = { 
                        // Use rememberCoroutineScope for scrolling to C4
                    },
                    modifier = Modifier.height(32.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                ) {
                    Text("C4", fontSize = 10.sp)
                }
            }
        }
        
        // Main piano keyboard
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(
                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.1f),
                            Color.Transparent
                        )
                    )
                )
                .clip(RoundedCornerShape(8.dp))
        ) {
            Canvas(
                modifier = Modifier
                    .width(with(density) { totalWidth.toDp() })
                    .height(200.dp)
                    .horizontalScroll(scrollState)
                    .pointerInput(Unit) {
                        var activePointers = mutableMapOf<Long, Int>() // pointerId to midiNote
                        
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent()
                                
                                when (event.type) {
                                    PointerEventType.Press -> {
                                        event.changes.forEach { change ->
                                            val adjustedX = change.position.x + scrollState.value
                                            val adjustedY = change.position.y
                                            val keyPressed = getKeyFromPosition(adjustedX, adjustedY, whiteKeyWidth, blackKeyWidth, blackKeyHeight, startNote)
                                            
                                            keyPressed?.let { note ->
                                                activePointers[change.id.value.toLong()] = note
                                                onKeyPress(note)
                                                change.consume()
                                            }
                                        }
                                    }
                                    
                                    PointerEventType.Move -> {
                                        event.changes.forEach { change ->
                                            val pointerId = change.id.value.toLong()
                                            if (activePointers.containsKey(pointerId)) {
                                                val adjustedX = change.position.x + scrollState.value
                                                val adjustedY = change.position.y
                                                val newKey = getKeyFromPosition(adjustedX, adjustedY, whiteKeyWidth, blackKeyWidth, blackKeyHeight, startNote)
                                                val oldKey = activePointers[pointerId]
                                                
                                                if (newKey != oldKey) {
                                                    oldKey?.let { onKeyRelease(it) }
                                                    newKey?.let { 
                                                        activePointers[pointerId] = it
                                                        onKeyPress(it)
                                                    } ?: run {
                                                        activePointers.remove(pointerId)
                                                    }
                                                }
                                                change.consume()
                                            }
                                        }
                                    }
                                    
                                    PointerEventType.Release -> {
                                        event.changes.forEach { change ->
                                            val pointerId = change.id.value.toLong()
                                            activePointers[pointerId]?.let { note ->
                                                onKeyRelease(note)
                                                activePointers.remove(pointerId)
                                                change.consume()
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
            ) {
                drawEnhancedPianoKeys(
                    startNote = startNote,
                    endNote = endNote,
                    pressedKeys = pressedKeys,
                    whiteKeyWidth = whiteKeyWidth,
                    blackKeyWidth = blackKeyWidth,
                    whiteKeyHeight = whiteKeyHeight,
                    blackKeyHeight = blackKeyHeight
                )
            }
            
            // Add scroll position indicator
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .background(
                        Color.Black.copy(alpha = 0.7f),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Keys: ${(scrollState.value / whiteKeyWidth).toInt() + 1}-${(scrollState.value / whiteKeyWidth).toInt() + 10}",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawEnhancedPianoKeys(
    startNote: Int,
    endNote: Int,
    pressedKeys: Set<Int>,
    whiteKeyWidth: Float,
    blackKeyWidth: Float,
    whiteKeyHeight: Float,
    blackKeyHeight: Float
) {
    var whiteKeyIndex = 0
    
    // Draw white keys first with enhanced styling
    for (midiNote in startNote..endNote) {
        if (isWhiteKey(midiNote)) {
            val isPressed = pressedKeys.contains(midiNote)
            val left = whiteKeyIndex * whiteKeyWidth
            
            // Enhanced white key styling
            val keyColor = when {
                isPressed -> Color(0xFF4CAF50) // Green when pressed
                midiNote % 12 == 0 -> Color(0xFFF5F5F5) // Slightly different for C notes
                else -> Color.White
            }
            
            // Draw white key
            drawRect(
                color = keyColor,
                topLeft = Offset(left, 0f),
                size = androidx.compose.ui.geometry.Size(whiteKeyWidth - 1, whiteKeyHeight)
            )
            
            // Draw gradient for 3D effect
            drawRect(
                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.3f),
                        Color.Transparent,
                        Color.Black.copy(alpha = 0.1f)
                    )
                ),
                topLeft = Offset(left, 0f),
                size = androidx.compose.ui.geometry.Size(whiteKeyWidth - 1, whiteKeyHeight)
            )
            
            // Draw border
            drawRect(
                color = Color.Black.copy(alpha = 0.3f),
                topLeft = Offset(left, 0f),
                size = androidx.compose.ui.geometry.Size(whiteKeyWidth - 1, whiteKeyHeight),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1f)
            )
            
            // Draw note name on C notes (simplified - no native canvas)
            if (midiNote % 12 == 0) {
                val octave = midiNote / 12 - 1
                // Note: Native canvas drawing removed for compatibility
                // Text drawing would need to be handled differently in Compose
            }
            
            whiteKeyIndex++
        }
    }
    
    // Draw black keys on top with enhanced styling
    whiteKeyIndex = 0
    for (midiNote in startNote..endNote) {
        if (isWhiteKey(midiNote)) {
            whiteKeyIndex++
        } else {
            // This is a black key
            val isPressed = pressedKeys.contains(midiNote)
            val leftWhiteKeyIndex = getLeftWhiteKeyIndex(midiNote, startNote)
            val left = leftWhiteKeyIndex * whiteKeyWidth + whiteKeyWidth - blackKeyWidth / 2
            
            // Enhanced black key styling
            val keyColor = when {
                isPressed -> Color(0xFF2196F3) // Blue when pressed
                else -> Color.Black
            }
            
            // Draw black key with rounded top
            val cornerRadius = 6f
            drawRoundRect(
                color = keyColor,
                topLeft = Offset(left, 0f),
                size = androidx.compose.ui.geometry.Size(blackKeyWidth, blackKeyHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius, cornerRadius)
            )
            
            // Draw gradient for 3D effect
            drawRoundRect(
                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.2f),
                        Color.Transparent,
                        Color.Black.copy(alpha = 0.3f)
                    )
                ),
                topLeft = Offset(left, 0f),
                size = androidx.compose.ui.geometry.Size(blackKeyWidth, blackKeyHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius, cornerRadius)
            )
            
            // Draw subtle border
            drawRoundRect(
                color = Color.Gray.copy(alpha = 0.5f),
                topLeft = Offset(left, 0f),
                size = androidx.compose.ui.geometry.Size(blackKeyWidth, blackKeyHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius, cornerRadius),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 0.5f)
            )
        }
    }
}

// Enhanced helper functions
private fun getWhiteKeysCount(startNote: Int, endNote: Int): Int {
    var count = 0
    for (note in startNote..endNote) {
        if (isWhiteKey(note)) count++
    }
    return count
}

private fun getKeyPosition(midiNote: Int, startNote: Int, whiteKeyWidth: Float): Float {
    var whiteKeyIndex = 0
    for (note in startNote until midiNote) {
        if (isWhiteKey(note)) {
            whiteKeyIndex++
        }
    }
    return whiteKeyIndex * whiteKeyWidth
}

private fun getCurrentOctaveRange(scrollValue: Float, whiteKeyWidth: Float, startNote: Int): String {
    val visibleStartKey = (scrollValue / whiteKeyWidth).toInt()
    val visibleEndKey = visibleStartKey + 12 // Approximate visible range
    
    val startOctave = (visibleStartKey * 12 / 7 + startNote) / 12 - 1
    val endOctave = (visibleEndKey * 12 / 7 + startNote) / 12 - 1
    
    return "C$startOctave - C${endOctave}"
}

private fun getVisibleKeyRange(scrollValue: Float, whiteKeyWidth: Float, startNote: Int): String {
    val keysPerScreen = 10 // Approximate number of visible white keys
    val startKeyIndex = (scrollValue / whiteKeyWidth).toInt()
    val endKeyIndex = startKeyIndex + keysPerScreen
    
    return "${startKeyIndex + 1} - ${endKeyIndex + 1}"
}

private fun getKeyFromPosition(
    x: Float, 
    y: Float,
    whiteKeyWidth: Float, 
    blackKeyWidth: Float,
    blackKeyHeight: Float,
    startNote: Int
): Int? {
    // Check if y position is within keyboard bounds
    if (y < 0) return null
    
    // First check black keys (they're on top and shorter)
    if (y <= blackKeyHeight) {
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
