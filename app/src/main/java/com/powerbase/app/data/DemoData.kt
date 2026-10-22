package com.powerbase.app.data

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Sample data for demo mode.
 *
 * The app is useful before a server exists — and a demo mode means the public
 * page and anyone who installs the app can see exactly what every screen does
 * without standing up a backend first. Values are deterministic so the same
 * build always looks the same.
 */
object DemoData {

    private val dayFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    private val stampFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    fun snapshot(days: Int = 7): Snapshot {
        val now = System.currentTimeMillis()
        val days_ = (days - 1 downTo 0).map { back -> dayFormat.format(Date(now - back * 86_400_000L)) }
        fun wave(base: Int, swing: Int): List<Point> = days_.mapIndexed { index, day ->
            Point(day, (base + (index * 37) % swing).toDouble())
        }
        val series = mapOf(
            "events" to wave(1_400, 260),
            "sessions" to wave(180, 40),
            "crashes" to days_.map { Point(it, 0.0) },
            "error_logs" to wave(5, 4),
        )

        return Snapshot(
            summary = Summary(
                sessions = 1_284,
                events = 48_915,
                logs = 2_306,
                errorLogs = 37,
                crashes = 4,
                activeDevices = 612,
                crashedSessions = 4,
                crashFreeRate = 0.9969,
                avgSessionDurationMs = 214_000.0,
            ),
            series = series,
            topScreens = listOf(
                TopItem("HomeActivity", 14_204.0),
                TopItem("ProductActivity", 9_811.0),
                TopItem("CheckoutActivity", 6_430.0),
                TopItem("SearchActivity", 4_902.0),
                TopItem("ProfileActivity", 2_118.0),
            ),
            topEvents = listOf(
                TopItem("screen_view", 18_442.0),
                TopItem("add_to_cart", 7_301.0),
                TopItem("purchase", 1_984.0),
                TopItem("search", 3_120.0),
                TopItem("share", 640.0),
            ),
            logs = listOf(
                LogRow(
                    4, stamp(now - 240_000), "ERROR", "CheckoutRepo",
                    "Payment intent failed for [REDACTED_EMAIL]",
                    "demo-dev-a1", null,
                ),
                LogRow(
                    3, stamp(now - 620_000), "WARN", "AuthManager",
                    "Token refresh took 2.4s, retrying",
                    "demo-dev-b7", null,
                ),
                LogRow(
                    2, stamp(now - 1_500_000), "INFO", "SyncWorker",
                    "Uploaded 412 queued events in 3 batches",
                    "demo-dev-a1", null,
                ),
                LogRow(
                    1, stamp(now - 3_100_000), "DEBUG", "Powerbase",
                    "Flush scheduled in 30s",
                    "demo-dev-c3", null,
                ),
            ),
            crashes = listOf(
                CrashGroup(
                    "45a5b23c327e250f3c032e32cd4e21e9", "java",
                    "java.lang.NullPointerException",
                    3, 3,
                    stamp(now - 2 * 86_400_000L), stamp(now - 240_000),
                    "1.0.0",
                ),
                CrashGroup(
                    "53c8d53b17329723120b204a3242bd60", "anr",
                    "Application Not Responding",
                    1, 1,
                    stamp(now - 86_400_000L), stamp(now - 86_400_000L),
                    "1.0.0",
                ),
            ),
            devices = listOf(
                DeviceRow("demo-dev-a1", "Pixel 8", "Google", "14", "1.0.0", stamp(now - 60_000), 214),
                DeviceRow("demo-dev-b7", "Redmi Note 12", "Xiaomi", "13", "1.0.0", stamp(now - 900_000), 187),
                DeviceRow("demo-dev-c3", "SM-S911B", "samsung", "14", "0.9.4", stamp(now - 5_400_000), 96),
            ),
            alerts = AlertsReport(
                ok = false,
                worst = "warning",
                alerts = listOf(
                    AlertItem(
                        "no_telemetry", "warning",
                        "No telemetry for 26.4h",
                        "The last record from this app arrived 26.4h ago (threshold 24h). " +
                            "The SDK may be failing to upload.",
                        1584.0, 1440.0,
                    ),
                ),
            ),
            source = "demo",
            fetchedAt = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date()),
        )
    }

    private fun stamp(millis: Long): String = stampFormat.format(Date(millis))
}
