package com.loyolal2.globoplaytest.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.loyolal2.globoplaytest.domain.model.ContentCategory
import com.loyolal2.globoplaytest.domain.model.MediaType
import com.loyolal2.globoplaytest.domain.model.Movie
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.favoritesDataStore by preferencesDataStore(name = "favorite_movies")

class FavoriteStorage(private val context: Context) {

    private val favoritesKey = stringPreferencesKey("favorites")
    private val json = Json { ignoreUnknownKeys = true }

    val favorites: Flow<List<Movie>> = context.favoritesDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val raw = preferences[favoritesKey]
            if (raw.isNullOrBlank()) {
                emptyList()
            } else {
                json.decodeFromString<List<StoredMovie>>(raw)
                    .map { it.toDomain() }
            }
        }

    suspend fun toggleFavorite(movie: Movie) {
        context.favoritesDataStore.edit { preferences ->
            val current = preferences[favoritesKey]?.let {
                json.decodeFromString<List<StoredMovie>>(it)
            } ?: emptyList()

            val existing = current.firstOrNull { it.id == movie.id && it.mediaType == movie.mediaType.name }
            val updated = if (existing == null) {
                current + movie.toStored()
            } else {
                current.filterNot { it.id == movie.id && it.mediaType == movie.mediaType.name }
            }
            if (updated.isEmpty()) {
                preferences.remove(favoritesKey)
            } else {
                preferences[favoritesKey] = json.encodeToString(updated)
            }
        }
    }

    suspend fun setFavorites(movies: List<Movie>) {
        context.favoritesDataStore.edit { preferences ->
            if (movies.isEmpty()) {
                preferences.remove(favoritesKey)
            } else {
                preferences[favoritesKey] = json.encodeToString(movies.map { it.toStored() })
            }
        }
    }

    private fun Movie.toStored(): StoredMovie = StoredMovie(
        id = id,
        title = title,
        overview = overview,
        posterUrl = posterUrl,
        backdropUrl = backdropUrl,
        releaseDate = releaseDate,
        voteAverage = voteAverage,
        mediaType = mediaType.name,
        category = category.name,
        isFromGlobo = isFromGlobo
    )

    @Serializable
    private data class StoredMovie(
        val id: Int,
        val title: String,
        val overview: String,
        val posterUrl: String?,
        val backdropUrl: String?,
        val releaseDate: String?,
        val voteAverage: Double,
        val mediaType: String = MediaType.MOVIE.name,
        val category: String = ContentCategory.FILME.name,
        val isFromGlobo: Boolean = false
    ) {
        fun toDomain(): Movie = Movie(
            id = id,
            title = title,
            overview = overview,
            posterUrl = posterUrl,
            backdropUrl = backdropUrl,
            releaseDate = releaseDate,
            voteAverage = voteAverage,
            mediaType = MediaType.valueOf(mediaType),
            category = ContentCategory.valueOf(category),
            isFromGlobo = isFromGlobo
        )
    }
}
