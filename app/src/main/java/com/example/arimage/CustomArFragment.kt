package com.example.arimage

import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentOnAttachListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.arimage.views.ArtistLinkAdapter
import com.example.arimage.views.indicators.CircularProgressIndicator
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
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject

// TODO ask for permission
@AndroidEntryPoint
class CustomArFragment: Fragment(),
    FragmentOnAttachListener,
    BaseArFragment.OnSessionConfigurationListener {

    private val TAG = CustomArFragment::class.java.simpleName

    private val viewModel by viewModels<CustomArtViewModel>()

    private var isModelAdded = false
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var arFragment: ArFragment
    @Inject lateinit var artistLinkAdapter: ArtistLinkAdapter

    private lateinit var progressIndicator: CircularProgressIndicator
    private lateinit var recordButton: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arFragment = ArFragment()
        childFragmentManager.addFragmentOnAttachListener(this)
        childFragmentManager.beginTransaction().apply {
            add(R.id.arFragment, ArFragment())
            commit()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.custom_ar_fragment, container, false)
        progressIndicator = view.findViewById(R.id.progress_indicator)
        recordButton = view.findViewById(R.id.record)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.artistLinks.observe(viewLifecycleOwner) { artistLinkAdapter.submitList(it) }
        viewModel.openWebIntent.observe(viewLifecycleOwner) { openWebView(it.first, it.second) }
        viewModel.isInitialLoading.observe(viewLifecycleOwner) { progressIndicator.isVisible = it }
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
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { arFragment.setSessionConfig(it, true) },
                { Log.d(TAG, "Failed to create config") }
            )

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
                    createArtistArView(arFragment, augmentedImage, it)
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
        recordButton.setOnClickListener {
            toggleRecording(videoRecorder, recordButton)
        }
    }

    // TODO this should be mvvm driven
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

    private fun resetModel() {
        isModelAdded = false
    }

    private fun navigateToPreviewAndEdit(filePath: String) {
        resetModel()
        val action = CustomArFragmentDirections.actionArFragmentToPreviewFragment(filePath)
        findNavController().navigate(action)
    }

    private fun createArtistArView(
        arFragment: ArFragment,
        augmentedImage: AugmentedImage,
        videoRes: Int
    ) {
        val anchorNode = AnchorNode(augmentedImage.createAnchor(augmentedImage.centerPose))
        mediaPlayer = initializeMediaPlayer(videoRes)

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
            videoNode.localRotation = flattenViewOnImage()
            videoNode.localScale = getScale(augmentedImage, mediaPlayer.videoHeight, mediaPlayer.videoWidth)
            videoNode.localPosition = Vector3(0f, NodeConfig.videoZPostion, augmentedImage.extentZ/2)
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
                node.localRotation = flattenViewOnImage()
                node.localPosition = Vector3(0f, 0f, augmentedImage.extentZ)
                node.scaleController.minScale = NodeConfig.viewMinScale
                node.scaleController.maxScale = NodeConfig.viewMaxScale
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
        val newVideoHeight = image.extentZ * 2
        val newVideoWidth = image.extentZ  * (videoWidth.toFloat()/videoHeight.toFloat())

        return Vector3(newVideoWidth, newVideoHeight, 1f)
    }

    private fun initializeMediaPlayer(videoRes: Int): MediaPlayer =
        MediaPlayer.create(activity, videoRes).also {
            it.isLooping = true
        }

    private fun flattenViewOnImage(): Quaternion = Quaternion(Vector3(1f, 0f, 0f), NodeConfig.flattenNodeOnImageRotation)
}