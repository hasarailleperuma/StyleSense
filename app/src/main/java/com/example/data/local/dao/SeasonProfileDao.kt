package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.SeasonProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SeasonProfileDao {

    @Query("SELECT * FROM season_profiles WHERE id = 1 LIMIT 1")
    fun getActiveProfile(): Flow<SeasonProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProfile(profile: SeasonProfileEntity)

    @Query("DELETE FROM season_profiles")
    suspend fun clearProfile()
}
