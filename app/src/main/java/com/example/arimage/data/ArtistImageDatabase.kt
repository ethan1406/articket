package com.example.arimage.data

import com.example.arimage.models.AugmentedImageBitmapModel
import com.example.arimage.modules.DefaultDispatcher
import com.google.ar.core.AugmentedImageDatabase
import com.google.ar.core.CameraConfig
import com.google.ar.core.Config
import com.google.ar.core.Session
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

private const val IMAGE_NAME_LENGTH = 6

class ArtistImageDatabase @Inject constructor(
    private val session: Session,
    @DefaultDispatcher private val dispatcher: CoroutineDispatcher
) {
    private val imageAndVideoMap = mutableMapOf<String, Int>()
    private val augmentedImageDatabase = AugmentedImageDatabase(session)
    private val config = Config(session)

    fun getVideoForImage(imageName: String): Int? = imageAndVideoMap[imageName]

    init {
        setupConfig()
    }

    suspend fun getConfigWithImageDatabase(artistImages: List<AugmentedImageBitmapModel>): Config {
        return withContext(dispatcher) {
            artistImages.forEach { model ->
                val imageName = getRandomString(IMAGE_NAME_LENGTH)
                model.imageWidthMeters?.let {
                    augmentedImageDatabase.addImage(imageName, model.image, model.imageWidthMeters)
                } ?: run {
                    augmentedImageDatabase.addImage(imageName, model.image)
                }
                imageAndVideoMap.putIfAbsent(imageName, model.videoRes)
            }
            config.augmentedImageDatabase = augmentedImageDatabase
            config
        }
    }

    private fun setupConfig() {
        config.depthMode = Config.DepthMode.DISABLED
        config.planeFindingMode = Config.PlaneFindingMode.HORIZONTAL_AND_VERTICAL
        config.focusMode = Config.FocusMode.AUTO
        config.updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
        if (session.cameraConfig.facingDirection == CameraConfig.FacingDirection.FRONT
            && config.lightEstimationMode == Config.LightEstimationMode.ENVIRONMENTAL_HDR
        ) {
            config.lightEstimationMode = Config.LightEstimationMode.DISABLED
        }
        config.planeFindingMode = Config.PlaneFindingMode.DISABLED
    }


    private fun getRandomString(length: Int) : String {
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return (1..length)
            .map { allowedChars.random() }
            .joinToString("")
    }

}