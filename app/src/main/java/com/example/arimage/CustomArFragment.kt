package com.example.arimage

import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentOnAttachListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.arimage.views.ArtistLinkAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.ar.core.Anchor
import com.google.ar.core.AugmentedImage
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
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

// TODO ask for permission
@AndroidEntryPoint
class CustomArFragment: Fragment(R.layout.custom_ar_fragment),
    FragmentOnAttachListener,
    BaseArFragment.OnSessionConfigurationListener {

    private val viewModel by viewModels<CustomArtViewModel>()

    private var isModelAdded = false
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var arFragment: ArFragment
    @Inject lateinit var artistLinkAdapter: ArtistLinkAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arFragment = ArFragment()
        childFragmentManager.addFragmentOnAttachListener(this)
        childFragmentManager.beginTransaction().apply {
            add(R.id.arFragment, ArFragment())
            commit()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.artistLinks.observe(viewLifecycleOwner) { artistLinkAdapter.submitList(it) }
        viewModel.openWebIntent.observe(viewLifecycleOwner) { openWebView(it.first, it.second) }
    }

    override fun onPause() {
        super.onPause()
        mediaPlayer?.pause()
    }


    override fun onAttachFragment(fragmentManager: FragmentManager, fragment: Fragment) {
        if (fragment.id == R.id.arFragment) {
            arFragment = fragment as ArFragment
            arFragment.setOnSessionConfigurationListener(this)
        }
    }

    override fun onSessionConfiguration(session: Session, config: Config) {
        config.planeFindingMode = Config.PlaneFindingMode.DISABLED
        viewModel.setupAugmentedImagesDB(config, session)

        arFragment.instructionsController = null
        // Check for image detection
        arFragment.setOnAugmentedImageUpdateListener(this::onAugmentedImageTrackingUpdate)
        initializeRecorder()
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private fun onAugmentedImageTrackingUpdate(augmentedImage: AugmentedImage) {
        if (augmentedImage.trackingState == TrackingState.TRACKING
            && augmentedImage.trackingMethod == AugmentedImage.TrackingMethod.FULL_TRACKING) {
            if (isModelAdded.not()) {
                viewModel.getVideoForImage(augmentedImage.name)?.let {
                    createArtistArView(arFragment, augmentedImage.createAnchor(augmentedImage.centerPose), augmentedImage)
                    isModelAdded = true
                }
            }
        }
    }

    private fun initializeRecorder() {
        activity?.let {
            val videoRecorder = VideoRecorder(
                arFragment.arSceneView,
                it.filesDir
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
            }
        }
    }

    private fun navigateToPreviewAndEdit(filePath: String) {
        val action = CustomArFragmentDirections.actionArFragmentToPreviewFragment(filePath)
        findNavController().navigate(action)
    }

    private fun createArtistArView(
        arFragment: ArFragment,
        anchor: Anchor,
        image: AugmentedImage
    ) {
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
    }

    private fun openWebView(customTabsIntent: CustomTabsIntent, url: String) {
        try {
            activity?.let {
                customTabsIntent.launchUrl(it, url.toUri())
            }
        } catch (e: Exception) {
            Toast.makeText(activity, "Please try again", Toast.LENGTH_SHORT).show()
        }
    }

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