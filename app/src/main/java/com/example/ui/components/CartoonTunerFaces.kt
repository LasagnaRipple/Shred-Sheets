package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.theme.LocalIsDarkTheme
import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * Cartoon Face States for Tuner Circle:
 * - 3 Idle faces rotating naturally during 'Tap to start' / ready mode
 * - 7 Expressive threshold faces representing pitch accuracy:
 *   [RIP] --- [Flat - Tune up] --- [Almost there] --- [Perfect! In Tune] --- [Almost there] --- [Sharp - Tune down] --- [RIP]
 */
enum class CartoonTunerFaceType {
    IDLE_CHEEKY,        // Idle Face 1: Looking down-left, cute "w" mouth
    IDLE_PUFFY_SMILE,   // Idle Face 2: Sweet smile with prominent puffy cheeks
    IDLE_SIDE_GLANCE,   // Idle Face 3: Side-glance right, sly smirk

    EXTREME_FLAT,       // "RIP": Tilted oval X-eyes, arched nose, and sad frown with tongue
    FLAT,               // "Flat - Tune up": Flaming eyes, screaming open mouth
    CLOSE_FLAT,         // "Almost there": Wide eager eyes looking right, toothy crescent grin
    PERFECT,            // "Perfect! In Tune": Lightning bolt eyes, confident dimpled rockstar grin
    CLOSE_SHARP,        // "Almost there": Side-glance eyes looking left, comic sweat drops flying
    SHARP,              // "Sharp - Tune down": Dizzy wavy eyes, twinkling sparkles/stars, squiggly mouth
    EXTREME_SHARP       // "RIP": Tilted oval X-eyes, arched nose, and sad downturned frown
}

@Composable
fun CartoonTunerFace(
    faceType: CartoonTunerFaceType,
    modifier: Modifier = Modifier,
    isAllStringsTuned: Boolean = false
) {
    val isDark = LocalIsDarkTheme.current

    // Infinite transitions for dynamic character animations (flame flicker, stars twinkling, tongue wobble, etc.)
    val infiniteTransition = rememberInfiniteTransition(label = "cartoonFaceInfiniteAnim")

    val flameFlicker by infiniteTransition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.14f,
        animationSpec = infiniteRepeatable(
            animation = tween(180, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flameFlicker"
    )

    val starRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "starRotation"
    )

    val starPulse by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(450, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "starPulse"
    )

    val sweatBounce by infiniteTransition.animateFloat(
        initialValue = -1.5f,
        targetValue = 2.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(320, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sweatBounce"
    )

    val tongueWobble by infiniteTransition.animateFloat(
        initialValue = -2.5f,
        targetValue = 2.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(260, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "tongueWobble"
    )

    val cloudBillow by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cloudBillow"
    )

    val lightningPulse by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "lightningPulse"
    )

    // Blink animation for idle faces during transitions
    val blinkScaleY = remember { Animatable(1.0f) }
    LaunchedEffect(faceType) {
        if (faceType in listOf(CartoonTunerFaceType.IDLE_CHEEKY, CartoonTunerFaceType.IDLE_PUFFY_SMILE, CartoonTunerFaceType.IDLE_SIDE_GLANCE)) {
            blinkScaleY.animateTo(0.12f, tween(65, easing = FastOutSlowInEasing))
            blinkScaleY.animateTo(1.0f, tween(90, easing = FastOutSlowInEasing))
        } else {
            blinkScaleY.snapTo(1.0f)
        }
    }

    // Color definitions for classic 1930s rubber-hose ink drawing
    val inkColor = Color(0xFF141416)
    val scleraColor = Color(0xFFFDFDFD)
    val highlightColor = Color.White
    // Solid white line for facial features (eyebrows, mouth, cheeks, nose) on dark background
    val featureLineColor = if (isDark) Color.White else Color(0xFF141416)

    Canvas(
        modifier = modifier
            .testTag("modern_eyes_icon")
            .testTag("cartoon_tuner_face_${faceType.name.lowercase()}")
    ) {
        val w = size.width
        val h = size.height
        val strokeWidth = (h * 0.048f).coerceIn(2.8f, 4.4f)

        when (faceType) {
            CartoonTunerFaceType.IDLE_CHEEKY -> {
                drawIdleCheekyFace(
                    w = w,
                    h = h,
                    strokeWidth = strokeWidth,
                    inkColor = inkColor,
                    scleraColor = scleraColor,
                    highlightColor = highlightColor,
                    featureLineColor = featureLineColor,
                    blinkScale = blinkScaleY.value
                )
            }
            CartoonTunerFaceType.IDLE_PUFFY_SMILE -> {
                drawIdlePuffySmileFace(
                    w = w,
                    h = h,
                    strokeWidth = strokeWidth,
                    inkColor = inkColor,
                    scleraColor = scleraColor,
                    highlightColor = highlightColor,
                    featureLineColor = featureLineColor,
                    blinkScale = blinkScaleY.value
                )
            }
            CartoonTunerFaceType.IDLE_SIDE_GLANCE -> {
                drawIdleSideGlanceFace(
                    w = w,
                    h = h,
                    strokeWidth = strokeWidth,
                    inkColor = inkColor,
                    scleraColor = scleraColor,
                    highlightColor = highlightColor,
                    featureLineColor = featureLineColor,
                    blinkScale = blinkScaleY.value
                )
            }
            CartoonTunerFaceType.EXTREME_FLAT -> {
                drawRipDeadFace(
                    w = w,
                    h = h,
                    strokeWidth = strokeWidth,
                    inkColor = inkColor,
                    scleraColor = scleraColor,
                    shudderWobble = tongueWobble,
                    featureLineColor = featureLineColor,
                    isDark = isDark
                )
            }
            CartoonTunerFaceType.FLAT -> {
                drawSharpFace(
                    w = w,
                    h = h,
                    strokeWidth = strokeWidth,
                    inkColor = inkColor,
                    scleraColor = scleraColor,
                    starPulse = starPulse,
                    starRotation = starRotation,
                    featureLineColor = featureLineColor
                )
            }
            CartoonTunerFaceType.CLOSE_FLAT -> {
                drawAlmostThereSharpFace(
                    w = w,
                    h = h,
                    strokeWidth = strokeWidth,
                    inkColor = inkColor,
                    scleraColor = scleraColor,
                    highlightColor = highlightColor,
                    sweatBounce = sweatBounce,
                    featureLineColor = featureLineColor
                )
            }
            CartoonTunerFaceType.PERFECT -> {
                drawPerfectFace(
                    w = w,
                    h = h,
                    strokeWidth = strokeWidth,
                    inkColor = inkColor,
                    scleraColor = scleraColor,
                    lightningPulse = lightningPulse,
                    featureLineColor = featureLineColor,
                    isAllStringsTuned = isAllStringsTuned
                )
            }
            CartoonTunerFaceType.CLOSE_SHARP -> {
                drawAlmostThereSharpFace(
                    w = w,
                    h = h,
                    strokeWidth = strokeWidth,
                    inkColor = inkColor,
                    scleraColor = scleraColor,
                    highlightColor = highlightColor,
                    sweatBounce = sweatBounce,
                    featureLineColor = featureLineColor
                )
            }
            CartoonTunerFaceType.SHARP -> {
                drawSharpFace(
                    w = w,
                    h = h,
                    strokeWidth = strokeWidth,
                    inkColor = inkColor,
                    scleraColor = scleraColor,
                    starPulse = starPulse,
                    starRotation = starRotation,
                    featureLineColor = featureLineColor
                )
            }
            CartoonTunerFaceType.EXTREME_SHARP -> {
                drawRipDeadFace(
                    w = w,
                    h = h,
                    strokeWidth = strokeWidth,
                    inkColor = inkColor,
                    scleraColor = scleraColor,
                    shudderWobble = tongueWobble,
                    featureLineColor = featureLineColor,
                    isDark = isDark
                )
            }
        }
    }
}

