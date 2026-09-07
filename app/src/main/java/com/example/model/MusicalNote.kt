package com.example.model

import kotlin.math.abs
import kotlin.math.log2
import kotlin.math.pow
import kotlin.math.roundToInt

data class PitchResult(
    val frequency: Double,
    val noteName: String,
    val noteLetter: String,
    val octave: Int,
    val targetFrequency: Double,
    val centsDiff: Double,     // Negative = flat (tune up), Positive = sharp (tune down)
    val isInTune: Boolean,     // Within +/- 4 cents tolerance
    val isClose: Boolean,      // Within +/- 15 cents
    val amplitude: Double,     // Signal strength (0.0 to 1.0)
    val confidence: Double     // Algorithm confidence (0.0 to 1.0)
) {
    companion object {
        val EMPTY = PitchResult(
            frequency = 0.0,
            noteName = "--",
            noteLetter = "-",
            octave = 0,
            targetFrequency = 0.0,
            centsDiff = 0.0,
            isInTune = false,
            isClose = false,
            amplitude = 0.0,
            confidence = 0.0
        )
    }
}

object MusicalPitchHelper {

    private val NOTE_NAMES = arrayOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")

    // Reference standard A4 = 440 Hz
    const val A4_FREQ = 440.0
    const val IN_TUNE_TOLERANCE_CENTS = 4.0
    const val CLOSE_TOLERANCE_CENTS = 15.0

    /**
     * Converts a frequency (Hz) to scientific pitch notation and cents offset.
     */
    fun frequencyToPitch(freq: Double, amplitude: Double = 1.0, confidence: Double = 1.0): PitchResult {
        if (freq <= 20.0 || freq > 4000.0) {
            return PitchResult(
                frequency = 0.0,
                noteName = "--",
                noteLetter = "-",
                octave = 0,
                targetFrequency = 0.0,
                centsDiff = 0.0,
                isInTune = false,
                isClose = false,
                amplitude = amplitude,
                confidence = 0.0
            )
        }

        // MIDI Note calculation: 69 + 12 * log2(freq / 440)
        val midiNote = 69.0 + 12.0 * log2(freq / A4_FREQ)
        val roundedMidi = midiNote.roundToInt()
        val targetFreq = A4_FREQ * 2.0.pow((roundedMidi - 69) / 12.0)
        val cents = 1200.0 * log2(freq / targetFreq)

        val noteIndex = (roundedMidi % 12 + 12) % 12
        val octave = (roundedMidi / 12) - 1
        val noteLetter = NOTE_NAMES[noteIndex]
        val noteName = "$noteLetter$octave"

        val inTune = abs(cents) <= IN_TUNE_TOLERANCE_CENTS
        val close = abs(cents) <= CLOSE_TOLERANCE_CENTS

        return PitchResult(
            frequency = freq,
            noteName = noteName,
            noteLetter = noteLetter,
            octave = octave,
            targetFrequency = targetFreq,
            centsDiff = cents,
            isInTune = inTune,
            isClose = close,
            amplitude = amplitude,
            confidence = confidence
        )
    }

    /**
     * Calculates cents difference against an explicitly selected target instrument string.
     */
    fun evaluateAgainstString(detectedFreq: Double, targetString: InstrumentString, amplitude: Double, confidence: Double): PitchResult {
        if (detectedFreq <= 20.0) {
            return frequencyToPitch(0.0, amplitude, 0.0)
        }

        val targetFreq = targetString.targetFrequency
        val cents = 1200.0 * log2(detectedFreq / targetFreq)
        val inTune = abs(cents) <= IN_TUNE_TOLERANCE_CENTS
        val close = abs(cents) <= CLOSE_TOLERANCE_CENTS

        return PitchResult(
            frequency = detectedFreq,
            noteName = targetString.noteName,
            noteLetter = targetString.noteLetter,
            octave = targetString.octave,
            targetFrequency = targetFreq,
            centsDiff = cents,
            isInTune = inTune,
            isClose = close,
            amplitude = amplitude,
            confidence = confidence
        )
    }

    /**
     * Calculates the deviation in musical cents between a frequency and target frequency.
     */
    fun calculateCentsDiff(freq: Double, targetFreq: Double): Double {
        if (freq <= 0.0 || targetFreq <= 0.0) return 0.0
        return 1200.0 * log2(freq / targetFreq)
    }

    /**
     * Finds the closest instrument string in a tuning for auto-detect mode.
     */
    fun findClosestString(freq: Double, strings: List<InstrumentString>): InstrumentString {
        if (strings.isEmpty()) return InstrumentRepository.GUITAR_STANDARD.strings.first()
        return strings.minByOrNull { string ->
            val cents = abs(1200.0 * log2(freq / string.targetFrequency))
            cents
        } ?: strings.first()
    }
}
