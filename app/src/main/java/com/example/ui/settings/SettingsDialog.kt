package com.example.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.AccentColor
import com.example.model.AppSettings
import com.example.model.AppStyleTheme
import com.example.model.ColorMode
import com.example.model.InstrumentRepository
import com.example.model.InstrumentType
import com.example.model.LocalizationManager
import com.example.model.TuningMode
import com.example.ui.theme.LocalIsDarkTheme
import com.example.ui.theme.ShredCardBorder
import com.example.ui.theme.ShredCardSurface
import com.example.ui.theme.ShredMutedText
import com.example.ui.theme.ShredPrimaryText

@Composable
fun SettingsDialog(
    settings: AppSettings,
    onUpdateSettings: (AppSettings) -> Unit,
    onDismiss: () -> Unit
) {
    var showLanguagePicker by remember { mutableStateOf(false) }
    var showPrivacyPolicy by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    val isDark = LocalIsDarkTheme.current
    val dialogSurface = if (isDark) ShredCardSurface else MaterialTheme.colorScheme.surface
    val dialogBorder = if (isDark) ShredCardBorder else MaterialTheme.colorScheme.outlineVariant
    val primaryText = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
    val mutedText = if (isDark) Color(0xFFD4D4CE) else MaterialTheme.colorScheme.onSurfaceVariant
    val primaryColor = MaterialTheme.colorScheme.primary

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
                .clip(RoundedCornerShape(20.dp))
                .border(1.5.dp, dialogBorder, RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(
                containerColor = dialogSurface
            )
        ) {
            val scrollState = rememberScrollState()

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with Settings Icon, Title and Close Button (matching other info popups)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Settings",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 19.sp
                            ),
                            color = primaryText
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_settings_dialog_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = mutedText
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- THEME & ACCENT CUSTOMIZER ---
                Text(
                    text = "Theme",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = mutedText,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))

                val isCurrentDark = settings.colorMode == ColorMode.DARK

                // Segmented Pill Toggle: [ Dark 🌙 | Light ☀️ ] like loop screen
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(999.dp))
                        .background(if (isCurrentDark) Color(0xFF0F141C) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                        .border(
                            1.dp,
                            if (isCurrentDark) Color(0xFF263242) else MaterialTheme.colorScheme.outlineVariant,
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
                            .background(if (isCurrentDark) MaterialTheme.colorScheme.primary else Color.Transparent)
                            .clickable {
                                if (settings.hapticsEnabled) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                                val nextAccent = if (settings.accentColor.darkAllowed) settings.accentColor else AccentColor.YELLOW
                                onUpdateSettings(settings.copy(colorMode = ColorMode.DARK, accentColor = nextAccent))
                            }
                            .padding(vertical = 10.dp)
                            .testTag("settings_dark_mode_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Dark 🌙",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isCurrentDark) FontWeight.Bold else FontWeight.SemiBold,
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
                            .background(if (!isCurrentDark) MaterialTheme.colorScheme.primary else Color.Transparent)
                            .clickable {
                                if (settings.hapticsEnabled) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                                val nextAccent = if (settings.accentColor.lightAllowed) settings.accentColor else AccentColor.BLUE
                                onUpdateSettings(settings.copy(colorMode = ColorMode.LIGHT, accentColor = nextAccent))
                            }
                            .padding(vertical = 10.dp)
                            .testTag("settings_light_mode_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Light ☀️",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (!isCurrentDark) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 14.sp
                            ),
                            color = if (!isCurrentDark) MaterialTheme.colorScheme.onPrimary else if (isCurrentDark) Color(0xFF94A3B8) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // --- 3. SOUND EFFECTS & HAPTICS TOGGLES ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "In-Tune Sound Chime",
                            fontWeight = FontWeight.Bold,
                            color = primaryText
                        )
                    }
                    Switch(
                        checked = settings.soundEffectsEnabled,
                        onCheckedChange = { isEnabled ->
                            if (settings.hapticsEnabled) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                            onUpdateSettings(settings.copy(soundEffectsEnabled = isEnabled))
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Vibration, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Haptic Feedback",
                            fontWeight = FontWeight.Bold,
                            color = primaryText
                        )
                    }
                    Switch(
                        checked = settings.hapticsEnabled,
                        onCheckedChange = { isEnabled ->
                            // Explicit haptic confirmation when toggling haptic feedback
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onUpdateSettings(settings.copy(hapticsEnabled = isEnabled))
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onSecondary,
                            checkedTrackColor = MaterialTheme.colorScheme.secondary
                        )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // --- 4. 40+ GOOGLE PLAY LANGUAGES SELECTOR ---
                val currentLang = LocalizationManager.SUPPORTED_LANGUAGES.firstOrNull { it.code == settings.selectedLanguageCode }
                    ?: LocalizationManager.SUPPORTED_LANGUAGES.first()

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                        .clickable {
                            if (settings.hapticsEnabled) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                            showLanguagePicker = true
                        }
                        .padding(14.dp)
                        .testTag("language_picker_button")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Language, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Language",
                                fontWeight = FontWeight.Bold,
                                color = primaryText
                            )
                        }
                        Text(
                            text = "${currentLang.flagEmoji} ${currentLang.name}",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Black),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // --- 5. PRIVACY POLICY BUTTON ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                        .clickable {
                            if (settings.hapticsEnabled) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                            showPrivacyPolicy = true
                        }
                        .padding(14.dp)
                        .testTag("privacy_policy_button")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Privacy Policy",
                            fontWeight = FontWeight.Bold,
                            color = primaryText
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Done Button
                Button(
                    onClick = {
                        if (settings.hapticsEnabled) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = "Save & Shred! 🤙",
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }

    // Modal List for 40+ Languages
    if (showLanguagePicker) {
        Dialog(onDismissRequest = { showLanguagePicker = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.5.dp, dialogBorder, RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = dialogSurface)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header with Language Icon, Title and Close Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = null,
                                tint = primaryColor,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "Select Language",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 19.sp
                                ),
                                color = primaryText
                            )
                        }

                        IconButton(
                            onClick = { showLanguagePicker = false },
                            modifier = Modifier.testTag("close_language_picker_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = mutedText
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(340.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(LocalizationManager.SUPPORTED_LANGUAGES) { lang ->
                            val isSelected = lang.code == settings.selectedLanguageCode
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                                    )
                                    .clickable {
                                        onUpdateSettings(settings.copy(selectedLanguageCode = lang.code))
                                        showLanguagePicker = false
                                    }
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${lang.flagEmoji}  ${lang.name} (${lang.nativeName})",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium
                                        ),
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else primaryText
                                    )
                                    if (isSelected) {
                                        Text(text = "✅", fontSize = 14.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showPrivacyPolicy) {
        PrivacyPolicyDialog(
            onDismiss = { showPrivacyPolicy = false }
        )
    }
}

@Composable
fun PrivacyPolicyDialog(
    onDismiss: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    val dialogSurface = if (isDark) ShredCardSurface else MaterialTheme.colorScheme.surface
    val dialogBorder = if (isDark) ShredCardBorder else MaterialTheme.colorScheme.outlineVariant
    val primaryText = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
    val mutedText = if (isDark) Color(0xFFD4D4CE) else MaterialTheme.colorScheme.onSurfaceVariant
    val primaryColor = MaterialTheme.colorScheme.primary

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
                .clip(RoundedCornerShape(20.dp))
                .border(1.5.dp, dialogBorder, RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(
                containerColor = dialogSurface
            )
        ) {
            val scrollState = rememberScrollState()

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with Security Icon, Title and Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Privacy Policy",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 19.sp
                            ),
                            color = primaryText
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_privacy_dialog_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = mutedText
                        )
                    }
                }

                // Badge
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "🛡️ 100% Free • Zero Customer Data Collected",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Policy Text Blocks
                PolicySection(
                    title = "1. Zero Personal Data Collected",
                    content = "Shred Sheets is completely free. We do NOT collect, store, transmit, sell, or share any personal information or customer data. There are no user accounts, passwords, email signups, or tracking cookies."
                )

                Spacer(modifier = Modifier.height(12.dp))

                PolicySection(
                    title = "2. Microphone Access (Audio Tuning)",
                    content = "The microphone permission (RECORD_AUDIO) is strictly used to capture live sound waves from your instrument in real-time to compute pitch frequency (Hz) and cent offset. Audio processing is 100% in-memory on your device. Audio is NEVER recorded to device storage, never saved, and never transmitted over the internet."
                )

                Spacer(modifier = Modifier.height(12.dp))

                PolicySection(
                    title = "3. Haptic Feedback (Vibration)",
                    content = "The vibration permission is solely used to deliver optional tactile confirmation when strings are in tune and during metronome beats. No data is collected."
                )

                Spacer(modifier = Modifier.height(12.dp))

                PolicySection(
                    title = "4. Local Device Storage",
                    content = "Your preferences (such as selected instrument, tuning preset, light/dark theme, and language) are stored solely inside your phone's private storage sandbox. This data never leaves your device and is erased if you uninstall the app."
                )

                Spacer(modifier = Modifier.height(12.dp))

                PolicySection(
                    title = "5. No Ads & No Third-Party Tracking",
                    content = "Shred Sheets contains no advertisements and does not use analytics SDKs (no Google AdMob, no Firebase Analytics, no tracking libraries). It is completely family-safe and COPPA compliant."
                )

                Spacer(modifier = Modifier.height(12.dp))

                PolicySection(
                    title = "6. Developer Contact",
                    content = "If you have questions regarding this policy, contact:\nDeveloper: Kyle Silver\nEmail: kylesilver27@gmail.com"
                )

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = "Understood",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun PolicySection(
    title: String,
    content: String
) {
    val isDark = LocalIsDarkTheme.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = content,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp, lineHeight = 17.sp),
            color = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
        )
    }
}
