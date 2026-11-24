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
    modifier: Modifier = Modifier,
    activeSilhouette: BodySilhouette? = null,
    onSelectSilhouette: ((BodySilhouette) -> Unit)? = null
) {
    val scrollState = rememberScrollState()
    var garmentNameInput by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(GarmentCategory.TOPS) }
    var selectedModeTab by remember { mutableStateOf(0) } // 0 = Single Garment, 1 = Pair Top & Bottom, 2 = Dress/Suit + Layer
    var selectedGender by remember { mutableStateOf(GarmentGender.ALL) }

    var currentSilhouette by remember(activeSilhouette) {
        mutableStateOf(activeSilhouette ?: BodySilhouette.HOURGLASS)
    }

    // Pair mode state
    var selectedTopHex by remember { mutableStateOf("#0F4C81") }
    var selectedTopName by remember { mutableStateOf("Sapphire Blouse") }
    var selectedBottomHex by remember { mutableStateOf("#1A202C") }
    var selectedBottomName by remember { mutableStateOf("Charcoal A-Line Skirt") }

    // Dress + Jacket pairing state
    var selectedDressHex by remember { mutableStateOf("#9B111E") }
    var selectedDressName by remember { mutableStateOf("Ruby Wrap Dress") }
    var selectedJacketHex by remember { mutableStateOf("#1A365D") }
    var selectedJacketName by remember { mutableStateOf("Navy Blazer") }

    val pairResult by remember(selectedTopHex, selectedBottomHex, userSeason, selectedOccasion, currentSilhouette) {
        derivedStateOf {
            val topColor = ColorSwatch(selectedTopName, selectedTopHex, Color(android.graphics.Color.parseColor(selectedTopHex)))
            val bottomColor = ColorSwatch(selectedBottomName, selectedBottomHex, Color(android.graphics.Color.parseColor(selectedBottomHex)))
            com.example.domain.harmony.MatsudaHarmonyEngine.evaluateTwoGarmentPair(
                listOf(topColor),
                listOf(bottomColor),
                userSeason,
                selectedOccasion,
                currentSilhouette
            )
        }
    }

    val dressJacketResult by remember(selectedDressHex, selectedJacketHex, userSeason, selectedOccasion, currentSilhouette) {
        derivedStateOf {
            val dressColor = ColorSwatch(selectedDressName, selectedDressHex, Color(android.graphics.Color.parseColor(selectedDressHex)))
            val jacketColor = ColorSwatch(selectedJacketName, selectedJacketHex, Color(android.graphics.Color.parseColor(selectedJacketHex)))
            com.example.domain.harmony.MatsudaHarmonyEngine.evaluateTwoGarmentPair(
                listOf(dressColor),
                listOf(jacketColor),
                userSeason,
                selectedOccasion,
                currentSilhouette
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

        // Mode Switcher: Single Garment vs Two-Garment Pairing vs Dress Styling
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
                text = { Text("Single Garment", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
            )
            Tab(
                selected = selectedModeTab == 1,
                onClick = { selectedModeTab = 1 },
                text = { Text("Top + Bottom", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
            )
            Tab(
                selected = selectedModeTab == 2,
                onClick = { selectedModeTab = 2 },
                text = { Text("Dress / Suit + Layer", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Wardrobe Gender Selector
        Text(
            text = "WARDROBE LINE (WOMEN / MEN / ALL)",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 0.5.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth().testTag("matcher_gender_row")
        ) {
            GarmentGender.entries.forEach { g ->
                val isSelected = g == selectedGender
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedGender = g },
                    label = { Text("${g.icon} ${g.displayName}", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
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

        Spacer(modifier = Modifier.height(14.dp))

        // Body Silhouette Selector (Appendix O Table O.1)
        Text(
            text = "SELECT BODY SILHOUETTE (APPENDIX O)",
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
            items(BodySilhouette.entries) { sil ->
                val isSelected = sil == currentSilhouette
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        currentSilhouette = sil
                        onSelectSilhouette?.invoke(sil)
                    },
                    label = { Text(sil.title, fontSize = 11.sp) },
                    modifier = Modifier.testTag("silhouette_${sil.name.lowercase()}"),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
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
                        text = if (selectedGender == GarmentGender.MEN) "Step 1: Choose Your Shirt / Sweater" else "Step 1: Choose Your Top / Blouse",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    val topOptions = when (selectedGender) {
                        GarmentGender.MEN -> listOf(
                            Pair("Oxford White Shirt", "#F7FAFC"),
                            Pair("Navy Merino Sweater", "#1A365D"),
                            Pair("Olive Camp Shirt", "#276749"),
                            Pair("Cable-Knit Oatmeal", "#E2E8F0"),
                            Pair("Cobalt Polo", "#0F4C81")
                        )
                        GarmentGender.WOMEN -> listOf(
                            Pair("Sapphire Blouse", "#0F4C81"),
                            Pair("Ruby Silk Top", "#9B111E"),
                            Pair("Emerald Flared Top", "#008F5A"),
                            Pair("Crisp Poplin Shirt", "#F7FAFC"),
                            Pair("Terracotta Linen", "#ED8936")
                        )
                        GarmentGender.ALL -> listOf(
                            Pair("Crisp White Top", "#F7FAFC"),
                            Pair("Sapphire Top/Knit", "#0F4C81"),
                            Pair("Navy Merino Layer", "#1A365D"),
                            Pair("Emerald Silk", "#008F5A"),
                            Pair("Ruby Accent Top", "#9B111E")
                        )
                    }
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
                        text = if (selectedGender == GarmentGender.MEN) "Step 2: Choose Your Trousers / Slacks" else "Step 2: Choose Your Skirt / Trousers",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    val bottomOptions = when (selectedGender) {
                        GarmentGender.MEN -> listOf(
                            Pair("Charcoal Dress Slacks", "#1A202C"),
                            Pair("British Khaki Chinos", "#744210"),
                            Pair("Midnight Wool Pants", "#1A365D"),
                            Pair("Raw Indigo Denim", "#1A202C"),
                            Pair("Stone Linen Slacks", "#CBD5E0")
                        )
                        GarmentGender.WOMEN -> listOf(
                            Pair("Charcoal A-Line", "#1A202C"),
                            Pair("Navy Flared Skirt", "#2C5282"),
                            Pair("Obsidian Trousers", "#171923"),
                            Pair("Ivory Wide-Leg", "#E2E8F0"),
                            Pair("Plum Trumpet Skirt", "#702459")
                        )
                        GarmentGender.ALL -> listOf(
                            Pair("Charcoal Trousers", "#1A202C"),
                            Pair("Navy Slacks", "#1A365D"),
                            Pair("Ivory Wide-Leg", "#E2E8F0"),
                            Pair("Khaki Chinos", "#744210"),
                            Pair("Bordeaux Skirt/Pants", "#9B111E")
                        )
                    }
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
        } else if (selectedModeTab == 2) {
            // Dress & Outerwear Styling Card (Dress + Blazer / Coat)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (selectedGender == GarmentGender.MEN) "Step 1: Choose Your Two-Piece Suit / Coordinate" else "Step 1: Choose Your Dress / Jumpsuit",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    val dressOptions = when (selectedGender) {
                        GarmentGender.MEN -> listOf(
                            Pair("Navy Worsted Suit", "#1A365D"),
                            Pair("Charcoal Two-Piece", "#1A202C"),
                            Pair("Italian Tuxedo", "#0D1117"),
                            Pair("Summer Linen Suit", "#744210"),
                            Pair("Houndstooth Suit", "#2D3748")
                        )
                        GarmentGender.WOMEN -> listOf(
                            Pair("Ruby Wrap Dress", "#9B111E"),
                            Pair("Sapphire Silk Dress", "#0F4C81"),
                            Pair("Emerald Flared Dress", "#276749"),
                            Pair("Black Sheath Dress", "#1A1A24"),
                            Pair("Plum Velvet Gown", "#702459")
                        )
                        GarmentGender.ALL -> listOf(
                            Pair("Sapphire Dress/Suit", "#0F4C81"),
                            Pair("Charcoal Suit Set", "#1A202C"),
                            Pair("Ruby Wrap Dress", "#9B111E"),
                            Pair("Navy Tailored Suit", "#1A365D"),
                            Pair("Black Sheath/Tuxedo", "#1A1A24")
                        )
                    }
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(dressOptions) { (name, hex) ->
                            val isSelected = selectedDressHex == hex
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedDressHex = hex
                                    selectedDressName = name
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
                        text = if (selectedGender == GarmentGender.MEN) "Step 2: Choose Your Blazer / Overcoat" else "Step 2: Choose Your Jacket / Coat",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    val jacketOptions = when (selectedGender) {
                        GarmentGender.MEN -> listOf(
                            Pair("Camel Wool Overcoat", "#744210"),
                            Pair("Charcoal Sport Coat", "#2D3748"),
                            Pair("Onyx Car Coat", "#1A202C"),
                            Pair("Leather Biker", "#171923"),
                            Pair("Slate Trench Coat", "#4A5568")
                        )
                        GarmentGender.WOMEN -> listOf(
                            Pair("Tailored Navy Blazer", "#1A365D"),
                            Pair("Belted Trench Coat", "#2D3748"),
                            Pair("Hourglass Peplum Jacket", "#702459"),
                            Pair("Straight Wool Topcoat", "#4A5568"),
                            Pair("Kimono Wool Coat", "#1C4532")
                        )
                        GarmentGender.ALL -> listOf(
                            Pair("Tailored Navy Blazer", "#1A365D"),
                            Pair("Belted Trench Coat", "#2D3748"),
                            Pair("Camel Wool Overcoat", "#744210"),
                            Pair("Slate Topcoat", "#4A5568"),
                            Pair("Sculpted Leather Jacket", "#171923")
                        )
                    }
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(jacketOptions) { (name, hex) ->
                            val isSelected = selectedJacketHex == hex
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedJacketHex = hex
                                    selectedJacketName = name
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
                    Text(
                        text = "Garment Category",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(GarmentCategory.entries) { cat ->
                            val isSelected = selectedCategory == cat
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedCategory = cat },
                                label = { Text(cat.displayName, fontSize = 11.sp) },
                                modifier = Modifier.testTag("cat_${cat.name.lowercase()}"),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

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
                            Text("Scan ${selectedCategory.displayName.substringBefore("&").trim()}", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val colors = matchResult?.dominantColors ?: emptyList()
                    if (colors.isEmpty()) {
                        Text(
                            text = "Tap 'Scan ${selectedCategory.displayName.substringBefore("&").trim()}' or pick an item to evaluate harmony.",
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

        val activeResult = when (selectedModeTab) {
            1 -> pairResult
            2 -> dressJacketResult
            else -> matchResult
        }

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
