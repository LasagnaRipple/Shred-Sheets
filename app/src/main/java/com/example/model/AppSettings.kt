package com.example.model

import androidx.compose.ui.graphics.Color

enum class AccentColor(
    val id: String,
    val displayName: String,
    val emoji: String,
    val swatchColor: Color,
    val darkAllowed: Boolean,
    val lightAllowed: Boolean
) {
    YELLOW("yellow", "Yellow", "🟡", Color(0xFFE2FF00), darkAllowed = true, lightAllowed = false),
    ORANGE("orange", "Orange", "🟠", Color(0xFFFF6D00), darkAllowed = true, lightAllowed = true),
    PURPLE("purple", "Purple", "🟣", Color(0xFFB537F2), darkAllowed = true, lightAllowed = true),
    GREEN("green", "Green", "🟢", Color(0xFF00E676), darkAllowed = true, lightAllowed = true),
    BLUE("blue", "Blue", "🔵", Color(0xFF00F0FF), darkAllowed = true, lightAllowed = true),
    RED("red", "Red", "🔴", Color(0xFFFF2A4B), darkAllowed = true, lightAllowed = true),
    WHITE("white", "White", "⚪️", Color(0xFFFFFFFF), darkAllowed = true, lightAllowed = false),
    BLACK("black", "Black", "⚫️", Color(0xFF121212), darkAllowed = false, lightAllowed = true)
}

enum class AppStyleTheme(val id: String, val title: String, val subtitle: String, val emoji: String) {
    ROCK("rock", "ROCK VIBE", "Electric neon & rockstar lightning", "⚡"),
    POP("pop", "POP CANDY", "Vibrant bubblegum & sunny pop", "🎸")
}

enum class ColorMode(val id: String, val displayName: String) {
    DARK("dark", "Dark"),
    LIGHT("light", "Light"),
    SYSTEM("system", "System Default")
}

enum class TunerMode(val id: String, val displayName: String) {
    AUTO("auto", "Auto Detect"),
    MANUAL("manual", "Manual String")
}

data class AppSettings(
    val selectedInstrument: InstrumentType = InstrumentType.GUITAR,
    val selectedTuningId: String = "guitar_standard",
    val accentColor: AccentColor = AccentColor.YELLOW,
    val colorMode: ColorMode = ColorMode.DARK,
    val soundEffectsEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val tunerMode: TunerMode = TunerMode.AUTO,
    val selectedLanguageCode: String = "en",
    val isOnboardingCompleted: Boolean = false,
    val currentAnagramSentence: String = "Eddie Ate Dynamite Good Bye Eddie"
) {
    // Retain compatibility property: (Existing) Dark mode + rock = yellow accent, Dark mode + pop = blue accent
    val styleTheme: AppStyleTheme
        get() = if (accentColor == AccentColor.BLUE) AppStyleTheme.POP else AppStyleTheme.ROCK
}

