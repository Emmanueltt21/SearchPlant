package com.kottland.searchplant.data.dao

import androidx.room.*
import com.kottland.searchplant.data.model.ScannedItem
import com.kottland.searchplant.data.model.ScannedItemSummary
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for ScannedItem entity
 * Provides methods for database operations
 */
@Dao
interface ScannedItemDao {
    
    /**
     * Get all scanned items ordered by timestamp (newest first)
     */
    @Query("SELECT * FROM scanned_items ORDER BY timestamp DESC")
    fun getAllItems(): Flow<List<ScannedItem>>
    
    /**
     * Get all favorite items
     */
    @Query("SELECT * FROM scanned_items WHERE isFavorite = 1 ORDER BY timestamp DESC")
    fun getFavoriteItems(): Flow<List<ScannedItem>>
    
    /**
     * Get item summaries for history list (optimized for performance)
     */
    @Query("SELECT id, name, imageUri, timestamp, isFavorite FROM scanned_items ORDER BY timestamp DESC")
    fun getItemSummaries(): Flow<List<ScannedItemSummary>>
    
    /**
     * Get a specific item by ID
     */
    @Query("SELECT * FROM scanned_items WHERE id = :id")
    suspend fun getItemById(id: Long): ScannedItem?
    
    /**
     * Search items by name (case-insensitive)
     */
    @Query("SELECT * FROM scanned_items WHERE name LIKE '%' || :query || '%' OR scientificName LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchItems(query: String): Flow<List<ScannedItem>>
    
    /**
     * Check if an item with similar characteristics already exists (for caching)
     */
    @Query("SELECT * FROM scanned_items WHERE name = :name AND scientificName = :scientificName LIMIT 1")
    suspend fun findSimilarItem(name: String, scientificName: String?): ScannedItem?
    
    /**
     * Insert a new scanned item
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ScannedItem): Long
    
    /**
     * Update an existing item
     */
    @Update
    suspend fun updateItem(item: ScannedItem)
    
    /**
     * Toggle favorite status of an item
     */
    @Query("UPDATE scanned_items SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavoriteStatus(id: Long, isFavorite: Boolean)
    
    /**
     * Delete a specific item
     */
    @Delete
    suspend fun deleteItem(item: ScannedItem)
    
    /**
     * Delete item by ID
     */
    @Query("DELETE FROM scanned_items WHERE id = :id")
    suspend fun deleteItemById(id: Long)
    
    /**
     * Delete all items (for testing or reset functionality)
     */
    @Query("DELETE FROM scanned_items")
    suspend fun deleteAllItems()
    
    /**
     * Get count of all items
     */
    @Query("SELECT COUNT(*) FROM scanned_items")
    suspend fun getItemCount(): Int
    
    /**
     * Get count of favorite items
     */
    @Query("SELECT COUNT(*) FROM scanned_items WHERE isFavorite = 1")
    suspend fun getFavoriteCount(): Int
    
    /**
     * Get items by category
     */
    @Query("SELECT * FROM scanned_items WHERE category = :category ORDER BY timestamp DESC")
    fun getItemsByCategory(category: String): Flow<List<ScannedItem>>
    
    /**
     * Get recent items (last 10)
     */
    @Query("SELECT * FROM scanned_items ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentItems(limit: Int = 10): List<ScannedItem>
}