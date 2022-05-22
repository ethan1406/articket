package com.trufflear.trufflear.file

import android.content.Context
import androidx.core.content.FileProvider
import java.io.File
import javax.inject.Inject

class ContentUriProvider @Inject constructor() {

    fun getUriForFile(context: Context, authority: String, file: File) =
        FileProvider.getUriForFile(context, authority, file)
}