package com.example.presentation.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.domain.model.BiometricMetrics
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun FaceMeshOverlay(
    metrics: BiometricMetrics?,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "scan_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    val scanY by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scan_y"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        val centerX = w * 0.5f
        val centerY = h * 0.48f
        val ovalRadiusX = w * 0.32f
        val ovalRadiusY = h * 0.24f

        // 1. Face Framing Oval (Anatomical Guide)
        val ovalStrokeColor = if (metrics != null && metrics.deltaE00 < 2.5f) {
            Color(0xFF06D6A0).copy(alpha = pulseAlpha) // Green when ΔE00 < 2.5 target is met
        } else {
            Color(0xFFFFD166).copy(alpha = pulseAlpha)
        }

        drawOval(
            color = ovalStrokeColor,
            topLeft = Offset(centerX - ovalRadiusX, centerY - ovalRadiusY),
            size = Size(ovalRadiusX * 2f, ovalRadiusY * 2f),
            style = Stroke(
                width = 3.5f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(24f, 12f), 0f)
            )
        )

        // 2. Facial Mesh Topological Wireframe Points
        val meshColor = Color.White.copy(alpha = 0.35f)
        val meshPoints = mutableListOf<Offset>()

        // Jawline contour arc
        val jawSteps = 16
        for (i in 0..jawSteps) {
            val theta = PI * 0.15 + (PI * 0.70 * i / jawSteps)
            val px = centerX + cos(theta).toFloat() * (ovalRadiusX * 0.95f)
            val py = centerY + sin(theta).toFloat() * (ovalRadiusY * 1.05f)
            meshPoints.add(Offset(px, py))
        }

        // Eyebrow and forehead arches
        for (i in -4..4) {
            val offsetFx = centerX + (i * ovalRadiusX * 0.18f)
            val offsetFy = centerY - (ovalRadiusY * 0.65f) - (cos(i * 0.35f).toFloat() * 12f)
            meshPoints.add(Offset(offsetFx, offsetFy))
        }

        // Bridge of nose and lip nodes
        for (i in -2..4) {
            val ny = centerY + (i * ovalRadiusY * 0.14f)
            meshPoints.add(Offset(centerX, ny))
            meshPoints.add(Offset(centerX - 18f, ny + 8f))
            meshPoints.add(Offset(centerX + 18f, ny + 8f))
        }

        meshPoints.forEach { pt ->
            drawCircle(
                color = meshColor,
                radius = 2.0f,
                center = pt
            )
        }

        // 3. Anatomical ROIs Highlighting
        // Left Cheek ROI
        val leftCheekX = centerX - (ovalRadiusX * 0.52f)
        val leftCheekY = centerY + (ovalRadiusY * 0.08f)
        val cheekBoxW = w * 0.16f
        val cheekBoxH = h * 0.09f

        drawRoundRect(
            color = Color(0xFFE0AAFF).copy(alpha = 0.85f),
            topLeft = Offset(leftCheekX - cheekBoxW / 2, leftCheekY - cheekBoxH / 2),
            size = Size(cheekBoxW, cheekBoxH),
            cornerRadius = CornerRadius(12f, 12f),
            style = Stroke(width = 2.5f)
        )

        // Right Cheek ROI
        val rightCheekX = centerX + (ovalRadiusX * 0.52f)
        val rightCheekY = centerY + (ovalRadiusY * 0.08f)

        drawRoundRect(
            color = Color(0xFFE0AAFF).copy(alpha = 0.85f),
            topLeft = Offset(rightCheekX - cheekBoxW / 2, rightCheekY - cheekBoxH / 2),
            size = Size(cheekBoxW, cheekBoxH),
            cornerRadius = CornerRadius(12f, 12f),
            style = Stroke(width = 2.5f)
        )

        // Infraorbital Under-Eye Zone (Lower eyelid to upper cheek crest)
        val underEyeW = ovalRadiusX * 1.15f
        val underEyeH = h * 0.045f
        val underEyeY = centerY - (ovalRadiusY * 0.12f)

        drawRoundRect(
            color = Color(0xFF06D6A0).copy(alpha = 0.75f),
            topLeft = Offset(centerX - underEyeW / 2, underEyeY),
            size = Size(underEyeW, underEyeH),
            cornerRadius = CornerRadius(8f, 8f),
            style = Stroke(
                width = 2.0f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
            )
        )

        // 4. Sweeping Holographic Laser Line
        val currentScanY = h * scanY
        drawLine(
            color = Color(0xFFC77DFF).copy(alpha = 0.6f),
            start = Offset(centerX - ovalRadiusX * 0.9f, currentScanY),
            end = Offset(centerX + ovalRadiusX * 0.9f, currentScanY),
            strokeWidth = 2.5f
        )
    }
}
