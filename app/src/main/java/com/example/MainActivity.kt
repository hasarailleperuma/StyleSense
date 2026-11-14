package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.presentation.ui.components.PrivacyModal
import com.example.presentation.ui.components.StyleSenseNavBar
import com.example.presentation.ui.screens.*
import com.example.presentation.viewmodel.AppDestination
import com.example.presentation.viewmodel.StyleSenseViewModel
import com.example.ui.theme.StyleSenseTheme
import androidx.hilt.navigation.compose.hiltViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint(ComponentActivity::class)
class MainActivity : Hilt_MainActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StyleSenseTheme {
                val viewModel: StyleSenseViewModel = hiltViewModel()
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        // Show bottom nav bar on main destination tabs
                        if (uiState.currentDestination != AppDestination.LIVE_CAMERA) {
                            StyleSenseNavBar(
                                currentDestination = uiState.currentDestination,
                                onNavigate = { dest -> viewModel.navigateTo(dest) }
                            )
                        }
                    },
                    contentWindowInsets = WindowInsets.systemBars
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(
                                // Don't apply bottom padding on live camera to achieve full immersion
                                if (uiState.currentDestination == AppDestination.LIVE_CAMERA) {
                                    PaddingValues(0.dp)
                                } else {
                                    innerPadding
                                }
                            )
                    ) {
                        AnimatedContent(
                            targetState = uiState.currentDestination,
                            transitionSpec = {
                                fadeIn() togetherWith fadeOut()
                            },
                            label = "screen_transition"
                        ) { destination ->
                            when (destination) {
                                AppDestination.ONBOARDING -> {
                                    OnboardingScreen(
                                        onNavigate = { dest -> viewModel.navigateTo(dest) },
                                        onPhotoSelected = { uri -> viewModel.onFacePhotoSelected(uri) },
                                        onShowPrivacyModal = { viewModel.togglePrivacyModal(true) }
                                    )
                                }

                                AppDestination.LIVE_CAMERA -> {
                                    LiveAnalysisScreen(
                                        liveMetrics = uiState.liveMetrics,
                                        classification = uiState.classification,
                                        cameraManager = viewModel.cameraManager,
                                        onPhotoSelected = { uri ->
                                            viewModel.onFacePhotoSelected(uri)
                                        },
                                        onMetricsReceived = { metrics ->
                                            viewModel.onLiveMetricsReceived(metrics)
                                        },
                                        onConfirmAnalysis = {
                                            viewModel.confirmLiveAnalysis()
                                        },
                                        onBack = {
                                            viewModel.navigateTo(AppDestination.ONBOARDING)
                                        },
                                        onSwitchToManual = {
                                            viewModel.navigateTo(AppDestination.MANUAL_INPUT)
                                        }
                                    )
                                }

                                AppDestination.MANUAL_INPUT -> {
                                    ManualInputScreen(
                                        onBack = {
                                            viewModel.navigateTo(AppDestination.ONBOARDING)
                                        },
                                        onSubmit = { warmth, depth, chroma, contrast, silhouette ->
                                            viewModel.onManualInputSubmitted(
                                                warmth,
                                                depth,
                                                chroma,
                                                contrast,
                                                silhouette
                                            )
                                        }
                                    )
                                }

                                AppDestination.RESULT_PALETTE -> {
                                    SeasonResultScreen(
                                        season = uiState.activeSeason,
                                        classification = uiState.classification,
                                        silhouette = uiState.activeSilhouette,
                                        onNavigateToOutfitMatch = {
                                            viewModel.evaluateCurrentOutfit()
                                            viewModel.navigateTo(AppDestination.OUTFIT_EVALUATION)
                                        },
                                        onSelectSeason = { s ->
                                            // Optional direct exploration
                                        }
                                    )
                                }

                                AppDestination.CAPSULE_WARDROBE -> {
                                    CapsuleWardrobeScreen(
                                        season = uiState.activeSeason,
                                        silhouette = uiState.activeSilhouette,
                                        onSaveItemToCloset = { name, category, pCol, sCol, score ->
                                            viewModel.saveCapsuleItemToCloset(name, category, pCol, sCol, score)
                                        }
                                    )
                                }

                                AppDestination.WARDROBE_CLOSET -> {
                                    WardrobeScreen(
                                        items = uiState.wardrobeItems,
                                        onGarmentPhotoSelected = { uri ->
                                            viewModel.onGarmentPhotoSelected(uri)
                                            viewModel.navigateTo(AppDestination.OUTFIT_EVALUATION)
                                        },
                                        onSelectItem = { item ->
                                            viewModel.selectWardrobeItem(item)
                                        },
                                        onDeleteItem = { id ->
                                            viewModel.deleteWardrobeItem(id)
                                        }
                                    )
                                }

                                AppDestination.OUTFIT_EVALUATION -> {
                                    OutfitMatcherScreen(
                                        userSeason = uiState.activeSeason,
                                        selectedOccasion = uiState.selectedOccasion,
                                        matchResult = uiState.outfitMatchResult,
                                        scannedGarment = uiState.scannedGarment,
                                        onSelectOccasion = { occ ->
                                            viewModel.selectOccasion(occ)
                                        },
                                        onGarmentPhotoSelected = { uri ->
                                            viewModel.onGarmentPhotoSelected(uri)
                                        },
                                        onSaveToWardrobe = { name, category ->
                                            viewModel.saveScannedGarment(name, category)
                                        }
                                    )
                                }
                            }
                        }

                        // Loading Overlay
                        if (uiState.isAnalyzing) {
                            Surface(
                                modifier = Modifier.fillMaxSize(),
                                color = MaterialTheme.colorScheme.background.copy(alpha = 0.85f)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    CircularProgressIndicator(
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "Analyzing Color Profile...",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                }
                            }
                        }

                        // UK GDPR Article 9 Privacy Modal
                        if (uiState.showPrivacyModal) {
                            PrivacyModal(
                                onDismiss = { viewModel.togglePrivacyModal(false) }
                            )
                        }
                    }
                }
            }
        }
    }
}
