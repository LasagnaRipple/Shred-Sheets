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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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
 * Circle Component Tuner States per the Spec and Thresholds:
 * - READY: Idle/waiting for pluck or tuner paused (cycles 3 natural cartoon idle faces)
 * - EXTREME_FLAT: Pitch > 35 cents below target ("RIP" tilted oval X-eyes + sad frown with tongue)
 * - FLAT: Pitch 15-35 cents below target ("Flat - Tune up" flaming eyes)
 * - CLOSE_FLAT: Pitch 5-15 cents below target ("Almost there" toothy grin)
 * - PERFECT: Pitch within ±5 cents of target ("Perfect! In Tune" lightning bolt eyes)
 * - CLOSE_SHARP: Pitch 5-15 cents above target ("Almost there" sweat drop eyes)
 * - SHARP: Pitch 15-35 cents above target ("Sharp - Tune down" dizzy wavy eyes + stars)
 * - EXTREME_SHARP: Pitch > 35 cents above target ("RIP" tilted oval X-eyes + sad downturned frown)
 */
enum class CircleTunerState {
    READY,
    EXTREME_FLAT,
    FLAT,
    CLOSE_FLAT,
    PERFECT,
    CLOSE_SHARP,
    SHARP,
    EXTREME_SHARP
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

    // Raw state calculation from pitch sensor with expanded extreme thresholds
    val rawState = remember(isTunerActive, hasSignal, pitchResult, isAllStringsTuned) {
        if (isAllStringsTuned) {
            CircleTunerState.PERFECT
        } else if (!isTunerActive || !hasSignal || pitchResult.frequency <= 20.0) {
            CircleTunerState.READY
        } else {
            val cents = pitchResult.centsDiff
            when {
                kotlin.math.abs(cents) <= com.example.model.MusicalPitchHelper.IN_TUNE_TOLERANCE_CENTS -> CircleTunerState.PERFECT
                cents < -35.0 -> CircleTunerState.EXTREME_FLAT
                cents < -15.0 -> CircleTunerState.FLAT
                cents < 0.0 -> CircleTunerState.CLOSE_FLAT
                cents <= 15.0 -> CircleTunerState.CLOSE_SHARP
                cents <= 35.0 -> CircleTunerState.SHARP
                else -> CircleTunerState.EXTREME_SHARP
            }
        }
    }

    // Direct, responsive state updates without artificial lag
    var debouncedState by remember { mutableStateOf(rawState) }

    LaunchedEffect(rawState) {
        debouncedState = rawState
    }

    // Idle face natural animation rotation: cycles 3 idle faces randomly with natural timing
    var idleFaceType by remember { mutableStateOf(CartoonTunerFaceType.IDLE_PUFFY_SMILE) }

    LaunchedEffect(debouncedState) {
        if (debouncedState == CircleTunerState.READY) {
            val idleFaces = listOf(
                CartoonTunerFaceType.IDLE_PUFFY_SMILE,
                CartoonTunerFaceType.IDLE_CHEEKY,
                CartoonTunerFaceType.IDLE_SIDE_GLANCE
            )
            // Delays mix fast transitions (900ms - 1300ms) and longer lingering looks (2400ms - 3200ms)
            val delayDurations = listOf(2800L, 1200L, 3200L, 900L, 2400L, 1600L)
            var delayIndex = 0
            while (true) {
                val waitTime = delayDurations[delayIndex % delayDurations.size]
                delay(waitTime)
                delayIndex++
                val nextOptions = idleFaces.filter { it != idleFaceType }
                idleFaceType = nextOptions.random()
            }
        }
    }

