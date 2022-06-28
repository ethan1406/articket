package com.trufflear.trufflear.models

data class CardImage(
    val imageId: Int,
    val imageUrl: String,
    val imageName: String,
    val physicalSize: Size?
)