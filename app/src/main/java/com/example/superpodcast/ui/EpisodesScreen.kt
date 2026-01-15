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
    holder: PlayerHolderViewModel,
    podcast: PodcastSummaryViewData,
    onBack: () -> Unit
) {
    var episodes by remember { mutableStateOf<List<EpisodeUi>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val now by holder.nowPlaying.collectAsState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(player, holder) {
        player.onPlayingChanged = { playing -> holder.setPlaying(playing) }
        player.onEnded = { holder.playNextEpisode(player) }
        player.onError = { holder.playNextEpisode(player) }
    }

    LaunchedEffect(podcast.id) {
        val feed = podcast.feedUrl?.trim().orEmpty()
        if (feed.isBlank()) {
            error = "This podcast has no playable RSS feedUrl"
            episodes = emptyList()
            return@LaunchedEffect
        }

        loading = true
        error = null
        try {
            episodes = vm.getEpisodes(feed)
            if (episodes.isEmpty()) error = "No episodes found"

            if (episodes.isNotEmpty()) {
                holder.setEpisodeQueue(episodes, startIndex = 0)
            }
        } catch (e: Exception) {
            error = e.message ?: "Failed to load episodes"
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

            if (loading) LinearProgressIndicator(Modifier.fillMaxWidth())
            if (error != null) Text("Error: $error", color = MaterialTheme.colorScheme.error)

            Spacer(Modifier.height(8.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(episodes, key = { it.id }) { ep ->
                    val selected = ep.id == now.episodeId
                    val playingNow = selected && now.isPlaying

                    Card {
                        Column(Modifier.padding(12.dp)) {
                            Text(
                                text = ep.title,
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(ep.pubDateText, style = MaterialTheme.typography.bodySmall)

                            Spacer(Modifier.height(8.dp))

                            Row {
                                FilledTonalButton(onClick = {
                                    scope.launch {
                                        val url = ep.audioUrl.trim()
                                        if (url.isBlank()) return@launch

                                        val startIndex = episodes.indexOfFirst { it.id == ep.id }.coerceAtLeast(0)
                                        holder.setEpisodeQueue(episodes, startIndex)

                                        val podcastKey = podcast.feedUrl?.trim()
                                            .takeIf { !it.isNullOrBlank() } ?: "id:${podcast.id}"

                                        holder.setNowPlaying(
                                            title = podcast.title,
                                            author = podcast.author,
                                            url = url,
                                            key = podcastKey,
                                            isPlaying = true,
                                            episodeId = ep.id,
                                            episodeTitle = ep.title
                                        )

                                        player.play(url, key = ep.id)
                                    }
                                }) {
                                    Icon(
                                        imageVector = if (playingNow) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                        contentDescription = null
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(if (playingNow) "Pause" else "Play")
                                }

                                Spacer(Modifier.width(10.dp))

                                OutlinedButton(onClick = {
                                    player.stop()
                                    holder.setPlaying(false)
                                }) {
                                    Icon(Icons.Filled.Stop, contentDescription = null)
                                    Spacer(Modifier.width(4.dp))
                                    Text("Stop")
                                }
                            }

                            if (selected) {
                                EpisodeProgressBar(
                                    player = player,
                                    episodeId = ep.id,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
