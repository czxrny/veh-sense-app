package com.android.example.vehsense.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import com.android.example.vehsense.local.ObdFrameEntity
import com.android.example.vehsense.model.RideEvent

@Composable
fun RideCharts(
    obdFrames: List<ObdFrameEntity>,
    rideEvents: List<RideEvent>,
    maxRpm: Float = 8000f,
    maxEngineLoad: Float = 100f,
    maxSpeed: Float = 200f // km/h
) {
    val chartHeight = 100.dp
    val leftMargin = 80f
    val tickCount = 5
    val rectWidth = 6f

    val rectAlpha = 0.05f

    val minTime = obdFrames.minOfOrNull { it.timestamp } ?: 0L
    val maxTime = obdFrames.maxOfOrNull { it.timestamp } ?: 1L
    val timeRange = (maxTime - minTime).coerceAtLeast(1L)

    val accelerationEvents = rideEvents.filter { it.type == "acceleration" }
    val brakingEvents = rideEvents.filter { it.type == "braking" }
    val overspeedEvents = rideEvents.filter { it.type == "overspeed" }

    val highRpmFrames = obdFrames.filter { it.rpm > 3000 }
    val highLoadFrames = obdFrames.filter { it.engineLoad > 80 }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Speed Chart (km/h)", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(4.dp))
        Spacer(modifier = Modifier.height(10.dp))
        Canvas(modifier = Modifier.fillMaxWidth().height(chartHeight)) {
            val widthF = size.width
            val heightF = size.height

            for (i in 0..tickCount) {
                val y = heightF - i / tickCount.toFloat() * heightF
                val value = (i / tickCount.toFloat() * maxSpeed).toInt()
                drawLine(Color.LightGray, Offset(leftMargin, y), Offset(widthF, y), 1f)
                drawContext.canvas.nativeCanvas.drawText("$value", 0f, y + 6f, android.graphics.Paint().apply {
                    color = android.graphics.Color.BLACK
                    textSize = 28f
                })
            }

            val eventColorMap = mapOf(
                "acceleration" to Color.Green.copy(rectAlpha),
                "braking" to Color.Red.copy(rectAlpha),
                "overspeed" to Color.Yellow.copy(rectAlpha)
            )
            accelerationEvents.forEach { e ->
                val x = leftMargin + ((e.timestamp - minTime) / timeRange.toFloat()) * (widthF - leftMargin)
                drawRect(eventColorMap["acceleration"]!!, Offset(x, 0f), size.copy(width = rectWidth, height = heightF))
            }
            brakingEvents.forEach { e ->
                val x = leftMargin + ((e.timestamp - minTime) / timeRange.toFloat()) * (widthF - leftMargin)
                drawRect(eventColorMap["braking"]!!, Offset(x, 0f), size.copy(width = rectWidth, height = heightF))
            }
            overspeedEvents.forEach { e ->
                val x = leftMargin + ((e.timestamp - minTime) / timeRange.toFloat()) * (widthF - leftMargin)
                drawRect(eventColorMap["overspeed"]!!, Offset(x, 0f), size.copy(width = rectWidth, height = heightF))
            }

            val path = Path()
            obdFrames.forEachIndexed { index, frame ->
                val x = leftMargin + ((frame.timestamp - minTime) / timeRange.toFloat()) * (widthF - leftMargin)
                val y = heightF - (frame.vehicleSpeed / maxSpeed * heightF) - 1
                if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            drawPath(path, color = Color.Blue, style = Stroke(2f))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ===== RPM CHART =====
        Text("RPM Chart", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(4.dp))
        Spacer(modifier = Modifier.height(10.dp))
        Canvas(modifier = Modifier
            .fillMaxWidth()
            .height(chartHeight)
        ) {
            val widthF = size.width
            val heightF = size.height

            for (i in 0..tickCount) {
                val y = heightF - i / tickCount.toFloat() * heightF
                val value = (i / tickCount.toFloat() * maxRpm).toInt()
                drawLine(Color.LightGray, Offset(leftMargin, y), Offset(widthF, y), 1f)
                drawContext.canvas.nativeCanvas.drawText("$value", 0f, y + 6f, android.graphics.Paint().apply {
                    color = android.graphics.Color.BLACK
                    textSize = 28f
                })
            }

            val path = Path()
            obdFrames.forEachIndexed { index, frame ->
                val x = leftMargin + ((frame.timestamp - minTime) / timeRange.toFloat()) * (widthF - leftMargin)
                val y = heightF - (frame.rpm / maxRpm * heightF) - 1
                if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            drawPath(path, color = Color.Cyan, style = Stroke(2f))

            highRpmFrames.forEach { f ->
                val x = leftMargin + ((f.timestamp - minTime) / timeRange.toFloat()) * (widthF - leftMargin)
                drawRect(Color.Magenta.copy(rectAlpha), Offset(x, 0f), size.copy(width = rectWidth, height = heightF))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("Engine Load Chart (%)", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(4.dp))
        Spacer(modifier = Modifier.height(10.dp))
        Canvas(modifier = Modifier
            .fillMaxWidth()
            .height(chartHeight)
        ) {
            val widthF = size.width
            val heightF = size.height

            for (i in 0..tickCount) {
                val y = heightF - i / tickCount.toFloat() * heightF
                val value = (i / tickCount.toFloat() * maxEngineLoad).toInt()
                drawLine(Color.LightGray, Offset(leftMargin, y), Offset(widthF, y), 1f)
                drawContext.canvas.nativeCanvas.drawText("$value", 0f, y + 6f, android.graphics.Paint().apply {
                    color = android.graphics.Color.BLACK
                    textSize = 28f
                })
            }

            val path = Path()
            obdFrames.forEachIndexed { index, frame ->
                val x = leftMargin + ((frame.timestamp - minTime) / timeRange.toFloat()) * (widthF - leftMargin)
                val y = heightF - (frame.engineLoad / maxEngineLoad * heightF) - 1
                if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            drawPath(path, color = Color.Green, style = Stroke(2f))

            highLoadFrames.forEach { f ->
                val x = leftMargin + ((f.timestamp - minTime) / timeRange.toFloat()) * (widthF - leftMargin)
                drawRect(Color.Red.copy(rectAlpha), Offset(x, 0f), size.copy(width = rectWidth, height = heightF))
            }
        }
    }
}