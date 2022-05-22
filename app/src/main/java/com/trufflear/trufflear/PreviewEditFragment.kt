package com.trufflear.trufflear

import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import android.widget.VideoView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bugsnag.android.Bugsnag
import com.google.firebase.analytics.FirebaseAnalytics
import com.trufflear.trufflear.file.ContentUriProvider
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class PreviewEditFragment: Fragment() {
    private val TAG = PreviewEditFragment::class.java.simpleName

    private val args by navArgs<PreviewEditFragmentArgs>()

    private lateinit var videoView: VideoView

    @Inject lateinit var contentUriProvider: ContentUriProvider

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.preview_edit_fragment, container, false)
        videoView = view.findViewById(R.id.video_view)
        val shareButton = view.findViewById<ImageButton>(R.id.share_btn)
        val backButton = view.findViewById<ImageButton>(R.id.back_btn)

        backButton.setOnClickListener {
            FirebaseAnalytics.getInstance(requireContext()).logEvent("preview_edit_dismiss_button_tapped", null)
            findNavController().popBackStack()
        }

        shareButton.setOnClickListener {
            FirebaseAnalytics.getInstance(requireContext()).logEvent("preview_edit_share_button_tapped", null)
            shareButtonHandler()
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        val file = File(args.filePath)
        if (file.exists()) {
            if (videoView.isPlaying.not()) {
                startVideo(videoView, Uri.fromFile(file))
            }
        } else {
            Bugsnag.notify(Exception("video file does not exist"))
            Toast.makeText(activity, resources.getString(R.string.generic_error_snackbar_message), Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
        }

        FirebaseAnalytics.getInstance(requireContext()).logEvent("preview_edit_screen_viewed", null)
    }

    private fun shareButtonHandler() {
        activity?.let {
            val contentUri = contentUriProvider.getUriForFile(it, FileProviderConstants.AUTHORITY, File(args.filePath))
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_STREAM, contentUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                type = "video/mp4"
            }

            val shareIntent = Intent.createChooser(sendIntent, null)
            val resolveList = it.packageManager.queryIntentActivities(shareIntent, PackageManager.MATCH_DEFAULT_ONLY)
            resolveList.forEach { info ->
                val packageName = info.activityInfo.packageName
                it.grantUriPermission(packageName, contentUri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            startActivity(shareIntent)
        }
    }

    private fun startVideo(videoView: VideoView, uri: Uri) {
        with(videoView) {
            setOnPreparedListener { mediaPlayer ->
                scaleVideoView(mediaPlayer, this)
                mediaPlayer.isLooping = true
            }
            setVideoURI(uri)
            start()
        }
    }

    private fun scaleVideoView(mediaPlayer: MediaPlayer, videoView: VideoView) {
        val videoRatio = mediaPlayer.videoWidth / mediaPlayer.videoHeight.toFloat()
        val screenRatio = videoView.width / videoView.height.toFloat()
        val scaleX = videoRatio / screenRatio
        if (scaleX >= 1f) {
            videoView.scaleX = scaleX
        } else {
            videoView.scaleY = 1f / scaleX
        }
    }
}
