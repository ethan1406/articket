package com.example.arimage.data

import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.example.arimage.AssetConstants
import com.example.arimage.R
import com.example.arimage.models.ArtistLinkModel
import com.example.arimage.models.AugmentedImageBitmapModel
import com.example.arimage.models.AugmentedImageModel
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ArImageRepository @Inject constructor(private val assets: AssetManager) {
    private val TAG = ArImageRepository::class.java.simpleName

    fun loadArImageBitmapModels(): List<AugmentedImageBitmapModel> =
        getLocalArImageAndVideoPairs().mapNotNull {
            try {
                val bitmap = assets.open(it.assetImage).use { BitmapFactory.decodeStream(it) }
                AugmentedImageBitmapModel(
                    bitmap, it.videoRes, it.imageWidthMeters
                )
            } catch (e: IOException) {
                Log.e(TAG, "io exception", e)
                null
            }
        }

    fun getArtistLinks(): List<ArtistLinkModel> =
        listOf(
            ArtistLinkModel(
                image = R.drawable.team_wang,
                text = "Wang Merch",
                webLink = "https://teamwangdesign.com/"
            ),
            ArtistLinkModel(
                image = R.drawable.bird,
                text = "Website",
                webLink = ""
            )
        )

    private fun getLocalArImageAndVideoPairs(): List<AugmentedImageModel> =
        listOf(
            AugmentedImageModel(AssetConstants.artistImage, R.raw.jackson, 0.20f)
        )
}