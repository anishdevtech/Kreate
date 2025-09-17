package dev.anishsharma.kreate.core.music

import dev.anishsharma.kreate.settings.FeatureFlags
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.math.max

class SearchController(
    private val orchestrator: SearchOrchestrator,
    private val flags: FeatureFlags,
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

    fun setQuery(q: String) { _query.value = q }

    fun search(q: String = _query.value, limit: Int = 20) {
        val sel = coerceSelection(_selection.value)
        scope.launch {
            val list = orchestrator.search(sel, q, limit)
            _results.value = list
        }
    }

    private fun coerceSelection(sel: ProviderSelection): ProviderSelection {
        val yt = flags.enableYouTube.value
        val sv = flags.enableSaavn.value
        val fed = flags.enableFederated.value
        return when (sel) {
            ProviderSelection.BOTH ->
                if (fed && yt && sv) ProviderSelection.BOTH
                else if (yt) ProviderSelection.YOUTUBE_ONLY
                else if (sv) ProviderSelection.SAAVN_ONLY
                else ProviderSelection.BOTH
            ProviderSelection.YOUTUBE_ONLY ->
                if (yt) ProviderSelection.YOUTUBE_ONLY else if (sv) ProviderSelection.SAAVN_ONLY else sel
            ProviderSelection.SAAVN_ONLY ->
                if (sv) ProviderSelection.SAAVN_ONLY else if (yt) ProviderSelection.YOUTUBE_ONLY else sel
        }
    }
}
