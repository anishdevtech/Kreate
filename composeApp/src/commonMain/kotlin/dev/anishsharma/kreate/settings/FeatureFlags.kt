package dev.anishsharma.kreate.settings

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

interface FeatureFlags {
    val enableYouTube: StateFlow<Boolean>
    val enableSaavn: StateFlow<Boolean>
    val enableFederated: StateFlow<Boolean>

    fun setEnableYouTube(value: Boolean)
    fun setEnableSaavn(value: Boolean)
    fun setEnableFederated(value: Boolean)
}

class InMemoryFeatureFlags(
    yt: Boolean = true,
    saavn: Boolean = true,
    federated: Boolean = true
) : FeatureFlags {
    private val _enableYouTube = MutableStateFlow(yt)
    private val _enableSaavn = MutableStateFlow(saavn)
    private val _enableFederated = MutableStateFlow(federated)

    override val enableYouTube: StateFlow<Boolean> = _enableYouTube
    override val enableSaavn: StateFlow<Boolean> = _enableSaavn
    override val enableFederated: StateFlow<Boolean> = _enableFederated

    override fun setEnableYouTube(value: Boolean) { _enableYouTube.value = value }
    override fun setEnableSaavn(value: Boolean) { _enableSaavn.value = value }
    override fun setEnableFederated(value: Boolean) { _enableFederated.value = value }
}
