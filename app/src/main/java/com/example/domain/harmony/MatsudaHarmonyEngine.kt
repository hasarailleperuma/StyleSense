package com.example.domain.harmony

import com.example.domain.colorimetry.ColorSpaceConverter
import com.example.domain.model.*
import kotlin.math.*

object MatsudaHarmonyEngine {

    enum class HarmonyType(val displayName: String, val bonusScore: Int) {
        MONOCHROMATIC("Monochromatic Tonal", 15),
        ANALOGOUS("Analogous Flow", 18),
        COMPLEMENTARY("Dynamic Complementary", 20),
        TRIADIC("Vibrant Triadic", 16),
        NEUTRAL_ACCENT("Neutral & Accent", 14),
        DISCORDANT("Discordant", -15)
    }

    /**
     * Evaluates outfit compatibility against the user's 12-season palette, selected occasion, and body silhouette.
     */
    fun evaluateOutfit(
        garmentColors: List<ColorSwatch>,
        userSeason: SeasonType,
        occasion: Occasion,
        silhouette: BodySilhouette?
    ): OutfitMatchResult {
        if (garmentColors.isEmpty()) {
            return OutfitMatchResult(
                overallScore = 50,
                harmonyCategory = "Neutral Base",
                seasonAlignment = "Neutral",
                plainEnglishRationale = "Select or scan an item to see personalized styling advice.",
                occasionVerdict = "Pending Selection",
                recommendedAccessories = listOf("Classic watch", "Tonal leather belt"),
                dominantColors = emptyList(),
                deltaEToSeasonPalette = 10f
            )
        }

        val primaryGarment = garmentColors.first()
        val primaryLab = hexToLab(primaryGarment.hex)

        // 1. Calculate minimum Delta E00 to user's 12-season primary & accent palette
        val fullPalette = userSeason.palette.primaryColors + userSeason.palette.accentColors + userSeason.palette.neutralColors
        val avoidPalette = userSeason.palette.colorsToAvoid

        var minDeltaEToAllowed = Float.MAX_VALUE
        var closestPaletteSwatch: ColorSwatch? = null

        for (swatch in fullPalette) {
            val sLab = hexToLab(swatch.hex)
            val dE = ColorSpaceConverter.calculateDeltaE00(primaryLab, sLab)
            if (dE < minDeltaEToAllowed) {
                minDeltaEToAllowed = dE
                closestPaletteSwatch = swatch
            }
        }

        // Check if color is close to an "avoid" color
        var minDeltaEToAvoid = Float.MAX_VALUE
        for (swatch in avoidPalette) {
            val sLab = hexToLab(swatch.hex)
            val dE = ColorSpaceConverter.calculateDeltaE00(primaryLab, sLab)
            if (dE < minDeltaEToAvoid) {
                minDeltaEToAvoid = dE
            }
        }

        // 2. Matsuda Color Harmony Formula computation
        val harmonyType = if (garmentColors.size > 1) {
            val secondLab = hexToLab(garmentColors[1].hex)
            classifyMatsudaHarmony(primaryLab, secondLab)
        } else {
            // Compare against user's best accent color
            val bestAccentLab = hexToLab(userSeason.palette.accentColors.first().hex)
            classifyMatsudaHarmony(primaryLab, bestAccentLab)
        }

        // 3. Compute Base Compatibility Score (0 - 100)
        // delta E < 5 is near identical, < 12 is harmonious, > 25 is discordant
        var baseScore = when {
            minDeltaEToAllowed < 6.0f -> 92
            minDeltaEToAllowed < 12.0f -> 84
            minDeltaEToAllowed < 20.0f -> 72
            minDeltaEToAllowed < 28.0f -> 58
            else -> 42
        }

        // Penalty if close to avoid colors
        if (minDeltaEToAvoid < 10.0f) {
            baseScore -= 22
        }

        // Add harmony bonus
        baseScore += harmonyType.bonusScore

        // Adjust for occasion formality vs palette intensity
        val occasionAlignment: String
        when (occasion) {
            Occasion.BUSINESS_INTERVIEW -> {
                // Prefers neutrals and deep tones; slightly dock loud neons
                if (primaryLab.l > 80f && (primaryLab.a * primaryLab.a + primaryLab.b * primaryLab.b) > 900) {
                    baseScore -= 10
                    occasionAlignment = "High-energy for interview setting"
                } else {
                    baseScore += 5
                    occasionAlignment = "Executive & Poised"
                }
            }
            Occasion.FORMAL_DINNER -> {
                if (minDeltaEToAllowed < 15f) baseScore += 6
                occasionAlignment = "Sophisticated Gala Presence"
            }
            Occasion.WEDDING -> {
                // If pure bridal white (L > 94 and low chroma), warn user
                if (primaryLab.l > 94f && abs(primaryLab.a) < 5 && abs(primaryLab.b) < 8) {
                    baseScore -= 30
                    occasionAlignment = "Caution: Too close to bridal white"
                } else {
                    baseScore += 5
                    occasionAlignment = "Celebratory & Elegant"
                }
            }
            Occasion.PARTY_NIGHT_OUT -> {
                // Rewards vivid accent colors
                if ((primaryLab.a * primaryLab.a + primaryLab.b * primaryLab.b) > 400) {
                    baseScore += 8
                    occasionAlignment = "Striking & Magnetic"
                } else {
                    occasionAlignment = "Chic Minimalist"
                }
            }
            Occasion.CASUAL_WEEKEND -> {
                baseScore += 4
                occasionAlignment = "Relaxed & Effortless"
            }
        }

        val finalScore = baseScore.coerceIn(25, 99)

        val seasonAlignment = when {
            finalScore >= 85 -> "Signature Match"
            finalScore >= 70 -> "Harmonious Flow"
            finalScore >= 55 -> "Complementary Accent"
            else -> "Color Clash"
        }

        // 4. Generate Plain-English Natural Language Rationale
        val rationale = buildPlainEnglishRationale(
            primaryGarment = primaryGarment,
            closestPaletteSwatch = closestPaletteSwatch,
            userSeason = userSeason,
            occasion = occasion,
            harmonyType = harmonyType,
            minDeltaEToAvoid = minDeltaEToAvoid,
            score = finalScore,
            silhouette = silhouette
        )

        // 5. Recommended accessories based on season metals and occasion
        val recommendedAccessories = mutableListOf<String>()
        recommendedAccessories.add(userSeason.palette.bestMetals.first())
        when (occasion) {
            Occasion.BUSINESS_INTERVIEW -> {
                recommendedAccessories.add("Structured leather tote")
                recommendedAccessories.add("Minimalist ${userSeason.palette.bestMetals.first().lowercase()} cuff")
            }
            Occasion.FORMAL_DINNER -> {
                recommendedAccessories.add("Statement ${userSeason.palette.bestMetals.getOrElse(1) { "silver" }.lowercase()} earrings")
                recommendedAccessories.add("Silk satin clutch")
            }
            Occasion.WEDDING -> {
                recommendedAccessories.add("Delicate pendant necklace")
                recommendedAccessories.add("Strappy metallic sandals")
            }
            Occasion.PARTY_NIGHT_OUT -> {
                recommendedAccessories.add("Bold geometric clutch")
                recommendedAccessories.add("Stacked ${userSeason.palette.bestMetals.first().lowercase()} rings")
            }
            Occasion.CASUAL_WEEKEND -> {
                recommendedAccessories.add("Tortoiseshell sunglasses")
                recommendedAccessories.add("Canvas crossbody strap")
            }
        }

        return OutfitMatchResult(
            overallScore = finalScore,
            harmonyCategory = harmonyType.displayName,
            seasonAlignment = seasonAlignment,
            plainEnglishRationale = rationale,
            occasionVerdict = occasionAlignment,
            recommendedAccessories = recommendedAccessories,
            dominantColors = garmentColors,
            deltaEToSeasonPalette = minDeltaEToAllowed
        )
    }

