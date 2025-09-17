package dev.anishsharma.kreate.settings

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AndroidPreferencesFeatureFlags(
    context: Context,
    prefsName: String = "provider_flags"
) : FeatureFlags {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)

    private val _enableYouTube = MutableStateFlow(prefs.getBoolean(KEY_YT, true))
    private val _enableSaavn = MutableStateFlow(prefs.getBoolean(KEY_SAAVN, true))
    private val _enableFederated = MutableStateFlow(prefs.getBoolean(KEY_FED, true))

    override val enableYouTube: StateFlow<Boolean> = _enableYouTube
    override val enableSaavn: StateFlow<Boolean> = _enableSaavn
    override val enableFederated: StateFlow<Boolean> = _enableFederated

    override fun setEnableYouTube(value: Boolean) {
        _enableYouTube.value = value
        prefs.edit().putBoolean(KEY_YT, value).apply()
    }

    override fun setEnableSaavn(value: Boolean) {
        _enableSaavn.value = value
        prefs.edit().putBoolean(KEY_SAAVN, value).apply()
    }

    override fun setEnableFederated(value: Boolean) {
        _enableFederated.value = value
        prefs.edit().putBoolean(KEY_FED, value).apply()
    }

    private companion object {
        const val KEY_YT = "enable_youtube"
        const val KEY_SAAVN = "enable_saavn"
        const val KEY_FED = "enable_federated"
    }
}
