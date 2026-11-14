package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity for storing local clothing items and their color swatches.
 */
@Entity(tableName = "wardrobe_items")
data class WardrobeItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val category: String, // e.g., TOPS, OUTERWEAR, TROUSERS, DRESSES, ACCESSORIES
    val primaryHex: String,
    val secondaryHex: String,
    val seasonScore: Int = 0,
    val harmonyTag: String = "Harmonized",
    val favorite: Boolean = false,
    val createdTimestamp: Long = System.currentTimeMillis()
)

/**
 * Typealias preserving full backward compatibility across the codebase.
 */
typealias WardrobeItemEntity = WardrobeItem
