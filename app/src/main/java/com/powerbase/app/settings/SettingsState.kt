package com.powerbase.app.settings

/** Everything the Settings screen can control. */
data class SettingsState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val accentColor: Long = 0xFF0F766E,
    val textScale: Float = 1.0f,
    val density: Float = 1.0f,
    val language: String = "system",
    val notificationsEnabled: Boolean = true,
    /** Log collection. On by default (Rule 7). */
    val diagnosticsEnabled: Boolean = true,
    /** Per-feature on/off switches, keyed by [Features]. */
    val featureFlags: Map<String, Boolean> = emptyMap(),

    // ---- Powerbase -------------------------------------------------------
    /** Base URL of the Powerbase server, e.g. https://logs.example.com */
    val serverUrl: String = "",
    /** A read-scoped API key. An ingest key cannot read. */
    val readApiKey: String = "",
    /** Auto-refresh cadence, in seconds. 0 disables it. */
    val refreshSeconds: Int = 60,
    /** Show sample data instead of calling a server. */
    val demoMode: Boolean = true,
    /** Default time range for every screen. */
    val rangeDays: Int = 7,
) {
    val isConfigured: Boolean
        get() = !demoMode && serverUrl.isNotBlank() && readApiKey.isNotBlank()

    fun feature(key: String): Boolean = featureFlags[key] ?: true
}

enum class ThemeMode { SYSTEM, LIGHT, DARK }

/** The switches that gate each screen. */
object Features {
    const val OVERVIEW = "overview"
    const val CHARTS = "charts"
    const val LOGS = "logs"
    const val CRASHES = "crashes"
    const val DEVICES = "devices"
    const val ALERTS = "alerts"

    /** Order matters: this is the bottom-bar order. */
    val ALL = listOf(OVERVIEW, CHARTS, LOGS, CRASHES, DEVICES, ALERTS)

    fun label(key: String): String = when (key) {
        OVERVIEW -> "Overview"
        CHARTS -> "Charts"
        LOGS -> "Log explorer"
        CRASHES -> "Crash groups"
        DEVICES -> "Devices"
        ALERTS -> "Alerts"
        else -> key
    }
}
