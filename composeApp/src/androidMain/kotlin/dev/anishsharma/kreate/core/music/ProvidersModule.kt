package dev.anishsharma.kreate.core.music

import android.content.Context
import dev.anishsharma.kreate.providers.saavn.SaavnProvider
import dev.anishsharma.kreate.providers.youtube.InnertubeGatewayImpl
import dev.anishsharma.kreate.providers.youtube.YouTubeProvider
import dev.anishsharma.kreate.settings.AndroidPreferencesFeatureFlags
import dev.anishsharma.kreate.settings.FeatureFlags
import me.knighthat.innertube.Innertube
import me.knighthat.innertube.request.Localization

class ProvidersModule(context: Context) {
    val flags: FeatureFlags = AndroidPreferencesFeatureFlags(context)

    fun orchestrator(
        saavnBaseUrl: String,
        innertube: Innertube,
        localization: Localization
    ): SearchOrchestrator {
        val yt = YouTubeProvider(InnertubeGatewayImpl(innertube, localization))
        val saavn = SaavnProvider(baseUrl = saavnBaseUrl)
        return SearchOrchestrator(yt = yt, saavn = saavn) { active -> FederatedSearchService(active) }
    }
}
