package dev.anishsharma.kreate.settings

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.flagsDataStore by preferencesDataStore(name = "provider_flags")

private object Keys {
    val ENABLE_YT = booleanPreferencesKey("enable_youtube")
    val ENABLE_SAAVN = booleanPreferencesKey("enable_saavn")
    val ENABLE_FED = booleanPreferencesKey("enable_federated")
}

class AndroidFeatureFlagsStore(private val context: Context) : FeatureFlags {
    private val ds = context.flagsDataStore

    override val enableYouTube: Flow<Boolean> =
        ds.data.catch { if (it is IOException) emit(androidx.datastore.preferences.core.emptyPreferences()) else throw it }
            .map { it[Keys.ENABLE_YT] ?: true }

    override val enableSaavn: Flow<Boolean> =
        ds.data.catch { if (it is IOException) emit(androidx.datastore.preferences.core.emptyPreferences()) else throw it }
            .map { it[Keys.ENABLE_SAAVN] ?: true }

    override val enableFederated: Flow<Boolean> =
        ds.data.catch { if (it is IOException) emit(androidx.datastore.preferences.core.emptyPreferences()) else throw it }
            .map { it[Keys.ENABLE_FED] ?: true }

    override suspend fun setEnableYouTube(value: Boolean) { ds.edit { it[Keys.ENABLE_YT] = value } }
    override suspend fun setEnableSaavn(value: Boolean) { ds.edit { it[Keys.ENABLE_SAAVN] = value } }
    override suspend fun setEnableFederated(value: Boolean) { ds.edit { it[Keys.ENABLE_FED] = value } }
}
