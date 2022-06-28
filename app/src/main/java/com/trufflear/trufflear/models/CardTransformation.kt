package com.trufflear.trufflear.models

data class CardTransformation(
    val transformationId: Int,
    val cardLinks: List<AttachmentLinkModel>,
    val attachmentView: AttachmentView,
    val cardImage: CardImage,
    val cardVideo: CardVideo
)