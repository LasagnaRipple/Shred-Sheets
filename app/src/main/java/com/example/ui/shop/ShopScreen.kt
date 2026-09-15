package com.example.ui.shop

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.NightlightRound
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.ui.geometry.Offset
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import com.example.R
import com.example.ui.theme.LocalIsDarkTheme
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

/**
 * Product variant data model for the 6 Shred Sheets editions.
 */
data class ShredSheetsProduct(
    val id: Int,
    val title: String,
    val instrument: String,
    val edition: String,
    val subtitle: String,
    val badge: String,
    val badgeColor: Color,
    val price: String,
    val description: String,
    val specs: List<String>,
    val amazonUrl: String
)

val SHRED_SHEETS_CATALOG = listOf(
    ShredSheetsProduct(
        id = 1,
        title = "Guitar - Rocker",
        instrument = "Guitar",
        edition = "Rocker",
        subtitle = "High-Voltage Blank Tabs for Riffs & Solos",
        badge = "ELECTRIC ROCK",
        badgeColor = Color(0xFFEF4444),
        price = "$9.99 USD",
        description = "Engineered for electric guitarists tearing up stages, garages, and studios. High-clarity 6-string tablature staves paired with chord chart boxes and dedicated solo lick sections.",
        specs = listOf(
            "Standard 6-String Guitar Staves + Chord Grids",
            "120 Acid-Free Bleed-Proof Heavyweight Pages",
            "Lay-Flat Double Wire-O Spiral Binding",
            "Pentatonic & Power Chord Quick Reference"
        ),
        amazonUrl = "https://www.amazon.com/s?k=shred+sheets+guitar+rocker"
    ),
    ShredSheetsProduct(
        id = 2,
        title = "Guitar - Songwriter",
        instrument = "Guitar",
        edition = "Songwriter",
        subtitle = "Lyrics, Progressions & Guitar Staves Side-by-Side",
        badge = "ACOUSTIC & LYRICS",
        badgeColor = Color(0xFFF59E0B),
        price = "$9.99 USD",
        description = "The premier acoustic and songwriting journal. Facing dual-page spreads dedicate the left page to lyrics, song structure, and chord progressions, while the right page provides 6-string guitar tablature.",
        specs = listOf(
            "Facing-Page Lyric Sheet & Guitar Tab Layout",
            "Song Structure Prompts (Verse, Chorus, Bridge)",
            "Chord Progression & Voicing Reference Boxes",
            "140 Perforated Cream Pages with Pocket Folder"
        ),
        amazonUrl = "https://www.amazon.com/s?k=shred+sheets+guitar+songwriter"
    ),
    ShredSheetsProduct(
        id = 3,
        title = "Guitar - Midnighter",
        instrument = "Guitar",
        edition = "Midnighter",
        subtitle = "Stealth Matte Obsidian for Late-Night Shred",
        badge = "MIDNIGHT STEALTH",
        badgeColor = Color(0xFF06B6D4),
        price = "$9.99 USD",
        description = "Tuned for nocturnal shredders, bedroom producers, and progressive metalists. Sleek stealth matte black aesthetic with ultra-high contrast crisp white staves that stay visible under dim ambient studio lighting.",
        specs = listOf(
            "Stealth Matte Obsidian Heavy Cardstock Cover",
            "High-Contrast White Tab Grids for Dim Light",
            "120gsm Anti-Bleed Dark-Edge Paper Stock",
            "Lay-Flat Wire-O Binding for 360° Desktop Use"
        ),
        amazonUrl = "https://www.amazon.com/s?k=shred+sheets+guitar+midnighter"
    ),
    ShredSheetsProduct(
        id = 4,
        title = "Bass - Rocker",
        instrument = "Bass",
        edition = "Rocker",
        subtitle = "Heavy Low-End Riffs, Slap & Walking Grooves",
        badge = "HEAVY GROOVE",
        badgeColor = Color(0xFF8B5CF6),
        price = "$9.99 USD",
        description = "Calibrated specifically for bassists driving the rhythm section. Features generous 4-string and 5-string bass tablature staves with expanded vertical spacing tailored for rapid slap bass, pop lines, and walking grooves.",
        specs = listOf(
            "4-String & 5-String Bass Staves with Extra Spacing",
            "Fretboard Map & Key Root Note Quick Reference",
            "120 Acid-Free Heavyweight Bleed-Proof Pages",
            "Durable Crush-Resistant Double Wire-O Binding"
        ),
        amazonUrl = "https://www.amazon.com/s?k=shred+sheets+bass+rocker"
    ),
    ShredSheetsProduct(
        id = 5,
        title = "Bass - Songwriter",
        instrument = "Bass",
        edition = "Songwriter",
        subtitle = "Arrangement, Chord Roots & Melodic Basslines",
        badge = "BASS ARRANGER",
        badgeColor = Color(0xFF10B981),
        price = "$9.99 USD",
        description = "Created for multi-instrumentalists, bandleaders, and bass composers. Side-by-side pages connect chord root charts, arrangement notes, and tempo markers directly with custom bassline tablature.",
        specs = listOf(
            "Side-by-Side Arrangement & Bass Tab Spreads",
            "Chord Root Notation & Harmonic Progression Grids",
            "Circle of 5ths & Modal Bass Scales Guide",
            "140 Perforated Pages with Ribbon Marker"
        ),
        amazonUrl = "https://www.amazon.com/s?k=shred+sheets+bass+songwriter"
    ),
    ShredSheetsProduct(
        id = 6,
        title = "Bass - Midnighter",
        instrument = "Bass",
        edition = "Midnighter",
        subtitle = "Nocturnal Groove Lab for Sub-Bass & Synth-Bass",
        badge = "MIDNIGHT SUB-BASS",
        badgeColor = Color(0xFFEC4899),
        price = "$9.99 USD",
        description = "A blackout edition tuned for late-night basement jams, synth-bass transcription, and drop-tuned low-end exploration. Stealth dark styling paired with razor-sharp stave contrast.",
        specs = listOf(
            "Matte Velvet Blackout Cover with Neon Magenta Foil",
            "Tuned for 4-String, 5-String & Drop Tunings",
            "High-Contrast Tablature for Low-Light Practice",
            "Heavy 120gsm Paper Proof Against Markers & Gel Pens"
        ),
        amazonUrl = "https://www.amazon.com/s?k=shred+sheets+bass+midnighter"
    )
)

