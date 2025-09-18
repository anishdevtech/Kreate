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

    // App-level scope for DI state
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // Cache provider flags as StateFlow so we can read .value synchronously via lambdas
    private val ytEnabled = flags.enableYouTube.stateIn(appScope, SharingStarted.Eagerly, true)
    private val svEnabled = flags.enableSaavn.stateIn(appScope, SharingStarted.Eagerly, true)
    private val fedEnabled = flags.enableFederated.stateIn(appScope, SharingStarted.Eagerly, true)

    // Ktor client for Saavn (shared provider in commonMain)
    private val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    // Build YouTube provider (platform impl) and adapt it to the shared MusicProvider interface
    private fun youtubeProvider(): MusicProvider {
        val gateway = InnertubeGatewayImpl(
            innertube = Innertube,
            localization = Localization()
        )
        val platform = PlatformYouTubeProvider(gateway = gateway)
        return object : MusicProvider {
            override suspend fun search(query: String, limit: Int) = platform.search(query, limit)
            override suspend fun getByUrl(url: String) = platform.getByUrl(url)
            override suspend fun getById(id: String) = platform.getById(id)
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
