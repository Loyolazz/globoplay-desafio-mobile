package com.loyolal2.globoplaytest.domain.model

data class MovieSection(
    val id: String,
    val title: String,
    val movies: List<Movie>
)