// =========================================================================================
// 1. IDLE FACES
// =========================================================================================

/**
 * Idle Face 1: Looking down-left with pie-cut pupils and adorable "w" cat mouth.
 */
private fun DrawScope.drawIdleCheekyFace(
    w: Float,
    h: Float,
    strokeWidth: Float,
    inkColor: Color,
    scleraColor: Color,
    highlightColor: Color,
    featureLineColor: Color,
    blinkScale: Float
) {
    // 1. Solid curved eyebrows over eyes
    val browY = h * 0.14f
    drawArch(
        start = Offset(w * 0.21f, browY + h * 0.04f),
        control = Offset(w * 0.33f, browY - h * 0.04f),
        end = Offset(w * 0.44f, browY + h * 0.03f),
        color = featureLineColor,
        strokeWidth = strokeWidth * 1.35f
    )
    drawArch(
        start = Offset(w * 0.56f, browY + h * 0.03f),
        control = Offset(w * 0.67f, browY - h * 0.04f),
        end = Offset(w * 0.79f, browY + h * 0.04f),
        color = featureLineColor,
        strokeWidth = strokeWidth * 1.35f
    )

    // 2. Eyes (Stadium capsules)
    val eyeTop = h * 0.22f
    val eyeH = h * 0.44f * blinkScale
    val eyeW = w * 0.26f

    val leftEyeLeft = w * 0.21f
    val rightEyeLeft = w * 0.53f

    drawStadiumEye(
        left = leftEyeLeft,
        top = eyeTop + (h * 0.44f - eyeH) * 0.5f,
        width = eyeW,
        height = eyeH,
        strokeWidth = strokeWidth,
        inkColor = inkColor,
        scleraColor = scleraColor
    )
    drawStadiumEye(
        left = rightEyeLeft,
        top = eyeTop + (h * 0.44f - eyeH) * 0.5f,
        width = eyeW,
        height = eyeH,
        strokeWidth = strokeWidth,
        inkColor = inkColor,
        scleraColor = scleraColor
    )

    if (blinkScale > 0.3f) {
        // Pupils looking down and left with classic pie-cut slice highlight
        val pupilW = eyeW * 0.48f
        val pupilH = eyeH * 0.52f
        val pupilY = eyeTop + eyeH * 0.38f

        drawPieCutPupil(
            center = Offset(leftEyeLeft + pupilW * 0.72f, pupilY + pupilH * 0.48f),
            radius = pupilW * 0.48f,
            inkColor = inkColor,
            highlightColor = highlightColor,
            notchAngle = -45f
        )
        drawPieCutPupil(
            center = Offset(rightEyeLeft + pupilW * 0.72f, pupilY + pupilH * 0.48f),
            radius = pupilW * 0.48f,
            inkColor = inkColor,
            highlightColor = highlightColor,
            notchAngle = -45f
        )
    }

    // 3. Cute rounded button nose loop
    drawArch(
        start = Offset(w * 0.46f, h * 0.67f),
        control = Offset(w * 0.50f, h * 0.70f),
        end = Offset(w * 0.54f, h * 0.67f),
        color = featureLineColor,
        strokeWidth = strokeWidth * 1.0f
    )

    // 4. "w" Cute smiling mouth in solid line
    val mouthY = h * 0.76f
    val mouthPath = Path().apply {
        moveTo(w * 0.33f, mouthY)
        quadraticTo(w * 0.42f, mouthY + h * 0.09f, w * 0.50f, mouthY + h * 0.02f)
        quadraticTo(w * 0.58f, mouthY + h * 0.09f, w * 0.67f, mouthY)
    }
    drawPathSolid(mouthPath, featureLineColor, strokeWidth * 1.35f)

    // Little chin tick
    val chinPath = Path().apply {
        moveTo(w * 0.47f, mouthY + h * 0.12f)
        quadraticTo(w * 0.50f, mouthY + h * 0.15f, w * 0.53f, mouthY + h * 0.12f)
    }
    drawPathSolid(chinPath, featureLineColor, strokeWidth * 1.0f)
}

/**
 * Idle Face 2: Sweet smile with prominent puffed cheeks (the one in the center circle).
 */
private fun DrawScope.drawIdlePuffySmileFace(
    w: Float,
    h: Float,
    strokeWidth: Float,
    inkColor: Color,
    scleraColor: Color,
    highlightColor: Color,
    featureLineColor: Color,
    blinkScale: Float
) {
    // 1. Eyebrows in solid line
    val browY = h * 0.13f
    drawArch(
        start = Offset(w * 0.21f, browY + h * 0.035f),
        control = Offset(w * 0.33f, browY - h * 0.045f),
        end = Offset(w * 0.44f, browY + h * 0.025f),
        color = featureLineColor,
        strokeWidth = strokeWidth * 1.35f
    )
    drawArch(
        start = Offset(w * 0.56f, browY + h * 0.025f),
        control = Offset(w * 0.67f, browY - h * 0.045f),
        end = Offset(w * 0.79f, browY + h * 0.035f),
        color = featureLineColor,
        strokeWidth = strokeWidth * 1.35f
    )

    // 2. Eyes (Tall oval capsules, relaxed top)
    val eyeTop = h * 0.21f
    val eyeH = h * 0.45f * blinkScale
    val eyeW = w * 0.26f

    val leftEyeLeft = w * 0.21f
    val rightEyeLeft = w * 0.53f

    drawStadiumEye(
        left = leftEyeLeft,
        top = eyeTop + (h * 0.45f - eyeH) * 0.5f,
        width = eyeW,
        height = eyeH,
        strokeWidth = strokeWidth,
        inkColor = inkColor,
        scleraColor = scleraColor
    )
    drawStadiumEye(
        left = rightEyeLeft,
        top = eyeTop + (h * 0.45f - eyeH) * 0.5f,
        width = eyeW,
        height = eyeH,
        strokeWidth = strokeWidth,
        inkColor = inkColor,
        scleraColor = scleraColor
    )

    if (blinkScale > 0.3f) {
        // Eyelid line at top of eyes for the sweet, relaxed gaze
        val lidPathL = Path().apply {
            moveTo(leftEyeLeft + eyeW * 0.08f, eyeTop + eyeH * 0.16f)
            quadraticTo(leftEyeLeft + eyeW * 0.5f, eyeTop + eyeH * 0.22f, leftEyeLeft + eyeW * 0.92f, eyeTop + eyeH * 0.16f)
        }
        drawPath(lidPathL, inkColor, style = Stroke(width = strokeWidth * 0.8f, cap = StrokeCap.Round))

        val lidPathR = Path().apply {
            moveTo(rightEyeLeft + eyeW * 0.08f, eyeTop + eyeH * 0.16f)
            quadraticTo(rightEyeLeft + eyeW * 0.5f, eyeTop + eyeH * 0.22f, rightEyeLeft + eyeW * 0.92f, eyeTop + eyeH * 0.16f)
        }
        drawPath(lidPathR, inkColor, style = Stroke(width = strokeWidth * 0.8f, cap = StrokeCap.Round))

        // Large friendly pupils with specular highlight
        val pupilW = eyeW * 0.46f
        val pupilH = eyeH * 0.46f
        val pupilY = eyeTop + eyeH * 0.35f

        drawPieCutPupil(
            center = Offset(leftEyeLeft + pupilW * 0.82f, pupilY + pupilH * 0.5f),
            radius = pupilW * 0.46f,
            inkColor = inkColor,
            highlightColor = highlightColor,
            notchAngle = -35f
        )
        drawPieCutPupil(
            center = Offset(rightEyeLeft + pupilW * 0.82f, pupilY + pupilH * 0.5f),
            radius = pupilW * 0.46f,
            inkColor = inkColor,
            highlightColor = highlightColor,
            notchAngle = -35f
        )
    }

    // 3. Nose (gentle solid loop)
    drawArch(
        start = Offset(w * 0.47f, h * 0.65f),
        control = Offset(w * 0.50f, h * 0.69f),
        end = Offset(w * 0.53f, h * 0.65f),
        color = featureLineColor,
        strokeWidth = strokeWidth * 1.0f
    )

    // Sweet smile in solid white line
    val mouthPath = Path().apply {
        moveTo(w * 0.35f, h * 0.77f)
        quadraticTo(w * 0.50f, h * 0.88f, w * 0.65f, h * 0.77f)
    }
    drawPathSolid(mouthPath, featureLineColor, strokeWidth * 1.35f)
}

