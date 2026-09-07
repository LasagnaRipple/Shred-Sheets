package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import com.example.model.AccentColor
import com.example.model.AppSettings
import com.example.model.AppStyleTheme
import com.example.model.ColorMode
import com.example.model.InstrumentRepository
import com.example.model.InstrumentType
import com.example.model.PitchResult
import com.example.ui.chords.ChordLibraryScreen
import com.example.ui.metronome.MetronomeScreen
import com.example.ui.theme.ShredSheetsTheme
import com.example.ui.tuner.TunerScreen
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class TunerScreenshotTest {

    @get:Rule val composeTestRule = createComposeRule()

    @Test
    fun testTunerScreenRendersCorrectly() {
        val guitarConfig = InstrumentRepository.getConfig(InstrumentType.GUITAR)
        val tuning = guitarConfig.tunings.first()

        composeTestRule.setContent {
            ShredSheetsTheme(
                styleTheme = AppStyleTheme.ROCK,
                colorMode = ColorMode.DARK
            ) {
                TunerScreen(
                    pitchResult = PitchResult(
                        frequency = 82.41,
                        noteName = "E2",
                        noteLetter = "E",
                        octave = 2,
                        targetFrequency = 82.41,
                        centsDiff = 0.0,
                        isInTune = true,
                        isClose = false,
                        amplitude = 0.8,
                        confidence = 0.95
                    ),
                    hasSignal = true,
                    isListening = true,
                    instrumentType = InstrumentType.GUITAR,
                    tuningMode = tuning,
                    styleTheme = AppStyleTheme.ROCK,
                    soundEnabled = true,
                    isAutoMode = true,
                    selectedString = tuning.strings.first(),
                    activeInTuneStringNumber = 6,
                    anagramWords = listOf("Every", "Angry", "Dad", "Gets", "Bad", "Eggs"),
                    anagramSentence = "Every Angry Dad Gets Bad Eggs",
                    onInstrumentClick = {},
                    onTuningClick = {},
                    onThemeToggle = {},
                    onSoundToggle = {},
                    onSettingsClick = {},
                    onToggleAutoMode = {},
                    onStringSelected = {},
                    onPlayReferenceTone = {},
                    onAnagramBarClick = {},
                    onRequestMicrophonePermission = {},
                    hasMicrophonePermission = true
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("pitch_ruler_gauge").assertExists()
        composeTestRule.onNodeWithTag("tuner_status_badge").assertExists()
        composeTestRule.onNodeWithTag("shaka_visualizer_canvas").assertExists()
        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/tuner_screen.png")
    }

    @Test
    fun testChordLibraryScreenRenders() {
        composeTestRule.setContent {
            ShredSheetsTheme(
                styleTheme = AppStyleTheme.ROCK,
                colorMode = ColorMode.DARK
            ) {
                ChordLibraryScreen(
                    instrumentType = InstrumentType.GUITAR,
                    onStrumChord = {}
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("strum_chord_button").assertExists()
        composeTestRule.onNodeWithTag("fretboard_diagram").assertExists()
        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/chord_library_screen.png")

        // Test tapping the chord diagram area to toggle enhanced view
        composeTestRule.onNodeWithTag("fretboard_diagram").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/chord_library_screen_enhanced.png")

        // Tap again to toggle back to simplified view
        composeTestRule.onNodeWithTag("fretboard_diagram").performClick()
        composeTestRule.waitForIdle()
    }

    @Test
    fun testMetronomeScreenRenders() {
        composeTestRule.setContent {
            ShredSheetsTheme(
                styleTheme = AppStyleTheme.ROCK,
                colorMode = ColorMode.DARK
            ) {
                MetronomeScreen(
                    bpm = 120,
                    isPlaying = false,
                    currentBeat = 1,
                    timeSignatureBeats = 4,
                    onBpmChange = {},
                    onTogglePlay = {},
                    onTapTempo = {},
                    onTimeSignatureChange = {}
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("bpm_text_display").assertExists()
        composeTestRule.onNodeWithTag("metronome_play_button").assertExists()
        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/metronome_screen.png")
    }

    @Test
    fun testTunerScreenNextStringPromptAndTickedStrings() {
        val guitarConfig = InstrumentRepository.getConfig(InstrumentType.GUITAR)
        val tuning = guitarConfig.tunings.first()

        composeTestRule.setContent {
            ShredSheetsTheme(
                styleTheme = AppStyleTheme.ROCK,
                colorMode = ColorMode.DARK
            ) {
                TunerScreen(
                    pitchResult = PitchResult(
                        frequency = 82.41,
                        noteName = "E2",
                        noteLetter = "E",
                        octave = 2,
                        targetFrequency = 82.41,
                        centsDiff = 0.0,
                        isInTune = true,
                        isClose = false,
                        amplitude = 0.8,
                        confidence = 0.95
                    ),
                    hasSignal = true,
                    isListening = true,
                    instrumentType = InstrumentType.GUITAR,
                    tuningMode = tuning,
                    styleTheme = AppStyleTheme.ROCK,
                    soundEnabled = true,
                    isAutoMode = true,
                    selectedString = tuning.strings.first(),
                    activeInTuneStringNumber = 6,
                    tunedStringNumbers = setOf(6),
                    promptNextString = tuning.strings[1], // A string
                    isAllStringsTuned = false,
                    anagramWords = listOf("Every", "Angry", "Dad", "Gets", "Bad", "Eggs"),
                    anagramSentence = "Every Angry Dad Gets Bad Eggs",
                    onInstrumentClick = {},
                    onTuningClick = {},
                    onThemeToggle = {},
                    onSoundToggle = {},
                    onSettingsClick = {},
                    onToggleAutoMode = {},
                    onStringSelected = {},
                    onPlayReferenceTone = {},
                    onAnagramBarClick = {},
                    onRequestMicrophonePermission = {},
                    hasMicrophonePermission = true
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("tuner_status_badge").assertExists()
        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/tuner_next_string_prompt.png")
    }

    @Test
    fun testTunerScreenAllStringsTunedItsTimeToShred() {
        val guitarConfig = InstrumentRepository.getConfig(InstrumentType.GUITAR)
        val tuning = guitarConfig.tunings.first()

        composeTestRule.setContent {
            ShredSheetsTheme(
                styleTheme = AppStyleTheme.ROCK,
                colorMode = ColorMode.DARK
            ) {
                TunerScreen(
                    pitchResult = PitchResult.EMPTY,
                    hasSignal = false,
                    isListening = true,
                    instrumentType = InstrumentType.GUITAR,
                    tuningMode = tuning,
                    styleTheme = AppStyleTheme.ROCK,
                    soundEnabled = true,
                    isAutoMode = true,
                    selectedString = tuning.strings.first(),
                    activeInTuneStringNumber = null,
                    tunedStringNumbers = setOf(6, 5, 4, 3, 2, 1),
                    promptNextString = null,
                    isAllStringsTuned = true,
                    anagramWords = listOf("Every", "Angry", "Dad", "Gets", "Bad", "Eggs"),
                    anagramSentence = "Every Angry Dad Gets Bad Eggs",
                    onInstrumentClick = {},
                    onTuningClick = {},
                    onThemeToggle = {},
                    onSoundToggle = {},
                    onSettingsClick = {},
                    onToggleAutoMode = {},
                    onStringSelected = {},
                    onPlayReferenceTone = {},
                    onAnagramBarClick = {},
                    onRequestMicrophonePermission = {},
                    hasMicrophonePermission = true
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("tuner_status_badge").assertExists()
        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/tuner_all_strings_shred.png")
    }

    @Test
    fun testTunerAndMetronomeScreenPinkTheme() {
        val guitarConfig = InstrumentRepository.getConfig(InstrumentType.GUITAR)
        val tuning = guitarConfig.tunings.first()

        composeTestRule.setContent {
            ShredSheetsTheme(
                accentColor = AccentColor.PURPLE,
                colorMode = ColorMode.DARK
            ) {
                TunerScreen(
                    pitchResult = PitchResult.EMPTY,
                    hasSignal = false,
                    isListening = true,
                    instrumentType = InstrumentType.GUITAR,
                    tuningMode = tuning,
                    styleTheme = AppStyleTheme.ROCK,
                    soundEnabled = true,
                    isAutoMode = true,
                    selectedString = tuning.strings.first(),
                    activeInTuneStringNumber = null,
                    anagramWords = listOf("Every", "Angry", "Dad", "Gets", "Bad", "Eggs"),
                    anagramSentence = "Every Angry Dad Gets Bad Eggs",
                    onInstrumentClick = {},
                    onTuningClick = {},
                    onThemeToggle = {},
                    onSoundToggle = {},
                    onSettingsClick = {},
                    onToggleAutoMode = {},
                    onStringSelected = {},
                    onPlayReferenceTone = {},
                    onAnagramBarClick = {},
                    onRequestMicrophonePermission = {},
                    hasMicrophonePermission = true
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("header_shred_sheets_logo").assertExists()
        composeTestRule.onNodeWithTag("quick_tuning_pill").assertExists()
        composeTestRule.onNodeWithTag("sound_toggle_button").assertExists()
        composeTestRule.onNodeWithTag("string_pill_6").assertExists()
        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/tuner_pink_theme.png")
    }
}
