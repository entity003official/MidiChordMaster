package com.midichordmaster

data class MidiEvent(
    val type: Type,
    val note: Int,
    val velocity: Int,
    val timestamp: Long = System.currentTimeMillis()
) {
    enum class Type {
        NOTE_ON,
        NOTE_OFF,
        CONTROL_CHANGE,
        PITCH_BEND
    }
}
