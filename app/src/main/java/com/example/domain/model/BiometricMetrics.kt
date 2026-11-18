package com.example.domain.model

import java.io.Serializable

data class LabColor(
    val l: Float,
    val a: Float,
    val b: Float
) : Serializable {
    fun toRgbHex(): String {
        // Approximate Lab to sRGB for display
        val y = (l + 16.0) / 116.0
        val x = a / 500.0 + y
        val z = y - b / 200.0

        fun fInv(t: Double): Double {
            val delta = 6.0 / 29.0
            return if (t > delta) t * t * t else 3.0 * delta * delta * (t - 4.0 / 29.0)
        }

        val xn = 95.047
        val yn = 100.0
        val zn = 108.883

        val X = fInv(x) * xn / 100.0
        val Y = fInv(y) * yn / 100.0
        val Z = fInv(z) * zn / 100.0

        var r = X * 3.2406 - Y * 1.5372 - Z * 0.4986
        var g = -X * 0.9689 + Y * 1.8758 + Z * 0.0415
        var bVal = X * 0.0557 - Y * 0.2040 + Z * 1.0570

        fun srgbGamma(c: Double): Double {
            return if (c <= 0.0031308) 12.92 * c else 1.055 * Math.pow(c, 1.0 / 2.4) - 0.055
        }

        val rClamped = (srgbGamma(r.coerceIn(0.0, 1.0)) * 255.0).toInt().coerceIn(0, 255)
        val gClamped = (srgbGamma(g.coerceIn(0.0, 1.0)) * 255.0).toInt().coerceIn(0, 255)
        val bClamped = (srgbGamma(bVal.coerceIn(0.0, 1.0)) * 255.0).toInt().coerceIn(0, 255)

        return String.format("#%02X%02X%02X", rClamped, gClamped, bClamped)
    }
}

data class BiometricMetrics(
    val itaDegrees: Float,
    val melaninIndex: Float,
    val erythemaIndex: Float,
    val ambientKelvin: Int,
    val deltaE00: Float,
    val skinLab: LabColor,
    val cheekLeftLab: LabColor,
    val cheekRightLab: LabColor,
    val underEyeLab: LabColor,
    val undertoneHueAngle: Float,
    val featureTensor512: FloatArray,
    val itaClassification: String,
    val undertoneDescription: String,
    val contrastRatio: Float
) : Serializable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as BiometricMetrics
        return itaDegrees == other.itaDegrees &&
                melaninIndex == other.melaninIndex &&
                erythemaIndex == other.erythemaIndex &&
                ambientKelvin == other.ambientKelvin
    }

    override fun hashCode(): Int {
        var result = itaDegrees.hashCode()
        result = 31 * result + melaninIndex.hashCode()
        result = 31 * result + erythemaIndex.hashCode()
        result = 31 * result + ambientKelvin
        return result
    }

    companion object {
        fun defaultSample(): BiometricMetrics {
            return BiometricMetrics(
                itaDegrees = 38.5f,
                melaninIndex = 34.2f,
                erythemaIndex = 28.6f,
                ambientKelvin = 5500,
                deltaE00 = 1.15f,
                skinLab = LabColor(68.2f, 11.4f, 17.6f),
                cheekLeftLab = LabColor(67.5f, 13.8f, 16.2f),
                cheekRightLab = LabColor(67.8f, 13.5f, 16.4f),
                underEyeLab = LabColor(63.2f, 14.1f, 14.8f),
                undertoneHueAngle = 57.0f,
                featureTensor512 = FloatArray(512) { 0.5f },
                itaClassification = "Intermediate",
                undertoneDescription = "Neutral-Warm",
                contrastRatio = 1.82f
            )
        }
    }
}
