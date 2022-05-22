package com.trufflear.trufflear.file

import java.io.File
import javax.inject.Inject

class FileCreator @Inject constructor() {

    fun createFile(parent: File, child: String): File = File(parent, child)
}