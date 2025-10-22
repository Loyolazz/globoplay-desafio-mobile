package com.loyolal2.globoplaytest.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.loyolal2.globoplaytest.R
import com.loyolal2.globoplaytest.domain.model.MediaType
import com.loyolal2.globoplaytest.domain.model.Movie
import com.loyolal2.globoplaytest.domain.model.MovieDetail
import java.util.Locale

@Composable
fun MovieDetailRoute(
    viewModel: MovieDetailViewModel,
    movieId: Int,
    mediaType: MediaType,
    onBack: () -> Unit,
    onPlayVideo: (String) -> Unit,
    onOpenContent: (Movie) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(movieId, mediaType) {
        viewModel.load(movieId, mediaType)
    }

    uiState.errorMessage?.let { message ->
        LaunchedEffect(message) {
            snackbarHostState.showSnackbar(message)
        }
    }

    MovieDetailScreen(
        state = uiState,
        snackbarHostState = snackbarHostState,
        onBack = onBack,
        onToggleFavorite = viewModel::toggleFavorite,
        onPlayVideo = onPlayVideo,
        onOpenContent = onOpenContent
    )
}

@Composable
fun MovieDetailScreen(
    state: MovieDetailUiState,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
    onPlayVideo: (String) -> Unit,
    onOpenContent: (Movie) -> Unit
) {
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            state.detail == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Não encontramos os detalhes do conteúdo")
                }
            }

            else -> {
                DetailContent(
                    detail = state.detail,
                    isFavorite = state.isFavorite,
                    modifier = Modifier.padding(padding),
                    onBack = onBack,
                    onToggleFavorite = onToggleFavorite,
                    onPlayVideo = onPlayVideo,
                    onOpenContent = onOpenContent
                )
            }
        }
    }
}

@Composable
private fun DetailContent(
    detail: MovieDetail,
    isFavorite: Boolean,
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
    onPlayVideo: (String) -> Unit,
    onOpenContent: (Movie) -> Unit
) {
    val movie = detail.movie
    val scrollState = rememberScrollState()
    var selectedTab by rememberSaveable { mutableStateOf(0) }
    val tabs = listOf("ASSISTA TAMBÉM", "DETALHES")
    val runtimeLabel = if (movie.mediaType == MediaType.TV) "Duração média" else "Duração"
    val releaseLabel = if (movie.mediaType == MediaType.TV) "Estreia" else "Lançamento"

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(movie.backdropUrl ?: movie.posterUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = movie.title,
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = R.drawable.ic_poster_placeholder),
                error = painterResource(id = R.drawable.ic_poster_placeholder),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                            startY = 120f
                        )
                    )
            )
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.TopStart)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_back),
                    contentDescription = "Voltar"
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Text(
                    text = movie.title,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = movie.category.displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                detail.genres.takeIf { it.isNotEmpty() }?.let { genres ->
                    Text(
                        text = genres.joinToString(separator = " • "),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        Column(modifier = Modifier.padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = {
                        detail.videoKey?.let(onPlayVideo)
                    },
                    enabled = detail.videoKey != null
                ) {
                    Text(text = "Assista")
                }
                OutlinedButton(onClick = onToggleFavorite) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_favorite),
                        contentDescription = null,
                        tint = if (isFavorite) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isFavorite) "Remover" else "Minha lista",
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }

            if (detail.videoKey == null) {
                Text(
                    text = "Trailer indisponível no momento.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Sinopse",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = movie.overview.ifBlank { "Sinopse não disponível." },
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(text = title) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            when (selectedTab) {
                0 -> {
                    if (detail.recommendations.isEmpty()) {
                        Text(
                            text = "Sem recomendações disponíveis no momento.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            items(detail.recommendations, key = { it.id }) { recommendation ->
                                RecommendationCard(
                                    movie = recommendation,
                                    onClick = onOpenContent
                                )
                            }
                        }
                    }
                }

                else -> {
                    DetailInfoRow(label = "Título original", value = detail.originalTitle ?: "-")
                    DetailInfoRow(label = releaseLabel, value = movie.releaseDate ?: "-")
                    detail.seasonCount?.let {
                        DetailInfoRow(label = "Temporadas", value = it.toString())
                    }
                    detail.episodeCount?.let {
                        DetailInfoRow(label = "Episódios", value = it.toString())
                    }
                    DetailInfoRow(
                        label = runtimeLabel,
                        value = detail.runtimeMinutes?.let { "$it min" } ?: "-"
                    )
                    DetailInfoRow(
                        label = "Avaliação",
                        value = "${"%.1f".format(movie.voteAverage)} / 10"
                    )
                    DetailInfoRow(
                        label = "Disponível na Globo?",
                        value = if (movie.isFromGlobo) "Sim" else "Não"
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailInfoRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun RecommendationCard(
    movie: Movie,
    onClick: (Movie) -> Unit
) {
    Card(
        modifier = Modifier
            .width(140.dp)
            .clickable { onClick(movie) },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(movie.posterUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = movie.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = R.drawable.ic_poster_placeholder),
                error = painterResource(id = R.drawable.ic_poster_placeholder)
            )
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = movie.category.displayName.uppercase(Locale.getDefault()),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = movie.title,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
