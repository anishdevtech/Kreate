package dev.anishsharma.kreate.providers.saavn

import dev.anishsharma.kreate.core.music.MusicProvider
import dev.anishsharma.kreate.core.music.ProviderType
import dev.anishsharma.kreate.core.music.Track
import dev.anishsharma.kreate.extentions.innersaavn.SaavnApi
import dev.anishsharma.kreate.extentions.innersaavn.SaavnRepository

class SaavnProvider(
    baseUrl: String,
    api: SaavnApi? = null
) : MusicProvider {

    private val repo = SaavnRepository(api ?: SaavnApi(baseUrl))

    override suspend fun search(query: String, limit: Int): List<Track> =
        repo.searchSongs(query, 1)
            .take(limit)
            .map { s ->
                Track(
                    provider = ProviderType.SAAVN,
                    providerId = s.id,
                    title = s.title
                )
            }

    override suspend fun details(id: String): Track? =
        runCatching { repo.songDetails(id) }.getOrNull()?.let { d ->
            Track(
                provider = ProviderType.SAAVN,
                providerId = d.id,
                title = d.title
            )
        }
}
