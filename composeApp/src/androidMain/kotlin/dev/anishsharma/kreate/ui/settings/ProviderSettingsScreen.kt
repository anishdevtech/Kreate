package dev.anishsharma.kreate.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.anishsharma.kreate.settings.FeatureFlags
import kotlinx.coroutines.launch

@Composable
fun ProviderSettingsScreen(flags: FeatureFlags) {
    val scope = rememberCoroutineScope() 
    val yt by flags.enableYouTube.collectAsState(initial = true) 
    val sv by flags.enableSaavn.collectAsState(initial = true) 
    val fed by flags.enableFederated.collectAsState(initial = true) 

    Column(Modifier.padding(16.dp)) {
        RowToggle("Enable YouTube", yt) { v -> scope.launch { flags.setEnableYouTube(v) } } 
        RowToggle("Enable JioSaavn", sv) { v -> scope.launch { flags.setEnableSaavn(v) } } 
        RowToggle("Merge results (Both)", fed) { v -> scope.launch { flags.setEnableFederated(v) } } 
    }
}

@Composable
private fun RowToggle(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(text = title, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange, colors = SwitchDefaults.colors())
    }
}
