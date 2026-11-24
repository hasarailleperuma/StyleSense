package com.example.presentation.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.VideocamOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.domain.classifier.ClassificationResult
import com.example.domain.model.BiometricMetrics
import com.example.presentation.camera.CameraManager
import com.example.presentation.ui.components.FaceMeshOverlay
import com.example.presentation.ui.components.TelemetryHud
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.colorimetry.IlluminantNormalizer
import java.util.Locale
import androidx.compose.foundation.clickable

@Composable
fun LiveAnalysisScreen(
    liveMetrics: BiometricMetrics?,
    classification: ClassificationResult?,
    onMetricsReceived: (BiometricMetrics) -> Unit,
    onConfirmAnalysis: () -> Unit,
    onBack: () -> Unit,
    onSwitchToManual: () -> Unit,
    modifier: Modifier = Modifier,
    cameraManager: CameraManager? = null,
    onPhotoSelected: ((Uri) -> Unit)? = null
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val activeCameraManager = remember(cameraManager, context, lifecycleOwner) {
        cameraManager ?: CameraManager(context, lifecycleOwner)
    }

    val currentLux by activeCameraManager.ambientLuxSensor.currentLux.collectAsStateWithLifecycle()
    val isLuxAdequate by activeCameraManager.ambientLuxSensor.isLuxAdequate.collectAsStateWithLifecycle()
    var showWhitePaperModal by remember { mutableStateOf(false) }
    var whitePaperCalibrated by remember { mutableStateOf(false) }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var cameraErrorMessage by remember { mutableStateOf<String?>(null) }
    var previewViewRef by remember { mutableStateOf<PreviewView?>(null) }
    var rotationAngle by remember { mutableFloatStateOf(0f) }
    var mirrorHorizontal by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (!isGranted) {
            cameraErrorMessage = "Camera permission denied"
        }
    }

    // Photo picker fallback for emulator environments without physical cameras
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            onPhotoSelected?.invoke(uri)
        }
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    DisposableEffect(activeCameraManager) {
        onDispose {
            activeCameraManager.stopCamera()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (hasCameraPermission) {
            // Camera Preview View with emulator orientation & mirror adaptation
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val scale = if (rotationAngle % 180f != 0f) {
                    val w = maxWidth.value
                    val h = maxHeight.value
                    if (w > 0 && h > 0) maxOf(h / w, w / h) else 1f
                } else 1f

                AndroidView(
                    factory = { ctx ->
                        PreviewView(ctx).also { pView ->
                            pView.implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                            previewViewRef = pView
                            activeCameraManager.startCamera(
                                lifecycleOwner = lifecycleOwner,
                                previewView = pView,
                                onMetricsExtracted = { metrics ->
                                    cameraErrorMessage = null
                                    onMetricsReceived(metrics)
                                },
                                onError = { err ->
                                    cameraErrorMessage = err.localizedMessage
                                        ?: "Unable to access camera hardware"
                                }
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            this.rotationZ = rotationAngle
                            this.scaleX = (if (mirrorHorizontal) -1f else 1f) * scale
                            this.scaleY = scale
                        }
                )
            }

            // Overlaid facial mesh wireframe & ROI indicators
            FaceMeshOverlay(
                metrics = liveMetrics,
                modifier = Modifier.fillMaxSize()
            )

            // Top Bar with back, live prediction chip, and camera toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0x88000000))
                        .testTag("btn_camera_back")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                // Real-Time Classification Chip
                if (classification != null) {
                    Surface(
                        color = Color(0xCC1E1035),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.testTag("live_season_chip")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(
                                        classification.topSeason.palette.accentColors.firstOrNull()?.color
                                            ?: Color(0xFF7209B7)
                                    )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = classification.topSeason.title.uppercase(Locale.ROOT),
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${(classification.confidence * 100).toInt()}%",
                                color = Color(0xFF4CC9F0),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Rotate 90 degrees button
                    IconButton(
                        onClick = {
                            rotationAngle = (rotationAngle + 90f) % 360f
                            activeCameraManager.manualRotationOffset = rotationAngle.toInt()
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0x88000000))
                            .testTag("btn_camera_rotate")
                    ) {
                        Icon(
                            imageVector = Icons.Default.RotateRight,
                            contentDescription = "Rotate Camera",
                            tint = Color.White
                        )
                    }

                    // Mirror / Invert flip button
                    IconButton(
                        onClick = {
                            mirrorHorizontal = !mirrorHorizontal
                            activeCameraManager.isMirrored = mirrorHorizontal
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(if (mirrorHorizontal) Color(0xCC7209B7) else Color(0x88000000))
                            .testTag("btn_camera_mirror")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Flip,
                            contentDescription = "Mirror Camera",
                            tint = Color.White
                        )
                    }

                    // Switch camera toggle
                    IconButton(
                        onClick = {
                            previewViewRef?.let { pView ->
                                activeCameraManager.toggleCamera(pView, onMetricsReceived) { err ->
                                    cameraErrorMessage = err.localizedMessage
                                }
                            }
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0x88000000))
                            .testTag("btn_camera_flip")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cameraswitch,
                            contentDescription = "Switch Camera",
                            tint = Color.White
                        )
                    }
                }
            }

            // Real-time Lux Telemetry & White Paper Calibration Banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(top = 74.dp, start = 16.dp, end = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = if (isLuxAdequate) Color(0xAA120E22) else Color(0xDDC53030),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = if (isLuxAdequate) "💡 ${currentLux.toInt()} lx" else "⚠️ ${currentLux.toInt()} lx (Low Light)",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Surface(
                    color = if (whitePaperCalibrated) Color(0xCC06D6A0) else Color(0xAA2B6CB0),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.clickable { showWhitePaperModal = true }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = if (whitePaperCalibrated) "✓ Calibrated" else "📄 Calibrate White",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // If camera hardware failed or is missing (common in browser streaming emulators), show a helpful fallback panel
            if (cameraErrorMessage != null) {
                Surface(
                    color = Color(0xEE1E1035),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 24.dp)
                        .testTag("camera_unavailable_card")
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color(0x33FFB703)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.VideocamOff,
                                contentDescription = null,
                                tint = Color(0xFFFFB703),
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Camera Sensor Not Detected",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Camera feed is unavailable. You can upload a photo from your gallery or use manual sliders:",
                            color = Color(0xFFD4C2F0),
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                photoPickerLauncher.launch(
                                    androidx.activity.result.PickVisualMediaRequest(
                                        ActivityResultContracts.PickVisualMedia.ImageOnly
                                    )
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("btn_camera_fallback_photo"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF7209B7)
                            ),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Select Photo from Gallery", fontSize = 14.sp)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedButton(
                            onClick = onSwitchToManual,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("btn_camera_fallback_manual"),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Use Manual Sliders", color = Color.White, fontSize = 14.sp)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        TextButton(
                            onClick = {
                                cameraErrorMessage = null
                                previewViewRef?.let { pView ->
                                    activeCameraManager.startCamera(
                                        lifecycleOwner = lifecycleOwner,
                                        previewView = pView,
                                        onMetricsExtracted = { metrics ->
                                            cameraErrorMessage = null
                                            onMetricsReceived(metrics)
                                        },
                                        onError = { err ->
                                            cameraErrorMessage = err.localizedMessage
                                        }
                                    )
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                tint = Color(0xFFB5A1E5),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Retry Camera", color = Color(0xFFB5A1E5), fontSize = 12.sp)
                        }
                    }
                }
            }

            val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

            // Bottom Section: Telemetry HUD + Confirm Button
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = if (isLandscape) 6.dp else 12.dp)
            ) {
                // Minimized by default in landscape so the face is 100% visible
                TelemetryHud(
                    metrics = liveMetrics,
                    initiallyExpanded = !isLandscape
                )

                Spacer(modifier = Modifier.height(if (isLandscape) 6.dp else 12.dp))

                if (isLandscape) {
                    // Single compact row in landscape mode to leave entire screen open for the face
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = onConfirmAnalysis,
                            modifier = Modifier
                                .weight(2f)
                                .height(44.dp)
                                .testTag("btn_lock_palette"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF7209B7)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "View Palette",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    androidx.activity.result.PickVisualMediaRequest(
                                        ActivityResultContracts.PickVisualMedia.ImageOnly
                                    )
                                )
                            },
                            modifier = Modifier
                                .weight(1.1f)
                                .height(44.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color.White
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Photo", color = Color.White, fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = onSwitchToManual,
                            modifier = Modifier
                                .weight(1.1f)
                                .height(44.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color.White
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Sliders", color = Color.White, fontSize = 12.sp)
                        }
                    }
                } else {
                    // Portrait Layout
                    Button(
                        onClick = onConfirmAnalysis,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("btn_lock_palette"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF7209B7)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Confirm & View Palette",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Alternative Options: Upload photo or Manual Input
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    androidx.activity.result.PickVisualMediaRequest(
                                        ActivityResultContracts.PickVisualMedia.ImageOnly
                                    )
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color.White
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Upload Photo", color = Color.White, fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = onSwitchToManual,
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color.White
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Manual Sliders", color = Color.White, fontSize = 12.sp)
                        }
                    }
                }
            }
        } else {
            // Permission Denied Fallback UI
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.errorContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Camera Permission Required",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "StyleSense requires camera access for skin tone analysis. All processing is performed locally on your device.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("btn_request_camera_perm"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Grant Permission")
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = onSwitchToManual,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("btn_fallback_manual"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Continue with Manual Color Selection")
                }
            }
        }

        // White Paper Calibration Modal Dialog
        if (showWhitePaperModal) {
            AlertDialog(
                onDismissRequest = { showWhitePaperModal = false },
                title = {
                    Text(
                        text = "White-Point Calibration",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "To calibrate for ambient room lighting, hold a plain white sheet of paper in front of your camera.",
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Adjusts color balance so skin tone detection remains consistent under any lighting conditions.",
                                fontSize = 11.sp,
                                modifier = Modifier.padding(12.dp),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            // Apply D65 daylight reference gains (IEC 61966-2-1 sRGB white point)
                            // These neutral gains center the white point without artificially warming or cooling.
                            // If a bitmap from the preview were available, use IlluminantNormalizer.calibrateWithWhitePaper(bitmap).
                            IlluminantNormalizer.customCalibrationGains = Triple(1.00f, 1.00f, 1.00f)
                            whitePaperCalibrated = true
                            showWhitePaperModal = false
                        }
                    ) {
                        Text("Apply Calibration")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showWhitePaperModal = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
