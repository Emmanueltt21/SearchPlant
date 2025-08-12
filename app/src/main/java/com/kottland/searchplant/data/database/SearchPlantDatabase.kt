package com.kottland.searchplant.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.kottland.searchplant.data.dao.ScannedItemDao
import com.kottland.searchplant.data.model.ScannedItem

/**
 * Room database for SearchPlant app
 * Manages local storage of scanned items
 */
@Database(
    entities = [ScannedItem::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class SearchPlantDatabase : RoomDatabase() {
    
    /**
     * Provides access to ScannedItem DAO
     */
    abstract fun scannedItemDao(): ScannedItemDao
    
    companion object {
        @Volatile
        private var INSTANCE: SearchPlantDatabase? = null
        
        private const val DATABASE_NAME = "search_plant_database"
        
        /**
         * Get database instance (Singleton pattern)
         */
        fun getDatabase(context: Context): SearchPlantDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SearchPlantDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration() // For development - remove in production
                    .build()
                INSTANCE = instance
                instance
            }
        }
        
        /**
         * Close database instance (for testing)
         */
        fun closeDatabase() {
            INSTANCE?.close()
            INSTANCE = null
        }
    }
}