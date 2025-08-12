package com.kottland.searchplant.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kottland.searchplant.presentation.screens.FavoritesScreen
import com.kottland.searchplant.presentation.screens.HistoryScreen
import com.kottland.searchplant.presentation.screens.HomeScreen
import com.kottland.searchplant.presentation.screens.ScanResultScreen
import com.kottland.searchplant.presentation.screens.SettingsScreen

/**
 * Navigation routes for the SearchPlant app
 */
object SearchPlantRoutes {
    const val HOME = "home"
    const val SCAN_RESULT = "scan_result/{itemId}"
    const val HISTORY = "history"
    const val FAVORITES = "favorites"
    const val SETTINGS = "settings"
    
    fun scanResult(itemId: Long) = "scan_result/$itemId"
}

/**
 * Main navigation component for SearchPlant app
 */
@Composable
fun SearchPlantNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = SearchPlantRoutes.HOME,
        modifier = modifier
    ) {
        // Home Screen - Camera and scanning
        composable(SearchPlantRoutes.HOME) {
            HomeScreen(
                onNavigateToHistory = {
                    navController.navigate(SearchPlantRoutes.HISTORY)
                },
                onNavigateToFavorites = {
                    navController.navigate(SearchPlantRoutes.FAVORITES)
                },
                onNavigateToSettings = {
                    navController.navigate(SearchPlantRoutes.SETTINGS)
                },
                onScanComplete = { itemId ->
                    navController.navigate(SearchPlantRoutes.scanResult(itemId))
                }
            )
        }
        
        // Scan Result Screen - Display identified plant details
        composable(SearchPlantRoutes.SCAN_RESULT) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getString("itemId")?.toLongOrNull() ?: 0L
            ScanResultScreen(
                itemId = itemId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToCamera = {
                    navController.popBackStack(SearchPlantRoutes.HOME, inclusive = false)
                }
            )
        }
        
        // History Screen - List of all scanned items
        composable(SearchPlantRoutes.HISTORY) {
            HistoryScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToResult = { itemId ->
                    navController.navigate(SearchPlantRoutes.scanResult(itemId))
                }
            )
        }
        
        // Favorites Screen - List of favorite items
        composable(SearchPlantRoutes.FAVORITES) {
            FavoritesScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToResult = { itemId ->
                    navController.navigate(SearchPlantRoutes.scanResult(itemId))
                }
            )
        }
        
        // Settings Screen - App settings and preferences
        composable(SearchPlantRoutes.SETTINGS) {
            SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

/**
 * Navigation actions interface for better organization
 */
interface NavigationActions {
    fun navigateToHome()
    fun navigateToHistory()
    fun navigateToFavorites()
    fun navigateToSettings()
    fun navigateToScanResult(itemId: Long)
    fun navigateBack()
}

/**
 * Implementation of navigation actions
 */
class SearchPlantNavigationActions(private val navController: NavHostController) : NavigationActions {
    
    override fun navigateToHome() {
        navController.navigate(SearchPlantRoutes.HOME) {
            popUpTo(SearchPlantRoutes.HOME) {
                inclusive = true
            }
        }
    }
    
    override fun navigateToHistory() {
        navController.navigate(SearchPlantRoutes.HISTORY)
    }
    
    override fun navigateToFavorites() {
        navController.navigate(SearchPlantRoutes.FAVORITES)
    }
    
    override fun navigateToSettings() {
        navController.navigate(SearchPlantRoutes.SETTINGS)
    }
    
    override fun navigateToScanResult(itemId: Long) {
        navController.navigate(SearchPlantRoutes.scanResult(itemId))
    }
    
    override fun navigateBack() {
        navController.popBackStack()
    }
}