/**
 * Dedicated renderer for the Shred Sheets book cover with custom edition styling.
 */
@Composable
fun ShredSheetsCoverItem(
    product: ShredSheetsProduct,
    modifier: Modifier = Modifier,
    isReflection: Boolean = false
) {
    Box(
        modifier = modifier
            .background(Color(0xFF12141C))
    ) {
        // Base book cover image
        Image(
            painter = painterResource(id = R.drawable.shred_sheets_cover),
            contentDescription = if (isReflection) null else "Shred Sheets ${product.title}",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Subtle atmosphere color wash matching the edition badge
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            product.badgeColor.copy(alpha = if (product.edition == "Midnighter") 0.16f else 0.24f),
                            Color.Transparent
                        ),
                        center = Offset(220f, 160f)
                    )
                )
        )

        // Midnighter stealth dark tone overlay
        if (product.edition == "Midnighter") {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.20f),
                                Color.Black.copy(alpha = 0.42f)
                            )
                        )
                    )
            )
        }

        // Edition Foil Plate on lower section of book
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp, start = 14.dp, end = 14.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF090B10).copy(alpha = 0.90f))
                .border(
                    width = 1.dp,
                    brush = Brush.horizontalGradient(
                        listOf(
                            product.badgeColor.copy(alpha = 0.85f),
                            Color.White.copy(alpha = 0.45f),
                            product.badgeColor.copy(alpha = 0.85f)
                        )
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = product.instrument.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 9.sp,
                            letterSpacing = 1.5.sp
                        ),
                        color = Color(0xFF94A3B8)
                    )
                    Text(
                        text = product.edition.uppercase(),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp,
                            letterSpacing = 0.5.sp
                        ),
                        color = product.badgeColor
                    )
                }

                // Edition Motif Icon
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(product.badgeColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    val icon = when (product.edition) {
                        "Rocker" -> Icons.Default.Bolt
                        "Songwriter" -> Icons.Default.MusicNote
                        else -> Icons.Default.NightlightRound
                    }
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = product.badgeColor,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        // Realistic Book Spine Sheen / Gradient overlay on left edge
        Box(
            modifier = Modifier
                .width(18.dp)
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.22f),
                            Color.White.copy(alpha = 0.05f),
                            Color.Transparent
                        )
                    )
                )
        )
    }
}

