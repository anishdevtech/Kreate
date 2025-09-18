package dev.anishsharma.kreate.core.music

import dev.anishsharma.kreate.settings.FeatureFlags
import dev.anishsharma.kreate.core.search.SearchOrchestrator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SearchController(
    private val searchOrchestrator: SearchOrchestrator,
    private val flags: FeatureFlags,
    private val scope: CoroutineScope
) {
    private val _selection = MutableStateFlow(ProviderSelection.BOTH)
    val selection: StateFlow<ProviderSelection> = _selection.asStateFlow()

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _results = MutableStateFlow<List<Track>>(emptyList())
    val results: StateFlow<List<Track>> = _results.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun setSelection(selection: ProviderSelection) {
        _selection.value = selection
        // Re-run search if query exists
        val currentQuery = _query.value
        if (currentQuery.isNotBlank()) {
            search(currentQuery)
        }
    }

    fun setQuery(query: String) {
        _query.value = query
    }

    fun search(query: String = _query.value, limit: Int = 20) {
        if (query.isBlank()) {
            _results.value = emptyList()
            return
        }
        
        _query.value = query
        _isLoading.value = true
        _errorMessage.value = null
        
        scope.launch {
            try {
                val searchResults = searchOrchestrator.search(
                    selection = _selection.value,
                    query = query,
                    limit = limit
                )
                _results.value = searchResults
                
                if (searchResults.isEmpty()) {
                    _errorMessage.value = "No results found for \"$query\""
                }
            } catch (e: Exception) {
                _errorMessage.value = "Search failed: ${e.message}"
                _results.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun clearError() {
        _errorMessage.value = null
    }
}
