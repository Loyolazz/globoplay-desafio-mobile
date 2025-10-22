package com.loyolal2.globoplaytest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.loyolal2.globoplaytest.data.local.FavoriteStorage
import com.loyolal2.globoplaytest.data.remote.TmdbNetwork
import com.loyolal2.globoplaytest.data.repository.MovieRepositoryImpl
import com.loyolal2.globoplaytest.domain.repository.MovieRepository
import com.loyolal2.globoplaytest.ui.GloboplayApp
import com.loyolal2.globoplaytest.ui.theme.GloboplaytestTheme

class MainActivity : ComponentActivity() {

    private val repository: MovieRepository by lazy {
        MovieRepositoryImpl(
            api = TmdbNetwork.api,
            favoriteStorage = FavoriteStorage(applicationContext)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var isDarkTheme by rememberSaveable { mutableStateOf(true) }

            GloboplaytestTheme(darkTheme = isDarkTheme) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    GloboplayApp(
                        repository = repository,
                        onPlayVideo = ::openVideo,
                        isDarkTheme = isDarkTheme,
                        onThemeChange = { isDarkTheme = it }
                    )
                }
            }
        }
    }

    private fun openVideo(videoKey: String) {
        val youtubeIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://www.youtube.com/watch?v=$videoKey")
        )
        youtubeIntent.putExtra("force_fullscreen", true)
        if (youtubeIntent.resolveActivity(packageManager) != null) {
            startActivity(youtubeIntent)
        }
    }
}