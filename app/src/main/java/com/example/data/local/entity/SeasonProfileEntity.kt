package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Database entity storing anonymous colorimetry profile metrics and seasonal classification.
 */
@Entity(tableName = "season_profiles")
data class SeasonProfileEntity(
    @PrimaryKey
    val id: Int = 1,
    val seasonId: String,
    val itaDegrees: Float,
    val melaninIndex: Float,
    val erythemaIndex: Float,
    val ambientKelvin: Int,
    val undertone: String,
    val itaClassification: String,
    val silhouetteName: String?,
    val analyzedTimestamp: Long = System.currentTimeMillis()
)
