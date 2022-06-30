package com.trufflear.trufflear.mappers

import com.trufflear.trufflear.GetCardTransformationDataResponse
import com.trufflear.trufflear.models.AttachmentLinkModel
import com.trufflear.trufflear.models.AttachmentView
import com.trufflear.trufflear.models.CardImage
import com.trufflear.trufflear.models.CardTransformation
import com.trufflear.trufflear.models.CardVideo
import com.trufflear.trufflear.models.Position
import com.trufflear.trufflear.models.Size

internal fun GetCardTransformationDataResponse.toArImageMap(): Map<Int, CardTransformation> =
    augmentedTransformationsList.map { transformation ->
        transformation.augmentedImagesList.associateBy (
            { it.imageId },
            { image ->
                CardTransformation(
                    transformationId = transformation.transformationId,
                    cardLinks = transformation
                        .attachmentView
                        .linkButtonsList
                        .map {
                            AttachmentLinkModel(
                                imageUrl = it.imageUrl,
                                text = it.text,
                                webLink = it.webUrl,
                                colorCode = it.colorCode
                            )
                        },
                    attachmentView = AttachmentView(
                        minScale = transformation.attachmentView.minScale,
                        maxScale = transformation.attachmentView.maxScale,
                        position = Position(
                            xScaleToImageWidth = transformation.attachmentView.position.xScaleToImageWidth,
                            y = transformation.attachmentView.position.y,
                            zScaleToImageHeight = transformation.attachmentView.position.zScaleToImageHeight
                        )
                    ),
                    cardImage = CardImage(
                        imageId = image.imageId,
                        imageUrl = image.imageUrl,
                        imageName = image.imageName,
                        physicalSize = Size(
                            width = image.physicalImageSize.width,
                            height = image.physicalImageSize.height
                        )
                    ),
                    cardVideo = CardVideo(
                        videoUrl = transformation.augmentedVideo.videoUrl,
                        widthScaleToImageWidth = transformation.augmentedVideo.videoWidthScaleToImageWidth,
                        position = Position(
                            xScaleToImageWidth = transformation.augmentedVideo.position.xScaleToImageWidth,
                            y = transformation.augmentedVideo.position.y,
                            zScaleToImageHeight = transformation.augmentedVideo.position.zScaleToImageHeight
                        )
                    )
                )
            }
        )
    }.flatMap {
        it.entries
    }.associate {
        it.key to it.value
    }