    val currentFaceType: CartoonTunerFaceType = when (debouncedState) {
        CircleTunerState.READY -> idleFaceType
        CircleTunerState.EXTREME_FLAT -> CartoonTunerFaceType.EXTREME_SHARP
        CircleTunerState.FLAT -> CartoonTunerFaceType.SHARP
        CircleTunerState.CLOSE_FLAT -> CartoonTunerFaceType.CLOSE_SHARP
        CircleTunerState.PERFECT -> CartoonTunerFaceType.PERFECT
        CircleTunerState.CLOSE_SHARP -> CartoonTunerFaceType.CLOSE_SHARP
        CircleTunerState.SHARP -> CartoonTunerFaceType.SHARP
        CircleTunerState.EXTREME_SHARP -> CartoonTunerFaceType.EXTREME_SHARP
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
        CircleTunerState.EXTREME_FLAT, CircleTunerState.EXTREME_SHARP -> Color(0xFFDC2626)
        CircleTunerState.FLAT, CircleTunerState.SHARP -> Color(0xFFD85A30)
        CircleTunerState.CLOSE_FLAT, CircleTunerState.CLOSE_SHARP -> Color(0xFFEF9F27)
        CircleTunerState.PERFECT -> if (isDark) Color(0xFFC8FF3D) else Color(0xFF16A34A)
    }
    val borderColor by animateColorAsState(targetValue = borderColorTarget, label = "circleBorderColor")

    val arrowColorTarget = when (debouncedState) {
        CircleTunerState.EXTREME_FLAT, CircleTunerState.EXTREME_SHARP -> Color(0xFFDC2626)
        CircleTunerState.FLAT, CircleTunerState.SHARP -> Color(0xFFD85A30)
        CircleTunerState.CLOSE_FLAT, CircleTunerState.CLOSE_SHARP -> Color(0xFFEF9F27)
        else -> Color.Transparent
    }
    val arrowColor by animateColorAsState(targetValue = arrowColorTarget, label = "arrowColor")

    // Badge text and styling matching the thresholds diagram
    val (badgeText, badgeTextColor, badgeBg) = when (debouncedState) {
        CircleTunerState.READY -> {
            val text = if (!isTunerActive) "Tap to start" else "Pluck a string"
            val textColor = if (isDark) Color(0xFF8A8D78) else MaterialTheme.colorScheme.onSurfaceVariant
            Triple(text, textColor, Color.Transparent)
        }
        CircleTunerState.EXTREME_FLAT -> {
            val textColor = if (isDark) Color(0xFFFFA285) else Color(0xFF7F1D1D)
            val bg = if (isDark) Color(0xFF5C1D0E) else Color(0xFFFEE2E2)
            Triple("RIP", textColor, bg)
        }
        CircleTunerState.FLAT -> {
            val textColor = if (isDark) Color(0xFFF0997B) else Color(0xFF991B1B)
            val bg = if (isDark) Color(0xFF4A1B0C) else Color(0xFFFEE2E2)
            Triple("Flat — tune up", textColor, bg)
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
                else -> "Perfect! In Tune"
            }
            val textColor = if (isDark) Color(0xFF9FE1CB) else Color(0xFF166534)
            val bg = if (isDark) Color(0xFF04342C) else Color(0xFFDCFCE7)
            Triple(text, textColor, bg)
        }
        CircleTunerState.SHARP -> {
            val textColor = if (isDark) Color(0xFFF0997B) else Color(0xFF991B1B)
            val bg = if (isDark) Color(0xFF4A1B0C) else Color(0xFFFEE2E2)
            Triple("Sharp — tune down", textColor, bg)
        }
        CircleTunerState.EXTREME_SHARP -> {
            val textColor = if (isDark) Color(0xFFFFA285) else Color(0xFF7F1D1D)
            val bg = if (isDark) Color(0xFF5C1D0E) else Color(0xFFFEE2E2)
            Triple("RIP", textColor, bg)
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
        CircleTunerState.EXTREME_FLAT, CircleTunerState.FLAT, CircleTunerState.CLOSE_FLAT -> true
        CircleTunerState.EXTREME_SHARP, CircleTunerState.SHARP, CircleTunerState.CLOSE_SHARP -> false
        else -> null
    }

