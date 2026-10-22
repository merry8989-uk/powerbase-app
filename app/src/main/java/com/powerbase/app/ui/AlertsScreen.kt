package com.powerbase.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.powerbase.app.data.AlertsReport

/**
 * Alert rules: crash-free rate, error-log rate, silent SDK, failed or stale
 * maintenance jobs. Rules stay quiet below a minimum sample size, so a quiet
 * app cannot raise a false alarm.
 */
@Composable
fun AlertsScreen(report: AlertsReport) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            SectionCard(
                title = if (report.ok) "Nothing is firing" else "${report.alerts.size} alert(s)",
                subtitle = report.worst?.let { "worst severity: $it" } ?: "all clear",
            ) {
                if (report.ok) {
                    EmptyRow("Every rule is inside its threshold.")
                } else {
                    EmptyRow("Rules are evaluated server-side; the app just shows them.")
                }
            }
        }

        items(report.alerts, key = { it.rule + it.title }) { alert ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Pill(alert.severity.uppercase(), severityColor(alert.severity))
                        Text(alert.rule, style = MaterialTheme.typography.labelMedium)
                    }
                    Text(alert.title, style = MaterialTheme.typography.titleSmall)
                    Text(
                        alert.detail,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (alert.value != null) {
                        Text(
                            "value ${alert.value}" +
                                (alert.threshold?.let { "  ·  threshold $it" } ?: ""),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}
