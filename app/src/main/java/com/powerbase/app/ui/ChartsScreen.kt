package com.powerbase.app.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.powerbase.app.data.Snapshot

private val METRICS = listOf(
    "events" to "Events",
    "sessions" to "Sessions",
    "crashes" to "Crashes",
    "error_logs" to "Error logs",
)

@Composable
fun ChartsScreen(snapshot: Snapshot, rangeDays: Int) {
    var metric by remember { mutableStateOf("events") }
    val label = METRICS.firstOrNull { it.first == metric }?.second ?: metric
    val points = snapshot.series[metric].orEmpty()
    val total = points.sumOf { it.value }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            SectionCard(title = "Metric", subtitle = "Last $rangeDays day(s)") {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    METRICS.forEach { (key, name) ->
                        FilterChip(
                            selected = metric == key,
                            onClick = { metric = key },
                            label = { Text(name) },
                        )
                    }
                }
            }
        }

        item {
            SectionCard(
                title = "$label per day",
                subtitle = "Total ${Math.round(total)} across the range",
            ) {
                LineChart(
                    points = points,
                    lineColor = MaterialTheme.colorScheme.primary,
                    gridColor = MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        points.firstOrNull()?.bucket.orEmpty(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        points.lastOrNull()?.bucket.orEmpty(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        item {
            SectionCard(title = "Events by name") {
                RankedBars(snapshot.topEvents, MaterialTheme.colorScheme.tertiary)
            }
        }

        item {
            SectionCard(title = "Events by screen") {
                RankedBars(snapshot.topScreens, MaterialTheme.colorScheme.primary)
            }
        }
    }
}
