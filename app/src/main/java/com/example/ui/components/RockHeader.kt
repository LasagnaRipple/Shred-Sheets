package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.AppStyleTheme
import com.example.model.InstrumentType
import com.example.model.TuningMode
import com.example.ui.theme.LocalIsDarkTheme
import com.example.ui.theme.VibrantDarkBorder
import com.example.ui.theme.VibrantDarkCard

@Composable
fun RockHeader(
    instrumentType: InstrumentType,
    tuningMode: TuningMode,
    styleTheme: AppStyleTheme,
    soundEnabled: Boolean,
    onInstrumentClick: () -> Unit,
    onTuningClick: () -> Unit,
    onThemeToggle: () -> Unit,
    onSoundToggle: () -> Unit,
    onSettingsClick: () -> Unit,
    isAutoMode: Boolean = true,
    onToggleAutoMode: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val isDark = LocalIsDarkTheme.current
    val buttonBg = if (isDark) VibrantDarkCard else MaterialTheme.colorScheme.surfaceVariant
    val buttonBorder = if (isDark) VibrantDarkBorder else MaterialTheme.colorScheme.outlineVariant

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 10.dp, bottom = 4.dp)
    ) {
        // Top Header Row (Shred Sheets brand & utility icons)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Arched Logo in Vibrant Palette typography
            Column {
                Image(
                    painter = painterResource(id = R.drawable.ic_shred_sheets_logo),
                    contentDescription = "SHRED SHEETS",
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .height(30.dp)
                        .widthIn(max = 140.dp)
                        .testTag("header_shred_sheets_logo"),
                    contentScale = ContentScale.Fit
                )
            }

            // Right: Instrument circular button & Theme pill/action buttons
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Instrument quick circle button
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(buttonBg)
                        .border(1.dp, buttonBorder, CircleShape)
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onInstrumentClick()
                        }
                        .testTag("header_instrument_button"),
                    contentAlignment = Alignment.Center
                ) {
                    InstrumentIcon(instrumentType = instrumentType, fontSize = 18.sp)
                }

                // Sound On/Off Toggle
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(buttonBg)
                        .border(
                            1.dp,
                            if (soundEnabled) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else buttonBorder,
                            CircleShape
                        )
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onSoundToggle()
                        }
                        .testTag("sound_toggle_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (soundEnabled) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                        contentDescription = "Toggle Sound",
                        tint = if (soundEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Settings Button
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(buttonBg)
                        .border(1.dp, buttonBorder, CircleShape)
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onSettingsClick()
                        }
                        .testTag("settings_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Row containing Tuning Select Button and Auto/Manual Mode Button in line
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Current Tuning pill / Change tuning (Simplified tuning name without strings)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(buttonBg)
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.primary.copy(alpha = if (isDark) 0.4f else 0.7f),
                        RoundedCornerShape(20.dp)
                    )
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onTuningClick()
                    }
                    .padding(horizontal = 14.dp, vertical = 7.dp)
                    .testTag("quick_tuning_pill"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${tuningMode.shortName.uppercase()} ▾",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Auto Detect Toggle Switch in line with tuning select button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onToggleAutoMode()
                    }
                    .testTag("auto_manual_toggle")
            ) {
                Text(
                    text = "AUTO DETECT",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 0.5.sp
                    ),
                    color = if (isAutoMode) {
                        if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                Switch(
                    checked = isAutoMode,
                    onCheckedChange = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onToggleAutoMode()
                    },
                    modifier = Modifier.scale(0.8f),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                        uncheckedThumbColor = if (isDark) Color(0xFF9E9E9E) else Color(0xFF94A3B8),
                        uncheckedTrackColor = if (isDark) Color(0xFF232A34) else Color(0xFFE2E8F0),
                        uncheckedBorderColor = if (isDark) Color(0xFF3B4856) else Color(0xFFCBD5E1)
                    )
                )
            }
        }
    }
}
