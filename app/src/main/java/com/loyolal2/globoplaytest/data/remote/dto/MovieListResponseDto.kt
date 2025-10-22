package com.loyolal2.globoplaytest.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class MovieListResponseDto(
    val page: Int,
    val results: List<MovieDto> = emptyList(),
    val totalPages: Int? = null,
    val totalResults: Int? = null
)
