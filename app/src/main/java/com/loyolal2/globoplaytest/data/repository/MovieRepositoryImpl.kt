package com.loyolal2.globoplaytest.data.repository

import com.loyolal2.globoplaytest.BuildConfig
import com.loyolal2.globoplaytest.data.local.FavoriteStorage
import com.loyolal2.globoplaytest.data.mapper.toDomain
import com.loyolal2.globoplaytest.data.mapper.toDomainList
import com.loyolal2.globoplaytest.data.remote.TmdbApiService
import com.loyolal2.globoplaytest.domain.model.ContentCategory
import com.loyolal2.globoplaytest.domain.model.MediaType
import com.loyolal2.globoplaytest.domain.model.Movie
import com.loyolal2.globoplaytest.domain.model.MovieDetail
import com.loyolal2.globoplaytest.domain.model.MovieSection
import com.loyolal2.globoplaytest.domain.repository.MovieRepository
import com.loyolal2.globoplaytest.domain.repository.PagedMovies
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val LANGUAGE = "pt-BR"
private const val GLOBO_NETWORK_ID = 25
private const val GLOBO_SOAP_GENRE_ID = "10766"
private val GLOBO_COMPANY_IDS = listOf("828", "1257", "3380")
private const val APPEND_DETAIL_PARAMS = "videos,recommendations"

