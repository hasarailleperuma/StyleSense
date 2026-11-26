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
import com.example.presentation.ui.components.PrivacyModal
import com.example.presentation.ui.components.StyleSenseNavBar
import com.example.presentation.ui.screens.*
import com.example.presentation.viewmodel.AppDestination
import com.example.presentation.viewmodel.StyleSenseViewModel
import com.example.ui.theme.StyleSenseTheme
import androidx.hilt.navigation.compose.hiltViewModel
import dagger.hilt.android.AndroidEntryPoint

// Ordered list of destinations used to decide slide direction
private val destinationOrder = listOf(
    AppDestination.ONBOARDING,
    AppDestination.LIVE_CAMERA,
    AppDestination.MANUAL_INPUT,
    AppDestination.RESULT_PALETTE,
    AppDestination.CAPSULE_WARDROBE,
    AppDestination.WARDROBE_CLOSET,
    AppDestination.OUTFIT_EVALUATION
)

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StyleSenseTheme {
                val viewModel: StyleSenseViewModel = hiltViewModel()
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                val snackbarHostState = remember { SnackbarHostState() }

                // Show error messages as snackbars
                LaunchedEffect(uiState.errorMessage) {
                    uiState.errorMessage?.let { message ->
                        snackbarHostState.showSnackbar(
                            message = message,
                            duration = SnackbarDuration.Short
                        )
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    bottomBar = {
                        // Hide bottom nav on full-screen immersive screens
                        if (uiState.currentDestination != AppDestination.LIVE_CAMERA &&
                            uiState.currentDestination != AppDestination.MANUAL_INPUT
                        ) {
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
                                // No bottom padding on immersive screens
                                if (uiState.currentDestination == AppDestination.LIVE_CAMERA ||
                                    uiState.currentDestination == AppDestination.MANUAL_INPUT
                                ) PaddingValues(0.dp) else innerPadding
                            )
                    ) {
                        AnimatedContent(
                            targetState = uiState.currentDestination,
                            transitionSpec = {
                                // Direction-aware slide: right→left for forward, left→right for back
                                val fromIdx = destinationOrder.indexOf(initialState)
                                val toIdx = destinationOrder.indexOf(targetState)
                                val forward = toIdx >= fromIdx
                                if (forward) {
                                    slideInHorizontally(initialOffsetX = { it / 4 }) + fadeIn() togetherWith
                                        slideOutHorizontally(targetOffsetX = { -it / 4 }) + fadeOut()
                                } else {
                                    slideInHorizontally(initialOffsetX = { -it / 4 }) + fadeIn() togetherWith
                                        slideOutHorizontally(targetOffsetX = { it / 4 }) + fadeOut()
                                }
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
                                        liveMetrics = uiState.liveMetrics,
                                        onNavigateToOutfitMatch = {
                                            viewModel.evaluateCurrentOutfit()
                                            viewModel.navigateTo(AppDestination.OUTFIT_EVALUATION)
                                        },
                                        onSelectSeason = { s ->
                                            viewModel.selectSeason(s)
                                        }
                                    )
                                }

                                AppDestination.CAPSULE_WARDROBE -> {
                                    CapsuleWardrobeScreen(
                                        season = uiState.activeSeason,
                                        silhouette = uiState.activeSilhouette,
                                        onSilhouetteSelected = { sil ->
                                            viewModel.selectSilhouette(sil)
                                        },
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
                                        activeSilhouette = uiState.activeSilhouette,
                                        onSelectSilhouette = { sil ->
                                            viewModel.selectSilhouette(sil)
                                        },
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

                        // ── Loading Overlay ──────────────────────────────────
                        AnimatedVisibility(
                            visible = uiState.isAnalyzing,
                            enter = fadeIn(),
                            exit = fadeOut(),
                            modifier = Modifier.fillMaxSize()
                        ) {
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
                                        text = "Analysing Color Profile...",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                }
                            }
                        }

                        // ── UK GDPR Article 9 Privacy Modal ──────────────────
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
