package dev.anishsharma.kreate.providers.youtube

import me.knighthat.innertube.Innertube
import me.knighthat.innertube.request.Localization

class InnertubeGatewayImpl(
    private val tube: Innertube,
    private val localization: Localization
) : YouTubeGateway {

    override suspend fun searchSongs(query: String, limit: Int): List<YTItem> {
        // If query is URL/ID, resolve it
        parseVideoId(query)?.let { vid -> details(vid)?.let { return listOf(it) } }
        // No generic text search exposed by public Innertube API; return empty for now
        return emptyList()
    }

    override suspend fun details(id: String): YTItem? {
        val res = tube.songBasicInfo(id, localization, params = null)
        return if (res.isSuccess) {
            // Minimal surface; map more fields if you consume InnertubeSong later
            YTItem(id = id, title = id)
        } else null
    }

    private fun parseVideoId(input: String): String? {
        val trimmed = input.trim()
        val idRegex = Regex("^[A-Za-z0-9_-]{11}\$")
        if (idRegex.matches(trimmed)) return trimmed

        val short = Regex("https?://(?:www\\.)?youtu\\.be/([A-Za-z0-9_-]{11})")
        short.find(trimmed)?.groupValues?.getOrNull(1)?.let { return it }

        val watch = Regex("https?://(?:www\\.)?youtube\\.com/watch\\?[^\\s]*v=([A-Za-z0-9_-]{11})")
        watch.find(trimmed)?.groupValues?.getOrNull(1)?.let { return it }

        val musicWatch = Regex("https?://(?:www\\.)?music\\.youtube\\.com/watch\\?[^\\s]*v=([A-Za-z0-9_-]{11})")
        musicWatch.find(trimmed)?.groupValues?.getOrNull(1)?.let { return it }

        return null
    }
}
