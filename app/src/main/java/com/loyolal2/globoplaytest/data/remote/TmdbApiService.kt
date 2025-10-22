package com.loyolal2.globoplaytest.data.remote

import com.loyolal2.globoplaytest.data.remote.dto.MovieDetailDto
import com.loyolal2.globoplaytest.data.remote.dto.MovieListResponseDto
import com.loyolal2.globoplaytest.data.remote.dto.TvDetailDto
import com.loyolal2.globoplaytest.data.remote.dto.VideoResponseDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TmdbApiService {

    @GET("trending/movie/week")
    suspend fun getTrendingMovies(
        @Query("page") page: Int,
        @Query("language") language: String,
        @Query("api_key") apiKey: String
    ): MovieListResponseDto

    @GET("movie/popular")
    suspend fun getPopularMovies(
        @Query("page") page: Int,
        @Query("language") language: String,
        @Query("api_key") apiKey: String
    ): MovieListResponseDto

    @GET("movie/top_rated")
    suspend fun getTopRatedMovies(
        @Query("page") page: Int,
        @Query("language") language: String,
        @Query("api_key") apiKey: String
    ): MovieListResponseDto

    @GET("movie/upcoming")
    suspend fun getUpcomingMovies(
        @Query("page") page: Int,
        @Query("language") language: String,
        @Query("api_key") apiKey: String
    ): MovieListResponseDto

    @GET("tv/popular")
    suspend fun getPopularTv(
        @Query("page") page: Int,
        @Query("language") language: String,
        @Query("api_key") apiKey: String
    ): MovieListResponseDto

    @GET("trending/tv/week")
    suspend fun getTrendingTv(
        @Query("page") page: Int,
        @Query("language") language: String,
        @Query("api_key") apiKey: String
    ): MovieListResponseDto

    @GET("discover/tv")
    suspend fun discoverTv(
        @Query("page") page: Int,
        @Query("language") language: String,
        @Query("sort_by") sortBy: String?,
        @Query("with_networks") withNetworks: String?,
        @Query("with_genres") withGenres: String?,
        @Query("without_genres") withoutGenres: String?,
        @Query("api_key") apiKey: String
    ): MovieListResponseDto

    @GET("discover/movie")
    suspend fun discoverMovies(
        @Query("page") page: Int,
        @Query("language") language: String,
        @Query("sort_by") sortBy: String?,
        @Query("with_companies") withCompanies: String?,
        @Query("api_key") apiKey: String
    ): MovieListResponseDto

    @GET("movie/{movie_id}")
    suspend fun getMovieDetails(
        @Path("movie_id") id: Int,
        @Query("language") language: String,
        @Query("append_to_response") appendToResponse: String,
        @Query("api_key") apiKey: String
    ): MovieDetailDto

    @GET("tv/{tv_id}")
    suspend fun getTvDetails(
        @Path("tv_id") id: Int,
        @Query("language") language: String,
        @Query("append_to_response") appendToResponse: String,
        @Query("api_key") apiKey: String
    ): TvDetailDto

    @GET("movie/{movie_id}/videos")
    suspend fun getMovieVideos(
        @Path("movie_id") id: Int,
        @Query("language") language: String,
        @Query("api_key") apiKey: String
    ): VideoResponseDto

    @GET("search/movie")
    suspend fun searchMovies(
        @Query("query") query: String,
        @Query("page") page: Int,
        @Query("language") language: String,
        @Query("api_key") apiKey: String
    ): MovieListResponseDto

    @GET("search/tv")
    suspend fun searchTv(
        @Query("query") query: String,
        @Query("page") page: Int,
        @Query("language") language: String,
        @Query("api_key") apiKey: String
    ): MovieListResponseDto
}
