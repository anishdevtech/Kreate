package dev.anishsharma.kreate.di

import dev.anishsharma.kreate.core.music.SearchController
import dev.anishsharma.kreate.core.search.SearchOrchestrator
import dev.anishsharma.kreate.providers.MusicProvider
import dev.anishsharma.kreate.providers.saavn.SaavnProvider
import dev.anishsharma.kreate.settings.FeatureFlags
import dev.anishsharma.kreate.providers.youtube.YouTubeProvider as PlatformYouTubeProvider
import dev.anishsharma.kreate.providers.youtube.InnertubeGatewayImpl
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.json.Json
import me.knighthat.innertube.Innertube
import me.knighthat.innertube.request.Localization

class SearchModule(private val flags: FeatureFlags) {

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // Cache provider flags as StateFlow so .value can be read in DI lambdas
    private val ytEnabled = flags.enableYouTube.stateIn(appScope, SharingStarted.Eagerly, true)
    private val svEnabled = flags.enableSaavn.stateIn(appScope, SharingStarted.Eagerly, true)
    private val fedEnabled = flags.enableFederated.stateIn(appScope, SharingStarted.Eagerly, true)

    // Shared Ktor client for Saavn
    private val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    // Build platform YouTube provider and adapt to the shared MusicProvider interface
    private fun youtubeProvider(): MusicProvider {
        // Use positional args for current gateway signature and supply localization codes
        val gateway = InnertubeGatewayImpl(
            Innertube,
            Localization(languageCode = "en", regionCode = "IN")
        )
        val platform = PlatformYouTubeProvider(gateway)
        return object : MusicProvider {
            override suspend fun search(query: String, limit: Int) =
                platform.search(query, limit)
            // URL and ID resolution not exposed by platform provider; return null to let orchestrator fall back
            override suspend fun getByUrl(url: String) = null
            override suspend fun getById(id: String) = null
        }
    }

    private fun saavnProvider(): MusicProvider = SaavnProvider(httpClient)

    fun createSearchOrchestrator(): SearchOrchestrator {
        val yt = youtubeProvider()
        val sv = saavnProvider()
        return SearchOrchestrator(
            youTubeProvider = yt,
            saavnProvider = sv,
            enableYouTube = { ytEnabled.value },
            enableSaavn = { svEnabled.value },
            enableFederated = { fedEnabled.value }
        )
    }

    fun createSearchController(scope: CoroutineScope): SearchController {
        return SearchController(
            searchOrchestrator = createSearchOrchestrator(),
            flags = flags,
            scope = scope
        )
    }
}
