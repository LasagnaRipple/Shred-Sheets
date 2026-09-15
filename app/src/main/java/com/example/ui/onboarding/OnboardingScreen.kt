package com.example.ui.onboarding

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import com.example.R
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AccentColor
import com.example.model.AppSettings
import com.example.model.ColorMode
import com.example.model.InstrumentType
import com.example.ui.components.InstrumentIcon
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    initialSettings: AppSettings,
    onUpdateTheme: (ColorMode, AccentColor) -> Unit = { _, _ -> },
    onThemeToggle: () -> Unit = {},
    onComplete: (AppSettings) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedInstrument by remember { mutableStateOf(initialSettings.selectedInstrument) }
    var selectedColorMode by remember { mutableStateOf(initialSettings.colorMode) }
    var selectedAccent by remember { mutableStateOf(initialSettings.accentColor) }

    val logoScale = remember { Animatable(1f) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(initialSettings.accentColor) {
        selectedAccent = initialSettings.accentColor
    }

    val scrollState = rememberScrollState()
    val haptic = LocalHapticFeedback.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Shred Sheets Arched Rock Logo (Tap to cycle accent color)
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .clip(RoundedCornerShape(12.dp))
                .clickable {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    coroutineScope.launch {
                        logoScale.animateTo(0.88f, animationSpec = tween(70))
                        logoScale.animateTo(
                            1f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium
                            )
                        )
                    }
                    val isDark = selectedColorMode == ColorMode.DARK
                    val allowed = AccentColor.entries.filter { if (isDark) it.darkAllowed else it.lightAllowed }
                    val currentIndex = allowed.indexOf(selectedAccent)
                    val nextAccent = if (currentIndex >= 0) allowed[(currentIndex + 1) % allowed.size] else allowed.first()
                    selectedAccent = nextAccent
                    onUpdateTheme(selectedColorMode, nextAccent)
                    onThemeToggle()
                }
                .padding(vertical = 4.dp)
                .semantics {
                    role = Role.Button
                    contentDescription = "Shred Sheets logo. Tap to cycle accent color theme."
                }
                .testTag("shred_sheets_logo"),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_shred_sheets_logo),
                contentDescription = null,
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 110.dp)
                    .scale(logoScale.value),
                contentScale = ContentScale.Fit
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Blank Tabs For Future Rockstars",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.secondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Step 1: Select Instrument
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "1. Pick Your Instrument",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(10.dp))

                val instruments = InstrumentType.entries

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    instruments.forEach { inst ->
                        val isSelected = selectedInstrument == inst
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                                .border(
                                    1.5.dp,
                                    if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    selectedInstrument = inst
                                }
                                .padding(vertical = 10.dp)
                                .testTag("onboarding_instrument_${inst.name.lowercase()}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                InstrumentIcon(instrumentType = inst, fontSize = 20.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = inst.displayName,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Step 2: Custom Theme & Accent Color (Circled in Screenshot 1)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "2. Choose Your Theme",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(10.dp))

                val isDark = selectedColorMode == ColorMode.DARK

                // Segmented Pill Toggle: [ Dark 🌙 | Light ☀️ ] like loop screen
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(999.dp))
                        .background(if (isDark) Color(0xFF0F141C) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                        .border(
                            1.dp,
                            if (isDark) Color(0xFF263242) else MaterialTheme.colorScheme.outlineVariant,
                            RoundedCornerShape(999.dp)
                        )
                        .padding(3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Dark Mode Pill
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(999.dp))
                            .background(if (isDark) MaterialTheme.colorScheme.primary else Color.Transparent)
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                selectedColorMode = ColorMode.DARK
                                val nextAccent = if (selectedAccent.darkAllowed) selectedAccent else AccentColor.YELLOW
                                selectedAccent = nextAccent
                                onUpdateTheme(ColorMode.DARK, nextAccent)
                            }
                            .padding(vertical = 10.dp)
                            .testTag("onboarding_dark_mode_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Dark 🌙",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isDark) FontWeight.Bold else FontWeight.SemiBold,
                                fontSize = 14.sp
                            ),
                            color = Color.Black
                        )
                    }

                    // Light Mode Pill
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(999.dp))
                            .background(if (!isDark) MaterialTheme.colorScheme.primary else Color.Transparent)
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                selectedColorMode = ColorMode.LIGHT
                                val nextAccent = if (selectedAccent.lightAllowed) selectedAccent else AccentColor.BLUE
                                selectedAccent = nextAccent
                                onUpdateTheme(ColorMode.LIGHT, nextAccent)
                            }
                            .padding(vertical = 10.dp)
                            .testTag("onboarding_light_mode_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Light ☀️",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (!isDark) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 14.sp
                            ),
                            color = if (!isDark) MaterialTheme.colorScheme.onPrimary else if (isDark) Color(0xFF94A3B8) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Finish Onboarding Button
        Button(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                val updated = initialSettings.copy(
                    selectedInstrument = selectedInstrument,
                    accentColor = selectedAccent,
                    colorMode = selectedColorMode,
                    isOnboardingCompleted = true
                )
                onComplete(updated)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
                .testTag("onboarding_complete_button"),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text = "START SHREDDING!",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp
                ),
                color = MaterialTheme.colorScheme.onPrimary
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