class MovieRepositoryImpl(
    private val api: TmdbApiService,
    private val favoriteStorage: FavoriteStorage,
    private val apiKey: String = BuildConfig.TMDB_API_KEY
) : MovieRepository {

    @Volatile
    private var globoMovieIdsCache: Set<Int>? = null

    @Volatile
    private var globoSeriesIdsCache: Set<Int>? = null

    @Volatile
    private var globoNovelaIdsCache: Set<Int>? = null

    private fun missingKeyError(): Result<Nothing> = Result.failure(
        IllegalStateException(
            "TMDB API Key não configurada. Informe a chave em gradle.properties ou na variável de ambiente TMDB_API_KEY."
        )
    )

    override suspend fun getHomeSections(): Result<List<MovieSection>> {
        if (apiKey.isBlank()) return missingKeyError()

        return runCatching {
            coroutineScope {
                val novelasDeferred = async {
                    api.discoverTv(
                        page = 1,
                        language = LANGUAGE,
                        sortBy = "popularity.desc",
                        withNetworks = GLOBO_NETWORK_ID.toString(),
                        withGenres = GLOBO_SOAP_GENRE_ID,
                        withoutGenres = null,
                        apiKey = apiKey
                    ).results
                }

                val globoSeriesDeferred = async {
                    api.discoverTv(
                        page = 1,
                        language = LANGUAGE,
                        sortBy = "popularity.desc",
                        withNetworks = GLOBO_NETWORK_ID.toString(),
                        withGenres = null,
                        withoutGenres = GLOBO_SOAP_GENRE_ID,
                        apiKey = apiKey
                    ).results
                }

                val popularSeriesDeferred = async {
                    api.getPopularTv(page = 1, language = LANGUAGE, apiKey = apiKey).results
                }

                val globoMoviesDeferred = async {
                    api.discoverMovies(
                        page = 1,
                        language = LANGUAGE,
                        sortBy = "popularity.desc",
                        withCompanies = GLOBO_COMPANY_IDS.joinToString(separator = ","),
                        apiKey = apiKey
                    ).results
                }

                val popularMoviesDeferred = async {
                    api.getPopularMovies(page = 1, language = LANGUAGE, apiKey = apiKey).results
                }

                val novelasDto = novelasDeferred.await()
                val globoSeriesDto = globoSeriesDeferred.await()
                val popularSeriesDto = popularSeriesDeferred.await()
                val globoMoviesDto = globoMoviesDeferred.await()
                val popularMoviesDto = popularMoviesDeferred.await()

                globoNovelaIdsCache = novelasDto.map { it.id }.toSet()
                globoSeriesIdsCache = globoSeriesDto.map { it.id }.toSet()
                globoMovieIdsCache = globoMoviesDto.map { it.id }.toSet()

                val novelas = novelasDto.toDomainList(
                    mediaType = MediaType.TV,
                    category = ContentCategory.NOVELA
                ) { true }

                val series = (globoSeriesDto + popularSeriesDto)
                    .distinctBy { it.id }
                    .filterNot { dto -> globoNovelaIdsCache?.contains(dto.id) == true }
                    .toDomainList(
                        mediaType = MediaType.TV,
                        category = ContentCategory.SERIE
                    ) { dto -> globoSeriesIdsCache?.contains(dto.id) == true }

                val cinema = (globoMoviesDto + popularMoviesDto)
                    .distinctBy { it.id }
                    .toDomainList(
                        mediaType = MediaType.MOVIE,
                        category = ContentCategory.FILME
                    ) { dto -> globoMovieIdsCache?.contains(dto.id) == true }

                listOf(
                    MovieSection(
                        id = "novelas",
                        title = "Novelas",
                        movies = novelas
                    ),
                    MovieSection(
                        id = "series",
                        title = "Séries",
                        movies = series
                    ),
                    MovieSection(
                        id = "cinema",
                        title = "Cinema",
                        movies = cinema
                    )
                ).map { section ->
                    section.copy(movies = section.movies.filter { it.posterUrl != null })
                }.filter { it.movies.isNotEmpty() }
            }
        }
    }

    override suspend fun searchContent(query: String, page: Int): Result<PagedMovies> {
        if (apiKey.isBlank()) return missingKeyError()

        return runCatching {
            ensureGloboCaches()

            val moviesResponse = api.searchMovies(query, page, LANGUAGE, apiKey)
            val tvResponse = api.searchTv(query, page, LANGUAGE, apiKey)

            val globoMovieIds = globoMovieIdsCache ?: emptySet()
            val globoSeriesIds = globoSeriesIdsCache ?: emptySet()
            val globoNovelaIds = globoNovelaIdsCache ?: emptySet()

            val movies = moviesResponse.results.toDomainList(
                mediaType = MediaType.MOVIE,
                category = ContentCategory.FILME
            ) { dto -> globoMovieIds.contains(dto.id) }

            val shows = tvResponse.results.map { dto ->
                val isNovela = globoNovelaIds.contains(dto.id)
                val category = if (isNovela) ContentCategory.NOVELA else ContentCategory.SERIE
                dto.toDomain(
                    mediaType = MediaType.TV,
                    category = category,
                    isFromGlobo = globoSeriesIds.contains(dto.id) || isNovela
                )
            }

            val combined = (movies + shows)
                .distinctBy(Movie::id)
                .sortedBy { it.title.lowercase() }

            PagedMovies(
                page = minOf(moviesResponse.page, tvResponse.page),
                totalPages = maxOf(moviesResponse.totalPages ?: 1, tvResponse.totalPages ?: 1),
                results = combined
            )
        }
    }

    override suspend fun getContentDetails(id: Int, mediaType: MediaType): Result<MovieDetail> {
        if (apiKey.isBlank()) return missingKeyError()

        return runCatching {
            ensureGloboCaches()
            when (mediaType) {
                MediaType.MOVIE -> {
                    val response = api.getMovieDetails(
                        id = id,
                        language = LANGUAGE,
                        appendToResponse = APPEND_DETAIL_PARAMS,
                        apiKey = apiKey
                    )
                    val recommendations = response.recommendations?.results.orEmpty().toDomainList(
                        mediaType = MediaType.MOVIE,
                        category = ContentCategory.FILME
                    ) { dto -> globoMovieIdsCache?.contains(dto.id) == true }
                    val isFromGlobo = response.productionCompanies.any { company ->
                        company.id.toString() in GLOBO_COMPANY_IDS
                    }
                    response.toDomain(
                        mediaType = MediaType.MOVIE,
                        category = ContentCategory.FILME,
                        isFromGlobo = isFromGlobo,
                        recommendations = recommendations
                    )
                }

                MediaType.TV -> {
                    val response = api.getTvDetails(
                        id = id,
                        language = LANGUAGE,
                        appendToResponse = APPEND_DETAIL_PARAMS,
                        apiKey = apiKey
                    )
                    val recommendations = response.recommendations?.results.orEmpty().map { dto ->
                        val isNovela = globoNovelaIdsCache?.contains(dto.id) == true
                        dto.toDomain(
                            mediaType = MediaType.TV,
                            category = if (isNovela) ContentCategory.NOVELA else ContentCategory.SERIE,
                            isFromGlobo = globoSeriesIdsCache?.contains(dto.id) == true || isNovela
                        )
                    }
                    val isFromGlobo = response.networks.any { network ->
                        network.id == GLOBO_NETWORK_ID
                    }
                    val category = if (globoNovelaIdsCache?.contains(id) == true) {
                        ContentCategory.NOVELA
                    } else {
                        ContentCategory.SERIE
                    }
                    response.toDomain(
                        category = category,
                        isFromGlobo = isFromGlobo,
                        recommendations = recommendations
                    )
                }
            }
        }
    }

    private suspend fun ensureGloboCaches() {
        if (globoMovieIdsCache == null) {
            globoMovieIdsCache = api.discoverMovies(
                page = 1,
                language = LANGUAGE,
                sortBy = "popularity.desc",
                withCompanies = GLOBO_COMPANY_IDS.joinToString(separator = ","),
                apiKey = apiKey
            ).results.map { it.id }.toSet()
        }

        if (globoSeriesIdsCache == null || globoNovelaIdsCache == null) {
            val novelas = api.discoverTv(
                page = 1,
                language = LANGUAGE,
                sortBy = "popularity.desc",
                withNetworks = GLOBO_NETWORK_ID.toString(),
                withGenres = GLOBO_SOAP_GENRE_ID,
                withoutGenres = null,
                apiKey = apiKey
            ).results

            val series = api.discoverTv(
                page = 1,
                language = LANGUAGE,
                sortBy = "popularity.desc",
                withNetworks = GLOBO_NETWORK_ID.toString(),
                withGenres = null,
                withoutGenres = GLOBO_SOAP_GENRE_ID,
                apiKey = apiKey
            ).results

            globoNovelaIdsCache = novelas.map { it.id }.toSet()
            globoSeriesIdsCache = series.map { it.id }.toSet()
        }
    }

    override fun observeFavorites(): Flow<List<Movie>> = favoriteStorage.favorites

    override suspend fun toggleFavorite(movie: Movie) {
        favoriteStorage.toggleFavorite(movie)
    }

    override fun isFavorite(id: Int, mediaType: MediaType): Flow<Boolean> = favoriteStorage.favorites
        .map { favorites -> favorites.any { it.id == id && it.mediaType == mediaType } }
}
