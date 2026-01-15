package com.example.superpodcast.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.superpodcast.ui.theme.Cocoa
import com.example.superpodcast.ui.theme.TextPrimary
import com.example.superpodcast.ui.theme.TextSecondary

@Composable
fun GlobalMiniPlayerBar(
    player: PlayerManager,
    holder: PlayerHolderViewModel,
    modifier: Modifier = Modifier
) {
    val now by holder.nowPlaying.collectAsState()
    val url = now.url?.trim().orEmpty()
    if (url.isBlank()) return

    // ✅ 1 раз подписались: isPlaying + auto-next
    LaunchedEffect(player, holder) {
        player.onPlayingChanged = { playing -> holder.setPlaying(playing) }
        player.onEnded = { holder.playNextEpisode(player) }
        player.onError = { holder.playNextEpisode(player) }
    }

    Surface(
        color = Cocoa,
        tonalElevation = 6.dp,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = now.episodeTitle?.takeIf { it.isNotBlank() }
                        ?: now.title.ifBlank { "Now playing" },
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = now.author,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(Modifier.width(8.dp))

            IconButton(onClick = {
                if (now.isPlaying) player.pause()
                else player.play(url, key = now.key)
            }) {
                Icon(
                    imageVector = if (now.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = "Play/Pause",
                    tint = TextPrimary
                )
            }

            IconButton(onClick = {
                player.stop()
                holder.clear()
            }) {
                Icon(
                    imageVector = Icons.Filled.Stop,
                    contentDescription = "Stop",
                    tint = TextPrimary
                )
            }
        }
    }
}
