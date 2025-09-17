package dev.anishsharma.kreate.settings

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map

private val Context.flagsDataStore by preferencesDataStore(name = "provider_flags")

object FlagKeys {
    val ENABLE_YT = booleanPreferencesKey("enable_youtube")
    val ENABLE_SAAVN = booleanPreferencesKey("enable_saavn")
    val ENABLE_FED = booleanPreferencesKey("enable_federated")
}

interface FeatureFlags {
    val enableYouTube: StateFlow<Boolean>
    val enableSaavn: StateFlow<Boolean>
    val enableFederated: StateFlow<Boolean>
    fun setEnableYouTube(value: Boolean)
    fun setEnableSaavn(value: Boolean)
    fun setEnableFederated(value: Boolean)
}

class FeatureFlagsStore(context: Context) : FeatureFlags {
    private val ds = context.flagsDataStore

    private val _yt = MutableStateFlow(true)
    private val _sv = MutableStateFlow(true)
    private val _fed = MutableStateFlow(true)

    override val enableYouTube: StateFlow<Boolean> = _yt
    override val enableSaavn: StateFlow<Boolean> = _sv
    override val enableFederated: StateFlow<Boolean> = _fed

    init {
        // Load initial values
        kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            ds.data.map { it[FlagKeys.ENABLE_YT] ?: true }.collect { _yt.value = it }
        }
        kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            ds.data.map { it[FlagKeys.ENABLE_SAAVN] ?: true }.collect { _sv.value = it }
        }
        kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            ds.data.map { it[FlagKeys.ENABLE_FED] ?: true }.collect { _fed.value = it }
        }
    }

    override fun setEnableYouTube(value: Boolean) {
        _yt.value = value
        kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.IO) { ds.edit { it[FlagKeys.ENABLE_YT] = value } }
    }
    override fun setEnableSaavn(value: Boolean) {
        _sv.value = value
        kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.IO) { ds.edit { it[FlagKeys.ENABLE_SAAVN] = value } }
    }
    override fun setEnableFederated(value: Boolean) {
        _fed.value = value
        kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.IO) { ds.edit { it[FlagKeys.ENABLE_FED] = value } }
    }
}
