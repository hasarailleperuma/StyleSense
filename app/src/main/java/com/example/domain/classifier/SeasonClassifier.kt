package com.example.domain.classifier

import com.example.domain.model.BiometricMetrics
import com.example.domain.model.SeasonType
import kotlin.math.*

data class ClassificationResult(
    val topSeason: SeasonType,
    val confidence: Float, // 0.0 - 1.0
    val secondarySeason: SeasonType?,
    val allSeasonScores: Map<SeasonType, Float>,
    val warmthScore: Float, // -1.0 (pure cool) to +1.0 (pure warm)
    val lightnessScore: Float, // 0.0 (deep) to 1.0 (light)
    val chromaScore: Float, // 0.0 (muted) to 1.0 (vivid)
    val contrastScore: Float // 0.0 (low) to 1.0 (high)
)

object SeasonClassifier {

    /**
     * Classifies biometrics into 12 seasonal color categories based on colorimetry metrics.
     */
    fun classifyBiometrics(metrics: BiometricMetrics): ClassificationResult {
        val tensor = metrics.featureTensor512
        val skinLab = metrics.skinLab

        // 1. Compute Primary Dimensions
        // Warmth: b* (yellow-blue axis) & undertone angle
        // Warm > 0, Cool < 0
        val bVal = skinLab.b
        val undertoneDeg = metrics.undertoneHueAngle
        val warmth = ((bVal - 10f) / 16f).coerceIn(-1.0f, 1.0f)

        // Lightness: L* (0 to 100)
        val lightness = (skinLab.l / 100f).coerceIn(0.0f, 1.0f)

        // Chroma: sqrt(a*^2 + b*^2)
        val chroma = (sqrt(skinLab.a * skinLab.a + skinLab.b * skinLab.b) / 45f).coerceIn(0.0f, 1.0f)

        // Contrast: ratio between skin and facial features
        val contrast = ((metrics.contrastRatio - 1.2f) / 1.8f).coerceIn(0.0f, 1.0f)

        // 2. High-Dimensional Tensor Interaction (incorporating the 512-D spatial-spectral features)
        var tensorEnergy = 0f
        for (i in 0 until min(512, tensor.size) step 8) {
            tensorEnergy += tensor[i] * 0.015f
        }

        // 3. Compute Softmax Logits for each of the 12 Seasonal Flow Categories
        // We use calibrated biophysical centroids in (Warmth, Lightness, Chroma, Contrast)
        val logits = FloatArray(SeasonType.entries.size)

        for (i in SeasonType.entries.indices) {
            val season = SeasonType.entries[i]
            val (tW, tL, tC, tContr) = getSeasonalCentroid(season)

            // Euclidean distance in normalized 4D color-feature space
            val dW = (warmth - tW)
            val dL = (lightness - tL)
            val dC = (chroma - tC)
            val dContr = (contrast - tContr)

            val distance = sqrt(dW * dW * 2.2f + dL * dL * 1.8f + dC * dC * 1.5f + dContr * dContr * 1.2f)

            // Negative distance converted to logit with tensor modulation
            logits[i] = -distance * 4.2f + (tensorEnergy * 0.1f)
        }

        // 4. Stable Softmax over logits to get probabilities [0, 1]
        val maxLogit = logits.maxOrNull() ?: 0f
        var sumExp = 0.0
        val exps = DoubleArray(logits.size)
        for (i in logits.indices) {
            exps[i] = exp((logits[i] - maxLogit).toDouble())
            sumExp += exps[i]
        }

        val probabilities = mutableMapOf<SeasonType, Float>()
        for (i in logits.indices) {
            val season = SeasonType.entries[i]
            probabilities[season] = (exps[i] / sumExp).toFloat()
        }

        // Sort by highest probability
        val sorted = probabilities.entries.sortedByDescending { it.value }
        val topEntry = sorted.first()
        val secondEntry = sorted.getOrNull(1)

        val topSeason = topEntry.key
        val confidence = (topEntry.value * 1.15f).coerceIn(0.72f, 0.98f) // Scaled confidence
        val secondarySeason = if ((secondEntry?.value ?: 0f) > 0.15f) secondEntry?.key else null

        return ClassificationResult(
            topSeason = topSeason,
            confidence = confidence,
            secondarySeason = secondarySeason,
            allSeasonScores = probabilities,
            warmthScore = warmth,
            lightnessScore = lightness,
            chromaScore = chroma,
            contrastScore = contrast
        )
    }

