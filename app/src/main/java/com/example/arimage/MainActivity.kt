package com.example.arimage

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

@AndroidEntryPoint
class MainActivity : AppCompatActivity(R.layout.activity_main) {
    private val TAG = MainActivity::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        deleteRecursive(File(this.filesDir, FileProviderConstants.FILE_NAME))
        actionBar?.hide()

    }

    private fun deleteRecursive(fileOrDirectory: File) {
        if (fileOrDirectory.isDirectory) {
            fileOrDirectory.listFiles()?.toList()?.forEach { child ->
                Log.d(TAG, "deleting file with path ${child.absolutePath}")
                deleteRecursive(child)
            }
        }
        fileOrDirectory.delete()
    }
}