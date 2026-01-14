package com.example.superpodcast.ui

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.superpodcast.model.PodcastSummaryViewData
import com.example.superpodcast.ui.theme.Cocoa
import com.example.superpodcast.ui.theme.TextPrimary
import com.example.superpodcast.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    vm: SearchViewModel,
    player: PlayerManager,
    initialTerm: String = "",
    onBack: () -> Unit = {},
    onOpenDetails: (PodcastSummaryViewData) -> Unit
) {
    var term by remember { mutableStateOf(initialTerm) }
    var regex by remember { mutableStateOf(".*") }
    var minWordsText by remember { mutableStateOf("1") }

    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var results by remember { mutableStateOf<List<PodcastSummaryViewData>>(emptyList()) }

    // ✅ state that связывает плеер и список
    var nowTitle by remember { mutableStateOf<String?>(null) }
    var nowAuthor by remember { mutableStateOf<String?>(null) }
    var nowFeedUrl by remember { mutableStateOf<String?>(null) }

    var isPlaying by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // ✅ автоподдержка isPlaying (чтобы список обновлялся сам)
    LaunchedEffect(nowFeedUrl) {
        while (true) {
            isPlaying = player.isPlaying()
            delay(350)
        }
    }

    fun runSearch() {
        val q = term.trim()
        if (q.isEmpty()) {
            error = "Enter a search term"
            return
        }

        val safeRegex = regex.trim().ifEmpty { ".*" }
        val minWords = minWordsText.toIntOrNull() ?: 1

        loading = true
        error = null

        scope.launch {
            try {
                results = vm.search(q, safeRegex, minWords)
            } catch (e: Exception) {
                error = e.message ?: "Unknown error"
            } finally {
                loading = false
            }
        }
    }

    fun playLatest(feedUrl: String, title: String, author: String) {
        scope.launch {
            try {
                error = null
                loading = true

                val audioUrl = vm.getLatestEpisodeAudioUrl(feedUrl)
                if (audioUrl.isNullOrBlank()) {
                    error = "No playable audio found for this podcast"
                } else {
                    // ✅ важно: обновляем nowFeedUrl (это и есть “какой подкаст играет”)
                    nowFeedUrl = feedUrl
                    nowTitle = title
                    nowAuthor = author

                    player.play(audioUrl)
                }
            } catch (e: Exception) {
                error = e.message ?: "Failed to play"
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(initialTerm) {
        if (initialTerm.isNotBlank()) runSearch()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Search") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { runSearch() }) {
                        Icon(Icons.Filled.Search, contentDescription = "Search")
                    }
                }
            )
        },
        bottomBar = {
            if (nowFeedUrl != null && nowTitle != null) {
                MiniPlayerBar(
                    title = nowTitle ?: "",
                    author = nowAuthor ?: "",
                    playing = isPlaying,
                    onPlayPause = {
                        if (player.isPlaying()) player.pause()
                        else {
                            val url = player.currentUrl
                            if (!url.isNullOrBlank()) player.play(url)
                            else playLatest(nowFeedUrl!!, nowTitle ?: "", nowAuthor ?: "")
                        }
                    },
                    onStop = {
                        player.stop()
                        nowFeedUrl = null
                        nowTitle = null
                        nowAuthor = null
                    }
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = term,
                onValueChange = { term = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Search term") },
                singleLine = true
            )

            OutlinedTextField(
                value = regex,
                onValueChange = { regex = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Regex filter (optional)") },
                singleLine = true
            )

            OutlinedTextField(
                value = minWordsText,
                onValueChange = { minWordsText = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Min words in title") },
                singleLine = true
            )

            if (loading) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())

            if (error != null) {
                Text("Error: $error", color = MaterialTheme.colorScheme.error)
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(results) { item ->
                    val canPlay = item.feedUrl.isNotBlank()
                    val isThisPlaying = (nowFeedUrl == item.feedUrl && isPlaying)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { onOpenDetails(item) },
                                onLongClick = {
                                    if (canPlay) playLatest(item.feedUrl, item.title, item.author)
                                    else error = "This item has no feedUrl (can't play)"
                                }
                            )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = TextPrimary
                                )
                                Text(
                                    text = item.author,
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = TextSecondary
                                )

                                if (isThisPlaying) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Filled.Equalizer,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            text = "Playing",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                } else {
                                    Text(
                                        text = "Tap = details • Long press = play",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                            }

                            IconButton(
                                enabled = canPlay,
                                onClick = { playLatest(item.feedUrl, item.title, item.author) }
                            ) {
                                Icon(Icons.Filled.PlayArrow, contentDescription = "Play")
                            }
                        }
                    }
                }

                item { Spacer(Modifier.height(64.dp)) }
            }
        }
    }
}

@Composable
private fun MiniPlayerBar(
    title: String,
    author: String,
    playing: Boolean,
    onPlayPause: () -> Unit,
    onStop: () -> Unit
) {
    Surface(color = Cocoa, tonalElevation = 6.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = author,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(Modifier.width(8.dp))

            IconButton(onClick = onPlayPause) {
                Icon(
                    imageVector = if (playing) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = "Play/Pause",
                    tint = TextPrimary
                )
            }

            IconButton(onClick = onStop) {
                Icon(
                    imageVector = Icons.Filled.Stop,
                    contentDescription = "Stop",
                    tint = TextPrimary
                )
            }
        }
    }
}
