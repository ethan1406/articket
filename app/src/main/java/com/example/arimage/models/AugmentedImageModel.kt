package com.example.arimage.models

import android.graphics.Bitmap

data class AugmentedImageModel(
    val assetImage: String,
    val videoRes: Int,
    val imageWidthMeters: Float?
)

data class AugmentedImageBitmapModel(
    val image: Bitmap,
    val videoRes: Int,
    val imageWidthMeters: Float?
)