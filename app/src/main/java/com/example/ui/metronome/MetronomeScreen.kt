package com.example.ui.metronome

import android.content.Context
import android.graphics.Paint
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.VibrantDarkBorder
import com.example.ui.theme.VibrantDarkCard
import com.example.ui.theme.VibrantDarkSurface
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

private const val MIN_BPM = 30
private const val MAX_BPM = 240
private const val START_ANGLE = 135f
private const val SWEEP_ANGLE = 270f // From 135° (bottom-left) to 405° / 45° (bottom-right)

@Composable
fun MetronomeScreen(
    bpm: Int,
    isPlaying: Boolean,
    currentBeat: Int,
    timeSignatureBeats: Int,
    onBpmChange: (Int) -> Unit,
    onTogglePlay: () -> Unit,
    onTapTempo: () -> Unit,
    onTimeSignatureChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val view = LocalView.current
    val context = LocalContext.current
    val vibrator = remember(context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    // Strong distinct haptic pulse on each slider/dial increment so every step is distinctly felt
    val triggerStrongTick = remember(vibrator, view, haptic) {
        {
            var executed = false
            if (vibrator != null && vibrator.hasVibrator()) {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
                        executed = true
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createOneShot(20L, VibrationEffect.DEFAULT_AMPLITUDE))
                        executed = true
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator.vibrate(20L)
                        executed = true
                    }
                } catch (_: Exception) {
                    // Fall back to View / Compose haptics
                }
            }

            if (!executed) {
                try {
                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                } catch (_: Exception) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                }
            }
        }
    }

    var showTimeSigDialog by remember { mutableStateOf(false) }
    var showTempoInfoDialog by remember { mutableStateOf(false) }

    val activeAccentColor = MaterialTheme.colorScheme.primary
    val backgroundColor = MaterialTheme.colorScheme.background

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(vertical = 12.dp, horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Section Header (aligned to Chord Library page)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Metronome",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 32.sp,
                        lineHeight = 36.sp,
                        letterSpacing = (-0.5).sp
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            IconButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    showTempoInfoDialog = true
                },
                modifier = Modifier.testTag("header_metronome_info_button")
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = "Tempo Guide",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Top BPM Display & Stepper Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Minus Button (-)
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .border(1.5.dp, activeAccentColor, CircleShape)
                    .clickable {
                        triggerStrongTick()
                        onBpmChange((bpm - 1).coerceAtLeast(MIN_BPM))
                    }
                    .testTag("bpm_decrement_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Decrease BPM",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }

            // BPM Counter & Subtitle
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "$bpm",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontSize = 58.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-1).sp
                    ),
                    color = Color.White,
                    modifier = Modifier.testTag("bpm_text_display")
                )
                Text(
                    text = "Beats per min",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    ),
                    color = Color(0xFF9CA3AF)
                )
            }

            // Plus Button (+)
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .border(1.5.dp, activeAccentColor, CircleShape)
                    .clickable {
                        triggerStrongTick()
                        onBpmChange((bpm + 1).coerceAtMost(MAX_BPM))
                    }
                    .testTag("bpm_increment_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Increase BPM",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Center Jog Wheel Dial with Play/Pause and Tap Tempo Center Pod
        val dialSize = 285.dp
        val innerPodSize = 168.dp

        Box(
            modifier = Modifier.size(dialSize),
            contentAlignment = Alignment.Center
        ) {
            // Radial Ticks and Rotary Jog Wheel Canvas
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures { change, _ ->
                            change.consume()
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val touch = change.position
                            val dx = touch.x - center.x
                            val dy = touch.y - center.y
                            val dist = sqrt(dx * dx + dy * dy)

                            // Only process touches on the dial ring (outside the inner pod)
                            val innerRadiusPx = (innerPodSize / 2f).toPx() * 0.85f
                            if (dist >= innerRadiusPx) {
                                var angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                                if (angle < 0) angle += 360f

                                val adjustedAngle = when {
                                    angle >= START_ANGLE -> angle
                                    angle <= (START_ANGLE + SWEEP_ANGLE - 360f) -> angle + 360f
                                    else -> {
                                        // Touch is in the bottom deadzone between 45° and 135°
                                        if (angle <= 90f) START_ANGLE + SWEEP_ANGLE else START_ANGLE
                                    }
                                }

                                val fraction = ((adjustedAngle - START_ANGLE) / SWEEP_ANGLE).coerceIn(0f, 1f)
                                val calculatedBpm = (MIN_BPM + fraction * (MAX_BPM - MIN_BPM)).roundToInt()
                                if (calculatedBpm != bpm) {
                                    triggerStrongTick()
                                    onBpmChange(calculatedBpm)
                                }
                            }
                        }
                    }
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val dx = offset.x - center.x
                            val dy = offset.y - center.y
                            val dist = sqrt(dx * dx + dy * dy)
                            val innerRadiusPx = (innerPodSize / 2f).toPx() * 0.95f
                            if (dist >= innerRadiusPx) {
                                var angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                                if (angle < 0) angle += 360f

                                val adjustedAngle = when {
                                    angle >= START_ANGLE -> angle
                                    angle <= (START_ANGLE + SWEEP_ANGLE - 360f) -> angle + 360f
                                    else -> if (angle <= 90f) START_ANGLE + SWEEP_ANGLE else START_ANGLE
                                }

                                val fraction = ((adjustedAngle - START_ANGLE) / SWEEP_ANGLE).coerceIn(0f, 1f)
                                val calculatedBpm = (MIN_BPM + fraction * (MAX_BPM - MIN_BPM)).roundToInt()
                                if (calculatedBpm != bpm) {
                                    triggerStrongTick()
                                    onBpmChange(calculatedBpm)
                                }
                            }
                        }
                    }
            ) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val outerRadius = size.width / 2f - 4.dp.toPx()
                val tickInnerRadius = outerRadius - 18.dp.toPx()
                val thumbCenterRadius = outerRadius - 9.dp.toPx()

                val numTicks = 72
                for (i in 0..numTicks) {
                    val tickFraction = i.toFloat() / numTicks
                    val tickAngle = START_ANGLE + tickFraction * SWEEP_ANGLE
                    val rad = Math.toRadians(tickAngle.toDouble())

                    val cosRad = cos(rad).toFloat()
                    val sinRad = sin(rad).toFloat()

                    val startX = center.x + tickInnerRadius * cosRad
                    val startY = center.y + tickInnerRadius * sinRad
                    val endX = center.x + outerRadius * cosRad
                    val endY = center.y + outerRadius * sinRad

                    drawLine(
                        color = Color(0xFF3F4652),
                        start = Offset(startX, startY),
                        end = Offset(endX, endY),
                        strokeWidth = 2.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }

                // Milestone Labels: 30, 75, 135, 195, 240
                val textPaint = Paint().apply {
                    color = android.graphics.Color.parseColor("#8B949E")
                    textSize = 12.sp.toPx()
                    isAntiAlias = true
                    textAlign = Paint.Align.CENTER
                    typeface = android.graphics.Typeface.DEFAULT_BOLD
                }

                val milestones = listOf(
                    30 to "30",
                    75 to "75",
                    135 to "135",
                    195 to "195",
                    240 to "240"
                )

                milestones.forEach { (mBpm, label) ->
                    val fraction = (mBpm - MIN_BPM).toFloat() / (MAX_BPM - MIN_BPM)
                    val mAngle = START_ANGLE + fraction * SWEEP_ANGLE
                    val rad = Math.toRadians(mAngle.toDouble())

                    val labelRadius = tickInnerRadius - 14.dp.toPx()
                    val lx = center.x + labelRadius * cos(rad).toFloat()
                    val ly = center.y + labelRadius * sin(rad).toFloat() + 4.dp.toPx()

                    drawContext.canvas.nativeCanvas.drawText(label, lx, ly, textPaint)
                }

                // Current BPM Thumb Ring (Teal / Volt outline circle)
                val currentFraction = (bpm - MIN_BPM).toFloat() / (MAX_BPM - MIN_BPM)
                val currentAngle = START_ANGLE + currentFraction * SWEEP_ANGLE
                val thumbRad = Math.toRadians(currentAngle.toDouble())
                val thumbX = center.x + thumbCenterRadius * cos(thumbRad).toFloat()
                val thumbY = center.y + thumbCenterRadius * sin(thumbRad).toFloat()

                // Draw thumb background to mask tick marks under it
                drawCircle(
                    color = backgroundColor,
                    radius = 13.dp.toPx(),
                    center = Offset(thumbX, thumbY)
                )

                // Draw thumb indicator ring
                drawCircle(
                    color = activeAccentColor,
                    radius = 13.dp.toPx(),
                    center = Offset(thumbX, thumbY),
                    style = Stroke(width = 2.5.dp.toPx())
                )
            }

            // Inner Circular Pod: Top Half Play/Pause, Bottom Half Tap Tempo
            Box(
                modifier = Modifier
                    .size(innerPodSize)
                    .clip(CircleShape)
                    .background(Color(0xFF16181D))
                    .border(1.dp, Color(0xFF2E333D), CircleShape)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Top Half: Play / Pause Button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onTogglePlay()
                            }
                            .testTag("metronome_play_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = activeAccentColor,
                            modifier = Modifier.size(38.dp)
                        )
                    }

                    // Subtle horizontal divider line
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .padding(horizontal = 24.dp)
                            .background(Color(0xFF2E333D))
                    )

                    // Bottom Half: Tap Tempo Button (standalone finger tap emoji)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onTapTempo()
                            }
                            .testTag("tap_tempo_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "👆",
                            fontSize = 30.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Visual Beat Indicator Dots (Directly below dial)
        Row(
            modifier = Modifier.padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (beat in 1..timeSignatureBeats) {
                val isActive = isPlaying && currentBeat == beat
                val isAccent = beat == 1

                val dotColor by animateColorAsState(
                    targetValue = when {
                        isActive && isAccent -> activeAccentColor
                        isActive -> activeAccentColor.copy(alpha = 0.9f)
                        else -> Color(0xFF4B5563)
                    },
                    animationSpec = tween(durationMillis = 80),
                    label = "dotColor"
                )

                val dotSize by animateDpAsState(
                    targetValue = if (isActive) 15.dp else 12.dp,
                    animationSpec = tween(durationMillis = 80),
                    label = "dotSize"
                )

                Box(
                    modifier = Modifier
                        .size(dotSize)
                        .clip(CircleShape)
                        .background(dotColor)
                        .then(
                            if (isActive) {
                                Modifier.border(1.5.dp, Color.White.copy(alpha = 0.8f), CircleShape)
                            } else {
                                Modifier
                            }
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Bottom Time Signature Capsule Pill
        val timeSigLabel = when (timeSignatureBeats) {
            2 -> "2/4"
            3 -> "3/4"
            4 -> "4/4"
            6 -> "6/8"
            else -> "$timeSignatureBeats/4"
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .border(1.5.dp, activeAccentColor, RoundedCornerShape(24.dp))
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        showTimeSigDialog = true
                    }
                    .padding(horizontal = 28.dp, vertical = 7.dp)
                    .testTag("time_signature_pill"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = timeSigLabel,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    ),
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Time signature",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal
                ),
                color = Color(0xFF9CA3AF)
            )
        }
    }

    // Time Signature Selection Dialog
    if (showTimeSigDialog) {
        val signatures = listOf(2 to "2/4", 3 to "3/4", 4 to "4/4", 6 to "6/8")
        AlertDialog(
            onDismissRequest = { showTimeSigDialog = false },
            containerColor = VibrantDarkSurface,
            title = {
                Text(
                    text = "Select Time Signature",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    signatures.forEach { (beats, label) ->
                        val isSelected = timeSignatureBeats == beats
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) activeAccentColor.copy(alpha = 0.15f) else VibrantDarkCard)
                                .border(
                                    1.5.dp,
                                    if (isSelected) activeAccentColor else VibrantDarkBorder,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onTimeSignatureChange(beats)
                                    showTimeSigDialog = false
                                }
                                .padding(vertical = 14.dp, horizontal = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = if (isSelected) activeAccentColor else Color.White
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTimeSigDialog = false }) {
                    Text("Close", color = activeAccentColor)
                }
            }
        )
    }

    // Tempo Markings Guide Dialog
    if (showTempoInfoDialog) {
        AlertDialog(
            onDismissRequest = { showTempoInfoDialog = false },
            containerColor = VibrantDarkSurface,
            title = {
                Text(
                    text = "Tempo Markings ⏱️",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val tempos = listOf(
                        "Largo" to "40 - 60 BPM (Slow & broad)",
                        "Adagio" to "66 - 76 BPM (Slow & stately)",
                        "Andante" to "76 - 108 BPM (Walking pace)",
                        "Moderato" to "108 - 120 BPM (Moderate)",
                        "Allegro" to "120 - 156 BPM (Fast & bright)",
                        "Presto" to "168 - 200 BPM (Very fast)"
                    )
                    tempos.forEach { (name, desc) ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(VibrantDarkCard)
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = name,
                                fontWeight = FontWeight.Bold,
                                color = activeAccentColor,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = desc,
                                color = Color(0xFF9CA3AF),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTempoInfoDialog = false }) {
                    Text("Got It", color = activeAccentColor)
                }
            }
        )
    }
}
