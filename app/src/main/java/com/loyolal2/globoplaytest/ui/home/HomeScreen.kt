package com.loyolal2.globoplaytest.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.annotation.DrawableRes
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.loyolal2.globoplaytest.domain.model.Movie
import com.loyolal2.globoplaytest.ui.components.MoviePosterCard
import com.loyolal2.globoplaytest.ui.components.SearchBar
import com.loyolal2.globoplaytest.ui.components.SectionHeader
import com.loyolal2.globoplaytest.R

@Composable
fun HomeRoute(
    viewModel: HomeViewModel,
    isDarkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit,
    onMovieClick: (Movie) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HomeScreen(
        state = uiState,
        onMovieClick = onMovieClick,
        onToggleFavorite = viewModel::toggleFavorite,
        onRefresh = viewModel::refresh,
        onSearchQueryChange = viewModel::onSearchQueryChanged,
        onShowOnlyGloboChange = viewModel::onShowOnlyGloboChanged,
        isDarkTheme = isDarkTheme,
        onThemeChange = onThemeChange
    )
}

@Composable
fun HomeScreen(
    state: HomeUiState,
    onMovieClick: (Movie) -> Unit,
    onToggleFavorite: (Movie) -> Unit,
    onRefresh: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onShowOnlyGloboChange: (Boolean) -> Unit,
    isDarkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {

        // ✅ Saver baseado em lista (compatível com todas as versões)
        val textFieldValueSaver: Saver<MutableState<TextFieldValue>, Any> = listSaver(
            save = { state ->
                val v = state.value
                // Mapeia para tipos "savables"
                listOf(v.text, v.selection.start, v.selection.end)
            },
            restore = { list ->
                val text = list[0] as String
                val start = list[1] as Int
                val end = list[2] as Int
                mutableStateOf(TextFieldValue(text, TextRange(start, end)))
            }
        )

        var searchField by rememberSaveable(
            state.searchQuery,            // chave para reiniciar quando a query externa mudar
            saver = textFieldValueSaver
        ) {
            mutableStateOf(TextFieldValue(state.searchQuery))
        }

        HomeHeader(
            isDarkTheme = isDarkTheme,
            onThemeChange = onThemeChange
        )

        SearchBar(
            value = searchField,
            onValueChange = {
                searchField = it
                onSearchQueryChange(it.text)
            },
            modifier = Modifier.padding(top = 16.dp, bottom = 12.dp)
        )

        GloboFilterRow(
            showOnlyGlobo = state.showOnlyGlobo,
            onShowOnlyGloboChange = onShowOnlyGloboChange
        )

        val filteredSections = remember(state.sections, state.showOnlyGlobo) {
            if (state.showOnlyGlobo) {
                state.sections
                    .map { section ->
                        section.copy(movies = section.movies.filter { it.isFromGlobo })
                    }
                    .filter { it.movies.isNotEmpty() }
            } else {
                state.sections
            }
        }

        val filteredResults = remember(state.searchResults, state.showOnlyGlobo) {
            if (state.showOnlyGlobo) state.searchResults.filter { it.isFromGlobo } else state.searchResults
        }

        state.errorMessage?.takeIf { filteredSections.isEmpty() && filteredResults.isEmpty() }?.let { error ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Button(onClick = onRefresh) {
                    Text(text = "Tentar novamente")
                }
            }
        }

        if (!state.isLoading && state.errorMessage == null && state.showOnlyGlobo &&
            filteredSections.isEmpty() && filteredResults.isEmpty()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Nenhum conteúdo da Globo encontrado para esta seção.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }
        }

        if (state.isSearching) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        when {
            filteredResults.isNotEmpty() -> {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(140.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredResults, key = { it.id }) { movie ->
                        MoviePosterCard(
                            movie = movie,
                            isFavorite = state.favorites.contains(movie.id),
                            onClick = onMovieClick,
                            onToggleFavorite = onToggleFavorite
                        )
                    }
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(filteredSections, key = { it.id }) { section ->
                        SectionHeader(title = section.title)
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(section.movies, key = { it.id }) { movie ->
                                MoviePosterCard(
                                    movie = movie,
                                    isFavorite = state.favorites.contains(movie.id),
                                    onClick = onMovieClick,
                                    onToggleFavorite = onToggleFavorite
                                )
                            }
                        }
                        HorizontalDivider(
                            modifier = Modifier
                                .padding(vertical = 16.dp)
                                .height(1.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }

                    if (filteredSections.isEmpty() && filteredResults.isEmpty() && !state.isLoading) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Nenhum conteúdo disponível no momento",
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    if (state.isLoading && state.sections.isEmpty()) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeHeader(
    isDarkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "globoplay",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )
        ThemeToggle(isDarkTheme = isDarkTheme, onThemeChange = onThemeChange)
    }
}

@Composable
private fun ThemeToggle(
    isDarkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ModeChip(
            selected = isDarkTheme,
            label = "Dark",
            iconRes = R.drawable.ic_dark_mode,
            onClick = { onThemeChange(true) }
        )
        ModeChip(
            selected = !isDarkTheme,
            label = "White",
            iconRes = R.drawable.ic_light_mode,
            onClick = { onThemeChange(false) }
        )
    }
}

@Composable
private fun ModeChip(
    selected: Boolean,
    label: String,
    @DrawableRes iconRes: Int,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text = label) },
        leadingIcon = {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            labelColor = MaterialTheme.colorScheme.onSurface,
            iconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
            selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}

@Composable
private fun GloboFilterRow(
    showOnlyGlobo: Boolean,
    onShowOnlyGloboChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Filtros",
            style = MaterialTheme.typography.titleMedium
        )
        FilterChip(
            selected = showOnlyGlobo,
            onClick = { onShowOnlyGloboChange(!showOnlyGlobo) },
            label = { Text(text = "Somente Globo") },
            leadingIcon = if (showOnlyGlobo) {
                {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_favorite),
                        contentDescription = null
                    )
                }
            } else null,
            colors = FilterChipDefaults.filterChipColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                labelColor = MaterialTheme.colorScheme.onSurface,
                iconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                selectedContainerColor = MaterialTheme.colorScheme.secondary,
                selectedLabelColor = MaterialTheme.colorScheme.onSecondary,
                selectedLeadingIconColor = MaterialTheme.colorScheme.onSecondary
            )
        )
    }
}
