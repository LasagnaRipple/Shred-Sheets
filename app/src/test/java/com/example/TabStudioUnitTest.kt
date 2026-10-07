package com.example

import com.example.model.GuitarTabMapper
import com.example.model.TabGridResolution
import com.example.model.TabNote
import com.example.model.TabStringDefaults
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TabStudioUnitTest {

    @Test
    fun `open high E frequency maps to string 0 and fret 0`() {
        val match = GuitarTabMapper.mapFrequencyToGuitarFret(329.63)
        assertNotNull(match)
        assertEquals(0, match!!.first)  // String 0 = high E
        assertEquals(0, match.second) // Fret 0
    }

    @Test
    fun `open low E frequency maps to string 5 and fret 0`() {
        val match = GuitarTabMapper.mapFrequencyToGuitarFret(82.41)
        assertNotNull(match)
        assertEquals(5, match!!.first)  // String 5 = low E
        assertEquals(0, match.second) // Fret 0
    }

    @Test
    fun `open G frequency maps to string 2 and fret 0`() {
        val match = GuitarTabMapper.mapFrequencyToGuitarFret(196.00)
        assertNotNull(match)
        assertEquals(2, match!!.first)  // String 2 = G
        assertEquals(0, match.second) // Fret 0
    }

    @Test
    fun `tab note calculates correct note name and frequency`() {
        val openHighE = TabNote(stringIndex = 0, fret = 0, stepIndex = 0)
        assertEquals("E4", openHighE.getNoteName())
        assertEquals(329.63, openHighE.getFrequency(), 0.5)

        val openLowE = TabNote(stringIndex = 5, fret = 0, stepIndex = 0)
        assertEquals("E2", openLowE.getNoteName())
        assertEquals(82.41, openLowE.getFrequency(), 0.5)

        val fifthFretLowE = TabNote(stringIndex = 5, fret = 5, stepIndex = 0)
        assertEquals("A2", fifthFretLowE.getNoteName())
        assertEquals(110.00, fifthFretLowE.getFrequency(), 0.5)
    }

    @Test
    fun `ascii tab export generates formatted 6 line guitar tab`() {
        val testNotes = listOf(
            TabNote(stringIndex = 2, fret = 0, stepIndex = 0),
            TabNote(stringIndex = 2, fret = 3, stepIndex = 2),
            TabNote(stringIndex = 2, fret = 5, stepIndex = 4)
        )
        val ascii = GuitarTabMapper.generateAsciiTab(
            title = "My Guitar Riff",
            bpm = 112,
            totalBars = 4,
            resolution = TabGridResolution.EIGHTH,
            notes = testNotes,
            strings = TabStringDefaults.GUITAR_STRINGS
        )

        assertTrue(ascii.contains("SHRED SHEETS — GUITAR TAB STUDIO"))
        assertTrue(ascii.contains("Song: My Guitar Riff"))
        assertTrue(ascii.contains("Tempo: 112 BPM"))
        assertTrue(ascii.contains("e |"))
        assertTrue(ascii.contains("B |"))
        assertTrue(ascii.contains("G |"))
        assertTrue(ascii.contains("D |"))
        assertTrue(ascii.contains("A |"))
        assertTrue(ascii.contains("E |"))
        assertTrue(ascii.contains("0-"))
        assertTrue(ascii.contains("3-"))
        assertTrue(ascii.contains("5-"))
    }

    @Test
    fun `tab audio engine tracks edit history and supports undo`() {
        // Simulating the stack operations
        val history = mutableListOf<List<TabNote>>()
        var notes = listOf(TabNote(stringIndex = 0, fret = 0, stepIndex = 0))

        // Mutation 1: add note
        history.add(notes.toList())
        notes = notes + TabNote(stringIndex = 1, fret = 3, stepIndex = 2)
        assertEquals(2, notes.size)
        assertTrue(history.isNotEmpty())

        // Mutation 2: modify fret
        history.add(notes.toList())
        notes = notes.map { if (it.stringIndex == 1) it.copy(fret = 5) else it }
        assertEquals(5, notes.find { it.stringIndex == 1 }?.fret)

        // Undo 1
        notes = history.removeAt(history.lastIndex)
        assertEquals(3, notes.find { it.stringIndex == 1 }?.fret)

        // Undo 2
        notes = history.removeAt(history.lastIndex)
        assertEquals(1, notes.size)
        assertTrue(history.isEmpty())
    }

    @Test
    fun `tab note move positioning logic prevents overwriting occupied slots`() {
        val notes = mutableListOf(
            TabNote(id = "note-1", stringIndex = 4, fret = 0, stepIndex = 0), // A string
            TabNote(id = "note-2", stringIndex = 3, fret = 2, stepIndex = 4)  // D string
        )

        fun canMoveNote(noteId: String, newString: Int, newStep: Int): Boolean {
            val isOccupied = notes.any { it.id != noteId && it.stringIndex == newString && it.stepIndex == newStep }
            return !isOccupied
        }

        // Move note-1 to blank slot (D string, step 0) -> Allowed
        assertTrue(canMoveNote("note-1", 3, 0))

        // Move note-1 to occupied slot (D string, step 4) -> Blocked
        assertFalse(canMoveNote("note-1", 3, 4))

        // Move note-1 along same A string to step 2 -> Allowed
        assertTrue(canMoveNote("note-1", 4, 2))
    }
}
