package com.example.superpodcast.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.superpodcast.model.EpisodeUi
import com.example.superpodcast.model.PodcastSummaryViewData
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EpisodesScreen(
    vm: SearchViewModel,
    player: PlayerManager,
    podcast: PodcastSummaryViewData,
    onBack: () -> Unit
) {
    var episodes by remember { mutableStateOf<List<EpisodeUi>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    var selectedEpisodeId by remember { mutableStateOf<String?>(null) }
    var isPlaying by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    fun refresh() {
        isPlaying = player.isPlaying()
    }

    LaunchedEffect(podcast.feedUrl) {
        loading = true
        error = null
        try {
            episodes = vm.getEpisodes(podcast.feedUrl)
            if (episodes.isEmpty()) error = "No episodes found"
        } catch (e: Exception) {
            error = e.message
        } finally {
            loading = false
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Episodes") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {

            Text(
                podcast.title,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(8.dp))

            if (loading) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            if (error != null) {
                Spacer(Modifier.height(6.dp))
                Text("Error: $error", color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(8.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(episodes) { ep ->
                    val selected = ep.id == selectedEpisodeId
                    val playingNow = selected && isPlaying

                    EpisodeCard(
                        episode = ep,
                        player = player,          // ✅ важно
                        playing = playingNow,
                        onPlayPause = {
                            scope.launch {
                                if (selected) {
                                    if (player.isPlaying()) player.pause()
                                    else player.play(player.currentUrl ?: ep.audioUrl)
                                } else {
                                    selectedEpisodeId = ep.id
                                    player.play(ep.audioUrl, key = ep.id)
                                }
                                refresh()
                            }
                        },
                        onStop = {
                            player.stop()
                            selectedEpisodeId = null
                            refresh()
                        }
                    )
                }

                item { Spacer(Modifier.height(8.dp)) }
            }
        }
    }
}

@Composable
private fun EpisodeCard(
    episode: EpisodeUi,
    player: PlayerManager,            // ✅ добавили
    playing: Boolean,
    onPlayPause: () -> Unit,
    onStop: () -> Unit
) {
    Card {
        Column(Modifier.padding(12.dp)) {

            Text(
                text = episode.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = episode.pubDateText,
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = episode.description,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(8.dp))

            Row {
                FilledTonalButton(onClick = onPlayPause) {
                    Icon(
                        imageVector = if (playing) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = null
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(if (playing) "Pause" else "Play")
                }

                Spacer(Modifier.width(10.dp))

                OutlinedButton(onClick = onStop) {
                    Icon(Icons.Filled.Stop, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Stop")
                }
            }

            // ✅ Прогресс бар внутри карточки
            EpisodeProgressBar(
                player = player,
                episodeId = episode.id,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )
        }
    }
}