/**
 * Idle Face 3: Side glance with playful smirk (matching attached screenshot).
 */
private fun DrawScope.drawIdleSideGlanceFace(
    w: Float,
    h: Float,
    strokeWidth: Float,
    inkColor: Color,
    scleraColor: Color,
    highlightColor: Color,
    featureLineColor: Color,
    blinkScale: Float
) {
    // 1. Eyebrows in solid line, smoothly arching right above the top of each eye capsule
    val browY = h * 0.14f
    drawArch(
        start = Offset(w * 0.21f, browY + h * 0.04f),
        control = Offset(w * 0.33f, browY - h * 0.045f),
        end = Offset(w * 0.44f, browY + h * 0.03f),
        color = featureLineColor,
        strokeWidth = strokeWidth * 1.35f
    )
    drawArch(
        start = Offset(w * 0.56f, browY + h * 0.03f),
        control = Offset(w * 0.67f, browY - h * 0.045f),
        end = Offset(w * 0.79f, browY + h * 0.04f),
        color = featureLineColor,
        strokeWidth = strokeWidth * 1.35f
    )

    // 2. Eyes (Left round/oval, Right slanted oval)
    val eyeTop = h * 0.22f
    val eyeH = h * 0.44f * blinkScale
    val eyeW = w * 0.26f

    val leftEyeLeft = w * 0.21f
    val rightEyeLeft = w * 0.53f

    drawStadiumEye(
        left = leftEyeLeft,
        top = eyeTop + (h * 0.44f - eyeH) * 0.5f,
        width = eyeW,
        height = eyeH,
        strokeWidth = strokeWidth,
        inkColor = inkColor,
        scleraColor = scleraColor
    )
    drawStadiumEye(
        left = rightEyeLeft,
        top = eyeTop + (h * 0.44f - eyeH) * 0.5f,
        width = eyeW,
        height = eyeH,
        strokeWidth = strokeWidth,
        inkColor = inkColor,
        scleraColor = scleraColor
    )

    if (blinkScale > 0.3f) {
        // Pupils looking rightward (side glance)
        val pupilW = eyeW * 0.48f
        val pupilH = eyeH * 0.50f
        val pupilY = eyeTop + eyeH * 0.30f

        drawPieCutPupil(
            center = Offset(leftEyeLeft + eyeW * 0.66f, pupilY + pupilH * 0.5f),
            radius = pupilW * 0.48f,
            inkColor = inkColor,
            highlightColor = highlightColor,
            notchAngle = 45f
        )
        drawPieCutPupil(
            center = Offset(rightEyeLeft + eyeW * 0.70f, pupilY + pupilH * 0.5f),
            radius = pupilW * 0.48f,
            inkColor = inkColor,
            highlightColor = highlightColor,
            notchAngle = 45f
        )
    }

    // 3. Cute subtle nose loop in solid white
    drawArch(
        start = Offset(w * 0.47f, h * 0.65f),
        control = Offset(w * 0.50f, h * 0.69f),
        end = Offset(w * 0.53f, h * 0.65f),
        color = featureLineColor,
        strokeWidth = strokeWidth * 1.0f
    )

    // 4. Solid white sly smirk with corner upturn (matching user's drawing)
    val smirkPath = Path().apply {
        moveTo(w * 0.42f, h * 0.79f)
        quadraticTo(w * 0.54f, h * 0.81f, w * 0.65f, h * 0.74f)
        quadraticTo(w * 0.68f, h * 0.71f, w * 0.67f, h * 0.68f)
    }
    drawPathSolid(smirkPath, featureLineColor, strokeWidth * 1.35f)
}

// =========================================================================================
// 2. THRESHOLD FACES (7 PITCH ACCURACY STATES)
// =========================================================================================

/**
 * State 1: "Ur dead" (Extreme Flat)
 * Mushroom clouds / nuclear explosions inside the eyes, dazed wobbly mouth.
 */
private fun DrawScope.drawUrDeadFace(
    w: Float,
    h: Float,
    strokeWidth: Float,
    inkColor: Color,
    scleraColor: Color,
    highlightColor: Color,
    featureLineColor: Color,
    cloudBillow: Float
) {
    // 1. Startled / shocked eyebrows raised high in solid line
    val browY = h * 0.10f
    drawArch(
        start = Offset(w * 0.20f, browY + h * 0.05f),
        control = Offset(w * 0.32f, browY - h * 0.04f),
        end = Offset(w * 0.44f, browY + h * 0.04f),
        color = featureLineColor,
        strokeWidth = strokeWidth * 1.35f
    )
    drawArch(
        start = Offset(w * 0.56f, browY + h * 0.04f),
        control = Offset(w * 0.68f, browY - h * 0.04f),
        end = Offset(w * 0.80f, browY + h * 0.05f),
        color = featureLineColor,
        strokeWidth = strokeWidth * 1.35f
    )

    // 2. Large round eye sockets
    val eyeW = w * 0.29f
    val eyeH = h * 0.48f
    val eyeTop = h * 0.17f
    val leftEyeLeft = w * 0.19f
    val rightEyeLeft = w * 0.52f

    drawStadiumEye(leftEyeLeft, eyeTop, eyeW, eyeH, strokeWidth, inkColor, scleraColor)
    drawStadiumEye(rightEyeLeft, eyeTop, eyeW, eyeH, strokeWidth, inkColor, scleraColor)

    // 3. Mushroom Cloud Explosions inside each eye!
    drawMushroomCloud(
        left = leftEyeLeft + eyeW * 0.08f,
        top = eyeTop + eyeH * 0.06f,
        width = eyeW * 0.84f,
        height = eyeH * 0.88f,
        strokeWidth = strokeWidth * 0.85f,
        inkColor = inkColor,
        scale = cloudBillow
    )
    drawMushroomCloud(
        left = rightEyeLeft + eyeW * 0.08f,
        top = eyeTop + eyeH * 0.06f,
        width = eyeW * 0.84f,
        height = eyeH * 0.88f,
        strokeWidth = strokeWidth * 0.85f,
        inkColor = inkColor,
        scale = cloudBillow
    )

    // 4. Nose (subtle curve in solid line)
    drawArch(
        start = Offset(w * 0.47f, h * 0.67f),
        control = Offset(w * 0.50f, h * 0.71f),
        end = Offset(w * 0.53f, h * 0.67f),
        color = featureLineColor,
        strokeWidth = strokeWidth * 1.0f
    )

    // 5. Dazed, shell-shocked wobbly wavy droop in solid white line: ~ ~
    val mouthY = h * 0.77f
    val mouthPath = Path().apply {
        moveTo(w * 0.32f, mouthY)
        cubicTo(w * 0.40f, mouthY - h * 0.05f, w * 0.44f, mouthY + h * 0.05f, w * 0.50f, mouthY)
        cubicTo(w * 0.56f, mouthY - h * 0.05f, w * 0.60f, mouthY + h * 0.05f, w * 0.68f, mouthY)
    }
    drawPathSolid(mouthPath, featureLineColor, strokeWidth * 1.35f)

    // Droop lines at edges
    drawPathSolid(
        Path().apply {
            moveTo(w * 0.32f, mouthY)
            lineTo(w * 0.30f, mouthY + h * 0.03f)
        },
        featureLineColor,
        strokeWidth * 1.15f
    )
    drawPathSolid(
        Path().apply {
            moveTo(w * 0.68f, mouthY)
            lineTo(w * 0.70f, mouthY + h * 0.03f)
        },
        featureLineColor,
        strokeWidth * 1.15f
    )
}

