package com.example.superpodcast.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.superpodcast.model.PodcastSummaryViewData
import com.example.superpodcast.ui.theme.TextPrimary
import com.example.superpodcast.ui.theme.TextSecondary
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PodcastDetailsScreen(
    vm: SearchViewModel,
    player: PlayerManager,                 // ✅ shared player
    podcast: PodcastSummaryViewData,
    onBack: () -> Unit
) {
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var playing by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    fun formatReleaseDate(iso: String?): String {
        if (iso.isNullOrBlank()) return "—"
        return runCatching {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
            parser.timeZone = TimeZone.getTimeZone("UTC")
            val date = parser.parse(iso)
            val out = SimpleDateFormat("MMM d, yyyy", Locale.US)
            out.format(date!!)
        }.getOrElse { iso }
    }

    fun refresh() {
        playing = player.isPlaying()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Podcast details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(podcast.title, style = MaterialTheme.typography.titleLarge, color = TextPrimary)
            Text(podcast.author, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)

            Spacer(Modifier.height(6.dp))

            InfoRow("Release date", formatReleaseDate(podcast.releaseDate))
            InfoRow("Genre", podcast.genre ?: "—")
            InfoRow("Country", podcast.country ?: "—")
            InfoRow("Episodes", podcast.trackCount?.toString() ?: "—")
            InfoRow("Feed URL", if (podcast.feedUrl.isNotBlank()) "Available" else "—")

            if (loading) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())

            if (error != null) {
                Text("Error: $error", color = MaterialTheme.colorScheme.error)
            }

            // ✅ Real PLAY button
            Button(
                onClick = {
                    scope.launch {
                        try {
                            error = null
                            loading = true

                            val audioUrl = vm.getLatestEpisodeAudioUrl(podcast.feedUrl)
                            if (audioUrl.isNullOrBlank()) {
                                error = "No playable audio found"
                            } else {
                                player.play(audioUrl)     // ✅ plays!
                                refresh()
                            }
                        } catch (e: Exception) {
                            error = e.message ?: "Failed to play"
                        } finally {
                            loading = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(if (playing) "Play latest (already playing)" else "Play latest episode")
            }

            OutlinedButton(
                onClick = {
                    player.pause()
                    refresh()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Pause")
            }

            OutlinedButton(
                onClick = {
                    player.stop()
                    refresh()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Stop")
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
        Text(value, color = TextPrimary, style = MaterialTheme.typography.bodySmall)
    }
}
