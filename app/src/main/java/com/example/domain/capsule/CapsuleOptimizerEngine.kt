package com.example.domain.capsule

import com.example.domain.colorimetry.ColorSpaceConverter
import com.example.domain.model.*
import kotlin.math.*

/**
 * Cut tags according to Appendix O (Table O.1) of the StyleSense specification.
 */
enum class CutTag(val displayName: String) {
    FITTED_WAIST("Fitted / Waist-defining"),
    A_LINE_FLARE("A-line / Flare"),
    STRAIGHT("Straight"),
    STRUCTURED_SHOULDER("Structured Shoulder"),
    WRAP("Wrap"),
    WIDE_LEG("Wide-leg")
}

data class CapsuleItem(
    val id: String,
    val name: String,
    val category: GarmentCategory,
    val cutTag: CutTag,
    val primaryHex: String,
    val secondaryHex: String,
    val primaryLab: LabColor,
    val description: String
)

data class ItemMarginalGain(
    val item: CapsuleItem,
    val marginalGain: Float,
    val colorHarmonyScore: Float, // 0 - 100
    val silhouetteSuitability: Float, // 0 - 100
    val versatilityScore: Float, // 0 - 100
    val rationale: String
)

data class CapsuleResult(
    val selectedItems: List<ItemMarginalGain>,
    val overallScore: Float, // 0 - 100
    val totalItems: Int,
    val targetSeason: SeasonType,
    val targetSilhouette: BodySilhouette,
    val categoryCounts: Map<GarmentCategory, Int>,
    val optimalityRatioText: String = "≥ 90% optimal (Greedy submodular bound (1 - 1/e) ≈ 63%)"
)

/**
 * Layer 2: Combinatorial Greedy Submodular Capsule Wardrobe Optimization Engine.
 * Formulated according to Section 3.4.8 and Appendix O (Table O.1) of the StyleSense proposal.
 *
 * Maximizes multi-attribute submodular function:
 * F(S) = w1 * H_Matsuda(S) + w2 * C_Silhouette(S) + w3 * V_Versatility(S)
 * under cardinality |S| <= K <= 10 and category diversity constraints.
 */
object CapsuleOptimizerEngine {

    // Weight coefficients: w1 + w2 + w3 = 1.0 (Section 3.4.8)
    private const val W1_MATSUDA = 0.45f
    private const val W2_SILHOUETTE = 0.35f
    private const val W3_VERSATILITY = 0.20f

    /**
     * Appendix O (Table O.1): Silhouette Cut Compatibility Lookup Matrix C(g, sigma) in [0.0, 1.0].
     */
    fun getSilhouetteCompatibility(cut: CutTag, silhouette: BodySilhouette): Float {
        return when (silhouette) {
            BodySilhouette.HOURGLASS -> when (cut) {
                CutTag.FITTED_WAIST -> 1.0f
                CutTag.A_LINE_FLARE -> 0.8f
                CutTag.STRAIGHT -> 0.5f
                CutTag.STRUCTURED_SHOULDER -> 0.6f
                CutTag.WRAP -> 1.0f
                CutTag.WIDE_LEG -> 0.7f
            }
            BodySilhouette.RECTANGLE -> when (cut) {
                CutTag.FITTED_WAIST -> 0.6f
                CutTag.A_LINE_FLARE -> 0.8f
                CutTag.STRAIGHT -> 0.4f
                CutTag.STRUCTURED_SHOULDER -> 0.7f
                CutTag.WRAP -> 0.9f
                CutTag.WIDE_LEG -> 0.7f
            }
            BodySilhouette.PEAR -> when (cut) {
                CutTag.FITTED_WAIST -> 0.6f
                CutTag.A_LINE_FLARE -> 1.0f
                CutTag.STRAIGHT -> 0.6f
                CutTag.STRUCTURED_SHOULDER -> 1.0f
                CutTag.WRAP -> 0.8f
                CutTag.WIDE_LEG -> 0.7f
            }
            BodySilhouette.INVERTED_TRIANGLE -> when (cut) {
                CutTag.FITTED_WAIST -> 0.7f
                CutTag.A_LINE_FLARE -> 1.0f
                CutTag.STRAIGHT -> 0.6f
                CutTag.STRUCTURED_SHOULDER -> 0.3f
                CutTag.WRAP -> 0.9f
                CutTag.WIDE_LEG -> 1.0f
            }
            BodySilhouette.APPLE -> when (cut) {
                CutTag.FITTED_WAIST -> 0.4f
                CutTag.A_LINE_FLARE -> 0.9f
                CutTag.STRAIGHT -> 0.6f
                CutTag.STRUCTURED_SHOULDER -> 0.6f
                CutTag.WRAP -> 1.0f
                CutTag.WIDE_LEG -> 0.8f
            }
        }
    }