/**
 * State 2: "Flat - Tune up" (Flat)
 * Raging flames blasting out of the eyes, screaming open mouth.
 */
private fun DrawScope.drawFlameFace(
    w: Float,
    h: Float,
    strokeWidth: Float,
    inkColor: Color,
    scleraColor: Color,
    flameFlicker: Float,
    featureLineColor: Color
) {
    // 1. Fiery, furious eyebrows angled inward in solid line
    val browY = h * 0.12f
    drawPathSolid(
        Path().apply {
            moveTo(w * 0.18f, browY - h * 0.02f)
            lineTo(w * 0.45f, browY + h * 0.08f)
        },
        featureLineColor,
        strokeWidth * 1.4f
    )
    drawPathSolid(
        Path().apply {
            moveTo(w * 0.82f, browY - h * 0.02f)
            lineTo(w * 0.55f, browY + h * 0.08f)
        },
        featureLineColor,
        strokeWidth * 1.4f
    )

    // 2. Eyes with Raging Flames licking upward!
    val eyeW = w * 0.28f
    val eyeH = h * 0.44f
    val eyeTop = h * 0.20f
    val leftEyeLeft = w * 0.20f
    val rightEyeLeft = w * 0.52f

    drawStadiumEye(leftEyeLeft, eyeTop, eyeW, eyeH, strokeWidth, inkColor, scleraColor)
    drawStadiumEye(rightEyeLeft, eyeTop, eyeW, eyeH, strokeWidth, inkColor, scleraColor)

    // Left Eye Flames
    drawEyeFlameTongues(
        centerX = leftEyeLeft + eyeW * 0.5f,
        bottomY = eyeTop + eyeH * 0.85f,
        width = eyeW * 0.95f,
        height = eyeH * 1.15f * flameFlicker,
        strokeWidth = strokeWidth * 0.9f,
        inkColor = inkColor
    )

    // Right Eye Flames
    drawEyeFlameTongues(
        centerX = rightEyeLeft + eyeW * 0.5f,
        bottomY = eyeTop + eyeH * 0.85f,
        width = eyeW * 0.95f,
        height = eyeH * 1.15f * flameFlicker,
        strokeWidth = strokeWidth * 0.9f,
        inkColor = inkColor
    )

    // 3. Screaming vertical open oval mouth (:O)
    val mouthCenter = Offset(w * 0.50f, h * 0.77f)
    val mouthW = w * 0.14f
    val mouthH = h * 0.18f

    // Outer dark mouth cavity
    drawOval(
        color = inkColor,
        topLeft = Offset(mouthCenter.x - mouthW * 0.5f, mouthCenter.y - mouthH * 0.5f),
        size = Size(mouthW, mouthH)
    )

    // Inner reddish throat highlight
    drawOval(
        color = Color(0xFFEF4444),
        topLeft = Offset(mouthCenter.x - mouthW * 0.35f, mouthCenter.y),
        size = Size(mouthW * 0.7f, mouthH * 0.42f)
    )

    // Solid white outline rim for scream mouth
    drawOval(
        color = featureLineColor,
        topLeft = Offset(mouthCenter.x - mouthW * 0.5f, mouthCenter.y - mouthH * 0.5f),
        size = Size(mouthW, mouthH),
        style = Stroke(width = strokeWidth * 1.35f)
    )
}

/**
 * State 3: "Almost there" (Flat side)
 * Wide enthusiastic cartoon eyes looking forward-right, huge open toothy crescent grin.
 */
private fun DrawScope.drawAlmostThereFlatFace(
    w: Float,
    h: Float,
    strokeWidth: Float,
    inkColor: Color,
    scleraColor: Color,
    highlightColor: Color,
    featureLineColor: Color
) {
    // 1. High arched eyebrows radiating excitement in solid line
    val browY = h * 0.12f
    drawArch(
        start = Offset(w * 0.20f, browY + h * 0.04f),
        control = Offset(w * 0.32f, browY - h * 0.04f),
        end = Offset(w * 0.44f, browY + h * 0.03f),
        color = featureLineColor,
        strokeWidth = strokeWidth * 1.35f
    )
    drawArch(
        start = Offset(w * 0.56f, browY + h * 0.03f),
        control = Offset(w * 0.68f, browY - h * 0.04f),
        end = Offset(w * 0.80f, browY + h * 0.04f),
        color = featureLineColor,
        strokeWidth = strokeWidth * 1.35f
    )

    // 2. Wide eager eyes
    val eyeW = w * 0.27f
    val eyeH = h * 0.44f
    val eyeTop = h * 0.20f
    val leftEyeLeft = w * 0.21f
    val rightEyeLeft = w * 0.52f

    drawStadiumEye(leftEyeLeft, eyeTop, eyeW, eyeH, strokeWidth, inkColor, scleraColor)
    drawStadiumEye(rightEyeLeft, eyeTop, eyeW, eyeH, strokeWidth, inkColor, scleraColor)

    // Pupils looking forward-right in eager anticipation
    val pupilW = eyeW * 0.48f
    val pupilH = eyeH * 0.50f
    val pupilY = eyeTop + eyeH * 0.28f

    drawPieCutPupil(
        center = Offset(leftEyeLeft + eyeW * 0.60f, pupilY + pupilH * 0.5f),
        radius = pupilW * 0.48f,
        inkColor = inkColor,
        highlightColor = highlightColor,
        notchAngle = -30f
    )
    drawPieCutPupil(
        center = Offset(rightEyeLeft + eyeW * 0.60f, pupilY + pupilH * 0.5f),
        radius = pupilW * 0.48f,
        inkColor = inkColor,
        highlightColor = highlightColor,
        notchAngle = -30f
    )

    // 3. Cute nose loop in solid line
    drawArch(
        start = Offset(w * 0.46f, h * 0.66f),
        control = Offset(w * 0.50f, h * 0.70f),
        end = Offset(w * 0.54f, h * 0.66f),
        color = featureLineColor,
        strokeWidth = strokeWidth * 1.0f
    )

    // 4. Huge wide toothy happy crescent grin (:D) with teeth and tongue framed in solid white line!
    val mouthPath = Path().apply {
        moveTo(w * 0.26f, h * 0.72f)
        quadraticTo(w * 0.50f, h * 0.70f, w * 0.74f, h * 0.72f)
        cubicTo(w * 0.74f, h * 0.95f, w * 0.26f, h * 0.95f, w * 0.26f, h * 0.72f)
        close()
    }
    // Mouth cavity fill
    drawPath(mouthPath, inkColor, style = Fill)

    // Teeth row (white band at top)
    val teethPath = Path().apply {
        moveTo(w * 0.28f, h * 0.72f)
        quadraticTo(w * 0.50f, h * 0.71f, w * 0.72f, h * 0.72f)
        lineTo(w * 0.71f, h * 0.79f)
        quadraticTo(w * 0.50f, h * 0.79f, w * 0.29f, h * 0.79f)
        close()
    }
    drawPath(teethPath, Color.White, style = Fill)
    // Teeth vertical dividers
    for (i in 1..3) {
        val toothX = w * (0.28f + i * 0.11f)
        drawLine(inkColor, Offset(toothX, h * 0.72f), Offset(toothX, h * 0.79f), strokeWidth * 0.6f)
    }

    // Tongue (pink mound at bottom of mouth)
    drawPath(
        Path().apply {
            moveTo(w * 0.36f, h * 0.86f)
            quadraticTo(w * 0.50f, h * 0.80f, w * 0.64f, h * 0.86f)
            cubicTo(w * 0.60f, h * 0.93f, w * 0.40f, h * 0.93f, w * 0.36f, h * 0.86f)
            close()
        },
        Color(0xFFF43F5E),
        style = Fill
    )

    // Mouth outer border in solid white line
    drawPathSolid(mouthPath, featureLineColor, strokeWidth * 1.35f)
}

