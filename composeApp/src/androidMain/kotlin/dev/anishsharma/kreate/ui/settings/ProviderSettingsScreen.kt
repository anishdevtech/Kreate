package dev.anishsharma.kreate.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.anishsharma.kreate.settings.FeatureFlags

@Composable
fun ProviderSettingsScreen(flags: FeatureFlags) {
    val yt by flags.enableYouTube.collectAsState()
    val saavn by flags.enableSaavn.collectAsState()
    val fed by flags.enableFederated.collectAsState()

    Column(Modifier.padding(16.dp)) {
        RowToggle(
            title = "Enable YouTube",
            checked = yt,
            onCheckedChange = { flags.setEnableYouTube(it) }
        )
        RowToggle(
            title = "Enable JioSaavn",
            checked = saavn,
            onCheckedChange = { flags.setEnableSaavn(it) }
        )
        RowToggle(
            title = "Merge results (Both)",
            checked = fed,
            onCheckedChange = { flags.setEnableFederated(it) }
        )
    }
}

@Composable
private fun RowToggle(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    androidx.compose.foundation.layout.Row(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(text = title, modifier = Modifier.weight(1f))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors()
        )
    }
}
