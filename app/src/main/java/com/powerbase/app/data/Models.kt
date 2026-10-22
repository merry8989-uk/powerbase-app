package com.powerbase.app.data

/** Colour intent for a KPI card, so the UI does not hard-code thresholds. */
enum class Tone { NEUTRAL, GOOD, WARN, BAD }

data class Kpi(val label: String, val value: String, val tone: Tone = Tone.NEUTRAL)

/** One bucket of a time series. */
data class Point(val bucket: String, val value: Double)

/** One row of a ranked breakdown. */
data class TopItem(val key: String, val value: Double)

data class Summary(
    val sessions: Long = 0,
    val events: Long = 0,
    val logs: Long = 0,
    val errorLogs: Long = 0,
    val crashes: Long = 0,
    val activeDevices: Long = 0,
    val crashedSessions: Long = 0,
    val crashFreeRate: Double = 1.0,
    val avgSessionDurationMs: Double = 0.0,
)

data class LogRow(
    val id: Long,
    val ts: String,
    val level: String,
    val tag: String?,
    val message: String,
    val deviceId: String?,
    val stacktrace: String?,
)

data class CrashGroup(
    val fingerprint: String,
    val kind: String,
    val message: String?,
    val occurrences: Long,
    val devices: Long,
    val firstSeen: String,
    val lastSeen: String,
    val appVersion: String?,
)

data class DeviceRow(
    val id: String,
    val model: String?,
    val manufacturer: String?,
    val osVersion: String?,
    val appVersion: String?,
    val lastSeen: String,
    val sessionCount: Long,
)

data class AlertItem(
    val rule: String,
    val severity: String,
    val title: String,
    val detail: String,
    val value: Double?,
    val threshold: Double?,
)

data class AlertsReport(val ok: Boolean, val worst: String?, val alerts: List<AlertItem>)

/** Everything one screenful of the app needs, fetched in one pass. */
data class Snapshot(
    val summary: Summary = Summary(),
    /** Pre-fetched time series, keyed by metric name (events, sessions, crashes…). */
    val series: Map<String, List<Point>> = emptyMap(),
    val topScreens: List<TopItem> = emptyList(),
    val topEvents: List<TopItem> = emptyList(),
    val logs: List<LogRow> = emptyList(),
    val crashes: List<CrashGroup> = emptyList(),
    val devices: List<DeviceRow> = emptyList(),
    val alerts: AlertsReport = AlertsReport(true, null, emptyList()),
    /** "live" when it came off a server, "demo" when it is sample data. */
    val source: String = "demo",
    val fetchedAt: String = "",
) {
    /** The headline series, used by the Overview sparkline. */
    val trend: List<Point> get() = series["events"].orEmpty()

    fun kpis(): List<Kpi> = listOf(
        Kpi("Active devices", summary.activeDevices.toString()),
        Kpi("Sessions", summary.sessions.toString()),
        Kpi("Events", summary.events.toString()),
        Kpi("Crashes", summary.crashes.toString(), if (summary.crashes == 0L) Tone.GOOD else Tone.BAD),
        Kpi(
            "Crash-free",
            formatPercent(summary.crashFreeRate),
            when {
                summary.crashFreeRate >= 0.99 -> Tone.GOOD
                summary.crashFreeRate >= 0.95 -> Tone.WARN
                else -> Tone.BAD
            },
        ),
        Kpi(
            "Error logs",
            summary.errorLogs.toString(),
            if (summary.errorLogs == 0L) Tone.GOOD else Tone.WARN,
        ),
    )
}

fun formatPercent(value: Double): String {
    val pct = value * 100.0
    val rounded = Math.round(pct * 10.0) / 10.0
    return "$rounded%"
}

fun formatDuration(millis: Double): String {
    if (millis <= 0) return "—"
    val totalSeconds = (millis / 1000.0).toLong()
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return if (minutes > 0) "${minutes}m ${seconds}s" else "${seconds}s"
}
