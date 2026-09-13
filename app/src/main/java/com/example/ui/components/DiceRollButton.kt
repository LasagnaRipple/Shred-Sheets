package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.HapticFeedbackManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

/**
 * Interactive Dice button that mimics a real physical dice roll when tapped.
 *
 * Features:
 * - Ultra-snappy 300ms 3D tumble-and-bounce animation (toss, mid-air spin, floor impact squash, rebound, settle).
 * - Multi-impact rhythmic haptic waveform perfectly synchronized with visual impacts.
 * - Dynamic contact shadow underneath the die responding to vertical displacement.
 * - Interruptible & responsive: rapid repeated taps immediately trigger fresh rolls with zero lag.
 */
@Composable
fun DiceRollButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val hapticManager = remember(context) { HapticFeedbackManager(context.applicationContext) }
    val composeHaptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()

    val rollProgress = remember { Animatable(0f) }
    var activeRollJob by remember { mutableStateOf<Job?>(null) }

    val handleTap = {
        // Synchronized haptic feedback mimicking the dice rolling, bouncing, and settling
        hapticManager.performDiceRollFeedback()
        composeHaptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)

        // Trigger action immediately so anagram text updates responsively with the roll
        onClick()

        activeRollJob?.cancel()
        activeRollJob = coroutineScope.launch {
            rollProgress.snapTo(0f)
            rollProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 310,
                    easing = LinearEasing
                )
            )
            rollProgress.snapTo(0f)
        }
    }

    val p = rollProgress.value

    // Calculate motion parameters based on animation progress [0f..1f]
    val (translationYPx, translationXPx, scaleX, scaleY, rotZ, rotX, rotY, shadowAlpha, shadowScale) = remember(p, density) {
        val densityVal = density.density
        if (p <= 0f || p >= 1f) {
            MotionParams(
                transY = 0f,
                transX = 0f,
                scaleX = 1f,
                scaleY = 1f,
                rotZ = 0f,
                rotX = 0f,
                rotY = 0f,
                shadowAlpha = 0.22f,
                shadowScale = 1.0f
            )
        } else if (p < 0.35f) {
            // Phase 1: Toss, mid-air 3D spin, and primary floor slam (0ms - 108ms)
            val t = p / 0.35f
            val hopSin = sin(t * PI.toFloat())
            val transY = (-10f * hopSin + t * 2.5f) * densityVal
            val transX = (-2.0f * hopSin) * densityVal

            val sX: Float
            val sY: Float
            if (t > 0.75f) {
                val squashFactor = (t - 0.75f) / 0.25f
                sX = 1f + 0.20f * squashFactor
                sY = 1f - 0.16f * squashFactor
            } else {
                sX = 1f + 0.10f * hopSin
                sY = 1f + 0.10f * hopSin
            }

            val rZ = t * 180f
            val rX = hopSin * 24f
            val rY = -hopSin * 20f

            val sAlpha = 0.22f - 0.14f * hopSin + (if (t > 0.75f) 0.18f else 0f)
            val sScale = 1.0f + 0.35f * hopSin

            MotionParams(transY, transX, sX, sY, rZ, rX, rY, sAlpha, sScale)
        } else if (p < 0.68f) {
            // Phase 2: Rebound bounce and secondary floor impact (108ms - 210ms)
            val t = (p - 0.35f) / 0.33f
            val hopSin = sin(t * PI.toFloat())
            val baseTransY = ((1f - t) * 2.5f + t * 0.8f) * densityVal
            val transY = baseTransY - (4.0f * hopSin * densityVal)
            val transX = ((1f - t) * (-0.5f) + t * 1.0f * hopSin) * densityVal

            val sX = 1f + 0.10f * (1f - hopSin) - 0.04f * hopSin
            val sY = 1f - 0.08f * (1f - hopSin) + 0.04f * hopSin

            val rZ = 180f + t * 115f
            val rX = -hopSin * 14f
            val rY = hopSin * 12f

            val sAlpha = 0.22f - 0.08f * hopSin
            val sScale = 1.0f + 0.18f * hopSin

            MotionParams(transY, transX, sX, sY, rZ, rX, rY, sAlpha, sScale)
        } else {
            // Phase 3: Final gentle hop, 360° full roll completion, and crisp settle (210ms - 310ms)
            val t = (p - 0.68f) / 0.32f
            val hopSin = sin(t * PI.toFloat())
            val transY = ((1f - t) * 0.8f - hopSin * 1.5f) * densityVal
            val transX = (1f - t) * 0.5f * densityVal

            val sX = 1f + (1f - t) * 0.04f * (1f - hopSin)
            val sY = 1f - (1f - t) * 0.03f * (1f - hopSin)

            val rZ = 295f + t * 65f
            val rX = hopSin * 5f * (1f - t)
            val rY = -hopSin * 4f * (1f - t)

            val sAlpha = 0.22f
            val sScale = 1.0f

            MotionParams(transY, transX, sX, sY, rZ, rX, rY, sAlpha, sScale)
        }
    }

    Box(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .size(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true, radius = 22.dp),
                onClick = { handleTap() }
            )
            .testTag("cycle_anagram_dice_button"),
        contentAlignment = Alignment.Center
    ) {
        // Dynamic floor contact shadow responding to vertical hop and floor impact
        Box(
            modifier = Modifier
                .offset(y = 12.dp)
                .size(width = (22.dp * shadowScale), height = 5.dp)
                .graphicsLayer {
                    this.alpha = shadowAlpha.coerceIn(0f, 1f)
                }
                .background(
                    color = Color.Black,
                    shape = CircleShape
                )
        )

        // Animated Dice Emoji with 3D rotation, trajectory translation, and impact squash
        Text(
            text = "🎲",
            fontSize = 23.sp,
            modifier = Modifier.graphicsLayer {
                this.translationY = translationYPx
                this.translationX = translationXPx
                this.scaleX = scaleX
                this.scaleY = scaleY
                this.rotationZ = rotZ
                this.rotationX = rotX
                this.rotationY = rotY
                this.cameraDistance = 12f * density.density
            }
        )
    }
}

private data class MotionParams(
    val transY: Float,
    val transX: Float,
    val scaleX: Float,
    val scaleY: Float,
    val rotZ: Float,
    val rotX: Float,
    val rotY: Float,
    val shadowAlpha: Float,
    val shadowScale: Float
)
