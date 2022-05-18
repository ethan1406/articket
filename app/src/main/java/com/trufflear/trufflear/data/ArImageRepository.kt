package com.trufflear.trufflear.data

import android.content.res.AssetManager
import android.graphics.BitmapFactory
import android.util.Log
import com.trufflear.trufflear.R
import com.trufflear.trufflear.AssetConstants
import com.trufflear.trufflear.models.ArtistLinkModel
import com.trufflear.trufflear.models.AugmentedImageBitmapModel
import com.trufflear.trufflear.models.AugmentedImageModel
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

    fun getWeddingLinks(): List<ArtistLinkModel> =
        listOf(
            ArtistLinkModel(
                image = R.drawable.ic_instagram,
                text = "Wedding pics",
                webLink = "https://www.trufflear.com/wedding-cards"
            ),
            ArtistLinkModel(
                image = R.drawable.ic_link,
                text = "Our Website",
                webLink = "https://www.trufflear.com/wedding-cards"
            ),

            ArtistLinkModel(
                image = R.drawable.ic_link,
                text = "Registry",
                webLink = "https://www.zola.com/wedding/phoebeandethan2022"
            ),
            ArtistLinkModel(
                image = R.drawable.ic_gallery,
                text = "Gallery",
                webLink = "https://www.zola.com/wedding/phoebeandethan2022"
            ),
            ArtistLinkModel(
                image = R.drawable.ic_calendar,
                text = "Schedule",
                webLink = "https://www.zola.com/wedding/phoebeandethan2022"
            )
        )

    private fun getLocalArImageAndVideoPairs(): List<AugmentedImageModel> =
        listOf(
            //AugmentedImageModel(AssetConstants.jacksonImage, R.raw.jackson, 0.20f),
//            AugmentedImageModel(AssetConstants.fredImage, R.raw.fred, 0.10f),
//            AugmentedImageModel(AssetConstants.WEDDING_COVER_IMAGE, R.raw.wedding_card, 0.14f),
//            AugmentedImageModel(AssetConstants.WEDDING_COVER_IMAGE_2, R.raw.wedding_card, 0.109f),
//            AugmentedImageModel(AssetConstants.WEDDING_COVER_IMAGE_3, R.raw.wedding_card, 0.129f),
            AugmentedImageModel(AssetConstants.WEDDING_COVER_IMAGE_4, R.raw.wedding_card, 0.129f),
        )
}