@Composable
fun ShopScreen(
    onThemeToggle: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isDark = LocalIsDarkTheme.current
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val titleScale = remember { Animatable(1f) }
    val titleScope = rememberCoroutineScope()

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { SHRED_SHEETS_CATALOG.size }
    )

    val currentProduct = SHRED_SHEETS_CATALOG[pagerState.currentPage]
    val activeAccentColor = MaterialTheme.colorScheme.primary

    // Showroom Dark Stage Palette
    val stageBg = if (isDark) Color(0xFF090A0E) else Color(0xFF101217)
    val stageFloorTop = if (isDark) Color(0xFF11141C) else Color(0xFF181C26)
    val stageFloorBottom = if (isDark) Color(0xFF060709) else Color(0xFF0A0C10)
    val cardSurface = if (isDark) Color(0xFF161820) else Color(0xFF1F222C)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(stageBg)
    ) {
        // Dramatic showroom ceiling spotlight beam shining down on center stage
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(380.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            activeAccentColor.copy(alpha = 0.18f),
                            Color(0xFF3B82F6).copy(alpha = 0.08f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Screen Header: "Buy Shred Sheets" (matching screen title style and tap-to-change color functionality)
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
                    .padding(horizontal = 8.dp, vertical = 2.dp)
                    .semantics {
                        role = Role.Button
                        contentDescription = "Buy Shred Sheets title. Tap to cycle accent color theme."
                    }
                    .testTag("shop_screen_title"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Buy Shred Sheets",
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

            Text(
                text = "Blank Tabs for Future Rockstars • Official Catalogue",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp
                ),
                color = Color(0xFF94A3B8),
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
            )

            // Instrument Quick Switcher (Guitar 1-3 | Bass 4-6)
            Row(
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF141721))
                    .border(1.dp, Color(0xFF282F40), RoundedCornerShape(20.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val isGuitar = pagerState.currentPage < 3
                val isBass = pagerState.currentPage >= 3

                // Guitar Switcher Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (isGuitar) Color(0xFFEF4444).copy(alpha = 0.22f) else Color.Transparent
                        )
                        .border(
                            1.dp,
                            if (isGuitar) Color(0xFFEF4444).copy(alpha = 0.75f) else Color.Transparent,
                            RoundedCornerShape(16.dp)
                        )
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            scope.launch {
                                val target = if (pagerState.currentPage >= 3) pagerState.currentPage - 3 else pagerState.currentPage
                                pagerState.animateScrollToPage(target)
                            }
                        }
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "🎸 Guitar (3)",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (isGuitar) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 12.sp
                        ),
                        color = if (isGuitar) Color.White else Color(0xFF94A3B8)
                    )
                }

                // Bass Switcher Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (isBass) Color(0xFF8B5CF6).copy(alpha = 0.22f) else Color.Transparent
                        )
                        .border(
                            1.dp,
                            if (isBass) Color(0xFF8B5CF6).copy(alpha = 0.75f) else Color.Transparent,
                            RoundedCornerShape(16.dp)
                        )
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            scope.launch {
                                val target = if (pagerState.currentPage < 3) pagerState.currentPage + 3 else pagerState.currentPage
                                pagerState.animateScrollToPage(target)
                            }
                        }
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "🎸 Bass (3)",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (isBass) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 12.sp
                        ),
                        color = if (isBass) Color.White else Color(0xFF94A3B8)
                    )
                }
            }

            // SHOWROOM CAROUSEL SECTION
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(430.dp),
                contentAlignment = Alignment.Center
            ) {
                val availableWidth = maxWidth
                val cardWidth = (availableWidth * 0.68f).coerceIn(240.dp, 330.dp)
                val horizontalPadding = (availableWidth - cardWidth) / 2

                HorizontalPager(
                    state = pagerState,
                    contentPadding = PaddingValues(horizontal = horizontalPadding),
                    pageSpacing = 16.dp,
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("shop_product_pager")
                ) { page ->
                    val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction).absoluteValue
                    val scale = lerp(0.86f, 1f, (1f - pageOffset).coerceIn(0f, 1f))
                    val alpha = lerp(0.55f, 1f, (1f - pageOffset).coerceIn(0f, 1f))
                    val product = SHRED_SHEETS_CATALOG[page]

                    Column(
                        modifier = Modifier
                            .width(cardWidth)
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                                this.alpha = alpha
                            },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // 1. The Real Book Cover Card (Top)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(0.75f) // 3:4 Book Ratio
                                .shadow(
                                    elevation = if (page == pagerState.currentPage) 22.dp else 6.dp,
                                    shape = RoundedCornerShape(12.dp),
                                    ambientColor = product.badgeColor.copy(alpha = 0.45f),
                                    spotColor = Color.Black
                                )
                                .clip(RoundedCornerShape(12.dp))
                                .border(
                                    width = 1.5.dp,
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            if (page == pagerState.currentPage) product.badgeColor.copy(alpha = 0.85f) else Color(0xFF334155),
                                            Color(0xFF1E293B)
                                        )
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                )
                        ) {
                            // Dedicated custom styled cover for this variation
                            ShredSheetsCoverItem(
                                product = product,
                                modifier = Modifier.fillMaxSize(),
                                isReflection = false
                            )
                        }

                        // 2. Glossy Showroom Floor Reflection directly underneath book!
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(95.dp)
                                .graphicsLayer {
                                    // Mirror the book upside down
                                    scaleY = -1f
                                }
                                .clip(RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp))
                        ) {
                            // Inverted Book Cover
                            ShredSheetsCoverItem(
                                product = product,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .alpha(0.30f)
                                    .blur(1.dp),
                                isReflection = true
                            )

                            // Vertical gradient mask creating realistic floor fade-out reflection
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                stageFloorTop.copy(alpha = 0.95f),
                                                stageFloorTop.copy(alpha = 0.70f),
                                                Color.Transparent
                                            )
                                        )
                                    )
                            )
                        }
                    }
                }

                // Showroom Floor Elliptical Light Pool (Right at the base of the hero car / book)
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 60.dp)
                        .width(cardWidth * 1.15f)
                        .height(28.dp)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    activeAccentColor.copy(alpha = 0.25f),
                                    Color.White.copy(alpha = 0.08f),
                                    Color.Transparent
                                )
                            ),
                            shape = CircleShape
                        )
                )

                // Left navigation arrow button
                if (pagerState.currentPage > 0) {
                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage - 1)
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 8.dp)
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E293B).copy(alpha = 0.75f))
                            .border(1.dp, Color(0xFF334155), CircleShape)
                            .testTag("shop_prev_arrow")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
                            contentDescription = "Previous Variant",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Right navigation arrow button
                if (pagerState.currentPage < SHRED_SHEETS_CATALOG.size - 1) {
                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 8.dp)
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E293B).copy(alpha = 0.75f))
                            .border(1.dp, Color(0xFF334155), CircleShape)
                            .testTag("shop_next_arrow")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = "Next Variant",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // 6-Variant Portfolio Pagination Dots
            Row(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .testTag("shop_pager_dots"),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (index in 0 until SHRED_SHEETS_CATALOG.size) {
                    val isSelected = pagerState.currentPage == index
                    val dotWidth by animateDpAsState(
                        targetValue = if (isSelected) 26.dp else 8.dp,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                        label = "dotWidth"
                    )
                    val dotColor by animateColorAsState(
                        targetValue = if (isSelected) SHRED_SHEETS_CATALOG[index].badgeColor else Color(0xFF475569),
                        animationSpec = tween(150),
                        label = "dotColor"
                    )

                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .height(8.dp)
                            .width(dotWidth)
                            .clip(CircleShape)
                            .background(dotColor)
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                scope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            }
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Current Variant Detail Card & Amazon CTA Section
            AnimatedContent(
                targetState = currentProduct,
                transitionSpec = {
                    fadeIn(animationSpec = tween(180)) togetherWith fadeOut(animationSpec = tween(180))
                },
                label = "variantDetailsAnim",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .widthIn(max = 600.dp)
            ) { product ->
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = cardSurface),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.verticalGradient(
                            listOf(Color(0xFF334155), Color(0xFF1E293B))
                        ),
                        width = 1.dp
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Badge & Title Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(product.badgeColor.copy(alpha = 0.2f))
                                    .border(1.dp, product.badgeColor.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = product.badge,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        letterSpacing = 1.sp
                                    ),
                                    color = product.badgeColor
                                )
                            }

                            // Price Tag
                            Text(
                                text = product.price,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 22.sp
                                ),
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Variant Title
                        Text(
                            text = product.title,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp
                            ),
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = product.subtitle,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = product.badgeColor,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                        )

                        Text(
                            text = product.description,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 13.sp,
                                lineHeight = 19.sp
                            ),
                            color = Color(0xFFCBD5E1),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        // Specs bullet points
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF0F172A).copy(alpha = 0.6f))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            product.specs.forEach { spec ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = activeAccentColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = spec,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium
                                        ),
                                        color = Color(0xFFE2E8F0)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // ==========================================
                        // OFFICIAL AMAZON BUY CTA BUTTON
                        // ==========================================
                        val openAmazon = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(product.amazonUrl)).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                context.startActivity(intent)
                            } catch (_: Exception) {
                                val browserIntent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://www.amazon.com/s?k=shred+sheets+guitar+edition")
                                ).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                context.startActivity(browserIntent)
                            }
                        }

                        Button(
                            onClick = openAmazon,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFF9900) // Official Amazon Brand Warm Gold
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 13.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("shop_buy_amazon_button")
                                .semantics {
                                    role = Role.Button
                                    contentDescription = "Buy ${product.title} on Amazon.com for $9.99 USD"
                                }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Buy on",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    ),
                                    color = Color(0xFF111111)
                                )
                                Spacer(modifier = Modifier.width(8.dp))

                                // Official Amazon Logo
                                Image(
                                    painter = painterResource(id = R.drawable.ic_amazon_logo),
                                    contentDescription = "Amazon.com",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier
                                        .height(24.dp)
                                        .width(82.dp)
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = "• $9.99 USD",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Black,
                                        fontSize = 15.sp
                                    ),
                                    color = Color(0xFF111111)
                                )

                                Spacer(modifier = Modifier.width(6.dp))

                                Icon(
                                    imageVector = Icons.Default.OpenInNew,
                                    contentDescription = null,
                                    tint = Color(0xFF111111),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        // Amazon Prime Free Delivery badge beneath button
                        Row(
                            modifier = Modifier
                                .padding(top = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalShipping,
                                contentDescription = null,
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Available on Amazon.com • FREE Prime Fast Delivery",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Normal
                                ),
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Showroom Features Grid
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .widthIn(max = 600.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "WHY ROCKSTARS LOVE SHRED SHEETS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 1.2.sp
                    ),
                    color = Color(0xFF64748B),
                    modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
                )

                ShowroomFeatureItem(
                    icon = Icons.Default.MenuBook,
                    title = "Lay-Flat Wire-O Spiral",
                    description = "Opens a full 360° on music stands and guitar amps without closing on you."
                )

                ShowroomFeatureItem(
                    icon = Icons.Default.Verified,
                    title = "Thick 100gsm Acid-Free Paper",
                    description = "Specially weighted to prevent ink bleed-through from sharpies, gel pens, and pencils."
                )

                ShowroomFeatureItem(
                    icon = Icons.Default.Star,
                    title = "Chord Grids on Every Spread",
                    description = "Quick chord boxes above each system so you never lose the harmonic context."
                )
            }
        }
    }
}

@Composable
fun ShowroomFeatureItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF141721))
            .border(1.dp, Color(0xFF242938), RoundedCornerShape(12.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF1E293B)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFFF59E0B),
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                ),
                color = Color.White
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                ),
                color = Color(0xFF94A3B8)
            )
        }
    }
}

private fun lerp(start: Float, stop: Float, fraction: Float): Float {
    return start + fraction * (stop - start)
}
