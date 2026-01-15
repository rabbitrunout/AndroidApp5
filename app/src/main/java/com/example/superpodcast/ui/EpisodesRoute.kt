package com.example.superpodcast.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.superpodcast.model.PodcastSummaryViewData

@Composable
fun EpisodesRoute(
    vm: SearchViewModel,
    player: PlayerManager,
    holder: PlayerHolderViewModel,     // ✅ ДОБАВИЛИ
    podcastId: Long,
    onBack: () -> Unit,
    appState: AppStateViewModel = viewModel()
) {
    val loading = remember { mutableStateOf(true) }
    val error = remember { mutableStateOf<String?>(null) }
    val podcast = remember { mutableStateOf<PodcastSummaryViewData?>(null) }

    LaunchedEffect(podcastId) {
        loading.value = true
        error.value = null
        podcast.value = null

        val p = appState.getPodcastById(podcastId)
        if (p == null) {
            error.value = "Podcast not found (open it from Search again)"
        } else {
            podcast.value = p
        }

        loading.value = false
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when {
            loading.value -> CircularProgressIndicator()
            error.value != null -> Text(
                text = "Error: ${error.value}",
                color = MaterialTheme.colorScheme.error
            )
            podcast.value != null -> EpisodesScreen(
                vm = vm,
                player = player,
                holder = holder,              // ✅ ВАЖНО
                podcast = podcast.value!!,
                onBack = onBack
            )
        }
    }
}
