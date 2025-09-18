package dev.anishsharma.kreate.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import dev.anishsharma.kreate.settings.FeatureFlags
import dev.anishsharma.kreate.ui.settings.ProviderSettingsScreen

fun NavGraphBuilder.providerSettingsGraph(
    navController: NavHostController,
    flags: FeatureFlags
) {
    composable("settings/providers") {
        ProviderSettingsScreen(flags = flags, onBack = { navController.popBackStack() })
    }
}
