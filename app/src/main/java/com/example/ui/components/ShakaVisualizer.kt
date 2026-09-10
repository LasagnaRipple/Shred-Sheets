package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.InstrumentString
import com.example.model.InstrumentType
import com.example.model.PitchResult
import com.example.ui.theme.LocalIsDarkTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Circle Component Tuner States per the Redesign Specification:
 * - READY: Idle/waiting for pluck or tuner paused
 * - FLAT: Pitch > 15 cents below target
 * - SHARP: Pitch > 15 cents above target
 * - CLOSE_FLAT: Pitch 5-15 cents below target
 * - CLOSE_SHARP: Pitch 5-15 cents above target
 * - PERFECT: Pitch within ±5 cents of target
 */
enum class CircleTunerState {
    READY,
    FLAT,
    SHARP,
    CLOSE_FLAT,
    CLOSE_SHARP,
    PERFECT
}

@Composable
fun ShakaVisualizer(
    pitchResult: PitchResult,
    hasSignal: Boolean,
    primaryColor: Color,
    accentColor: Color,
    isTunerActive: Boolean = true,
    isAllStringsTuned: Boolean = false,
    isStringConfirmed: Boolean = false,
    promptNextString: InstrumentString? = null,
    instrumentEmoji: String = "🎸",
    instrumentType: InstrumentType = InstrumentType.GUITAR,
    onTap: () -> Unit = {},
    onResetTuning: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val isDark = LocalIsDarkTheme.current

    // Raw state calculation from pitch sensor
    val rawState = remember(isTunerActive, hasSignal, pitchResult, isAllStringsTuned) {
        if (isAllStringsTuned) {
            CircleTunerState.PERFECT
        } else if (!isTunerActive || !hasSignal || pitchResult.frequency <= 20.0) {
            CircleTunerState.READY
        } else {
            val cents = pitchResult.centsDiff
            when {
                kotlin.math.abs(cents) <= 5.0 -> CircleTunerState.PERFECT
                cents < -15.0 -> CircleTunerState.FLAT
                cents > 15.0 -> CircleTunerState.SHARP
                cents < 0.0 -> CircleTunerState.CLOSE_FLAT
                else -> CircleTunerState.CLOSE_SHARP
            }
        }
    }

    // Debounce state changes by ~150ms of stable reading to prevent jitter
    var debouncedState by remember { mutableStateOf(rawState) }

    LaunchedEffect(rawState) {
        if (rawState == CircleTunerState.READY) {
            debouncedState = CircleTunerState.READY
        } else {
            delay(150)
            debouncedState = rawState
        }
    }

    // Light haptic tick when entering the perfect zone (distinct from the confirmed lock-in double-pulse)
    var previousState by remember { mutableStateOf(debouncedState) }
    LaunchedEffect(debouncedState) {
        if (debouncedState == CircleTunerState.PERFECT && previousState != CircleTunerState.PERFECT && !isStringConfirmed) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
        previousState = debouncedState
    }

    // One-shot "confirmed" animation burst (1.0 -> 1.15 -> 1.0 over ~300ms) with glow flash
    val confirmedBurstScale = remember { Animatable(1.0f) }
    val confirmedFlashAlpha = remember { Animatable(0f) }

    LaunchedEffect(isStringConfirmed) {
        if (isStringConfirmed) {
            launch {
                confirmedBurstScale.animateTo(
                    targetValue = 1.15f,
                    animationSpec = tween(150, easing = FastOutSlowInEasing)
                )
                confirmedBurstScale.animateTo(
                    targetValue = 1.0f,
                    animationSpec = tween(150, easing = FastOutSlowInEasing)
                )
            }
            launch {
                confirmedFlashAlpha.animateTo(
                    targetValue = 1.0f,
                    animationSpec = tween(120)
                )
                confirmedFlashAlpha.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(180)
                )
            }
        } else {
            confirmedBurstScale.snapTo(1.0f)
            confirmedFlashAlpha.snapTo(0f)
        }
    }

    // Colors per spec
    val borderColorTarget = when (debouncedState) {
        CircleTunerState.READY -> if (isDark) Color(0xFF4A4D3A) else MaterialTheme.colorScheme.outlineVariant
        CircleTunerState.FLAT, CircleTunerState.SHARP -> Color(0xFFD85A30)
        CircleTunerState.CLOSE_FLAT, CircleTunerState.CLOSE_SHARP -> Color(0xFFEF9F27)
        CircleTunerState.PERFECT -> if (isDark) Color(0xFFC8FF3D) else Color(0xFF16A34A)
    }
    val borderColor by animateColorAsState(targetValue = borderColorTarget, label = "circleBorderColor")

    val arrowColorTarget = when (debouncedState) {
        CircleTunerState.FLAT, CircleTunerState.SHARP -> Color(0xFFD85A30)
        CircleTunerState.CLOSE_FLAT, CircleTunerState.CLOSE_SHARP -> Color(0xFFEF9F27)
        else -> Color.Transparent
    }
    val arrowColor by animateColorAsState(targetValue = arrowColorTarget, label = "arrowColor")

    // Badge text and styling
    val (badgeText, badgeTextColor, badgeBg) = when (debouncedState) {
        CircleTunerState.READY -> {
            val text = if (!isTunerActive) "Tap to start" else "Pluck a string"
            val textColor = if (isDark) Color(0xFF8A8D78) else MaterialTheme.colorScheme.onSurfaceVariant
            Triple(text, textColor, Color.Transparent)
        }
        CircleTunerState.FLAT -> {
            val textColor = if (isDark) Color(0xFFF0997B) else Color(0xFF991B1B)
            val bg = if (isDark) Color(0xFF4A1B0C) else Color(0xFFFEE2E2)
            Triple("Flat — tune up", textColor, bg)
        }
        CircleTunerState.SHARP -> {
            val textColor = if (isDark) Color(0xFFF0997B) else Color(0xFF991B1B)
            val bg = if (isDark) Color(0xFF4A1B0C) else Color(0xFFFEE2E2)
            Triple("Sharp — tune down", textColor, bg)
        }
        CircleTunerState.CLOSE_FLAT, CircleTunerState.CLOSE_SHARP -> {
            val textColor = if (isDark) Color(0xFFFAEC9F) else Color(0xFF854D0E)
            val bg = if (isDark) Color(0xFF412402) else Color(0xFFFEF3C7)
            Triple("Almost there", textColor, bg)
        }
        CircleTunerState.PERFECT -> {
            val text = when {
                isAllStringsTuned -> "Time to shred! 🎸"
                isStringConfirmed -> "Locked in! ✓"
                else -> "Perfect — in tune!"
            }
            val textColor = if (isDark) Color(0xFF9FE1CB) else Color(0xFF166534)
            val bg = if (isDark) Color(0xFF04342C) else Color(0xFFDCFCE7)
            Triple(text, textColor, bg)
        }
    }

    // Infinite transitions for scaling and bouncing
    val infiniteTransition = rememberInfiniteTransition(label = "circleComponentAnim")

    // State 1 (Ready) idle pulse: 1.0 -> 1.04 -> 1.0, duration 2.2s (2200ms)
    val idlePulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "idlePulseScale"
    )

    // State 5 (Perfect) success pulse: 1.0 -> 1.07 -> 1.0, duration 1.5s (1500ms)
    val successPulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.07f,
        animationSpec = infiniteRepeatable(
            animation = tween(750, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "successPulseScale"
    )

    // Arrow bounce for Flat / Sharp (0 -> 6dp, duration 700ms)
    val urgentBounce by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(350, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "urgentBounce"
    )

    // Arrow soft bounce for Close (0 -> 3dp, duration 1100ms)
    val softBounce by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(550, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "softBounce"
    )

    val baseCircleScale = when (debouncedState) {
        CircleTunerState.READY -> idlePulseScale
        CircleTunerState.PERFECT -> if (isAllStringsTuned) 1.08f * successPulseScale else successPulseScale
        else -> 1.0f
    }
    val effectiveCircleScale = baseCircleScale * confirmedBurstScale.value

    val arrowDirectionUp = when (debouncedState) {
        CircleTunerState.FLAT, CircleTunerState.CLOSE_FLAT -> true
        CircleTunerState.SHARP, CircleTunerState.CLOSE_SHARP -> false
        else -> null
    }

    val arrowBounceOffsetDp = when (debouncedState) {
        CircleTunerState.FLAT -> -urgentBounce.dp
        CircleTunerState.SHARP -> urgentBounce.dp
        CircleTunerState.CLOSE_FLAT -> -softBounce.dp
        CircleTunerState.CLOSE_SHARP -> softBounce.dp
        else -> 0.dp
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
    ) {
        // Outer Circle container with accent glow behind
        val outerBoxSize = 190.dp
        val circleDiameter = 124.dp
        Box(
            modifier = Modifier
                .size(outerBoxSize)
                .testTag("shaka_visualizer_canvas"),
            contentAlignment = Alignment.Center
        ) {
            // Subtle glow behind pulsating tuner 'play circle' in user selected accent color (matching chord page finger glow)
            val glowColor = accentColor
            if (debouncedState == CircleTunerState.READY) {
                Canvas(modifier = Modifier.size(outerBoxSize)) {
                    val innerRadius = (circleDiameter.toPx() / 2f) * effectiveCircleScale
                    val glowSpread = 30.dp.toPx() * idlePulseScale
                    val outerRadius = innerRadius + glowSpread

                    // Radial glow extending outward from behind the play circle
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                glowColor.copy(alpha = 0.55f),
                                glowColor.copy(alpha = 0.28f),
                                glowColor.copy(alpha = 0.08f),
                                Color.Transparent
                            ),
                            center = center,
                            radius = outerRadius
                        ),
                        radius = outerRadius,
                        center = center
                    )
                }
            } else if (debouncedState == CircleTunerState.PERFECT || confirmedFlashAlpha.value > 0.01f) {
                val glowBaseColor = if (isDark) Color(0xFFC8FF3D) else Color(0xFF16A34A)
                Canvas(modifier = Modifier.size(outerBoxSize)) {
                    val innerRadius = (circleDiameter.toPx() / 2f) * effectiveCircleScale
                    val glowSpread = 28.dp.toPx() * successPulseScale
                    val outerRadius = innerRadius + glowSpread
                    val glowAlpha = (0.50f * successPulseScale + 0.45f * confirmedFlashAlpha.value).coerceAtMost(1f)

                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                glowBaseColor.copy(alpha = glowAlpha),
                                glowBaseColor.copy(alpha = glowAlpha * 0.45f),
                                Color.Transparent
                            ),
                            center = center,
                            radius = outerRadius
                        ),
                        radius = outerRadius,
                        center = center
                    )
                }
            }

            // Circle Container with 2dp border
            Box(
                modifier = Modifier
                    .size(circleDiameter)
                    .scale(effectiveCircleScale)
                    .clip(CircleShape)
                    .background(if (isDark) Color(0xFF151A12) else MaterialTheme.colorScheme.surface)
                    .border(2.dp, borderColor, CircleShape)
                    .pointerInput(Unit) {
                        detectTapGestures {
                            if (isAllStringsTuned) {
                                onResetTuning()
                            } else {
                                onTap()
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                // Bouncing directional arrow (positioned above/below center icon)
                if (arrowDirectionUp != null) {
                    DirectionalChevron(
                        directionUp = arrowDirectionUp,
                        color = arrowColor,
                        modifier = Modifier
                            .align(if (arrowDirectionUp) Alignment.TopCenter else Alignment.BottomCenter)
                            .padding(top = if (arrowDirectionUp) 12.dp else 0.dp, bottom = if (!arrowDirectionUp) 12.dp else 0.dp)
                            .offset(y = arrowBounceOffsetDp)
                    )
                }

                // Center Icon: morphs between states (play / fist / loosening fist / shaka)
                AnimatedContent(
                    targetState = debouncedState,
                    transitionSpec = {
                        (fadeIn(animationSpec = tween(180)) + scaleIn(animationSpec = spring(stiffness = 500f)))
                            .togetherWith(fadeOut(animationSpec = tween(120)) + scaleOut(animationSpec = tween(120)))
                    },
                    label = "centerIconTransition"
                ) { state ->
                    when (state) {
                        CircleTunerState.READY -> {
                            // Play / idle icon
                            Canvas(modifier = Modifier.size(34.dp)) {
                                val w = size.width
                                val h = size.height
                                val path = Path().apply {
                                    moveTo(w * 0.28f, h * 0.16f)
                                    lineTo(w * 0.82f, h * 0.50f)
                                    lineTo(w * 0.28f, h * 0.84f)
                                    close()
                                }
                                drawPath(path = path, color = if (isDark) Color(0xFF8A8D78) else primaryColor)
                            }
                        }
                        CircleTunerState.FLAT, CircleTunerState.SHARP -> {
                            // Clenched Fist 👊
                            Text(
                                text = "👊",
                                fontSize = 42.sp,
                                lineHeight = 42.sp
                            )
                        }
                        CircleTunerState.CLOSE_FLAT, CircleTunerState.CLOSE_SHARP -> {
                            // Fist loosening ✊
                            Text(
                                text = "✊",
                                fontSize = 42.sp,
                                lineHeight = 42.sp
                            )
                        }
                        CircleTunerState.PERFECT -> {
                            // Shaka open 🤘 (rendered at larger scale when all strings are complete)
                            Text(
                                text = "🤘",
                                fontSize = if (isAllStringsTuned) 52.sp else 44.sp,
                                lineHeight = if (isAllStringsTuned) 52.sp else 44.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Text Badge below circle (min 14sp for legibility and accessibility)
        Box(
            modifier = Modifier
                .testTag("tuner_status_badge")
                .clickable {
                    if (isAllStringsTuned) {
                        onResetTuning()
                    } else {
                        onTap()
                    }
                }
                .then(
                    if (badgeBg != Color.Transparent) {
                        Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(badgeBg)
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    } else {
                        Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = badgeText,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (badgeBg != Color.Transparent) FontWeight.Bold else FontWeight.SemiBold,
                    fontSize = 14.sp,
                    letterSpacing = 0.3.sp
                ),
                color = badgeTextColor
            )
        }
    }
}

/**
 * Directional chevron drawn with crisp rounded strokes.
 * Points UP (▲) when directionUp is true (tune up),
 * Points DOWN (▼) when directionUp is false (tune down).
 */
@Composable
private fun DirectionalChevron(
    directionUp: Boolean,
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier.size(width = 24.dp, height = 12.dp)
    ) {
        val w = size.width
        val h = size.height
        val strokeWidth = 3.2.dp.toPx()

        val path = Path().apply {
            if (directionUp) {
                moveTo(3.dp.toPx(), h - 2.dp.toPx())
                lineTo(w / 2f, 2.dp.toPx())
                lineTo(w - 3.dp.toPx(), h - 2.dp.toPx())
            } else {
                moveTo(3.dp.toPx(), 2.dp.toPx())
                lineTo(w / 2f, h - 2.dp.toPx())
                lineTo(w - 3.dp.toPx(), 2.dp.toPx())
            }
        }

        drawPath(
            path = path,
            color = color,
            style = Stroke(
                width = strokeWidth,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }
}
