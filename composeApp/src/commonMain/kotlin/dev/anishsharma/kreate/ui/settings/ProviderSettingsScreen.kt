package dev.anishsharma.kreate.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.Flow

@Composable
fun ProviderSettingsScreen(
    enableYouTube: Flow<Boolean>,
    enableSaavn: Flow<Boolean>,
    enableFederated: Flow<Boolean>,
    onSetYouTube: (Boolean) -> Unit,
    onSetSaavn: (Boolean) -> Unit,
    onSetFederated: (Boolean) -> Unit
) {
    val yt by enableYouTube.collectAsState(initial = true)
    val sv by enableSaavn.collectAsState(initial = true)
    val fed by enableFederated.collectAsState(initial = true)

    Column(Modifier.padding(16.dp)) {
        RowToggle("Enable YouTube", yt, onSetYouTube)
        RowToggle("Enable JioSaavn", sv, onSetSaavn)
        RowToggle("Merge results (Both)", fed, onSetFederated)
    }
}

@Composable
private fun RowToggle(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(text = title, modifier = Modifier.weight(1f))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors()
        )
    }
}
