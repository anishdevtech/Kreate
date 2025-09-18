package dev.anishsharma.kreate.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.anishsharma.kreate.settings.FeatureFlags
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderSettingsDestination(
    flags: FeatureFlags,
    onBackPressed: () -> Unit
) {
    val scrollState = rememberScrollState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Provider Settings") },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Music Providers",
                fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            ProviderToggleCard(flags = flags)
            
            Text(
                text = "Search Settings",
                fontSize = 18.sp,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )
            
            SearchSettingsCard(flags = flags)
        }
    }
}

@Composable
private fun ProviderToggleCard(flags: FeatureFlags) {
    val scope = rememberCoroutineScope()
    val ytEnabled by flags.enableYouTube.collectAsState(initial = true)
    val saavnEnabled by flags.enableSaavn.collectAsState(initial = true)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ProviderToggleRow(
                title = "YouTube Music",
                description = "Search and play music from YouTube",
                enabled = ytEnabled,
                onToggle = { enabled ->
                    scope.launch { flags.setEnableYouTube(enabled) }
                }
            )
            
            Divider()
            
            ProviderToggleRow(
                title = "JioSaavn",
                description = "Search and play music from JioSaavn",
                enabled = saavnEnabled,
                onToggle = { enabled ->
                    scope.launch { flags.setEnableSaavn(enabled) }
                }
            )
        }
    }
}

@Composable
private fun SearchSettingsCard(flags: FeatureFlags) {
    val scope = rememberCoroutineScope()
    val federatedEnabled by flags.enableFederated.collectAsState(initial = true)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            ProviderToggleRow(
                title = "Merge Search Results",
                description = "Combine results from multiple providers when using 'Both' mode",
                enabled = federatedEnabled,
                onToggle = { enabled ->
                    scope.launch { flags.setEnableFederated(enabled) }
                }
            )
        }
    }
}

@Composable
private fun ProviderToggleRow(
    title: String,
    description: String,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Switch(
            checked = enabled,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors()
        )
    }
}
