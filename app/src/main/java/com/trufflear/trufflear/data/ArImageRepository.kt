package com.trufflear.trufflear.data

import android.content.res.AssetManager
import android.graphics.BitmapFactory
import android.util.Log
import com.bugsnag.android.Bugsnag
import com.trufflear.trufflear.R
import com.trufflear.trufflear.AssetConstants
import com.trufflear.trufflear.data.api.CardTransformationApi
import com.trufflear.trufflear.mappers.toArImageMap
import com.trufflear.trufflear.models.WeddingLinkModel
import com.trufflear.trufflear.models.AugmentedImageBitmapModel
import com.trufflear.trufflear.models.AugmentedImageModel
import com.trufflear.trufflear.models.CardTransformation
import com.trufflear.trufflear.modules.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ArImageRepository @Inject constructor(
    private val assets: AssetManager,
    private val cardTransformationApi: CardTransformationApi,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
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

    suspend fun getCardTransformations(): Result<Map<Int, CardTransformation>> {
        return withContext(ioDispatcher) {
            val response = cardTransformationApi.getCardTransformationData()
            response.map { it.toArImageMap() }
        }
    }

    fun getWeddingLinks(): List<WeddingLinkModel> =
        listOf(
            WeddingLinkModel(
                image = R.drawable.ic_instagram,
                text = "Wedding pics",
                webLink = "https://www.trufflear.com/wedding-cards"
            ),
            WeddingLinkModel(
                image = R.drawable.ic_link,
                text = "Our Website",
                webLink = "https://www.trufflear.com/wedding-cards"
            ),

            WeddingLinkModel(
                image = R.drawable.ic_link,
                text = "Registry",
                webLink = "https://www.zola.com/wedding/phoebeandethan2022"
            ),
            WeddingLinkModel(
                image = R.drawable.ic_gallery,
                text = "Gallery",
                webLink = "https://www.zola.com/wedding/phoebeandethan2022"
            ),
            WeddingLinkModel(
                image = R.drawable.ic_calendar,
                text = "Schedule",
                webLink = "https://www.zola.com/wedding/phoebeandethan2022"
            )
        )

    private fun getLocalArImageAndVideoPairs(): List<AugmentedImageModel> =
        listOf(
            AugmentedImageModel(AssetConstants.WEDDING_COVER_IMAGE, R.raw.wedding_card, 0.141f),
//            AugmentedImageModel(AssetConstants.WEDDING_CARD, R.raw.wedding_card, 0.141f),
//            AugmentedImageModel(AssetConstants.INVITATION_CARD, R.raw.wedding_card, 0.178f)
        )
}