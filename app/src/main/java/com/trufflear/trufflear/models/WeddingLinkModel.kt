package com.trufflear.trufflear.models

import androidx.annotation.DrawableRes

data class WeddingLinkModel(
    @DrawableRes val image: Int,
    val text: String,
    val webLink: String
)

data class AttachmentLinkModel(
    val imageUrl: String,
    val text: String,
    val webLink: String,
    val colorCode: String
)