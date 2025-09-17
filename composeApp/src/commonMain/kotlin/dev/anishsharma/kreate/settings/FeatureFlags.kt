package dev.anishsharma.kreate.settings

import kotlinx.coroutines.flow.Flow

interface FeatureFlags {
    val enableYouTube: Flow<Boolean>
    val enableSaavn: Flow<Boolean>
    val enableFederated: Flow<Boolean>

    suspend fun setEnableYouTube(value: Boolean)
    suspend fun setEnableSaavn(value: Boolean)
    suspend fun setEnableFederated(value: Boolean)
}
