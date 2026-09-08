package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.AccentColor
import com.example.model.AppSettings
import com.example.model.ColorMode
import com.example.model.InstrumentType
import com.example.model.TunerMode

/**
 * Manages local persistence for user settings, selected instrument, tuning,
 * and current anagram mnemonic.
 */
class SettingsPreferences(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "shred_sheets_settings"

        private const val KEY_INSTRUMENT = "selected_instrument"
        private const val KEY_TUNING_ID = "selected_tuning_id"
        private const val KEY_ACCENT_COLOR = "accent_color"
        private const val KEY_COLOR_MODE = "color_mode"
        private const val KEY_SOUND_ENABLED = "sound_effects_enabled"
        private const val KEY_HAPTICS_ENABLED = "haptics_enabled"
        private const val KEY_TUNER_MODE = "tuner_mode"
        private const val KEY_LANGUAGE_CODE = "language_code"
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
        private const val KEY_CURRENT_ANAGRAM = "current_anagram_sentence"
    }

    fun loadSettings(): AppSettings {
        val instrumentStr = prefs.getString(KEY_INSTRUMENT, InstrumentType.GUITAR.name) ?: InstrumentType.GUITAR.name
        val selectedInstrument = try {
            InstrumentType.valueOf(instrumentStr)
        } catch (_: Exception) {
            InstrumentType.GUITAR
        }

        val selectedTuningId = prefs.getString(KEY_TUNING_ID, "guitar_standard") ?: "guitar_standard"

        val accentStr = prefs.getString(KEY_ACCENT_COLOR, AccentColor.YELLOW.name) ?: AccentColor.YELLOW.name
        val accentColor = try {
            AccentColor.valueOf(accentStr)
        } catch (_: Exception) {
            AccentColor.YELLOW
        }

        val colorModeStr = prefs.getString(KEY_COLOR_MODE, ColorMode.DARK.name) ?: ColorMode.DARK.name
        val colorMode = try {
            ColorMode.valueOf(colorModeStr)
        } catch (_: Exception) {
            ColorMode.DARK
        }

        val soundEnabled = prefs.getBoolean(KEY_SOUND_ENABLED, true)
        val hapticsEnabled = prefs.getBoolean(KEY_HAPTICS_ENABLED, true)

        // On first load or return, Auto detect is always initialized to ON (TunerMode.AUTO)
        val tunerMode = TunerMode.AUTO

        val languageCode = prefs.getString(KEY_LANGUAGE_CODE, "en") ?: "en"
        val isOnboardingCompleted = prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)

        val defaultAnagram = if (selectedInstrument == InstrumentType.GUITAR && selectedTuningId == "guitar_standard") {
            "Eddie Ate Dynamite Good Bye Eddie"
        } else {
            "Every Angry Dad Gets Bad Eggs"
        }
        val currentAnagram = prefs.getString(KEY_CURRENT_ANAGRAM, defaultAnagram) ?: defaultAnagram

        return AppSettings(
            selectedInstrument = selectedInstrument,
            selectedTuningId = selectedTuningId,
            accentColor = accentColor,
            colorMode = colorMode,
            soundEffectsEnabled = soundEnabled,
            hapticsEnabled = hapticsEnabled,
            tunerMode = tunerMode,
            selectedLanguageCode = languageCode,
            isOnboardingCompleted = isOnboardingCompleted,
            currentAnagramSentence = currentAnagram
        )
    }

    fun saveSettings(settings: AppSettings) {
        prefs.edit()
            .putString(KEY_INSTRUMENT, settings.selectedInstrument.name)
            .putString(KEY_TUNING_ID, settings.selectedTuningId)
            .putString(KEY_ACCENT_COLOR, settings.accentColor.name)
            .putString(KEY_COLOR_MODE, settings.colorMode.name)
            .putBoolean(KEY_SOUND_ENABLED, settings.soundEffectsEnabled)
            .putBoolean(KEY_HAPTICS_ENABLED, settings.hapticsEnabled)
            .putString(KEY_TUNER_MODE, settings.tunerMode.name)
            .putString(KEY_LANGUAGE_CODE, settings.selectedLanguageCode)
            .putBoolean(KEY_ONBOARDING_COMPLETED, settings.isOnboardingCompleted)
            .putString(KEY_CURRENT_ANAGRAM, settings.currentAnagramSentence)
            .apply()
    }
}
