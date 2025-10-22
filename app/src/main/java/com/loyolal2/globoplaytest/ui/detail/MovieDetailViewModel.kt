package com.loyolal2.globoplaytest.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.loyolal2.globoplaytest.domain.model.MediaType
import com.loyolal2.globoplaytest.domain.model.MovieDetail
import com.loyolal2.globoplaytest.domain.repository.MovieRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class MovieDetailUiState(
    val isLoading: Boolean = true,
    val detail: MovieDetail? = null,
    val isFavorite: Boolean = false,
    val errorMessage: String? = null
)

class MovieDetailViewModel(
    private val repository: MovieRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MovieDetailUiState())
    val uiState: StateFlow<MovieDetailUiState> = _uiState.asStateFlow()

    private var observeFavoritesJob: Job? = null

    fun load(movieId: Int, mediaType: MediaType) {
        _uiState.value = MovieDetailUiState(isLoading = true)
        observeFavorites(movieId, mediaType)
        viewModelScope.launch {
            repository.getContentDetails(movieId, mediaType)
                .onSuccess { detail ->
                    _uiState.value = _uiState.value.copy(
                        detail = detail,
                        isLoading = false,
                        errorMessage = null
                    )
                }
                .onFailure { throwable ->
                    _uiState.value = _uiState.value.copy(
                        detail = null,
                        isLoading = false,
                        errorMessage = throwable.message ?: "Não foi possível carregar os detalhes"
                    )
                }
        }
    }

    fun toggleFavorite() {
        val movie = _uiState.value.detail?.movie ?: return
        viewModelScope.launch {
            repository.toggleFavorite(movie)
        }
    }

    private fun observeFavorites(movieId: Int, mediaType: MediaType) {
        observeFavoritesJob?.cancel()
        observeFavoritesJob = viewModelScope.launch {
            repository.isFavorite(movieId, mediaType).collectLatest { isFavorite ->
                _uiState.value = _uiState.value.copy(isFavorite = isFavorite)
            }
        }
    }
}
