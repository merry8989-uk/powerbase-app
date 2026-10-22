package com.powerbase.app.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.powerbase.app.BuildConfig
import com.powerbase.app.diagnostics.DiagnosticsSection
import com.powerbase.app.updater.UpdateUiState
import com.powerbase.app.updater.UpdateViewModel

private val ACCENTS = listOf(
    "Teal" to 0xFF0F766E,
    "Indigo" to 0xFF4F46E5,
    "Amber" to 0xFFB45309,
    "Rose" to 0xFFBE123C,
)

private val LANGUAGES = listOf("system", "en", "hi")

private val REFRESH_OPTIONS = listOf(0, 15, 30, 60, 300)

private val RANGE_OPTIONS = listOf(1, 7, 14, 30, 90)

/**
 * Rich, fully-wired Settings screen. Every control writes through the view
 * model to DataStore and reads back from the same Flow (Rule 4). Ends with the
 * version + @osintgram_io contact, and carries the in-app update control
 * (Rule 6).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    vm: SettingsViewModel = viewModel(),
    updater: UpdateViewModel = viewModel(),
) {
    val state by vm.state.collectAsState()
    val connection by vm.connection.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Back") } },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Section("Appearance")

            Text("Theme")
            SingleChoiceSegmentedButtonRow {
                ThemeMode.entries.forEachIndexed { i, mode ->
                    SegmentedButton(
                        selected = state.themeMode == mode,
                        onClick = { vm.setThemeMode(mode) },
                        shape = SegmentedButtonDefaults.itemShape(i, ThemeMode.entries.size),
                    ) {
                        Text(mode.name.lowercase().replaceFirstChar { it.uppercase() })
                    }
                }
            }

            Text("Accent colour")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ACCENTS.forEach { (name, argb) ->
                    FilterChip(
                        selected = state.accentColor == argb,
                        onClick = { vm.setAccentColor(argb) },
                        label = { Text(name) },
                    )
                }
            }

            Text("Text size (${"%.2f".format(state.textScale)}x)")
            Slider(
                value = state.textScale,
                onValueChange = vm::setTextScale,
                valueRange = 0.85f..1.5f,
            )

            Text("Density (${"%.2f".format(state.density)}x)")
            Slider(
                value = state.density,
                onValueChange = vm::setDensity,
                valueRange = 0.80f..1.20f,
            )

            Section("Server")

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("Demo mode")
                Switch(checked = state.demoMode, onCheckedChange = vm::setDemoMode)
            }
            Text(
                "Demo mode shows built-in sample data. Turn it off and point the app at your own Powerbase server.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            OutlinedTextField(
                value = state.serverUrl,
                onValueChange = vm::setServerUrl,
                label = { Text("Server URL") },
                placeholder = { Text("https://logs.example.com") },
                singleLine = true,
                enabled = !state.demoMode,
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = state.readApiKey,
                onValueChange = vm::setReadApiKey,
                label = { Text("Read API key") },
                placeholder = { Text("pb_…") },
                singleLine = true,
                enabled = !state.demoMode,
                modifier = Modifier.fillMaxWidth(),
            )

            Button(
                onClick = { vm.testConnection(state.serverUrl, state.readApiKey) },
                enabled = !state.demoMode,
            ) { Text("Test connection") }

            connection?.let { message ->
                Text(
                    message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Text("Auto-refresh")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                REFRESH_OPTIONS.forEach { seconds ->
                    FilterChip(
                        selected = state.refreshSeconds == seconds,
                        onClick = { vm.setRefreshSeconds(seconds) },
                        label = { Text(if (seconds == 0) "Off" else "${seconds}s") },
                    )
                }
            }

            Text("Default range")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RANGE_OPTIONS.forEach { days ->
                    FilterChip(
                        selected = state.rangeDays == days,
                        onClick = { vm.setRangeDays(days) },
                        label = { Text("${days}d") },
                    )
                }
            }

            Section("Screens")

            Features.ALL.forEach { key ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(Features.label(key))
                    Switch(
                        checked = state.feature(key),
                        onCheckedChange = { vm.setFeature(key, it) },
                    )
                }
            }

            Section("General")

            Text("Language")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LANGUAGES.forEach { lang ->
                    FilterChip(
                        selected = state.language == lang,
                        onClick = { vm.setLanguage(lang) },
                        label = { Text(lang) },
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("Notifications")
                Switch(
                    checked = state.notificationsEnabled,
                    onCheckedChange = vm::setNotifications,
                )
            }

            Section("Updates")

            val updateState = updater.state
            Text("Current version ${BuildConfig.VERSION_NAME}")

            when (updateState) {
                UpdateUiState.Checking -> Text("Checking for updates…")
                UpdateUiState.UpToDate -> Text("You're on the latest version.")
                is UpdateUiState.Available -> {
                    Text("Version ${updateState.info.latestVersion} is available.")
                    Button(onClick = { updater.install(updateState.info) }) {
                        Text("Update now")
                    }
                }
                UpdateUiState.Downloading -> Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    Text("Downloading update…")
                }
                is UpdateUiState.Failed -> Text("Update failed: ${updateState.message}")
                UpdateUiState.Idle -> Unit
            }

            OutlinedButton(
                onClick = { updater.check(BuildConfig.VERSION_NAME) },
                enabled = updateState !is UpdateUiState.Checking &&
                    updateState !is UpdateUiState.Downloading,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Check for updates")
            }

            DiagnosticsSection(
                enabled = state.diagnosticsEnabled,
                onEnabledChange = vm::setDiagnosticsEnabled,
            )

            Section("Reset")
            OutlinedButton(onClick = vm::reset, modifier = Modifier.fillMaxWidth()) {
                Text("Reset to defaults")
            }

            HorizontalDivider()

            // --- Footer: version + contact (Rule 4) ---
            Text("Version ${BuildConfig.VERSION_NAME}", style = MaterialTheme.typography.bodySmall)
            Text(
                text = "@${Contact.INSTAGRAM_HANDLE}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.clickable { Contact.openInstagram(context) },
            )
            Text(
                "Suggestions, feedback or a new feature request? Tap the handle above.",
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun Section(title: String) {
    Text(title, style = MaterialTheme.typography.titleMedium)
}
