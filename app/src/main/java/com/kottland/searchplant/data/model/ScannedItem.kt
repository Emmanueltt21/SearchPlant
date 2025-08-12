package com.kottland.searchplant.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Data model for scanned plant/object items
 * Represents a plant or object that has been scanned and identified
 */
@Entity(tableName = "scanned_items")
data class ScannedItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    /** The common name of the plant/object */
    val name: String,
    
    /** Detailed description of the plant/object */
    val description: String,
    
    /** Scientific/botanical name (nullable for non-plants) */
    val scientificName: String? = null,
    
    /** URI/path to the captured image */
    val imageUri: String,
    
    /** Medicinal properties and uses (nullable for non-medicinal items) */
    val medicinalUses: String? = null,
    
    /** Timestamp when the item was scanned */
    val timestamp: Long = System.currentTimeMillis(),
    
    /** Whether the item is marked as favorite */
    val isFavorite: Boolean = false,
    
    /** Confidence score from the AI identification (0.0 to 1.0) */
    val confidenceScore: Float = 0.0f,
    
    /** Category of the identified item (plant, flower, tree, etc.) */
    val category: String? = null,
    
    /** Additional properties in JSON format for extensibility */
    val additionalProperties: String? = null
)

/**
 * Simplified data class for displaying items in lists
 */
data class ScannedItemSummary(
    val id: Long,
    val name: String,
    val imageUri: String,
    val timestamp: Long,
    val isFavorite: Boolean
)

/**
 * Data class for API response from Gemini
 */
data class PlantIdentificationResponse(
    val name: String,
    val scientificName: String?,
    val description: String,
    val medicinalUses: String?,
    val category: String?,
    val confidenceScore: Float,
    val additionalInfo: Map<String, String>? = null
)