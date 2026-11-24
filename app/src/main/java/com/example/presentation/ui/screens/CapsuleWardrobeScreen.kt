package com.example.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.domain.capsule.CapsuleItem
import com.example.domain.capsule.CapsuleOptimizerEngine
import com.example.domain.capsule.CapsuleResult
import com.example.domain.capsule.ItemMarginalGain
import com.example.domain.model.BodySilhouette
import com.example.domain.model.GarmentCategory
import com.example.domain.model.SeasonType

@Composable
fun CapsuleWardrobeScreen(
    season: SeasonType,
    silhouette: BodySilhouette?,
    onSaveItemToCloset: (name: String, category: GarmentCategory, primaryHex: String, secondaryHex: String, score: Int) -> Unit,
    modifier: Modifier = Modifier,
    onSilhouetteSelected: ((BodySilhouette) -> Unit)? = null
) {
    var localSilhouette by remember(silhouette) { mutableStateOf(silhouette ?: BodySilhouette.HOURGLASS) }
    val targetSilhouette = localSilhouette

    val capsuleResult by remember(season, targetSilhouette) {
        derivedStateOf {
            CapsuleOptimizerEngine.optimizeCapsule(season, targetSilhouette, maxItems = 10)
        }
    }

    var savedItemIds by remember { mutableStateOf(setOf<String>()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        // Header
        Text(
            text = "SMART CAPSULE WARDROBE",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 1.sp
        )
        Text(
            text = "10-Piece Capsule Collection",
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Body Silhouette Selector Row
        Text(
            text = "ADAPT TO BODY SILHOUETTE (APPENDIX O):",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth().testTag("capsule_silhouette_row")
        ) {
            items(BodySilhouette.entries) { sil ->
                val isSelected = sil == targetSilhouette
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        localSilhouette = sil
                        onSilhouetteSelected?.invoke(sil)
                    },
                    label = {
                        Text(
                            text = sil.title,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.testTag("chip_silhouette_${sil.name.lowercase()}")
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Optimization Summary Banner
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "CAPSULE SYNERGY SCORE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "${capsuleResult.overallScore.toInt()}% Synergy",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0x2206D6A0)
                    ) {
                        Text(
                            text = "${capsuleResult.totalItems} Curated Items",
                            color = Color(0xFF06D6A0),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Optimized for color harmony, silhouette cut, and versatility.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Selected Capsule Items List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(capsuleResult.selectedItems) { gain ->
                CapsuleItemCard(
                    gain = gain,
                    isSaved = savedItemIds.contains(gain.item.id),
                    onSave = {
                        onSaveItemToCloset(
                            gain.item.name,
                            gain.item.category,
                            gain.item.primaryHex,
                            gain.item.secondaryHex,
                            gain.colorHarmonyScore.toInt()
                        )
                        savedItemIds = savedItemIds + gain.item.id
                    }
                )
            }
        }
    }
}

@Composable
private fun CapsuleItemCard(
    gain: ItemMarginalGain,
    isSaved: Boolean,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    val item = gain.item
    val primaryColor = try {
        Color(android.graphics.Color.parseColor(item.primaryHex))
    } catch (e: Exception) {
        Color.DarkGray
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(primaryColor)
                            .border(1.5.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = item.name,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${item.category.displayName} • ${item.cutTag.displayName}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                IconButton(
                    onClick = onSave,
                    enabled = !isSaved,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (isSaved) Icons.Default.Check else Icons.Default.BookmarkAdd,
                        contentDescription = "Save to Closet",
                        tint = if (isSaved) Color(0xFF06D6A0) else MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = gain.rationale,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 3-Pill Marginal Gain Breakdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                MetricTag(
                    label = "Color",
                    value = "${gain.colorHarmonyScore.toInt()}%",
                    modifier = Modifier.weight(1f)
                )
                MetricTag(
                    label = "Cut Fit",
                    value = "${gain.silhouetteSuitability.toInt()}%",
                    modifier = Modifier.weight(1f)
                )
                MetricTag(
                    label = "Gain",
                    value = String.format(java.util.Locale.US, "+%.2f", gain.marginalGain),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun MetricTag(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 4.dp, horizontal = 6.dp)
        ) {
            Text(
                text = label.uppercase(),
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = value,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
