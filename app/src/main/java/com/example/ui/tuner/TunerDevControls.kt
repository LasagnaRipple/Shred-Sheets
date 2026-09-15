package com.example.ui.tuner

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalIsDarkTheme
import kotlin.math.roundToInt

/**
 * DEV MODE BANNER & CONTROLS FOR TUNER TESTING
 * 
 * Instructions for removal before production Google Play store release:
 * 1. Delete this file (TunerDevControls.kt)
 * 2. Remove the TunerDevControls and dev mode state calls in TunerScreen.kt
 */

@Composable
fun TunerDevModeBanner(
    isDevModeEnabled: Boolean,
    onToggleDevMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val isDark = LocalIsDarkTheme.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isDevModeEnabled) {
                    Color(0xFF2A2000) // Warning dark amber
                } else {
                    if (isDark) Color(0xFF1E232B) else MaterialTheme.colorScheme.surfaceVariant
                }
            )
            .border(
                1.5.dp,
                if (isDevModeEnabled) Color(0xFFFFB300) else (if (isDark) Color(0xFF334155) else MaterialTheme.colorScheme.outlineVariant),
                RoundedCornerShape(12.dp)
            )
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onToggleDevMode()
            }
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .testTag("tuner_dev_mode_toggle"),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(if (isDevModeEnabled) Color(0xFFFFB300) else Color(0xFF475569)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Build,
                        contentDescription = null,
                        tint = if (isDevModeEnabled) Color.Black else Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }

                Column {
                    Text(
                        text = "TUNER DEV MODE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        ),
                        color = if (isDevModeEnabled) Color(0xFFFFD54F) else (if (isDark) Color(0xFFCBD5E1) else MaterialTheme.colorScheme.onSurface)
                    )
                    Text(
                        text = if (isDevModeEnabled) "Manual pitch override active • Tap to close" else "Tap to manually test pitch & face animations",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = if (isDevModeEnabled) Color(0xFFFFECB3) else (if (isDark) Color(0xFF94A3B8) else MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
            }

            // Compact status switch pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(if (isDevModeEnabled) Color(0xFFFFB300) else Color(0xFF334155))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isDevModeEnabled) "DEV ON" else "DEV OFF",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp
                    ),
                    color = if (isDevModeEnabled) Color.Black else Color.White
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TunerDevControlsCard(
    centsOffset: Float,
    onCentsChange: (Float) -> Unit,
    hasSignal: Boolean,
    onToggleHasSignal: () -> Unit,
    isAllStringsTuned: Boolean,
    onToggleAllStringsTuned: () -> Unit,
    onTriggerLockInBurst: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val isDark = LocalIsDarkTheme.current

    // Determine current face name and description based on cents
    val (faceStateName, faceDescription, statusColor) = when {
        isAllStringsTuned -> Triple("PERFECT (All Tuned)", "Rockstar Lightning Eyes (Victory celebration)", Color(0xFF00E676))
        !hasSignal -> Triple("READY / IDLE", "Rotating Idle Faces (3 natural looks)", Color(0xFF94A3B8))
        kotlin.math.abs(centsOffset) <= 4f -> Triple("PERFECT", "Lightning Bolt Eyes & Confident Rockstar Grin", Color(0xFF00E676))
        centsOffset < -35f -> Triple("EXTREME_FLAT", "Tilted Oval X-Eyes & Sad Frown with Tongue", Color(0xFFFF3D71))
        centsOffset < -15f -> Triple("FLAT", "Dizzy Wavy Eyes & Twinkling Sparkle Stars", Color(0xFFFF6D00))
        centsOffset < -4f -> Triple("CLOSE_FLAT", "Side-Glance Eyes & Flying Sweat Drops", Color(0xFFFFD600))
        centsOffset <= 15f -> Triple("CLOSE_SHARP", "Side-Glance Eyes & Flying Sweat Drops", Color(0xFFFFD600))
        centsOffset <= 35f -> Triple("SHARP", "Dizzy Wavy Eyes & Twinkling Sparkle Stars", Color(0xFFFF6D00))
        else -> Triple("EXTREME_SHARP", "Tilted Oval X-Eyes & Sad Frown with Tongue", Color(0xFFFF3D71))
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .border(1.5.dp, Color(0xFFFFB300), RoundedCornerShape(16.dp))
            .testTag("tuner_dev_controls_panel"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) Color(0xFF15181D) else Color(0xFFFFFBEB)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header: Face trigger monitor
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🛠️ TUNER FACE SIMULATOR",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    ),
                    color = Color(0xFFFFB300)
                )

                // Live Cents Display
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(statusColor.copy(alpha = 0.2f))
                        .border(1.dp, statusColor, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (hasSignal) "${if (centsOffset > 0) "+" else ""}${centsOffset.roundToInt()}¢" else "NO SIGNAL",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = statusColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Current Face Description Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isDark) Color(0xFF1E232B) else Color(0xFFF1F5F9))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "TRIGGERED FACE:",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            ),
                            color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                        )
                        Text(
                            text = faceStateName,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp
                            ),
                            color = statusColor
                        )
                    }
                    Text(
                        text = faceDescription,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = if (isDark) Color.White else Color.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Continuous Tuning Position Slider (-50¢ to +50¢)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tuning Position:",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (isDark) Color.White else Color.Black
                )
                Text(
                    text = "${if (centsOffset > 0) "+" else ""}${centsOffset.roundToInt()} cents",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    ),
                    color = statusColor
                )
            }

            Slider(
                value = centsOffset,
                onValueChange = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onCentsChange(it)
                },
                valueRange = -50f..50f,
                enabled = hasSignal,
                colors = SliderDefaults.colors(
                    thumbColor = statusColor,
                    activeTrackColor = statusColor,
                    inactiveTrackColor = if (isDark) Color(0xFF334155) else Color(0xFFCBD5E1)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("tuner_dev_slider")
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("-50¢ (Dead Flat)", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = Color(0xFF94A3B8))
                Text("0¢ (In Tune)", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold), color = Color(0xFF00E676))
                Text("+50¢ (Dead Sharp)", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = Color(0xFF94A3B8))
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quick Preset Buttons
            Text(
                text = "Quick Face Presets:",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                ),
                color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF334155)
            )

            Spacer(modifier = Modifier.height(6.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                DevPresetChip(
                    label = "😵 RIP (-45¢)",
                    isSelected = hasSignal && centsOffset <= -36f,
                    activeColor = Color(0xFFFF3D71),
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onCentsChange(-45f)
                    }
                )
                DevPresetChip(
                    label = "💫 Flat (-25¢)",
                    isSelected = hasSignal && centsOffset > -36f && centsOffset <= -16f,
                    activeColor = Color(0xFFFF6D00),
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onCentsChange(-25f)
                    }
                )
                DevPresetChip(
                    label = "💦 Near Flat (-8¢)",
                    isSelected = hasSignal && centsOffset > -16f && centsOffset < -4f,
                    activeColor = Color(0xFFFFD600),
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onCentsChange(-8f)
                    }
                )
                DevPresetChip(
                    label = "⚡ IN TUNE (0¢)",
                    isSelected = hasSignal && kotlin.math.abs(centsOffset) <= 4f && !isAllStringsTuned,
                    activeColor = Color(0xFF00E676),
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onCentsChange(0f)
                    }
                )
                DevPresetChip(
                    label = "💦 Near Sharp (+8¢)",
                    isSelected = hasSignal && centsOffset > 4f && centsOffset <= 15f,
                    activeColor = Color(0xFFFFD600),
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onCentsChange(8f)
                    }
                )
                DevPresetChip(
                    label = "💫 Sharp (+25¢)",
                    isSelected = hasSignal && centsOffset > 15f && centsOffset <= 35f,
                    activeColor = Color(0xFFFF6D00),
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onCentsChange(25f)
                    }
                )
                DevPresetChip(
                    label = "😵 RIP (+45¢)",
                    isSelected = hasSignal && centsOffset > 35f,
                    activeColor = Color(0xFFFF3D71),
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onCentsChange(45f)
                    }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Toggles: Signal toggle, Lock-In test, and All Strings celebration
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Toggle Signal (Test Idle Faces)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (!hasSignal) Color(0xFF6366F1).copy(alpha = 0.25f) else (if (isDark) Color(0xFF262C36) else Color(0xFFE2E8F0)))
                        .border(
                            1.dp,
                            if (!hasSignal) Color(0xFF6366F1) else Color.Transparent,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onToggleHasSignal()
                        }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (hasSignal) "Signal: ON" else "😴 Idle Faces",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        ),
                        color = if (!hasSignal) Color(0xFF818CF8) else (if (isDark) Color.White else Color.Black)
                    )
                }

                // Test Lock-In Animation Burst
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF00E676).copy(alpha = 0.15f))
                        .border(1.dp, Color(0xFF00E676).copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onTriggerLockInBurst()
                        }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Lock-In ⚡",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        ),
                        color = Color(0xFF00E676)
                    )
                }

                // Toggle All Strings Tuned
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isAllStringsTuned) Color(0xFFFFD700).copy(alpha = 0.25f) else (if (isDark) Color(0xFF262C36) else Color(0xFFE2E8F0)))
                        .border(
                            1.dp,
                            if (isAllStringsTuned) Color(0xFFFFD700) else Color.Transparent,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onToggleAllStringsTuned()
                        }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isAllStringsTuned) "🏆 All Tuned" else "Victory 🎉",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        ),
                        color = if (isAllStringsTuned) Color(0xFFFFD700) else (if (isDark) Color.White else Color.Black)
                    )
                }
            }
        }
    }
}

@Composable
private fun DevPresetChip(
    label: String,
    isSelected: Boolean,
    activeColor: Color,
    onClick: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(
                if (isSelected) activeColor else (if (isDark) Color(0xFF262C36) else Color(0xFFE2E8F0))
            )
            .border(
                1.dp,
                if (isSelected) activeColor else (if (isDark) Color(0xFF334155) else Color(0xFFCBD5E1)),
                RoundedCornerShape(999.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (isSelected) FontWeight.Black else FontWeight.SemiBold,
                fontSize = 11.sp
            ),
            color = if (isSelected) Color.Black else (if (isDark) Color.White else Color.Black)
        )
    }
}