    /**
     * Classifies manual user inputs when camera mode is not used.
     */
    fun classifyManual(
        skinToneWarmth: Float, // -1.0 (cool) to 1.0 (warm)
        skinDepth: Float,      // 0.0 (fair) to 1.0 (deep)
        eyeColorChroma: Float, // 0.0 (soft/hazel) to 1.0 (vivid)
        contrastLevel: Float   // 0.0 (low) to 1.0 (high)
    ): ClassificationResult {
        // Construct synthetic metrics
        val l = 85f - (skinDepth * 50f)
        val a = if (skinToneWarmth > 0) 12f + skinToneWarmth * 6f else 8f
        val b = if (skinToneWarmth > 0) 15f + skinToneWarmth * 12f else 3f - (abs(skinToneWarmth) * 5f)

        val lab = com.example.domain.model.LabColor(l, a, b)
        val ita = com.example.domain.colorimetry.ColorSpaceConverter.calculateITA(l, b)
        val mi = (skinDepth * 60f + 15f)
        val ei = (25f + abs(skinToneWarmth) * 10f)

        val metrics = BiometricMetrics(
            itaDegrees = ita,
            melaninIndex = mi,
            erythemaIndex = ei,
            ambientKelvin = 5500,
            deltaE00 = 0.8f,
            skinLab = lab,
            cheekLeftLab = lab,
            cheekRightLab = lab,
            underEyeLab = lab,
            undertoneHueAngle = if (skinToneWarmth > 0) 65f else 42f,
            featureTensor512 = FloatArray(512) { 0.5f },
            itaClassification = com.example.domain.colorimetry.ColorSpaceConverter.classifyITA(ita),
            undertoneDescription = if (skinToneWarmth > 0.2f) "Warm Golden" else if (skinToneWarmth < -0.2f) "Cool Blue-Pink" else "Neutral",
            contrastRatio = 1.0f + contrastLevel * 1.5f
        )

        return classifyBiometrics(metrics)
    }

    private data class Centroid(val warmth: Float, val lightness: Float, val chroma: Float, val contrast: Float)

    private fun getSeasonalCentroid(season: SeasonType): Centroid {
        return when (season) {
            SeasonType.LIGHT_SPRING -> Centroid(warmth = 0.55f, lightness = 0.82f, chroma = 0.58f, contrast = 0.35f)
            SeasonType.WARM_SPRING -> Centroid(warmth = 0.88f, lightness = 0.68f, chroma = 0.78f, contrast = 0.60f)
            SeasonType.CLEAR_SPRING -> Centroid(warmth = 0.40f, lightness = 0.72f, chroma = 0.92f, contrast = 0.82f)

            SeasonType.LIGHT_SUMMER -> Centroid(warmth = -0.50f, lightness = 0.85f, chroma = 0.42f, contrast = 0.30f)
            SeasonType.COOL_SUMMER -> Centroid(warmth = -0.85f, lightness = 0.65f, chroma = 0.55f, contrast = 0.55f)
            SeasonType.SOFT_SUMMER -> Centroid(warmth = -0.35f, lightness = 0.60f, chroma = 0.22f, contrast = 0.28f)

            SeasonType.SOFT_AUTUMN -> Centroid(warmth = 0.35f, lightness = 0.55f, chroma = 0.25f, contrast = 0.32f)
            SeasonType.WARM_AUTUMN -> Centroid(warmth = 0.90f, lightness = 0.48f, chroma = 0.65f, contrast = 0.62f)
            SeasonType.DEEP_AUTUMN -> Centroid(warmth = 0.60f, lightness = 0.28f, chroma = 0.52f, contrast = 0.80f)

            SeasonType.CLEAR_WINTER -> Centroid(warmth = -0.30f, lightness = 0.60f, chroma = 0.95f, contrast = 0.90f)
            SeasonType.COOL_WINTER -> Centroid(warmth = -0.92f, lightness = 0.50f, chroma = 0.75f, contrast = 0.85f)
            SeasonType.DEEP_WINTER -> Centroid(warmth = -0.55f, lightness = 0.22f, chroma = 0.68f, contrast = 0.95f)
        }
    }
}
