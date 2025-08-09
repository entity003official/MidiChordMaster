package com.midichordmaster

class ChordAnalyzer {
    
    data class ChordResult(
        val chordName: String,
        val noteNames: List<String>
    )
    
    private val chordPatterns = mapOf(
        // Major chords
        setOf(0, 4, 7) to "Maj",
        setOf(0, 4, 7, 11) to "Maj7", 
        setOf(0, 4, 7, 10) to "7",
        setOf(0, 4, 7, 9) to "add9",
        setOf(0, 4, 7, 14) to "9",
        setOf(0, 4, 7, 14, 17) to "11",
        setOf(0, 4, 7, 14, 21) to "13",
        setOf(0, 4, 7, 9, 14) to "Maj9",
        
        // Minor chords  
        setOf(0, 3, 7) to "m",
        setOf(0, 3, 7, 10) to "m7",
        setOf(0, 3, 7, 11) to "mMaj7",
        setOf(0, 3, 7, 9) to "madd9",
        setOf(0, 3, 7, 14) to "m9",
        
        // Diminished chords
        setOf(0, 3, 6) to "dim",
        setOf(0, 3, 6, 9) to "dim7",
        setOf(0, 3, 6, 10) to "m7b5", // half-diminished
        
        // Augmented chords
        setOf(0, 4, 8) to "aug",
        setOf(0, 4, 8, 11) to "augMaj7",
        
        // Suspended chords
        setOf(0, 5, 7) to "sus4",
        setOf(0, 2, 7) to "sus2",
        setOf(0, 5, 7, 10) to "7sus4",
        setOf(0, 2, 7, 10) to "7sus2",
        
        // Sixth chords
        setOf(0, 4, 7, 9) to "6",
        setOf(0, 3, 7, 9) to "m6",
        
        // Power chord
        setOf(0, 7) to "5"
    )
    
    private val noteNames = arrayOf(
        "C", "C#", "D", "D#", "E", "F", 
        "F#", "G", "G#", "A", "A#", "B"
    )
    
    fun analyzeChord(notes: Set<Int>): ChordResult {
        if (notes.isEmpty()) return ChordResult("", emptyList())
        
        val sortedNotes = notes.sorted()
        val noteNamesList = sortedNotes.map { getNoteNameFromMidi(it) }
        
        if (notes.size == 1) {
            return ChordResult(noteNamesList.first(), noteNamesList)
        }
        
        // Convert MIDI notes to relative intervals
        val rootNote = sortedNotes.first()
        val intervals = sortedNotes.map { (it - rootNote) % 12 }.toSet()
        
        // Try to find exact match
        chordPatterns[intervals]?.let { chordType ->
            val rootName = getNoteNameFromMidi(rootNote)
            val chordName = "$rootName$chordType"
            return ChordResult(chordName, noteNamesList)
        }
        
        // Try inversions by rotating the root
        for (possibleRoot in sortedNotes) {
            val invertedIntervals = sortedNotes.map { 
                var interval = (it - possibleRoot) % 12
                if (interval < 0) interval += 12
                interval
            }.toSet()
            
            chordPatterns[invertedIntervals]?.let { chordType ->
                val rootName = getNoteNameFromMidi(possibleRoot)
                val inversionText = when (sortedNotes.indexOf(possibleRoot)) {
                    0 -> ""
                    1 -> "/1st"
                    2 -> "/2nd" 
                    else -> "/inv"
                }
                val chordName = "$rootName$chordType$inversionText"
                return ChordResult(chordName, noteNamesList)
            }
        }
        
        // If no exact match found, return note names
        return ChordResult(noteNamesList.joinToString("-"), noteNamesList)
    }
    
    private fun findPartialChord(intervals: Set<Int>): String {
        // Check for major/minor thirds and perfect fifths
        val hasMinorThird = intervals.contains(3)
        val hasMajorThird = intervals.contains(4)
        val hasPerfectFifth = intervals.contains(7)
        val hasFlatFifth = intervals.contains(6)
        val hasSharpFifth = intervals.contains(8)
        
        return when {
            hasMajorThird && hasPerfectFifth -> "Major"
            hasMinorThird && hasPerfectFifth -> "Minor"
            hasMajorThird && hasFlatFifth -> "Diminished"
            hasMajorThird && hasSharpFifth -> "Augmented"
            hasMinorThird && hasFlatFifth -> "Diminished"
            hasPerfectFifth && !hasMajorThird && !hasMinorThird -> "5 (Power chord)"
            hasMajorThird && !hasPerfectFifth -> "Major (no 5th)"
            hasMinorThird && !hasPerfectFifth -> "Minor (no 5th)"
            else -> "Unknown"
        }
    }
    
    private fun getNoteNameFromMidi(midiNote: Int): String {
        val noteIndex = midiNote % 12
        val octave = midiNote / 12 - 1
        return "${noteNames[noteIndex]}$octave"
    }
    
    fun getChordNotes(chordName: String): Set<Int>? {
        // This method could be used to generate chord notes from chord name
        // For now, return null as it's mainly for analysis
        return null
    }
}
