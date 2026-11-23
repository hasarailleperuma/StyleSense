package com.example.presentation.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.colorimetry.DominantColorExtractor
import com.example.domain.model.*
import java.util.Locale

@Composable
fun OutfitMatcherScreen(
    userSeason: SeasonType,
    selectedOccasion: Occasion,
    matchResult: OutfitMatchResult?,
    scannedGarment: DominantColorExtractor.GarmentAnalysis?,
    onSelectOccasion: (Occasion) -> Unit,
    onGarmentPhotoSelected: (Uri) -> Unit,
    onSaveToWardrobe: (name: String, category: GarmentCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var garmentNameInput by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(GarmentCategory.TOPS) }
    var selectedModeTab by remember { mutableStateOf(0) } // 0 = Single Garment, 1 = Pair Top & Bottom

    // Pair mode state
    var selectedTopHex by remember { mutableStateOf("#0F4C81") }
    var selectedTopName by remember { mutableStateOf("Sapphire Blouse") }
    var selectedBottomHex by remember { mutableStateOf("#1A202C") }
    var selectedBottomName by remember { mutableStateOf("Charcoal A-Line Skirt") }

    val pairResult by remember(selectedTopHex, selectedBottomHex, userSeason, selectedOccasion) {
        derivedStateOf {
            val topColor = ColorSwatch(selectedTopName, selectedTopHex, Color(android.graphics.Color.parseColor(selectedTopHex)))
            val bottomColor = ColorSwatch(selectedBottomName, selectedBottomHex, Color(android.graphics.Color.parseColor(selectedBottomHex)))
            com.example.domain.harmony.MatsudaHarmonyEngine.evaluateTwoGarmentPair(
                listOf(topColor),
                listOf(bottomColor),
                userSeason,
                selectedOccasion,
                null
            )
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            onGarmentPhotoSelected(uri)
        }
    }

    LaunchedEffect(scannedGarment) {
        if (scannedGarment != null) {
            selectedCategory = scannedGarment.category
            garmentNameInput = "${scannedGarment.category.displayName.substringBefore("&").trim()} in ${scannedGarment.dominantColors.firstOrNull()?.name ?: "Color"}"
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        Text(
            text = "OUTFIT HARMONY MATCHER",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 1.sp
        )
        Text(
            text = "Outfit Matcher",
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Mode Switcher: Single Garment vs Two-Garment Pairing
        TabRow(
            selectedTabIndex = selectedModeTab,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
        ) {
            Tab(
                selected = selectedModeTab == 0,
                onClick = { selectedModeTab = 0 },
                text = { Text("Single Garment", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            )
            Tab(
                selected = selectedModeTab == 1,
                onClick = { selectedModeTab = 1 },
                text = { Text("Mix & Match (Top + Skirt)", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Occasion Selector
        Text(
            text = "SELECT OCCASION",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 0.5.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(Occasion.entries) { occ ->
                val isSelected = occ == selectedOccasion
                FilterChip(
                    selected = isSelected,
                    onClick = { onSelectOccasion(occ) },
                    label = { Text(occ.title) },
                    modifier = Modifier.testTag("occasion_${occ.name.lowercase()}"),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Garment Ingestion & Active Swatches Row
        if (selectedModeTab == 1) {
            // Two-Garment Mix & Match Card (Top + Bottom / Skirt)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Step 1: Choose Your Blouse / Top",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    val topOptions = listOf(
                        Pair("Sapphire Blouse", "#0F4C81"),
                        Pair("Ruby Silk Top", "#9B111E"),
                        Pair("Emerald Top", "#008F5A"),
                        Pair("Crisp White", "#F7FAFC"),
                        Pair("Terracotta", "#ED8936")
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(topOptions) { (name, hex) ->
                            val isSelected = selectedTopHex == hex
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedTopHex = hex
                                    selectedTopName = name
                                },
                                label = { Text(name, fontSize = 11.sp) },
                                leadingIcon = {
                                    Box(
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clip(CircleShape)
                                            .background(Color(android.graphics.Color.parseColor(hex)))
                                    )
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Step 2: Choose Your Skirt / Trousers",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    val bottomOptions = listOf(
                        Pair("Charcoal A-Line", "#1A202C"),
                        Pair("Navy Flared Skirt", "#2C5282"),
                        Pair("Obsidian Trousers", "#171923"),
                        Pair("Ivory Wide-Leg", "#E2E8F0"),
                        Pair("Plum Trumpet Skirt", "#702459")
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(bottomOptions) { (name, hex) ->
                            val isSelected = selectedBottomHex == hex
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedBottomHex = hex
                                    selectedBottomName = name
                                },
                                label = { Text(name, fontSize = 11.sp) },
                                leadingIcon = {
                                    Box(
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clip(CircleShape)
                                            .background(Color(android.graphics.Color.parseColor(hex)))
                                    )
                                }
                            )
                        }
                    }
                }
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Active Garment Colors",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        OutlinedButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("btn_scan_for_match")
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Scan Garment", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val colors = matchResult?.dominantColors ?: emptyList()
                    if (colors.isEmpty()) {
                        Text(
                            text = "Tap 'Scan Garment' or pick an item from your closet to analyze harmony.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            colors.forEach { swatch ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(swatch.color)
                                            .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text(text = swatch.name, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Text(text = swatch.hex, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        val activeResult = if (selectedModeTab == 1) pairResult else matchResult

        Spacer(modifier = Modifier.height(16.dp))

        // Compatibility Score Hero Card
        if (activeResult != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("match_result_card"),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = activeResult.seasonAlignment,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = when {
                                    activeResult.overallScore >= 85 -> Color(0xFF06D6A0)
                                    activeResult.overallScore >= 70 -> MaterialTheme.colorScheme.primary
                                    else -> Color(0xFFFF6F61)
                                }
                            )
                            Text(
                                text = "Harmony: ${activeResult.harmonyCategory}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Circular score badge
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        activeResult.overallScore >= 85 -> Color(0x2206D6A0)
                                        activeResult.overallScore >= 70 -> Color(0x227209B7)
                                        else -> Color(0x22FF6F61)
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${activeResult.overallScore}",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = when {
                                    activeResult.overallScore >= 85 -> Color(0xFF06D6A0)
                                    activeResult.overallScore >= 70 -> MaterialTheme.colorScheme.primary
                                    else -> Color(0xFFFF6F61)
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Occasion Verdict: ${activeResult.occasionVerdict}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = String.format(Locale.US, "ΔE %.1f", activeResult.deltaEToSeasonPalette),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Styling Rationale:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = activeResult.plainEnglishRationale,
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Recommended Accessories
                    Text(
                        text = "Curated Accents & Jewelry:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    activeResult.recommendedAccessories.forEach { acc ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF06D6A0),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = acc, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }

            // Save to Closet Card if Scanned
            if (scannedGarment != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Save Scanned Garment to Closet",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = garmentNameInput,
                            onValueChange = { garmentNameInput = it },
                            label = { Text("Garment Name") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_garment_name")
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                onSaveToWardrobe(garmentNameInput, selectedCategory)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("btn_save_garment_to_closet"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.BookmarkAdd, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Save to My Wardrobe")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
