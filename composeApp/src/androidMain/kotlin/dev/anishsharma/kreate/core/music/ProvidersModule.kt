package dev.anishsharma.kreate.core.music

import dev.anishsharma.kreate.providers.saavn.SaavnProvider
import dev.anishsharma.kreate.providers.youtube.InnertubeGatewayImpl
import dev.anishsharma.kreate.providers.youtube.YouTubeProvider
import dev.anishsharma.kreate.settings.FeatureFlags
import me.knighthat.innertube.Innertube
import me.knighthat.innertube.request.Localization

object ProvidersModule {

    fun provideSearchOrchestrator(
        saavnBaseUrl: String,
        innertube: Innertube,
        localization: Localization,
        flags: FeatureFlags
    ): SearchOrchestrator {
        val yt = if (flags.enableYouTube.value)
            YouTubeProvider(InnertubeGatewayImpl(innertube, localization)) else null

        val saavn = if (flags.enableSaavn.value)
            SaavnProvider(baseUrl = saavnBaseUrl) else null

        return SearchOrchestrator(
            yt = yt,
            saavn = saavn
        ) { active -> FederatedSearchService(active) }
    }
}
