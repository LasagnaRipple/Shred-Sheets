package com.example.ui.chords

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.ChordItem
import com.example.model.ChordRepository
import com.example.model.InstrumentType
import com.example.ui.theme.LocalIsDarkTheme
import com.example.ui.theme.ShredCardBorder
import com.example.ui.theme.ShredCardSurface
import com.example.ui.theme.ShredFretGrid
import com.example.ui.theme.ShredMuteCoral
import com.example.ui.theme.ShredMutedText
import com.example.ui.theme.ShredNutColor
import com.example.ui.theme.ShredOpenNeonGreen
import com.example.ui.theme.ShredPrimaryText
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ChordLibraryScreen(
    instrumentType: InstrumentType,
    onStrumChord: (ChordItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val allChords = remember(instrumentType) {
        ChordRepository.getChordsForInstrument(instrumentType)
    }

    var selectedCategory by remember { mutableStateOf("All") }
    var selectedChord by remember(allChords) { mutableStateOf(allChords.firstOrNull()) }

    val categories = listOf("All", "Major", "Minor", "7th")
    val filteredChords = remember(allChords, selectedCategory) {
        when (selectedCategory) {
            "All" -> allChords
            "7th" -> allChords.filter { it.category == "7th" || it.name.contains("7") }
            "Minor" -> allChords.filter { it.category == "Minor" }
            "Major" -> allChords.filter { it.category == "Major" }
            else -> allChords.filter { it.category.equals(selectedCategory, ignoreCase = true) }
        }
    }
    val haptic = LocalHapticFeedback.current
    var showKeyDialog by remember { mutableStateOf(false) }
    var showEnhancedView by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    var isStrumming by remember { mutableStateOf(false) }
    var strumJob by remember { mutableStateOf<Job?>(null) }
    val speakerScale = remember { Animatable(1.0f) }
    val activeAccent = MaterialTheme.colorScheme.primary
    val onActiveAccent = MaterialTheme.colorScheme.onPrimary
    val isDark = LocalIsDarkTheme.current
    val cardSurface = if (isDark) ShredCardSurface else MaterialTheme.colorScheme.surface
    val cardBorder = if (isDark) ShredCardBorder else MaterialTheme.colorScheme.outlineVariant
    val primaryTextColor = if (isDark) ShredPrimaryText else MaterialTheme.colorScheme.onSurface
    val mutedTextColor = if (isDark) ShredMutedText else MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Header: "Chord library" in sentence case, brand accent, info icon on right
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Chord library",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 32.sp,
                        lineHeight = 36.sp,
                        letterSpacing = (-0.5).sp
                    ),
                    color = activeAccent
                )
            }

            // Quick Info button in screen header
            IconButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    showKeyDialog = true
                },
                modifier = Modifier.testTag("header_chart_key_button")
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = "Diagram Key",
                    tint = activeAccent
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 2. Filter pills (All / Major / Minor / Power) - 1.5dp border, fully rounded
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { category ->
                val isSelected = selectedCategory == category
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(
                            if (isSelected) activeAccent else cardSurface
                        )
                        .border(
                            width = 1.5.dp,
                            color = if (isSelected) activeAccent else cardBorder,
                            shape = RoundedCornerShape(999.dp)
                        )
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            selectedCategory = category
                        }
                        .padding(horizontal = 16.dp, vertical = 7.dp)
                        .testTag("filter_chip_$category"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = category,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        ),
                        color = if (isSelected) onActiveAccent else primaryTextColor
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 3. Chord diagram card — the main enhancement:
        selectedChord?.let { chord ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(BorderStroke(1.5.dp, cardBorder), RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(
                    containerColor = cardSurface
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 10.dp)
                        ) {
                            val chordDisplayName = chord.fullName.ifEmpty { chord.name }
                            Text(
                                text = chordDisplayName,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 22.sp
                                ),
                                color = primaryTextColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Speaker / play button: circular outline with ~300ms tap scale pulse
                        Box(
                            modifier = Modifier
                                .scale(speakerScale.value)
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(if (isStrumming) activeAccent else Color.Transparent)
                                .border(1.5.dp, activeAccent, CircleShape)
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onStrumChord(chord)
                                    strumJob?.cancel()
                                    strumJob = coroutineScope.launch {
                                        launch {
                                            speakerScale.animateTo(
                                                targetValue = 1.15f,
                                                animationSpec = tween(150, easing = FastOutSlowInEasing)
                                            )
                                            speakerScale.animateTo(
                                                targetValue = 1.0f,
                                                animationSpec = tween(150, easing = FastOutSlowInEasing)
                                            )
                                        }
                                        isStrumming = true
                                        delay(1600L)
                                        isStrumming = false
                                    }
                                }
                                .testTag("strum_chord_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Strum Chord",
                                tint = if (isStrumming) onActiveAccent else activeAccent,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Animated Fretboard Diagram with glowing dots & thin lines
                    AnimatedContent(
                        targetState = chord,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(180)) togetherWith fadeOut(animationSpec = tween(120))
                        },
                        label = "chordFretboardTransition"
                    ) { targetChord ->
                        FretboardDiagram(
                            chord = targetChord,
                            primaryColor = activeAccent,
                            onPrimaryColor = onActiveAccent,
                            isEnhanced = showEnhancedView,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(185.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    showEnhancedView = !showEnhancedView
                                }
                                .testTag("fretboard_diagram")
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 4. "Select chord to learn" section header in sentence case, muted color
        Text(
            text = "Select chord to learn",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                letterSpacing = 0.5.sp
            ),
            color = mutedTextColor,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Grid of available chords to tap & select (4 columns matching 4x4 chart layout)
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(filteredChords) { chord ->
                val isSelected = selectedChord?.id == chord.id
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isSelected) activeAccent.copy(alpha = if (isDark) 0.16f else 0.12f) else cardSurface
                        )
                        .border(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) activeAccent else cardBorder,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            selectedChord = chord
                        }
                        .padding(horizontal = 4.dp)
                        .testTag("chord_item_${chord.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = chord.name,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        ),
                        color = if (isSelected) activeAccent else primaryTextColor,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // Info popup dialog explaining the diagram key (O, X, dot)
        if (showKeyDialog) {
            ChordDiagramKeyDialog(
                primaryColor = activeAccent,
                onDismiss = { showKeyDialog = false }
            )
        }
    }
}

