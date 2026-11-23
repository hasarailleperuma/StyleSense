package com.example.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.clickable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.BiometricMetrics
import java.util.Locale

@Composable
fun TelemetryHud(
    metrics: BiometricMetrics?,
    modifier: Modifier = Modifier,
    initiallyExpanded: Boolean = true
) {
    var isExpanded by remember { mutableStateOf(initiallyExpanded) }

    if (!isExpanded) {
        // Minimized compact chip: leaves user face 100% visible
        Card(
            modifier = modifier
                .fillMaxWidth()
                .clickable { isExpanded = true }
                .testTag("telemetry_hud_minimized"),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xCC120E22)),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = SolidColor(Color(0x44E0AAFF))
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 7.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Privacy Shield",
                        tint = Color(0xFF06D6A0),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = String.format(
                            Locale.US,
                            "ITA° %+.1f°  •  MI %.1f  •  %dK",
                            metrics?.itaDegrees ?: 38.5f,
                            metrics?.melaninIndex ?: 34.2f,
                            metrics?.ambientKelvin ?: 5400
                        ),
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(
                        text = "Show Metrics",
                        color = Color(0xFFE0AAFF),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = "Expand",
                        tint = Color(0xFFE0AAFF),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    } else {
        // Full expanded HUD card with minimize button
        Card(
            modifier = modifier
                .fillMaxWidth()
                .testTag("telemetry_hud_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xDD120E22)
            ),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = SolidColor(Color(0x44E0AAFF))
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                // Header row with privacy badge + minimize control
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Privacy Shield",
                            tint = Color(0xFF06D6A0),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "On-Device Processing",
                            color = Color(0xFF06D6A0),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val deltaE = metrics?.deltaE00 ?: 1.2f
                        val deltaEValid = deltaE < 2.5f
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (deltaEValid) Color(0x3306D6A0) else Color(0x33FFD166)
                        ) {
                            Text(
                                text = String.format(Locale.US, "ΔE %.2f %s", deltaE, if (deltaEValid) "✓" else "~"),
                                color = if (deltaEValid) Color(0xFF06D6A0) else Color(0xFFFFD166),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Minimize button
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0x33FFFFFF),
                            modifier = Modifier.clickable { isExpanded = false }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "Hide",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Minimize",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }

            Spacer(modifier = Modifier.height(10.dp))

            // 4-cell telemetry grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // ITA Cell
                TelemetryPill(
                    title = "ITA",
                    value = String.format(Locale.US, "%+.1f°", metrics?.itaDegrees ?: 38.5f),
                    subtext = metrics?.itaClassification ?: "Intermediate",
                    highlightColor = Color(0xFFFFD166),
                    modifier = Modifier.weight(1f)
                )

                // Melanin Index Cell
                TelemetryPill(
                    title = "Melanin",
                    value = String.format(Locale.US, "%.1f", metrics?.melaninIndex ?: 34.2f),
                    subtext = "Reflectance",
                    highlightColor = Color(0xFFE0AAFF),
                    modifier = Modifier.weight(1f)
                )

                // Erythema Index Cell
                TelemetryPill(
                    title = "Erythema",
                    value = String.format(Locale.US, "%.1f", metrics?.erythemaIndex ?: 28.6f),
                    subtext = "Flow Rate",
                    highlightColor = Color(0xFFFF85A1),
                    modifier = Modifier.weight(1f)
                )

                // Illuminant Kelvin Cell
                TelemetryPill(
                    title = "Lighting",
                    value = "${metrics?.ambientKelvin ?: 5400}K",
                    subtext = "Normalized",
                    highlightColor = Color(0xFF4CC9F0),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Undertone indicator row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val skinHex = metrics?.skinLab?.toRgbHex() ?: "#D6A374"
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(Color(android.graphics.Color.parseColor(skinHex)))
                            .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Undertone: ${metrics?.undertoneDescription ?: "Warm Golden"} (${String.format(Locale.US, "%.0f°", metrics?.undertoneHueAngle ?: 58f)})",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Text(
                    text = "Normalized",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 10.sp
                )
            }
        }
    }
}
}

@Composable
private fun TelemetryPill(
    title: String,
    value: String,
    subtext: String,
    highlightColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = Color(0x33FFFFFF)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Normal
            )
            Text(
                text = value,
                color = highlightColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtext,
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 9.sp,
                maxLines = 1
            )
        }
    }
}
