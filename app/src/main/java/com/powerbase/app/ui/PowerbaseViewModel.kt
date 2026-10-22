package com.powerbase.app.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.powerbase.app.data.DemoData
import com.powerbase.app.data.PowerbaseApi
import com.powerbase.app.data.Snapshot
import com.powerbase.app.diagnostics.DiagnosticsLogger
import com.powerbase.app.settings.SettingsState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class UiState(
    val loading: Boolean = false,
    val snapshot: Snapshot? = null,
    val error: String? = null,
)

/**
 * Loads one snapshot for the whole app.
 *
 * Demo mode is chosen when the user asks for it *or* when no server is
 * configured — an unconfigured app showing an empty error screen would be
 * useless, and sample data is honest about being sample data.
 */
class PowerbaseViewModel(app: Application) : AndroidViewModel(app) {

    var state by mutableStateOf(UiState())
        private set

    var logLevel by mutableStateOf<String?>(null)
        private set

    var logQuery by mutableStateOf("")
        private set

    fun setLogFilter(level: String?, query: String, settings: SettingsState) {
        logLevel = level
        logQuery = query
        load(settings)
    }

    fun load(settings: SettingsState) {
        val useDemo = settings.demoMode || !settings.isConfigured
        state = state.copy(loading = true, error = null)

        viewModelScope.launch {
            val outcome = withContext(Dispatchers.IO) {
                runCatching {
                    if (useDemo) {
                        DemoData.snapshot(settings.rangeDays)
                    } else {
                        PowerbaseApi(settings.serverUrl, settings.readApiKey).fetchSnapshot(
                            days = settings.rangeDays,
                            level = logLevel,
                            query = logQuery.ifBlank { null },
                        )
                    }
                }
            }

            outcome.fold(
                onSuccess = { snapshot ->
                    state = UiState(loading = false, snapshot = snapshot, error = null)
                    DiagnosticsLogger.log(
                        "Powerbase",
                        "Loaded ${snapshot.source} snapshot: ${snapshot.summary.events} events, " +
                            "${snapshot.logs.size} log lines, ${snapshot.crashes.size} crash groups",
                    )
                },
                onFailure = { error ->
                    val message = error.message ?: error.javaClass.simpleName
                    state = state.copy(loading = false, error = message)
                    DiagnosticsLogger.log("Powerbase", "Load failed: $message")
                },
            )
        }
    }
}
