package com.powerbase.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import com.powerbase.app.diagnostics.DiagnosticsLogger
import com.powerbase.app.settings.SettingsRepository
import com.powerbase.app.settings.SettingsState
import com.powerbase.app.settings.ThemeMode
import com.powerbase.app.ui.AppRoot
import com.powerbase.app.ui.theme.PowerbaseTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        enterImmersiveMode()

        // Apply the stored diagnostics preference (Rule 7).
        lifecycleScope.launch {
            val stored = SettingsRepository(applicationContext).settings.first()
            DiagnosticsLogger.setEnabled(stored.diagnosticsEnabled)
        }

        setContent {
            val repository = remember { SettingsRepository(applicationContext) }
            val settings by repository.settings.collectAsState(initial = SettingsState())

            PowerbaseTheme(
                darkTheme = when (settings.themeMode) {
                    ThemeMode.SYSTEM -> isSystemInDarkTheme()
                    ThemeMode.LIGHT -> false
                    ThemeMode.DARK -> true
                },
                accent = Color(settings.accentColor),
                textScale = settings.textScale,
                densityScale = settings.density,
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    AppRoot()
                }
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        // Re-hide the bars whenever focus returns (Rule 4: full-screen default).
        if (hasFocus) enterImmersiveMode()
    }

    /** Hides the status and navigation bars; swipe reveals them transiently. */
    private fun enterImmersiveMode() {
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller.hide(WindowInsetsCompat.Type.systemBars())
    }
}
