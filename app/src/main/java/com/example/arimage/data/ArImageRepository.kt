package com.example.arimage.data

import android.content.res.AssetManager
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
                image = R.drawable.ic_instagram,
                text = "New Song",
                webLink = "https://instagram.com/frederic0913?utm_medium=copy_link"
            ),
            ArtistLinkModel(
                image = R.drawable.ic_spotify,
                text = "Spotify",
                webLink = ""
            ),
            ArtistLinkModel(
                image = R.drawable.ic_link,
                text = "Merch",
                webLink = ""
            )
        )

    private fun getLocalArImageAndVideoPairs(): List<AugmentedImageModel> =
        listOf(
            //AugmentedImageModel(AssetConstants.jacksonImage, R.raw.jackson, 0.20f),
            AugmentedImageModel(AssetConstants.fredImage, R.raw.fred, 0.10f),
            AugmentedImageModel(AssetConstants.WEDDING_COVER_IMAGE, R.raw.wedding_card, 0.20f),
        )
}