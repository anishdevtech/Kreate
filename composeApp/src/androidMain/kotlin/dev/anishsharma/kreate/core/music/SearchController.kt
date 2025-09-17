package dev.anishsharma.kreate.core.music

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SearchController(
    private val orchestrator: SearchOrchestrator,
    private val scope: CoroutineScope
) {
    private val _selection = MutableStateFlow(ProviderSelection.BOTH)
    val selection: StateFlow<ProviderSelection> = _selection

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    private val _results = MutableStateFlow<List<Track>>(emptyList())
    val results: StateFlow<List<Track>> = _results

    fun setSelection(sel: ProviderSelection) {
        _selection.value = sel
        search(_query.value)
    }

    fun setQuery(q: String) {
        _query.value = q
    }

    fun search(q: String = _query.value, limit: Int = 20) {
        val sel = _selection.value
        scope.launch {
            val list = orchestrator.search(sel, q, limit)
            _results.value = list
        }
    }
}
