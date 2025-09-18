package dev.anishsharma.kreate.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.anishsharma.kreate.settings.FeatureFlags
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderSettingsScreen(flags: FeatureFlags, onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    val yt by flags.enableYouTube.collectAsState(initial = true)
    val sv by flags.enableSaavn.collectAsState(initial = true)
    val fed by flags.enableFederated.collectAsState(initial = true)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Provider Settings") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("Back") }
                }
            )
        }
    ) { pad ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Music Providers", style = MaterialTheme.typography.titleMedium)
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("YouTube Music", style = MaterialTheme.typography.titleMedium)
                            Text("Search and play from YouTube", style = MaterialTheme.typography.bodySmall)
                        }
                        Switch(checked = yt, onCheckedChange = { v -> scope.launch { flags.setEnableYouTube(v) } })
                    }
                    Divider()
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("JioSaavn", style = MaterialTheme.typography.titleMedium)
                            Text("Search and play from JioSaavn", style = MaterialTheme.typography.bodySmall)
                        }
                        Switch(checked = sv, onCheckedChange = { v -> scope.launch { flags.setEnableSaavn(v) } })
                    }
                }
            }
            Text("Search Settings", style = MaterialTheme.typography.titleMedium)
            Card(Modifier.fillMaxWidth()) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Merge Results (Both)", style = MaterialTheme.typography.titleMedium)
                        Text("Combine provider results in Both mode", style = MaterialTheme.typography.bodySmall)
                    }
                    Switch(checked = fed, onCheckedChange = { v -> scope.launch { flags.setEnableFederated(v) } })
                }
            }
        }
    }
}
