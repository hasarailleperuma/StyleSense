package com.example.presentation.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entity.SeasonProfileEntity
import com.example.data.local.entity.WardrobeItemEntity
import com.example.data.local.repository.WardrobeRepository
import com.example.domain.classifier.ClassificationResult
import com.example.domain.classifier.SeasonClassifier
import com.example.domain.colorimetry.DominantColorExtractor
import com.example.domain.harmony.MatsudaHarmonyEngine
import com.example.domain.model.*
import com.example.presentation.camera.CameraManager
import com.example.presentation.camera.PhotoPickerManager
import com.example.domain.colorimetry.BiometricProcessor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StyleSenseViewModel @Inject constructor(
    application: Application,
    private val repository: WardrobeRepository,
    val cameraManager: CameraManager,
    private val biometricProcessor: BiometricProcessor
) : AndroidViewModel(application) {

    // Secondary constructor for test environments & previews
    constructor(application: Application) : this(
        application,
        WardrobeRepository(
            AppDatabase.getDatabase(application).seasonProfileDao(),
            AppDatabase.getDatabase(application).wardrobeDao()
        ),
        CameraManager(application, BiometricProcessor()),
        BiometricProcessor()
    )

    private val _uiState = MutableStateFlow(StyleSenseUiState())
    val uiState: StateFlow<StyleSenseUiState> = _uiState.asStateFlow()

    init {
        // Collect persisted wardrobe items
        viewModelScope.launch {
            repository.allWardrobeItems.collect { items ->
                _uiState.update { it.copy(wardrobeItems = items) }
                // Pre-populate if empty to give an immediate polished wardrobe experience
                if (items.isEmpty()) {
                    seedDefaultWardrobe()
                }
            }
        }

        // Collect persisted anonymous season profile if previously analyzed
        viewModelScope.launch {
            repository.activeProfile.collect { profile ->
                if (profile != null) {
                    val season = SeasonType.fromId(profile.seasonId)
                    val sil = profile.silhouetteName?.let { name ->
                        BodySilhouette.entries.firstOrNull { it.name == name }
                    }
                    _uiState.update {
                        it.copy(
                            activeSeason = season,
                            activeSilhouette = sil ?: it.activeSilhouette
                        )
                    }
                }
            }
        }
    }

    private suspend fun seedDefaultWardrobe() {
        val sampleItems = listOf(
            WardrobeItemEntity(
                name = "Tailored Silk Blazer",
                category = GarmentCategory.OUTERWEAR.name,
                primaryHex = "#0F4C81",
                secondaryHex = "#FFFFFF",
                seasonScore = 94,
                harmonyTag = "Signature Match"
            ),
            WardrobeItemEntity(
                name = "Cashmere Crewneck",
                category = GarmentCategory.TOPS.name,
                primaryHex = "#5B1865",
                secondaryHex = "#E0AAFF",
                seasonScore = 88,
                harmonyTag = "Harmonious Flow"
            ),
            WardrobeItemEntity(
                name = "High-Waist Wool Trousers",
                category = GarmentCategory.TROUSERS.name,
                primaryHex = "#121212",
                secondaryHex = "#495057",
                seasonScore = 92,
                harmonyTag = "Monochromatic Neutral"
            ),
            WardrobeItemEntity(
                name = "Emerald Cocktail Slip Dress",
                category = GarmentCategory.DRESSES.name,
                primaryHex = "#008F5A",
                secondaryHex = "#00353F",
                seasonScore = 96,
                harmonyTag = "Vivid Jewel Accent"
            ),
            WardrobeItemEntity(
                name = "Structured Crossbody Bag",
                category = GarmentCategory.ACCESSORIES.name,
                primaryHex = "#9B111E",
                secondaryHex = "#F8C8DC",
                seasonScore = 86,
                harmonyTag = "Dynamic Complementary"
            )
        )
        sampleItems.forEach { repository.addWardrobeItem(it) }
    }

    fun saveCapsuleItemToCloset(
        name: String,
        category: GarmentCategory,
        primaryHex: String,
        secondaryHex: String,
        score: Int
    ) {
        viewModelScope.launch {
            val item = WardrobeItemEntity(
                name = name,
                category = category.name,
                primaryHex = primaryHex,
                secondaryHex = secondaryHex,
                seasonScore = score,
                harmonyTag = "Capsule Synergy"
            )
            repository.addWardrobeItem(item)
        }
    }

    fun navigateTo(destination: AppDestination) {
        _uiState.update { it.copy(currentDestination = destination, errorMessage = null) }
    }

    fun onLiveMetricsReceived(metrics: BiometricMetrics) {
        val classification = SeasonClassifier.classifyBiometrics(metrics)
        _uiState.update {
            it.copy(
                liveMetrics = metrics,
                classification = classification
            )
        }
    }

    fun confirmLiveAnalysis() {
        val metrics = _uiState.value.liveMetrics ?: return
        val classification = _uiState.value.classification ?: SeasonClassifier.classifyBiometrics(metrics)
        applyClassificationAndPersist(classification, metrics)
        navigateTo(AppDestination.RESULT_PALETTE)
    }

    fun onFacePhotoSelected(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isAnalyzing = true, errorMessage = null) }
            val result = PhotoPickerManager.analyzeVolatileFacePhoto(getApplication(), uri)
            result.onSuccess { metrics ->
                val classification = SeasonClassifier.classifyBiometrics(metrics)
                applyClassificationAndPersist(classification, metrics)
                _uiState.update {
                    it.copy(
                        isAnalyzing = false,
                        liveMetrics = metrics,
                        classification = classification,
                        currentDestination = AppDestination.RESULT_PALETTE
                    )
                }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isAnalyzing = false,
                        errorMessage = "Analysis error: ${err.localizedMessage ?: "Failed to process photo."}"
                    )
                }
            }
        }
    }

    fun onManualInputSubmitted(
        warmth: Float,
        depth: Float,
        eyeChroma: Float,
        contrast: Float,
        silhouette: BodySilhouette
    ) {
        val classification = SeasonClassifier.classifyManual(warmth, depth, eyeChroma, contrast)
        _uiState.update {
            it.copy(
                activeSeason = classification.topSeason,
                activeSilhouette = silhouette,
                classification = classification,
                currentDestination = AppDestination.RESULT_PALETTE
            )
        }
        viewModelScope.launch {
            val profile = SeasonProfileEntity(
                seasonId = classification.topSeason.name,
                itaDegrees = 35f,
                melaninIndex = (depth * 50f + 20f),
                erythemaIndex = 25f,
                ambientKelvin = 5500,
                undertone = if (warmth > 0) "Warm" else "Cool",
                itaClassification = "Intermediate",
                silhouetteName = silhouette.name
            )
            repository.saveProfile(profile)
        }
    }

    private fun applyClassificationAndPersist(
        classification: ClassificationResult,
        metrics: BiometricMetrics
    ) {
        val season = classification.topSeason
        _uiState.update {
            it.copy(
                activeSeason = season,
                classification = classification
            )
        }
        viewModelScope.launch {
            // Save anonymous season profile and colorimetry metrics
            val profile = SeasonProfileEntity(
                seasonId = season.name,
                itaDegrees = metrics.itaDegrees,
                melaninIndex = metrics.melaninIndex,
                erythemaIndex = metrics.erythemaIndex,
                ambientKelvin = metrics.ambientKelvin,
                undertone = metrics.undertoneDescription,
                itaClassification = metrics.itaClassification,
                silhouetteName = _uiState.value.activeSilhouette?.name
            )
            repository.saveProfile(profile)
        }
    }

    fun onGarmentPhotoSelected(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isAnalyzing = true, errorMessage = null) }
            val result = PhotoPickerManager.digitizeGarmentPhoto(getApplication(), uri)
            result.onSuccess { analysis ->
                _uiState.update { it.copy(scannedGarment = analysis, isAnalyzing = false) }
                // Immediately evaluate outfit against current season & occasion
                evaluateCurrentOutfit(analysis.dominantColors)
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isAnalyzing = false,
                        errorMessage = "Garment scan error: ${err.localizedMessage ?: "Could not extract colors."}"
                    )
                }
            }
        }
    }

    fun selectOccasion(occasion: Occasion) {
        _uiState.update { it.copy(selectedOccasion = occasion) }
        val colors = _uiState.value.scannedGarment?.dominantColors
            ?: _uiState.value.selectedWardrobeItem?.let { item ->
                listOf(
                    ColorSwatch("Primary", item.primaryHex, androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(item.primaryHex))),
                    ColorSwatch("Secondary", item.secondaryHex, androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(item.secondaryHex)))
                )
            }
        if (colors != null) {
            evaluateCurrentOutfit(colors)
        }
    }

    fun selectSilhouette(silhouette: BodySilhouette) {
        _uiState.update { it.copy(activeSilhouette = silhouette) }
        viewModelScope.launch {
            val current = _uiState.value.activeSeason
            val profile = SeasonProfileEntity(
                seasonId = current.name,
                itaDegrees = _uiState.value.liveMetrics?.itaDegrees ?: 35f,
                melaninIndex = _uiState.value.liveMetrics?.melaninIndex ?: 30f,
                erythemaIndex = _uiState.value.liveMetrics?.erythemaIndex ?: 25f,
                ambientKelvin = _uiState.value.liveMetrics?.ambientKelvin ?: 5500,
                undertone = _uiState.value.liveMetrics?.undertoneDescription ?: "Neutral",
                itaClassification = _uiState.value.liveMetrics?.itaClassification ?: "Intermediate",
                silhouetteName = silhouette.name
            )
            repository.saveProfile(profile)
        }
    }

    fun selectWardrobeItem(item: WardrobeItemEntity) {
        val color1 = try {
            ColorSwatch("Primary", item.primaryHex, androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(item.primaryHex)))
        } catch (e: Exception) {
            ColorSwatch("Primary", item.primaryHex, androidx.compose.ui.graphics.Color.DarkGray)
        }
        val color2 = try {
            ColorSwatch("Secondary", item.secondaryHex, androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(item.secondaryHex)))
        } catch (e: Exception) {
            ColorSwatch("Secondary", item.secondaryHex, androidx.compose.ui.graphics.Color.LightGray)
        }
        val colors = listOf(color1, color2)
        _uiState.update { it.copy(selectedWardrobeItem = item) }
        evaluateCurrentOutfit(colors)
        navigateTo(AppDestination.OUTFIT_EVALUATION)
    }

    fun evaluateCurrentOutfit(colors: List<ColorSwatch>? = null) {
        val activeColors = colors
            ?: _uiState.value.scannedGarment?.dominantColors
            ?: _uiState.value.selectedWardrobeItem?.let { item ->
                listOf(
                    ColorSwatch("Primary", item.primaryHex, androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(item.primaryHex))),
                    ColorSwatch("Secondary", item.secondaryHex, androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(item.secondaryHex)))
                )
            }
            ?: _uiState.value.activeSeason.palette.primaryColors.take(2)

        val result = MatsudaHarmonyEngine.evaluateOutfit(
            garmentColors = activeColors,
            userSeason = _uiState.value.activeSeason,
            occasion = _uiState.value.selectedOccasion,
            silhouette = _uiState.value.activeSilhouette
        )
        _uiState.update { it.copy(outfitMatchResult = result) }
    }

    fun saveScannedGarment(name: String, category: GarmentCategory) {
        val scan = _uiState.value.scannedGarment ?: return
        val primary = scan.primaryHex
        val secondary = scan.dominantColors.getOrNull(1)?.hex ?: primary
        val score = _uiState.value.outfitMatchResult?.overallScore ?: 85
        val harmony = _uiState.value.outfitMatchResult?.seasonAlignment ?: "Harmonious Flow"

        viewModelScope.launch {
            val item = WardrobeItemEntity(
                name = name.ifBlank { "${category.displayName.substringBefore("&").trim()} in ${scan.dominantColors.firstOrNull()?.name ?: "Color"}" },
                category = category.name,
                primaryHex = primary,
                secondaryHex = secondary,
                seasonScore = score,
                harmonyTag = harmony
            )
            repository.addWardrobeItem(item)
            navigateTo(AppDestination.WARDROBE_CLOSET)
        }
    }

    fun deleteWardrobeItem(id: Long) {
        viewModelScope.launch {
            repository.deleteWardrobeItem(id)
        }
    }

    fun togglePrivacyModal(show: Boolean) {
        _uiState.update { it.copy(showPrivacyModal = show) }
    }

    fun toggleExportDialog(show: Boolean) {
        _uiState.update { it.copy(showExportDialog = show) }
    }
}
