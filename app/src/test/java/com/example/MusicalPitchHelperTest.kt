package com.example

import com.example.model.InstrumentRepository
import com.example.model.InstrumentType
import com.example.model.MusicalPitchHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MusicalPitchHelperTest {

    @Test
    fun `exact A4 440Hz calculates note A4 with 0 cents diff`() {
        val result = MusicalPitchHelper.frequencyToPitch(440.0)
        assertEquals("A", result.noteLetter)
        assertEquals(4, result.octave)
        assertEquals("A4", result.noteName)
        assertEquals(0.0, result.centsDiff, 0.5)
        assertTrue(result.isInTune)
    }

    @Test
    fun `low E guitar string 82_41Hz calculates note E2`() {
        val result = MusicalPitchHelper.frequencyToPitch(82.41)
        assertEquals("E", result.noteLetter)
        assertEquals(2, result.octave)
        assertEquals("E2", result.noteName)
        assertTrue(result.isInTune)
    }

    @Test
    fun `slightly flat pitch calculates negative cents and isClose`() {
        val result = MusicalPitchHelper.frequencyToPitch(438.0)
        assertEquals("A", result.noteLetter)
        assertEquals(4, result.octave)
        assertTrue(result.centsDiff < 0)
        assertFalse(result.isInTune)
        assertTrue(result.isClose)
    }

    @Test
    fun `closest string matches correct guitar string`() {
        val guitarConfig = InstrumentRepository.getConfig(InstrumentType.GUITAR)
        val standardTuning = guitarConfig.tunings.first()

        val lowE = MusicalPitchHelper.findClosestString(82.5, standardTuning.strings)
        assertEquals(6, lowE?.stringNumber)
        assertEquals("E", lowE?.noteLetter)

        val highE = MusicalPitchHelper.findClosestString(330.0, standardTuning.strings)
        assertEquals(1, highE?.stringNumber)
        assertEquals("E", highE?.noteLetter)
    }

    @Test
    fun `instrument repository provides all required instruments`() {
        val instruments = InstrumentType.entries
        assertEquals(4, instruments.size)
        assertTrue(instruments.contains(InstrumentType.GUITAR))
        assertTrue(instruments.contains(InstrumentType.BASS))
        assertTrue(instruments.contains(InstrumentType.UKULELE))
        assertTrue(instruments.contains(InstrumentType.BANJO))
    }

    @Test
    fun `deep bass E1 41_2Hz and D1 36_71Hz calculate correct bass notes`() {
        val bassE = MusicalPitchHelper.frequencyToPitch(41.20)
        assertEquals("E", bassE.noteLetter)
        assertEquals(1, bassE.octave)
        assertTrue(bassE.isInTune)

        val dropD = MusicalPitchHelper.frequencyToPitch(36.71)
        assertEquals("D", dropD.noteLetter)
        assertEquals(1, dropD.octave)
        assertTrue(dropD.isInTune)
    }

    @Test
    fun `MPM algorithm accurately detects synthesized sine wave for A4 440Hz and Bass E1 41Hz`() {
        val sampleRate = 44100
        val bufferLength = 8192
        val pitchDetector = com.example.audio.PitchDetector()

        // 1. Synthesize 440Hz wave
        val sineA4 = FloatArray(bufferLength)
        for (i in 0 until bufferLength) {
            sineA4[i] = kotlin.math.sin(2.0 * Math.PI * 440.0 * i / sampleRate).toFloat()
        }
        val (freqA4, confA4) = pitchDetector.detectPitchMPM(sineA4, bufferLength, sampleRate)
        assertTrue("Expected confidence > 0.8 but was $confA4", confA4 > 0.8)
        assertEquals(440.0, freqA4, 0.5)

        // 2. Synthesize deep bass Low E1 (~41.2Hz)
        val sineBassE = FloatArray(bufferLength)
        for (i in 0 until bufferLength) {
            sineBassE[i] = kotlin.math.sin(2.0 * Math.PI * 41.2 * i / sampleRate).toFloat()
        }
        val (freqBass, confBass) = pitchDetector.detectPitchMPM(sineBassE, bufferLength, sampleRate)
        assertTrue("Expected bass confidence > 0.75 but was $confBass", confBass > 0.75)
        assertEquals(41.2, freqBass, 0.3)

        // 3. Synthesize Guitar Low E (82.41 Hz) with strong 2nd (164.8 Hz) and 3rd (247.2 Hz) harmonics
        val guitarLowE = FloatArray(bufferLength)
        for (i in 0 until bufferLength) {
            val t = i.toDouble() / sampleRate
            guitarLowE[i] = (
                0.55 * kotlin.math.sin(2.0 * Math.PI * 82.41 * t) +
                0.75 * kotlin.math.sin(2.0 * Math.PI * 164.82 * t) +
                0.40 * kotlin.math.sin(2.0 * Math.PI * 247.23 * t)
            ).toFloat()
        }
        val (freqGuitarE, confGuitarE) = pitchDetector.detectPitchMPM(guitarLowE, bufferLength, sampleRate)
        assertTrue("Expected guitar confidence > 0.6 but was $confGuitarE", confGuitarE > 0.6)
        assertEquals(82.41, freqGuitarE, 0.5)

        // 4. Test evaluateAgainstString
        val standardE2 = com.example.model.InstrumentRepository.GUITAR_STANDARD.strings.first()
        val evaluatedInTune = MusicalPitchHelper.evaluateAgainstString(82.5, standardE2, 0.8, 0.9)
        assertTrue(evaluatedInTune.isInTune)
        assertTrue(kotlin.math.abs(evaluatedInTune.centsDiff) < 2.0)

        val evaluatedSharp = MusicalPitchHelper.evaluateAgainstString(84.0, standardE2, 0.8, 0.9)
        assertFalse(evaluatedSharp.isInTune)
        assertTrue(evaluatedSharp.centsDiff > 30.0) // ~33 cents sharp
    }
}
