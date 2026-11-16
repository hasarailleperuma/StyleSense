package com.example.presentation.camera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.example.domain.colorimetry.BiometricProcessor
import com.example.domain.colorimetry.DominantColorExtractor
import com.example.domain.model.BiometricMetrics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object PhotoPickerManager {

    /**
     * Decodes and analyzes user face photo in volatile RAM with strict GDPR Article 9 compliance.
     * Never writes image bytes to disk, cache, or database. Immediately calls bitmap.recycle().
     */
    suspend fun analyzeVolatileFacePhoto(
        context: Context,
        uri: Uri
    ): Result<BiometricMetrics> = withContext(Dispatchers.IO) {
        try {
            val contentResolver = context.contentResolver

            // 1. Inspect image dimensions without decoding full bitmap into memory
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, options)
            }

            // Downsample to ~480-640px to respect the < 15MB peak RAM constraint
            val targetSize = 512
            var inSampleSize = 1
            if (options.outHeight > targetSize || options.outWidth > targetSize) {
                val halfHeight = options.outHeight / 2
                val halfWidth = options.outWidth / 2
                while ((halfHeight / inSampleSize) >= targetSize && (halfWidth / inSampleSize) >= targetSize) {
                    inSampleSize *= 2
                }
            }

            options.inJustDecodeBounds = false
            options.inSampleSize = inSampleSize
            options.inPreferredConfig = Bitmap.Config.ARGB_8888

            // 2. Decode into volatile memory
            val volatileBitmap = contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, options)
            } ?: return@withContext Result.failure(Exception("Unable to decode photo stream"))

            // 3. Process biometric extraction
            val metrics = BiometricProcessor.processVolatileBitmap(volatileBitmap)

            // 4. UK GDPR Article 9: Purge raw pixels from memory immediately
            volatileBitmap.recycle()

            Result.success(metrics)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Decodes and digitizes a garment photo to extract dominant colors and category.
     * Recycles bitmap immediately afterwards.
     */
    suspend fun digitizeGarmentPhoto(
        context: Context,
        uri: Uri
    ): Result<DominantColorExtractor.GarmentAnalysis> = withContext(Dispatchers.IO) {
        try {
            val contentResolver = context.contentResolver
            val options = BitmapFactory.Options().apply {
                inSampleSize = 2
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }

            val volatileBitmap = contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, options)
            } ?: return@withContext Result.failure(Exception("Unable to decode garment photo"))

            val analysis = DominantColorExtractor.extractGarmentColors(volatileBitmap)

            volatileBitmap.recycle()
            Result.success(analysis)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
