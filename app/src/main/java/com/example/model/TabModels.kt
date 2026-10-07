package com.example.model

import java.util.UUID
import kotlin.math.abs
import kotlin.math.log2
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * Standard guitar string definitions (from High E = string index 0 down to Low E = string index 5).
 * Standard tablature notation convention:
 * - Line 1 (top of tab): High E (String 1)
 * - Line 2: B (String 2)
 * - Line 3: G (String 3)
 * - Line 4: D (String 4)
 * - Line 5: A (String 5)
 * - Line 6 (bottom of tab): Low E (String 6)
 */
data class TabStringDef(
    val index: Int,            // 0 = High E, 1 = B, 2 = G, 3 = D, 4 = A, 5 = Low E
    val stringNumber: Int,     // 1 to 6 (or 1 to 4 for bass)
    val label: String,         // "e", "B", "G", "D", "A", "E"
    val openNoteName: String,  // "E4", "B3", "G3", "D3", "A2", "E2"
    val openFrequency: Double  // Hz
)

object TabStringDefaults {
    val GUITAR_STRINGS = listOf(
        TabStringDef(0, 1, "e", "E4", 329.63),
        TabStringDef(1, 2, "B", "B3", 246.94),
        TabStringDef(2, 3, "G", "G3", 196.00),
        TabStringDef(3, 4, "D", "D3", 146.83),
        TabStringDef(4, 5, "A", "A2", 110.00),
        TabStringDef(5, 6, "E", "E2", 82.41)
    )

    val BASS_STRINGS = listOf(
        TabStringDef(0, 1, "G", "G2", 98.00),
        TabStringDef(1, 2, "D", "D2", 73.42),
        TabStringDef(2, 3, "A", "A1", 55.00),
        TabStringDef(3, 4, "E", "E1", 41.20)
    )
}

/**
 * A single note placed on the guitar tablature.
 */
data class TabNote(
    val id: String = UUID.randomUUID().toString(),
    val stringIndex: Int, // 0 to 5 (0 = High E)
    val fret: Int,        // 0 to 24
    val stepIndex: Int    // 0 to totalSteps - 1
) {
    /**
     * Calculates the audio frequency for playback using the 12-TET chromatic scale.
     */
    fun getFrequency(strings: List<TabStringDef> = TabStringDefaults.GUITAR_STRINGS): Double {
        val stringDef = strings.getOrNull(stringIndex)
            ?: TabStringDefaults.GUITAR_STRINGS.getOrElse(stringIndex.coerceIn(0, 5)) { TabStringDefaults.GUITAR_STRINGS[0] }
        return stringDef.openFrequency * 2.0.pow(fret / 12.0)
    }

    /**
     * Note letter & pitch representation, e.g. "E4", "G3", "A2".
     */
    fun getNoteName(strings: List<TabStringDef> = TabStringDefaults.GUITAR_STRINGS): String {
        val noteNames = arrayOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
        val freq = getFrequency(strings)
        val midiNote = (69.0 + 12.0 * log2(freq / 440.0)).roundToInt()
        val noteLetter = noteNames[(midiNote % 12 + 12) % 12]
        val octave = (midiNote / 12) - 1
        return "$noteLetter$octave"
    }
}

/**
 * Grid subdivision resolution for Note Quantisation.
 */
enum class TabGridResolution(
    val stepsPerBeat: Int,
    val stepsPerBar: Int,
    val displayName: String,
    val shortName: String
) {
    QUARTER(1, 4, "1/4 Notes", "1/4"),
    EIGHTH(2, 8, "1/8 Notes", "1/8"),
    SIXTEENTH(4, 16, "1/16 Notes", "1/16")
}

/**
 * Frequency to guitar string & fret mapping with natural position heuristic.
 */
object GuitarTabMapper {

    /**
     * Given an audio frequency in Hz, finds the best guitar string and fret number.
     * Prefers open positions (frets 0-7) for clean guitar tab readability.
     */
    fun mapFrequencyToGuitarFret(
        frequency: Double,
        strings: List<TabStringDef> = TabStringDefaults.GUITAR_STRINGS
    ): Pair<Int, Int>? {
        if (frequency < 70.0 || frequency > 1200.0) return null

        var bestMatch: Pair<Int, Int>? = null
        var minScore = Double.MAX_VALUE

        for (string in strings) {
            val openFreq = string.openFrequency
            val rawFret = 12.0 * log2(frequency / openFreq)
            val fret = rawFret.roundToInt()

            if (fret in 0..19) {
                val noteFreq = openFreq * 2.0.pow(fret / 12.0)
                val centsDiff = abs(1200.0 * log2(frequency / noteFreq))
                // Allow up to 45 cents tolerance for live guitar tuning
                if (centsDiff <= 45.0) {
                    // Penalty for higher frets so the tab prefers open/first position when notes are identical
                    val positionPenalty = if (fret <= 5) 0.0 else (fret - 5) * 3.0
                    val totalScore = centsDiff + positionPenalty
                    if (totalScore < minScore) {
                        minScore = totalScore
                        bestMatch = Pair(string.index, fret)
                    }
                }
            }
        }
        return bestMatch
    }

    /**
     * Generates standard ASCII tab text for sharing / exporting.
     */
    fun generateAsciiTab(
        title: String,
        bpm: Int,
        totalBars: Int,
        resolution: TabGridResolution,
        notes: List<TabNote>,
        strings: List<TabStringDef> = TabStringDefaults.GUITAR_STRINGS
    ): String {
        val totalSteps = totalBars * resolution.stepsPerBar
        val sb = StringBuilder()
        sb.appendLine("// ═══════════════════════════════════════════")
        sb.appendLine("// SHRED SHEETS — GUITAR TAB STUDIO")
        sb.appendLine("// Song: $title")
        sb.appendLine("// Tempo: $bpm BPM | Time: 4/4 | Grid: ${resolution.displayName}")
        sb.appendLine("// ═══════════════════════════════════════════")
        sb.appendLine()

        strings.forEach { str ->
            sb.append(str.label.padEnd(2, ' ')).append("|")
            for (step in 0 until totalSteps) {
                if (step > 0 && step % resolution.stepsPerBar == 0) {
                    sb.append("|") // Bar line
                }
                val noteOnString = notes.find { it.stringIndex == str.index && it.stepIndex == step }
                if (noteOnString != null) {
                    val fretStr = noteOnString.fret.toString()
                    sb.append(fretStr).append("-".repeat((2 - fretStr.length).coerceAtLeast(1)))
                } else {
                    sb.append("--")
                }
            }
            sb.appendLine("|")
        }
        sb.appendLine()
        sb.appendLine("Created with Shred Sheets Tab Studio")
        return sb.toString()
    }
}
