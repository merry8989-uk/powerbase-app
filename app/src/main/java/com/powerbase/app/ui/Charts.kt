package com.powerbase.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.powerbase.app.data.Point
import com.powerbase.app.data.TopItem

/**
 * A line chart drawn straight onto a Canvas.
 *
 * Hand-drawn rather than pulled from a charting library: the app already avoids
 * extra dependencies for networking and JSON, and a sparkline is a path and a
 * few circles.
 */
@Composable
fun LineChart(
    points: List<Point>,
    lineColor: Color,
    gridColor: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        if (points.size < 2) return@Canvas

        val maxValue = points.maxOf { it.value }.coerceAtLeast(1.0)
        val stepX = size.width / (points.size - 1)

        // Horizontal gridlines give the eye a scale without drawing axis labels.
        for (i in 0..3) {
            val y = size.height * i / 3f
            drawLine(
                color = gridColor,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1f,
            )
        }

        val path = Path()
        points.forEachIndexed { index, point ->
            val x = index * stepX
            val y = size.height - (point.value / maxValue * size.height).toFloat()
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path = path, color = lineColor, style = Stroke(width = 5f))

        points.forEachIndexed { index, point ->
            val x = index * stepX
            val y = size.height - (point.value / maxValue * size.height).toFloat()
            drawCircle(color = lineColor, radius = 6f, center = Offset(x, y))
        }
    }
}

/** A horizontal ranked bar list. Compose layout, so the labels are real text. */
@Composable
fun RankedBars(
    items: List<TopItem>,
    barColor: Color,
    modifier: Modifier = Modifier,
    valueSuffix: String = "",
) {
    if (items.isEmpty()) {
        Text(
            text = "No data in this range.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = modifier,
        )
        return
    }

    val maxValue = items.maxOf { it.value }.coerceAtLeast(1.0)

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items.forEach { item ->
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = item.key,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    Text(
                        text = formatValue(item.value) + valueSuffix,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth((item.value / maxValue).toFloat().coerceIn(0.02f, 1f))
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(barColor),
                    )
                }
            }
        }
    }
}

private fun formatValue(value: Double): String {
    val rounded = Math.round(value).toLong()
    return when {
        rounded >= 1_000_000 -> "${rounded / 1_000_000}M"
        rounded >= 10_000 -> "${rounded / 1_000}K"
        else -> rounded.toString()
    }
}

/** Padding helper so charts line up with surrounding cards. */
@Composable
fun ChartBlock(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(modifier = modifier.padding(vertical = 4.dp), contentAlignment = Alignment.CenterStart) {
        content()
    }
}
