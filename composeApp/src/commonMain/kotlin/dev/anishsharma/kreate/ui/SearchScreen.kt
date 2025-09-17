package dev.anishsharma.kreate.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.anishsharma.kreate.core.music.ProviderSelection
import dev.anishsharma.kreate.core.music.Track
import kotlinx.coroutines.flow.StateFlow

@Composable
fun SearchScreen(
    selection: StateFlow<ProviderSelection>,
    results: StateFlow<List<Track>>,
    onSelect: (ProviderSelection) -> Unit,
    onSearch: (String) -> Unit
) {
    val selectionState by selection.collectAsState(initial = ProviderSelection.BOTH)
    val resultsState by results.collectAsState(initial = emptyList())
    var query by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        SingleChoiceSegmentedButtonRow {
            SegmentedButton(
                selected = selectionState == ProviderSelection.BOTH,
                onClick = { onSelect(ProviderSelection.BOTH) },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3)
            ) { Text("Both") }
            SegmentedButton(
                selected = selectionState == ProviderSelection.YOUTUBE_ONLY,
                onClick = { onSelect(ProviderSelection.YOUTUBE_ONLY) },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3)
            ) { Text("YouTube") }
            SegmentedButton(
                selected = selectionState == ProviderSelection.SAAVN_ONLY,
                onClick = { onSelect(ProviderSelection.SAAVN_ONLY) },
                shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3)
            ) { Text("JioSaavn") }
        }
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("Search or paste YouTube URL/ID") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        Button(onClick = { onSearch(query) }) { Text("Search") }
        Spacer(Modifier.height(16.dp))
        LazyColumn(Modifier.fillMaxSize()) {
            items(resultsState) { t -> TrackRow(t) }
        }
    }
}

@Composable
private fun TrackRow(t: Track) {
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(text = "[${t.provider}] ${t.title}")
    }
}
