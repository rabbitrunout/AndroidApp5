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
    data class Details(val podcast: PodcastSummaryViewData) : Screen()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SuperPodcastTheme {
                val context = LocalContext.current

                // ✅ ONE player for whole app
                val player = remember { PlayerManager(context) }
                DisposableEffect(Unit) {
                    onDispose { player.release() }
                }

                val vm = remember { SearchViewModel() }
                var screen by remember { mutableStateOf<Screen>(Screen.Discover) }

                when (val s = screen) {
                    Screen.Discover -> DiscoverScreen(
                        onSearchClick = { screen = Screen.Search("") },
                        onCategoryClick = { cat -> screen = Screen.Search(cat.title) }
                    )

                    is Screen.Search -> SearchScreen(
                        vm = vm,
                        player = player,
                        initialTerm = s.term,
                        onBack = { screen = Screen.Discover },
                        onOpenDetails = { podcast -> screen = Screen.Details(podcast) }
                    )

                    is Screen.Details -> PodcastDetailsScreen(
                        vm = vm,
                        player = player,
                        podcast = s.podcast,
                        onBack = { screen = Screen.Search("") }
                    )
                }
            }
        }
    }
}
