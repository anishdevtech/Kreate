package dev.anishsharma.kreate.core.search

import dev.anishsharma.kreate.core.music.Track
import dev.anishsharma.kreate.core.music.ProviderSelection
import dev.anishsharma.kreate.core.music.ProviderType
import dev.anishsharma.kreate.providers.MusicProvider
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlin.math.max

interface SearchStrategy {
    suspend fun search(query: String, limit: Int = 20): List<Track>
}

class SingleProviderStrategy(
    private val provider: MusicProvider,
    private val providerType: ProviderType
) : SearchStrategy {
    override suspend fun search(query: String, limit: Int): List<Track> {
        return try {
            provider.search(query, limit).map { track ->
                track.copy(provider = providerType)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}

class FederatedSearchStrategy(
    private val providers: Map<ProviderType, MusicProvider>,
    private val rrfK: Int = 60
) : SearchStrategy {
    override suspend fun search(query: String, limit: Int): List<Track> = coroutineScope {
        val activeProviders = providers.entries.filter { (_, provider) -> provider != null }
        
        if (activeProviders.isEmpty()) return@coroutineScope emptyList()
        if (activeProviders.size == 1) {
            val (type, provider) = activeProviders.first()
            return@coroutineScope provider.search(query, limit).map { it.copy(provider = type) }
        }
        
        val results = activeProviders.map { (type, provider) ->
            async {
                try {
                    provider.search(query, limit).map { track ->
                        track.copy(provider = type)
                    }
                } catch (e: Exception) {
                    emptyList<Track>()
                }
            }
        }.awaitAll()
        
        // Apply Reciprocal Rank Fusion
        return@coroutineScope fuseResults(results, limit)
    }
    
    private fun fuseResults(results: List<List<Track>>, outLimit: Int): List<Track> {
        val scoreMap = mutableMapOf<String, Double>()
        val trackMap = mutableMapOf<String, Track>()
        
        results.forEach { rankedList ->
            rankedList.forEachIndexed { index, track ->
                val key = generateTrackKey(track)
                val score = 1.0 / (rrfK + (index + 1))
                
                scoreMap[key] = (scoreMap[key] ?: 0.0) + score
                trackMap.putIfAbsent(key, track)
            }
        }
        
        return scoreMap.entries
            .sortedByDescending { it.value }
            .mapNotNull { trackMap[it.key] }
            .take(outLimit)
    }
    
    private fun generateTrackKey(track: Track): String {
        val normalizedTitle = track.title.lowercase().replace(Regex("[^a-z0-9\\s]"), "")
        val normalizedArtists = track.artists.joinToString(",") { 
            it.lowercase().replace(Regex("[^a-z0-9\\s]"), "")
        }
        return "$normalizedTitle|$normalizedArtists|${track.durationSec ?: -1}"
    }
}

class SearchOrchestrator(
    private val youTubeProvider: MusicProvider?,
    private val saavnProvider: MusicProvider?,
    private val enableYouTube: () -> Boolean,
    private val enableSaavn: () -> Boolean,
    private val enableFederated: () -> Boolean
) {
    suspend fun search(selection: ProviderSelection, query: String, limit: Int = 20): List<Track> {
        // Handle URL detection first
        if (isValidUrl(query)) {
            return handleUrlSearch(query, selection)
        }
        
        // Handle text search
        return when (selection) {
            ProviderSelection.YOUTUBE_ONLY -> {
                if (enableYouTube() && youTubeProvider != null) {
                    SingleProviderStrategy(youTubeProvider, ProviderType.YOUTUBE).search(query, limit)
                } else emptyList()
            }
            
            ProviderSelection.SAAVN_ONLY -> {
                if (enableSaavn() && saavnProvider != null) {
                    SingleProviderStrategy(saavnProvider, ProviderType.SAAVN).search(query, limit)
                } else emptyList()
            }
            
            ProviderSelection.BOTH -> {
                if (!enableFederated()) {
                    // Fall back to single provider if federation is disabled
                    return when {
                        enableYouTube() && youTubeProvider != null -> 
                            SingleProviderStrategy(youTubeProvider, ProviderType.YOUTUBE).search(query, limit)
                        enableSaavn() && saavnProvider != null -> 
                            SingleProviderStrategy(saavnProvider, ProviderType.SAAVN).search(query, limit)
                        else -> emptyList()
                    }
                }
                
                val activeProviders = buildMap {
                    if (enableYouTube() && youTubeProvider != null) {
                        put(ProviderType.YOUTUBE, youTubeProvider)
                    }
                    if (enableSaavn() && saavnProvider != null) {
                        put(ProviderType.SAAVN, saavnProvider)
                    }
                }
                
                if (activeProviders.isEmpty()) {
                    emptyList()
                } else {
                    FederatedSearchStrategy(activeProviders).search(query, limit)
                }
            }
        }
    }
    
    private suspend fun handleUrlSearch(url: String, selection: ProviderSelection): List<Track> {
        return when {
            isJioSaavnUrl(url) -> {
                if ((selection == ProviderSelection.SAAVN_ONLY || selection == ProviderSelection.BOTH) 
                    && enableSaavn() && saavnProvider != null) {
                    try {
                        val track = saavnProvider.getByUrl(url)
                        if (track != null) listOf(track.copy(provider = ProviderType.SAAVN)) else emptyList()
                    } catch (e: Exception) {
                        emptyList()
                    }
                } else emptyList()
            }
            
            isYouTubeUrl(url) -> {
                if ((selection == ProviderSelection.YOUTUBE_ONLY || selection == ProviderSelection.BOTH) 
                    && enableYouTube() && youTubeProvider != null) {
                    try {
                        val track = youTubeProvider.getByUrl(url)
                        if (track != null) listOf(track.copy(provider = ProviderType.YOUTUBE)) else emptyList()
                    } catch (e: Exception) {
                        emptyList()
                    }
                } else emptyList()
            }
            
            else -> emptyList()
        }
    }
    
    private fun isValidUrl(query: String): Boolean {
        return isJioSaavnUrl(query) || isYouTubeUrl(query)
    }
    
    private fun isJioSaavnUrl(url: String): Boolean {
        return url.contains("jiosaavn.com", ignoreCase = true) ||
               url.contains("saavn.com", ignoreCase = true)
    }
    
    private fun isYouTubeUrl(url: String): Boolean {
        return url.contains("youtube.com", ignoreCase = true) ||
               url.contains("youtu.be", ignoreCase = true) ||
               url.contains("music.youtube.com", ignoreCase = true)
    }
}
