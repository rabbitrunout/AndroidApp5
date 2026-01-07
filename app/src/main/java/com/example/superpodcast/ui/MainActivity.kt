package com.example.superpodcast.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.example.superpodcast.ui.theme.SuperPodcastTheme

private sealed class Screen {
    data object Discover : Screen()
    data class Search(val term: String = "") : Screen()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SuperPodcastTheme {
                val vm = remember { SearchViewModel() }
                var screen: Screen by remember { mutableStateOf<Screen>(Screen.Discover) }

                when (val s = screen) {
                    Screen.Discover -> DiscoverScreen(
                        onSearchClick = { screen = Screen.Search("") },
                        onCategoryClick = { cat -> screen = Screen.Search(cat.title) }
                    )

                    is Screen.Search -> SearchScreen(
                        vm = vm,
                        initialTerm = s.term,
                        onBack = { screen = Screen.Discover }
                    )
                }
            }
        }
    }
}
