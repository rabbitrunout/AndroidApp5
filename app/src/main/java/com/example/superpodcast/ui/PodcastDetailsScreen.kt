package com.example.superpodcast.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.superpodcast.model.PodcastSummaryViewData
import com.example.superpodcast.ui.theme.TextPrimary
import com.example.superpodcast.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PodcastDetailsScreen(
    vm: SearchViewModel,
    player: PlayerManager,
    holder: PlayerHolderViewModel,
    podcast: PodcastSummaryViewData,
    onBack: () -> Unit,
    onOpenEpisodes: (PodcastSummaryViewData) -> Unit
) {
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var isPlaying by remember { mutableStateOf(false) }

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

    LaunchedEffect(player) {
        while (true) {
            isPlaying = player.isPlaying()
            delay(250)
        }
    }

    fun smartPlayPause() {
        scope.launch {
            try {
                error = null

                // toggle если уже есть трек
                val currentUrl = player.currentUrl
                if (!currentUrl.isNullOrBlank()) {
                    if (player.isPlaying()) player.pause()
                    else player.play(currentUrl)
                    return@launch
                }

                // иначе грузим latest из RSS
                val feed = podcast.feedUrl?.trim().orEmpty()
                if (feed.isBlank()) {
                    error = "This podcast has no RSS feedUrl"
                    return@launch
                }

                loading = true
                val audioUrl = vm.getLatestEpisodeAudioUrl(feed)

                if (audioUrl.isNullOrBlank()) {
                    error = "No playable audio found"
                    return@launch
                }

                holder.setNowPlaying(
                    title = podcast.title,
                    author = podcast.author,
                    url = audioUrl,
                    key = feed
                )
                player.play(audioUrl)

            } catch (e: Exception) {
                error = e.message ?: "Failed to play"
            } finally {
                loading = false
            }
        }
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
        },
        bottomBar = { GlobalMiniPlayerBar(player = player, holder = holder) }
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
            InfoRow("Feed URL", if (!podcast.feedUrl.isNullOrBlank()) "Available" else "—")

            if (error != null) Text("Error: $error", color = MaterialTheme.colorScheme.error)

            Button(
                onClick = { smartPlayPause() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !loading
            ) {
                if (loading) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(10.dp))
                    Text("Loading audio…")
                } else {
                    Icon(
                        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = null
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(if (isPlaying) "Pause" else "Play latest")
                }
            }

            OutlinedButton(
                onClick = { onOpenEpisodes(podcast) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !podcast.feedUrl.isNullOrBlank()
            ) {
                Text("Open episodes list")
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
