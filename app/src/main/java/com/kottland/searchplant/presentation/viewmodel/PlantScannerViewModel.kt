package com.kottland.searchplant.presentation.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kottland.searchplant.data.model.ScannedItem
import com.kottland.searchplant.data.model.ScannedItemSummary
import com.kottland.searchplant.data.repository.PlantRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Sealed class representing different scan states
 */
sealed class ScanState {
    object Idle : ScanState()
    object Scanning : ScanState()
    object Success : ScanState()
    object Error : ScanState()
}

/**
 * ViewModel for the Plant Scanner app
 * Manages UI state and business logic using MVVM pattern
 */
@HiltViewModel
class PlantScannerViewModel @Inject constructor(
    private val repository: PlantRepository
) : ViewModel() {
    
    // UI State for scanning
    private val _scanState = MutableStateFlow<ScanState>(ScanState.Idle)
    val scanState: StateFlow<ScanState> = _scanState.asStateFlow()
    
    // UI State for scan result
    private val _currentScanResult = MutableStateFlow<ScannedItem?>(null)
    val currentScanResult: StateFlow<ScannedItem?> = _currentScanResult.asStateFlow()
    
    // Error state
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    // History items
    val historyItems: StateFlow<List<ScannedItemSummary>> = repository.getItemSummaries()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    // Favorite items
    val favoriteItems: StateFlow<List<ScannedItem>> = repository.getFavoriteItems()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    // Search query and results
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    val searchResults: StateFlow<List<ScannedItem>> = _searchQuery
        .debounce(300) // Debounce search queries
        .flatMapLatest { query ->
            if (query.isBlank()) {
                flowOf(emptyList())
            } else {
                repository.searchItems(query)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    /**
     * Scan plant from captured image
     */
    fun scanPlant(bitmap: Bitmap, imageUri: String) {
        viewModelScope.launch {
            try {
                _scanState.value = ScanState.Scanning
                _errorMessage.value = null
                
                val result = repository.identifyPlant(bitmap, imageUri)
                
                result.fold(
                    onSuccess = { scannedItem ->
                        _scanState.value = ScanState.Success
                        _currentScanResult.value = scannedItem
                    },
                    onFailure = { exception ->
                        _scanState.value = ScanState.Error
                        _errorMessage.value = exception.message ?: "Unknown error occurred"
                    }
                )
                
            } catch (e: Exception) {
                _scanState.value = ScanState.Error
                _errorMessage.value = e.message ?: "Failed to scan plant"
            }
        }
    }
    
    /**
     * Reset scan state
     */
    fun resetScanState() {
        _scanState.value = ScanState.Idle
        _currentScanResult.value = null
        _errorMessage.value = null
    }
    
    /**
     * Clear current scan result to prevent re-navigation
     */
    fun clearCurrentScanResult() {
        _currentScanResult.value = null
    }
    
    /**
     * Toggle favorite status of an item
     */
    fun toggleFavorite(itemId: Long, isFavorite: Boolean) {
        viewModelScope.launch {
            try {
                repository.toggleFavorite(itemId, isFavorite)
                
                // Update current scan result if it matches
                _currentScanResult.value?.let { current ->
                    if (current.id == itemId) {
                        _currentScanResult.value = current.copy(isFavorite = isFavorite)
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update favorite: ${e.message}"
            }
        }
    }
    
    /**
     * Delete an item from history
     */
    fun deleteItem(itemId: Long) {
        viewModelScope.launch {
            try {
                repository.deleteItemById(itemId)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to delete item: ${e.message}"
            }
        }
    }
    
    /**
     * Get item details by ID
     */
    suspend fun getItemById(id: Long): ScannedItem? {
        return try {
            repository.getItemById(id)
        } catch (e: Exception) {
            _errorMessage.value = "Failed to load item: ${e.message}"
            null
        }
    }
    
    /**
     * Update search query
     */
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    /**
     * Clear search query
     */
    fun clearSearch() {
        _searchQuery.value = ""
    }
    
    /**
     * Clear error message
     */
    fun clearError() {
        _errorMessage.value = null
    }
    
    /**
     * Get recent items for dashboard
     */
    fun getRecentItems() = flow {
        try {
            val recentItems = repository.getRecentItems(5)
            emit(recentItems)
        } catch (e: Exception) {
            _errorMessage.value = "Failed to load recent items: ${e.message}"
            emit(emptyList<ScannedItem>())
        }
    }
    
    /**
     * Get app statistics
     */
    fun getStatistics() = flow {
        try {
            val stats = repository.getStatistics()
            emit(stats)
        } catch (e: Exception) {
            _errorMessage.value = "Failed to load statistics: ${e.message}"
            emit(null)
        }
    }
}

/**
 * UI state for different screens
 */
data class HomeUiState(
    val isLoading: Boolean = false,
    val recentItems: List<ScannedItem> = emptyList(),
    val errorMessage: String? = null
)

data class HistoryUiState(
    val items: List<ScannedItemSummary> = emptyList(),
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val searchResults: List<ScannedItem> = emptyList(),
    val errorMessage: String? = null
)

data class FavoritesUiState(
    val items: List<ScannedItem> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)