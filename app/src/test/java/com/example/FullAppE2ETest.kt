package com.example

import android.app.Application
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.example.data.SettingsPreferences
import com.example.model.AccentColor
import com.example.model.AppSettings
import com.example.model.AppStyleTheme
import com.example.model.ColorMode
import com.example.model.DrumStyle
import com.example.model.InstrumentType
import com.example.model.MetronomeSoundMode
import com.example.model.PitchResult
import com.example.model.RecordingState
import com.example.ui.theme.ShredSheetsTheme
import com.example.viewmodel.AppTab
import com.example.viewmodel.MainViewModel
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class FullAppE2ETest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var application: Application
    private lateinit var settingsPreferences: SettingsPreferences
    private lateinit var viewModel: MainViewModel

    @Before
    fun setup() {
        application = ApplicationProvider.getApplicationContext()
        settingsPreferences = SettingsPreferences(application)

        // Ensure onboarding is marked completed so the main app content loads directly
        val testSettings = AppSettings(
            selectedInstrument = InstrumentType.GUITAR,
            selectedTuningId = "guitar_standard",
            accentColor = AccentColor.YELLOW,
            colorMode = ColorMode.DARK,
            soundEffectsEnabled = true,
            hapticsEnabled = true,
            isOnboardingCompleted = true,
            currentAnagramSentence = "Eddie Ate Dynamite Good Bye Eddie"
        )
        settingsPreferences.saveSettings(testSettings)

        viewModel = MainViewModel(application)
    }

    @Test
    fun e2e_fullNavigation_tabSwitching() {
        composeTestRule.setContent {
            val settings = viewModel.settings.value
            ShredSheetsTheme(
                accentColor = settings.accentColor,
                colorMode = settings.colorMode
            ) {
                MainAppContent(
                    viewModel = viewModel,
                    onRequestPermission = {}
                )
            }
        }

        composeTestRule.waitForIdle()

        // 1. Initially on Tuner screen
        assertEquals(AppTab.TUNER, viewModel.currentTab.value)
        composeTestRule.onNodeWithTag("header_shred_sheets_logo").assertExists()
        composeTestRule.onNodeWithTag("pitch_ruler_gauge").assertExists()
        composeTestRule.onNodeWithTag("tuner_status_badge").assertExists()

        // 2. Switch to Chords tab
        composeTestRule.onNodeWithTag("nav_tab_chords").performClick()
        composeTestRule.waitForIdle()
        assertEquals(AppTab.CHORDS, viewModel.currentTab.value)
        composeTestRule.onNodeWithTag("strum_chord_button").assertExists()
        composeTestRule.onNodeWithTag("fretboard_diagram").assertExists()

        // 3. Switch to Metronome tab
        composeTestRule.onNodeWithTag("nav_tab_metronome").performClick()
        composeTestRule.waitForIdle()
        assertEquals(AppTab.METRONOME, viewModel.currentTab.value)
        composeTestRule.onNodeWithTag("bpm_text_display").assertExists()
        composeTestRule.onNodeWithTag("metronome_play_button").assertExists()

        // 4. Switch back to Tuner tab
        composeTestRule.onNodeWithTag("nav_tab_tuner").performClick()
        composeTestRule.waitForIdle()
        assertEquals(AppTab.TUNER, viewModel.currentTab.value)
        composeTestRule.onNodeWithTag("pitch_ruler_gauge").assertExists()
    }

    @Test
    fun e2e_tuner_stringSelectionAndSoundToggle() {
        composeTestRule.setContent {
            val settings = viewModel.settings.value
            ShredSheetsTheme(
                accentColor = settings.accentColor,
                colorMode = settings.colorMode
            ) {
                MainAppContent(
                    viewModel = viewModel,
                    onRequestPermission = {}
                )
            }
        }

        composeTestRule.waitForIdle()

        // Sound toggle button
        val initialSound = viewModel.settings.value.soundEffectsEnabled
        composeTestRule.onNodeWithTag("sound_toggle_button").performClick()
        composeTestRule.waitForIdle()
        assertEquals(!initialSound, viewModel.settings.value.soundEffectsEnabled)

        // Select Low E (string 6)
        composeTestRule.onNodeWithTag("string_pill_6").performClick()
        composeTestRule.waitForIdle()
        assertEquals(6, viewModel.selectedString.value?.stringNumber)

        // Select High E (string 1)
        composeTestRule.onNodeWithTag("string_pill_1").performClick()
        composeTestRule.waitForIdle()
        assertEquals(1, viewModel.selectedString.value?.stringNumber)
    }

    @Test
    fun e2e_tuner_stringSelectionAndReset() {
        val standardStrings = viewModel.getCurrentTuning().strings

        // Select Low E (string 6)
        viewModel.selectString(standardStrings[0])
        assertEquals(6, viewModel.selectedString.value?.stringNumber)

        // Select High E (string 1)
        viewModel.selectString(standardStrings.last())
        assertEquals(1, viewModel.selectedString.value?.stringNumber)

        // Reset tuning progress
        viewModel.resetTunedStringsProgress()
        assertFalse(viewModel.isAllStringsTuned.value)
        assertTrue(viewModel.tunedStringNumbers.value.isEmpty())
    }

    @Test
    fun e2e_chords_interactions() {
        viewModel.selectTab(AppTab.CHORDS)

        composeTestRule.setContent {
            val settings = viewModel.settings.value
            ShredSheetsTheme(
                accentColor = settings.accentColor,
                colorMode = settings.colorMode
            ) {
                MainAppContent(
                    viewModel = viewModel,
                    onRequestPermission = {}
                )
            }
        }

        composeTestRule.waitForIdle()

        // Strum chord button
        composeTestRule.onNodeWithTag("strum_chord_button").assertExists()
        composeTestRule.onNodeWithTag("strum_chord_button").performClick()
        composeTestRule.waitForIdle()

        // Diagram tap to toggle enhanced view
        composeTestRule.onNodeWithTag("fretboard_diagram").assertExists()
        composeTestRule.onNodeWithTag("fretboard_diagram").performClick()
        composeTestRule.waitForIdle()
    }

    @Test
    fun e2e_metronome_bpmControlsAndPlayToggle() {
        viewModel.selectTab(AppTab.METRONOME)

        composeTestRule.setContent {
            val settings = viewModel.settings.value
            ShredSheetsTheme(
                accentColor = settings.accentColor,
                colorMode = settings.colorMode
            ) {
                MainAppContent(
                    viewModel = viewModel,
                    onRequestPermission = {}
                )
            }
        }

        composeTestRule.waitForIdle()

        val initialBpm = viewModel.metronomeBpm.value

        // Increment BPM
        composeTestRule.onNodeWithTag("bpm_increment_button").performClick()
        composeTestRule.waitForIdle()
        assertEquals(initialBpm + 1, viewModel.metronomeBpm.value)

        // Decrement BPM
        composeTestRule.onNodeWithTag("bpm_decrement_button").performClick()
        composeTestRule.waitForIdle()
        assertEquals(initialBpm, viewModel.metronomeBpm.value)

        // Tap tempo button
        composeTestRule.onNodeWithTag("tap_tempo_button").performClick()
        composeTestRule.waitForIdle()

        // Toggle metronome play
        assertFalse(viewModel.metronomePlaying.value)
        composeTestRule.onNodeWithTag("metronome_play_button").performClick()
        composeTestRule.waitForIdle()
        assertTrue(viewModel.metronomePlaying.value)

        // Stop metronome
        composeTestRule.onNodeWithTag("metronome_play_button").performClick()
        composeTestRule.waitForIdle()
        assertFalse(viewModel.metronomePlaying.value)
    }

    @Test
    fun e2e_settings_themeAndModePersistence() {
        // Change accent color to GREEN
        viewModel.setAccentColor(AccentColor.GREEN)
        assertEquals(AccentColor.GREEN, viewModel.settings.value.accentColor)

        // Change color mode to LIGHT
        viewModel.setColorMode(ColorMode.LIGHT)
        assertEquals(ColorMode.LIGHT, viewModel.settings.value.colorMode)

        // Verify reloaded from preferences
        val reloaded = SettingsPreferences(application).loadSettings()
        assertEquals(AccentColor.GREEN, reloaded.accentColor)
        assertEquals(ColorMode.LIGHT, reloaded.colorMode)
    }

    @Test
    fun e2e_metronome_drumPlaybackAndStyleSelection() {
        viewModel.selectTab(AppTab.METRONOME)

        composeTestRule.setContent {
            val settings = viewModel.settings.value
            ShredSheetsTheme(
                accentColor = settings.accentColor,
                colorMode = settings.colorMode
            ) {
                MainAppContent(
                    viewModel = viewModel,
                    onRequestPermission = {}
                )
            }
        }

        composeTestRule.waitForIdle()

        // 1. Check default is Click mode
        assertEquals(MetronomeSoundMode.CLICK, viewModel.metronomeSoundMode.value)
        composeTestRule.onNodeWithTag("sound_mode_click").assertExists()
        composeTestRule.onNodeWithTag("sound_mode_drums").assertExists()

        // 2. Switch to Drums mode
        composeTestRule.onNodeWithTag("sound_mode_drums").performClick()
        composeTestRule.waitForIdle()
        assertEquals(MetronomeSoundMode.DRUMS, viewModel.metronomeSoundMode.value)
        assertEquals(DrumStyle.ROCK, viewModel.drumStyle.value)

        // Switch back to Click mode
        composeTestRule.onNodeWithTag("sound_mode_click").performClick()
        composeTestRule.waitForIdle()
        assertEquals(MetronomeSoundMode.CLICK, viewModel.metronomeSoundMode.value)
    }

    @Test
    fun e2e_loopStation_navigationAndControls() {
        viewModel.selectTab(AppTab.LOOP)

        composeTestRule.setContent {
            val settings = viewModel.settings.value
            ShredSheetsTheme(
                accentColor = settings.accentColor,
                colorMode = settings.colorMode
            ) {
                MainAppContent(
                    viewModel = viewModel,
                    onRequestPermission = {}
                )
            }
        }

        composeTestRule.waitForIdle()

        // Verify Loop Station controls exist
        composeTestRule.onNodeWithTag("loop_backing_play_button").assertExists()
        composeTestRule.onNodeWithTag("loop_sound_mode_click").assertExists()
        composeTestRule.onNodeWithTag("loop_sound_mode_drums").assertExists()
        composeTestRule.onNodeWithTag("loop_backing_volume_button").assertExists().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("loop_backing_volume_slider").assertExists()
        // Tapping the volume button again hides the slider
        composeTestRule.onNodeWithTag("loop_backing_volume_button").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("loop_backing_volume_slider").assertDoesNotExist()
        composeTestRule.onNodeWithTag("loop_length_slider").assertDoesNotExist()
        composeTestRule.onNodeWithTag("loop_bpm_decrement_button").assertExists()
        composeTestRule.onNodeWithTag("loop_bpm_increment_button").assertExists()
        composeTestRule.onNodeWithTag("loop_tap_tempo_button").assertDoesNotExist()
        composeTestRule.onNodeWithTag("loop_bpm_slider").assertExists()
        composeTestRule.onNodeWithTag("track_pad_1").assertExists()
        composeTestRule.onNodeWithTag("track_pad_2").assertExists()
        composeTestRule.onNodeWithTag("track_pad_3").assertDoesNotExist()
        composeTestRule.onNodeWithTag("track_1_speaker_button").assertExists()
        composeTestRule.onNodeWithTag("track_2_speaker_button").assertExists()
        composeTestRule.onNodeWithTag("track_3_speaker_button").assertDoesNotExist()
        composeTestRule.onNodeWithTag("export_mix_save_button").assertExists()
        composeTestRule.onNodeWithTag("export_mix_share_button").assertExists()

        // Test tempo adjustment from Loop Station
        val initialBpm = viewModel.metronomeBpm.value
        composeTestRule.onNodeWithTag("loop_bpm_increment_button").performClick()
        composeTestRule.waitForIdle()
        assertEquals(initialBpm + 1, viewModel.metronomeBpm.value)

        composeTestRule.onNodeWithTag("loop_bpm_decrement_button").performClick()
        composeTestRule.waitForIdle()
        assertEquals(initialBpm, viewModel.metronomeBpm.value)

        // Toggle backing playback from Loop Station
        assertFalse(viewModel.metronomePlaying.value)
        composeTestRule.onNodeWithTag("loop_backing_play_button").performClick()
        composeTestRule.waitForIdle()
        assertTrue(viewModel.metronomePlaying.value)

        // Arm Track 1 by tapping Track Pad 1
        composeTestRule.onNodeWithTag("track_pad_1").performClick()
        composeTestRule.waitForIdle()
        assertEquals(0, viewModel.loopStationEngine.armedTrackIndex.value)

        // Stop backing
        composeTestRule.onNodeWithTag("loop_backing_play_button").performClick()
        composeTestRule.waitForIdle()
        assertFalse(viewModel.metronomePlaying.value)
    }
}
