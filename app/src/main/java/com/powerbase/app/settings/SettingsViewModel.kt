package com.powerbase.app.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.powerbase.app.data.PowerbaseApi
import com.powerbase.app.diagnostics.DiagnosticsLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = SettingsRepository(app)

    val state: StateFlow<SettingsState> = repo.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsState())

    private val _connection = MutableStateFlow<String?>(null)
    val connection: StateFlow<String?> = _connection.asStateFlow()

    fun setThemeMode(mode: ThemeMode) = viewModelScope.launch { repo.setThemeMode(mode) }
    fun setAccentColor(color: Long) = viewModelScope.launch { repo.setAccentColor(color) }
    fun setTextScale(scale: Float) = viewModelScope.launch { repo.setTextScale(scale) }
    fun setDensity(value: Float) = viewModelScope.launch { repo.setDensity(value) }
    fun setLanguage(language: String) = viewModelScope.launch { repo.setLanguage(language) }
    fun setNotifications(enabled: Boolean) = viewModelScope.launch { repo.setNotifications(enabled) }
    fun setServerUrl(url: String) = viewModelScope.launch { repo.setServerUrl(url) }
    fun setReadApiKey(key: String) = viewModelScope.launch { repo.setReadApiKey(key) }
    fun setRefreshSeconds(seconds: Int) = viewModelScope.launch { repo.setRefreshSeconds(seconds) }
    fun setDemoMode(enabled: Boolean) = viewModelScope.launch { repo.setDemoMode(enabled) }
    fun setRangeDays(days: Int) = viewModelScope.launch { repo.setRangeDays(days) }
    fun setFeature(key: String, enabled: Boolean) = viewModelScope.launch { repo.setFeature(key, enabled) }

    fun setDiagnosticsEnabled(enabled: Boolean) = viewModelScope.launch {
        repo.setDiagnosticsEnabled(enabled)
        DiagnosticsLogger.setEnabled(enabled)
    }

    /** Round-trips the health endpoint so the user knows the settings work. */
    fun testConnection(url: String, key: String) {
        if (url.isBlank() || key.isBlank()) {
            _connection.value = "Enter a server URL and a read key first."
            return
        }
        viewModelScope.launch {
            _connection.value = "Checking…"
            _connection.value = withContext(Dispatchers.IO) {
                runCatching { PowerbaseApi(url, key).ping() }
                    .fold(
                        onSuccess = { status -> "Connected — server reports '$status'." },
                        onFailure = { error ->
                            "Could not connect: ${error.message ?: error.javaClass.simpleName}"
                        },
                    )
            }
            DiagnosticsLogger.log("Settings", "Connection test: ${_connection.value}")
        }
    }

    fun reset() = viewModelScope.launch { repo.reset() }
}
