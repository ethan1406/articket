package com.trufflear.trufflear.viewmodels

import androidx.annotation.DrawableRes

data class ArtistLinkViewModel(
    @DrawableRes val image: Int,
    val text: String,
    val onClick: (String) -> Unit,
    val webLink: String
)