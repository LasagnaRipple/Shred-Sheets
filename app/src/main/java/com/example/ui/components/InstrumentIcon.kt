package com.example.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.example.model.InstrumentType

/**
 * ColorMatrix transforming the standard red guitar emoji (🎸) into an electric blue Bass guitar.
 *
 * Swaps and tunes color channels:
 * - Red guitar body (high R, low G/B) becomes vibrant electric blue / cyan.
 * - Hardware and neck maintain cool metallic highlights.
 */
val BassBlueColorMatrix = ColorMatrix(
    floatArrayOf(
        0.0f, 0.0f, 0.7f, 0f, 0f,   // Red channel
        0.35f, 0.4f, 0.2f, 0f, 0f,   // Green channel (adds punchy electric teal/cyan tone)
        1.15f, 0.0f, 0.2f, 0f, 0f,   // Blue channel (high gain from red body)
        0.0f, 0.0f, 0.0f, 1f, 0f    // Alpha
    )
)

/**
 * Renders an instrument icon.
 * - Guitar: standard 🎸 (red)
 * - Bass: blue-tinted 🎸 (electric blue)
 * - Ukulele: 🌴 (palm tree)
 * - Banjo: 🪕 (banjo)
 */
@Composable
fun InstrumentIcon(
    instrumentType: InstrumentType,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 20.sp
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        if (instrumentType == InstrumentType.BASS) {
            Text(
                text = "🎸",
                fontSize = fontSize,
                modifier = Modifier.drawWithContent {
                    drawIntoCanvas { canvas ->
                        val paint = Paint().apply {
                            colorFilter = ColorFilter.colorMatrix(BassBlueColorMatrix)
                        }
                        canvas.saveLayer(Rect(0f, 0f, size.width, size.height), paint)
                        drawContent()
                        canvas.restore()
                    }
                }
            )
        } else {
            Text(
                text = instrumentType.iconName,
                fontSize = fontSize
            )
        }
    }
}
