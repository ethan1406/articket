package com.example.arimage.models

import androidx.annotation.DrawableRes

data class ArtistLinkModel(
    @DrawableRes val image: Int,
    val text: String,
    val webLink: String
)