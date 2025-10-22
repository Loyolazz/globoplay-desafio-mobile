package com.loyolal2.globoplaytest.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TvDetailDto(
    val id: Int,
    val name: String? = null,
    @SerialName("original_name")
    val originalName: String? = null,
    val overview: String? = null,
    @SerialName("poster_path")
    val posterPath: String? = null,
    @SerialName("backdrop_path")
    val backdropPath: String? = null,
    @SerialName("first_air_date")
    val firstAirDate: String? = null,
    @SerialName("episode_run_time")
    val episodeRunTime: List<Int> = emptyList(),
    @SerialName("vote_average")
    val voteAverage: Double? = null,
    val genres: List<GenreDto> = emptyList(),
    @SerialName("number_of_seasons")
    val numberOfSeasons: Int? = null,
    @SerialName("number_of_episodes")
    val numberOfEpisodes: Int? = null,
    val networks: List<NetworkDto> = emptyList(),
    val videos: VideoResponseDto? = null,
    val recommendations: MovieListResponseDto? = null
)

@Serializable
data class NetworkDto(
    val id: Int,
    val name: String
)
