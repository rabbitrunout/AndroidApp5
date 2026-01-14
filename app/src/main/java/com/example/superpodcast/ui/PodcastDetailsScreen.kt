package com.example.superpodcast.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
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
    podcast: PodcastSummaryViewData,
    onBack: () -> Unit,
    onOpenEpisodes: (PodcastSummaryViewData) -> Unit
) {
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    // локальное состояние UI (обновляем таймером)
    var isPlaying by remember { mutableStateOf(false) }
    var hasTrack by remember { mutableStateOf(false) } // есть ли текущий url у плеера

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

    // ✅ авто-обновление состояния плеера (чтобы кнопки менялись сразу)
    LaunchedEffect(Unit) {
        while (true) {
            isPlaying = player.isPlaying()
            hasTrack = !player.currentUrl.isNullOrBlank()
            delay(250)
        }
    }

    // ✅ SMART play/pause:
    // 1) если уже есть currentUrl -> toggle play/pause
    // 2) если нет -> загрузи latest episode и play
    fun smartPlayPause() {
        scope.launch {
            try {
                error = null

                // если уже есть трек, просто toggle
                if (!player.currentUrl.isNullOrBlank()) {
                    if (player.isPlaying()) player.pause()
                    else player.play(player.currentUrl!!)
                    return@launch
                }

                // иначе получаем latest audio и play
                loading = true
                val audioUrl = vm.getLatestEpisodeAudioUrl(podcast.feedUrl)
                if (audioUrl.isNullOrBlank()) {
                    error = "No playable audio found"
                } else {
                    player.play(audioUrl)
                }
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

            if (error != null) {
                Text("Error: $error", color = MaterialTheme.colorScheme.error)
            }

            // ✅ вместо LinearProgressIndicator — показываем состояние прямо на кнопке
            Button(
                onClick = { smartPlayPause() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !loading
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
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

            // ✅ episodes list
            OutlinedButton(
                onClick = { onOpenEpisodes(podcast) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Open episodes list")
            }

            // ✅ Stop — отдельной маленькой кнопкой (и только если что-то уже загружено)
            if (hasTrack) {
                OutlinedButton(
                    onClick = { player.stop() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Stop, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Stop")
                }
            }

            // ✅ Waveform/Seek показываем только когда есть трек (иначе будет 0:00 и пусто)
            if (hasTrack) {
                PlayerWaveformBar(
                    player = player,
                    modifier = Modifier.fillMaxWidth()
                )
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
