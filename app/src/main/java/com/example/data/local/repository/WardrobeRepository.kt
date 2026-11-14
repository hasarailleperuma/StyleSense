package com.example.data.local.repository

import com.example.data.local.dao.SeasonProfileDao
import com.example.data.local.dao.WardrobeItemDao
import com.example.data.local.entity.SeasonProfileEntity
import com.example.data.local.entity.WardrobeItem
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WardrobeRepository @Inject constructor(
    private val seasonProfileDao: SeasonProfileDao,
    private val wardrobeDao: WardrobeItemDao
) {
    val activeProfile: Flow<SeasonProfileEntity?> = seasonProfileDao.getActiveProfile()
    val allWardrobeItems: Flow<List<WardrobeItem>> = wardrobeDao.getAllItems()

    fun getItemsByCategory(category: String): Flow<List<WardrobeItem>> {
        return wardrobeDao.getItemsByCategory(category)
    }

    suspend fun saveProfile(profile: SeasonProfileEntity) {
        seasonProfileDao.saveProfile(profile)
    }

    suspend fun clearProfile() {
        seasonProfileDao.clearProfile()
    }

    suspend fun addWardrobeItem(item: WardrobeItem): Long {
        return wardrobeDao.insertItem(item)
    }

    suspend fun updateWardrobeItem(item: WardrobeItem) {
        wardrobeDao.updateItem(item)
    }

    suspend fun deleteWardrobeItem(id: Long) {
        wardrobeDao.deleteById(id)
    }

    suspend fun getWardrobeCount(): Int {
        return wardrobeDao.getCount()
    }
}
