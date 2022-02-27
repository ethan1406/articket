package com.example.arimage.data

import com.example.arimage.models.AugmentedImageBitmapModel
import com.example.arimage.modules.DefaultDispatcher
import com.google.ar.core.AugmentedImageDatabase
import com.google.ar.core.Config
import com.google.ar.core.Session
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

private const val IMAGE_NAME_LENGTH = 6

class ArtistImageDatabase @Inject constructor(
    @DefaultDispatcher private val dispatcher: CoroutineDispatcher
) {
    private val imageAndVideoMap = mutableMapOf<String, Int>()
    private var augmentedImageDatabase: AugmentedImageDatabase? = null

    fun getVideoForImage(imageName: String): Int? = imageAndVideoMap[imageName]

    suspend fun getConfigWithImageDatabase(
        config: Config,
        session: Session,
        artistImages: List<AugmentedImageBitmapModel>,
    ): Config {
        if (augmentedImageDatabase != null) {
            config.augmentedImageDatabase = augmentedImageDatabase
            return config
        }
        return withContext(dispatcher) {
            augmentedImageDatabase = AugmentedImageDatabase(session)
            artistImages.forEach { model ->
                val imageName = getRandomString(IMAGE_NAME_LENGTH)
                model.imageWidthMeters?.let {
                    augmentedImageDatabase?.addImage(imageName, model.image, model.imageWidthMeters)
                } ?: run {
                    augmentedImageDatabase?.addImage(imageName, model.image)
                }
                imageAndVideoMap.putIfAbsent(imageName, model.videoRes)
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