/**
 * Renders a crisp vector Fretboard diagram with:
 * - 1px thin muted gray grid lines (#3A3D30)
 * - Thicker, brighter off-white nut anchor (#F2F2EA)
 * - Coral mute indicators 'X' (#D85A30)
 * - Neon green open indicators 'O' (#C8FF3D)
 * - Radial neon glow behind solid brand-yellow finger dots with finger numbers (1..4)
 */
@Composable
fun FretboardDiagram(
    chord: ChordItem,
    primaryColor: Color = MaterialTheme.colorScheme.primary,
    onPrimaryColor: Color = MaterialTheme.colorScheme.onPrimary,
    isEnhanced: Boolean = false,
    modifier: Modifier = Modifier
) {
    val isDark = LocalIsDarkTheme.current
    val nutLineColor = if (isDark) ShredNutColor else MaterialTheme.colorScheme.onSurface
    val fretLineColor = if (isDark) ShredFretGrid else Color(0xFFCBD5E1)
    val fretTextColor = if (isDark) "#8A8D78" else "#64748B"

    Canvas(modifier = modifier) {
        val numStrings = chord.positions.size
        val numFrets = 4 // standard 4 fret window

        // Margins
        val leftMargin = (if (isEnhanced) 42.dp else 36.dp).toPx()
        val rightMargin = (if (isEnhanced) 26.dp else 36.dp).toPx()
        val topMargin = 30.dp.toPx()
        val bottomMargin = 20.dp.toPx()

        val fretboardWidth = size.width - leftMargin - rightMargin
        val fretboardHeight = size.height - topMargin - bottomMargin

        val stringSpacing = fretboardWidth / (numStrings - 1).coerceAtLeast(1)
        val fretSpacing = fretboardHeight / numFrets

        // Nut (top thick bar, anchor line) - bright off-white in dark, high contrast in light
        drawLine(
            color = nutLineColor,
            start = Offset(leftMargin, topMargin),
            end = Offset(leftMargin + fretboardWidth, topMargin),
            strokeWidth = 5.5.dp.toPx()
        )

        // Fret wires (horizontal) - thin 1px/1dp
        for (fret in 1..numFrets) {
            val y = topMargin + fret * fretSpacing
            drawLine(
                color = fretLineColor,
                start = Offset(leftMargin, y),
                end = Offset(leftMargin + fretboardWidth, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        // Strings (vertical lines) - thin lines receding behind dots
        for (i in 0 until numStrings) {
            val x = leftMargin + i * stringSpacing
            val stringWidth = (1.0f + (numStrings - 1 - i) * 0.25f).dp.toPx()
            drawLine(
                color = fretLineColor.copy(alpha = 0.9f),
                start = Offset(x, topMargin),
                end = Offset(x, topMargin + fretboardHeight),
                strokeWidth = stringWidth
            )
        }

        // In Enhanced mode: draw fret numbers (1 to 4) down the left side
        if (isEnhanced) {
            val fretTextPaint = Paint().apply {
                color = android.graphics.Color.parseColor(fretTextColor)
                textSize = 12.sp.toPx()
                textAlign = Paint.Align.CENTER
                typeface = Typeface.DEFAULT_BOLD
                isAntiAlias = true
            }

            val fretNumberX = leftMargin - 18.dp.toPx()
            val textOffsetY = (fretTextPaint.descent() + fretTextPaint.ascent()) / 2f
            for (fret in 1..numFrets) {
                val fretCenterY = topMargin + (fret - 0.5f) * fretSpacing
                drawContext.canvas.nativeCanvas.drawText(
                    fret.toString(),
                    fretNumberX,
                    fretCenterY - textOffsetY,
                    fretTextPaint
                )
            }
        }

        // Paint for finger numbers inside the dots
        val fingerTextPaint = Paint().apply {
            color = onPrimaryColor.toArgb()
            textSize = 11.5.sp.toPx()
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }

        // Finger dots and Mute/Open indicators
        chord.positions.forEachIndexed { index, pos ->
            val stringX = leftMargin + index * stringSpacing

            when {
                pos.fret == -1 -> {
                    // Muted string 'X' above nut in app's coral (#D85A30)
                    val cy = topMargin - 14.dp.toPx()
                    val d = 5.5.dp.toPx()
                    drawLine(ShredMuteCoral, Offset(stringX - d, cy - d), Offset(stringX + d, cy + d), strokeWidth = 2.5.dp.toPx())
                    drawLine(ShredMuteCoral, Offset(stringX + d, cy - d), Offset(stringX - d, cy + d), strokeWidth = 2.5.dp.toPx())
                }
                pos.fret == 0 -> {
                    // Open string 'O' above nut in app's neon green (#C8FF3D)
                    val cy = topMargin - 14.dp.toPx()
                    drawCircle(ShredOpenNeonGreen, radius = 5.5.dp.toPx(), center = Offset(stringX, cy), style = Stroke(2.2.dp.toPx()))
                }
                pos.fret in 1..numFrets -> {
                    // Finger dot inside fret
                    val dotY = topMargin + (pos.fret - 0.5f) * fretSpacing
                    val dotRadius = 11.5.dp.toPx()

                    // Radial glow behind dot (matching tuning circle glow)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                primaryColor.copy(alpha = 0.55f),
                                primaryColor.copy(alpha = 0.15f),
                                Color.Transparent
                            ),
                            center = Offset(stringX, dotY),
                            radius = dotRadius * 2.2f
                        ),
                        radius = dotRadius * 2.2f,
                        center = Offset(stringX, dotY)
                    )

                    // Solid brand yellow-green dot
                    drawCircle(
                        color = primaryColor,
                        radius = dotRadius,
                        center = Offset(stringX, dotY)
                    )
                    // Crisp dark border for contrast
                    drawCircle(
                        color = Color(0xFF151A12),
                        radius = dotRadius,
                        center = Offset(stringX, dotY),
                        style = Stroke(1.5.dp.toPx())
                    )

                    // Draw finger number inside dot (1 = Index, 2 = Middle, 3 = Ring, 4 = Pinky)
                    if (pos.finger > 0) {
                        val textOffsetY = (fingerTextPaint.descent() + fingerTextPaint.ascent()) / 2f
                        drawContext.canvas.nativeCanvas.drawText(
                            pos.finger.toString(),
                            stringX,
                            dotY - textOffsetY,
                            fingerTextPaint
                        )
                    }
                }
            }
        }
    }
}

