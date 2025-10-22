package com.loyolal2.globoplaytest.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.loyolal2.globoplaytest.domain.repository.MovieRepository
import com.loyolal2.globoplaytest.ui.detail.MovieDetailViewModel
import com.loyolal2.globoplaytest.ui.favorites.FavoritesViewModel
import com.loyolal2.globoplaytest.ui.home.HomeViewModel

class AppViewModelFactory(
    private val repository: MovieRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val viewModel = when (modelClass) {
            HomeViewModel::class.java -> HomeViewModel(repository)
            FavoritesViewModel::class.java -> FavoritesViewModel(repository)
            MovieDetailViewModel::class.java -> MovieDetailViewModel(repository)
            else -> throw IllegalArgumentException("ViewModel desconhecido: ${modelClass.name}")
        }
        @Suppress("UNCHECKED_CAST")
        return viewModel as T
    }
}
