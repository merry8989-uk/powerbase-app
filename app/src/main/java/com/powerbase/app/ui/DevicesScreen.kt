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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.powerbase.app.data.DeviceRow

@Composable
fun DevicesScreen(devices: List<DeviceRow>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            SectionCard(
                title = "${devices.size} device(s)",
                subtitle = "Most recently seen first",
            ) {
                EmptyRow("Tap through the list below for model, OS and app version.")
            }
        }

        if (devices.isEmpty()) {
            item { EmptyRow("No devices have reported yet.") }
        }

        items(devices, key = { it.id }) { device ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        listOfNotNull(device.manufacturer, device.model)
                            .joinToString(" ")
                            .ifBlank { device.id },
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Text(
                        "Android ${device.osVersion ?: "?"}  ·  app ${device.appVersion ?: "?"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Pill("${device.sessionCount} SESSIONS", MaterialTheme.colorScheme.primary)
                    }
                    Text(
                        "last seen ${device.lastSeen.take(19).replace('T', ' ')}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        device.id,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}
