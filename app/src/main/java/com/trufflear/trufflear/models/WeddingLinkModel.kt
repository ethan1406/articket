package com.trufflear.trufflear.models

import androidx.annotation.DrawableRes

data class WeddingLinkModel(
    @DrawableRes val image: Int,
    val text: String,
    val webLink: String
)