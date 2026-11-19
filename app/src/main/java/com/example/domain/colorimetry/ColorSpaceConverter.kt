package com.example.domain.colorimetry

import com.example.domain.model.LabColor
import kotlin.math.*

object ColorSpaceConverter {

    // Standard CIE D65 2° Standard Observer Tristimulus Values
    const val REF_X_D65 = 95.047
    const val REF_Y_D65 = 100.000
    const val REF_Z_D65 = 108.883

    /**
     * Converts standard sRGB (0-255) to CIE L*a*b* under D65 illuminant.
     */
    fun rgbToLab(r: Int, g: Int, b: Int): LabColor {
        // 1. Normalize sRGB to [0, 1]
        var rNorm = r / 255.0
        var gNorm = g / 255.0
        var bNorm = b / 255.0

        // 2. Inverse sRGB Companding (linearization)
        fun linearize(c: Double): Double {
            return if (c > 0.04045) {
                ((c + 0.055) / 1.055).pow(2.4)
            } else {
                c / 12.92
            }
        }

        rNorm = linearize(rNorm) * 100.0
        gNorm = linearize(gNorm) * 100.0
        bNorm = linearize(bNorm) * 100.0

        // 3. Convert to CIE XYZ (sRGB D65 matrix)
        val x = rNorm * 0.4124564 + gNorm * 0.3575761 + bNorm * 0.1804375
        val y = rNorm * 0.2126729 + gNorm * 0.7151522 + bNorm * 0.0721750
        val z = rNorm * 0.0193339 + gNorm * 0.1191920 + bNorm * 0.9503041

        // 4. Convert XYZ to CIE L*a*b*
        val xr = x / REF_X_D65
        val yr = y / REF_Y_D65
        val zr = z / REF_Z_D65

        val epsilon = 216.0 / 24389.0 // (6/29)^3
        val kappa = 24389.0 / 27.0    // (29/3)^3

        fun f(t: Double): Double {
            return if (t > epsilon) t.pow(1.0 / 3.0) else (kappa * t + 16.0) / 116.0
        }

        val fx = f(xr)
        val fy = f(yr)
        val fz = f(zr)

        val L = (116.0 * fy - 16.0).coerceIn(0.0, 100.0).toFloat()
        val aLab = (500.0 * (fx - fy)).toFloat()
        val bLab = (200.0 * (fy - fz)).toFloat()

        return LabColor(L, aLab, bLab)
    }

    /**
     * Converts a hex color string (e.g. "#0F4C81") to CIE L*a*b*.
     */
    fun hexToLab(hex: String): LabColor {
        val cleanHex = hex.removePrefix("#").trim()
        val intColor = try {
            if (cleanHex.length == 6) {
                cleanHex.toLong(16).toInt()
            } else if (cleanHex.length == 8) {
                cleanHex.substring(2).toLong(16).toInt()
            } else {
                0x808080
            }
        } catch (e: Exception) {
            0x808080
        }
        val r = (intColor shr 16) and 0xFF
        val g = (intColor shr 8) and 0xFF
        val b = intColor and 0xFF
        return rgbToLab(r, g, b)
    }

    /**
     * Calculates Individual Typology Angle:
     * ITA° = (atan2((L* - 50.0), b*) * 180) / PI
     */
    fun calculateITA(l: Float, b: Float): Float {
        val deltaL = (l - 50.0).toDouble()
        val bVal = max(0.001, b.toDouble()) // Avoid division/atan anomaly
        val angleRad = atan2(deltaL, bVal)
        return (angleRad * 180.0 / PI).toFloat()
    }

    fun classifyITA(ita: Float): String {
        return when {
            ita > 55.0f -> "Very Light"
            ita > 41.0f -> "Light"
            ita > 28.0f -> "Intermediate"
            ita > 10.0f -> "Tan"
            ita > -30.0f -> "Brown"
            else -> "Dark"
        }
    }

    /**
     * Computes Melanin Index (MI): MI = 100 * log10(1 / R_red)
     * Normalized red reflectance from 0.01 to 1.0.
     */
    fun calculateMelaninIndex(rNorm: Float): Float {
        val safeR = max(0.01f, min(1.0f, rNorm))
        return (100.0 * log10(1.0 / safeR)).toFloat()
    }

    /**
     * Computes Erythema Index (EI): EI = 100 * log10(1 / G_green)
     * Normalized green reflectance from 0.01 to 1.0.
     */
    fun calculateErythemaIndex(gNorm: Float): Float {
        val safeG = max(0.01f, min(1.0f, gNorm))
        return (100.0 * log10(1.0 / safeG)).toFloat()
    }

