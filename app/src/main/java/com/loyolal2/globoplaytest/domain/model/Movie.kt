package com.loyolal2.globoplaytest.domain.model

data class Movie(
    val id: Int,
    val title: String,
    val overview: String,
    val posterUrl: String?,
    val backdropUrl: String?,
    val releaseDate: String?,
    val voteAverage: Double,
    val mediaType: MediaType,
    val category: ContentCategory,
    val isFromGlobo: Boolean
)

enum class MediaType { MOVIE, TV }

enum class ContentCategory(val displayName: String) {
    FILME("Filme"),
    SERIE("Série"),
    NOVELA("Novela")
}
