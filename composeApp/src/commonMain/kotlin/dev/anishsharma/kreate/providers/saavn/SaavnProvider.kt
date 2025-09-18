package dev.anishsharma.kreate.providers.saavn

import dev.anishsharma.kreate.core.music.Track
import dev.anishsharma.kreate.core.music.ProviderType
import dev.anishsharma.kreate.providers.MusicProvider
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Duration.Companion.seconds

class SaavnProvider(
    private val httpClient: HttpClient,
    private val baseUrl: String = "https://saavn.dev"
) : MusicProvider {
    
    override suspend fun search(query: String, limit: Int): List<Track> {
        return try {
            val response = httpClient.get("$baseUrl/api/search/songs") {
                parameter("query", query)
                parameter("limit", limit)
            }.body<SaavnSearchResponse>()
            
            response.data.results.map { it.toTrack() }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    override suspend fun getByUrl(url: String): Track? {
        return try {
            // Extract song ID from JioSaavn URL
            val songId = extractSongIdFromUrl(url) ?: return null
            getById(songId)
        } catch (e: Exception) {
            null
        }
    }
    
    override suspend fun getById(id: String): Track? {
        return try {
            val response = httpClient.get("$baseUrl/api/songs/$id")
                .body<SaavnSongResponse>()
            
            response.data.firstOrNull()?.toTrack()
        } catch (e: Exception) {
            null
        }
    }
    
    private fun extractSongIdFromUrl(url: String): String? {
        // JioSaavn URL patterns:
        // https://www.jiosaavn.com/song/[song-name]/[id]
        // https://jiosaavn.com/song/[song-name]/[id]
        val regex = """jiosaavn\.com/song/[^/]+/([^/?]+)""".toRegex(RegexOption.IGNORE_CASE)
        return regex.find(url)?.groupValues?.get(1)
    }
}

@Serializable
data class SaavnSearchResponse(
    val status: String,
    val message: String? = null,
    val data: SaavnSearchData
)

@Serializable
data class SaavnSearchData(
    val total: Int,
    val start: Int,
    val results: List<SaavnSong>
)

@Serializable
data class SaavnSongResponse(
    val status: String,
    val message: String? = null,
    val data: List<SaavnSong>
)

@Serializable
data class SaavnSong(
    val id: String,
    val name: String,
    val artists: SaavnArtists,
    val duration: Int? = null,
    val image: List<SaavnImage> = emptyList(),
    @SerialName("download_url") val downloadUrl: List<SaavnDownloadUrl> = emptyList(),
    @SerialName("perma_url") val permaUrl: String? = null
) {
    fun toTrack(): Track {
        return Track(
            provider = ProviderType.SAAVN,
            providerId = id,
            title = name,
            artists = artists.all.map { it.name },
            durationSec = duration,
            artwork = image.lastOrNull()?.url, // Get highest quality image
            streamUrl = downloadUrl.lastOrNull()?.url // Get highest quality stream
        )
    }
}

@Serializable
data class SaavnArtists(
    val all: List<SaavnArtist>
)

@Serializable
data class SaavnArtist(
    val id: String,
    val name: String,
    val role: String,
    val type: String,
    val image: List<SaavnImage> = emptyList()
)

@Serializable
data class SaavnImage(
    val quality: String,
    val url: String
)

@Serializable
data class SaavnDownloadUrl(
    val quality: String,
    val url: String
)
