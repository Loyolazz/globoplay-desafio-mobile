package com.loyolal2.globoplaytest.ui.favorites

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.loyolal2.globoplaytest.domain.model.Movie
import com.loyolal2.globoplaytest.ui.components.MoviePosterCard

@Composable
fun FavoritesRoute(
    viewModel: FavoritesViewModel,
    onMovieClick: (Movie) -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    FavoritesScreen(
        state = state,
        onMovieClick = onMovieClick,
        onToggleFavorite = viewModel::toggleFavorite
    )
}

@Composable
fun FavoritesScreen(
    state: FavoritesUiState,
    onMovieClick: (Movie) -> Unit,
    onToggleFavorite: (Movie) -> Unit
) {
    if (state.movies.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Sua lista está vazia",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Text(
                text = "Adicione conteúdos tocando no ícone de coração",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .fillMaxWidth()
            )
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(140.dp),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(state.movies, key = { it.id }) { movie ->
                MoviePosterCard(
                    movie = movie,
                    isFavorite = true,
                    onClick = onMovieClick,
                    onToggleFavorite = onToggleFavorite
                )
            }
        }
    }
}
