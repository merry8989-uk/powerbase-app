package com.powerbase.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.powerbase.app.data.DemoData
import com.powerbase.app.settings.Features
import com.powerbase.app.settings.SettingsScreen
import com.powerbase.app.settings.SettingsViewModel
import kotlinx.coroutines.delay

/**
 * Root composable: a tab per enabled feature, with Settings one tap away.
 *
 * Tabs come from the Settings feature switches, so turning a screen off really
 * removes it (Rule 4: no dead toggles).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppRoot() {
    val vm: PowerbaseViewModel = viewModel()
    val settingsVm: SettingsViewModel = viewModel()
    val settings by settingsVm.state.collectAsState()

    var showSettings by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf(Features.OVERVIEW) }

    val state = vm.state

    // Reload whenever anything that changes the answer changes.
    LaunchedEffect(settings.demoMode, settings.serverUrl, settings.readApiKey, settings.rangeDays) {
        vm.load(settings)
    }

    // Auto-refresh, driven by the interval in Settings.
    LaunchedEffect(settings.refreshSeconds, settings.demoMode, settings.serverUrl, settings.readApiKey) {
        val seconds = settings.refreshSeconds
        if (seconds <= 0) return@LaunchedEffect
        while (true) {
            delay(seconds * 1000L)
            vm.load(settings)
        }
    }

    if (showSettings) {
        SettingsScreen(onBack = { showSettings = false })
        return
    }

    val visible = Features.ALL.filter { settings.feature(it) }
    val active = if (visible.contains(selected)) selected else visible.firstOrNull() ?: Features.OVERVIEW

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Powerbase") },
                actions = {
                    TextButton(onClick = { vm.load(settings) }) { Text("Refresh") }
                    TextButton(onClick = { showSettings = true }) { Text("Settings") }
                },
            )
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (visible.isEmpty()) {
                MessageBox(
                    title = "Every screen is switched off",
                    detail = "Turn a screen back on in Settings → Screens.",
                    actionLabel = "Open Settings",
                    onAction = { showSettings = true },
                )
                return@Box
            }

            androidx.compose.foundation.layout.Column(modifier = Modifier.fillMaxSize()) {
                ScrollableTabRow(
                    selectedTabIndex = visible.indexOf(active),
                    containerColor = MaterialTheme.colorScheme.surface,
                ) {
                    visible.forEach { key ->
                        Tab(
                            selected = key == active,
                            onClick = { selected = key },
                            text = { Text(Features.label(key)) },
                        )
                    }
                }

                val snapshot = state.snapshot ?: DemoData.snapshot(settings.rangeDays)

                when {
                    state.error != null && state.snapshot == null -> MessageBox(
                        title = "Could not reach the server",
                        detail = state.error ?: "Unknown error",
                        actionLabel = "Retry",
                        onAction = { vm.load(settings) },
                    )

                    active == Features.OVERVIEW ->
                        OverviewScreen(snapshot, state.loading, onRefresh = { vm.load(settings) })

                    active == Features.CHARTS ->
                        ChartsScreen(snapshot, settings.rangeDays)

                    active == Features.LOGS ->
                        LogsScreen(
                            logs = snapshot.logs,
                            level = vm.logLevel,
                            query = vm.logQuery,
                            onFilter = { level, query -> vm.setLogFilter(level, query, settings) },
                        )

                    active == Features.CRASHES ->
                        CrashesScreen(snapshot.crashes)

                    active == Features.DEVICES ->
                        DevicesScreen(snapshot.devices)

                    active == Features.ALERTS ->
                        AlertsScreen(snapshot.alerts)

                    else -> MessageBox("Nothing here", "Pick another tab.")
                }
            }
        }
    }
}
