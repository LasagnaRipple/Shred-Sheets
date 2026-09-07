package com.example

import com.example.model.AccentColor
import com.example.model.AppSettings
import com.example.model.AppStyleTheme
import com.example.model.ColorMode
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun defaultSettings_isDarkModeWithYellowAccent() {
        val settings = AppSettings()
        assertEquals(ColorMode.DARK, settings.colorMode)
        assertEquals(AccentColor.YELLOW, settings.accentColor)
        assertEquals(AppStyleTheme.ROCK, settings.styleTheme)
    }

    @Test
    fun darkMode_accentAvailabilityRules() {
        val darkAccents = AccentColor.entries.filter { it.darkAllowed }
        assertTrue("Yellow must be available in dark mode", darkAccents.contains(AccentColor.YELLOW))
        assertTrue("Orange must be available in dark mode", darkAccents.contains(AccentColor.ORANGE))
        assertTrue("Purple must be available in dark mode", darkAccents.contains(AccentColor.PURPLE))
        assertTrue("Green must be available in dark mode", darkAccents.contains(AccentColor.GREEN))
        assertTrue("Blue must be available in dark mode", darkAccents.contains(AccentColor.BLUE))
        assertTrue("Red must be available in dark mode", darkAccents.contains(AccentColor.RED))
        assertTrue("White must be available in dark mode", darkAccents.contains(AccentColor.WHITE))
        assertFalse("Black must NOT be available in dark mode", darkAccents.contains(AccentColor.BLACK))
    }

    @Test
    fun lightMode_accentAvailabilityRules() {
        val lightAccents = AccentColor.entries.filter { it.lightAllowed }
        assertFalse("Yellow must NOT be available in light mode (readability)", lightAccents.contains(AccentColor.YELLOW))
        assertFalse("White must NOT be available in light mode (readability)", lightAccents.contains(AccentColor.WHITE))
        assertTrue("Orange must be available in light mode", lightAccents.contains(AccentColor.ORANGE))
        assertTrue("Purple must be available in light mode", lightAccents.contains(AccentColor.PURPLE))
        assertTrue("Green must be available in light mode", lightAccents.contains(AccentColor.GREEN))
        assertTrue("Blue must be available in light mode", lightAccents.contains(AccentColor.BLUE))
        assertTrue("Red must be available in light mode", lightAccents.contains(AccentColor.RED))
        assertTrue("Black must be available in light mode", lightAccents.contains(AccentColor.BLACK))
    }

    @Test
    fun legacyThemeMapping_preservesRockAndPop() {
        val rockMapped = AppSettings(accentColor = AccentColor.YELLOW)
        assertEquals(AppStyleTheme.ROCK, rockMapped.styleTheme)

        val popMapped = AppSettings(accentColor = AccentColor.BLUE)
        assertEquals(AppStyleTheme.POP, popMapped.styleTheme)
    }

    @Test
    fun tuningMode_shortName_simplifiesName() {
        val dropD = com.example.model.InstrumentRepository.GUITAR_DROP_D
        assertEquals("Drop D", dropD.shortName)

        val standard = com.example.model.InstrumentRepository.GUITAR_STANDARD
        assertEquals("Standard", standard.shortName)

        val halfStep = com.example.model.InstrumentRepository.GUITAR_HALF_STEP
        assertEquals("Half Step Down", halfStep.shortName)
    }

    @Test
    fun allTunings_haveKidFriendlyAnagramsMatchingStringNotes() {
        com.example.model.InstrumentRepository.INSTRUMENTS.forEach { config ->
            config.tunings.forEach { tuning ->
                assertTrue("Tuning ${tuning.id} should have preloaded anagrams", tuning.anagrams.isNotEmpty())
                tuning.anagrams.forEach { anagram ->
                    val words = anagram.split(" ").filter { it.isNotBlank() }
                    assertEquals(
                        "Anagram '$anagram' in tuning ${tuning.id} must match string count (${tuning.strings.size})",
                        tuning.strings.size,
                        words.size
                    )
                }
            }
        }
    }

    @Test
    fun dropDTuning_firstWordStartsWithD_notEvery() {
        val dropD = com.example.model.InstrumentRepository.GUITAR_DROP_D
        val firstStringNote = dropD.strings.first().noteLetter // "D"
        val defaultAnagram = dropD.anagrams.first()
        val firstWord = defaultAnagram.split(" ").first()

        assertTrue(
            "Drop D first word should start with 'D', got '$firstWord'",
            firstWord.startsWith("D", ignoreCase = true)
        )
        assertNotEquals("Every", firstWord)
    }

    @Test
    fun aStandardTuning_isConfiguredWithMatchingAnagrams() {
        val aStandard = com.example.model.InstrumentRepository.GUITAR_A_STANDARD
        assertEquals("A Standard", aStandard.shortName)
        assertEquals("A Standard (A D G C E A)", aStandard.name)
        assertEquals(listOf("A", "D", "G", "C", "E", "A"), aStandard.strings.map { it.noteLetter })
        assertTrue(aStandard.anagrams.isNotEmpty())
        aStandard.anagrams.forEach { anagram ->
            val words = anagram.split(" ").filter { it.isNotBlank() }
            assertEquals(6, words.size)
            assertTrue(words[0].startsWith("A", ignoreCase = true))
            assertTrue(words[1].startsWith("D", ignoreCase = true))
            assertTrue(words[2].startsWith("G", ignoreCase = true))
            assertTrue(words[3].startsWith("C", ignoreCase = true))
            assertTrue(words[4].startsWith("E", ignoreCase = true))
            assertTrue(words[5].startsWith("A", ignoreCase = true))
        }
    }
}

