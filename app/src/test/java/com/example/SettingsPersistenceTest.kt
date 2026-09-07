package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.SettingsPreferences
import com.example.model.AccentColor
import com.example.model.AppSettings
import com.example.model.ColorMode
import com.example.model.InstrumentRepository
import com.example.model.InstrumentType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SettingsPersistenceTest {

    @Test
    fun `default guitar anagram is Eddie Ate Dynamite Good Bye Eddie`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = SettingsPreferences(context)
        val loaded = prefs.loadSettings()
        assertEquals(InstrumentType.GUITAR, loaded.selectedInstrument)
        assertEquals("Eddie Ate Dynamite Good Bye Eddie", loaded.currentAnagramSentence)
    }

    @Test
    fun `settings and chosen anagram persist across sessions`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = SettingsPreferences(context)

        val customSettings = AppSettings(
            selectedInstrument = InstrumentType.BASS,
            selectedTuningId = "bass_standard",
            accentColor = AccentColor.PURPLE,
            colorMode = ColorMode.DARK,
            soundEffectsEnabled = false,
            hapticsEnabled = true,
            isOnboardingCompleted = true,
            currentAnagramSentence = "Elephants And Donkeys Grow"
        )

        prefs.saveSettings(customSettings)

        // Read again from fresh preferences instance to simulate returning user opening app
        val returningUserPrefs = SettingsPreferences(context)
        val reloaded = returningUserPrefs.loadSettings()

        assertEquals(InstrumentType.BASS, reloaded.selectedInstrument)
        assertEquals("bass_standard", reloaded.selectedTuningId)
        assertEquals(AccentColor.PURPLE, reloaded.accentColor)
        assertEquals(ColorMode.DARK, reloaded.colorMode)
        assertEquals(false, reloaded.soundEffectsEnabled)
        assertTrue(reloaded.isOnboardingCompleted)
        assertEquals("Elephants And Donkeys Grow", reloaded.currentAnagramSentence)
    }

    @Test
    fun `guitar standard tuning repository lists Eddie Ate Dynamite Good Bye Eddie first`() {
        val guitarConfig = InstrumentRepository.getConfig(InstrumentType.GUITAR)
        val standardTuning = guitarConfig.tunings.first()
        assertEquals("guitar_standard", standardTuning.id)
        assertEquals("Eddie Ate Dynamite Good Bye Eddie", standardTuning.anagrams.first())
    }
}
