package com.example.superpodcast.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.roundToInt
import kotlin.random.Random

@Composable
fun PlayerWaveformBar(
    player: PlayerManager,
    modifier: Modifier = Modifier,
    updateEveryMs: Long = 250L
) {
    var duration by remember { mutableStateOf(0L) }
    var position by remember { mutableStateOf(0L) }

    // когда пользователь двигает ползунок — не дергаем position
    var userDragging by remember { mutableStateOf(false) }
    var dragValue by remember { mutableStateOf(0f) }

    // стабильные "столбики" waveform
    val bars = remember { generateWaveBars(count = 48) }

    LaunchedEffect(player) {
        while (true) {
            val d = player.durationMs()
            val p = player.positionMs()
            duration = d
            if (!userDragging) position = p
            delay(updateEveryMs)
        }
    }

    val progress = if (duration > 0) (position.toFloat() / duration.toFloat()).coerceIn(0f, 1f) else 0f

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {

        // Waveform
        Waveform(
            bars = bars,
            progress = if (userDragging && duration > 0) dragValue else progress,
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp),
            playedColor = MaterialTheme.colorScheme.primary,
            unplayedColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
        )

        // Slider (seek)
        Slider(
            value = if (userDragging && duration > 0) dragValue else progress,
            onValueChange = { v ->
                userDragging = true
                dragValue = v.coerceIn(0f, 1f)
            },
            onValueChangeFinished = {
                if (duration > 0) {
                    val targetMs = (duration * dragValue).roundToInt().toLong()
                    player.seekTo(targetMs)
                    position = targetMs
                }
                userDragging = false
            }
        )

        // time row
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(formatTimeMs(if (userDragging && duration > 0) (duration * dragValue).toLong() else position),
                style = MaterialTheme.typography.bodySmall
            )
            Text(formatTimeMs(duration), style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun Waveform(
    bars: List<Float>,
    progress: Float,
    modifier: Modifier,
    playedColor: Color,
    unplayedColor: Color
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val count = bars.size
        val gap = w * 0.008f
        val barW = (w - gap * (count - 1)) / count.toFloat()

        for (i in 0 until count) {
            val x = i * (barW + gap)
            val barH = (h * bars[i]).coerceIn(h * 0.15f, h) // не слишком мелко
            val top = (h - barH) / 2f

            val barProgress = i.toFloat() / (count - 1).toFloat()
            val color = if (barProgress <= progress) playedColor else unplayedColor

            drawRoundRect(
                color = color,
                topLeft = androidx.compose.ui.geometry.Offset(x, top),
                size = androidx.compose.ui.geometry.Size(barW, barH),
                cornerRadius = CornerRadius(barW / 2f, barW / 2f)
            )
        }
    }
}

private fun generateWaveBars(count: Int): List<Float> {
    val rnd = Random(7) // фиксируем seed, чтобы выглядело одинаково каждый раз
    return List(count) {
        // красивые "волны"
        val base = rnd.nextFloat()
        (0.25f + base * 0.75f).coerceIn(0.25f, 1f)
    }
}

private fun formatTimeMs(ms: Long): String {
    if (ms <= 0) return "0:00"
    val totalSec = (ms / 1000).toInt()
    val min = totalSec / 60
    val sec = totalSec % 60
    return "${min}:${sec.toString().padStart(2, '0')}"
}
