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

    private lateinit var videoView: VideoView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.preview_edit_fragment, container, false)
        videoView = view.findViewById(R.id.video_view)
        val shareButton = view.findViewById<ImageButton>(R.id.share_btn)

        shareButton.setOnClickListener {
            shareButtonHandler()
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        val file = File(args.filePath)
        if (file.exists()) {
            startVideo(videoView, Uri.fromFile(file))
        } else {
            Toast.makeText(activity, resources.getString(R.string.generic_error_message), Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
        }
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
        with(videoView) {
            setOnPreparedListener { mediaPlayer ->
                val videoRatio = mediaPlayer.videoWidth / mediaPlayer.videoHeight.toFloat()
                val screenRatio = videoView.width / videoView.height.toFloat()
                val scaleX = videoRatio / screenRatio
                if (scaleX >= 1f) {
                    videoView.scaleX = scaleX
                } else {
                    videoView.scaleY = 1f / scaleX
                }
                mediaPlayer.isLooping = true
            }
            setVideoURI(uri)
            start()
        }
    }
}