/**
 * State 4: "Perfect! In Tune" (Center)
 * Glowing LIGHTNING BOLTS inside both pupils, confident dimpled rockstar grin!
 */
private fun DrawScope.drawPerfectFace(
    w: Float,
    h: Float,
    strokeWidth: Float,
    inkColor: Color,
    scleraColor: Color,
    lightningPulse: Float,
    featureLineColor: Color,
    isAllStringsTuned: Boolean
) {
    // 1. Confident proud arched brows in solid line
    val browY = h * 0.12f
    drawArch(
        start = Offset(w * 0.21f, browY + h * 0.04f),
        control = Offset(w * 0.33f, browY - h * 0.04f),
        end = Offset(w * 0.44f, browY + h * 0.02f),
        color = featureLineColor,
        strokeWidth = strokeWidth * 1.35f
    )
    drawArch(
        start = Offset(w * 0.56f, browY + h * 0.02f),
        control = Offset(w * 0.67f, browY - h * 0.04f),
        end = Offset(w * 0.79f, browY + h * 0.04f),
        color = featureLineColor,
        strokeWidth = strokeWidth * 1.35f
    )

    // 2. Large energetic cartoon eyes
    val eyeW = w * 0.27f
    val eyeH = h * 0.45f
    val eyeTop = h * 0.20f
    val leftEyeLeft = w * 0.21f
    val rightEyeLeft = w * 0.52f

    drawStadiumEye(leftEyeLeft, eyeTop, eyeW, eyeH, strokeWidth, inkColor, scleraColor)
    drawStadiumEye(rightEyeLeft, eyeTop, eyeW, eyeH, strokeWidth, inkColor, scleraColor)

    // 3. LIGHTNING BOLTS inside both eyes (Rock & Roll electric power!)
    val boltColor = if (isAllStringsTuned) Color(0xFFC8FF3D) else Color(0xFFFDE047)
    drawLightningBolt(
        center = Offset(leftEyeLeft + eyeW * 0.50f, eyeTop + eyeH * 0.50f),
        width = eyeW * 0.55f * lightningPulse,
        height = eyeH * 0.72f * lightningPulse,
        color = boltColor,
        outlineColor = inkColor,
        strokeWidth = strokeWidth * 0.8f
    )
    drawLightningBolt(
        center = Offset(rightEyeLeft + eyeW * 0.50f, eyeTop + eyeH * 0.50f),
        width = eyeW * 0.55f * lightningPulse,
        height = eyeH * 0.72f * lightningPulse,
        color = boltColor,
        outlineColor = inkColor,
        strokeWidth = strokeWidth * 0.8f
    )

    // 4. Perky upward curved nose loop in solid line
    drawArch(
        start = Offset(w * 0.47f, h * 0.65f),
        control = Offset(w * 0.50f, h * 0.69f),
        end = Offset(w * 0.53f, h * 0.65f),
        color = featureLineColor,
        strokeWidth = strokeWidth * 1.0f
    )

    // 5. Confident broad rockstar grin with sharp cheek dimples in solid white line
    val mouthPath = Path().apply {
        moveTo(w * 0.28f, h * 0.73f)
        quadraticTo(w * 0.50f, h * 0.89f, w * 0.72f, h * 0.73f)
    }
    drawPathSolid(mouthPath, featureLineColor, strokeWidth * 1.4f)

    // Sharp left dimple tick
    drawPathSolid(
        Path().apply {
            moveTo(w * 0.25f, h * 0.70f)
            lineTo(w * 0.30f, h * 0.75f)
        },
        featureLineColor,
        strokeWidth * 1.2f
    )
    // Sharp right dimple tick
    drawPathSolid(
        Path().apply {
            moveTo(w * 0.75f, h * 0.70f)
            lineTo(w * 0.70f, h * 0.75f)
        },
        featureLineColor,
        strokeWidth * 1.2f
    )
}

/**
 * State 5: "Almost there" (Sharp side)
 * Side-glance eyes looking leftward, comic sweat droplets flying off.
 */
private fun DrawScope.drawAlmostThereSharpFace(
    w: Float,
    h: Float,
    strokeWidth: Float,
    inkColor: Color,
    scleraColor: Color,
    highlightColor: Color,
    sweatBounce: Float,
    featureLineColor: Color
) {
    // 1. Tense brows in concentration in solid line
    val browY = h * 0.13f
    drawArch(
        start = Offset(w * 0.22f, browY + h * 0.04f),
        control = Offset(w * 0.34f, browY - h * 0.03f),
        end = Offset(w * 0.45f, browY + h * 0.03f),
        color = featureLineColor,
        strokeWidth = strokeWidth * 1.35f
    )
    drawArch(
        start = Offset(w * 0.57f, browY + h * 0.02f),
        control = Offset(w * 0.68f, browY - h * 0.04f),
        end = Offset(w * 0.79f, browY + h * 0.03f),
        color = featureLineColor,
        strokeWidth = strokeWidth * 1.35f
    )

    // 2. Eyes (Looking left back towards center)
    val eyeW = w * 0.26f
    val eyeH = h * 0.44f
    val eyeTop = h * 0.21f
    val leftEyeLeft = w * 0.21f
    val rightEyeLeft = w * 0.53f

    drawStadiumEye(leftEyeLeft, eyeTop, eyeW, eyeH, strokeWidth, inkColor, scleraColor)
    drawStadiumEye(rightEyeLeft, eyeTop, eyeW, eyeH, strokeWidth, inkColor, scleraColor)

    // Pupils looking left
    val pupilW = eyeW * 0.46f
    val pupilH = eyeH * 0.48f
    val pupilY = eyeTop + eyeH * 0.30f

    drawPieCutPupil(
        center = Offset(leftEyeLeft + eyeW * 0.38f, pupilY + pupilH * 0.5f),
        radius = pupilW * 0.46f,
        inkColor = inkColor,
        highlightColor = highlightColor,
        notchAngle = -135f
    )
    drawPieCutPupil(
        center = Offset(rightEyeLeft + eyeW * 0.38f, pupilY + pupilH * 0.5f),
        radius = pupilW * 0.46f,
        inkColor = inkColor,
        highlightColor = highlightColor,
        notchAngle = -135f
    )

    // 3. Comic Sweat Drops flying off the side (phew, almost there!)
    drawSweatDrop(
        center = Offset(w * 0.86f, h * 0.46f + sweatBounce),
        size = w * 0.11f,
        color = Color(0xFF38BDF8),
        outlineColor = inkColor,
        strokeWidth = strokeWidth * 0.7f
    )
    drawSweatDrop(
        center = Offset(w * 0.90f, h * 0.60f + sweatBounce * 0.7f),
        size = w * 0.08f,
        color = Color(0xFF38BDF8),
        outlineColor = inkColor,
        strokeWidth = strokeWidth * 0.6f
    )

    // 4. Nose loop in solid line
    drawArch(
        start = Offset(w * 0.48f, h * 0.66f),
        control = Offset(w * 0.50f, h * 0.70f),
        end = Offset(w * 0.52f, h * 0.66f),
        color = featureLineColor,
        strokeWidth = strokeWidth * 1.0f
    )

    // 5. Focused / tense mouth in solid line
    val mouthPath = Path().apply {
        moveTo(w * 0.36f, h * 0.77f)
        quadraticTo(w * 0.50f, h * 0.85f, w * 0.64f, h * 0.77f)
    }
    drawPathSolid(mouthPath, featureLineColor, strokeWidth * 1.35f)
}

