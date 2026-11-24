package com.example.domain.model

enum class Occasion(
    val title: String,
    val description: String,
    val formalLevel: Int, // 1 to 5
    val contrastRule: String,
    val iconName: String
) {
    BUSINESS_INTERVIEW(
        title = "Business & Interview",
        description = "Corporate meetings, interviews, executive presentations",
        formalLevel = 4,
        contrastRule = "High-contrast authoritative neutrals with controlled accent tones.",
        iconName = "business"
    ),
    FORMAL_DINNER(
        title = "Formal Dinner & Gala",
        description = "Black tie, gala dinners, opera, prestigious celebrations",
        formalLevel = 5,
        contrastRule = "Dramatic monochrome or deep jewel-tones with metallic luxury.",
        iconName = "dinner"
    ),
    WEDDING(
        title = "Wedding Guest",
        description = "Day or evening weddings, romantic celebrations",
        formalLevel = 4,
        contrastRule = "Harmonious analogous pastel or romantic jewel-tones (avoid pure bridal white).",
        iconName = "celebration"
    ),
    PARTY_NIGHT_OUT(
        title = "Party & Night Out",
        description = "Cocktail parties, lounges, festive weekend gatherings",
        formalLevel = 3,
        contrastRule = "Vibrant complementary contrasts, bold accents, and high-energy hues.",
        iconName = "party"
    ),
    CASUAL_WEEKEND(
        title = "Casual & Weekend",
        description = "Brunch, coffee runs, travel, relaxed day outings",
        formalLevel = 1,
        contrastRule = "Effortless tonal neutrals, soft accents, and breathable balance.",
        iconName = "weekend"
    )
}

enum class GarmentGender(val displayName: String, val icon: String) {
    ALL("All / Unisex", "✨"),
    WOMEN("Women", "👗"),
    MEN("Men", "👔")
}

enum class GarmentCategory(val displayName: String, val icon: String) {
    TOPS("Tops & Blouses", "top"),
    OUTERWEAR("Jackets & Coats", "coat"),
    TROUSERS("Trousers & Skirts", "bottom"),
    DRESSES("Dresses & Jumpsuits", "dress"),
    ACCESSORIES("Bags & Scarves", "scarf")
}

enum class BodySilhouette(
    val title: String,
    val description: String,
    val stylingGuidance: String
) {
    HOURGLASS(
        title = "Hourglass (X-Shape)",
        description = "Balanced bust and hips with defined waistline.",
        stylingGuidance = "Highlight waist definition with fitted tops, wrap silhouettes, and belt accents."
    ),
    PEAR(
        title = "Pear (A-Shape)",
        description = "Hips broader than shoulders; defined waist.",
        stylingGuidance = "Draw eyes upward using statement necklines, shoulder details, and darker bottoms."
    ),
    RECTANGLE(
        title = "Rectangle (H-Shape)",
        description = "Bust, waist, and hips of relatively uniform width.",
        stylingGuidance = "Create visual curves with color-blocking, belted jackets, and flared hems."
    ),
    INVERTED_TRIANGLE(
        title = "Inverted Triangle (V-Shape)",
        description = "Broad shoulders and chest tapering to narrower hips.",
        stylingGuidance = "Balance the upper body with V-necklines, soft draping, and eye-catching patterned bottoms."
    ),
    APPLE(
        title = "Apple (O-Shape)",
        description = "Fuller midsection with slender limbs.",
        stylingGuidance = "Elongate the torso with monochromatic vertical column lines, empire waists, and open jackets."
    )
}

data class OutfitMatchResult(
    val overallScore: Int, // 0 - 100
    val harmonyCategory: String, // Monochromatic, Analogous, Complementary, Triadic, Mismatched
    val seasonAlignment: String, // Harmonious, Compatible, High-Contrast, Suboptimal
    val plainEnglishRationale: String,
    val occasionVerdict: String,
    val recommendedAccessories: List<String>,
    val dominantColors: List<ColorSwatch>,
    val deltaEToSeasonPalette: Float
)
