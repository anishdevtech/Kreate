package dev.anishsharma.kreate.di

import dev.anishsharma.kreate.core.search.SearchOrchestrator
import dev.anishsharma.kreate.core.music.SearchController
import dev.anishsharma.kreate.providers.saavn.SaavnProvider
import dev.anishsharma.kreate.providers.youtube.YouTubeProvider
import dev.anishsharma.kreate.settings.AndroidPreferencesFeatureFlags
import dev.anishsharma.kreate.settings.FeatureFlags
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class SearchModule(private val flags: FeatureFlags) {
    
    private val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }
    
    fun createSearchOrchestrator(): SearchOrchestrator {
        val youTubeProvider = YouTubeProvider() // Your existing implementation
        val saavnProvider = SaavnProvider(httpClient)
        
        return SearchOrchestrator(
            youTubeProvider = youTubeProvider,
            saavnProvider = saavnProvider,
            enableYouTube = { flags.enableYouTube.value },
            enableSaavn = { flags.enableSaavn.value },
            enableFederated = { flags.enableFederated.value }
        )
    }
    
    fun createSearchController(scope: kotlinx.coroutines.CoroutineScope): SearchController {
        return SearchController(
            searchOrchestrator = createSearchOrchestrator(),
            flags = flags,
            scope = scope
        )
    }
}
