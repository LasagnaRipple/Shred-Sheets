package com.example.ui.loop

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.core.content.FileProvider
import com.example.model.DrumStyle
import com.example.model.LoopTake
import com.example.model.LoopTrack
import com.example.model.MetronomeSoundMode
import com.example.model.RecordingState
import com.example.ui.theme.LocalIsDarkTheme
import com.example.ui.theme.ShredCardBorder
import com.example.ui.theme.ShredCardSurface
import com.example.ui.theme.ShredMutedText
import com.example.ui.theme.ShredPrimaryText
import com.example.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoopStationScreen(
    viewModel: MainViewModel,
    onThemeToggle: () -> Unit = { viewModel.toggleTheme() },
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val view = LocalView.current
    val isDark = LocalIsDarkTheme.current
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()

    val vibrator = remember(context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    // Strong distinct haptic pulse on each slider increment matching Metronome tactile feel
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

    val activeAccentColor = MaterialTheme.colorScheme.primary
    val backgroundColor = MaterialTheme.colorScheme.background
    val cardBg = if (isDark) Color(0xFF1E293B) else MaterialTheme.colorScheme.surfaceVariant
    val cardBorder = if (isDark) Color(0xFF334155) else MaterialTheme.colorScheme.outlineVariant

    // Metronome Backing State
    val bpm by viewModel.metronomeBpm.collectAsState()
    val isPlaying by viewModel.metronomePlaying.collectAsState()
    val timeSignature by viewModel.metronomeTimeSignature.collectAsState()
    val soundMode by viewModel.metronomeSoundMode.collectAsState()

    // Loop Station Engine State
    val tracks by viewModel.loopStationEngine.tracks.collectAsState()
    val recordingState by viewModel.loopStationEngine.recordingState.collectAsState()
    val armedTrackIndex by viewModel.loopStationEngine.armedTrackIndex.collectAsState()
    val recordingTrackIndex by viewModel.loopStationEngine.recordingTrackIndex.collectAsState()
    val recordingElapsedMs by viewModel.loopStationEngine.recordingElapsedMs.collectAsState()
    val liveInputLevel by viewModel.loopStationEngine.liveInputLevel.collectAsState()
    val loopProgress by viewModel.loopStationEngine.loopProgress.collectAsState()
    val currentBarIndex by viewModel.loopStationEngine.currentBarIndex.collectAsState()
    val backingVolume by viewModel.loopStationEngine.backingVolume.collectAsState()
    val loopLengthSeconds by viewModel.loopStationEngine.loopLengthSeconds.collectAsState()
    val showHeadphoneTip by viewModel.loopStationEngine.showHeadphoneTip.collectAsState()
    val previewingTakeId by viewModel.loopStationEngine.previewingTakeId.collectAsState()
    val currentRecordingTakeNumber by viewModel.loopStationEngine.currentRecordingTakeNumber.collectAsState()
    val exportTrackName by viewModel.exportTrackName.collectAsState()
    val focusManager = LocalFocusManager.current

    val totalBars = 4

    // Dialog & Sheet States
    var showInfoDialog by remember { mutableStateOf(false) }
    var showTimeSigDialog by remember { mutableStateOf(false) }
    var activeSheetTrackIndex by remember { mutableStateOf<Int?>(null) }
    var isExporting by remember { mutableStateOf(false) }
    var showBackingVolumePopup by remember { mutableStateOf(false) }
    var lastVolumeDismissTime by remember { mutableStateOf(0L) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Header: "Loop station", Guide Info Icon (tap title to cycle accent)
        val titleScale = remember { Animatable(1f) }
        val titleScope = rememberCoroutineScope()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        titleScope.launch {
                            titleScale.animateTo(0.90f, animationSpec = tween(70))
                            titleScale.animateTo(
                                1f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessMedium
                                )
                            )
                        }
                        onThemeToggle()
                    }
                    .padding(horizontal = 4.dp, vertical = 2.dp)
                    .semantics {
                        role = Role.Button
                        contentDescription = "Loop station title. Tap to cycle accent color theme."
                    }
                    .testTag("header_loop_station_title"),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = "Loop station",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 32.sp,
                        lineHeight = 36.sp,
                        letterSpacing = (-0.5).sp
                    ),
                    color = activeAccentColor,
                    modifier = Modifier.scale(titleScale.value)
                )
            }

            IconButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    showInfoDialog = true
                },
                modifier = Modifier
                    .size(40.dp)
                    .testTag("loop_station_info_button")
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = "Loop Station Guide",
                    tint = activeAccentColor,
                    modifier = Modifier.size(26.dp)
                )
            }
        }

        // --- SECTION 1: TEMPO ---
        Text(
            text = "TEMPO",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                letterSpacing = 1.2.sp
            ),
            color = if (isDark) Color(0xFF6B7280) else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, top = 6.dp, bottom = 8.dp)
        )

        // Card 1: TEMPO Card (BPM (-) 100 (+), Slider, Divider, Time Signature & Bars in Loop)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, if (isDark) Color(0xFF263242) else cardBorder, RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(
                containerColor = if (isDark) Color(0xFF161B22) else cardBg
            )
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 18.dp)
            ) {
                // Row 1: (-) Button | 100 beats per min | (+) Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Decrement BPM Button (-)
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(if (isDark) Color(0xFF10151E) else MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.5.dp, activeAccentColor, CircleShape)
                            .clickable {
                                triggerStrongTick()
                                viewModel.setMetronomeBpm((bpm - 1).coerceAtLeast(30))
                            }
                            .testTag("loop_bpm_decrement_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Decrease BPM",
                            tint = activeAccentColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // BPM Counter & "beats per min"
                    val tapTempoScale = remember { Animatable(1f) }
                    val tapTempoScope = rememberCoroutineScope()

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                triggerStrongTick()
                                tapTempoScope.launch {
                                    tapTempoScale.animateTo(0.92f, animationSpec = tween(50))
                                    tapTempoScale.animateTo(
                                        1f,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness = Spring.StiffnessMedium
                                        )
                                    )
                                }
                                viewModel.tapTempo()
                            }
                            .padding(horizontal = 12.dp, vertical = 2.dp)
                            .scale(tapTempoScale.value)
                            .semantics {
                                role = Role.Button
                                contentDescription = "Tempo $bpm beats per minute. Tap repeatedly to set tempo."
                            }
                            .testTag("loop_tap_tempo_area")
                    ) {
                        Text(
                            text = "$bpm",
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontSize = 56.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-1).sp
                            ),
                            color = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "beats per min",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp
                            ),
                            color = if (isDark) Color(0xFF94A3B8) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Increment BPM Button (+)
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(if (isDark) Color(0xFF10151E) else MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.5.dp, activeAccentColor, CircleShape)
                            .clickable {
                                triggerStrongTick()
                                viewModel.setMetronomeBpm((bpm + 1).coerceAtMost(240))
                            }
                            .testTag("loop_bpm_increment_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Increase BPM",
                            tint = activeAccentColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Row 2: Horizontal Tempo Slider (with clean tactile response)
                val minBpm = 30
                val maxBpm = 240
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(34.dp)
                        .padding(horizontal = 4.dp)
                        .testTag("loop_bpm_slider")
                        .pointerInput(Unit) {
                            detectTapGestures { offset ->
                                val fraction = (offset.x / size.width).coerceIn(0f, 1f)
                                val calculatedBpm = (minBpm + fraction * (maxBpm - minBpm)).roundToInt()
                                if (calculatedBpm != bpm) {
                                    triggerStrongTick()
                                    viewModel.setMetronomeBpm(calculatedBpm)
                                }
                            }
                        }
                        .pointerInput(Unit) {
                            detectDragGestures { change, _ ->
                                change.consume()
                                val fraction = (change.position.x / size.width).coerceIn(0f, 1f)
                                val calculatedBpm = (minBpm + fraction * (maxBpm - minBpm)).roundToInt()
                                if (calculatedBpm != bpm) {
                                    triggerStrongTick()
                                    viewModel.setMetronomeBpm(calculatedBpm)
                                }
                            }
                        },
                    contentAlignment = Alignment.CenterStart
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val trackHeight = 4.dp.toPx()
                        val thumbRadius = 10.dp.toPx()
                        val cornerRadius = trackHeight / 2f
                        val w = size.width
                        val centerY = size.height / 2f

                        val minX = thumbRadius
                        val maxX = w - thumbRadius
                        val travel = maxX - minX

                        val fraction = ((bpm - minBpm).toFloat() / (maxBpm - minBpm)).coerceIn(0f, 1f)
                        val thumbX = minX + fraction * travel

                        // Unfilled track (dark rail)
                        drawRoundRect(
                            color = if (isDark) Color(0xFF263242) else Color(0xFFCBD5E1),
                            topLeft = Offset(0f, centerY - trackHeight / 2f),
                            size = Size(w, trackHeight),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius, cornerRadius)
                        )

                        // Subtle active rail fill
                        if (thumbX > 0f) {
                            drawRoundRect(
                                color = activeAccentColor.copy(alpha = 0.5f),
                                topLeft = Offset(0f, centerY - trackHeight / 2f),
                                size = Size(thumbX, trackHeight),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius, cornerRadius)
                            )
                        }

                        // Circular Thumb with dark center and accent ring
                        drawCircle(
                            color = if (isDark) Color(0xFF161B22) else Color.White,
                            radius = thumbRadius,
                            center = Offset(thumbX, centerY)
                        )
                        drawCircle(
                            color = activeAccentColor,
                            radius = thumbRadius,
                            center = Offset(thumbX, centerY),
                            style = Stroke(width = 2.5.dp.toPx())
                        )
                    }
                }

                // Subtle Divider line
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 14.dp)
                        .height(1.dp)
                        .background(if (isDark) Color(0xFF242C3D) else Color(0xFFE2E8F0))
                )

                // Row 3: Time Signature (Left) & Bars in Loop (Right)
                val timeSigLabel = when (timeSignature) {
                    2 -> "2/4"
                    3 -> "3/4"
                    4 -> "4/4"
                    6 -> "6/8"
                    else -> "$timeSignature/4"
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: Time signature
                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                triggerStrongTick()
                                showTimeSigDialog = true
                            }
                            .padding(4.dp)
                            .semantics {
                                role = Role.Button
                                contentDescription = "Time signature $timeSigLabel. Tap to change."
                            }
                            .testTag("loop_time_signature_button")
                    ) {
                        Text(
                            text = "Time signature",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Normal
                            ),
                            color = if (isDark) Color(0xFF94A3B8) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = timeSigLabel,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            ),
                            color = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Right: Bars in loop
                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier.padding(4.dp)
                    ) {
                        Text(
                            text = "Bars in loop",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Normal
                            ),
                            color = if (isDark) Color(0xFF94A3B8) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        // 4 Bars dots (First dot active or current playing bar active)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.testTag("loop_bar_indicator")
                        ) {
                            for (bar in 0 until totalBars) {
                                val isLit = if (isPlaying) {
                                    currentBarIndex == bar
                                } else {
                                    bar == 0
                                }

                                if (isLit) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(activeAccentColor)
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .border(1.5.dp, if (isDark) Color(0xFF475569) else Color(0xFF94A3B8), CircleShape)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- SECTION 2: SOUND ---
        Text(
            text = "SOUND",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                letterSpacing = 1.2.sp
            ),
            color = if (isDark) Color(0xFF6B7280) else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, top = 16.dp, bottom = 8.dp)
        )

        // Card 2: SOUND Card ([Click | Drums] Pill Toggle and Volume Button)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, if (isDark) Color(0xFF263242) else cardBorder, RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(
                containerColor = if (isDark) Color(0xFF161B22) else cardBg
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Segmented Pill: [Click | Drums]
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(if (isDark) Color(0xFF0F141C) else MaterialTheme.colorScheme.surface)
                        .border(1.dp, if (isDark) Color(0xFF263242) else MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(999.dp))
                        .padding(3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val isClick = soundMode == MetronomeSoundMode.CLICK
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(if (isClick) activeAccentColor else Color.Transparent)
                            .clickable {
                                triggerStrongTick()
                                viewModel.setMetronomeSoundMode(MetronomeSoundMode.CLICK)
                            }
                            .padding(horizontal = 18.dp, vertical = 8.dp)
                            .testTag("loop_sound_mode_click"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Click",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isClick) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 14.sp
                            ),
                            color = if (isClick) Color.Black else if (isDark) Color(0xFF94A3B8) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    val isDrums = soundMode == MetronomeSoundMode.DRUMS
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(if (isDrums) activeAccentColor else Color.Transparent)
                            .clickable {
                                triggerStrongTick()
                                viewModel.setMetronomeSoundMode(MetronomeSoundMode.DRUMS)
                            }
                            .padding(horizontal = 18.dp, vertical = 8.dp)
                            .testTag("loop_sound_mode_drums"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Drums",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isDrums) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 14.sp
                            ),
                            color = if (isDrums) Color.Black else if (isDark) Color(0xFF94A3B8) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Volume Button + Popup Slider
                Box {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                triggerStrongTick()
                                val now = System.currentTimeMillis()
                                if (showBackingVolumePopup) {
                                    showBackingVolumePopup = false
                                    lastVolumeDismissTime = now
                                } else {
                                    if (now - lastVolumeDismissTime > 280L) {
                                        showBackingVolumePopup = true
                                    } else {
                                        showBackingVolumePopup = false
                                    }
                                }
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("loop_backing_volume_button"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val isVolumeActive = (backingVolume * 100).roundToInt() > 0
                        Icon(
                            imageVector = if (!isVolumeActive) Icons.Default.VolumeMute else if (backingVolume < 0.5f) Icons.Default.VolumeDown else Icons.Default.VolumeUp,
                            contentDescription = "Backing volume",
                            tint = if (isVolumeActive) activeAccentColor else if (isDark) Color(0xFF94A3B8) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${(backingVolume * 100).roundToInt()}%",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            ),
                            color = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Dropdown vertical slider Popup when tapped
                    if (showBackingVolumePopup) {
                        val offsetYPx = with(density) { 42.dp.roundToPx() }
                        Popup(
                            alignment = Alignment.TopEnd,
                            offset = IntOffset(0, offsetYPx),
                            onDismissRequest = {
                                lastVolumeDismissTime = System.currentTimeMillis()
                                showBackingVolumePopup = false
                            },
                            properties = PopupProperties(
                                focusable = false,
                                dismissOnClickOutside = true,
                                dismissOnBackPress = true
                            )
                        ) {
                            Card(
                                modifier = Modifier
                                    .width(76.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .border(1.5.dp, activeAccentColor, RoundedCornerShape(16.dp)),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isDark) Color(0xFF0F172A) else MaterialTheme.colorScheme.surface
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .padding(vertical = 12.dp, horizontal = 10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${(backingVolume * 100).roundToInt()}%",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Black,
                                                fontSize = 11.sp
                                            ),
                                            color = activeAccentColor
                                        )
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Close volume slider",
                                            tint = if (isDark) Color(0xFF94A3B8) else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier
                                                .size(14.dp)
                                                .clickable {
                                                    triggerStrongTick()
                                                    showBackingVolumePopup = false
                                                    lastVolumeDismissTime = System.currentTimeMillis()
                                                }
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    // Vertical Volume Track
                                    val sliderHeight = 130.dp
                                    Box(
                                        modifier = Modifier
                                            .width(36.dp)
                                            .height(sliderHeight)
                                            .testTag("loop_backing_volume_slider")
                                            .pointerInput(Unit) {
                                                detectTapGestures { offset ->
                                                    val fraction = (1f - (offset.y / size.height)).coerceIn(0f, 1f)
                                                    val oldVal = (backingVolume * 20).roundToInt()
                                                    val newVal = (fraction * 20).roundToInt()
                                                    if (oldVal != newVal) triggerStrongTick()
                                                    viewModel.setBackingVolume(fraction)
                                                }
                                            }
                                            .pointerInput(Unit) {
                                                detectDragGestures { change, _ ->
                                                    change.consume()
                                                    val fraction = (1f - (change.position.y / size.height)).coerceIn(0f, 1f)
                                                    val oldVal = (backingVolume * 20).roundToInt()
                                                    val newVal = (fraction * 20).roundToInt()
                                                    if (oldVal != newVal) triggerStrongTick()
                                                    viewModel.setBackingVolume(fraction)
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Canvas(modifier = Modifier.fillMaxSize()) {
                                            val trackWidth = 6.dp.toPx()
                                            val cornerRadius = 3.dp.toPx()
                                            val centerX = size.width / 2f
                                            val h = size.height

                                            // Background rail
                                            drawRoundRect(
                                                color = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0),
                                                topLeft = Offset(centerX - trackWidth / 2f, 0f),
                                                size = Size(trackWidth, h),
                                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius, cornerRadius)
                                            )

                                            // Active fill from bottom
                                            val activeHeight = h * backingVolume
                                            val activeTop = h - activeHeight
                                            drawRoundRect(
                                                color = activeAccentColor,
                                                topLeft = Offset(centerX - trackWidth / 2f, activeTop),
                                                size = Size(trackWidth, activeHeight),
                                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius, cornerRadius)
                                            )

                                            // Thumb circle ring matching metronome
                                            val thumbY = activeTop.coerceIn(8.dp.toPx(), h - 8.dp.toPx())
                                            drawCircle(
                                                color = if (isDark) Color(0xFF0F172A) else Color.White,
                                                radius = 9.dp.toPx(),
                                                center = Offset(centerX, thumbY)
                                            )
                                            drawCircle(
                                                color = activeAccentColor,
                                                radius = 9.dp.toPx(),
                                                center = Offset(centerX, thumbY),
                                                style = Stroke(width = 2.5.dp.toPx())
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // --- PRIMARY ACTION BUTTON: PLAY / STOP ---
        Button(
            onClick = {
                triggerStrongTick()
                viewModel.toggleMetronome()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("loop_backing_play_button"),
            shape = RoundedCornerShape(22.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = activeAccentColor,
                contentColor = Color.Black
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 2.dp)
        ) {
            Text(
                text = if (isPlaying) "Stop" else "Play",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                ),
                color = Color.Black
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 5. Two Track Pads (Track 1, Track 2)
        Text(
            text = "RECORD TRACKS",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                letterSpacing = 1.sp
            ),
            color = if (isDark) Color(0xFF94A3B8) else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, bottom = 8.dp)
        )

        // Headphone Tip Banner (Dismissible) - Recommended for recording tracks
        AnimatedVisibility(
            visible = showHeadphoneTip,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, activeAccentColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(
                    containerColor = activeAccentColor.copy(alpha = 0.12f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Headphones,
                        contentDescription = "Headphones recommended",
                        tint = activeAccentColor,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "For clean multi-track recording without speaker bleed, headphones are recommended.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        ),
                        color = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = {
                            viewModel.dismissHeadphoneTip()
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss tip",
                            tint = if (isDark) Color(0xFF94A3B8) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            tracks.forEachIndexed { index, track ->
                TrackPadColumn(
                    track = track,
                    trackIndex = index,
                    recordingState = recordingState,
                    isArmed = armedTrackIndex == index,
                    isRecording = recordingTrackIndex == index,
                    currentRecordingTakeNumber = currentRecordingTakeNumber,
                    recordingElapsedMs = recordingElapsedMs,
                    targetDurationSec = loopLengthSeconds,
                    liveInputLevel = if (recordingTrackIndex == index) liveInputLevel else 0f,
                    loopProgress = loopProgress,
                    isBackingPlaying = isPlaying,
                    activeAccentColor = activeAccentColor,
                    cardBg = cardBg,
                    cardBorder = cardBorder,
                    isDark = isDark,
                    onPadClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.onTrackPadClicked(index)
                    },
                    onTogglePlayback = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.toggleTrackPlayback(index)
                    },
                    onCycleTake = { dir ->
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.cycleTake(index, dir)
                    },
                    onOpenTakesSheet = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        activeSheetTrackIndex = index
                    },
                    onVolumeChange = { vol ->
                        viewModel.setTrackVolume(index, vol)
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 6. Export Mix Section Header
        Text(
            text = "EXPORT MIX",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                letterSpacing = 1.sp
            ),
            color = if (isDark) Color(0xFF94A3B8) else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, bottom = 8.dp)
        )

        // Export Mix Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .border(1.5.dp, cardBorder, RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = cardBg)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                // Track name label
                Text(
                    text = "Track name",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    color = if (isDark) Color(0xFF94A3B8) else MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Editable Track Name field (borderless with underline)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BasicTextField(
                        value = exportTrackName,
                        onValueChange = { viewModel.setExportTrackName(it) },
                        singleLine = true,
                        textStyle = TextStyle(
                            color = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("export_track_name_input"),
                        decorationBox = { innerTextField ->
                            if (exportTrackName.isEmpty()) {
                                Text(
                                    text = "My track #1",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8)
                                )
                            }
                            innerTextField()
                        }
                    )

                    if (exportTrackName.isNotEmpty()) {
                        IconButton(
                            onClick = { viewModel.setExportTrackName("") },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear track name",
                                tint = if (isDark) Color(0xFF94A3B8) else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Divider line under track name
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 14.dp)
                        .height(1.dp)
                        .background(if (isDark) Color(0xFF242C3D) else Color(0xFFE2E8F0))
                )

                // Dark Summary Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isDark) Color(0xFF0F1520) else Color(0xFFF1F5F9))
                        .padding(horizontal = 14.dp, vertical = 14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        val backingDesc = if (soundMode == MetronomeSoundMode.CLICK) "Click" else "Drums"
                        val backingVolPct = (backingVolume * 100).roundToInt()
                        Text(
                            text = "Backing: $backingDesc, vol $backingVolPct%",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            ),
                            color = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
                        )

                        tracks.forEachIndexed { i, t ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = null,
                                    tint = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                val trackStatus = when {
                                    t.takes.isEmpty() -> "empty, muted"
                                    !t.isPlaybackEnabled -> "${t.takes.size} take${if (t.takes.size > 1) "s" else ""}, muted"
                                    else -> "${t.takes.size} take${if (t.takes.size > 1) "s" else ""}, in mix, vol ${(t.volume * 100).roundToInt()}%"
                                }
                                Text(
                                    text = "Track ${i + 1}: $trackStatus",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Normal
                                    ),
                                    color = if (isDark) Color(0xFF64748B) else Color(0xFF64748B)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Export buttons: Save / Share (single line, robust padding)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            scope.launch {
                                try {
                                    isExporting = true
                                    val file = viewModel.bounceMix(exportTrackName)
                                    Toast.makeText(context, "Saved to device: ${file.name}", Toast.LENGTH_SHORT).show()
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Export error: ${e.message}", Toast.LENGTH_SHORT).show()
                                } finally {
                                    isExporting = false
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("export_mix_save_button"),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDark) Color(0xFF161F2E) else Color(0xFFE2E8F0),
                            contentColor = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
                        ),
                        border = BorderStroke(1.dp, if (isDark) Color(0xFF2E3A4D) else Color(0xFFCBD5E1)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = "Save mix",
                                modifier = Modifier.size(17.dp),
                                tint = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isExporting) "Saving..." else "Save mix",
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp
                                )
                            )
                        }
                    }

                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            scope.launch {
                                try {
                                    isExporting = true
                                    val file = viewModel.bounceMix(exportTrackName)
                                    val uri = FileProvider.getUriForFile(
                                        context,
                                        "${context.packageName}.fileprovider",
                                        file
                                    )
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "audio/wav"
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Share ${file.name}"))
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Share error: ${e.message}", Toast.LENGTH_SHORT).show()
                                } finally {
                                    isExporting = false
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("export_mix_share_button"),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = activeAccentColor,
                            contentColor = Color(0xFF0F172A)
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Upload,
                                contentDescription = "Share mix",
                                modifier = Modifier.size(18.dp),
                                tint = Color(0xFF0F172A)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Share mix",
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp
                                )
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // --- DIALOGS & BOTTOM SHEETS ---

    // 1. Takes Bottom Sheet
    if (activeSheetTrackIndex != null) {
        val trackIdx = activeSheetTrackIndex!!
        val track = tracks.getOrNull(trackIdx)
        if (track != null) {
            TakesBottomSheet(
                track = track,
                trackIndex = trackIdx,
                previewingTakeId = previewingTakeId,
                activeAccentColor = activeAccentColor,
                isDark = isDark,
                onDismiss = {
                    viewModel.loopStationEngine.stopPreview()
                    activeSheetTrackIndex = null
                },
                onSelectActive = { takeId ->
                    viewModel.setActiveTake(trackIdx, takeId)
                },
                onDelete = { takeId ->
                    viewModel.deleteTake(trackIdx, takeId)
                },
                onPreview = { take ->
                    viewModel.previewTake(take)
                }
            )
        }
    }

    // 3. Info & Help Dialog (aligned to Chord Library dialog design)
    if (showInfoDialog) {
        val dialogSurface = if (isDark) ShredCardSurface else MaterialTheme.colorScheme.surface
        val dialogBorder = if (isDark) ShredCardBorder else MaterialTheme.colorScheme.outlineVariant
        val primaryText = if (isDark) ShredPrimaryText else MaterialTheme.colorScheme.onSurface
        val mutedText = if (isDark) ShredMutedText else MaterialTheme.colorScheme.onSurfaceVariant

        Dialog(onDismissRequest = { showInfoDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.5.dp, dialogBorder, RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = dialogSurface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header with Info Icon, Title and Close Button
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
                                imageVector = Icons.Outlined.Info,
                                contentDescription = null,
                                tint = activeAccentColor,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "Loop Station Guide",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 19.sp
                                ),
                                color = primaryText
                            )
                        }
                        IconButton(
                            onClick = { showInfoDialog = false },
                            modifier = Modifier.testTag("close_guide_dialog_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = mutedText
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Guide Items with styled cards like Chord Library
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val guideItems = listOf(
                            Triple("Quantized Recording", "Tap any track pad to arm it. Recording automatically begins on Beat 1 and loops seamlessly.", Icons.Default.Mic),
                            Triple("Multi-Takes", "Record up to 20 takes per track. Cycle takes directly on the pad or open the Takes list to audition.", Icons.Default.PlayArrow),
                            Triple("Real-Time Mixing", "Adjust each track's volume independently. Export or share your combined mix with backing drums.", Icons.Default.VolumeUp),
                            Triple("Headphone Advice", "Use headphones to avoid speaker sound feeding back into your phone microphone.", Icons.Default.Headphones)
                        )

                        guideItems.forEach { (title, desc, icon) ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (isDark) ShredCardBorder.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .border(1.dp, if (isDark) ShredCardBorder.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.Top,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(activeAccentColor.copy(alpha = 0.14f))
                                            .border(1.dp, activeAccentColor.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            tint = activeAccentColor,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = title,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = primaryText
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = desc,
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp, lineHeight = 17.sp),
                                            color = mutedText
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = { showInfoDialog = false },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("dismiss_guide_dialog_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = activeAccentColor,
                            contentColor = Color(0xFF12140F)
                        )
                    ) {
                        Text(
                            text = "Got it! 🎸",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }

    // 4. Time Signature Selection Dialog
    if (showTimeSigDialog) {
        val signatures = listOf(2 to "2/4", 3 to "3/4", 4 to "4/4", 6 to "6/8")
        val dialogSurface = if (isDark) ShredCardSurface else MaterialTheme.colorScheme.surface
        val dialogBorder = if (isDark) ShredCardBorder else MaterialTheme.colorScheme.outlineVariant
        val primaryText = if (isDark) ShredPrimaryText else MaterialTheme.colorScheme.onSurface
        val mutedText = if (isDark) ShredMutedText else MaterialTheme.colorScheme.onSurfaceVariant

        Dialog(onDismissRequest = { showTimeSigDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.5.dp, dialogBorder, RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = dialogSurface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
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
                                imageVector = Icons.Outlined.Info,
                                contentDescription = null,
                                tint = activeAccentColor,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "Time Signature",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 19.sp
                                ),
                                color = primaryText
                            )
                        }
                        IconButton(
                            onClick = { showTimeSigDialog = false },
                            modifier = Modifier.testTag("close_time_sig_dialog_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = mutedText
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        signatures.forEach { (beats, label) ->
                            val isSelected = timeSignature == beats
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        if (isSelected) activeAccentColor.copy(alpha = 0.15f)
                                        else if (isDark) ShredCardBorder.copy(alpha = 0.25f)
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    )
                                    .border(
                                        1.dp,
                                        if (isSelected) activeAccentColor.copy(alpha = 0.7f)
                                        else if (isDark) ShredCardBorder.copy(alpha = 0.5f)
                                        else Color.Transparent,
                                        RoundedCornerShape(14.dp)
                                    )
                                    .clickable {
                                        triggerStrongTick()
                                        viewModel.setMetronomeTimeSignature(beats)
                                        showTimeSigDialog = false
                                    }
                                    .padding(vertical = 12.dp, horizontal = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                                        ),
                                        color = if (isSelected) activeAccentColor else primaryText
                                    )
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = activeAccentColor
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Single column representing one of the 2 tracks.
 */
@Composable
private fun TrackPadColumn(
    track: LoopTrack,
    trackIndex: Int,
    recordingState: RecordingState,
    isArmed: Boolean,
    isRecording: Boolean,
    currentRecordingTakeNumber: Int = 1,
    recordingElapsedMs: Long,
    targetDurationSec: Float,
    liveInputLevel: Float,
    loopProgress: Float,
    isBackingPlaying: Boolean,
    activeAccentColor: Color,
    cardBg: Color,
    cardBorder: Color,
    isDark: Boolean,
    onPadClick: () -> Unit,
    onTogglePlayback: () -> Unit,
    onCycleTake: (Int) -> Unit,
    onOpenTakesSheet: () -> Unit,
    onVolumeChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val coralRecordingColor = Color(0xFFEF4444)
    val emeraldPlayingColor = Color(0xFF10B981)

    // Pulse animation for armed state
    val infiniteTransition = rememberInfiniteTransition(label = "armedPulse")
    val armedPulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(400),
            repeatMode = RepeatMode.Reverse
        ),
        label = "armedAlpha"
    )

    Card(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .border(1.5.dp, if (isRecording || isArmed) coralRecordingColor else cardBorder, RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = cardBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Track Title & Playback Toggle (Speaker symbol)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Track ${trackIndex + 1}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        letterSpacing = 0.sp
                    ),
                    color = if (isRecording || isArmed) coralRecordingColor else activeAccentColor
                )

                // Speaker / Sound icon: tap to bring loop into the mix or mute
                IconButton(
                    onClick = onTogglePlayback,
                    enabled = track.takes.isNotEmpty(),
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("track_${trackIndex + 1}_speaker_button")
                ) {
                    Icon(
                        imageVector = if (track.isPlaybackEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                        contentDescription = if (track.isPlaybackEnabled) "Mute Track ${trackIndex + 1}" else "Unmute Track ${trackIndex + 1}",
                        tint = when {
                            track.takes.isEmpty() -> if (isDark) Color(0xFF64748B) else Color(0xFFCBD5E1)
                            track.isPlaybackEnabled -> emeraldPlayingColor
                            else -> if (isDark) Color(0xFF64748B) else MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Circular Pad (104dp)
            val padSize = 104.dp
            val glowBoxSize = 124.dp
            Box(
                modifier = Modifier
                    .size(glowBoxSize)
                    .padding(2.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isRecording || isArmed || (track.activeTake != null && track.isPlaybackEnabled && isBackingPlaying)) {
                    Canvas(modifier = Modifier.size(glowBoxSize)) {
                        val innerRadius = (padSize.toPx() / 2f)
                        val glowSpread = 12.dp.toPx()
                        val outerRadius = innerRadius + glowSpread

                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    (if (isRecording || isArmed) coralRecordingColor else emeraldPlayingColor).copy(alpha = 0.4f),
                                    (if (isRecording || isArmed) coralRecordingColor else emeraldPlayingColor).copy(alpha = 0.15f),
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

                Box(
                    modifier = Modifier
                        .size(padSize)
                        .clip(CircleShape)
                        .background(if (isDark) Color(0xFF0F1522) else MaterialTheme.colorScheme.surface)
                        .clickable { onPadClick() }
                        .testTag("track_pad_${trackIndex + 1}"),
                    contentAlignment = Alignment.Center
                ) {
                // Background circle reacting to live input level during recording
                val liveScale = if (isRecording) 1.0f + (liveInputLevel * 0.12f) else 1.0f

                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .scale(liveScale)
                ) {
                    val diameter = size.minDimension
                    val radius = diameter / 2f
                    val strokeWidth = 4.dp.toPx()
                    val arcSize = Size(diameter - strokeWidth, diameter - strokeWidth)
                    val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)

                    // Track state rendering:
                    when {
                        isRecording -> {
                            drawCircle(
                                color = coralRecordingColor.copy(alpha = 0.15f),
                                radius = radius
                            )
                            drawArc(
                                color = coralRecordingColor.copy(alpha = 0.3f),
                                startAngle = -90f,
                                sweepAngle = 360f,
                                useCenter = false,
                                topLeft = topLeft,
                                size = arcSize,
                                style = Stroke(width = strokeWidth)
                            )
                            val targetMs = (targetDurationSec * 1000f).coerceAtLeast(1f)
                            val recProgress = (recordingElapsedMs.toFloat() / targetMs).coerceIn(0f, 1f)
                            drawArc(
                                color = coralRecordingColor,
                                startAngle = -90f,
                                sweepAngle = recProgress * 360f,
                                useCenter = false,
                                topLeft = topLeft,
                                size = arcSize,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                        }
                        isArmed -> {
                            drawCircle(
                                color = coralRecordingColor.copy(alpha = 0.15f * armedPulseAlpha),
                                radius = radius
                            )
                            drawArc(
                                color = coralRecordingColor.copy(alpha = armedPulseAlpha),
                                startAngle = -90f,
                                sweepAngle = 360f,
                                useCenter = false,
                                topLeft = topLeft,
                                size = arcSize,
                                style = Stroke(width = strokeWidth)
                            )
                        }
                        track.activeTake != null -> {
                            if (track.isPlaybackEnabled) {
                                drawCircle(
                                    color = emeraldPlayingColor.copy(alpha = 0.12f),
                                    radius = radius
                                )
                                drawArc(
                                    color = emeraldPlayingColor.copy(alpha = 0.3f),
                                    startAngle = -90f,
                                    sweepAngle = 360f,
                                    useCenter = false,
                                    topLeft = topLeft,
                                    size = arcSize,
                                    style = Stroke(width = strokeWidth)
                                )
                                if (isBackingPlaying) {
                                    val currentSweep = loopProgress * 360f
                                    drawArc(
                                        color = emeraldPlayingColor,
                                        startAngle = -90f,
                                        sweepAngle = currentSweep,
                                        useCenter = false,
                                        topLeft = topLeft,
                                        size = arcSize,
                                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                    )
                                    val angleRad = Math.toRadians((currentSweep - 90.0)).toFloat()
                                    val dotRadius = (diameter - strokeWidth) / 2f
                                    val cx = size.width / 2f + dotRadius * cos(angleRad)
                                    val cy = size.height / 2f + dotRadius * sin(angleRad)
                                    drawCircle(
                                        color = Color.White,
                                        radius = 3.5.dp.toPx(),
                                        center = Offset(cx, cy)
                                    )
                                }
                            } else {
                                drawCircle(
                                    color = if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0),
                                    radius = radius
                                )
                                drawArc(
                                    color = if (isDark) Color(0xFF475569) else Color(0xFFCBD5E1),
                                    startAngle = -90f,
                                    sweepAngle = 360f,
                                    useCenter = false,
                                    topLeft = topLeft,
                                    size = arcSize,
                                    style = Stroke(width = 1.5.dp.toPx())
                                )
                            }
                        }
                        else -> {
                            // Empty / Idle: clean circle border
                            drawArc(
                                color = if (isDark) Color(0xFF263345) else Color(0xFFCBD5E1),
                                startAngle = -90f,
                                sweepAngle = 360f,
                                useCenter = false,
                                topLeft = topLeft,
                                size = arcSize,
                                style = Stroke(width = 1.5.dp.toPx())
                            )
                        }
                    }
                }

                // Inner content / text inside circular pad
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(6.dp)
                ) {
                    when {
                        isRecording -> {
                            val elapsedSec = (recordingElapsedMs / 1000f)
                            Text(
                                text = "REC",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 11.sp
                                ),
                                color = coralRecordingColor
                            )
                            Text(
                                text = "Take $currentRecordingTakeNumber • ${String.format("%.1fs", elapsedSec)}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 8.5.sp
                                ),
                                color = Color.White
                            )
                            Text(
                                text = "STOP",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 8.5.sp
                                ),
                                color = if (isDark) Color(0xFF94A3B8) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        isArmed -> {
                            Text(
                                text = "ARMED",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 10.sp
                                ),
                                color = coralRecordingColor
                            )
                            Text(
                                text = "Get ready",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 8.5.sp
                                ),
                                color = if (isDark) Color(0xFFCBD5E1) else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        track.activeTake != null -> {
                            Icon(
                                imageVector = if (track.isPlaybackEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                                contentDescription = if (track.isPlaybackEnabled) "Track in mix" else "Track muted",
                                tint = if (track.isPlaybackEnabled) emeraldPlayingColor else if (isDark) Color(0xFF94A3B8) else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = track.activeTake?.name ?: "",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                ),
                                color = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (track.isPlaybackEnabled) "IN MIX" else "Muted",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 8.sp,
                                    textAlign = TextAlign.Center
                                ),
                                color = if (track.isPlaybackEnabled) emeraldPlayingColor else if (isDark) Color(0xFF94A3B8) else MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                        else -> {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Start recording",
                                tint = if (isDark) Color(0xFF8B9CB2) else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Start\nrecording",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 12.5.sp,
                                    lineHeight = 15.sp,
                                    textAlign = TextAlign.Center
                                ),
                                color = if (isDark) Color(0xFF8B9CB2) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

            Spacer(modifier = Modifier.height(6.dp))

            // Take Selector or "No takes yet" label
            if (track.takes.isNotEmpty()) {
                val totalTakes = track.takes.size
                val currentTakeIndex = track.takes.indexOfFirst { it.id == track.activeTakeId }.let { if (it < 0) 0 else it } + 1

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { onCycleTake(-1) },
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBackIosNew,
                            contentDescription = "Previous take",
                            tint = activeAccentColor,
                            modifier = Modifier.size(12.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isDark) Color(0xFF0F172A) else MaterialTheme.colorScheme.surface)
                            .clickable { onOpenTakesSheet() }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                            .testTag("track_${trackIndex + 1}_takes_list_button")
                    ) {
                        Text(
                            text = "$currentTakeIndex/$totalTakes takes",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            ),
                            color = activeAccentColor
                        )
                    }

                    IconButton(
                        onClick = { onCycleTake(1) },
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowForwardIos,
                            contentDescription = "Next take",
                            tint = activeAccentColor,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            } else {
                Text(
                    text = "No takes yet",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 13.5.sp,
                        color = if (isDark) Color(0xFF64748B) else MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Track Volume Row: Icon + Slider + %
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.VolumeMute,
                    contentDescription = "Track volume",
                    tint = if (isDark) Color(0xFF8B9CB2) else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )

                Slider(
                    value = track.volume,
                    onValueChange = onVolumeChange,
                    modifier = Modifier
                        .weight(1f)
                        .height(24.dp)
                        .padding(horizontal = 4.dp)
                        .testTag("track_${trackIndex + 1}_volume_slider"),
                    colors = SliderDefaults.colors(
                        thumbColor = if (isDark) Color(0xFF475569) else activeAccentColor,
                        activeTrackColor = if (isDark) Color(0xFF475569) else activeAccentColor,
                        inactiveTrackColor = if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0)
                    )
                )

                Text(
                    text = "${(track.volume * 100).roundToInt()}%",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.5.sp
                    ),
                    color = if (isDark) Color(0xFF8B9CB2) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Modal bottom sheet showing all recorded takes for a track.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TakesBottomSheet(
    track: LoopTrack,
    trackIndex: Int,
    previewingTakeId: String?,
    activeAccentColor: Color,
    isDark: Boolean,
    onDismiss: () -> Unit,
    onSelectActive: (String) -> Unit,
    onDelete: (String) -> Unit,
    onPreview: (LoopTake) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = if (isDark) ShredCardSurface else MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Sheet Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Track ${trackIndex + 1} Takes",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        ),
                        color = if (isDark) ShredPrimaryText else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${track.takes.size} of ${com.example.audio.LoopStationEngine.MAX_TAKES_PER_TRACK} takes stored",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp,
                            color = if (isDark) ShredMutedText else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close takes",
                        tint = if (isDark) ShredMutedText else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (track.takes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No takes recorded yet.\nTap the track pad on the main screen to record!",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            textAlign = TextAlign.Center,
                            color = if (isDark) Color(0xFF94A3B8) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(380.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(track.takes, key = { it.id }) { take ->
                        val isActive = take.id == track.activeTakeId
                        val isPreviewing = take.id == previewingTakeId

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .border(
                                    1.5.dp,
                                    if (isActive) activeAccentColor else if (isDark) Color(0xFF334155) else MaterialTheme.colorScheme.outlineVariant,
                                    RoundedCornerShape(12.dp)
                                ),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isDark) Color(0xFF1E293B) else MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Preview play button
                                    IconButton(
                                        onClick = { onPreview(take) },
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(if (isPreviewing) activeAccentColor else activeAccentColor.copy(alpha = 0.15f))
                                    ) {
                                        Icon(
                                            imageVector = if (isPreviewing) Icons.Default.Pause else Icons.Default.PlayArrow,
                                            contentDescription = if (isPreviewing) "Pause take preview" else "Preview take",
                                            tint = if (isPreviewing) MaterialTheme.colorScheme.onPrimary else activeAccentColor,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    // Take name (without rename pencil or notes)
                                    Text(
                                        text = take.name,
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        ),
                                        color = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f)
                                    )

                                    // Pill button to select as active
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(999.dp))
                                            .background(if (isActive) activeAccentColor else Color.Transparent)
                                            .border(
                                                1.5.dp,
                                                if (isActive) activeAccentColor else if (isDark) Color(0xFF64748B) else MaterialTheme.colorScheme.outline,
                                                RoundedCornerShape(999.dp)
                                            )
                                            .clickable { onSelectActive(take.id) }
                                            .padding(horizontal = 10.dp, vertical = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            if (isActive) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onPrimary,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                            }
                                            Text(
                                                text = if (isActive) "Active" else "Select",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.sp
                                                ),
                                                color = if (isActive) MaterialTheme.colorScheme.onPrimary else if (isDark) Color(0xFFCBD5E1) else MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(4.dp))

                                    // Delete icon moved next to selected / active
                                    IconButton(
                                        onClick = { onDelete(take.id) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete take",
                                            tint = if (isDark) Color(0xFF94A3B8) else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Mini Waveform Canvas Thumbnail (28 bars)
                                WaveformThumbnail(
                                    waveformPoints = take.waveformPoints,
                                    activeAccentColor = if (isActive) activeAccentColor else Color(0xFF64748B),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(26.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (isDark) Color(0xFF0F172A) else Color(0xFFE2E8F0))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Draws a mini visual waveform thumbnail from the pre-computed peak points.
 */
@Composable
private fun WaveformThumbnail(
    waveformPoints: List<Float>,
    activeAccentColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val points = if (waveformPoints.isNotEmpty()) waveformPoints else List(24) { 0.15f }
        val barCount = points.size
        val availableWidth = size.width
        val barWidth = (availableWidth / (barCount * 1.5f)).coerceAtLeast(2f)
        val spacing = barWidth * 0.5f
        val centerY = size.height / 2f
        val maxAmplitude = size.height * 0.45f

        for (i in 0 until barCount) {
            val x = i * (barWidth + spacing) + (barWidth / 2f)
            val amplitude = (points[i] * maxAmplitude).coerceAtLeast(2f)
            drawLine(
                color = activeAccentColor,
                start = Offset(x, centerY - amplitude),
                end = Offset(x, centerY + amplitude),
                strokeWidth = barWidth,
                cap = StrokeCap.Round
            )
        }
    }
}
