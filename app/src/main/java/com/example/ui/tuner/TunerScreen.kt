package com.example.ui.tuner

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppStyleTheme
import com.example.model.InstrumentString
import com.example.model.InstrumentType
import com.example.model.PitchResult
import com.example.model.TuningMode
import com.example.ui.components.PitchMeter
import com.example.ui.components.RockHeader
import com.example.ui.components.ShakaVisualizer
import com.example.ui.components.StringPillStrip
import com.example.ui.theme.InTuneGreen
import com.example.ui.theme.VibrantDarkBorder
import com.example.ui.theme.VibrantDarkBorderSubtle
import com.example.ui.theme.VibrantDarkCard

@Composable
fun TunerScreen(
    pitchResult: PitchResult,
    hasSignal: Boolean,
    isListening: Boolean,
    instrumentType: InstrumentType,
    tuningMode: TuningMode,
    styleTheme: AppStyleTheme,
    soundEnabled: Boolean,
    isAutoMode: Boolean,
    selectedString: InstrumentString?,
    activeInTuneStringNumber: Int?,
    tunedStringNumbers: Set<Int> = emptySet(),
    promptNextString: InstrumentString? = null,
    isAllStringsTuned: Boolean = false,
    recheckingStringNumber: Int? = null,
    anagramWords: List<String>,
    anagramSentence: String,
    onInstrumentClick: () -> Unit,
    onTuningClick: () -> Unit,
    onThemeToggle: () -> Unit,
    onSoundToggle: () -> Unit,
    onSettingsClick: () -> Unit,
    onToggleAutoMode: () -> Unit,
    onStringSelected: (InstrumentString) -> Unit,
    onPlayReferenceTone: () -> Unit,
    onAnagramBarClick: () -> Unit,
    onCycleAnagram: () -> Unit = {},
    onRequestMicrophonePermission: () -> Unit,
    hasMicrophonePermission: Boolean,
    isTunerActive: Boolean = isListening,
    isStringConfirmed: Boolean = false,
    pluckAnimationEvent: Pair<Int, Long>? = null,
    onToggleTunerActive: () -> Unit = {},
    onResetTuning: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val haptic = LocalHapticFeedback.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // App Rock Header with quick mode pills & brand
        RockHeader(
            instrumentType = instrumentType,
            tuningMode = tuningMode,
            styleTheme = styleTheme,
            soundEnabled = soundEnabled,
            onInstrumentClick = onInstrumentClick,
            onTuningClick = onTuningClick,
            onThemeToggle = onThemeToggle,
            onSoundToggle = onSoundToggle,
            onSettingsClick = onSettingsClick,
            isAutoMode = isAutoMode,
            onToggleAutoMode = onToggleAutoMode
        )

        // Mic Permission Prompt Banner if needed
        if (!hasMicrophonePermission) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .clickable { onRequestMicrophonePermission() }
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MicOff,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        text = "Tap to Enable Microphone for Real-Time Pitch Detection 🎙️",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Center Attraction: Circular Tuning Component per Redesign Spec
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            ShakaVisualizer(
                pitchResult = pitchResult,
                hasSignal = hasSignal,
                primaryColor = MaterialTheme.colorScheme.primary,
                accentColor = MaterialTheme.colorScheme.secondary,
                isTunerActive = isTunerActive,
                isAllStringsTuned = isAllStringsTuned,
                isStringConfirmed = isStringConfirmed,
                promptNextString = promptNextString,
                instrumentEmoji = instrumentType.iconName,
                instrumentType = instrumentType,
                onTap = onToggleTunerActive,
                onResetTuning = onResetTuning,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Real-Time Pitch Meter & Cents Arc
        PitchMeter(
            pitchResult = pitchResult,
            hasSignal = hasSignal,
            isTunerActive = isTunerActive,
            promptNextString = promptNextString,
            isAllStringsTuned = isAllStringsTuned,
            onStatusClick = onToggleTunerActive,
            stringCount = tuningMode.strings.size,
            pluckAnimationEvent = pluckAnimationEvent,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Interactive String Selector Strip
        StringPillStrip(
            strings = tuningMode.strings,
            selectedStringNumber = selectedString?.stringNumber,
            activeInTuneStringNumber = activeInTuneStringNumber,
            tunedStringNumbers = tunedStringNumbers,
            recheckingStringNumber = recheckingStringNumber,
            isAllStringsTuned = isAllStringsTuned,
            anagramWords = anagramWords,
            pluckAnimationEvent = pluckAnimationEvent,
            onStringSelected = onStringSelected,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(14.dp))

        // String Mnemonic Box (Vibrant Palette design with interactive Dice to cycle anagrams)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(VibrantDarkCard)
                .border(1.dp, VibrantDarkBorderSubtle, RoundedCornerShape(16.dp))
                .testTag("anagram_banner_pill")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 14.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onAnagramBarClick()
                        },
                    contentAlignment = Alignment.CenterStart
                ) {
                    SingleLineAnagramText(
                        sentence = anagramSentence,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Interactive Dice button to cycle through anagrams (standalone emoji)
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onCycleAnagram()
                        }
                        .testTag("cycle_anagram_dice_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🎲",
                        fontSize = 22.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

/**
 * Renders the anagram mnemonic sentence strictly on a single line,
 * automatically adjusting font size to guarantee all anagrams fit without wrapping.
 */
@Composable
private fun SingleLineAnagramText(
    sentence: String,
    modifier: Modifier = Modifier,
    color: Color = Color.White
) {
    val baseFontSize = when {
        sentence.length >= 45 -> 10.5f
        sentence.length >= 40 -> 11.5f
        sentence.length >= 34 -> 12.5f
        sentence.length >= 28 -> 13.5f
        else -> 14.5f
    }

    var currentFontSize by remember(sentence) { mutableStateOf(baseFontSize) }

    Text(
        text = sentence,
        style = MaterialTheme.typography.bodyLarge.copy(
            fontStyle = FontStyle.Italic,
            fontWeight = FontWeight.Bold,
            fontSize = currentFontSize.sp
        ),
        color = color,
        maxLines = 1,
        softWrap = false,
        overflow = TextOverflow.Ellipsis,
        onTextLayout = { textLayoutResult ->
            if (textLayoutResult.hasVisualOverflow && currentFontSize > 8f) {
                currentFontSize = (currentFontSize - 0.5f).coerceAtLeast(8f)
            }
        },
        modifier = modifier
    )
}
