package com.example.superpodcast.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.superpodcast.model.PodcastSummaryViewData

@Composable
fun PodcastDetailsRoute(
    vm: SearchViewModel,
    player: PlayerManager,
    podcastId: Long,
    returnTerm: String,
    onBack: () -> Unit,
    onOpenEpisodes: (PodcastSummaryViewData) -> Unit,
    appState: AppStateViewModel
) {
    val podcast = remember(podcastId) { appState.getPodcastById(podcastId) }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when {
            podcast == null -> Text(
                text = "Podcast not found. Open it from Search again.",
                color = MaterialTheme.colorScheme.error
            )
            else -> PodcastDetailsScreen(
                vm = vm,
                player = player,
                podcast = podcast,
                onBack = onBack,
                onOpenEpisodes = onOpenEpisodes,
                holder = TODO()
            )
        }
    }
}
