package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.WardrobeItem
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for local wardrobe items and color swatches.
 */
@Dao
interface WardrobeItemDao {

    @Query("SELECT * FROM wardrobe_items ORDER BY createdTimestamp DESC")
    fun getAllItems(): Flow<List<WardrobeItem>>

    @Query("SELECT * FROM wardrobe_items WHERE category = :category ORDER BY createdTimestamp DESC")
    fun getItemsByCategory(category: String): Flow<List<WardrobeItem>>

    @Query("SELECT * FROM wardrobe_items WHERE id = :id")
    suspend fun getItemById(id: Long): WardrobeItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: WardrobeItem): Long

    @Update
    suspend fun updateItem(item: WardrobeItem)

    @Delete
    suspend fun deleteItem(item: WardrobeItem)

    @Query("DELETE FROM wardrobe_items WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM wardrobe_items")
    suspend fun getCount(): Int
}

/**
 * Typealias preserving full backward compatibility across the codebase.
 */
typealias WardrobeDao = WardrobeItemDao