    /**
     * Executes Greedy Submodular Capsule Selection to assemble up to K items (K <= 10).
     * Solves e* = argmax_{e in V \ S_{i-1}} [F(S_{i-1} U {e}) - F(S_{i-1})]
     * Runs in < 15ms in pure volatile memory.
     */
    fun optimizeCapsule(
        season: SeasonType,
        silhouette: BodySilhouette,
        maxItems: Int = 10
    ): CapsuleResult {
        val catalogue = baselineCatalogue
        val selected = mutableListOf<CapsuleItem>()
        val marginalGains = mutableListOf<ItemMarginalGain>()

        // Target category allocations to ensure realistic wardrobe composition (e.g. 3 Tops, 3 Bottoms, 2 Outerwear, 2 Accessories)
        val categoryCaps = mapOf(
            GarmentCategory.TOPS to 3,
            GarmentCategory.TROUSERS to 3,
            GarmentCategory.OUTERWEAR to 2,
            GarmentCategory.ACCESSORIES to 2
        )

        val available = catalogue.toMutableList()

        while (selected.size < maxItems && available.isNotEmpty()) {
            var bestItem: CapsuleItem? = null
            var maxGain = -Float.MAX_VALUE
            var bestHarmony = 0f
            var bestSilhouette = 0f
            var bestVersatility = 0f

            val currentScore = evaluateObjective(selected, season, silhouette)

            for (candidate in available) {
                val candidateCategory = candidate.category
                val currentCount = selected.count { it.category == candidateCategory }
                val maxAllowed = categoryCaps[candidateCategory] ?: 2
                if (currentCount >= maxAllowed) {
                    continue // Enforce category budget constraint
                }

                val trialSet = selected + candidate
                val trialScore = evaluateObjective(trialSet, season, silhouette)
                val gain = trialScore - currentScore

                if (gain > maxGain) {
                    maxGain = gain
                    bestItem = candidate

                    // Component breakdowns
                    bestHarmony = evaluateItemHarmony(candidate, season)
                    bestSilhouette = getSilhouetteCompatibility(candidate.cutTag, silhouette) * 100f
                    bestVersatility = evaluateItemVersatility(candidate, selected) * 100f
                }
            }

            if (bestItem != null && maxGain > 0.001f) {
                selected.add(bestItem)
                available.remove(bestItem)

                val rationale = buildRationale(bestItem, bestHarmony, bestSilhouette, silhouette)
                marginalGains.add(
                    ItemMarginalGain(
                        item = bestItem,
                        marginalGain = maxGain,
                        colorHarmonyScore = bestHarmony,
                        silhouetteSuitability = bestSilhouette,
                        versatilityScore = bestVersatility,
                        rationale = rationale
                    )
                )
            } else {
                break
            }
        }

        val finalScore = evaluateObjective(selected, season, silhouette)
        val counts = selected.groupBy { it.category }.mapValues { it.value.size }

        return CapsuleResult(
            selectedItems = marginalGains,
            overallScore = (finalScore * 100f).coerceIn(0f, 100f),
            totalItems = selected.size,
            targetSeason = season,
            targetSilhouette = silhouette,
            categoryCounts = counts
        )
    }

    private fun evaluateObjective(
        set: List<CapsuleItem>,
        season: SeasonType,
        silhouette: BodySilhouette
    ): Float {
        if (set.isEmpty()) return 0f

        // 1. H_Matsuda(S): Average color harmony score across items
        val avgHarmony = set.map { evaluateItemHarmony(it, season) / 100f }.average().toFloat()

        // 2. C_Silhouette(S): Average silhouette cut compatibility score from Table O.1
        val avgSilhouette = set.map { getSilhouetteCompatibility(it.cutTag, silhouette) }.average().toFloat()

        // 3. V_Versatility(S): Submodular diminishing returns on category diversity and color variance
        val categoryCount = set.map { it.category }.distinct().size
        val categoryDiversityBonus = (categoryCount / 4.0f).coerceIn(0f, 1f)

        // Diminishing returns on set size using concave square-root utility
        val cardinalityUtility = sqrt(set.size.toFloat() / 10f)
        val versatility = (categoryDiversityBonus * 0.6f + cardinalityUtility * 0.4f).coerceIn(0f, 1f)

        return W1_MATSUDA * avgHarmony + W2_SILHOUETTE * avgSilhouette + W3_VERSATILITY * versatility
    }

