package com.example.superpodcast.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

@Composable
fun AppNav() {
    val navController = rememberNavController()
    val context = LocalContext.current

    val player = remember { PlayerManager(context) }
    DisposableEffect(Unit) {
        onDispose { player.release() }
    }

    val searchVm: SearchViewModel = viewModel()
    val appState: AppStateViewModel = viewModel()
    val holder: PlayerHolderViewModel = viewModel() // ✅ один на всё приложение

    NavHost(
        navController = navController,
        startDestination = Routes.DISCOVER
    ) {

        composable(Routes.DISCOVER) {
            DiscoverScreen(
                player = player,
                holder = holder,
                onSearchClick = {
                    // ✅ term может быть пустым — безопасно
                    navController.navigate(Routes.search()) { launchSingleTop = true }
                },
                onCategoryClick = { cat ->
                    navController.navigate(Routes.search(cat.title)) { launchSingleTop = true }
                }
            )
        }

        composable(
            route = Routes.SEARCH_ROUTE,
            arguments = listOf(
                navArgument(Routes.ARG_TERM) {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val term = backStackEntry.arguments?.getString(Routes.ARG_TERM).orEmpty()

            SearchScreen(
                vm = searchVm,
                player = player,
                holder = holder,
                initialTerm = term,
                onBack = { navController.popBackStack() },
                onOpenDetails = { podcast ->
                    appState.putPodcast(podcast)
                    // ✅ returnTerm тоже безопасен (через query)
                    navController.navigate(Routes.details(podcast.id, returnTerm = term))
                }
            )
        }

        composable(
            route = Routes.DETAILS_ROUTE,
            arguments = listOf(
                navArgument(Routes.ARG_ID) { type = NavType.LongType },
                navArgument(Routes.ARG_TERM) {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong(Routes.ARG_ID) ?: -1L
            val podcast = appState.getPodcastById(id)

            if (podcast == null) {
                navController.popBackStack()
                return@composable
            }

            PodcastDetailsScreen(
                vm = searchVm,
                player = player,
                holder = holder,
                podcast = podcast,
                onBack = { navController.popBackStack() },
                onOpenEpisodes = { pod ->
                    appState.putPodcast(pod)
                    navController.navigate(Routes.episodes(pod.id))
                }
            )
        }

        composable(
            route = Routes.EPISODES_ROUTE,
            arguments = listOf(
                navArgument(Routes.ARG_ID) { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong(Routes.ARG_ID) ?: -1L
            val podcast = appState.getPodcastById(id)

            if (podcast == null) {
                navController.popBackStack()
                return@composable
            }

            EpisodesScreen(
                vm = searchVm,
                player = player,
                holder = holder,
                podcast = podcast,
                onBack = { navController.popBackStack() }
            )



        }
    }
}
