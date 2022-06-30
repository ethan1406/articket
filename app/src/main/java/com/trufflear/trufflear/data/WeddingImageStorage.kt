package com.trufflear.trufflear.data

import com.trufflear.trufflear.modules.DefaultDispatcher
import com.google.ar.core.AugmentedImageDatabase
import com.google.ar.core.Config
import com.google.ar.core.Session
import com.squareup.picasso.Picasso
import com.trufflear.trufflear.models.CardImage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class WeddingImageStorage @Inject constructor(
    @DefaultDispatcher private val dispatcher: CoroutineDispatcher
) {
    private var augmentedImageDatabase: AugmentedImageDatabase? = null

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
}