/**
 * State 6: "Sharp - Tune down" (Sharp)
 * Dizzy wavy vibrating eyes, twinkling stars/sparkles, squiggly trembling mouth.
 */
private fun DrawScope.drawSharpFace(
    w: Float,
    h: Float,
    strokeWidth: Float,
    inkColor: Color,
    scleraColor: Color,
    starPulse: Float,
    starRotation: Float,
    featureLineColor: Color
) {
    // 1. Distressed, wavy furrowed eyebrows angled inward in solid line
    val browY = h * 0.15f
    drawPathSolid(
        Path().apply {
            moveTo(w * 0.20f, browY + h * 0.03f)
            cubicTo(w * 0.27f, browY - h * 0.02f, w * 0.33f, browY + h * 0.04f, w * 0.44f, browY + h * 0.06f)
        },
        featureLineColor,
        strokeWidth * 1.35f
    )
    drawPathSolid(
        Path().apply {
            moveTo(w * 0.80f, browY + h * 0.03f)
            cubicTo(w * 0.73f, browY - h * 0.02f, w * 0.67f, browY + h * 0.04f, w * 0.56f, browY + h * 0.06f)
        },
        featureLineColor,
        strokeWidth * 1.35f
    )

    // 2. Eyes with dizzy vibrating wavy lines
    val eyeW = w * 0.27f
    val eyeH = h * 0.44f
    val eyeTop = h * 0.23f
    val leftEyeLeft = w * 0.21f
    val rightEyeLeft = w * 0.52f

    drawStadiumEye(leftEyeLeft, eyeTop, eyeW, eyeH, strokeWidth, inkColor, scleraColor)
    drawStadiumEye(rightEyeLeft, eyeTop, eyeW, eyeH, strokeWidth, inkColor, scleraColor)

    // Dizzy wavy horizontal lines across left eye
    for (i in 1..4) {
        val lineY = eyeTop + eyeH * (0.16f + i * 0.16f)
        val wavePath = Path().apply {
            moveTo(leftEyeLeft + eyeW * 0.10f, lineY)
            quadraticTo(leftEyeLeft + eyeW * 0.35f, lineY - h * 0.025f, leftEyeLeft + eyeW * 0.55f, lineY)
            quadraticTo(leftEyeLeft + eyeW * 0.75f, lineY + h * 0.025f, leftEyeLeft + eyeW * 0.90f, lineY)
        }
        drawPath(wavePath, inkColor, style = Stroke(width = strokeWidth * 0.85f, cap = StrokeCap.Round))
    }

    // Dizzy wavy lines across right eye
    for (i in 1..4) {
        val lineY = eyeTop + eyeH * (0.16f + i * 0.16f)
        val wavePath = Path().apply {
            moveTo(rightEyeLeft + eyeW * 0.10f, lineY)
            quadraticTo(rightEyeLeft + eyeW * 0.35f, lineY + h * 0.025f, rightEyeLeft + eyeW * 0.55f, lineY)
            quadraticTo(rightEyeLeft + eyeW * 0.75f, lineY - h * 0.025f, rightEyeLeft + eyeW * 0.90f, lineY)
        }
        drawPath(wavePath, inkColor, style = Stroke(width = strokeWidth * 0.85f, cap = StrokeCap.Round))
    }

    // 3. Twinkling Stars / Sparkles floating above the head (dizzy high tension!)
    drawComicStar(
        center = Offset(w * 0.15f, h * 0.12f),
        radius = w * 0.06f * starPulse,
        color = Color(0xFFFACC15),
        outlineColor = inkColor,
        strokeWidth = strokeWidth * 0.6f
    )
    drawComicStar(
        center = Offset(w * 0.50f, h * 0.08f),
        radius = w * 0.07f * (2f - starPulse),
        color = Color(0xFFFDE047),
        outlineColor = inkColor,
        strokeWidth = strokeWidth * 0.6f
    )
    drawComicStar(
        center = Offset(w * 0.85f, h * 0.12f),
        radius = w * 0.06f * starPulse,
        color = Color(0xFFFACC15),
        outlineColor = inkColor,
        strokeWidth = strokeWidth * 0.6f
    )

    // 4. Distressed nose in solid line
    drawArch(
        start = Offset(w * 0.48f, h * 0.68f),
        control = Offset(w * 0.51f, h * 0.72f),
        end = Offset(w * 0.54f, h * 0.68f),
        color = featureLineColor,
        strokeWidth = strokeWidth * 1.0f
    )

    // 5. Squiggly trembling mouth in solid line (〰️)
    val mouthY = h * 0.79f
    val mouthPath = Path().apply {
        moveTo(w * 0.34f, mouthY)
        cubicTo(w * 0.40f, mouthY - h * 0.035f, w * 0.44f, mouthY + h * 0.035f, w * 0.50f, mouthY)
        cubicTo(w * 0.56f, mouthY - h * 0.035f, w * 0.60f, mouthY + h * 0.035f, w * 0.66f, mouthY)
    }
    drawPathSolid(mouthPath, featureLineColor, strokeWidth * 1.35f)
}

/**
 * State 7: "RIP" (Extreme Sharp / +45¢)
 * Redrawn based on user reference:
 * - Two outward-tilted oval eyes with bold X's inside
 * - Inverted arch cute button nose in center
 * - Deep cartoon frown with downturned corner accent marks
 * - Cohesive with rubber-hose / ink cartoon style of existing faces
 */