    private fun classifyMatsudaHarmony(lab1: LabColor, lab2: LabColor): HarmonyType {
        val h1 = ColorSpaceConverter.calculateHueAngle(lab1.a, lab1.b)
        val h2 = ColorSpaceConverter.calculateHueAngle(lab2.a, lab2.b)

        val c1 = sqrt(lab1.a * lab1.a + lab1.b * lab1.b)
        val c2 = sqrt(lab2.a * lab2.a + lab2.b * lab2.b)

        // If one is very low chroma (neutral like white, black, grey, cream)
        if (c1 < 10 || c2 < 10) {
            return HarmonyType.NEUTRAL_ACCENT
        }

        var diff = abs(h1 - h2)
        if (diff > 180) diff = 360 - diff

        return when {
            diff <= 20 -> HarmonyType.MONOCHROMATIC
            diff in 21.0..65.0 -> HarmonyType.ANALOGOUS
            diff in 105.0..135.0 -> HarmonyType.TRIADIC
            diff in 155.0..180.0 -> HarmonyType.COMPLEMENTARY
            else -> HarmonyType.ANALOGOUS
        }
    }

    private fun buildPlainEnglishRationale(
        primaryGarment: ColorSwatch,
        closestPaletteSwatch: ColorSwatch?,
        userSeason: SeasonType,
        occasion: Occasion,
        harmonyType: HarmonyType,
        minDeltaEToAvoid: Float,
        score: Int,
        silhouette: BodySilhouette?
    ): String {
        val garmentName = primaryGarment.name
        val seasonTitle = userSeason.title
        val occasionTitle = occasion.title

        val colorStatement = if (minDeltaEToAvoid < 10f) {
            "This ${garmentName.lowercase()} shade borders on tones that can cast dull shadows against your $seasonTitle undertone. "
        } else if (closestPaletteSwatch != null) {
            "This ${garmentName.lowercase()} shade aligns closely with your ideal ${closestPaletteSwatch.name} tone, enhancing your natural skin luminosity. "
        } else {
            "This ${garmentName.lowercase()} shade pairs cleanly with your $seasonTitle palette. "
        }

        val occasionStatement = when (occasion) {
            Occasion.BUSINESS_INTERVIEW ->
                "For $occasionTitle, it projects clean authority while keeping focus on your face."
            Occasion.FORMAL_DINNER ->
                "Under evening lighting, the depth of this tone adds an elegant, luminous presence."
            Occasion.WEDDING ->
                "It strikes a joyful, sophisticated celebratory balance without competing with the bridal party."
            Occasion.PARTY_NIGHT_OUT ->
                "Its saturation creates dynamic visual interest that stands out in festive atmospheres."
            Occasion.CASUAL_WEEKEND ->
                "It feels relaxed and effortlessly chic for daytime wear."
        }

        val silhouetteStatement = silhouette?.let {
            " For your ${it.title} silhouette, ${it.stylingGuidance.lowercase()}"
        } ?: ""

        val metalStatement = " Pair with ${userSeason.palette.bestMetals.first().lowercase()} jewelry to complete the look."

        return "$colorStatement$occasionStatement$silhouetteStatement$metalStatement"
    }

