package com.loyolal2.globoplaytest.ui.navigation

import androidx.annotation.DrawableRes
import com.loyolal2.globoplaytest.R

sealed class GloboplayDestination(
    val route: String,
    val label: String,
    @DrawableRes val iconRes: Int
) {
    object Home : GloboplayDestination("home", "Início", R.drawable.ic_home)
    object Favorites : GloboplayDestination("favorites", "Minha lista", R.drawable.ic_favorite)

    companion object {
        val bottomDestinations: List<GloboplayDestination>
            get() = listOf(Home, Favorites)
        const val SplashRoute = "splash"
        const val DetailsRoute = "details/{mediaType}/{contentId}"
        const val DetailsIdArgument = "contentId"
        const val DetailsMediaTypeArgument = "mediaType"
    }
}
