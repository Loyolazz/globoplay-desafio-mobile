package com.loyolal2.globoplaytest.ui

import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import androidx.lifecycle.viewmodel.compose.viewModel
import com.loyolal2.globoplaytest.domain.model.MediaType
import com.loyolal2.globoplaytest.domain.repository.MovieRepository
import com.loyolal2.globoplaytest.ui.detail.MovieDetailRoute
import com.loyolal2.globoplaytest.ui.favorites.FavoritesRoute
import com.loyolal2.globoplaytest.ui.home.HomeRoute
import com.loyolal2.globoplaytest.ui.navigation.GloboplayDestination
import com.loyolal2.globoplaytest.ui.splash.SplashScreen

@Composable
fun GloboplayApp(
    repository: MovieRepository,
    onPlayVideo: (String) -> Unit,
    isDarkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit
) {
    val navController = rememberNavController()
    val factory = remember { AppViewModelFactory(repository) }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = when {
        currentRoute == null -> false
        currentRoute.startsWith(GloboplayDestination.DetailsRoute.substringBefore("/")) -> false
        currentRoute == GloboplayDestination.SplashRoute -> false
        else -> true
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavigationBar(navController = navController)
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = GloboplayDestination.SplashRoute,
            modifier = Modifier.padding(padding)
        ) {
            composable(GloboplayDestination.SplashRoute) {
                SplashScreen {
                    navController.navigate(GloboplayDestination.Home.route) {
                        popUpTo(GloboplayDestination.SplashRoute) { inclusive = true }
                    }
                }
            }

            composable(GloboplayDestination.Home.route) {
                val viewModel: com.loyolal2.globoplaytest.ui.home.HomeViewModel = viewModel(factory = factory)
                HomeRoute(
                    viewModel = viewModel,
                    isDarkTheme = isDarkTheme,
                    onThemeChange = onThemeChange
                ) { movie ->
                    navController.navigate("details/${movie.mediaType.name}/${movie.id}")
                }
            }

            composable(GloboplayDestination.Favorites.route) {
                val viewModel: com.loyolal2.globoplaytest.ui.favorites.FavoritesViewModel = viewModel(factory = factory)
                FavoritesRoute(viewModel = viewModel) { movie ->
                    navController.navigate("details/${movie.mediaType.name}/${movie.id}")
                }
            }

            composable(
                route = GloboplayDestination.DetailsRoute,
                arguments = listOf(
                    navArgument(GloboplayDestination.DetailsMediaTypeArgument) { type = NavType.StringType },
                    navArgument(GloboplayDestination.DetailsIdArgument) { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val movieId = backStackEntry.arguments?.getInt(GloboplayDestination.DetailsIdArgument) ?: return@composable
                val mediaTypeName = backStackEntry.arguments?.getString(GloboplayDestination.DetailsMediaTypeArgument) ?: return@composable
                val mediaType = runCatching { MediaType.valueOf(mediaTypeName) }.getOrNull() ?: return@composable
                val viewModel: com.loyolal2.globoplaytest.ui.detail.MovieDetailViewModel = viewModel(factory = factory)
                MovieDetailRoute(
                    viewModel = viewModel,
                    movieId = movieId,
                    mediaType = mediaType,
                    onBack = { navController.popBackStack() },
                    onPlayVideo = onPlayVideo,
                    onOpenContent = { recommended ->
                        navController.navigate("details/${recommended.mediaType.name}/${recommended.id}") {
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun BottomNavigationBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val bottomDestinations = GloboplayDestination.bottomDestinations
        .filterIsInstance<GloboplayDestination>()

    NavigationBar {
        bottomDestinations.forEach { destination ->
            val selected = currentDestination?.route == destination.route
            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(destination.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    androidx.compose.material3.Icon(
                        painter = painterResource(id = destination.iconRes),
                        contentDescription = destination.label
                    )
                },
                label = { Text(text = destination.label) }
            )
        }
    }
}
