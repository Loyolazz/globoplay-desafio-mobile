package com.loyolal2.globoplaytest.domain.model

data class MovieDetail(
    val movie: Movie,
    val originalTitle: String?,
    val genres: List<String>,
    val runtimeMinutes: Int?,
    val videoKey: String?,
    val seasonCount: Int? = null,
    val episodeCount: Int? = null,
    val recommendations: List<Movie> = emptyList()
)
