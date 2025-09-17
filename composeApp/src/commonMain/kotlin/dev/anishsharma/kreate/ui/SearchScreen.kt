package dev.anishsharma.kreate.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
    val sel by selection.collectAsState(initial = ProviderSelection.BOTH)
    val list by results.collectAsState(initial = emptyList())
    var query by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        // Segmented toggle using TabRow (stable across Material3 versions)
        val tabs = listOf("Both", "YouTube", "JioSaavn")
        val selectedIndex = when (sel) {
            ProviderSelection.BOTH -> 0
            ProviderSelection.YOUTUBE_ONLY -> 1
            ProviderSelection.SAAVN_ONLY -> 2
        }
        TabRow(selectedTabIndex = selectedIndex) {
            Tab(selected = selectedIndex == 0, onClick = { onSelect(ProviderSelection.BOTH) }, text = { Text("Both") })
            Tab(selected = selectedIndex == 1, onClick = { onSelect(ProviderSelection.YOUTUBE_ONLY) }, text = { Text("YouTube") })
            Tab(selected = selectedIndex == 2, onClick = { onSelect(ProviderSelection.SAAVN_ONLY) }, text = { Text("JioSaavn") })
        }

        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("Search or paste YouTube URL/ID") },
            singleLine = true,
            modifier = Modifier.fillMaxSize(fraction = 1f)
        )
        Spacer(Modifier.height(8.dp))
        Button(onClick = { onSearch(query) }) { Text("Search") }

        Spacer(Modifier.height(16.dp))
        LazyColumn(Modifier.fillMaxSize()) {
            items(list) { t -> Text("[${t.provider}] ${t.title}") }
        }
    }
}
