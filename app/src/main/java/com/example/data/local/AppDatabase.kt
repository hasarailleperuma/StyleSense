package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.SeasonProfileDao
import com.example.data.local.dao.WardrobeItemDao
import com.example.data.local.entity.SeasonProfileEntity
import com.example.data.local.entity.WardrobeItem

@Database(
    entities = [SeasonProfileEntity::class, WardrobeItem::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun seasonProfileDao(): SeasonProfileDao
    abstract fun wardrobeDao(): WardrobeItemDao
    fun wardrobeItemDao(): WardrobeItemDao = wardrobeDao()

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "stylesense_database.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
