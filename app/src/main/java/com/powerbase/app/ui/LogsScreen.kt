package com.powerbase.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.powerbase.app.data.LogRow

private val LEVELS = listOf<Pair<String?, String>>(
    null to "Any",
    "DEBUG" to "Debug",
    "INFO" to "Info",
    "WARN" to "Warn",
    "ERROR" to "Error",
    "FATAL" to "Fatal",
)

/**
 * Log explorer: filter by level, full-text search, and tap a row to see the
 * stacktrace. The search runs against the server's FTS5 index.
 */
@Composable
fun LogsScreen(
    logs: List<LogRow>,
    level: String?,
    query: String,
    onFilter: (String?, String) -> Unit,
) {
    var text by remember { mutableStateOf(query) }
    var expanded by remember { mutableStateOf<Long?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                LEVELS.forEach { (value, name) ->
                    FilterChip(
                        selected = level == value,
                        onClick = { onFilter(value, text) },
                        label = { Text(name) },
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Search messages") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
                Button(onClick = { onFilter(level, text) }) { Text("Search") }
            }

            Text(
                "${logs.size} line(s) shown",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (logs.isEmpty()) {
                item { EmptyRow("No log lines match this filter.") }
            }
            items(logs, key = { it.id }) { row ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expanded = if (expanded == row.id) null else row.id },
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Pill(row.level, levelColor(row.level))
                            row.tag?.let {
                                Text(it, style = MaterialTheme.typography.labelMedium)
                            }
                        }
                        Text(row.message, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            buildString {
                                append(row.ts.take(19).replace('T', ' '))
                                row.deviceId?.let { append("  ·  ").append(it) }
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        if (expanded == row.id && row.stacktrace != null) {
                            Text(
                                row.stacktrace,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}
