package com.example.di

import android.content.Context
import com.example.domain.colorimetry.BiometricProcessor
import com.example.presentation.camera.AmbientLuxSensor
import com.example.presentation.camera.CameraManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt DI module managing life-cycle dependencies for the CameraManager,
 * BiometricProcessor, and AmbientLuxSensor instances.
 */
@Module
@InstallIn(SingletonComponent::class)
object BiometricModule {

    @Provides
    @Singleton
    fun provideBiometricProcessor(): BiometricProcessor {
        return BiometricProcessor()
    }

    @Provides
    @Singleton
    fun provideCameraManager(
        @ApplicationContext context: Context,
        biometricProcessor: BiometricProcessor
    ): CameraManager {
        return CameraManager(context, biometricProcessor)
    }

    @Provides
    @Singleton
    fun provideAmbientLuxSensor(
        @ApplicationContext context: Context
    ): AmbientLuxSensor {
        return AmbientLuxSensor(context)
    }
}
