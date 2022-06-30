package com.trufflear.trufflear.viewmodels

data class AttachmentViewLinkModel(
    val imageUrl: String,
    val text: String,
    val onClick: (String) -> Unit,
    val colorCode: String,
    val webLink: String
)