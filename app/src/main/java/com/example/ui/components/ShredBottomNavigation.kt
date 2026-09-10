package com.example.ui.components

import android.provider.Settings
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
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
import kotlinx.coroutines.launch

val PillSlideEasing = CubicBezierEasing(0.22f, 1.0f, 0.36f, 1.0f)

@Composable
fun ShredBottomNavigation(
    tabs: List<AppTab>,
    currentTab: AppTab,
    onTabSelected: (AppTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val density = LocalDensity.current
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

    val tabContentBounds = remember { mutableStateMapOf<Int, Pair<Float, Float>>() }
    val activeIndex = tabs.indexOf(currentTab).coerceAtLeast(0)
    val activeBounds = tabContentBounds[activeIndex]

    val animSpecFloat = if (isReduceMotionEnabled) {
        snap()
    } else {
        tween<Float>(durationMillis = 280, easing = PillSlideEasing)
    }

    val animatedPillLeft by animateFloatAsState(
        targetValue = activeBounds?.first ?: 0f,
        animationSpec = animSpecFloat,
        label = "pillLeftAnim"
    )
    val animatedPillWidth by animateFloatAsState(
        targetValue = activeBounds?.second ?: 0f,
        animationSpec = animSpecFloat,
        label = "pillWidthAnim"
    )

    val activeAccent = MaterialTheme.colorScheme.primary
    val isDark = LocalIsDarkTheme.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                1.5.dp,
                if (isDark) ShredCardBorder.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant,
                RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(68.dp)
        ) {
            if (animatedPillWidth > 0f) {
                val pillLeftDp = with(density) { animatedPillLeft.toDp() }
                val pillWidthDp = with(density) { animatedPillWidth.toDp() }

                Box(
                    modifier = Modifier
                        .offset(x = pillLeftDp)
                        .width(pillWidthDp)
                        .height(40.dp)
                        .align(Alignment.CenterStart)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val cornerRadPx = 14.dp.toPx()
                        drawRoundRect(
                            color = activeAccent.copy(alpha = 0.18f),
                            topLeft = Offset(-3.dp.toPx(), -2.dp.toPx()),
                            size = Size(size.width + 6.dp.toPx(), size.height + 4.dp.toPx()),
                            cornerRadius = CornerRadius(cornerRadPx + 2.dp.toPx())
                        )
                        drawRoundRect(
                            color = activeAccent.copy(alpha = 0.22f),
                            topLeft = Offset.Zero,
                            size = size,
                            cornerRadius = CornerRadius(cornerRadPx)
                        )
                        drawRoundRect(
                            color = activeAccent.copy(alpha = 0.40f),
                            topLeft = Offset.Zero,
                            size = size,
                            cornerRadius = CornerRadius(cornerRadPx),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                tabs.forEachIndexed { index, tab ->
                    val isSelected = tab == currentTab
                    ShredNavTabItem(
                        tab = tab,
                        isSelected = isSelected,
                        isReduceMotion = isReduceMotionEnabled,
                        onBoundsCalculated = { startX, width ->
                            tabContentBounds[index] = Pair(startX, width)
                        },
                        onClick = {
                            if (!isSelected) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onTabSelected(tab)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun RowScope.ShredNavTabItem(
    tab: AppTab,
    isSelected: Boolean,
    isReduceMotion: Boolean,
    onBoundsCalculated: (Float, Float) -> Unit,
    onClick: () -> Unit
) {
    val animDuration = if (isReduceMotion) 0 else 280
    val activeAccent = MaterialTheme.colorScheme.primary
    val isDark = LocalIsDarkTheme.current
    val inactiveColor = if (isDark) ShredNavInactive else MaterialTheme.colorScheme.onSurfaceVariant
    val labelColor by animateColorAsState(
        targetValue = if (isSelected) activeAccent else inactiveColor,
        animationSpec = tween(animDuration, easing = PillSlideEasing),
        label = "navColorCrossFade"
    )

    Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .defaultMinSize(minHeight = 48.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .testTag("nav_tab_${tab.name.lowercase()}"),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .onGloballyPositioned { coordinates ->
                    val parent = coordinates.parentLayoutCoordinates
                    if (parent != null) {
                        val rowPos = coordinates.positionInParent()
                        val boxPos = parent.positionInParent()
                        onBoundsCalculated(boxPos.x + rowPos.x, coordinates.size.width.toFloat())
                    } else {
                        val pos = coordinates.positionInParent()
                        onBoundsCalculated(pos.x, coordinates.size.width.toFloat())
                    }
                }
                .padding(horizontal = 14.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = tab.title,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Clip,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 0.5.sp
                ),
                color = labelColor
            )
        }
    }
}
