package com.example.superpodcast.ui

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.superpodcast.model.PodcastSummaryViewData
import com.example.superpodcast.ui.theme.TextPrimary
import com.example.superpodcast.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    vm: SearchViewModel,
    player: PlayerManager,
    holder: PlayerHolderViewModel,
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

    val scope = rememberCoroutineScope()

    // ✅ глобальное состояние "что сейчас играет"
    val nowPlaying by holder.nowPlaying.collectAsState()

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
                results = vm.search(q, safeRegex, minWords, onlyPlayable = false)
                if (results.isEmpty()) error = "No results found."

            } catch (e: Exception) {
                error = e.message ?: "Unknown error"
            } finally {
                loading = false
            }
        }
    }

    suspend fun playLatest(item: PodcastSummaryViewData, itemKey: String, feed: String) {
        val audioUrl = vm.getLatestEpisodeAudioUrl(feed)
        if (audioUrl.isNullOrBlank()) {
            error = "No playable audio found"
            return
        }

        // ✅ сохраняем current key + playing state
        holder.setNowPlaying(
            title = item.title,
            author = item.author,
            url = audioUrl,
            key = itemKey,
            isPlaying = true
        )
        player.play(audioUrl)
    }

    fun quickPlayOrToggle(item: PodcastSummaryViewData) {
        scope.launch {
            try {
                error = null
                loading = true

                val feed = item.feedUrl?.trim().orEmpty()
                if (feed.isBlank()) {
                    error = "No RSS for this podcast"
                    return@launch
                }

                // ✅ стабильный ключ: feedUrl, иначе id (на случай странных данных)
                val itemKey = feed.ifBlank { "id:${item.id}" }

                val isThisItemPlaying = nowPlaying.isPlaying && nowPlaying.key == itemKey

                if (isThisItemPlaying && player.isPlaying()) {
                    // ✅ пауза только для текущего
                    player.pause()
                    holder.setPlaying(false)
                } else {
                    // ✅ запускаем именно этот подкаст
                    // если уже есть url — можно продолжить, иначе берем последний эпизод
                    val currentUrl = nowPlaying.url
                    if (!currentUrl.isNullOrBlank() && nowPlaying.key == itemKey) {
                        player.play(currentUrl)
                        holder.setPlaying(true)
                    } else {
                        playLatest(item, itemKey, feed)
                    }
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
        bottomBar = { GlobalMiniPlayerBar(player = player, holder = holder) }
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
            if (error != null) Text("Error: $error", color = MaterialTheme.colorScheme.error)

            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(results, key = { it.id }) { item ->
                    val feed = item.feedUrl?.trim().orEmpty()
                    val playable = feed.isNotBlank()

                    // ✅ ключ элемента + "играет ли именно он"
                    val itemKey = if (feed.isNotBlank()) feed else "id:${item.id}"
                    val isThisItemPlaying = nowPlaying.isPlaying && nowPlaying.key == itemKey

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(
                                onClick = { onOpenDetails(item) },
                                onLongClick = { if (playable) quickPlayOrToggle(item) }
                            ),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
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
                                Text(
                                    text = if (playable) "Tap = details • Long press = play" else "Not playable (no RSS)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }

                            if (playable) {
                                Icon(
                                    imageVector = if (isThisItemPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                item { Spacer(Modifier.height(72.dp)) }
            }
        }
    }
}
