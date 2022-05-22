package com.trufflear.trufflear.models

import android.graphics.Bitmap
import androidx.annotation.RawRes

data class AugmentedImageModel(
    val assetImage: String,
    @RawRes val videoRes: Int,
    val imageWidthMeters: Float?
)

data class AugmentedImageBitmapModel(
    val image: Bitmap,
    @RawRes val videoRes: Int,
    val imageWidthMeters: Float?
)