package com.example.presentation.viewmodel

import com.example.data.local.entity.WardrobeItemEntity
import com.example.domain.classifier.ClassificationResult
import com.example.domain.colorimetry.DominantColorExtractor
import com.example.domain.model.*

enum class AppDestination {
    ONBOARDING,
    LIVE_CAMERA,
    MANUAL_INPUT,
    RESULT_PALETTE,
    CAPSULE_WARDROBE,
    WARDROBE_CLOSET,
    OUTFIT_EVALUATION
}

data class StyleSenseUiState(
    val currentDestination: AppDestination = AppDestination.ONBOARDING,
    val liveMetrics: BiometricMetrics? = null,
    val classification: ClassificationResult? = null,
    val activeSeason: SeasonType = SeasonType.CLEAR_WINTER,
    val activeSilhouette: BodySilhouette? = BodySilhouette.HOURGLASS,
    val selectedOccasion: Occasion = Occasion.BUSINESS_INTERVIEW,
    val scannedGarment: DominantColorExtractor.GarmentAnalysis? = null,
    val outfitMatchResult: OutfitMatchResult? = null,
    val wardrobeItems: List<WardrobeItemEntity> = emptyList(),
    val selectedWardrobeItem: WardrobeItemEntity? = null,
    val isAnalyzing: Boolean = false,
    val errorMessage: String? = null,
    val showPrivacyModal: Boolean = false,
    val showExportDialog: Boolean = false
)
