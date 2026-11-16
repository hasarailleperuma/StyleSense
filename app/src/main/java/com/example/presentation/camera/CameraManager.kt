package com.example.presentation.camera

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import android.util.Size
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.example.domain.colorimetry.BiometricProcessor
import com.example.domain.model.BiometricMetrics
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.inject.Inject
import javax.inject.Singleton

/**
 * CameraManager handles CameraX initialization, lifecycle management, and
 * volatile frame processing via ImageAnalysis.
 *
 * UK GDPR ARTICLE 9 & PRIVACY MANDATES:
 * - All frame pixels remain strictly in volatile RAM.
 * - No images, frames, or biometric data are ever written to disk or network.
 * - Bitmaps are immediately recycled after colorimetric feature extraction.
 */
@Singleton
class CameraManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val biometricProcessor: BiometricProcessor
) {
    companion object {
        private const val TAG = "CameraManager"
    }

    val ambientLuxSensor: AmbientLuxSensor = AmbientLuxSensor(context)
    private val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private var cameraProvider: ProcessCameraProvider? = null
    private var imageAnalysis: ImageAnalysis? = null
    private var preview: Preview? = null
    private var camera: Camera? = null
    private var boundLifecycleOwner: LifecycleOwner? = null

    // Secondary constructor for non-DI / legacy instantiations
    constructor(context: Context, lifecycleOwner: LifecycleOwner) : this(context, BiometricProcessor()) {
        this.boundLifecycleOwner = lifecycleOwner
    }

    var isFrontCamera: Boolean = true
        private set

    var manualRotationOffset: Int = 0
    var isMirrored: Boolean = false

    private var lastAnalyzedTimestamp = 0L

    @SuppressLint("UnsafeOptInUsageError")
    fun startCamera(
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView,
        onMetricsExtracted: (BiometricMetrics) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        this.boundLifecycleOwner = lifecycleOwner
        startCamera(previewView, onMetricsExtracted, onError)
    }

    @SuppressLint("UnsafeOptInUsageError")
    fun startCamera(
        previewView: PreviewView,
        onMetricsExtracted: (BiometricMetrics) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        val owner = boundLifecycleOwner
            ?: run {
                val err = IllegalStateException("LifecycleOwner must be provided before or during startCamera")
                Log.e(TAG, "Missing LifecycleOwner", err)
                onError(err)
                return
            }

        // Configure PreviewView to COMPATIBLE mode (TextureView) for reliable emulator & streaming rendering
        previewView.implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        ambientLuxSensor.startListening()

        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                val provider = cameraProviderFuture.get()
                cameraProvider = provider

                // Check for camera availability and fallback dynamically
                val cameraSelector = getBestCameraSelector(provider)
                if (cameraSelector == null) {
                    val err = IllegalStateException("No available camera sensors detected on this device or emulator.")
                    Log.e(TAG, "No cameras available in provider", err)
                    onError(err)
                    return@addListener
                }

                // Setup Preview
                preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                // Setup ImageAnalysis using universal YUV / platform default
                imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .setTargetResolution(Size(480, 640))
                    .build()
                    .also { analysis ->
                        analysis.setAnalyzer(cameraExecutor) { imageProxy ->
                            try {
                                val currentTime = System.currentTimeMillis()
                                // Process at ~20-25 FPS to conserve battery while giving smooth telemetry
                                if (currentTime - lastAnalyzedTimestamp >= 40) {
                                    lastAnalyzedTimestamp = currentTime
                                    processImageProxy(imageProxy, onMetricsExtracted)
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "Error in image analyzer", e)
                            } finally {
                                imageProxy.close()
                            }
                        }
                    }

                // Unbind previous bindings before re-binding to lifecycle
                provider.unbindAll()
                camera = provider.bindToLifecycle(
                    owner,
                    cameraSelector,
                    preview,
                    imageAnalysis
                )
                Log.i(TAG, "Camera bound successfully to lifecycle. CameraSelector: $cameraSelector")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start camera", e)
                onError(e)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    private fun getBestCameraSelector(provider: ProcessCameraProvider): CameraSelector? {
        val frontExists = provider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA)
        val backExists = provider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA)

        return when {
            isFrontCamera && frontExists -> CameraSelector.DEFAULT_FRONT_CAMERA
            !isFrontCamera && backExists -> CameraSelector.DEFAULT_BACK_CAMERA
            backExists -> {
                isFrontCamera = false
                CameraSelector.DEFAULT_BACK_CAMERA
            }
            frontExists -> {
                isFrontCamera = true
                CameraSelector.DEFAULT_FRONT_CAMERA
            }
            else -> null
        }
    }

    private fun processImageProxy(
        imageProxy: ImageProxy,
        onMetricsExtracted: (BiometricMetrics) -> Unit
    ) {
        // Convert to Bitmap using CameraX built-in toBitmap()
        val rawBitmap = imageProxy.toBitmap()

        val rotationDegrees = imageProxy.imageInfo.rotationDegrees
        val totalRotation = (rotationDegrees + manualRotationOffset) % 360
        val matrix = Matrix().apply {
            if (totalRotation != 0) {
                postRotate(totalRotation.toFloat())
            }
            val shouldMirror = if (isFrontCamera) !isMirrored else isMirrored
            if (shouldMirror) {
                postScale(-1f, 1f, (rawBitmap.width / 2).toFloat(), (rawBitmap.height / 2).toFloat())
            }
        }

        val orientedBitmap = if (rotationDegrees != 0 || isFrontCamera) {
            Bitmap.createBitmap(rawBitmap, 0, 0, rawBitmap.width, rawBitmap.height, matrix, true).also {
                if (it != rawBitmap) {
                    rawBitmap.recycle()
                }
            }
        } else {
            rawBitmap
        }

        // Downsample to volatile buffer (384x512) for colorimetric feature extraction
        val scaledBitmap = Bitmap.createScaledBitmap(orientedBitmap, 384, 512, true)
        if (scaledBitmap != orientedBitmap) {
            orientedBitmap.recycle()
        }

        // Colorimetric feature extraction strictly in volatile RAM
        val metrics = biometricProcessor.processVolatileBitmap(scaledBitmap)

        // UK GDPR Article 9: Purge volatile memory immediately
        scaledBitmap.recycle()

        onMetricsExtracted(metrics)
    }

    fun toggleCamera(
        previewView: PreviewView,
        onMetricsExtracted: (BiometricMetrics) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        val provider = cameraProvider ?: return
        val frontExists = provider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA)
        val backExists = provider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA)

        if (isFrontCamera && backExists) {
            isFrontCamera = false
        } else if (!isFrontCamera && frontExists) {
            isFrontCamera = true
        } else {
            Log.w(TAG, "No secondary camera available to switch to.")
            return
        }

        startCamera(previewView, onMetricsExtracted, onError)
    }

    fun stopCamera() {
        try {
            ambientLuxSensor.stopListening()
            cameraProvider?.unbindAll()
            camera = null
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping camera", e)
        }
    }

    fun release() {
        stopCamera()
        cameraExecutor.shutdown()
    }
}
