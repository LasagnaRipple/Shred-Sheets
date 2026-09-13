package com.example.ui.components

import android.provider.Settings
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalIsDarkTheme
import com.example.ui.theme.ShredCardBorder
import com.example.ui.theme.ShredNavInactive
import com.example.viewmodel.AppTab

val PillSlideEasing = CubicBezierEasing(0.22f, 1.0f, 0.36f, 1.0f)

@Composable
fun ShredBottomNavigation(
    tabs: List<AppTab>,
    currentTab: AppTab,
    onTabSelected: (AppTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    val isReduceMotionEnabled = remember(context) {
        try {
            val scale = Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1.0f
            )
            scale == 0f
        } catch (_: Exception) {
            false
        }
    }

    val activeIndex = tabs.indexOf(currentTab).coerceAtLeast(0)
    val activeAccent = MaterialTheme.colorScheme.primary
    val isDark = LocalIsDarkTheme.current
    val inactiveColor = if (isDark) ShredNavInactive else MaterialTheme.colorScheme.onSurfaceVariant

    val animDuration = if (isReduceMotionEnabled) 0 else 280

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = 1.dp,
                color = if (isDark) ShredCardBorder.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
            )
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(76.dp)
        ) {
            val totalWidth = maxWidth
            val tabCount = tabs.size.coerceAtLeast(1)
            val tabWidth = totalWidth / tabCount
            val pillWidth = 64.dp
            val pillHeight = 32.dp
            val pillTop = 10.dp

            val targetPillOffset = tabWidth * activeIndex + (tabWidth - pillWidth) / 2

            val animatedPillOffset by animateDpAsState(
                targetValue = targetPillOffset,
                animationSpec = if (isReduceMotionEnabled) snap() else tween(durationMillis = animDuration, easing = PillSlideEasing),
                label = "pillOffsetAnim"
            )

            // WhatsApp / M3 style sliding active indicator pill behind active tab icon
            Box(
                modifier = Modifier
                    .offset(x = animatedPillOffset, y = pillTop)
                    .width(pillWidth)
                    .height(pillHeight)
                    .clip(RoundedCornerShape(16.dp))
                    .background(activeAccent.copy(alpha = 0.22f))
                    .border(
                        width = 1.dp,
                        color = activeAccent.copy(alpha = 0.38f),
                        shape = RoundedCornerShape(16.dp)
                    )
            )

            // Tab items row
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.Top
            ) {
                tabs.forEach { tab ->
                    val isSelected = tab == currentTab

                    val labelColor by animateColorAsState(
                        targetValue = if (isSelected) activeAccent else inactiveColor,
                        animationSpec = tween(animDuration, easing = PillSlideEasing),
                        label = "labelColorAnim"
                    )

                    val iconTint by animateColorAsState(
                        targetValue = if (isSelected) activeAccent else inactiveColor,
                        animationSpec = tween(animDuration, easing = PillSlideEasing),
                        label = "iconTintAnim"
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .defaultMinSize(minHeight = 48.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = {
                                    if (!isSelected) {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        onTabSelected(tab)
                                    }
                                }
                            )
                            .testTag("nav_tab_${tab.name.lowercase()}"),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Top,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = pillTop, bottom = 6.dp)
                        ) {
                            // Icon container perfectly matching the pill dimensions
                            Box(
                                modifier = Modifier
                                    .width(pillWidth)
                                    .height(pillHeight),
                                contentAlignment = Alignment.Center
                            ) {
                                ShredNavTabIcon(
                                    tab = tab,
                                    tint = iconTint,
                                    modifier = Modifier.size(21.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = tab.title,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 12.sp,
                                    letterSpacing = 0.3.sp
                                ),
                                color = labelColor
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ShredNavTabIcon(
    tab: AppTab,
    tint: Color,
    modifier: Modifier = Modifier
) {
    when (tab) {
        AppTab.TUNER -> NavTunerIcon(tint = tint, modifier = modifier)
        AppTab.CHORDS -> NavChordsIcon(tint = tint, modifier = modifier)
        AppTab.LOOP -> NavInfinityIcon(tint = tint, modifier = modifier)
        AppTab.SHOP -> NavShopIcon(tint = tint, modifier = modifier)
    }
}

/**
 * Tuner icon: Lightning bolt matching tuner_1_bolt.png
 * Vertically and horizontally centered with balanced top/bottom margins.
 */
@Composable
fun NavTunerIcon(
    tint: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(21.dp)) {
        val w = size.width
        val h = size.height
        val boltPath = Path().apply {
            moveTo(w * 0.62f, h * 0.08f)
            lineTo(w * 0.22f, h * 0.53f)
            lineTo(w * 0.45f, h * 0.53f)
            lineTo(w * 0.34f, h * 0.92f)
            lineTo(w * 0.78f, h * 0.43f)
            lineTo(w * 0.55f, h * 0.43f)
            close()
        }
        drawPath(path = boltPath, color = tint)
    }
}

/**
 * Chords icon: 3x3 chord fretboard diagram matching chords_3_grid.png
 * Vertically and horizontally centered with balanced margins.
 */
@Composable
fun NavChordsIcon(
    tint: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(21.dp)) {
        val w = size.width
        val h = size.height

        val left = w * 0.16f
        val right = w * 0.84f
        val top = h * 0.12f
        val bottom = h * 0.88f

        val gridWidth = right - left
        val gridHeight = bottom - top

        val colStep = gridWidth / 3f
        val rowStep = gridHeight / 3f

        val lineStroke = 1.5.dp.toPx()
        val nutStroke = 3.0.dp.toPx()

        // 3 fret wire lines (rows 1, 2, 3)
        for (i in 1..3) {
            val y = top + i * rowStep
            drawLine(
                color = tint,
                start = Offset(left, y),
                end = Offset(right, y),
                strokeWidth = lineStroke
            )
        }

        // 4 vertical string lines (cols 0, 1, 2, 3)
        for (i in 0..3) {
            val x = left + i * colStep
            drawLine(
                color = tint,
                start = Offset(x, top),
                end = Offset(x, bottom),
                strokeWidth = lineStroke
            )
        }

        // Nut at top (thicker bar)
        drawLine(
            color = tint,
            start = Offset(left - 0.5.dp.toPx(), top),
            end = Offset(right + 0.5.dp.toPx(), top),
            strokeWidth = nutStroke
        )

        // Dot 1: String 2 (index 1), Fret 1 (top fret)
        drawCircle(
            color = tint,
            radius = 2.2.dp.toPx(),
            center = Offset(left + colStep, top + rowStep * 0.5f)
        )

        // Dot 2: String 3 (index 2), Fret 2 (middle fret)
        drawCircle(
            color = tint,
            radius = 2.2.dp.toPx(),
            center = Offset(left + 2f * colStep, top + rowStep * 1.5f)
        )
    }
}

/**
 * Metronome icon: Clock icon
 * Symmetrically centered within 21.dp canvas.
 */
@Composable
fun NavClockIcon(
    tint: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(21.dp)) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val radius = size.minDimension * 0.38f
        val strokeWidth = 1.9.dp.toPx()

        // Clock outer ring
        drawCircle(
            color = tint,
            radius = radius,
            center = Offset(cx, cy),
            style = Stroke(width = strokeWidth)
        )

        // Minute hand pointing to 12 o'clock
        drawLine(
            color = tint,
            start = Offset(cx, cy),
            end = Offset(cx, cy - radius * 0.65f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )

        // Hour hand pointing to 3 o'clock
        drawLine(
            color = tint,
            start = Offset(cx, cy),
            end = Offset(cx + radius * 0.48f, cy),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )

        // Center pivot dot
        drawCircle(
            color = tint,
            radius = 1.4.dp.toPx(),
            center = Offset(cx, cy)
        )
    }
}

/**
 * Loops icon: Infinity symbol (∞)
 * Symmetrically centered within 21.dp canvas.
 */
@Composable
fun NavInfinityIcon(
    tint: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(21.dp)) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val rx = size.width * 0.36f
        val ry = size.height * 0.20f
        val strokeWidth = 2.0.dp.toPx()

        val path = Path().apply {
            moveTo(cx, cy)
            // Left lobe: top curve going out to left
            cubicTo(
                cx - rx * 0.45f, cy - ry * 1.15f,
                cx - rx * 1.12f, cy - ry * 1.15f,
                cx - rx, cy
            )
            // Left lobe: bottom curve coming back to center
            cubicTo(
                cx - rx * 1.12f, cy + ry * 1.15f,
                cx - rx * 0.45f, cy + ry * 1.15f,
                cx, cy
            )
            // Right lobe: top curve going out to right
            cubicTo(
                cx + rx * 0.45f, cy - ry * 1.15f,
                cx + rx * 1.12f, cy - ry * 1.15f,
                cx + rx, cy
            )
            // Right lobe: bottom curve coming back to center
            cubicTo(
                cx + rx * 1.12f, cy + ry * 1.15f,
                cx + rx * 0.45f, cy + ry * 1.15f,
                cx, cy
            )
            close()
        }

        drawPath(
            path = path,
            color = tint,
            style = Stroke(
                width = strokeWidth,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }
}

/**
 * Shop icon: Sleek shopping bag with curved handle
 * Symmetrically centered within 21.dp canvas.
 */
@Composable
fun NavShopIcon(
    tint: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(21.dp)) {
        val w = size.width
        val h = size.height
        val strokeWidth = 1.9.dp.toPx()

        // Bag body: trapezoid with rounded bottom
        val bagPath = Path().apply {
            moveTo(w * 0.22f, h * 0.38f)
            lineTo(w * 0.18f, h * 0.88f)
            lineTo(w * 0.82f, h * 0.88f)
            lineTo(w * 0.78f, h * 0.38f)
            close()
        }

        drawPath(
            path = bagPath,
            color = tint,
            style = Stroke(
                width = strokeWidth,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        // Handle: arch looping up from the bag top
        val handlePath = Path().apply {
            moveTo(w * 0.36f, h * 0.38f)
            cubicTo(
                w * 0.36f, h * 0.14f,
                w * 0.64f, h * 0.14f,
                w * 0.64f, h * 0.38f
            )
        }

        drawPath(
            path = handlePath,
            color = tint,
            style = Stroke(
                width = strokeWidth,
                cap = StrokeCap.Round
            )
        )
    }
}

