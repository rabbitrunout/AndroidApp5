package com.example.superpodcast.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun PlayerWaveformBar(
    player: PlayerManager,
    modifier: Modifier = Modifier
) {
    var posMs by remember { mutableLongStateOf(0L) }
    var durMs by remember { mutableLongStateOf(0L) }

    // обновляем пока composable жив
    LaunchedEffect(player) {
        while (isActive) {
            posMs = player.positionMs()
            durMs = player.durationMs()
            delay(250)
        }
    }

    // если трека нет / duration неизвестна — не показываем
    if (durMs <= 0L) return

    val fraction = (posMs.toFloat() / durMs.toFloat()).coerceIn(0f, 1f)

    Column(modifier = modifier) {
        Slider(
            value = fraction,
            onValueChange = { f -> player.seekTo((durMs * f).toLong()) }
        )

        Row(modifier = Modifier.fillMaxWidth()) {
            Text(formatMs(posMs), style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.weight(1f))
            Text(formatMs(durMs), style = MaterialTheme.typography.bodySmall)
        }
    }
}

private fun formatMs(ms: Long): String {
    val totalSec = (ms / 1000).coerceAtLeast(0)
    val m = totalSec / 60
    val s = totalSec % 60
    return "%d:%02d".format(m, s)
}
