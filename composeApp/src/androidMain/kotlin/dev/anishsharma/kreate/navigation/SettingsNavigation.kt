package dev.anishsharma.kreate.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import dev.anishsharma.kreate.settings.FeatureFlags
import dev.anishsharma.kreate.ui.settings.ProviderSettingsDestination

fun NavGraphBuilder.addProviderSettings(
    navController: NavHostController,
    flags: FeatureFlags
) {
    composable("settings/providers") {
        ProviderSettingsDestination(
            flags = flags,
            onBackPressed = { navController.popBackStack() }
        )
    }
}

// Usage in your main navigation setup
fun NavGraphBuilder.settingsNavigation(
    navController: NavHostController,
    flags: FeatureFlags
) {
    // Add to your existing settings menu
    composable("settings") {
        SettingsScreen(
            onProviderSettingsClick = { 
                navController.navigate("settings/providers")
            },
            onBackPressed = { navController.popBackStack() }
        )
    }
    
    addProviderSettings(navController, flags)
}
