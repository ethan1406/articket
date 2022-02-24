package com.example.arimage.viewmodels

import java.io.File

sealed class RecordViewState {
    object StartRecording: RecordViewState()

    data class StopRecording(
        val filePath: File?
    ): RecordViewState()
}