private fun DrawScope.drawRipDeadFace(
    w: Float,
    h: Float,
    strokeWidth: Float,
    inkColor: Color,
    scleraColor: Color,
    shudderWobble: Float,
    featureLineColor: Color,
    isDark: Boolean = true
) {
    // 1. Expressive Eyebrows lifted off eyeballs and angled in sad droop (/ \)
    val browY = h * 0.07f
    drawArch(
        start = Offset(w * 0.20f, browY + h * 0.075f),
        control = Offset(w * 0.28f, browY + h * 0.010f),
        end = Offset(w * 0.38f, browY + h * 0.025f),
        color = featureLineColor,
        strokeWidth = strokeWidth * 1.35f
    )
    drawArch(
        start = Offset(w * 0.62f, browY + h * 0.025f),
        control = Offset(w * 0.72f, browY + h * 0.010f),
        end = Offset(w * 0.80f, browY + h * 0.075f),
        color = featureLineColor,
        strokeWidth = strokeWidth * 1.35f
    )

    // 2. Tilted Oval Eyes with Bold X Marks
    // Left eye tilts outward to top-left (-14°), Right eye tilts outward to top-right (+14°)
    val eyeW = w * 0.26f
    val eyeH = h * 0.38f
    val eyeCenterY = h * 0.36f
    val leftEyeCenter = Offset(w * 0.31f + shudderWobble * 0.3f, eyeCenterY)
    val rightEyeCenter = Offset(w * 0.69f + shudderWobble * 0.3f, eyeCenterY)

    // Helper to draw tilted oval eye with bold X inside
    fun drawTiltedXEye(center: Offset, angle: Float) {
        withTransform({
            rotate(degrees = angle, pivot = center)
        }) {
            val ovalTopLeft = Offset(center.x - eyeW * 0.5f, center.y - eyeH * 0.5f)
            val ovalSize = Size(eyeW, eyeH)

            // Solid sclera fill (bright white eyeball)
            drawOval(
                color = scleraColor,
                topLeft = ovalTopLeft,
                size = ovalSize,
                style = Fill
            )

            // Outer contour stroke framing the eye
            drawOval(
                color = if (isDark) Color(0xFFF1F5F9) else inkColor,
                topLeft = ovalTopLeft,
                size = ovalSize,
                style = Stroke(width = strokeWidth * 0.95f)
            )

            // Bold X inside the eyeball
            val xSpanW = eyeW * 0.33f
            val xSpanH = eyeH * 0.28f
            val xStroke = strokeWidth * 1.55f

            // Stroke 1 (\)
            drawLine(
                color = inkColor,
                start = Offset(center.x - xSpanW, center.y - xSpanH),
                end = Offset(center.x + xSpanW, center.y + xSpanH),
                strokeWidth = xStroke,
                cap = StrokeCap.Round
            )
            // Stroke 2 (/)
            drawLine(
                color = inkColor,
                start = Offset(center.x + xSpanW, center.y - xSpanH),
                end = Offset(center.x - xSpanW, center.y + xSpanH),
                strokeWidth = xStroke,
                cap = StrokeCap.Round
            )
        }
    }

    drawTiltedXEye(leftEyeCenter, -14f)
    drawTiltedXEye(rightEyeCenter, 14f)

    // 3. Center Inverted-U Nose between and below eyes
    val nosePath = Path().apply {
        moveTo(w * 0.445f, h * 0.545f)
        cubicTo(
            w * 0.445f, h * 0.490f,
            w * 0.555f, h * 0.490f,
            w * 0.555f, h * 0.545f
        )
    }
    drawPath(
        path = nosePath,
        color = featureLineColor,
        style = Stroke(
            width = strokeWidth * 1.35f,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )

    // 4. Red cartoon tongue hanging out of the left side of the mouth
    val tongueTipX = w * 0.435f + shudderWobble * 0.35f
    val tongueTipY = h * 0.84f
    val tonguePath = Path().apply {
        moveTo(w * 0.37f, h * 0.70f)
        cubicTo(
            w * 0.35f, h * 0.76f,
            tongueTipX - w * 0.055f, tongueTipY,
            tongueTipX, tongueTipY
        )
        cubicTo(
            tongueTipX + w * 0.055f, tongueTipY,
            w * 0.51f, h * 0.75f,
            w * 0.50f, h * 0.665f
        )
        close()
    }

    // Rich cartoon red tongue fill
    drawPath(tonguePath, Color(0xFFDC2626), style = Fill)

    // Tongue center crease line
    drawLine(
        color = Color(0xFF881337),
        start = Offset(tongueTipX, h * 0.72f),
        end = Offset(tongueTipX, h * 0.805f),
        strokeWidth = strokeWidth * 0.85f,
        cap = StrokeCap.Round
    )

    // Tongue outline
    drawPath(
        path = tonguePath,
        color = featureLineColor,
        style = Stroke(
            width = strokeWidth * 1.3f,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )

    // 5. Sad Cartoon Frown Mouth
    // Main frown arch drawn over the tongue root
    val mouthArch = Path().apply {
        moveTo(w * 0.28f, h * 0.73f)
        cubicTo(
            w * 0.36f, h * 0.655f,
            w * 0.64f, h * 0.655f,
            w * 0.72f, h * 0.73f
        )
    }
    drawPath(
        path = mouthArch,
        color = featureLineColor,
        style = Stroke(
            width = strokeWidth * 1.4f,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )
}

// =========================================================================================
// HELPER DRAWING PRIMITIVES
// =========================================================================================

/**
 * Draw a classic tall capsule (stadium) eyeball with solid sclera and bold black contour.
 */
private fun DrawScope.drawStadiumEye(
    left: Float,
    top: Float,
    width: Float,
    height: Float,
    strokeWidth: Float,
    inkColor: Color,
    scleraColor: Color
) {
    val cornerRadius = CornerRadius(width * 0.5f, width * 0.5f)

    // Solid sclera fill
    drawRoundRect(
        color = scleraColor,
        topLeft = Offset(left, top),
        size = Size(width, height),
        cornerRadius = cornerRadius,
        style = Fill
    )

    // Bold black outline
    drawRoundRect(
        color = inkColor,
        topLeft = Offset(left, top),
        size = Size(width, height),
        cornerRadius = cornerRadius,
        style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )
}

/**
 * Draw a classic 1930s pie-cut cartoon pupil (black circle with a triangular slice cutout).
 */
private fun DrawScope.drawPieCutPupil(
    center: Offset,
    radius: Float,
    inkColor: Color,
    highlightColor: Color,
    notchAngle: Float
) {
    // Black pupil
    drawCircle(
        color = inkColor,
        radius = radius,
        center = center,
        style = Fill
    )

    // Specular highlight notch (white wedge cutout or dot)
    val notchRad = Math.toRadians(notchAngle.toDouble())
    val notchX = center.x + (radius * 0.55f * Math.cos(notchRad)).toFloat()
    val notchY = center.y + (radius * 0.55f * Math.sin(notchRad)).toFloat()

    drawCircle(
        color = highlightColor,
        radius = radius * 0.28f,
        center = Offset(notchX, notchY),
        style = Fill
    )
}

/**
 * Draw arched line (eyebrow, nose curve) with solid line and round cap.
 */
private fun DrawScope.drawArch(
    start: Offset,
    control: Offset,
    end: Offset,
    color: Color,
    strokeWidth: Float
) {
    val path = Path().apply {
        moveTo(start.x, start.y)
        quadraticTo(control.x, control.y, end.x, end.y)
    }
    drawPath(
        path = path,
        color = color,
        style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )
}

/**
 * Draw a path as a solid crisp line with round cap and round join.
 */
private fun DrawScope.drawPathSolid(
    path: Path,
    color: Color,
    strokeWidth: Float,
    cap: StrokeCap = StrokeCap.Round,
    join: StrokeJoin = StrokeJoin.Round
) {
    drawPath(
        path = path,
        color = color,
        style = Stroke(width = strokeWidth, cap = cap, join = join)
    )
}

/**
 * Draw a comic mushroom cloud / nuclear explosion.
 */
private fun DrawScope.drawMushroomCloud(
    left: Float,
    top: Float,
    width: Float,
    height: Float,
    strokeWidth: Float,
    inkColor: Color,
    scale: Float
) {
    val cx = left + width * 0.5f
    val capY = top + height * 0.35f * scale
    val capW = width * 0.88f * scale
    val capH = height * 0.44f * scale

    // Billowing smoke cap (white fill with cloud lobes)
    val capPath = Path().apply {
        moveTo(cx - capW * 0.45f, capY)
        cubicTo(cx - capW * 0.6f, capY - capH * 0.6f, cx - capW * 0.2f, capY - capH * 0.9f, cx, capY - capH * 0.85f)
        cubicTo(cx + capW * 0.2f, capY - capH * 0.9f, cx + capW * 0.6f, capY - capH * 0.6f, cx + capW * 0.45f, capY)
        cubicTo(cx + capW * 0.3f, capY + capH * 0.2f, cx - capW * 0.3f, capY + capH * 0.2f, cx - capW * 0.45f, capY)
        close()
    }
    drawPath(capPath, Color.White, style = Fill)
    drawPath(capPath, inkColor, style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))

    // Smoke stem pillar
    val stemPath = Path().apply {
        moveTo(cx - width * 0.12f, capY)
        lineTo(cx - width * 0.18f, top + height * 0.82f)
        lineTo(cx + width * 0.18f, top + height * 0.82f)
        lineTo(cx + width * 0.12f, capY)
        close()
    }
    drawPath(stemPath, Color(0xFFE2E8F0), style = Fill)
    drawPath(stemPath, inkColor, style = Stroke(width = strokeWidth * 0.8f, cap = StrokeCap.Round))

    // Base dust ring
    val baseOval = Path().apply {
        addOval(
            androidx.compose.ui.geometry.Rect(
                cx - width * 0.35f,
                top + height * 0.78f,
                cx + width * 0.35f,
                top + height * 0.96f
            )
        )
    }
    drawPath(baseOval, Color.White, style = Fill)
    drawPath(baseOval, inkColor, style = Stroke(width = strokeWidth * 0.8f))
}

/**
 * Draw raging flame tongues licking upward from an eye socket.
 */
private fun DrawScope.drawEyeFlameTongues(
    centerX: Float,
    bottomY: Float,
    width: Float,
    height: Float,
    strokeWidth: Float,
    inkColor: Color
) {
    val flamePath = Path().apply {
        moveTo(centerX - width * 0.45f, bottomY)
        // Left tongue
        cubicTo(
            centerX - width * 0.55f, bottomY - height * 0.45f,
            centerX - width * 0.40f, bottomY - height * 0.85f,
            centerX - width * 0.25f, bottomY - height * 0.70f
        )
        // Tall center tongue
        cubicTo(
            centerX - width * 0.15f, bottomY - height * 0.95f,
            centerX + width * 0.05f, bottomY - height * 1.05f,
            centerX + width * 0.12f, bottomY - height * 0.80f
        )
        // Right tongue
        cubicTo(
            centerX + width * 0.30f, bottomY - height * 0.90f,
            centerX + width * 0.50f, bottomY - height * 0.55f,
            centerX + width * 0.45f, bottomY
        )
        close()
    }

    // Outer vivid orange/red flame fill
    drawPath(flamePath, Color(0xFFEF4444), style = Fill)
    // Black outline
    drawPath(flamePath, inkColor, style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))

    // Inner bright yellow flame core
    val innerCorePath = Path().apply {
        moveTo(centerX - width * 0.25f, bottomY)
        cubicTo(
            centerX - width * 0.25f, bottomY - height * 0.40f,
            centerX, bottomY - height * 0.65f,
            centerX + width * 0.05f, bottomY - height * 0.50f
        )
        cubicTo(
            centerX + width * 0.20f, bottomY - height * 0.55f,
            centerX + width * 0.25f, bottomY - height * 0.35f,
            centerX + width * 0.25f, bottomY
        )
        close()
    }
    drawPath(innerCorePath, Color(0xFFFDE047), style = Fill)
}

/**
 * Draw sharp rockstar lightning bolt.
 */
private fun DrawScope.drawLightningBolt(
    center: Offset,
    width: Float,
    height: Float,
    color: Color,
    outlineColor: Color,
    strokeWidth: Float
) {
    val cx = center.x
    val cy = center.y
    val hw = width * 0.5f
    val hh = height * 0.5f

    val boltPath = Path().apply {
        moveTo(cx + hw * 0.2f, cy - hh)
        lineTo(cx - hw, cy + hh * 0.05f)
        lineTo(cx - hw * 0.1f, cy + hh * 0.05f)
        lineTo(cx - hw * 0.3f, cy + hh)
        lineTo(cx + hw, cy - hh * 0.05f)
        lineTo(cx + hw * 0.1f, cy - hh * 0.05f)
        close()
    }

    // Solid vibrant yellow fill
    drawPath(boltPath, color, style = Fill)
    // Black comic outline
    drawPath(boltPath, outlineColor, style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))
}

