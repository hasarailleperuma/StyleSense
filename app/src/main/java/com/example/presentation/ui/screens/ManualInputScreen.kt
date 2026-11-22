package com.example.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
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
import com.example.domain.model.BodySilhouette

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualInputScreen(
    onBack: () -> Unit,
    onSubmit: (warmth: Float, depth: Float, eyeChroma: Float, contrast: Float, silhouette: BodySilhouette) -> Unit,
    modifier: Modifier = Modifier
) {
    var warmth by remember { mutableFloatStateOf(0.1f) }
    var depth by remember { mutableFloatStateOf(0.35f) }
    var eyeChroma by remember { mutableFloatStateOf(0.5f) }
    var contrast by remember { mutableFloatStateOf(0.6f) }
    var selectedSilhouette by remember { mutableStateOf(BodySilhouette.HOURGLASS) }

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manual Setup", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("btn_manual_back")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Text(
                text = "Set your visual characteristics manually to compute your personalized 12-season color palette.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Slider 1: Skin Warmth
            ControlSliderCard(
                title = "Skin Undertone",
                description = if (warmth < -0.2f) "Cool Rosy / Blue-Pink" else if (warmth > 0.2f) "Warm Golden / Peachy" else "Balanced Neutral",
                value = warmth,
                valueRange = -1.0f..1.0f,
                leftLabel = "Cool Rosy",
                rightLabel = "Warm Golden",
                onValueChange = { warmth = it },
                testTag = "slider_warmth"
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Slider 2: Skin Depth
            ControlSliderCard(
                title = "Skin Depth",
                description = if (depth < 0.3f) "Fair / Ivory" else if (depth < 0.65f) "Medium / Golden Tan" else "Deep Rich Espresso",
                value = depth,
                valueRange = 0.0f..1.0f,
                leftLabel = "Fair / Porcelain",
                rightLabel = "Deep Espresso",
                onValueChange = { depth = it },
                testTag = "slider_depth"
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Slider 3: Eye Chroma
            ControlSliderCard(
                title = "Saturation (Chroma)",
                description = if (eyeChroma < 0.4f) "Soft, Muted, Smokey" else "Bright, Clear, High Saturation",
                value = eyeChroma,
                valueRange = 0.0f..1.0f,
                leftLabel = "Soft / Muted",
                rightLabel = "Vivid / Jewel",
                onValueChange = { eyeChroma = it },
                testTag = "slider_chroma"
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Slider 4: Overall Facial Contrast
            ControlSliderCard(
                title = "Contrast Ratio",
                description = if (contrast < 0.4f) "Gentle, Blended Contrast" else "Striking, High Dramatic Contrast",
                value = contrast,
                valueRange = 0.0f..1.0f,
                leftLabel = "Low Contrast",
                rightLabel = "High Contrast",
                onValueChange = { contrast = it },
                testTag = "slider_contrast"
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Body Silhouette Selection
            Text(
                text = "BODY SILHOUETTE",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            BodySilhouette.entries.forEach { sil ->
                val isSelected = sil == selectedSilhouette
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { selectedSilhouette = sil }
                        .testTag("sil_${sil.name.lowercase()}"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                    ),
                    border = if (isSelected) CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary)
                    ) else null
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = sil.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = sil.description,
                                fontSize = 11.sp,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    onSubmit(warmth, depth, eyeChroma, contrast, selectedSilhouette)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("btn_submit_manual"),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "Generate Color Profile",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ControlSliderCard(
    title: String,
    description: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    leftLabel: String,
    rightLabel: String,
    onValueChange: (Float) -> Unit,
    testTag: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Slider(
                value = value,
                onValueChange = onValueChange,
                valueRange = valueRange,
                modifier = Modifier.testTag(testTag)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = leftLabel, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = rightLabel, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
