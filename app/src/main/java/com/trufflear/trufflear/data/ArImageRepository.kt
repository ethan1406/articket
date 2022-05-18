package com.trufflear.trufflear.data

import android.content.res.AssetManager
import android.graphics.BitmapFactory
import android.util.Log
import com.bugsnag.android.Bugsnag
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
                Bugsnag.notify(e)
                Log.e(TAG, "io exception", e)
                null
            }
        }

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
            AugmentedImageModel(AssetConstants.WEDDING_COVER_IMAGE_4, R.raw.wedding_card, 0.129f),
        )
}