package com.kottland.searchplant.network

import android.graphics.Bitmap
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.kottland.searchplant.BuildConfig
import com.kottland.searchplant.data.model.PlantIdentificationResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Service class for interacting with Google Gemini API
 * Handles plant identification requests
 */
class GeminiApiService {
    
    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = getApiKey() // You'll need to add your API key
    )
    
    /**
     * Identify plant from image using Gemini API
     */
    suspend fun identifyPlant(bitmap: Bitmap): PlantIdentificationResponse {
        return withContext(Dispatchers.IO) {
            try {
                val prompt = buildPlantIdentificationPrompt()
                
                val inputContent = content {
                    image(bitmap)
                    text(prompt)
                }
                
                val response = generativeModel.generateContent(inputContent)
                val responseText = response.text ?: throw Exception("Empty response from Gemini API")
                
                // Parse the response
                parseGeminiResponse(responseText)
                
            } catch (e: Exception) {
                throw Exception("Failed to identify plant: ${e.message}", e)
            }
        }
    }
    
    /**
     * Build the prompt for plant identification
     */
    private fun buildPlantIdentificationPrompt(): String {
        return """
            Please analyze this image and identify the plant or object shown. Provide the following information in a structured format:
            
            Name: [Common name of the plant/object]
            Scientific Name: [Scientific/botanical name if it's a plant, or 'N/A' if not applicable]
            Description: [Detailed description of the plant/object, including key characteristics]
            Category: [Type such as 'flowering plant', 'tree', 'shrub', 'herb', 'succulent', 'object', etc.]
            Medicinal Uses: [Medicinal properties and traditional uses if applicable, or 'None known' if not medicinal]
            Confidence: [Your confidence level in this identification as a percentage from 0-100]
            
            Additional Information:
            - Family: [Plant family if applicable]
            - Native Region: [Where this plant is naturally found]
            - Care Instructions: [Basic care if it's a houseplant]
            - Toxicity: [Any toxicity warnings if applicable]
            
            Please be as accurate and detailed as possible. If you're not certain about the identification, please indicate your uncertainty and provide the most likely possibilities.
            
            Format your response exactly as shown above with clear labels.
        """.trimIndent()
    }
    
    /**
     * Parse Gemini API response into structured data
     */
    private fun parseGeminiResponse(responseText: String): PlantIdentificationResponse {
        try {
            val lines = responseText.lines().map { it.trim() }
            
            val name = extractValue(lines, "Name:") ?: "Unknown Plant"
            val scientificName = extractValue(lines, "Scientific Name:")?.takeIf { 
                it != "N/A" && it.isNotBlank() 
            }
            val description = extractValue(lines, "Description:") ?: "No description available"
            val category = extractValue(lines, "Category:") ?: "Unknown"
            val medicinalUses = extractValue(lines, "Medicinal Uses:")?.takeIf { 
                it != "None known" && it.isNotBlank() 
            }
            
            // Extract confidence score
            val confidenceText = extractValue(lines, "Confidence:")
            val confidenceScore = confidenceText?.let { 
                val numberRegex = """(\d+)""".toRegex()
                numberRegex.find(it)?.groupValues?.get(1)?.toFloatOrNull()?.div(100f)
            } ?: 0.5f
            
            // Extract additional information
            val additionalInfo = mutableMapOf<String, String>()
            extractValue(lines, "Family:")?.let { additionalInfo["family"] = it }
            extractValue(lines, "Native Region:")?.let { additionalInfo["nativeRegion"] = it }
            extractValue(lines, "Care Instructions:")?.let { additionalInfo["careInstructions"] = it }
            extractValue(lines, "Toxicity:")?.let { additionalInfo["toxicity"] = it }
            
            return PlantIdentificationResponse(
                name = name,
                scientificName = scientificName,
                description = description,
                medicinalUses = medicinalUses,
                category = category,
                confidenceScore = confidenceScore,
                additionalInfo = additionalInfo.takeIf { it.isNotEmpty() }
            )
            
        } catch (e: Exception) {
            // Fallback parsing if structured format fails
            return PlantIdentificationResponse(
                name = "Identified Plant",
                scientificName = null,
                description = responseText.take(500), // Use first 500 chars as description
                medicinalUses = null,
                category = "Plant",
                confidenceScore = 0.3f // Low confidence for fallback
            )
        }
    }
    
    /**
     * Extract value from response lines
     */
    private fun extractValue(lines: List<String>, label: String): String? {
        return lines.find { it.startsWith(label, ignoreCase = true) }
            ?.substringAfter(label)
            ?.trim()
            ?.takeIf { it.isNotBlank() }
    }
    
    /**
     * Get API key from BuildConfig
     */
    private fun getApiKey(): String {
        return BuildConfig.GEMINI_API_KEY
    }
}

/**
 * Exception class for API-related errors
 */
class PlantIdentificationException(message: String, cause: Throwable? = null) : Exception(message, cause)