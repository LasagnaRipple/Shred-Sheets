package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.InstrumentString
import com.example.model.PitchResult
import com.example.ui.theme.CloseYellow
import com.example.ui.theme.OutOfTuneRed
import com.example.ui.theme.VibrantDarkBorder
import com.example.ui.theme.VibrantDarkCard

@Composable
fun PitchMeter(
    pitchResult: PitchResult,
    hasSignal: Boolean,
    isTunerActive: Boolean = true,
    promptNextString: InstrumentString? = null,
    isAllStringsTuned: Boolean = false,
    onStatusClick: () -> Unit = {},
    stringCount: Int = 6,
    pluckAnimationEvent: Pair<Int, Long>? = null,
    modifier: Modifier = Modifier
) {
    val cents = if (isTunerActive && hasSignal) pitchResult.centsDiff.coerceIn(-50.0, 50.0).toFloat() else 0f
    val animatedCents by animateFloatAsState(
        targetValue = cents,
        animationSpec = spring(stiffness = 400f),
        label = "animatedCents"
    )

    val inTuneColor = Color(0xFF00E676) // Vivid neon green matching reference

    val statusColor by animateColorAsState(
        targetValue = when {
            !isTunerActive -> MaterialTheme.colorScheme.primary
            isAllStringsTuned -> inTuneColor
            promptNextString != null -> MaterialTheme.colorScheme.primary
            !hasSignal || pitchResult.frequency <= 20.0 -> Color(0xFF6B7280)
            pitchResult.isInTune -> inTuneColor
            pitchResult.isClose -> CloseYellow
            else -> OutOfTuneRed
        },
        label = "statusColor"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        // Redesigned Precision Ruler Gauge (inspired by reference image)
        PitchRulerGauge(
            cents = animatedCents,
            isTunerActive = isTunerActive,
            hasSignal = hasSignal && pitchResult.frequency > 20.0,
            isInTune = pitchResult.isInTune,
            statusColor = statusColor,
            stringCount = stringCount,
            pluckAnimationEvent = pluckAnimationEvent,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Precision Pitch Ruler Gauge displaying graduation tick marks and a sweeping needle.
 * Major line breaks are centered directly to the center of each string circle below it.
 */
@Composable
fun PitchRulerGauge(
    cents: Float,
    isTunerActive: Boolean,
    hasSignal: Boolean,
    isInTune: Boolean,
    statusColor: Color,
    modifier: Modifier = Modifier,
    stringCount: Int = 6,
    pluckAnimationEvent: Pair<Int, Long>? = null
) {
    val pluckAnim = remember { Animatable(0f) }
    var vibratingStringIndex by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(pluckAnimationEvent) {
        val event = pluckAnimationEvent ?: return@LaunchedEffect
        val strIndex = (event.first - 1).coerceIn(0, (stringCount - 1).coerceAtLeast(0))
        vibratingStringIndex = strIndex
        // Oscillating vibration sequence mimicking a plucked physical string
        pluckAnim.snapTo(1f)
        pluckAnim.animateTo(
            targetValue = 0f,
            animationSpec = keyframes {
                durationMillis = 350
                1f at 0
                -0.75f at 60
                0.55f at 130
                -0.35f at 200
                0.18f at 270
                0f at 350
            }
        )
        vibratingStringIndex = null
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(112.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF14191F))
            .testTag("pitch_ruler_gauge"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 8.dp)
        ) {
            val width = size.width
            val height = size.height
            val centerY = height / 2f
            val centerX = width / 2f

            val minorTickColor = Color(0xFF2A3440)
            val majorTickColor = Color(0xFF4A5666)
            val minorTickHeight = height * 0.42f
            val majorTickHeight = height * 0.68f

            val numStrings = stringCount.coerceAtLeast(1)
            val colWidth = width / numStrings
            val step = colWidth / 4f

            // String circle centers aligned directly with StringPillStrip columns
            val stringCenters = (0 until numStrings).map { s -> (s + 0.5f) * colWidth }

            // Grid bounds: 1 step outside outermost string centers
            val startX = stringCenters.first() - step
            val endX = stringCenters.last() + step

            var currentX = startX
            while (currentX <= endX + 0.5f) {
                val stringCenterIndex = stringCenters.indexOfFirst { kotlin.math.abs(it - currentX) < 1.0f }
                val isStringCenter = stringCenterIndex != -1
                val isCenter = kotlin.math.abs(centerX - currentX) < 1.0f
                val isMajor = isStringCenter || isCenter

                val isVibrating = isStringCenter && stringCenterIndex == vibratingStringIndex && pluckAnim.value != 0f
                val pluckOffset = if (isVibrating) pluckAnim.value * 3.5.dp.toPx() else 0f
                val vibrationAlpha = if (isVibrating) kotlin.math.abs(pluckAnim.value) else 0f

                val tickHeight = if (isMajor) majorTickHeight else minorTickHeight
                val tickColor = when {
                    isVibrating -> statusColor
                    isCenter && isInTune && hasSignal && isTunerActive -> statusColor
                    isStringCenter -> Color(0xFF5A6A7C)
                    isCenter -> Color(0xFF3B4856)
                    else -> minorTickColor
                }

                // Gauge thickness: thickest on low-pitch string (index 0, e.g. Low E),
                // progressively tapering down to thinnest on high-pitch string (last index, e.g. High E)
                val strokeWidth = when {
                    isStringCenter -> {
                        val fraction = if (numStrings > 1) {
                            stringCenterIndex.toFloat() / (numStrings - 1).toFloat()
                        } else {
                            0.5f
                        }
                        (4.8f - fraction * 3.5f).dp.toPx()
                    }
                    isCenter -> 2.2.dp.toPx()
                    else -> 1.4.dp.toPx()
                }

                if (isVibrating) {
                    drawLine(
                        color = statusColor.copy(alpha = 0.55f * vibrationAlpha),
                        start = Offset(currentX + pluckOffset, centerY - (tickHeight * 1.25f) / 2f),
                        end = Offset(currentX + pluckOffset, centerY + (tickHeight * 1.25f) / 2f),
                        strokeWidth = strokeWidth * 2.5f,
                        cap = StrokeCap.Round
                    )
                }

                drawLine(
                    color = tickColor,
                    start = Offset(currentX + pluckOffset, centerY - tickHeight / 2f),
                    end = Offset(currentX + pluckOffset, centerY + tickHeight / 2f),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round
                )

                currentX += step
            }

            // Draw center target caret ▲ at bottom
            val caretSize = 4.5.dp.toPx()
            val caretBottom = height - 2.dp.toPx()
            val caretPath = Path().apply {
                moveTo(centerX, caretBottom - caretSize * 1.5f)
                lineTo(centerX - caretSize, caretBottom)
                lineTo(centerX + caretSize, caretBottom)
                close()
            }
            drawPath(
                path = caretPath,
                color = if (isInTune && hasSignal && isTunerActive) statusColor else Color(0xFF4A5666)
            )

            // Draw active sweeping needle
            val maxOffset = (width / 2f) - step
            if (isTunerActive && hasSignal) {
                val normalizedCents = (cents / 50f).coerceIn(-1f, 1f)
                val needleX = centerX + normalizedCents * maxOffset
                val needleHeight = height * 0.82f
                val needleWidth = 4.dp.toPx()

                // Glow behind active needle
                drawLine(
                    color = statusColor.copy(alpha = 0.35f),
                    start = Offset(needleX, centerY - needleHeight / 2f),
                    end = Offset(needleX, centerY + needleHeight / 2f),
                    strokeWidth = needleWidth * 2.2f,
                    cap = StrokeCap.Round
                )

                // Needle solid line
                drawLine(
                    color = statusColor,
                    start = Offset(needleX, centerY - needleHeight / 2f),
                    end = Offset(needleX, centerY + needleHeight / 2f),
                    strokeWidth = needleWidth,
                    cap = StrokeCap.Round
                )
            } else {
                // Inactive / resting needle at center with muted color
                val needleHeight = height * 0.70f
                val needleWidth = 2.8.dp.toPx()
                drawLine(
                    color = Color(0xFF333E4C),
                    start = Offset(centerX, centerY - needleHeight / 2f),
                    end = Offset(centerX, centerY + needleHeight / 2f),
                    strokeWidth = needleWidth,
                    cap = StrokeCap.Round
                )
            }
        }
    }
}
