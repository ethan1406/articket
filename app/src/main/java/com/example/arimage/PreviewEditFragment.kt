package com.example.arimage

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import android.widget.VideoView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import java.io.File

class PreviewEditFragment: Fragment() {
    private val TAG = PreviewEditFragment::class.java.simpleName

    private val args by navArgs<PreviewEditFragmentArgs>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.preview_edit_fragment, container, false)
        val videoView = view.findViewById<VideoView>(R.id.video_view)
        val shareButton = view.findViewById<ImageButton>(R.id.share_btn)

        val file = File(args.filePath)
        if (file.exists()) {
            startVideo(videoView, Uri.fromFile(file))
        } else {
            Toast.makeText(activity, resources.getString(R.string.generic_error_message), Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
        }
        shareButton.setOnClickListener {
            shareButtonHandler()
        }
        return view
    }

    override fun onDestroyView() {
        val file = File(args.filePath)
        if (file.exists()) {
            val didDelete = file.delete()
            Log.d(TAG, "Deleting local video successfully was $didDelete")
        }
        super.onDestroyView()
    }

    private fun shareButtonHandler() {
        activity?.let {
            val contentUri = ContentUriProvider().getUriForFile(it, FileProviderConstants.AUTHORITY, File(args.filePath))
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_STREAM, contentUri)
                type = "video/mp4"
            }
            val shareIntent = Intent.createChooser(sendIntent, null)
            startActivity(shareIntent)
        }
    }

    private fun startVideo(videoView: VideoView, uri: Uri) {
        videoView.setVideoURI(uri)
        videoView.start()
        videoView.setOnPreparedListener { mediaPlayer -> mediaPlayer.isLooping = true }
    }
}
