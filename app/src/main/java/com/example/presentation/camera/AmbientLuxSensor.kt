package com.example.presentation.camera

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Hardware illuminance sensor monitor for verifying ambient lighting conditions.
 * Threshold: Sensor.TYPE_LIGHT >= 150 lux.
 */
@Singleton
class AmbientLuxSensor @Inject constructor(
    private val context: Context
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val lightSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_LIGHT)

    private val _currentLux = MutableStateFlow(450f) // Default daylight assumption
    val currentLux: StateFlow<Float> = _currentLux.asStateFlow()

    private val _isLuxAdequate = MutableStateFlow(true)
    val isLuxAdequate: StateFlow<Boolean> = _isLuxAdequate.asStateFlow()

    companion object {
        const val MINIMUM_REQUIRED_LUX = 150f // Minimum recommended ambient light level
    }

    fun startListening() {
        if (lightSensor != null && sensorManager != null) {
            sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stopListening() {
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_LIGHT) {
            val lux = event.values.firstOrNull() ?: return
            _currentLux.value = lux
            _isLuxAdequate.value = lux >= MINIMUM_REQUIRED_LUX
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No-op
    }
}
