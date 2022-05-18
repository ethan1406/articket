package com.trufflear.trufflear

import android.content.Context
import androidx.core.content.FileProvider
import java.io.File

class ContentUriProvider {

    fun getUriForFile(context: Context, authority: String, file: File) =
        FileProvider.getUriForFile(context, authority, file)
}