    val arrowBounceOffsetDp = when (debouncedState) {
        CircleTunerState.EXTREME_FLAT, CircleTunerState.FLAT -> -urgentBounce.dp
        CircleTunerState.EXTREME_SHARP, CircleTunerState.SHARP -> urgentBounce.dp
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
            // Strong, vibrant neon glow behind pulsating tuner 'play circle' in user selected accent color
            val glowColor = accentColor
            if (debouncedState == CircleTunerState.READY) {
                Canvas(modifier = Modifier.size(240.dp)) {
                    val innerRadius = (circleDiameter.toPx() / 2f) * effectiveCircleScale
                    val wideSpread = 44.dp.toPx() * idlePulseScale
                    val wideOuterRadius = innerRadius + wideSpread
                    val wideInnerRatio = (innerRadius / wideOuterRadius).coerceIn(0.4f, 0.75f)

                    // Layer 1: Wide atmospheric neon glow radiating outward
                    drawCircle(
                        brush = Brush.radialGradient(
                            colorStops = arrayOf(
                                0.0f to glowColor.copy(alpha = 0.90f),
                                wideInnerRatio to glowColor.copy(alpha = 0.85f),
                                wideInnerRatio + (1f - wideInnerRatio) * 0.35f to glowColor.copy(alpha = 0.45f),
                                wideInnerRatio + (1f - wideInnerRatio) * 0.70f to glowColor.copy(alpha = 0.18f),
                                1.0f to Color.Transparent
                            ),
                            center = center,
                            radius = wideOuterRadius
                        ),
                        radius = wideOuterRadius,
                        center = center
                    )

                    // Layer 2: Intense core halo hugging the circular border
                    val coreSpread = 16.dp.toPx() * idlePulseScale
                    val coreOuterRadius = innerRadius + coreSpread
                    val coreInnerRatio = (innerRadius / coreOuterRadius).coerceIn(0.5f, 0.85f)

                    drawCircle(
                        brush = Brush.radialGradient(
                            colorStops = arrayOf(
                                0.0f to glowColor.copy(alpha = 0.95f),
                                coreInnerRatio to glowColor.copy(alpha = 0.92f),
                                coreInnerRatio + (1f - coreInnerRatio) * 0.50f to glowColor.copy(alpha = 0.50f),
                                1.0f to Color.Transparent
                            ),
                            center = center,
                            radius = coreOuterRadius
                        ),
                        radius = coreOuterRadius,
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

                // Center Animated Cartoon Face: expressive 1930s rubber-hose cartoon feedback
                AnimatedContent(
                    targetState = currentFaceType,
                    transitionSpec = {
                        (fadeIn(animationSpec = tween(160)) + scaleIn(animationSpec = spring(stiffness = 500f)))
                            .togetherWith(fadeOut(animationSpec = tween(120)) + scaleOut(animationSpec = tween(120)))
                    },
                    label = "centerCartoonFaceTransition"
                ) { faceType ->
                    val faceWidth = if (arrowDirectionUp != null) 78.dp else 84.dp
                    val faceHeight = if (arrowDirectionUp != null) 70.dp else 76.dp
                    CartoonTunerFace(
                        faceType = faceType,
                        isAllStringsTuned = isAllStringsTuned,
                        modifier = Modifier.size(width = faceWidth, height = faceHeight)
                    )
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

/**
 * Modern vector Eyes icon matching the user's reference design:
 * Two tall stadium (capsule) eyes joined in the center with bold outlines,
 * solid white sclera, left-gazing stadium pupils, and crisp specular highlight dots.
 */
@Composable
fun ModernEyesIcon(
    modifier: Modifier = Modifier,
    outlineColor: Color = Color(0xFF141416),
    scleraColor: Color = Color.White,
    pupilColor: Color = Color(0xFF141416)
) {
    val isDark = LocalIsDarkTheme.current
    Canvas(
        modifier = modifier
            .testTag("modern_eyes_icon")
    ) {
        val w = size.width
        val h = size.height
        val strokeWidth = 3.2.dp.toPx().coerceIn(2.5f, h * 0.085f)
        val halfStroke = strokeWidth / 2f

        // Dimensions of each stadium eyeball
        // The two eyes meet at the vertical centerline (w * 0.5f)
        val top = halfStroke
        val bottom = h - halfStroke
        val eyeH = bottom - top

        val left1 = halfStroke
        val right1 = w * 0.5f + halfStroke * 0.5f
        val eyeW1 = right1 - left1

        val left2 = w * 0.5f - halfStroke * 0.5f
        val right2 = w - halfStroke
        val eyeW2 = right2 - left2

        val cornerRadius1 = CornerRadius(eyeW1 / 2f, eyeW1 / 2f)
        val cornerRadius2 = CornerRadius(eyeW2 / 2f, eyeW2 / 2f)

        // Subtle outer contrast stroke in dark mode so the black outline pops against the dark pod
        if (isDark) {
            val outerStrokeWidth = strokeWidth + 1.6.dp.toPx()
            val outerColor = Color(0x38FFFFFF)
            drawRoundRect(
                color = outerColor,
                topLeft = Offset(left1, top),
                size = Size(eyeW1, eyeH),
                cornerRadius = cornerRadius1,
                style = Stroke(width = outerStrokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
            drawRoundRect(
                color = outerColor,
                topLeft = Offset(left2, top),
                size = Size(eyeW2, eyeH),
                cornerRadius = cornerRadius2,
                style = Stroke(width = outerStrokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }

        // 1. Sclera fills (solid white interior)
        drawRoundRect(
            color = scleraColor,
            topLeft = Offset(left1, top),
            size = Size(eyeW1, eyeH),
            cornerRadius = cornerRadius1
        )
        drawRoundRect(
            color = scleraColor,
            topLeft = Offset(left2, top),
            size = Size(eyeW2, eyeH),
            cornerRadius = cornerRadius2
        )

        // 2. Eyeball outer contours and center divider (bold black stroke)
        drawRoundRect(
            color = outlineColor,
            topLeft = Offset(left1, top),
            size = Size(eyeW1, eyeH),
            cornerRadius = cornerRadius1,
            style = Stroke(
                width = strokeWidth,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
        drawRoundRect(
            color = outlineColor,
            topLeft = Offset(left2, top),
            size = Size(eyeW2, eyeH),
            cornerRadius = cornerRadius2,
            style = Stroke(
                width = strokeWidth,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        // 3. Left-gazing stadium pupils (tall rounded capsule)
        val pupilW = eyeW1 * 0.38f
        val pupilH = eyeH * 0.45f
        val pupilCornerRadius = CornerRadius(pupilW / 2f, pupilW / 2f)
        val pupilTop = top + (eyeH - pupilH) * 0.5f

        // Left eye pupil (hugging left interior wall)
        val pupil1Left = left1 + strokeWidth * 0.8f + eyeW1 * 0.07f
        drawRoundRect(
            color = pupilColor,
            topLeft = Offset(pupil1Left, pupilTop),
            size = Size(pupilW, pupilH),
            cornerRadius = pupilCornerRadius
        )

        // Right eye pupil (looking leftward in parallel)
        val pupil2Left = left2 + strokeWidth * 0.8f + eyeW2 * 0.07f
        drawRoundRect(
            color = pupilColor,
            topLeft = Offset(pupil2Left, pupilTop),
            size = Size(pupilW, pupilH),
            cornerRadius = pupilCornerRadius
        )

        // 4. Specular highlight reflection dots (upper-right quadrant of each pupil)
        val dotRadius = (pupilW * 0.20f).coerceIn(1.8f, 3.2.dp.toPx())
        val dot1Center = Offset(
            x = pupil1Left + pupilW * 0.68f,
            y = pupilTop + pupilH * 0.30f
        )
        val dot2Center = Offset(
            x = pupil2Left + pupilW * 0.68f,
            y = pupilTop + pupilH * 0.30f
        )

        drawCircle(
            color = Color.White,
            radius = dotRadius,
            center = dot1Center
        )
        drawCircle(
            color = Color.White,
            radius = dotRadius,
            center = dot2Center
        )
    }
}
