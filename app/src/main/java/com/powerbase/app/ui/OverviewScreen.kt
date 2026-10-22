package com.powerbase.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.powerbase.app.data.AlertsReport
import com.powerbase.app.data.Kpi
import com.powerbase.app.data.Snapshot

@Composable
fun OverviewScreen(snapshot: Snapshot, loading: Boolean, onRefresh: () -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { SourceRow(snapshot, loading) }

        item { KpiGrid(snapshot.kpis()) }

        if (!snapshot.alerts.ok) {
            item { AlertsBanner(snapshot.alerts) }
        }

        item {
            SectionCard(
                title = "Events per day",
                subtitle = "Last ${snapshot.trend.size} day(s), ${snapshot.source} source",
            ) {
                LineChart(
                    points = snapshot.trend,
                    lineColor = MaterialTheme.colorScheme.primary,
                    gridColor = MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier.fillMaxWidth().height(160.dp),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        snapshot.trend.firstOrNull()?.bucket.orEmpty(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        snapshot.trend.lastOrNull()?.bucket.orEmpty(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        item {
            SectionCard(title = "Top screens") {
                RankedBars(snapshot.topScreens, MaterialTheme.colorScheme.primary)
            }
        }

        item {
            SectionCard(title = "Top events") {
                RankedBars(snapshot.topEvents, MaterialTheme.colorScheme.tertiary)
            }
        }

        item {
            SectionCard(
                title = "Recent crashes",
                subtitle = "${snapshot.crashes.size} group(s) in range",
            ) {
                if (snapshot.crashes.isEmpty()) {
                    EmptyRow("No crashes in this range.")
                } else {
                    snapshot.crashes.take(3).forEach { group ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Pill(group.kind.uppercase(), levelColor("ERROR"))
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    group.message ?: group.fingerprint,
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 2,
                                )
                                Text(
                                    "${group.occurrences} on ${group.devices} device(s)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SourceRow(snapshot: Snapshot, loading: Boolean) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Pill(
                if (snapshot.source == "demo") "DEMO DATA" else "LIVE SERVER",
                if (snapshot.source == "demo") {
                    MaterialTheme.colorScheme.tertiary
                } else {
                    MaterialTheme.colorScheme.primary
                },
            )
            Text(
                "updated ${snapshot.fetchedAt}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (loading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun KpiGrid(kpis: List<Kpi>) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        kpis.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { kpi -> KpiCard(kpi, Modifier.weight(1f)) }
                if (row.size == 1) Box(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun AlertsBanner(report: AlertsReport) {
    SectionCard(
        title = "${report.alerts.size} alert(s) firing",
        subtitle = report.worst?.let { "worst severity: $it" },
    ) {
        report.alerts.take(3).forEach { alert ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Pill(alert.severity.uppercase(), severityColor(alert.severity))
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(alert.title, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        alert.rule,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        Text(
            "Open the Alerts tab for the full detail.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
