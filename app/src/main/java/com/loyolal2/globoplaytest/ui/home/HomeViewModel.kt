package com.loyolal2.globoplaytest.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.loyolal2.globoplaytest.domain.model.Movie
import com.loyolal2.globoplaytest.domain.model.MovieSection
import com.loyolal2.globoplaytest.domain.repository.MovieRepository
import java.util.Locale
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = true,
    val sections: List<MovieSection> = emptyList(),
    val favorites: Set<Int> = emptySet(),
    val errorMessage: String? = null,
    val searchQuery: String = "",
    val searchResults: List<Movie> = emptyList(),
    val isSearching: Boolean = false,
    val showOnlyGlobo: Boolean = false
)

class HomeViewModel(
    private val repository: MovieRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val searchQuery = MutableStateFlow("")
    private var searchJob: Job? = null

    init {
        observeFavorites()
        loadSections()
        observeSearch()
    }

    fun refresh() {
        loadSections()
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        searchQuery.value = query
    }

    fun toggleFavorite(movie: Movie) {
        viewModelScope.launch {
            repository.toggleFavorite(movie)
        }
    }

    private fun loadSections() {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            repository.getHomeSections()
                .onSuccess { sections ->
                    _uiState.value = _uiState.value.copy(
                        sections = sections,
                        isLoading = false,
                        errorMessage = null
                    )
                }
                .onFailure { throwable ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = throwable.message ?: "Não foi possível carregar os conteúdos"
                    )
                }
        }
    }

    private fun observeFavorites() {
        viewModelScope.launch {
            repository.observeFavorites()
                .map { list -> list.map(Movie::id).toSet() }
                .collect { favorites ->
                    _uiState.value = _uiState.value.copy(favorites = favorites)
                }
        }
    }

    @OptIn(FlowPreview::class)
    private fun observeSearch() {
        viewModelScope.launch {
            searchQuery
                .debounce(400)
                .map { it.trim() }
                .distinctUntilChanged()
                .collectLatest { query ->
                    handleSearchQuery(query)
                }
        }
    }

    private fun handleSearchQuery(query: String) {
        searchJob?.cancel()
        if (query.length < 3) {
            _uiState.value = _uiState.value.copy(
                searchResults = emptyList(),
                isSearching = false
            )
            return
        }
        _uiState.value = _uiState.value.copy(isSearching = true)
        searchJob = viewModelScope.launch {
            repository.searchContent(query)
                .onSuccess { paged ->
                    val results = paged.results.sortedBy { it.title.lowercase(Locale.getDefault()) }
                    _uiState.value = _uiState.value.copy(
                        searchResults = results,
                        isSearching = false,
                        errorMessage = null
                    )
                }
                .onFailure { throwable ->
                    _uiState.value = _uiState.value.copy(
                        searchResults = emptyList(),
                        isSearching = false,
                        errorMessage = throwable.message ?: "Erro ao buscar conteúdos"
                    )
                }
        }
    }

    fun onShowOnlyGloboChanged(onlyGlobo: Boolean) {
        _uiState.value = _uiState.value.copy(showOnlyGlobo = onlyGlobo)
    }
}
