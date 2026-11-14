package com.example.di

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.dao.SeasonProfileDao
import com.example.data.local.dao.WardrobeItemDao
import com.example.data.local.repository.WardrobeRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt DI module managing life-cycle dependencies for the Room database instances.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    fun provideWardrobeItemDao(database: AppDatabase): WardrobeItemDao {
        return database.wardrobeDao()
    }

    @Provides
    fun provideSeasonProfileDao(database: AppDatabase): SeasonProfileDao {
        return database.seasonProfileDao()
    }

    @Provides
    @Singleton
    fun provideWardrobeRepository(
        seasonProfileDao: SeasonProfileDao,
        wardrobeItemDao: WardrobeItemDao
    ): WardrobeRepository {
        return WardrobeRepository(seasonProfileDao, wardrobeItemDao)
    }
}