    /**
     * Computes Undertone Hue Angle h_ab = atan2(b*, a*) in degrees [0, 360)
     */
    fun calculateHueAngle(a: Float, b: Float): Float {
        var h = (atan2(b.toDouble(), a.toDouble()) * 180.0 / PI).toFloat()
        if (h < 0) h += 360f
        return h
    }

    /**
     * Calculates CIEDE2000 (ΔE00) color difference between two Lab colors.
     */
    fun calculateDeltaE00(lab1: LabColor, lab2: LabColor): Float {
        val L1 = lab1.l.toDouble()
        val a1 = lab1.a.toDouble()
        val b1 = lab1.b.toDouble()

        val L2 = lab2.l.toDouble()
        val a2 = lab2.a.toDouble()
        val b2 = lab2.b.toDouble()

        val avgL = (L1 + L2) / 2.0
        val C1 = sqrt(a1.pow(2.0) + b1.pow(2.0))
        val C2 = sqrt(a2.pow(2.0) + b2.pow(2.0))
        val avgC = (C1 + C2) / 2.0

        val G = 0.5 * (1.0 - sqrt(avgC.pow(7.0) / (avgC.pow(7.0) + 25.0.pow(7.0))))
        val a1Prime = (1.0 + G) * a1
        val a2Prime = (1.0 + G) * a2

        val C1Prime = sqrt(a1Prime.pow(2.0) + b1.pow(2.0))
        val C2Prime = sqrt(a2Prime.pow(2.0) + b2.pow(2.0))
        val avgCPrime = (C1Prime + C2Prime) / 2.0

        fun getHPrime(aP: Double, b: Double): Double {
            if (aP == 0.0 && b == 0.0) return 0.0
            var h = Math.toDegrees(atan2(b, aP))
            if (h < 0.0) h += 360.0
            return h
        }

        val h1Prime = getHPrime(a1Prime, b1)
        val h2Prime = getHPrime(a2Prime, b2)

        val deltaLPrime = L2 - L1
        val deltaCPrime = C2Prime - C1Prime

        var deltahPrime: Double
        if (C1Prime * C2Prime == 0.0) {
            deltahPrime = 0.0
        } else if (abs(h2Prime - h1Prime) <= 180.0) {
            deltahPrime = h2Prime - h1Prime
        } else if (h2Prime - h1Prime > 180.0) {
            deltahPrime = (h2Prime - h1Prime) - 360.0
        } else {
            deltahPrime = (h2Prime - h1Prime) + 360.0
        }

        val deltaHPrime = 2.0 * sqrt(C1Prime * C2Prime) * sin(Math.toRadians(deltahPrime / 2.0))

        val avgHPrime: Double
        if (C1Prime * C2Prime == 0.0) {
            avgHPrime = h1Prime + h2Prime
        } else if (abs(h1Prime - h2Prime) <= 180.0) {
            avgHPrime = (h1Prime + h2Prime) / 2.0
        } else if (h1Prime + h2Prime < 360.0) {
            avgHPrime = (h1Prime + h2Prime + 360.0) / 2.0
        } else {
            avgHPrime = (h1Prime + h2Prime - 360.0) / 2.0
        }

        val T = 1.0 - 0.17 * cos(Math.toRadians(avgHPrime - 30.0)) +
                0.24 * cos(Math.toRadians(2.0 * avgHPrime)) +
                0.32 * cos(Math.toRadians(3.0 * avgHPrime + 6.0)) -
                0.20 * cos(Math.toRadians(4.0 * avgHPrime - 63.0))

        val SL = 1.0 + (0.015 * (avgL - 50.0).pow(2.0)) / sqrt(20.0 + (avgL - 50.0).pow(2.0))
        val SC = 1.0 + 0.045 * avgCPrime
        val SH = 1.0 + 0.015 * avgCPrime * T

        val deltaTheta = 30.0 * exp(-((avgHPrime - 275.0) / 25.0).pow(2.0))
        val RC = 2.0 * sqrt(avgCPrime.pow(7.0) / (avgCPrime.pow(7.0) + 25.0.pow(7.0)))
        val RT = -sin(Math.toRadians(2.0 * deltaTheta)) * RC

        val termL = deltaLPrime / SL
        val termC = deltaCPrime / SC
        val termH = deltaHPrime / SH

        return sqrt(termL.pow(2.0) + termC.pow(2.0) + termH.pow(2.0) + RT * termC * termH).toFloat()
    }
}
