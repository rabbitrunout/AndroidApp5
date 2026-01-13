package com.example.superpodcast.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.example.superpodcast.model.PodcastSummaryViewData
import com.example.superpodcast.ui.theme.SuperPodcastTheme

private sealed class Screen {
    data object Discover : Screen()
    data class Search(val term: String = "") : Screen()
    data class Details(val podcast: PodcastSummaryViewData, val returnTerm: String = "") : Screen()
    data class Episodes(val podcast: PodcastSummaryViewData) : Screen()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SuperPodcastTheme {
                val context = LocalContext.current

                // ✅ one shared player
                val player = remember { PlayerManager(context) }
                DisposableEffect(Unit) {
                    onDispose { player.release() }
                }

                val vm = remember { SearchViewModel() }

                var lastSearchTerm by remember { mutableStateOf("") }
                var screen by remember { mutableStateOf<Screen>(Screen.Discover) }

                when (val s = screen) {
                    Screen.Discover -> DiscoverScreen(
                        onSearchClick = { screen = Screen.Search("") },
                        onCategoryClick = { cat -> screen = Screen.Search(cat.title) }
                    )

                    is Screen.Search -> {
                        // запоминаем последний term (чтобы красиво вернуться)
                        lastSearchTerm = s.term
                        SearchScreen(
                            vm = vm,
                            player = player,
                            initialTerm = s.term,
                            onBack = { screen = Screen.Discover },
                            onOpenDetails = { podcast ->
                                screen = Screen.Details(podcast = podcast, returnTerm = s.term)
                            }
                        )
                    }

                    is Screen.Details -> PodcastDetailsScreen(
                        vm = vm,
                        player = player,
                        podcast = s.podcast,
                        onBack = { screen = Screen.Search(s.returnTerm) }, // назад в search с тем же term
                        onOpenEpisodes = { pod ->
                            screen = Screen.Episodes(pod)
                        }
                    )

                    is Screen.Episodes -> EpisodesScreen(
                        vm = vm,
                        player = player,
                        podcast = s.podcast,
                        onBack = { screen = Screen.Details(s.podcast, returnTerm = lastSearchTerm) }
                    )
                }
            }
        }
    }
}