/**
 * Info popup [ i ] displaying the Key / Legend for reading chord diagrams.
 * Uses app tokens: Coral for X, Neon Green for O, brand yellow glow for dots.
 */
@Composable
fun ChordDiagramKeyDialog(
    primaryColor: Color = MaterialTheme.colorScheme.primary,
    onDismiss: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    val dialogSurface = if (isDark) ShredCardSurface else MaterialTheme.colorScheme.surface
    val dialogBorder = if (isDark) ShredCardBorder else MaterialTheme.colorScheme.outlineVariant
    val primaryText = if (isDark) ShredPrimaryText else MaterialTheme.colorScheme.onSurface
    val mutedText = if (isDark) ShredMutedText else MaterialTheme.colorScheme.onSurfaceVariant

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
                .clip(RoundedCornerShape(20.dp))
                .border(1.5.dp, dialogBorder, RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(
                containerColor = dialogSurface
            )
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
                            tint = primaryColor,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "How to read chords",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 19.sp
                            ),
                            color = primaryText
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_key_dialog_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = mutedText
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // The Key Items using app tokens
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Item 1: O = Open string in ShredOpenNeonGreen
                    DiagramKeyRow(
                        symbolSlot = {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(ShredOpenNeonGreen.copy(alpha = 0.14f))
                                    .border(1.dp, ShredOpenNeonGreen.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Canvas(modifier = Modifier.size(20.dp)) {
                                    drawCircle(
                                        color = ShredOpenNeonGreen,
                                        radius = 6.dp.toPx(),
                                        style = Stroke(2.2.dp.toPx())
                                    )
                                }
                            }
                        },
                        codeSymbol = "O",
                        symbolTitle = "Open String",
                        description = "Play this string 'open' with no frets pressed"
                    )

                    // Item 2: X = Strings that you don't play in ShredMuteCoral
                    DiagramKeyRow(
                        symbolSlot = {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(ShredMuteCoral.copy(alpha = 0.14f))
                                    .border(1.dp, ShredMuteCoral.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Canvas(modifier = Modifier.size(20.dp)) {
                                    val d = 6.dp.toPx()
                                    val center = Offset(size.width / 2f, size.height / 2f)
                                    drawLine(
                                        color = ShredMuteCoral,
                                        start = Offset(center.x - d, center.y - d),
                                        end = Offset(center.x + d, center.y + d),
                                        strokeWidth = 2.4.dp.toPx()
                                    )
                                    drawLine(
                                        color = ShredMuteCoral,
                                        start = Offset(center.x + d, center.y - d),
                                        end = Offset(center.x - d, center.y + d),
                                        strokeWidth = 2.4.dp.toPx()
                                    )
                                }
                            }
                        },
                        codeSymbol = "X",
                        symbolTitle = "Don't Play (Mute)",
                        description = "Mute or skip this string when strumming"
                    )

                    // Item 3: Dot = Fingers go here (with glow & finger numbers)
                    DiagramKeyRow(
                        symbolSlot = {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(primaryColor.copy(alpha = 0.15f))
                                    .border(1.dp, primaryColor.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Canvas(modifier = Modifier.size(22.dp)) {
                                    val center = Offset(size.width / 2f, size.height / 2f)
                                    drawCircle(
                                        color = primaryColor,
                                        radius = 9.dp.toPx(),
                                        center = center
                                    )
                                    drawCircle(
                                        color = Color(0xFF151A12),
                                        radius = 9.dp.toPx(),
                                        center = center,
                                        style = Stroke(1.5.dp.toPx())
                                    )
                                    val p = Paint().apply {
                                        color = android.graphics.Color.parseColor("#121212")
                                        textSize = 10.sp.toPx()
                                        textAlign = Paint.Align.CENTER
                                        typeface = Typeface.DEFAULT_BOLD
                                        isAntiAlias = true
                                    }
                                    val offY = (p.descent() + p.ascent()) / 2f
                                    drawContext.canvas.nativeCanvas.drawText("1", center.x, center.y - offY, p)
                                }
                            }
                        },
                        codeSymbol = "⚫️",
                        symbolTitle = "Finger Position",
                        description = "Numbers 1-4 show which finger presses the fret"
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Finger Numbers Guide
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(primaryColor.copy(alpha = 0.08f))
                        .border(1.dp, primaryColor.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "🖐️ Finger Numbers:",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black),
                            color = primaryColor
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("1 = Index", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = primaryText)
                            Text("2 = Middle", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = primaryText)
                            Text("3 = Ring", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = primaryText)
                            Text("4 = Pinky", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = primaryText)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Dismiss Button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dismiss_key_dialog_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryColor
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = "Got it! 🎸",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF121212)
                    )
                }
            }
        }
    }
}

@Composable
private fun DiagramKeyRow(
    symbolSlot: @Composable () -> Unit,
    codeSymbol: String,
    symbolTitle: String,
    description: String
) {
    val isDark = LocalIsDarkTheme.current
    val rowBackground = if (isDark) ShredCardBorder.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surfaceVariant
    val primaryText = if (isDark) ShredPrimaryText else MaterialTheme.colorScheme.onSurface
    val mutedText = if (isDark) ShredMutedText else MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(rowBackground)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        symbolSlot()
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = codeSymbol,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp
                    ),
                    color = primaryText
                )
                Text(
                    text = "=",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = mutedText
                )
                Text(
                    text = symbolTitle,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = primaryText
                )
            }
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                color = mutedText
            )
        }
    }
}
