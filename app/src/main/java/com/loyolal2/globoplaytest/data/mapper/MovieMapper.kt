package com.loyolal2.globoplaytest.data.mapper

import com.loyolal2.globoplaytest.data.remote.dto.GenreDto
import com.loyolal2.globoplaytest.data.remote.dto.MovieDetailDto
import com.loyolal2.globoplaytest.data.remote.dto.MovieDto
import com.loyolal2.globoplaytest.data.remote.dto.TvDetailDto
import com.loyolal2.globoplaytest.data.remote.dto.VideoDto
import com.loyolal2.globoplaytest.domain.model.ContentCategory
import com.loyolal2.globoplaytest.domain.model.MediaType
import com.loyolal2.globoplaytest.domain.model.Movie
import com.loyolal2.globoplaytest.domain.model.MovieDetail

private const val IMAGE_BASE_URL = "https://image.tmdb.org/t/p/"

private fun String?.toPosterUrl(size: String = "w500"): String? = this?.let {
    IMAGE_BASE_URL + size + it
}

private fun String?.toBackdropUrl(): String? = this?.let {
    IMAGE_BASE_URL + "w780" + it
}

fun MovieDto.toDomain(
    mediaType: MediaType,
    category: ContentCategory,
    isFromGlobo: Boolean
): Movie = Movie(
    id = id,
    title = title ?: name.orEmpty(),
    overview = overview.orEmpty(),
    posterUrl = posterPath.toPosterUrl(),
    backdropUrl = backdropPath.toBackdropUrl(),
    releaseDate = releaseDate ?: firstAirDate,
    voteAverage = voteAverage ?: 0.0,
    mediaType = mediaType,
    category = category,
    isFromGlobo = isFromGlobo
)

fun MovieDetailDto.toDomain(
    mediaType: MediaType,
    category: ContentCategory,
    isFromGlobo: Boolean,
    recommendations: List<Movie>
): MovieDetail = MovieDetail(
    movie = Movie(
        id = id,
        title = title.orEmpty(),
        overview = overview.orEmpty(),
        posterUrl = posterPath.toPosterUrl(),
        backdropUrl = backdropPath.toBackdropUrl(),
        releaseDate = releaseDate,
        voteAverage = voteAverage ?: 0.0,
        mediaType = mediaType,
        category = category,
        isFromGlobo = isFromGlobo
    ),
    originalTitle = originalTitle,
    genres = genres.map(GenreDto::name),
    runtimeMinutes = runtime,
    videoKey = videos?.results?.firstOrNull(VideoDto::isOfficialTrailer)?.key,
    seasonCount = null,
    episodeCount = null,
    recommendations = recommendations
)

fun TvDetailDto.toDomain(
    category: ContentCategory,
    isFromGlobo: Boolean,
    recommendations: List<Movie>
): MovieDetail = MovieDetail(
    movie = Movie(
        id = id,
        title = name.orEmpty(),
        overview = overview.orEmpty(),
        posterUrl = posterPath.toPosterUrl(),
        backdropUrl = backdropPath.toBackdropUrl(),
        releaseDate = firstAirDate,
        voteAverage = voteAverage ?: 0.0,
        mediaType = MediaType.TV,
        category = category,
        isFromGlobo = isFromGlobo
    ),
    originalTitle = originalName,
    genres = genres.map(GenreDto::name),
    runtimeMinutes = episodeRunTime.firstOrNull(),
    videoKey = videos?.results?.firstOrNull(VideoDto::isOfficialTrailer)?.key,
    seasonCount = numberOfSeasons,
    episodeCount = numberOfEpisodes,
    recommendations = recommendations
)

private fun VideoDto.isOfficialTrailer(): Boolean {
    val isTrailer = type.equals("Trailer", ignoreCase = true)
    val supportedSite = site.equals("YouTube", ignoreCase = true)
    return isTrailer && supportedSite && !key.isNullOrBlank()
}

fun List<MovieDto>.toDomainList(
    mediaType: MediaType,
    category: ContentCategory,
    isFromGlobo: (MovieDto) -> Boolean
): List<Movie> = map { dto ->
    dto.toDomain(
        mediaType = mediaType,
        category = category,
        isFromGlobo = isFromGlobo(dto)
    )
}