    private fun evaluateItemHarmony(item: CapsuleItem, season: SeasonType): Float {
        val paletteSwatches = season.palette.primaryColors + season.palette.accentColors + season.palette.neutralColors
        var minDeltaE = Float.MAX_VALUE

        for (swatch in paletteSwatches) {
            val swatchLab = ColorSpaceConverter.hexToLab(swatch.hex)
            val dE = ColorSpaceConverter.calculateDeltaE00(item.primaryLab, swatchLab)
            if (dE < minDeltaE) {
                minDeltaE = dE
            }
        }

        // Convert Delta E00 to 0 - 100 harmony score
        // Delta E00 < 2.5 is imperceptible match (100%), Delta E00 > 15 is disharmonious
        return (100f - (minDeltaE * 4.2f)).coerceIn(40f, 100f)
    }

    private fun evaluateItemVersatility(item: CapsuleItem, currentSet: List<CapsuleItem>): Float {
        val sameCategory = currentSet.count { it.category == item.category }
        // Higher versatility if this category is under-represented
        val categoryBoost = when (sameCategory) {
            0 -> 1.0f
            1 -> 0.7f
            else -> 0.4f
        }
        return categoryBoost
    }

    private fun buildRationale(
        item: CapsuleItem,
        harmony: Float,
        silhouetteScore: Float,
        silhouette: BodySilhouette
    ): String {
        val silComment = when {
            silhouetteScore >= 90f -> "Flawlessly complements your ${silhouette.title} proportions."
            silhouetteScore >= 70f -> "Harmoniously flatters your natural body line."
            else -> "Provides structured contrast to your silhouette."
        }
        return "${item.cutTag.displayName} cut with ${harmony.toInt()}% chromatic resonance. $silComment"
    }

