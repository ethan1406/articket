package com.trufflear.trufflear.data

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import com.trufflear.trufflear.models.AugmentedImageBitmapModel
import com.trufflear.trufflear.modules.DefaultDispatcher
import com.google.ar.core.AugmentedImageDatabase
import com.google.ar.core.Config
import com.google.ar.core.Session
import com.squareup.picasso.Picasso
import com.trufflear.trufflear.models.CardImage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

private const val IMAGE_NAME_LENGTH = 6

class WeddingImageStorage @Inject constructor(
    @DefaultDispatcher private val dispatcher: CoroutineDispatcher
) {
    private val imageAndVideoMap = mutableMapOf<String, Int>()
    private var augmentedImageDatabase: AugmentedImageDatabase? = null

    fun getVideoForImage(imageName: String): Int? = imageAndVideoMap[imageName]

    suspend fun getConfigWithImageDatabase(
        config: Config,
        session: Session,
        cardImages: List<CardImage>
    ): Config {
        augmentedImageDatabase?.let {
            config.augmentedImageDatabase = augmentedImageDatabase
            return config
        }
        return withContext(dispatcher) {
            augmentedImageDatabase = AugmentedImageDatabase(session)
            cardImages.forEach { cardImage ->
                val bitmap = Picasso.get()
                    .load(cardImage.imageUrl)
                    .get()

                cardImage.physicalSize?.width?.let {
                    augmentedImageDatabase?.addImage(cardImage.imageId.toString(), bitmap, it)
                } ?: run {
                    augmentedImageDatabase?.addImage(cardImage.imageId.toString(), bitmap)
                }
            }
            config.augmentedImageDatabase = augmentedImageDatabase
            config
        }
    }

    private fun getRandomString(length: Int) : String {
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return (1..length)
            .map { allowedChars.random() }
            .joinToString("")
    }

}