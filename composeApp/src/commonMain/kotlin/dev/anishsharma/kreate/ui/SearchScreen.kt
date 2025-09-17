package dev.anishsharma.kreate.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.anishsharma.kreate.core.music.ProviderSelection
import dev.anishsharma.kreate.core.music.SearchController
import dev.anishsharma.kreate.core.music.Track

@Composable
fun SearchScreen(controller: SearchController) {
    val selection by controller.selection.collectAsState()
    val results by controller.results.collectAsState()
    var query by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        SegmentedProviderToggle(selection) { controller.setSelection(it) }
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("Search or paste YouTube URL/ID") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        Button(onClick = { controller.setQuery(query); controller.search(query) }) { Text("Search") }
        Spacer(Modifier.height(16.dp))
        LazyColumn(Modifier.fillMaxSize()) {
            items(results) { t -> TrackRow(t) }
        }
    }
}

@Composable
private fun SegmentedProviderToggle(
    selection: ProviderSelection,
    onSelect: (ProviderSelection) -> Unit
) {
    SingleChoiceSegmentedButtonRow {
        SegmentedButton(
            checked = selection == ProviderSelection.BOTH,
            onCheckedChange = { onSelect(ProviderSelection.BOTH) },
            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3)
        ) { Text("Both") }
        SegmentedButton(
            checked = selection == ProviderSelection.YOUTUBE_ONLY,
            onCheckedChange = { onSelect(ProviderSelection.YOUTUBE_ONLY) },
            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3)
        ) { Text("YouTube") }
        SegmentedButton(
            checked = selection == ProviderSelection.SAAVN_ONLY,
            onCheckedChange = { onSelect(ProviderSelection.SAAVN_ONLY) },
            shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3)
        ) { Text("JioSaavn") }
    }
}

@Composable
private fun TrackRow(t: Track) {
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(text = "[${t.provider}] ${t.title}")
    }
}
