package com.loyolal2.globoplaytest.domain.repository

import com.loyolal2.globoplaytest.domain.model.MediaType
import com.loyolal2.globoplaytest.domain.model.Movie
import com.loyolal2.globoplaytest.domain.model.MovieDetail
import com.loyolal2.globoplaytest.domain.model.MovieSection
import kotlinx.coroutines.flow.Flow

interface MovieRepository {
    suspend fun getHomeSections(): Result<List<MovieSection>>
    suspend fun searchContent(query: String, page: Int = 1): Result<PagedMovies>
    suspend fun getContentDetails(id: Int, mediaType: MediaType): Result<MovieDetail>
    fun observeFavorites(): Flow<List<Movie>>
    suspend fun toggleFavorite(movie: Movie)
    fun isFavorite(id: Int, mediaType: MediaType): Flow<Boolean>
}

data class PagedMovies(
    val page: Int,
    val totalPages: Int,
    val results: List<Movie>
)
