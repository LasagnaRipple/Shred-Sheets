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
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.core.content.FileProvider
import com.example.model.DrumStyle
import com.example.model.LoopTake
import com.example.model.LoopTrack
import com.example.model.MetronomeSoundMode
import com.example.model.RecordingState
import com.example.ui.theme.LocalIsDarkTheme
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
    val drumStyle by viewModel.drumStyle.collectAsState()

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

    val totalBars = 4

    // Dialog & Sheet States
    var showInfoDialog by remember { mutableStateOf(false) }
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
        // 1. Header: "Loop station", Guide Info Icon
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Loop Station",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 32.sp,
                        lineHeight = 36.sp,
                        letterSpacing = (-0.5).sp
                    ),
                    color = activeAccentColor
                )
            }

            IconButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    showInfoDialog = true
                },
                modifier = Modifier.testTag("loop_station_info_button")
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = "Loop Station Guide",
                    tint = activeAccentColor
                )
            }
        }

        // 2. Headphone Tip Banner (Dismissible)
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

        // Title: SETUP YOUR BEAT (outside component, matching TRACKS)
        Text(
            text = "SETUP YOUR BEAT",
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

        // 3. Backing Bar Card (Play/Pause, Sound Mode, Tempo controls, Volume)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .border(1.5.dp, cardBorder, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = cardBg)
        ) {
            Column(
                modifier = Modifier.padding(14.dp)
            ) {
                // Top row: [Click / Drums Toggle] (Left) & [Backing Vol Button + Dropdown] (Right)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Backing Sound Mode Toggle (Click / Drums) matching Metronome segmented pill
                    val chipSurface = if (isDark) Color(0xFF0F172A) else MaterialTheme.colorScheme.surface
                    val chipBorderColor = if (isDark) Color(0xFF334155) else MaterialTheme.colorScheme.outlineVariant
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(chipSurface)
                            .border(1.5.dp, chipBorderColor, RoundedCornerShape(999.dp))
                            .padding(2.dp),
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
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                                .testTag("loop_sound_mode_click"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Click",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isClick) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 12.sp
                                ),
                                color = if (isClick) MaterialTheme.colorScheme.onPrimary else if (isDark) Color(0xFF94A3B8) else MaterialTheme.colorScheme.onSurfaceVariant
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
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                                .testTag("loop_sound_mode_drums"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Drums",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isDrums) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 12.sp
                                ),
                                color = if (isDrums) MaterialTheme.colorScheme.onPrimary else if (isDark) Color(0xFF94A3B8) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Backing Vol Button with percentage badge + dropdown vertical slider (icon + percentage badge only)
                    Box {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (showBackingVolumePopup) activeAccentColor.copy(alpha = 0.15f) else Color.Transparent)
                                .border(
                                    1.dp,
                                    if (showBackingVolumePopup) activeAccentColor else if (isDark) Color(0xFF334155) else MaterialTheme.colorScheme.outlineVariant,
                                    RoundedCornerShape(8.dp)
                                )
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
                                .padding(horizontal = 8.dp, vertical = 5.dp)
                                .testTag("loop_backing_volume_button"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (backingVolume <= 0.01f) Icons.Default.VolumeMute else if (backingVolume < 0.5f) Icons.Default.VolumeDown else Icons.Default.VolumeUp,
                                contentDescription = "Backing volume",
                                tint = if (showBackingVolumePopup) activeAccentColor else if (isDark) Color(0xFF94A3B8) else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .border(1.dp, if (isDark) Color(0xFF475569) else MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 5.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${(backingVolume * 100).roundToInt()}%",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    ),
                                    color = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        // Dropdown vertical slider Popup when tapped (tap on button or anywhere outside dismisses)
                        if (showBackingVolumePopup) {
                            val offsetYPx = with(density) { 38.dp.roundToPx() }
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
                                        .width(72.dp)
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

                                        Spacer(modifier = Modifier.height(6.dp))

                                        Icon(
                                            imageVector = Icons.Default.VolumeDown,
                                            contentDescription = null,
                                            tint = if (isDark) Color(0xFF94A3B8) else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Row 2: BPM Counter (Large 58sp font matching MetronomeScreen) flanked by (-) and (+) Circular Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Decrement BPM Button (-) matching Metronome circular border style
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(if (isDark) Color.Transparent else MaterialTheme.colorScheme.surfaceVariant)
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
                            tint = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // BPM Counter & Subtitle (Beats per min) - strictly re-using MetronomeScreen typography
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "$bpm",
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontSize = 54.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = (-1).sp
                            ),
                            color = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Beats per min",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp
                            ),
                            color = if (isDark) Color(0xFF9CA3AF) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Increment BPM Button (+) matching Metronome circular border style
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(if (isDark) Color.Transparent else MaterialTheme.colorScheme.surfaceVariant)
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
                            tint = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Row 3: Horizontal Tactile Slider with Circle Indicator (re-using metronome slider tick feel & circle thumb)
                val minBpm = 30
                val maxBpm = 240
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                        .padding(horizontal = 6.dp)
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
                        val trackHeight = 8.dp.toPx()
                        val thumbRadius = 11.dp.toPx()
                        val cornerRadius = trackHeight / 2f
                        val w = size.width
                        val centerY = size.height / 2f

                        // Available horizontal travel for thumb center
                        val minX = thumbRadius
                        val maxX = w - thumbRadius
                        val travel = maxX - minX

                        val fraction = ((bpm - minBpm).toFloat() / (maxBpm - minBpm)).coerceIn(0f, 1f)
                        val thumbX = minX + fraction * travel

                        // Unfilled track (dark rail)
                        drawRoundRect(
                            color = if (isDark) Color(0xFF1E2530) else Color(0xFFCBD5E1),
                            topLeft = Offset(0f, centerY - trackHeight / 2f),
                            size = Size(w, trackHeight),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius, cornerRadius)
                        )

                        // Filled track (primary accent)
                        if (thumbX > 0f) {
                            drawRoundRect(
                                color = activeAccentColor,
                                topLeft = Offset(0f, centerY - trackHeight / 2f),
                                size = Size(thumbX, trackHeight),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius, cornerRadius)
                            )
                        }

                        // Milestone tick marks along slider
                        val numTicks = 28
                        for (i in 0..numTicks) {
                            val tickFraction = i.toFloat() / numTicks
                            val tickX = minX + tickFraction * travel
                            val isPassed = tickX <= thumbX
                            val tickColor = if (isPassed) {
                                activeAccentColor.copy(alpha = 0.5f)
                            } else {
                                if (isDark) Color(0xFF334155) else Color(0xFF94A3B8)
                            }
                            drawLine(
                                color = tickColor,
                                start = Offset(tickX, centerY - 5.dp.toPx()),
                                end = Offset(tickX, centerY + 5.dp.toPx()),
                                strokeWidth = 1.5.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                        }

                        // Circular Thumb (Circle outline ring matching the Metronome dial circle)
                        drawCircle(
                            color = cardBg,
                            radius = thumbRadius,
                            center = Offset(thumbX, centerY)
                        )
                        drawCircle(
                            color = activeAccentColor,
                            radius = thumbRadius,
                            center = Offset(thumbX, centerY),
                            style = Stroke(width = 3.dp.toPx())
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Row 4: Centered Play/Stop Button
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(if (isPlaying) activeAccentColor else activeAccentColor.copy(alpha = 0.15f))
                            .border(
                                1.5.dp,
                                if (isPlaying) activeAccentColor else activeAccentColor.copy(alpha = 0.6f),
                                RoundedCornerShape(999.dp)
                            )
                            .clickable {
                                triggerStrongTick()
                                viewModel.toggleMetronome()
                            }
                            .padding(horizontal = 22.dp, vertical = 9.dp)
                            .testTag("loop_backing_play_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Stop" else "Play",
                                tint = if (isPlaying) MaterialTheme.colorScheme.onPrimary else activeAccentColor,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isPlaying) "Stop" else "Play",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                ),
                                color = if (isPlaying) MaterialTheme.colorScheme.onPrimary else activeAccentColor
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Row 5: Centered BAR O O O O position indicator dots below Play Beat
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "BAR",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            letterSpacing = 1.sp
                        ),
                        color = if (isDark) Color(0xFF94A3B8) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))

                    for (bar in 0 until totalBars) {
                        val isCurrentBar = isPlaying && (currentBarIndex == bar)
                        val isPastBar = isPlaying && (bar < currentBarIndex)

                        val dotColor by animateColorAsState(
                            targetValue = when {
                                isCurrentBar -> activeAccentColor
                                isPastBar -> activeAccentColor.copy(alpha = 0.7f)
                                else -> if (isDark) Color(0xFF475569) else MaterialTheme.colorScheme.outlineVariant
                            },
                            animationSpec = tween(120),
                            label = "loopDotColor"
                        )

                        val dotSize by animateFloatAsState(
                            targetValue = if (isCurrentBar) 12f else 8f,
                            animationSpec = tween(120),
                            label = "loopDotSize"
                        )

                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .size(dotSize.dp)
                                .clip(CircleShape)
                                .background(dotColor)
                                .then(
                                    if (isCurrentBar) Modifier.border(1.5.dp, Color.White, CircleShape)
                                    else Modifier
                                )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 5. Two Track Pads (Track 1, Track 2)
        Text(
            text = "TRACKS",
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

        // 6. Export Mix Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .border(1.5.dp, cardBorder, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = cardBg)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = "Export Mix",
                        tint = activeAccentColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Export Mix",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        ),
                        color = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Summary of included streams
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isDark) Color(0xFF0F172A) else MaterialTheme.colorScheme.surface)
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val backingDesc = if (soundMode == MetronomeSoundMode.CLICK) "Click" else "Drums"
                    Text(
                        text = "• Backing: $backingDesc · Vol ${(backingVolume * 100).roundToInt()}%",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = if (isDark) Color(0xFFCBD5E1) else MaterialTheme.colorScheme.onSurface
                    )
                    tracks.forEachIndexed { i, t ->
                        val takeDesc = t.activeTake?.name ?: "Empty"
                        val mixDesc = if (t.isPlaybackEnabled) "In Mix" else "Muted"
                        val volDesc = if (t.activeTake != null) "· Vol ${(t.volume * 100).roundToInt()}%" else ""
                        Text(
                            text = "• Track ${i + 1}: $takeDesc · $mixDesc $volDesc",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = if (t.activeTake != null && t.isPlaybackEnabled) activeAccentColor else if (isDark) Color(0xFF64748B) else MaterialTheme.colorScheme.outline
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Export buttons: Save / Share
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            scope.launch {
                                try {
                                    isExporting = true
                                    val file = viewModel.bounceMix()
                                    Toast.makeText(context, "Mix saved: ${file.name}", Toast.LENGTH_SHORT).show()
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Export error: ${e.message}", Toast.LENGTH_SHORT).show()
                                } finally {
                                    isExporting = false
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("export_mix_save_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = activeAccentColor.copy(alpha = 0.2f),
                            contentColor = activeAccentColor
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (isExporting) "Rendering..." else "Save Mix",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Button(
                        onClick = {
                            scope.launch {
                                try {
                                    isExporting = true
                                    val file = viewModel.bounceMix()
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
                                    context.startActivity(Intent.createChooser(shareIntent, "Share Shred Sheets Mix"))
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Share error: ${e.message}", Toast.LENGTH_SHORT).show()
                                } finally {
                                    isExporting = false
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("export_mix_share_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = activeAccentColor,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Share Mix",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
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
                onRename = { takeId, newName ->
                    viewModel.updateTakeName(trackIdx, takeId, newName)
                },
                onUpdateNotes = { takeId, notes ->
                    viewModel.updateTakeNotes(trackIdx, takeId, notes)
                },
                onDelete = { takeId ->
                    viewModel.deleteTake(trackIdx, takeId)
                },
                onPreview = { take ->
                    viewModel.previewTake(take)
                },
                onShare = { take ->
                    try {
                        val file = File(take.audioFilePath)
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
                        context.startActivity(Intent.createChooser(shareIntent, "Share ${take.name}"))
                    } catch (e: Exception) {
                        Toast.makeText(context, "Share failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
    }

    // 3. Info & Help Dialog
    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null,
                        tint = activeAccentColor
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Loop Station Guide",
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "• Quantized Recording: Tap any track pad to arm it. Recording automatically begins on the next Beat 1 downbeat of the backing track and stops at the end of the loop.",
                        style = MaterialTheme.typography.bodySmall,
                        lineHeight = 16.sp
                    )
                    Text(
                        text = "• Multi-Takes: Record up to 20 takes per track. Cycle takes directly on the pad or open the Takes list to audition, rename, or delete takes.",
                        style = MaterialTheme.typography.bodySmall,
                        lineHeight = 16.sp
                    )
                    Text(
                        text = "• Real-Time Mixing: Adjust each track's volume independently. Export or share your combined mix with the backing drums.",
                        style = MaterialTheme.typography.bodySmall,
                        lineHeight = 16.sp
                    )
                    Text(
                        text = "• Headphone Advice: Use headphones to avoid the speaker output feeding back into your phone microphone while playing guitar.",
                        style = MaterialTheme.typography.bodySmall,
                        lineHeight = 16.sp
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showInfoDialog = false }) {
                    Text("Got It")
                }
            }
        )
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
            .clip(RoundedCornerShape(16.dp))
            .border(1.5.dp, if (isRecording || isArmed) coralRecordingColor else cardBorder, RoundedCornerShape(16.dp)),
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
                    text = "TRACK ${trackIndex + 1}",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        letterSpacing = 0.5.sp
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
                            track.takes.isEmpty() -> if (isDark) Color(0xFF475569) else Color(0xFFCBD5E1)
                            track.isPlaybackEnabled -> emeraldPlayingColor
                            else -> if (isDark) Color(0xFF94A3B8) else MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Circular Pad (100dp for spacious 2-column layout) with prominent accent glow behind
            val padSize = 100.dp
            val glowBoxSize = 132.dp
            Box(
                modifier = Modifier
                    .size(glowBoxSize)
                    .padding(2.dp),
                contentAlignment = Alignment.Center
            ) {
                // Subtle accent glow behind Track 1 / Track 2 pad matching chord page & tuner play circle
                Canvas(modifier = Modifier.size(glowBoxSize)) {
                    val innerRadius = (padSize.toPx() / 2f)
                    val glowSpread = 16.dp.toPx()
                    val outerRadius = innerRadius + glowSpread

                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                activeAccentColor.copy(alpha = 0.55f),
                                activeAccentColor.copy(alpha = 0.28f),
                                activeAccentColor.copy(alpha = 0.08f),
                                Color.Transparent
                            ),
                            center = center,
                            radius = outerRadius
                        ),
                        radius = outerRadius,
                        center = center
                    )
                }

                Box(
                    modifier = Modifier
                        .size(padSize)
                        .clip(CircleShape)
                        .background(if (isDark) Color(0xFF0F172A) else MaterialTheme.colorScheme.surface)
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
                    val strokeWidth = 5.dp.toPx()
                    val arcSize = Size(diameter - strokeWidth, diameter - strokeWidth)
                    val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)

                    // Track state rendering:
                    when {
                        isRecording -> {
                            // Recording: coral ring filling 0..100%
                            drawCircle(
                                color = coralRecordingColor.copy(alpha = 0.15f),
                                radius = radius
                            )
                            // Track background
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
                            // Armed: pulsing coral border
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
                            // Has active take: emerald ring filling when enabled and playing, or muted ring
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
                                    // Bright dot riding leading edge
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
                                // Muted take: static subtle neutral ring
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
                                    style = Stroke(width = 2.dp.toPx())
                                )
                            }
                        }
                        else -> {
                            // Empty / Idle: subtle dashed border
                            drawCircle(
                                color = if (isDark) Color(0xFF0F172A) else Color.White.copy(alpha = 0.5f),
                                radius = radius
                            )
                            drawArc(
                                color = if (isDark) Color(0xFF475569) else Color(0xFFCBD5E1),
                                startAngle = -90f,
                                sweepAngle = 360f,
                                useCenter = false,
                                topLeft = topLeft,
                                size = arcSize,
                                style = Stroke(width = 2.dp.toPx())
                            )
                        }
                    }
                }

                // Inner content / text inside circular pad
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(4.dp)
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
                                modifier = Modifier.size(20.dp)
                            )
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
                                text = if (track.isPlaybackEnabled) "IN MIX" else "MUTED",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 8.5.sp
                                ),
                                color = if (track.isPlaybackEnabled) emeraldPlayingColor else if (isDark) Color(0xFF94A3B8) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        else -> {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Tap to record",
                                tint = if (isDark) Color(0xFF94A3B8) else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Tap",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 10.sp
                                ),
                                color = if (isDark) Color(0xFF94A3B8) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

            // Take Selector Badge & Chevrons
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

                    // Badge: Take X/Y
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isDark) Color(0xFF0F172A) else MaterialTheme.colorScheme.surface)
                            .clickable { onOpenTakesSheet() }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "$currentTakeIndex/$totalTakes",
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
                    text = "No takes",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        color = if (isDark) Color(0xFF64748B) else MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            // Takes sheet icon button
            IconButton(
                onClick = { onOpenTakesSheet() },
                modifier = Modifier
                    .size(28.dp)
                    .testTag("track_${trackIndex + 1}_takes_list_button")
            ) {
                Icon(
                    imageVector = Icons.Default.QueueMusic,
                    contentDescription = "Takes list",
                    tint = activeAccentColor,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Track Volume Slider
            Slider(
                value = track.volume,
                onValueChange = onVolumeChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .testTag("track_${trackIndex + 1}_volume_slider"),
                colors = SliderDefaults.colors(
                    thumbColor = activeAccentColor,
                    activeTrackColor = activeAccentColor
                )
            )

            Text(
                text = "${(track.volume * 100).roundToInt()}%",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp
                ),
                color = if (isDark) Color(0xFF94A3B8) else MaterialTheme.colorScheme.onSurfaceVariant
            )
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
    onRename: (String, String) -> Unit,
    onUpdateNotes: (String, String) -> Unit,
    onDelete: (String) -> Unit,
    onPreview: (LoopTake) -> Unit,
    onShare: (LoopTake) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var renameTargetTake by remember { mutableStateOf<LoopTake?>(null) }
    var renameText by remember { mutableStateOf("") }
    var notesTargetTake by remember { mutableStateOf<LoopTake?>(null) }
    var notesText by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = if (isDark) Color(0xFF0F172A) else MaterialTheme.colorScheme.surface
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
                        color = activeAccentColor
                    )
                    Text(
                        text = "${track.takes.size} of ${com.example.audio.LoopStationEngine.MAX_TAKES_PER_TRACK} takes stored",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp,
                            color = if (isDark) Color(0xFF94A3B8) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close takes",
                        tint = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
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
                            Column(modifier = Modifier.padding(12.dp)) {
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

                                    // Take name & notes summary
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = take.name,
                                                style = MaterialTheme.typography.titleSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp
                                                ),
                                                color = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
                                            )
                                            IconButton(
                                                onClick = {
                                                    renameTargetTake = take
                                                    renameText = take.name
                                                },
                                                modifier = Modifier.size(20.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Edit,
                                                    contentDescription = "Rename take",
                                                    tint = activeAccentColor,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                            }
                                        }

                                        if (take.notes.isNotBlank()) {
                                            Text(
                                                text = take.notes,
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    fontSize = 11.sp,
                                                    color = if (isDark) Color(0xFF94A3B8) else MaterialTheme.colorScheme.onSurfaceVariant
                                                ),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }

                                    // Radio button to select as active
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

                                Spacer(modifier = Modifier.height(6.dp))

                                // Actions row: Notes, Share, Delete
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TextButton(
                                        onClick = {
                                            notesTargetTake = take
                                            notesText = take.notes
                                        }
                                    ) {
                                        Text(
                                            text = if (take.notes.isBlank()) "+ Add Notes" else "Edit Notes",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp)
                                        )
                                    }

                                    IconButton(
                                        onClick = { onShare(take) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Share,
                                            contentDescription = "Share take",
                                            tint = activeAccentColor,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = { onDelete(take.id) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete take",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(16.dp)
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

    // Rename dialog
    if (renameTargetTake != null) {
        AlertDialog(
            onDismissRequest = { renameTargetTake = null },
            title = { Text("Rename Take", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    singleLine = true,
                    label = { Text("Take Name") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        renameTargetTake?.let { onRename(it.id, renameText) }
                        renameTargetTake = null
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { renameTargetTake = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Notes dialog
    if (notesTargetTake != null) {
        AlertDialog(
            onDismissRequest = { notesTargetTake = null },
            title = { Text("Take Notes", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    maxLines = 4,
                    label = { Text("Musical notes / chords used") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        notesTargetTake?.let { onUpdateNotes(it.id, notesText) }
                        notesTargetTake = null
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { notesTargetTake = null }) {
                    Text("Cancel")
                }
            }
        )
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
