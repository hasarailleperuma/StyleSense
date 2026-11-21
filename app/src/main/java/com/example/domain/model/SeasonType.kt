package com.example.domain.model

import androidx.compose.ui.graphics.Color

/**
 * The 12 Seasonal Flow Categories in advanced directional color analysis.
 * Spring (Warm & Bright), Summer (Cool & Soft), Autumn (Warm & Muted/Deep), Winter (Cool & Vivid/Deep).
 */
enum class SeasonGroup(val displayName: String, val undertone: String) {
    SPRING("Spring", "Warm & Clear"),
    SUMMER("Summer", "Cool & Soft"),
    AUTUMN("Autumn", "Warm & Muted"),
    WINTER("Winter", "Cool & Vivid")
}

data class SeasonPalette(
    val primaryColors: List<ColorSwatch>,
    val accentColors: List<ColorSwatch>,
    val neutralColors: List<ColorSwatch>,
    val colorsToAvoid: List<ColorSwatch>,
    val bestMetals: List<String>,
    val makeupTones: String,
    val contrastPreference: String,
    val description: String
)

data class ColorSwatch(
    val name: String,
    val hex: String,
    val color: Color
)

enum class SeasonType(
    val group: SeasonGroup,
    val title: String,
    val subtitle: String,
    val dominantCharacteristics: String,
    val palette: SeasonPalette
) {
    // --- SPRING ---
    LIGHT_SPRING(
        group = SeasonGroup.SPRING,
        title = "Light Spring",
        subtitle = "Delicate, Warm & Luminous",
        dominantCharacteristics = "High lightness (L* > 65), warm golden undertone, low-to-medium melanin.",
        palette = SeasonPalette(
            primaryColors = listOf(
                ColorSwatch("Peach Blossom", "#FFB085", Color(0xFFFFB085)),
                ColorSwatch("Warm Coral", "#FF7F66", Color(0xFFFF7F66)),
                ColorSwatch("Buttercup Yellow", "#FFE17D", Color(0xFFFFE17D)),
                ColorSwatch("Sprout Green", "#98D8AA", Color(0xFF98D8AA))
            ),
            accentColors = listOf(
                ColorSwatch("Aqua Breeze", "#4DD0E1", Color(0xFF4DD0E1)),
                ColorSwatch("Salmon Pink", "#FA8072", Color(0xFFFA8072)),
                ColorSwatch("Warm Mint", "#A8E6CF", Color(0xFFA8E6CF))
            ),
            neutralColors = listOf(
                ColorSwatch("Soft Ivory", "#FFFFF0", Color(0xFFFFFFF0)),
                ColorSwatch("Warm Sand", "#E0C9A6", Color(0xFFE0C9A6)),
                ColorSwatch("Camel", "#C19A6B", Color(0xFFC19A6B)),
                ColorSwatch("Warm Dove Grey", "#B8B0A8", Color(0xFFB8B0A8))
            ),
            colorsToAvoid = listOf(
                ColorSwatch("Stark Black", "#101010", Color(0xFF101010)),
                ColorSwatch("Deep Burgundy", "#4A0E17", Color(0xFF4A0E17)),
                ColorSwatch("Icy Grey", "#708090", Color(0xFF708090))
            ),
            bestMetals = listOf("Light Yellow Gold", "Champagne Gold", "Rose Gold"),
            makeupTones = "Warm peachy gloss, apricot blush, soft golden bronze mascara.",
            contrastPreference = "Low to Medium contrast with tonal harmony.",
            description = "Light Spring shines in soft, sunlit pastels with warm golden undertones. Avoid overpowering dark shades."
        )
    ),

    WARM_SPRING(
        group = SeasonGroup.SPRING,
        title = "Warm Spring",
        subtitle = "Golden, Radiant & Vibrant",
        dominantCharacteristics = "True warm undertone (b* > +18), radiant saturation, golden reflectance.",
        palette = SeasonPalette(
            primaryColors = listOf(
                ColorSwatch("Marigold", "#FFAA00", Color(0xFFFFAA00)),
                ColorSwatch("Tangerine", "#FF6B35", Color(0xFFFF6B35)),
                ColorSwatch("Warm Jade", "#2EC4B6", Color(0xFF2EC4B6)),
                ColorSwatch("Poppy Red", "#E63946", Color(0xFFE63946))
            ),
            accentColors = listOf(
                ColorSwatch("Turquoise", "#00B4D8", Color(0xFF00B4D8)),
                ColorSwatch("Chartreuse", "#B5E48C", Color(0xFFB5E48C)),
                ColorSwatch("Flamingo", "#FF758F", Color(0xFFFF758F))
            ),
            neutralColors = listOf(
                ColorSwatch("Warm Buttermilk", "#FFF8DC", Color(0xFFFFF8DC)),
                ColorSwatch("Cognac Tan", "#9C6644", Color(0xFF9C6644)),
                ColorSwatch("Warm Khaki", "#BDB246", Color(0xFFBDB246)),
                ColorSwatch("Golden Cream", "#F7E1D7", Color(0xFFF7E1D7))
            ),
            colorsToAvoid = listOf(
                ColorSwatch("Pure Black", "#000000", Color(0xFF000000)),
                ColorSwatch("Dusty Mauve", "#915C83", Color(0xFF915C83)),
                ColorSwatch("Cool Charcoal", "#36454F", Color(0xFF36454F))
            ),
            bestMetals = listOf("Rich 18k Yellow Gold", "Polished Brass", "Warm Copper"),
            makeupTones = "Terracotta coral lips, warm bronze highlighter, honey-amber tones.",
            contrastPreference = "Medium to High warmth contrast.",
            description = "Warm Spring is drenched in rich golden vitality. Pure warms like marigold, warm coral, and turquoise bring out your skin's glow."
        )
    ),

    CLEAR_SPRING(
        group = SeasonGroup.SPRING,
        title = "Clear Spring",
        subtitle = "Brilliant, Crisp & High-Chroma",
        dominantCharacteristics = "High chroma (C* > 32), radiant clarity, warm-neutral undertone.",
        palette = SeasonPalette(
            primaryColors = listOf(
                ColorSwatch("Electric Coral", "#FF4365", Color(0xFFFF4365)),
                ColorSwatch("Bright Turquoise", "#00F5D4", Color(0xFF00F5D4)),
                ColorSwatch("Sunburst Gold", "#FFD000", Color(0xFFFFD000)),
                ColorSwatch("Bright Kelly Green", "#06D6A0", Color(0xFF06D6A0))
            ),
            accentColors = listOf(
                ColorSwatch("Cobalt Splash", "#0077B6", Color(0xFF0077B6)),
                ColorSwatch("Watermelon Red", "#FF3366", Color(0xFFFF3366)),
                ColorSwatch("Violet Blossom", "#9B5DE5", Color(0xFF9B5DE5))
            ),
            neutralColors = listOf(
                ColorSwatch("Porcelain White", "#F8F9FA", Color(0xFFF8F9FA)),
                ColorSwatch("Warm Espresso", "#3D2B1F", Color(0xFF3D2B1F)),
                ColorSwatch("Bright Navy", "#1A365D", Color(0xFF1A365D)),
                ColorSwatch("Light Almond", "#EED9C4", Color(0xFFEED9C4))
            ),
            colorsToAvoid = listOf(
                ColorSwatch("Muted Grey-Brown", "#6E6259", Color(0xFF6E6259)),
                ColorSwatch("Dusty Olive", "#556B2F", Color(0xFF556B2F)),
                ColorSwatch("Muddy Taupe", "#8B8589", Color(0xFF8B8589))
            ),
            bestMetals = listOf("Polished Yellow Gold", "Bright Platinum", "Rose Gold"),
            makeupTones = "Vivid coral-red lip, defined mascara, dewy crystalline highlighter.",
            contrastPreference = "High contrast with crisp clarity.",
            description = "Clear Spring is defined by striking saturation and brightness. Vivid jewel and citrus tones make your features pop."
        )
    ),

    // --- SUMMER ---
    LIGHT_SUMMER(
        group = SeasonGroup.SUMMER,
        title = "Light Summer",
        subtitle = "Ethereal, Cool & Powder Soft",
        dominantCharacteristics = "High lightness (L* > 68), cool undertone (b* < +8), low melanin index.",
        palette = SeasonPalette(
            primaryColors = listOf(
                ColorSwatch("Powder Blue", "#90CDF4", Color(0xFF90CDF4)),
                ColorSwatch("Soft Rose", "#FBB6CE", Color(0xFFFBB6CE)),
                ColorSwatch("Lavender Fog", "#D6BCFA", Color(0xFFD6BCFA)),
                ColorSwatch("Seafoam Frost", "#B2F5EA", Color(0xFFB2F5EA))
            ),
            accentColors = listOf(
                ColorSwatch("Cornflower Blue", "#63B3ED", Color(0xFF63B3ED)),
                ColorSwatch("Orchid Mist", "#ED64A6", Color(0xFFED64A6)),
                ColorSwatch("Cool Mint", "#81E6D9", Color(0xFF81E6D9))
            ),
            neutralColors = listOf(
                ColorSwatch("Soft Cool White", "#F7FAFC", Color(0xFFF7FAFC)),
                ColorSwatch("Pearl Grey", "#CBD5E0", Color(0xFFCBD5E0)),
                ColorSwatch("Rose Beige", "#E2D9D2", Color(0xFFE2D9D2)),
                ColorSwatch("Slate Grey", "#718096", Color(0xFF718096))
            ),
            colorsToAvoid = listOf(
                ColorSwatch("Mustard Yellow", "#D4AF37", Color(0xFFD4AF37)),
                ColorSwatch("Burnt Orange", "#CC5500", Color(0xFFCC5500)),
                ColorSwatch("Heavy Jet Black", "#050505", Color(0xFF050505))
            ),
            bestMetals = listOf("White Gold", "Polished Silver", "Platinum"),
            makeupTones = "Cool petal pink lipstick, soft rose blush, slate grey eyeliner.",
            contrastPreference = "Low to subtle medium contrast.",
            description = "Light Summer thrives in delicate watercolor pastels with cool undertones. Soft blues, lilacs, and rose petal hues enhance your natural clarity."
        )
    ),

    COOL_SUMMER(
        group = SeasonGroup.SUMMER,
        title = "Cool Summer",
        subtitle = "Aristocratic, Pure Cool & Crisp",
        dominantCharacteristics = "Dominant blue-undertone (b* < +4), medium value, cool erythema baseline.",
        palette = SeasonPalette(
            primaryColors = listOf(
                ColorSwatch("True Cornflower", "#4299E1", Color(0xFF4299E1)),
                ColorSwatch("Raspberry", "#D53F8C", Color(0xFFD53F8C)),
                ColorSwatch("Cool Spruce", "#319795", Color(0xFF319795)),
                ColorSwatch("Royal Amethyst", "#805AD5", Color(0xFF805AD5))
            ),
            accentColors = listOf(
                ColorSwatch("Cool Berry", "#97266D", Color(0xFF97266D)),
                ColorSwatch("French Blue", "#3182CE", Color(0xFF3182CE)),
                ColorSwatch("Watermelon Ice", "#E53E3E", Color(0xFFE53E3E))
            ),
            neutralColors = listOf(
                ColorSwatch("Pure Crisp Grey", "#A0AEC0", Color(0xFFA0AEC0)),
                ColorSwatch("Cool Navy", "#2A4365", Color(0xFF2A4365)),
                ColorSwatch("Platinum Ice", "#EDF2F7", Color(0xFFEDF2F7)),
                ColorSwatch("Charcoal Slate", "#4A5568", Color(0xFF4A5568))
            ),
            colorsToAvoid = listOf(
                ColorSwatch("Warm Amber", "#FFBF00", Color(0xFFFFBF00)),
                ColorSwatch("Rust Brown", "#B7410E", Color(0xFFB7410E)),
                ColorSwatch("Olive Drab", "#6B8E23", Color(0xFF6B8E23))
            ),
            bestMetals = listOf("Sterling Silver", "Platinum", "Antique Silver"),
            makeupTones = "Cool plum or berry lipstick, cool berry blush, soft navy liner.",
            contrastPreference = "Medium contrast with cool crispness.",
            description = "Cool Summer is entirely devoid of warm yellow undertones. Crisp raspberry, royal amethyst, and twilight blues create an effortlessly regal aura."
        )
    ),

    SOFT_SUMMER(
        group = SeasonGroup.SUMMER,
        title = "Soft Summer",
        subtitle = "Velvety, Muted & Smoky Rose",
        dominantCharacteristics = "Low chroma (C* < 16), dusty muted quality, cool-neutral undertone.",
        palette = SeasonPalette(
            primaryColors = listOf(
                ColorSwatch("Smoky Mauve", "#A3808C", Color(0xFFA3808C)),
                ColorSwatch("Dusty Teal", "#4E878C", Color(0xFF4E878C)),
                ColorSwatch("Heather Lilac", "#9381FF", Color(0xFF9381FF)),
                ColorSwatch("Faded Rose", "#C08497", Color(0xFFC08497))
            ),
            accentColors = listOf(
                ColorSwatch("Soft Pine", "#588157", Color(0xFF588157)),
                ColorSwatch("Muted Periwinkle", "#7E90BF", Color(0xFF7E90BF)),
                ColorSwatch("Burgundy Mist", "#7B3F61", Color(0xFF7B3F61))
            ),
            neutralColors = listOf(
                ColorSwatch("Mushroom Taupe", "#8D8177", Color(0xFF8D8177)),
                ColorSwatch("Soft Pewter", "#7D8491", Color(0xFF7D8491)),
                ColorSwatch("Rose White", "#F5F3F4", Color(0xFFF5F3F4)),
                ColorSwatch("Cadet Navy", "#3D5A80", Color(0xFF3D5A80))
            ),
            colorsToAvoid = listOf(
                ColorSwatch("Neon Lime", "#39FF14", Color(0xFF39FF14)),
                ColorSwatch("Vivid Orange", "#FF5F1F", Color(0xFFFF5F1F)),
                ColorSwatch("Electric Yellow", "#FFFF33", Color(0xFFFFFF33))
            ),
            bestMetals = listOf("Brushed Silver", "Pewter", "Matte Rose Gold"),
            makeupTones = "Dusty rose satin lip, mauve blush, smoky taupe eyeshadow.",
            contrastPreference = "Muted low-to-medium tonal harmony.",
            description = "Soft Summer has a sophisticated, dreamy quality. Blended tones like smoky mauve, dusty teal, and antique pewter look stunning on you."
        )
    ),

    // --- AUTUMN ---
    SOFT_AUTUMN(
        group = SeasonGroup.AUTUMN,
        title = "Soft Autumn",
        subtitle = "Gentle, Warm & Earthy Velvet",
        dominantCharacteristics = "Low chroma (C* < 18), gentle warmth, blended honey-hazel contrast.",
        palette = SeasonPalette(
            primaryColors = listOf(
                ColorSwatch("Dusty Terracotta", "#D48B6A", Color(0xFFD48B6A)),
                ColorSwatch("Sage Green", "#8A9A86", Color(0xFF8A9A86)),
                ColorSwatch("Warm Rosewood", "#9C6644", Color(0xFF9C6644)),
                ColorSwatch("Muted Mustard", "#D8A47F", Color(0xFFD8A47F))
            ),
            accentColors = listOf(
                ColorSwatch("Warm Jade", "#52796F", Color(0xFF52796F)),
                ColorSwatch("Apricot Buff", "#E8A598", Color(0xFFE8A598)),
                ColorSwatch("Soft Teal", "#5B8E7D", Color(0xFF5B8E7D))
            ),
            neutralColors = listOf(
                ColorSwatch("Oatmeal Cream", "#E9DCC9", Color(0xFFE9DCC9)),
                ColorSwatch("Warm Taupe", "#8A7968", Color(0xFF8A7968)),
                ColorSwatch("Olive Grey", "#6B705C", Color(0xFF6B705C)),
                ColorSwatch("Rich Camel", "#B5835A", Color(0xFFB5835A))
            ),
            colorsToAvoid = listOf(
                ColorSwatch("Fuchsia", "#FF00FF", Color(0xFFFF00FF)),
                ColorSwatch("Royal Cobalt", "#0020C2", Color(0xFF0020C2)),
                ColorSwatch("Stark White", "#FFFFFF", Color(0xFFFFFFFF))
            ),
            bestMetals = listOf("Brushed Gold", "Antique Bronze", "Warm Brass"),
            makeupTones = "Warm nude terracotta lip, peachy-brown blush, soft bronze liner.",
            contrastPreference = "Low to medium blended warmth.",
            description = "Soft Autumn is warm, gentle, and complex. Earthy sage, cashmere camel, and muted terracotta harmonize with your softly blended features."
        )
    ),

    WARM_AUTUMN(
        group = SeasonGroup.AUTUMN,
        title = "Warm Autumn",
        subtitle = "Spiced, Golden & Amber Harvest",
        dominantCharacteristics = "Dominant warm yellow-red undertone (b* > +20), golden melanin richness.",
        palette = SeasonPalette(
            primaryColors = listOf(
                ColorSwatch("Burnt Pumpkin", "#E36414", Color(0xFFE36414)),
                ColorSwatch("Rich Olive", "#588157", Color(0xFF588157)),
                ColorSwatch("Spiced Ochre", "#C97A1F", Color(0xFFC97A1F)),
                ColorSwatch("Deep Rust", "#9A3B3B", Color(0xFF9A3B3B))
            ),
            accentColors = listOf(
                ColorSwatch("Warm Amber", "#FB8500", Color(0xFFFB8500)),
                ColorSwatch("Forest Teal", "#1A535C", Color(0xFF1A535C)),
                ColorSwatch("Cinnamon Bark", "#7F2929", Color(0xFF7F2929))
            ),
            neutralColors = listOf(
                ColorSwatch("Warm Biscuit", "#E0A96D", Color(0xFFE0A96D)),
                ColorSwatch("Dark Chocolate", "#3E2723", Color(0xFF3E2723)),
                ColorSwatch("Warm Khaki", "#8B8C4A", Color(0xFF8B8C4A)),
                ColorSwatch("Ivory Parchment", "#F4EBD9", Color(0xFFF4EBD9))
            ),
            colorsToAvoid = listOf(
                ColorSwatch("Ice Blue", "#AFEEEE", Color(0xFFAFEEEE)),
                ColorSwatch("Cool Magenta", "#CA1F7B", Color(0xFFCA1F7B)),
                ColorSwatch("Pure Cool Grey", "#808080", Color(0xFF808080))
            ),
            bestMetals = listOf("Copper", "Antique Brass", "Rich 22k Gold"),
            makeupTones = "Spiced cinnamon lip, terracotta bronzer, olive and gold shadows.",
            contrastPreference = "Medium to high warm contrast.",
            description = "Warm Autumn embodies the peak of harvest foliage. Burnt pumpkin, spiced ochre, and deep moss green make you look incandescent."
        )
    ),

    DEEP_AUTUMN(
        group = SeasonGroup.AUTUMN,
        title = "Deep Autumn",
        subtitle = "Rich, Saturated & Dramatic Earth",
        dominantCharacteristics = "Low lightness (L* < 45), high melanin depth, warm undertone.",
        palette = SeasonPalette(
            primaryColors = listOf(
                ColorSwatch("Auburn Mahogany", "#5C2018", Color(0xFF5C2018)),
                ColorSwatch("Deep Forest Green", "#1B4332", Color(0xFF1B4332)),
                ColorSwatch("Spiced Burgundy", "#660708", Color(0xFF660708)),
                ColorSwatch("Golden Mustard", "#BA791D", Color(0xFFBA791D))
            ),
            accentColors = listOf(
                ColorSwatch("Rich Copper", "#B85D19", Color(0xFFB85D19)),
                ColorSwatch("Deep Teal", "#0B525B", Color(0xFF0B525B)),
                ColorSwatch("Garnet Red", "#800E13", Color(0xFF800E13))
            ),
            neutralColors = listOf(
                ColorSwatch("Espresso Brown", "#2B1E16", Color(0xFF2B1E16)),
                ColorSwatch("Warm Charcoal", "#2B2D42", Color(0xFF2B2D42)),
                ColorSwatch("Warm Camel", "#A66E38", Color(0xFFA66E38)),
                ColorSwatch("Warm Sandalwood", "#DDBEA9", Color(0xFFDDBEA9))
            ),
            colorsToAvoid = listOf(
                ColorSwatch("Baby Pink", "#F4C2C2", Color(0xFFF4C2C2)),
                ColorSwatch("Pale Lavender", "#E6E6FA", Color(0xFFE6E6FA)),
                ColorSwatch("Cool Mint", "#98FF98", Color(0xFF98FF98))
            ),
            bestMetals = listOf("Antique Gold", "Burnished Bronze", "Hammered Copper"),
            makeupTones = "Deep blackberry or brick red lips, warm contour, rich copper shadow.",
            contrastPreference = "High depth contrast.",
            description = "Deep Autumn is commanding, dark, and warm. Deep forest green, mahogany, and spiced burgundy provide breathtaking depth."
        )
    ),

    // --- WINTER ---
    CLEAR_WINTER(
        group = SeasonGroup.WINTER,
        title = "Clear Winter",
        subtitle = "Luminous, High-Contrast & Jewel Vivid",
        dominantCharacteristics = "High chroma (C* > 34), high contrast (sclera vs iris / hair), cool-neutral undertone.",
        palette = SeasonPalette(
            primaryColors = listOf(
                ColorSwatch("Emerald Jewel", "#008F5A", Color(0xFF008F5A)),
                ColorSwatch("Royal Cobalt", "#0038A8", Color(0xFF0038A8)),
                ColorSwatch("True Magenta", "#D90368", Color(0xFFD90368)),
                ColorSwatch("Bright Amethyst", "#7209B7", Color(0xFF7209B7))
            ),
            accentColors = listOf(
                ColorSwatch("Icy Lemon", "#FFF066", Color(0xFFFFF066)),
                ColorSwatch("Electric Cyan", "#00B4D8", Color(0xFF00B4D8)),
                ColorSwatch("Bright Crimson", "#D00000", Color(0xFFD00000))
            ),
            neutralColors = listOf(
                ColorSwatch("Pure White", "#FFFFFF", Color(0xFFFFFFFF)),
                ColorSwatch("Jet Black", "#0A0A0A", Color(0xFF0A0A0A)),
                ColorSwatch("Midnight Navy", "#03045E", Color(0xFF03045E)),
                ColorSwatch("Cool Pewter", "#5C677D", Color(0xFF5C677D))
            ),
            colorsToAvoid = listOf(
                ColorSwatch("Muddy Olive", "#555D50", Color(0xFF555D50)),
                ColorSwatch("Golden Mustard", "#DAA520", Color(0xFFDAA520)),
                ColorSwatch("Dusty Peach", "#F4A460", Color(0xFFF4A460))
            ),
            bestMetals = listOf("Brilliant Platinum", "Bright Rhodium Silver", "White Gold"),
            makeupTones = "Vivid clear red lip, crisp liquid eyeliner, luminous highlighter.",
            contrastPreference = "Striking, ultra-high contrast.",
            description = "Clear Winter combines icy coolness with diamond-sharp clarity. Stark black & white, emerald, and electric cobalt match your dramatic presence."
        )
    ),

    COOL_WINTER(
        group = SeasonGroup.WINTER,
        title = "Cool Winter",
        subtitle = "Pure Cool, Regal & Crystalline",
        dominantCharacteristics = "Zero warm undertone (b* < 0), pure blue reflectance, crisp cool saturation.",
        palette = SeasonPalette(
            primaryColors = listOf(
                ColorSwatch("True Sapphire", "#0F4C81", Color(0xFF0F4C81)),
                ColorSwatch("Cool Plum", "#5B1865", Color(0xFF5B1865)),
                ColorSwatch("Pine Needles", "#0B3C35", Color(0xFF0B3C35)),
                ColorSwatch("Ruby Red", "#9B111E", Color(0xFF9B111E))
            ),
            accentColors = listOf(
                ColorSwatch("Icy Pink", "#F8C8DC", Color(0xFFF8C8DC)),
                ColorSwatch("Silver Sky", "#A5C4D4", Color(0xFFA5C4D4)),
                ColorSwatch("Violet Royale", "#480CA8", Color(0xFF480CA8))
            ),
            neutralColors = listOf(
                ColorSwatch("Stark Snow White", "#FFFFFF", Color(0xFFFFFFFF)),
                ColorSwatch("Deep Onyx", "#121212", Color(0xFF121212)),
                ColorSwatch("Steel Grey", "#495057", Color(0xFF495057)),
                ColorSwatch("Ink Navy", "#0D1B2A", Color(0xFF0D1B2A))
            ),
            colorsToAvoid = listOf(
                ColorSwatch("Warm Orange", "#FFA500", Color(0xFFFFA500)),
                ColorSwatch("Terracotta", "#E2725B", Color(0xFFE2725B)),
                ColorSwatch("Warm Golden Brown", "#8B4513", Color(0xFF8B4513))
            ),
            bestMetals = listOf("Polished Platinum", "Mirror Silver", "White Gold"),
            makeupTones = "True cool ruby red lips, icy pink blush, charcoal smokey eye.",
            contrastPreference = "High contrast with absolute coolness.",
            description = "Cool Winter features completely cool undertones without a drop of gold. True sapphire, pine, and pure snow white give an effortlessly regal finish."
        )
    ),

    DEEP_WINTER(
        group = SeasonGroup.WINTER,
        title = "Deep Winter",
        subtitle = "Mysterious, Intense & Midnight Contrast",
        dominantCharacteristics = "Low lightness (L* < 42), high melanin contrast, cool undertone.",
        palette = SeasonPalette(
            primaryColors = listOf(
                ColorSwatch("Midnight Black", "#000000", Color(0xFF000000)),
                ColorSwatch("Bordeaux Wine", "#4A001F", Color(0xFF4A001F)),
                ColorSwatch("Midnight Teal", "#00353F", Color(0xFF00353F)),
                ColorSwatch("Deep Royal Purple", "#240046", Color(0xFF240046))
            ),
            accentColors = listOf(
                ColorSwatch("True Crimson", "#A0001C", Color(0xFFA0001C)),
                ColorSwatch("Ice Lavender", "#E0AAFF", Color(0xFFE0AAFF)),
                ColorSwatch("Cobalt Night", "#10002B", Color(0xFF10002B))
            ),
            neutralColors = listOf(
                ColorSwatch("Onyx Black", "#050505", Color(0xFF050505)),
                ColorSwatch("Bright Chalk White", "#F8F9FA", Color(0xFFF8F9FA)),
                ColorSwatch("Midnight Blue", "#001233", Color(0xFF001233)),
                ColorSwatch("Dark Graphite", "#333533", Color(0xFF333533))
            ),
            colorsToAvoid = listOf(
                ColorSwatch("Golden Camel", "#C19A6B", Color(0xFFC19A6B)),
                ColorSwatch("Pale Mustard", "#E1AD01", Color(0xFFE1AD01)),
                ColorSwatch("Warm Olive", "#808000", Color(0xFF808000))
            ),
            bestMetals = listOf("High-Shine Platinum", "Gunmetal", "Oxidized Silver"),
            makeupTones = "Deep berry or blackened red lips, defined contour, dramatic lashes.",
            contrastPreference = "Extreme high contrast between light and dark.",
            description = "Deep Winter carries mysterious, profound intensity. Midnight black, Bordeaux wine, and contrasting icy accents create unforgettable impact."
        )
    );

    companion object {
        fun fromId(id: String): SeasonType {
            return entries.firstOrNull { it.name.equals(id, ignoreCase = true) } ?: CLEAR_WINTER
        }
    }
}
