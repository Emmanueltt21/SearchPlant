package com.kottland.searchplant.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.kottland.searchplant.data.dao.ScannedItemDao
import com.kottland.searchplant.data.model.PlantIdentificationResponse
import com.kottland.searchplant.data.model.ScannedItem
import com.kottland.searchplant.data.model.ScannedItemSummary
import com.kottland.searchplant.network.GeminiApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository class that handles data operations for the SearchPlant app
 * Serves as a single source of truth for plant data
 */
@Singleton
class PlantRepository @Inject constructor(
    private val scannedItemDao: ScannedItemDao,
    private val geminiApiService: GeminiApiService,
    private val context: Context
) {
    
    /**
     * Get all scanned items
     */
    fun getAllItems(): Flow<List<ScannedItem>> = scannedItemDao.getAllItems()
    
    /**
     * Get all favorite items
     */
    fun getFavoriteItems(): Flow<List<ScannedItem>> = scannedItemDao.getFavoriteItems()
    
    /**
     * Get item summaries for history list
     */
    fun getItemSummaries(): Flow<List<ScannedItemSummary>> = scannedItemDao.getItemSummaries()
    
    /**
     * Get a specific item by ID
     */
    suspend fun getItemById(id: Long): ScannedItem? = scannedItemDao.getItemById(id)
    
    /**
     * Search items by query
     */
    fun searchItems(query: String): Flow<List<ScannedItem>> = scannedItemDao.searchItems(query)
    
    /**
     * Identify plant from image using Gemini API
     * Includes offline caching logic
     */
    suspend fun identifyPlant(
        bitmap: Bitmap,
        imageUri: String
    ): Result<ScannedItem> {
        return try {
            // Check if we're online
            if (!isNetworkAvailable()) {
                return Result.failure(Exception("No internet connection. Please check your network and try again."))
            }
            
            // Call Gemini API to identify the plant
            val response = geminiApiService.identifyPlant(bitmap)
            
            // Check if we already have a similar item in cache
            val existingItem = scannedItemDao.findSimilarItem(
                response.name,
                response.scientificName
            )
            
            val scannedItem = if (existingItem != null) {
                // Update existing item with new image and timestamp
                existingItem.copy(
                    imageUri = imageUri,
                    timestamp = System.currentTimeMillis(),
                    confidenceScore = response.confidenceScore
                )
            } else {
                // Create new item
                ScannedItem(
                    name = response.name,
                    description = response.description,
                    scientificName = response.scientificName,
                    imageUri = imageUri,
                    medicinalUses = response.medicinalUses,
                    timestamp = System.currentTimeMillis(),
                    confidenceScore = response.confidenceScore,
                    category = response.category,
                    additionalProperties = response.additionalInfo?.let { 
                        // Convert map to JSON string if needed
                        it.entries.joinToString(",") { "${it.key}:${it.value}" }
                    }
                )
            }
            
            // Save to database
            val id = scannedItemDao.insertItem(scannedItem)
            val savedItem = scannedItem.copy(id = id)
            
            Result.success(savedItem)
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Toggle favorite status of an item
     */
    suspend fun toggleFavorite(id: Long, isFavorite: Boolean) {
        scannedItemDao.updateFavoriteStatus(id, isFavorite)
    }
    
    /**
     * Delete an item
     */
    suspend fun deleteItem(item: ScannedItem) {
        scannedItemDao.deleteItem(item)
    }
    
    /**
     * Delete item by ID
     */
    suspend fun deleteItemById(id: Long) {
        scannedItemDao.deleteItemById(id)
    }
    
    /**
     * Get items by category
     */
    fun getItemsByCategory(category: String): Flow<List<ScannedItem>> = 
        scannedItemDao.getItemsByCategory(category)
    
    /**
     * Get recent items
     */
    suspend fun getRecentItems(limit: Int = 10): List<ScannedItem> = 
        scannedItemDao.getRecentItems(limit)
    
    /**
     * Get statistics
     */
    suspend fun getStatistics(): RepositoryStatistics {
        return RepositoryStatistics(
            totalItems = scannedItemDao.getItemCount(),
            favoriteItems = scannedItemDao.getFavoriteCount(),
            recentItems = scannedItemDao.getRecentItems(5)
        )
    }
    
    /**
     * Check if network is available
     */
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        
        return networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
    }
    
    /**
     * Clear all data (for testing or reset)
     */
    suspend fun clearAllData() {
        scannedItemDao.deleteAllItems()
    }
}

/**
 * Data class for repository statistics
 */
data class RepositoryStatistics(
    val totalItems: Int,
    val favoriteItems: Int,
    val recentItems: List<ScannedItem>
)