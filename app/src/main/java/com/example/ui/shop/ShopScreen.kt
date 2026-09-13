package com.example.ui.shop

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
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
        title = "Guitar Edition",
        subtitle = "Blank Tabs for Future Rockstars",
        badge = "HERO ORIGINAL",
        badgeColor = Color(0xFFEF4444),
        price = "$12.99",
        description = "The flagship tab journal for electric & acoustic guitarists. Features 6-string staves paired with chord chart boxes on every spread.",
        specs = listOf("120 Acid-Free Pages", "Lay-Flat Wire-O Binding", "Standard 6-String Tabs + Chords"),
        amazonUrl = "https://www.amazon.com/s?k=shred+sheets+guitar+edition"
    ),
    ShredSheetsProduct(
        id = 2,
        title = "Bass Edition",
        subtitle = "Low-End Precision for Bassists",
        badge = "BESTSELLER",
        badgeColor = Color(0xFF8B5CF6),
        price = "$12.99",
        description = "Specially calibrated 4 and 5 string tablature layout with generous spacing for grooves, walking lines, and slap bass riffs.",
        specs = listOf("4 & 5 String Bass Staves", "100gsm Ink Bleed-Proof Stock", "Scale & Fretboard Quick Reference"),
        amazonUrl = "https://www.amazon.com/s?k=shred+sheets+bass+edition"
    ),
    ShredSheetsProduct(
        id = 3,
        title = "7-String & Extended",
        subtitle = "Modern Metal, Prog & Djent",
        badge = "HEAVY METAL",
        badgeColor = Color(0xFF10B981),
        price = "$13.99",
        description = "Engineered for low tunings (Drop A, Drop G, 8-string). Wide tablature grids allow rapid transcription of intricate polyrhythms.",
        specs = listOf("7 & 8 String Formats", "Extra Wide Fret Grids", "Thick 120gsm Heavyweight Paper"),
        amazonUrl = "https://www.amazon.com/s?k=shred+sheets+7+string"
    ),
    ShredSheetsProduct(
        id = 4,
        title = "Songwriter Edition",
        subtitle = "Lyrics, Chords & Staves Side-by-Side",
        badge = "NEW RELEASE",
        badgeColor = Color(0xFFF59E0B),
        price = "$14.99",
        description = "Left page dedicated to song structure, lyrics, and harmonic progressions; right page features dual guitar & vocal notation.",
        specs = listOf("Facing Spread Layout", "Song Structure Prompts", "140 Perforated Sheets"),
        amazonUrl = "https://www.amazon.com/s?k=shred+sheets+songwriter+edition"
    ),
    ShredSheetsProduct(
        id = 5,
        title = "Pocket Rockstar Spiral",
        subtitle = "Tour Bus & Gigbag Travel Edition",
        badge = "COMPACT TRAVEL",
        badgeColor = Color(0xFF06B6D4),
        price = "$9.99",
        description = "A5 compact footprint with rugged water-resistant poly covers and double-loop spiral for writing on tour, backstage, or rehearsals.",
        specs = listOf("Compact A5 Size", "Heavy Duty Waterproof Poly Cover", "Elastic Band Closure"),
        amazonUrl = "https://www.amazon.com/s?k=shred+sheets+pocket+edition"
    ),
    ShredSheetsProduct(
        id = 6,
        title = "Deluxe Collector's Hardcover",
        subtitle = "Foil Stamped Archival Edition",
        badge = "LIMITED EDITION",
        badgeColor = Color(0xFFEAB308),
        price = "$19.99",
        description = "Casebound hard linen cover with metallic hot-foil lettering, double ribbon bookmarks, and luxury 120gsm warm ivory paper.",
        specs = listOf("Embossed Linen Hardcover", "Dual Ribbon Bookmarks", "Expandable Back Pocket"),
        amazonUrl = "https://www.amazon.com/s?k=shred+sheets+deluxe+edition"
    )
)

@Composable
fun ShopScreen(
    modifier: Modifier = Modifier
) {
    val isDark = LocalIsDarkTheme.current
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

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

            // Screen Header: "Buy Shred Sheets"
            Text(
                text = "Buy Shred Sheets",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 28.sp,
                    letterSpacing = (-0.5).sp
                ),
                color = Color.White,
                modifier = Modifier.testTag("shop_screen_title")
            )

            Text(
                text = "Blank Tabs for Future Rockstars • Official Catalogue",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp
                ),
                color = Color(0xFF94A3B8),
                modifier = Modifier.padding(top = 4.dp, bottom = 18.dp)
            )

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
                                    elevation = if (page == pagerState.currentPage) 20.dp else 6.dp,
                                    shape = RoundedCornerShape(12.dp),
                                    ambientColor = activeAccentColor.copy(alpha = 0.4f),
                                    spotColor = Color.Black
                                )
                                .clip(RoundedCornerShape(12.dp))
                                .border(
                                    width = 1.5.dp,
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            if (page == pagerState.currentPage) activeAccentColor.copy(alpha = 0.8f) else Color(0xFF334155),
                                            Color(0xFF1E293B)
                                        )
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .background(Color(0xFF141518))
                        ) {
                            // The Attached Product Image loaded for each of the 6 variants
                            Image(
                                painter = painterResource(id = R.drawable.shred_sheets_cover),
                                contentDescription = "Shred Sheets ${SHRED_SHEETS_CATALOG[page].title}",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )

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
                            // Inverted Book Image
                            Image(
                                painter = painterResource(id = R.drawable.shred_sheets_cover),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .alpha(0.28f)
                                    .blur(1.dp)
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
                        targetValue = if (isSelected) activeAccentColor else Color(0xFF475569),
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
                            color = activeAccentColor,
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
                                    contentDescription = "Buy ${product.title} on Amazon for ${product.price}"
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
                                        fontSize = 16.sp
                                    ),
                                    color = Color(0xFF111111)
                                )
                                Spacer(modifier = Modifier.width(10.dp))

                                // Official Amazon Logo
                                Image(
                                    painter = painterResource(id = R.drawable.ic_amazon_logo),
                                    contentDescription = "Amazon",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier
                                        .height(26.dp)
                                        .width(90.dp)
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Icon(
                                    imageVector = Icons.Default.OpenInNew,
                                    contentDescription = null,
                                    tint = Color(0xFF111111),
                                    modifier = Modifier.size(17.dp)
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
                                text = "Eligible for FREE Prime Fast Delivery • Ships Worldwide",
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