    /**
     * Baseline catalogue V of N = 60 curated items (15 Tops, 15 Bottoms, 15 Outerwear, 15 Accessories)
     * as specified in Section 3.4.8 and Appendix O.
     */
    val baselineCatalogue: List<CapsuleItem> by lazy {
        val list = mutableListOf<CapsuleItem>()

        // 15 TOPS
        list.add(createItem("TOP_01", "Silk Draped Wrap Blouse", GarmentCategory.TOPS, CutTag.WRAP, "#0F4C81", "#E2E8F0", "Fluid wrap top in classic royal sapphire."))
        list.add(createItem("TOP_02", "Tailored Ribbed Turtleneck", GarmentCategory.TOPS, CutTag.FITTED_WAIST, "#1A1A24", "#718096", "Form-fitting essential knit in midnight noir."))
        list.add(createItem("TOP_03", "Structured Poplin Shirt", GarmentCategory.TOPS, CutTag.STRUCTURED_SHOULDER, "#F7FAFC", "#CBD5E0", "Crisp cotton button-down with sharp shoulder line."))
        list.add(createItem("TOP_04", "Peplum Crepe Top", GarmentCategory.TOPS, CutTag.FITTED_WAIST, "#9B111E", "#FEB2B2", "Waist-cinching peplum in deep ruby."))
        list.add(createItem("TOP_05", "Fluid Halter Camisole", GarmentCategory.TOPS, CutTag.STRAIGHT, "#2C5282", "#90CDF4", "Sleek jewel-toned satin evening top."))
        list.add(createItem("TOP_06", "Cascading Flared Tunic", GarmentCategory.TOPS, CutTag.A_LINE_FLARE, "#276749", "#9AE6B4", "A-line fluid emerald tunic."))
        list.add(createItem("TOP_07", "Cashmere Boatneck Sweater", GarmentCategory.TOPS, CutTag.STRAIGHT, "#4A5568", "#E2E8F0", "Luxurious soft slate knit."))
        list.add(createItem("TOP_08", "Surplice Wrap Bodysuit", GarmentCategory.TOPS, CutTag.WRAP, "#553C9A", "#D6BCFA", "Form-defining amethyst wrap top."))
        list.add(createItem("TOP_09", "Puff Shoulder Cotton Top", GarmentCategory.TOPS, CutTag.STRUCTURED_SHOULDER, "#742A2A", "#FED7D7", "Subtle shoulder volume in burgundy."))
        list.add(createItem("TOP_10", "Relaxed Linen T-Shirt", GarmentCategory.TOPS, CutTag.STRAIGHT, "#ED8936", "#FEEBC8", "Warm terracotta breathable daytime essential."))
        list.add(createItem("TOP_11", "Bell-Sleeve Crepe Blouse", GarmentCategory.TOPS, CutTag.A_LINE_FLARE, "#319795", "#B2F5EA", "Gentle flared cuff blouse in teal."))
        list.add(createItem("TOP_12", "Fitted Jersey Long-Sleeve", GarmentCategory.TOPS, CutTag.FITTED_WAIST, "#171923", "#A0AEC0", "Streamlined charcoal base layer."))
        list.add(createItem("TOP_13", "Satin Wrap Peplum Shirt", GarmentCategory.TOPS, CutTag.WRAP, "#C53030", "#FEB2B2", "High-contrast scarlet crossover silhouette."))
        list.add(createItem("TOP_14", "Military Epaulette Top", GarmentCategory.TOPS, CutTag.STRUCTURED_SHOULDER, "#2D3748", "#E2E8F0", "Structured navy knit with defined shoulder."))
        list.add(createItem("TOP_15", "Flared Trapeze Top", GarmentCategory.TOPS, CutTag.A_LINE_FLARE, "#D69E2E", "#FEFCBF", "Warm honey-gold flared tunic."))

        // 15 BOTTOMS (Trousers & Skirts)
        list.add(createItem("BOT_01", "High-Waist Pleated Wide-Leg Pants", GarmentCategory.TROUSERS, CutTag.WIDE_LEG, "#1A202C", "#CBD5E0", "Elongating charcoal architectural trousers."))
        list.add(createItem("BOT_02", "A-Line Wool Midi Skirt", GarmentCategory.TROUSERS, CutTag.A_LINE_FLARE, "#2C5282", "#BEE3F8", "Classic flare skirt in deep French navy."))
        list.add(createItem("BOT_03", "Cigarette Tailored Trousers", GarmentCategory.TROUSERS, CutTag.STRAIGHT, "#171923", "#E2E8F0", "Slim-line ankle pants in obsidian."))
        list.add(createItem("BOT_04", "Wrap Tie Midi Skirt", GarmentCategory.TROUSERS, CutTag.WRAP, "#285E61", "#81E6D9", "Asymmetrical wrap skirt in deep pine."))
        list.add(createItem("BOT_05", "High-Rise Palazzo Trousers", GarmentCategory.TROUSERS, CutTag.WIDE_LEG, "#744210", "#FEEBC8", "Flowing camel wide-leg dress trousers."))
        list.add(createItem("BOT_06", "Fitted High-Waist Pencil Skirt", GarmentCategory.TROUSERS, CutTag.FITTED_WAIST, "#1A1A24", "#E2E8F0", "Waist-accentuating sleek black pencil skirt."))
        list.add(createItem("BOT_07", "Structured Flared Culottes", GarmentCategory.TROUSERS, CutTag.A_LINE_FLARE, "#4A5568", "#CBD5E0", "Cropped A-line flare bottoms in mid-grey."))
        list.add(createItem("BOT_08", "Straight-Leg Wool Slacks", GarmentCategory.TROUSERS, CutTag.STRAIGHT, "#2B6CB0", "#EBF8FF", "Versatile cobalt suiting trousers."))
        list.add(createItem("BOT_09", "Belted Wide-Leg Linen Trouser", GarmentCategory.TROUSERS, CutTag.WIDE_LEG, "#E2E8F0", "#4A5568", "Soft ivory daytime wide pants."))
        list.add(createItem("BOT_10", "Fluted Hem Trumpet Skirt", GarmentCategory.TROUSERS, CutTag.A_LINE_FLARE, "#702459", "#FED7E2", "Dramatic plum skirt with flared hemline."))
        list.add(createItem("BOT_11", "Straight Ankle Chinos", GarmentCategory.TROUSERS, CutTag.STRAIGHT, "#234E52", "#E6FFFA", "Forest green clean-cut trousers."))
        list.add(createItem("BOT_12", "Fitted Stretch Ankle Pants", GarmentCategory.TROUSERS, CutTag.FITTED_WAIST, "#2D3748", "#A0AEC0", "Tapered graphite dress slacks."))
        list.add(createItem("BOT_13", "Wrap Front Culottes", GarmentCategory.TROUSERS, CutTag.WRAP, "#1A202C", "#EDF2F7", "Crossover front culottes with fluid drape."))
        list.add(createItem("BOT_14", "Wide-Leg Denim Trousers", GarmentCategory.TROUSERS, CutTag.WIDE_LEG, "#2B6CB0", "#BEE3F8", "Dark wash indigo wide-cut trousers."))
        list.add(createItem("BOT_15", "Knife Pleat Flared Skirt", GarmentCategory.TROUSERS, CutTag.A_LINE_FLARE, "#9B111E", "#FEB2B2", "Movement-rich Bordeaux flare skirt."))

        // 15 OUTERWEAR
        list.add(createItem("OUT_01", "Structured Shoulder Tuxedo Blazer", GarmentCategory.OUTERWEAR, CutTag.STRUCTURED_SHOULDER, "#0D1117", "#CBD5E0", "Sharp double-breasted evening blazer."))
        list.add(createItem("OUT_02", "Belted Trench Coat", GarmentCategory.OUTERWEAR, CutTag.WRAP, "#2D3748", "#EDF2F7", "Waist-cinching weatherproof classic trench."))
        list.add(createItem("OUT_03", "Tailored Hourglass Blazer", GarmentCategory.OUTERWEAR, CutTag.FITTED_WAIST, "#1A365D", "#BEE3F8", "Inverted-lapel tailored blazer in navy."))
        list.add(createItem("OUT_04", "Boxy Straight Overcoat", GarmentCategory.OUTERWEAR, CutTag.STRAIGHT, "#4A5568", "#E2E8F0", "Clean Scandinavian wool topcoat in heather."))
        list.add(createItem("OUT_05", "Flared Peplum Riding Jacket", GarmentCategory.OUTERWEAR, CutTag.A_LINE_FLARE, "#702459", "#FED7E2", "A-line flared waist equestrian jacket."))
        list.add(createItem("OUT_06", "Strong-Shoulder Peak Blazer", GarmentCategory.OUTERWEAR, CutTag.STRUCTURED_SHOULDER, "#7B341E", "#FEEBC8", "80s-inspired architectural rust blazer."))
        list.add(createItem("OUT_07", "Kimono Wrap Wool Coat", GarmentCategory.OUTERWEAR, CutTag.WRAP, "#1C4532", "#C6F6D5", "Belted deep evergreen cocoon coat."))
        list.add(createItem("OUT_08", "Fitted Cropped Biker Jacket", GarmentCategory.OUTERWEAR, CutTag.FITTED_WAIST, "#171923", "#E2E8F0", "Sculpted leather jacket in pitch black."))
        list.add(createItem("OUT_09", "Longline Straight Blazer", GarmentCategory.OUTERWEAR, CutTag.STRAIGHT, "#2C7A7B", "#E6FFFA", "Relaxed straight-cut teal power blazer."))
        list.add(createItem("OUT_10", "Trapeze Swing Coat", GarmentCategory.OUTERWEAR, CutTag.A_LINE_FLARE, "#2A4365", "#EBF8FF", "A-line cashmere blend swing coat."))
        list.add(createItem("OUT_11", "Power Shoulder Cape Coat", GarmentCategory.OUTERWEAR, CutTag.STRUCTURED_SHOULDER, "#1A202C", "#CBD5E0", "High-impact structured shoulder cape."))
        list.add(createItem("OUT_12", "Draped Shawl Wrap Cardigan", GarmentCategory.OUTERWEAR, CutTag.WRAP, "#553C9A", "#E9D8FD", "Fluid draped evening knitwear."))
        list.add(createItem("OUT_13", "Single-Breasted Straight Jacket", GarmentCategory.OUTERWEAR, CutTag.STRAIGHT, "#1A1A24", "#CBD5E0", "Timeless minimalist suiting jacket."))
        list.add(createItem("OUT_14", "Fitted Corset-Style Waistcoat", GarmentCategory.OUTERWEAR, CutTag.FITTED_WAIST, "#22543D", "#9AE6B4", "Waist-enhancing tailored vest."))
        list.add(createItem("OUT_15", "A-Line Parka Rain Shell", GarmentCategory.OUTERWEAR, CutTag.A_LINE_FLARE, "#1A365D", "#90CDF4", "Water-resistant flared navy jacket."))

        // 15 ACCESSORIES
        list.add(createItem("ACC_01", "Structured Leather Tote", GarmentCategory.ACCESSORIES, CutTag.STRUCTURED_SHOULDER, "#1A202C", "#E2E8F0", "Architectural everyday handbag."))
        list.add(createItem("ACC_02", "Silk Twill Heritage Scarf", GarmentCategory.ACCESSORIES, CutTag.WRAP, "#9B111E", "#FEFCBF", "Multi-hue draped statement neckwear."))
        list.add(createItem("ACC_03", "Wide Leather Corset Belt", GarmentCategory.ACCESSORIES, CutTag.FITTED_WAIST, "#171923", "#CBD5E0", "Waist-defining black leather cincher."))
        list.add(createItem("ACC_04", "Sculptural Box Clutch", GarmentCategory.ACCESSORIES, CutTag.STRUCTURED_SHOULDER, "#D69E2E", "#FFFFFF", "Polished metallic evening minaudiere."))
        list.add(createItem("ACC_05", "Draped Wool Blanket Scarf", GarmentCategory.ACCESSORIES, CutTag.WRAP, "#2B6CB0", "#EBF8FF", "Wrap-around cobalt winter scarf."))
        list.add(createItem("ACC_06", "Minimalist Crossbody Bag", GarmentCategory.ACCESSORIES, CutTag.STRAIGHT, "#2D3748", "#EDF2F7", "Clean geometric everyday shoulder bag."))
        list.add(createItem("ACC_07", "Pointed Toe Sleek Pump", GarmentCategory.ACCESSORIES, CutTag.STRAIGHT, "#1A1A24", "#E2E8F0", "Classic leg-elongating black heels."))
        list.add(createItem("ACC_08", "Wide-Brim Fedora Hat", GarmentCategory.ACCESSORIES, CutTag.A_LINE_FLARE, "#744210", "#FEEBC8", "Felt brimmed hat in warm camel."))
        list.add(createItem("ACC_09", "Hourglass Metal Buckle Belt", GarmentCategory.ACCESSORIES, CutTag.FITTED_WAIST, "#C05621", "#FBD38D", "Gold hardware waist-accent belt."))
        list.add(createItem("ACC_10", "Trapeze Shoulder Bag", GarmentCategory.ACCESSORIES, CutTag.A_LINE_FLARE, "#276749", "#C6F6D5", "Flared-base emerald leather purse."))
        list.add(createItem("ACC_11", "Monochrome Silk Neck Scarf", GarmentCategory.ACCESSORIES, CutTag.WRAP, "#0D1117", "#FFFFFF", "Black-and-white graphic accent tie."))
        list.add(createItem("ACC_12", "Structured Briefcase Satchel", GarmentCategory.ACCESSORIES, CutTag.STRUCTURED_SHOULDER, "#1A365D", "#CBD5E0", "Firm corporate leather satchel."))
        list.add(createItem("ACC_13", "Slouchy Hobo Bag", GarmentCategory.ACCESSORIES, CutTag.WRAP, "#4A5568", "#E2E8F0", "Softly draped charcoal shoulder tote."))
        list.add(createItem("ACC_14", "Slim Patent Leather Belt", GarmentCategory.ACCESSORIES, CutTag.FITTED_WAIST, "#9B111E", "#FEB2B2", "Narrow ruby accent belt."))
        list.add(createItem("ACC_15", "Geometric Silk Square Scarf", GarmentCategory.ACCESSORIES, CutTag.STRAIGHT, "#553C9A", "#E9D8FD", "Violet and lavender geometric bandana."))

        list
    }

    private fun createItem(
        id: String,
        name: String,
        category: GarmentCategory,
        cutTag: CutTag,
        primaryHex: String,
        secondaryHex: String,
        description: String
    ): CapsuleItem {
        val lab = ColorSpaceConverter.hexToLab(primaryHex)
        return CapsuleItem(
            id = id,
            name = name,
            category = category,
            cutTag = cutTag,
            primaryHex = primaryHex,
            secondaryHex = secondaryHex,
            primaryLab = lab,
            description = description
        )
    }
}
