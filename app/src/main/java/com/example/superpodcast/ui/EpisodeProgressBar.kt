package com.example.superpodcast.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun EpisodeProgressBar(
    player: PlayerManager,
    episodeId: String,
    modifier: Modifier = Modifier
) {
    // показываем ТОЛЬКО если текущий эпизод = этот id
    val isThisEpisode = player.currentKey == episodeId
    if (!isThisEpisode) return

    var posMs by remember { mutableLongStateOf(0L) }
    var durMs by remember { mutableLongStateOf(0L) }

    LaunchedEffect(episodeId) {
        while (true) {
            posMs = player.positionMs()
            durMs = player.durationMs()
            delay(250)
        }
    }

    if (durMs <= 0L) return

    val fraction = (posMs.toFloat() / durMs.toFloat()).coerceIn(0f, 1f)

    Column(modifier = modifier) {
        Slider(
            value = fraction,
            onValueChange = { f -> player.seekTo((durMs * f).toLong()) }
        )
        Row(Modifier.fillMaxWidth()) {
            Text(formatMs(posMs))
            Spacer(Modifier.weight(1f))
            Text(formatMs(durMs))
        }
    }
}

private fun formatMs(ms: Long): String {
    val totalSec = (ms / 1000).coerceAtLeast(0)
    val m = totalSec / 60
    val s = totalSec % 60
    return "%d:%02d".format(m, s)
}