    /**
     * Evaluates compatibility between two garments (e.g. top and bottom) for color harmony and occasion alignment.
     */
    fun evaluateTwoGarmentPair(
        topColors: List<ColorSwatch>,
        bottomColors: List<ColorSwatch>,
        userSeason: SeasonType,
        occasion: Occasion,
        silhouette: BodySilhouette?
    ): OutfitMatchResult {
        if (topColors.isEmpty() || bottomColors.isEmpty()) {
            return evaluateOutfit(topColors.ifEmpty { bottomColors }, userSeason, occasion, silhouette)
        }

        val topPrimary = topColors.first()
        val bottomPrimary = bottomColors.first()
        val topLab = hexToLab(topPrimary.hex)
        val bottomLab = hexToLab(bottomPrimary.hex)

        // 1. Mutual Top vs Bottom Matsuda harmony
        val mutualHarmony = classifyMatsudaHarmony(topLab, bottomLab)

        // 2. Season alignment of both items
        val topSingle = evaluateOutfit(topColors, userSeason, occasion, silhouette)
        val bottomSingle = evaluateOutfit(bottomColors, userSeason, occasion, silhouette)

        // 3. Combined score
        val avgBase = (topSingle.overallScore + bottomSingle.overallScore) / 2
        val mutualBonus = when (mutualHarmony) {
            HarmonyType.COMPLEMENTARY -> 8
            HarmonyType.ANALOGOUS -> 6
            HarmonyType.MONOCHROMATIC -> 5
            HarmonyType.NEUTRAL_ACCENT -> 4
            HarmonyType.TRIADIC -> 5
            HarmonyType.DISCORDANT -> -12
        }
        val combinedScore = (avgBase + mutualBonus).coerceIn(40, 99)

        val pairCategory = "Top & Bottom: ${mutualHarmony.displayName}"
        val alignment = if (combinedScore >= 85) "Exquisite Harmony" else if (combinedScore >= 70) "Coordinated Flow" else "Needs Accent Neutral"

        val rationale = "Your ${topPrimary.name.lowercase()} top and ${bottomPrimary.name.lowercase()} bottom create a striking ${mutualHarmony.displayName.lowercase()} balance. " +
                "For ${occasion.title}, this pairing creates flattering proportion contrast. " +
                (silhouette?.let { "For your ${it.title}, this balance ${it.stylingGuidance.lowercase()} " } ?: "") +
                "Accent with ${userSeason.palette.bestMetals.first()} jewelry and coordinated ${userSeason.palette.neutralColors.first().name.lowercase()} footwear."

        val jewelryList = listOf(
            "${userSeason.palette.bestMetals.first()} statement earrings",
            "${userSeason.palette.bestMetals.getOrElse(1) { "Polished gold" }} layered chain",
            "${userSeason.palette.neutralColors.first().name} leather bag",
            "Coordinated tonal belt"
        )

        val combinedSwatches = listOf(
            ColorSwatch("Top: ${topPrimary.name}", topPrimary.hex, topPrimary.color),
            ColorSwatch("Bottom: ${bottomPrimary.name}", bottomPrimary.hex, bottomPrimary.color)
        )

        return OutfitMatchResult(
            overallScore = combinedScore,
            harmonyCategory = pairCategory,
            seasonAlignment = alignment,
            plainEnglishRationale = rationale,
            occasionVerdict = topSingle.occasionVerdict,
            recommendedAccessories = jewelryList,
            dominantColors = combinedSwatches,
            deltaEToSeasonPalette = (topSingle.deltaEToSeasonPalette + bottomSingle.deltaEToSeasonPalette) / 2f
        )
    }

    private fun hexToLab(hex: String): LabColor {
        val clean = hex.removePrefix("#")
        val r = clean.substring(0, 2).toIntOrNull(16) ?: 128
        val g = clean.substring(2, 4).toIntOrNull(16) ?: 128
        val b = clean.substring(4, 6).toIntOrNull(16) ?: 128
        return ColorSpaceConverter.rgbToLab(r, g, b)
    }
}
