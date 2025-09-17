package dev.anishsharma.kreate.core.music

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlin.math.max

enum class ProviderType { YOUTUBE, SAAVN }
enum class ProviderSelection { YOUTUBE_ONLY, SAAVN_ONLY, BOTH }

data class Track(
    val provider: ProviderType,
    val providerId: String,
    val title: String,
    val artists: List<String> = emptyList(),
    val durationSec: Int? = null,
    val artwork: String? = null,
    val streamUrl: String? = null
)

interface MusicProvider {
    suspend fun search(query: String, limit: Int = 20): List<Track>
    suspend fun details(id: String): Track?
}

class FederatedSearchService(
    private val providers: List<MusicProvider>,
    private val rrfK: Int = 60
) {
    suspend fun searchAll(query: String, limitPerProvider: Int = 20, outLimit: Int = 30): List<Track> = coroutineScope {
        val lists = providers.map { p ->
            async { runCatching { p.search(query, limitPerProvider) }.getOrElse { emptyList() } }
        }.awaitAll()

        fun key(t: Track): String {
            val artists = t.artists.joinToString(",").lowercase()
            return "${t.title.lowercase()}|$artists|${t.durationSec ?: -1}"
        }

        val score = HashMap<String, Double>(outLimit * 2)
        val pick = HashMap<String, Track>(outLimit * 2)

        lists.forEach { ranked ->
            ranked.forEachIndexed { idx, t ->
                val add = 1.0 / (rrfK + (idx + 1))
                val k = key(t)
                score[k] = (score[k] ?: 0.0) + add
                pick.putIfAbsent(k, t)
            }
        }

        score.entries.asSequence()
            .sortedByDescending { it.value }
            .mapNotNull { pick[it.key] }
            .take(outLimit)
            .toList()
    }
}

class SearchOrchestrator(
    private val yt: MusicProvider?,
    private val saavn: MusicProvider?,
    private val federatedBuilder: (List<MusicProvider>) -> FederatedSearchService
) {
    suspend fun search(selection: ProviderSelection, query: String, limit: Int = 20): List<Track> {
        return when (selection) {
            ProviderSelection.YOUTUBE_ONLY -> yt?.search(query, limit).orEmpty()
            ProviderSelection.SAAVN_ONLY -> saavn?.search(query, limit).orEmpty()
            ProviderSelection.BOTH -> {
                val active = listOfNotNull(yt, saavn)
                if (active.size <= 1) return active.firstOrNull()?.search(query, limit).orEmpty()
                federatedBuilder(active).searchAll(query, limitPerProvider = limit, outLimit = max(20, limit))
            }
        }
    }
}
