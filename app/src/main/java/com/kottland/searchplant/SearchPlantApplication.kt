package com.kottland.searchplant

import android.app.Application
import com.kottland.searchplant.data.database.SearchPlantDatabase
import com.kottland.searchplant.data.repository.PlantRepository
import com.kottland.searchplant.network.GeminiApiService
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SearchPlantApplication : Application() {
    
    // Database instance
    val database by lazy { SearchPlantDatabase.getDatabase(this) }
    
    // Repository instance
    val repository by lazy { 
        PlantRepository(
            scannedItemDao = database.scannedItemDao(),
            geminiApiService = GeminiApiService(),
            context = this
        )
    }
    
    override fun onCreate() {
        super.onCreate()
    }
}