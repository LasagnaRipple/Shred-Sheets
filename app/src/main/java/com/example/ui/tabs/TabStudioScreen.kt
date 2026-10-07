package com.example.ui.tabs

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
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
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
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
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import com.example.audio.TabAudioEngine
import com.example.model.GuitarTabMapper
import com.example.model.TabGridResolution
import com.example.model.TabNote
import com.example.model.TabStringDef
import com.example.ui.theme.LocalIsDarkTheme
import com.example.ui.theme.ShredCardBorder
import com.example.ui.theme.ShredCardSurface
import com.example.ui.theme.ShredMutedText
import com.example.ui.theme.ShredPrimaryText
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabStudioScreen(
    tabEngine: TabAudioEngine,
    hasMicPermission: Boolean,
    onRequestMicPermission: () -> Unit,
    onThemeToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val isDark = LocalIsDarkTheme.current
    val scope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current

    val vibrator = remember(context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    val triggerClickHaptic = {
        if (vibrator != null && vibrator.hasVibrator()) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(15L, VibrationEffect.DEFAULT_AMPLITUDE))
                }
            } catch (_: Exception) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
        } else {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    val triggerLongPressHaptic = {
        if (vibrator != null && vibrator.hasVibrator()) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(50L, VibrationEffect.DEFAULT_AMPLITUDE))
                }
            } catch (_: Exception) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            }
        } else {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    val triggerRejectHaptic = {
        if (vibrator != null && vibrator.hasVibrator()) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 35, 40, 35), intArrayOf(0, 180, 0, 180), -1))
                }
            } catch (_: Exception) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
        } else {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    val activeAccent = MaterialTheme.colorScheme.primary
    val backgroundColor = MaterialTheme.colorScheme.background
    val cardBg = if (isDark) ShredCardSurface else MaterialTheme.colorScheme.surface
    val cardBorder = if (isDark) ShredCardBorder else MaterialTheme.colorScheme.outlineVariant
    val primaryText = if (isDark) ShredPrimaryText else MaterialTheme.colorScheme.onSurface
    val mutedText = if (isDark) ShredMutedText else MaterialTheme.colorScheme.onSurfaceVariant

    // Engine State
    val riffTitle by tabEngine.riffTitle.collectAsState()
    val bpm by tabEngine.bpm.collectAsState()
    val resolution by tabEngine.resolution.collectAsState()
    val totalBars by tabEngine.totalBars.collectAsState()
    val notes by tabEngine.notes.collectAsState()
    val selectedNoteId by tabEngine.selectedNoteId.collectAsState()
    val multiSelectedNoteIds by tabEngine.multiSelectedNoteIds.collectAsState()
    val isPlaying by tabEngine.isPlaying.collectAsState()
    val isRecording by tabEngine.isRecording.collectAsState()
    val isCountingIn by tabEngine.isCountingIn.collectAsState()
    val countInBeat by tabEngine.countInBeat.collectAsState()
    val currentPlaybackStep by tabEngine.currentPlaybackStep.collectAsState()
    val isLooping by tabEngine.isLooping.collectAsState()
    val isClickEnabled by tabEngine.isClickEnabled.collectAsState()
    val isCountInEnabled by tabEngine.isCountInEnabled.collectAsState()
    val liveDetectedNote by tabEngine.liveDetectedNote.collectAsState()
    val canUndo by tabEngine.canUndo.collectAsState()
    val isEditorTrayOpen by tabEngine.isEditorTrayOpen.collectAsState()
    val activeStringIndex by tabEngine.activeStringIndex.collectAsState()
    val activeStepIndex by tabEngine.activeStepIndex.collectAsState()
    val activeFret by tabEngine.activeFret.collectAsState()

    // Determine high-contrast text color on activeAccent
    val isLightAccent = remember(activeAccent) {
        (0.299 * activeAccent.red + 0.587 * activeAccent.green + 0.114 * activeAccent.blue) > 0.45
    }
    val onAccentText = if (isLightAccent) Color(0xFF0F172A) else Color.White

    // Modals
    var showInfoDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showClearDialog by remember { mutableStateOf(false) }

    val noteOnActiveSlot = remember(notes, activeStringIndex, activeStepIndex) {
        notes.find { it.stringIndex == activeStringIndex && it.stepIndex == activeStepIndex }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ─────────────────────────────────────────────────────────────────────────────
        // 1. Header: "Tab studio", theme toggle, quick action icons
        // ─────────────────────────────────────────────────────────────────────────────
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
                        triggerClickHaptic()
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
                        contentDescription = "Tab studio title. Tap to cycle accent color theme."
                    }
                    .testTag("header_tab_studio_title"),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = "Tab studio",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 32.sp,
                        lineHeight = 36.sp,
                        letterSpacing = (-0.5).sp
                    ),
                    color = activeAccent,
                    modifier = Modifier.scale(titleScale.value)
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Undo button
                IconButton(
                    onClick = {
                        triggerClickHaptic()
                        tabEngine.undo()
                    },
                    enabled = canUndo,
                    modifier = Modifier.size(44.dp).testTag("tab_btn_undo_header")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Undo,
                        contentDescription = "Undo Tab Edit",
                        tint = if (canUndo) activeAccent else mutedText.copy(alpha = 0.35f),
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Share / Export button
                IconButton(
                    onClick = {
                        triggerClickHaptic()
                        showExportDialog = true
                    },
                    modifier = Modifier.size(44.dp).testTag("tab_btn_export")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share Tab as Text",
                        tint = activeAccent,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Guide Info button
                IconButton(
                    onClick = {
                        triggerClickHaptic()
                        showInfoDialog = true
                    },
                    modifier = Modifier.size(44.dp).testTag("tab_btn_info")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = "Tab Studio Guide",
                        tint = activeAccent,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // ─────────────────────────────────────────────────────────────────────────────
        // 2. Transport & Recording Controls Card
        // ─────────────────────────────────────────────────────────────────────────────
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = cardBg),
            border = BorderStroke(1.dp, cardBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                // Top row: Big Play, Big Record, and status badges (adaptive weights prevent button overlap)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Play / Pause Button
                    Button(
                        onClick = {
                            triggerClickHaptic()
                            tabEngine.togglePlayback()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isPlaying) activeAccent else activeAccent.copy(alpha = 0.15f),
                            contentColor = if (isPlaying) MaterialTheme.colorScheme.onPrimary else activeAccent
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                        modifier = Modifier
                            .weight(1.1f)
                            .height(44.dp)
                            .testTag("tab_btn_play")
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Stop" else "Play",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isPlaying) "STOP" else "PLAY",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            maxLines = 1
                        )
                    }

                    // Record Button (Mic transcription)
                    val recBg = if (isRecording || isCountingIn) Color(0xFFEF4444) else Color(0xFFEF4444).copy(alpha = 0.15f)
                    val recText = if (isRecording || isCountingIn) Color.White else Color(0xFFEF4444)

                    val infiniteTransition = rememberInfiniteTransition(label = "recPulse")
                    val pulseScale by infiniteTransition.animateFloat(
                        initialValue = 1f,
                        targetValue = 1.08f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(500),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "recScale"
                    )

                    Button(
                        onClick = {
                            triggerClickHaptic()
                            if (!hasMicPermission) {
                                onRequestMicPermission()
                            } else {
                                tabEngine.toggleRecording()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = recBg,
                            contentColor = recText
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                        modifier = Modifier
                            .weight(1.2f)
                            .height(44.dp)
                            .scale(if (isRecording) pulseScale else 1f)
                            .testTag("tab_btn_record")
                    ) {
                        Icon(
                            imageVector = if (isRecording || isCountingIn) Icons.Default.Stop else Icons.Default.FiberManualRecord,
                            contentDescription = if (isRecording) "Stop Recording" else "Record Guitar Riff",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isCountingIn) "COUNT" else if (isRecording) "REC..." else "RECORD",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            maxLines = 1
                        )
                    }

                    // Loop, Click, and Clear controls
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Click Track Toggle
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isClickEnabled) activeAccent.copy(alpha = 0.22f) else cardBg)
                                .border(1.dp, if (isClickEnabled) activeAccent else cardBorder, RoundedCornerShape(10.dp))
                                .clickable {
                                    triggerClickHaptic()
                                    tabEngine.toggleClick()
                                }
                                .testTag("tab_toggle_click"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = "Metronome Click Track",
                                tint = if (isClickEnabled) activeAccent else mutedText,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Loop Toggle
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isLooping) activeAccent.copy(alpha = 0.22f) else cardBg)
                                .border(1.dp, if (isLooping) activeAccent else cardBorder, RoundedCornerShape(10.dp))
                                .clickable {
                                    triggerClickHaptic()
                                    tabEngine.toggleLooping()
                                }
                                .testTag("tab_toggle_loop"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Repeat,
                                contentDescription = "Loop Playback",
                                tint = if (isLooping) activeAccent else mutedText,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Clear Tab Button
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(cardBg)
                                .border(1.dp, cardBorder, RoundedCornerShape(10.dp))
                                .clickable {
                                    triggerClickHaptic()
                                    showClearDialog = true
                                }
                                .testTag("tab_btn_clear"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Clear Tab Sheet",
                                tint = mutedText,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Count-In Banner / Live Recording Feedback
                AnimatedVisibility(
                    visible = isCountingIn || isRecording,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isCountingIn) activeAccent.copy(alpha = 0.18f)
                                else Color(0xFFEF4444).copy(alpha = 0.18f)
                            )
                            .border(
                                1.dp,
                                if (isCountingIn) activeAccent.copy(alpha = 0.4f)
                                else Color(0xFFEF4444).copy(alpha = 0.4f),
                                RoundedCornerShape(10.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        if (isCountingIn) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "COUNT-IN: ",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = activeAccent
                                )
                                Text(
                                    text = "$countInBeat",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 20.sp,
                                    color = activeAccent
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "• Get ready to shred!",
                                    fontSize = 12.sp,
                                    color = mutedText
                                )
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFEF4444))
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "LIVE TRANSCRIBING",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 11.sp,
                                        color = Color(0xFFEF4444),
                                        letterSpacing = 0.5.sp
                                    )
                                }
                                Text(
                                    text = liveDetectedNote ?: "Play into microphone...",
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 12.sp,
                                    color = primaryText,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        // ─────────────────────────────────────────────────────────────────────────────
        // 3. Tempo & Quantization Controls
        // ─────────────────────────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Tempo Card (BPM - + stepper)
            Card(
                modifier = Modifier.weight(1.1f),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = BorderStroke(1.dp, cardBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            triggerClickHaptic()
                            tabEngine.stepBpm(-5)
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Decrease Tempo",
                            tint = activeAccent,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$bpm",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp
                            ),
                            color = primaryText
                        )
                        Text(
                            text = "BPM",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                letterSpacing = 0.5.sp
                            ),
                            color = mutedText
                        )
                    }

                    IconButton(
                        onClick = {
                            triggerClickHaptic()
                            tabEngine.stepBpm(5)
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Increase Tempo",
                            tint = activeAccent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Quantize Grid Selector (1/8 vs 1/16 vs 1/4)
            Card(
                modifier = Modifier.weight(1.3f),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = BorderStroke(1.dp, cardBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf(
                        TabGridResolution.QUARTER,
                        TabGridResolution.EIGHTH,
                        TabGridResolution.SIXTEENTH
                    ).forEach { res ->
                        val isSelected = (resolution == res)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) activeAccent else Color.Transparent)
                                .clickable {
                                    triggerClickHaptic()
                                    tabEngine.setResolution(res)
                                }
                                .testTag("tab_res_${res.shortName}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = res.shortName,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else mutedText
                            )
                        }
                    }
                }
            }
        }

        // ─────────────────────────────────────────────────────────────────────────────
        // 4. Interactive Guitar Tab Sheet
        // ─────────────────────────────────────────────────────────────────────────────
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 4.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = cardBg),
            border = BorderStroke(1.dp, cardBorder)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Tab Header Bar with instructions
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(cardBorder.copy(alpha = 0.35f))
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tab Staff",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = primaryText
                    )
                    Text(
                        text = "Tap string to place/edit fret",
                        fontSize = 11.sp,
                        color = mutedText
                    )
                }

                // Interactive Tab Canvas with Sticky String Headers
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    InteractiveTabCanvas(
                        strings = tabEngine.strings,
                        notes = notes,
                        totalBars = totalBars,
                        resolution = resolution,
                        currentStep = currentPlaybackStep,
                        selectedNoteId = selectedNoteId,
                        multiSelectedNoteIds = multiSelectedNoteIds,
                        isEditorTrayOpen = isEditorTrayOpen,
                        activeStringIndex = activeStringIndex,
                        activeStepIndex = activeStepIndex,
                        activeAccent = activeAccent,
                        isDark = isDark,
                        triggerClickHaptic = triggerClickHaptic,
                        triggerLongPressHaptic = triggerLongPressHaptic,
                        triggerRejectHaptic = triggerRejectHaptic,
                        onSlotTapped = { stringIdx, stepIdx ->
                            triggerClickHaptic()
                            val existing = notes.find { it.stringIndex == stringIdx && it.stepIndex == stepIdx }
                            if (multiSelectedNoteIds.isNotEmpty()) {
                                if (existing != null) {
                                    tabEngine.toggleMultiSelectNote(existing.id)
                                }
                            } else {
                                tabEngine.toggleOrAddNoteAt(stringIdx, stepIdx)
                            }
                        },
                        onSlotLongPressed = { stringIdx, stepIdx ->
                            val existing = notes.find { it.stringIndex == stringIdx && it.stepIndex == stepIdx }
                            if (existing != null) {
                                if (multiSelectedNoteIds.isNotEmpty()) {
                                    tabEngine.toggleMultiSelectNote(existing.id)
                                } else {
                                    tabEngine.enterMultiSelect(existing.id)
                                }
                            }
                        },
                        onNoteMoved = { noteId, newStringIndex, newStepIndex ->
                            tabEngine.moveNote(noteId, newStringIndex, newStepIndex)
                        }
                    )
                }
            }
        }

        // ─────────────────────────────────────────────────────────────────────────────
        // 5. Selected Note Quick Edit Bar (Stays displayed on single delete; handles bulk delete in multi-select)
        // ─────────────────────────────────────────────────────────────────────────────
        AnimatedVisibility(
            visible = isEditorTrayOpen,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            val isMulti = multiSelectedNoteIds.isNotEmpty()
            val stringDef = tabEngine.strings.getOrNull(activeStringIndex)
            val stringLabel = stringDef?.label ?: "D"
            val currentPitchName = remember(activeStringIndex, activeFret) {
                TabNote(stringIndex = activeStringIndex, fret = activeFret, stepIndex = activeStepIndex)
                    .getNoteName(tabEngine.strings)
            }
            val hasNoteOnActiveSlot = noteOnActiveSlot != null

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = BorderStroke(1.5.dp, if (isMulti) Color(0xFFEF4444).copy(alpha = 0.8f) else activeAccent.copy(alpha = 0.6f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    // 1. Top Bar: Note Info on Left ("G String"), Actions on Right
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp),
                            verticalArrangement = Arrangement.Center
                        ) {
                            if (isMulti) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "Multi-Select •",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 14.sp,
                                        color = Color(0xFFEF4444)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(22.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFEF4444)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${multiSelectedNoteIds.size}",
                                            fontWeight = FontWeight.Black,
                                            fontSize = 12.sp,
                                            color = Color.White
                                        )
                                    }
                                    Text(
                                        text = "Notes",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 14.sp,
                                        color = Color(0xFFEF4444)
                                    )
                                }
                            } else {
                                Text(
                                    text = "$stringLabel String",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 16.sp,
                                    color = activeAccent
                                )
                            }
                        }

                        // Actions: Undo, Delete, and Clean Close Button
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Undo in Tab Editor
                            IconButton(
                                onClick = {
                                    triggerClickHaptic()
                                    tabEngine.undo()
                                },
                                enabled = canUndo,
                                modifier = Modifier.size(36.dp).testTag("note_btn_undo")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Undo,
                                    contentDescription = "Undo Note Edit",
                                    tint = if (canUndo) activeAccent else mutedText.copy(alpha = 0.35f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // Delete (single delete or bulk delete)
                            IconButton(
                                onClick = {
                                    triggerClickHaptic()
                                    if (isMulti) {
                                        tabEngine.deleteMultiSelectedNotes()
                                    } else {
                                        tabEngine.deleteSelectedNote()
                                    }
                                },
                                enabled = if (isMulti) multiSelectedNoteIds.isNotEmpty() else hasNoteOnActiveSlot,
                                modifier = Modifier
                                    .size(36.dp)
                                    .then(
                                        if (isMulti) {
                                            Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xFFEF4444).copy(alpha = 0.16f))
                                                .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.45f), RoundedCornerShape(8.dp))
                                        } else Modifier
                                    )
                                    .testTag("note_btn_delete")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = if (isMulti) "Bulk Delete Selected Notes" else "Delete Note",
                                    tint = if ((isMulti && multiSelectedNoteIds.isNotEmpty()) || (!isMulti && hasNoteOnActiveSlot)) {
                                        Color(0xFFEF4444)
                                    } else {
                                        mutedText.copy(alpha = 0.35f)
                                    },
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // Prominent Clean Close Button (Icon only, no text)
                            IconButton(
                                onClick = {
                                    triggerClickHaptic()
                                    if (isMulti) {
                                        tabEngine.clearMultiSelect()
                                    } else {
                                        tabEngine.closeEditorTray()
                                    }
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(activeAccent.copy(alpha = 0.16f))
                                    .border(1.dp, activeAccent.copy(alpha = 0.45f), RoundedCornerShape(8.dp))
                                    .testTag("note_btn_close")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = if (isMulti) "Done" else "Close Note Editor",
                                    tint = activeAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    if (!isMulti) {
                        Spacer(modifier = Modifier.height(10.dp))

                        // 2. Bottom Bar: Fret Stepper (- / +) and Circled Fret Markers (e.g. circled 8)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = {
                                    triggerClickHaptic()
                                    tabEngine.stepSelectedNoteFret(-1)
                                },
                                modifier = Modifier.size(34.dp).testTag("note_fret_minus")
                            ) {
                                Icon(Icons.Default.Remove, "Fret Down", tint = primaryText, modifier = Modifier.size(18.dp))
                            }

                            Spacer(modifier = Modifier.width(4.dp))

                            val quickFrets = remember(activeFret) {
                                val standard = listOf(0, 3, 5, 7, 12)
                                if (activeFret in standard) standard
                                else (standard + activeFret).sorted()
                            }

                            Row(
                                modifier = Modifier.horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                quickFrets.forEach { quickFret ->
                                    val isCur = (activeFret == quickFret)
                                    // Fret marker: active fret is rendered with the 'circled fret marker'
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(if (isCur) activeAccent else cardBorder.copy(alpha = 0.35f))
                                            .border(
                                                width = if (isCur) 2.dp else 1.dp,
                                                color = if (isCur) activeAccent else cardBorder.copy(alpha = 0.6f),
                                                shape = CircleShape
                                            )
                                            .clickable {
                                                triggerClickHaptic()
                                                tabEngine.updateSelectedNoteFret(quickFret)
                                            }
                                            .testTag("fret_chip_$quickFret"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "$quickFret",
                                            fontWeight = FontWeight.Black,
                                            fontSize = 13.sp,
                                            color = if (isCur) onAccentText else primaryText
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(4.dp))

                            IconButton(
                                onClick = {
                                    triggerClickHaptic()
                                    tabEngine.stepSelectedNoteFret(1)
                                },
                                modifier = Modifier.size(34.dp).testTag("note_fret_plus")
                            ) {
                                Icon(Icons.Default.Add, "Fret Up", tint = primaryText, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // Dialogs & Modals
    // ─────────────────────────────────────────────────────────────────────────────

    // 1. Share / Export ASCII Tab Dialog
    if (showExportDialog) {
        val asciiTab = remember(riffTitle, bpm, totalBars, resolution, notes) {
            GuitarTabMapper.generateAsciiTab(
                title = riffTitle,
                bpm = bpm,
                totalBars = totalBars,
                resolution = resolution,
                notes = notes,
                strings = tabEngine.strings
            )
        }

        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = {
                Text(
                    text = "Share Guitar Tab",
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp,
                    color = activeAccent
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Standard formatted ASCII tablature ready to copy or share with friends:",
                        fontSize = 13.sp,
                        color = mutedText,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black.copy(alpha = 0.85f))
                            .padding(10.dp)
                            .horizontalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = asciiTab,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = Color(0xFF10B981)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        triggerClickHaptic()
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, asciiTab)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Share Tab via"))
                        showExportDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = activeAccent)
                ) {
                    Icon(Icons.Default.Share, "Share", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Share Tab")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        triggerClickHaptic()
                        clipboardManager.setText(AnnotatedString(asciiTab))
                        Toast.makeText(context, "Copied tab to clipboard!", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Copy to Clipboard", color = activeAccent)
                }
            }
        )
    }

    // 3. Clear Confirmation Dialog
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear All Notes?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to clear this entire tab sheet?", color = mutedText) },
            confirmButton = {
                Button(
                    onClick = {
                        triggerClickHaptic()
                        tabEngine.clearAllNotes()
                        showClearDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Clear All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel", color = mutedText)
                }
            }
        )
    }

    // 4. Info Guide Dialog
    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Info, null, tint = activeAccent, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Tab Studio Guide", fontWeight = FontWeight.Black, color = activeAccent)
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "🎸 Reading Guitar Tab:",
                        fontWeight = FontWeight.Bold,
                        color = primaryText,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "• Top line (e) is high E (thinnest string).\n• Bottom line (E) is low E (thickest string).\n• Numbers show which fret to hold down (0 = open string).",
                        fontSize = 12.sp,
                        color = mutedText
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "🎙️ Real-Time Audio Transcription:",
                        fontWeight = FontWeight.Bold,
                        color = primaryText,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "• Tap RECORD. Play your guitar into your device's mic along with the click track.\n• Single notes automatically transcribe onto the tab staff in real time!",
                        fontSize = 12.sp,
                        color = mutedText
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "⏱️ Note Quantisation:",
                        fontWeight = FontWeight.Bold,
                        color = primaryText,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "• Automatically slots your played notes into clean rhythm subdivisions (1/8th or 1/16th notes) so your riffs stay in perfect timing.",
                        fontSize = 12.sp,
                        color = mutedText
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showInfoDialog = false }) {
                    Text("Got it!", color = activeAccent, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

/**
 * Custom Canvas & Composable hybrid for responsive, ultra-smooth Guitar Tab notation.
 */
@Composable
fun InteractiveTabCanvas(
    strings: List<TabStringDef>,
    notes: List<TabNote>,
    totalBars: Int,
    resolution: TabGridResolution,
    currentStep: Int,
    selectedNoteId: String?,
    multiSelectedNoteIds: Set<String> = emptySet(),
    isEditorTrayOpen: Boolean = false,
    activeStringIndex: Int = 3,
    activeStepIndex: Int = 0,
    activeAccent: Color,
    isDark: Boolean,
    triggerClickHaptic: () -> Unit = {},
    triggerLongPressHaptic: () -> Unit = {},
    triggerRejectHaptic: () -> Unit = {},
    onSlotTapped: (stringIndex: Int, stepIndex: Int) -> Unit,
    onSlotLongPressed: (stringIndex: Int, stepIndex: Int) -> Unit = { _, _ -> },
    onNoteMoved: (noteId: String, newStringIndex: Int, newStepIndex: Int) -> Unit = { _, _, _ -> }
) {
    val totalSteps = totalBars * resolution.stepsPerBar
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    val currentOnSlotTapped by rememberUpdatedState(onSlotTapped)
    val currentOnSlotLongPressed by rememberUpdatedState(onSlotLongPressed)
    val currentOnNoteMoved by rememberUpdatedState(onNoteMoved)
    val currentTriggerClickHaptic by rememberUpdatedState(triggerClickHaptic)
    val currentTriggerLongPressHaptic by rememberUpdatedState(triggerLongPressHaptic)
    val currentTriggerRejectHaptic by rememberUpdatedState(triggerRejectHaptic)

    var draggingNote by remember { mutableStateOf<TabNote?>(null) }
    var dragPosition by remember { mutableStateOf<Offset?>(null) }
    var hoveredSlot by remember { mutableStateOf<Pair<Int, Int>?>(null) }

    // Auto-scroll staff to follow playback/recording playhead
    LaunchedEffect(currentStep) {
        if (currentStep >= 0 && totalSteps > 0) {
            val targetScroll = (currentStep * 44 - 100).coerceAtLeast(0)
            scrollState.animateScrollTo(targetScroll)
        }
    }

    val stringCount = strings.size
    val staffLineColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF334155)
    val barDividerColor = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8)
    val noteBg = if (isDark) Color(0xFF1E293B) else Color.White
    val isLightAccent = remember(activeAccent) {
        (0.299 * activeAccent.red + 0.587 * activeAccent.green + 0.114 * activeAccent.blue) > 0.45
    }
    val onAccentText = if (isLightAccent) Color(0xFF0F172A) else Color.White

    Row(modifier = Modifier.fillMaxSize()) {
        // Sticky String Labels on Left
        Column(
            modifier = Modifier
                .width(36.dp)
                .fillMaxSize()
                .background(if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0))
                .border(
                    BorderStroke(
                        1.dp,
                        if (isDark) Color(0xFF334155) else Color(0xFFCBD5E1)
                    )
                ),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Measure bar dummy header height
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "TAB",
                    fontWeight = FontWeight.Black,
                    fontSize = 10.sp,
                    color = activeAccent
                )
            }

            // String names
            strings.forEachIndexed { i, str ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = str.label,
                        fontWeight = if (i >= 3) FontWeight.Black else FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (isDark) Color(0xFFF1F5F9) else Color(0xFF0F172A)
                    )
                }
            }
        }

        // Horizontal Scrolling Staff
        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
                .horizontalScroll(scrollState)
        ) {
            val columnWidth = 44.dp
            val totalWidth = columnWidth * totalSteps
            val density = LocalDensity.current
            val totalHeightPx = with(density) { maxHeight.toPx() }
            val topMarginPx = with(density) { 24.dp.toPx() }
            val availableHeightPx = (totalHeightPx - topMarginPx).coerceAtLeast(1f)
            val rowHeightPx = availableHeightPx / stringCount
            val columnWidthPx = with(density) { columnWidth.toPx() }

            Box(
                modifier = Modifier
                    .width(totalWidth)
                    .fillMaxSize()
            ) {
                // Background Canvas: Staff lines, bar lines, beat subdivisions
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val topMargin = 24.dp.toPx()
                    val availableH = h - topMargin
                    val lineSpacing = availableH / stringCount

                    // 1. Draw String Lines (substantially increased thickness: High e at 2.5dp tapering to Low E at 6.5dp)
                    for (i in 0 until stringCount) {
                        val y = topMargin + lineSpacing * (i + 0.5f)
                        val fractionFromTop = i.toFloat() / (stringCount - 1).coerceAtLeast(1)
                        val strokeWidth = (2.5f + (fractionFromTop * 4.0f)).dp.toPx()
                        val stringAlpha = 0.85f + (fractionFromTop * 0.15f)

                        drawLine(
                            color = staffLineColor.copy(alpha = stringAlpha),
                            start = Offset(0f, y),
                            end = Offset(w, y),
                            strokeWidth = strokeWidth,
                            cap = StrokeCap.Round
                        )
                    }

                    // 2. Draw Bar lines and Beat lines
                    val stepWidthCanvasPx = columnWidth.toPx()
                    for (step in 0..totalSteps) {
                        val x = step * stepWidthCanvasPx
                        if (step % resolution.stepsPerBar == 0) {
                            // Heavy Bar line
                            drawLine(
                                color = barDividerColor,
                                start = Offset(x, topMargin),
                                end = Offset(x, h),
                                strokeWidth = 2.dp.toPx()
                            )
                        } else if (step % resolution.stepsPerBeat == 0) {
                            // Light Beat divider
                            drawLine(
                                color = barDividerColor.copy(alpha = 0.35f),
                                start = Offset(x, topMargin),
                                end = Offset(x, h),
                                strokeWidth = 1.dp.toPx()
                            )
                        }
                    }

                    // 3. Playhead cursor (if playing or recording)
                    if (currentStep in 0 until totalSteps) {
                        val playheadX = (currentStep + 0.5f) * stepWidthCanvasPx
                        drawLine(
                            color = activeAccent,
                            start = Offset(playheadX, 0f),
                            end = Offset(playheadX, h),
                            strokeWidth = 3.dp.toPx()
                        )
                        drawCircle(
                            color = activeAccent,
                            radius = 5.dp.toPx(),
                            center = Offset(playheadX, topMargin / 2)
                        )
                    }
                }

                // Bar Labels Row along top
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp)
                ) {
                    for (bar in 1..totalBars) {
                        Box(
                            modifier = Modifier
                                .width(columnWidth * resolution.stepsPerBar)
                                .height(24.dp)
                                .padding(start = 4.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = "BAR $bar",
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = barDividerColor,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }

                // Tappable Note Grid Slots & Rendered Note Badges
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 24.dp)
                ) {
                    for (stringIdx in 0 until stringCount) {
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            for (stepIdx in 0 until totalSteps) {
                                val noteOnSlot = notes.find { it.stringIndex == stringIdx && it.stepIndex == stepIdx }
                                val isMultiSelected = noteOnSlot != null && multiSelectedNoteIds.contains(noteOnSlot.id)
                                val isSlotActive = isEditorTrayOpen && multiSelectedNoteIds.isEmpty() &&
                                    (noteOnSlot != null && noteOnSlot.id == selectedNoteId ||
                                     (stringIdx == activeStringIndex && stepIdx == activeStepIndex))
                                val isSelected = isMultiSelected || (noteOnSlot != null && isSlotActive)
                                val isSlotFocused = isEditorTrayOpen && multiSelectedNoteIds.isEmpty() &&
                                    stringIdx == activeStringIndex && stepIdx == activeStepIndex && noteOnSlot == null

                                val isBeingDragged = draggingNote != null && draggingNote?.id == noteOnSlot?.id
                                val isHoveredTarget = draggingNote != null && hoveredSlot == Pair(stringIdx, stepIdx)
                                val isHoveredOccupied = isHoveredTarget && noteOnSlot != null && noteOnSlot.id != draggingNote?.id
                                val isHoveredValid = isHoveredTarget && !isHoveredOccupied

                                Box(
                                    modifier = Modifier
                                        .width(columnWidth)
                                        .fillMaxSize()
                                        .then(
                                            if (noteOnSlot == null) {
                                                Modifier.pointerInput(stringIdx, stepIdx) {
                                                    detectTapGestures(
                                                        onTap = {
                                                            currentOnSlotTapped(stringIdx, stepIdx)
                                                        }
                                                    )
                                                }
                                            } else {
                                                Modifier.pointerInput(noteOnSlot.id, multiSelectedNoteIds.isNotEmpty()) {
                                                    if (multiSelectedNoteIds.isNotEmpty()) {
                                                        detectTapGestures(
                                                            onTap = { currentOnSlotTapped(stringIdx, stepIdx) }
                                                        )
                                                    } else {
                                                        awaitEachGesture {
                                                            val down = awaitFirstDown(requireUnconsumed = false)
                                                            val downTime = System.currentTimeMillis()
                                                            val downPos = down.position
                                                            var isDraggingThisNote = false
                                                            var longHoldTriggered = false

                                                            // 2.0-second timer for bulk delete
                                                            val timerJob = coroutineScope.launch {
                                                                delay(2000L)
                                                                if (!isDraggingThisNote) {
                                                                    longHoldTriggered = true
                                                                    currentTriggerLongPressHaptic()
                                                                    currentOnSlotLongPressed(stringIdx, stepIdx)
                                                                }
                                                            }

                                                            try {
                                                                while (true) {
                                                                    val event = awaitPointerEvent()
                                                                    val change = event.changes.firstOrNull { it.id == down.id } ?: break

                                                                    if (!change.pressed) {
                                                                        change.consume()
                                                                        break
                                                                    }

                                                                    val dx = change.position.x - downPos.x
                                                                    val dy = change.position.y - downPos.y
                                                                    val distanceMoved = kotlin.math.hypot(dx, dy)
                                                                    if (!isDraggingThisNote && distanceMoved > 10.dp.toPx() && !longHoldTriggered) {
                                                                        // User started dragging! Cancel bulk delete timer
                                                                        timerJob.cancel()
                                                                        isDraggingThisNote = true
                                                                        currentTriggerClickHaptic()
                                                                        draggingNote = noteOnSlot
                                                                    }

                                                                    if (isDraggingThisNote) {
                                                                        change.consume()
                                                                        val slotTopLeftX = stepIdx * columnWidthPx
                                                                        val slotTopLeftY = topMarginPx + stringIdx * rowHeightPx
                                                                        val currentCanvasX = slotTopLeftX + change.position.x
                                                                        val currentCanvasY = slotTopLeftY + change.position.y
                                                                        dragPosition = Offset(currentCanvasX, currentCanvasY)

                                                                        val hoveredStep = (currentCanvasX / columnWidthPx).toInt().coerceIn(0, totalSteps - 1)
                                                                        val hoveredString = ((currentCanvasY - topMarginPx) / rowHeightPx).toInt().coerceIn(0, stringCount - 1)
                                                                        hoveredSlot = Pair(hoveredString, hoveredStep)
                                                                    }
                                                                }
                                                            } finally {
                                                                timerJob.cancel()
                                                            }

                                                            if (isDraggingThisNote) {
                                                                val target = hoveredSlot
                                                                if (target != null) {
                                                                    val (targetString, targetStep) = target
                                                                    val isSameSlot = (targetString == noteOnSlot.stringIndex && targetStep == noteOnSlot.stepIndex)
                                                                    val isOccupied = notes.any { it.id != noteOnSlot.id && it.stringIndex == targetString && it.stepIndex == targetStep }

                                                                    if (!isSameSlot && !isOccupied) {
                                                                        currentTriggerClickHaptic()
                                                                        currentOnNoteMoved(noteOnSlot.id, targetString, targetStep)
                                                                    } else if (isOccupied) {
                                                                        currentTriggerRejectHaptic()
                                                                    }
                                                                }
                                                                draggingNote = null
                                                                dragPosition = null
                                                                hoveredSlot = null
                                                            } else if (!longHoldTriggered) {
                                                                val duration = System.currentTimeMillis() - downTime
                                                                if (duration < 600L) {
                                                                    currentOnSlotTapped(stringIdx, stepIdx)
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    // Visual indicators for drop targets
                                    if (isHoveredValid && noteOnSlot == null) {
                                        Box(
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(CircleShape)
                                                .background(activeAccent.copy(alpha = 0.22f))
                                                .border(2.dp, activeAccent, CircleShape)
                                        )
                                    } else if (isHoveredOccupied) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .border(2.dp, Color(0xFFEF4444).copy(alpha = 0.85f), CircleShape)
                                        )
                                    }

                                    if (noteOnSlot != null) {
                                        if (isBeingDragged) {
                                            // Ghost placeholder at original position
                                            Box(
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .clip(CircleShape)
                                                    .background(noteBg.copy(alpha = 0.35f))
                                                    .border(1.5.dp, if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "${noteOnSlot.fret}",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp,
                                                    color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                                                )
                                            }
                                        } else {
                                            // Normal Note Badge
                                            val badgeScale = if (isSelected) 1.15f else 1f
                                            Box(
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .scale(badgeScale)
                                                    .clip(CircleShape)
                                                    .background(
                                                        if (isMultiSelected) Color(0xFFEF4444)
                                                        else if (isSlotActive) activeAccent
                                                        else noteBg
                                                    )
                                                    .border(
                                                        width = if (isSelected) 2.dp else 1.5.dp,
                                                        color = if (isMultiSelected) Color(0xFFEF4444)
                                                        else if (isSlotActive) activeAccent
                                                        else if (isDark) Color(0xFF94A3B8)
                                                        else Color(0xFF334155),
                                                        shape = CircleShape
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "${noteOnSlot.fret}",
                                                    fontWeight = FontWeight.Black,
                                                    fontSize = 14.sp,
                                                    color = if (isMultiSelected) Color.White
                                                    else if (isSlotActive) onAccentText
                                                    else if (isDark) Color.White else Color(0xFF0F172A)
                                                )
                                            }
                                        }
                                    } else if (isSlotFocused && !isHoveredValid) {
                                        // Focus indicator on active empty slot
                                        Box(
                                            modifier = Modifier
                                                .size(26.dp)
                                                .border(
                                                    width = 1.5.dp,
                                                    color = activeAccent.copy(alpha = 0.65f),
                                                    shape = CircleShape
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .background(activeAccent.copy(alpha = 0.8f), CircleShape)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Floating dragged marker following finger
                if (draggingNote != null && dragPosition != null) {
                    val isTargetOccupied = hoveredSlot?.let { (hStr, hStep) ->
                        notes.any { it.id != draggingNote!!.id && it.stringIndex == hStr && it.stepIndex == hStep }
                    } ?: false

                    val floatingBadgeSize = 38.dp
                    val halfBadgePx = with(density) { (floatingBadgeSize / 2).toPx() }

                    Box(
                        modifier = Modifier
                            .offset {
                                IntOffset(
                                    (dragPosition!!.x - halfBadgePx).roundToInt(),
                                    (dragPosition!!.y - halfBadgePx).roundToInt()
                                )
                            }
                            .size(floatingBadgeSize)
                            .shadow(elevation = 12.dp, shape = CircleShape)
                            .clip(CircleShape)
                            .background(
                                if (isTargetOccupied) Color(0xFFEF4444).copy(alpha = 0.95f)
                                else activeAccent
                            )
                            .border(
                                width = 2.5.dp,
                                color = Color.White,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${draggingNote!!.fret}",
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
