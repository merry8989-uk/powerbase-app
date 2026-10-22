package com.powerbase.app.data

import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/** Raised when the server answers with a non-2xx status. */
class PowerbaseException(val status: Int, val body: String) :
    Exception("Powerbase returned HTTP $status")

/**
 * Read-only client for a Powerbase server.
 *
 * Uses `HttpURLConnection` and `org.json` on purpose: both ship with Android, so
 * the app pulls in no networking or serialisation library to talk to its own
 * backend.
 *
 * Every call authenticates with a **read-scoped** key. An ingest key cannot
 * reach these endpoints — that separation is enforced server-side.
 */
class PowerbaseApi(private val baseUrl: String, private val apiKey: String) {

    private val timeFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    /** Fetches one consistent snapshot of everything the app shows. */
    fun fetchSnapshot(days: Int, level: String? = null, query: String? = null): Snapshot {
        val until = timeFormat.format(Date())
        val since = timeFormat.format(Date(System.currentTimeMillis() - days * 86_400_000L))

        val summary = summary(since, until)
        val series = listOf("events", "sessions", "crashes", "error_logs").associateWith {
            timeseries(it, "day", since, until)
        }
        val crashes = crashGroups(since, until)
        val alerts = alerts()

        return Snapshot(
            summary = summary,
            series = series,
            topScreens = top("screen", since, until),
            topEvents = top("event", since, until),
            logs = logs(since, until, level, query),
            crashes = crashes,
            devices = devices(),
            alerts = alerts,
            source = "live",
            fetchedAt = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date()),
        )
    }

    fun summary(since: String, until: String): Summary {
        val json = getJson("/v1/metrics/summary", mapOf("since" to since, "until" to until))
        return Summary(
            sessions = json.optLong("sessions"),
            events = json.optLong("events"),
            logs = json.optLong("logs"),
            errorLogs = json.optLong("error_logs"),
            crashes = json.optLong("crashes"),
            activeDevices = json.optLong("active_devices"),
            crashedSessions = json.optLong("crashed_sessions"),
            crashFreeRate = json.optDouble("crash_free_rate", 1.0),
            avgSessionDurationMs = json.optDouble("avg_session_duration_ms", 0.0),
        )
    }

    fun timeseries(metric: String, bucket: String, since: String, until: String): List<Point> {
        val json = getJson(
            "/v1/metrics/timeseries",
            mapOf("metric" to metric, "bucket" to bucket, "since" to since, "until" to until),
        )
        val points = json.optJSONArray("points") ?: return emptyList()
        return buildList {
            for (i in 0 until points.length()) {
                val row = points.optJSONObject(i) ?: continue
                add(Point(row.optString("bucket"), row.optDouble("value")))
            }
        }
    }

    fun top(dimension: String, since: String, until: String, limit: Int = 8): List<TopItem> {
        val json = getJson(
            "/v1/metrics/top",
            mapOf(
                "dimension" to dimension,
                "since" to since,
                "until" to until,
                "limit" to limit.toString(),
            ),
        )
        val items = json.optJSONArray("items") ?: return emptyList()
        return buildList {
            for (i in 0 until items.length()) {
                val row = items.optJSONObject(i) ?: continue
                add(TopItem(row.optString("key", "—"), row.optDouble("value")))
            }
        }
    }

    fun logs(
        since: String,
        until: String,
        level: String? = null,
        query: String? = null,
        limit: Int = 100,
    ): List<LogRow> {
        val params = mutableMapOf(
            "since" to since,
            "until" to until,
            "limit" to limit.toString(),
            "order" to "desc",
        )
        if (!level.isNullOrBlank()) params["level"] = level
        if (!query.isNullOrBlank()) params["q"] = query

        val json = getJson("/v1/query/logs", params)
        val items = json.optJSONArray("items") ?: return emptyList()
        return buildList {
            for (i in 0 until items.length()) {
                val row = items.optJSONObject(i) ?: continue
                add(
                    LogRow(
                        id = row.optLong("id"),
                        ts = row.optString("ts"),
                        level = row.optString("level", "INFO"),
                        tag = row.optString("tag").ifBlank { null },
                        message = row.optString("message"),
                        deviceId = row.optString("device_id").ifBlank { null },
                        stacktrace = row.optString("stacktrace").ifBlank { null },
                    )
                )
            }
        }
    }

    fun crashGroups(since: String, until: String): List<CrashGroup> {
        val json = getJson(
            "/v1/query/crash-groups",
            mapOf("since" to since, "until" to until, "limit" to "50"),
        )
        val items = json.optJSONArray("items") ?: return emptyList()
        return buildList {
            for (i in 0 until items.length()) {
                val row = items.optJSONObject(i) ?: continue
                add(
                    CrashGroup(
                        fingerprint = row.optString("fingerprint"),
                        kind = row.optString("kind", "java"),
                        message = row.optString("message").ifBlank { null },
                        occurrences = row.optLong("occurrences"),
                        devices = row.optLong("devices"),
                        firstSeen = row.optString("first_seen"),
                        lastSeen = row.optString("last_seen"),
                        appVersion = row.optString("app_version").ifBlank { null },
                    )
                )
            }
        }
    }

    fun devices(): List<DeviceRow> {
        val json = getJson("/v1/query/devices", mapOf("limit" to "100"))
        val items = json.optJSONArray("items") ?: return emptyList()
        return buildList {
            for (i in 0 until items.length()) {
                val row = items.optJSONObject(i) ?: continue
                add(
                    DeviceRow(
                        id = row.optString("id"),
                        model = row.optString("model").ifBlank { null },
                        manufacturer = row.optString("manufacturer").ifBlank { null },
                        osVersion = row.optString("os_version").ifBlank { null },
                        appVersion = row.optString("app_version").ifBlank { null },
                        lastSeen = row.optString("last_seen"),
                        sessionCount = row.optLong("session_count"),
                    )
                )
            }
        }
    }

    fun alerts(): AlertsReport {
        val json = getJson("/v1/alerts", emptyMap())
        val items = json.optJSONArray("alerts") ?: JSONArray()
        val list = buildList {
            for (i in 0 until items.length()) {
                val row = items.optJSONObject(i) ?: continue
                add(
                    AlertItem(
                        rule = row.optString("rule"),
                        severity = row.optString("severity", "warning"),
                        title = row.optString("title"),
                        detail = row.optString("detail"),
                        value = if (row.isNull("value")) null else row.optDouble("value"),
                        threshold = if (row.isNull("threshold")) null else row.optDouble("threshold"),
                    )
                )
            }
        }
        return AlertsReport(
            ok = json.optBoolean("ok", list.isEmpty()),
            worst = json.optString("worst").ifBlank { null },
            alerts = list,
        )
    }

    /** Verifies the URL and key without pulling a whole snapshot. */
    fun ping(): String {
        val json = getJson("/v1/health", emptyMap())
        return json.optString("status", "unknown")
    }

    private fun getJson(path: String, params: Map<String, String>): JSONObject {
        val body = get(path, params)
        return JSONObject(body)
    }

    private fun get(path: String, params: Map<String, String>): String {
        val query = params.entries.joinToString("&") { (key, value) ->
            "${URLEncoder.encode(key, "UTF-8")}=${URLEncoder.encode(value, "UTF-8")}"
        }
        val url = URL(baseUrl.trimEnd('/') + path + if (query.isEmpty()) "" else "?$query")
        val connection = url.openConnection() as HttpURLConnection
        return try {
            connection.requestMethod = "GET"
            connection.connectTimeout = 10_000
            connection.readTimeout = 20_000
            connection.setRequestProperty("Accept", "application/json")
            if (apiKey.isNotBlank()) {
                connection.setRequestProperty("Authorization", "Bearer $apiKey")
            }
            val status = connection.responseCode
            val stream = if (status in 200..299) connection.inputStream else connection.errorStream
            val text = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
            if (status !in 200..299) throw PowerbaseException(status, text)
            text
        } finally {
            connection.disconnect()
        }
    }
}
