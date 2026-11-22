package com.example.presentation.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.classifier.ClassificationResult
import com.example.domain.model.*

@Composable
fun SeasonResultScreen(
    season: SeasonType,
    classification: ClassificationResult?,
    silhouette: BodySilhouette?,
    onNavigateToOutfitMatch: () -> Unit,
    onSelectSeason: (SeasonType) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scrollState = rememberScrollState()
    var selectedSwatch by remember { mutableStateOf<ColorSwatch?>(null) }
    var showExportDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "YOUR SEASONAL PALETTE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
                Text(
                    text = season.title,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            IconButton(
                onClick = { showExportDialog = true },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .testTag("btn_export_palette")
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Export Hex Palette",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Hero Season Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("season_hero_card"),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = season.group.displayName.uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    val confidence = classification?.confidence ?: 0.95f
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0x2206D6A0)
                    ) {
                        Text(
                            text = "${(confidence * 100).toInt()}% Match",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF06D6A0),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = season.subtitle,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = season.palette.description,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Dimensions Bars
                SeasonDimensionsGrid(classification = classification)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Color Swatch Sections
        PaletteSection(
            title = "Signature Primary Colors",
            description = "Your most flattering power colors for tops, dresses, and suiting.",
            swatches = season.palette.primaryColors,
            onSwatchClick = { selectedSwatch = it },
            testTagPrefix = "primary_swatch"
        )

        Spacer(modifier = Modifier.height(16.dp))

        PaletteSection(
            title = "Accent & Pop Colors",
            description = "Vibrant statement hues for scarves, evening wear, and accessories.",
            swatches = season.palette.accentColors,
            onSwatchClick = { selectedSwatch = it },
            testTagPrefix = "accent_swatch"
        )

        Spacer(modifier = Modifier.height(16.dp))

        PaletteSection(
            title = "Wardrobe Neutrals",
            description = "Investment basics for trousers, coats, knitwear, and tailoring.",
            swatches = season.palette.neutralColors,
            onSwatchClick = { selectedSwatch = it },
            testTagPrefix = "neutral_swatch"
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Colors to Avoid Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Block,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Colors to Avoid",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = "These shades create dull shadows or cast a sallow tone against your undertone:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 6.dp)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    items(season.palette.colorsToAvoid) { swatch ->
                        AvoidSwatchItem(swatch = swatch)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Styling Details (Metals & Silhouette)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Jewelry Metals Card
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Optimal Metals",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    season.palette.bestMetals.forEach { metal ->
                        Text(
                            text = "• $metal",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Makeup / Grooming Card
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Makeup & Grooming",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = season.palette.makeupTones,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 4
                    )
                }
            }
        }

        if (silhouette != null) {
            Spacer(modifier = Modifier.height(14.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Silhouette Styling: ${silhouette.title}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = silhouette.stylingGuidance,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // CTA to Test Outfit
        Button(
            onClick = onNavigateToOutfitMatch,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("btn_test_outfit_from_palette"),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Test an Outfit Against This Palette", fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // Selected Swatch Bottom Sheet / Dialog
    selectedSwatch?.let { swatch ->
        AlertDialog(
            onDismissRequest = { selectedSwatch = null },
            title = { Text(text = swatch.name, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(swatch.color)
                            .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Hex Code: ${swatch.hex}", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Text("Harmonized for your ${season.title} season.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(swatch.hex))
                        Toast.makeText(context, "Copied ${swatch.hex} to clipboard", Toast.LENGTH_SHORT).show()
                        selectedSwatch = null
                    }
                ) {
                    Text("Copy Hex")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedSwatch = null }) {
                    Text("Close")
                }
            }
        )
    }

    // Export Palette Dialog
    if (showExportDialog) {
        val allHexList = (season.palette.primaryColors + season.palette.accentColors + season.palette.neutralColors)
            .joinToString("\n") { "${it.name}: ${it.hex}" }

        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("${season.title} Palette Export") },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = "Digital swatch cards for in-store shopping & styling:\n\n$allHexList",
                        fontSize = 12.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(allHexList))
                        Toast.makeText(context, "Full palette copied to clipboard!", Toast.LENGTH_SHORT).show()
                        showExportDialog = false
                    }
                ) {
                    Text("Copy All Hex Codes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("Done")
                }
            }
        )
    }
}

@Composable
private fun PaletteSection(
    title: String,
    description: String,
    swatches: List<ColorSwatch>,
    onSwatchClick: (ColorSwatch) -> Unit,
    testTagPrefix: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(swatches) { swatch ->
                    SwatchItem(
                        swatch = swatch,
                        onClick = { onSwatchClick(swatch) },
                        testTag = "${testTagPrefix}_${swatch.hex.removePrefix("#")}"
                    )
                }
            }
        }
    }
}

@Composable
private fun SwatchItem(
    swatch: ColorSwatch,
    onClick: () -> Unit,
    testTag: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(swatch.color)
                .border(1.5.dp, Color.White.copy(alpha = 0.5f), CircleShape)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = swatch.name,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1
        )
    }
}

@Composable
private fun AvoidSwatchItem(swatch: ColorSwatch) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(swatch.color)
                .border(1.5.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.7f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Avoid",
                tint = Color.White.copy(alpha = 0.85f),
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = swatch.name,
            fontSize = 9.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
    }
}

@Composable
private fun SeasonDimensionsGrid(classification: ClassificationResult?) {
    val warmth = classification?.warmthScore ?: -0.6f
    val chroma = classification?.chromaScore ?: 0.85f
    val contrast = classification?.contrastScore ?: 0.9f

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        DimensionPill(
            label = "Undertone",
            value = if (warmth > 0) "Warm" else "Cool",
            color = if (warmth > 0) Color(0xFFFFD166) else Color(0xFF4CC9F0),
            modifier = Modifier.weight(1f)
        )
        DimensionPill(
            label = "Chroma",
            value = if (chroma > 0.6f) "Clear" else "Muted",
            color = Color(0xFFB5179E),
            modifier = Modifier.weight(1f)
        )
        DimensionPill(
            label = "Contrast",
            value = if (contrast > 0.6f) "High" else "Low",
            color = Color(0xFF06D6A0),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun DimensionPill(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = color.copy(alpha = 0.15f),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}
