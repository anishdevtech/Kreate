package dev.anishsharma.kreate.core.music

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
