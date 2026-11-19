package com.example.domain.colorimetry

import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.ui.graphics.Color as ComposeColor
import com.example.domain.model.ColorSwatch
import com.example.domain.model.GarmentCategory
import com.example.domain.model.LabColor
import kotlin.math.*

object DominantColorExtractor {

    data class GarmentAnalysis(
        val category: GarmentCategory,
        val dominantColors: List<ColorSwatch>,
        val primaryHex: String
    )

    /**
     * Extracts dominant color swatches and garment category using K-Means clustering in CIE L*a*b* space.
     */
    fun extractGarmentColors(bitmap: Bitmap, k: Int = 4): GarmentAnalysis {
        val width = bitmap.width
        val height = bitmap.height

        // 1. Determine inferred category by aspect ratio and region coverage
        val aspectRatio = height.toFloat() / max(1f, width.toFloat())
        val category = when {
            aspectRatio > 1.8f -> GarmentCategory.DRESSES
            aspectRatio > 1.3f -> GarmentCategory.TROUSERS
            aspectRatio < 0.9f -> GarmentCategory.ACCESSORIES
            else -> GarmentCategory.TOPS
        }

        // 2. Sample representative pixels (downsample for fast computation < 15ms)
        val step = max(2, min(width, height) / 40)
        val sampledRgb = mutableListOf<Triple<Int, Int, Int>>()

        for (x in (width * 0.15f).toInt() until (width * 0.85f).toInt() step step) {
            for (y in (height * 0.15f).toInt() until (height * 0.85f).toInt() step step) {
                val pixel = bitmap.getPixel(x, y)
                val r = Color.red(pixel)
                val g = Color.green(pixel)
                val b = Color.blue(pixel)

                // Skip near-white background or transparent borders
                val lum = 0.299 * r + 0.587 * g + 0.114 * b
                if (lum > 245 || lum < 10) continue

                sampledRgb.add(Triple(r, g, b))
            }
        }

        if (sampledRgb.isEmpty()) {
            val fallback = ColorSwatch("Navy Blue", "#1A2A3A", ComposeColor(0xFF1A2A3A))
            return GarmentAnalysis(category, listOf(fallback), "#1A2A3A")
        }

        // Convert sampled pixels to Lab
        val sampledLab = sampledRgb.map {
            ColorSpaceConverter.rgbToLab(it.first, it.second, it.third)
        }

        // 3. Run K-Means Clustering (3 iterations are sufficient for fast convergence)
        val centroids = mutableListOf<LabColor>()
        val clusterCount = min(k, sampledLab.size)

        // Initialize centroids with spread
        val stride = sampledLab.size / clusterCount
        for (i in 0 until clusterCount) {
            centroids.add(sampledLab[i * stride])
        }

        val assignments = IntArray(sampledLab.size)
        for (iter in 0 until 4) {
            // Assign to nearest centroid
            for (p in sampledLab.indices) {
                var bestDist = Double.MAX_VALUE
                var bestIndex = 0
                for (c in centroids.indices) {
                    val dL = (sampledLab[p].l - centroids[c].l).toDouble()
                    val da = (sampledLab[p].a - centroids[c].a).toDouble()
                    val db = (sampledLab[p].b - centroids[c].b).toDouble()
                    val dist = dL * dL + da * da + db * db
                    if (dist < bestDist) {
                        bestDist = dist
                        bestIndex = c
                    }
                }
                assignments[p] = bestIndex
            }

            // Recompute centroids
            for (c in centroids.indices) {
                var sumL = 0.0
                var suma = 0.0
                var sumb = 0.0
                var count = 0
                for (p in sampledLab.indices) {
                    if (assignments[p] == c) {
                        sumL += sampledLab[p].l
                        suma += sampledLab[p].a
                        sumb += sampledLab[p].b
                        count++
                    }
                }
                if (count > 0) {
                    centroids[c] = LabColor(
                        (sumL / count).toFloat(),
                        (suma / count).toFloat(),
                        (sumb / count).toFloat()
                    )
                }
            }
        }

        // Count cluster sizes to sort by dominance
        val clusterCounts = IntArray(centroids.size)
        for (a in assignments) {
            clusterCounts[a]++
        }

        val sortedIndices = centroids.indices.sortedByDescending { clusterCounts[it] }

        val dominantSwatches = sortedIndices.mapIndexed { idx, cIdx ->
            val lab = centroids[cIdx]
            val hex = lab.toRgbHex()
            val name = nameHexColor(hex, idx)
            val parsedColor = try {
                ComposeColor(android.graphics.Color.parseColor(hex))
            } catch (e: Exception) {
                ComposeColor(0xFF333333)
            }
            ColorSwatch(name, hex, parsedColor)
        }

        val primaryHex = dominantSwatches.firstOrNull()?.hex ?: "#333333"
        return GarmentAnalysis(category, dominantSwatches, primaryHex)
    }

    private fun nameHexColor(hex: String, rank: Int): String {
        val clean = hex.removePrefix("#")
        if (clean.length < 6) return "Color ${rank + 1}"
        val r = clean.substring(0, 2).toIntOrNull(16) ?: 128
        val g = clean.substring(2, 4).toIntOrNull(16) ?: 128
        val b = clean.substring(4, 6).toIntOrNull(16) ?: 128

        val maxC = max(r, max(g, b))
        val minC = min(r, min(g, b))
        val delta = maxC - minC

        if (delta < 20) {
            return when {
                r > 200 -> "Crisp White / Cream"
                r > 140 -> "Light Grey"
                r > 70 -> "Slate Charcoal"
                else -> "Onyx Black"
            }
        }

        return when (maxC) {
            r -> when {
                g > 150 && b < 100 -> "Warm Mustard / Ochre"
                g > 80 && b < 80 -> "Terracotta / Rust"
                b > 120 -> "Berry Magenta"
                else -> "Crimson Red"
            }
            g -> when {
                b > 150 -> "Aquamarine / Teal"
                r > 120 -> "Olive Chartreuse"
                else -> "Emerald Forest"
            }
            else -> when {
                r > 120 -> "Royal Violet / Indigo"
                g > 150 -> "Sky Cyan"
                else -> "Navy Cobalt"
            }
        }
    }
}