/**
 * Draw a comic teardrop sweat droplet.
 */
private fun DrawScope.drawSweatDrop(
    center: Offset,
    size: Float,
    color: Color,
    outlineColor: Color,
    strokeWidth: Float
) {
    val dropPath = Path().apply {
        moveTo(center.x, center.y - size * 0.7f)
        cubicTo(
            center.x + size * 0.6f, center.y - size * 0.1f,
            center.x + size * 0.5f, center.y + size * 0.6f,
            center.x, center.y + size * 0.6f
        )
        cubicTo(
            center.x - size * 0.5f, center.y + size * 0.6f,
            center.x - size * 0.6f, center.y - size * 0.1f,
            center.x, center.y - size * 0.7f
        )
        close()
    }
    drawPath(dropPath, color, style = Fill)
    drawPath(dropPath, outlineColor, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))

    // Tiny highlight dot
    drawCircle(Color.White, size * 0.14f, Offset(center.x - size * 0.16f, center.y + size * 0.15f))
}

/**
 * Draw a 4-point comic sparkle / star.
 */
private fun DrawScope.drawComicStar(
    center: Offset,
    radius: Float,
    color: Color,
    outlineColor: Color,
    strokeWidth: Float
) {
    val starPath = Path().apply {
        moveTo(center.x, center.y - radius)
        quadraticTo(center.x, center.y, center.x + radius, center.y)
        quadraticTo(center.x, center.y, center.x, center.y + radius)
        quadraticTo(center.x, center.y, center.x - radius, center.y)
        quadraticTo(center.x, center.y, center.x, center.y - radius)
        close()
    }
    drawPath(starPath, color, style = Fill)
    drawPath(starPath, outlineColor, style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))
}

/**
 * Draw a bold cartoon X eye for the dead face.
 */
private fun DrawScope.drawCartoonXEye(
    center: Offset,
    size: Float,
    color: Color,
    strokeWidth: Float
) {
    val hs = size * 0.5f
    val path1 = Path().apply {
        moveTo(center.x - hs, center.y - hs)
        lineTo(center.x + hs, center.y + hs)
    }
    val path2 = Path().apply {
        moveTo(center.x + hs, center.y - hs)
        lineTo(center.x - hs, center.y + hs)
    }

    drawPathSolid(path1, color, strokeWidth)
    drawPathSolid(path2, color, strokeWidth)
}
