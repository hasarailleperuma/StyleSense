package com.example.domain.colorimetry

import android.graphics.Bitmap
import android.graphics.Color
import com.example.domain.model.LabColor
import kotlin.math.*

object IlluminantNormalizer {

    /**
     * Estimates Correlated Color Temperature (CCT in Kelvin) using McCamy's approximation
     * from estimated ambient white balance (2700K to 6500K).
     */
    fun estimateKelvin(avgR: Float, avgG: Float, avgB: Float): Int {
        val sum = max(1e-5f, avgR + avgG + avgB)
        val x = avgR / sum
        val y = avgG / sum

        // McCamy's formula: CCT = 449n^3 + 3525n^2 + 6823.3n + 5520.33
        // where n = (x - 0.3320) / (0.1858 - y)
        val denom = 0.1858 - y
        if (abs(denom) < 1e-4) return 6500

        val n = (x - 0.3320) / denom
        val cct = 449.0 * n.pow(3.0) + 3525.0 * n.pow(2.0) + 6823.3 * n + 5520.33

        return cct.roundToInt().coerceIn(2700, 7500)
    }

    /**
     * Calibrates white point using a reference white surface.
     */
    @Volatile
    var customCalibrationGains: Triple<Float, Float, Float>? = null

    fun calibrateWithWhitePaper(bitmap: Bitmap): Triple<Float, Float, Float> {
        val w = bitmap.width
        val h = bitmap.height
        val startX = (w * 0.40f).toInt()
        val startY = (h * 0.40f).toInt()
        val sampleW = (w * 0.20f).toInt().coerceAtLeast(10)
        val sampleH = (h * 0.20f).toInt().coerceAtLeast(10)

        val pixels = IntArray(sampleW * sampleH)
        bitmap.getPixels(pixels, 0, sampleW, startX, startY, sampleW, sampleH)

        var sumR = 0L
        var sumG = 0L
        var sumB = 0L
        for (p in pixels) {
            sumR += Color.red(p)
            sumG += Color.green(p)
            sumB += Color.blue(p)
        }
        val count = max(1, pixels.size)
        val avgR = (sumR.toFloat() / count).coerceAtLeast(1f)
        val avgG = (sumG.toFloat() / count).coerceAtLeast(1f)
        val avgB = (sumB.toFloat() / count).coerceAtLeast(1f)

        // Target D65 white point (255, 255, 255)
        val gainR = (255f / avgR).coerceIn(0.5f, 2.0f)
        val gainG = (255f / avgG).coerceIn(0.5f, 2.0f)
        val gainB = (255f / avgB).coerceIn(0.5f, 2.0f)

        val gains = Triple(gainR, gainG, gainB)
        customCalibrationGains = gains
        return gains
    }

    /**
     * Von Kries chromatic adaptation to CIE Standard Illuminant D65 (6504K).
     */
    fun adaptToD65(r: Int, g: Int, b: Int, kelvin: Int): Triple<Int, Int, Int> {
        val custom = customCalibrationGains
        if (custom != null) {
            val normR = (r * custom.first).roundToInt().coerceIn(0, 255)
            val normG = (g * custom.second).roundToInt().coerceIn(0, 255)
            val normB = (b * custom.third).roundToInt().coerceIn(0, 255)
            return Triple(normR, normG, normB)
        }
        // Temperature response curve relative to D65
        // Under warm incandescent (e.g. 2700K-3500K), R is inflated and B is deficient.
        // Under cool fluorescent (>6500K), B is high and R is lower.
        val factorR: Float
        val factorG: Float
        val factorB: Float

        if (kelvin < 6500) {
            val t = (6500 - kelvin) / 3800f // 0 at 6500K, 1 at 2700K
            factorR = 1.0f - (0.28f * t)
            factorG = 1.0f - (0.05f * t)
            factorB = 1.0f + (0.42f * t)
        } else {
            val t = (kelvin - 6500) / 1000f
            factorR = 1.0f + (0.08f * t)
            factorG = 1.0f
            factorB = 1.0f - (0.12f * t)
        }

        val normR = (r * factorR).roundToInt().coerceIn(0, 255)
        val normG = (g * factorG).roundToInt().coerceIn(0, 255)
        val normB = (b * factorB).roundToInt().coerceIn(0, 255)

        return Triple(normR, normG, normB)
    }

    /**
     * Fast bilateral filter + highlight suppression on a target pixel sample region.
     * Separates skin illumination highlights (specular shine) from true biophysical pigments.
     */
    fun extractDiffusePigmentRgb(
        pixels: IntArray,
        width: Int,
        height: Int
    ): Triple<Int, Int, Int> {
        if (pixels.isEmpty()) return Triple(180, 140, 120)

        var totalR = 0.0
        var totalG = 0.0
        var totalB = 0.0
        var validPixelCount = 0

        // 1. Identify luminance thresholds to discard specular gloss/highlights
        val lumArray = DoubleArray(pixels.size)
        for (i in pixels.indices) {
            val p = pixels[i]
            val r = Color.red(p)
            val g = Color.green(p)
            val b = Color.blue(p)
            lumArray[i] = 0.2126 * r + 0.7152 * g + 0.0722 * b
        }

        // Calculate median luminance
        val sortedLum = lumArray.clone().apply { sort() }
        val medianLum = sortedLum[sortedLum.size / 2]
        val specularThreshold = min(240.0, medianLum * 1.35)
        val shadowThreshold = max(20.0, medianLum * 0.45)

        // 2. Bilateral spatial-range weighting to extract true diffuse pigment
        for (i in pixels.indices) {
            val lum = lumArray[i]
            // Skip specular highlights and deep occlusion shadows
            if (lum > specularThreshold || lum < shadowThreshold) continue

            val p = pixels[i]
            val r = Color.red(p)
            val g = Color.green(p)
            val b = Color.blue(p)

            // Weight higher for pixels near the median skin reflectance
            val diff = abs(lum - medianLum)
            val weight = exp(-(diff * diff) / (2.0 * 25.0 * 25.0))

            totalR += r * weight
            totalG += g * weight
            totalB += b * weight
            validPixelCount++
        }

        if (validPixelCount == 0) {
            // Fallback to average
            var sumR = 0
            var sumG = 0
            var sumB = 0
            for (p in pixels) {
                sumR += Color.red(p)
                sumG += Color.green(p)
                sumB += Color.blue(p)
            }
            val count = max(1, pixels.size)
            return Triple(sumR / count, sumG / count, sumB / count)
        }

        val rFinal = (totalR / validPixelCount).roundToInt().coerceIn(0, 255)
        val gFinal = (totalG / validPixelCount).roundToInt().coerceIn(0, 255)
        val bFinal = (totalB / validPixelCount).roundToInt().coerceIn(0, 255)

        return Triple(rFinal, gFinal, bFinal)
    }
}
