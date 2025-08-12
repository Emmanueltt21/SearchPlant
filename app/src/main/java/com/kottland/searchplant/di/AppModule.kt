package com.kottland.searchplant.di

import android.content.Context
import androidx.room.Room
import com.kottland.searchplant.data.dao.ScannedItemDao
import com.kottland.searchplant.data.database.SearchPlantDatabase
import com.kottland.searchplant.data.repository.PlantRepository
import com.kottland.searchplant.network.GeminiApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideSearchPlantDatabase(
        @ApplicationContext context: Context
    ): SearchPlantDatabase {
        return SearchPlantDatabase.getDatabase(context)
    }

    @Provides
    fun provideScannedItemDao(database: SearchPlantDatabase): ScannedItemDao {
        return database.scannedItemDao()
    }

    @Provides
    @Singleton
    fun provideGeminiApiService(): GeminiApiService {
        return GeminiApiService()
    }

    @Provides
    @Singleton
    fun providePlantRepository(
        scannedItemDao: ScannedItemDao,
        geminiApiService: GeminiApiService,
        @ApplicationContext context: Context
    ): PlantRepository {
        return PlantRepository(scannedItemDao, geminiApiService, context)
    }
}