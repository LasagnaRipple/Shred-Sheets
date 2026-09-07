package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.model.AccentColor

/**
 * Reusable accent color swatch selector.
 * In Dark Mode: Yellow (default), Orange, Purple, Green, Blue, Red, White are available (Black disabled).
 * In Light Mode: Orange, Purple, Green, Blue, Red, Black are available (Yellow & White disabled for readability).
 */
@Composable
fun AccentColorSwatchPicker(
    selectedAccent: AccentColor,
    isDark: Boolean,
    onSelectAccent: (AccentColor) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val availableAccents = remember(isDark) {
        AccentColor.entries.filter { if (isDark) it.darkAllowed else it.lightAllowed }
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        availableAccents.forEach { accent ->
            val isSelected = selectedAccent == accent
            val checkmarkTint = when (accent) {
                AccentColor.YELLOW, AccentColor.WHITE, AccentColor.GREEN, AccentColor.BLUE -> Color(0xFF121212)
                AccentColor.ORANGE, AccentColor.PURPLE, AccentColor.RED, AccentColor.BLACK -> Color.White
            }

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .testTag("accent_swatch_${accent.id}")
                    .semantics { contentDescription = "${accent.displayName} accent" }
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onSelectAccent(accent)
                    },
                contentAlignment = Alignment.Center
            ) {
                // Outer selection ring
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .border(
                                width = 2.5.dp,
                                color = if (isDark) Color.White else Color(0xFF121212),
                                shape = CircleShape
                            )
                    )
                }

                // Swatch Circle
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(accent.swatchColor)
                        .then(
                            // Add subtle contrast border for White and Black swatches
                            when (accent) {
                                AccentColor.WHITE -> Modifier.border(1.dp, Color(0xFF9E9E9E), CircleShape)
                                AccentColor.BLACK -> Modifier.border(1.dp, Color(0xFF616161), CircleShape)
                                else -> Modifier
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = checkmarkTint,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
