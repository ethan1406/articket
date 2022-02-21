package com.example.arimage

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentOnAttachListener
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.arimage.views.ArtistLinkAdapter
import com.example.arimage.views.ArtistLinkViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.ar.core.Anchor
import com.google.ar.core.AugmentedImage
import com.google.ar.core.AugmentedImageDatabase
import com.google.ar.core.Config
import com.google.ar.core.Session
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.BaseArFragment
import com.google.ar.sceneform.ux.TransformableNode
import com.google.ar.sceneform.ux.VideoNode
import java.io.IOException

// TODO create view model and ask for permission
class CustomArFragment: Fragment(R.layout.custom_ar_fragment),
    FragmentOnAttachListener,
    BaseArFragment.OnSessionConfigurationListener {
    private var isModelAdded = false
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var arFragment: ArFragment
    private lateinit var artistLinkAdapter: ArtistLinkAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arFragment = ArFragment()
        childFragmentManager.addFragmentOnAttachListener(this)
        childFragmentManager.beginTransaction().apply {
            add(R.id.arFragment, ArFragment())
            commit()
        }

        initializeRecyclerViewAdapter()
    }

    override fun onPause() {
        super.onPause()
        mediaPlayer?.pause()
    }

    private fun initializeRecyclerViewAdapter() {
        artistLinkAdapter = ArtistLinkAdapter(LayoutInflater.from(activity))
    }

    override fun onAttachFragment(fragmentManager: FragmentManager, fragment: Fragment) {
        if (fragment.id == R.id.arFragment) {
            arFragment = fragment as ArFragment

            arFragment.setOnSessionConfigurationListener(this)
        }
    }

    override fun onSessionConfiguration(session: Session, config: Config) {
        config.planeFindingMode = Config.PlaneFindingMode.DISABLED

//        if (setupAugmentedImagesDB(config, session)) Log.d("arcoreimg_db", "success") else Log.e(
//            "arcoreimg_db",
//            "faliure setting up db"
//        )

        arFragment.instructionsController = null
        // Check for image detection
        arFragment.setOnAugmentedImageUpdateListener(this::onAugmentedImageTrackingUpdate)
        initializeRecorder()
    }

    private fun setupAugmentedImagesDB(config: Config, session: Session?): Boolean {
        val bitmap: Bitmap = loadAugmentedImage() ?: return false
        val augmentedImageDatabase = AugmentedImageDatabase(session)
        augmentedImageDatabase.addImage("ticket", bitmap, 0.20f)
        config.augmentedImageDatabase = augmentedImageDatabase
        return true
    }

    private fun loadAugmentedImage(): Bitmap? {
        try {
            activity?.assets?.open("ticket.jpg").use { `is` -> return BitmapFactory.decodeStream(`is`) }
        } catch (e: IOException) {
            Log.e("arcoreimage", "io exception", e)
        }
        return null
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private fun onAugmentedImageTrackingUpdate(augmentedImage: AugmentedImage) {
        if (augmentedImage.trackingState == TrackingState.TRACKING
            && augmentedImage.trackingMethod == AugmentedImage.TrackingMethod.FULL_TRACKING) {
            if (augmentedImage.name == "ticket" && isModelAdded.not()) {
                createArtistArView(arFragment, augmentedImage.createAnchor(augmentedImage.centerPose), augmentedImage)
                isModelAdded = true
            }
        }
    }

    private fun initializeRecorder() {
        activity?.let {
            val videoRecorder = VideoRecorder(
                arFragment.arSceneView,
                it.contentResolver,
                it
            )
            setupRecordButton(videoRecorder)
        }
    }

    private fun setupRecordButton(videoRecorder: VideoRecorder) {
        val recordButton = view?.findViewById<FloatingActionButton>(R.id.record)
        recordButton?.setOnClickListener {
            toggleRecording(videoRecorder, recordButton)
        }
    }

    private fun toggleRecording(videoRecorder: VideoRecorder, recordButton: FloatingActionButton) {
        val recording = videoRecorder.onToggleRecord()

        recording.onSuccess { isRecording ->
            if (isRecording) {
                recordButton.setImageResource(R.drawable.round_stop)
            } else {
                mediaPlayer?.pause()
                videoRecorder.recordingFile?.let {
                    navigateToPreviewAndEdit(it.absolutePath)
                }

//                val uri = Uri.fromFile(videoRecorder.recordingFile)
//                recordButton.setImageResource(R.drawable.round_videocam)
//                videoRecorder.videoPath?.absolutePath?.let {
//                    Toast.makeText(this, "Video saved: $it", Toast.LENGTH_SHORT).show()
//
//                    // Send  notification of updated content.
//                    val values = ContentValues()
//                    values.put(MediaStore.Video.Media.TITLE, "Jackson Wang")
//                    values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
//
//                    values.put(MediaStore.Video.Media.DATA, it)
//                    contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)
//                }
            }
        }
    }

    private fun navigateToPreviewAndEdit(filePath: String) {
        val action = CustomArFragmentDirections.actionArFragmentToPreviewFragment(filePath)
        findNavController().navigate(action)
    }

    private fun createArtistArView(arFragment: ArFragment, anchor: Anchor, image: AugmentedImage) {
        val anchorNode = AnchorNode(anchor)
        mediaPlayer = initializeMediaPlayer()

        mediaPlayer?.let { mediaPlayer ->
            val videoNode = VideoNode(
                activity,
                mediaPlayer.apply { start() },
                null,
                null
            )
            videoNode.setOnTapListener { _, event ->
                if (event.action == MotionEvent.ACTION_UP) {
                    if(mediaPlayer.isPlaying) {
                        mediaPlayer.pause()
                    } else {
                        mediaPlayer.start()
                    }
                }
            }
            videoNode.parent = anchorNode
            videoNode.localRotation = Quaternion(Vector3(1f, 0f, 0f), -90f)
            videoNode.localScale = getScale(image, mediaPlayer.videoHeight, mediaPlayer.videoWidth)
            videoNode.localPosition = Vector3(0f, 0.007f, image.extentZ/2)
        }
        arFragment.arSceneView.scene.addChild(anchorNode)

        ViewRenderable.builder()
            .setView(activity, R.layout.artist_links)
            .build()
            .thenAccept {
                val recyclerView = it.view.findViewById<RecyclerView>(R.id.artist_links)
                recyclerView.apply {
                    adapter = artistLinkAdapter
                }

                it.isShadowCaster = false
                it.isShadowReceiver = false
                val node = TransformableNode(arFragment.transformationSystem)
                node.renderable = it
                node.parent = anchorNode
                node.localRotation = Quaternion(Vector3(1f, 0f, 0f), -90f)
                node.localPosition = Vector3(0f, 0f, image.extentZ)
                //node.localScale = Vector3(0.05f,  0.01f, 1f)
                node.scaleController.minScale = 0.1f
                node.scaleController.maxScale = 0.2f
            }
            .exceptionally {
                Toast.makeText(activity, "Please try again", Toast.LENGTH_SHORT).show()
                null
            }

        artistLinkAdapter.submitList(
            listOf(
                ArtistLinkViewModel(
                    image = R.drawable.team_wang,
                    text = "Wang Merch",
                    onClick = { openWebView(it) },
                    webLink = "https://teamwangdesign.com/"
                ),
                ArtistLinkViewModel(
                    image = R.drawable.bird,
                    text = "Website",
                    onClick = {},
                    webLink = ""
                )
            )
        )
    }

    private fun openWebView(url: String) {
        val customTabsIntent = createCustomTabIntent()
        try {
            activity?.let {
                customTabsIntent.launchUrl(it, url.toUri())
            }
        } catch (e: Exception) {
            Toast.makeText(activity, "Please try again", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createCustomTabIntent(): CustomTabsIntent =
        CustomTabsIntent.Builder()
            .setShowTitle(true)
            .build()

    private fun getScale(image: AugmentedImage, videoHeight: Int, videoWidth: Int): Vector3 {
        val imageHeight = 0.143f
        val imageWidth = 0.20f

        val newVideoHeight = image.extentZ * 2
        val newVideoWidth = image.extentZ  * (videoWidth.toFloat()/videoHeight.toFloat())

        return Vector3(newVideoWidth, newVideoHeight, 1f)
    }

    private fun initializeMediaPlayer(): MediaPlayer =
        MediaPlayer.create(activity, R.raw.jackson).also {
            it.isLooping = true
        }
}