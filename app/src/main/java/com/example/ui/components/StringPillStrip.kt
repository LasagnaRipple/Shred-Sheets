package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.InstrumentString
import com.example.ui.theme.LocalIsDarkTheme
import com.example.ui.theme.VibrantDarkBorder
import com.example.ui.theme.VibrantDarkCard

@Composable
fun StringPillStrip(
    strings: List<InstrumentString>,
    selectedStringNumber: Int?,
    activeInTuneStringNumber: Int?,
    tunedStringNumbers: Set<Int> = emptySet(),
    recheckingStringNumber: Int? = null,
    isAllStringsTuned: Boolean = false,
    anagramWords: List<String>,
    pluckAnimationEvent: Pair<Int, Long>? = null,
    onStringSelected: (InstrumentString) -> Unit,
    onStringHoldStart: (InstrumentString) -> Unit = onStringSelected,
    onStringHoldStop: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            strings.forEachIndexed { index, instString ->
                val isSelected = selectedStringNumber == instString.stringNumber
                val isTuned = tunedStringNumbers.contains(instString.stringNumber)
                val word = anagramWords.getOrNull(index) ?: instString.defaultWord

                val isPlucked = pluckAnimationEvent?.first == instString.stringNumber
                val pluckScale = remember { Animatable(1f) }
                LaunchedEffect(pluckAnimationEvent) {
                    if (isPlucked) {
                        pluckScale.snapTo(1.22f)
                        pluckScale.animateTo(
                            targetValue = 1f,
                            animationSpec = spring(stiffness = 500f, dampingRatio = 0.45f)
                        )
                    }
                }

                val scale by animateFloatAsState(
                    targetValue = if (isSelected) 1.08f else 1.0f,
                    label = "stringPillScale"
                )

                val isDark = LocalIsDarkTheme.current

                // Row States:
                // 1. Active: Currently selected/being tuned -> Bright highlight (primary)
                // 2. Done: Confirmed in tune this session -> Filled green circle with checkmark, dimmed slightly so it recedes
                // 3. Untuned: Not yet confirmed in tune -> Theme card / pill surface
                val circleBgColor by animateColorAsState(
                    targetValue = when {
                        isSelected -> MaterialTheme.colorScheme.primary
                        isTuned -> if (isDark) Color(0xFF1B4D3E) else Color(0xFFDCFCE7)
                        else -> if (isDark) VibrantDarkCard else MaterialTheme.colorScheme.surfaceVariant
                    },
                    label = "circleBgColor"
                )

                val textColor by animateColorAsState(
                    targetValue = when {
                        isSelected -> MaterialTheme.colorScheme.onPrimary
                        isTuned -> if (isDark) Color(0xFF9FE1CB) else Color(0xFF15803D)
                        else -> if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
                    },
                    label = "textColor"
                )

                val dotColor by animateColorAsState(
                    targetValue = when {
                        isSelected -> MaterialTheme.colorScheme.primary
                        isTuned -> if (isDark) Color(0xFF2E7D32) else Color(0xFF16A34A)
                        else -> if (isDark) Color(0xFF4B5563) else MaterialTheme.colorScheme.outline
                    },
                    label = "dotColor"
                )

                // Fixed-weight column guarantees every string circle stays in an exact stationary position regardless of word length
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .semantics {
                            role = Role.Button
                            onClick {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onStringSelected(instString)
                                true
                            }
                        }
                        .pointerInput(instString) {
                            awaitEachGesture {
                                awaitFirstDown(requireUnconsumed = false)
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onStringHoldStart(instString)
                                try {
                                    waitForUpOrCancellation()
                                } finally {
                                    onStringHoldStop()
                                }
                            }
                        }
                        .padding(vertical = 4.dp)
                        .testTag("string_pill_${instString.stringNumber}"),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // String circle button (e.g., E, A, D, G, B, E or ✓)
                    Box(
                        modifier = Modifier
                            .scale(scale * pluckScale.value)
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(circleBgColor)
                            .border(
                                width = if (isSelected) 0.dp else 1.5.dp,
                                color = when {
                                    isSelected -> Color.Transparent
                                    isTuned -> if (isDark) Color(0xFF2E7D32) else Color(0xFF86EFAC)
                                    else -> if (isDark) VibrantDarkBorder else MaterialTheme.colorScheme.outlineVariant
                                },
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isTuned && !isSelected) "✓" else instString.noteLetter,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = if (isTuned && !isSelected) 21.sp else 18.sp
                            ),
                            color = textColor
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Small indicator dot below circle
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(dotColor)
                    )

                    Spacer(modifier = Modifier.height(3.dp))

                    // Word from Mnemonic below (e.g., "Every", "Angry", "Dad")
                    Text(
                        text = word,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold
                        ),
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            if (isDark) Color(0xFF9CA3AF) else MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 2.dp)
                    )
                }
            }
        }
    }
}
