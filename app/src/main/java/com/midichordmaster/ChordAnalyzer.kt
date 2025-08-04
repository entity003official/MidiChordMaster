package com.midichordmaster

class ChordAnalyzer {
    
    private val chordPatterns = mapOf(
        // Major chords
        setOf(0, 4, 7) to "Major",
        setOf(0, 4, 7, 11) to "Major 7",
        setOf(0, 4, 7, 10) to "Dominant 7",
        setOf(0, 4, 7, 9) to "Major add9",
        
        // Minor chords
        setOf(0, 3, 7) to "Minor",
        setOf(0, 3, 7, 10) to "Minor 7",
        setOf(0, 3, 7, 11) to "Minor Major 7",
        setOf(0, 3, 7, 9) to "Minor add9",
        
        // Diminished chords
        setOf(0, 3, 6) to "Diminished",
        setOf(0, 3, 6, 9) to "Diminished 7",
        
        // Augmented chords
        setOf(0, 4, 8) to "Augmented",
        
        // Suspended chords
        setOf(0, 5, 7) to "Sus4",
        setOf(0, 2, 7) to "Sus2",
        
        // Extended chords
        setOf(0, 4, 7, 10, 2) to "9",
        setOf(0, 4, 7, 10, 2, 5) to "11",
        setOf(0, 4, 7, 10, 2, 9) to "13",
        
        // Power chord
        setOf(0, 7) to "5"
    )
    
    private val noteNames = arrayOf(
        "C", "C#", "D", "D#", "E", "F", 
        "F#", "G", "G#", "A", "A#", "B"
    )
    
    fun analyzeChord(notes: Set<Int>): String {
        if (notes.isEmpty()) return ""
        if (notes.size == 1) {
            return "${getNoteNameFromMidi(notes.first())} (single note)"
        }
        
        // Convert MIDI notes to relative intervals
        val sortedNotes = notes.sorted()
        val rootNote = sortedNotes.first()
        val intervals = sortedNotes.map { (it - rootNote) % 12 }.toSet()
        
        // Try to find exact match
        chordPatterns[intervals]?.let { chordType ->
            return "${getNoteNameFromMidi(rootNote)} $chordType"
        }
        
        // Try inversions by rotating the root
        for (possibleRoot in sortedNotes) {
            val invertedIntervals = sortedNotes.map { 
                var interval = (it - possibleRoot) % 12
                if (interval < 0) interval += 12
                interval
            }.toSet()
            
            chordPatterns[invertedIntervals]?.let { chordType ->
                val inversion = when (sortedNotes.indexOf(possibleRoot)) {
                    0 -> ""
                    1 -> " (1st inversion)"
                    2 -> " (2nd inversion)"
                    else -> " (inversion)"
                }
                return "${getNoteNameFromMidi(possibleRoot)} $chordType$inversion"
            }
        }
        
        // If no exact match found, try to identify partial chords
        val partialMatch = findPartialChord(intervals)
        if (partialMatch.isNotEmpty()) {
            return "${getNoteNameFromMidi(rootNote)} $partialMatch"
        }
        
        // Return notes if no chord pattern matches
        val notesList = sortedNotes.joinToString("-") { getNoteNameFromMidi(it) }
        return notesList
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
