package com.powerbase.app.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/**
 * Persists every setting to DataStore. Reads are a Flow, so the UI reacts the
 * moment a value changes — no restart, no dead toggles (Rule 4).
 */
class SettingsRepository(private val context: Context) {

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val ACCENT_COLOR = longPreferencesKey("accent_color")
        val TEXT_SCALE = floatPreferencesKey("text_scale")
        val DENSITY = floatPreferencesKey("density")
        val LANGUAGE = stringPreferencesKey("language")
        val NOTIFICATIONS = booleanPreferencesKey("notifications_enabled")
        val DIAGNOSTICS = booleanPreferencesKey("diagnostics_enabled")

        val SERVER_URL = stringPreferencesKey("server_url")
        val READ_API_KEY = stringPreferencesKey("read_api_key")
        val REFRESH_SECONDS = intPreferencesKey("refresh_seconds")
        val DEMO_MODE = booleanPreferencesKey("demo_mode")
        val RANGE_DAYS = intPreferencesKey("range_days")

        fun feature(key: String) = booleanPreferencesKey("feature_$key")
    }

    val settings: Flow<SettingsState> = context.dataStore.data.map { p ->
        SettingsState(
            themeMode = p[Keys.THEME_MODE]
                ?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
                ?: ThemeMode.SYSTEM,
            accentColor = p[Keys.ACCENT_COLOR] ?: 0xFF0F766E,
            textScale = p[Keys.TEXT_SCALE] ?: 1.0f,
            density = p[Keys.DENSITY] ?: 1.0f,
            language = p[Keys.LANGUAGE] ?: "system",
            notificationsEnabled = p[Keys.NOTIFICATIONS] ?: true,
            diagnosticsEnabled = p[Keys.DIAGNOSTICS] ?: true,
            featureFlags = Features.ALL.associateWith { p[Keys.feature(it)] ?: true },
            serverUrl = p[Keys.SERVER_URL] ?: "",
            readApiKey = p[Keys.READ_API_KEY] ?: "",
            refreshSeconds = p[Keys.REFRESH_SECONDS] ?: 60,
            demoMode = p[Keys.DEMO_MODE] ?: true,
            rangeDays = p[Keys.RANGE_DAYS] ?: 7,
        )
    }

    suspend fun setThemeMode(mode: ThemeMode) =
        context.dataStore.edit { it[Keys.THEME_MODE] = mode.name }

    suspend fun setAccentColor(color: Long) =
        context.dataStore.edit { it[Keys.ACCENT_COLOR] = color }

    suspend fun setTextScale(scale: Float) =
        context.dataStore.edit { it[Keys.TEXT_SCALE] = scale }

    suspend fun setDensity(value: Float) =
        context.dataStore.edit { it[Keys.DENSITY] = value }

    suspend fun setLanguage(language: String) =
        context.dataStore.edit { it[Keys.LANGUAGE] = language }

    suspend fun setNotifications(enabled: Boolean) =
        context.dataStore.edit { it[Keys.NOTIFICATIONS] = enabled }

    suspend fun setDiagnosticsEnabled(enabled: Boolean) =
        context.dataStore.edit { it[Keys.DIAGNOSTICS] = enabled }

    suspend fun setServerUrl(url: String) =
        context.dataStore.edit { it[Keys.SERVER_URL] = url.trim() }

    suspend fun setReadApiKey(key: String) =
        context.dataStore.edit { it[Keys.READ_API_KEY] = key.trim() }

    suspend fun setRefreshSeconds(seconds: Int) =
        context.dataStore.edit { it[Keys.REFRESH_SECONDS] = seconds }

    suspend fun setDemoMode(enabled: Boolean) =
        context.dataStore.edit { it[Keys.DEMO_MODE] = enabled }

    suspend fun setRangeDays(days: Int) =
        context.dataStore.edit { it[Keys.RANGE_DAYS] = days }

    suspend fun setFeature(key: String, enabled: Boolean) =
        context.dataStore.edit { it[Keys.feature(key)] = enabled }

    suspend fun reset() = context.dataStore.edit { it.clear() }
}
