package com.loyolal2.globoplaytest.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MovieDetailDto(
    val id: Int,
    val title: String? = null,
    @SerialName("original_title")
    val originalTitle: String? = null,
    val overview: String? = null,
    @SerialName("poster_path")
    val posterPath: String? = null,
    @SerialName("backdrop_path")
    val backdropPath: String? = null,
    @SerialName("release_date")
    val releaseDate: String? = null,
    val runtime: Int? = null,
    @SerialName("vote_average")
    val voteAverage: Double? = null,
    val genres: List<GenreDto> = emptyList(),
    @SerialName("production_companies")
    val productionCompanies: List<ProductionCompanyDto> = emptyList(),
    val videos: VideoResponseDto? = null,
    val recommendations: MovieListResponseDto? = null
)

@Serializable
data class GenreDto(
    val id: Int,
    val name: String
)

@Serializable
data class ProductionCompanyDto(
    val id: Int,
    val name: String
)
