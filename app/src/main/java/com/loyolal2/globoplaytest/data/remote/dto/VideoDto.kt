package com.loyolal2.globoplaytest.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VideoDto(
    val id: String,
    val name: String? = null,
    val site: String? = null,
    val type: String? = null,
    @SerialName("key")
    val key: String? = null
)

@Serializable
data class VideoResponseDto(
    val id: Int? = null,
    val results: List<VideoDto> = emptyList()
)
