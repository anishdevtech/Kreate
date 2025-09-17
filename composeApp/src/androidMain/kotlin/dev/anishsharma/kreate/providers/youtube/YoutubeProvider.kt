package dev.anishsharma.kreate.providers.youtube

import dev.anishsharma.kreate.core.music.MusicProvider
import dev.anishsharma.kreate.core.music.ProviderType
import dev.anishsharma.kreate.core.music.Track

class YouTubeProvider(
    private val gateway: YouTubeGateway
) : MusicProvider {

    override suspend fun search(query: String, limit: Int): List<Track> {
        val items = gateway.searchSongs(query, limit = limit)
        return items.take(limit).map { it.toTrack() }
    }

    override suspend fun details(id: String): Track? {
        return gateway.details(id)?.toTrack()
    }

    private fun YTItem.toTrack(): Track = Track(
        provider = ProviderType.YOUTUBE,
        providerId = id,
        title = title,
        artists = artists ?: author?.split(',')?.map { it.trim() } ?: emptyList(),
        durationSec = durationSec ?: parseDurationToSeconds(duration),
        artwork = artwork,
        streamUrl = streamUrl
    )

    private fun parseDurationToSeconds(text: String?): Int? {
        if (text.isNullOrBlank()) return null
        val parts = text.trim().split(":").mapNotNull { it.toIntOrNull() }
        return when (parts.size) {
            2 -> parts[0] * 60 + parts[1]
            3 -> parts[0] * 3600 + parts[1] * 60 + parts[2]
            else -> null
        }
    }
}

interface YouTubeGateway {
    suspend fun searchSongs(query: String, limit: Int = 20): List<YTItem>
    suspend fun details(id: String): YTItem?
}

data class YTItem(
    val id: String,
    val title: String,
    val author: String? = null,
    val artists: List<String>? = null,
    val duration: String? = null,
    val durationSec: Int? = null,
    val artwork: String? = null,
    val streamUrl: String? = null
)
